package jimm.forms;

import DrawControls.*;
import jimm.ContactList;
import jimm.Emotions;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.comm.StringConvertor;
import jimm.ui.*;

import javax.microedition.lcdui.*;

public class FormEx extends VirtualTree implements VirtualTreeCommands, VirtualListCommands, CommandListener, MenuListener {
/**
 *@author Rishat Shamsutdinov
 */
    /**
     * ***************************************
     */
    class FormItem {
        Object item;
        Icon icon;
        Icon subicon;
        int idx;
        String text;

        FormItem(Object item, String text, int idx, Image img, Icon icon) {
            this.item = item;
            this.idx = idx;
            subicon = new Icon(img);
            this.icon = icon;
            this.text = text;
        }

        FormItem(Object item, String text, int idx, Icon icon) {
            this.item = item;
            this.icon = icon;
            this.idx = idx;
            this.text = text;
        }

        FormItem(Object item, String text, Icon icon) {
            this(item, text, -1, icon);
        }
    }

    /**
     * ***************************************
     */

    private final ImageList fmImList = ImageList.loadFull("form.png");
    private ItemStateListenerEx iSListener;
    private final java.util.Vector commands = new java.util.Vector();

    public FormEx(String cap, Command rightCmd) {
        this(cap, null, rightCmd);
    }

    public FormEx(String cap, Command leftCmd, Command rightCmd) {
        super(cap, false);
        setStepSize(5);
        this.fontSize = Font.SIZE_SMALL;
        if (leftCmd == null) {
            addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        } else {
            addCommandEx(leftCmd, MENU_TYPE_LEFT_BAR);
        }
        addCommandEx(rightCmd, MENU_TYPE_RIGHT_BAR);
        setVTCommands(this);
        setVLCommands(this);
    }

    public void beforeShow() {
        setColorScheme();
        int count = getSize();
        for (int i = count - 1; i >= 0; i--) {
            update(getItem(i));
        }
    }

    private void update(TreeNode src) {
        Object o = src.getData();
        if ((o == null) || !(o instanceof FormItem)) {
            return;
        }
        FormItem fi = (FormItem) o;
        if (!(fi.item instanceof Item)) {
            return;
        }
        Item data = (Item) fi.item;
        if (data instanceof ChoiceGroup) {
            if (fi.idx == -1) {
                int imgIndex = 1;
                if (!src.getExpanded()) {
                    imgIndex = 0;
                }
                fi.icon = ContactList.grIcons.elementAt(imgIndex);
            } else {
                ChoiceGroupEx chgr = (ChoiceGroupEx) data;
                fi.icon = getIcon(chgr, fi.idx);
                fi.text = chgr.getString(fi.idx);
            }
        } //else if (data instanceof TextField) {
        //fi.text = getText(data);
        //}
    }

    public void append(Item JVMItem) {
        String label = StringConvertor.getString(JVMItem.getLabel());
        //Icon icon = null;
        if (JVMItem instanceof TextField) {
            TextField tf = (TextField) JVMItem;
            addNode(null, new FormItem(JVMItem, null, new TextIcon(fmImList.elementAt(4), label, getText(tf), check4Pass(tf), getDrawWidth() * 19 / 20, getGIHeight())));
            //return;
        } else if (JVMItem instanceof Gauge) {
            //icon = fmImList.elementAt(4);
            //if (label.length() > 0) {
            //    append(label);
            //}
            //if (item instanceof Gauge) {
            Gauge g = (Gauge) JVMItem;
            addNode(null, new FormItem(JVMItem, null, new GaugeIcon(getDrawWidth() * 19 / 20, getGIHeight(), g.getValue(), g.getMaxValue(), label)));
            //return;
            //}
        } else if (JVMItem instanceof ChoiceGroupEx) {
            //icon = ContactList.grIcons.elementAt(0);
            if (label.length() == 0) {
                fill(getRoot(), JVMItem);
                //return;
            }
        }
        //fill(addNode(null, new FormItem(item, getText(item), icon)), item);
    }


    public void append(String str) {
        addNode(null, str);
    }

//    public void append(ColorIcon ci) {
//        addNode(null, new FormItem(null, null, ci));
//    }

