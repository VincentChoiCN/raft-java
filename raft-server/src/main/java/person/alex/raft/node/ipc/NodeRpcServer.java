package person.alex.raft.node.ipc;


import com.google.protobuf.BlockingService;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import person.alex.raft.node.ResponseEncoder;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.node.ipc.utils.IPCUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;


/**
 * 基于netty实现rpc服务，提供两个调用：
 * 1. AppendEntries
 * 2. RequestVote
 */
public class NodeRpcServer {

  BlockingService service;

  BlockingQueue<ServerCall> requestQueue;

  public NodeRpcServer(InetSocketAddress address, BlockingQueue<ServerCall> requestQueue, BlockingService service) throws InterruptedException {
    this.service = service;
    this.requestQueue = requestQueue;
    EventLoopGroup eventloopGroup = new NioEventLoopGroup(10, new DefaultThreadFactory("alex.raft", true, Thread.MAX_PRIORITY));
    ServerBootstrap bootstrap = new ServerBootstrap().group(eventloopGroup).channel(NioServerSocketChannel.class).childOption(ChannelOption.MAX_MESSAGES_PER_READ, 1024*1024*1024).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childHandler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("frameProcessor", new FrameProcessor());
        pipeline.addLast("requestDecoder", new ServerRequestHandler());
        pipeline.addLast("responseEncoder", new ResponseEncoder());
      }
    });
    bootstrap.bind(address).sync().channel();
  }

  class FrameProcessor extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      ByteBuf buf = (ByteBuf) in;
      if (buf.readableBytes() < 4) {
        return;
      }
      int rpclength = buf.getInt(buf.readerIndex());
      if (buf.readableBytes() < rpclength + 4) {
        return;
      }

      buf.skipBytes(4);
      byte[] dst = new byte[rpclength];
      buf.readBytes(dst);
      out.add(dst);
    }
  }

  class ServerRequestHandler extends ChannelInboundHandlerAdapter {

    Connection connection;

    public ServerRequestHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if(connection == null) {
        connection = new Connection(ctx.channel().remoteAddress(), NodeRpcServer.this);
      }
      connection.processOneRpc((byte[]) msg);
      if (connection.request != null)
        requestQueue.add(new ServerCall(connection.header, connection.request, ctx.channel(), connection));
      else throw new IOException("can not parse error");
    }

    /**
     * duty for process call
     */
    class Connection {
      ClientProtos.RequestHeader header;
      Message request;
      NodeRpcServer rpcServer;
      SocketAddress remoteAddress;

      public Connection(SocketAddress remoteAddress, NodeRpcServer rpcServer) {
        this.remoteAddress = remoteAddress;
        this.rpcServer = rpcServer;
      }

      public void processOneRpc(byte[] data) throws IOException {
        CodedInputStream cis = CodedInputStream.newInstance(data);
        processHeader(cis);
        processRequest(cis);
      }

      void processHeader(CodedInputStream cis) throws IOException {
        int headerSize = cis.readRawVarint32();
        ClientProtos.RequestHeader.Builder builder = ClientProtos.RequestHeader.newBuilder();
        header = (ClientProtos.RequestHeader) IPCUtils.buildFromCIS(cis, builder, headerSize);
      }

      void processRequest(CodedInputStream cis) throws IOException {
        int requestSize = cis.readRawVarint32();
        Descriptors.MethodDescriptor md = service.getDescriptorForType().findMethodByName(header.getMethod());
        Message.Builder builder = service.getRequestPrototype(md).newBuilderForType();
        request = IPCUtils.buildFromCIS(cis, builder, requestSize);
      }
    }
  }
}
