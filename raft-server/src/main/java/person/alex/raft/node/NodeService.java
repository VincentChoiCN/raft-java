package person.alex.raft.node;

import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;

public interface NodeService {

  ClientProtos.AppendResponse appendEntries(ClientProtos.AppendRequest appendRequest) throws IOException, InterruptedException;

  ClientProtos.VoteResponse requestVote(ClientProtos.VoteRequest voteRequest);

}
