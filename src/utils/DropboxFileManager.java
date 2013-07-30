package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import common.DropboxConstants;
import common.DummyFile;
import common.FileOperation;
import common.ProtocolConstants;
/**
 * Package: client
 * Class: DropboxClientFileManager
 * Description: Responsible for getting file info
 */
public class DropboxFileManager {
	
	private boolean _debug;
	private String _home;
	private HashMap<String, DummyFile> _fileMap;
	private HashMap<String, DummyFile> _prevFileMap;
	
	private HashMap<String, FileOperation> _receivedFileMap;
	
	private void showDirRecursive(File dir, int level){
		if(dir.isDirectory()){
			File []dirContents = dir.listFiles();
			for( int i = 0; i < dirContents.length; i++ ){
				for( int j= 0; j < level; j++ )
					System.out.print(' ');
				
				if(dirContents[i].isDirectory()){
					System.out.println(dirContents[i].getName() + "(Dir)");
					showDirRecursive(dirContents[i],level+1);
				}
				else{
					System.out.println(dirContents[i].getName());
				}
			}
		}
	}
	public void showDir(){
		File root = new File(_home);
		if(!root.isDirectory())
			System.err.println("Your home dir is invalid");
		else
			showDirRecursive(root, 0);
	}
	
	public HashMap<String, DummyFile> buildFileMapRecursive(File dir){
		File []files = dir.listFiles();
		HashMap<String, DummyFile> curDirFileMap = new HashMap<String, DummyFile>();
		if(!dir.getPath().equals(_home)){
			DummyFile df = new DummyFile(dir.isDirectory(), dir);
			df.setLastModifiedTime(dir.lastModified());
			curDirFileMap.put(getRelativeRootPath(dir), df);
		}
		for( int i = 0; i < files.length; i++ ){
			if(files[i].isDirectory()){
				curDirFileMap.putAll(buildFileMapRecursive(files[i]));				
			}
			else{
				DummyFile df = new DummyFile(files[i].isDirectory(), files[i]);
				df.setLastModifiedTime(files[i].lastModified());
				curDirFileMap.put(getRelativeRootPath(files[i]), df);
			}
		}
		return curDirFileMap;
	}
	@SuppressWarnings("unchecked")
	public void buildFileMap(){
		
		File root = new File(_home);
		if(!root.isDirectory() || !root.exists())
			System.err.println("Your home dir is invalid");
		else{
			
			if(_fileMap != null)
			{
				_prevFileMap = (HashMap<String, DummyFile>)(_fileMap.clone());
			}
			_fileMap = buildFileMapRecursive(root);
		}
	}
	
	public boolean checkDiff(){
		return _prevFileMap.entrySet().equals(_fileMap.entrySet());
	}
	
	public void printFileMap(){
		@SuppressWarnings("rawtypes")
		Iterator it = _fileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, DummyFile> entry = (Map.Entry<String, DummyFile>)it.next();
			String fileName = entry.getKey();
			System.out.println(fileName);
		}
		
		System.out.println("Prev fileMap is:");
		//@SuppressWarnings("rawtypes")
		it = _prevFileMap.entrySet().iterator();
		while(it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, DummyFile> entry = (Map.Entry<String, DummyFile>)it.next();
			String fileName = entry.getKey();
			System.out.println(fileName);
		}
	}
	
	public void receiveFileMap(HashMap<String, FileOperation> rFileMap){
		_receivedFileMap = rFileMap;
	}
	
	public void printReceivedFileMap(){
		printReceivedFileMap(_receivedFileMap);
	}
	
	public void printReceivedFileMap(HashMap<String, FileOperation> fileMap){
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
	

	public synchronized void processReceivedFileMap(){
		if(_receivedFileMap != null){
			processReceivedFileMap(_receivedFileMap);
		}
	}
	
	private synchronized void processReceivedFileMap(HashMap<String, FileOperation> fileMap){
		// Firstly deal with directory
		//try{
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
					// This will cause exception if remove
					//fileMap.remove(key);
				}
				else if(fo.getOperation() == ProtocolConstants.OP_DEL && file.exists()){
					deleteSubDirs(file);
					// This will cause exception if remove
				//	fileMap.remove(key);
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
					try{
						if(!file.exists())		
							file.createNewFile();
						byte bytes[] = fo.getBytes();
						if(bytes != null){

							FileOutputStream fs = new FileOutputStream(file);
							fs.write(bytes);
						}
					}catch(IOException e){
						System.err.println("Error occurs when wrtie file");
						if(_debug)
							e.printStackTrace();
					}
				}else if(fo.getOperation() == ProtocolConstants.OP_DEL){
					if(file.exists())
						file.delete();
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
	
	public String getRelativeRootPath(File file){
		return file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(_home)+_home.length());
	}
	/**
	 * Constructor
	 */
	public DropboxFileManager(){
		_home = DropboxConstants.DROPBOX_TEST_DIRECTORY;
		_fileMap = new HashMap<String, DummyFile>();
		_prevFileMap = new HashMap<String, DummyFile>();
	}
	
	/**
	 * Constructor
	 * @param home: The home directory
	 */
	public DropboxFileManager(String home, boolean debug){
		_debug = debug;
		_home = home;
		_fileMap = new HashMap<String, DummyFile>();
		_prevFileMap = new HashMap<String, DummyFile>();
	}
	
	/**
	 * Getters
	 */
	public String getHomeDir(){
		return _home;
	}
	
	public HashMap<String, DummyFile> getFileMap(){
		return _fileMap;
	}
	
	public HashMap<String, DummyFile> getPrevFileMap(){
		return _prevFileMap;
	}
	
	public String getHome(){
		return _home;
	}
}
