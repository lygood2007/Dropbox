package common;

import common.ProtocolConstants;

public class FileOperation {
	private byte _operation;
	private DummyFile _file;
	private byte _fileBytes[];
	
	public FileOperation(){
		_operation = ProtocolConstants.OP_NULL;
		_file = null;
		_fileBytes = null;
	}
	
	public FileOperation(byte operation, DummyFile file){
		_operation = operation;
		_file = file;
		_fileBytes = null;
	}
	
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
	public void setBytes(byte fileBytes[]){
		_fileBytes = fileBytes;
	}
	
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
