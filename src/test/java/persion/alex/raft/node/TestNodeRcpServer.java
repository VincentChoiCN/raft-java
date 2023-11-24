package persion.alex.raft.node;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestNodeRcpServer {

  @Test
  public void TestRpcProcess() throws InterruptedException, IOException {
    NodeRpcServer server = new NodeRpcServer(7777);

    Socket socket = new Socket();
    socket.connect(new InetSocketAddress("127.0.0.1", 7777));
    OutputStream os = socket.getOutputStream();
    InputStream is = socket.getInputStream();
    AppendRequest request = TestAppendRequest.getAnRandomAppendRequest(4);
    byte[] msg = request.toString().getBytes(StandardCharsets.UTF_8);
    ByteBuffer intBuffer = ByteBuffer.allocate(4);
    intBuffer.putInt(msg.length + 1);
    os.write(intBuffer.array());
    os.write((byte) 0x00);
    os.write(msg);
    os.flush();
    byte[] intBytes = new byte[4];
    for (int i = 0; i < 4; i++) {
      intBytes[i] = (byte) is.read();
    }
    intBuffer.clear();
    intBuffer.put(intBytes);
    intBuffer.flip();
    int responseLen = intBuffer.getInt();
    byte[] msgBytes = new byte[responseLen];
    for (int i = 0; i < responseLen; i++) {
      msgBytes[i] = (byte) is.read();
    }
    ByteBuf buf = Unpooled.buffer(responseLen);
    buf.writeBytes(msgBytes);
    AppendResponse response = new AppendResponse();
    response.parseFromBuf(buf);
    System.out.println(response);
    AppendResponse expectResponse = new AppendResponse();
    expectResponse.setSuccess(false);
    expectResponse.setTerm(4);
    assertNotEquals(response.toString(), expectResponse.toString());
    assertEquals(response.toString().replace("55555", "4"), expectResponse.toString());
  }
}
