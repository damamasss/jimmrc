package jimm.ui;

import DrawControls.ImageList;

public class LineChoise {

    public int current;
    public int limited;
    public String label = "";
    public String[] items;

    public LineChoise(String capm, String[] itemsm) {
        label = capm;
        items = itemsm;
        if (items != null) {
            limited = items.length;
        }
        current = 0;
    }

    public void setSelected(int set) {
        current = Math.max(0, Math.min(limited - 1, set));
    }

    public void lineAction(boolean right) {
        current = Math.max(0, Math.min(current + ((right) ? 1 : -1), limited - 1));
    }

    public String[] getItems() {
        return items;
    }

    public int getSelected() {
        return current;
    }

    public String getLabel() {
        return label;
    }

    public int getLimited() {
        return limited;
    }

    public ImageList getImageList() {
        return null;
    }
}
