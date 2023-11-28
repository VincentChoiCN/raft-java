package person.alex.raft.client;

import com.google.protobuf.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import person.alex.raft.node.NodeService;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.protobuf.ClientProtos.AppendRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class InternalClient implements NodeService {

  Channel channel;

  AtomicLong callId = new AtomicLong(0);

  Queue<ClientCall> callQ = new LinkedBlockingQueue<>();

  public InternalClient(InetSocketAddress address, NioEventLoopGroup loopGroup) throws InterruptedException {
    channel = new Bootstrap().group(loopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1).handler(new ChannelInitializer<Channel>() {

      @Override
      protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        // Construct the inbound handler for processing response.
        p.addLast("ClientResponse", new ClientResponseHandler(callQ));
        p.addLast("requestEncoder", new ClientRequestEncoder());
      }
    }).localAddress(null).remoteAddress(address).connect().sync().channel();
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(AppendRequest appendRequest) throws IOException, InterruptedException {
    ClientCall clientCall = new ClientCall(channel, callId.incrementAndGet(), appendRequest, 0);
    callQ.add(clientCall);
    return (ClientProtos.AppendResponse) clientCall.rpcCallAndWait();
  }

  @Override
  public ClientProtos.VoteResponse requestVote(ClientProtos.VoteRequest voteRequest) {
    return null;
  }

  class ClientCall {
    long callId;
    Message response;
    Channel channel;
    int code;
    Message request;

    public ClientCall(Channel channel, long id, Message request, int code) {
      this.callId = id;
      this.channel = channel;
      this.code = code;
      this.request = request;
    }

    public Message rpcCallAndWait() throws InterruptedException {
      channel.writeAndFlush(this);
      synchronized (this) {
        try {
          wait();
        } catch (InterruptedException e) {
          throw e;
        }
      }
      return this.response;
    }

    public void setResponse(Message response) {
      this.response = response;
    }

    public ByteBuf serializeRequest() {
      if (code == 0) {
        byte[] bytes = ((AppendRequest) request).toByteArray();
        ByteBuf buff = Unpooled.buffer(1 + bytes.length + 4);
        buff.writeInt(1 + bytes.length);
        buff.writeByte(0x00);
        buff.writeBytes(bytes);
        return buff;
      } else {
        byte[] bytes = ((ClientProtos.VoteRequest) request).toByteArray();
        ByteBuf buff = Unpooled.buffer(1 + bytes.length + 4);
        buff.writeInt(1 + bytes.length);
        buff.writeByte(0x01);
        buff.writeBytes(bytes);
        return buff;
      }
    }
  }
}
