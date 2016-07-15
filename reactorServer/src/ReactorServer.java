import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhouyang on 16/7/16.
 */
public class ReactorServer implements Runnable {
    private final Selector _selector;
    private final ServerSocketChannel _serverSocketChannel;
    private static final int WORKER_POOL_SIZE = 10;
    private static ExecutorService _workerPool;

    ReactorServer(int port) throws IOException {
        _selector = Selector.open();
        _serverSocketChannel = ServerSocketChannel.open();
        _serverSocketChannel.socket().bind(new InetSocketAddress(port));
        _serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = _serverSocketChannel.register(_selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor());

    }


    public void run() {
        while (true) {
            try {
                _selector.select();
                Iterator it = _selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    Runnable r = (Runnable) key.attachment();
                    if (r != null) {
                        r.run();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static ExecutorService getWorketPool() {
        return _workerPool;
    }

    private class Acceptor implements Runnable {
        public void run() {
            try {
                SocketChannel socketChannel = _serverSocketChannel.accept();
                if (socketChannel != null) {
                    new Handler(_selector, socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String args[]) {
        _workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);
        try {
            new Thread(new ReactorServer(9090)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
