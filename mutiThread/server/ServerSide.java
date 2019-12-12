
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;

/**
 * ServerSide
 */
public class ServerSide {

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}

class Server extends Thread {

	public static final int BLOCK_SIZE = 100000;
    public static final int BUFFER_SIZE = 1024*10;
    public static final int SERVER_PORT = 2201;
    public static final int CLIENT_PORT = 9999;
    public static final String SERVER_IP = "127.0.0.1";
	public static final int NUM_OF_CLIENT = 3;
	
    public ServerSocket serverSocket;
    public ArrayList<ClientInfo> clientInfos;
    public FileInfo fileInfo;

    @Override
    public void run() {
        clientInfos = new ArrayList<>();
        fileInfos = new ArrayList<>();

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SERVER_PORT));

            System.out.println("Da khoi tao server socket " + serverSocket);
            System.out.println("Dang lang nghe ket noi o cong " + SERVER_PORT);

            int numberClient = 0;
			// cho ket noi du 3 client
			while (numberClient < NUM_OF_CLIENT) {
				Socket socket = serverSocket.accept();
				System.out.println("Da ket noi voi client qua socket " + socket);
				
				// them client vao list client info
				ClientInfo client = new ClientInfo(socket, numberClient);
				
				clientInfos.add(client);
				numberClient++;
			}
			
			// khi du 3 client ket noi thi nhap ten file
			
			Scanner scanner = new Scanner(System.in);
			System.out.println("Nhap ten file can gui cho client:");
            String fileName = scanner.nextLine();
            File file = new File(fileName);
			if (file.exists()) {
                fileInfo = new FileInfo(fileName);
			}
            if (scanner != null) {
				scanner.close();
			}
			
			// bat dau dat bien de tinh thoi gian download
			//
			
			// tao cac thread de gui file cho client
			for (ClientInfo client : clientInfos) {
				WorkerThread handler = new WorkerThread(client.getSocket(), clientInfos, fileInfo);
				handler.start();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WorkerThread extends Thread {
    
    private Socket socket;
	private ArrayList<ClientInfo> clientInfos;
	private FileInfo fileInfo;
	private int clientId;

    public WorkerThread(Socket socket, ArrayList<ClientInfo> clients, FileInfo file) {
        this.socket = socket;
		this.clientInfos = clients;
		this.fileInfo = file;
		
		for (ClientInfo client : clientInfos) {
			if (this.socket.equals(client.getSocket())) {
				this.clientId = client.getID();
				clientInfos.remove(client);
			}

		}
		System.out.println(this.getId() + " Co socket cua client co id " + this.clientId);
    }

    @Override
    public void run() {
        System.out.println(this.getId() + " Dang xu ly: " + socket);
		
		OutputStream outputStream = null;
		// objectOutputStream de gui 2 arraylist cho client 
		ObjectOutputStream objectOutputStream = null;
		
		try {
			outputStream = socket.getOutputStream();
			objectOutputStream = new ObjectOutputStream(outputStream);
			
			//gui thong tin cac client
			objectOutputStream.writeObject(clientInfos);

			// gui thong tin file
			objectOutputStream.writeObject(fileInfo);
			
			// gui file
			// mo file tu fileinfo va xu ly du lieu
			File file = new File(files.getFileName());
			System.out.println(this.getId() + " Kich thuoc cua file dang xu ly: " + file.length());

			// gui cac part doi voi moi client
			byte[] newBlock = new byte[100000];
			FileInputStream fileInputStream = new FileInputStream(file);
				
            int send = 0, index = 0, totalSended = 0;
			while ((send = fileInputStream.read(newBlock)) != -1) {
                if ((index == 0 && clientId == 0) || (index == 1 && clientId == 1) || (index == 2 && clientId == 2)
                    || (index >= clientId && ((index - clientId)%3 == 0))) {
                    totalSended += send;
                    Piece newPiece = new Piece(index, newBlock);
                    objectOutputStream.writeObject(newPiece);
                    System.out.println(this.getId() +" Client "+ clientId + " dang lay block "+ index);
                    System.out.println(this.getId() +" Da gui " + totalSended + " cho client "+ clientId);
				}
				index++;
            }
            System.out.println(this.getId() + " File " + files.getFileName() + " co " + index + " block du lieu");
                
            fileInputStream.close();
            objectOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    // public void sendFileToClient(FileInfo fileInfo) throws IOException {
    //     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
    //     File file = new File(fileInfo.getFileName());
    //     FileInputStream fileInputStream = new FileInputStream(file);

    //     System.out.println("size " + file.length());
    //     byte[] buffer = new byte[Constants.BUFFER_SIZE];

    //     int send = 0, index = 0;
    //     long totalSend = 0;
    //     while ((send = fileInputStream.read(buffer)) != -1) {
	// 		index++;
    //         totalSend += send;
    //         System.out.println("Da gui " + totalSend + " bytes");
    //         dataOutputStream.write(buffer, 0, send);
    //     }

    //     fileInputStream.close();
    //     //dataOutputStream.close();
    // }
}

class ClientInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Socket socket;
    private String IP;
    private int ID;

    public ClientInfo(Socket socket, int id) {
        this.socket = socket;
        this.IP = socket.getInetAddress().getHostAddress();
        this.ID = id;
    }

    /**
     * @param iD the iD to set
     */
    public void setID(int iD) {
        ID = iD;
    }

    /**
     * @return the iD
     */
    public int getID() {
        return ID;
    }

    /**
     * @param iP the iP to set
     */
    public void setIP(String iP) {
        IP = iP;
    }

    /**
     * @return the iP
     */
    public String getIP() {
        return IP;
    }

    /**
     * @param socket the socket to set
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * @return the socket
     */
    public Socket getSocket() {
        return socket;
    }
}

class FileInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String fileName;
    private long fileSize;
    private long parts;

    public FileInfo(String fileName) {
        this.fileName = fileName;
        File file = new File(fileName);
        if (file.exists()) {
            this.fileSize = file.length();
            if (fileSize % 100000 == 0) {
                this.parts = fileSize/100000;
            } else {
                this.parts = fileSize/100000 + 1;
            }
        }
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the fileSize
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @param parts the parts to set
     */
    public void setParts(long parts) {
        this.parts = parts;
    }

    /**
     * @return the parts
     */
    public long getParts() {
        return parts;
    }
}

class Piece implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int index;
	private byte[] data = new byte[100000];
	
	public Piece(int index, byte[] data) {
		this.index = index;
		this.data = data;
    }
    
    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
}

