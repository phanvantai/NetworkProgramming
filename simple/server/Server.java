import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.text.*;

/**
 * Server
 */
public class Server extends Thread {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    @Override
    public void run() {
        ArrayList<Socket> clientSock;
        DataOutputStream outputStream;
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(Constants.SERVER_PORT));
            System.out.println("Dang lang nghe o cong " + Constants.SERVER_PORT);

            clientSock = new ArrayList<>();
            int clientNum = 0;
            while (clientNum < Constants.CLIENT_NUM) {
                Socket socket = serverSocket.accept();
                System.out.println("Da ket noi voi " + socket);
                clientSock.add(socket);
                clientNum++;
            }

            // Nhap ten file gui cho client
            File file;
            String fileName = "";
            long fileSize = 0;
            boolean fileNotFound = true;
            Scanner scanner = new Scanner(System.in);
            while (fileNotFound) {
                System.out.println("Nhap ten file can gui cho client:");
                fileName = scanner.nextLine();
                file = new File(fileName);
                if (file.exists()) {
                    fileSize = file.length();
                    fileNotFound = false;
                } else {
                    System.out.println("File khong ton tai!");
                }
            }
            scanner.close();

            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            System.out.println("Bat dau gui file " + fileName +
                 " luc " + dateFormat.format(date));

            FileInputStream fStream = new FileInputStream(fileName);
            // gui ten file va kich thuoc cho cac client
            outputStream = new DataOutputStream(clientSock.get(2).getOutputStream());
            outputStream.writeUTF(fileName);
            outputStream.writeLong(fileSize);
            
            // clientSock.remove(0);
            // for (Socket socket : clientSock) {
            //     DataOutputStream oStream = new DataOutputStream(socket.getOutputStream());
            //     oStream.writeUTF(fileName);
            //     oStream.writeLong(fileSize);
            //     oStream.close();
            // }

            // gui file cho client 1
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int send;
            while ((send = fStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, send);
            }
            
            fStream.close();
            serverSocket.close();
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}

class Constants {
    public static final int CLIENT_NUM = 3;

    public static final String SERVER_IP = "192.168.21.0";
    public static final int SERVER_PORT = 9999;

    public static final String CLIENT2_IP = "10.10.2.0";
    public static final int CLIENT2_PORT = 22222;

    public static final String CLIENT3_IP = "10.10.3.0";
    public static final int CLIENT3_PORT = 33333;

    public static final int BUFFER_SIZE = 1024*100;

    public static final String DATE_FORMAT = "hh:mm:ss";

}
