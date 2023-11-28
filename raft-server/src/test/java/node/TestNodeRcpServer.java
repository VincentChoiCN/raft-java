package node;

import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;
import person.alex.raft.client.InternalClient;
import person.alex.raft.node.Node;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TestNodeRcpServer {

  @Test
  public void TestRpcProcess() throws InterruptedException, IOException {
    System.out.println("start");
    // start one node.
    Node node = new Node(new InetSocketAddress("0.0.0.0", 7777));
    node.initialize();

    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(1);

    InternalClient client = new InternalClient(new InetSocketAddress("127.0.0.1", 7777), nioEventLoopGroup);

    ClientProtos.AppendResponse response = client.appendEntries(TestAppendRequest.getAnRandomAppendRequest(4));

    System.out.println(response);

  }
}
