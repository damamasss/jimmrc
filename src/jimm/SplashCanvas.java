/*******************************************************************************
 Jimm - Mobile Messaging - J2ME ICQ clone
 Copyright (C) 2003-05  Jimm Project

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
 File: src/jimm/SplashCanvas.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Manuel Linsmayer, Andreas Rossbacher
 *******************************************************************************/


package jimm;

import DrawControls.*;
import jimm.comm.Icq;
import jimm.util.ResourceBundle;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Form;

public final class SplashCanvas extends CanvasEx {

    private ImageList splash;
    private Image unlbk;
    private int background;
    private Image bgdImg;
    private boolean pushlock;
    private int xunblk;
    private int yunblk;
    private int progress;
    public boolean isLocked;
    public int availableMessages;
    private long poundPressTime;

    public SplashCanvas() {
        progress = 0;
        isLocked = false;
        setFullScreenMode(true);
        background();
        if (Jimm.isTouch()) {
            try {
                unlbk = Image.createImage("/unlbk.png");
                unblk();
            } catch (Exception ignored) {
            }
        }
    }

    private Icon getSplashIcon() {
        if (splash == null) {
            splash = new ImageList();
            int count = 1;
            try {
                splash.load("/logo.png", count);
            } catch (Exception ignored) {
            }
        }
        return splash.elementAt(0);
    }

    public final void setProgress(int progress) {
        if (this.progress != progress) {
            this.progress = progress;
            invalidate();
        }
    }

    public final synchronized void lockProgramm() {
        if (isLocked)
            return;
        if (Options.getInt(Options.OPTION_SPLASH_TRANS) != 0) {
            try {
                int buf[] = new int[100];
                int transLevel = 255 - Options.getInt(Options.OPTION_SPLASH_TRANS) * 255 / 10;
                int color = (transLevel << 24) | getColor(COLOR_SPLASH);
                for (int i = buf.length - 1; i >= 0; i--)
                    buf[i] = color;

                bgdImg = Image.createRGBImage(buf, 10, 10, true);
            } catch (OutOfMemoryError ignored) {
            }
        }
        isLocked = true;
        Jimm.setDisplay(this);
    }

    public final void bgdImg(int transLevel) {
        try {
            int buf[] = new int[100];
            int color = (transLevel << 24) | getColor(COLOR_SPLASH);
            for (int i = buf.length - 1; i >= 0; i--)
                buf[i] = color;

            bgdImg = Image.createRGBImage(buf, 10, 10, true);
        } catch (OutOfMemoryError ignored) {
        }
    }

    public final synchronized void unlockProgramm() {
        if (!isLocked)
            return;
        bgdImg = null;
        isLocked = false;
        availableMessages = 0;
        Icq.keyPressed();
        Jimm.getContactList().activate();
        setProgress(0);
        background();
    }

    public final void doKeyreaction(int keyCode, int type) {
        if (type == KEY_PRESSED) {
            if (isLocked) {
                if (keyCode == -43 && Jimm.is_phone_SE()) {
                    /*
                    *-43 Slider open
                    *-44 Slider close
                    */
                    if (Options.getString(Options.OPTION_ENTER_PASSWORD).length() > 0) {
                        (new EnterPassword(this, Jimm.getContactList())).showPasswordForm(false);
                    } else {
                        unlockProgramm();
                        poundPressTime = 0;
                    }
                    return;
                }
                if (keyCode == Canvas.KEY_POUND || keyCode == Canvas.KEY_STAR) {
                    poundPressTime = System.currentTimeMillis();
                    return;
                }
                invalidate();
            }
        } else {
            tryToUnlock(keyCode, type);
        }
    }

    private synchronized void tryToUnlock(int keyCode, int type) {
        if (!isLocked)
            return;
        if (keyCode != Canvas.KEY_POUND && keyCode != Canvas.KEY_STAR) {
            poundPressTime = 0;
            setProgress(0);
            background();
            return;
        }
        long wait = System.currentTimeMillis() - poundPressTime;
        if (poundPressTime != 0) {
            setProgress((int) ((100 * wait) / 900L));
            if (wait >= 900) {
                if (Options.getString(Options.OPTION_ENTER_PASSWORD).length() > 0/* && bw.a(237, 1)*/) {
                    (new EnterPassword(this, Jimm.getContactList())).showPasswordForm(false);
                } else {
                    unlockProgramm();
                    poundPressTime = 0;
                }
            }
        }
        if (type == KEY_RELEASED) {
            setProgress(0);
            background();
        }
    }

