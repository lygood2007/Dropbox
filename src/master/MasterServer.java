package master;

import common.*;

import java.util.*;


/**
 * 
 * class MasterServer
 * Description: The big server! It's mainly responsible for handling dispatching client connection
 *              to a specified file server.
 */
final class MasterServer {

	private int _clientsPort;
	private int _clusterPort;
	private boolean _debug;
	private boolean _useUI;
	private boolean _hideException;
	private MasterServerNet _serverNet;
	private volatile LinkedList<ClientRecord> _clients;
	private volatile LinkedList<FileServerNode> _fsnodes;
	private Timer _timer;
	
	private void _dlog(String str){
		if(_debug)
			System.out.println("[MasterServer (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[MasterServer (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServer]:" + str);
	}
	
	public MasterServer(int clientPort, int clusterPort, boolean useUI, boolean debug, boolean hideException){
		_clientsPort = clientPort;
		_clusterPort = clusterPort;
		_useUI = useUI;
		_debug = debug;
		_hideException = hideException;
		_clients = new LinkedList<ClientRecord>();
		 
		_fsnodes = /*(LinkedList<FileServerNode>) Collections.synchronizedList(*/new LinkedList<FileServerNode>();
		initNet();
		initTimer();
		printStatus();
	}
	
	private void initNet(){
		_serverNet = new MasterServerNet(this);
	}
	
	private void initTimer(){
		_timer = new Timer();
		_timer.scheduleAtFixedRate(new Echo(this), DropboxConstants.ECHO_MASTER, DropboxConstants.ECHO_MASTER);
	}
	
	public void run(){
		//_serverNet.listenToClients();
		_serverNet.run();
	}
	
	public void printStatus(){
		_log("Master Server configuration:");
		_log("Debug:" + _debug);
		_log("UseUI:" + _useUI);
		_log("HideException:" + _hideException);
		_log("Client port:" + _clientsPort);
		_log("Cluster port:" + _clusterPort);
    	System.out.println();
	}
	
	public void usage(){
		_log("Master Server:");
		_log("-d: for debug mode (default false)" );
		_log("-u: to use user interface (default false)");
		_log("-cp: to specify the listen port for clients (default " + DropboxConstants.MASTER_CLIENT_PORT+")");
		_log("-lp: to specify the listen port for cluster (default " + DropboxConstants.MASTER_CLUSTER_PORT+")");
    	_log("-e: to hide non-runtime exceptions' reports");
		System.out.println();
	}
	
	/**
	 * Getters
	 */
	public int clientsPort(){
		return _clientsPort;
	}
	
	public int clusterPort(){
		return _clusterPort;
	}
	
	public boolean debugMode(){
		return _debug;
	}
	
	public boolean noException(){
		return _hideException;
	}
	
	public LinkedList<FileServerNode> getFS(){
		return _fsnodes;
	}
	
	public FileServerNode findFileServer(int id){
		for(FileServerNode fsn: _fsnodes){
			if(fsn.getID() == id){
				return fsn;
			}
		}
		return null;
	}
	
	public synchronized void insertFileServer(FileServerNode fs){
		_fsnodes.add(fs);
	}
	
	public synchronized void removeFileServer(int id){

		int i = 0;
		for(FileServerNode fsn: _fsnodes){
			if(id == fsn.getID()){
				fsn.clear();
				break;
			}
			i++;
		}
		_fsnodes.remove(i);
	}
	
	public void printFileServers(){
		
		_log("There are " + _fsnodes.size() + " file server connected");
		int i = 0;
		for(FileServerNode fsn: _fsnodes){
			_log("Server " + i);
			_log("ID:" + fsn.getID());
			_log("IP:" + fsn.getIP());
			_log("MAX CLIENTS:" + fsn.getMaxClients());
			_log("PRIO:" + fsn.getPriority());
			_log("CONNECTED CLIENTS:"+fsn.getNumClients());
			System.out.println();
			i++;
		}
	}
	
