import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Recepteur {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Utilisation : java Recepteur <port> <fichier_sortie>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String nomFichier = args[1];

        try {
            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("📥 Récepteur en écoute sur le port " + port + "...");

            byte[] buffer = new byte[65507];
            DatagramPacket paquet = new DatagramPacket(buffer, buffer.length);
            socket.receive(paquet);

            ByteArrayInputStream bais = new ByteArrayInputStream(paquet.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Packet p = (Packet) ois.readObject();

            FileOutputStream fos = new FileOutputStream(nomFichier);
            fos.write(p.data);
            fos.close();

            System.out.println("✅ Fichier reçu et sauvegardé sous : " + nomFichier);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
