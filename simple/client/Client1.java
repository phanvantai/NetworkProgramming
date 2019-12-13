import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client1 extends Thread {

    public static void main(String[] args) {
        Client1 client = new Client1();
        client.start();
    }

    @Override
    public void run() {
        DataInputStream inputStream;
        DataOutputStream outputStreamC2, outputStreamC3;

        try {
            // ket noi den server
            Socket socket = new Socket(Constants.SERVER_IP, Constants.SERVER_PORT);
            System.out.println("Da ket noi den server " + socket.getInetAddress().getHostAddress());

            inputStream = new DataInputStream(socket.getInputStream());

            // nhan ten file, kich thuoc file
            String fileName = inputStream.readUTF();
            long fileSize = inputStream.readLong();
            System.out.println("Dang download file " + fileName);

            // Ket noi den Client 2 va 3
            Socket socketC2 = new Socket(Constants.CLIENT2_IP, Constants.CLIENT2_PORT);
            Socket socketC3 = new Socket(Constants.CLIENT3_IP, Constants.CLIENT3_PORT);

            outputStreamC2 = new DataOutputStream(socketC2.getOutputStream());
            outputStreamC2.writeUTF(fileName);
            outputStreamC2.writeLong(fileSize);
            outputStreamC3 = new DataOutputStream(socketC3.getOutputStream());
            outputStreamC3.writeUTF(fileName);
            outputStreamC3.writeLong(fileSize);

            // nhan file tu server
            FileOutputStream fStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int read;
            long received = 0;
            while (received != fileSize) {
                read = inputStream.read(buffer);
                received += read;
                fStream.write(buffer, 0, read);

                // gui cho client 2, 3
                outputStreamC2.write(buffer, 0, read);
                outputStreamC3.write(buffer, 0, read);
            }
            Date date = new Date();
            DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            System.out.println("Download file " + fileName +
                 " thanh cong luc " + dateFormat.format(date));

            fStream.close();
            socket.close();
            socketC2.close();
            socketC3.close();
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
    }
}

class Constants {
    public static final String SERVER_IP = "192.168.21.0";
    public static final int SERVER_PORT = 9999;

    public static final String CLIENT2_IP = "10.10.2.0";
    public static final int CLIENT2_PORT = 22222;

    public static final String CLIENT3_IP = "10.10.3.0";
    public static final int CLIENT3_PORT = 33333;

    public static final int BUFFER_SIZE = 1024*100;

    public static final String DATE_FORMAT = "hh:mm:ss";

}
