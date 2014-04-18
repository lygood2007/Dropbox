package master;

import java.io.*;
import java.net.*;

import common.*;
/**
 * 
 * class MasterServerFileServerRequest
 * Description: Actually just responsible for sending heartbeat message to
 *              get the status of file servers
 */
public class MasterServerFileServerRequest extends ThreadBase{

	private MasterServer _server;
	private PrintWriter _out;
	private BufferedReader _in;
	private Socket _sock;
	private FileServerNode _node;
	private void _dlog(String str){
		if(_server.debugMode())
			System.out.println("[MasterServerFileServerRequest (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[MasterServerFileServerRequest (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[MasterServerFileServerRequest]:" + str);
	}
	
	public MasterServerFileServerRequest(MasterServer server, PrintWriter out,
					BufferedReader in, Socket sock, FileServerNode node){
		super("MasterServerFileServerRequest",server.debugMode());
		_server = server;
		assert _server != null;
		_sock = sock;
		_in = in;
		_out = out;
		_node = node;
	}

	
	protected void parse(String str){
		if(str.equals(ProtocolConstants.PACK_STR_HEARTBEAT_HEAD)){
			_dlog("HearBeat confirmed");
		}else{
			_elog("Invalid message");
		}
	}
	
	protected void sendHeartBeat() throws Exception{
		// TODO: add more story here
		/*NetComm.send(ProtocolConstants.PACK_STR_HEARTBEAT_HEAD,_out);
		String reply = NetComm.receive(_in);*/
		String reply = NetComm.sendAndRecv(ProtocolConstants.PACK_STR_HEARTBEAT_HEAD, _out, _in);
		parse(reply);
	}
	
	public void run()
	{
		Thread thisThread = Thread.currentThread();
		try{
			while(thisThread == _t){ // Cancel point
				synchronized (this){
					while(_suspended){ // Suspension point
						wait();
					}
				}
				Thread.sleep(DropboxConstants.HEART_BEAT_HZ);
				sendHeartBeat();
			}
		}catch(InterruptedException e){
			if(!_server.noException()){
				_elog(e.toString());
			}
			if(_server.debugMode()){
				e.printStackTrace();
			}
		}catch(Exception e){
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
	
	protected void clear(){
		_dlog("Do clear..");
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
			if(_server.debugMode())
				e.printStackTrace();
		}
		if(_node != null && _node.isAlive())
			_node.destroy();
		_node = null;
		_dlog("Finished");
	}
}
