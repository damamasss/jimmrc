//#sijapp cond.if modules_SBOLTUN is "true"#
package jimm.chat;

import jimm.comm.Util;
import jimm.comm.StringConvertor;
import jimm.ContactItem;
import jimm.JimmUI;
import jimm.Options;

import java.util.Vector;

/**
 * Created [16.03.2011, 11:38:11]
 * Develop by Lavlinsky Roman on 2011
 */
public class sBoltun implements Runnable {

    class Input {
        String input;
        ContactItem item;
    }

    class Reply {
        byte type;
        byte intonation;
        String[] head;
        String[] data;
    }

    // [] - Совпадают: интоннация, порядок слов, знаки препинания, а также пробелы
    // () - Совпадают интоннация. Имеются: все слова реплики. Не использовать: знаки препинания
    // {} - Совпадают интоннация. Имеются: одно из слов реплики. Не использовать: знаки препинания
    // В конце реплики должен ОБЯЗАТЕЛЬНО стоять символ ! ? . // его отсутствие равно точке
    // Ответы должны прописываться через символ ';', количество ответов желательно должно быть равно: 1, 3, 7, 15 для улучшения рандома 

    //final static byte GENERAL = (byte) 1;
    //final static byte PRIORITY = (byte) 2;
    //final static byte KEY = (byte) 3;
    //final static byte TYPICAL = (byte) 4;
    //final static byte SPECIAL = (byte) 5;
    //final static byte FULL = (byte) 6;
    //final static byte INIT = (byte) 7;

    public Vector mindVector = new Vector();
    public Vector inputVector = new Vector();

    public void parseMind() {
        String[] array = Util.explode(Util.getStringAsStream("/sboltun.mind"), '\n');
        StringBuffer iarray = new StringBuffer();
        char ch;
        int lastChar;
        Reply reply;
        //System.out.println("array=" + array.length);
        for (int i = 0; i < array.length - 1; i += 2) {
            iarray.append(Util.removeCr(array[i].trim()));
            //System.out.println(i);
            //System.out.println("iarray=" + iarray.toString());
            reply = new Reply();
            lastChar = iarray.length() - 1;
            iarray.deleteCharAt(lastChar);
            lastChar = iarray.length() - 1;
            ch = iarray.charAt(lastChar);
            //System.out.println("ch=" + ch);
            reply.intonation = (byte) ch;
            switch (ch) {
                case '?':
                case '!':
                case '.':
                    iarray.deleteCharAt(lastChar);
                    break;
                default:
                    reply.intonation = '.';
                    break;
            }
            //System.out.println("iarray del=" + iarray.toString());
            ch = iarray.charAt(0);
            iarray.deleteCharAt(0);
            //System.out.println("ch=" + ch);
            reply.type = (byte) ch;
            switch (ch) {
                case '(':
                case '{':
                    reply.head = Util.explode(iarray.toString(), ' ');
                    for (int h = 0; h < reply.head.length; h++) {
                        reply.head[h] = StringConvertor.clearText(reply.head[h]);
                    }
                    break;

                case '[':
                    reply.head = new String[]{iarray.toString()};
                    break;

            }
            //System.out.println("iarray=" + iarray.toString());
            reply.data = Util.explode(Util.removeCr(array[i + 1]), ';');
            //System.out.println("data=" + reply.data.length);
            mindVector.addElement(reply);
            iarray.setLength(0);
        }
        System.out.println("LOADED=" + mindVector.size());
    }

    public void input(String inpmessage, ContactItem item) {
        if (mindVector.size() == 0) return;
        Input input = new Input();
        input.input = StringConvertor.toLowerCase(inpmessage.trim());
        input.item = item;
        synchronized (inputVector) {
            inputVector.addElement(input);
            if (inputVector.size() <= 1)
                new Thread(this).start();
        }

    }

    public byte intonation(String input) {
        if (input.endsWith("!")) {
            return '!';
        }
        if (input.endsWith("?")) {
            return '?';
        }
        return '.';
//        if (input.indexOf("!") > 0) {
//            return '!';
//        }
//        if (input.indexOf("?") > 0) {
//            return '?';
//        }
//        return '.';
    }

    public void sendMessage(String[] string, ContactItem contactItem) {
        int choise = Math.min(Math.max((new java.util.Random()).nextInt() & string.length, 0), string.length - 1);
        //System.out.println("SEND=" + string[choise]);
        JimmUI.sendMessage(string[choise], contactItem);
    }

    public void run() {
        while (inputVector.size() > 0) {
//#sijapp cond.if modules_DEBUGLOG is "true" #
            System.out.println("sBoltun Think!");
//#sijapp cond.end #            
            try {
                Thread.sleep(Options.getInt(Options.OPTION_SBOLTUN_SLEEP) * 1000);
            } catch (InterruptedException ignored) {
            }
            Input temp = (Input) inputVector.elementAt(0);
            Reply answer;
            byte intonation = intonation(temp.input);
            String[] words = Util.explode(temp.input, ' ');
            for (int h = 0; h < words.length; h++) {
                words[h] = StringConvertor.clearText(words[h]);
            }
            System.out.println("INPUT MESSAGE=" + temp.input);
            byte find;
            boolean stop;
            for (int i = 0; i < mindVector.size(); i++) {
                stop = false;
                answer = (Reply) mindVector.elementAt(i);
                if (answer.intonation == intonation) {
                    switch (answer.type) {
                        case (byte) '(':
                            find = 0;
                            for (int p = 0; p < words.length; p++) {                                
                                for (int k = 0; k < answer.head.length; k++) {
                                    if (words[p].equals(answer.head[k])) {
                                        stop = (++find >= answer.head.length);

                                        //System.out.println((char) answer.intonation);
                                        System.out.print("FIND '" + (char) answer.type + "' = " + words[p]);
                                        System.out.print(" (" + stop + "); apply = " + find + "/" + answer.head.length);
                                        for (int h = 0; h < answer.head.length; h++) {
                                            System.out.print(" w" + h + " = " + answer.head[h]);    
                                        }
                                        System.out.println();
                                        //System.out.println("STOP = " + stop);
                                        //System.out.println("FINDED = " + find);
                                        //System.out.println("LEN = " + answer.head.length);

                                        if (stop) {

                                            System.out.println((char) answer.intonation);
                                            System.out.println("SEND '" + (char) answer.type + "' = " + words[p]);

                                            sendMessage(answer.data, temp.item);
                                            break;
                                        }
                                    }
                                }
                                if (stop) {
                                    break;
                                }
                            }
                            break;

                        case (byte) '[':
                            if (temp.input.equals(answer.head[0])) {
                                stop = true;

                                System.out.println((char) answer.intonation);
                                System.out.println("SEND '" + (char) answer.type + "' = " + temp.input);

                                sendMessage(answer.data, temp.item);
                            }
                            break;

                        case (byte) '{':
                            for (int p = 0; p < words.length; p++) {
                                for (int k = 0; k < answer.head.length; k++) {
                                    if (stop = words[p].equals(answer.head[k])) {

                                        System.out.println((char) answer.intonation);
                                        System.out.println("SEND '" + (char) answer.type + "' = " + words[p]);

                                        sendMessage(answer.data, temp.item);
                                        break;
                                    }
                                }
                                if (stop) {
                                    break;
                                }
                            }
                            break;
                    }
                }
                if (stop) {
                    break;
                }
            }
            synchronized (inputVector) {
                inputVector.removeElementAt(0);
            }
            System.out.println("THREAD FIN");
            System.out.println(" ");
        }
    }
}
//#sijapp cond.end#