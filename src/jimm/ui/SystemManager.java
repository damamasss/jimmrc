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
 File: src/jimm/MagicEye.java
 Version: ###VERSION###  Date: ###DATE###
 Author(s): Lavlinskii Roman
 *******************************************************************************/

//#sijapp cond.if modules_SYSMANAGER is "true"#

package jimm.ui;

import DrawControls.CanvasEx;
import DrawControls.TextList;
import DrawControls.VirtualList;
import DrawControls.NativeCanvas;
import jimm.Jimm;
import jimm.JimmUI;
import jimm.Options;
import jimm.comm.StringConvertor;
import jimm.comm.Util;

import javax.microedition.lcdui.*;
import java.util.TimerTask;
import java.util.Timer;

public class SystemManager extends TextList implements CommandListener, MenuListener {

    class ManagerIconBase {
        String name;
        String bottom;
        String pref;
        long[] poits;
        long total;
        long max;

        void add(long free) {
            if (free > total) {
                total = free;
            }
            if (free < max) {
                max = free;
            }
            if (poits == null) {
                poits = new long[]{free};
                return;
            }
            synchronized (poits) {
                long[] temp = poits;
                if (poits.length >= 11) {
                    System.arraycopy(temp, 1, poits, 0, temp.length - 1);
                    poits[temp.length - 1] = free;
                } else {
                    poits = new long[temp.length + 1];
                    System.arraycopy(temp, 0, poits, 0, temp.length);
                    poits[temp.length] = free;
                }
            }
        }
    }

    class ManagerIcon extends DrawControls.Icon {
        ManagerIconBase mib = null;

        public ManagerIcon(int w, int h) {
            super(null);
            width = w;
            height = h;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void drawByLeftTop(Graphics g, int x, int y) {
            int clipX = g.getClipX();
            int clipY = g.getClipY();
            int clipHeight = g.getClipHeight();
            int clipWidth = g.getClipWidth();
            g.clipRect(x, y, width, height);
            g.setColor(0x000000);
            g.fillRect(x, y, width, height);
            int x1, y1, x2, y2;
            int off = width / 10;
            g.setColor(0x333333);
            for (int i = 0; i < 10; i++) {
                x1 = x + off * i;
                y1 = y + height / 10 * i;
                g.drawLine(x1, y, x1, y + height); // vertic
                g.drawLine(x, y1, x + width, y1);//horiz
            }
//            for (int i = 0; i < width; i+=off) {
//                g.drawLine(i, y, i, height);
//                g.drawLine(x, i, width, i);
//            }
//            for (int i = 0; i < height; i+=height/10) {
//                g.drawLine(x, i, width, i);
//            }
            if (mib != null) {
                g.setColor(0x00ff00);
                int len = mib.poits.length - 1;
                for (int i = len; i > 0; i--) {
                    x1 = width - (len - i) * off;
                    y1 = (int) (height * mib.poits[i] / mib.total);
                    x2 = width - (len - i + 1) * off;
                    y2 = (int) (height * mib.poits[i - 1] / mib.total);
                    g.drawLine(x2, y + y2, x1, y + y1);
                }

                g.setColor(0xff0000);
                g.setStrokeStyle(Graphics.DOTTED);
                int maxy = (int) (height * mib.max / mib.total) - 1;
                g.drawLine(x, y + maxy, x + width, y + maxy);
                g.setStrokeStyle(Graphics.SOLID);

                g.setColor(0xffffff);
                drawString(g, CanvasEx.facade, String.valueOf(mib.total / 1024) + ((mib.pref != null) ? mib.pref : ""), x, y, Graphics.TOP | Graphics.LEFT);
                if (mib.name != null) {
                    drawString(g, CanvasEx.facade, mib.name, x + width - 1, y + height - 1, Graphics.BOTTOM | Graphics.RIGHT);
                }
                if (mib.bottom != null) {
                    drawString(g, CanvasEx.facade, mib.bottom, x, y + height - 1, Graphics.BOTTOM | Graphics.LEFT);
                }
            }
            g.setClip(clipX, clipY, clipWidth, clipHeight);
        }
    }

    private final static byte MENU_GC = (byte) 0;

    private CanvasEx prvScreen;
    private ManagerIcon mem;
    //private ManagerIcon cpu;
    private TimerTask live;
    private Timer liveTimer;

    public SystemManager(CanvasEx screen) {
        super("JVM-info", false);
        prvScreen = screen;
        setMode(MODE_TEXT);
        setColorScheme();
        setCommandListener(this);
        creating();
    }

