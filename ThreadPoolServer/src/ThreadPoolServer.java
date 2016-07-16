import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhouyang on 16-7-16.
 */
public class ThreadPoolServer {
    public final static String HOST = "127.0.0.1";
    public static final int PORT = 8080;
    private static final int BUFFER_SZ = 1024;

    //服务器根据输入input进行响应
    //此处是示例函数,只是简单的返回 hello world
    static byte[] process(byte input[]) {
        String output = "hello world from Server";
        return output.getBytes();
    }

    private static class Handler implements Runnable {
        Socket _socket;

        public Handler(Socket socket) {
            _socket = socket;
        }

        @Override
        public void run() {
            try {
                //读取客户端发送的内容
                byte[] input = new byte[BUFFER_SZ];
                int len = _socket.getInputStream().read(input);
                String readStr = new String(input);
                String inputStr = String.copyValueOf(readStr.toCharArray(), 0, len);
                System.out.println("the client says: " + inputStr);
                //向客户端发送响应
                byte[] output = process(input);
                _socket.getOutputStream().write(output);
                _socket.getOutputStream().flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            //使用线程池进行服务器通信
            ExecutorService executor = Executors.newCachedThreadPool();
            System.out.println("Server begin to listen to port to accept!");
            while (true) {
                Socket socket = server.accept();
                executor.submit(new Handler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
