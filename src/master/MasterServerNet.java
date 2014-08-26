/**
 * File: MasterServerNet.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package master;

/**
 * 
 * class: MasterServerNet
 * Description: Two major duties: listen to new cluster added,
 *              and listen to new client added
 */
class MasterServerNet {
	/* Network objects */
	private MasterServerClusterNet _clusterNet;
	private MasterServerClientNet _clientsNet;
	
	/* The server */
	private MasterServer _server;
	
	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	@SuppressWarnings("unused")
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerNet (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	@SuppressWarnings("unused")
	private static void _elog(String str){
		System.err.println("[MasterServerNet (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	@SuppressWarnings("unused")
	private static void _log(String str){
		System.out.println("[MasterServerNet]:" + str);
	}
	
	/**
	 * run: run listener for clusters and listener for clients
	 */
	public void run(){
		//TODO: to be filled later, I want the main thread to do something 
		//      more useful
		// Spawn a new thread to listen new clusters
		listenToCluster();
		
		// Spawn a new thread to listen new clients
		listenToClients();
		
	}
	
	/**
	 * Constructor
	 * @param server: the server reference
	 */
	public MasterServerNet(MasterServer server){
		_server = server;
		_clusterNet = new MasterServerClusterNet(_server);
		_clientsNet = new MasterServerClientNet(_server);
	}
	
	/**
	 * listenToCluster: start to listen to clusters
	 */
	public void listenToCluster(){
		assert _clusterNet != null;
		_clusterNet.start();
	}
	
	/**
	 * listenToClients: start to listen to clients
	 */
	public void listenToClients(){
		assert _clientsNet != null;
		_clientsNet.start();
	}
}
