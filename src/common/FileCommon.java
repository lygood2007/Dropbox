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
	
	public static String getParentDirStr(File dir){
		if(dir.isDirectory()){
			return new String(dir.getName().substring(0, dir.getName().lastIndexOf('/')-1));
		}else{
			return null;
		}
	}
	
	public static File getParentDir(File dir){
		String fileName = getParentDirStr(dir);
		if(fileName != null)
			return new File(fileName);
		else
			return null;
	}
	
	/**
	 * Find the first existed dir, only applied in Dummy file
	 * @param dfile: Dummy file
	 * @return the first directory
	 */
	public static File findFirstExistDir(DummyFile dfile){
		String absPath;
		absPath = getPath(dfile);
		
		if(absPath == null)
			return null;
		
		String[] split = absPath.split("/");
		if(split.length == 0)
			return new File("/");
		
		String lastPath = new String("");
		File nextPath = null;
		int i = 0;
		while(i < split.length){
			String nextPathStr = lastPath + "/" + split[i];
			nextPath = new File(nextPathStr);
			if(!nextPath.exists())
				break;
			else
				lastPath = nextPath.getAbsolutePath();
			i++;
		}
		if(nextPath != null)
		{
			if(nextPath.exists()){
				return nextPath;
			}
			else if(!nextPath.exists())
				return new File(lastPath);
			else
				return null;
		}else
			return null;
			
	}
	
	public static String getPath(File file){
		if(file.isDirectory())
			return file.getAbsolutePath();
		else{
			String path = file.getAbsolutePath();
			return new String(path.substring(0, path.lastIndexOf("/")));
		}
	}
	
	public static String getPath(DummyFile dfile){
		if(dfile.isDir())
			return dfile.getFile().getAbsolutePath();
		else{
			String path = dfile.getFile().getAbsolutePath();
			return new String(path.substring(0, path.lastIndexOf("/")));
		}
	}
}
