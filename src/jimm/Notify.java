package jimm;

import java.io.InputStream;

import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.util.Vector;

public class Notify
//#sijapp cond.if (target isnot "DEFAULT" & modules_SOUNDS is "true")#
        implements PlayerListener
//#sijapp cond.end#
{
    /**
     * @author aspro.
     */
//#sijapp cond.if modules_SOUNDS is "true"#
    public static final int SOUND_TYPE_MESSAGE = 1;
    public static final int SOUND_TYPE_ONLINE = 2;
    public static final int SOUND_TYPE_TYPING = 3;
    public static final int SOUND_TYPE_OFFLINE = 4;
    private static Player player;
    private static final Notify instance = new Notify();
    private static final Vector sq = new Vector();

    private void play(String file, int volume) {
        if (sq.size() > 3) {
            return;
        }
        if (player != null) {
            sq.addElement(new Object[]{file, new Integer(volume)});
            return;
        }
        createPlayer(file);
        setVolume(volume);
        try {
            player.start();
        } catch (Exception e) {
            closePlayer();
        }
    }

    private static synchronized void playSound(int notType) {
        String fileName = "";
        int volume = 0;
        switch (notType) {
            case SOUND_TYPE_MESSAGE:
                fileName = Options.getString(Options.OPTION_MESS_NOTIF_FILE);
                volume = Options.getInt(Options.OPTION_NOTIF_MESSAGE_VOL);
                break;
            case SOUND_TYPE_ONLINE:
                fileName = Options.getString(Options.OPTION_ONLINE_NOTIF_FILE);
                volume = Options.getInt(Options.OPTION_NOTIF_ONLINE_VOL);
                break;
            case SOUND_TYPE_OFFLINE:
                fileName = Options.getString(Options.OPTION_OFFLINE_NOTIF_FILE);
                volume = Options.getInt(Options.OPTION_NOTIF_OFFLINE_VOL);
                break;
            case SOUND_TYPE_TYPING:
                fileName = Options.getString(Options.OPTION_TYPING_NOTIF_FILE);
                volume = Options.getInt(Options.OPTION_NOTIF_TYPING_VOL);
                break;
        }
        if (fileName.length() == 0 || volume == 0) {
            return;
        }
        instance.play(fileName, volume);
    }

    public static boolean testSoundFile(String source) {
        instance.createPlayer(source);
        boolean ok = (player != null);
        instance.closePlayer();
        return ok;
    }

    public void playerUpdate(final Player player, final String event, Object eventData) {
        if (event.equals(PlayerListener.END_OF_MEDIA)) {
            if (sq.size() > 0) {
                Object[] o = (Object[]) sq.elementAt(0);
                closePlayer();
                play((String) o[0], ((Integer) o[1]).intValue());
                sq.removeElementAt(0);
            } else {
                closePlayer();
            }
        }
    }

    private void createPlayer(String source) {
        closePlayer();
        try {
            String ext = "wav";
            int point = source.lastIndexOf('.');
            if (point != -1) {
                ext = source.substring(point + 1).toLowerCase();
            }
            String mediaType;
            if ("mp3".equals(ext)) mediaType = "audio/mpeg";
            else if ("mid".equals(ext) || "midi".equals(ext)) mediaType = "audio/midi";
            else if ("amr".equals(ext)) mediaType = "audio/amr";
            else if ("mmf".equals(ext)) mediaType = "audio/mmf";
            else mediaType = "audio/X-wav";

            InputStream is = getClass().getResourceAsStream('/' + source);
            if (is == null) {
                is = getClass().getResourceAsStream(source);
            }
            if (is != null) {
                player = Manager.createPlayer(is, mediaType);
                player.addPlayerListener(this);
            }
        } catch (Exception e) {
            closePlayer();
        }
    }

    private void closePlayer() {
        if (player != null) {
            try {
                if (player.getState() == Player.STARTED) {
                    player.stop();
                }
                player.removePlayerListener(this);
                player.close();
            } catch (Exception ignored) {
            }
            player = null;
        }
    }

    private void setVolume(int value) {
        try {
            player.realize();
            VolumeControl c = (VolumeControl) player.getControl("VolumeControl");
            if (c != null) {
                c.setLevel(value);
            }
            player.prefetch();
        } catch (Exception ignored) {
        }
    }

    static public synchronized void playSoundNotification(int notType, boolean anyway) {
        if (Options.getBoolean(Options.OPTION_SILENT_MODE) && !anyway) {
            return;
        }
        playSound(notType);
    }
//#sijapp cond.end#

    static public void vibrate(boolean anyway) {  // todo со временем пропадает
        int vibraKind = Options.getInt(Options.OPTION_VIBRATOR);
        if (vibraKind == 2) vibraKind = Jimm.locked() ? 1 : 0;
        if (vibraKind > 0 || anyway) {
            try {
//#sijapp cond.if target is "MIDP2"#
                boolean flag = false;
                int fraq = Options.getInt(Options.OPTION_VIBRO_FRAQ);
                try {
                    flag = Class.forName("com.nokia.mid.ui.DeviceControl") != null;
                } catch (ClassNotFoundException ignored) {
                }
                if (flag && fraq != 10)
                    com.nokia.mid.ui.DeviceControl.startVibra(fraq, Options.getInt(Options.OPTION_VIBRO_TIME));
                else
//#sijapp cond.end#
                    Jimm.getDisplay().vibrate(Options.getInt(Options.OPTION_VIBRO_TIME));
            } catch (Exception e) {
                Jimm.getDisplay().vibrate(500);
            }
        }
        if (vibraKind == 3) {
            try {
                Thread.sleep((Options.getInt(Options.OPTION_VIBRO_TIME) * 2) - (Options.getInt(Options.OPTION_VIBRO_TIME) / 2));
                Jimm.getDisplay().vibrate(Options.getInt(Options.OPTION_VIBRO_TIME));
            } catch (Exception ignored) {
            }
        }
    }
}