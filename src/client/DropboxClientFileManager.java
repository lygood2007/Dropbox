package client;

import java.io.File;
import java.util.*;

import common.DropboxConstants;
import common.DummyFile;
/**
 * Package: client
 * Class: DropboxClientFileManager
 * Description: Responsible for getting file info
 */
public class DropboxClientFileManager {
	
	private String _home;
	private HashMap<String, DummyFile> _fileMap;
	private HashMap<String, DummyFile> _prevFileMap;
	
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
		if(!dir.getPath().equals(DropboxConstants.DROPBOX_CLIENT_ROOT)){
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
		if(!root.isDirectory())
			System.err.println("Your home dir is invalid");
		else{
			
			if(_fileMap != null)
			{
				
				_prevFileMap = (HashMap<String, DummyFile>)(_fileMap.clone());
			}
			_fileMap = buildFileMapRecursive(root);
		}
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
	}
	
	public String getRelativeRootPath(File file){
		return file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(_home)+_home.length());
	}
	/**
	 * Constructor
	 */
	public DropboxClientFileManager(){
		_home = DropboxConstants.DROPBOX_CLIENT_ROOT;
		_fileMap = new HashMap<String, DummyFile>();
		_prevFileMap = new HashMap<String, DummyFile>();
	}
	
	/**
	 * Constructor
	 * @param home: The home directory
	 */
	public DropboxClientFileManager(String home){
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
}
