/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-07  Jimm Project

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
 File: src/DrawControls/VirtualTree.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Artyomov Denis
 *******************************************************************************/

package DrawControls;

import jimm.Options;
//#sijapp cond.if modules_TOOLBAR is "true"#
import jimm.ui.Toolbar;
//#sijapp cond.end#

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Graphics;
import java.util.Vector;

//! Tree implementation, which allows programmers to store node data themself

/*!
VirtualTree is successor of VirtualList. It store tree structure in.
It shows itself on display and handles user key commands.
You must inherit new class from VirtualDrawTree and reload next functions:
VirtualTree#getItemDrawData Tree control call this function for request
of data for tree node to be drawn
*/
public class VirtualTree extends VirtualList {

    final protected TreeNode root = new TreeNode(null);

    private Vector drawItems;

    private int stepSize = 6;
    private int currPole = 0;
    private int polesOpt = 1;

    private boolean isChanged = false;
    private boolean showButtons = true;
    private boolean autoExpand = true;
    private boolean rightX;
    private boolean clientIconsFlag;

//	private int currFontHeight;

    private VirtualTreeCommands commands;

    {
        root.expanded = true;
    }

    //! Constructor
    public VirtualTree(String capt, boolean autoExpand) {
        super(capt);
        this.autoExpand = autoExpand;
    }

    public VirtualTree(String capt, boolean autoExpand, boolean isCL) {
        this(capt, autoExpand);
        clActive = isCL;
    }

    //! Constructor
//	public VirtualTree
//	(
//		String capt,      //!< Caption shown at the top of control
//		int capTextColor, //!< Caption text color
//		int backColor,    //!< Control back color
//		int fontSize,     /*!< Control font size. This font size if used both for caption and text in tree nodes */
//		boolean autoExpand
//	)
//	{
//		super(capt, capTextColor, backColor, fontSize, VirtualList.MODE_LIST);
//		this.autoExpand = autoExpand;
//	}

    public void setVTCommands(VirtualTreeCommands commands) {
        this.commands = commands;
    }

    //! For internal use only
    /*! If someone change node structure wasChanged mast be called! */
    protected void wasChanged() {
        isChanged = true;
    }

    protected TreeNode getItem(int index) {
        DrawData dd = getDrawItem(index);
        if (dd == null) {
            return null;
        }
        return dd.elementAt(currPole);
    }

    // private TreeNode getDrawItem(int index)
    private DrawData getDrawItem(int index) {
        if (drawItems == null) {
            return null;
        }
        return (DrawData) drawItems.elementAt(index);
    }

    // private void checkToRebuildTree()
    private void checkToRebuildTree() {
        if ((isChanged) || (drawItems == null)) {
            rebuildTreeIntItems();
        }
    }

    //! Sets size of space for next level node
    public void setStepSize(int value) {
        stepSize = value;
        invalidate();
    }

    public void setShowButtons(boolean value) {
        if (value == showButtons) {
            return;
        }
        showButtons = value;
        invalidate();
    }

    public boolean getShowButtons() {
        return showButtons;
    }

    //! Returns current selected node
    public TreeNode getCurrentItem() {
        if ((drawItems == null) || (getCurrIndex() < 0) || (getCurrIndex() >= drawItems.size())) {
            return null;
        }
        return getDrawItem(getCurrIndex()).elementAt(currPole);
    }

    //! Set node as current. Make autoscroll if needs.
    public void setCurrentItem(TreeNode node) {
        if (node == null) {
            return;
        }
        int count, i;

        if (getLocked()) {
            lastNode = node;
            return;
        }

        checkToRebuildTree();

        if (getCurrentItem() == node) {
            return;
        }

        // finding at visible nodes
        count = drawItems.size();
        int idx;
        for (i = 0; i < count; i++) {
            if ((idx = getDrawItem(i).indexOf(node)) < 0) {
                continue;
            }
            currPole = idx;
            setCurrentItem(i);
            return;
        }

        // finding at all nodes
        Vector path = new Vector();
        buildNodePath(path, getRoot(), node);

        count = path.size();
        if (count != 0) {
            // make item visible
            //System.out.println("Path to tree node");
            for (i = 0; i < count; i++) {
                ((TreeNode) path.elementAt(i)).expanded = true;
            }
            rebuildTreeIntItems();
            setCurrentItem(node);
            wasChanged();
            invalidate();
        }
    }

