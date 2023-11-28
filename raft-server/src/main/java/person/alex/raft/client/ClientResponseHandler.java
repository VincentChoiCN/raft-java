package person.alex.raft.client;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import person.alex.raft.protobuf.ClientProtos;

import java.util.Map;
import java.util.Queue;

public class ClientResponseHandler extends ChannelInboundHandlerAdapter {

  Map<Long, InternalClient.ClientCall> callQ;

  public ClientResponseHandler(Map<Long, InternalClient.ClientCall> callQ) {
    this.callQ = callQ;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    ByteBuf buf = (ByteBuf) msg;
    int MsgLength = buf.readInt();
    byte code = buf.readByte();
    ByteBuf bytebuf = buf.readSlice(MsgLength - 1);

    Message response = null;
    long callId = 0;
    if (code == 0x00) {
      response = ClientProtos.AppendResponse.parseFrom(bytebuf.slice().nioBuffer());
      callId = ((ClientProtos.AppendResponse)response).getCallId();
    } else if (code == 0x01) {
      response = ClientProtos.VoteResponse.parseFrom(bytebuf.slice().nioBuffer());
      callId = ((ClientProtos.VoteResponse)response).getCallId();
    }
    InternalClient.ClientCall call = callQ.get(callId);
    call.done.complete(response);
  }
}
