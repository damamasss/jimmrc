package jimm.ui;

import jimm.*;
import jimm.forms.StatusesForm;
import jimm.comm.StringConvertor;
import jimm.util.ResourceBundle;
import jimm.util.Device;

import javax.microedition.lcdui.*;
import java.util.Vector;

import DrawControls.ImageList;
import DrawControls.Icon;

public final class InputTextBox implements InputListener, CommandListener {

    final public static byte EDITOR_MODE_MESSAGE = 0;
    final public static byte EDITOR_MODE_AUTH_MESSAGE = (byte) 1;
    final public static byte EDITOR_MODE_STATUS_MESSAGE = (byte) 2;
    final public static byte EDITOR_MODE_RENAME = (byte) 3;
    final public static byte EDITOR_MODE_ALT = (byte) 4;

    private static final ImageList inpicons = ImageList.loadFull("inpicons.png");

    public Command cmdSendBox;
    public Command cmdCancelBox;
    private TextBox textBox;
    private static String textEx;
    private Vector pages = new Vector();
    private Vector commands = new Vector();
    private byte textMessCurMode;
    private int currentPage = 0;

    public InputTextBox(byte mode, String caption, String initText) {
        showTextBox(mode, caption, initText);
    }

    public void activate() {
        Jimm.setDisplay(getTextBox());
    }

    public void showTextBox(byte mode, String caption, String initText) {
        int size = 0;
        boolean flag = false;
        //int maxSize = Options.getInt(Options.OPTION_MAX_TEXT_SIZE);
        int maxSize = 2048;
        textMessCurMode = mode;
        switch (mode) {
            case EDITOR_MODE_MESSAGE:
            case EDITOR_MODE_ALT:
                if (/*mode != textMessCurMode || */getTextBox().size() == 0) {
                    try {
                        getTextBox().setMaxSize(maxSize);
                    } catch (Exception ignored) {
                    }
                }
                getTextBox().setTitle(initTitle(caption));
                if (textEx != null) {
                    getTextBox().insert(initString(textEx, textBox.getMaxSize()), textBox.getCaretPosition());
                }
                String str = StringConvertor.getString(textBox.getString());
                flag = (mode != textMessCurMode || str.length() == 0);
                break;
            case EDITOR_MODE_AUTH_MESSAGE:
                size = 500;
                break;
            case EDITOR_MODE_STATUS_MESSAGE:
                size = 255;
                break;
            case EDITOR_MODE_RENAME:
                size = 64;
                break;
        }
        if (mode != EDITOR_MODE_MESSAGE && mode != EDITOR_MODE_ALT) {
            try {
                getTextBox().setMaxSize(size);
            } catch (Exception ignored) {
            }
            getTextBox().setTitle(Options.getBoolean(Options.OPTION_EMPTY_TITLE) ? null : caption);
            getTextBox().setString(initText);
        }
//#sijapp cond.if target is "MIDP2" | target is "MOTOROLA" | target is "SIEMENS2"#
        try {
            int capsMode = TextField.ANY;
            capsMode |= TextField.INITIAL_CAPS_SENTENCE;
            getTextBox().setConstraints(capsMode);
        } catch (Exception ignored) {
        }
//#sijapp cond.end#
        if (flag) {
            initText = initString(initText, getTextBox().getMaxSize());
            getTextBox().insert(initText, getTextBox().getCaretPosition());
        }
        addMenu();
    }

    public static boolean isShownEx() {
        return (Jimm.getCurrentDisplay() instanceof TextBox);
    }

    private String initString(String initText, int maxSize) {
        if (initText == null) {
            initText = getText();
            pages.removeAllElements();
            currentPage = 0;
        }
        if (initText != null) {
            int length = initText.length();
            if (length > maxSize) {
                String text = null;
                for (int i = 0; i < length; i += maxSize) {
                    text = initText.substring(i, Math.min(i + maxSize, length));
                    getPages().setElementAt(text, currentPage);
                    if (++currentPage > 8) {
                        --currentPage;
                        break;
                    }
                }
                initText = text;
            }
        }
        return initText;
    }

