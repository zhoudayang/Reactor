import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by zhouyang on 16/7/9.
 */
public class Client implements Runnable {
    //向服务器发送的数字
    private int code;

    //构造函数
    Client(int code) {
        this.code = code;
    }

    public void run() {
        try {
            //新建客户端对应的socket
            Socket client = new Socket(Server.host, Server.port);

            //向服务器发送内容
            OutputStreamWriter writer = new OutputStreamWriter(client.getOutputStream());
            InputStreamReader reader = new InputStreamReader(client.getInputStream());
            writer.write("hello server! I am a client! I send "+code+" to you!");
            writer.flush();

            //接收服务器响应的内容
            char[] response = new char[Server.MAX_INPUT];
            int len = reader.read(response);
            String ret = new String(response, 0, len);

            //输出
            System.out.println("Server's response is: " + ret);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //main 函数
    public static void main(String args[]) {
        //新建4个线程模拟4个客户端同时向服务器发起访问

        //Thread 对象数组
        Thread threads[] = {
                new Thread(new Client(0)),
                new Thread(new Client(1)),
                new Thread(new Client(2)),
                new Thread(new Client(3)),
        };
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

    }
}
