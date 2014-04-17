package master;

/**
 * 
 * class: MasterServerNet
 * Description: Two major duties: listen to new cluster added,
 *              and listen to new client added
 */
class MasterServerNet {

	private MasterServerClusterNet _clusterNet;
	private MasterServerClientNet _clientsNet;
	//private Thread _threadClients;
	private MasterServer _server;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerNet (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		if(!_server.noException())
			System.err.println("[MasterServerNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerNet]:" + str);
	}
	
	public void run(){
		//TODO: to be filled later, I want the main thread to do something 
		//      more useful
		
		// Spawn a new thread to listen new clusters
		listenToCluster();
		
		// Spawn a new thread to listen new clients
		listenToClients();
		
	}
	public MasterServerNet(MasterServer server){
		_server = server;
		_clusterNet = new MasterServerClusterNet(_server);
		
		//_threadCluster = null;
		//_threadClients = null;
	}
	
	public void listenToCluster(){
		assert _clusterNet != null;
		_clusterNet.start();
	}
	
	public void listenToClients(){
		//_threadClients = new Thread(new ClientListener(_server));
		//_threadClients.start();
	}
}