    public void creating() {
        mem = new ManagerIcon(NativeCanvas.getWidthEx() - 4, getDrawHeight() / 2);
        mem.mib = new ManagerIconBase();
        mem.mib.name = "Memory";
        mem.mib.pref = "kB";
        mem.mib.add(Runtime.getRuntime().freeMemory());
        mem.mib.total = Runtime.getRuntime().totalMemory();
        mem.mib.bottom = RAM();
        mem.mib.max = mem.mib.total;
        //addImage(mem, null, -1);

//        cpu = new ManagerIcon(NativeCanvas.getWidthEx() - 4, getDrawHeight() / 2);
//        cpu.mib = new ManagerIconBase();
//        cpu.mib.name = "CPU";
//        cpu.mib.pref = "%";
//        cpu.mib.add(Runtime.getRuntime().freeMemory());
//        cpu.mib.total = 100;
//        cpu.mib.max = cpu.mib.total;
//        //addImage(cpu, null, -1);

        live = new TimerTask() {
            public void run() {
                try {
                    mem.mib.add(Runtime.getRuntime().freeMemory());
                    //cpu.mib.add(cpuLoad());
                    //System.out.println(cpuLoad());
                    repaint();
                } catch (Exception ignored) {
                }
            }
        };
        liveTimer = new Timer();
        liveTimer.schedule(live, 1000, 1000);

        cdouble = (double) (new java.util.Random()).nextInt() * 43213.575199999999D + 1.0D;
        adouble = 1.0D;

        String rom = "";
        try {
            int save;
            int load;
            long l = System.currentTimeMillis();
            Options.safe_save();
            save = (int) (System.currentTimeMillis() - l);
            l = System.currentTimeMillis();
            Options.load();
            load = (int) (System.currentTimeMillis() - l);
            rom = String.valueOf(load) + "ms/" + String.valueOf(save) + "ms";
        } catch (Exception ignored) {
        }

        String[] names =
                Util.explode("Platform" + '|' +
                        "Configuration" + '|' +
                        "Profiles" + '|' +
                        "Locale" + '|' +
                        "Encoding" + '|' +
                        "Screen" + '|' +
                        "Signal" + '|' +
                        "Network availability" + '|' +
                        "Baterry" + '|' +
                        "Java platform" + '|' +
                        "Setup" + '|' +
                        "CPU [ARM 9]" + '|' +
                        "ROM", '|');

        String[] action =
                Util.explode("microedition.platform" + '|' +
                        "microedition.configuration" + '|' +
                        "microedition.profiles" + '|' +
                        "microedition.locale" + '|' +
                        "microedition.encoding" + '|' +
                        Integer.toString(NativeCanvas.getWidthEx()) + "x" + Integer.toString(NativeCanvas.getHeightEx()) + " [" + Jimm.getDisplay().numColors() + "]" + '|' +
                        "com.nokia.mid.networksignal" + '|' +
                        "com.nokia.mid.networkavailability" + '|' +
                        "com.nokia.mid.batterylevel" + '|' +
                        "com.sonyericsson.java.platform" + '|' +
                        jimm.Options.firstDate + '|' +
                        CPU() + '|' +
                        rom, '|');

        addImage(mem, null, -1);
        //System.out.println("uhhh... =)");

        String name;
        String out;
        for (int i = 0; i < names.length; i++) {
            name = "\n" + names[i] + ": ";
            out = (i != 5 & i < 10) ? StringConvertor.getSystemProperty(action[i], "none") : action[i];
            addBigText(name, getColor(COLOR_CC_TEXT), Font.STYLE_BOLD, -1);
            addBigText(out, getColor(COLOR_CAT_TEXT), Font.STYLE_PLAIN, -1);
        }

        //((new StringBuilder()).append(ToolkitDirs.LIB).append("runtime.properties").toString()) //todo

        //addImage(cpu, null, -1);
        // Thread.activeCount()

        addCommandEx(JimmUI.cmdMenu, MENU_TYPE_LEFT_BAR);
        addCommandEx(JimmUI.cmdBack, MENU_TYPE_RIGHT_BAR);
        activate();
    }

    private String RAM() {
        int operations = 100;
        byte ram[] = new byte[operations];
        long currTime = System.currentTimeMillis();
        for (int j = 0; j < operations; j++)
            for (int i = 0; i < operations; i++)
                for (int r = 0; r < operations; r++)
                    ram[r] = 100;

        long min = System.currentTimeMillis();
        long out = (1000 * (operations * operations * operations) * 2) / (min - currTime);
        return String.valueOf(out / 1000) + " KB/s";   // 1 000 Byte
    }

    private double adouble;
    private double cdouble;

