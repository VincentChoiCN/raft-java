package person.alex.raft.node;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * 基于netty实现rpc服务，提供两个调用：
 * 1. AppendEntries
 * 2. RequestVote
 */
public class NodeRpcServer {

  public NodeRpcServer(InetSocketAddress address, Node node) throws InterruptedException, IOException {
    EventLoopGroup eventloopGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("alex.raft", true, Thread.MAX_PRIORITY));
    ServerBootstrap bootstrap = new ServerBootstrap().group(eventloopGroup).channel(NioServerSocketChannel.class).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("requestDecoder", new ServerRequestHandler(node));
        pipeline.addLast("responseEncoder", new ResponseEncoder());
      }
    });
    Channel serverChannel = bootstrap.bind(address).sync().channel();
  }
}
