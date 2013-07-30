package client;

import common.DropboxConstants;
import common.FileOperation;
import common.ProtocolConstants;

import java.io.*;
import java.util.HashMap;

import utils.DropboxFileManager;
import utils.DropboxStreamWriter;
import utils.DropboxStreamParser;

/**
 * Package: client
 * Class: DropboxClient
 * Description:
 */
public class DropboxClient implements FileSynchronizationClient, Runnable {

	private boolean _debug;
	private boolean _useUI;
	
	private DropboxStreamWriter _sw;
	private DropboxStreamParser _sp;
	private DropboxClientNet _cn;
	private DropboxFileManager _fm;
	
	private static void log(String s){
		System.err.println("Dropbox Client | " + s);
	}
	/**
	 * Implement run()
	 */
    public void run() {
    	try{
    		// Keep trying to connect
    		while(true){
    			_cn.connect();
    			if(_cn.getSocket().isConnected())
    			{
    				_sw = new DropboxStreamWriter(_fm.getHome(),new DataOutputStream(_cn.getSocket().getOutputStream()),_debug);
    				_sp = new DropboxStreamParser(_fm.getHome(),new DataInputStream(_cn.getSocket().getInputStream()),_debug);
    				// The first time we connect we write a query package to get the files from server 
    				System.out.println("Sent a package to server to query");
    				_sw.writePackageHeader(ProtocolConstants.PACK_QUERY_HEAD);
    				Thread.sleep(DropboxConstants.SYNC_SLEEP_MILLIS);
    				break;
    			}
    		}
    			// We get a connection, then sync
    		while(_cn != null && _cn.getSocket().isConnected()){
    			sync();
    			Thread.sleep(DropboxConstants.SYNC_SLEEP_MILLIS);
    		}
    		
    	}catch(InterruptedException e){
    		System.err.println("Thread sleep error");
    		if( _debug )
    			e.printStackTrace();
    	}catch(IOException e){
    		System.err.println("Write stream error");
    		if( _debug )
    			e.printStackTrace();
    	}
    	finally{
    		_cn.close();
    	}
    }

    /**
     * Implement sync()
     */
    public boolean sync() throws IOException {

    	
    	//_fm.showDir();
    	
    	// Sync once
    	// Firstly read to see if there is file map from server, if yes, then we sync from server
    	int packHead = _sp.parse();
		if(packHead == ProtocolConstants.PACK_DATA_HEAD){
			System.out.println("I got data stream, now syncing");
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
			
		}else if(packHead == ProtocolConstants.PACK_NULL_HEAD)
		{
			System.out.println("The directoy content of your home is:");
			_fm.buildFileMap();
			_fm.printFileMap();
			if(!_fm.checkDiff())
			{
				//System.out.println("Check diff: " + _fm.checkDiff());
			    System.out.println("Your home is updated");
				System.out.println("Now syncing your home to server");
				try{
					_sw.writePackageHeader(ProtocolConstants.PACK_DATA_HEAD);
				}catch(IOException e){
					System.err.println("Error occurs when writing data header");
					if(_debug)
						e.printStackTrace();
				}
				_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
			}
			else{
				System.out.println("Send an empty header");
				// Else we just send an empty header
				try{
					_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
				}catch(IOException e){
					System.err.println("Error occurs when writing data header");
					if(_debug)
						e.printStackTrace();
				}
			}
		}
        return false;
    }

    
    /**
     * Default constructor
     */
    public DropboxClient(){
    	_debug = false;
    	_useUI = false;
    	_cn = new DropboxClientNet();
    	_fm = new DropboxFileManager(DropboxConstants.DROPBOX_TEST_DIRECTORY, _debug);
    }
    
    /**
     * Constructor 
     * @param ip: IP address
     * @param port: port number
     * @param debug: in debug mode?
     * @param useUI: use user interface?
     */
    public DropboxClient( String ip, int port, boolean debug, boolean useUI, String home){
    	_debug = debug;
    	_useUI = useUI;
    	_cn = new DropboxClientNet(ip, port, debug);
    	_fm = new DropboxFileManager(home,_debug);
    }
    
    /**
     * Entry of program
     * @param args argument list
     */
    public static void main(String[] args) {
    	
    	boolean debug = false;
    	boolean useUI = false;
    	String ip = "127.0.0.1";
    	int port = DropboxConstants.SERVER_PORT;
    	String home = DropboxConstants.DROPBOX_TEST_DIRECTORY;
    	
    	// Show help:
    	System.out.println("Dropbox Client:");
    	System.out.println("-d for debug mode (default false)" );
    	System.out.println("-u to use user interface (default false)");
    	System.out.println("-addr following the IP address and port number"
    			+ " (default using 127.0.0.1:5000)");
    	System.out.println("-home following the home folder you want to sync"
    			+ " (default using /tmp/DropboxTest)");
    	
    	// parse the arguments
    	try{
    		System.out.println("Processing cmd...");
    		for( int i = 0; i < args.length; i++ ){
    			if(args[i].equals("-d"))
    				debug = true;
    			else if(args[i].equals("-u"))
    				useUI = true;
    			else if(args[i].equals("-addr")){
    				i++;
    				int tokenPos = args[i].indexOf(':');
    				ip = args[i].substring(0, tokenPos);
    				port = Integer.parseInt(args[i].substring(tokenPos+1));
    			}
    			else if(args[i].equals("-home")){
    				i++;
    				home = args[i];
    			}
    		}
    	}catch(Exception e){
    		log("Processing cmd failed");
    		System.exit(1);
    	}
    	
    	// Show the settings we get
    	System.out.println("Client setting:");
    	System.out.println("Debug mode? | " + debug );
    	System.out.println("Use UI? | " + useUI );
    	System.out.println("Server address | " + ip + ":" + port );
    	System.out.println("Home directory | " + home);
    	
    	// New a thread for client
    	Thread t = new Thread(new DropboxClient(ip, port, debug, useUI,home), "DropboxClient");
    	t.start();
    }
}
