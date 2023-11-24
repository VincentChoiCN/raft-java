package persion.alex.raft.node;

import io.netty.channel.Channel;

public class ServerCall {

  Channel channel;
  RpcRequest request;
  RpcResponse response;

  public ServerCall(RpcRequest request, Channel channel) {
    this.request = request;
    this.channel = channel;
  }

  public void setResponse(RpcResponse response) {
    this.response = response;
  }

  public void flush() {
    channel.writeAndFlush(response);
  }
}
