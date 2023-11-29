package person.alex.raft.node;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import person.alex.raft.protobuf.ClientProtos;
import person.alex.raft.utils.IPCUtils;

import java.io.IOException;
import java.util.List;

public class ServerRequestHandler extends ByteToMessageDecoder {

  Node node;

  public ServerRequestHandler(Node node) {
    super();
    this.node = node;
  }

  Connection connection = new Connection();

  /**
   * @param ctx
   * @param in  服务id（一个字节）#参数
   *            0：AppendEntries
   *            1：RequestVote
   * @throws Exception
   */

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    connection.processOneRpc(in);
    if (connection.request != null)
      node.requestQueue.add(new ServerCall(connection.header, connection.request, ctx.channel(), connection));
    else throw new IOException("can not parse error");
  }

  /**
   * duty for process call
   */
  class Connection {
    ClientProtos.RequestHeader header;
    boolean headerInitialized = false;
    Message request;

    /**
     * the return ByteBuf should be release after the rpc call Finished.
     *
     * @param buf
     * @return
     */
    byte[] processFrame(ByteBuf buf) {
      if (buf.readableBytes() < 4) {
        return null;
      }
      int rpclength = buf.getInt(buf.readerIndex());
      if (buf.readableBytes() < rpclength + 4) {
        return null;
      }

      buf.skipBytes(4);
      byte[] dst = new byte[rpclength];
      buf.readBytes(dst);
      return dst;
    }

    public void processOneRpc(ByteBuf buf) throws IOException {
      byte[] data = processFrame(buf);
      if (data == null) {
        return;
      }
      CodedInputStream cis = CodedInputStream.newInstance(data);
      processHeader(cis);
      processRequest(cis);
    }

    void processHeader(CodedInputStream cis) throws IOException {
      int headerSize = cis.readRawVarint32();
      ClientProtos.RequestHeader.Builder builder = ClientProtos.RequestHeader.newBuilder();
      header = (ClientProtos.RequestHeader) IPCUtils.buildFromCIS(cis, builder, headerSize);
      headerInitialized = true;
    }


    void processRequest(CodedInputStream cis) throws IOException {
      if (!headerInitialized) {
        return;
      }
      int requestSize = cis.readRawVarint32();
      Descriptors.MethodDescriptor md = node.service.getDescriptorForType().findMethodByName(header.getMethod());
      Message.Builder builder = node.service.getRequestPrototype(md).newBuilderForType();
      request = IPCUtils.buildFromCIS(cis, builder, requestSize);
    }
  }
}
