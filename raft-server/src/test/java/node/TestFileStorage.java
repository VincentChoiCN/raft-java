package node;

import org.junit.Before;
import org.junit.Test;
import person.alex.raft.node.ipc.Entry;
import person.alex.raft.node.FileStorage;

import java.io.File;
import java.io.IOException;
import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class TestFileStorage {

  String dir = "/tmp/raft_test";

  @Before
  public void testBefore() {
    File file = new File(dir);
    if (file.exists()) {
      file.delete();
    }
    file.mkdir();
  }

  public Entry getEntry(int num) {
    return new Entry("str" + num, num, num, num);
  }

  @Test
  public void testAppend() throws IOException {
    FileStorage fileStorage = new FileStorage(dir);
    fileStorage.append(getEntry(1));

    fileStorage.append(getEntry(2));
    fileStorage.append(getEntry(3));
    fileStorage.append(getEntry(4));

    NavigableMap<Integer, Entry> map = fileStorage.load();
    assertEquals(map.firstEntry().getValue().getMsg(), getEntry(1).getMsg());
    assertEquals(map.lastEntry().getValue().getMsg(), getEntry(4).getMsg());
  }
}
