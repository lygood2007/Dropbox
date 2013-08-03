package utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import common.DropboxConstants;
import common.DummyFile;
import common.FileOperation;
import common.ProtocolConstants;
import java.io.*;


/**
 * Package: utils
 * Class: DropboxStreamParser
 * Description: Parse the stream;
 */
public class DropboxStreamParser {
	
	private DataInputStream _is;
	private String _home;
	boolean _debug;
	
	public int parse(){
		int packHead = 0;
		try{
			packHead = parseHead();
		}catch(IOException e){
			System.err.println("Error occurs when parse stream header");
			if(_debug)
				e.printStackTrace();
		}
		return packHead;
	}
	
	public HashMap<String, FileOperation> parseFileMap() {
		if(_is != null){
			String targetHome = null;
			try{
				int tarLength = _is.readInt();
				if(_debug){
					System.out.println("Target home length: " + tarLength);
				}
				byte []nameBytes = new byte[tarLength];
				_is.read(nameBytes);
				targetHome = new String(nameBytes);
				if(_debug){
					System.out.println("Target home dir: " + targetHome);
				}
			}catch(IOException e){
				System.err.println("Error occurs when parsing the home directory");
				if(_debug)
					e.printStackTrace();
			}
			int fileNum = 0;
			try{
				// Valid stream			
				fileNum = _is.readInt();
				if(_debug)
					System.out.println("Filenum: " + fileNum);
			}catch(IOException e){
				System.err.println("Error occurs when parsing the file number");
				if(_debug)
					e.printStackTrace();
			}
			
			try
			{
				// The hashmap is used for storing the structure
				HashMap<String, FileOperation> fileMap = new HashMap<String, FileOperation>();

				for( int i = 0; i < fileNum; i++ ){
					// Read each file
					readEach(_is, fileMap);
				}
				return fileMap;
			}	
			catch(IOException e){
				System.err.println("Error occurs when process received file map");
				if(_debug)
					e.printStackTrace();
			}
		}
		return null;
	}
	
	public void readEach(DataInputStream is, HashMap<String, FileOperation> fileMap) throws IOException {
		if(is != null){
			int nameLength = is.readInt();
			if(_debug){
				System.out.println("Name length: " + nameLength);
			}
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
			
			if(flag == false &&
					(operation == ProtocolConstants.OP_ADD ||
					operation == ProtocolConstants.OP_MOD)){
				fileLength = is.readLong();
				
				/**
				 * CAUTION:
				 * HERE IT MEANS IT DOES NOT SUPPORT LARGE FILE
				 * REVISE THIS LATER
				 */
				fileBytes = new byte[(int)fileLength];
				//is.read(fileBytes);
				int i = 0;
				while(i < fileLength){
					fileBytes[i] = is.readByte();
					i++;
				}
				fo.setBytes(fileBytes);
			}
				// Print the data we get
			if(_debug)
				System.out.println("Filename: "+fileName + "(" + flag +")"
						+ " Operation: " + operation + " LastTime: " + lastModifiedTime);
			if(fileBytes != null && _debug){
				String fileContent = new String(fileBytes);
				System.out.println(fileContent);
			}
		}
	}
	
	public int parseHead() throws IOException {
		if(_is != null)
			return _is.readInt();
		else
			return ProtocolConstants.PACK_INVALID_HEAD;
	}
	
	public DropboxStreamParser(){
		_is = null;
		_debug = false;
		_home = DropboxConstants.DROPBOX_TEST_DIRECTORY;
	}
	
	public DropboxStreamParser(String home, DataInputStream is, boolean debug){
		_is = is;
		_debug = debug;
		_home = home;
	}
	
	public DataInputStream getInputStream(){
		return _is;
	}
}
