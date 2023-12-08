package person.alex.raft.node.ipc;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import person.alex.raft.node.Node;
import person.alex.raft.node.replica.Replica;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;

public class NodeServiceImpl implements ClientProtos.ClientService.BlockingInterface {

  Node node;

  Replica replica = new Replica();

  public NodeServiceImpl(Node node) throws IOException {
    this.node = node;
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(RpcController controller, ClientProtos.AppendRequest request) throws ServiceException {
    return replica.appendEntries(controller, request);
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return replica.requestVote(controller, request);
  }
}
