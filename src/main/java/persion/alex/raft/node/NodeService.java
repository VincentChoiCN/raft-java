package persion.alex.raft.node;

import java.io.IOException;

public interface NodeService {

  AppendResponse appendEntries(AppendRequest appendRequest) throws IOException;

  VoteResponse requestVote(VoteRequest voteRequest);

}
