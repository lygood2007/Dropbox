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
 * Description: Handle the client connected, basically about network issue.
 */
class DropboxFileServerClientHandler implements Runnable{
	
	private Socket _sock;
	
	private SyncStreamParser _sp;
	private DropboxFileManager _fm;
	private SyncStreamWriter _sw;
	private DropboxFileServer _server;
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[DropboxFileServerClientHandler (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		if(!_server.noException())
			System.err.println("[DropboxFileServerClientHandler (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxFileServerClientHandler]:" + str);
	}
	
	public DropboxFileServerClientHandler(Socket sock,DropboxFileServer server){
		_sock = sock;
		_server = server;
		assert _server != null;
		try{
			_fm = new DropboxFileManager(_server.disk(), _server.debugMode());
			_sp = new SyncStreamParser(_fm.getHome(),new DataInputStream(_sock.getInputStream()), _server.debugMode());
			_sw = new SyncStreamWriter(_fm.getHome(),new DataOutputStream(_sock.getOutputStream()), _server.debugMode());
			
		}catch(IOException e){
			_elog("IO error occurs when you get input stream from socket");
			if(_server.debugMode())
				e.printStackTrace();
		}
	}
	
	@Override
	public void run(){
		if(_sock != null && _sock.isConnected() ){
			while(true){
				if(!_sock.isConnected()||_sock.isClosed())
					break;
				
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
						_elog("Error occurs when writing data header");
						if(_server.debugMode())
							e.printStackTrace();
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
						_elog("Close socket");
						if(_server.debugMode())
							e.printStackTrace();
					}
				}
			}
		}
		_dlog("Finish sync");
	}
	
	protected void dispatchFileMap(){
		// dispatch the file map into client
		
    	_dlog("Now syncing your home to client");
    	try{
    		_sw.writePackageHeader(ProtocolConstants.PACK_DATA_HEAD);
    	}catch(IOException e){
    		_dlog("Error occurs when writing data header");
    		if(_server.debugMode())
    			e.printStackTrace();
    	}
    	_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
	}
	
	protected void dispatchEmptyHeader(){
		try{
			_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
		}catch(IOException e){
			_elog("Error occurs when dispatching null header");
			if(_server.debugMode())
				e.printStackTrace();
		}
	}

	// Getters
	public Socket getSocket(){
		return _sock;
	}
}