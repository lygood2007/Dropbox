/**
 * File: NetComm.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

import java.io.*;

/**
 * 
 * class NetComm
 * Description: the layer to provide some common used functions
 */
public class NetComm{
	
	public static void send(String str, PrintWriter out) throws Exception{
		assert str != null;
		if(out.checkError()){
			throw new Exception("Socket is closed");
		}
		out.println(str);
	}
	
	public static String receive(BufferedReader in) throws Exception{
		String from = null;
		try{
			if((from = in.readLine()) == null)
				throw new Exception("No response received");
		}catch(Exception e){ 
			// So what does this mean, it usually means the connection is broken
			throw new Exception("Connection is broken");
		}
		return from;
	}
	
	/**
	 * sendAndRecv: send the string to the print writer and read the response right away.
	 * @param str: the string to send
	 * @param out: the print writer
	 * @param in: the buffered reader
	 * @return: the reply string
	 */
	public static synchronized String sendAndRecv(String str, PrintWriter out, BufferedReader in) throws Exception{
		send(str, out);
		String reply = receive(in);
		return reply;
	}
}
