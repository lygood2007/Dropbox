// File Name GreetingClient.java

import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;

public class GreetingClient
{
   public static void main(String [] args)
   {
      String serverName = args[0];
      int port = Integer.parseInt(args[1]);
      try
      {
         System.out.println("Connecting to " + serverName
                             + " on port " + port);
         Socket client = new Socket(serverName, port);
         System.out.println("Just connected to "
                      + client.getRemoteSocketAddress());
         OutputStream outToServer = client.getOutputStream();
         GZIPOutputStream gzipout = new 
        		  GZIPOutputStream(outToServer);
         ObjectOutputStream out = new 
        		  ObjectOutputStream(gzipout);
         out.writeUTF("Hello from "
                      + client.getLocalSocketAddress());
         gzipout.finish(); 
         InputStream inFromServer = client.getInputStream();
         GZIPInputStream gzipin = new 
        		  GZIPInputStream(inFromServer);
         ObjectInputStream in = new ObjectInputStream(gzipin);
         System.out.println("Server says " + in.readUTF());
         client.close();
      }catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}