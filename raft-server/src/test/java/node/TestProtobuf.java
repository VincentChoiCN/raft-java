package node;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;
import org.junit.Test;
import person.alex.raft.protobuf.ClientProtos;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestProtobuf {

  @Test
  public void testCodedThing() throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(100);
    CodedOutputStream cos = CodedOutputStream.newInstance(buffer);
    cos.writeUInt32NoTag(32);
    cos.writeUInt32NoTag(34);
    ClientProtos.AppendRequest request = TestAppendRequest.getAnRandomAppendRequest(4);
    cos.writeUInt32NoTag(request.getSerializedSize());
    request.writeTo(cos);
    cos.flush();

    CodedInputStream cis = CodedInputStream.newInstance(buffer.array());
    int num = cis.readRawVarint32();
    assertEquals(32, num);
    num = cis.readRawVarint32();
    assertEquals(34, num);

    int msglen = cis.readRawVarint32();
    System.out.println(msglen);
    cis.pushLimit(msglen);
    ClientProtos.AppendRequest msg = ClientProtos.AppendRequest.newBuilder().mergeFrom(cis).build();
    cis.popLimit(msglen);
    System.out.println(msg);
  }
}
