package client;

import common.DropboxConstants;
import java.io.*;

/**
 * Package: csci1310.client
 * Class: DropboxClient
 * Description:
 */
public class DropboxClient implements FileSynchronizationClient, Runnable {

	private boolean _debug;
	private boolean _useUI;
	
	private DropboxClientStreamWriter _sw;
	private DropboxClientNet _cn;
	private DropboxClientFileManager _fm;
	
	private static void log(String s){
		System.err.println("Dropbox Client | " + s);
	}
	/**
	 * Implement run()
	 */
    public void run() {
    	try{
    		_cn.connect();
    		// Chain the stream to low-level stream
    		_sw = new DropboxClientStreamWriter(_debug,_cn.getSocket(), _fm);
    		sync();
    		Thread.sleep(DropboxConstants.SYNC_SLEEP_MILLIS);
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

    	System.out.println("The directoy content of your home is:");
    	//_fm.showDir();
    	_fm.printFileMap();
    	// Sync once
    	_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
        return false;
    }

    
    /**
     * Default constructor
     */
    public DropboxClient(){
    	_debug = false;
    	_useUI = false;
    	_cn = new DropboxClientNet();
    	_fm = new DropboxClientFileManager(DropboxConstants.DROPBOX_TEST_DIRECTORY);
    }
    
    /**
     * Constructor 
     * @param ip: IP address
     * @param port: port number
     * @param debug: in debug mode?
     * @param useUI: use user interface?
     */
    public DropboxClient( String ip, int port, boolean debug, boolean useUI, String home ){
    	_debug = debug;
    	_useUI = useUI;
    	_cn = new DropboxClientNet(ip, port, debug);
    	_fm = new DropboxClientFileManager(home);
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