    // Build path to node int tree
    private boolean buildNodePath(Vector path, TreeNode root, TreeNode node) {
        int count = root.size();
        TreeNode childNode;
        for (int i = 0; i < count; i++) {
            childNode = root.elementAt(i);
            if (childNode == node) return true;
            if (buildNodePath(path, childNode, node)) {
                path.addElement(childNode);
                return true;
            }
        }
        return false;
    }

    //! Returns root node (root node is parent for all nodes and never visible).
    public TreeNode getRoot() {
        return root;
    }

    //! Internal function
    /*! Changes node state*/
    protected boolean itemSelected() {
        TreeNode currItem = getCurrentItem();
        if (currItem == null) {
            return false;
        }
        if (autoExpand) {
            if (currItem.size() != 0) {
                currItem.expanded = !currItem.expanded;
                rebuildTreeIntItems();
                invalidate();
            }
            return false;
        }
        return executeCommand(findMenuByType(Command.OK));
//		return true;
    }

    //#sijapp cond.if target is "MIDP2"#
    protected boolean pointerPressedOnUtem(int index, int x, int y, int x1, int x2) {
        TreeNode currItem = getCurrentItem();
        if (currItem == null) {
            return true;
        }
        if ((currItem.size() > 0) && (x < (3 * getFontHeight() / 2 + currItem.level * stepSize))) {
            if (!itemSelected()) {
                keyReaction(1000004, KEY_RELEASED, Canvas.FIRE);
            }
            checkCurrItem();
            return false;
        }
        int prPole = currPole;
        currPole = Math.min(x * polesOpt / (x2 - x1), polesOpt);
        checkCurrItem();
        invalidate();
        return (prPole == currPole);
    }

    protected boolean oldDoubleClick() {
        return true;
    }
//#sijapp cond.end#

    //! For internal use only

    protected int getSize() {
        checkToRebuildTree();
        return drawItems.size();
    }

    // private void rebuildTreeIntItems()
    private synchronized void rebuildTreeIntItems() {
        isChanged = false;
        if (drawItems == null) {
            drawItems = new Vector();
        }
        synchronized (drawItems) {
            drawItems.removeAllElements();
            int count = root.size();
            for (int i = 0; i < count; i++) {
                fillTreeIntItems(root.elementAt(i), 0);
            }
        }
        checkCurrItem();
    }

    // private void fillTreeIntItems(TextDrawTreeItem top, int level)
    private void fillTreeIntItems(TreeNode top, int level) {
        if (top.getROT() || (getRoot().indexOf(top) != -1 && showButtons)) {
            drawItems.addElement((new DrawData()).addElement(top));
        } else {
            DrawData lastData = null;
            if (drawItems.size() > 0) {
                lastData = (DrawData) drawItems.lastElement();
            }
            if ((lastData != null) && (!lastData.elementAt(0).getROT()) && (lastData.getSize() < polesOpt)) {
                lastData.addElement(top);
            } else {
                drawItems.addElement((new DrawData()).addElement(top));
            }
        }
        top.level = level;
        if (top.getExpanded()) {
            int count = top.size();
            for (int i = 0; i < count; i++) {
                fillTreeIntItems(top.elementAt(i), level + 1);
            }
        }
    }

    // protected void get(int index, ListItem item)
    protected void get(int index, ListItem item) {
        checkToRebuildTree();
        TreeNode treeItem = getDrawItem(index).elementAt(0);
        commands.VTGetItemDrawData(treeItem, item);
        item.horizOffset = treeItem.level * stepSize;
    }


    protected int stringWidth(FontFacade font, String str) {
        return font.stringWidth(str);
    }

    protected static int drawNodeRect(Graphics g, TreeNode item, int x, int y1, int y2, int fontHeight) {
        int height = 2 * fontHeight / 3;
        if (height < 7) {
            height = 7;
        }
        if (height % 2 == 0) {
            height--;
        }
        int y = (y1 + y2 - height) / 2;
        int oldColor = g.getColor();
        g.setColor(0x808080);
        g.drawRect(x, y, height - 1, height - 1);
        int mx = x + height / 2;
        int my = y + height / 2;
        g.drawLine(x + 2, my, x + height - 3, my);
        if (item.getExpanded() == false) {
            g.drawLine(mx, y + 2, mx, y + height - 3);
        }
        g.setColor(oldColor);
        return height + 1;
    }

