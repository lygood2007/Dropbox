package fileserver;

import common.*;

import java.io.*;
import java.util.*;

/**
 * 
 * Class: DropboxFileServer
 * Description: The server for syncing files and talking to the master node
 *              , here we simulate the behavior of data storage.
 */
final class DropboxFileServer {

	private int _port;
	private boolean _debug; 
	private boolean _useUI;
	private boolean _hideException;
	private String _disk; // The location of the disk, is indeed the directory. 
	                             // Just simulate.
	private Timer _timer;
	private DropboxFileServerClientNet _clientNet;
	private DropboxFileServerMasterNet _masterNet;
	private int _id; // The unique identifier for this file server
	private int _prio; // The priority of this file server. (1->5)
	               // The larger it is the higher it will be chosen to be the sync server
	private int _maxClientNum;
	private Map<String, ClientNode> _clients;
	/*private boolean _clientsDirty;*/
	private void _dlog(String str){
		if(_debug)
			System.out.println("[DropboxFileServer (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[DropboxFileServer (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxFileServer]:" + str);
	}
	
    public DropboxFileServer(int id, int prio, int maxClientNum, int port, boolean debug, boolean useUI, boolean hideException, String disk){
    	_port = port;
    	_debug = debug;
    	_useUI = useUI;
    	_hideException = hideException;
    	_disk = disk;
    	_id = id;
    	_prio = prio;
    	_maxClientNum = maxClientNum;
    	_clients = new TreeMap<String, ClientNode>();
    	//_clientsDirty = false;
    	initDisk(_disk);
    	initNet();
    	initTimer();
    	printStatus();
    }
    
    private void initTimer(){
    	_timer = new Timer();
    	_timer.schedule(new Echo(this), DropboxConstants.ECHO_FILE_SERVER,DropboxConstants.ECHO_FILE_SERVER);
    }
    
    private void initDisk(String disk){
    	_dlog("Initialize disk");
    	File theDir = new File(disk);
    	// if the disk does not exist, create it
    	if (!theDir.exists()) {
    		_log("Creating directory: " +disk);
    		boolean result = theDir.mkdir();  

    		if(result) {    
    			_log(disk + " created.");  
    			/* String subDir = disk + System.getProperty("file.separator") + _id;
    			 File subDirFile = new File(subDir);
    			 result = subDirFile.mkdir();
    			 if(result){ 
    				 _log(subDir + " created.");
    				 _root = subDir;
    			 }else{
    				 _elog("Cannot initialize " + subDir);
    				 System.exit(1);
    			 }*/
    		}
    		else{
    			_elog("Cannot initialize " + disk);
    			System.exit(1);
    		}
    	}
    	// load client record into mem
    	loadClientsFromFile();
    	_dlog("Disk root: " + _disk);
    }
    
    private void initNet(){
    	_dlog("Initialize network..");
    	_masterNet = new DropboxFileServerMasterNet(this);
    	_clientNet = new DropboxFileServerClientNet(this);
    }
    
    public void run(){
    	if(connectToMaster() == true){
    		// Spawn a new thread to listen to new clients
    		listenToClients();
    		// Use main thread to accepting master's message
    		// Block here until socket is closed
    		_masterNet.listen();
    	}
    	_dlog("Done!");
    	cancelTimer();
    }
    
    public void cancelTimer(){
    	_dlog("Cancel the timer");
    	_timer.cancel();
    }
    
    /**
     * connectToMaster: Connect to master using two threads ((main and another)).
     *                  main thread: mainly accept master's heartbeat message
     *                  and master's forwarded request from client.
     *                  new thread: mainly get input from user and request master.
     */
    private boolean connectToMaster(){
    	// Let the miracle happen!
    	return _masterNet.openConnections();
    }
    
    /**
     * listenToClients: listen to new client (spawn a new thread)
     */
    private void listenToClients(){
    	_clientNet.start();
    }
    
    public void cancelListeningThread(){
    	_clientNet.stop();
    	_clientNet.join();
    }
    
    public synchronized void cancelSyncers(){
    	_log("Cancel all of the syncers..");
    	Iterator it = _clients.entrySet().iterator();
    	while(it.hasNext()){
    		Map.Entry pair = (Map.Entry)it.next();
    		ClientNode cn = (ClientNode)pair.getValue();
    		cn.cancelSyncer();
    	}
    }
    
    public ClientNode findMatch(String name, String password){
    	if(!_clients.containsKey(name)){
    		return null;
    	}else{
    		ClientNode cn = _clients.get(name);
    		if(!cn.match(name, password)){
    			return null;
    		}else{
    			return cn;
    		}
    	}
    }
    
