package jimm.ui;

import DrawControls.CanvasEx;
import jimm.*;

public class ProfileList extends SelectList {
    /**
     * @author Rishat Shamsutdinov
     */
    public ProfileList(CanvasEx prvScr, String profiles[]) {
        super(prvScr, profiles);
        update(profiles);
    }

    public void update() {
        lock();
        int i = getCurrIndex();
        update((String[]) objects);
        setCurrent(i);
        unlock();
    }

    private void update(String profiles[]) {
        clear();
        int len = profiles.length;
        String cur = Jimm.getCurrentProfile().getUin();
        String name;
        int curIdx = 0;
        for (int i = 0; i < len; i++) {
            name = "";
            try {
                name = (String) Options.nicks.elementAt(i);
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            if (name.trim().length() == 0) {
                name = profiles[i];
            }
            boolean exist = Profiles.profileExist(profiles[i]);
            // if (!exist) {
            // DebugLog.addText("Not exist: " + profiles[i]);
            // }
            boolean current = cur.equals(profiles[i]);
            int idx = (exist) ? JimmUI.getStatusImageIndex(Profiles.getProfile(profiles[i]).getIcq().getCurrentStatus()) : 6;
            if ((exist) && (Profiles.getProfile(profiles[i]).getUnreadMessCount() > 0)) {
                idx = 14;
            }
            addMenuItem(current ? name + '*' : name, ContactList.imageList.elementAt(idx), (byte) 0);
            if (current) {
                curIdx = i;
            }
        }
        setCurrent(curIdx + 1);
        if (curIdx == getCurrIndex()) {
            setCurrent(0);
        }
    }

    protected void select() {
        Profiles.setProfile(getCurrIndex());
        if (Jimm.getCurrentDisplay() == this) {
            back();
        }
    }
}