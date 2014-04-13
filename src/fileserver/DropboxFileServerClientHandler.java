package fileserver;

import java.io.*;
import java.net.*;
import java.util.*;
import utils.*;
import common.*;

/**
 * 
 * Package: server
 * Class: DropboxFileCliendHandler
 * Description: Handle the client connected, basically about network issue.
 */
class DropboxFileServerClientHandler implements Runnable{
	
	private Socket _sock;
	
	private boolean _debug;
	
	DropboxStreamParser _sp;
	DropboxFileManager _fm;
	DropboxStreamWriter _sw;
	
	public DropboxFileServerClientHandler(Socket sock, boolean debug, String home){
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
				if(!_sock.isConnected()||_sock.isClosed())
					break;
				
				int	packHead = _sp.parse();
				if(packHead == ProtocolConstants.PACK_DATA_HEAD){
					HashMap<String, FileOperation> fileMap = _sp.parseFileMap();
					_fm.receiveFileMap(fileMap);
					//if(_debug)
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
				}else if(packHead == ProtocolConstants.PACK_INVALID_HEAD){
					try{
					_sock.close();
					}catch(IOException e){
						System.out.println("Close socket");
						if(_debug)
							e.printStackTrace();
					}
				}
			}
		}
		System.out.println("Finish sync");
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