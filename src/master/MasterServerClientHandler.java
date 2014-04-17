package master;

import java.net.*;

/**
 * 
 * Class MasterServerClientHandler
 * Description: Handles the new connected file server
 */
class MasterServerClientHandler implements Runnable{
	private Socket _sock;
	private MasterServer _server;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerClientHandler (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		if(!_server.noException())
			System.err.println("[MasterServerClientHandler (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerClientHandler]:" + str);
	}
	
	public MasterServerClientHandler(Socket sock,MasterServer server){
		_sock = sock;
		_server = server;
		assert _server != null;
	}
	
	@Override
	public void run(){
		
	}
	
	public Socket getSocket(){
		return _sock;
	}
}