    private void funlock() {
        if ((Options.getString(Options.OPTION_ENTER_PASSWORD)).length() > 0/* && bw.a(237, 1)*/) {
            (new EnterPassword(this, Jimm.getContactList())).showPasswordForm(false);
        } else {
            unlockProgramm();
        }
    }

    public final void pointerPressed(int x, int y) {
        if (unlbk == null) {
            if (ptInRect(x, y, 0, NativeCanvas.getHeightEx() - getBottom() - 2, NativeCanvas.getWidthEx(), NativeCanvas.getHeightEx()) && isLocked) {
                funlock();
            }
        } else if (isLocked && ptInRect(x, y, xunblk, yunblk, xunblk + unlbk.getWidth(), yunblk + unlbk.getHeight())) {
            pushlock = true;
            lastPointerXCrd = x;
        }
    }

    public final void pointerDragged(int x, int y) {
        if (pushlock) {
            int l = (xunblk + x) - lastPointerXCrd;
            xunblk = Math.max(0, Math.min(l, NativeCanvas.getWidthEx() - unlbk.getWidth()));
            lastPointerXCrd = x;
            invalidate();
            if (xunblk >= NativeCanvas.getWidthEx() - (5 * unlbk.getWidth()) / 3) {
                funlock();
                unblk();
            }
        }
    }

    public final void pointerReleased(int x, int y) {
        if (pushlock)
            unblk();
    }

    private void background() {
        background = getColor(COLOR_SPLASH);
        if (Options.getInt(Options.OPTION_SPLASH_TRANS) != 0)
            bgdImg(255 - Options.getInt(Options.OPTION_SPLASH_TRANS) * 255 / 10);
    }

    private void unblk() {
        if (unlbk != null) {
            xunblk = 0;
            yunblk = NativeCanvas.getHeightEx() - unlbk.getHeight();
            pushlock = false;
            invalidate();
        }
    }

    private int getBottom() {
        if (unlbk == null || !Jimm.isTouch()) {
            return facade.getFontHeight();
        }
        return unlbk.getHeight();
    }

    public final void paint(Graphics g) {
        if (g.getClipY() < NativeCanvas.getHeightEx() - getBottom() - 2) {
            if (Options.getInt(Options.OPTION_SPLASH_TRANS) != 0 & isLocked) {
                Jimm.getContactList().repaintTree(g);                
                if (progress > 0) {
                    int alpha = 255 - Options.getInt(Options.OPTION_SPLASH_TRANS) * 255 / 10;
                    alpha = alpha * (100 - progress) / 100;
                    bgdImg(alpha);
                }
                int wid = NativeCanvas.getWidthEx();
                int hei = NativeCanvas.getHeightEx();
                for (int x = 0; x < wid; x += 10) {
                    for (int y = 0; y < hei; y += 10)
                        g.drawImage(bgdImg, x, y, Graphics.TOP | Graphics.LEFT);
                }
            } else {
                if (progress > 0) {
                    int def = getColor(COLOR_SPLASH);
                    int inv = getInverseColor(def);
                    int rd = (def & 0xFF);
                    int gr = ((def >> 8) & 0xFF);
                    int bl = ((def >> 16) & 0xFF);
                    int rdi = (inv & 0xFF);
                    int gri = ((inv >> 8) & 0xFF);
                    int bli = ((inv >> 16) & 0xFF);
                    int light = ((rdi + gri + bli) - (rd + gr + bl)) / 3;
                    light = light * progress / 100;
                    background = transformColorLight(def, light);
                }
                g.setColor(background);
                g.fillRect(0, 0, NativeCanvas.getWidthEx(), NativeCanvas.getHeightEx());
            }
        }
        Icon icon = getSplashIcon();
        if (icon != null) {
            icon.drawInCenter(g, NativeCanvas.getWidthEx() / 2, NativeCanvas.getHeightEx() / 2);
        }
        if (isLocked && availableMessages > 0) {
            Icon mess = ContactList.imageList.elementAt(14);
            mess.drawByLeftBottom(g, 1, NativeCanvas.getHeightEx());
            g.setColor(getColor(COLOR_SPL_TEXT));
            drawString(g, facade, "# " + availableMessages, /*ContactList.imageList.elementAt(14).getWidth() + */1, NativeCanvas.getHeightEx() - getBottom(), Graphics.LEFT | Graphics.BOTTOM);
        }
        if ((unlbk == null || !Jimm.isTouch()) & isLocked) {
            g.setColor(getColor(COLOR_SPL_TEXT));
            drawString(g, facade, ResourceBundle.getString("keylock_message"), NativeCanvas.getWidthEx() / 2, NativeCanvas.getHeightEx(), Graphics.BOTTOM | Graphics.HCENTER);
        } else if (unlbk != null && isLocked) {
            g.drawImage(unlbk, xunblk, yunblk, Graphics.TOP | Graphics.LEFT);
        }
    }
}

