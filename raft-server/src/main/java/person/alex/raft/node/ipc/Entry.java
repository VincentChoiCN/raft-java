package person.alex.raft.node.ipc;

public class Entry {

  String msg;
  int term;
  int commitIndex;
  long seqId;

  public Entry(String msg, int term, int index, long seqId) {
    this.msg = msg;
    this.term = term;
    this.commitIndex = index;
    this.seqId = seqId;
  }

  /**
   * for delete entry
   */
  public Entry(int index, long seqId) {
    this.commitIndex = index;
    this.seqId = seqId;
  }

  public Entry() {

  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public int getTerm() {
    return term;
  }

  public void setTerm(int term) {
    this.term = term;
  }

  public int getCommitIndex() {
    return commitIndex;
  }

  public void setCommitIndex(int commitIndex) {
    this.commitIndex = commitIndex;
  }

  public long getSeqId() {
    return seqId;
  }

  public void setSeqId(long seqId) {
    this.seqId = seqId;
  }

  /**
   * index#epoch#seqid#msg
   *
   * @return
   */
  public String toString() {
    return commitIndex + "#" + term + "#" + seqId + "#" + msg;
  }

  public static Entry parseFromString(String str) {
    Entry entry = new Entry();
    int count = 0, index = 0;
    while (index < str.length()) {
      String item = "";
      char c;
      while (index < str.length() && (c = str.charAt(index++)) != '#') {
        item += c;
      }
      switch (count) {
        case 0:
          entry.commitIndex = Integer.parseInt(item);
          break;
        case 1:
          entry.term = Integer.parseInt(item);
          break;
        case 2:
          entry.seqId = Long.parseLong(item);
          break;
        case 3:
          entry.msg = item;
          break;
      }
      count++;
    }
    return entry;
  }
}
