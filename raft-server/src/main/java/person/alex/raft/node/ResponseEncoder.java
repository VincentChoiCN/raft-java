package person.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import person.alex.raft.node.ipc.ServerCall;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.node.ipc.utils.IPCUtils;

public class ResponseEncoder extends ChannelOutboundHandlerAdapter {

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ServerCall call = (ServerCall) msg;
    ClientProtos.ResponseHeader header = ClientProtos.ResponseHeader.newBuilder().setCallId(call.getRequestHeader().getCallId()).build();
    call.setResponseHeader(header);
    int responseSize = IPCUtils.computeMessagesDelimitSize(call.getResponseHeader(), call.getResponse());
    ByteBuf buf = Unpooled.buffer(4 + responseSize);
    ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
    bbos.writeInt(responseSize);
    call.getResponseHeader().writeDelimitedTo(bbos);
    call.getResponse().writeDelimitedTo(bbos);
    ctx.writeAndFlush(Unpooled.wrappedBuffer(buf), promise);
  }
}
