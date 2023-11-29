package person.alex.raft.node;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.utils.IPCUtils;

import java.io.IOException;

public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ServerCall call = (ServerCall) msg;
    ClientProtos.ResponseHeader header = ClientProtos.ResponseHeader.newBuilder().setCallId(call.requestHeader.getCallId()).build();
    call.setResponseHeader(header);
    int responseSize = IPCUtils.computeMessagesDelimitSize(call.responseHeader, call.response);
    ByteBuf buf = Unpooled.buffer(4 + responseSize);
    ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
    bbos.writeInt(responseSize);
    call.responseHeader.writeDelimitedTo(bbos);
    call.response.writeDelimitedTo(bbos);
    ctx.writeAndFlush(Unpooled.wrappedBuffer(buf), promise);
  }
}
