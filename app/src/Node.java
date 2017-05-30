import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ana Coimbra on 23/05/2017.
 */
public class Node {
    private static final int PORT = 80;
    private static final String SERVER = "localhost";

    private String ip;
    private ServerSocket server;
    private List<Neighbor> neighbors;

    private int osn = 0;
    private int hsn = 0;

    private boolean waiting;
    private boolean using;

    Node(String ip) throws RemoteException, ServerNotActiveException {
        super();
        this.ip = ip;
        neighbors = new ArrayList<>();
        new Thread(this::startClient).start();
    }

    void join(String ip) {
        try {
            Socket socket = new Socket(ip, PORT);
            PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
            out.println("Join," + this.ip);
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void print() {
        new Timer().schedule(new TimerTask() {
            int cont = 1;
            @Override
            public void run() {
                try {
                    cont ++;
                    osn++;
                    if (cont == 10) this.cancel();
                    Socket socket = new Socket(SERVER, PORT);
                    PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
                    out.println("Print," + Node.this.ip);
                    out.close();
                    socket.close();
                    release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 10, 1000);

    }

    void cancel() {
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addNeighbor(Neighbor neighbor) throws RemoteException {
        neighbors.add(neighbor);
    }

    boolean request(){
        waiting = true;
        osn = hsn + 1;

        neighbors.stream().filter(neighbor -> (!neighbor.isAccess())).forEach(neighbor -> sendRequest(osn, neighbor.getIp()));

        waiting = false;
        using = true;

        return true;
    }

    void printAllHosts() {
        neighbors.stream().forEach(neighbor -> System.out.println(neighbor.getIp()));
    }

    private void reply(int theirSeqNum, Neighbor neighbor){

        boolean ourPriority;
        hsn = Math.max(hsn, theirSeqNum);
        ourPriority = (theirSeqNum > osn) || ((theirSeqNum == osn));

        if (using || (waiting && ourPriority)){
            neighbor.setReply(true);
        }

        if (!(using || waiting) || (waiting && (!neighbor.isAccess()) && (!ourPriority))){
            if (neighbor != null) {
                neighbor.setAccess(false);
                sendReply(neighbor);
            }
        }

        if (waiting && neighbor.isAccess() && (!ourPriority)){
            neighbor.setAccess(false);
            sendReply(neighbor);
            sendRequest(osn, ip);
        }

    }

    private void response(Neighbor neighbor){
        neighbor.setAccess(true);
    }

    private void release(){
        using = false;

        neighbors.stream().filter(Neighbor::isReply).forEach(this::sendReply);
    }

    private void sendReply(Neighbor neighbor) {
        try {
            Socket socket = new Socket(neighbor.getIp(), PORT);
            PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
            out.println("Ok," + this.ip);
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendRequest(int osn, String ip) {
        try {
            Socket socket = new Socket(ip, PORT);
            PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
            out.println(osn + "," + this.ip);
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startClient() {
        try {
            // Establish the listen socket.
            server = new ServerSocket(PORT);

            while( true ) {
                // Listen for a TCP connection request.
                if (server.isClosed()) break;

                Socket connection = server.accept();

                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                // Get the request line of the HTTP request message.
                String requestLine = br.readLine();

                String[] request = requestLine.split(",");

                if (request[0].startsWith("Ok")) {
                    response(getNeighborFromIp(request[1]));
                } else if (request[0].startsWith("Join")) {
                    Neighbor neighbor = new Neighbor(request[1]);
                    addNeighbor(neighbor);
                } else if (request[0].startsWith("Print")) {
                    System.out.println(request[1]);
                } else {
                    reply(Integer.parseInt(request[0]), getNeighborFromIp(request[1]));
                }

                br.close();
                connection.close();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Neighbor getNeighborFromIp(String ip) {
        Neighbor neighbor = null;
        for (Neighbor n : neighbors) {
            if (n.getIp().equals(ip)) {
                neighbor = n;
                break;
            }
        }

        return neighbor;
    }
}
