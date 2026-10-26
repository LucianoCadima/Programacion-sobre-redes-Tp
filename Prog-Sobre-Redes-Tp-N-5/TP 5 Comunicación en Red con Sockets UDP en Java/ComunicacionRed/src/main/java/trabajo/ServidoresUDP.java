package trabajo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServidoresUDP {

    public static void main(String[] args) {

        try {
            DatagramSocket socket = new DatagramSocket(9876);

            System.out.println("Servidor UDP iniciado.");
            System.out.println("Esperando mensajes...");

            while (true) {

                byte[] datos = new byte[1024];

                DatagramPacket paquete = new DatagramPacket(
                        datos,
                        datos.length
                );

                socket.receive(paquete);

                String mensaje = new String(
                        paquete.getData(),
                        paquete.getOffset(),
                        paquete.getLength()
                );

                System.out.println(
                        "Mensaje recibido: " + mensaje
                );

                System.out.println(
                        "IP de origen: " +
                        paquete.getAddress().getHostAddress()
                );

                System.out.println(
                        "Puerto de origen: " +
                        paquete.getPort()
                );

                if (mensaje.equals("FIN")) {
                    System.out.println("Cliente finalizado.");
                    break;
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
