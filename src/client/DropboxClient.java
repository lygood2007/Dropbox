package client;

import common.DropboxConstants;


/**
 * Class: DropboxClient
 * Description: synchronizes all of the files & dirs change in the syncDirectory to the remote server whose IP is specified by the argument
 * Example: Java client/DropboxClient 192.161.3.57
 */
public class DropboxClient implements FileSynchronizationClient, Runnable {
    private String syncDirectory; //DropboxConstants.DROPBOX_DIRECTORY;
    private String serverHost;
    private int serverPort;
    
    public DropboxClient(String host) {
        this.serverHost = host;
        
        // replace it with the directory you need to synchronize
        this.syncDirectory = DropboxConstants.DROPBOX_DIRECTORY; 
        this.serverPort = DropboxConstants.SERVER_PORT;
    }

    public void run() {
    }

    public void startConnection(String serverHost, int serverPort) {
    }

    public void endConnection() {
    }
    
    public boolean sync() {
        return true;
    }

    public static void main(String[] args) {
    }
}