    private String CPU() {
        double operations = 100000D;
        double herz = 0.0D;
        for (int y = 0; y < 3; y++) {
            long currTime = System.currentTimeMillis();
            for (long c = 0L; (double) c < operations; c++)
                adouble += cdouble;

            double msec = System.currentTimeMillis() - currTime;
            herz += 5D * 5D * 2D * (2D * operations) / msec; // ~~~  // 5D * 5D * 2D - offset
        }
        return String.valueOf((int) herz / 1000) + " MHz";
    }

//    class ThreadEx extends Thread {
//        ThreadEx() {
//        }
//
//        public void run() {
//            for (int i = 0; i < 10000; i++) {
//                try {
//                    yield();
//                } catch (Exception exception) {
//                }
//            }
//            currentThread++;
//        }
//    }
//
//    int currentThread = 0;
//    ThreadEx[] threads = new ThreadEx[3];
//
//    private String DoThreads() {
//		for (int b = 0; b < 3; b++)
//			threads[b] = new ThreadEx();
//
//        long currTime = System.currentTimeMillis();
//        for (int b = 0; b < 3; b++) {
//			threads[b].start();
//        }
//
//		while (currentThread != 3) {
//			try {
//				java.lang.Thread.yield();
//			} catch (java.lang.Exception exception) {
//            }
//        }
//        long min = System.currentTimeMillis();
//        long out = (10000 * 3) / (min - currTime);
//        return String.valueOf(out) + " KT/s";
//	}

//    public class Methods {
//        private Methods a;
//
//        public Methods() {
//        }
//
//        protected void go() {
//            a = new Methods();
//            long ms = System.currentTimeMillis();
//            for (int i = 0; i < 1000000; i++)
//                b();
//
//            SystemLog("Same method", String.valueOf(100000 / (System.currentTimeMillis() - ms)) + " KM/s");
//            ms = System.currentTimeMillis();
//            for (int k = 0; k < 1000000; k++)
//                a.b();
//
//            SystemLog("Other method", String.valueOf(100000 / (System.currentTimeMillis() - ms)) + " KM/s");
//        }
//
//        public void b() {
//        }
//    }

//    private String writtingRAM() {
//        //System.out.println("writtingRAM  ");
//        int operations = 100000;
//        byte segment = 100;
//        byte ram[] = new byte[operations];
//        long currTime = System.currentTimeMillis();
//        for (int c = 0; c < operations; c++)
//            ram[c] = segment;
//
//        long min = System.currentTimeMillis();
//        //System.out.println("ms writtingRAM " + (min - currTime));
//        double herz = (double)(1000 * operations * 2) / (min - currTime);
//        //System.out.println("writtingRAM  oh");
//		return String.valueOf((int)herz);
//    }


//    double setHz = 0.0D;
//
//    private int cpuLoad() {
//        setHz += getHz(10000D);
//        setHz += getHz(10000D);
//        setHz += getHz(10000D);
//        setHz /= 3D;
//        return (int) (100D * ((double) getCPU() / setHz));
//    }
//
//    private double getHz(double operations) {
//        double a = 0.1111D;
//        double b = 0.22220000000000001D;
//        double d = a + b;
//        long currTime = (new java.util.Date()).getTime();
//        for (long c = 0L; (double) c < operations; c++) {
//            double summ = d;
//        }
//
//        double delta = (new java.util.Date()).getTime() - currTime;
//        currTime = (new java.util.Date()).getTime();
//        for (long c = 0L; (double) c < operations; c++) {
//            double summ = a + b;
//        }
//
//        double min = (new java.util.Date()).getTime() - currTime;
//        min -= delta;
//        double herz = (1000000D * operations) / min;
//        herz /= 1000000D;
//        double toShow = (long) herz;
//        if (toShow == 0.0D)
//            toShow = herz;
//        return toShow;
//    }
//
//    private int getCPU() {
//        double a = 0.1111D;
//        double b = 0.22220000000000001D;
//        double d = a + b;
//        double operations = 5000D;
//        double maxpower = 1.0D;
//        double herz = 0.0D;
//        maxpower = setHz;
//        long currTime = (new java.util.Date()).getTime();
//        for (long c = 0L; (double) c < operations; c++) {
//            double summ = d;
//        }
//
//        double delta = (new java.util.Date()).getTime() - currTime;
//        currTime = (new java.util.Date()).getTime();
//        for (long c = 0L; (double) c < operations; c++) {
//            double summ = a + b;
//        }
//
//        double min = (new java.util.Date()).getTime() - currTime;
//        if (min > delta)
//            min -= delta;
//        if (min != 0.0D)
//            herz = (1000000D * operations) / min;
//        herz /= 1000000D;
//        double toShow = (long) herz;
//        if (toShow == 0.0D)
//            toShow = herz;
//        if (toShow > maxpower)
//            toShow = maxpower;
//        int val = 100 - (int) ((100D * toShow) / maxpower);
//        return val;
//    }

    private void showMenu() {
        Menu menu = new Menu(this);
        menu.addMenuItem("GC", MENU_GC);
        menu.setMenuListener(this);
        Jimm.setDisplay(menu);
    }

    public void menuSelect(Menu menu, byte action) {
        switch (action) {
            case MENU_GC:
                menu.back();
                //mib.add(Runtime.getRuntime().freeMemory());
                //repaint();
                System.gc();
                return;
        }
        if (menu != null) {
            menu.back();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == JimmUI.cmdBack) {
            if (prvScreen != null) {
                Jimm.setDisplay(prvScreen);
                prvScreen = null;
            } else {
                Jimm.getContactList().activate();
            }
            if (live != null) {
                live.cancel();
                live = null;
            }
            if (liveTimer != null) {
                liveTimer.cancel();
                liveTimer = null;
            }
        } else if (c == JimmUI.cmdMenu) {
            showMenu();
        }
    }
}
//#sijapp cond.end#