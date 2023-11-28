package person.alex.raft.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import person.alex.raft.protobuf.ClientProtos;

import java.util.Queue;

public class ClientResponseHandler extends ChannelInboundHandlerAdapter {

  Queue<InternalClient.ClientCall> callQ;

  public ClientResponseHandler(Queue<InternalClient.ClientCall> callQ) {
    this.callQ = callQ;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf buf = (ByteBuf) msg;
    int MsgLength = buf.readInt();
    byte code = buf.readByte();
    ByteBuf bytebuf = buf.readSlice(MsgLength - 1);
    InternalClient.ClientCall call = callQ.poll();
    if (code == 0x00) {
      ClientProtos.AppendResponse appendResponse = ClientProtos.AppendResponse.parseFrom(bytebuf.slice().nioBuffer());
      call.setResponse(appendResponse);
    } else if (code == 0x01) {
      ClientProtos.VoteResponse voteResponse = ClientProtos.VoteResponse.parseFrom(bytebuf.slice().nioBuffer());
      call.setResponse(voteResponse);
    }
    synchronized (call) {
      call.notifyAll();
    }
  }
}
