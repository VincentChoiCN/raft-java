package person.alex.raft.node;

import person.alex.raft.node.ipc.Entry;

import java.io.IOException;
import java.util.NavigableMap;

public interface Storage {

  void append(Entry entry) throws IOException;

  NavigableMap<Integer, Entry> load() throws IOException;

  void delete(Entry entry) throws IOException;
}
