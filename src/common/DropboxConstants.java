package common;

/**
 * Package: common
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    final static int FILE_SERVER_PORT = 8664;
    final static int CLIENT_PORT = 8088;
    final static int SYNC_SLEEP_MILLIS = 2*1000;
    final static int TRY_CONNECT_MILLIS = 5*1000;
    final static int ECHO_FILE_SERVER = 2*1000;
    final static int ECHO_MASTER = 2*1000;
    final static int IDENTIFY_DELAY = 2*1000;
    
    final static int MAX_TRY = 3;
    final static String MASTER_IP = "127.0.0.1";// Hard coded, cannot be changed
    
    final static int MASTER_CLUSTER_PORT = 8080;
    final static int MASTER_CLIENT_PORT = 8086;
    
    final static String FILE_SERVER_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "server_root";
    final static String CLIENT_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "client_root";
    //final static String DROPBOX_CLIENT_DIR = System.getProperty("user.dir") + System.getProperty("file.separator");

    final static int FILE_SERVER_MAX_CLIENTS = 3;
    
    final static int MAX_PRIO = 5;
    final static int MIN_PRIO = 1;
    
    final static int MAX_CLIENTS_IN_FS = 3;
    
    final static int HEART_BEAT_HZ = 5*1000;
    
    final static String DEFAULT_PWD = "123456";
    
    final static int MAX_CLIENT_NAME_LEN = 16;
    final static int MAX_PASSWORD_NAME_LEN = 16;
}
