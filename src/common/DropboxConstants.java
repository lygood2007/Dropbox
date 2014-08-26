/**
 * File: DropboxConstants.java
 * Author: Yan Li (yan_li@brown.edu)
 * Date: Apr 21 2014
 */

package common;

/**
 * Package: common
 * Interface: DropboxConstants
 * Description: Contains constants for the whole Dropbox project
 */
public interface DropboxConstants {
    final static int FILE_SERVER_PORT = 8664; // Default port for file server
    final static int CLIENT_PORT = 8088; // Default port for client port
    final static int SYNC_SLEEP_MILLIS = 2*1000; // Sleep interval
    final static int TRY_CONNECT_MILLIS = 5*1000; // Try connection interval
    final static int ECHO_FILE_SERVER = 2*1000; // Echo file server interval
    final static int ECHO_MASTER = 2*1000;  // Echo master interval
    final static int IDENTIFY_DELAY = 2*1000; // Identifying interval
    
    final static int MAX_TRY = 3; // Max number of tries for connection
    final static String MASTER_IP = "127.0.0.1";// Hard coded, cannot be changed
    
    final static int MASTER_CLUSTER_PORT = 8080; // Default master port for receiving cluster connection
    final static int MASTER_CLIENT_PORT = 8086; // Default master port for receiving client connection
    
    final static String FILE_SERVER_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "server_root"; // Default server_root directory
    final static String CLIENT_ROOT = System.getProperty("user.dir") + System.getProperty("file.separator") + "client_root"; // Default client_root directory
    
    final static int MAX_PRIO = 5; // Max priority
    final static int MIN_PRIO = 1; // Min priority
    
    final static int MAX_CLIENTS_IN_FS = 3; //Max client in each file server
    
    final static int HEART_BEAT_HZ = 5*1000; // Heart Beat message frequency
    
    final static String DEFAULT_PWD = "123456"; // Default password
    
    final static int MAX_CLIENT_NAME_LEN = 16; // Max client name length
    final static int MAX_PASSWORD_NAME_LEN = 16; // Max pass word name length
}
