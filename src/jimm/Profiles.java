package jimm;

import java.util.Enumeration;
import java.util.Hashtable;

public class Profiles {
    /**
     * @author Rishat Shamsutdinov
     */
    private static final byte ACTION_GET_UREADMCOUNT = 0;
    private static final byte ACTION_RESTORE_STATUS = 1;
    private static final byte ACTION_SET_COLOR_SCHEME = 2;
    private static final byte ACTION_SAVE = 3;
    private static final byte ACTION_DISCONNECT = 4;
    private static final byte ACTION_CONNECTED = 5;

    private static final Hashtable profiles = new Hashtable();

    private static int action(byte action) {
        Enumeration prfs = profiles.elements();
        int res = 0;
        Profile profile;
        while (prfs.hasMoreElements()) {
            profile = (Profile) prfs.nextElement();
            switch (action) {
                case ACTION_GET_UREADMCOUNT:
                    res += profile.getUnreadMessCount();
                    break;
                case ACTION_RESTORE_STATUS:
                    profile.getIcq().restoreStatus();
                    break;
                case ACTION_SET_COLOR_SCHEME:
                    profile.setColorSchemes();
                    break;
                case ACTION_SAVE:
                    profile.safeSave();
                    break;
                case ACTION_DISCONNECT:
                    profile.disconnect();
                    break;
                case ACTION_CONNECTED:
                    if (profile.connectionIsActive() || profile.getIcq().isConnected()) {
                        res++;
                    }
                    break;
            }
        }
        return res;
    }

    public static void showProfiles(DrawControls.CanvasEx back) {
        String nicks[] = new String[Options.nicks.size()];
        if (nicks.length > 0) {
            Options.nicks.copyInto(nicks);
            //Jimm.setDisplay(new ProfileList(back, uins));
            Jimm.setDisplay(new jimm.forms.ProfileListEx());
        }
    }

    public static Profile getProfile(String nick, boolean createIfNeccessary) {
        Profile profile = (Profile) profiles.get(nick);
        if (profile == null && createIfNeccessary) {
            profile = new Profile(nick);
            profiles.put(nick, profile);
        }
        return profile;
    }

    public static Profile getProfile(String nick) {
        return getProfile(nick, false);
    }

    public static void putProfile(Profile profile) {
        profiles.put(profile.getNick(), profile);
    }

    public static boolean profileExist(String nick) {
        return profiles.containsKey(nick);
    }

    public static void update() {
        Enumeration nicks = profiles.keys();
        String nick;
        Profile profile;
        while (nicks.hasMoreElements()) {
            nick = (String) nicks.nextElement();
            if (nick.length() == 0) {
                profile = getProfile(nick);
                profiles.remove(nick);
                profiles.put(profile.getNick(), profile);
                nicks = profiles.keys();
            } else if (!Options.nicks.contains(nick)) {
                profiles.remove(nick);
                nicks = profiles.keys();
            }
        }
    }

    public static void removeProfile(String nick) {
        profiles.remove(nick);
    }

    public static int getUnreadMessCount() {
        return action(ACTION_GET_UREADMCOUNT);
    }

    public static void restoreStatuses() {
        action(ACTION_RESTORE_STATUS);
    }

    public static void setProfile(int i) {
        int size = Options.nicks.size();
        if (i < 0 || i >= size) {
            return;
        }
        String nick = (String) Options.nicks.elementAt(i);
        Jimm.getCurrentProfile().tryToDestroy(nick);
        getProfile(nick, true).activate(Jimm.getCurrentProfile().getNick());
    }

    public static void setColorSchemes() {
        action(ACTION_SET_COLOR_SCHEME);
    }

    public static void save() {
        action(ACTION_SAVE);
    }

    public static void disconnect() {
        action(ACTION_DISCONNECT);
    }

    public static int connectedProfiles() {
        return action(ACTION_CONNECTED);
    }
}