    public void append(LineChoise ls) {
        addNode(null, new FormItem(ls, null, new LineIcon(ls, getDrawWidth() * 19 / 20, getGIHeight())));
    }

    private int getGIHeight() {
        return vtGetItemHeight();
    }

    public int getFormIconsHeight() {
        return fmImList.getHeight();
    }

    public void addCommand(Command c) {
        commands.addElement(c);
    }

    public void removeCommand(Command c) {
        removeCommandEx(c);
    }

    public void removeAllCommands() {
        super.removeAllCommands();
        commands.removeAllElements();
    }

    protected Command findMenuByType(int type) {
        if (type == Command.OK) {
            return null;
        }
        return super.findMenuByType(type);
    }

    private void fill(TreeNode node, Item item) {
        if (item instanceof ChoiceGroupEx) {
            ChoiceGroupEx chgr = (ChoiceGroupEx) item;
            //if (chgr.getChoiceType() != Choice.POPUP) {
            //	setExpandFlag(node, true);
            //}
            int count = chgr.size();
            for (int i = 0; i < count; i++) {
                setExpandFlag(addNode(node, new FormItem(item, chgr.getString(i), i, chgr.getImage(i), getIcon(chgr, i))), chgr.isSelected(i));
            }
        }
    }

    public void deleteAll() {
        clear();
    }

    public void setItemStateListener(ItemStateListenerEx listener) {
        iSListener = listener;
    }

    //protected boolean isItemSelected(int index) {
    //	boolean result = super.isItemSelected(index);
    //	Object data = getItem(index).getData();
    //	if (result && data != null) {
    //		result = !(data instanceof String);
    //	}
    //	return result;
    //}

    public void VTGetItemDrawData(TreeNode src, ListItem dst) {
        Object data = src.getData();
        if (data == null) {
            return;
        }
        dst.color = getColor(COLOR_TEXT);
        if (data instanceof FormItem) {
            FormItem ddFI = (FormItem) data;
            dst.image = ddFI.icon;
            dst.HappyImg = ddFI.subicon;
            dst.text = ddFI.text;
            //if (ddFI.item instanceof ChoiceGroupEx && ddFI.idx == -1) {
            //    dst.fontStyle = Font.STYLE_BOLD;
            //}
            //if (ddFI.item instanceof TextField) {
            //    dst.needBottom = true;
            //}
            if ((ddFI.icon == null) && (ddFI.item instanceof ChoiceGroupEx) && (ddFI.idx >= 0)) {
                if (((ChoiceGroupEx) ddFI.item).isSelected(ddFI.idx)) {
                    dst.text = "[x]" + dst.text;
                } else {
                    dst.text = "[ ]" + dst.text;
                }
            }
        } else {
            dst.text = (String) data;
            //dst.fontStyle = Font.STYLE_BOLD;
        }
        //dst.text = getText(data, true);
        //if (data instanceof ChoiceGroup) {
        //	int imgIndex = 1;
        //	if (!src.getExpanded()) {
        //		imgIndex = 0;
        //	}
        //	dst.image = ContactList.grIcons.elementAt(imgIndex);
        //} else if (data instanceof FormItem) {
        //	ddFI = (FormItem) data;
        //	dst.image = ddFI.icon;
        //	if (ddFI.item instanceof ChoiceGroupEx) {
        //		//dst.image = getIcon((ChoiceGroupEx) ddFI.item, ddFI.idx);
        //		dst.AuthImg = ddFI.subicon;
        //	}// else if (ddFI.item instanceof Gauge) {
        //	//	dst.image = ddFI.icon;
        //	//}
        //} else if (data instanceof Item) {
        //	if (data instanceof TextField) {
        //		dst.image = fmImList.elementAt(4);
        //	}
        //}
    }

