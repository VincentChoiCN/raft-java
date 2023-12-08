package node;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import person.alex.raft.protobuf.ClientProtos;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestAppendRequest {

  public static final byte[] oneMB = new byte[1024 * 1024];

  public static ClientProtos.AppendRequest getAnRandomAppendRequest(int num) {
    int randomNum;
    if (num == 0) randomNum = new Random().nextInt(100);
    else randomNum = num;
    ClientProtos.AppendRequest.Builder request = ClientProtos.AppendRequest.newBuilder();
    request.setEntry("data_1");
    request.setTerm(randomNum);
    request.setLeaderId(randomNum);
    request.setPrevLogIndex(randomNum);
    request.setPrevLogTerm(randomNum);
    request.setLeaderCommit(randomNum);
    return request.build();
  }

  /**
   * 1 MB Entry
   * @return
   */
  public static ClientProtos.AppendRequest getLargeRequest() {
    int randomNum = new Random().nextInt(100);
    ClientProtos.AppendRequest.Builder request = ClientProtos.AppendRequest.newBuilder();
    request.setEntry(new String(oneMB, StandardCharsets.UTF_8));
    request.setTerm(randomNum);
    request.setLeaderId(randomNum);
    request.setPrevLogIndex(randomNum);
    request.setPrevLogTerm(randomNum);
    request.setLeaderCommit(randomNum);
    return request.build();
  }

  @Test
  public void TestParse() throws InvalidProtocolBufferException {
    ClientProtos.AppendRequest request = getAnRandomAppendRequest(0);

    ByteBuf buf = Unpooled.buffer(request.toString().getBytes(StandardCharsets.UTF_8).length);
    buf.writeBytes(request.toString().getBytes(StandardCharsets.UTF_8));
    ClientProtos.AppendRequest newRequest = ClientProtos.AppendRequest.parseFrom(buf.slice().array());

    assertEquals(newRequest.toString(), request.toString());
    System.out.println(newRequest);
  }
}
