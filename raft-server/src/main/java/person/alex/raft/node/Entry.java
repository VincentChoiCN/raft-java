package person.alex.raft.node;

public class Entry {

  String msg;
  int epoch;
  int index;
  long seqId;

  public Entry(String msg, int epoch, int index, long seqId) {
    this.msg = msg;
    this.epoch = epoch;
    this.index = index;
    this.seqId = seqId;
  }

  /**
   * for delete entry
   */
  public Entry(int index, long seqId) {
    this.index = index;
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

  public int getEpoch() {
    return epoch;
  }

  public void setEpoch(int epoch) {
    this.epoch = epoch;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  /**
   * index#epoch#seqid#msg
   *
   * @return
   */
  public String toString() {
    return index + "#" + epoch + "#" + seqId + "#" + msg;
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
          entry.index = Integer.parseInt(item);
          break;
        case 1:
          entry.epoch = Integer.parseInt(item);
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
