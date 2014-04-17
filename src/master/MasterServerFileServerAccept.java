package master;

import java.net.*;
import java.io.*;

import common.*;

/**
 * 
 * Class MasterServerFileServerHandler
 * Description: Handles the new connected file server
 *              Mainly accept the message from file server
 */
class MasterServerFileServerAccept extends ThreadBase{
	private Socket _sock;
	private BufferedReader _in;
	private PrintWriter _out;
	private MasterServer _server;
	private volatile FileServerNode _node;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerFileServerAccept (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		if(!_server.noException())
			System.err.println("[MasterServerFileServerAccept (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerFileServerAccept]:" + str);
	}
	
	public MasterServerFileServerAccept(MasterServer server, PrintWriter out, 
				BufferedReader in, Socket sock, FileServerNode node){
		super("MasterServerFileServerAccept", server.debugMode());
		_server = server;
		assert _server != null;
		_sock = sock;
		_in = in;
		_out = out;
		_node = node;
	}
	
	@Override
	public void run(){
		Thread thisThread = Thread.currentThread();
		try{
			while(thisThread == _t){ // Cancel point
				synchronized (this){
					while(_suspended){ // Suspension point
						wait();
					}
				}
				String message = NetComm.receive(_in);
				parse(message);
			}
		}catch(Exception e){
			_elog(e.toString());
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		clear();
		_log(_threadName + " is stopped");
	}
	
	protected void parse(String str){
		if(str.equals(ProtocolConstants.PACK_STR_CLOSE_HEAD)){
			_dlog("I got a close package, now close");
			
			_node.destroy(); // Will stop myself too
			// Cancel the other thread
		}else{
			_elog("Invalid message");
		}
	}
	
	public Socket getSocket(){
		return _sock;
	}
	
	/* Will also stop the current thread */
	protected void clear(){
		_dlog("Do clear..");
		try{
			if(!_sock.isClosed())
				_sock.close();
			/* Close stream */
			_in.close();
			_out.close();

			/* Set to null */
			_sock = null;
			_in = null;
			_out = null;

		}catch(IOException e){
			_elog(e.toString());
			if(_server.debugMode())
				e.printStackTrace();
		}
		if(_node != null && _node.isAlive())
			_node.destroy();
		_node = null;
		_dlog("Finished");
	}
}
