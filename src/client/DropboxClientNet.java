package client;

import java.io.*;
import java.net.*;

import common.DropboxConstants;
import common.ProtocolConstants;

/**
 * Class: DropboxClientNet
 * Description: Deal with the network connecting issues
 */
public class DropboxClientNet {

	private Socket _sock;
	private DropboxClient _client;
	
	private void _dlog(String str){
		if(_client.debugMode())
			System.out.println("[DropboxClientNet (DEBUG)]:" + str);
	}
	
	private void _elog(String str){
		System.err.println("[DropboxClientNet (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[DropboxClientNet]:" + str);
	}
	
	public boolean openConnections(String IP, int port){
		_log("Try to connect to " + IP + ":" + port + "...");
		
		int i = 0;
		while(true){
			boolean isConnected = connect(IP, port);

			if(!isConnected){
				i++;
				if(i == DropboxConstants.MAX_TRY){
					break;
				}
				_log("Cannot connect to Master, retry " + i);
				
				try{
					Thread.sleep(DropboxConstants.TRY_CONNECT_MILLIS);
				}catch(InterruptedException e){
					if(!_client.noException()){
						_elog(e.toString());
					}
					if(_client.debugMode()){
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
	
	public String identify(){
		try{
			DataInputStream in = new DataInputStream(_sock.getInputStream());
			DataOutputStream out = new DataOutputStream(_sock.getOutputStream());
			out.writeInt(ProtocolConstants.PACK_INIT_HEAD);
			out.writeChars(_client.getName());
			out.writeChar('\n');
			out.writeChars(_client.getPassword());
			out.writeChar('\n');
			
			int pack_head = in.readInt();
			if(pack_head != ProtocolConstants.PACK_FS_INFO_HEAD){
				return null;
			}
			int i = 0;
			/* read IP*/
			String ip = "";
			int IP_LENGTH = 4*3+3;
			while(i < IP_LENGTH){
				char c = in.readChar();
				if(c == '\n'){
					break;
				}else{	
					ip = ip + c;
					i++;
					if(i > IP_LENGTH){
						return null;
					}
				}
			}
			_log("File server ip assigned to you is:" + ip);
			return ip;
		}
		catch(IOException e){
			if(!_client.noException()){
				_elog(e.toString());
			}
			if(_client.debugMode()){
				e.printStackTrace();
			}
		}
		return null;
	}
	/**
	 * Connect: connect the socket to server
	 */
	public boolean connect(String IP, int port){
		try{
			_sock = new Socket(IP, port);
			_log("Successful connection to " + IP + ":" + port);
			return true;
		}catch(UnknownHostException e){
			if(!_client.noException()){
				_elog(e.toString());
			}
			if(_client.debugMode()){
				e.printStackTrace();
			}
		}catch(IOException e){
			if(!_client.noException()){
				_elog(e.toString());
			}
			if(_client.debugMode()){
				e.printStackTrace();
			}
		}
		return false;
	}

	public void closeConnection(){
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
	
	public boolean close(){
		if(_sock != null){
			try{
				_sock.close();
				_log("Closed connection to " + _client.getMasterIP() + ":" + _client.getMasterPort());
				_sock = null;
				return true;
			}catch(IOException e){
				if(!_client.noException()){
					_elog(e.toString());
				}
				if(_client.debugMode()){
					e.printStackTrace();
				}
				return false;
			}
		}else{
			return true;
		}
	}
	
	/**
	 * Constructor
	 * @param host: The IP address
	 * @param port: The port number
	 */
	public DropboxClientNet(DropboxClient client){
		assert client != null;
		_client = client;
	}
	
	/**
	 * Getters
	 */
	
	public Socket getSocket(){
		return _sock;
	}
}
