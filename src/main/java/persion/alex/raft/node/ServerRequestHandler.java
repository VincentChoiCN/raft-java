package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ServerRequestHandler extends ByteToMessageDecoder {

  NodeService nodeService;

  public ServerRequestHandler(NodeService nodeService) {
    super();
    this.nodeService = nodeService;
  }

  /**
   * @param ctx
   * @param in 服务id（一个字节）#参数
   *            0：AppendEntries
   *            1：RequestVote
   * @throws Exception
   */

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    int requestLen = in.readInt();
    ByteBuf buf =  Unpooled.buffer(requestLen);
    in.readBytes(buf);

    byte b = buf.readByte();
    if ((b & 1) == 0) {
      AppendRequest appendRequest = AppendRequest.parse(buf);
      AppendResponse appendResponse = nodeService.appendEntries(appendRequest);
      ctx.channel().writeAndFlush(appendResponse);
    } else if ((b & 1) == 1) {
      ;
    }
  }
}
