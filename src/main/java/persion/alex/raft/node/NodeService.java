package persion.alex.raft.node;

public interface NodeService {

  AppendResponse appendEntries(AppendRequest appendRequest);

  VoteResponse requestVote(VoteRequest voteRequest);

}
