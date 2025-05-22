import java.nio.ByteBuffer;

public class Packet {
    private boolean synFlag;
    private boolean ackFlag;
    private boolean finFlag;
    private boolean rstFlag;
    private int sequenceNumber;
    private byte[] payload;

    /**
     * Constructeur du paquet avec les flags, numéro de séquence et données.
     */
    public Packet(boolean synFlag, boolean ackFlag, boolean finFlag, boolean rstFlag, int sequenceNumber, byte[] payload) {
        this.synFlag = synFlag;
        this.ackFlag = ackFlag;
        this.finFlag = finFlag;
        this.rstFlag = rstFlag;
        this.sequenceNumber = sequenceNumber;
        this.payload = payload;
    }

    // Getters pour accéder aux propriétés du paquet
    public boolean isSynFlag() { return synFlag; }
    public boolean isAckFlag() { return ackFlag; }
    public boolean isFinFlag() { return finFlag; }
    public boolean isRstFlag() { return rstFlag; }
    public int getSequenceNumber() { return sequenceNumber; }
    public byte[] getPayload() { return payload; }

    /**
     * Sérialise le paquet en tableau d'octets.
     * Format :
     * - 1 octet pour les flags (SYN, ACK, FIN, RST codés dans les 4 bits de poids forts)
     * - 4 octets pour le numéro de séquence (int)
     * - 4 octets pour la longueur du payload (int)
     * - payload (données)
     */
    public byte[] toBytes() {
        byte flags = 0;
        if (synFlag) flags |= 0b1000;  // bit 3 pour SYN
        if (ackFlag) flags |= 0b0100;  // bit 2 pour ACK
        if (finFlag) flags |= 0b0010;  // bit 1 pour FIN
        if (rstFlag) flags |= 0b0001;  // bit 0 pour RST

        int payloadLength = (payload != null) ? payload.length : 0;

        // Allocation d'un ByteBuffer de la taille totale nécessaire
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + payloadLength);
        buffer.put(flags);                 // Ajout des flags
        buffer.putInt(sequenceNumber);    // Ajout numéro de séquence
        buffer.putInt(payloadLength);     // Ajout longueur du payload

        // Ajout des données si payload non nul
        if (payloadLength > 0) {
            buffer.put(payload);
        }

        return buffer.array(); // Retourne le tableau d'octets complet
    }

    /**
     * Désérialise un tableau d'octets en un objet Packet.
     * Vérifie que la taille minimale est respectée et que la longueur du payload est valide.
     * @param bytes tableau d'octets à convertir
     * @return Packet correspondant
     * @throws IllegalArgumentException si le format est incorrect
     */
    public static Packet fromBytes(byte[] bytes) throws IllegalArgumentException {
        if (bytes.length < 9) {
            throw new IllegalArgumentException("Taille de paquet trop petite");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte flags = buffer.get();

        // Extraction des flags selon bits codés
        boolean synFlag = (flags & 0b1000) != 0;
        boolean ackFlag = (flags & 0b0100) != 0;
        boolean finFlag = (flags & 0b0010) != 0;
        boolean rstFlag = (flags & 0b0001) != 0;

        int sequenceNumber = buffer.getInt();
        int payloadLength = buffer.getInt();

        // Vérification que la longueur du payload est cohérente avec la taille du tableau
        if (payloadLength < 0 || payloadLength > bytes.length - 9) {
            throw new IllegalArgumentException("Payload length invalide");
        }

        byte[] payload = new byte[payloadLength];
        if (payloadLength > 0) {
            buffer.get(payload, 0, payloadLength);
        }

        return new Packet(synFlag, ackFlag, finFlag, rstFlag, sequenceNumber, payload);
    }

    /**
     * Représentation textuelle du paquet, utile pour debug/log
     * Indique l’état de chaque flag, numéro de séquence, et taille des données.
     */
    @Override
    public String toString() {
        return "Paquet reçu :\n" +
            "  (Un flag à 1 signifie qu'il est activé)\n" +
            "  - SYN flag : " + (synFlag ? "1 (activé)" : "0 (désactivé)") + "\n" +
            "  - ACK flag : " + (ackFlag ? "1 (activé)" : "0 (désactivé)") + "\n" +
            "  - FIN flag : " + (finFlag ? "1 (activé)" : "0 (désactivé)") + "\n" +
            "  - RST flag : " + (rstFlag ? "1 (activé)" : "0 (désactivé)") + "\n" +
            "  - Numéro de séquence : " + sequenceNumber + "\n" +
            "  - Taille des données (payload) : " + (payload != null ? payload.length : 0) + " octets";
    }
}
