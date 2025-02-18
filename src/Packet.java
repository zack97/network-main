import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Classe représentant un paquet utilisé dans la communication réseau.
 * Ce paquet contient des données ainsi que des informations liées au protocole
 * (numéro de séquence, drapeaux SYN/ACK/FIN/RST).
 */
public class Packet implements Serializable {
    private static final long serialVersionUID = 1L; // Identifiant pour la sérialisation
    private int sequenceNumber; // Numéro de séquence du paquet
    private byte[] data; // Données transportées par le paquet
    private boolean synFlag; // Drapeau SYN : utilisé pour l'initialisation de la connexion
    private boolean ackFlag; // Drapeau ACK : utilisé pour confirmer la réception
    private boolean finFlag; // Drapeau FIN : utilisé pour terminer la connexion
    private boolean rstFlag; // Drapeau RST : utilisé pour réinitialiser la connexion

    /**
     * Constructeur pour créer un nouveau paquet.
     * 
     * @param sequenceNumber Numéro de séquence du paquet
     * @param data           Données à inclure dans le paquet
     * @param syn            Indique si le drapeau SYN est activé
     * @param ack            Indique si le drapeau ACK est activé
     * @param fin            Indique si le drapeau FIN est activé
     * @param rst            Indique si le drapeau RST est activé
     */
    public Packet(int sequenceNumber, byte[] data, boolean syn, boolean ack, boolean fin, boolean rst) {
        this.sequenceNumber = sequenceNumber;
        this.data = data;
        this.synFlag = syn;
        this.ackFlag = ack;
        this.finFlag = fin;
        this.rstFlag = rst;
    }

    /**
     * Sérialise un objet Packet en un tableau d'octets.
     * Cela est utile pour envoyer le paquet sur le réseau.
     * 
     * @param packet Le paquet à sérialiser
     * @return Un tableau d'octets représentant le paquet
     * @throws Exception En cas de problème durant la sérialisation
     */
    public static byte[] serialize(Packet packet) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Flux pour stocker les données en mémoire
        ObjectOutputStream oos = new ObjectOutputStream(baos); // Flux pour écrire l'objet en bytes
        oos.writeObject(packet); // Écriture de l'objet Packet
        return baos.toByteArray(); // Récupération du tableau d'octets
    }

    /**
     * Désérialise un tableau d'octets pour recréer un objet Packet.
     * Cela est utilisé pour recevoir un paquet envoyé sur le réseau.
     * 
     * @param bytes Le tableau d'octets à désérialiser
     * @return L'objet Packet recréé
     * @throws Exception En cas de problème durant la désérialisation
     */
    public static Packet deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); // Flux pour lire les données à partir d'un tableau
                                                                     // d'octets
        ObjectInputStream ois = new ObjectInputStream(bais); // Flux pour recréer l'objet
        return (Packet) ois.readObject(); // Conversion des bytes en objet Packet
    }

    // Accesseurs pour obtenir les informations du paquet

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isSynFlag() {
        return synFlag;
    }

    public boolean isAckFlag() {
        return ackFlag;
    }

    public boolean isFinFlag() {
        return finFlag;
    }

    public boolean isRstFlag() {
        return rstFlag;
    }
    /*
     * Points à Améliorer :
     * 
     * Format du protocole :
     * 
     * 
     * Manque la taille totale des données (16 bits)
     * Pas de gestion du byte padding
     * Pas de gestion explicite du format big-endian
     */

    // Devrait utiliser une sérialisation manuelle pour contrôler le format
    private byte[] manualSerialize() {
        ByteBuffer buffer = ByteBuffer.allocate(calculateSize());
        buffer.order(ByteOrder.BIG_ENDIAN);
        // Ajouter les champs...
        return buffer.array();
    }
}
