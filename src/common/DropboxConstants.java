package common;

/**
 * Package: common
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    final static int FILE_SERVER_PORT = 5000;
    final static int SYNC_SLEEP_MILLIS = 3 * 1000;
    final static String MASTER_IP = "127.0.0.1";// Hard coded, cannot be changed
    final static int MASTER_PORT = 8080;
    
    final static String DROPBOX_SERVER_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "server_root";
    final static String DROPBOX_CLIENT_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "client_root";
}
