package com.company;

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
        //设置为非阻塞
        _socketChannel.configureBlocking(false);
        //注册SelecttionKey
        // Register _socketChannel with _selector listening on OP_READ events.
        // Callback: Handler, selected when the connection is established and ready for READ
        _selectionKey = _socketChannel.register(selector, SelectionKey.OP_READ);
        //附加this
        _selectionKey.attach(this);
        //let blocking select() return
        selector.wakeup();
    }

    public void run() {
        try {
            //可以读取了
            if (_selectionKey.isReadable()) {
                //读取
                read();
                //可以写入了
            } else if (_selectionKey.isWritable()) {
                //写入
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //服务器根据输入input进行响应
    //此处是示例函数,只是简单的返回 hello world
    byte[] process(byte input[]) {
        String output = "hello world from Server";
        return output.getBytes();
    }
    // synchronized关键字保证同一时刻只有一个线程执行此段代码
    synchronized void process() {
        _readBuf.flip();
        byte[] input = new byte[_readBuf.remaining()];
        _readBuf.get(input, 0, input.length);
        System.out.println("the client says: " + new String(input));
        //向客户端发送响应
        byte[] output = process(input);
        //通过包装一个已有的数组来创建ByteBuffer
        _writeBuf = ByteBuffer.wrap(output);
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
            }
            else {
                ReactorServer.getWorketPool().execute(new Runnable() {
                    public void run() {
                        process();
                    }
                });
            }
        }
        catch (IOException e) {
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
                // Set the key's interest-set back to READ operation
                _selectionKey.interestOps(SelectionKey.OP_READ);
                _selectionKey.selector().wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