    protected void checkCurrItem() {
        super.checkCurrItem();
        TreeNode node = getCurrentItem();
        if ((node == null) || (node.getROT())) {
            currPole = 0;
        }
    }

    //! For internal use only
    /*! Draw a tree node. Called by base class DrawControls#VirtualDrawList */
    protected void drawItemData(Graphics g, int index, int x1, int y1, int x2, int y2, int fontHeight) {
        checkToRebuildTree();

        getDrawItem(index).drawItems(g, paintedItem, commands, x1, y1, --x2, y2, getFontHeight(), currPole,
                isItemSelected(index), cursorColor, cursorTrans, dcursor, stepSize, rightX, polesOpt);
    }

    public int getItemHeight(int itemIndex) {
//#sijapp cond.if target is "MOTOROLA"#
//#		int imgHeight = 0, fontHeight = getFontHeight() - 1;
//#sijapp cond.else#
        int imgHeight = 0, fontHeight = getFontHeight();
//#sijapp cond.end#
        paintedItem.clear();
        get(itemIndex, paintedItem);
        imgHeight = commands.vtGetItemHeight();

        return (imgHeight > fontHeight) ? imgHeight + 2 : fontHeight + 2;
    }

    public void move(int step) {
        TreeNode currItem = getCurrentItem();
        if (currItem == null) {
            return;
        }
        if (currItem.getROT()) {
            super.moveCursor(step, false);
        } else {
            currPole += step;
            TreeNode node = findParent(root, currItem);
            if (currPole < 0) {
                currPole = 0;
                if (showButtons) {
                    setCurrentItem(node);
                } else {
                    setCurrentItem(0);
                }
            } else if (currPole >= polesOpt) {
                if ((node != null) && (node.size() > 0)) {
                    setCurrentItem(node.lastElement());
                } else {
                    currPole = 0;
                }
            }
            checkCurrItem();
            repaint();
        }
    }

    //! Add new node
    /*! Method "addNode" insert new item at node root. Function return reference to new node. */

    public TreeNode addNode(TreeNode node, Object obj) {
        if (node == null) {
            node = this.root;
        }
        TreeNode result = new TreeNode(obj);
        node.addItem(result);
        wasChanged();
        invalidate();
        return result;
    }

    protected void addNode(TreeNode node) {
        getRoot().addItem(node);
        wasChanged();
        invalidate();
    }

    public TreeNode addNodeNative(Object obj) {
        return new TreeNode(obj);
    }

    protected boolean nodeIsExist(TreeNode root, TreeNode node) {
        return (root.indexOf(node) != -1);
    }

