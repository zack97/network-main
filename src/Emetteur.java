import java.io.*;
import java.net.*;
import java.nio.file.Files;

public class Emetteur {

    private static final int MAX_TENTATIVES = 3;    // Nombre max de tentatives pour le handshake SYN
    private static final int TIMEOUT_MS = 3000;     // Timeout en ms pour attendre la réponse
    private static final int MAX_PAYLOAD_SIZE = 512; // Taille max d'un segment de données

    public static void main(String[] args) throws IOException {
        // Vérification des arguments : adresse IP, port et fichier à envoyer
        if (args.length != 3) {
            System.out.println("Usage : java Emetteur <adresse_ip> <port> <fichier_texte>");
            System.exit(1);
        }

        InetAddress adresse = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        String nomFichier = args[2];

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT_MS); // Timeout sur receive pour ne pas bloquer indéfiniment

        // 1) Handshake SYN : envoyer paquet SYN et attendre SYN+ACK, avec plusieurs tentatives
        Packet syn = new Packet(true, false, false, false, 0, new byte[0]);
        boolean synAckRecu = false;
        int tentative = 0;

        while (tentative < MAX_TENTATIVES && !synAckRecu) {
            tentative++;
            byte[] synBytes = syn.toBytes();
            DatagramPacket synPacket = new DatagramPacket(synBytes, synBytes.length, adresse, port);
            socket.send(synPacket);
            System.out.println("Tentative " + tentative + ": Envoyé SYN");

            try {
                // Attente d'une réponse
                byte[] buffer = new byte[1024];
                DatagramPacket reponsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(reponsePacket);

                // Extraction des données reçues
                byte[] dataRecu = new byte[reponsePacket.getLength()];
                System.arraycopy(buffer, 0, dataRecu, 0, reponsePacket.getLength());

                Packet reponse = Packet.fromBytes(dataRecu);

                // Vérification que c'est bien un SYN+ACK attendu
                if (reponse.isSynFlag() && reponse.isAckFlag()) {
                    System.out.println("Réception SYN+ACK, handshake terminé.");
                    synAckRecu = true;
                } else {
                    System.out.println("Réponse reçue mais pas SYN+ACK, retry...");
                }

            } catch (SocketTimeoutException e) {
                // Timeout => réessayer
                System.out.println("Timeout, pas de réponse au SYN, nouvelle tentative...");
            }
        }

        if (!synAckRecu) {
            // Échec après plusieurs tentatives : abandon
            System.out.println("Échec du handshake après " + MAX_TENTATIVES + " tentatives.");
            socket.close();
            System.exit(1);
        }

        // 2) Lecture du fichier en bytes pour préparation de l'envoi
        byte[] fichierBytes = Files.readAllBytes(new File(nomFichier).toPath());

        // 3) Envoi segmenté des données avec numéro de séquence
        int seq = 1;  // Initialisation du numéro de séquence
        int offset = 0; // Position dans le tableau de bytes

        while (offset < fichierBytes.length) {
            // Taille du segment à envoyer (max 512 bytes)
            int tailleSegment = Math.min(MAX_PAYLOAD_SIZE, fichierBytes.length - offset);

            // Copie du segment
            byte[] segment = new byte[tailleSegment];
            System.arraycopy(fichierBytes, offset, segment, 0, tailleSegment);

            // Création du paquet de données
            Packet dataPacket = new Packet(false, false, false, false, seq, segment);
            byte[] dataBytes = dataPacket.toBytes();

            // Envoi du paquet UDP
            DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, adresse, port);
            socket.send(sendPacket);
            System.out.println("Envoyé paquet seq=" + seq + ", taille=" + tailleSegment);

            // Pour simplifier, pas d'attente d'ACK ici (à implémenter pour fiabilité)
            seq++;
            offset += tailleSegment;
        }

        // 4) Envoi d'un paquet FIN pour signaler la fin de transmission
        Packet fin = new Packet(false, false, true, false, seq, new byte[0]);
        byte[] finBytes = fin.toBytes();
        DatagramPacket finPacket = new DatagramPacket(finBytes, finBytes.length, adresse, port);
        socket.send(finPacket);
        System.out.println("Envoyé FIN");

        socket.close(); // Fermeture du socket
    }
}
