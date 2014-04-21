package utils;

import java.io.*;
import java.net.*;
import java.util.*;

import common.*;

/**
 *
 * Class: DropboxClientStreamWriter
 * Description: Responsible for writing data into stream
 */
public class SyncStreamWriter {

	private boolean _debug;
	private DataOutputStream _os;
	private String _home;
	
	private void _dlog(String str){
		if(_debug)
			System.out.println("[SyncStreamWriter (DEBUG)]:" + str);
	}
	
	private static void _elog(String str){
		System.err.println("[SyncStreamWriter (ERROR)]:" + str);
	}
	
	private static void _log(String str){
		System.out.println("[SyncStreamWriter]:" + str);
	}
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
	
	public void writeOperations(HashMap<String, FileOperation> operations) throws IOException{

		writeHomeDirectory(_home);		
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
			_os.flush();
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

	public void writePackageHeader(int type) throws IOException {
		if(_os != null){
			_os.writeInt(type);
			_os.flush();
		}
	}
	
	public void writeLastModifiedTime(long time) throws IOException{
		if(_os != null){
			_os.writeLong(time);
			_os.flush();
		}
	}
	
	public void writeHomeDirectory(String home) throws IOException{
		byte []bytes = home.getBytes();
		if(_os != null){
			_os.writeInt(bytes.length);
			_os.write(bytes);
			_os.flush();
		}
	}

	/**
	 * Constructor
	 * @param debug: Debug mode?
	 * @param sock: Socket
	 */
	public SyncStreamWriter(String home, DataOutputStream os, boolean debug){
		_home = home;
		_debug = debug;
		_os = os;
	}
	
	public DataOutputStream getOutputStream(){
		return _os;
	}
	
	public void closeStream() throws IOException{
		try{
			_os.close();
		}catch(IOException e){
			throw e;
		}
	}
}
