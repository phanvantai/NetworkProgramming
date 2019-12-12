import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * ClientSide
 */
public class ClientSide {

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}

class Client extends Thread {

    public static final int BLOCK_SIZE = 100000;
    public static final int BUFFER_SIZE = 1024*10;
    public static final int SERVER_PORT = 2201;
    public static final int CLIENT_PORT = 9999;
    public static final String SERVER_IP = "127.0.0.1";
    public static final int NUM_OF_CLIENT = 3;

    public ServerSocket clientSocket;
    public Socket connSocketServer;
    public ArrayList<ClientInfo> clientInfos;
    public FileInfo fileInfo;
    public ArrayList<Socket> acceptSocketClients;
    public static ArrayList<Piece> listPiece = new ArrayList<>();
    public static long partsFile = 0;
    public static boolean isDownloadDone = false;

    @Override
    public void run() {
        //clientInfos = new ArrayList<>();

        ArrayList<Socket> connSocketClients = new ArrayList<>();

        InputStream inputStream = null;
        //OutputStream outputStream = null;
        ObjectInputStream objectInputStream = null;

        try {
            // tao socket va ket noi den server
            connSocketServer = new Socket(SERVER_IP, SERVER_PORT);
            System.out.println("Da ket noi den server voi socket: " + connSocketServer);

            // Mo serversocket de cho ket noi tu cac client khac
            new Thread(new Runnable() {
            
                @Override
                public void run() {
                    try {
                        clientSocket = new ServerSocket();
                        clientSocket.setReuseAddress(true);
                        clientSocket.bind(new InetSocketAddress(CLIENT_PORT));
                    
                        // cho cac client khac ket noi den
                        int otherClient = 0;
                        while (otherClient < (NUM_OF_CLIENT - 1)) {
                            Socket socket = clientSocket.accept();
                            synchronized (listPiece) {
                                ClientWorkerThread cThread = new ClientWorkerThread(socket, listPiece, isDownloadDone);
                                cThread.start();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                }
            }).start();

            inputStream = connSocketServer.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            // nhan list client info
            clientInfos = (ArrayList<ClientInfo>) objectInputStream.readObject();
            System.out.println("Da nhan thong tin " + clientInfos.size() + " client");

            // ket noi den cac client khac
            for (ClientInfo clientInfo : clientInfos) {
                Socket cSocket = new Socket(clientInfo.getIP(), CLIENT_PORT);
                System.out.println("Da ket noi den client khac qua socket " + cSocket);
                connSocketClients.add(cSocket);
            }
            // nhan list file info
            fileInfo = (FileInfo) objectInputStream.readObject();
            
            // nhan file
            int size = 0;
            listPiece.clear();
            System.out.println("Can nhan file " + fileInfo.getFileName() + " " + fileInfo.getFileSize() + " bytes bao gom "+ fInfo.getParts()+ " part");
            partsFile = fInfo.getParts();

            while (((long) size) != partsFile) {
                // nhan cac part 
                Piece receivedPiece = (Piece) objectInputStream.readObject();

                // gui luon cho cac client khac
                for (Socket socket : connSocketClients) {
                    OutputStream ops = socket.getOutputStream();
                    ObjectOutputStream objectOutput = new ObjectOutputStream(ops);
                    objectOutput.writeObject(receivedPiece);
                }
                synchronized(listPiece) {
                    listPiece.add(receivedPiece);
                    size = listPiece.size();
                }
            }

            //Download xong
            isDownloadDone = true;

            // tao lai file tu list piece
            if (createFileDownloaded(listPiece, fileInfo)) {
                System.out.println("Da nhan duoc file " + fileInfo.getFileName());
            } else {
                System.out.println("Co loi khi tai file " + fileInfo.getFileName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean createFileDownloaded(ArrayList<Piece> list, FileInfo fileInfo) {
        // sap xep lai list piece
        Collections.sort(list, new PieceComparator());

        try {
            File file = new File(fileInfo.getFileName());
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[BLOCK_SIZE];

            for (Piece piece : list) {
                buffer = piece.getData();
                fileOutputStream.write(buffer, 0, buffer.length);
            }

            fileOutputStream.close();

            if (fileInfo.getFileSize() != file.length()) {
                return false;
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
        return true;
    }
}

class ClientWorkerThread extends Thread {
    private Socket socket;
    private ArrayList<Piece> listPiece;
    private boolean isDone = false;

    public ClientWorkerThread(Socket socket, ArrayList<Piece> listPiece, boolean done) {
        this.socket = socket;
        this.listPiece = listPiece;
        this.isDone = done;
    }

    @Override
    public void run() {
        try {
            //int size = 0;
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            while (true) {
                Piece rPiece = (Piece) objectInputStream.readObject();
                if (rPiece != null) {
                    synchronized (listPiece) {
                        listPiece.add(rPiece);
                    }
                }
                if (isDone) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * class de sap xep lai cac manh cua file
 */
class PieceComparator implements Comparator<Piece> {
    public int compare(Piece p1, Piece p2) {
        if (p1.getIndex() == p2.getIndex()) {
            return 0;
        }
        if (p1.getIndex() > p2.getIndex()) {
            return 1;
        } else {
            return -1;
        }
    }
}

/**
 * class the hien thong tin client
 */
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

/**
 * class the hien thong tin file
 */
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

/**
 * class the hien mot manh cua file
 */
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

