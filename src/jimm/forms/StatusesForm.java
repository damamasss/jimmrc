package jimm.forms;

import DrawControls.CanvasEx;
import DrawControls.ListItem;
import DrawControls.VirtualList;
import DrawControls.VirtualListCommands;
import jimm.*;
import jimm.comm.Icq;
import jimm.comm.Util;
import jimm.comm.XStatus;
import jimm.ui.InputTextBox;
import jimm.ui.Menu;
import jimm.ui.Selector;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.*;

public class StatusesForm extends VirtualList implements VirtualListCommands, CommandListener {

    /**
     * @author Shamsutdinov Rishat
     */

    private Selector sts, xsts, psts;
    private int onlineStatus;
    private CanvasEx prvScreen;
    private Icq icq;

    public StatusesForm(CanvasEx ps, Icq icq) {
        super(null);
        //capDivisor = 2;
        this.icq = icq;
        setMode(MODE_TEXT);
        (sts = new Selector(Selector.UI_MODE_STS, this, statusToIdx(icq.getProfile().getInt(Profile.OPTION_ONLINE_STATUS)))).setFullScreenMode(false);
        (xsts = new Selector(Selector.UI_MODE_XSTS, this, (icq.getProfile().getInt(Profile.OPTION_XSTATUS) == XStatus.XSTATUS_NONE) ? 0 : icq.getProfile().getInt(Profile.OPTION_XSTATUS) + 1)).setFullScreenMode(false);
        (psts = new Selector(Selector.UI_MODE_PSTS, this, icq.getProfile().getInt(Profile.OPTION_PSTATUS) - 1)).setFullScreenMode(false);

        xsts.setForcedSize(getDrawWidth(), -1);
        sts.setForcedSize(getDrawWidth(), -1);
        psts.setForcedSize(getDrawWidth(), -1);

        xsts.calcColsAndRows();
        sts.calcColsAndRows();
        psts.calcColsAndRows();

        int stsSize = sts.getRows();
        int xstsSize = xsts.getRows();
        int pstsSize = psts.getRows();
        setCapElements(getCurrSelector().getCurrName());

        int xstsHeight = xstsSize * xsts.getItemHeight(0) + 4,
                stsHeight = stsSize * sts.getItemHeight(0) + 4,
                pstsHeight = pstsSize * psts.getItemHeight(0);

        int height = getDrawHeight();

        xsts.setForcedSize(getDrawWidth(), Math.min(xstsHeight, height));
        sts.setForcedSize(getDrawWidth(), Math.min(stsHeight, height));
        psts.setForcedSize(getDrawWidth(), Math.min(pstsHeight, height));

//        stsSize = sts.getRows();
//        xstsSize = xsts.getRows();
        setVLCommands(this);
        setCommandListener(this);
        setColorScheme();
        sts.setColorScheme();
        xsts.setColorScheme();
        psts.setColorScheme();
        addCommandEx(JimmUI.cmdSelect, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        prvScreen = ps;
    }

    protected void checkTopItem() {
        int visCount = getVisCount();
        int size = getSize();
        if (size > visCount && currItem > topItem + visCount - 1 && topItem != currItem) {
            topItem = currItem;
        }
        if (currItem < topItem) {
            topItem = currItem;
        }
    }

    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        int transY = 0;
        if (index > 0) {
            y1 -= (transY = getItemHeight(0));
        }
        if (index == 2) {
            y1 -= getItemHeight(1);
            transY += getItemHeight(1);
        }
        g.translate(0, transY);
        if (index == 0) {
            sts.drawItems(g, y1, fontHeight, DMS_DRAW, -1, -1, false, false);
            //sts.drawScroller(g, y1, sts.getVisCount(), 0, true);
        } else if (index == 1) {
            g.setColor(getColor(COLOR_DCURSOR));
            g.fillRect(x1, y1 - 3, getWidthInternal(), 2);
            xsts.drawItems(g, y1, fontHeight, DMS_DRAW, -1, -1, false, false);
            //xsts.drawScroller(g, y1, xsts.getVisCount(), 0, true);
        } else {
            g.setColor(getColor(COLOR_DCURSOR));
            g.fillRect(x1, y1 - 3, getWidthInternal(), 2);
            psts.drawItems(g, y1, fontHeight, DMS_DRAW, -1, -1, false, false);
            //psts.drawScroller(g, y1, psts.getVisCount(), 0, true);
        }
        g.translate(0, -transY);
    }

    protected void get(int index, ListItem item) {
    }

    protected int getSize() {
        return 3;
    }

    public void setCapElements(String cap) {
        setCaption(cap);
    }

