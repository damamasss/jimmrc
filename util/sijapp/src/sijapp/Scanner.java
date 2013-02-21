/*******************************************************************************
SiJaPP - Simple Java PreProcessor
Copyright (C) 2003  Manuel Linsmayer

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
*******************************************************************************/


package sijapp;


import java.util.Vector;


public class Scanner {


  // Alphabetic character test
  private static boolean isAlpha(char c) {
    return (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')));
  }


  // Numeric character test
  private static boolean isNumeric(char c) {
    return ((c >= '0') && (c <= '9'));
  }


  // Whitespace test
  private static boolean isWhitespace(char c) {
    return ((c == ' ') || (c == '\t'));
  }


  // Exclamation markt test
  private static boolean isExclamationMark(char c) {
    return (c == '!');
  }


  // Quote test
  private static boolean isQuote(char c) {
    return (c == '"');
  }


  // Pound test
  private static boolean isPound(char c) {
    return (c == '#');
  }


  // Ampersand test
  private static boolean isAmpersand(char c) {
    return (c == '&');
  }


  // Opening parenthesis test
  private static boolean isOpeningParenthesis(char c) {
    return (c == '(');
  }


  // Closing parenthesis test
  private static boolean isClosingParenthesis(char c) {
    return (c == ')');
  }


  // Dot test
  private static boolean isDot(char c) {
    return (c == '.');
  }


  // Equals sign test
  private static boolean isEqualsSign(char c) {
    return (c == '=');
  }


  // Backslash test
  private static boolean isBackslash(char c) {
    return (c == '\\');
  }


  // Underscore
  private static boolean isUnderscore(char c) {
    return (c == '_');
  }


  // Vertical bar test
  private static boolean isVerticalBar(char c) {
    return (c == '|');
  }


  // Scanner
  public static Token[] scan(String s) throws SijappException {

    // Look for SiJaPP statement
    if (s.indexOf("#sijapp") == -1) {
      return (new Token[0]);
    }

    // Crop anything to the left
    StringBuffer buf = new StringBuffer(s.substring(s.indexOf("#sijapp")) + " ");

    // Vector
    Vector tokens = new Vector();

    // Required string buffers
    StringBuffer ident = null;
    StringBuffer string = null;

    // State variable
    int state = 0;

    // Exit flag
    boolean exit = false;

    // Scanner main loop
    do {

      // Get next character
      char ch = buf.charAt(0);
      buf.deleteCharAt(0);

      // Deterministic finite automaton, see README file
      switch (state) {
        case 0:
          if (Scanner.isPound(ch)) {
            ident = new StringBuffer();
            state = 1;
            break;
          }
          else if (Scanner.isAlpha(ch) || Scanner.isUnderscore(ch)) {
            ident = new StringBuffer();
            ident.append(ch);
            state = 3;
            break;
          }
          else if (Scanner.isDot(ch)) {
            state = 4;
            break;
          }
          else if (Scanner.isQuote(ch)) {
            string = new StringBuffer();
            state = 5;
            break;
          }
          else if (Scanner.isWhitespace(ch)) {
            state = 8;
            break;
          }
          else if (Scanner.isOpeningParenthesis(ch)) {
            state = 9;
            break;
          }
          else if (Scanner.isClosingParenthesis(ch)) {
            state = 10;
            break;
          }
          else if (Scanner.isEqualsSign(ch)) {
            state = 11;
            break;
          }
          else if (Scanner.isExclamationMark(ch)) {
            state = 12;
            break;
          }
          else if (Scanner.isAmpersand(ch)) {
            state = 14;
            break;
          }
          else if (Scanner.isVerticalBar(ch)) {
            state = 15;
            break;
          }
          else {
            throw (new SijappException("Syntax error"));
          }
        case 1:
          if (Scanner.isAlpha(ch) || Scanner.isUnderscore(ch)) {
            ident.append(ch);
            state = 2;
            break;
          }
          else {
            tokens.add(new Token(Token.T_MAGIC_END, null));
            exit = true;
            buf.insert(0, ch);
            state = 0;
            break;
          }
        case 2:
          if (Scanner.isAlpha(ch) || Scanner.isNumeric(ch) || Scanner.isUnderscore(ch)) {
            ident.append(ch);
            state = 2;
            break;
          }
          else {
            if (ident.toString().equals("sijapp")) {
              tokens.add(new Token(Token.T_MAGIC_BEGIN, null));
              buf.insert(0, ch);
              state = 0;
              break;
            }
            else {
              throw (new SijappException("Syntax error"));
            }
          }
        case 3:
          if (Scanner.isAlpha(ch) || Scanner.isNumeric(ch) || Scanner.isUnderscore(ch)) {
            ident.append(ch);
            state = 3;
            break;
          }
          else {
            if (ident.toString().equals("true")) {
              tokens.add(new Token(Token.T_BOOL, new Boolean(true)));
            }
            else if (ident.toString().equals("false")) {
              tokens.add(new Token(Token.T_BOOL, new Boolean(false)));
            }
            else if (ident.toString().equals("cond") || ident.toString().equals("condition")) {
              tokens.add(new Token(Token.T_CMD1_COND, null));
            }
            else if (ident.toString().equals("echo")) {
              tokens.add(new Token(Token.T_CMD1_ECHO, null));
            }
            else if (ident.toString().equals("env") || ident.toString().equals("environment")) {
              tokens.add(new Token(Token.T_CMD1_ENV, null));
            }
            else if (ident.toString().equals("exit")) {
              tokens.add(new Token(Token.T_CMD1_EXIT, null));
            }
            else if (ident.toString().equals("if") || ident.toString().equals("If")) {
              tokens.add(new Token(Token.T_CMD2_IF, null));
            }
            else if (ident.toString().equals("elseif")) {
              tokens.add(new Token(Token.T_CMD2_ELSEIF, null));
            }
            else if (ident.toString().equals("else")) {
              tokens.add(new Token(Token.T_CMD2_ELSE, null));
            }
            else if (ident.toString().equals("end")) {
              tokens.add(new Token(Token.T_CMD2_END, null));
            }
            else if (ident.toString().equals("def") || ident.toString().equals("define")) {
              tokens.add(new Token(Token.T_CMD2_DEF, null));
            }
            else if (ident.toString().equals("undef") || ident.toString().equals("undefine")) {
              tokens.add(new Token(Token.T_CMD2_UNDEF, null));
            }
            else if (ident.toString().equals("is")) {
              tokens.add(new Token(Token.T_EXPR_EQ, null));
            }
            else if (ident.toString().equals("isnot")) {
              tokens.add(new Token(Token.T_EXPR_NEQ, null));
            }
            else if (ident.toString().equals("not")) {
              tokens.add(new Token(Token.T_EXPR_NOT, null));
            }
            else if (ident.toString().equals("and")) {
              tokens.add(new Token(Token.T_EXPR_AND, null));
            }
            else if (ident.toString().equals("or")) {
              tokens.add(new Token(Token.T_EXPR_OR, null));
            }
            else if (ident.toString().equals("defined")) {
              tokens.add(new Token(Token.T_EXPR_DEF, null));
            }
            else {
              tokens.add(new Token(Token.T_IDENT, ident.toString()));
            }
            buf.insert(0, ch);
            state = 0;
            break;
          }
        case 4:
          tokens.add(new Token(Token.T_SEP, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 5:
          if (Scanner.isQuote(ch)) {
            state = 6;
            break;
          }
          else if (Scanner.isBackslash(ch)) {
            state = 7;
            break;
          }
          else {
            string.append(ch);
            state = 5;
            break;
          }
        case 6:
          tokens.add(new Token(Token.T_STRING, string.toString()));
          buf.insert(0, ch);
          state = 0;
          break;
        case 7:
          if (Scanner.isQuote(ch) || Scanner.isBackslash(ch)) {
            string.append(ch);
            state = 5;
            break;
          }
          else {
            throw (new SijappException("Syntax error"));
          }
        case 8:
          buf.insert(0, ch);
          state = 0;
          break;
        case 9:
          tokens.add(new Token(Token.T_EXPR_PRS_LEFT, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 10:
          tokens.add(new Token(Token.T_EXPR_PRS_RIGHT, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 11:
          tokens.add(new Token(Token.T_EXPR_EQ, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 12:
          if (Scanner.isEqualsSign(ch)) {
            state = 13;
            break;
          }
          else {
            tokens.add(new Token(Token.T_EXPR_NOT, null));
            break;
          }
        case 13:
          tokens.add(new Token(Token.T_EXPR_NEQ, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 14:
          tokens.add(new Token(Token.T_EXPR_AND, null));
          buf.insert(0, ch);
          state = 0;
          break;
        case 15:
          tokens.add(new Token(Token.T_EXPR_OR, null));
          buf.insert(0, ch);
          state = 0;
          break;
      }

    } while (!exit && (buf.length() > 0));

    // Return tokens as array
    Token[] ret = new Token[tokens.size()];
    tokens.copyInto(ret);
    return (ret);

  }


  /****************************************************************************/
  /****************************************************************************/
  /****************************************************************************/


  public static class Token {


    // Tokens
    public static final int T_MAGIC_BEGIN       = 1;
    public static final int T_MAGIC_END         = 2;
    public static final int T_IDENT             = 3;
    public static final int T_STRING            = 4;
    public static final int T_BOOL              = 5;
    public static final int T_SEP               = 6;
    public static final int T_CMD1_COND         = 7;
    public static final int T_CMD1_ECHO         = 8;
    public static final int T_CMD1_ENV          = 9;
    public static final int T_CMD1_EXIT         = 10;
    public static final int T_CMD2_IF           = 11;
    public static final int T_CMD2_ELSEIF       = 12;
    public static final int T_CMD2_ELSE         = 13;
    public static final int T_CMD2_END          = 14;
    public static final int T_CMD2_DEF          = 15;
    public static final int T_CMD2_UNDEF        = 16;
    public static final int T_EXPR_PRS_LEFT     = 17;
    public static final int T_EXPR_PRS_RIGHT    = 18;
    public static final int T_EXPR_EQ           = 19;
    public static final int T_EXPR_NEQ          = 20;
    public static final int T_EXPR_NOT          = 21;
    public static final int T_EXPR_AND          = 22;
    public static final int T_EXPR_OR           = 23;
    public static final int T_EXPR_DEF          = 24;


    /**************************************************************************/


    // Token type
    private int type;


    // Token value
    private Object value;


    // Constructor
    public Token(int type, Object value) {
      this.type = type;
      this.value = value;
    }


    // Returns the token type
    public int getType() {
      return (this.type);
    }


    // Returns the token value
    public Object getValue() {
      return (this.value);
    }


  }


}
