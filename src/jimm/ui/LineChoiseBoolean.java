package jimm.ui;

import jimm.comm.Util;
import jimm.util.ResourceBundle;

public class LineChoiseBoolean extends LineChoise {

    public LineChoiseBoolean(String label, boolean yes) {
        super(label, null);
        items = Util.explode(ResourceBundle.getString("no") + '|' + ResourceBundle.getString("yes"), '|');
        limited = items.length;
        current = (yes) ? 1 : 0;
    }

    public boolean getBooolean() {
        return current > 0;
    }
}
