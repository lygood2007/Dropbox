// File Name GreetingServer.java

import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.io.*;

public class GreetingServer extends Thread
{
   private ServerSocket serverSocket;
   
   public GreetingServer(int port) throws IOException
   {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(10000);
   }

   public void run()
   {
      while(true)
      {
         try
         {
            System.out.println("Waiting for client on port " +
            serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();
            System.out.println("Just connected to "
                  + server.getRemoteSocketAddress());
            GZIPInputStream gzipin = new 
            		  GZIPInputStream(server.getInputStream());
            ObjectInputStream in = new ObjectInputStream(gzipin);
            System.out.println(in.readUTF());
            GZIPOutputStream gzipout = new 
            		  GZIPOutputStream(server.getOutputStream());
            ObjectOutputStream out = new 
            		  ObjectOutputStream(gzipout);
            out.writeUTF("Thank you for connecting to "
              + server.getLocalSocketAddress() + "\nGoodbye!");
            gzipout.finish(); 
            server.close();
         }catch(SocketTimeoutException s)
         {
            System.out.println("Socket timed out!");
            break;
         }catch(IOException e)
         {
            e.printStackTrace();
            break;
         }
      }
   }
   public static void main(String [] args)
   {
      int port = Integer.parseInt(args[0]);
      try
      {
         Thread t = new GreetingServer(port);
         t.start();
      }catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
