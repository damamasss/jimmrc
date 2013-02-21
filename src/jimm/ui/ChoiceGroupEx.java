package jimm.ui;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Image;

public class ChoiceGroupEx extends ChoiceGroup {
    /**
     * @author Rishat Shamsutdinov
     */
    private int chType;

    public ChoiceGroupEx(String label, int choiceType) {
        super(label, choiceType);
        chType = choiceType;
    }

    public ChoiceGroupEx(String label, int choiceType, String[] stringElements, Image[] imageElements) {
        super(label, choiceType, stringElements, imageElements);
        chType = choiceType;
    }

    public int getChoiceType() {
        return chType;
    }
}