package fileserver;

import common.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class DropboxFileServerUserNet extends ThreadBase{
	private DropboxFileServer _server;
	private PrintWriter _out;
	private BufferedReader _in;
	private Socket _sock;
	
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[DropboxFileServerUserNet (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[DropboxFileServerUserNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxFileServerUserNet]:" + str);
	}
	
	public DropboxFileServerUserNet(DropboxFileServer s, PrintWriter userOut,
			BufferedReader userIn, Socket sock){
		super("DropboxFileServerUserNet",s.debugMode());
		_server = s;
		_in = userIn;
		_sock = sock;
		_out = userOut;
	}

	
	public void clear(){
		_dlog("Do clear...");
		try{
			if(!_sock.isClosed())
				_sock.close();
			/* Close stream */
			_in.close();
			_out.close();
			
			/* Set to null */
			_sock = null;
			_in = null;
			_out = null;
			
		}catch(IOException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}
		_dlog("Finished");
	}
	
	@Override
	public void run(){
		usage();
		Scanner in = new Scanner(System.in);
		Thread thisThread = Thread.currentThread();
		try
		{
			while(!_sock.isClosed() && thisThread == _t){ //Cancel point
				synchronized(this) {
					while(_suspended) { // Suspension point
						wait();
					}
				}
				System.out.println("Your input:");
				String s = in.nextLine();// Read from user input
				parse(s);
			}
		}catch(InterruptedException e){ // Cancel point
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}catch(Exception e){ // Always last
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}		
		clear();
		_log(_threadName + " is stopped");
	}

	private void usage(){
		_log("File Server usage:");
		_log("-q: request the Master's status");
		_log("-s: shutdown the current file server");
		_log("-z: close the socket (this will close the server too)");
		_log("-h: show usage");
		_log("-p: set the priority of this server ("+DropboxConstants.MIN_PRIO+
    			"-"+DropboxConstants.MAX_PRIO+")");
		_log("-d: toggle debug mode");
		_log("-a: add a client (-a 'name' 'password'(optional)) (should be deprecated when client can connect to file server)");
		_log("-r: remove a client (-r 'name')(should be deprecated when client can connect to file server)");
		_log("-ls: print the status of the file server");
		_log("-w: change password (-a 'name' 'old password' 'password')");
	}
	
	protected void sendClose() throws Exception{
		NetComm.send(ProtocolConstants.PACK_STR_CLOSE_HEAD, _out);
		// No need to get reply here
	}
	
	protected boolean sendAdd(String name) throws Exception{
		ClientNode cn = _server.getClients().get(name);
		assert cn != null;
		
		String dir = cn.getDir();
		String password = cn.getPassword();
		assert dir != null;
		
		String str = ProtocolConstants.PACK_STR_ADD_CLIENT_HEAD;
		str = str + " " + name + " " + password;
		String reply = NetComm.sendAndRecv(str, _out, _in);
		if(reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
			return true;
		}else{
			return false;
		}
	}
	
	protected boolean sendRemove(String name) throws Exception{
		String str = ProtocolConstants.PACK_STR_REMOVE_CLIENT_HEAD;
		str = str + " " + name;
		String reply = NetComm.sendAndRecv(str, _out, _in);
		if(reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
			return true;
		}else{
			return false;
		}
	}
	
	protected boolean sendPriority() throws Exception{
		String str = ProtocolConstants.PACK_STR_SET_PRIO_HEAD;
		str = str + " " + _server.getPrio();
		String reply = NetComm.sendAndRecv(str, _out, _in);
		//_dlog("sended");
		if(reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
			//_dlog("confirmed");
			return true;
		}else{
			return false;
		}
	}
	
	protected boolean sendChangePassword(String name, String password) throws Exception{
		String str = ProtocolConstants.PACK_STR_CHANGE_PWD_HEAD;
		str = str + " " + name + " " + password;
		String reply = NetComm.sendAndRecv(str, _out, _in);
		if(reply.equals(ProtocolConstants.PACK_STR_CONFIRM_HEAD)){
			return true;
		}else{
			return false;
		}
	}
	
	protected void sendRequest() throws Exception {
		String str = ProtocolConstants.PACK_STR_REQUEST_FS_HEAD;
		NetComm.send(str, _out);
		String masterStatus = NetComm.receive(_in);
		_log(masterStatus);
	}
	
	protected void parse(String str) throws Exception{
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens() == 1){
			if(str.equals("-q")){
				// request the master's status
				sendRequest();
			}else if(str.equals("-s")){
				// Shut down the server brutally
				System.exit(0);
			}
			else if(str.equals("-z")){
				sendClose();
				stop();
			}else if(str.equals("-h")){
				usage();
			}else if(str.equals("-d")){
				_server.toogleDebug();
			}
			else if(str.equals("-ls")){
				_server.printStatus();
			}
			else{
				_elog("Invalid input");
			}
		}
		else if(st.countTokens() >= 2){
			String tmp = st.nextToken();
			if(tmp.equals("-p")){
				if(st.countTokens() > 1){
					_elog("Invalid input");
					return;
				}
				int prio = Integer.parseInt(st.nextToken());
				if(prio < DropboxConstants.MIN_PRIO){
					_elog("Priority is too small");
					return;
				}else if(prio > DropboxConstants.MAX_PRIO){
					_elog("Priority is too large");
					return;
				}
				_server.setPrio(prio);
				if(sendPriority() == true){
					if(_server.debugMode())
						_server.printStatus();
				}else{
					_elog("Not confirmed, dangerous because you have changed the priority locally");
				}
			}
			else if(tmp.equals("-r")){
				if(st.countTokens() > 1){
					_elog("Invalid input");
					return;
				}
				String name = st.nextToken();
				if(_server.removeClient(name) == false){
					_elog("Error in local remove");
					// TODO: add ?
				}else{
					if(sendRemove(name) == true){
						_log("Success");
					}else{
						_elog("Not confirmed: dangerous because you have removed locally");
					}
				}	
			}
			else if(tmp.equals("-a")){
				if(st.countTokens() > 2){
					_elog("Invalid input");
					return;
				}
				String name = st.nextToken();
				String passWord = DropboxConstants.DEFAULT_PWD;
				if(st.hasMoreTokens()){
					passWord = st.nextToken();
				}
				if(_server.addClient(name, passWord) == false){
					_elog("Error in local add");
					// TODO: remove ?
				}else{
					if(sendAdd(name) == true){
						_log("Success");
					}else{
						_elog("Not confirmed: dangerous because you have added locally");
					}
				}	
			}
			else if(tmp.equals("-w")){
				if(st.countTokens() != 3){
					_elog("Invalid input");
					return;
				}
				String name = st.nextToken();
				String oldPassword = st.nextToken();
				String password = st.nextToken();
				if(_server.changePassword(name, oldPassword, password)){
					if(sendChangePassword(name, password)){
						_log("Change password success.");
					}else{
						_elog("Not confiemd: dangerous because you have changed the password locally");
					}
				}
				else{
					_elog("Failed");
				}
			}
			else {
				_elog("Invalid input");
			}
		}
		else{
			_elog("Invalid input");
		}
	}
}
