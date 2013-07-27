package client;

import java.io.*;
import java.net.*;
import java.util.*;
import common.ProtocolConstants;

/**
 * Class: DropboxClientStreamWriter
 * Description: Responsible for writing data into stream
 */
public class DropboxClientStreamWriter {

	boolean _debug;
	DataOutputStream _os;
	DropboxClientFileManager _fm;
	
	private class FileOperation{
		private byte _operation;
		private File _file;
		
		FileOperation(){
			_operation = ProtocolConstants.OP_NULL;
			_file = null;
		}
		
		FileOperation(byte operation, File file){
			_operation = operation;
			_file = file;
		}
		
		public File getFile(){
			return _file;
		}
		
		public byte getOperation(){
			return _operation;
		}
	}
	
	public void writeFromFileMap(HashMap<String, File> fileMap, HashMap<String, File> prevFileMap) throws IOException {
		HashMap<String, FileOperation> operations = new HashMap<String, FileOperation>();
		
 		int fileNum = fileMap.size();
		Iterator it = fileMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, File> entry = (Map.Entry<String, File>)(it.next());
			String key = entry.getKey();
			File file = entry.getValue();
			File prev = prevFileMap.get(key);
			if( prev != null ){
				long modified = file.lastModified();
				long prevModified = file.lastModified();
				if( prevModified != modified ){
					// Need to update
					operations.put(key, new FileOperation(ProtocolConstants.OP_MOD, file));
				}
			}
			else{
				// Need to add
				operations.put(key, new FileOperation(ProtocolConstants.OP_ADD, file));
			}
		}
		
		it = prevFileMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, File> entry = (Map.Entry<String, File>)(it.next());
			String key = entry.getKey();
			File file = entry.getValue();
			File cur = fileMap.get(key);
			if(cur == null){
				// Need to delete
				operations.put(key, new FileOperation(ProtocolConstants.OP_DEL, file));
			}
		}
		
		writeOperations(operations);
	}
	
	public void writeOperations(HashMap<String, FileOperation> operations){
		try{
			writePackage();
			writeFileNum(operations.size());
			Iterator it = operations.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, FileOperation> entry = (Map.Entry<String, FileOperation>)(it.next());
				String key = entry.getKey();
				FileOperation op = entry.getValue();
				File file = op.getFile();
				
				writeFileName(key);
				writeOperation(op.getOperation());
				writeFileFlag(file.isDirectory());
				if(!file.isDirectory() &&
						(op.getOperation() == ProtocolConstants.OP_ADD ||
						 op.getOperation() == ProtocolConstants.OP_MOD)){
					// We need to write the file content
					//FileInputStream is = new FileInputStream(file);
					//writeFileLength(file.length());
					//writeFileContent(is);
				}
			}
		}catch(IOException e){
			System.err.println("Wrtie stream error");
			if(_debug)
				e.printStackTrace();
		}
	}
	public void writeOneFile() throws IOException {
		
	}
	public void writeFileNum (int size) throws IOException {
		if(_os != null){
			_os.writeInt(size);
			_os.flush();
		}
	}
	public void writeTest(long test) throws IOException {
		if(_os != null){
			_os.writeLong(test);
			_os.flush();
		}
	}
	
	public void writeOperation(byte operation) throws IOException {
		if(_os != null){
			_os.writeByte(operation);
			_os.flush();
		}
	}
	
	public void writeFileFlag(boolean isDir) throws IOException {
		if(_os != null){
			_os.writeBoolean(isDir);
			_os.flush();
		}
	}
	
	public void writeFileName(String fileName) throws IOException {
		byte []bytes = fileName.getBytes();
		if(_os != null){
			_os.writeInt(bytes.length);
			_os.writeBytes(fileName);
			_os.flush();
		}
		
	}
	
	public void writeFileLength(long length) throws IOException {
		if(_os != null){
			_os.writeLong(length);
		}
	}
	
	public void writeFileContent(FileInputStream is) throws IOException {
		if(_os != null){
			byte b[] = new byte[1];
			while((is.read(b)) != -1){
				_os.writeByte(b[0]);
			}
			_os.flush();
		}
	}
	
	public void writePackage() throws IOException{
		if(_os != null){
			_os.writeInt(ProtocolConstants.PACK_BEGIN);
		}
	}
	/**
	 * Constructor
	 */
	public DropboxClientStreamWriter(){
		_debug = false;
		_os = null;
		_fm = null;
	}
	/**
	 * Constructor
	 * @param debug: Debug mode?
	 * @param sock: Socket
	 */
	public DropboxClientStreamWriter(boolean debug,Socket sock, DropboxClientFileManager fm){
		try{
		_os = new DataOutputStream(sock.getOutputStream());
		_fm = fm;
		}catch(IOException e){
			System.err.println("Error occurrs when getting outputstream");
			if( _debug ){
				e.printStackTrace();
			}
		}
	}
}
