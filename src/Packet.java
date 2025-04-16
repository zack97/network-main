import java.io.Serializable;

public class Packet implements Serializable {
    public int numero;
    public byte[] donnees;
    public boolean synFlag;
    public boolean ackFlag;
    public boolean finFlag;
    public boolean rstFlag;

    // Constructeur principal
    public Packet(int numero, byte[] donnees, boolean syn, boolean ack, boolean fin, boolean rst) {
        this.numero = numero;
        this.donnees = donnees;
        this.synFlag = syn;
        this.ackFlag = ack;
        this.finFlag = fin;
        this.rstFlag = rst;
    }

    // Constructeur simplifié (par défaut tous les flags à false)
    public Packet(int numero, byte[] donnees) {
        this(numero, donnees, false, false, false, false);
    }
}
