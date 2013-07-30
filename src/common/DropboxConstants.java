package common;

/**
 * Package: common
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    final static int SERVER_PORT = 5000;

    final static int SYNC_SLEEP_MILLIS = 3 * 1000;

    // Set the dropbox directory, also the server directory
    final static String DROPBOX_DIRECTORY = "/tmp" + System.getProperty("file.separator")+ "Dropbox";
    final static String DROPBOX_TEST_DIRECTORY = "/tmp" + System.getProperty("file.separator") + "Droptest";
}
