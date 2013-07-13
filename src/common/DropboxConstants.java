package common;

/**
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    int SERVER_PORT = 8987;
    int MAX_FILE_NAME_LENGTH = 4;
    int SYNC_SLEEP_MILLIS = 10 * 1000;
    final static int MAX_CLIENT_NUM = 3;
	final static byte DELETE = 0;
	final static byte MODIFY = 1;
	final static byte ADD = 2;
    
    String LOGIN = "chengren";
    String TMP_DIRECTORY = "/tmp";
    String DROPBOX_DIRECTORY = TMP_DIRECTORY + System.getProperty("file.separator") + LOGIN;
}