    private String initTitle(String caption) {
        if (caption == null) {
            return null;
        }
        StringBuffer title = new StringBuffer();
        if (currentPage > 0) {
            title.append('(').append(currentPage + 1).append("/9)").append(caption);
        } else {
            title.append(caption);
        }
        return title.toString();
    }

    public TextBox getTextBox() {
        if (textBox == null) {
            textBox = new TextBox(null, null, 450, TextField.ANY);
            removeCommands();
            addCommands();
            System.gc();
            textBox.setCommandListener(this);
        }
        return textBox;
    }

    private Vector getPages() {
        if (pages.size() == 0) {
            pages.setSize(9);
        }
        return pages;
    }

    public void removeCommands() {
        if (cmdSendBox != null)
            textBox.removeCommand(cmdSendBox);
        if (cmdCancelBox != null)
            textBox.removeCommand(cmdCancelBox);
        textBox.removeCommand(JimmUI.cmdInsertEmo);
        textBox.removeCommand(JimmUI.cmdMenu);
        textBox.removeCommand(JimmUI.cmdOk);
        textBox.removeCommand(JimmUI.cmdCancel);
    }

    public void addCommands() {
        if (textMessCurMode == EDITOR_MODE_AUTH_MESSAGE || textMessCurMode == EDITOR_MODE_MESSAGE) {
            byte cancelType = (byte) (Jimm.is_smart_SE() ? Command.CANCEL : Command.BACK);
            boolean flag = Options.getBoolean(Options.OPTION_SWAP_SEND);
            if (flag && (cmdSendBox == null || cmdSendBox.getCommandType() == Command.OK)) {
                cmdSendBox = new Command(ResourceBundle.getString("send"), cancelType, 1);
                cmdCancelBox = new Command(ResourceBundle.getString("cancel"), Command.ITEM, 3);
            } else if (!flag && (cmdSendBox == null || cmdSendBox.getCommandType() != Command.OK)) {
                cmdSendBox = new Command(ResourceBundle.getString("send"), Command.OK, 1);
                cmdCancelBox = new Command(ResourceBundle.getString("cancel"), cancelType, 3);
            }
        }
        if ((textMessCurMode == EDITOR_MODE_STATUS_MESSAGE) || (textMessCurMode == EDITOR_MODE_RENAME) || (textMessCurMode == EDITOR_MODE_ALT)) {
            textBox.addCommand(JimmUI.cmdOk);
            textBox.addCommand(JimmUI.cmdCancel);
        } else {
            textBox.addCommand(cmdSendBox);
            textBox.addCommand(cmdCancelBox);
        }
        textBox.addCommand(JimmUI.cmdInsertEmo);
        textBox.addCommand(JimmUI.cmdMenu);
    }

    private void addMenu() {
//        for (int i = 0; i < commands.size(); i++) {    
//            textBox.removeCommand((Command)commands.elementAt(i));
//        }
        commands.removeAllElements();
        commands.addElement(JimmUI.cmdClear);
        //commands.addElement(JimmUI.detransliterateCommand); // todo skip
        commands.addElement(JimmUI.cmdInsTemplate);
        //commands.addElement(JimmUI.transliterateCommand); // todo skip
        if (textMessCurMode == EDITOR_MODE_MESSAGE || textMessCurMode == EDITOR_MODE_ALT) {
//            if (textMessCurMode == EDITOR_MODE_MESSAGE) {
            //commands.addElement(JimmUI.cmdSendAll); // todo skip
//            }
            if (currentPage < 8) {
                commands.addElement(JimmUI.cmdNext);
            }
            if (currentPage > 0) {
                commands.addElement(JimmUI.cmdPrev);
            }
        }
        if (!JimmUI.clipBoardIsEmpty()) {
            commands.addElement(JimmUI.cmdQuote);
            commands.addElement(JimmUI.cmdPaste);
        }
        int i1 = commands.size();
        boolean flag;
        do {
            flag = false;
            for (int i = 0; i < i1 - 1; i++) {
                Command command1 = (Command) commands.elementAt(i + 1);
                Command command2 = (Command) commands.elementAt(i);
                if (command1.getPriority() < command2.getPriority()) {
                    commands.setElementAt(command1, i);
                    commands.setElementAt(command2, i + 1);
                    flag |= true;
                }
            }
        } while (flag);
//        for (int i = 0; i < commands.size(); i++) {
//            textBox.addCommand((Command)commands.elementAt(i));
//        }
//        Vector pairs;

    }

