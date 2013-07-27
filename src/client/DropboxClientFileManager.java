package client;

import java.io.File;
import java.util.*;

import common.DropboxConstants;

/**
 * Class: DropboxClientFileManager
 * Description: Responsible for getting file info
 */
public class DropboxClientFileManager {
	
	private String _home;
	private HashMap<String, File> _fileMap;
	private HashMap<String, File> _prevFileMap;
	
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
	
	public HashMap<String, File> buildFileMapRecursive(File dir){
		File []files = dir.listFiles();
		HashMap<String, File> curDirFileMap = new HashMap<String, File>();
		if(!dir.getPath().equals(DropboxConstants.DROPBOX_TEST_DIRECTORY))
			curDirFileMap.put(getRelativePath(dir), dir);
		for( int i = 0; i < files.length; i++ ){
			if(files[i].isDirectory()){
				curDirFileMap.putAll(buildFileMapRecursive(files[i]));				
			}
			else{
				String a = files[i].getAbsolutePath();
				curDirFileMap.put(getRelativePath(files[i]), files[i]);
			}
		}
		return curDirFileMap;
	}
	
	public void buildFileMap(){
		File root = new File(_home);
		if(!root.isDirectory())
			System.err.println("Your home dir is invalid");
		else{
			if(_fileMap != null)
				_prevFileMap = (HashMap<String, File>)(_fileMap.clone());
			_fileMap = buildFileMapRecursive(root);
		}
	}
	public void printFileMap(){
		Iterator it = _fileMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, File> entry = (Map.Entry<String, File>)it.next();
			File file = entry.getValue();
			String fileName = entry.getKey();
			//if(!file.isDirectory())
				//System.out.println(fileName + file.getName());
			//else
				System.out.println(fileName);
		}
	}
	
	public String getRelativePath(File file){
		return file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(_home)+_home.length());
	}
	/**
	 * Constructor
	 */
	public DropboxClientFileManager(){
		_home = DropboxConstants.DROPBOX_TEST_DIRECTORY;
		_fileMap = new HashMap<String, File>();
		_prevFileMap = new HashMap<String, File>();
	}
	
	/**
	 * Constructor
	 * @param home: The home directory
	 */
	public DropboxClientFileManager(String home){
		_home = home;
		_fileMap = new HashMap<String, File>();
		_prevFileMap = new HashMap<String, File>();
		buildFileMap();
	}
	
	/**
	 * Getters
	 */
	public String getHomeDir(){
		return _home;
	}
	
	public HashMap<String, File> getFileMap(){
		return _fileMap;
	}
	
	public HashMap<String, File> getPrevFileMap(){
		return _prevFileMap;
	}
}
