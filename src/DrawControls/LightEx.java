//#sijapp cond.if modules_LIGHT is "true"#
package DrawControls;

import jimm.Jimm;

import java.util.TimerTask;


public class LightEx extends TimerTask {

    private static LightEx light;
    private byte wait;
    private boolean lightOn;
    private static boolean future = false;
    private static byte time = (byte) 1;  // x 5 sec
    private static int sun = 0x64;      // x 10 %
    private static boolean control = false;

    static {
        try {
            control = Class.forName("com.nokia.mid.ui.DeviceControl") != null;
        } catch (ClassNotFoundException ignored) {
        }
    }

    public LightEx() {
        wait = 0;
        lightOn = false;
    }

    public static int getSun() {
        return sun;
    }

    public static void update() {
        sun = 0x64;
        time = 0;
    }

    public static void setLightOnTimer() {
        wake();
        Jimm.getTimerRef().schedule(light = new LightEx(), 0, 5000);
    }

    public static void wake() {
        if (light != null) {
            light.cancel();
            light.wait = 0;
            setLights(0x64);
        }
    }

    public static void flash(boolean flag) {
        if (future) {
            future ^= flag;
            return;
        }
        if (light != null) {
            if (!light.lightOn) {
                light.wait = -1;
            }
            light.lightOn = flag;
            light.run();
        }
    }

    public static void flash(int i) {
        future = true;
        if (light != null) {
            light.wait = time;
            light.lightOn = false;
        }
        setLights(i);
    }

    public final void run() {
        if (control) {
            int i = sun;
            if (!(Jimm.getCurrentDisplay() instanceof CanvasEx) || lightOn) {
                wait = 0;
                setLights(i);
                return;
            }
            byte byte0 = time;
            if (wait++ >= byte0) {
                i = 0;
                wait = byte0;
            }
            setLights(i);
        }
    }

    public static void setLights(int light) {
        if (control) {
            com.nokia.mid.ui.DeviceControl.setLights(0x00, light);
        }
    }
}
//#sijapp cond.end#
