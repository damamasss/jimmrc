package jimm.util;

import DrawControls.NativeCanvas;
import jimm.comm.StringConvertor;

public class Device {

    public static final byte TOUCH = 1 << 0;
    public static final byte PHONE_SE = 1 << 1;
    public static final byte SMART_SE = 1 << 2;
    public static final byte PHONE_NOKIA = 1 << 3;
    public static final byte SMART_NOKIA = 1 << 4;

    int phoneValues = 0;

    public void addPhone(int key, boolean value) {
        phoneValues = (phoneValues & (~key)) | (value ? key : 0x00000000);
    }

    public boolean avaiblePhone(int key) {
        return (phoneValues & key) != 0;
    }

    public Device() {
        addPhone(TOUCH, NativeCanvas.getInst().hasPointerEvents());
        String microeditionPlatform = StringConvertor.getSystemProperty("microedition.platform", null);
        if (microeditionPlatform == null) {
            try {
                Class.forName("com.nokia.mid.ui.DeviceControl");
                addPhone(PHONE_NOKIA, true);
            } catch (Exception ignored) {
            }
            return;
        }
        String platform = microeditionPlatform.toLowerCase();
        addPhone(PHONE_SE, platform.indexOf("ericsson") != -1);
        addPhone(SMART_SE, StringConvertor.getSystemProperty("com.sonyericsson.java.platform", "").toLowerCase().indexOf("sjp") != -1);
        addPhone(PHONE_NOKIA, platform.indexOf("nokia") != -1);
        addPhone(SMART_NOKIA, StringConvertor.getSystemProperty("com.nokia.mid.timeformat", null) == null ||
                StringConvertor.getSystemProperty("com.nokia.mid.batterylevel", null) != null);
    }

    /*public final static byte FOLDER_DATA = (byte) 0;
    public final static byte FOLDER_GRAPHICS = (byte) 1;
    public final static byte FOLDER_SMILEYS = (byte) 2;
    public final static byte FOLDER_SOUNDS = (byte) 3;

    public static String getFolderName(byte folder) {
        switch (folder) {
            case FOLDER_DATA:
                return "data/";
            case FOLDER_GRAPHICS:
                return "graphics/";
            case FOLDER_SMILEYS:
                return "smileys/";
            case FOLDER_SOUNDS:
                return "sounds/";
        }
        return "";
    }*/


//    public boolean is_Touch;
//    public boolean is_phone_SE;
//    public boolean is_smart_SE;
//    public boolean is_phone_NOKIA;
//    public boolean is_smart_NOKIA;
//
//    public Device() {
//        is_Touch = (NativeCanvas.getInst().hasPointerEvents());
//        String microeditionPlatform = StringConvertor.getSystemProperty("microedition.platform", null);
//        if (microeditionPlatform == null) {
//            try {
//                Class.forName("com.nokia.mid.ui.DeviceControl");
//                is_phone_NOKIA = true;
//            } catch (Exception ignored) {
//            }
//            return;
//        }
//        String platform = microeditionPlatform.toLowerCase();
//        is_phone_SE = (platform.indexOf("ericsson") != -1);
//        is_smart_SE = is_phone_SE && ((-1 != StringConvertor.getSystemProperty("com.sonyericsson.java.platform", "").toLowerCase().indexOf("sjp")));
//        is_phone_NOKIA = (platform.indexOf("nokia") != -1);
//        is_smart_NOKIA = (is_phone_NOKIA && (StringConvertor.getSystemProperty("com.nokia.mid.timeformat", null) == null ||
//                StringConvertor.getSystemProperty("com.nokia.mid.batterylevel", null) != null));
//    }
}
