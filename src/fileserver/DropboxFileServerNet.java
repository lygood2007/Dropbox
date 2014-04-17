package fileserver;

import java.io.*;
import java.net.*;

/**
 * 
 * Package: server
 * Class: DropboxFileServerNet
 * Description: Used for handling the network request from client.
 */
/**
 * TODO: should add the network interface for the connection to master node
 * 
 */
public class DropboxFileServerNet implements FileSynchronizationServer {

	private int _port;
	private boolean _debug;
	private Thread _thread;
	private String _home;
	
	public DropboxFileServerNet(int port, boolean debug, String home){
		_thread = null;
		_debug = debug;
		_port = port;
		_home = home;
	}
	
	public void listen(){
		if(_debug)
		{
			System.out.println("DropboxFileServerNet | listen(){ ");
		}
		System.out.println("Server is listening...");
    	// Now only support one client
    	Socket client = null;
    	ServerSocket serverSocket = null;
    	try{
    		serverSocket = new ServerSocket(_port);
    		serverSocket.setSoTimeout(1000*100);
    		while(true)
    		{
    			client = serverSocket.accept();
    			System.out.println("Get connection from " + client.getInetAddress().getHostAddress());
    			_thread = new Thread(new DropboxFileServerClientHandler(client, _debug, _home));
    			_thread.start();
    		}
    	}catch(InterruptedIOException e){
    		System.err.println("Time out");
    		if(_debug)
    			e.printStackTrace();
    		
    	}catch(IOException e){
    		System.err.println("IO error occurs");
    		if(_debug)
    			e.printStackTrace();
    	}finally{
    		try{
    			System.out.println("Close connection from " + client.getInetAddress().getHostAddress());
    			if(serverSocket != null )
    				serverSocket.close();
    			if( client != null )
    				client.close();
    		}catch(IOException e){
    			System.err.println("IO error occurs when closing socket");
        		if(_debug)
        			e.printStackTrace();
    		}
    		if(_debug)
    		{
    			System.out.println("} ");
    		}
    	}
	}
}
