/**
 * File: DropboxClient.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package client;

import common.*;

import java.io.*;
import java.util.*;
import utils.*;

/**
 * Class: DropboxClient
 * Description: The client side wrapper, responsible for everything in the client side
 */
public class DropboxClient {

	/* Setting switch */
	private boolean _debug;
	private boolean _hideException;
	private boolean _useUI;
	
	/* Components */
	private SyncStreamWriter _sw;
	private SyncStreamParser _sp;
	private DropboxClientNet _net;
	private DropboxFileManager _fm;
	
	/* Identification stuff */
	private String _name;
	private String _root;
	private String _password;
	
	/* Use default settings */
	private String _masterIP = DropboxConstants.MASTER_IP;
	private int _masterPort = DropboxConstants.MASTER_CLIENT_PORT;

	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	private void _dlog(String str){
		if (_debug)
			System.out.println("[DropboxClient (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	private static void _elog(String str){
		System.err.println("[DropboxClient (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	private static void _log(String str){
		System.out.println("[DropboxClient]:" + str);
	}

	/**
	 * connectToMaster: connect to master node
	 * @return: succeed or not
	 */
	private boolean connectToMaster(){
		return _net.openConnections(_masterIP, _masterPort);
	}
	
	/**
	 * closeConnection: close the connection
	 */
	private void closeConnection(){
		_net.closeConnection();
	}

	/**
	 * connectToFileServer: connect to file server
	 * @param ip: the ip of file server
	 * @return: succeed or not
	 */
	private boolean connectToFileServer(String ip){
		_net.closeConnection();
		return _net.openConnections(ip, DropboxConstants.FILE_SERVER_PORT);
	}
	
	/**
	 * run: run the main loop here
	 */
	public void run() {
		while (true){
			if (!connectToMaster()){
				_elog("Aborting");
				return;
			}else{
				_log("Delay the identification for " + DropboxConstants.IDENTIFY_DELAY + " milliseconds");
				try{
					Thread.sleep(DropboxConstants.IDENTIFY_DELAY);
				}catch (InterruptedException e){
					if (!_hideException){
						_elog(e.toString());
					}
					if (_debug){
						e.printStackTrace();
					}
				}
				String fsIP = _net.identify();
				if (fsIP == null){
					
					_elog("Not confirmed by master");
					_elog("Reason could be: wrong account|name; bad package");
					_elog("Aborting");
					closeConnection();
					return;
				}else{
					if (!connectToFileServer(fsIP)){
						_elog("Aborting");
						return;
					}

					// We got the conection, send the identification to file server
					_log("Send verification message to file server");
					try{
						DataOutputStream out = new DataOutputStream(_net.getSocket().getOutputStream());
						DataInputStream in = new DataInputStream(_net.getSocket().getInputStream());
						out.writeInt(ProtocolConstants.PACK_INIT_HEAD);
						out.writeChars(_name);
						out.writeChar('\n');
						out.writeChars(_password);
						out.writeChar('\n');
						int reply = in.readInt();
						if (reply == ProtocolConstants.PACK_CONFIRM_HEAD){
							_log("User account confirmed!");
							prepareSync();
						}else if (reply == ProtocolConstants.PACK_FULL_HEAD){
							_elog("One terminal is already using"); // Not applicable now
							return;
						}else if (reply == ProtocolConstants.PACK_FAIL_HEAD){
							_elog("Your combination of name and password is not correct");
							return;
						}
						while (!_net.getSocket().isClosed()){
							sync();
							_dlog("sync suspended (resume after " + DropboxConstants.SYNC_SLEEP_MILLIS + ")");
							Thread.sleep(DropboxConstants.SYNC_SLEEP_MILLIS);
						}
					}
					catch (InterruptedException e){
						if (!_hideException){
							e.printStackTrace();
						}
						if (_debug){
							e.printStackTrace();
						}
					}
					catch (IOException e){
						if (!_hideException){
							e.printStackTrace();
						}
						if (_debug){
							e.printStackTrace();
						}
					}
					finally{
						closeConnection();
						_elog("The connection to file server is broken, retry connection to master");
					}
				}
			}
		}
	}

	/**
	 * prepareSync: prepare the synchronization (send a query head and then sleep)
	 */
    private void prepareSync() throws IOException, InterruptedException{
    	_sw = new SyncStreamWriter(_fm.getHome(),new DataOutputStream(_net.getSocket().getOutputStream()),_debug);
		_sp = new SyncStreamParser(_fm.getHome(),new DataInputStream(_net.getSocket().getInputStream()),_debug);
		_sw.writePackageHeader(ProtocolConstants.PACK_QUERY_HEAD);
		Thread.sleep(DropboxConstants.SYNC_SLEEP_MILLIS);
    }
    
    /**
     * sync: the synchronization story is here.
     * It basically defines a rule to do parse the stream.
     */
    private void sync() throws IOException {
    	
    	// Sync once
    	// Firstly read to see if there is file map from server, if yes, then we sync from server
    	int packHead = _sp.parse();
		if (packHead == ProtocolConstants.PACK_DATA_HEAD){
			_dlog("I got data stream, now syncing");
			HashMap<String, FileOperation> fileMap = _sp.parseFileMap();
			_fm.receiveFileMap(fileMap);
		    _fm.printReceivedFileMap();
			_fm.processReceivedFileMap();
			
			_dlog("Send an empty header");
			try{
				_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
			}catch (IOException e){
				if (!_hideException){
					_elog(e.toString());
				}
				if (_debug){
					e.printStackTrace();
				}
			}
			
		}else if (packHead == ProtocolConstants.PACK_QUERY_HEAD){
			_dlog("I got query");
			
		}else if (packHead == ProtocolConstants.PACK_NULL_HEAD)
		{
			_dlog("The directoy content of your home is:");
			_fm.buildFileMap();
			_fm.printFileMap();
			if (!_fm.checkDiff())
			{
			    _dlog("Your home is updated");
				_dlog("Now syncing your home to server");
				try{
					_sw.writePackageHeader(ProtocolConstants.PACK_DATA_HEAD);
				}catch (IOException e){
					if (!_hideException){
						_elog(e.toString());
					}
					if (_debug){
						e.printStackTrace();
					}
				}
				_sw.writeFromFileMap(_fm.getFileMap(), _fm.getPrevFileMap());
			}
			else{
				_dlog("Send an empty header");
				// Else we just send an empty header
				try{
					_sw.writePackageHeader(ProtocolConstants.PACK_NULL_HEAD);
				}catch (IOException e){
					if (!_hideException){
						_elog(e.toString());
					}
					if (_debug){
						e.printStackTrace();
					}
				}
			}
		}else if (packHead == ProtocolConstants.PACK_INVALID_HEAD){
			try{
				_net.getSocket().close();
			}catch (IOException e){
				if (!_hideException){
					_elog(e.toString());
				}
				if (_debug){
					e.printStackTrace();
				}
			}
		}
    }

    /**
     * usage: show the usage for the user
     */
    public static void usage(){
    	_log("Dropbox Client:");
    	_log("-d: for debug mode (default false)" );
    	_log("-u: to use user interface (default false)");
    	_log("-he: to hide the exception");
    	_log("-name: the client name");
    	_log("-pass: the client password");
    	_log("You must provide your client name and password");
    	_log("The home folder will automatically be: CWD/YOUR CLIENTNAME"+
    			", where CWD is your current working directory)");
    }
    
    /**
     * printStatus: print the current configuration of the client
     */
    public void printStatus(){
    	_log("**Dropbox Client configuration:");
    	_log("Debug:" + _debug);
    	_log("Use UI:" + _useUI);
    	_log("Hide Exception:" + _hideException);
    	_log("Name:" + _name);
    	_log("Password:" + _password);
    	_log("Port (destination):" +  _masterPort);
    }
    
    /**
     * initHome: Initialize the home directory here.
     *           Also create the dropbox file manager which is important
     * @param name: the name of the home directory
     */
    public void initHome(String name){
    	assert _root != null;
    	_dlog("Initialize disk");
    	String fullpath = _root + System.getProperty("file.separator") + name;
    	_dlog("Client home: " + fullpath);
    	_fm = new DropboxFileManager(fullpath,_debug);
    }
    
    /**
     * initRoot: Initialize the root: initialize the root directory
     *           If it doesn't exist, create it.
     * @param home: the home name
     */
    private void initRoot(String home){
    	_dlog("Initialize disk");
    	File theDir = new File(home);
    	// if the disk does not exist, create it
    	if (!theDir.exists()) {
    		_log("Creating directory: " + home);
    		boolean result = theDir.mkdir();  

    		if (result) {    
    			_log(home + " created.");  
    		}
    		else{
    			_elog("Cannot initialize the directory");
    			System.exit(1);
    		}
    	}
    	_root = home;
    	_dlog("Disk home: " + home);
    }
    
    /**
     * Getters
     */
    public boolean debugMode(){
    	return _debug;
    }
    
    public boolean noException(){
    	return _hideException;
    }
    
    public String getMasterIP(){
    	return _masterIP;
    }
    
    public int getMasterPort(){
    	return _masterPort;
    }
    
    public String getName(){
    	return _name;
    }
    
    public String getPassword(){
    	return _password;
    }
    
    public String getRoot(){
    	return _root;
    }
    
    public String getClientRoot(){
    	return _root + System.getProperty("file.separator") + _name;
    }
    
    /**
     * Constructor
     * @param debug: debug mode?
     * @param useUI: use UI? (not useful for now)
     * @param hideException: hide exceptions?
     * @param name: the name of the client?
     * @param password: the password of the client?
     */
    public DropboxClient(boolean debug, boolean useUI, boolean hideException, String name, String password){
    	_debug = debug;
    	_hideException = hideException;
    	_name = name;
    	_password = password;
    	_useUI = useUI;
    	_net = new DropboxClientNet(this);
    	initRoot(DropboxConstants.CLIENT_ROOT);
    	initHome(_name);
    	printStatus();
    }
    
    public static void main(String[] args) {
    	
    	String name = null;
    	String password = null;
    	boolean debug = false;
    	boolean useUI = false;
    	boolean hideException = false;
    	
    	for( int i = 0; i < args.length; i++ ){
    		if (args[i].equals("-d")){
    			debug = true;
    		}
    		else if (args[i].equals("-u")){
    			useUI = true;
    		}
    		else if (args[i].equals("-he")){
    			hideException = true;
    		}
    		else if (args[i].equals("-name")){
    			i++;
    			name = args[i];
    		}else if (args[i].equals("-pass")){
    			i++;
    			password = args[i];
    		}
    	}
    	
    	if (name == null || password == null){
    		usage();
    		System.exit(1);
    	}
    	
    	DropboxClient client = new DropboxClient(debug, useUI, hideException, name, password);
    	client.run();
    }
}
