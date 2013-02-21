//#sijapp cond.if modules_FILES="true"#
package jimm.files;

import jimm.JimmException;
import jimm.Jimm;

//#sijapp cond.if target="MIDP2"|target="MOTOROLA"#
import javax.microedition.io.file.*;
//#sijapp cond.elseif target="SIEMENS2"#
import com.siemens.mp.io.file.FileConnection;
import com.siemens.mp.io.file.FileSystemRegistry;
//#sijapp cond.end#
import javax.microedition.io.Connector;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

class JSR75FileSystem {
    FileConnection fileConnection;

    public static String[] getDirectoryContents(String currDir, boolean only_dirs) throws JimmException {
        String[] items;
        try {
            if (currDir.equals(FileBrowser.ROOT_DIRECTORY)) {
                Vector roots_vect = new Vector();
                String element;
                Enumeration roots = FileSystemRegistry.listRoots();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
                while (roots.hasMoreElements()) {
                    element = (String) roots.nextElement();
                    //#sijapp cond.if target is "MIDP2"#
                    if (element.indexOf(':') >= 0 || !Jimm.is_phone_NOKIA())
                        //#sijapp cond.end#
                        roots_vect.addElement(element);
                }
                items = new String[roots_vect.size()];
                roots_vect.copyInto(items);
            } else {
                FileConnection fileconn;
//#sijapp cond.if target="SIEMENS2"#
//#				fileconn = (FileConnection) Connector.open("file://" + currDir);
//#sijapp cond.else#
                fileconn = (FileConnection) Connector.open("file://localhost" + currDir, Connector.READ);
//#sijapp cond.end#

                Enumeration list = fileconn.list();
                fileconn.close();
                Vector list_vect = new Vector();
                list_vect.addElement(FileBrowser.PARENT_DIRECTORY);
                String filename;
                while (list.hasMoreElements()) {
                    filename = (String) list.nextElement();
                    if (only_dirs & !filename.endsWith("/")) {
                        continue;
                    }
                    list_vect.addElement(filename);
                }
                items = new String[list_vect.size()];
                list_vect.copyInto(items);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new JimmException(191, 0, true);
        }
        return items;
    }

    public static long totalSize(String name) throws Exception {
        long total_size;
        FileConnection fileconn;
//#sijapp cond.if target="SIEMENS2"#
//#		fileconn = (FileConnection) Connector.open("file:///" + name);
//#sijapp cond.else#
        fileconn = (FileConnection) Connector.open("file://localhost/" + name);
//#sijapp cond.end#

        total_size = fileconn.totalSize();
        fileconn.close();
        return total_size;
    }

    public void openFile(String file) throws Exception {
        fileConnection = (FileConnection) Connector.open("file://" + file);
    }

    public void openFileLoc(String file) throws Exception {
        fileConnection = (FileConnection) Connector.open("file://localhost" + file);
    }

    public void readFile(String file) throws Exception {
//#sijapp cond.if target="SIEMENS2"#
//#		openFile(file);
//#sijapp cond.else#
        fileConnection = (FileConnection) Connector.open("file://" + file, Connector.READ);
//#sijapp cond.end#
    }

    public OutputStream openOutputStream() throws Exception {
        if (fileConnection.exists()) {
            fileConnection.delete();
        }
        fileConnection.create();
        return fileConnection.openOutputStream();
    }

    public InputStream openInputStream() throws Exception {
        return fileConnection.openInputStream();
    }

    public void close() {
        try {
            if (fileConnection != null) {
                fileConnection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long fileSize() throws Exception {
        if (fileConnection != null) {
            return fileConnection.fileSize();
        }
        return -1;
    }

    public String getName() {
        if (fileConnection != null) {
            return fileConnection.getName();
        }
        return null;
    }

    public final void mkdir() throws IOException {
        if (fileConnection != null) {
            fileConnection.mkdir();
        }
    }

//    public final long directorySize(boolean flag) {  // use
//        if (fileConnection != null) {
//            return fileConnection.directorySize(flag);    // flag - true
//        }
//        return -1;
//    }
//
//    public final boolean isDirectory() {  // use
//        if (fileConnection != null) {
//            return fileConnection.isDirectory();
//        }
//        return false;
//    }
//
//    public final Enumeration list(String s, boolean flag) {  // flag - hide objects // use
//        if (fileConnection != null) {
//            return fileConnection.list(s, flag);
//        }
//        return null;
//    }
//
//     public final Enumeration list() {
//        if (fileConnection != null) {
//            return fileConnection.list();
//        }
//        return null;
//    }
//
//    public final long lastModified() {
//        if (fileConnection != null) {
//            return fileConnection.lastModified();
//        }
//        return -1;
//    }
//
//    public final long availableSize() {
//        if (fileConnection != null) {
//            return fileConnection.availableSize();
//        }
//        return -1;
//    }
//
//    public final boolean isWrite() {
//        if (fileConnection != null) {
//            return fileConnection.canWrite();
//        }
//        return false;
//    }
//
//    public final boolean isHide() {
//        if (fileConnection != null) {
//            return fileConnection.isHidden();
//        }
//        return false;
//    }
//
//    public final void setWrite(boolean flag) {
//        if (fileConnection != null) {
//            fileConnection.setWritable(flag);
//        }
//    }
//
//    public final void setHide(boolean flag) {
//        if (fileConnection != null) {
//            fileConnection.setHidden(flag);
//        }
//    }
//
//    public final void delete() {
//        if (fileConnection != null) {
//            fileConnection.delete();
//        }
//    }
//
//    public final void rename(String s) {
//        if (fileConnection != null) {
//            fileConnection.rename(s);
//        }
//    }
//
//    public final void truncate(long l1) {
//        if (fileConnection != null) {
//            fileConnection.truncate(l1);
//        }
//    }
}