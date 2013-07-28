package server;

import common.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Package: server
 * Class: ParserException
 * Description: Exception when error occurs in parser
 */
class ParserException extends Exception{
	public ParserException(String message){
		super(message);
	}
	
	public ParserException(){
		super();
	}
}
	
/**
 * 
 * Package: server
 * Class: DropboxCliendHandler
 * Description: Handle the client
 */
class DropboxClientHandler implements Runnable{
	
	Socket _sock;
	DataInputStream _is;
	boolean _debug;
	
	public DropboxClientHandler(Socket sock, boolean debug){
		_sock = sock;
		_debug = debug;
		try{
			_is = new DataInputStream(_sock.getInputStream());
		}catch(IOException e){
			System.out.println("IO error");
		}
	}
	
	// Parse the stream got from socket
	public void streamParser() throws IOException, ParserException {
		if(_is != null){
			
			int packBegin = _is.readInt();
			if(packBegin == ProtocolConstants.PACK_BEGIN){
				// Valid stream
				
				int fileNum = _is.readInt();
				if(_debug)
					System.out.println("Filenum: " + fileNum);
				
				// The hashmap is used for storing the structure
				HashMap<String, FileOperation> fileMap = new HashMap<String, FileOperation>();
				for( int i = 0; i < fileNum; i++ ){
					// Read each file
					readEach(_is, fileMap);
				}
				
				if(_debug)
					printFileMap(fileMap);
				
				processFileMap(fileMap);
			}
		}
			
	}
	
	public void run(){
		if( _is != null && _sock != null ){
			try{
				streamParser();
			}catch(IOException e){
				System.err.println("Read error");
			}catch(ParserException e){
				System.err.println("Parsing error");
			}
		}
	}
	
	public void readEach(DataInputStream is, HashMap<String, FileOperation> fileMap) throws IOException {
		if(is != null){
			int nameLength = is.readInt();
			if(_debug){
				System.out.println("Name length: " + nameLength);
			}
			byte []nameBytes = new byte[nameLength];
			is.read(nameBytes);
			String fileName = new String(nameBytes);
		
			long lastModifiedTime = is.readLong();
			byte operation = is.readByte();
			
			// Push the file operation into hashmap
			
			boolean flag = is.readBoolean();
			DummyFile f = new DummyFile(flag,
					new File(DropboxConstants.DROPBOX_DIRECTORY+fileName));
			
			FileOperation fo = new FileOperation(operation, f);
			fileMap.put(fileName, fo);
			long fileLength = 0;
			byte []fileBytes = null;
			
			if(flag == false &&
					(operation == ProtocolConstants.OP_ADD ||
					operation == ProtocolConstants.OP_MOD)){
				fileLength = is.readLong();
				
				/**
				 * CAUTION:
				 * HERE IT MEANS IT DOES NOT SUPPORT LARGE FILE
				 * REVISE THIS LATER
				 */
				fileBytes = new byte[(int)fileLength];
				//is.read(fileBytes);
				int i = 0;
				while(i < fileLength){
					fileBytes[i] = is.readByte();
					i++;
				}
				fo.setBytes(fileBytes);
			}
				// Print the data we get
			if(_debug)
				System.out.println("Filename: "+fileName + "(" + flag +")"
						+ " Operation: " + operation + " LastTime: " + lastModifiedTime);
			if(fileBytes != null){
				String fileContent = new String(fileBytes);
				System.out.println(fileContent);
			}
		}
	}

