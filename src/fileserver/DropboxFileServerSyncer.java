package fileserver;

import java.io.*;
import java.net.*;
import java.util.*;

import utils.*;
import common.*;

/**
 * 
 * 
 * Class: DropboxFileCliendHandler
 * Description: Handle the client connected, do SYNCING!
 *              CAUTION: this class should be paid more attention.
 */
class DropboxFileServerSyncer extends ThreadBase{
	
	private Socket _sock;
	private SyncStreamParser _sp;
	private DropboxFileManager _fm;
	private SyncStreamWriter _sw;
	private DropboxFileServer _server;
	private boolean _alive;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[DropboxFileServerSyncer (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	private static void _elog(String str){
		System.err.println("[DropboxFileServerSyncer (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	private static void _log(String str){
		System.out.println("[DropboxFileServerSyncer]:" + str);
	}
	
	public DropboxFileServerSyncer(Socket sock,DropboxFileServer server, String dir){
		super("DropboxFileServerSyncer", server.debugMode());
		_sock = sock;
		_server = server;
		_alive = true;
		assert _server != null;
		try{
			_fm = new DropboxFileManager(dir, _server.debugMode());
			_sp = new SyncStreamParser(_fm.getHome(),new DataInputStream(_sock.getInputStream()), _server.debugMode());
			_sw = new SyncStreamWriter(_fm.getHome(),new DataOutputStream(_sock.getOutputStream()), _server.debugMode());
			
		}catch(IOException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode())
				e.printStackTrace();
			stop();
		}
	}
	
	public void sync() throws IOException {
		int	packHead = _sp.parse();
		if(packHead == ProtocolConstants.PACK_DATA_HEAD){
			HashMap<String, FileOperation> fileMap = _sp.parseFileMap();
			_fm.receiveFileMap(fileMap);
			if(_server.debugMode())
				_fm.printReceivedFileMap();

			_fm.processReceivedFileMap();
			_dlog("Send an empty header");
			try{
				_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
			}catch(IOException e){
				throw e;
			}
		}else if(packHead == ProtocolConstants.PACK_QUERY_HEAD){

			_dlog("I got query");
			_dlog("The directoy content of your home is:");
			_fm.buildFileMap();
			_fm.printFileMap();
			dispatchFileMap();
		}else if(packHead == ProtocolConstants.PACK_NULL_HEAD){		
			_dlog("The directoy content of your home is:");
			_fm.buildFileMap();
			_fm.printFileMap();
			if(!_fm.checkDiff()){
				_dlog("Now sync the home from server to client");
				dispatchFileMap();
			}else
			{
				dispatchEmptyHeader();
			}
		}else if(packHead == ProtocolConstants.PACK_INVALID_HEAD){
			try{
				_sock.close();
			}catch(IOException e){
				throw e;
			}
		}
	}
	@Override
	public void run(){
		Thread thisThread = Thread.currentThread();
		try{
			while(_sock != null && !_sock.isClosed() &&thisThread == _t){ // Cancel point
				// Suspension point
				synchronized(this) {
					while(_suspended) { // Suspension point
						wait();
					}
				}
				sync();
			}
		}catch(InterruptedException e){ // Cancel Point
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		catch(IOException e){ // Cancel point
			if(!_server.noException())
				_elog(e.toString());
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		clear();
		_log(_threadName + " is stopped");
	}
	
	protected void dispatchFileMap() throws IOException {
		// dispatch the file map into client
		
    	_dlog("Now syncing your home to client");
    	try{
    		_sw.writePackageHeader(ProtocolConstants.PACK_DATA_HEAD);
    	}catch(IOException e){
    		throw e;
    	}
    	_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
	}
	
	protected void dispatchEmptyHeader() throws IOException{
		try{
			_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
		}catch(IOException e){
			throw e;
		}
	}

	// Getters
	public Socket getSocket(){
		return _sock;
	}

	protected void clear() {
		assert _alive == true;
		_dlog("Do clear...");
		try{
			if(!_sock.isClosed())
				_sock.close();
			
			//close the stream
			_sp.closeStream();
			_sp = null;
			_sw.closeStream();
			_sw = null;
			_fm = null;
		}catch(IOException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		_dlog("Finished");
		_alive = false;
	}
	
	@Override
	public void stop(){
		if(_suspended == true){
			_elog("Cannot stop when suspending");
			return;
		}
		/* Interrupt the thread*/
		if(_t != null){
			_t.interrupt();
		}
		_t = null;
	}
	
	public boolean isAlive(){
		return _alive;
	}
}