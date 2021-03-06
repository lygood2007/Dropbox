/**
 * File: SyncStreamParser.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import common.DummyFile;
import common.FileOperation;
import common.ProtocolConstants;


/**
 * 
 * Class:SyncStreamParser
 * Description: Parse the stream;
 */
public class SyncStreamParser {
	
	private DataInputStream _is;
	private String _home;
	boolean _debug;
	
	/**
	 * _dlog: debug log
	 * @param str: the log string
	 */
	private void _dlog(String str){
		if (_debug)
			System.out.println("[SyncStreamParser (DEBUG)]:" + str);
	}
	
	/**
	 * _elog: error log
	 * @param str: the log string
	 */
	@SuppressWarnings("unused")
	private static void _elog(String str){
		System.err.println("[SyncStreamParser (ERROR)]:" + str);
	}
	
	/**
	 * _log: general log
	 * @param str: the log string
	 */
	@SuppressWarnings("unused")
	private static void _log(String str){
		System.out.println("[SyncStreamParser]:" + str);
	}
	
	/**
	 * parse: parse the head
	 * @return: the head integer
	 */
	public int parse() throws IOException {
		int packHead = 0;
		try{
			packHead = parseHead();
		}catch(IOException e){
			throw e;
		}
		return packHead;
	}
	
	/**
	 * parseFileMap: parse the file map from incoming stream
	 * @return: the parsed file map
	 */
	public HashMap<String, FileOperation> parseFileMap() throws IOException{
		if (_is != null){
			String targetHome = null;
			try{
				int tarLength = _is.readInt();
				
				_dlog("Target home length: " + tarLength);
				
				byte []nameBytes = new byte[tarLength];
				_is.read(nameBytes);
				targetHome = new String(nameBytes);
				
				_dlog("Target home dir: " + targetHome);
				
			}catch(IOException e){
				throw e;
			}
			
			int fileNum = 0;
			try{
				// Valid stream			
				fileNum = _is.readInt();		
				_dlog("Filenum: " + fileNum);
			}catch(IOException e){
				throw e;
			}
			
			try
			{
				// The hashmap is used for storing the structure
				HashMap<String, FileOperation> fileMap =
						new HashMap<String, FileOperation>();

				for( int i = 0; i < fileNum; i++ ){
					// Read each file
					readEach(_is, fileMap);
				}
				return fileMap;
			}	
			catch(IOException e){
				throw e;
			}
		}
		return null;
	}
	
	/**
	 * readEach: read each item
	 * @param is: the input stream
	 * @param fileMap: the file map
	 */
	public void readEach(DataInputStream is,
					HashMap<String, FileOperation> fileMap) throws IOException {
		if (is != null){
			int nameLength = is.readInt();
			_dlog("Name length: " + nameLength);
			
			byte []nameBytes = new byte[nameLength];
			is.read(nameBytes);
			String fileName = new String(nameBytes);
		
			long lastModifiedTime = is.readLong();
			byte operation = is.readByte();
			
			// Push the file operation into hashmap
			
			boolean flag = is.readBoolean();
			DummyFile f = new DummyFile(flag,
					new File(_home+fileName));
			
			FileOperation fo = new FileOperation(operation, f);
			fileMap.put(fileName, fo);
			long fileLength = 0;
			byte []fileBytes = null;
			
			if (flag == false &&
					(operation == ProtocolConstants.OP_ADD ||
					operation == ProtocolConstants.OP_MOD)){
				fileLength = is.readLong();
				
				/**
				 * CAUTION:
				 * HERE IT MEANS IT DOES NOT SUPPORT LARGE FILE
				 * REVISE THIS LATER
				 */
				fileBytes = new byte[(int)fileLength];

				int i = 0;
				while (i < fileLength){
					fileBytes[i] = is.readByte();
					i++;
				}
				fo.setBytes(fileBytes);
			}
				// Print the data we get
			_dlog("Filename: "+fileName + "(" + flag +")"
						+ " Operation: " + operation +
						" LastTime: " + lastModifiedTime
						);
			if (fileBytes != null && _debug){
				String fileContent = new String(fileBytes);
				_dlog(fileContent);
			}
		}
	}
	
	/**
	 * parseHead: parse the head
	 * @return: the head integer
	 */
	public int parseHead() throws IOException {
		if (_is != null)
			return _is.readInt();
		else
			return ProtocolConstants.PACK_INVALID_HEAD;
	}
	
	/**
	 * Constructor
	 * @param home: the home directory
	 * @param is: the input stream
	 * @param debug: debug mode?
	 */
	public SyncStreamParser(String home, DataInputStream is, boolean debug){
		_is = is;
		_debug = debug;
		_home = home;
	}
	
	/**
	 * Getters
	 */
	public DataInputStream getInputStream(){
		return _is;
	}
	
	/**
	 * closeStream: close the input stream
	 */
	public void closeStream() throws IOException{
		try{
			_is.close();
		}catch(IOException e){
			throw e;
		}
	}
}