	public boolean clientExist(String name){
		for(ClientRecord cr: _clients){
			if(cr.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	
	public ClientRecord findClient(String name, String password){
		for(ClientRecord cr: _clients){
			if(cr.getName().equals(name) && cr.getPassword().equals(password)){
				return cr;
			}
		}
		return null;
	}
	
	public synchronized boolean addClient(String name, String password, FileServerNode fs){
		boolean exist = clientExist(name);
		if(exist){
			return false;
		}else{
			ClientRecord cr = new ClientRecord(name, password, fs);
			_clients.add(cr);
			return true;
		}
	}
	
	public synchronized boolean removeClient(String name){
		boolean exist = clientExist(name);
		if(!exist){
			return false;
		}
		Iterator it = _clients.iterator();
		while(it.hasNext()){
			ClientRecord cr = (ClientRecord)it.next();
			if(cr.getName().equals(name)){
				it.remove();
				break;
			}
		}
		return true;
	}
	
	public synchronized boolean removeClient(ClientRecord cr){
		if(!_clients.contains(cr))
			return false;
		_clients.remove(cr);
		return true;
	}
	
	public synchronized boolean changePassword(String name, String pwd){
		for(ClientRecord cr: _clients){
			if(cr.getName().equals(name)){
				cr.changePassword(pwd);
				return true;
			}
		}
		return false;
	}
	
	public synchronized void printClients(){
		_log("There are " + _clients.size() + " clients");
		int i = 0;
		for(ClientRecord cr: _clients){
			_log("client " + i + ": " + cr.getName() + " owner: file server " + cr.getOwner().getID()
					+ " password:"+cr.getPassword());
		}
	}
	
	public synchronized void removeDead(FileServerNode fs){
		assert fs.isAlive() == false;
		
		_log("Remove fileserver " + fs.getID());
		/* Also remove associate clients */
		Iterator it = _clients.iterator();
		while(it.hasNext()){
			ClientRecord cr = (ClientRecord)it.next();
			if(cr.getOwner() == fs){
				it.remove();
			}
		}
		_fsnodes.remove(fs);
		printFileServers();
	}
	
	public synchronized void garbageCollection(){
		_dlog("Run garbage collection");
		/* Need to use iterator to loop */
		Iterator<FileServerNode> it = _fsnodes.iterator();
		while(it.hasNext()){
			FileServerNode node = it.next();
			if(!node.isAlive()){
				_log("Remove fileserver " + node.getID());
				it.remove();
			}
		}
	}
	
	/**
	 * 
	 * class Echo
	 * Description: Walk through the linked list and do garbage collection.
	 *              Also print out the status of the server
	 */
	private class Echo extends TimerTask{
		
		private MasterServer _server;
		
		private void _log(String str){
			System.out.println("[Echo]:"+str);
		}
		public Echo(MasterServer server){
			_server = server;
		}
		
		@Override
		public void run(){
			garbageCollection();
			printFileServers();
			printClients();
		}
	}
	
	public static void main(String[] args) {
		int clientPort = DropboxConstants.MASTER_CLIENT_PORT;
		int clusterPort = DropboxConstants.MASTER_CLUSTER_PORT;
		boolean useUI = false; // Not used now
		boolean debug = false;
		boolean hideException = false;
		
		for( int i = 0; i < args.length; i++ ){
    		if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-u"))
				useUI = true;
			else if(args[i].equals("-cp")){
				i++;
				clientPort = Integer.parseInt(args[i]);
			}
			else if(args[i].equals("-lp")){
				i++;
				clusterPort = Integer.parseInt(args[i]);
			}else if(args[i].equals("-e")){
				hideException = true;
			}
    	}
		
		MasterServer server = new MasterServer(clientPort, clusterPort, useUI, debug, hideException);
		server.run();
	}
}
