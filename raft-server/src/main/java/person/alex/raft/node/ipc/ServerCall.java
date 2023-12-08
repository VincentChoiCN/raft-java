package person.alex.raft.node.ipc;

import com.google.protobuf.BlockingService;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.ServiceException;
import io.netty.channel.Channel;
import person.alex.raft.client.RaftController;
import person.alex.raft.protobuf.ClientProtos;

public class ServerCall {

  Channel channel;
  ClientProtos.RequestHeader requestHeader;
  Message request;
  ClientProtos.ResponseHeader responseHeader;
  Message response;
  NodeRpcServer.ServerRequestHandler.Connection connection;

  public ServerCall(ClientProtos.RequestHeader requestHeader, Message request, Channel channel, NodeRpcServer.ServerRequestHandler.Connection connection) {
    this.requestHeader = requestHeader;
    this.request = request;
    this.channel = channel;
    this.connection = connection;
  }

  public ClientProtos.RequestHeader getRequestHeader() {
    return requestHeader;
  }

  public void setRequestHeader(ClientProtos.RequestHeader requestHeader) {
    this.requestHeader = requestHeader;
  }

  public Message getRequest() {
    return request;
  }

  public void setRequest(Message request) {
    this.request = request;
  }

  public ClientProtos.ResponseHeader getResponseHeader() {
    return responseHeader;
  }

  public void setResponseHeader(ClientProtos.ResponseHeader responseHeader) {
    this.responseHeader = responseHeader;
  }

  public Message getResponse() {
    return response;
  }

  public void setResponse(Message response) {
    this.response = response;
  }

  public NodeRpcServer.ServerRequestHandler.Connection getConnection() {
    return connection;
  }

  public void setConnection(NodeRpcServer.ServerRequestHandler.Connection connection) {
    this.connection = connection;
  }

  public void flush() {
    channel.writeAndFlush(this);
  }

  public void call() throws ServiceException {
    BlockingService service = connection.rpcServer.service;
    Descriptors.MethodDescriptor md = service.getDescriptorForType().findMethodByName(requestHeader.getMethod());
    response = service.callBlockingMethod(md, new RaftController(), request);
    this.flush();
  }

  /**
   * release the resource include connection;
   */
  public void clear() {

  }
}
