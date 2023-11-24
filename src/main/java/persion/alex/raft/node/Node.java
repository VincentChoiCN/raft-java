package persion.alex.raft.node;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 执行rpcQueue中到来的命令，然后
 */
public class Node implements NodeService {

  public static final Logger LOG = Logger.getLogger(Node.class);

  BlockingQueue<ServerCall> requestQueue = new LinkedBlockingQueue<>();

  NodeStatus status;

  volatile boolean running = true;

  long heartbeatTimeout = 1000L;

  long lastHearbeatTime;

  long pollInterval = 50L;

  NodeService nodeService;

  /**
   * fields before the end flag all should be stored in the stable storage.
   */
  int currentTerm;

  int votedFor;

  NavigableMap<Integer, Entry> log;

  // end;

  long commitIndex;

  long lastApplied;

  int lastCommitIndex;

  Storage storage;

  AtomicLong seqid = new AtomicLong(0);

  public void initialize() throws InterruptedException, IOException {
    nodeService = new NodeServiceImpl(this);
    startRpcServer();
    synchronized (this) {
      status = NodeStatus.FOLLOWER;
    }
    // TODO here should provider a data dir name;
    storage = new FileStorage("");
  }

  public void startRpcServer() throws InterruptedException {
    new NodeRpcServer(7777);
  }

  void triggerElection() {

  }

  public void run() {

  }

  /**
   * 记录到持久化存储中，告知leader我们的存储结果
   *
   * @param appendRequest
   * @return
   */
  @Override
  public AppendResponse appendEntries(AppendRequest appendRequest) throws IOException {
    AppendResponse response = new AppendResponse();
    response.setTerm(currentTerm);
    if (appendRequest.term < this.currentTerm) {
      // clause-1
      response.setSuccess(false);
      return response;
    }
    int prevLogIndex = appendRequest.prevLogIndex;
    int prevLogTerm = appendRequest.prevLogTerm;
    Entry prevLogEntry = null;
    if (log.size() > prevLogIndex && (prevLogEntry = log.get(prevLogIndex)) != null) {
      if (prevLogEntry.epoch != prevLogTerm) {
        // clause-3
        Iterator<Integer> it = log.tailMap(prevLogIndex).keySet().iterator();
        while (it.hasNext()) {
          it.remove();
          storage.delete(new Entry(prevLogIndex, seqid.incrementAndGet()));
        }
      }
      // clause-4
      Entry entry = new Entry(appendRequest.entry, currentTerm, prevLogIndex + 1, seqid.incrementAndGet());
      log.put(prevLogIndex + 1, entry);
      storage.append(entry);
      lastApplied = prevLogIndex + 1;

      // clause-5
      commitIndex = Math.min(appendRequest.leaderCommit, lastCommitIndex);
      response.setSuccess(true);
    } else {
      // clause-2
      response.setSuccess(false);
    }
    // according to clause-1, there is no difference between FOLLOWER and CANDIDATE
    if (status == Node.NodeStatus.FOLLOWER) {

    } else if (status == Node.NodeStatus.CANDIDATE) {
      // receive one append when current status is candidate, so let's check we should change to be follower, or just ignore.

    }
    return response;
  }

  @Override
  public VoteResponse requestVote(VoteRequest voteRequest) {
    return null;
  }

  enum NodeStatus {
    FOLLOWER, CANDIDATE, LEADER
  }

  class RequestProcessor extends Thread {

    private Node node;

    public RequestProcessor(Node node) {
      this.node = node;
    }

    public void run() {
      try {
        while (running) {
          long waitUntil = lastHearbeatTime + heartbeatTimeout;
          ServerCall one = null;
          try {
            do {
              one = requestQueue.poll(pollInterval, TimeUnit.MICROSECONDS);
            } while (System.currentTimeMillis() <= waitUntil);
          } catch (InterruptedException e) {
            // 心跳超时了
            synchronized (node) {
              if (status == NodeStatus.FOLLOWER) {
                status = NodeStatus.CANDIDATE;
                node.triggerElection();
              } else {
                // should do something for CANDIDATE or LEADER ??
              }
            }
          }
          // get one request, process
          if (one.request instanceof VoteRequest) {
            VoteResponse voteResponse = nodeService.requestVote((VoteRequest) one.request);
            one.setResponse(voteResponse);
            one.flush();
          } else if (one.request instanceof AppendRequest) {
            AppendResponse appendResponse = nodeService.appendEntries((AppendRequest) one.request);
            one.setResponse(appendResponse);
            one.flush();
          } else {
            // should not reach here.
          }

        }
      } catch (IOException e) {
        LOG.error("process request failed", e);
        System.exit(1);
      } finally {

      }
    }

  }

  public static void main(String[] args) throws InterruptedException {

  }
}
