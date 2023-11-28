package person.alex.raft.node;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import person.alex.raft.protobuf.ClientProtos;

public class NodeServiceImpl implements ClientProtos.ClientService.BlockingInterface {

  Node node;

  public NodeServiceImpl(Node node) {
    this.node = node;
  }

  @Override
  public ClientProtos.AppendResponse appendEntries(RpcController controller, ClientProtos.AppendRequest request) throws ServiceException {
    return ClientProtos.AppendResponse.newBuilder().setCallId(request.getCallId()).setSuccess(true).setTerm(-1).build();
  }

  @Override
  public ClientProtos.VoteResponse requestVote(RpcController controller, ClientProtos.VoteRequest request) throws ServiceException {
    return null;
  }
}
