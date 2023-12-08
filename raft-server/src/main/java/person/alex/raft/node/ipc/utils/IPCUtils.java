package person.alex.raft.node.ipc.utils;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

import java.io.IOException;

public class IPCUtils {

  public static int computeMessagesDelimitSize(Message... msgs) {
    int size = 0;
    for (Message msg : msgs) {
      size += CodedOutputStream.computeUInt32SizeNoTag(msg.getSerializedSize());
      size += msg.getSerializedSize();
    }
    return size;
  }

  public static Message buildFromCIS(CodedInputStream cis, Message.Builder builder, int msgLen) throws IOException {
    int oldLimit = cis.pushLimit(msgLen);
    builder.mergeFrom(cis);
    cis.popLimit(oldLimit);
    return builder.build();
  }
}
