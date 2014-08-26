/**
 * File: DropboxFileManager.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import common.DummyFile;
import common.FileOperation;
import common.ProtocolConstants;

/**
 * 
 * Class: DropboxFileManager
 * Description: Responsible for managing the files
 */
public class DropboxFileManager {
	
	private boolean _debug;
	private String _home;
	private HashMap<String, DummyFile> _fileMap;
	private HashMap<String, DummyFile> _prevFileMap;
	private HashMap<String, FileOperation> _receivedFileMap;
	
	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	private void _dlog(String str){
		if (_debug)
			System.out.println("[DropboxFileManager (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	private static void _elog(String str){
		System.err.println("[DropboxFileManager (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	private static void _log(String str){
		System.out.println("[DropboxFileManager]:" + str);
	}
	
	/**
	 * showDirRecursive: show directories recursively
	 * @param dir: the current directory
	 * @param level: the level counter
	 */
	private void showDirRecursive(File dir, int level){
		if (dir.isDirectory()){
			File []dirContents = dir.listFiles();
			for( int i = 0; i < dirContents.length; i++ ){
				for( int j= 0; j < level; j++ )
					System.out.print(' ');
				
				if (dirContents[i].isDirectory()){
					_log(dirContents[i].getName() + "(Dir)");
					showDirRecursive(dirContents[i],level+1);
				}
				else{
					_log(dirContents[i].getName());
				}
			}
		}
	}
	
	/**
	 * showDir: show the directory
	 */
	public void showDir(){
		File root = new File(_home);
		if (!root.isDirectory())
			_elog("Your home dir is invalid");
		else
			showDirRecursive(root, 0);
	}
	
	/**
	 * buildFileMapRecursive: build the file map from the current directory
	 * @param dir: the directory
	 * @return: the file map
	 */
	public HashMap<String, DummyFile> buildFileMapRecursive(File dir){
		File []files = dir.listFiles();
		HashMap<String, DummyFile> curDirFileMap =
				new HashMap<String, DummyFile>();
		if (!dir.getPath().equals(_home)){
			DummyFile df = new DummyFile(dir.isDirectory(), dir);
			df.setLastModifiedTime(dir.lastModified());
			curDirFileMap.put(getRelativeRootPath(dir), df);
		}
		for( int i = 0; i < files.length; i++ ){
			if (files[i].isDirectory()){
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
	
	/**
	 * buildFileMap: build the file map
	 */
	@SuppressWarnings("unchecked")
	public void buildFileMap(){
		
		File root = new File(_home);
		if (!root.isDirectory() || !root.exists())
			_elog("Your home dir is invalid");
		else{
			
			if (_fileMap != null)
			{
				_prevFileMap = (HashMap<String, DummyFile>)(_fileMap.clone());
			}
			_fileMap = buildFileMapRecursive(root);
		}
	}
	
	/**
	 * checkDiff: check the difference between the current and previous one
	 * @return: true for equal, false for different
	 */
	public boolean checkDiff(){
		return _prevFileMap.entrySet().equals(_fileMap.entrySet());
	}
	
	/**
	 * printFileMap: print the current file map
	 */
	public void printFileMap(){
		@SuppressWarnings("rawtypes")
		Iterator it = _fileMap.entrySet().iterator();
		while (it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, DummyFile> entry = 
			(Map.Entry<String, DummyFile>)it.next();
			String fileName = entry.getKey();
			_log(fileName);
		}
		// In debug mode, we print the prev map
		if (_debug){
			_dlog("Prev fileMap is:");
			it = _prevFileMap.entrySet().iterator();
			while (it.hasNext()){
				@SuppressWarnings("unchecked")
				Map.Entry<String, DummyFile> entry =
				(Map.Entry<String, DummyFile>)it.next();
				String fileName = entry.getKey();
				_dlog(fileName);
			}
		}
	}
	
	/**
	 * receiveFileMap: setter
	 * @param rFileMap
	 */
	public void receiveFileMap(HashMap<String, FileOperation> rFileMap){
		_receivedFileMap = rFileMap;
	}
	
	/**
	 * printReceivedFileMap: print the received file map
	 */
	public void printReceivedFileMap(){
		printReceivedFileMap(_receivedFileMap);
	}
	
	/**
	 * printReceivedfileMap: print the received file map
	 * @param fileMap: the file map
	 */
	public void printReceivedFileMap(HashMap<String, FileOperation> fileMap){
		_dlog("The filemap we got is: ");
		@SuppressWarnings("rawtypes")
		Iterator it = fileMap.entrySet().iterator();
		int i = 0;
		while (it.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, FileOperation> entry =
			(Map.Entry<String, FileOperation>)it.next();
			String key = entry.getKey();
			FileOperation fo = entry.getValue();
			_log("[Entry" + i + "] " + key + " " +
				 FileOperation.getOperationString(fo.getOperation()));
			i++;
		}
	}
	

	/**
	 * processReceivedFileMap: process the received file map
	 */
	public synchronized void processReceivedFileMap() throws IOException{
		if (_receivedFileMap != null){
			processReceivedFileMap(_receivedFileMap);
		}
	}
	
	/**
	 * processReceivedFileMap: process the file map got from the connection.
	 * @param fileMap: the file map
	 */
	@SuppressWarnings("unchecked")
	private synchronized void processReceivedFileMap(
					HashMap<String, FileOperation> fileMap) throws IOException{
		@SuppressWarnings("rawtypes")
		Iterator it = fileMap.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, FileOperation> entry = 
					(Map.Entry<String, FileOperation>)it.next();
			FileOperation fo = entry.getValue();
			File file = fo.getDummyFile().getFile();
			if (fo.getDummyFile().isDir()){
				if (fo.getOperation() == ProtocolConstants.OP_ADD
					&&!file.exists()){
					file.mkdirs();
				}
				else if (fo.getOperation() == ProtocolConstants.OP_DEL &&
						file.exists()){
					deleteSubDirs(file);
				}
			}
		}
		
		it = fileMap.entrySet().iterator();
		while (it.hasNext()){
			Map.Entry<String, FileOperation> entry =
					(Map.Entry<String, FileOperation>)it.next();
			FileOperation fo = entry.getValue();
			File file = fo.getDummyFile().getFile();
			if (!fo.getDummyFile().isDir()){
				if (fo.getOperation() == ProtocolConstants.OP_ADD ||
						fo.getOperation() == ProtocolConstants.OP_MOD){

					if (!file.exists())		
						file.createNewFile();
					byte bytes[] = fo.getBytes();
					if (bytes != null){

						FileOutputStream fs = new FileOutputStream(file);
						fs.write(bytes);
						fs.close();
					}
				}else if (fo.getOperation() == ProtocolConstants.OP_DEL){
					if (file.exists())
						file.delete();
				}
			}
		}
		
		// We need to rebuild the map and clone the current one into prev map
		File root = new File(_home);
		if (!root.isDirectory() || !root.exists())
			_elog("Your home dir is invalid");
		else{
			_fileMap = buildFileMapRecursive(root);
			_prevFileMap = (HashMap<String, DummyFile>)_fileMap.clone();
		}
	}
	
	/**
	 * deleteSubDirs: recursively delete sub-directories
	 * @param file: the current directory
	 */
	public void deleteSubDirs(File file){
		if (file.isDirectory()){
			File[] list = file.listFiles();
			for(File f:list){
				if (f.isDirectory())
					deleteSubDirs(f);
				f.delete();
			}
			file.delete();
		}
	}
	
	/**
	 * getRelativeRootPath: get the relative path from file object
	 * @param file: the file
	 * @return: the path
	 */
	public String getRelativeRootPath(File file){
		return file.getAbsolutePath().substring(
			   file.getAbsolutePath().indexOf(_home)+_home.length()
			   );
	}
	
	/**
	 * Constructor
	 * @param home: The home directory
	 * @param debug: debug mode?
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
