import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Emetteur {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Utilisation : java Emetteur <ip_destinataire> <port> <fichier_entree>");
            return;
        }

        String ipDest = args[0];
        int port = Integer.parseInt(args[1]);
        String nomFichier = args[2];

        try {
            File fichier = new File(nomFichier);
            FileInputStream fis = new FileInputStream(fichier);
            byte[] dataFichier = new byte[(int) fichier.length()];
            fis.read(dataFichier);
            fis.close();

            // On utilise le constructeur simplifié
            Packet p = new Packet(1, dataFichier);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(p);
            byte[] data = baos.toByteArray();

            InetAddress ip = InetAddress.getByName(ipDest);
            DatagramPacket paquet = new DatagramPacket(data, data.length, ip, port);

            DatagramSocket socket = new DatagramSocket();
            socket.send(paquet);
            socket.close();

            System.out.println("📤 Fichier '" + nomFichier + "' envoyé à " + ipDest + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
