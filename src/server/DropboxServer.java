package server;

import common.DropboxConstants;
import common.ProtocolConstants;
import java.net.*;
import java.io.*;
import java.util.*;

class DropboxClientHandler implements Runnable{
	
	Socket _sock;
	DataInputStream _is;
	
	public DropboxClientHandler(Socket sock){
		_sock = sock;
		try{
			_is = new DataInputStream(_sock.getInputStream());
		}catch(IOException e){
			System.out.println("IO error");
		}
	}
	
	// Parse the stream got from socket
	public void streamParser() throws IOException{
		if(_is != null){
			
			int packBegin = _is.readInt();
			if(packBegin == ProtocolConstants.PACK_BEGIN){
				// Valid stream
				int fileNum = _is.readInt();
				System.out.println(fileNum);
				for( int i = 0; i < fileNum; i++ ){
					// Read each file
					readEach(_is);
				}
			}
		}
			
	}
	
	public void run(){
		if( _is != null && _sock != null ){
			try{
				streamParser();
			}catch(IOException e){
				System.out.println("Read error");
			}
		}
	}
	
	public void readEach(DataInputStream is) throws IOException {
		if(is != null){
			int nameLength = is.readInt();
			
			byte []nameBytes = new byte[nameLength];
			is.read(nameBytes);
			String fileName = new String(nameBytes);
		
			byte operation = is.readByte();
			boolean flag = is.readBoolean();
			int fileLength = 0;
			byte []fileBytes = null;
			/*if(flag == false){
				fileLength = is.readInt();
				fileBytes = new byte[fileLength];
				is.read(fileBytes);
			}
			*/
				// Print the data we get
			System.out.println("Filename: "+fileName + "(" + flag +")" + " Operation: " + operation);
			/*if(fileBytes != null){
				String fileContent = new String(fileBytes);
				System.out.println(fileContent);
			}*/
		}
	}

	public void readFileName(DataInputStream is) throws IOException {
		
	}
	// Getters
	public Socket getSocket(){
		return _sock;
	}
	
	public DataInputStream getStream(){
		return _is;
	}
}
/**
 * Package: csci1310.server
 * Class: DropboxServer
 * Description:
 */
public class DropboxServer implements FileSynchronizationServer {

	private int _port;
	private boolean _debug;
	private boolean _useUI;
	
    public void listen() {
    	System.out.println("Server is listening...");
    	// Now only support one client
    	Socket client = null;
    	DataInputStream is  = null;
    	ServerSocket serverSocket = null;
    	// Close the server in 5 seconds
    	//long startTime = System.currentTimeMillis();
    	//long elapsedTime = 0;
    	try{
    		serverSocket = new ServerSocket(_port);
    		serverSocket.setSoTimeout(100*1000);
    		while(true){
    			//elapsedTime = (new Date()).getTime() - startTime;
    			
    			client = serverSocket.accept();
    			System.out.println("Get connection from" + client.getInetAddress().getHostAddress());
    			Thread t = new Thread(new DropboxClientHandler(client));
    			t.start();
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
    		
    	}
    }

    public DropboxServer(){
    	_port = DropboxConstants.SERVER_PORT;
    	_debug = false;
    	_useUI = false;
    }
    
    public DropboxServer( int port, boolean debug, boolean useUI ){
    	_port = port;
    	_debug = debug;
    	_useUI = useUI;
    }
    
    public static void main(String[] args) {
    	
    	int port = DropboxConstants.SERVER_PORT;
    	boolean debug = false;
    	boolean useUI = false;	
    	
    	System.out.println("Dropbox Server:");
    	System.out.println("-d for debug mode (default false)" );
    	System.out.println("-u to use user interface (default false)");
    	System.out.println("-p to specify port (default 5000)");
    	
    	for( int i = 0; i < args.length; i++ ){
    		if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-u"))
				useUI = true;
			else if(args[i].equals("-p")){
				i++;
				port = Integer.parseInt(args[i]);
			}
    	}
    	DropboxServer server = new DropboxServer(port,debug,useUI);
    	server.listen();
    }
}
