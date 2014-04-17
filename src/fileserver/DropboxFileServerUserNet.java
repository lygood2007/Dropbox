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
	
	private void _elog(String str){
		if(!_server.noException())
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
			_elog(e.toString());
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
				//Thread.sleep(100);
				synchronized(this) {
					while(_suspended) { // Suspension point
						wait();
					}
				}
				System.out.println("Your input:");
				String s = in.nextLine();// Read from user input
				parse(s);
				NetComm.send(s,_out);
			}
		}catch(InterruptedException e){ // Cancel point
			_elog(e.toString());
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}catch(Exception e){ // Always last
			_elog(e.toString());
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
		_log("-a: add a client (-a 'name') (should be deprecated when client can connect to file server)");
		_log("-r: remove a client (-r 'name')(should be deprecated when client can connect to file server)");
		_log("-ls: print the status of the file server");
	}
	
	protected void sendClose() throws Exception{
		NetComm.send(ProtocolConstants.PACK_STR_CLOSE_HEAD, _out);
	}
	
	protected void parse(String str) throws Exception{
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens() == 1){
			if(str.equals("-q")){
				// request the master's status
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
		else if(st.countTokens() == 2){
			String tmp = st.nextToken();
			if(tmp.equals("-p")){
				_server.setPrio(Integer.parseInt(st.nextToken()));
				_server.printStatus();
			}
			else if(tmp.equals("-r")){
				if(_server.removeClient(st.nextToken()) == true){
					_elog("Error in remove");
				}else{
					_log("Success!");
				}
			}
			else if(tmp.equals("-a")){
				if(_server.addClient(st.nextToken()) == false){
					//TODO: should return to the client and tell him the failure
					//      normally it will always succeed
					_elog("Error in add");
				}else{
					_log("Success");
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