    // private TreeNode findParent(TreeNodeInternal root, TreeNode node)
    protected TreeNode findParent(TreeNode root, TreeNode node) {
        if ((root == null) || (node == null)) {
            return null;
        }
        if (root.indexOf(node) != -1) {
            return root;
        }
        int count = root.size();
        TreeNode result;
        for (int i = 0; i < count; i++) {
            result = findParent(root.elementAt(i), node);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    //! Removes node from tree. Returns true if removeing is successful.
    public boolean removeNode(TreeNode node) {
        storeLastNode();
        TreeNode parent = findParent(root, node);
        if (parent == null) {
            return false;
        }
        int index = parent.findItem(node);
        if (index == -1) {
            return false;
        }
        parent.removeItem(index);
        checkCurrItem();
        wasChanged();
        invalidate();
        restoreLastNode();
        return true;
    }

    //! Move one tree node to another. Returns true if moving is successful.
    public boolean moveNode(TreeNode node, TreeNode dst) {
        if (node == dst) {
            return false;
        }
        if (!removeNode(node)) {
            return false;
        }
        if (dst == null) {
            dst = root;
        }
        dst.addItem(node);
        checkCurrItem();
        wasChanged();
        invalidate();
        return true;
    }

    protected int compareNodes(TreeNode node1, TreeNode node2) {
        return 0;
    }

    public void sortNode(TreeNode node) {
        storeLastNode();
        if (node == null) {
            node = getRoot();
        }
        node.sort(commands);
        if (node.getExpanded()) {
            wasChanged();
            invalidate();
        }
        restoreLastNode();
    }

    public void insertChild(TreeNode root, TreeNode element, int index) {
        if (root == null) root = getRoot();
        storeLastNode();
        root.insertChild(element, index);
        if (root.getExpanded()) {
            wasChanged();
            invalidate();
        }
        restoreLastNode();
    }

    public void deleteChild(TreeNode root, int index) {
        if (root == null) {
            root = getRoot();
        }
        storeLastNode();
        root.items.removeElementAt(index);
        if (root.getExpanded()) {
            wasChanged();
            invalidate();
        }
        restoreLastNode();
    }

    public int getIndexOfChild(TreeNode root, TreeNode element) {
        if (root.items == null) {
            return -1;
        }
        return root.items.indexOf(element);
    }

    //! Expand or collapse tree node. NOTE: this is not recursive operation!
    public void setExpandFlag(TreeNode node, boolean value) {
        if (node.getExpanded() == value) {
            return;
        }
        node.expanded = value;
        wasChanged();
        checkCurrItem();
        checkTopItem();
        invalidate();
    }

    //! Remove all nodes from tree
    public void clear() {
        root.clear();
        rebuildTreeIntItems();
        checkCurrItem();
        checkTopItem();
        invalidate();
    }

    private TreeNode lastNode = null;

    private void storeLastNode() {
        lastNode = getCurrentItem();
    }

    protected void afterUnlock() {
        restoreLastNode();
    }

    private void restoreLastNode() {
        if (getLocked()) {
            return;
        }
        setCurrentItem(lastNode);
        lastNode = null;
    }

    public void updateParamsVT() {
        updateParams();
        if (clActive) {
            polesOpt = Options.getInt(Options.OPTION_POLES);
            rightX = Options.getBoolean(Options.OPTION_RIGHT_XTRAZ);
            capDivisor = 1;
            //menuDivisor = (Options.getBoolean(Options.OPTION_TOOLBAR)) ? 2 : 1;
            clientIconsFlag = Options.getBoolean(Options.OPTION_CLIENT_ICON);
            //cliIcon = null;
            //#sijapp cond.if modules_TOOLBAR is "true"#
            if (Options.getBoolean(Options.OPTION_TOOLBAR)) {
                if (toolbar == null) {
                    toolbar = new Toolbar();
                } else {
                    toolbar.load();
                }
                return;
            }
            toolbar = null;
            //#sijapp cond.end#
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    private class DrawData {
        /**
         * @author Rishat Shamsutdinov
         */
        private final Vector elements = new Vector();
        private TreeNode treeItem;
        private FontFacade font;

        protected DrawData() {
        }

        protected DrawData addElement(TreeNode node) {
            elements.addElement(node);
            return this;
        }

        protected int getSize() {
            return elements.size();
        }

        protected int indexOf(TreeNode data) {
            return elements.indexOf(data);
        }

        protected TreeNode elementAt(int idx) {
            if ((idx < 0) || (idx >= getSize())) {
                return null;
            }
            return (TreeNode) elements.elementAt(idx);
        }

        protected void drawItems(Graphics g, ListItem paintedItem, VirtualTreeCommands commands,
                                 int x1, int y1, int x2, int y2, int fontHeight, int currPole,
                                 boolean isCurrent, int cursorColor, int cursorTrans, int dcursor, int stepSize,
                                 boolean rightX, int poles) {

            int size = getSize();
            boolean center = false;
            int w_d = x2 - x1;
            if (!elementAt(0).getROT()) {
                w_d /= poles;
            } else {
                center = (poles > 1);
            }
            x2 = x1 + w_d;
            for (int i = 0; i < size; i++) {
                paintedItem.clear();
                treeItem = elementAt(i);
                commands.VTGetItemDrawData(treeItem, paintedItem);
                paintedItem.horizOffset = treeItem.level * stepSize;
                if ((isCurrent) && (currPole == i)) {
                    VirtualTree.this.capNick = paintedItem.text;
                    //if (!VirtualTree.this.clientIconsFlag) {
                    //    VirtualTree.this.cliIcon = paintedItem.ClientImg;
                    //}
                    if (cursorTrans == 0) {
                        VirtualList.drawGradient(g, x1, y1, x2 - x1 - 1, y2 - y1, cursorColor, 16, -48, 0);
                    } else {
                        drawAlphaGradient(g, x1, y1, x2 - x1 - 1, y2 - y1, cursorColor, -48, 0, 255 - cursorTrans);
                    }
                    g.setColor(dcursor);
                    g.drawRect(x1 - 1, y1 - 1, x2 - x1, y2 - y1 + 1);
                }
//                if (paintedItem.needBottom) {
//                    int color2 = g.getColor();
//                    g.setColor(textColor); // rect
//                    g.drawRect(x1 - 1, y1 - 1, x2 - x1, y2 - y1 + 1);
//                    g.setColor(color2);
//                }
                g.setColor(paintedItem.color);
                font = VirtualTree.this.getQuickFont(paintedItem.fontStyle);

                drawItemData(g, paintedItem, treeItem, font, x1, y1, x2, y2, fontHeight, VirtualTree.this.getShowButtons(), rightX, center, (isCurrent) && (currPole == i));
                x1 += w_d;
                x2 += w_d;
            }
        }

        protected void drawItemData(Graphics g, ListItem paintedItem, TreeNode node, FontFacade font, int x1, int y1, int x2,
                                    int y2, int fontHeight, boolean showButtons, boolean rightX, boolean center, boolean active) {

            int x = x1 + paintedItem.horizOffset;
            int yCenter = (y1 + y2) / 2;

            if (paintedItem.image != null) {
                paintedItem.image.drawInVCenter(g, x + 1, yCenter);

                if (paintedItem.isMessage) {
                    x += paintedItem.image.getWidth() + 1;
                    {
                        if (paintedItem.unreadMessCount > 0) {
                            drawString(g, font, Integer.toString(paintedItem.unreadMessCount), x - 1, (y1 + y2 - fontHeight) / 2, Graphics.TOP | Graphics.LEFT);
                            x += font.stringWidth(Integer.toString(paintedItem.unreadMessCount));
                        }
                    }
                    x += 2;
                } else {
                    x += paintedItem.image.getWidth() + 1;
                }
            } else {
                if ((node.size() > 0) && (showButtons)) {
                    VirtualTree.drawNodeRect(g, node, x1, y1, y2, fontHeight);
                    x += 3 * fontHeight / 4;
                }
            }

            // Отрисовка Х-статуса
            if ((!rightX) && (paintedItem.XStatusImg != null)) {
                paintedItem.XStatusImg.drawInVCenter(g, x, yCenter);
                x += paintedItem.XStatusImg.getWidth();
            }

            if (paintedItem.HappyImg != null) {
                paintedItem.HappyImg.drawInVCenter(g, x, yCenter);
                x += paintedItem.HappyImg.getWidth();
            }

//		if (paintedItem.AuthImg != null) {
//			paintedItem.AuthImg.drawInVCenter(g, x, yCenter);
//			x += paintedItem.AuthImg.getWidth();
//		}

            // Отрисовка значка ICQ-клиента
            if ((VirtualTree.this.clientIconsFlag) && (paintedItem.ClientImg != null)) {
                paintedItem.ClientImg.drawInVCenter(g, x2 - paintedItem.ClientImg.getWidth(), yCenter);
                x2 -= paintedItem.ClientImg.getWidth();
            }

            // Отрисовка Х-статуса
            if ((rightX) && (paintedItem.XStatusImg != null)) {
                paintedItem.XStatusImg.drawInVCenter(g, x2 - paintedItem.XStatusImg.getWidth(), yCenter);
                x2 -= paintedItem.XStatusImg.getWidth();
            }

            if (paintedItem.AuthImg != null) {
                paintedItem.AuthImg.drawInVCenter(g, x2 - paintedItem.AuthImg.getWidth(), yCenter);
                x2 -= paintedItem.AuthImg.getWidth();
            }

            if (paintedItem.PrivateImg != null) {
                paintedItem.PrivateImg.drawInVCenter(g, x2 - paintedItem.image.getWidth() - 1, yCenter);
                x2 -= paintedItem.image.getWidth() + 1;
            }

            if (paintedItem.text != null) {
                int clipX = g.getClipX();
                int clipY = g.getClipY();
                int clipWidth = g.getClipWidth();
                int clipHeight = g.getClipHeight();
                if (center) {
                    x = (x1 + x2 - VirtualTree.this.stringWidth(font, paintedItem.text)) / 2;
                } else {
                    x++;
                }
                g.clipRect(x, y1, x2 - x, y2 - y1);
                {
                    int minus = 0;
                    if (active) {
                        minus = ((font.stringWidth(paintedItem.text) - (x2 - x)) / 10) * textOff;
                        if (minus > 0) {
                            minus = 0;
                        }
                    }
                    drawString(g, font, paintedItem.text, x + minus, (y1 + y2 - fontHeight) / 2, Graphics.TOP | Graphics.LEFT);
                }
                g.setClip(clipX, clipY, clipWidth, clipHeight);
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////
}