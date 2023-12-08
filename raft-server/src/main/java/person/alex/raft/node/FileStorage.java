package person.alex.raft.node;

import org.apache.log4j.Logger;
import person.alex.raft.node.ipc.Entry;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * format like below:
 * logIndex#epoch#StringBody\n
 * logIndex#epoch#StringBody\n
 * ……
 */
public class FileStorage implements Storage {

  public static final Logger LOG = Logger.getLogger(FileStorage.class);

  File data;

  File delete;

  DataInput input;

  DataOutput output;

  DataInput deleteInput;

  DataOutput deleteOutput;

  public FileStorage(String dirPath) throws IOException {
    File dir = new File(dirPath);
    if (!dir.isDirectory()) {
      throw new IOException(dirPath + " is not a valid dir");
    }
    File[] files = dir.listFiles();

    if (files == null || files.length < 1) {
      LOG.info("start to create a data file and a delete file");
      String dataPath = dirPath + File.separator + "data";
      data = new File(dataPath);
      data.createNewFile();


      String deletePath = dirPath + File.separator + "delete";
      delete = new File(deletePath);
      delete.createNewFile();
    } else if (files.length == 2) {
      for (File f : files) {
        if (f.getName().equals("data")) {
          data = f;
        } else if (f.getName().equals("delete")) {
          delete = f;
        } else {
          // should not reach here;
          throw new IOException("data dir has a file named " + f.getName() + ". maybe the data dir has bean broken.");
        }
      }
    }

    // init the inputstream and outputstream;
    input = new DataInputStream(new FileInputStream(data));
    output = new DataOutputStream(new FileOutputStream(data));

    deleteInput = new DataInputStream(new FileInputStream(delete));
    deleteOutput = new DataOutputStream(new FileOutputStream(delete));
  }

  @Override
  public void append(Entry entry) throws IOException {
    output.writeBytes(entry.toString());
    output.writeByte('\n');
  }

  @Override
  public NavigableMap<Integer, Entry> load() throws IOException {
    NavigableMap<Integer, Entry> treeMap = new TreeMap<>(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return o1 - o2;
      }
    });
    String str;
    while((str = input.readLine()) != null) {
      Entry entry = Entry.parseFromString(str);
      treeMap.put(entry.getIndex(), entry);
    }
    return treeMap;
  }

  @Override
  public void delete(Entry entry) throws IOException {
    deleteOutput.writeUTF(entry.toString());
  }
}
