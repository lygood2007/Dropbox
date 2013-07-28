package common;

import java.io.*;

/**
 * Package: common
 * Class: DummyFile
 * Description: We encapsulate the File class here because we may
 * 				use a file that does not exist. We need to store manually
 * 			    whether the file is directory here 
 */
public class DummyFile {
	
	private boolean _isDir;
	private File _file;
	private long _lastModifiedTime;
	
	public DummyFile(){
		_isDir = false;
		_file = null;
		_lastModifiedTime = 0;
	}
	
	public DummyFile(boolean isDir, File file){
		_isDir = isDir;
		_file = file;
		_lastModifiedTime = 0;
	}
	
	public void setLastModifiedTime(long lastModifiedTime){
		_lastModifiedTime = lastModifiedTime;
	}
	
	public long getLastModifiedTime(){
		return _lastModifiedTime;
	}
	
	public File getFile(){
		return _file;
	}
	
	public boolean isDir(){
		return _isDir;
	}
}
