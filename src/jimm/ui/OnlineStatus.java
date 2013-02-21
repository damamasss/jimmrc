package jimm.ui;

import jimm.JimmException;
import jimm.util.ResourceBundle;
import jimm.comm.Util;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class OnlineStatus {
    /**
     * @author Roman Lavlinsky
     */
    private int idx;
    private int max;
    private int type;
    private String text;
    private StringBuffer adress;
    //private Random rndm = new Random();

    public OnlineStatus(int i) {
        try {
            type = i;
            adress = new StringBuffer(site()).append(indexStatus(type) + 1);
            max = howManyStatuses();
            if (max < 0) {
                text = "";
                return;
            }
            idx = getRandom();
            adressPlus();
            text = getStringAsHttp(adress.toString());
        } catch (Exception e) {
            text = "";
        }
    }

    private String site() {
        return "http://jimm.besticq.ru/NeU1rde4/";
    }

    private int indexStatus(int i) {
        int[] serverStatusNum = {3, 12, 18, 24, 30, 1, 7, 13, 19, 25, 31, 2, 8, 14, 20, 26, 32, 9, 15, 21, 27, 33, 4, 6, 10, 16, 22, 28, 34, 5, 11, 17, 35, 36, 38, 23, 29};
        if (i < serverStatusNum.length) {
            return serverStatusNum[i];
        }
        return 0;
    }

    private int howManyStatuses() {
        int max = -1;
        String localAdress = adress.toString() + "/_size.txt";
        String maxx = getStringAsHttp(localAdress);
        try {
            max = Integer.parseInt(maxx);
        } catch (Exception ignored) {
        }
        return max;
    }

    private int getRandom() {
        int idx;
        int delta = getDelta();
        Random rndm = new Random();
        for (; ;) {
            int rnd = rndm.nextInt();
            idx = (rnd / delta);
            if (idx <= max && idx >= 0) {
                break;
            }
        }
        return idx;
    }

//    private int randomDigit() {
//        int max = 1497;
//        int idx;
//        int delta = getDelta();
//        Random rndm = new Random();
//        for (; ;) {
//            int rnd = rndm.nextInt();
//            idx = (rnd / delta);
//            if (idx <= max && idx >= 0) {
//                break;
//            }
//        }
//        return idx;
//    }

    private int getDelta() {
        if (max < 50) {
            return 10000000;
        } else if (max < 100) {
            return 5000000;
        } else if (max < 500) {
            return 2500000;
        } else if (max < 1500) {
            return 750000;
        } else {
            return 500000;
        }
    }

    private void adressPlus() {
        adress.append("/random_status_").append(idx).append(".txt");
    }

    public static String getApply(int type) {
        switch (type) {
            case 0:
                return new StringBuffer().append(ResourceBundle.getString("wait")).append(' ').append(ResourceBundle.getString("waiting")).toString();
            case 1:
                return new StringBuffer().append(ResourceBundle.getString("change_status")).toString();
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public static String getStringAsHttp(String str) {
        return getStringAsHttp(str, false);
    }

    public static String getStringAsHttp(String str, boolean showNull) {
        HttpConnection httemp = null;
        InputStream inputstream = null;

        try {
            httemp = (HttpConnection) Connector.open(str);
            if (httemp.getResponseCode() != HttpConnection.HTTP_OK) throw new IOException();
            inputstream = httemp.openInputStream();
            Object obj = new ByteArrayOutputStream();
            int i;
            while ((i = inputstream.read()) != -1) {
                ((ByteArrayOutputStream) (obj)).write(i);
            }
            byte abytes[] = ((ByteArrayOutputStream) (obj)).toByteArray();
            ((ByteArrayOutputStream) (obj)).close();
            inputstream.read(abytes, 0, abytes.length);
            obj = Util.byteArrayToString(abytes, Util.isDataUTF8(abytes)).trim();
            return ((java.lang.String) (obj));
        } catch (Exception e) {
            JimmException.handleExceptionEx(e);
            return (showNull) ? e.toString() : "";
        } finally {
            if (inputstream != null) try {
                inputstream.close();
            } catch (Exception ignored) {
            }
            if (httemp != null) try {
                httemp.close();
            } catch (Exception ignored) {
            }
        }
    }
}