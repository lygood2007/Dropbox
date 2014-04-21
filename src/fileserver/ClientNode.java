package fileserver;

import java.io.File;
import java.util.*;

/**
 * class ClientNode
 * Description: stores terminal threads and client info
 */
final class ClientNode {
	// No upper limit for syncer
	private LinkedList<DropboxFileServerSyncer> _syncers;
	private String _clientName;
	private String _dir;
	private String _password; // Not being used now
	
	public ClientNode(String clientName, String dir, String password){
		_clientName = clientName;
		_dir = dir;
		_password = password;
		_syncers = new LinkedList<DropboxFileServerSyncer>();
	}
	
	public synchronized void addSyncer(DropboxFileServerSyncer syncer){
		_syncers.add(syncer);
	}
	
	public int getNumSyncer(){
		return _syncers.size();
	}
	
	public synchronized void removeSyncer(DropboxFileServerSyncer syncer){
		_syncers.remove(syncer);
	}
	
	public synchronized void removeDeadSyncer(){
		if(_syncers == null)
			return;
		Iterator it = _syncers.iterator();
		while(it.hasNext()){
			DropboxFileServerSyncer fss = (DropboxFileServerSyncer)it.next();
			if(!fss.isAlive()){
				it.remove();
			}
		}
	}
	
	public synchronized void cancelSyncer(){
		Iterator it = _syncers.iterator();
		while(it.hasNext()){
			DropboxFileServerSyncer fss = (DropboxFileServerSyncer)it.next();
			fss.stop();
			fss.join();
		}
		
		_syncers.clear();
	}
	public String getClientName(){
		return _clientName;
	}
	
	public String getDir(){
		return _dir;
	}
	
	public String getPassword(){
		return _password;
	}
	
	public boolean match(String clientName, String pwd){
		if(clientName.equals(_clientName) && pwd.equals(_password))
			return true;
		else
			return false;
	}
	
	public synchronized void setPassword(String password){
		_password = password;
	}
	
	public synchronized boolean clear(){
		File tmp = new File(_dir);
		if(!tmp.delete()){
			return false;
		}
		
		for(DropboxFileServerSyncer fss: _syncers){
			fss.stop();
			fss.join();
		}
		_syncers = null;
		_clientName = null;
		_dir = null;
		_password = null;
		return true;
	}
}