    private String getText(Object data) {
        if (data instanceof ChoiceGroup) {
            return ((Item) data).getLabel();
        }
        if (data instanceof FormItem) {
            FormItem fi = (FormItem) data;
            //if (fi.item instanceof ChoiceGroupEx) {
            //	return ((ChoiceGroupEx) fi.item).getString(fi.idx);
            //}
            if (fi.item instanceof TextField) {
                TextIcon tfc = (TextIcon) fi.icon;
                return tfc.getTitle() + ":\n" + check4Pass((TextField) fi.item);
            } else if (fi.item instanceof Gauge) {
                GaugeIcon gi = (GaugeIcon) fi.icon;
                Gauge g = (Gauge) fi.item;
                return gi.getLabel() + ":\n" + 100 * g.getValue() / g.getMaxValue() + "%";
            } else if (fi.item instanceof LineChoise) {
                LineChoise ls = (LineChoise) fi.item;
                return ls.getLabel() + ":\n" + ls.getItems()[ls.getSelected()];
            } else if (fi.icon instanceof ColorIcon) {
                ColorIcon ci = (ColorIcon) fi.icon;
                String hex = "000000" + Integer.toHexString(ci.getColor());
                return ci.getHeader() + ":\n" + hex.substring(hex.length() - 6);
            }
            return fi.text;
        }
        if (data instanceof Item) {
            StringBuffer text = new StringBuffer();
            if (data instanceof TextField) {
                //text.append(" [").append(getString((TextField) data, getQuickFont(Font.STYLE_PLAIN))).append(']');
                text.append(' ').append(getString((TextField) data, getQuickFont(Font.STYLE_PLAIN)));
                //text.append(getString((TextField) data, getQuickFont(Font.STYLE_PLAIN)));
            } else {
                text.append(StringConvertor.getString(((Item) data).getLabel()));
            }
            return text.toString();
        }
        if (data instanceof String) {
            return (String) data;
        }
        return null;
    }

    private String check4Pass(TextField tf) {
        String str = tf.getString();
        StringBuffer buf = new StringBuffer(str.length());
        if (tf.getConstraints() != TextField.PASSWORD) {
            buf.append(str);
        } else {
            for (int i = str.length() - 1; i >= 0; i--) {
                buf.append('*');
            }
        }
        return buf.toString();
    }

    private String getString(TextField tf, FontFacade font) {
        String str = check4Pass(tf);

        int screenW = getDrawWidth() - fmImList.getWidth() - 9 - stringWidth(font, " [...]");
        int len = str.length();
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < len; i++) {
            result.append(getChar(str.charAt(i)));
            if (stringWidth(font, result.toString()) >= screenW) {
                break;
            }
        }
        if (result.length() != str.length()) {
            result.append("...");
        }
        return result.toString();
    }

    private char getChar(char ch) {
        return (ch == '\n') ? ' ' : ch;
    }

    //#sijapp cond.if target is "MIDP2" #
    protected boolean pointerPressedOnUtem(int index, int x, int y, int x1, int x2) {
        TreeNode item = getItem(index);
        if (item == null) {
            return true;
        }
        Object data = item.getData();
        if ((data == null) || !(data instanceof FormItem)) {
            return true;
        }
        FormItem fi = (FormItem) data;
        if (fi.item instanceof Gauge && currItem == index) {
            GaugeIcon gi = (GaugeIcon) fi.icon;
            gi.pPressed(x);
            ((Gauge) fi.item).setValue(gi.getPosition());
            invalidate();
            return false;
        }
        if (fi.item instanceof LineChoise && currItem == index) {
            LineIcon li = (LineIcon) fi.icon;
            if (x < NativeCanvas.getWidthEx() / 3 || x > 2 * NativeCanvas.getWidthEx() / 3) {
                li.pPressed(x);
                invalidate();
            } else {
                this.vlItemClicked(this);
            }
            return false;
        }
        return true;
    }

    protected boolean oldDoubleClick() {
        return true;
    }
