package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class VoteResponse implements RpcResponse{
  int term;
  boolean voteGranted;


  @Override
  public ByteBuf getResponse() {
    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(voteGranted);
    ByteBuf buf = Unpooled.buffer(sb.toString().getBytes(StandardCharsets.UTF_8).length);
    buf.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
    return buf;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(voteGranted);
    return sb.toString();
  }

  @Override
  public void parseFromBuf(ByteBuf buf) {
    int index = 0;
    while (buf.readableBytes() > 0) {
      String item = "";
      char c;
      while (buf.readableBytes() > 0 && (c = (char) buf.readByte()) != '#') {
        item += c;
      }
      switch (index) {
        case 0:
          this.term = Integer.parseInt(item);
          break;
        case 1:
          this.voteGranted = Boolean.parseBoolean(item);
          break;
      }
      index++;
    }
  }
}
