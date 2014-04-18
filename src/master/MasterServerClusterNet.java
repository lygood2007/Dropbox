package master;

import java.io.*;
import java.net.*;
import java.util.*;

import common.*;

/**
 * 
 * class MasterServerClusterNet
 * Description: Listen to new file server connected and spawn a new thread to handle it
 */
class MasterServerClusterNet extends ThreadBase {
	private MasterServer _server;
	private ServerSocket _serverSocket;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerClusterNet (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[MasterServerClusterNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerClusterNet]:" + str);
	}
	
	public MasterServerClusterNet(MasterServer server){
		super("MasterServerClusterNet",server.debugMode());
		_server = server;
		assert _server != null;
	}
	
	/*private String receive(BufferedReader in) throws Exception{
		String from = null;
		try{
			if((from = in.readLine()) == null)
				throw new Exception("No response received");
		}catch(Exception e){
			_elog(e.toString());
			// If it gets here, it means the socket is closed by the 
			// other side
			throw new Exception("Connection is broken");
		}
		return from;
	}*/
	
	private void parse(String str, Socket s){
		assert str != null;
		assert s != null;
		StringTokenizer st = new StringTokenizer(str);
		String tk = st.nextToken();
		if(tk.equals(ProtocolConstants.PACK_STR_ID_HEAD)){
			int id = Integer.parseInt(st.nextToken());
			int prio = Integer.parseInt(st.nextToken());
			int maxClientNum = Integer.parseInt(st.nextToken());
			FileServerNode node = _server.findFileServer(id);
			if(node == null){
				node = new FileServerNode();
				_server.insertFileServer(node);
			}
			// load all entries
			while(st.hasMoreTokens()){
				String clientName = st.nextToken();
				String password = st.nextToken();
				node.addEntry(clientName, password);
			}
			node.setID(id);
			node.setIP(s.getInetAddress().getHostAddress());
			node.setSocket(s);
			node.setPriority(prio);
			node.setMaxClients(maxClientNum);
			
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				// Send back confirmation, temporary use
				out.println(ProtocolConstants.PACK_STR_CONFIRM_HEAD);
				MasterServerFileServerRequest fsq = new MasterServerFileServerRequest(_server, out, in, s, node);
				node.setRequestThread(fsq);
				fsq.start();
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode())
					e.printStackTrace();
				
				return;
			}
			
		}else if(tk.equals(ProtocolConstants.PACK_STR_USR_HEAD)){
			int id = Integer.parseInt(st.nextToken());
			//Thread t = new Thread(new MasterServerFileServerAccept(s, _server));
			FileServerNode node = _server.findFileServer(id);
			if(node == null){
				node = new FileServerNode();
				_server.insertFileServer(node);
			}	
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				out.println(ProtocolConstants.PACK_STR_CONFIRM_HEAD);
				MasterServerFileServerAccept fsa = new MasterServerFileServerAccept(_server, out, in, s, node);
				node.setAcceptThread(fsa);
				fsa.start();
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
			}
		}else if(tk.equals(ProtocolConstants.PACK_STR_CLOSE_HEAD)){
			// Close both the socket
			/*int id = Integer.parseInt(st.nextToken());
			FileServerNode node = _server.findFileServer(id);
			if(node != null){
				node.clear();
			}*/
		}
		else{
			
			//TODO: to be added
			_elog("Invalid package");
		}
	}
	
	public void clear(){
		_dlog("Do clear...");
		
		_dlog("Finished");
	}
	
	@Override
	public void run(){
		_log("listening to new client...");
		// Now only support one client
		Thread thisThread = Thread.currentThread();
		while(_t == thisThread){ // Cancel point
			
			try{
				synchronized(this) {
					while(_suspended) { // Suspension point
						wait();
					}
				}
			}catch(InterruptedException e){ // Cancel point
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode())
					e.printStackTrace();
				break;
			}
			
			try{
				_serverSocket = new ServerSocket(_server.clusterPort());
				if(_server.debugMode()){
					_dlog("Server timeout after 100 seconds");
					_serverSocket.setSoTimeout(1000*100);
				}
				while(true)
				{
					synchronized(this) {
						while(_suspended) { // Suspension point
							wait();
						}
					}
					Socket fileServer = _serverSocket.accept();
					_log("Get connection from " + fileServer.getInetAddress().getHostAddress());
					//TODO: here, it should control how many threads we could accept at most

					// read the identification
					BufferedReader in = new BufferedReader(new InputStreamReader(fileServer.getInputStream()));
					String init = NetComm.receive(in);
					/*if(init == null){
						_elog("The connection from " + fileServer.getInetAddress().getHostAddress() 
								+ "is closed");
					}else{*/
						parse(init, fileServer);
					//}
				}
			}catch(InterruptedException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
			}catch(InterruptedIOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
				// exit
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
				// exit
			}catch(Exception e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
			}
			finally{
				try{
					// close the listen socket
					if(_serverSocket != null )
						_serverSocket.close();
				}catch(IOException e){
					if(!_server.noException()){
						_elog(e.toString());
					}
					if(_server.debugMode()){
						e.printStackTrace();
					}
					// exit
				}
				// Stop the thread
				stop();
			}
		}
		_dlog("Stopped");
		clear();
	}
	
	// Setup a timer (Deprecated)
	// TODO: good way to do garbage collection
	/*
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

		}
	}*/
}