	public void printFileMap(HashMap<String, FileOperation> fileMap){
		
		System.out.println("The filemap we got is: ");
		@SuppressWarnings("rawtypes")
		Iterator it = fileMap.entrySet().iterator();
		int i = 0;
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, FileOperation> entry = (Map.Entry<String, FileOperation>)it.next();
			String key = entry.getKey();
			FileOperation fo = entry.getValue();
			System.out.println("[Entry" + i + "] " + key + " " + FileOperation.getOperationString(fo.getOperation()));
			byte bytes[] = fo.getBytes();
			if(bytes != null){
				System.out.println("[FileContent]");
				System.out.println(new String(bytes));
			}
			i++;
		}
	}
	
	public void processFileMap(HashMap<String, FileOperation> fileMap) throws IOException{
		// Firstly deal with directory
		@SuppressWarnings("rawtypes")
		Iterator it = fileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, FileOperation> entry = (Map.Entry<String, FileOperation>)it.next();
			String key = entry.getKey();
			FileOperation fo = entry.getValue();
			File file = fo.getDummyFile().getFile();
			if(fo.getDummyFile().isDir()){
				if(fo.getOperation() == ProtocolConstants.OP_ADD
					&&!file.exists()){
					file.mkdirs();
					fileMap.remove(key);
				}
				else if(fo.getOperation() == ProtocolConstants.OP_DEL && file.exists()){
					deleteSubDirs(file);
					fileMap.remove(key);
				}
			}
		}
		
		// Then deal with file
		it = fileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, FileOperation> entry = (Map.Entry<String, FileOperation>)it.next();
			String key = entry.getKey();
			FileOperation fo = entry.getValue();
			File file = fo.getDummyFile().getFile();
			if(!fo.getDummyFile().isDir()){
				if(fo.getOperation() == ProtocolConstants.OP_ADD ||
						fo.getOperation() == ProtocolConstants.OP_MOD){
					if(!file.exists())		
						file.createNewFile();
					byte bytes[] = fo.getBytes();
					if(bytes != null){
						FileOutputStream fs = new FileOutputStream(file);
						fs.write(bytes);
					}
				}
			}
		}
	}
	
	public void deleteSubDirs(File file){
		if(file.isDirectory()){
			File[] list = file.listFiles();
			for(File f:list){
				if(f.isDirectory())
					deleteSubDirs(f);
				f.delete();
			}
			file.delete();
		}
	}
	// Getters
	public Socket getSocket(){
		return _sock;
	}
	
	public DataInputStream getStream(){
		return _is;
	}
}
/**
 * Package: server
 * Class: DropboxServer
 * Description: Deal with everything!
 */
public class DropboxServer implements FileSynchronizationServer {

	private int _port;
	private boolean _debug;
	private boolean _useUI;
	
    public void listen() {
    	System.out.println("Server is listening...");
    	// Now only support one client
    	Socket client = null;
    	ServerSocket serverSocket = null;
    	// Close the server in 5 seconds
    	//long startTime = System.currentTimeMillis();
    	//long elapsedTime = 0;
    	try{
    		serverSocket = new ServerSocket(_port);
    		serverSocket.setSoTimeout(100*1000);
    		while(true){
    			//elapsedTime = (new Date()).getTime() - startTime;
    			
    			client = serverSocket.accept();
    			System.out.println("Get connection from " + client.getInetAddress().getHostAddress());
    			Thread t = new Thread(new DropboxClientHandler(client, _debug));
    			t.start();
    			//DropboxClientHandler h = new DropboxClientHandler(client);
    			//h.run();
    		}
    	}catch(InterruptedIOException e){
    		System.err.println("Time out");
    		if(_debug)
    			e.printStackTrace();
    		
    	}catch(IOException e){
    		System.err.println("IO error occurs");
    		if(_debug)
    			e.printStackTrace();
    	}finally{
    		try{
    			System.out.println("Close connection from " + client.getInetAddress().getHostAddress());
    			if(serverSocket != null )
    				serverSocket.close();
    			if( client != null )
    				client.close();
    		}catch(IOException e){
    			System.err.println("IO error occurs when closing socket");
        		if(_debug)
        			e.printStackTrace();
    		}
    		
    	}
    }

    public DropboxServer(){
    	_port = DropboxConstants.SERVER_PORT;
    	_debug = false;
    	_useUI = false;
    }
    
    public DropboxServer( int port, boolean debug, boolean useUI ){
    	_port = port;
    	_debug = debug;
    	_useUI = useUI;
    }
    
    public static void main(String[] args) {
    	
    	int port = DropboxConstants.SERVER_PORT;
    	boolean debug = false;
    	boolean useUI = false;	
    	
    	System.out.println("Dropbox Server:");
    	System.out.println("-d for debug mode (default false)" );
    	System.out.println("-u to use user interface (default false)");
    	System.out.println("-p to specify port (default 5000)");
    	
    	for( int i = 0; i < args.length; i++ ){
    		if(args[i].equals("-d"))
				debug = true;
			else if(args[i].equals("-u"))
				useUI = true;
			else if(args[i].equals("-p")){
				i++;
				port = Integer.parseInt(args[i]);
			}
    	}
    	DropboxServer server = new DropboxServer(port,debug,useUI);
    	server.listen();
    }
}
