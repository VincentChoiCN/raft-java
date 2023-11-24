package persion.alex.raft.node;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;


/**
 * 基于netty实现rpc服务，提供两个调用：
 * 1. AppendEntries
 * 2. RequestVote
 */
public class NodeRpcServer {


  public NodeRpcServer(int port) throws InterruptedException {

    NodeService nodeService = new NodeServiceImpl();

    EventLoopGroup eventloopGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("alex.raft", true, Thread.MAX_PRIORITY));
    ServerBootstrap bootstrap = new ServerBootstrap().group(eventloopGroup).channel(NioServerSocketChannel.class).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
//        FixedLengthFrameDecoder frameDecoder = new FixedLengthFrameDecoder(4);
//        pipeline.addLast("frameDecoder", frameDecoder);
        pipeline.addLast("requestDecoder", new ServerRequestHandler(nodeService));
        pipeline.addLast("responseEncoder", new ResponseEncoder());
      }
    });
    Channel serverChannel = bootstrap.bind(new InetSocketAddress("0.0.0.0", port)).sync().channel();
  }
}