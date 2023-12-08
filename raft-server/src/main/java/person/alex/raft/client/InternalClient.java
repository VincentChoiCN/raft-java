package person.alex.raft.client;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.protobuf.ClientProtos.AppendRequest;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

public class InternalClient implements ClientProtos.ClientService.BlockingInterface {

  Channel channel;

  ClientProtos.ClientService.BlockingInterface stub = ClientProtos.ClientService.newBlockingStub(new MyBlockingRpcChannel());

  class MyBlockingRpcChannel implements BlockingRpcChannel {

    AtomicLong curCallId = new AtomicLong(0);

    @Override
    public Message callBlockingMethod(Descriptors.MethodDescriptor methodDescriptor, RpcController rpcController, Message message, Message returnType) throws ServiceException {
      try {
        ClientProtos.RequestHeader.Builder builder = ClientProtos.RequestHeader.newBuilder();
        builder.setService(ClientProtos.ClientService.getDescriptor().getName());
        builder.setMethod(methodDescriptor.getName());
        builder.setCallId(curCallId.incrementAndGet());
        ClientProtos.RequestHeader header = builder.build();
        ClientCall clientCall = new ClientCall(channel, header, message, returnType);
        return clientCall.rpcCallAndWait();
      } catch (InterruptedException e) {
        throw new ServiceException(e);
      } catch (ExecutionException e) {
        throw new ServiceException(e);
      }
    }
  }

  public InternalClient(InetSocketAddress address, NioEventLoopGroup loopGroup) throws InterruptedException {
    channel = new Bootstrap().group(loopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1).handler(new ChannelInitializer<Channel>() {

      @Override
      protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("requestAndResponse", new ClientRequestEncoder());
      }
    }).localAddress(null).remoteAddress(address).connect().sync().channel();
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(RpcController controller, AppendRequest request) throws ServiceException {
    return stub.appendEntries(controller, request);
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return null;
  }

  class ClientCall {
    Channel channel;
    Message request;
    Message header;
    Message returnType;
    CompletableFuture<Message> done = new CompletableFuture<>();

    public ClientCall(Channel channel, Message header, Message request, Message returnType) {
      this.channel = channel;
      this.request = request;
      this.header = header;
      this.returnType = returnType;
    }

    public Message rpcCallAndWait() throws InterruptedException, ExecutionException {
      channel.writeAndFlush(this);
      return done.get();
    }

    public void complete(Message response) {
      done.complete(response);
    }
  }
}
