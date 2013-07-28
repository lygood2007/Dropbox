package client;

import java.io.*;
import java.net.*;
import java.util.*;
import common.*;

/**
 * Package: client
 * Class: DropboxClientStreamWriter
 * Description: Responsible for writing data into stream
 */
public class DropboxClientStreamWriter {

	boolean _debug;
	DataOutputStream _os;
	DropboxClientFileManager _fm;
	
	public void writeFromFileMap(HashMap<String, DummyFile> fileMap, HashMap<String, DummyFile> prevFileMap) throws IOException {
		HashMap<String, FileOperation> operations = new HashMap<String, FileOperation>();
		
		@SuppressWarnings("rawtypes")
		Iterator it = fileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, DummyFile> entry = (Map.Entry<String, DummyFile>)(it.next());
			String key = entry.getKey();
			DummyFile file = entry.getValue();
			DummyFile prev = prevFileMap.get(key);
			if( prev != null ){
				//File f = file.getFile();
				//File prevf = prev.getFile();
				long modified = file.getLastModifiedTime();
				long prevModified = prev.getLastModifiedTime();
				if( prevModified != modified ){
					// Need to update
					operations.put(key,
							new FileOperation(ProtocolConstants.OP_MOD, file));
				}
			}
			else{
				// Need to add
				operations.put(key,
						new FileOperation(ProtocolConstants.OP_ADD, file));
			}
		}
		
		it = prevFileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, DummyFile> entry = (Map.Entry<String, DummyFile>)(it.next());
			String key = entry.getKey();
			DummyFile file = entry.getValue();
			DummyFile cur = fileMap.get(key);
			if(cur == null){
				// Need to delete
				operations.put(key,
						new FileOperation(ProtocolConstants.OP_DEL, file));
			}
		}
		
		writeOperations(operations);
	}
	
	public void writeOperations(HashMap<String, FileOperation> operations){
		try{
			writePackage();
			//writeFileNum(1);
			writeFileNum(operations.size());
			@SuppressWarnings("rawtypes")
			Iterator it = operations.entrySet().iterator();
			while(it.hasNext()){
				@SuppressWarnings("unchecked")
				Map.Entry<String, FileOperation> entry = (Map.Entry<String, FileOperation>)(it.next());
				String key = entry.getKey();
				FileOperation op = entry.getValue();
				File file = op.getDummyFile().getFile();
				
				writeFileName(key);
				writeLastModifiedTime(file.lastModified());
				writeOperation(op.getOperation());
				writeFileFlag(op.getDummyFile().isDir());
				if(!file.isDirectory() &&
						(op.getOperation() == ProtocolConstants.OP_ADD ||
						 op.getOperation() == ProtocolConstants.OP_MOD)){
					// We need to write the file content
					FileInputStream is = new FileInputStream(file);
					writeFileLength(file.length());
					writeFileContent(is);
				}
			}
		}catch(IOException e){
			System.err.println("Wrtie stream error");
			if(_debug)
				e.printStackTrace();
		}
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
			_os.write(bytes);
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
	
	public void writeLastModifiedTime(long time) throws IOException{
		if(_os != null)
			_os.writeLong(time);
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
