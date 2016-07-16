import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by zhouyang on 16-7-16.
 */
public class simpleClient implements Runnable {
    //向服务器发送的数字
    private int code;
    private static final int BUFFER_SZ = 1024;

    //构造函数
    simpleClient(int code) {
        this.code = code;
    }

    @Override
    public void run() {
        try {
            //新建socket,指向服务器的ip地址和端口号
            Socket client = new Socket(ThreadPoolServer.HOST, ThreadPoolServer.PORT);

            //向服务器发送内容
            OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
            InputStreamReader reader = new InputStreamReader(client.getInputStream());
            writer.write("hello server! I am a client! I send " + code + " to you!");
            writer.flush();

            //接收服务器响应的内容
            char[] response = new char[BUFFER_SZ];
            int len = reader.read(response);
            String ret = new String(response, 0, len);

            //输出
            System.out.println("Server's response is : " + ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //main函数
    public static void main(String args[]) {
        //新建4个线程模拟4个客户端同时向服务器发起访问

        //Thread 对象数组
        Thread clientThreads[] = {
                new Thread(new simpleClient(0)),
                new Thread(new simpleClient(1)),
                new Thread(new simpleClient(2)),
                new Thread(new simpleClient(3))
        };

        for (int i = 0; i < clientThreads.length; i++)
            clientThreads[i].start();

    }

}
