package fileserver;

import java.io.*;
import java.net.*;
/**
 * 
 * Class: DropboxFileServerClientNet
 * Description: Listen to new client connected
 */
class DropboxFileServerClientNet{

	
	private ServerSocket _serverSocket;
	private DropboxFileServer _server;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[DropboxFileServerClientNet (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[DropboxFileServerClientNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxFileServerClientNet]:" + str);
	}

	public DropboxFileServerClientNet(DropboxFileServer server){
		_server = server;
		assert _server != null;
	}
	
	public void listen(){
		_log("listening to new client...");
    	Socket client = null;
    	try{
    		_serverSocket = new ServerSocket(_server.listenPort());
    		_serverSocket.setSoTimeout(1000*100);
    		while(true)
    		{
    			client = _serverSocket.accept();
    			_log("Get connection from " + client.getInetAddress().getHostAddress());
    			//TODO: here, it should control how many threads we could accept at most
    			//TEST
    			//TODO: add the thread pool
    			Thread t;
    			
    			// TODO: if the client is firstly connected, should make a new directory for him
    			t = new Thread(new DropboxFileServerSyncer(client, _server));
    			t.start();
    		}
    	}catch(InterruptedIOException e){
    		if(!_server.noException()){
				_elog(e.toString());
			}
    		if(_server.debugMode()){
    			e.printStackTrace();
    		}
    		
    	}catch(IOException e){
    		if(!_server.noException()){
				_elog(e.toString());
			}
    		if(_server.debugMode()){
    			e.printStackTrace();
    		}
    	}finally{
    		try{
    			_log("Close connection from " + client.getInetAddress().getHostAddress());
    			if(_serverSocket != null )
    				_serverSocket.close();
    			if( client != null )
    				client.close();
    		}catch(IOException e){
    			if(!_server.noException()){
    				_elog(e.toString());
    			}
        		if(_server.debugMode()){
        			e.printStackTrace();
        		}
    		}
    	}
	}
	
}
