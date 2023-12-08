package person.alex.raft.node;

import com.google.protobuf.ServiceException;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import person.alex.raft.client.InternalClient;
import person.alex.raft.client.RaftController;
import person.alex.raft.node.replica.Replica;
import person.alex.raft.protobuf.ClientProtos;
import sun.awt.image.IntegerComponentRaster;

import javax.xml.ws.Response;
import java.awt.peer.ListPeer;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ElectionManager {

  Node node;

  List<Peer> peers;

  Replica replica;

  NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

  Thread thread;

  boolean electionRunning = false;

  public ElectionManager() {

  }

  public ElectionManager(Replica replica, List<Peer> peers) {
    this.peers = peers;
    this.replica = replica;
  }

  public ElectionManager(Node node) {
    this.node = node;
  }

  class ElectionRunnable implements Runnable {

    long lastStartTime;
    long electionTimeout;

    ExecutorService pool = new ThreadPoolExecutor(peers.size(), peers.size(), 5000, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @Override
    public void run() {
      try {
        lastStartTime = System.currentTimeMillis();
        while (electionRunning) {
          // 请求多数
          List<Future<ClientProtos.VoteResponse>> futures = new ArrayList<>();
          ClientProtos.VoteRequest voteRequest = ClientProtos.VoteRequest.newBuilder().setTerm(replica.getCurrentTerm()).setCandidateId(replica.getId()).setLastLogIndex(replica.getLastCommitIndex()).setLastLogTerm(replica.getLastLogTerm()).build();
          for (Peer peer : peers) {
            futures.add(pool.submit(() -> {
              try {
                ClientProtos.VoteResponse response = peer.requestVote(new RaftController(), voteRequest);
                return response;
              } catch (ServiceException e) {
                return null;
              }
            }));
          }

          long waitTime = lastStartTime + electionTimeout - System.currentTimeMillis();
          int voteCount = 0;
          for(Future<ClientProtos.VoteResponse> f : futures) {
            try {
              ClientProtos.VoteResponse response = f.get(waitTime, TimeUnit.MILLISECONDS);
              if(response.getVoteGranted() == true){
                if(++voteCount >= peers.size() / 2) {
                  electionRunning = false;
                  break;
                }
              }
            } catch (Exception e) {

            }
          }


          int randomSleep = new Random().nextInt(5000);
          Thread.sleep(randomSleep);
        }


      } catch (InterruptedException e) {
        e.printStackTrace();
      } finally {

      }
    }
  }
}
