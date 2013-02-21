//#sijapp cond.if modules_FILES="true"#
package jimm.files;

import jimm.ContactItem;

public interface FileBrowserListener {
    public void onFileSelect(String file);

    public void onDirectorySelect(String directory);

    public ContactItem getCItem();
}