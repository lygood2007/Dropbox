package fileserver;

import java.io.*;
import java.net.*;
import java.util.*;

import common.*;
/**
 * 
 * Class: DropboxFileServerMasterNet
 * Description: Handles the connection to master net
 */
class DropboxFileServerMasterNet {

	private DropboxFileServer _server;
	private DropboxFileServerUserNet _userNet;
	
	private Socket _sock;
	private PrintWriter _out;
	private BufferedReader _in;
	
	private Socket _userSock;
	private PrintWriter _userOut;
	private BufferedReader _userIn;
	
	private String _threadName;
	
	/* Always use that for now */
	private String _serverIP = DropboxConstants.MASTER_IP;
	private int _serverPort = DropboxConstants.MASTER_CLUSTER_PORT;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[DropboxFileServerMasterNet (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	private static void _elog(String str){
		System.err.println("[DropboxFileServerMasterNet (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	private static void _log(String str){
		System.out.println("[DropboxFileServerMasterNet]:" + str);
	}
	
	public DropboxFileServerMasterNet(DropboxFileServer server){
		_threadName = "DropboxFileServerMasterNet";
		_server = server;
		assert _server != null;
	}
	
	public boolean openConnections(){
		_log("Try to connect to Master...");
		
		int i = 0;
		while(true){
			boolean isConnected = connect();
			
			if(!isConnected){
				i++;
				if(i == DropboxConstants.MAX_TRY){
					break;
				}
				_log("Cannot connect to Master, retry " + i);
				try{
					Thread.sleep(DropboxConstants.TRY_CONNECT_MILLIS);
				}catch(InterruptedException e){
					if(!_server.noException()){
						_elog(e.toString());
					}
					if(_server.debugMode()){
						e.printStackTrace();
					}
					_log("Retry connection is interrupted");
					break;
				}
			}else{
				_log("Success!");
				return true;
			}
		}
		_log("Failed");
		return false;
	}
	
	public void listen(){
		// Firstly spawn a new thread and then the main thread also start listening
		_userNet = new DropboxFileServerUserNet(_server, _userOut, _userIn, _userSock);
		_userNet.start();
		
		/* Use main thread, cannot be stopped */
		while(_sock != null && !_sock.isClosed()){
			try
			{
				String line = NetComm.receive(_in); // if it receives null, it means the connection is broken
				_dlog(line);
				parse(line);
			}catch(Exception e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				break;
				// Break the loop
			}
		}
		
		/* Clear */	
		// Close main thread
		clear();
		// Cancel the listening thread and retry
		_server.cancelListeningThread(); // listening thread is guaranteed to be closed
		/* Also cancel all of the syncers */
		_server.cancelSyncers();
		// Retry after 
		try{
			
			_log("The connection to master is broken, reset everything and retry connection after "
			+ DropboxConstants.TRY_CONNECT_MILLIS + " milliseconds");
			
			Thread.sleep(DropboxConstants.TRY_CONNECT_MILLIS);
		
		}catch(InterruptedException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		/* Renew user net */
		//_userNet = new DropboxFileServerUserNet(_server, _userOut, _userIn, _userSock);
		//_userNet.start();
		// Recursive call, rerun the main thread
		// Forever recursion
		_server.run();
		
		clear();
		_log(_threadName +" is stopped");
	}
	
	protected void parse(String str){
		StringTokenizer st = new StringTokenizer(str);
		String tkn = st.nextToken();
		try{
			if(tkn.equals(ProtocolConstants.PACK_STR_HEARTBEAT_HEAD)){
				_dlog("I got heartbeat");
				// Send back confirmation
				// TODO: add more story here
				NetComm.send(ProtocolConstants.PACK_STR_HEARTBEAT_HEAD,_out);
			}else if(tkn.equals(ProtocolConstants.PACK_STR_REQUEST_FS_HEAD)){
				// TODO: add more story here
			}else{
				_elog("Invalid header, skip.");
			}
		}catch(Exception e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
	}
	
	// Only send name and password to master
	protected String sendAll() throws Exception{
		assert _out != null;
		String output = _server.getID() + " " + _server.getPrio() +
				" " + _server.getMaxClientNum();
		Map<String, ClientNode> mp = _server.getClients();
		Iterator it = mp.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			String name = (String)pair.getKey();
			ClientNode node = (ClientNode)pair.getValue();
			output += " " + name + " " + node.getPassword(); 
		}
		output = ProtocolConstants.PACK_STR_ID_HEAD + " " + output;
		String reply = NetComm.sendAndRecv(output,_out,_in);
		return reply;
	}
	
	protected String sendUserID() throws Exception {
		assert _userOut != null;
		String output = ProtocolConstants.PACK_STR_USR_HEAD + " " + _server.getID();
		String reply = NetComm.sendAndRecv(output,_userOut, _userIn);
		return reply;
	}
	
	
	protected boolean connect(){
		boolean connected = true;
		try{
			_sock = new Socket(_serverIP, _serverPort);
			_out = new PrintWriter(_sock.getOutputStream(), true);
			_in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
			String reply = sendAll();
			if(!reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
				/* Tokenize */
				StringTokenizer st = new StringTokenizer(reply);
				if(st.nextToken().equals(ProtocolConstants.PACK_STR_ERRMES_HEAD)){
					_elog(st.nextToken());
					return false;
				}
			}

			_userSock = new Socket(_serverIP, _serverPort);
			_userOut = new PrintWriter(_userSock.getOutputStream(), true);
			_userIn = new BufferedReader(new InputStreamReader(_userSock.getInputStream()));
			String replyUser = sendUserID();
			if(!replyUser.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
				throw new Exception("Not confirmed");
			}
		}catch(UnknownHostException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
			connected = false;
		}catch(IOException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
			connected = false;
		}catch(Exception e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
			connected = false;
		}
		if(!connected){
			return connected;
		}

		if(!connected){
			/* Remove the pair of socket */
			_sock = null;
			_in = null;
			_out = null;
			
			_userSock = null;
			_userOut = null;
			_userIn = null;
		}
		return connected;
	}
	
	public void closeConnections(){
		_log("Close the connection...");
		int i = 0;
		while(i < DropboxConstants.MAX_TRY){
			if(close()){
				_log("Success");
				return;
			}else{
				_elog("Failed closing, retry " + i);
				i++;
			}
		}
		_elog("Failed");
	}
	
	protected void clear(){
		closeConnections();
	}
	
	protected boolean close(){
		_dlog("Do main thread closing...");
		if(_sock != null && !_sock.isClosed()){
			_dlog("Closing the receiving thread");
			try{
				_sock.close();
				_in.close();
				_out.close();
				
				_sock = null;
				_in = null;
				_out = null;
				_dlog("Success!");
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
				return false;
			}
		}
		_sock = null;
		_in = null;
		_out = null;
		
		_dlog("Cancel the user input thread");
		if(_userNet != null){
			_userNet.stop();
			_userNet.join(); // guaranteed to be closed
			_userNet = null;
		}
		if(_userSock != null && !_userSock.isClosed()){
			_dlog("Closing the user input thread");
			//Stop the user thread
			try{
				_userSock.close();
				_userIn.close();
				_userOut.close();
				_userSock = null;
				_userIn = null;
				_userOut = null;
				_dlog("Success!");
			}catch(IOException e){
				if(!_server.noException()){
					_elog(e.toString());
				}
				if(_server.debugMode()){
					e.printStackTrace();
				}
				return false;
			}
		}
		_userSock = null;
		_userIn = null;
		_userOut = null;
		
		/* Cancel all client nodes */
		
		_dlog("Finished");
		return true;
		
		/* CAUTION: _server is never cleared */
	}
}