    public int getItemHeight(int itemIndex) {
        if (itemIndex == 0) {
            return sts.getHeightInternal();
        } else if (itemIndex == 1) {
            return xsts.getHeightInternal();
        }
        return psts.getHeightInternal();
    }

    public Selector getCurrSelector() {
        return getSelector(currItem);
    }

    public Selector getSelector(int i) {
        switch (i) {
            case 0:
                return sts;
            case 1:
                return xsts;
        }
        return psts;
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        storelastItemIndexes();
        getCurrSelector().vlKeyPress(sender, keyCode, type, gameAct);
        checkItemsAndRepaint();
    }

    public void nextSelector(boolean flag) {
        int idx = getCurrSelector().getCurrSelectedIdx();
        int rows = getCurrSelector().getRows();
        int cols = getCurrSelector().getCols();
        currItem++;
        if (currItem > 2) {
            currItem = 0;
        }
        if (flag) {
            idx -= rows * cols;
            getCurrSelector().setCurrSelectedIdx(idx);
        } else {
            getCurrSelector().setCurrSelectedIdx(0);
        }
        setCapElements(getCurrSelector().getCurrName());
    }

    public void prevSelector(boolean flag) {
        int idx = getCurrSelector().getCurrSelectedIdx();
        currItem--;
        if (currItem < 0) currItem = 2;
        if (flag) {
            int rows = getCurrSelector().getRows();
            int cols = getCurrSelector().getCols();
            idx += rows * cols;
            getCurrSelector().setCurrSelectedIdx(idx);
        } else {
            getCurrSelector().setCurrSelectedIdx(getCurrSelector().getLength() - 1);
        }
        setCapElements(getCurrSelector().getCurrName());
    }

    private void selectXst() {
        int xstIndex = xsts.getCurrSelectedIdx();

        if (xstIndex == 0) {
            icq.setXStatus(XStatus.XSTATUS_NONE, "", "");
            setXstDefIndex();

            invalidate();
        } else {
            Jimm.setPrevScreen(this);
            (new XStatusForm(icq)).showXtrazForm(xstIndex);
        }
    }

    public int getXSIndex() {
        return xsts.getCurrSelectedIdx();
    }

    public void besticqstatus(String get, int index) {
        XStatusForm xf = new XStatusForm(icq);
        String all = xf.getRecordDesc(index);
        String text = all.substring(0, all.indexOf('\t'));
        String xStatus = text + '\t' + get;
        if (!xf.getRecordDesc(index).equals(xStatus)) {
            xf.xstatusform.setElementAt(xStatus, index);
            xf.save();
        }
        //Jimm.setDisplay(new Alert(null, OnlineStatus.getApply(1), null, AlertType.INFO));
        if (index + 1 == xsts.defIdx) {
            icq.setXStatus(index, text, get);
        }
    }

    public void setXstDefIndex() {
        xsts.defIdx = (icq.getProfile().getInt(Profile.OPTION_XSTATUS) == XStatus.XSTATUS_NONE) ? 0 : icq.getProfile().getInt(Profile.OPTION_XSTATUS) + 1;
    }

    public void selectSts() {
        onlineStatus = (int) JimmUI.statuses[sts.getStsIdx()];
        int statusMsgIdx = getMsgIdx();

        if (statusMsgIdx != -1) {
            Jimm.setPrevScreen(this);
            new InputTextBox(InputTextBox.EDITOR_MODE_STATUS_MESSAGE, ResourceBundle.getString("status_message"), icq.getProfile().getString((byte) statusMsgIdx)).activate();
        } else {
            invalidate();
            setStatus();
        }
    }


    public void setStatus() {
        icq.getProfile().setInt(Profile.OPTION_ONLINE_STATUS, onlineStatus);
        icq.getProfile().saveOptions();
        sts.defIdx = sts.getCurrSelectedIdx();
        if (icq.isConnected()) {
            try {
                icq.setOnlineStatus(onlineStatus);
            } catch (JimmException e) {
                JimmException.handleException(e);
            }
        } else {
            if (onlineStatus == ContactItem.STATUS_INVISIBLE) {
                try {
                    icq.setPrivateStatus((byte) 3);
                    psts.defIdx = 2;
                } catch (JimmException ignored) {
                }
            } else if (onlineStatus == ContactItem.STATUS_INVIS_ALL) {
                try {
                    icq.setPrivateStatus((byte) 2);
                    psts.defIdx = 1;
                } catch (JimmException ignored) {
                }
            }
        }
    }

