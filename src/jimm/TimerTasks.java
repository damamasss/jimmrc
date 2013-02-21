package jimm;

import DrawControls.*;
import jimm.comm.*;
import jimm.forms.StatusesForm;
import jimm.ui.OnlineStatus;
import jimm.ui.PopUp;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import java.util.TimerTask;

public class TimerTasks extends TimerTask {

    public static final int CEX_AUTO_REPAINT = 1;
    public static final int SC_HIDE_KEYLOCK = 2;
    public static final int SC_RESET_TEXT_AND_IMG = 3;
    public static final int UI_UPDATE = 4;
    public static final int GET_VERSION = 5;
    public static final int GET_STATUS = 6;
    public static final int GET_FAQ = 7;
    public static final int GET_UPDATE = 8;
    public static final int SAVE_OPTPRO = 9;

    private static long lastKeyPressedTime = 0;
    private CanvasEx c;
    private int type = -1;
    private Action action;
    boolean wasError = false;
    boolean canceled = false;

    public TimerTasks(CanvasEx c) {
        this.c = c;
        type = CEX_AUTO_REPAINT;
    }

    public TimerTasks(Action action) {
        this.action = action;
    }

    public TimerTasks(int type) {
        this.type = type;
    }

    public boolean cancel() {
        canceled = true;
        return super.cancel();
    }

    public int getType() {
        return type;
    }

    public Action getAction() {
        return action;
    }

    public static void keyPressed() {
        lastKeyPressedTime = System.currentTimeMillis();
    }

    public void run() {
        if (wasError || canceled) {
            return;
        }
        if (type != -1) {
            switch (type) {
                case CEX_AUTO_REPAINT:
                    if (!NativeCanvas.isActive(c) && !(c instanceof SplashCanvas && Jimm.getCurrentDisplay() instanceof SplashCanvas)) {
                        cancel();
                        return;
                    }
                    c.invalidate();
                    break;

                case SC_HIDE_KEYLOCK:
                    //Jimm.getSplashCanvasRef().showKeylock = false;
                    Jimm.getSplashCanvasRef().invalidate();
                    break;

                case SC_RESET_TEXT_AND_IMG:
                    //Jimm.getSplashCanvasRef().setMessage(ResourceBundle.getString("keylock_enabled"));
                    //Jimm.setStatusesToDraw(JimmUI.getStatusImageIndex(Jimm.getIcqRef().getCurrentStatus()),
                            //Jimm.getIcqRef().getCurrentXStatus());
                    Jimm.getSplashCanvasRef().invalidate();
                    cancel();
                    break;

                case UI_UPDATE:
                    if ((lastKeyPressedTime > 0) && (System.currentTimeMillis() - lastKeyPressedTime >= 30000) && (Options.getBoolean(Options.OPTION_AUTOLOCK))) {
                        if (Jimm.getContactList().isActive()) {
                            Jimm.getSplashCanvasRef().lockProgramm();
                        }
                    }
                    if (DateAndTime.nextDay()) {
                        Jimm.getCurrentProfile().updateHappyFlags();
                    }
                    if (Runtime.getRuntime().freeMemory() < 30000) {
                        //int i = 0;
                        //boolean flag;
                        //do {
                        //	flag = false;
                        //	i += 10240;
                        //    try {
                        //        int ai[] = new int[i];
                        //        flag = true;
                        //    } catch(OutOfMemoryError outofmemoryerror) {
                        //	}
                        //} while(flag);
                        System.gc();
                    }
                    VirtualList.updateTimeString();
                    break;

                case GET_STATUS:
                    analyseOS();
                    break;

                case SAVE_OPTPRO:
                    Options.safe_save();
                    try {
                        Thread.sleep(10);
                    } catch (Exception ignored) {
                    }
                    Jimm.getCurrentProfile().safeSave();
                    break;
            }
            return;
        }
        // отрисовка надписей при подключении. значения берутся из ConnectAction.java
        String screenMsg = action.getProgressMsg();
        if (screenMsg == null) {
            screenMsg = ResourceBundle.getString("wait");
        }
        if (wasError || canceled) {
            return;
        }
        // отрисовка програесс-бара при подключении. значения берутся из ConnectAction.java
        action.getIcq().getProfile().addAction(screenMsg, action.getProgress(), null, action);
        if (action.isCompleted()) {
            action.getIcq().getProfile().actionCompleted(action);
            action.onEvent(Action.ON_COMPLETE);
            cancel();
        } else if (action.isError()) {
            wasError = true;
            action.getIcq().getProfile().actionCompleted(action);
            action.onEvent(Action.ON_ERROR);
            cancel();
        }
    }

    public boolean onCancel() {
        cancel();
        if (action != null) {
            action.onEvent(Action.ON_CANCEL);
            action.getIcq().getProfile().actionCompleted(action);
        }
        return true;
    }

    private void analyseOS() {
        boolean good;
        Profile profile = Jimm.getCurrentProfile();
        int id = profile.getInt(Profile.OPTION_XSTATUS);
        good = (id != XStatus.XSTATUS_NONE);
        Object display = Jimm.getCurrentDisplay();
        if (display instanceof StatusesForm) {
            StatusesForm statusesForm = (StatusesForm) display;
            id = statusesForm.getXSIndex() - 1;
            good = (id != -1);
        }
        String toShow = OnlineStatus.getApply(good ? 0 : 1);
        Jimm.setDisplay(new Alert(null, toShow, null, AlertType.INFO));
        if (!good) {
            return;
        }
        /*process...*/
        OnlineStatus on = new OnlineStatus(id); // max timeout
        String status = on.getText();
        if (status.length() < 1) {
            Jimm.setDisplay(new Alert(null, ResourceBundle.getString("errorlng"), null, AlertType.INFO));
            return;
        }
        display = Jimm.getCurrentDisplay();
        if (display instanceof StatusesForm) {
            StatusesForm statusesForm = (StatusesForm) display;
            statusesForm.besticqstatus(status, on.getType());
            return;
        } else if (display instanceof PopUp) {
            PopUp popUp = (PopUp) display;
            popUp.doKeyreaction(CanvasEx.KEY_CODE_RIGHT_MENU, CanvasEx.KEY_RELEASED);
            if (popUp.getPrvScreen() instanceof StatusesForm) {
                StatusesForm statusesForm = (StatusesForm) popUp.getPrvScreen();
                statusesForm.besticqstatus(status, on.getType());
                return;
            }
        }
        JimmUI.setClipBoardText(status);
    }
}
