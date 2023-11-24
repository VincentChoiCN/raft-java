package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestAppendRequest {

  public static AppendRequest getAnRandomAppendRequest(int num) {
    int randomNum;
    if (num == 0) randomNum = new Random().nextInt(100);
    else randomNum = num;
    AppendRequest request = new AppendRequest();
    request.setEntry("data1");
    request.setTerm(randomNum);
    request.setLeaderId(randomNum);
    request.setPrevLogIndex(randomNum);
    request.setPrevLogTerm(randomNum);
    request.setLeaderCommit(randomNum);
    return request;
  }

  @Test
  public void TestParse() {
    AppendRequest request = getAnRandomAppendRequest(0);

    ByteBuf buf = Unpooled.buffer(request.toString().getBytes(StandardCharsets.UTF_8).length);
    buf.writeBytes(request.toString().getBytes(StandardCharsets.UTF_8));
    AppendRequest newRequest = AppendRequest.parse(buf);

    assertEquals(newRequest.toString(), request.toString());
    System.out.println(newRequest);
  }
}
