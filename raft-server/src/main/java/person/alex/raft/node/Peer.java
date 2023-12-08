package person.alex.raft.node;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import io.netty.channel.nio.NioEventLoopGroup;
import person.alex.raft.client.InternalClient;
import person.alex.raft.protobuf.ClientProtos;

import java.net.InetSocketAddress;

public class Peer implements ClientProtos.ClientService.BlockingInterface{

  private int id;
  private int port;
  private String ip;

  InternalClient client;

  boolean connected = false;

  public Peer(int id, int port, String ip) {
    this.id = 0;
    this.port = port;
    this.ip = ip;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(RpcController controller, ClientProtos.AppendRequest request) throws ServiceException {
    return client.appendEntries(controller, request);
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return client.requestVote(controller, request);
  }

  private void connect(NioEventLoopGroup nioEventLoopGroup) throws InterruptedException {
    client = new InternalClient(new InetSocketAddress(this.ip, this.port), nioEventLoopGroup);
    connected = true;
  }
}
