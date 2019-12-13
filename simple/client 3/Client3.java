import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.*;
import java.util.*;

/**
 * Client2
 */
public class Client3 extends Thread {

    public static void main(String[] args) {
        Client3 client = new Client3();
        client.start();
    }

    @Override
    public void run() {
        Socket socketC1;
        ServerSocket serverSocket;

        DataInputStream inputStream;

        try {
            Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
            
            // mo cong cho client 1 ket noi
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Constants.CLIENT3_PORT));

            socketC1 = serverSocket.accept();
            inputStream = new DataInputStream(socketC1.getInputStream());

            // nhan thong tin file
            String fileName = inputStream.readUTF();
            long fileSize = inputStream.readLong();
            System.out.println("Dang download file " + fileName);

            FileOutputStream fStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int read;
            long received = 0;
            while (received != fileSize) {
                read = inputStream.read(buffer);
                received += read;
                fStream.write(buffer, 0, read);
            }
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            System.out.println("Download file " + fileName +
                 " thanh cong luc " + dateFormat.format(date));

            fStream.close();
            socket.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Constants {
    public static final int CLIENT_NUM = 3;

    public static final String SERVER_IP = "192.168.1.0";
    public static final int SERVER_PORT = 9999;

    public static final String CLIENT2_IP = "10.10.2.0";
    public static final int CLIENT2_PORT = 22222;

    public static final String CLIENT3_IP = "10.10.3.0";
    public static final int CLIENT3_PORT = 33333;

    public static final int BUFFER_SIZE = 1024*100;

    public static final String DATE_FORMAT = "hh:mm:ss";

}