    private String getPage(int page) {
        Object o = getPages().elementAt(page);
        return (o == null) ? "" : (String) o;
    }

    private String getText() {
        StringBuffer text = new StringBuffer();
        String str;
        Object o;
        for (int i = 0; i < pages.size(); i++) {
            o = pages.elementAt(i);
            if (o != null) {
                str = (String) o;
                if (str.length() > 0) {
                    text.append(str);
                }
            }
        }
        return text.toString();
    }

    private void checkBack(Command c) {
        if ((c == JimmUI.cmdCancel || c == cmdCancelBox) && textMessCurMode == EDITOR_MODE_MESSAGE) {
            textEx = getTextBox().getString();
        } else {
            textEx = null;
        }
    }

    public void commandAction(Command c, Displayable d) {
        checkBack(c);
        if (c == cmdCancelBox) {
            boolean message = (EDITOR_MODE_MESSAGE == textMessCurMode);
            if (message) {
                String str = StringConvertor.getString(textBox.getString());
                if (str.length() != 0) {
                    getPages().setElementAt(textBox.getString(), currentPage);
                }
            }
            //textBox.setString(null);
            JimmUI.hideTextBox(message);
            //textBox = null;
        } else if (c == JimmUI.transliterateCommand) {
            textBox.setString((new StringConvertor()).transliterate(textBox.getString()));
        } else if (c == JimmUI.detransliterateCommand) {
            textBox.setString((new StringConvertor()).detransliterate(textBox.getString()));
        } else if (c == cmdSendBox) {
            switch (textMessCurMode) {
                case EDITOR_MODE_MESSAGE:// case EDITOR_MODE_ALT:
                    getPages().setElementAt(textBox.getString(), currentPage);
                    try {
                        JimmUI.sendMessage(getText());
                    } catch (Exception e) {
                        Jimm.back();
                    }
                    //textBox.setString(null);
                    pages.removeAllElements();
                    currentPage = 0;
                    //textBox = null;
                    break;
                case EDITOR_MODE_AUTH_MESSAGE:
                    String textBoxText = textBox.getString();
                    String reasonText = ((textBoxText == null) || (textBoxText.length() < 1)) ? "" : textBoxText;
                    //textBox.setString(null);
                    JimmUI.authOperation(reasonText);
                    //textBox = null;
                    break;
            }
//#sijapp cond.if modules_SMILES is "true" #
        } else if (c == JimmUI.cmdInsertEmo) {
            try {
                Emotions.selectEmotion(textBox, textBox);
            } catch (Exception e) {
//#sijapp cond.if modules_DEBUGLOG is "true"#
//                DebugLog.addText("Smiles: " + e.toString() + " " + e.getClass().getName());
//#sijapp cond.end#
            }
//#sijapp cond.end#
        } else if (c == JimmUI.cmdInsTemplate) {
            (new Templates()).selectTemplate(textBox, textBox);
        } else if (c == JimmUI.cmdClear) {
            textBox.setString("");
        } else if ((c == JimmUI.cmdQuote) || (c == JimmUI.cmdPaste)) {
            String text = JimmUI.getClipBoardText(c == JimmUI.cmdQuote);
            textBox.insert(initString(text, textBox.getMaxSize()), textBox.getCaretPosition());
        } else if (c == JimmUI.cmdSendAll) {
            getPages().setElementAt(textBox.getString(), currentPage);
            JimmUI.showMassiveSendList(getText());
            pages.removeAllElements();
        } else if (c == JimmUI.cmdNext) {
            if (currentPage < 8) {
                getPages().setElementAt(textBox.getString(), currentPage);
                textBox.setString("");
                textBox.setString(getPage(++currentPage));
                if (textBox.getTitle() != null && currentPage > 0 && textBox.getTitle().length() > 5) {
                    textBox.setTitle(initTitle(textBox.getTitle().substring(5)));
                } else {
                    textBox.setTitle(initTitle(textBox.getTitle()));
                }
                addMenu();
            }
        } else if (c == JimmUI.cmdPrev) {
            if (currentPage > 0) {
                getPages().setElementAt(textBox.getString(), currentPage);
                textBox.setString("");
                textBox.setString(getPage(--currentPage));
                if (textBox.getTitle() != null && textBox.getTitle().length() > 5) {
                    textBox.setTitle(initTitle(textBox.getTitle().substring(5)));
                } else {
                    textBox.setTitle(initTitle(textBox.getTitle()));
                }
                addMenu();
            }
        } else if (c == JimmUI.cmdOk) {
            if (textMessCurMode == EDITOR_MODE_STATUS_MESSAGE) {
                StatusesForm stsForm = (StatusesForm) Jimm.getPrevScreen();
                Jimm.getCurrentProfile().setString((byte) stsForm.getMsgIdx(), textBox.getString());
                Jimm.getCurrentProfile().saveOptions();
                stsForm.setStatus();
                //textBox.setString(null);
                Jimm.back();
                //textBox = null;
            } else if (textMessCurMode == EDITOR_MODE_RENAME) {
                JimmUI.menuRenameSelected(textBox.getString());
                //textBox.setString(null);
                //textBox = null;
//#sijapp cond.if modules_CLASSIC_CHAT is "true"#
            } else if (textMessCurMode == EDITOR_MODE_ALT) {
                getPages().setElementAt(textBox.getString(), currentPage);
                jimm.chat.ChatTextList.textLine.setString(getText());
                pages.removeAllElements();
                currentPage = 0;
                //textBox.setString(null);
                Jimm.back();
                //textBox = null;
//#sijapp cond.end#
            }
        } else if (c == JimmUI.cmdCancel) {
            Jimm.back();
            //textBox.setString(null);
            //textBox = null;
            if (textMessCurMode == EDITOR_MODE_ALT) {
                pages.removeAllElements();
                currentPage = 0;
            }
        } else if (c == JimmUI.cmdMenu) {
            int comm = commands.size();
            Command temp;
            String[] names = new String[comm];
            Icon icons[] = null;
            if (inpicons.size() != 0) {
                icons = new Icon[comm];
            }
            boolean flag = comm < 11;
            for (int i1 = comm - 1; i1 >= 0; i1--) {
                temp = (Command) commands.elementAt(i1);
                names[i1] = (flag ? ((i1 + 1) % 10 + ". " + temp.getLabel()) : temp.getLabel());
                icons[i1] =
                        temp != JimmUI.detransliterateCommand ?
                                temp != JimmUI.transliterateCommand ?
                                        temp != JimmUI.cmdInsTemplate ?
                                                temp != JimmUI.cmdNext ?
                                                        temp != JimmUI.cmdPrev ?
                                                                temp != JimmUI.cmdSendAll ?
                                                                        temp != JimmUI.cmdQuote ?
                                                                                temp != JimmUI.cmdPaste ?
                                                                                        temp != JimmUI.cmdClear ?
                                                                                                null :
                                                                                                inpicons.elementAt(8) :
                                                                                        inpicons.elementAt(7) :
                                                                                inpicons.elementAt(6) :
                                                                        inpicons.elementAt(5) :
                                                                inpicons.elementAt(4) :
                                                        inpicons.elementAt(3) :
                                                inpicons.elementAt(2) :
                                        inpicons.elementAt(1) :
                                inpicons.elementAt(0);
            }
            JimmUI.activateMessMenu(JimmUI.cmdMenu.getLabel(), names, icons, this, textBox);
        }
        System.gc();
    }

    public void action(int i, Object back) {
        Command command = (Command) commands.elementAt(i);
        if (command != JimmUI.cmdInsTemplate && command != JimmUI.cmdInsertEmo && command != JimmUI.cmdSendAll) {
            Jimm.setDisplay(back);
        }
        commandAction(command, textBox);
    }
}