/*
import DrawControls.TextList;
import jimm.comm.*;
import jimm.util.ResourceBundle;
import jimm.ui.PopUp;

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.lcdui.*;
import java.util.Timer;

import DrawControls.*;

public class SplashCanvas extends CanvasEx {

    private FontFacade normalFont;
    private int fontHeight;
    private long lastUpdate = -1;
    private Timer t1, t2;
    private ImageList splash;
    private int splInt;
    private byte optimisation = 0x00;
    private long animTime = -1;
    private long time = -1;
    private int currFrame = 0;
    private Image bgdImg;
    private Icon unlbk;
    private String message;
    private int progress = 0;
    private boolean isLocked = false;
    private int availableMessages;
    public long poundPressTime;
    protected boolean showKeylock;
    private int status_index = -1;
    private Icon xstatus_img;
    private PopUp lockMessage;

    public SplashCanvas(String message) {
        getSplashIcon();
        super.setFullScreenMode(true);
        setMessage(message);
        normalFont = new FontFacade(getSuperFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, (getHeight() > 220) ? Font.SIZE_MEDIUM : Font.SIZE_SMALL));
        fontHeight = normalFont.getFontHeight();
        showKeylock = false;
        lockMessage = new PopUp(this, ResourceBundle.getString("keylock_message"), getWidth(), getHeight(), 4, 10);
        //lockMessage = null;
    }

    public SplashCanvas() {
        this(null);
    }

    public void setMessage(String m) {
        if (System.currentTimeMillis() - lastUpdate > 50) {
            message = m;
            invalidate();
        }
    }

    public synchronized void setStatusToDraw(int st_index) {
        status_index = st_index;
    }

    public synchronized void setXStatusToDraw(Icon xst_icon) {
        xstatus_img = xst_icon;
    }

    private Icon getSplashIcon() {
        if (splash == null) {
            splash = new ImageList();
            int count = 1;
            try {
                InputStream inpst;
                inpst = getClass().getResourceAsStream("/logo.png.bin");
                count = inpst.read();
                if (count > 1) {
                    optimisation = (byte) inpst.read();
                }
                animTime = inpst.read() * 100;
            } catch (Exception ignored) {
            }
            try {
                splash.load("/logo.png", count);
            } catch (Exception ignored) {
            }
            if (splash.size() > 1) {
                splInt = 10000 / (splash.size() - 1);
            }
        }
        if (splash.size() < 2) {
            return splash.elementAt(0);
        }
        int idx = 100 * progress / splInt;
        if (animTime > 0) {
            long ctime = System.currentTimeMillis();
            if (ctime - time >= animTime && time > 0) {
                currFrame += (int) ((ctime - time) / animTime);
                currFrame = currFrame % splash.size();
                time = ctime;
            } else if (time <= 0) {
                time = ctime;
            }
            if (currFrame == 0 && optimisation != 0x00) {
                return null;
            }
            idx = currFrame;
        }
        return splash.elementAt(idx);
    }

    private void getUnlockImage() {
        if (unlbk == null) {
            Image tu = null;
            try {
                tu = Image.createImage("/unlbk.png");
            } catch (IOException ignored) {
            }
            if (tu != null) {
                unlbk = new Icon(tu);
            }
        }
    }

    private void animate() {
        if (animTime <= 0) return;
        time = -1;
        if (t2 != null) {
            t2.cancel();
            t2 = null;
        }
        (t2 = new Timer()).schedule(new TimerTasks(this), animTime, animTime);
    }

    public void show() {
        if (t2 != null) {
            t2.cancel();
            t2 = null;
        }
        Jimm.setDisplay(this);
        animate();
    }

    public void setProgress(int progress) {
        if (this.progress == progress) {
            return;
        }
        this.progress = progress;
        invalidate();
    }

    public synchronized void lockJimm(Icq icq) {
        if (isLocked) {
            return;
        }
//#sijapp cond.if modules_LIGHT is "true"#
//		JimmUI.setLights(0x00);
//#sijapp cond.end#
        boolean mix = (Options.getInt(Options.OPTION_SPLASH_TRANS) != 0);
        if (mix) {
            try {
                int buf[] = new int[100];
                int transLevel = 255 - Options.getInt(Options.OPTION_SPLASH_TRANS) * 255 / 10;
                int color = (transLevel << 24) | getColor(COLOR_SPLASH);
                for (int i = buf.length - 1; i >= 0; i--) {
                    buf[i] = color;
                }
                bgdImg = Image.createRGBImage(buf, 10, 10, true);
            } catch (OutOfMemoryError ignored) {
            }
        }
        isLocked = true;
        setMessage(ResourceBundle.getString("keylock_enabled"));
        setStatusToDraw(JimmUI.getStatusImageIndex(icq.getCurrentStatus()));
        setXStatusToDraw(icq.getCurrentXStatus());
//  #sijapp cond.if target is "MIDP2"#
        if (Jimm.isTouch()) {
            getUnlockImage();
            lastPointerXCrd = this.getWidth() / 5 + ((unlbk != null) ? unlbk.getWidth() / 2 : (this.getWidth() / 20));
        }
//  #sijapp cond.end#
        show();
        if ((mix) && (animTime <= 0)) {
            (t2 = new Timer()).schedule(new TimerTasks(this), mix ? 500 : 20000, mix ? 500 : 20000);
        }
    }

    public synchronized void unlockJimm() {
        if (!isLocked) {
            return;
        }
        bgdImg = null;
        isLocked = false;
        availableMessages = 0;
        if (t2 != null) {
            t2.cancel();
        }
        Icq.keyPressed();
        Jimm.getContactList().activate();
    }

    public boolean locked() {
        return (isLocked);
    }

    public synchronized void messageAvailable() {
        if (isLocked) {
            ++availableMessages;
            invalidate();
        }
    }

    public void doKeyreaction(int keyCode, int type) {
        if (type == KEY_PRESSED) {
            if (isLocked) {
                if ((keyCode == Canvas.KEY_POUND) || (keyCode == Canvas.KEY_STAR)) {
                    poundPressTime = System.currentTimeMillis();
                } else {
                    if (t1 != null) {
                        t1.cancel();
                    }
                    //lockMessage = new PopUp(this, ResourceBundle.getString("keylock_message"), getWidth(), getHeight(), 4, 4);
                    showKeylock = true;
                    invalidate();
                }
            }
        } else {
            tryToUnlock(keyCode);
        }
    }

    private void tryToUnlock(int keyCode) {
        if (!isLocked) return;

        if ((keyCode != Canvas.KEY_POUND) && (keyCode != Canvas.KEY_STAR)) {
            poundPressTime = 0;
            return;
        }
        if ((poundPressTime != 0) && ((System.currentTimeMillis() - poundPressTime) > 900)) {
            if ((Options.getString(Options.OPTION_ENTER_PASSWORD)).length() > 0) {
                (new EnterPassword(this, Jimm.getContactList())).showPasswordForm(false);
            } else {
                unlockJimm();
                poundPressTime = 0;
            }
        }
    }

    private void unlockTouch() {
        Jimm.getContactList().pointerReleasedEmu(-1, -1);
        if ((Options.getString(Options.OPTION_ENTER_PASSWORD)).length() > 0) {
            (new EnterPassword(this, Jimm.getContactList())).showPasswordForm(false);
        } else {
            unlockJimm();
        }
    }

    public void hideLock() {
        showKeylock = false;
        //lockMessage = null;
        invalidate();
    }

    //  #sijapp cond.if target is "MIDP2"#
    protected boolean unblockScreen = false;

    public void pointerPressed(int x, int y) {
// #sijapp cond.if modules_TOUCH2 is "true" #
//        if (ptInRect(x, y, 0, this.getHeight() - fontHeight - 2, this.getWidth(), this.getHeight())) {
//            if (isLocked) {
//                unlockTouch();
//                return;
//            }
//        }
// #sijapp cond.end#
        int wh = this.getWidth();
        int yh = this.getHeight();
        int yi = (unlbk != null) ? unlbk.getHeight() : yh / 8;
        if (ptInRect(x, y, wh / 5, yh * 4 / 5, wh / 2, yh * 4 / 5 + yi)) {
            lastPointerXCrd = x;
            unblockScreen = true;
            return;
        }
        unblockScreen = false;
    }

    public void pointerDragged(int x, int y) {
        if (!unblockScreen) return;
        int xh = (unlbk != null) ? unlbk.getWidth() / 2 : (this.getWidth() / 20);
        if (x >= this.getWidth() * 4 / 5 - xh) {
            unlockTouch();
            return;
        }
        if (x != lastPointerXCrd) {
            lastPointerXCrd = x;
            invalidate();
        }
    }

    public void pointerReleased(int x, int y) {
        lastPointerXCrd = this.getWidth() / 5 + ((unlbk != null) ? unlbk.getWidth() / 2 : (this.getWidth() / 20));
        invalidate();
    }
//  #sijapp cond.end#

    */
