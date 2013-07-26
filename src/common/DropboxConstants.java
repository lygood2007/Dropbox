package common;

/**
 * Package: csci1310.common
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    int SERVER_PORT = 8945;

    int SYNC_SLEEP_MILLIS = 10 * 1000;

    String LOGIN = "cs131000";
    String TMP_DIRECTORY = "/tmp";
    String DROPBOX_DIRECTORY = TMP_DIRECTORY + System.getProperty("file.separator") + LOGIN;
}
