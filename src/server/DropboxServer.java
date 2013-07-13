package server;

import common.DropboxConstants;

/**
 * Class: DropboxServer
 * Description: Receives file stream, parses the stream and synchronizes it to the disk
 */

public class DropboxServer implements FileSynchronizationServer {

	public void listen() {

    }

    public static void main(String[] args) {
    	DropboxServer server = new DropboxServer();
    	System.out.println("Server started..");
    	server.listen();
    }
}
