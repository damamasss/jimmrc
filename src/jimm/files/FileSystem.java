//#sijapp cond.if modules_FILES="true"#
package jimm.files;

public class FileSystem extends
//#sijapp cond.if target="MOTOROLA"#
        MotorolaFileSystem
//#sijapp cond.else#
        JSR75FileSystem
//#sijapp cond.end#
{


    public static FileSystem getInstance() {
        return new FileSystem();
    }
}