package common;

import java.io.*;

/**
 * 
 * class NetBase
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
		
}
