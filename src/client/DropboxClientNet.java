package client;

import java.io.*;
import java.net.*;

import common.DropboxConstants;

/**
 * Class: DropboxClientNet
 * Description: Deal with the network connecting issues
 */
public class DropboxClientNet {

	private String _ip;
	private int _port;
	private Socket _sock;
	private boolean _debug;
	
	/**
	 * Connect: connect the socket to server
	 */
	public void connect(){
		try{
			_sock = new Socket(_ip, _port);
			System.out.println("Successful connection to " + _ip + ":" + _port);
		}catch(UnknownHostException e){
			System.err.println("Unknown host!");
			if(_debug)
				e.printStackTrace();
		}catch(IOException e){
			System.err.println("Errors occur when creating socket");
			if(_debug)
				e.printStackTrace();
		}
		
	}
	
	/**
	 * Close: close the connection
	 */
	public void close(){
		if(_sock != null){
			try{
				_sock.close();
				System.out.println("Close connection to " + _ip + ":" + _port);
			}catch(IOException e){
				System.err.println("IO errors occur when closing socket");
				if(_debug)
					e.printStackTrace();
			}
		}
	}
	
	/**
	 * Constructor
	 */
	public DropboxClientNet(){
		_ip = "127.0.0.1";
		_port = DropboxConstants.SERVER_PORT;
		_sock = null;
		_debug = false;
	}
	/**
	 * Constructor
	 * @param host: The IP address
	 * @param port: The port number
	 */
	public DropboxClientNet(String ip, int port, boolean debug){
		_ip = ip;
		_port = port;
		_sock = null;
		_debug = debug;
	}
	
	/**
	 * Getters
	 */
	public String getIP(){
		return _ip;
	}
	
	public int getPort(){
		return _port;
	}
	
	public Socket getSocket(){
		return _sock;
	}
}
