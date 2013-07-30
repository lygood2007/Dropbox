package server;

import common.*;

import java.net.*;
import java.io.*;
import java.util.*;
import utils.*;
/**
 * 
 * Package: server
 * Class: DropboxCliendHandler
 * Description: Handle the client
 */
class DropboxClientHandler implements Runnable{
	
	private Socket _sock;
	
	private boolean _debug;
	
	DropboxStreamParser _sp;
	DropboxFileManager _fm;
	DropboxStreamWriter _sw;
	
	public DropboxClientHandler(Socket sock, boolean debug, String home){
		_sock = sock;
		_debug = debug;
		try{
			_fm = new DropboxFileManager(home, _debug);
			_sp = new DropboxStreamParser(_fm.getHome(),new DataInputStream(_sock.getInputStream()), _debug);
			_sw = new DropboxStreamWriter(_fm.getHome(),new DataOutputStream(_sock.getOutputStream()), _debug);
			
		}catch(IOException e){
			System.err.println("IO error occurs when you get input stream from socket");
			if(_debug)
				e.printStackTrace();
		}
	}
	
	public void run(){
		if(_sock != null && _sock.isConnected() ){
			while(true){
				if(!_sock.isConnected())
					break;
				int	packHead = _sp.parse();
				if(packHead == ProtocolConstants.PACK_DATA_HEAD){
					HashMap<String, FileOperation> fileMap = _sp.parseFileMap();
					_fm.receiveFileMap(fileMap);
					if(_debug)
						_fm.printReceivedFileMap();
					
					_fm.processReceivedFileMap();
					
					System.out.println("Send an empty header");
					try{
						_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
					}catch(IOException e){
						System.err.println("Error occurs when writing data header");
						if(_debug)
							e.printStackTrace();
					}
				}else if(packHead == ProtocolConstants.PACK_QUERY_HEAD){

					System.out.println("I got query");
					System.out.println("The directoy content of your home is:");
					_fm.buildFileMap();
					_fm.printFileMap();
					dispatchFileMap();
				}else if(packHead == ProtocolConstants.PACK_NULL_HEAD){		
					System.out.println("The directoy content of your home is:");
					_fm.buildFileMap();
					_fm.printFileMap();
					if(!_fm.checkDiff()){
						System.out.println("Now sync the home from server to client");
						dispatchFileMap();
					}else
					{
						dispatchEmptyHeader();
					}
				}
			}
		}
	}
	
	public void dispatchFileMap(){
		// dispatch the file map into client
		
    	System.out.println("Now syncing your home to client");
    	try{
    		_sw.writePackageHeader(ProtocolConstants.PACK_DATA_HEAD);
    	}catch(IOException e){
    		System.err.println("Error occurs when writing data header");
    		if(_debug)
    			e.printStackTrace();
    	}
    	_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
	}
	
	public void dispatchEmptyHeader(){
		try{
			_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
		}catch(IOException e){
			System.err.print("Error occurs when dispatching null header");
			if(_debug)
				e.printStackTrace();
		}
	}

	// Getters
	public Socket getSocket(){
		return _sock;
	}
}
/**
 * Package: server
 * Class: DropboxServer
 * Description: Deal with everything!
 */
public class DropboxServer implements FileSynchronizationServer {

	private int _port;
	private boolean _debug;
	private boolean _useUI;
	private String _home;
	
    public void listen() {
    	System.out.println("Server is listening...");
    	// Now only support one client
    	Socket client = null;
    	ServerSocket serverSocket = null;
    	try{
    		serverSocket = new ServerSocket(_port);
    		serverSocket.setSoTimeout(100*1000);
    		client = serverSocket.accept();
    		System.out.println("Get connection from " + client.getInetAddress().getHostAddress());
    		Thread t = new Thread(new DropboxClientHandler(client, _debug, _home));
    		t.start();
    		client = serverSocket.accept();
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
    		
    	}
    }

    public DropboxServer(){
    	_port = DropboxConstants.SERVER_PORT;
    	_debug = false;
    	_useUI = false;
    	_home = DropboxConstants.DROPBOX_DIRECTORY;
    }
    
    public DropboxServer(int port, boolean debug, boolean useUI, String home){
    	_port = port;
    	_debug = debug;
    	_useUI = useUI;
    	_home = home;
    }
    
    public static void main(String[] args) {
    	
    	int port = DropboxConstants.SERVER_PORT;
    	boolean debug = false;
    	boolean useUI = false;	
    	String home = DropboxConstants.DROPBOX_DIRECTORY;
    	
    	System.out.println("Dropbox Server:");
    	System.out.println("-d for debug mode (default false)" );
    	System.out.println("-u to use user interface (default false)");
    	System.out.println("-p to specify port (default 5000)");
    	System.out.println("-home following the home folder your server is located"
    			+ " (default using /tmp/Dropbox)");
    	
    	for( int i = 0; i < args.length; i++ ){
    		if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-u"))
				useUI = true;
			else if(args[i].equals("-p")){
				i++;
				port = Integer.parseInt(args[i]);
			}
			else if(args[i].equals("-h")){
				i++;
				home = args[i];
			}
    	}
    	DropboxServer server = new DropboxServer(port,debug,useUI,home);
    	System.err.println("Home: " + home);
    	server.listen();
    }
}
