import java.io.Serializable;

public class Packet implements Serializable {
    public int numero;
    public byte[] data;

    public Packet(int numero, byte[] data) {
        this.numero = numero;
        this.data = data;
    }
}
