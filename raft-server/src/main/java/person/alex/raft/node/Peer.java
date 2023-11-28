package person.alex.raft.node;

public class Peer {

  private int id;
  private int port;
  private String ip;

  public Peer(int id, int port, String ip) {
    this.id = 0;
    this.port = port;
    this.ip = ip;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }
}
