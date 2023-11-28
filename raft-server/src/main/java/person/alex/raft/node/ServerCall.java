package person.alex.raft.node;

import com.google.protobuf.Message;
import io.netty.channel.Channel;

public class ServerCall {

  Channel channel;
  Message request;
  Message response;

  public ServerCall(Message request, Channel channel) {
    this.request = request;
    this.channel = channel;
  }

  public void setResponse(Message response) {
    this.response = response;
  }

  public void flush() {
    channel.writeAndFlush(response);
  }
}