    public int getMsgIdx() {
        return getMsgIdx(onlineStatus);
    }

//    private int getMsgIdx(int onlineStatus) {
//        switch (onlineStatus) {
//            case ContactItem.STATUS_INVISIBLE:
//            case ContactItem.STATUS_INVIS_ALL:
//            case ContactItem.STATUS_ONLINE:
//            case ContactItem.STATUS_EVIL:
//            case ContactItem.STATUS_DEPRESSION:
//            case ContactItem.STATUS_HOME:
//            case ContactItem.STATUS_WORK:
//            case ContactItem.STATUS_LUNCH:
//            case ContactItem.STATUS_CHAT:
//                return -1;
//            case ContactItem.STATUS_NA:
//                return Profile.OPTION_STATUS_MESSAGE_NA;
//            case ContactItem.STATUS_DND:
//                return Profile.OPTION_STATUS_MESSAGE_DND;
//            case ContactItem.STATUS_OCCUPIED:
//                return Profile.OPTION_STATUS_MESSAGE_OCCUPIED;
//        }
//        return Profile.OPTION_STATUS_MESSAGE_AWAY;
//    }

    private int getMsgIdx(int onlineStatus) {
        switch (onlineStatus) {
            case ContactItem.STATUS_INVISIBLE:
            case ContactItem.STATUS_INVIS_ALL:
            case ContactItem.STATUS_ONLINE:
            case ContactItem.STATUS_CHAT:
                return -1;
            case ContactItem.STATUS_NA:
                return Profile.OPTION_STATUS_MESSAGE_NA;
            case ContactItem.STATUS_DND:
                return Profile.OPTION_STATUS_MESSAGE_DND;
            case ContactItem.STATUS_OCCUPIED:
                return Profile.OPTION_STATUS_MESSAGE_OCCUPIED;
            case ContactItem.STATUS_EVIL:
                return Profile.OPTION_STATUS_MESSAGE_EVIL;
            case ContactItem.STATUS_DEPRESSION:
                return Profile.OPTION_STATUS_MESSAGE_DEPRESSION;
            case ContactItem.STATUS_HOME:
                return Profile.OPTION_STATUS_MESSAGE_HOME;
            case ContactItem.STATUS_WORK:
                return Profile.OPTION_STATUS_MESSAGE_WORK;
            case ContactItem.STATUS_LUNCH:
                return Profile.OPTION_STATUS_MESSAGE_LUNCH;
        }
        return Profile.OPTION_STATUS_MESSAGE_AWAY;
    }

    private void selectPst() {
        try {
            icq.setPrivateStatus((byte) icq.transformVisId(psts.getCurrSelectedIdx() + 1), true);
        } catch (JimmException ignored) {
        }
        psts.defIdx = psts.getCurrSelectedIdx();
        invalidate();
    }

//    private int statusToIdx(int onlineStatus) {
//        switch (onlineStatus) {
//            case ContactItem.STATUS_ONLINE:
//                return 0;
//            //case ContactItem.STATUS_CHAT:
//                //return 1;
//            //case ContactItem.STATUS_EVIL:
//                //return 2;
//            //case ContactItem.STATUS_DEPRESSION:
//                //return 3;
//            //case ContactItem.STATUS_HOME:
//                //return 4;
//            //case ContactItem.STATUS_WORK:
//                //return 5;
//            //case ContactItem.STATUS_LUNCH:
//                //return 6;
//            case ContactItem.STATUS_AWAY:
//                return 1;
//            case ContactItem.STATUS_NA:
//                return 2;
//            case ContactItem.STATUS_OCCUPIED:
//                return 3;
//            case ContactItem.STATUS_DND:
//                return 4;
//            case ContactItem.STATUS_INVISIBLE:
//                return 5;
//        }
//        return 0;
//    }


    private int statusToIdx(int onlineStatus) {
        switch (onlineStatus) {
            case ContactItem.STATUS_ONLINE:
                return 0;
            case ContactItem.STATUS_CHAT:
                return 1;
            case ContactItem.STATUS_EVIL:
                return 2;
            case ContactItem.STATUS_DEPRESSION:
                return 3;
            case ContactItem.STATUS_HOME:
                return 4;
            case ContactItem.STATUS_WORK:
                return 5;
            case ContactItem.STATUS_LUNCH:
                return 6;
            case ContactItem.STATUS_AWAY:
                return 7;
            case ContactItem.STATUS_NA:
                return 8;
            case ContactItem.STATUS_OCCUPIED:
                return 9;
            case ContactItem.STATUS_DND:
                return 10;
            case ContactItem.STATUS_INVISIBLE:
                return 11;
        }
        return 12;
    }

