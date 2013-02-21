///*******************************************************************************
// Jimm - Mobile Messaging - J2ME ICQ clone
// Copyright (C) 2003-05  Jimm Project
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
// ********************************************************************************
// File: src/jimm/ChatItem.java
// Version: ###VERSION###  Date: ###DATE###
// Author(s): Rishat Shamsutdinov
// *******************************************************************************/
//
//package jimm.chat;
//
//import jimm.*;
//import jimm.util.ResourceBundle;
//import jimm.comm.StringConvertor;
//import jimm.ui.InputTextBox;
//
//import javax.microedition.lcdui.*;
//
//import DrawControls.CanvasEx;
//
////#sijapp cond.if modules_CLASSIC_CHAT is "true"#
//class ChatItem extends CustomItem implements ItemStateListener, ItemCommandListener {
//
//    private Command cmdSendBox;
//    private Command cmdQuote;
//    private Command cmdPaste;
//    private Command cmdInsertEmo;
//    private Command cmdInsTemplate;
//    private Command cmdClearText;
//    private Command cmdSendAll;
//
//    private int height, width;
//    private ChatTextList chat;
//    private Form form;
//    private TextField textField;
//
//    ChatItem(String name) {
//        super("");
//        form = new Form(name);
//        int capFlag = TextField.ANY;
//        capFlag |= TextField.INITIAL_CAPS_SENTENCE;
//        textField = new TextField(null, null, Options.getInt(Options.OPTION_MAX_TEXT_SIZE), capFlag);
//        if (Options.getInt(Options.OPTION_FREEZE_CLCHAT_SIZE) > 0) {
//            textField.setPreferredSize(1, Options.getInt(Options.OPTION_FREEZE_CLCHAT_SIZE));
//        }
//        this.height = form.getHeight() - textField.getMinimumHeight();
//        this.width = form.getWidth();
//        addCommands();
//        addCommand(JimmUI.cmdMenu);
//        addCommand(ChatTextList.cmdCloseChat);
//        if (Options.getInt(Options.OPTION_LINE_POSITION) == 0) {
//            form.append(textField);
//            form.append(this);
//        } else {
//            form.append(this);
//            form.append(textField);
//        }
//        textField.setItemCommandListener(this);
//        this.setItemCommandListener(this);
//        form.setItemStateListener(this);
//    }
//
//    void setChat(ChatTextList chat) {
//        this.chat = chat;
//    }
//
//    protected int getMinContentHeight() {
//        return height;
//    }
//
//    protected int getMinContentWidth() {
//        return width;
//    }
//
//    protected int getPrefContentHeight(int width) {
//        return height;
//    }
//
//    protected int getPrefContentWidth(int height) {
//        return width;
//    }
//
//    protected void paint(Graphics g, int w, int h) {
//        if (chat == null) {
//            return;
//        }
//        chat.setForcedSize(w, h);
//        chat.paint(g);
//    }
//
//    protected void keyPressed(int keyCode) {
//        chat.doKeyreaction(keyCode, CanvasEx.KEY_PRESSED);
//        repaint();
//    }
//
//    protected void keyRepeated(int keyCode) {
//        chat.doKeyreaction(keyCode, CanvasEx.KEY_REPEATED);
//        repaint();
//    }
//
//    protected void keyReleased(int keyCode) {
//        chat.doKeyreaction(keyCode, CanvasEx.KEY_RELEASED);
//        repaint();
//    }
//
//    //#sijapp cond.if target is "MIDP2"#
//    protected void pointerDragged(int x, int y) {
//        chat.pointerDragged(x, y);
//        repaint();
//    }
//
//    protected void pointerPressed(int x, int y) {
//        chat.pointerPressed(x, y);
//        repaint();
//    }
//
//    protected void pointerReleased(int x, int y) {
//        chat.pointerReleased(x, y);
//        repaint();
//    }
////#sijapp cond.end#
//
//    void setHeight(int value) {
//        height = value;
//        invalidate();
//    }
//
//    void updateContents(ChatTextList _chat) {
//        if (chat == _chat) {
//            repaint();
//        }
//    }
//
//    private void addCommands() {
//        textField.addCommand(cmdSendBox = new Command(ResourceBundle.getString("send"), Command.OK, 1));
//        textField.addCommand(JimmUI.transliterateCommand);
//        textField.addCommand(JimmUI.detransliterateCommand);
//        textField.addCommand(cmdSendAll = new Command(ResourceBundle.getString("massive_send"), Command.ITEM, 5));
//        textField.addCommand(cmdClearText = new Command(ResourceBundle.getString("clear"), Command.ITEM, 8));
//        textField.addCommand(cmdQuote = new Command(ResourceBundle.getString("quote"), Command.ITEM, 4));
//        textField.addCommand(cmdPaste = new Command(ResourceBundle.getString("paste"), Command.ITEM, 4));
////#sijapp cond.if modules_SMILES is "true" #
//        textField.addCommand(cmdInsertEmo = new Command(ResourceBundle.getString("insert_emotion"), Command.ITEM, 2));
////#sijapp cond.end#
//        textField.addCommand(cmdInsTemplate = new Command(ResourceBundle.getString("templates"), Command.ITEM, 4));
//    }
//
//    public void commandAction(Command c, Item item) {
//        if (item == this) {
//            if (chat.getContact().uTyping()) {
//                chat.getContact().beginTyping(false);
//            }
//            int cmdType = c.getCommandType();
//            if ((!chat.pressSoft(c)) && (cmdType == Command.BACK || cmdType == Command.CANCEL)) {
//                chat.commandAction(c, null);
//            }
//            repaint();
//        } else if (c == cmdSendBox) {
//            try {
//                JimmUI.sendMessage(textField.getString(), chat.getContact());
//            } catch (Exception ignored) {
//            }
//            setString("");
//            //Jimm.getDisplay().setCurrentItem(this);
//        } else {
//            doAction(c);
//        }
//    }
//
//    private void doAction(Command c) {
//        if (c == JimmUI.transliterateCommand) {
//            textField.setString((new StringConvertor()).transliterate(textField.getString()));
//        } else if (c == JimmUI.detransliterateCommand) {
//            textField.setString((new StringConvertor()).detransliterate(textField.getString()));
//        } else if (c == cmdSendBox) {
//            try {
//                JimmUI.sendMessage(textField.getString(), chat.getContact());
//            } catch (Exception ignored) {
//            }
//            textField.setString("");
//        }
////#sijapp cond.if modules_SMILES is "true" #
//        else if (c == cmdInsertEmo) {
//            Emotions.selectEmotion(textField, form);
//        }
////#sijapp cond.end#
//        else if (c == cmdInsTemplate) {
//            (new Templates()).selectTemplate(textField, form);
//        } else if (c == cmdClearText) {
//            textField.setString("");
//        } else if ((c == cmdQuote) || (c == cmdPaste)) {
//            if (!JimmUI.clipBoardIsEmpty()) {
//                int caretPos = textField.getCaretPosition();
//                String text = JimmUI.getClipBoardText(c == cmdQuote);
//                textField.insert(text, caretPos);
//                textField.insert("", caretPos + text.length());   // T O D O
//            }
//        }
////#sijapp cond.if target is "MIDP2"#
////		else if ((c == InputTextBox.cmdMakeCall) || (c == InputTextBox.cmdSendSms)) {
////			String messText = textField.getString();
////			if ((messText != null) && (messText.length() > 0)) {
////				SendSmsMakeCall.act((c == InputTextBox.cmdMakeCall), messText);
////				textField.setString(new String());
////			}
////		}
////#sijapp cond.end#
//        else if (c == cmdSendAll) {
//            JimmUI.showMassiveSendList(textField.getString());
//        } else if (c == cmdClearText) {
//            textField.setString("");
//        }
//    }
//
//    public void itemStateChanged(Item item) {
//        if (item == textField) {
//            if (!chat.getContact().uTyping()) {
//                chat.getContact().beginTyping(true);
//            }
//            updateChatHeight();
//        } else if (textField.size() > 0) {
//            chat.saveCurrMessage(textField.getString());
//            int height = form.getHeight() - 4;
//            setHeight(height);
//            chat.setForcedSize(getMinContentWidth(), height);
//        }
//    }
//
//    private int lastHeight = -1;
//
//    void updateChatHeight() {
//        int th = textField.getPreferredHeight();
//        int fs = Options.getInt(Options.OPTION_FREEZE_CLCHAT_SIZE);
//        if ((fs > 0) && (fs != th)) {
//            textField.setPreferredSize(1, fs);
//            th = textField.getPreferredHeight();
//        }
//        int height = form.getHeight() - th - 4;
//        if (lastHeight != height) {
//            setHeight(height);
//            chat.setForcedSize(getMinContentWidth(), height);
//            int size = chat.getSize();
//            if (size != 0) {
//                chat.setCurrentItem(size - 1);
//            }
//            lastHeight = height;
//        }
//    }
//
//    boolean isShown(ChatTextList _chat) {
//        return (form.isShown() && chat == _chat);
//    }
//
//    Form getForm() {
//        return form;
//    }
//
//    void setString(String str) {
//        textField.setString(str);
//        chat.saveCurrMessage(textField.getString());
//    }
//
//    void insert(String str, int caretPos) {
//        textField.insert(str, caretPos);
//        chat.saveCurrMessage(textField.getString());
//    }
//
//    int getCaretPosition() {
//        return textField.getCaretPosition();
//    }
//
//    void activateTextField() {
//        Jimm.getDisplay().setCurrentItem(textField);
//    }
//}
//// #sijapp cond.end#