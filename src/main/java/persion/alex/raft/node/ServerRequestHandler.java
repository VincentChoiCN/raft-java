package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class ServerRequestHandler extends ByteToMessageDecoder {

  Node node;

  public ServerRequestHandler(Node node) {
    super();
    this.node = node;
  }

  /**
   * @param ctx
   * @param in  服务id（一个字节）#参数
   *            0：AppendEntries
   *            1：RequestVote
   * @throws Exception
   */

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    int requestLen = in.readInt();
    ByteBuf buf = Unpooled.buffer(requestLen);
    in.readBytes(buf);

    byte b = buf.readByte();
    RpcRequest request = null;
    if ((b & 1) == 0) {
      request = AppendRequest.parse(buf);
    } else if ((b & 1) == 1) {
      request = VoteRequest.parse(buf);
    }
    if (request != null) node.requestQueue.add(new ServerCall(request, ctx.channel()));
    else throw new IOException("can not parse error");
  }
}
