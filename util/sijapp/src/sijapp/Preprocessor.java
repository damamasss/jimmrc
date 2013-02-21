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


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;


public class Preprocessor {


  // Defines
  private Hashtable defines;
  private Hashtable localDefines = new Hashtable();


  // Input stream
  private BufferedReader reader;


  // Output stream
  private BufferedWriter writer;


  // Line counter
  private int lineNum;


  // Stop flag
  private boolean stop;


  // Skip flag/stack
  private boolean skip;
  private Stack skipStack = new Stack();


  // Done flag/stack
  private boolean done;
  private Stack doneStack = new Stack();


  // Constructor
  public Preprocessor(Hashtable defines) {
    this.defines = defines;
  }


  // Evaluate expression
  public Scanner.Token[] evalExpr(Scanner.Token[] tokens) throws SijappException {

    // Create token vector
    Vector t = new Vector();
    for (int i = 0; i < tokens.length; i++) {
      t.add(tokens[i]);
    }

    main: while(true) {

      // T_EXPR_PRS_LEFT T_BOOL T_EXPR_PRS_RIGHT
      for (int i = 0; i < t.size()-2; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        Scanner.Token t3 = (Scanner.Token) t.get(i+2);
        if ((t1.getType() == Scanner.Token.T_EXPR_PRS_LEFT) && (t2.getType() == Scanner.Token.T_BOOL) && (t3.getType() == Scanner.Token.T_EXPR_PRS_RIGHT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, t2);
          continue main;
        }
      }

      // T_IDENT/T_STRING/T_EXPR_DEF T_EXPR_EQ T_IDENT/T_STRING/T_EXPR_DEF
      for (int i = 0; i < t.size()-2; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        Scanner.Token t3 = (Scanner.Token) t.get(i+2);
        if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) this.localDefines.get(t1.getValue());
          String right = (String) this.localDefines.get(t3.getValue());
          if (((left == null) && (right != null)) || ((left != null) && (right == null))) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
            continue main;
          }
          else if ((left == null) && (right == null)) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) this.localDefines.get(t1.getValue());
          String right = (String) t3.getValue();
          if (left == null) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(this.localDefines.containsKey(t1.getValue()))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String right = (String) this.localDefines.get(t1.getValue());
          String left = (String) t1.getValue();
          if (left == null) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) t1.getValue();
          String right = (String) t3.getValue();
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(left.equals(right))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(this.localDefines.containsKey(t3.getValue()))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_EQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
          continue main;
        }
      }

      // T_IDENT/T_STRING/T_EXPR_DEF T_EXPR_NEQ T_IDENT/T_STRING/T_EXPR_DEF
      for (int i = 0; i < t.size()-2; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        Scanner.Token t3 = (Scanner.Token) t.get(i+2);
        if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) this.localDefines.get(t1.getValue());
          String right = (String) this.localDefines.get(t3.getValue());
          if (((left == null) && (right != null)) || ((left != null) && (right == null))) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
            continue main;
          }
          else if ((left == null) && (right == null)) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) this.localDefines.get(t1.getValue());
          String right = (String) t3.getValue();
          if (left == null) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_IDENT) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!this.localDefines.containsKey(t1.getValue()))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String right = (String) this.localDefines.get(t1.getValue());
          String left = (String) t1.getValue();
          if (left == null) {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(true)));
            continue main;
          }
          else {
            t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!left.equals(right))));
            continue main;
          }
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          String left = (String) t1.getValue();
          String right = (String) t3.getValue();
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!left.equals(right))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_STRING) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_IDENT)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!this.localDefines.containsKey(t3.getValue()))));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_STRING)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
          continue main;
        }
        else if ((t1.getType() == Scanner.Token.T_EXPR_DEF) && (t2.getType() == Scanner.Token.T_EXPR_NEQ) && (t3.getType() == Scanner.Token.T_EXPR_DEF)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(false)));
          continue main;
        }
      }

      // T_EXPR_NOT T_BOOL
      for (int i = 0; i < t.size()-1; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        if ((t1.getType() == Scanner.Token.T_EXPR_NOT) && (t2.getType() == Scanner.Token.T_BOOL)) {
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(!((Boolean) t2.getValue()).booleanValue())));
          continue main;
        }
      }

      // T_BOOL T_EXPR_AND T_BOOL
      for (int i = 0; i < t.size()-2; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        Scanner.Token t3 = (Scanner.Token) t.get(i+2);
        if ((t1.getType() == Scanner.Token.T_BOOL) && (t2.getType() == Scanner.Token.T_EXPR_AND) && (t3.getType() == Scanner.Token.T_BOOL)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(((Boolean) t1.getValue()).booleanValue() && ((Boolean) t3.getValue()).booleanValue())));
          continue main;
        }
      }

      // T_BOOL T_EXPR_OR T_BOOL
      for (int i = 0; i < t.size()-2; i++) {
        Scanner.Token t1 = (Scanner.Token) t.get(i);
        Scanner.Token t2 = (Scanner.Token) t.get(i+1);
        Scanner.Token t3 = (Scanner.Token) t.get(i+2);
        if ((t1.getType() == Scanner.Token.T_BOOL) && (t2.getType() == Scanner.Token.T_EXPR_OR) && (t3.getType() == Scanner.Token.T_BOOL)) {
          t.remove(i+2);
          t.remove(i+1);
          t.remove(i);
          t.add(i, new Scanner.Token(Scanner.Token.T_BOOL, new Boolean(((Boolean) t1.getValue()).booleanValue() || ((Boolean) t3.getValue()).booleanValue())));
          continue main;
        }
      }

      break;
    }

    // Return resulting tokens as array
    Scanner.Token[] ret = new Scanner.Token[t.size()];
    t.copyInto(ret);
    return (ret);

  }


  // Evaluate condition (if/unless/elseif/else/end) statement
  public void evalCond(Scanner.Token[] tokens) throws SijappException {

    // Copy non-evaluted tokens to new array
    Scanner.Token[] remainingTokens = new Scanner.Token[tokens.length - 2];
    System.arraycopy(tokens, 2, remainingTokens, 0, tokens.length - 2);

    // Command condition.if
    if ((tokens.length >= 2) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_IF)) {

      // Evaluate expression and check result type
      Scanner.Token[] resultingTokens = this.evalExpr(remainingTokens);
      if ((resultingTokens.length != 1) || (resultingTokens[0].getType() != Scanner.Token.T_BOOL)) {
        throw (new SijappException("Syntax error"));
      }
      boolean result = ((Boolean) resultingTokens[0].getValue()).booleanValue();

      // Save state variables
      this.skipStack.push(new Boolean(this.skip));
      this.doneStack.push(new Boolean(this.done));

      // Set state variables
      this.skip |= !result;
      this.done = result;

    }

    // Command condition.elseif
    else if ((tokens.length >= 2) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_ELSEIF)) {

      // Evaluate expression and check result type
      Scanner.Token[] resultingTokens = this.evalExpr(remainingTokens);
      if ((resultingTokens.length != 1) || (resultingTokens[0].getType() != Scanner.Token.T_BOOL)) {
        throw (new SijappException("Syntax error"));
      }
      boolean result = ((Boolean) resultingTokens[0].getValue()).booleanValue();

      // Set state variables
      this.skip = ((Boolean) this.skipStack.peek()).booleanValue() || this.done || !result;
      this.done |= result;

    }

    // Command condition.else
    else if ((tokens.length == 2) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_ELSE)) {

      // Set state variables
      this.skip = ((Boolean) this.skipStack.peek()).booleanValue() || this.done;
      this.done = true;

    }

    // Command condition.end
    else if ((tokens.length == 2) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_END)) {

      // Reset state variables
      this.skip = ((Boolean) this.skipStack.pop()).booleanValue();
      this.done = ((Boolean) this.doneStack.pop()).booleanValue();

    }

    // Unknown command/Syntax error
    else {
      throw (new SijappException("Syntax error"));
    }

  }


  // Evaluate echo statement
  public void evalEcho(Scanner.Token[] tokens) throws SijappException {
    if ((tokens.length != 1) || (tokens[0].getType() != Scanner.Token.T_STRING)) {
      throw (new SijappException("Syntax error"));
    }
    else if (!this.skip) {
      String s = (String) tokens[0].getValue();
      try {
        writer.write(s, 0, s.length());
        writer.newLine();
      }
      catch (IOException e) {
        throw (new SijappException("An I/O error occured"));
      }
    }
  }


  // Evaluate enviroment statement
  public void evalEnv(Scanner.Token[] tokens) throws SijappException {
    if ((tokens.length == 3) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_DEF) && (tokens[2].getType() == Scanner.Token.T_IDENT)) {
      if (!this.skip) {
        this.localDefines.put(tokens[2].getValue(), "defined");
      }
    }
    else if ((tokens.length == 4) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_DEF) && (tokens[2].getType() == Scanner.Token.T_IDENT) && (tokens[3].getType() == Scanner.Token.T_STRING)) {
      if (!this.skip) {
        this.localDefines.put(tokens[2].getValue(), tokens[3].getValue());
      }
    }
    else if ((tokens.length == 3) && (tokens[0].getType() == Scanner.Token.T_SEP) && (tokens[1].getType() == Scanner.Token.T_CMD2_UNDEF) && (tokens[2].getType() == Scanner.Token.T_IDENT)) {
      if (!this.skip) {
        this.localDefines.remove(tokens[2].getValue());
      }
    }
    else {
      throw (new SijappException("Syntax error"));
    }
  }


  // Evaluate exit statement
  public void evalExit(Scanner.Token[] tokens) throws SijappException {
    if (tokens.length != 0) {
      throw (new SijappException("Syntax error"));
    }
    else if (!this.skip) {
      this.stop = true;
    }
  }


  // Evaluate SiJaPP statement
  public void eval(Scanner.Token[] tokens) throws SijappException {

    // Valid statement consists of at least 3 tokens
    if (tokens.length < 3) {
      throw (new SijappException("Syntax error"));
    }

    // Look for MAGIC_BEGIN and MAGIC_END
    if ((tokens[0].getType() != Scanner.Token.T_MAGIC_BEGIN) || (tokens[tokens.length-1].getType() != Scanner.Token.T_MAGIC_END)) {
      throw (new SijappException("Syntax error"));
    }

    // Copy non-evaluted tokens to new array
    Scanner.Token[] remainingTokens = new Scanner.Token[tokens.length - 3];
    System.arraycopy(tokens, 2, remainingTokens, 0, tokens.length - 3);

    // Delegate evaluation
    switch (tokens[1].getType()) {
      case Scanner.Token.T_CMD1_COND:
        this.evalCond(remainingTokens);
        break;
      case Scanner.Token.T_CMD1_ECHO:
        this.evalEcho(remainingTokens);
        break;
      case Scanner.Token.T_CMD1_ENV:
        this.evalEnv(remainingTokens);
        break;
      case Scanner.Token.T_CMD1_EXIT:
        this.evalExit(remainingTokens);
        break;
      default:
        throw (new SijappException("Syntax error"));
    }

  }


  // Preprocess
  public void run(BufferedReader reader, BufferedWriter writer) throws SijappException {

    // Restore global defines
    this.localDefines.clear();
    for (Enumeration keys = this.defines.keys(); keys.hasMoreElements(); ) {
      String key = new String((String) keys.nextElement());
      String value = new String((String) this.defines.get(key));
      this.localDefines.put(key, value);
    }

    // Save input/output stream
    this.reader = reader;
    this.writer = writer;

    // Reset line counter
    this.lineNum = 1;

    // Reset stop flag
    this.stop = false;

    // Reset skip flag
    this.skip = false;

    // Read until EOF
    try {
      String line;
      while ((line = this.reader.readLine()) != null) {

        // Scan read line for s SiJaPP statement
        Scanner.Token[] tokens = Scanner.scan(line);

        // Get rid of special sijapp comments //# if inside an sijapp statement 
        if (tokens.length == 0 && line.trim().startsWith("//#") && this.doneStack.size() > 0)
        	line = line.trim().substring(3);
        // No statement has been found
        if (tokens.length == 0) {
          if (!skip) {
            this.writer.write(line, 0, line.length());
            this.writer.newLine();
          }
          else
          {
              this.writer.write("//# "+line, 0, line.length()+4);
              this.writer.newLine();
          }
        }
        // A statement has been found
        else {
          this.eval(tokens);
          this.writer.write(line, 0, line.length());
          this.writer.newLine();
          if (this.stop) {
            return;
          }
        }

        // Advance line counter
        this.lineNum++;

      }
    }
    catch (SijappException e) {
      throw (new SijappException(this.lineNum + ": " + e.getMessage()));
    }
    catch (IOException e) {
      throw (new SijappException("An I/O error occured"));
    }

  }


}
