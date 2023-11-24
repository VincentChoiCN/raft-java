package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;

public class AppendRequest implements RpcRequest{

  int term;
  int leaderId;
  int prevLogIndex;
  int prevLogTerm;
  String entry;
  int leaderCommit;

  public int getTerm() {
    return term;
  }

  public void setTerm(int term) {
    this.term = term;
  }

  public int getLeaderId() {
    return leaderId;
  }

  public void setLeaderId(int leaderId) {
    this.leaderId = leaderId;
  }

  public long getPrevLogIndex() {
    return prevLogIndex;
  }

  public void setPrevLogIndex(int prevLogIndex) {
    this.prevLogIndex = prevLogIndex;
  }

  public int getPrevLogTerm() {
    return prevLogTerm;
  }

  public void setPrevLogTerm(int prevLogTerm) {
    this.prevLogTerm = prevLogTerm;
  }

  public String getEntry() {
    return entry;
  }

  public void setEntry(String entry) {
    this.entry = entry;
  }

  public long getLeaderCommit() {
    return leaderCommit;
  }

  public void setLeaderCommit(int leaderCommit) {
    this.leaderCommit = leaderCommit;
  }

  public static AppendRequest parse(ByteBuf buf) {
    AppendRequest request = new AppendRequest();
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
          request.leaderId = Integer.parseInt(item);
          break;
        case 2:
          request.prevLogIndex = Integer.parseInt(item);
          break;
        case 3:
          request.prevLogTerm = Integer.parseInt(item);
          break;
        case 4:
          request.entry = item;
          break;
        case 5:
          request.leaderCommit = Integer.parseInt(item);
          break;
      }
      index++;
    }
    return request;
  }

  public java.lang.String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(term).append("#").append(leaderId).append("#").append(prevLogIndex).append("#").append(prevLogTerm).append("#").append(entry).append("#").append(leaderCommit);
    return sb.toString();
  }

}
