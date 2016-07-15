import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by zhouyang on 16/7/16.
 */
public class Handler implements Runnable {
    private final SocketChannel _socketChannel;
    private final SelectionKey _selectionKey;

    private static final int READ_BUF_SIZE = 1024;
    private static final int WRITE_BUF_SIZE = 1024;
    private ByteBuffer _readBuf = ByteBuffer.allocate(READ_BUF_SIZE);
    private ByteBuffer _writeBuf = ByteBuffer.allocate(WRITE_BUF_SIZE);

    public Handler(Selector selector, SocketChannel socketChannel) throws IOException {
        _socketChannel = socketChannel;
        _socketChannel.configureBlocking(false);
        _selectionKey = _socketChannel.register(selector, SelectionKey.OP_READ);
        _selectionKey.attach(this);
        selector.wakeup();
    }

    public void run() {
        try {
            if (_selectionKey.isReadable()) {
                read();
            } else if (_selectionKey.isWritable()) {
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void process() {
        _readBuf.flip();
        byte[] bytes = new byte[_readBuf.remaining()];
        _readBuf.get(bytes, 0, bytes.length);
        System.out.println("process(): " + new String(bytes, Charset.forName("ISO-8859-1")));
        _writeBuf = ByteBuffer.wrap(bytes);
        _selectionKey.interestOps(SelectionKey.OP_WRITE);
        _selectionKey.selector().wakeup();
    }

    synchronized void read() throws IOException {
        try {
            int numBytes = _socketChannel.read(_readBuf);
            System.out.println("read():# bytes read into '_readBuf' buffer =" + numBytes);
            if (numBytes == -1) {
                _selectionKey.cancel();
                _socketChannel.close();
                System.out.println("read():client connection might have been dropped!");
            } else {
                ReactorServer.getWorketPool().execute(new Runnable() {
                    public void run() {
                        process();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void write() throws IOException {
        int numBytes = 0;
        try {
            numBytes = _socketChannel.write(_writeBuf);
            System.out.println("write(): #bytes read from '_writeBuf' buffer = " + numBytes);
            if (numBytes > 0) {
                _readBuf.clear();
                _writeBuf.clear();
                _selectionKey.interestOps(SelectionKey.OP_READ);
                _selectionKey.selector().wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
