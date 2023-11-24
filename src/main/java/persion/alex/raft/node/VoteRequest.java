package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;

public class VoteRequest implements RpcRequest{
  int term;
  int candidateId;
  long lastLogIndex;
  int lastLogTerm;


  public static VoteRequest parse(ByteBuf buf) {
    VoteRequest request = new VoteRequest();
    int index = 0;
    while (buf.readableBytes() > 0) {
      String item = "";
      char c;
      while (buf.readableBytes() > 0 && (c = (char) buf.readByte()) != '#') {
        item += c;
      }
      switch (index) {
        case 0:
          request.term = Integer.parseInt(item);
          break;
        case 1:
          request.candidateId = Integer.parseInt(item);
          break;
        case 2:
          request.lastLogIndex = Long.parseLong(item);
          break;
        case 3:
          request.lastLogTerm = Integer.parseInt(item);
          break;
      }
      index++;
    }
    return request;
  }

  public java.lang.String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(candidateId).append("#").append(lastLogIndex).append("#").append(lastLogTerm);
    return sb.toString();
  }
}
