import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Ana Coimbra on 29/05/2017.
 */
public class Client {
    public static void main(String[] args) throws IOException {
        String ip;
        Node node = null;
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
            node = new Node(ip);
            checkHosts(node, ip.substring(0, ip.lastIndexOf(".") - 1));
            node.printAllHosts();
        } catch (UnknownHostException | ServerNotActiveException | RemoteException e) {
            e.printStackTrace();
        }

        while (true) {
            Scanner keyboard = new Scanner(System.in);
            System.out.println("Aperte enter para sortear um número, 'E' para sair");
            String text = keyboard.nextLine();

            if (text.startsWith("E") || text.startsWith("e")) {
                assert node != null;
                node.cancel();
                break;
            }

            if(Objects.equals(text, "")) {
                Random random = new Random();
                double num = random.nextDouble();
                System.out.println("Numero sorteado: " + num);
                if (num >= 0.5 && node != null) {
                    if (node.request()) node.print();
                }
            }
        }
    }

    private static void checkHosts(Node node, String subnet){
        int timeout=1000;
        for (int i=1;i<255;i++){
            String host=subnet + "." + i;
            try {
                if (InetAddress.getByName(host).isReachable(timeout)){
                    node.join(host);
                }
            } catch (IOException ignored) {
            }
        }
    }
}
