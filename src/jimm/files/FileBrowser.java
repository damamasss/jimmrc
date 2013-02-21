//#sijapp cond.if modules_FILES="true"#
package jimm.files;

import DrawControls.*;
import jimm.*;
import jimm.comm.StringConvertor;
import jimm.ui.Menu;
import jimm.ui.MenuListener;
import jimm.util.ResourceBundle;
import jimm.util.Device;

import javax.microedition.lcdui.*;

import java.util.Hashtable;
import java.io.IOException;


public class FileBrowser extends VirtualTree implements CommandListener, VirtualTreeCommands, VirtualListCommands, MenuListener, Runnable {
    public static final String ROOT_DIRECTORY = "/";
    public static final String PARENT_DIRECTORY = "../";

    private static final boolean TACTION_ACTIVATE = true;
    private static final boolean TACTION_ONCLICK = false;

    public static final Command backCommand = JimmUI.cmdBack;
    public static final Command openCommand = new Command(ResourceBundle.getString("open"), Command.OK, 1);

    private boolean needToSelectDirectory;

    private FileBrowserListener listener;

    private ImageList imageList = ImageList.loadFull("fs.png");

    private Hashtable iformat;

    private String[] items;

    private String currDir;

    private boolean threadAction;

    public FileBrowser() {
        super(null, false);
        setVTCommands(this);
        setVLCommands(this);
        // setImageList(imageList);
        //setFontSize((imageList.getHeight() <= 16) ? VirtualList.SMALL_FONT : VirtualList.MEDIUM_FONT);
        setFontSize(VirtualList.SMALL_FONT);
        //setFontSize(VirtualList.SMALL_FONT);
        setStepSize(-getFontHeight() / 2);
        setCapImage(imageList.elementAt(0));
        setColorScheme();
        setShowButtons(false);
        addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
        setCommandListener(this);
        //loadFSI();
    }

    public void beforeShow() {
        updateTreeCaptionAndCommands(getCap());
    }

    private void reset() {
        lock();
        items = new String[0];
        clear();
        unlock();
    }

    private static int getNodeWeight(String filename) {
        if (filename.equals(PARENT_DIRECTORY)) return 0;
        if (filename.endsWith("/")) return 10;
        return 20;
    }

    public int vtCompareNodes(TreeNode node1, TreeNode node2) {
        int result;
        String name1 = (String) node1.getData();
        String name2 = (String) node2.getData();
        int weight1 = getNodeWeight(name1);
        int weight2 = getNodeWeight(name2);
        if (weight1 == weight2) result = name1.toLowerCase().compareTo(name2.toLowerCase());
        else result = (weight1 < weight2) ? -1 : 1;
        return result;
    }

    public int vtGetItemHeight() {
        return Math.max(imageList.getHeight() * 2 + 4, (getQuickFont(Font.STYLE_PLAIN).getFontHeight() + 4) * 2);
    }

    private void rebuildTree() {
        lock();
        clear();
        for (int i = 0; i < items.length; i++) {
            addNode(null, items[i]);
        }
        sortNode(null);
        getSize();
        updateTreeCaptionAndCommands(getCap());
        unlock();
    }

    private String getCap() {
        TreeNode currItem = getCurrentItem();
        String cap = (currItem == null) ? null : (String) currItem.getData();
        return StringConvertor.getString(cap);
    }

    public void setParameters(boolean select_dir) {
        needToSelectDirectory = select_dir;
    }

    public void setListener(FileBrowserListener listener) {
        this.listener = listener;
    }

    public void VTnodeClicked(TreeNode node) {
        if (node == null) {
            return;
        }
        String file = (String) node.getData();

        boolean flag = false;

        if (file.equals(PARENT_DIRECTORY)) {
            int d = currDir.lastIndexOf('/', currDir.length() - 2);
            currDir = (d != -1) ? currDir.substring(0, d + 1) : ROOT_DIRECTORY;
        } else if (file.endsWith("/")) {
            currDir += file;
        } else {
            flag = true;
            listener.onFileSelect(currDir + file);
        }

        if (flag) {
            return;
        }
        threadAction = TACTION_ONCLICK;
        (new Thread(this)).start();
    }

