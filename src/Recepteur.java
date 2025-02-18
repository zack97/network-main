import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Classe Receiver (Récepteur)
 * Cette classe reçoit des paquets UDP envoyés par l'émetteur et reconstruit le
 * fichier à partir de ces paquets.
 */
public class Recepteur {
    public static void main(String[] args) {
        try {
            // Vérification des arguments
            if (args.length != 2) {
                System.err.println("Utilisation : java Receiver <port> <fichier_sortie>");
                return;
            }

            // Lecture des arguments
            int port = Integer.parseInt(args[0]); // Port sur lequel écouter
            String outputFile = args[1]; // Nom du fichier dans lequel les données seront sauvegardées

            // Création du socket UDP pour écouter sur le port spécifié
            DatagramSocket socket = new DatagramSocket(port);

            // Création d'un flux de fichier pour sauvegarder les données reçues
            FileOutputStream fos = new FileOutputStream(outputFile);

            boolean isRunning = true; // Indique si le récepteur doit continuer à fonctionner
            while (isRunning) {
                // Tampon pour stocker les données reçues
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Réception d'un paquet
                socket.receive(packet);

                // Désérialisation des données pour reconstruire le paquet
                Packet receivedPacket = Packet.deserialize(packet.getData());

                // Vérification si le paquet est un paquet RST (réinitialisation)
                if (receivedPacket.isRstFlag()) {
                    System.err.println("Paquet RST reçu. Connexion réinitialisée.");
                    isRunning = false; // Arrêter immédiatement la réception
                    break; // Quitter la boucle de réception
                }

                // Vérification si le paquet est un paquet FIN (fin de transmission)
                if (receivedPacket.isFinFlag()) {
                    System.out.println("Paquet FIN reçu. Fermeture de la connexion.");
                    isRunning = false; // Arrête la boucle de réception
                } else {
                    // Écriture des données du paquet dans le fichier de sortie
                    fos.write(receivedPacket.getData());
                    System.out.println("Paquet reçu avec numéro de séquence : " + receivedPacket.getSequenceNumber());
                }
            }

            // Fermeture des ressources
            fos.close(); // Ferme le flux du fichier
            socket.close(); // Ferme le socket
        } catch (Exception e) {
            e.printStackTrace(); // Affiche les erreurs éventuelles
        }
    }

    /*
     * Points à Améliorer :
     * 
     * Protocole incomplet :
     * 
     * 
     * Pas de gestion du 3-way handshake
     * Pas d'envoi d'ACK pour les paquets reçus
     * Pas de vérification des numéros de séquence
     * Pas de gestion de l'ordre des paquets
     */
    // Gestion des ACKs
private void sendAck(int sequenceNumber, DatagramSocket socket, InetAddress sender, int port)

// Buffer pour les paquets hors ordre
private SortedMap<Integer, byte[]> packetBuffer = new TreeMap<>();

    // Vérification de l'ordre
private boolean isNextExpectedPacket(int sequenceNumber)
}
