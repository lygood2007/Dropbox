/**
 * File: GeneralServer.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

import java.io.*;
import java.net.*;
/**
 * 
 * class GeneralServer
 * Description: The base class for a general server interface
 */
public abstract class GeneralServer extends ThreadBase{

	protected ServerSocket _serverSocket;
	protected boolean _debug;
	protected boolean _noException;
	
	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	private void _dlog(String str){
		if (_debug)
			System.out.println("[GeneralServer (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	private static void _elog(String str){
		System.err.println("[GeneralServer (ERROR)]:" + str);
	}
	
	/**
	 * Constructor
	 * @param name: the name of the server
	 * @param noException: no exception?
	 * @param debug: debug?
	 */
	public GeneralServer(String name, boolean noException, boolean debug){
		super(name, debug);
		_noException = noException;
		_debug = debug;
	}
	
	/**
	 * stop: stop the thread
	 */
	@Override
	public void stop(){
		if (_suspended == true){
			_elog("Cannot stop when suspending");
			return;
		}
		_t = null;
		/* Interrupt socket */
		if (_serverSocket != null){
			try{
				_serverSocket.close();
			}catch(IOException e){
				if (!_noException){
					_elog(e.toString());
				}
				if (_debug){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * clear: clear resources
	 */
	protected void clear(){
		_dlog("Do clear...");
		try{
			if (_serverSocket != null){
				_serverSocket.close();
			}
			_serverSocket = null;
		}catch(IOException e){
			if (!_noException){
				_elog(e.toString());
			}
			if (_debug){
				e.printStackTrace();
			}
		}
		
		_dlog("Finished");
	}
}