//#sijapp cond.end #

    protected boolean keyReaction(int keyCode, int type, int gameAct) {
        return (keyCode == Canvas.KEY_STAR || keyCode == Canvas.KEY_POUND) && (type == KEY_PRESSED) || super.keyReaction(keyCode, type, gameAct);
    }

    public int vtCompareNodes(TreeNode node1, TreeNode node2) {
        return 0;
    }

    public int vtGetItemHeight() {
        return Math.max(getFormIconsHeight() * 2 + 4, (facade.getFontHeight() + 4) * 2);
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        TreeNode node = getCurrentItem();
        if (node == null) {
            return;
        }
        Object data = node.getData();
        if (keyCode == Canvas.KEY_STAR && data != null) {
            PopUp popUp = new PopUp(this, getText(data), getDrawWidth(), 4, getCapHeight());
            if ((popUp != null) && (popUp.getPrvScreen() != null)) {
                Jimm.setDisplay(popUp);
            }
            return;
        }
        //if (keyCode == Canvas.KEY_NUM0) {
        //    this.vlItemClicked(sender);
        //    return;
        //}
        if (type == KEY_RELEASED) {
            return;
        }        
        if ((data == null) || !(data instanceof FormItem)) {
            return;
        }
        FormItem fi = (FormItem) data;
        if (fi.item instanceof Gauge) {
            Gauge g = (Gauge) fi.item;
            try {
                switch (gameAct) {
                    case Canvas.RIGHT:
                        g.setValue(g.getValue() + 1);
                        break;
                    case Canvas.LEFT:
                        g.setValue(g.getValue() - 1);
                        break;
                }
            } catch (IllegalArgumentException ignored) {
            }
            ((GaugeIcon) fi.icon).setPosition(g.getValue());
            if (iSListener != null) {
                iSListener.itemStateChanged(g);
            }
            repaint();
        } else if (fi.item instanceof LineChoise) {
            LineIcon lineIcon = (LineIcon) fi.icon;
            try {
                switch (gameAct) {
                    case Canvas.RIGHT:
                        lineIcon.action(true);
                        break;
                    case Canvas.LEFT:
                        lineIcon.action(false);
                        break;
                }
            } catch (IllegalArgumentException ignored) {
            }
            //if (iSListener != null) {
            //    iSListener.itemStateChanged(fi.item);
            //}
            repaint();
        } else if (fi.item instanceof TextField) {
            // ???
        } else {
            switch (gameAct) {
                case Canvas.RIGHT:
                    move(1);
                    break;
                case Canvas.LEFT:
                    move(-1);
                    break;
            }
        }
    }

