package node;

import com.google.protobuf.ServiceException;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.Test;
import person.alex.raft.client.InternalClient;
import person.alex.raft.client.RaftController;
import person.alex.raft.node.Node;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;

public class TestNodeRcpServer {

  @Test
  public void TestRpcProcess() throws InterruptedException, IOException, ServiceException {
    // start one node.
    Node node = new Node(new InetSocketAddress("0.0.0.0", 7777));
    node.initialize();

    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(1);

    InternalClient client = new InternalClient(new InetSocketAddress("127.0.0.1", 7777), nioEventLoopGroup);

    for (int i = 0; i < 450000; i++) {
      ClientProtos.AppendResponse response = client.appendEntries(new RaftController(), TestAppendRequest.getLargeRequest());
      System.out.println(response);
      System.out.println(i);
      assertEquals(response.getTerm(), -1);
      if(i % 3000 == 0)
        System.gc();
    }
    System.gc();


    Thread.sleep(10000000);
  }
}