/**
 * **************************Draw SplashCanvas*************************
 */
/*

    public void paint(Graphics g) {
        Icon icon;
        if (g.getClipY() < this.getHeight() - fontHeight - 2) {
            if (bgdImg != null) {
                Jimm.getContactList().repaintTree(g);
                int wid = getWidth();
                int hei = getHeight();
                for (int x = 0, y; x < wid; x += 10) {
                    for (y = 0; y < hei; y += 10) {
                        g.drawImage(bgdImg, x, y, Graphics.TOP | Graphics.LEFT);
                    }
                }
            } else {
                g.setColor(getColor(COLOR_SPLASH));
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }

            if (optimisation != 0x00) {
                icon = splash.elementAt(0);
            } else {
                icon = getSplashIcon();
            }
            if (icon != null) {
                icon.drawInCenter(g, this.getWidth() / 2, this.getHeight() / 2);
            } else {
                g.setColor(getColor(COLOR_SPL_TEXT));
                drawString(g, normalFont, "J[i]mm", this.getWidth() / 2, this.getHeight() / 2 + 5, Graphics.HCENTER | Graphics.BASELINE);
            }

            if (isLocked && availableMessages > 0) {
                ContactList.imageList.elementAt(14).drawByLeftTop(g, 1, this.getHeight() - (2 * fontHeight) - 9);
                g.setColor(getColor(COLOR_SPL_TEXT));
                drawString(g, normalFont, "# " + availableMessages, ContactList.imageList.elementAt(14).getWidth() + 4, this.getHeight() - (2 * fontHeight) - 5, Graphics.LEFT | Graphics.TOP);
            }
            drawDate(g);

            if (showKeylock && lockMessage != null) {
//                int color = getInverseColor(getColor(COLOR_SPLASH));
//                int kx, ky, kw, kh;
//                kw = getWidth() / 10 * 8;
//                kh = normalFont.getFontHeight() * TextList.getLineNumbers(ResourceBundle.getString("keylock_message"), kw, 0, 0, 0);
//                kx = getWidth() / 2 - (getWidth() / 10 * 4);
//                ky = getHeight() / 2 - (kh / 2);
//                drawGlassRect(g, getColor(COLOR_CAP), kx - 2, ky - 2, kx + kw + 4, ky + kh + 4);
//                g.setColor(color);
//                g.fillRect(kx, ky, kw, kh);
//                TextList.showText(g, ResourceBundle.getString("keylock_message"), kx, ky, kw, kh, TextList.SMALL_FONT, 0, getInverseColor(color));
//                (t1 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_HIDE_KEYLOCK), 2000);
                lockMessage.paint0(g);
                (t1 = new Timer()).schedule(new TimerTasks(TimerTasks.SC_HIDE_KEYLOCK), 2000);
            }

        }
        Icon subIcon = null;
        int subIconW = 0;
        if (status_index != -1) {
            subIcon = ContactList.imageList.elementAt(status_index);
            subIconW = subIcon.getWidth();
        }
        subIconW = drawIcons(g, subIcon, subIconW);
        drawProgressMess(g, subIconW);
//  #sijapp cond.if target is "MIDP2"#
        if (isLocked && Jimm.isTouch()) {
            int xh = lastPointerXCrd;
            int yh = getHeight();
            int wh = getWidth();
            boolean wi = (getInverseColor(getColor(COLOR_SPLASH)) == 0x000000);
            int fon = ((wi) ? 0xcccccc : 0x333333);
            drawGradient(g, wh / 5 + 1, yh * 4 / 5 - 1, wh * 3 / 5 - 1, (unlbk != null) ? unlbk.getHeightEx() + 1 : yh / 20, fon, 16, -48, 0);
            if (unlbk == null) {
                drawGlassRect(g, getColor(COLOR_SPL_PRGS), xh - (wh / 20), yh * 4 / 5 - 1, xh + (wh / 20), yh * 4 / 5 + yh / 20 - 1);
            } else {
                unlbk.drawByLeftTop(g, xh - unlbk.getWidthEx() / 2, this.getHeight() * 4 / 5);
            }
        }
//#sijapp cond.end#
        int progressPx = this.getHeight() * progress / 100;
        if (((progressPx < 1) && ((splash == null) || (splash.size() < 2))) || isLocked) {
            return;
        }
        if ((splash == null) || (splash.size() < 2)) {
            drawload(g, 0, progressPx);
        } else if (100 * progress >= splInt || animTime > 0 && currFrame > 0) {
            if (optimisation == 0x01) {
                icon = getSplashIcon();
                if (icon != null) {
                    icon.drawInCenter(g, this.getWidth() / 2, this.getHeight() / 2);
                }
            } else if (optimisation == 0x02) {
                int count = splash.size();
                for (int i = 1; i < count; i++) {
                    (splash.elementAt(i)).drawInCenter(g, this.getWidth() / 2, this.getHeight() / 2);
                }
            }
            if (optimisation != 0x00) {
                drawDate(g);
                drawProgressMess(g, subIconW);
                drawIcons(g, subIcon, subIconW);
            }
        }
    }

    private void drawload(Graphics g, int x, int px) {
        int yh = this.getHeight();
        int wh = this.getWidth();
        boolean wi = (getInverseColor(getColor(COLOR_SPLASH)) == 0x000000);
        int fon = ((wi) ? 0xcccccc : 0x333333);

        drawGradient(g, wh / 5 + 1, yh * 4 / 5 - 1, wh * 3 / 5 - 1, yh / 20, fon, 16, -48, 0);
        drawGlassRect(g, getColor(COLOR_SPL_PRGS), wh / 5 + 1, yh * 4 / 5 - 1, wh / 5 + (wh * 3 / 5) * progress / 100, yh * 4 / 5 + yh / 20 - 1);

        */
