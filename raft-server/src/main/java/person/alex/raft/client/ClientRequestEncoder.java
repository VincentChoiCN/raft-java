package person.alex.raft.client;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.utils.IPCUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientRequestEncoder extends ChannelDuplexHandler {

  Map<Long, InternalClient.ClientCall> callQ = new ConcurrentHashMap<>();

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    InternalClient.ClientCall call = (InternalClient.ClientCall) msg;
    callQ.put(((ClientProtos.RequestHeader) call.header).getCallId(), call);

    int rpcSize = 4 + IPCUtils.computeMessagesDelimitSize(call.header, call.request);
    ByteBuf buffer = Unpooled.buffer(rpcSize);
    ByteBufOutputStream os = new ByteBufOutputStream(buffer);
    os.writeInt(rpcSize - 4);
    call.header.writeDelimitedTo(os);
    call.request.writeDelimitedTo(os);
    ctx.write(Unpooled.wrappedBuffer(buffer));
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    byte[] data = readFrame((ByteBuf) msg);
    if (data == null) return;

    CodedInputStream cis = CodedInputStream.newInstance(data);
    int responseHeaderSize = cis.readRawVarint32();
    ClientProtos.ResponseHeader header = (ClientProtos.ResponseHeader) IPCUtils.buildFromCIS(cis, ClientProtos.ResponseHeader.newBuilder(), responseHeaderSize);
    int responseSize = cis.readRawVarint32();
    long callId = header.getCallId();
    InternalClient.ClientCall call = callQ.get(callId);
    Message.Builder builder = call.returnType.newBuilderForType();
    IPCUtils.buildFromCIS(cis, builder, responseSize);
    call.complete(builder.build());
  }

  private byte[] readFrame(ByteBuf buf) {
    if (buf.readableBytes() < 4) return null;

    int responseSize = buf.getInt(buf.readerIndex());
    if (buf.readableBytes() < responseSize + 4) return null;
    buf.skipBytes(4);
    byte[] dst = new byte[responseSize];
    buf.readBytes(dst);
    return dst;
  }
}
