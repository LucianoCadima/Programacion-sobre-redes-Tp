package trabajo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class ClienteUDP {

    public static void main(String[] args) {

        try {
            DatagramSocket socket = new DatagramSocket();

            Scanner teclado = new Scanner(System.in);

            InetAddress direccion = InetAddress.getByName("localhost");

            while (true) {

                System.out.print("Ingrese un mensaje: ");
                String mensaje = teclado.nextLine();

                byte[] datos = mensaje.getBytes();

                DatagramPacket paquete = new DatagramPacket(
                        datos,
                        datos.length,
                        direccion,
                        9876
                );

                socket.send(paquete);

                if (mensaje.equals("FIN")) {
                    break;
                }
            }

            socket.close();
            teclado.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
