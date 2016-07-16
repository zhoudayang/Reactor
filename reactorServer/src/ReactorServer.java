package com.company;

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
    //线程池大小
    private static final int WORKER_POOL_SIZE = 10;
    private static ExecutorService _workerPool;

    //初始化函数
    ReactorServer(int port) throws IOException {
        //创建selector
        _selector = Selector.open();
        //创建ServerSocketChannel
        _serverSocketChannel = ServerSocketChannel.open();
        //绑定端口
        _serverSocketChannel.socket().bind(new InetSocketAddress(port));
        //设置为非阻塞
        _serverSocketChannel.configureBlocking(false);
        //注册SelecttionKey
        SelectionKey selectionKey = _serverSocketChannel.register(_selector, SelectionKey.OP_ACCEPT);
        //附加Acceptor对象
        selectionKey.attach(new Acceptor());
    }


    public void run() {
        //事件循环
        while (true) {
            try {
                _selector.select();
                Iterator it = _selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    it.remove();
                    //初始化时附加的Acceptor对象
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

    //返回线程池
    public static ExecutorService getWorketPool() {
        return _workerPool;
    }

    //Acceptor: if connection is established, assign a handler to it
    private class Acceptor implements Runnable {
        public void run() {
            try {
                //获取SocketChannel 对象
                SocketChannel socketChannel = _serverSocketChannel.accept();
                if (socketChannel != null) {
                    //创建Handler进行后续处理
                    new Handler(_selector, socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String args[]) {
        //新建固定大小的线程池
        _workerPool = Executors.newFixedThreadPool(WORKER_POOL_SIZE);
        try {
            //在8080端口上启动服务器
            new Thread(new ReactorServer(8080)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
