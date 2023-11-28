package person.alex.raft.client;

import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.protobuf.ClientProtos.AppendRequest;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class InternalClient implements ClientProtos.ClientService.BlockingInterface {

  Channel channel;

  AtomicLong curCallId = new AtomicLong(0);

  Map<Long, ClientCall> callQ = new ConcurrentHashMap<>();

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
  public ClientProtos.AppendResponse appendEntries(RpcController controller, AppendRequest request) throws ServiceException {
    try {
      ClientProtos.AppendRequest.Builder builder =  request.toBuilder();
      builder.setCallId(curCallId.incrementAndGet());
      request = builder.build();

      ClientCall clientCall = new ClientCall(channel, request, 0);
      callQ.put(request.getCallId(), clientCall);
      return (ClientProtos.AppendResponse) clientCall.rpcCallAndWait();
    } catch (InterruptedException ie) {
      throw new ServiceException(ie);
    } catch (ExecutionException e) {
      throw new ServiceException(e);
    }
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return null;
  }

  class ClientCall {
    Channel channel;
    int code;
    Message request;
    CompletableFuture<Message> done = new CompletableFuture<>();

    public ClientCall(Channel channel, Message request, int code) {
      this.channel = channel;
      this.code = code;
      this.request = request;
    }

    public Message rpcCallAndWait() throws InterruptedException, ExecutionException {
      channel.writeAndFlush(this);
      return done.get();
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
