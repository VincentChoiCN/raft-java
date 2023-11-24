package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;

public interface RpcResponse {

  ByteBuf getResponse();

  void parseFromBuf(ByteBuf buf);
}
