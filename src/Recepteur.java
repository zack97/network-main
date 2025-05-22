import java.io.*;
import java.net.*;

public class Recepteur {

    public static void main(String[] args) throws IOException {
        // Vérification des arguments d'entrée : port et fichier de sortie
        if (args.length != 2) {
            System.out.println("Usage : java Recepteur <port> <fichier_sortie>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        String nomFichierSortie = args[1];

        // Création du socket UDP sur le port donné
        DatagramSocket socket = new DatagramSocket(port);
        System.out.println("Récepteur démarré sur le port " + port);

        // Ouverture du fichier en mode append pour écrire les données reçues
        try (FileOutputStream fos = new FileOutputStream(nomFichierSortie, true)) {

            boolean receptionTerminee = false; // Flag pour arrêter la boucle à la fin de la transmission
            boolean handshakeFini = false;     // Flag indiquant la fin du handshake SYN-SYN+ACK

            // Boucle principale d'écoute et traitement des paquets reçus
            while (!receptionTerminee) {
                byte[] buffer = new byte[1024]; // Buffer temporaire pour recevoir les données UDP
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Blocant : attente de réception d'un paquet UDP
                socket.receive(packet);

                // Extraction précise des données reçues dans un tableau de taille adaptée
                byte[] dataRecu = new byte[packet.getLength()];
                System.arraycopy(buffer, 0, dataRecu, 0, packet.getLength());

                Packet p = null;
                try {
                    // Désérialisation des données reçues en objet Packet
                    p = Packet.fromBytes(dataRecu);
                } catch (IllegalArgumentException e) {
                    // En cas d'erreur dans le format du paquet, on l'ignore et attend le prochain
                    System.out.println("Erreur désérialisation paquet: " + e.getMessage());
                    continue;
                }

                // Gestion du handshake initial (connexion)
                if (!handshakeFini) {
                    // Si paquet SYN reçu sans ACK, répondre avec SYN+ACK
                    if (p.isSynFlag() && !p.isAckFlag()) {
                        System.out.println("Réception SYN, envoi SYN+ACK");

                        // Création du paquet SYN+ACK en réponse
                        Packet synAck = new Packet(true, true, false, false, 0, new byte[0]);
                        byte[] synAckBytes = synAck.toBytes();

                        // Envoi du paquet SYN+ACK à l'adresse et port de l'émetteur
                        DatagramPacket synAckPacket = new DatagramPacket(synAckBytes, synAckBytes.length, packet.getAddress(), packet.getPort());
                        socket.send(synAckPacket);

                        handshakeFini = true; // Le handshake est considéré comme terminé côté récepteur
                    } else {
                        // Si autre paquet reçu avant la fin du handshake, on l'ignore (log pour debug)
                        System.out.println("Handshake en attente, reçu autre paquet : " + p);
                    }
                } else {
                    // Après handshake, gestion des paquets de données

                    // Si paquet FIN reçu, cela signifie la fin de la transmission
                    if (p.isFinFlag()) {
                        System.out.println("Réception FIN, fin de transmission.");
                        receptionTerminee = true; // Sortie de la boucle principale
                    } else {
                        // Sinon, écrire le payload reçu dans le fichier de sortie
                        byte[] payload = p.getPayload();
                        if (payload != null && payload.length > 0) {
                            fos.write(payload);
                            fos.flush(); // Assure que les données sont écrites immédiatement sur disque
                            System.out.println("Paquet seq=" + p.getSequenceNumber() + " écrit dans le fichier.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Gestion des erreurs liées à l'écriture fichier
            System.err.println("Erreur écriture fichier : " + e.getMessage());
        }

        // Fermeture du socket UDP proprement à la fin de l'exécution
        socket.close();
    }
}
