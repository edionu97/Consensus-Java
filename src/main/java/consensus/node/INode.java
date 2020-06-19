package consensus.node;

public interface INode {

    /**
     * Starts a server socket connection that listens for incoming messages
     */
    void start();

    /**
     * Register the node, so that the hub to be aware of the node
     */
    void register();
}
