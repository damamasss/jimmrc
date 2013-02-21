package jimm.ui;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class Arrays {

    private Object[][] arrays;

    public Arrays() {
        arrays = new Object[3][];
    }

    public String[] getStrings(int item) {
        if (arrays != null) {
            return (String[]) arrays[Math.min(arrays.length - 1, Math.max(item, 0))];
        }
        return null;
    }

    public int[] getIndex() {
        if (arrays != null && arrays[0] != null) {
            int[] ints = new int[arrays[0].length];
            for (int i = 0; i < ints.length; i++) {
                try {
                    ints[i] = ((Integer) arrays[0][i]).intValue();
                } catch (Exception e) {
                    ints[i] = 0;
                }
            }
            return ints;
        }
        return null;
    }

    public void init(String local) {
        InputStream stream = getClass().getResourceAsStream(local);
        if (stream == null) {
            return;
        }
        Vector elements = new Vector();
        try {
            DataInputStream dos = new DataInputStream(stream);
            StringBuffer strBuffer = new StringBuffer();
            boolean eof = false;
            String word;
            String name = null;
            boolean ctlf;
            for (; ;) {
                ctlf = false;
                readStringFromStream(strBuffer, dos);
                Integer idx = Integer.valueOf(strBuffer.toString());
                try {
                    ctlf = readStringFromStream(strBuffer, dos);
                } catch (EOFException eofExcept) {
                    eof = true;
                }
                word = new String(strBuffer).trim();
                if (!ctlf && !eof) {
                    try {
                        readStringFromStream(strBuffer, dos);
                    } catch (EOFException eofExcept) {
                        eof = true;
                    }
                    name = new String(strBuffer).trim();
                }
                elements.addElement(new Object[]{idx, word, name});
                if (eof) {
                    break;
                }
            }
            dos.close();
        } catch (Exception ignored) {
        }
        try {
            stream.close();
        } catch (Exception ignored) {
        }

        int size = elements.size();
        if (size < 1) {
            return;
        }
        arrays[0] = new Integer[size];
        arrays[1] = new String[size];
        arrays[2] = new String[size];
        Object[] data;
        for (int i = 0; i < size; i++) {
            data = (Object[]) elements.elementAt(i);
            arrays[0][i] = data[0];
            arrays[1][i] = data[1];
            arrays[2][i] = data[2];
        }
    }

    private boolean readStringFromStream(StringBuffer buffer, DataInputStream stream) throws IOException {
        byte chr;
        buffer.setLength(0);
        for (; ;) {
            chr = stream.readByte();
            if ((chr == ',') || (chr == '\n') || (chr == '\t')) break;
            if (chr >= ' ') buffer.append((char) chr);
        }
        return (chr == '\n');
    }
}
