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
class MasterServerClusterNet extends GeneralServer {
	private MasterServer _server;
	
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
		super("MasterServerClusterNet", server.noException(),server.debugMode());
		_server = server;
		assert _server != null;
	}
	
	private void parse(String str, Socket s){
		assert str != null;
		assert s != null;
		StringTokenizer st = new StringTokenizer(str);
		String tk = st.nextToken();
		
		if(tk.equals(ProtocolConstants.PACK_STR_ID_HEAD)){
			try
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				int id = Integer.parseInt(st.nextToken());
				int prio = Integer.parseInt(st.nextToken());
				int maxClientNum = Integer.parseInt(st.nextToken());
				FileServerNode node = _server.findFileServer(id);
				assert node == null;
				node = new FileServerNode();
				
				
				// load all entries
				while(st.hasMoreTokens()){
					String clientName = st.nextToken();
					String password = st.nextToken();
					boolean result = _server.addClient(clientName, password, node);
					if(result == false){
						NetComm.send(ProtocolConstants.PACK_STR_ERRMES_HEAD + " "
								+ "Conflict in adding client, the client name already exists!", out);
						out.close();
						node = null;
						
						return;
					}
				}
				node.setID(id);
				node.setIP(s.getInetAddress().getHostAddress());
				//s.getPort();
				node.setSocket(s);
				node.setPriority(prio);
				node.setMaxClients(maxClientNum);
				_server.insertFileServer(node);
				// Send back confirmation, temporary use
				NetComm.send(ProtocolConstants.PACK_STR_CONFIRM_HEAD, out);
				MasterServerFileServerRequest fsq = new MasterServerFileServerRequest(_server, out, in, s, node);
				node.setRequestThread(fsq);
				fsq.start();
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode())
					e.printStackTrace();
				
			}catch(Exception e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode())
					e.printStackTrace();
			}
			
		}else if(tk.equals(ProtocolConstants.PACK_STR_USR_HEAD)){
			int id = Integer.parseInt(st.nextToken());
			_dlog("" + id);
			//Thread t = new Thread(new MasterServerFileServerAccept(s, _server));
			FileServerNode node = _server.findFileServer(id);
			assert node != null;
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
		}
		else{			
			//TODO: to be added
			_elog("Invalid package");
		}
	}
	
	@Override
	public void run(){
		_log("listening to new fileservers...");
		// Now only support one
		Thread thisThread = Thread.currentThread();
		try{
			_serverSocket = new ServerSocket(_server.clusterPort());
			if(_server.debugMode()){
				_dlog("Server timeout after 100 seconds");
				_serverSocket.setSoTimeout(1000*100);
			}
			while(true)
			{
				if(_serverSocket == null || _serverSocket.isClosed() || _t != thisThread)// Cancel point
					break;

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
				parse(init, fileServer);
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
			// Stop the thread
			stop();
		}
		
		clear();
		_log(_threadName + " is stopped");
	}
}
