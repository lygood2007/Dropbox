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
	
	private static void _elog(String str){
		System.err.println("[DropboxFileServerMasterNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxFileServerMasterNet]:" + str);
	}
	
	public DropboxFileServerMasterNet(DropboxFileServer server){
		// TODO: map is for future use
		_threadName = "DropboxFileServerMasterNet";
		_server = server;
		assert _server != null;
	}
	
	public boolean openConnections(){
		_log("Try to connect to Master...");
		
		_dlog("openConnections(){");
		boolean isConnected = connect();
		_dlog("}");
		
		if(!isConnected){
			_log("Cannot connect to Master");
			//TODO: make it better, not just simply kill the whole
		}else{
			_log("Success!");
			//_clientThread = new Thread(new ClientListner(_server));
			//_clientThread.start();
			// Master net also spawn a new thread to get user input
		}
		return isConnected;
	}
	
	public void listen(){
		// Firstly spawn a new thread and then the main thread also start listening
		_userNet = new DropboxFileServerUserNet(_server, _userOut, _userIn, _userSock);
		_userNet.start();
		
		/* Use main thread, cannot be stopped */
		while(!_sock.isClosed()){
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
		close();
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
	
	
	// TODO: setup a timeout mechanism, if the master is not running, retry.
	protected boolean connect(){
		boolean connected = true;
		try{
			_sock = new Socket(_serverIP, _serverPort);
			_out = new PrintWriter(_sock.getOutputStream(), true);
			_in = new BufferedReader(new InputStreamReader(_sock.getInputStream()));
			String reply = sendAll();
			if(!reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
				throw new Exception("Not confirmed");
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
		close();
	}
	
	protected void close(){
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
			}
		}
		else{
			if(_sock == null){
				throw new NullPointerException();
			}else{
				_sock = null;
				_in = null;
				_out = null;
			}
		}
		
		_dlog("Cancel the user input thread");
		if(_userNet != null){
				_userNet.stop();
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
			}
		}else{
			if(_userSock == null){
				throw new NullPointerException();
			}else{
				//_elog("Already closed");
				_userSock = null;
				_in = null;
				_out = null;
			}
		}
		_dlog("Finished");
	}
}
