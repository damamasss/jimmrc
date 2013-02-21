package jimm.ui;

import DrawControls.CanvasEx;
import DrawControls.NativeCanvas;

public abstract class SelectList extends Menu {
    /**
     * @author Rishat Shamsutdinov
     */
    protected Object objects[];

    public SelectList(CanvasEx prvScr, Object objects[]) {
        super(prvScr, (byte) 0);
        this.objects = objects;
    }

    protected int correctDeltaY(int dY, int height, int topY, int scHeight) {
        return dY - 1;
    }
}