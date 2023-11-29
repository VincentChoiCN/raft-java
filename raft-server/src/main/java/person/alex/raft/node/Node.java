package person.alex.raft.node;

import com.google.protobuf.BlockingService;
import com.google.protobuf.ServiceException;
import org.apache.log4j.Logger;
import person.alex.raft.client.RaftController;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 执行rpcQueue中到来的命令，然后
 */
public class Node {

  public static final Logger LOG = Logger.getLogger(Node.class);

  BlockingQueue<ServerCall> requestQueue = new LinkedBlockingQueue<>();

  NodeStatus status;

  volatile boolean running = true;

  long heartbeatTimeout = 1000L;

  long lastHearbeatTime;

  long pollInterval = 50L;

  ClientProtos.ClientService.BlockingInterface nodeService;

  BlockingService service;

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

  ExecutorService pool = new ThreadPoolExecutor(5, 5, Long.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  int id;

  Map<Integer, Peer> peers;

  InetSocketAddress localAddress;


  // only for test
  public Node(InetSocketAddress address) {
    this.localAddress = address;
  }

  public Node(int id, Map<Integer, Peer> peers) {
    this.id = id;
    this.peers = peers;
  }

  public void initialize() throws InterruptedException, IOException {
    running = true;
    nodeService = new NodeServiceImpl(this);
    service = ClientProtos.ClientService.newReflectiveBlockingService(nodeService);
    startRpcServer();
    synchronized (this) {
      status = NodeStatus.FOLLOWER;
    }
    // TODO here should provider a data dir name;
    storage = new FileStorage("/tmp/raft");
    RequestProcessor proccessor = new RequestProcessor(this);
    proccessor.start();
  }

  public void startRpcServer() throws InterruptedException, IOException {
    new NodeRpcServer(localAddress, this);
  }

  void triggerElection() {
    pool.execute(new Election());
  }


//  /**
//   * 记录到持久化存储中，告知leader我们的存储结果
//   *
//   * @param appendRequest
//   * @return
//   */
//  @Override
//  public ClientProtos.AppendResponse appendEntries(ClientProtos.AppendRequest appendRequest) throws IOException {
//    ClientProtos.AppendResponse.Builder response = ClientProtos.AppendResponse.newBuilder();
//    response.setTerm(currentTerm);
//    if (appendRequest.getTerm() < this.currentTerm) {
//      // clause-1
//      response.setSuccess(false);
//      return response.build();
//    }
//    int prevLogIndex = appendRequest.getPrevLogIndex();
//    int prevLogTerm = appendRequest.getPrevLogTerm();
//    Entry prevLogEntry = null;
//    if (log.size() > prevLogIndex && (prevLogEntry = log.get(prevLogIndex)) != null) {
//      if (prevLogEntry.epoch != prevLogTerm) {
//        // clause-3
//        Iterator<Integer> it = log.tailMap(prevLogIndex).keySet().iterator();
//        while (it.hasNext()) {
//          it.remove();
//          storage.delete(new Entry(prevLogIndex, seqid.incrementAndGet()));
//        }
//      }
//      // clause-4
//      Entry entry = new Entry(appendRequest.getEntry(), currentTerm, prevLogIndex + 1, seqid.incrementAndGet());
//      log.put(prevLogIndex + 1, entry);
//      storage.append(entry);
//      lastApplied = prevLogIndex + 1;
//
//      // clause-5
//      commitIndex = Math.min(appendRequest.getLeaderCommit(), lastCommitIndex);
//      response.setSuccess(true);
//    } else {
//      // clause-2
//      response.setSuccess(false);
//    }
//    // according to clause-1, there is no difference between FOLLOWER and CANDIDATE
//    if (status == Node.NodeStatus.FOLLOWER) {
//
//    } else if (status == Node.NodeStatus.CANDIDATE) {
//      // receive one append when current status is candidate, so let's check we should change to be follower, or just ignore.
//
//    }
//    return response.build();
//  }

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

          }
          if (one == null) {
            synchronized (node) {
              if (status == NodeStatus.FOLLOWER) {
                status = NodeStatus.CANDIDATE;
                node.triggerElection();
              } else {
                // should do something for CANDIDATE or LEADER ??
              }
            }
            continue;
          }

          // get one request, process
          if (one.request instanceof ClientProtos.VoteRequest) {
            ClientProtos.VoteResponse voteResponse = nodeService.requestVote(new RaftController(), (ClientProtos.VoteRequest) one.request);
            one.setResponse(voteResponse);
            one.flush();
          } else if (one.request instanceof ClientProtos.AppendRequest) {
            ClientProtos.AppendResponse appendResponse = nodeService.appendEntries(new RaftController(), (ClientProtos.AppendRequest) one.request);
            one.setResponse(appendResponse);
            one.flush();
          } else {
            // should not reach here.
          }

        }
      } catch (ServiceException e) {
        e.printStackTrace();
      } finally {

      }
    }

  }
}
