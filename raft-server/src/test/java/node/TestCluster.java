package node;

import person.alex.raft.node.Node;
import person.alex.raft.node.Peer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestCluster {

  public void createCluster(int count) throws IOException, InterruptedException {
    Map<Integer, Peer> peers = new HashMap<>();
    for (int i = 0; i < count; i++) {
      peers.put(i, new Peer(i, 9000 + i, "127.0.0.1"));
    }

    Node[] nodes = new Node[5];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new Node(i, peers);
      nodes[i].initialize();
    }
  }


}
