package person.alex.raft.node.replica;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import io.netty.util.Timeout;
import org.apache.log4j.Logger;
import person.alex.raft.node.ElectionManager;
import person.alex.raft.node.FileStorage;
import person.alex.raft.node.Node;
import person.alex.raft.node.Peer;
import person.alex.raft.node.Storage;
import person.alex.raft.node.ipc.Entry;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;
import java.util.List;
import java.util.NavigableMap;

// Represents one replica of the data.
public class Replica implements ClientProtos.ClientService.BlockingInterface {

  public static final Logger LOG = Logger.getLogger(Replica.class);

  int id;

  // begin raft
  /**
   * fields before the end flag all should be stored in the stable storage.
   */
  int currentTerm;

  int votedFor;

  //
  NavigableMap<Integer, Entry> log;

  long commitIndex;

  long lastApplied;

  int lastCommitIndex;

  // end raft

  //
  long heartbeatTimeout = 1000L;

  long lastHeartbeatTime;

  Storage storage;

  boolean enable = true;

  boolean shouldTriggerElection = false;

  boolean electionRunning = false;

  ElectionManager electionManager;

  ReplicaStatus status;

  List<Peer> peers;

  public Replica() throws IOException {
    // TODO here should provider a data dir name;
    storage = new FileStorage("/tmp/raft");
    status = ReplicaStatus.FOLLOWER;
  }

  class ElectionTimeout extends Thread {

    public void run() {
      while (enable) {
        try {
          Thread.sleep(heartbeatTimeout);

          // 超过选举超时时间
          if (System.currentTimeMillis() - lastHeartbeatTime > heartbeatTimeout) {
            LOG.warn("election timeout, trigger the election");


            // trigger election manager
            if (shouldTriggerElection && !electionRunning) {
              status = ReplicaStatus.CANDIDATE;
              electionManager = new ElectionManager(Replica.this, peers);
              electionManager.start();
              electionRunning = true;
              shouldTriggerElection = false;
            }
          } else {
            // do nothing;
          }
        } catch (InterruptedException ie) {

        } finally {

        }
      }
    }
  }

  private void deleteExceededEntries(int prevLogIndex) {

  }

  private void appendLog(ClientProtos.AppendRequest request) {

  }

  @Override
  public ClientProtos.AppendResponse appendEntries(RpcController controller, ClientProtos.AppendRequest request) throws ServiceException {

    if(request.getTerm() < this.currentTerm) {
      return createErrorResponseForAppend();
    }

    Entry entry = log.get(request.getPrevLogIndex());
    if(entry == null) {
      return createErrorResponseForAppend();
    }

    if(entry.getTerm() != request.getTerm()) {
      deleteExceededEntries(request.getPrevLogIndex());
    }

    appendLog(request);

    if(request.getLeaderCommit() > commitIndex) {
      commitIndex = Math.min(request.getLeaderCommit(), log.lastKey());
    }

    return ClientProtos.AppendResponse.newBuilder().setTerm(this.currentTerm).setSuccess(true).build();
  }

  private ClientProtos.AppendResponse createErrorResponseForAppend() {
    return ClientProtos.AppendResponse.newBuilder().setTerm(this.currentTerm).setSuccess(false).build();
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return null;
  }

  public int getId() {
    return id;
  }

  public int getCurrentTerm() {
    return currentTerm;
  }

  public int getVotedFor() {
    return votedFor;
  }

  public NavigableMap<Integer, Entry> getLog() {
    return log;
  }

  public long getCommitIndex() {
    return commitIndex;
  }

  public long getLastApplied() {
    return lastApplied;
  }

  public int getLastCommitIndex() {
    return lastCommitIndex;
  }

  public long getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  public long getLastHeartbeatTime() {
    return lastHeartbeatTime;
  }

  public Storage getStorage() {
    return storage;
  }

  public boolean isEnable() {
    return enable;
  }

  public boolean isShouldTriggerElection() {
    return shouldTriggerElection;
  }

  public boolean isElectionRunning() {
    return electionRunning;
  }

  public ElectionManager getElectionManager() {
    return electionManager;
  }

  public ReplicaStatus getStatus() {
    return status;
  }

  public List<Peer> getPeers() {
    return peers;
  }

  public int getLastLogTerm() {
    return log.get(lastCommitIndex).getTerm();
  }

  void triggerElection() {
    //pool.execute(new Election());
  }
}
