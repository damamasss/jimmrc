/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 ********************************************************************************
 File: src/jimm/FileTransfer.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Andreas Rossbacher, Dmitry Tunin
 *******************************************************************************/

//#sijapp cond.if modules_FILES="true"#
package jimm.files;

import DrawControls.*;
import jimm.*;
import jimm.comm.*;
import jimm.forms.FormEx;
import jimm.ui.LineChoiseBoolean;
import jimm.util.ResourceBundle;

// #sijapp cond.if target is "MIDP2" | target is "MOTOROLA"#
//#sijapp cond.elseif target is "SIEMENS2"#
import com.siemens.mp.io.file.FileConnection;
import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
//#sijapp cond.if target isnot "MOTOROLA"#
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;
//#sijapp cond.end#
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileTransfer implements CommandListener, FileBrowserListener, Runnable {
    public static final int FT_TYPE_FILE_BY_NAME = 1;
    // #sijapp cond.if target isnot "MOTOROLA" #
    public static final int FT_TYPE_CAMERA_SNAPSHOT = 2;
    // #sijapp cond.end #
    public static final int FT_TYPE_WEB = 3;

    private String shortFileName, description;

    // #sijapp cond.if target isnot "MOTOROLA" #
    private ViewFinder vf;
    // #sijapp cond.end #
    private FormEx name_Desc;
    private InputStream fis;
    private int fsize;
    private TextField fileNameField;
    private TextField descriptionField;
    //private ChoiceGroupEx ft_type_web;
    private LineChoiseBoolean ft_type_web;
    private int type;
    private ContactItem cItem;
    private boolean showFiles = true;

    public FileTransfer(int ftType, ContactItem _cItem) {
        type = ftType;
        cItem = _cItem;
    }

    public FileTransfer(int ftType, ContactItem _cItem, boolean sf) {
        this(ftType, _cItem);
        showFiles = sf;
    }

    public ContactItem getCItem() {
        return (cItem);
    }

    public void setData(InputStream is, int size) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ignored) {
            }
            fis = null;
        }
        fis = is;
        fsize = size;
    }

    public void startFT(String dir) {
// #sijapp cond.if target isnot "MOTOROLA" #
        if (type == FileTransfer.FT_TYPE_CAMERA_SNAPSHOT) {
            if (System.getProperty("video.snapshot.encodings") == null) {
                JimmException.handleException(new JimmException(185, 0, true));
            } else {
                vf = new ViewFinder();
                //Display.getDisplay(Jimm.jimm).setCurrent(vf);
                Jimm.setDisplay(vf);
                vf.start();
            }
        } else if (type == FileTransfer.FT_TYPE_FILE_BY_NAME || type == FT_TYPE_WEB)
// #sijapp cond.end #
        {
            FileBrowser fb = new FileBrowser();
            fb.setListener(this);
            fb.setParameters(!showFiles);
            if (dir == null) {
                fb.activate();
            } else {
                fb.activate(dir);
            }
        }
    }

    public void startFT() {
        startFT(null);
    }

    public void onFileSelect(String fileName) {
        try {
            InputStream fis;
            int size;
            FileSystem file = FileSystem.getInstance();
            file.readFile(fileName);
            fis = file.openInputStream();

            if (getCItem() == null) {
                Options.setString(Options.OPTION_SKIN_PATH, fileName);
                try {
                    Options.getOptionsForm().skin = Image.createImage(fis);
                    fis.close();
                } catch (Exception ignored) {
                } catch (OutOfMemoryError ignored) {
                }
                free();
                Options.getOptionsForm().retutnFromFiles();
                return;
            }

            size = (int) file.fileSize();
            setData(fis, size);
            //#sijapp cond.if target is "SIEMENS2"| target is "MIDP2"#
            askForNameDesc(file.getName(), "");
            //#sijapp cond.else#
            askForNameDesc(fileName, "");
            //#sijapp cond.end#
        } catch (Exception e) {
            e.printStackTrace();
            JimmException.handleException(new JimmException(191, 0, true));
        }
    }

    //
    public void onDirectorySelect(String s0) {
        if (!showFiles) {
            Options.getOptionsForm().iconsPrefix = s0;
            Options.getOptionsForm().retutnFromFiles();
        }
    }

    public void initFT(String filename, String description) {
        cItem.getProfile().addAction(ResourceBundle.getString("init_ft"), 0, this, cItem);
        NativeCanvas.getLPCanvas().show();
        if (type == FT_TYPE_WEB) {
            String[] fnItems = Util.explode(filename, '/');
            shortFileName = (fnItems.length == 0) ? filename : fnItems[fnItems.length - 1];
            this.description = description;
            (new Thread(this)).start();
        } else {
            // #sijapp cond.if target isnot "MOTOROLA" #
            vf = null;
            // #sijapp cond.end #
            FileTransferMessage ftm = new FileTransferMessage(cItem.getIcq().getUin(), cItem, Message.MESSAGE_TYPE_EXTENDED, filename, description, fis, fsize);
            SendMessageAction act = new SendMessageAction(ftm);
            try {
                cItem.getIcq().requestAction(act);
            } catch (JimmException e) {
                JimmException.handleException(e);
                if (e.isCritical()) {
                    //return;
                }
            }
        }
    }

    public void askForNameDesc(String filename, String description) {
        name_Desc = new FormEx(ResourceBundle.getString("name_desc"), JimmUI.cmdOk, JimmUI.cmdBack);
        fileNameField = new TextField(ResourceBundle.getString("filename"), filename, 255, TextField.ANY);
        descriptionField = new TextField(ResourceBundle.getString("description"), description, 255, TextField.ANY);
        //ft_type_web = new ChoiceGroupEx(null, Choice.MULTIPLE);
        //ft_type_web.append(ResourceBundle.getString("through_web"), null);
        ft_type_web = new LineChoiseBoolean(ResourceBundle.getString("through_web"), true);

        name_Desc.append(fileNameField);
        name_Desc.append(descriptionField);
        name_Desc.append(ft_type_web);
        name_Desc.append(ResourceBundle.getString("size") + ": " + String.valueOf(fsize / 1024) + " kb");
        name_Desc.setCommandListener(this);
        Jimm.setDisplay(name_Desc);
    }

    // Command listener
    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdOk) {
            if (JimmUI.isControlActive(name_Desc)) {
                Jimm.getContactList().activate();
                if (ft_type_web.getBooolean()) {
                    type = FT_TYPE_WEB;
                }
                initFT(fileNameField.getString(), descriptionField.getString());
            }
        } else if (c == JimmUI.cmdBack) {
            free();
            getCItem().activate();
        } else if (c == JimmUI.cmdCancel) {
            if (cItem != null) {
                cItem.getProfile().actionCompleted(cItem);
            }
            free();
        }
    }

    private void free() {
// #sijapp cond.if target isnot "MOTOROLA" #
        vf = null;
// #sijapp cond.end #
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException ignored) {
            }
            fis = null;
        }
        name_Desc = null;
        fileNameField = null;
        System.gc();
    }

    public void run() {
        InputStream is;
        OutputStream os;
        HttpConnection sc;

        //String host = "filetransfer.jimm.org";
        //String url = "http://"+host+"/__receive_file.php";

        String host = "ft.fay.by:89";
        String url = "http://" + host + "/__receive_file.php?au=igrym&mod=best";
        //String s1 = java.lang.Integer.toString(java.lang.Integer.parseInt(cItem.uinString) ^ 0x39447);
        //String url = "http://" + host + "/__receive_file.php?cli=dichat&hash=" + s1;

        //boolean error = false;

        try {
            sc = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
            sc.setRequestMethod(HttpConnection.POST);

            String boundary = "a9f843c9b8a736e53c40f598d434d283e4d9ff72";

            sc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            os = sc.openOutputStream();

            // Send post header
            StringBuffer buffer2 = new StringBuffer();
            buffer2.append("--").append(boundary).append("\r\n");
            buffer2.append("Content-Disposition: form-data; name=\"jimmfile\"; filename=\"").append(shortFileName).append("\"\r\n");
            buffer2.append("Content-Type: application/octet-stream\r\n");
            buffer2.append("Content-Transfer-Encoding: binary\r\n");
            buffer2.append("\r\n");
            os.write(Util.stringToByteArray(buffer2.toString(), true));

            // Send file data and show progress
            byte[] buffer = new byte[1024];
            int counter = fsize;
            int read;
            int percent;
            do {
                read = fis.read(buffer);
                os.write(buffer, 0, read);
                //os.flush();
                counter -= read;
                if (fsize != 0) {
                    percent = 100 * (fsize - counter) / fsize;
                    cItem.getProfile().addAction(ResourceBundle.getString("filetransfer")/*+" "+percent+"%/"+fsize/1024+"KB"*/, percent, this, cItem, true);
                }
            } while (counter > 0);
            cItem.getProfile().addAction(ResourceBundle.getString("filetransfer"), 0, cItem);

            Traffic.addTrafficOut(fsize);

            StringBuffer buffer3 = new StringBuffer();
            buffer3.append("\r\n--").append(boundary).append("--\r\n");
            os.write(Util.stringToByteArray(buffer3.toString(), true));
            os.flush();

            int respCode = sc.getResponseCode();
            if (respCode != HttpConnection.HTTP_OK) {
                throw new JimmException(201, respCode, true);
            }

            is = sc.openInputStream();

            StringBuffer response = new StringBuffer();
            while (true) {
                read = is.read();
                if (read == -1) {
                    break;
                }
                response.append((char) (read & 0xFF));
            }

            String respString = response.toString();

            int dataPos = respString.indexOf("http://");
            if (dataPos == -1) {
                throw new JimmException(200, 0, true);
            }

            respString = Util.replaceStr(respString, "\r\n", "");
            respString = Util.replaceStr(respString, "\r", "");
            respString = Util.replaceStr(respString, "\n", "");

            //if(((javax.microedition.io.HttpConnection)javax.microedition.io.Connector.open("http://mrdark.ru/di/ft.php")).getResponseCode() == 200)
            //    respString = ("http://mrdark.ru/di/ft.php?file=" + respString).replace('&', '+');
            // Качать файл через промежуточную страницу

//			System.out.println(respString);

            // Close all http connection headers
            os.close();
            is.close();
            sc.close();

            // Send info about file
            StringBuffer messText = new StringBuffer();
            messText.append("Filename: ").append(shortFileName).append("\n");
            messText.append("Filesize: ").append(fsize / 1024).append("KB\n");
            messText.append("Link: ").append(respString).append("&lang=ru");
            if ((description != null) && (description.length() > 0)) {
                messText.append("\nDescription: ").append(description);
            }
            JimmUI.sendMessage(messText.toString(), cItem);
        } catch (JimmException je) {
            JimmException.handleException(je);
            //error = true;
        } catch (IOException ioe) {
            JimmException.handleException(new JimmException(201, 0, true));
            //error = true;
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            JimmException.handleException(new JimmException(201, 1, true));
            //error = true;
//#sijapp cond.if modules_DEBUGLOG is "true"#
//#			jimm.DebugLog.addText("Error in sending filer through web: "+e.toString());
//#sijapp cond.end#
        }
        free();
        cItem.getProfile().actionCompleted(cItem);
    }

    //#sijapp cond.if target isnot "MOTOROLA" #
    private class ViewFinder extends VirtualList implements CommandListener {

        private Player p = null;
        private VideoControl vc = null;
        private boolean active = false;
        private boolean viewfinder = true;
        private Image img;
        private byte[] data;
        private int sourceWidth = 0;
        private int sourceHeight = 0;
        //private String encoding = null;
        private String extension = "jpeg";
        //private TextList vl;

        public ViewFinder() {
            super(null);
            addCommandEx(JimmUI.cmdOk, VirtualList.MENU_TYPE_LEFT_BAR);
            addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
            setColorScheme();
            //setFullScreenMode(true);
            //addCommand(JimmUI.cmdBack);
            //addCommand(JimmUI.cmdOk);
            setCommandListener(this);
            //vl.setCommandListener(this);
        }

        protected int getSize() {
            return 0;
        }

        protected void get(int index, ListItem item) {
        }

        private void reset() {
            img = null;
            if (vc != null) {
                vc.setVisible(false);
                vc = null;
            }
            if (p != null) {
                try {
                    if (p.getState() == Player.STARTED)
                        p.stop();
                    p.deallocate();
                    p.close();
                } catch (Exception ignored) {
                }
                p = null;
            }
            System.gc();
        }

        public void paintAllOnGraphics(Graphics graphics, int mode, int curX, int curY) {
            if (mode == DMS_DRAW) {
                int menuBarHeight = getMenuBarHeight();
                int bottom = getHeightInternal();
                graphics.setColor(0x000000);
                graphics.fillRect(0, 0, getWidth(), getHeight());
                if (!viewfinder) {
                    if (img != null) {
                        graphics.drawImage(img, getWidth() / 2, getHeight() / 2, Graphics.VCENTER | Graphics.HCENTER);
                    }
                    drawCaption(graphics, mode, curX, curY);
                }
                //graphics.setColor(bkgrndColor);
                //graphics.fillRect(0, NativeCanvas.getHeightEx() - menuBarHeight, NativeCanvas.getWidthEx(), menuBarHeight);
                drawMenuBar(graphics, menuBarHeight, bottom, mode, -1, -1);
                return;
            }
            super.paintAllOnGraphics(graphics, mode, curX, curY);
        }

        // start the viewfinder
        public synchronized void start() {
            reset();
            if (!active) {
                try {
//#sijapp cond.if modules_DEBUGLOG is "true" #
//				String contentTypes[]=Manager.getSupportedContentTypes("capture");
//				DebugLog.addText(">>" + "capture" + "<<");
//				for (int i = 0; i < contentTypes.length; i++)
//					DebugLog.addText(contentTypes[i]);
//#sijapp cond.end#
                    String cam_dev = "capture://image";
                    try {
                        p = Manager.createPlayer(cam_dev);
                    } catch (Exception mxe) {
                        cam_dev = "capture://video";
                        p = Manager.createPlayer(cam_dev);
                    }
                    p.realize();
/*
                    int curRes = Options.getInt(Options.OPTION_CAMERA_RES);
                    int curEnc = Options.getInt(Options.OPTION_CAMERA_ENCODING);
                    //encoding = null;

                    int key1 = 0;
                    int key2 = 0;
                    String[] imageTypes = Util.explode(System.getProperty("video.snapshot.encodings"), ' ');
                    String tmp = "";
                    String[] params;
                    String[] values;
                    String width;
                    String height;
                    for (int i = 0; i < imageTypes.length; i++) {
                        params = Util.explode(imageTypes[i], '&');
                        width = null;
                        height = null;
                        for (int j = 0; j < params.length; j++) {
                            values = Util.explode(params[j], '=');
                            if (values[0].equals("encoding")) {
                                if (strCountOccur(tmp, values[1]) == 0) {
                                    if (key1 == curEnc) {
                                        //encoding = "encoding=" + values[1];
                                        extension = values[1];
                                    }
                                    tmp += values[1];
                                    key1++;
                                }
                            } else if (values[0].equals("width")) {
                                width = values[1];
                            } else if (values[0].equals("height")) {
                                height = values[1];
                            }
                        }
                        if ((width != null) && (height != null)) {
                            if (key2 == curRes) {
                                //encoding += "&" + "width=" + width + "&" + "height=" + height;
                                break;
                            }
                            key2++;
                        }
                    }*/
                    // Get the video control
                    vc = (VideoControl) p.getControl("VideoControl");
                    if (vc != null) {
                        vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, NativeCanvas.getInst());
                        int canvasWidth = getWidth();
                        int canvasHeight = getHeight() - getMenuBarHeight();
                        try {
                            vc.setDisplayLocation(0, 0);
                            vc.setDisplaySize(canvasWidth, canvasHeight);
                        } catch (MediaException me) {
                            try {
                                vc.setDisplayFullScreen(true);
                            } catch (MediaException ignored) {
                            }
                        }

                        vc.setVisible(true);
                        p.start();
                        active = true;
                        setCaption(null);
                    } else {
                        JimmException.handleException(new JimmException(180, 0, true));
                    }
                } catch (IOException ioe) {
                    reset();
                    JimmException.handleException(new JimmException(181, 0, true));
                } catch (MediaException me) {
                    reset();
                    JimmException.handleException(new JimmException(181, 1, true)); // hmmm..
                } catch (SecurityException se) {
                    reset();
                    JimmException.handleException(new JimmException(181, 2, true));
                }
            }
        }

        private byte[] getSnapshot(String type) {
            byte[] data;
            try {
                data = vc.getSnapshot(type);
            } catch (Exception e) {
                return null;
            }
            return data;
        }

        // take a snapshot form the viewfinder
        public void takeSnapshot() {
            if (p != null) {
                data = getSnapshot("encoding=" + extension + "&width=" + 320 + "&height=" + 240);
                if (data == null) {
                    data = getSnapshot("JPEG");
                }
                if (data == null) {
                    data = getSnapshot(null);
                }
                if (data == null) {
                    JimmException.handleException(new JimmException(183, 0, true));
                }
                viewfinder = false;
                stop();
                if (data == null) {
                    return;
                }
                img = Image.createImage(data, 0, data.length);
                if (img != null) {
                    sourceWidth = img.getWidth();
                    sourceHeight = img.getHeight();
                }
                setCaption(ResourceBundle.getString("send_img") + "? " + sourceWidth + "x" + sourceHeight);
                img = createThumbnail(img, getWidth(), getHeight());
                try {
                    vc.setVisible(false);
                } catch (Exception ignored) {
                }
                repaint();
            }
        }

        // stop the viewfinder
        public synchronized void stop() {
            if (active) {
                try {
                    vc.setVisible(false);
                    p.stop();
                } catch (Exception e) {
                    reset();
                }
                active = false;
            }
        }

        // action listener
        public void commandAction(Command c, Displayable d) {
            if (c.equals(JimmUI.cmdOk)) {
                if (!viewfinder) {
                    stop();
                    reset();
                    FileTransfer.this.setData(new ByteArrayInputStream(data), data.length);
                    String name = "jimm_cam_" + toWindowsFormat(DateAndTime.getDateString(false, false, DateAndTime.createCurrentDate(true))) + "_" + Util.getCounter() + "." + extension;
                    FileTransfer.this.askForNameDesc(name, "");
                } else {
                    try{
                        takeSnapshot();
                    } catch (OutOfMemoryError oome) {
                        System.gc();
                        stop();
                        reset();
                        Jimm.back();
                        FileTransfer.this.vf = null;
                        Alert alert = new Alert(null, oome.toString(), null, null);                        
                        Jimm.setDisplay(alert);
                    }
                }
            } else if (c.equals(JimmUI.cmdBack)) {
                if (!viewfinder) {
                    viewfinder = true;
                    active = false;
                    start();
                } else {
                    stop();
                    reset();
                    Jimm.back();
                    FileTransfer.this.vf = null;
                }
            }
        }

        private Image createThumbnail(Image image, int width, int height) {
            int sourceWidth = image.getWidth();
            int sourceHeight = image.getHeight();
            if ((height == 0) && (width != 0)) {
                height = width * sourceHeight / sourceWidth;
            } else if ((width == 0) && (height != 0)) {
                width = height * sourceWidth / sourceHeight;
            } else if (sourceHeight >= sourceWidth) {
                width = height * sourceWidth / sourceHeight;
            } else {
                height = width * sourceHeight / sourceWidth;
            }
            Image thumb = Image.createImage(width, height);
            Graphics g = thumb.getGraphics();
            int dx;
            int dy;
            for (int y = 0; y < height; y++) {
                dy = y * sourceHeight / height;
                for (int x = 0; x < width; x++) {
                    g.setClip(x, y, 1, 1);
                    dx = x * sourceWidth / width;
                    g.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
                }
            }
            return Image.createImage(thumb);
        }

//        private int strCountOccur(String source, String sub) {
//            int index = 0;
//            int index2 = 0;
//            int res = 0;
//            if ((source != null) && (sub != null)) {
//                while ((index = source.indexOf(sub, index2)) != -1) {
//                    index2 = index + sub.length();
//                    res++;
//                }
//            }
//            return res;
//        }

        private String toWindowsFormat(String src) {
            StringBuffer result = new StringBuffer();
            char ch;
            for (int i = 0; i < src.length(); i++) {
                ch = src.charAt(i);
                result.append((ch == ':') ? '-' : ch);
            }
            return result.toString();
        }
    }
//#sijapp cond.end #
}
//#sijapp cond.end#