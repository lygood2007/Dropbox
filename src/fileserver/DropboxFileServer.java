package fileserver;

import common.*;

import java.net.*;
import java.io.*;
//import java.util.*;
//import utils.*;
/**
 * Package: server
 * Class: DropboxServer
 * Description: The server for syncing files, here we simulate the behavior of data storage.
 */
public class DropboxFileServer {

	private static int _port;
	private static boolean _debug; 
	private static boolean _useUI;
	private static String _disk; // The location of the disk, is indeed the directory. 
	                             // Just simulate.
	private static DropboxFileServerNet _serverNet;
	
	private static void _log(String str){
		System.out.println("DropboxFileServer:"+str);
	}
	
    public DropboxFileServer(int port, boolean debug, boolean useUI, String disk){
    	_port = port;
    	_debug = debug;
    	_useUI = useUI;
    	_disk = disk;
    	
    	initDisk(_disk);
    	initNet(_port, _debug, _disk);
    	printStatus();
    }
    
    public void initDisk(String disk){
    	File theDir = new File(disk);
    	// if the disk does not exist, create it
    	if (!theDir.exists()) {
    		_log("Creating directory: " +disk);
    		boolean result = theDir.mkdir();  

    		if(result) {    
    			_log(disk + " created.");  
    		}
    		else{
    			_log("Cannot initialize the directory");
    			System.exit(1);
    		}
    	}
    	
    	if(_debug)
    		_log("Home: " + _disk);
    }
    
    public void initNet(int port, boolean debug, String disk){
    	_serverNet = new DropboxFileServerNet(port, debug, disk);
    }
    
    public void listen(){
    	assert _serverNet != null;
    	// Dead loop here
    	_serverNet.listen();
    }
    
    public void printStatus(){
    	System.out.println("Dropbox File Server configuration:");
    	System.out.println("Debug:" + Boolean.toString(_debug));
    	System.out.println("UseUI:" + Boolean.toString(_useUI));
    	System.out.println("Root disk:" + _disk);
    	System.out.println("Listen port:" + Integer.toString(_port));
    	System.out.println();
    }
    
    public static void usage(){
    	System.out.println("Dropbox File Server:");
    	System.out.println("-d for debug mode (default false)" );
    	System.out.println("-u to use user interface (default false)");
    	System.out.println("-p to specify port (default 5000)");
    	System.out.println("-disk root your server is located"
    			+ " (default using cwd/ServerRoot, where cwd is your current working directory)");
    }
    
    public static void main(String[] args) {
    	
    	_port = DropboxConstants.FILE_SERVER_PORT;
    	_debug = false;
    	_useUI = false;	
    	_disk = DropboxConstants.DROPBOX_SERVER_ROOT;
    	
    	//usage();
    
    	for( int i = 0; i < args.length; i++ ){
    		if(args[i].equals("-d"))
				_debug = true;
			else if(args[i].equals("-u"))
				_useUI = true;
			else if(args[i].equals("-p")){
				i++;
				_port = Integer.parseInt(args[i]);
			}
			else if(args[i].equals("-h")){
				i++;
				_disk = args[i];
			}
    	}
    	
    	DropboxFileServer server = new DropboxFileServer(_port,_debug,_useUI,_disk);
    	server.listen();
    }
}