/*int h = this.getHeight();
        //lock();
        g.setColor(JimmUI.getColor(JimmUI.COLOR_SPL_PRGS));
        for(int i = h; i > (h - px); i-=9) {
            g.fillRect(x,i-6,6,6);
            //g.fillTriangle(x+3,i-6,x,i,x+6,i);
        }
        g.setColor(JimmUI.getColor(JimmUI.COLOR_SPL_TEXT));
        for(int i = h; i > 0; i-=9) {
            g.fillRect(x+2,i-4,2,2);
            //g.fillTriangle(x+3,i-5,x+1,i-1,x+5,i-1);
        }
        //unlock();*/
/*
    }

    private void drawDate(Graphics g) {
        g.setColor(getColor(COLOR_SPL_TEXT));
        drawString(g, normalFont, DateAndTime.getDateString(false, false), this.getWidth() / 2, 12, Graphics.TOP | Graphics.HCENTER);
        drawString(g, normalFont, DateAndTime.getCurrentDay(), this.getWidth() / 2, 16 + normalFont.getFontHeight(), Graphics.TOP | Graphics.HCENTER);
    }

    private void drawProgressMess(Graphics g, int im_width) {
        g.setColor(getColor(COLOR_SPL_TEXT));
        drawString(g, normalFont, message, getWidth() / 2 + im_width / 2, getHeight(), Graphics.BOTTOM | Graphics.HCENTER);
    }

    private int drawIcons(Graphics g, Icon draw_img, int im_width) {
        int ims_width = 0;
        if (xstatus_img != null && xstatus_img != XStatus.getStatusImage(XStatus.XSTATUS_NONE) && getWidth() > 129) {
            ims_width = xstatus_img.getWidthEx();
        }
        if (xstatus_img != null && xstatus_img != XStatus.getStatusImage(XStatus.XSTATUS_NONE) && getWidth() > 129) {
            xstatus_img.drawInCenterByRight(g, (getWidth() / 2) - (normalFont.stringWidth(message) / 2), getHeight() - (fontHeight / 2));
        }
        if (draw_img != null) {
            draw_img.drawInCenterByRight(g, (getWidth() / 2) - (normalFont.stringWidth(message) / 2) - ims_width, getHeight() - (fontHeight / 2));
        }
        return im_width;
    }

    public void startTimer() {
        if (status_index != 8) {
            Jimm.getTimerRef().schedule(new TimerTasks(TimerTasks.SC_RESET_TEXT_AND_IMG), 15000);
        }
    }

    public int getWidth() {
        return NativeCanvas.getWidthEx();
    }

    public int getHeight() {
        return NativeCanvas.getHeightEx();
    }
}
*/
