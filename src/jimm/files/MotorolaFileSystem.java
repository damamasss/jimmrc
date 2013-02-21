//#sijapp cond.if modules_FILES="true"#
package jimm.files;

//#sijapp cond.if target="MIDP2"|target="MOTOROLA"#

import jimm.JimmException;

import javax.microedition.io.Connector;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

//#sijapp cond.if target="MOTOROLA"#
class MotorolaFileSystem {
    com.motorola.io.FileConnection fileConnection;

    public static String[] getDirectoryContents(String currDir, boolean only_dirs) throws JimmException {
        String[] items = null;
        try {
            if (currDir.equals(FileBrowser.ROOT_DIRECTORY)) {
                String[] roots = com.motorola.io.FileSystemRegistry.listRoots();
                items = new String[roots.length];
                for (int i = 0; i < roots.length; i++) {
                    items[i] = roots[i].substring(1);
                }
            } else {
                com.motorola.io.FileConnection fileconn = (com.motorola.io.FileConnection) Connector.open("file://" + currDir);
                String[] list = fileconn.list();
                fileconn.close();
                Vector list_vect = new Vector(list.length + 1);
                list_vect.addElement(FileBrowser.PARENT_DIRECTORY);
                for (int i = 0; i < list.length; i++) {
                    if (only_dirs & !list[i].endsWith("/")) {
                        continue;
                    }
                    list_vect.addElement(list[i].substring(currDir.length()));
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
        long total_size = 0;
        com.motorola.io.FileConnection fileconn = (com.motorola.io.FileConnection) Connector.open("file:///" + name);
        total_size = fileconn.totalSize();
        fileconn.close();
        return total_size;
    }

    public void openFile(String file) throws Exception {
        fileConnection = (com.motorola.io.FileConnection) Connector.open("file://" + file);
    }

    public void openFileLoc(String file) throws Exception {
        fileConnection = (com.motorola.io.FileConnection) Connector.open("file://localhost" + file);
    }

    public void readFile(String file) throws Exception {
        fileConnection = (com.motorola.io.FileConnection) Connector.open("file://" + file, Connector.READ);
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

    public final void mkdir() throws java.io.IOException {
    }
}
//#sijapp cond.end#
//#sijapp cond.end#