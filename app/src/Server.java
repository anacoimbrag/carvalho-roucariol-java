import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Ana Coimbra on 29/05/2017.
 */
public class Server {
    private static final int PORT = 80;

    public static void main(String[] args) {
        try {
            // Establish the listen socket.
            ServerSocket server = new ServerSocket(PORT);

            while( true ) {
                // Listen for a TCP connection request.
                if (server.isClosed()) break;

                Socket connection = server.accept();

                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                // Get the request line of the HTTP request message.
                String requestLine = br.readLine();

                String[] request = requestLine.split(",");

                if (request[0].startsWith("Print")) {
                    System.out.println(request[1]);
                }

                br.close();
                connection.close();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