    public void pointerDragged(int x, int y) {
        Selector em1;
        int l;
        Selector em2 = getCurrSelector();
        l = y - lastPointerYCrd;
        em1 = em2;
        int i1 = em2.getRows();
        int j1 = em1.getVisCount();
        //if (l == i1 || (k <= 0 ? k == l - i1 : k == 0)) {
        if (i1 == j1 || (l <= 0 ? em1.topItem == i1 - j1 : em1.topItem == 0)) {
            lastPointerTopItem = topItem;
            super.pointerDragged(x, y);
            if (lastPointerTopItem != topItem) {
                currItem = topItem;
            }
            getCurrSelector().pointerPressed(-1, -1);
        } else {
            lastPointerTopItem = -1;
            getCurrSelector().pointerDragged(x, y);
            invalidate();
        }
    }

    public void pointerPressed(int x, int y) {
        super.pointerPressed(x, y);
        getCurrSelector().pointerPressed(x, y);
        // #sijapp cond.if modules_TOUCH2 is "true" #
        pointerReleased(x, y);
        // #sijapp cond.end#
    }

    public void pointerReleased(int x, int y) {
        if (isDraggedWas) {
            return;
        }
        if ((y >= getHeightInternal() - getMenuBarHeight())) {
            super.pointerReleased(x, y);
            return;
        }
        int stsY = sts.getHeightInternal() + 4;
        int xstsY = xsts.getHeightInternal() + 4;
        if (topItem == 0) {
            xstsY += stsY;
        }
        y -= getCapHeight();
        boolean flag = false;
        if (y > stsY && currItem == 0) {
            currItem++;
            flag = true;
        }
        if (y > xstsY && currItem == 1) {
            currItem++;
            flag = true;
        }
        if (topItem != currItem) {
            if (!flag) {
                if (currItem == 2 && y < xstsY) {
                    currItem--;
                }
                if (currItem == 1 && y < stsY) {
                    currItem--;
                }
            }
            if (currItem > 0 && topItem == 0) {
                y -= getItemHeight(0);
            }
            if (currItem == 2 && topItem < 2) {
                y -= getItemHeight(1);
            }
        }
        getCurrSelector().pointerReleased(x, y);
    }

    public void hotNavigation(int keyCode) {
        if (getCurrSelector().getRows() > 1) {
            getCurrSelector().hotNavigation(keyCode);
        }
    }

    public void moveCursor(int step, boolean moveTop) {
        storelastItemIndexes();
        getCurrSelector().moveCursor(step, moveTop);
        checkItemsAndRepaint();
    }

    public void doKeyreaction(int keyCode, int type) {
        if (type == KEY_RELEASED) {
            switch (keyCode) {                
                case Canvas.KEY_NUM0:
                    if (currItem == 1)
                        Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.GET_STATUS), 50);
                    break;

                case Canvas.KEY_POUND:
                    currItem = ++currItem % 3;
                    Selector selector = getCurrSelector();
                    selector.setCurrSelectedIdx(selector.defIdx);
                    //int idx = getCurrSelector().defIdx;
                    //getCurrSelector().setCurrSelectedIdx(idx);
                    setCapElements(selector.getCurrName());
                    checkItemsAndRepaint();
                    break;

                default:
                    super.doKeyreaction(keyCode, type);
                    break;
            }
        } else {
            switch (keyCode) {
                case Canvas.KEY_STAR: {
                    String text = null;
                    switch (currItem) {
                        case 0:
                            byte idx = (byte) getMsgIdx((int) JimmUI.statuses[sts.getStsIdx()]);
                            if (idx != -1)
                                text = icq.getProfile().getString(idx);
                            break;

                        case 1:
                            text = (new XStatusForm(icq)).getRecordDesc(xsts.getCurrSelectedIdx() - 1);
                            break;

                        case 2:
                            text = getCurrSelector().getCurrName();
                            break;
                    }
                    if (text != null)
                        Jimm.setDisplay(new Alert(null, Util.removeNullChars(text).replace('\t', '\n'), null, null));
                }
                return;
            }
            super.doKeyreaction(keyCode, type);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdSelect) {
            vlItemClicked(this);
        } else if (prvScreen instanceof Menu) {
            Jimm.getContactList().showMenu(false);
        } else if (prvScreen != null) {
            Jimm.setDisplay(prvScreen);
        } else {
            Jimm.getContactList().activate();
        }
    }

    public void vlCursorMoved(VirtualList sender) {
        setCapElements(getCurrSelector().getCurrName());
    }

    public void vlItemClicked(VirtualList sender) {
        switch (currItem) {
            case 0:
                selectSts();
                break;
            case 1:
                selectXst();
                break;
            default:
                selectPst();
                break;
        }
    }
}
