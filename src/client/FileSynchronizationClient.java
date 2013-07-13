package client;

/**
 * Interface: FileSynchronizationClient
 * Description: General interface for a file synchronization client.
 */
public interface FileSynchronizationClient {
    void run() throws Exception;
    boolean sync() throws Exception;
}
