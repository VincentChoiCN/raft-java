package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    RpcResponse response = (RpcResponse) msg;
    ByteBuf length = Unpooled.buffer(4);
    length.writeInt(response.getResponse().readableBytes());
    ctx.writeAndFlush(Unpooled.wrappedBuffer(length, response.getResponse()), promise);
  }
}