    private void updateTreeCaptionAndCommands(String name) {
        removeCommandEx(openCommand);
        boolean showSize = Options.getBoolean(Options.OPTION_SHOW_SIZE);
        if (name.equals(PARENT_DIRECTORY)) {
            int d = currDir.lastIndexOf('/', currDir.length() - 2);
            addCommandEx(openCommand, VirtualList.MENU_TYPE_LEFT_BAR);
            setCaption((d != -1) ? currDir.substring(0, d + 1) : ROOT_DIRECTORY);
        } else if (name.endsWith("/") & currDir.equals(ROOT_DIRECTORY)) {
            if (showSize) {
                try {
                    setCaption(ResourceBundle.getString("total_mem") + ": " + (FileSystem.totalSize(name) >> 10) + ResourceBundle.getString("kb"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else setCaption("");
            if (needToSelectDirectory) {
                addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
            } else {
                addCommandEx(openCommand, VirtualList.MENU_TYPE_LEFT_BAR);
            }
        } else if (name.endsWith("/") & !currDir.equals(ROOT_DIRECTORY)) {
            if (needToSelectDirectory) {
                addCommandEx(JimmUI.cmdMenu, VirtualList.MENU_TYPE_LEFT_BAR);
            } else {
                addCommandEx(openCommand, VirtualList.MENU_TYPE_LEFT_BAR);
            }
            setCaption(currDir + name);
        } else {
            addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
            if (showSize) {
                try {
                    int file_size;
                    FileSystem file = FileSystem.getInstance();
                    file.openFile(currDir + name);
                    file_size = (int) (file.fileSize() >> 10);
                    file.close();
                    int ext = name.lastIndexOf('.');
                    StringBuffer str_buf = new StringBuffer();
                    if (ext != -1) str_buf = str_buf.append(name.substring(ext + 1).toUpperCase()).append(", ");
                    str_buf = str_buf.append(file_size).append(ResourceBundle.getString("kb"));
                    setCaption(str_buf.toString());
                } catch (Exception ignored) {
                }
            } else {
                setCaption(jimm.comm.StringConvertor.getString(name));
            }
        }
    }

    public void run() {
        if (threadAction == TACTION_ACTIVATE) {
            try {
                setColorScheme();
                reset();
                items = FileSystem.getDirectoryContents(currDir, needToSelectDirectory);
                rebuildTree();
                //#sijapp cond.if modules_HISTORY is "true" #
                if (Jimm.getPrevScreen() instanceof HistoryStorageList) {
                    removeAllCommands();
                    addCommandEx(JimmUI.cmdBack, VirtualList.MENU_TYPE_RIGHT_BAR);
                    addCommandEx(openCommand, VirtualList.MENU_TYPE_LEFT_BAR);
                    setCommandListener(this);
                }
                //#sijapp cond.end#
                Jimm.setDisplay(this);
            } catch (JimmException je) {
                JimmException.handleException(je);
            }
        } else if (threadAction == TACTION_ONCLICK) {
            reset();
            try {
                items = FileSystem.getDirectoryContents(currDir, needToSelectDirectory);
            } catch (JimmException e) {
                JimmException.handleException(e);
            }
            rebuildTree();
        }
    }

    public void VTGetItemDrawData(TreeNode src, ListItem dst) {
        String file = (String) src.getData();
        dst.text = file;
        dst.image = getIcon(file);
        dst.color = getTextColor();
        dst.fontStyle = Font.STYLE_PLAIN;
        //int ext = file.lastIndexOf('.');
        //String format = file.substring(ext + 1).toLowerCase();
        //checkToFSI(format, dst);
    }

    private Icon getIcon(String dir) {
        if (dir.endsWith("/"))
            return imageList.elementAt(0);
        String ext = null;
        if (dir.indexOf('.') >= 0 && (ext = dir.substring(dir.lastIndexOf('.') + 1).toLowerCase()).length() == 0)
            ext = null;
        Icon icon = null;
        Image image = null;
        if (iformat != null && ext != null && iformat.containsKey(ext)) {
            icon = (Icon) iformat.get(ext);
        } else if (ext != null) {
            try {
                image = Image.createImage("/fs/" + ext + ".png");
            }
            catch (IOException ignored) {
            }
            if (iformat == null)
                iformat = new Hashtable();
            iformat.put(ext, (image != null) ? icon = new Icon(image) : imageList.elementAt(1));
        }
        if (icon == null)
            return imageList.elementAt(1);
        else
            return icon;
    }

//    private int[] formatIdx = new int[0];
//    private String[] formatString = new String[0];
//
//    private void loadFSI() {
//        String content = Util.removeCr(Util.getStringAsStream("/format.xml"));
//        if (content.length() == 0) {
//            return;
//        }
//        try {
//            XMLGear xg = new XMLGear();
//            xg.setStructure(content);
//            XMLItem[] files = xg.getItemsWithHeader(new String[]{"container"}, "item");
//            if (files == null) {
//                return;
//            }
//            int len = files.length, img;
//            formatString = new String[len];
//            formatIdx = new int[len];
//            for (int i = 0; i < len; i++) {
//                XMLItem file = files[i];
//                formatString[i] = file.getParamValue("exp");
//                img = -1;
//                try {
//                    img = Integer.parseInt(file.getParamValue("pic"));
//                } catch (Exception ignored) {
//                }
//                formatIdx[i] = img;
//            }
//        } catch (Exception ignored) {
//        }
//    }

//    private void checkToFSI(String format, ListItem dst) {
//        if (formatString.length > 0 && ContactList.formatIcons != null) {
//            for (int i = 0; i < formatString.length; i++) {
//                if (formatString[i].equals(format)) {
//                    dst.image = ContactList.formatIcons.elementAt(formatIdx[i]);
//                    break;
//                }
//            }
//        }
//    }

    public void vlCursorMoved(VirtualList sender) {
        if (sender == this) {
            updateTreeCaptionAndCommands(getCap());
        }
    }

    public void vlItemClicked(VirtualList sender) {
        VTnodeClicked(getCurrentItem());
    }

    public void vlKeyPress(VirtualList sender, int keyCode, int type, int gameAct) {
        if (type == KEY_REPEATED && !getCap().endsWith("/")) {
            DrawControls.NativeCanvas.getInst().cancelKeyRepeatTask();
        }
    }

    public void activate(String dir) {
        currDir = dir;
        threadAction = TACTION_ACTIVATE;
        (new Thread(this)).start();
    }

    public void activate() {
        activate(ROOT_DIRECTORY);
    }

    private boolean toAddOpen() {
        String name = getCap();
        return name != null && (name.equals(PARENT_DIRECTORY) || name.endsWith("/") & currDir.equals(ROOT_DIRECTORY) || name.endsWith("/") & !currDir.equals(ROOT_DIRECTORY));
    }

    private boolean toAddSelect() {
        String name = getCap();
        if (name == null) {
            return false;
        }
        if (name.equals(PARENT_DIRECTORY)) {
        } else if (name.endsWith("/") & currDir.equals(ROOT_DIRECTORY)) {
            if (needToSelectDirectory) {
                return true;
            }
        } else if (name.endsWith("/") & !currDir.equals(ROOT_DIRECTORY)) {
            if (needToSelectDirectory) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void showMenu() {
        Menu menu = new Menu(this);
        if (toAddOpen()) {
            menu.addMenuItem("open", (byte) 0);
        }
        if (toAddSelect()) {
            menu.addMenuItem("select", (byte) 1);
        }
        //#sijapp cond.if target="MIDP2"#
        if (!currDir.equals(ROOT_DIRECTORY)) {
            menu.addMenuItem("create_folder", (byte) 2);
        }
        //#sijapp cond.end#
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        menu.back();
        switch (action) {
            case 1:
                String filename = (String) getCurrentItem().getData();
                if (filename.endsWith("/")) {
                    listener.onDirectorySelect(currDir + filename);
                } else {
                    listener.onFileSelect(currDir + filename);
                }
                //setCommandListener(null);
                break;
            //#sijapp cond.if target="MIDP2"#
            case 2:
                String folder = currDir + "Folder_" + System.currentTimeMillis();
                FileSystem fs = FileSystem.getInstance();
                if (!folder.endsWith("/")) {
                    folder = folder + '/';
                }
                try {
                    //fs.openFileLoc(folder);
                    fs.openFile(folder);
                    fs.mkdir();
                    fs.close();
                } catch (Exception e) {
                    JimmException.handleException(new JimmException(172, 1, true));
                }
                reset();
                try {
                    items = FileSystem.getDirectoryContents(currDir, needToSelectDirectory);
                } catch (JimmException e) {
                    JimmException.handleException(e);
                }
                rebuildTree();
                break;
            //#sijapp cond.end#
            default:
                VTnodeClicked(getCurrentItem());
                break;
        }
    }

    public void commandAction(Command c, Displayable d) {
        DrawControls.NativeCanvas.getInst().cancelKeyRepeatTask();
        if (JimmUI.isControlActive(this)) {
            if (c == JimmUI.cmdBack) {
                Jimm.back();
                setCommandListener(null);
            } else if (c == JimmUI.cmdMenu) {
                showMenu();
            } else if (c == openCommand) {
                VTnodeClicked(getCurrentItem());
            }
        }
    }
}
//#sijapp cond.end#