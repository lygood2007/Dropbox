package master;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * 
 * class: MasterServerClientNet
 * Description: Listen to clients connected and spawn a new thread to handle it
 */
class MasterServerClientNet{
	private ServerSocket _serverSocket;
	private Timer _timer;
	// TODO: should handle non-running threads, remove the reference of it?
	private ArrayList<Thread> _threads;
	private MasterServer _server;
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerClientsNet (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		if(!_server.noException())
			System.err.println("[MasterServerClientsNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerClientsNet]:" + str);
	}
	
	public MasterServerClientNet(MasterServer server){
		_server = server;
		assert _server != null;
		_threads = new ArrayList<Thread>();
		//setupTimer();
	}
	
	// TEST
	/*private void setupTimer(){
		_timer = new Timer();
		_timer.scheduleAtFixedRate(new ThreadCollection(_threads),1000, 1000);
	}
	
	private class ThreadCollection extends TimerTask{ 
		
		ArrayList<Thread> _threads;
		
		private void _log(String str){
			System.out.println("ThreadCollection:"+str);
		}
		
		public ThreadCollection(ArrayList<Thread> threads){
			_threads = threads;
		}
		
		@Override
		public void run(){
			_log("Threre are " + _threads.size() + " threads");
			
			// Just use thread pool...
			/*
			for(int i = _threads.size()-1; i>= 0; i--){
				Thread t= _threads.get(i);
				// TODO: really useful here
				// TEST
				if(!t.isAlive()){
					_threads.remove(i);
				}
			}
			
		}
	}*/

	public void listen(){
		_log("listening to new client...");
    	Socket client = null;
    	
    	try{
    		_serverSocket = new ServerSocket(_server.clientsPort());
    		_serverSocket.setSoTimeout(1000*100);
    		while(true)
    		{
    			client = _serverSocket.accept();
    			_dlog("Get connection from " + client.getInetAddress().getHostAddress());
    			//TODO: here, it should control how many threads we could accept at most
    			//TEST
    			//TODO: add the thread pool
    			Thread t;
    			
    			t = new Thread(new MasterServerClientHandler(client, _server));
    			t.start();
    			_threads.add(t);
    		}
    	}catch(InterruptedIOException e){
    		_elog(e.toString());
    		if(_server.debugMode()){
    			e.printStackTrace();
    		}
    		
    	}catch(IOException e){
    		_elog(e.toString());
    		if(_server.debugMode()){
    			e.printStackTrace();
    		}
    	}finally{
    		try{
    			_dlog("Close connection from " + client.getInetAddress().getHostAddress());
    			if(_serverSocket != null )
    				_serverSocket.close();
    			if( client != null )
    				client.close();
    		}catch(IOException e){
    			_elog(e.toString());
        		if(_server.debugMode()){
        			e.printStackTrace();
        		}
    		}
    	}
	}
}
