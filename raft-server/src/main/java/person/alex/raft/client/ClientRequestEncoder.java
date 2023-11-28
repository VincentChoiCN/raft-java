package person.alex.raft.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ClientRequestEncoder extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    InternalClient.ClientCall call = (InternalClient.ClientCall) msg;
    ctx.write(Unpooled.wrappedBuffer(call.serializeRequest()));
  }
}
