/**
 * File: FileCommon.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

import java.io.*;

/**
 * 
 * Class: FileCommon
 * Description: This class contains the supplementary operations of File
 */

/**
 * 
 * CAUTION:
 * SOME OF THE FUNCTIONS MAY BE DREPCATED IN THE FUTURE
 */
public class FileCommon {
	
	/**
	 * getParentDirStr: get parent directory from current file
	 * @param dir: the file
	 * @return: the parent directory in string
	 */
	public static String getParentDirStr(File dir){
		if (dir.isDirectory()){
			return new String(dir.getName().substring(0,
							  dir.getName().lastIndexOf('/')-1));
		}else{
			return null;
		}
	}
	
	/**
	 * getParentDir: get parent directory file from file
	 * @param file: the file
	 * @return: the parent directory file object
	 */
	public static File getParentDir(File dir){
		String fileName = getParentDirStr(dir);
		if (fileName != null)
			return new File(fileName);
		else
			return null;
	}
	
	/**
	 * fineFirstExistDir: find the first existed dir, only applied in Dummy file
	 * @param dfile: Dummy file
	 * @return the first directory
	 */
	public static File findFirstExistDir(DummyFile dfile){
		String absPath;
		absPath = getPath(dfile);
		
		if (absPath == null)
			return null;
		
		String[] split = absPath.split("/");
		if (split.length == 0)
			return new File("/");
		
		String lastPath = new String("");
		File nextPath = null;
		int i = 0;
		while (i < split.length){
			String nextPathStr = lastPath + "/" + split[i];
			nextPath = new File(nextPathStr);
			if (!nextPath.exists())
				break;
			else
				lastPath = nextPath.getAbsolutePath();
			i++;
		}
		if (nextPath != null)
		{
			if (nextPath.exists()){
				return nextPath;
			}
			else if (!nextPath.exists())
				return new File(lastPath);
			else
				return null;
		}else
			return null;
			
	}
	
	/**
	 * getPath: get path from file
	 * @param file: the file
	 * @return: the path in string
	 */
	public static String getPath(File file){
		if (file.isDirectory())
			return file.getAbsolutePath();
		else{
			String path = file.getAbsolutePath();
			return new String(path.substring(0, path.lastIndexOf("/")));
		}
	}
	
	/**
	 * getPath: get path from dummy file
	 * @param dfile: the dummy file
	 * @return: the path in string
	 */
	public static String getPath(DummyFile dfile){
		if (dfile.isDir())
			return dfile.getFile().getAbsolutePath();
		else{
			String path = dfile.getFile().getAbsolutePath();
			return new String(path.substring(0, path.lastIndexOf("/")));
		}
	}
}
