package master;

/**
 * 
 * Class ClientRecord
 * Description: The container for each client in master side 
 */
final class ClientRecord {

	private String _name;
	private String _password;
	private volatile FileServerNode _serverNode;
	
	public ClientRecord(String name, String password, FileServerNode serverNode){
		_name = name;
		_password = password;
		_serverNode = serverNode;
	}
	
	/**
	 * Getters
	 */
	public String getName(){
		return _name;
	}
	
	public String getPassword(){
		return _password;
	}
	
	public synchronized FileServerNode getOwner(){
		return _serverNode;
	}
	
	
	public boolean isMatch(String name, String password){
		return _name.equals(name) && _password.equals(password);
	}
	
	public synchronized void changePassword(String password){
		_password = password;
	}
	
	public synchronized void clear(){
		_name = null;
		_password = null;
		_serverNode = null;
	}
}
