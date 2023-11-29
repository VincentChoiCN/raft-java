package person.alex.raft.node;

import com.google.protobuf.Message;
import io.netty.channel.Channel;
import person.alex.raft.protobuf.ClientProtos;

public class ServerCall {

  Channel channel;
  ClientProtos.RequestHeader requestHeader;
  Message request;
  ClientProtos.ResponseHeader responseHeader;
  Message response;
  ServerRequestHandler.Connection connection;

  public ServerCall(ClientProtos.RequestHeader requestHeader, Message request, Channel channel, ServerRequestHandler.Connection connection) {
    this.requestHeader = requestHeader;
    this.request = request;
    this.channel = channel;
    this.connection = connection;
  }

  public void setResponse(Message response) {
    this.response = response;
  }

  public void setResponseHeader(ClientProtos.ResponseHeader responseHeader) {
    this.responseHeader = responseHeader;
  }

  public void flush() {
    channel.writeAndFlush(this);
  }

  /**
   * release the resource include connection;
   */
  public void clear() {

  }
}