    public void appendClientToFile(String name, String password){
    	File file = new File(_disk + System.getProperty("file.separator") + "clients.txt");
		try{
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(_disk + System.getProperty("file.separator") +"clients.txt", true);
			fw.write(name + " " + password + "\n");
			/* Close */
			fw.flush();
			fw.close();
		}catch(IOException e){
			if(!_hideException){
				_elog(e.toString());
			}
			if(_debug){
				e.printStackTrace();
			}
		}
    }
    
    public void writeClientsToFile(){
    	File file = new File(_disk + System.getProperty("file.separator") + "clients.txt");
		try{
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(_disk + System.getProperty("file.separator") + "clients.txt", false);
			Iterator it = _clients.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry pair= (Map.Entry)it.next();
				String name = (String)pair.getKey();
				ClientNode c = (ClientNode)pair.getValue();
				fw.write(name + " " + c.getPassword() + '\n');
			}
			/* Close */
			fw.flush();
			fw.close();
		}catch(IOException e){
			if(!_hideException){
				_elog(e.toString());
			}
			if(_debug){
				e.printStackTrace();
			}
		}
    }
    
    public void loadClientsFromFile(){
    	try{
    		BufferedReader saveFile=
    				new BufferedReader(new FileReader(_disk + System.getProperty("file.separator") + "clients.txt"));
    		while(true){
    			String nextLine = saveFile.readLine();
    			
    			
    			if(nextLine == null)
    				return;
    			StringTokenizer st = new StringTokenizer(nextLine);
    			if(st.countTokens() != 2){
    				throw new Exception("Invalid file");
    			}
    			String name = st.nextToken();
    			String password = st.nextToken();
    			addClient(name, password, true);
    		}
    	}catch(IOException e){
    		if(!_hideException){
    			_elog(e.toString());
    		}
    		if(_debug){
    			e.printStackTrace();
    		}
    		
    	}catch(Exception e){
    		if(!_hideException){
    			_elog(e.toString());
    		}
    		if(_debug){
    			e.printStackTrace();
    		}
    	}
    	
    	// Create the file if needed
    	File f = new File(_disk 
				+ System.getProperty("file.separator") + "clients.txt");
		try{
			f.createNewFile();
			_log("Created " + _disk 
					+ System.getProperty("file.separator") + "clients.txt");
		}catch(IOException e){
			if(!_hideException){
    			_elog(e.toString());
    		}
    		if(_debug){
    			e.printStackTrace();
    		}
		}
    }
    
    public boolean changePassword(String name, String oldPassword, String password){
    	if(!_clients.containsKey(name))
    		return false;
    	else
    	{
    		ClientNode cn = _clients.get(name);
    		if(!cn.match(name, oldPassword)){
    			_elog("Your old password is not correct");
    			return false;
    		}else{
    			cn.setPassword(password);
    			//_clientsDirty = true;
    			return true;
    		}
    	}
    }
    
    public boolean addClient(String clientName, String password, boolean nocheck){
    	assert _disk != null;
    	if(_clients.containsKey(clientName)){
    		_elog("Client already exist");
    		return false;
    	}
    	// we create a new name for this client
    	String fullpath = _disk +  System.getProperty("file.separator") + clientName;
    	File newDir =  new File(fullpath);
    	_dlog("Add client " + clientName);
    	if (!newDir.exists()) {
    		_log("Creating directory: " +newDir);
    		boolean result = newDir.mkdir();  

    		if(result) {    
    			_log(newDir + " created.");  
    			ClientNode cn = new ClientNode(clientName, fullpath, password);
    			_clients.put(clientName, cn);
    			// Renew the file. Currently just delete it and recreate a new one
    			// This is stupid
    			// TODO (Optional): make this better
    			appendClientToFile(clientName, password);
    		}
    		else{
    			_elog("Cannot create the directory");
    			// TODO: a better way to tell the client that it's failed
    			//System.exit(1);
    			return false;
    		}
    	}else{
    		if(nocheck){
    			ClientNode cn = new ClientNode(clientName, fullpath, password);
    			_clients.put(clientName, cn);
    		}else{
    			_elog("The name is used");
    			return false;
    		}
    		/*_elog("The name is used");
    		return false;*/
    	}
    	//_clientsDirty = true;
    	return true;
    }
    
    public boolean removeClient(String clientName){
    	if(!_clients.containsKey(clientName)){
    		return false;
    	}else
    	{
    		ClientNode cn = _clients.get(clientName);
    		boolean success = cn.clear();
    		_clients.remove(clientName);
    		if(success == true){
    			// Renew the file. Currently just delete it and recreate a new one
    			// This is stupid
    			// TODO (Optional): make this better
    			writeClientsToFile();
    		}
    		return success;
    	}
    }
    
    public void printStatus(){
    	_log("**Dropbox File Server configuration:");
    	_log("ID:" + _id);
    	_log("Debug:" + _debug);
    	_log("UseUI:" + _useUI);
    	_log("Hide Exception:" + _hideException);
    	_log("Priority:" + _prio);
    	_log("Max Client Num:" + _maxClientNum);
    	_log("Current Client Num:" + _clients.size());
    	_log("Root disk:" + _disk);
    	_log("Listen port:" + _port);
    	Map<String, ClientNode> mp = _clients;
		Iterator it = mp.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			String name = (String)pair.getKey();
			ClientNode node = (ClientNode)pair.getValue();
			 _log("<Clients name>:" + name + " <Client dir>:"
			+ node.getDir() +" <Syncers>:" 
			+ node.getNumSyncer() + " <Password>:"
			+ node.getPassword()); 
		}
    	System.out.println();
    }
    
    public static void usage(){
    	_log("Dropbox File Server:");
    	_log("/* You must specify the id for this server */");
    	_log("-id give an id to this server");
    	_log("-d: for debug mode (default false)" );
    	_log("-u: to use user interface (default false)");
    	_log("-he: to hide non-runtime exceptions (default false)");
    	_log("-p: to specify port for listening (default "+
    			DropboxConstants.FILE_SERVER_PORT+")");
    	_log("-disk: root your server is located"
    			+ " (default using CWD/"+DropboxConstants.FILE_SERVER_ROOT+ " id" + 
    			", where CWD is your current working directory)");
    	_log("-prio: the priority of this server ("+DropboxConstants.MIN_PRIO+
    			"-"+DropboxConstants.MAX_PRIO+")");
    	_log("-mc: the max number of clients can be connected in this server (0-"+
    			DropboxConstants.MAX_CLIENTS_IN_FS+")");
    	
    	System.out.println();
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
    public synchronized void toogleDebug(){
    	_debug = !_debug;
    }
    
    public int listenPort(){
    	return _port;
    }
    
    public String disk(){
    	return _disk;
    }
    
    public Map<String, ClientNode> getClients(){
    	return _clients;
    }
    
    public int getID(){
    	return _id;
    }
    
    public int getPrio(){
    	return _prio;
    }
    
    public synchronized void setPrio(int prio){
    	_prio = prio;
    }
    
    public int getMaxClientNum(){
    	return _maxClientNum;
    }
    
    public int getCurClientNum(){
    	return _clients.size();
    }
    
    public synchronized void garbageCollection(){
    	_dlog("Run garbage collection");
    	Iterator it = _clients.entrySet().iterator();
    	while(it.hasNext()){
    		Map.Entry pair = (Map.Entry)it.next();
    		//TODO
    		//String name = (String)pair.getKey();
    		ClientNode cn = (ClientNode)pair.getValue();
    		_dlog("Remove dead syncer");
    		cn.removeDeadSyncer();
    	}
    }
    
    private class Echo extends TimerTask{
		
		private DropboxFileServer _server;
		
		private void _log(String str){
			System.out.println("[Echo]:"+str);
		}
		public Echo(DropboxFileServer server){
			_server = server;
		}
		
		@Override
		public void run(){
			garbageCollection();
			if(_server.debugMode())
				printStatus();
		}
    }
		
    public static void main(String[] args) {
    	
    	int port = DropboxConstants.FILE_SERVER_PORT;
    	boolean debug = false;
    	boolean useUI = false;	
    	boolean hideException = false;
    	String disk = DropboxConstants.FILE_SERVER_ROOT;
    	int id = 0;
    	int prio = 1;
    	int maxClientNum = DropboxConstants.MAX_CLIENTS_IN_FS;
    	
    	if(args.length < 2){
    		usage();
    		System.exit(1);
    	}
    	
    	for( int i = 0; i < args.length; i++ ){
    		
    		if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-u"))
				useUI = true;
			else if(args[i].equals("-p")){
				i++;
				port = Integer.parseInt(args[i]);
			}
			else if(args[i].equals("-disk")){
				i++;
				disk = args[i];
			}else if(args[i].equals("-id")){
				i++;
				id = Integer.parseInt(args[i]);
			}else if(args[i].equals("-prio")){
				i++;
				prio = Integer.parseInt(args[i]);
				prio = Math.max(prio, DropboxConstants.MIN_PRIO);
				prio = Math.min(prio, DropboxConstants.MAX_PRIO);
			}
			else if(args[i].equals("-mc")){
				i++;
				maxClientNum = Integer.parseInt(args[i]);
				maxClientNum = Math.max(maxClientNum, 0);
				maxClientNum = Math.min(maxClientNum, DropboxConstants.MAX_CLIENTS_IN_FS);
			}
			else if(args[i].equals("-he")){
				hideException = true;
			}
    	}
    	disk = disk + id;
    	DropboxFileServer server = new DropboxFileServer(id, prio, maxClientNum, port,debug,useUI,hideException,disk);
    	server.run();
    }
}
