package jimm.plus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created [13.03.2011, 21:51:26]
 * Develop by Lavlinsky Roman on 2011
 */
public class ByteArrayStream {
    ByteArrayInputStream bais;
    ByteArrayOutputStream baos;

    public void append(char c) {
        baos.write(c);
    }

    public byte[] sub(ByteArrayInputStream bais, int len) throws IOException{
        byte[] temp = new byte[len];
        bais.read(temp);
        return temp;
    }

    public void index(char c) {
        baos.write(c);
    }
}
