package person.alex.raft.node;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;

public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    Message response = (Message) msg;
    ByteBuf buf = Unpooled.buffer(4 + response.getSerializedSize() + 1);
    buf.writeInt(response.getSerializedSize() + 1);
    if (response instanceof ClientProtos.AppendResponse) {
      buf.writeByte(0x00);
    } else if (response instanceof ClientProtos.VoteResponse) {
      buf.writeByte(0x01);
    } else {
      throw new IOException("response is not AppendResponse either VoteResponse");
    }
    buf.writeBytes(response.toByteArray());
    ctx.writeAndFlush(Unpooled.wrappedBuffer(buf), promise);
  }
}
