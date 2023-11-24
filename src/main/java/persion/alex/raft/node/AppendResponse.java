package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class AppendResponse implements RpcResponse {

  int term;
  boolean success;

  public int getTerm() {
    return term;
  }

  public void setTerm(int term) {
    this.term = term;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  @Override
  public ByteBuf getResponse() {
    // fake work
    term = 55555;

    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(success);
    ByteBuf buf = Unpooled.buffer(sb.toString().getBytes(StandardCharsets.UTF_8).length);
    buf.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
    return buf;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(success);
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
          this.success = Boolean.parseBoolean(item);
          break;
      }
      index++;
    }
  }
}