//	public void moveCursor(int step, boolean moveTop) {
//		super.moveCursor(step, moveTop);
//		//while (!isItemSelected(getCurrIndex())) {
//		//	super.moveCursor(step, moveTop);
//		//}
//	}

    public void vlCursorMoved(VirtualList sender) {
    }

    public void vlItemClicked(VirtualList sender) {
        TreeNode node = getCurrentItem();
        if (node == null) {
            return;
        }
        setExpandFlag(node, !node.getExpanded());
        Object data = node.getData();
        if (data == null) {
            return;
        }
        if (data instanceof FormItem) {
            FormItem fi = (FormItem) data;
            if (fi.item instanceof ChoiceGroupEx) {
                if (fi.idx == -1) {
                    fi.icon = ContactList.grIcons.elementAt(node.getExpanded() ? 1 : 0);
                    int count = node.size();
                    for (int i = count - 1; i >= 0; i--) {
                        update(node.elementAt(i));
                    }
                    return;
                }
                ChoiceGroupEx chgr = (ChoiceGroupEx) fi.item;
                chgr.setSelectedIndex(fi.idx, node.getExpanded());
                fi.icon = getIcon(chgr, fi.idx);
                if (chgr.getChoiceType() == Choice.POPUP || chgr.getChoiceType() == Choice.EXCLUSIVE) {
                    update(chgr, findParent(getRoot(), node));
                }
                if (iSListener != null) {
                    iSListener.itemStateChanged(fi.item);
                }
            } else if (fi.item instanceof TextField) {
                TextField tf = (TextField) fi.item;
                TextBox tb = new TextBox(tf.getLabel(), tf.getString(), tf.getMaxSize(), tf.getConstraints());
                tb.addCommand(JimmUI.cmdBack);
                tb.addCommand(JimmUI.cmdOk);
                if (tb.getConstraints() != TextField.PASSWORD) {
                    tb.addCommand(JimmUI.cmdPaste);
                    //if (tb.getConstraints() != TextField.NUMERIC) {
                    //    tb.addCommand(JimmUI.transliterateCommand);
                    //    tb.addCommand(JimmUI.detransliterateCommand);
                    //}
                }
//#sijapp cond.if modules_SMILES is "true" #
                if (tb.getConstraints() == TextField.ANY) {
                    tb.addCommand(JimmUI.cmdInsertEmo);
                }
//#sijapp cond.end#
                tb.setCommandListener(this);
                Jimm.setDisplay(tb);
            } else if (fi.item instanceof LineChoise) {
                //if (fi.item instanceof LineChoise && fi.icon != null) {
                //LineIcon ls = (LineIcon) fi.icon ;
                //ls.open = !ls.open;
                //if (ls.open) {
                //return;
                //}
                //}
                if (iSListener != null) {
                    iSListener.itemStateChanged(fi.item);
                }
            } else if (fi.icon instanceof ColorIcon) {
                if (iSListener != null) {
                    iSListener.itemStateChanged(fi.icon);
                }
            }
        }
    }

    private void update(ChoiceGroupEx chgr, TreeNode node) {
        int count = node.size();
        TreeNode el;
        for (int i = count - 1; i >= 0; i--) {
            el = node.elementAt(i);
            setExpandFlag(el, chgr.isSelected(i));
            ((FormItem) el.getData()).icon = getIcon(chgr, i);
        }
    }

    protected boolean executeCommand(Command command) {
        if (command == JimmUI.cmdMenu) {
            showMenu();
            return true;
        }
        return super.executeCommand(command);
    }

    protected void showMenu() {
        Menu menu = new Menu(this);
        int size = commands.size();
        boolean swaped;
        do {
            swaped = false;
            Command cmd1, cmd2;
            for (int i = 0; i < size - 1; i++) {
                cmd1 = (Command) commands.elementAt(i + 1);
                cmd2 = (Command) commands.elementAt(i);
                if (cmd1.getPriority() < cmd2.getPriority()) {
                    commands.setElementAt(cmd1, i);
                    commands.setElementAt(cmd2, i + 1);
                    swaped |= true;
                }
            }
        } while (swaped);
        for (int i = 0; i < size; i++) {
            menu.addMenuItem(((Command) commands.elementAt(i)).getLabel(), (byte) i);
        }
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        executeCommand((Command) commands.elementAt(action));
    }

    public void commandAction(Command c, Displayable d) {
        if (d instanceof TextBox) {
            TextBox tb = (TextBox) d;
            if (c == JimmUI.cmdOk) {
                FormItem fi = (FormItem) getCurrentItem().getData();
                TextField tf = (TextField) fi.item;
                tf.setString(tb.getString());
                //fi.text = getText(tf);
                TextIcon old = (TextIcon) fi.icon;
                fi.icon = new TextIcon(old.getIcon(), old.getTitle(), getText(tf), check4Pass(tf), getDrawWidth() * 19 / 20, getGIHeight());
                if (iSListener != null) {
                    iSListener.itemStateChanged(tf);
                }
                Jimm.setDisplay(this);
            } else if (c == JimmUI.cmdPaste) {
                String text = JimmUI.getClipBoardText(false);
                do {
                    if (text.indexOf('\n') < 0) {
                        break;
                    }
                    text = text.substring(0, Math.max(text.length() - 1, 0));
                } while (text.charAt(text.length() - 1) == '\n');
                try {
                    tb.insert(text, tb.getCaretPosition());
                } catch (Exception ignored) {
                }
            //} else if (c == JimmUI.transliterateCommand) {
            //    tb.setString((new StringConvertor()).transliterate(tb.getString()));
            //} else if (c == JimmUI.detransliterateCommand) {
            //    tb.setString((new StringConvertor()).detransliterate(tb.getString()));
//#sijapp cond.if modules_SMILES is "true" #
            } else if (c == JimmUI.cmdInsertEmo) {
                try {
                    Emotions.selectEmotion(tb, tb);
                } catch (Exception ignored) {
                }
//#sijapp cond.end#
            } else {
                Jimm.setDisplay(this);
            }
        }
    }

    private Icon getIcon(ChoiceGroupEx chgr, int idx) {
        boolean isSelected = chgr.isSelected(idx);
        int index = (isSelected) ? 1 : 0;
        if (chgr.getChoiceType() == Choice.EXCLUSIVE || chgr.getChoiceType() == Choice.POPUP) {
            index += 2;
        }
        return fmImList.elementAt(index);
    }
}