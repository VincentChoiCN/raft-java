package person.alex.raft.node;

import person.alex.raft.protobuf.ClientProtos;

public class NodeServiceImpl implements NodeService {

  Node node;

  public NodeServiceImpl(Node node) {
    this.node = node;
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(ClientProtos.AppendRequest appendRequest) {

    return ClientProtos.AppendResponse.newBuilder().build();
  }

  @Override
  public ClientProtos.VoteResponse requestVote(ClientProtos.VoteRequest voteRequest) {
    return null;
  }
}
