import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by zhouyang on 16/7/8.
 */
/*
    最基本的多线程服务器编程模型,对于每一个请求都建立一个线程进行响应,每个线程只处理一个访问
 */


public class Server implements Runnable {
    public final static int port = 8080;
    public final static String host = "127.0.0.1";
    public final static int MAX_INPUT = 2048;

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server begin to listen to port to accept!\n");
            while (!Thread.interrupted()) {//直至线程被中断
                //启动线程处理客户端发起请求对应的socket对象
                new Thread(new Handler(serverSocket.accept())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Handler implements Runnable {
        //客户端对应的socket
        final Socket socket;

        Handler(Socket s) {
            socket = s;
        }

        //服务器根据输入input进行响应
        //此处是示例函数,只是简单的返回 hello world
        byte[] process(byte input[]) {
            String output = "hello world from Server";
            return output.getBytes();
        }

        public void run() {
            try {
                byte[] input = new byte[MAX_INPUT];

                //读取客户端的输入
                int len = socket.getInputStream().read(input);
                String readStr = new String(input);
                String inputStr = String.copyValueOf(readStr.toCharArray(),0,len);
                System.out.println("the client says: "+inputStr);

                //向客户端发送响应内容
                byte[] output = process(input);
                socket.getOutputStream().write(output);
                socket.getOutputStream().flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        //新建Thread对象,并启动线程
        new Thread(new Server()).start();
    }
}
