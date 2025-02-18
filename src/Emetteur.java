import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe Transmitter (Émetteur)
 * Cette classe envoie un fichier via des paquets UDP à un récepteur.
 */
public class Emetteur {
    public static void main(String[] args) {
        try {
            // Vérification des arguments
            if (args.length != 3) {
                System.err.println(
                        "Utilisation : java Transmitter <adresse_ip_recepteur> <port_recepteur> <chemin_fichier>");
                return;
            }

            // Lecture des arguments
            String recepteurIP = args[0]; // Adresse IP du récepteur
            int recepteurPort = Integer.parseInt(args[1]); // Port du récepteur
            String filePath = args[2]; // Chemin du fichier à envoyer

            // Lecture du fichier en tableau d'octets
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));

            // Création du socket UDP
            DatagramSocket socket = new DatagramSocket();
            InetAddress recepteurAddress = InetAddress.getByName(recepteurIP);

            int sequenceNumber = 0; // Numéro de séquence initial

            // Simulation d'une erreur pour envoyer un paquet RST (par exemple)
            boolean sendReset = false; // Remplacez par la condition qui vous semble appropriée

            if (sendReset) {
                // Si on veut envoyer un paquet RST pour interrompre la transmission
                Packet rstPacket = new Packet(sequenceNumber++, new byte[0], false, false, false, true); // Flag RST
                                                                                                         // activé
                byte[] serializedRst = Packet.serialize(rstPacket);
                DatagramPacket rstDatagram = new DatagramPacket(serializedRst, serializedRst.length, recepteurAddress,
                        recepteurPort);
                socket.send(rstDatagram);
                System.out.println("Paquet RST envoyé pour réinitialiser la connexion.");
                socket.close(); // Fermer immédiatement après l'envoi du RST
                return; // Arrêter l'exécution si RST est envoyé
            }

            // Division des données en fragments et envoi
            for (int i = 0; i < fileData.length; i += 512) {
                // Taille du fragment (512 octets ou moins pour le dernier fragment)
                int chunkSize = Math.min(512, fileData.length - i);
                byte[] chunk = new byte[chunkSize];

                // Copie des données dans le fragment
                System.arraycopy(fileData, i, chunk, 0, chunkSize);

                // Création d'un paquet avec le numéro de séquence et le fragment
                Packet packet = new Packet(sequenceNumber++, chunk, false, false, false, false);
                byte[] serializedPacket = Packet.serialize(packet);

                // Création et envoi du DatagramPacket
                DatagramPacket datagram = new DatagramPacket(serializedPacket, serializedPacket.length,
                        recepteurAddress, recepteurPort);
                socket.send(datagram);

                System.out.println("Paquet envoyé avec le numéro de séquence : " + (sequenceNumber - 1));
            }

            // Envoi du paquet FIN pour signaler la fin de la transmission
            Packet finPacket = new Packet(sequenceNumber, new byte[0], false, false, true, false);
            byte[] serializedFin = Packet.serialize(finPacket);
            DatagramPacket finDatagram = new DatagramPacket(serializedFin, serializedFin.length, recepteurAddress,
                    recepteurPort);
            socket.send(finDatagram);

            System.out.println("Transmission terminée.");
            socket.close(); // Fermeture du socket
        } catch (Exception e) {
            e.printStackTrace(); // Gestion des erreurs
        }
    }
    /*
     * Points à Améliorer :
     * 
     * Protocole incomplet :
     * 
     * 
     * Manque le 3-way handshake initial (SYN)
     * Pas de gestion des ACKs
     * Pas de fenêtre glissante de 256
     * Pas de retransmission après 3 ACKs identiques
     * 
     * 
     * Sécurité/Robustesse :
     * 
     * 
     * Pas de timeout
     * Pas de gestion des erreurs réseau
     * Pas de vérification de l'endianness
     */

    // Ajouter handshake
private boolean performHandshake(DatagramSocket socket, InetAddress recepteur, int port)

// Ajouter gestion ACK
private void waitForAck(DatagramSocket socket)

// Ajouter fenêtre glissante
    private List<Packet> windowBuffer = new ArrayList<>();
}
