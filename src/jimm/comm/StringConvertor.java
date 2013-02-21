/*
 * StringConvertor.java
 *
 * Created on 6 Февраль 2007 г., 19:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimm.comm;

/**
 * @author vladimir
 */
public class StringConvertor {

    public static String replaceStr(String text, String from, String to) {
        int fromSize = from.length();
        for (; ;) {
            int pos = text.indexOf(from);
            if (pos == -1) break;
            text = text.substring(0, pos) + to + text.substring(pos + fromSize, text.length());
        }
        return text;
    }

    public static String toLowerCase(String s) {
        char[] chars = s.toCharArray();
        for (int i = s.length() - 1; i >= 0; i--) {
            chars[i] = toLowerCase(chars[i]);
        }
        String res = new String(chars);
        return res.equals(s) ? s : res;
    }

    public static char toLowerCase(char c) {
        c = Character.toLowerCase(c);
        if (c >= 'A' && c <= 'Z' || c >= '\300' && c <= '\326' || c >= '\330' && c <= '\336' || c >= '\u0400' && c <= '\u042F') {
            if (c <= 'Z' || c >= '\u0410' && c <= '\u042F') {
                return (char) (c + 32);
            }
            if (c < '\u0410') {
                return (char) (c + 80);
            }
            return (char) (c + 32);
        }
        return c;
    }

    public static String toUpperCase(String s) {
        char[] chars = s.toCharArray();
        for (int i = s.length() - 1; i >= 0; i--) {
            chars[i] = toUpperCase(chars[i]);
        }
        String res = new String(chars);
        return res.equals(s) ? s : res;
    }

    public static char toUpperCase(char c) {
        c = Character.toUpperCase(c);
        if ((c >= 'a' && c <= 'z') || (c >= '\337' && c <= '\366') || (c >= '\370' && c <= '\377') || (c >= '\u0430' && c <= '\u045F')) {
            if ((c <= 'z') || (c >= '\u0430' && c <= '\u044F')) {
                return (char) (c - 32);
            }
            if (c > '\u042F') {
                return (char) (c - 80);
            }
            return (char) (c - 32);
        }
        return c;
    }

    private String convertChar(String str, String[] src, String[] dest) {
        for (int i = src.length - 1; i >= 0; i--) {
            if (src[i].equals(str)) {
                return dest[i];
            }
            if (src[i].equals(toLowerCase(str))) {
                return toUpperCase(dest[i]);
            }
        }
        return null;
    }

    private String convertText(String str, String[] src, String[] dest) {
        StringBuffer buf = new StringBuffer();
        int i = 0;
        while (i < str.length()) {
            String ch = "";
            int endPos = Math.min(i + src[0].length(), str.length());
            while (endPos > i) {
                ch = str.substring(i, endPos);
                String trans = convertChar(ch, src, dest);
                if (trans != null) {
                    buf.append(trans);
                    break;
                } else if (ch.length() == 1) {
                    buf.append(ch);
                    break;
                }
                endPos--;
            }
            i += ch.length();
        }
        return buf.toString();
    }

    private final String[] deTransSrc = Util.explode("tsya|tsja|shch|shh|ehn|zh|jo|jj|j|h|kh|eh|e-|yu|yu|ju|ya|ja|ts|c|._|'|ch|sh|a|b|w|v|g|d|e|z|i|k|l|m|n|o|p|r|s|t|u|f|\"|y|*", '|');
    private final String[] deTransDest = Util.explode("\u0442\u0441\u044f|\u0442\u0441\u044f|\u0449|\u0449|\u0435\u0445\u043d|\u0436|\u0451|\u0439|\u0439|\u0445|\u0445|\u044d|\u044d|\u044e|\u044e|\u044e|\u044f|\u044f|\u0446|\u0446|\u044c|\u044c|\u0447|\u0448|\u0430|\u0431|\u0432|\u0432|\u0433|\u0434|\u0435|\u0437|\u0438|\u043a|\u043b|\u043c|\u043d|\u043e|\u043f|\u0440|\u0441|\u0442|\u0443|\u0444|\u044a|\u044b|'", '|');

    public String detransliterate(String str) {
        try {
            return convertText(str, deTransSrc, deTransDest);
        } catch (Exception ignored) {
        }
        return str;
    }

    private final String[] transSrc = Util.explode("\u0430|\u0431|\u0432|\u0433|\u0434|\u0435|\u0451|\u0436|\u0437|\u0438|\u0439|\u043a|\u043b|\u043c|\u043d|\u043e|\u043f|\u0440|\u0441|\u0442|\u0443|\u0444|\u0445|\u0446|\u0447|\u0448|\u0449|\u044a|\u044b|\u044c|\u044d|\u044e|\u044f|'", '|');
    private final String[] transDest = Util.explode("a|b|v|g|d|e|jo|zh|z|i|jj|k|l|m|n|o|p|r|s|t|u|f|kh|c|ch|sh|shh|\"|y|'|eh|yu|ya|*", '|');

    public String transliterate(String str) {
        try {
            return convertText(str, transSrc, transDest);
        } catch (Exception ignored) {
        }
        return str;
    }

    public static String getString(String str) {
        return (str == null) ? "" : str;
    }

    public static String clearText(String s) {
        StringBuffer sb = new StringBuffer();
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '\u0430' && c <= '\u044F')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isEmpty(String value) {
        return (null == value) || (0 == value.length());
    }

    public static String getSystemProperty(String key, String defval) {
        String res = null;
        try {
            res = System.getProperty(key);
        } catch (Exception ignored) {
        }

        return isEmpty(res) ? defval : res;
    }
}