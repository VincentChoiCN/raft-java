package persion.alex.raft.node;

public class NodeServiceImpl implements NodeService {

  Node node;

  public NodeServiceImpl(Node node) {
    this.node = node;
  }

  @Override
  public AppendResponse appendEntries(AppendRequest appendRequest) {

    return new AppendResponse();
  }

  @Override
  public VoteResponse requestVote(VoteRequest voteRequest) {
    return null;
  }
}
