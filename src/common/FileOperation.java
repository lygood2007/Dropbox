/**
 * File: FileOperation.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

import common.ProtocolConstants;

/**
 * 
 * Class: FileOperation
 * Description: Basic functions for conversion
 *              between byte operation and string
 */
public class FileOperation {
	
	private byte _operation;
	private DummyFile _file;
	private byte _fileBytes[];
	
	/**
	 * Default constructor
	 */
	public FileOperation(){
		_operation = ProtocolConstants.OP_NULL;
		_file = null;
		_fileBytes = null;
	}
	
	/**
	 * Constructor
	 * @param operation: the operation in byte format
	 * @param file: the file object
	 */
	public FileOperation(byte operation, DummyFile file){
		_operation = operation;
		_file = file;
		_fileBytes = null;
	}
	
	/**
	 * getOperationString: convert the byte to corresponding string
	 * @param b: the byte
	 * @return: return the result string
	 */
	static public String getOperationString(byte b){
		String str = new String();
		if(b == ProtocolConstants.OP_ADD)
			str = "OP_ADD";
		else if(b == ProtocolConstants.OP_MOD)
			str = "OP_MOD";
		else if(b == ProtocolConstants.OP_NULL)
			str = "OP_NULL";
		else if(b == ProtocolConstants.OP_DEL)
			str = "OP_DEL";
		return str;
	}
	
	/**
	 * Setters
	 */
	public void setBytes(byte fileBytes[]){
		_fileBytes = fileBytes;
	}
	
	/**
	 * Getters
	 */
	public byte[] getBytes(){
		return _fileBytes;
	}
	
	public DummyFile getDummyFile(){
		return _file;
	}
	
	public byte getOperation(){
		return _operation;
	}
}
