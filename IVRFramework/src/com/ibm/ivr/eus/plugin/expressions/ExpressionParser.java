package com.ibm.ivr.eus.plugin.expressions;
import java.io.*;
import java.util.*;
import com.ibm.ivr.eus.plugin.expressions.Expressions;

/** Parses simple expressions. This parser is machine generated using
 * ExpressionParserGrammar.jj
 * 
 * For example usage see main()
 *  
 * @author Greg
 *
 */
public class ExpressionParser {
    // private static Logger LOGGER = Logger.getLogger(ExpressionPluginImpl.class);
    Expressions handler = null;
    public String logtoken = "ExpressionParser.";
        public static void main(String args []) throws ParseException
        {

        String test1 =
"x = 'gregsam' ; y=\u005c"x'tom'\u005c" ; POS(y, x + y + x)";
                ExpressionParser parser =  new ExpressionParser(new StringReader(test1));
                parser.handler = new Expressions();
                String result = ""+parser.statements();
                System.out.println(test1+"="+result);
        }

  final public Object expression() throws ParseException {
        Object value;
        Object e1 = null, e2 = null, r1 = null, r2 = null;
        Token op = null;
    if(handler.debug) handler.log2("expression->");
    if (jj_2_4(10)) {
      e1 = lvalue();
      jj_consume_token(23);
      e2 = expression();
                                                        value = handler.setVar(""+e1, e2);
    } else if (jj_2_5(10)) {
      r1 = relation();
                         value = r1;
      label_1:
      while (true) {
        if (jj_2_1(10)) {
          ;
        } else {
          break label_1;
        }
        if (jj_2_2(10)) {
          op = jj_consume_token(AND);
        } else if (jj_2_3(10)) {
          op = jj_consume_token(OR);
        } else {
          jj_consume_token(-1);
          throw new ParseException();
        }
                    if(handler.debug) handler.log2(" " + op.image + " ");
        r2 = relation();
                                        value = handler.relation(value, op.image, r2);
      }
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
            //if(handler.debug) handler.log2(")");
                {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public Object relation() throws ParseException {
        Object value;
        Object r1 = null, r2 = null;
        Token op = null;
    r1 = add();
                    value = r1;
    label_2:
    while (true) {
      if (jj_2_6(10)) {
        ;
      } else {
        break label_2;
      }
      op = jj_consume_token(BOOL_OPER);
                                              if(handler.debug) handler.log2(" " + op.image + " ");
      r2 = add();
                                value = handler.relation(value, op.image, r2);
    }
          {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public String literal() throws ParseException {
  Token t;
  String s;
    if (jj_2_7(10)) {
      t = jj_consume_token(LITERAL1);
    } else if (jj_2_8(10)) {
      t = jj_consume_token(LITERAL2);
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
     s = ""+t.image.substring(1, t.image.length() - 1);
            if(handler.debug) handler.log2("literal->['"+s+"']");
        {if (true) return s;}
    throw new Error("Missing return statement in function");
  }

//term:	 number | identifier | function | '(' expression ')' |	literal;
  final public Object actualParameters() throws ParseException {
        Object value;
        Object e1 = null, e2 = null, r1 = null, r2 = null;
        Token op = null;
//	String value = "";
//	String e1 = "", e2 = "", r1 = "", r2 = "";
//	Token op = null;
        Token t = null;
        List results = new ArrayList();
    e1 = expression();
            results.add(e1);
    label_3:
    while (true) {
      if (jj_2_9(10)) {
        ;
      } else {
        break label_3;
      }
      jj_consume_token(24);
            if(handler.debug) handler.log2(",");
      e2 = expression();
                    results.add(e2);
    }
                {if (true) return results.toArray();}
    throw new Error("Missing return statement in function");
  }

  final public Object function() throws ParseException {
  String fn = "";
  Object value;
  Object parms = null;
  Token t;
  if(handler.debug) handler.log2("function->");
    fn = functionname();
    jj_consume_token(25);

    if (jj_2_10(10)) {
      parms = actualParameters();
    } else {
      ;
    }
    jj_consume_token(26);

           value = handler.function(fn, parms);
           // if(handler.debug) handler.log2("==>"+value+")");
                {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public Object term() throws ParseException {
  Object value;
  if(handler.debug) handler.log2("term->");
    if (jj_2_11(10)) {
      value = number();
    } else if (jj_2_12(10)) {
      value = function();
    } else if (jj_2_13(10)) {
      value = literal();
    } else if (jj_2_14(10)) {
      jj_consume_token(25);
      value = expression();
      jj_consume_token(26);
    } else if (jj_2_15(10)) {
      value = identifier();
    } else {
      jj_consume_token(-1);
      throw new ParseException();
    }
                {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public String functionname() throws ParseException {
        Token t;
    if(handler.debug) handler.log2("functionname->");
    t = jj_consume_token(IDENT);
          if(handler.debug) handler.log2(t.image+"(");
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final public String lvalue() throws ParseException {
        Token t;
    if(handler.debug) handler.log2("lvalue->");
    t = jj_consume_token(IDENT);
          if(handler.debug) handler.log2(t.image + " = ");
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }

  final public Object identifier() throws ParseException {
        Token t;
        Object v;
    t = jj_consume_token(IDENT);
    if(handler.debug) handler.log2("identifier->["+t.image+"]");
                if(t.image.equals("true") || t.image.equals("false"))
                        v = Boolean.parseBoolean(t.image);
                else
                        v = handler.getVar(t.image);
          //if(handler.debug) handler.log2(t.image+"->"+v);
          {if (true) return v;}
    throw new Error("Missing return statement in function");
  }

  final public Object statements() throws ParseException {
        Object r1,r2=null;
        Token t, op;
    r1 = expression();
    label_4:
    while (true) {
      if (jj_2_16(10)) {
        ;
      } else {
        break label_4;
      }
      op = jj_consume_token(SEPERATOR);
                                       if(handler.debug) handler.log2(" " + op.image + " ");
      r2 = expression();
    }
          {if (true) return r2 == null ? r1 : r2;}
    throw new Error("Missing return statement in function");
  }

  final public Object add() throws ParseException {
        Object r1,r2, value;
        String opval="+";
        Token t, op;
    r1 = mult();
                   value = r1;
    label_5:
    while (true) {
      if (jj_2_17(10)) {
        ;
      } else {
        break label_5;
      }
      if (jj_2_18(10)) {
        op = jj_consume_token(PLUS);
                  opval = op.image;
      } else if (jj_2_19(10)) {
        op = jj_consume_token(MINUS);
                   opval = op.image;
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
                              if(handler.debug) handler.log2(" " + opval + " ");
      r2 = mult();
                                value = handler.math(value, opval, r2);
    }
          {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public Object mult() throws ParseException {
        Object r1,r2, value;
        Token t, op;
    r1 = term();
                           value = r1;
    label_6:
    while (true) {
      if (jj_2_20(10)) {
        ;
      } else {
        break label_6;
      }
      if (jj_2_21(10)) {
        op = jj_consume_token(MULTIPLY);
      } else if (jj_2_22(10)) {
        op = jj_consume_token(DIVIDE);
      } else {
        jj_consume_token(-1);
        throw new ParseException();
      }
                                                                if(handler.debug) handler.log2(" " + op.image + " ");
      r2 = term();
                                        value = handler.math(value, op.image, r2);
    }
          {if (true) return value;}
    throw new Error("Missing return statement in function");
  }

  final public Object number() throws ParseException {
  Token t;
    t = jj_consume_token(NUMBER);
            if(handler.debug) handler.log2("number->["+t.image+"]");
                if ((""+t.image).indexOf(".") != -1)
                        {if (true) return Double.parseDouble(""+t.image);}

                {if (true) return new Integer(Integer.parseInt(""+t.image));}
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_1(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_2(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_3(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_4(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_5(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_6(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_2_7(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_7(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  private boolean jj_2_8(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_8(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  private boolean jj_2_9(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_9(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  private boolean jj_2_10(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_10(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  private boolean jj_2_11(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_11(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(10, xla); }
  }

  private boolean jj_2_12(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_12(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(11, xla); }
  }

  private boolean jj_2_13(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_13(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(12, xla); }
  }

  private boolean jj_2_14(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_14(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(13, xla); }
  }

  private boolean jj_2_15(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_15(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(14, xla); }
  }

  private boolean jj_2_16(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_16(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(15, xla); }
  }

  private boolean jj_2_17(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_17(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(16, xla); }
  }

  private boolean jj_2_18(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_18(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(17, xla); }
  }

  private boolean jj_2_19(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_19(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(18, xla); }
  }

  private boolean jj_2_20(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_20(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(19, xla); }
  }

  private boolean jj_2_21(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_21(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(20, xla); }
  }

  private boolean jj_2_22(int xla) {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return !jj_3_22(); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(21, xla); }
  }

  private boolean jj_3R_12() {
    if (jj_scan_token(NUMBER)) return true;
    return false;
  }

  private boolean jj_3_8() {
    if (jj_scan_token(LITERAL2)) return true;
    return false;
  }

  private boolean jj_3_16() {
    if (jj_scan_token(SEPERATOR)) return true;
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_16() {
    if (jj_3R_17()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_20()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_18() {
    if (jj_scan_token(IDENT)) return true;
    return false;
  }

  private boolean jj_3R_13() {
    if (jj_3R_18()) return true;
    if (jj_scan_token(25)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_10()) jj_scanpos = xsp;
    if (jj_scan_token(26)) return true;
    return false;
  }

  private boolean jj_3_3() {
    if (jj_scan_token(OR)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_scan_token(AND)) return true;
    return false;
  }

  private boolean jj_3_1() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_2()) {
    jj_scanpos = xsp;
    if (jj_3_3()) return true;
    }
    if (jj_3R_7()) return true;
    return false;
  }

  private boolean jj_3_22() {
    if (jj_scan_token(DIVIDE)) return true;
    return false;
  }

  private boolean jj_3_7() {
    if (jj_scan_token(LITERAL1)) return true;
    return false;
  }

  private boolean jj_3_15() {
    if (jj_3R_15()) return true;
    return false;
  }

  private boolean jj_3_19() {
    if (jj_scan_token(MINUS)) return true;
    return false;
  }

  private boolean jj_3_14() {
    if (jj_scan_token(25)) return true;
    if (jj_3R_9()) return true;
    if (jj_scan_token(26)) return true;
    return false;
  }

  private boolean jj_3R_14() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_7()) {
    jj_scanpos = xsp;
    if (jj_3_8()) return true;
    }
    return false;
  }

  private boolean jj_3_13() {
    if (jj_3R_14()) return true;
    return false;
  }

  private boolean jj_3_12() {
    if (jj_3R_13()) return true;
    return false;
  }

  private boolean jj_3_6() {
    if (jj_scan_token(BOOL_OPER)) return true;
    if (jj_3R_10()) return true;
    return false;
  }

  private boolean jj_3_11() {
    if (jj_3R_12()) return true;
    return false;
  }

  private boolean jj_3R_15() {
    if (jj_scan_token(IDENT)) return true;
    return false;
  }

  private boolean jj_3_18() {
    if (jj_scan_token(PLUS)) return true;
    return false;
  }

  private boolean jj_3_5() {
    if (jj_3R_7()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_1()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_17() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_11()) {
    jj_scanpos = xsp;
    if (jj_3_12()) {
    jj_scanpos = xsp;
    if (jj_3_13()) {
    jj_scanpos = xsp;
    if (jj_3_14()) {
    jj_scanpos = xsp;
    if (jj_3_15()) return true;
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3_4() {
    if (jj_3R_8()) return true;
    if (jj_scan_token(23)) return true;
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3_17() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_18()) {
    jj_scanpos = xsp;
    if (jj_3_19()) return true;
    }
    if (jj_3R_16()) return true;
    return false;
  }

  private boolean jj_3_9() {
    if (jj_scan_token(24)) return true;
    if (jj_3R_9()) return true;
    return false;
  }

  private boolean jj_3R_9() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_4()) {
    jj_scanpos = xsp;
    if (jj_3_5()) return true;
    }
    return false;
  }

  private boolean jj_3_10() {
    if (jj_3R_11()) return true;
    return false;
  }

  private boolean jj_3_21() {
    if (jj_scan_token(MULTIPLY)) return true;
    return false;
  }

  private boolean jj_3R_11() {
    if (jj_3R_9()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_9()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_10() {
    if (jj_3R_16()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_17()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3_20() {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_21()) {
    jj_scanpos = xsp;
    if (jj_3_22()) return true;
    }
    if (jj_3R_17()) return true;
    return false;
  }

  private boolean jj_3R_8() {
    if (jj_scan_token(IDENT)) return true;
    return false;
  }

  private boolean jj_3R_7() {
    if (jj_3R_10()) return true;
    Token xsp;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3_6()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  /** Generated Token Manager. */
  public ExpressionParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[0];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {};
   }
  final private JJCalls[] jj_2_rtns = new JJCalls[22];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public ExpressionParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public ExpressionParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public ExpressionParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ExpressionParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public ExpressionParser(ExpressionParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(ExpressionParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 0; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  static private final class LookaheadSuccess extends java.lang.Error { }
  final private LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0; Token tok = token;
      while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) return;
    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];
      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }
      jj_entries_loop: for (java.util.Iterator<?> it = jj_expentries.iterator(); it.hasNext();) {
        int[] oldentry = (int[])(it.next());
        if (oldentry.length == jj_expentry.length) {
          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              continue jj_entries_loop;
            }
          }
          jj_expentries.add(jj_expentry);
          break jj_entries_loop;
        }
      }
      if (pos != 0) jj_lasttokens[(jj_endpos = pos) - 1] = kind;
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[27];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 0; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 27; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 22; i++) {
    try {
      JJCalls p = jj_2_rtns[i];
      do {
        if (p.gen > jj_gen) {
          jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
          switch (i) {
            case 0: jj_3_1(); break;
            case 1: jj_3_2(); break;
            case 2: jj_3_3(); break;
            case 3: jj_3_4(); break;
            case 4: jj_3_5(); break;
            case 5: jj_3_6(); break;
            case 6: jj_3_7(); break;
            case 7: jj_3_8(); break;
            case 8: jj_3_9(); break;
            case 9: jj_3_10(); break;
            case 10: jj_3_11(); break;
            case 11: jj_3_12(); break;
            case 12: jj_3_13(); break;
            case 13: jj_3_14(); break;
            case 14: jj_3_15(); break;
            case 15: jj_3_16(); break;
            case 16: jj_3_17(); break;
            case 17: jj_3_18(); break;
            case 18: jj_3_19(); break;
            case 19: jj_3_20(); break;
            case 20: jj_3_21(); break;
            case 21: jj_3_22(); break;
          }
        }
        p = p.next;
      } while (p != null);
      } catch(LookaheadSuccess ls) { }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) { p = p.next = new JJCalls(); break; }
      p = p.next;
    }
    p.gen = jj_gen + xla - jj_la; p.first = token; p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }









  
  
  
  
  
  
  
  
  
  
  
  
  
  



  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int COMMENT1 = 5;
  /** RegularExpression Id. */
  int COMMENT2 = 6;
  /** RegularExpression Id. */
  int SEPERATOR = 7;
  /** RegularExpression Id. */
  int PLUS = 8;
  /** RegularExpression Id. */
  int MINUS = 9;
  /** RegularExpression Id. */
  int MULTIPLY = 10;
  /** RegularExpression Id. */
  int DIVIDE = 11;
  /** RegularExpression Id. */
  int BOOL_OPER = 12;
  /** RegularExpression Id. */
  int AND = 13;
  /** RegularExpression Id. */
  int OR = 14;
  /** RegularExpression Id. */
  int BITAND = 15;
  /** RegularExpression Id. */
  int BITOR = 16;
  /** RegularExpression Id. */
  int LITERAL1 = 17;
  /** RegularExpression Id. */
  int LITERAL2 = 18;
  /** RegularExpression Id. */
  int LETTER = 19;
  /** RegularExpression Id. */
  int IDENT = 20;
  /** RegularExpression Id. */
  int DIGIT = 21;
  /** RegularExpression Id. */
  int NUMBER = 22;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\r\"",
    "\"\\t\"",
    "\"\\n\"",
    "<COMMENT1>",
    "<COMMENT2>",
    "\";\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "<BOOL_OPER>",
    "<AND>",
    "<OR>",
    "\"&\"",
    "\"|\"",
    "<LITERAL1>",
    "<LITERAL2>",
    "<LETTER>",
    "<IDENT>",
    "<DIGIT>",
    "<NUMBER>",
    "\"=\"",
    "\",\"",
    "\"(\"",
    "\")\"",
  };


  
  


  
  
//static ParseException pe = new ExpressionParser().new ParseException();
ParseException pe = new ParseException();


/**
 * This exception is thrown when parse errors are encountered.
 * You can explicitly create objects of this exception type by
 * calling the method generateParseException in the generated
 * parser.
 *
 * You can modify this class to customize your error reporting
 * mechanisms so long as you retain the public fields.
 */
public class ParseException extends Exception {
	

  /**
   * The version identifier for this Serializable class.
   * Increment only if the <i>serialized</i> form of the
   * class changes.
   */
  private static final long serialVersionUID = 1L;
  
  String message;

  /**
   * This constructor is used by the method "generateParseException"
   * in the generated parser.  Calling this constructor generates
   * a new object of this type with the fields "currentToken",
   * "expectedTokenSequences", and "tokenImage" set.
   */
  public ParseException(Token currentTokenVal,
                        int[][] expectedTokenSequencesVal,
                        String[] tokenImageVal
                       )
  {
	  
	//super();
	this.message = initialise(currentTokenVal, expectedTokenSequencesVal, tokenImageVal);
    currentToken = currentTokenVal;
    expectedTokenSequences = expectedTokenSequencesVal;
    tokenImage = tokenImageVal;
  }

  @Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}
  public ParseException() {
    super();
  }

  /** Constructor with message. */
  public ParseException(String message) {
    super(message);
  }


  /**
   * This is the last token that has been consumed successfully.  If
   * this object has been created due to a parse error, the token
   * followng this token will (therefore) be the first error token.
   */
  public Token currentToken;

  /**
   * Each entry in this array is an array of integers.  Each array
   * of integers represents a sequence of tokens (by their ordinal
   * values) that is expected at this point of the parse.
   */
  public int[][] expectedTokenSequences;

  /**
   * This is a reference to the "tokenImage" array of the generated
   * parser within which the parse error occurred.  This array is
   * defined in the generated ...Constants interface.
   */
  public String[] tokenImage;

  /**
   * It uses "currentToken" and "expectedTokenSequences" to generate a parse
   * error message and returns it.  If this object has been created
   * due to a parse error, and you do not catch it (it gets thrown
   * from the parser) the correct error message
   * gets displayed.
   */
  private String initialise(Token currentToken,
                           int[][] expectedTokenSequences,
                           String[] tokenImage) {
    String eol = System.getProperty("line.separator", "\n");
    StringBuffer expected = new StringBuffer();
    int maxSize = 0;
    for (int i = 0; i < expectedTokenSequences.length; i++) {
      if (maxSize < expectedTokenSequences[i].length) {
        maxSize = expectedTokenSequences[i].length;
      }
      for (int j = 0; j < expectedTokenSequences[i].length; j++) {
        expected.append(tokenImage[expectedTokenSequences[i][j]]).append(' ');
      }
      if (expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0) {
        expected.append("...");
      }
      expected.append(eol).append("    ");
    }
    String retval = "Encountered \"";
    Token tok = currentToken.next;
    for (int i = 0; i < maxSize; i++) {
      if (i != 0) retval += " ";
      if (tok.kind == 0) {
        retval += tokenImage[0];
        break;
      }
      retval += " " + tokenImage[tok.kind];
      retval += " \"";
      retval += add_escapes(tok.image);
      retval += " \"";
      tok = tok.next;
    }
    retval += "\" at line " + currentToken.next.beginLine + ", column " + currentToken.next.beginColumn;
    retval += "." + eol;
    if (expectedTokenSequences.length == 1) {
      retval += "Was expecting:" + eol + "    ";
    } else {
      retval += "Was expecting one of:" + eol + "    ";
    }
    retval += expected.toString();
    return retval;
  }

  /**
   * The end of line string for this machine.
   */
  protected String eol = System.getProperty("line.separator", "\n");

  /**
   * Used to convert raw characters to their escaped version
   * when these raw version cannot be used as part of an ASCII
   * string literal.
   */
  String add_escapes(String str) {
      StringBuffer retval = new StringBuffer();
      char ch;
      for (int i = 0; i < str.length(); i++) {
        switch (str.charAt(i))
        {
           case 0 :
              continue;
           case '\b':
              retval.append("\\b");
              continue;
           case '\t':
              retval.append("\\t");
              continue;
           case '\n':
              retval.append("\\n");
              continue;
           case '\f':
              retval.append("\\f");
              continue;
           case '\r':
              retval.append("\\r");
              continue;
           case '\"':
              retval.append("\\\"");
              continue;
           case '\'':
              retval.append("\\\'");
              continue;
           case '\\':
              retval.append("\\\\");
              continue;
           default:
              if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                 String s = "0000" + Integer.toString(ch, 16);
                 retval.append("\\u" + s.substring(s.length() - 4, s.length()));
              } else {
                 retval.append(ch);
              }
              continue;
        }
      }
      return retval.toString();
   }

}

/**
 * Describes the input token stream.
 */

public class Token implements java.io.Serializable {
  public int kind;

  /** The line number of the first character of this Token. */
  public int beginLine;
  /** The column number of the first character of this Token. */
  public int beginColumn;
  /** The line number of the last character of this Token. */
  public int endLine;
  /** The column number of the last character of this Token. */
  public int endColumn;

  /**
   * The string image of the token.
   */
  public String image;

  /**
   * A reference to the next regular (non-special) token from the input
   * stream.  If this is the last token from the input stream, or if the
   * token manager has not read tokens beyond this one, this field is
   * set to null.  This is true only if this token is also a regular
   * token.  Otherwise, see below for a description of the contents of
   * this field.
   */
  public Token next;

  /**
   * This field is used to access special tokens that occur prior to this
   * token, but after the immediately preceding regular (non-special) token.
   * If there are no such special tokens, this field is set to null.
   * When there are more than one such special token, this field refers
   * to the last of these special tokens, which in turn refers to the next
   * previous special token through its specialToken field, and so on
   * until the first special token (whose specialToken field is null).
   * The next fields of special tokens refer to other special tokens that
   * immediately follow it (without an intervening regular token).  If there
   * is no such token, this field is null.
   */
  public Token specialToken;

  /**
   * An optional attribute value of the Token.
   * Tokens which are not used as syntactic sugar will often contain
   * meaningful values that will be used later on by the compiler or
   * interpreter. This attribute value is often different from the image.
   * Any subclass of Token that actually wants to return a non-null value can
   * override this method as appropriate.
   */
  public Object getValue() {
    return null;
  }

  /**
   * No-argument constructor
   */
  public Token() {}

  /**
   * Constructs a new token for the specified Image.
   */
  public Token(int kind)
  {
    this(kind, null);
  }

  /**
   * Constructs a new token for the specified Image and Kind.
   */
  public Token(int kind, String image)
  {
    this.kind = kind;
    this.image = image;
  }

  /**
   * Returns the image.
   */
  public String toString()
  {
    return image;
  }

  /**
   * Returns a new Token object, by default. However, if you want, you
   * can create and return subclass objects based on the value of ofKind.
   * Simply add the cases to the switch for all those special cases.
   * For example, if you have a subclass of Token called IDToken that
   * you want to create if ofKind is ID, simply add something like :
   *
   *    case MyParserConstants.ID : return new IDToken(ofKind, image);
   *
   * to the following switch statement. Then you can cast matchedToken
   * variable to the appropriate type and use sit in your lexical actions.
   */
  public Token newToken(int ofKind, String image)
  {
    switch(ofKind)
    {
      default : return new Token(ofKind, image);
    }
  }

  public Token newToken(int ofKind)
  {
    return newToken(ofKind, null);
  }

}



/** Token Manager Error. */
public class TokenMgrError extends Error
{

  /**
   * The version identifier for this Serializable class.
   * Increment only if the <i>serialized</i> form of the
   * class changes.
   */
  private static final long serialVersionUID = 1L;

  /*
   * Ordinals for various reasons why an Error of this type can be thrown.
   */

  /**
   * Lexical error occurred.
   */
  static final int LEXICAL_ERROR = 0;

  /**
   * An attempt was made to create a second instance of a static token manager.
   */
  static final int STATIC_LEXER_ERROR = 1;

  /**
   * Tried to change to an invalid lexical state.
   */
  static final int INVALID_LEXICAL_STATE = 2;

  /**
   * Detected (and bailed out of) an infinite loop in the token manager.
   */
  static final int LOOP_DETECTED = 3;

  /**
   * Indicates the reason why the exception is thrown. It will have
   * one of the above 4 values.
   */
  int errorCode;

  /**
   * Replaces unprintable characters by their escaped (or unicode escaped)
   * equivalents in the given string
   */
  protected  final String addEscapes(String str) {
    StringBuffer retval = new StringBuffer();
    char ch;
    for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i))
      {
        case 0 :
          continue;
        case '\b':
          retval.append("\\b");
          continue;
        case '\t':
          retval.append("\\t");
          continue;
        case '\n':
          retval.append("\\n");
          continue;
        case '\f':
          retval.append("\\f");
          continue;
        case '\r':
          retval.append("\\r");
          continue;
        case '\"':
          retval.append("\\\"");
          continue;
        case '\'':
          retval.append("\\\'");
          continue;
        case '\\':
          retval.append("\\\\");
          continue;
        default:
          if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
            String s = "0000" + Integer.toString(ch, 16);
            retval.append("\\u" + s.substring(s.length() - 4, s.length()));
          } else {
            retval.append(ch);
          }
          continue;
      }
    }
    return retval.toString();
  }

  /**
   * Returns a detailed message for the Error when it is thrown by the
   * token manager to indicate a lexical error.
   * Parameters :
   *    EOFSeen     : indicates if EOF caused the lexical error
   *    curLexState : lexical state in which this error occurred
   *    errorLine   : line number when the error occurred
   *    errorColumn : column number when the error occurred
   *    errorAfter  : prefix that was seen before this error occurred
   *    curchar     : the offending character
   * Note: You can customize the lexical error message by modifying this method.
   */
  protected String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar) {
    return("Lexical error at line " +
          errorLine + ", column " +
          errorColumn + ".  Encountered: " +
          (EOFSeen ? "<EOF> " : ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int)curChar + "), ") +
          "after : \"" + addEscapes(errorAfter) + "\"");
  }

  /**
   * You can also modify the body of this method to customize your error messages.
   * For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
   * of end-users concern, so you can return something like :
   *
   *     "Internal Error : Please file a bug report .... "
   *
   * from this method for such cases in the release version of your parser.
   */
  public String getMessage() {
    return super.getMessage();
  }

  /*
   * Constructors of various flavors follow.
   */

  /** No arg constructor. */
  public TokenMgrError() {
  }

  /** Constructor with message and reason. */
  public TokenMgrError(String message, int reason) {
    super(message);
    errorCode = reason;
  }

  /** Full Constructor. */ 
  public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar, int reason) {
	    this("Lexical error at line " +
	            errorLine + ", column " +
	            errorColumn + ".  Encountered: " +
	            (EOFSeen ? "<EOF> " : ("\"" + String.valueOf(curChar) + "\"") + " (" + (int)curChar + "), ") +
	            "after : \"" + errorAfter + "\"", reason);

	  
	//  this("", reason);
	 // LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar)
  }
  
}








/**
 * An implementation of interface CharStream, where the stream is assumed to
 * contain only ASCII characters (without unicode processing).
 */

public class SimpleCharStream
{
/** Whether parser is static. */
  public static final boolean staticFlag = false;
  int bufsize;
  int available;
  int tokenBegin;
/** Position in buffer. */
  public int bufpos = -1;
  protected int bufline[];
  protected int bufcolumn[];

  protected int column = 0;
  protected int line = 1;

  protected boolean prevCharIsCR = false;
  protected boolean prevCharIsLF = false;

  protected java.io.Reader inputStream;

  protected char[] buffer;
  protected int maxNextCharInd = 0;
  protected int inBuf = 0;
  protected int tabSize = 8;

  protected void setTabSize(int i) { tabSize = i; }
  protected int getTabSize(int i) { return tabSize; }


  protected void ExpandBuff(boolean wrapAround)
  {
    char[] newbuffer = new char[bufsize + 2048];
    int newbufline[] = new int[bufsize + 2048];
    int newbufcolumn[] = new int[bufsize + 2048];

    try
    {
      if (wrapAround)
      {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
        bufcolumn = newbufcolumn;

        maxNextCharInd = (bufpos += (bufsize - tokenBegin));
      }
      else
      {
        System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
        buffer = newbuffer;

        System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
        bufline = newbufline;

        System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
        bufcolumn = newbufcolumn;

        maxNextCharInd = (bufpos -= tokenBegin);
      }
    }
    catch (Throwable t)
    {
      throw new Error(t.getMessage());
    }


    bufsize += 2048;
    available = bufsize;
    tokenBegin = 0;
  }

  protected void FillBuff() throws java.io.IOException
  {
    if (maxNextCharInd == available)
    {
      if (available == bufsize)
      {
        if (tokenBegin > 2048)
        {
          bufpos = maxNextCharInd = 0;
          available = tokenBegin;
        }
        else if (tokenBegin < 0)
          bufpos = maxNextCharInd = 0;
        else
          ExpandBuff(false);
      }
      else if (available > tokenBegin)
        available = bufsize;
      else if ((tokenBegin - available) < 2048)
        ExpandBuff(true);
      else
        available = tokenBegin;
    }

    int i;
    try {
      if ((i = inputStream.read(buffer, maxNextCharInd, available - maxNextCharInd)) == -1)
      {
        inputStream.close();
        throw new java.io.IOException();
      }
      else
        maxNextCharInd += i;
      return;
    }
    catch(java.io.IOException e) {
      --bufpos;
      backup(0);
      if (tokenBegin == -1)
        tokenBegin = bufpos;
      throw e;
    }
  }

/** Start. */
  public char BeginToken() throws java.io.IOException
  {
    tokenBegin = -1;
    char c = readChar();
    tokenBegin = bufpos;

    return c;
  }

  protected void UpdateLineColumn(char c)
  {
    column++;

    if (prevCharIsLF)
    {
      prevCharIsLF = false;
      line += (column = 1);
    }
    else if (prevCharIsCR)
    {
      prevCharIsCR = false;
      if (c == '\n')
      {
        prevCharIsLF = true;
      }
      else
        line += (column = 1);
    }

    switch (c)
    {
      case '\r' :
        prevCharIsCR = true;
        break;
      case '\n' :
        prevCharIsLF = true;
        break;
      case '\t' :
        column--;
        column += (tabSize - (column % tabSize));
        break;
      default :
        break;
    }

    bufline[bufpos] = line;
    bufcolumn[bufpos] = column;
  }

/** Read a character. */
  public char readChar() throws java.io.IOException
  {
    if (inBuf > 0)
    {
      --inBuf;

      if (++bufpos == bufsize)
        bufpos = 0;

      return buffer[bufpos];
    }

    if (++bufpos >= maxNextCharInd)
      FillBuff();

    char c = buffer[bufpos];

    UpdateLineColumn(c);
    return c;
  }

  @Deprecated
  /**
   * @deprecated
   * @see #getEndColumn
   */

  public int getColumn() {
    return bufcolumn[bufpos];
  }

  @Deprecated
  /**
   * @deprecated
   * @see #getEndLine
   */

  public int getLine() {
    return bufline[bufpos];
  }

  /** Get token end column number. */
  public int getEndColumn() {
    return bufcolumn[bufpos];
  }

  /** Get token end line number. */
  public int getEndLine() {
     return bufline[bufpos];
  }

  /** Get token beginning column number. */
  public int getBeginColumn() {
    return bufcolumn[tokenBegin];
  }

  /** Get token beginning line number. */
  public int getBeginLine() {
    return bufline[tokenBegin];
  }

/** Backup a number of characters. */
  public void backup(int amount) {

    inBuf += amount;
    if ((bufpos -= amount) < 0)
      bufpos += bufsize;
  }

  /** Constructor. */
  public SimpleCharStream(java.io.Reader dstream, int startline,
  int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    available = bufsize = buffersize;
    buffer = new char[buffersize];
    bufline = new int[buffersize];
    bufcolumn = new int[buffersize];
  }

  /** Constructor. */
  public SimpleCharStream(java.io.Reader dstream, int startline,
                          int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.Reader dstream)
  {
    this(dstream, 1, 1, 4096);
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader dstream, int startline,
  int startcolumn, int buffersize)
  {
    inputStream = dstream;
    line = startline;
    column = startcolumn - 1;

    if (buffer == null || buffersize != buffer.length)
    {
      available = bufsize = buffersize;
      buffer = new char[buffersize];
      bufline = new int[buffersize];
      bufcolumn = new int[buffersize];
    }
    prevCharIsLF = prevCharIsCR = false;
    tokenBegin = inBuf = maxNextCharInd = 0;
    bufpos = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader dstream, int startline,
                     int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }
  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline,
  int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    this(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream, int startline,
  int startcolumn, int buffersize)
  {
    this(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream, String encoding, int startline,
                          int startcolumn) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream, int startline,
                          int startcolumn)
  {
    this(dstream, startline, startcolumn, 4096);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    this(dstream, encoding, 1, 1, 4096);
  }

  /** Constructor. */
  public SimpleCharStream(java.io.InputStream dstream)
  {
    this(dstream, 1, 1, 4096);
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream, String encoding, int startline,
                          int startcolumn, int buffersize) throws java.io.UnsupportedEncodingException
  {
    ReInit(encoding == null ? new java.io.InputStreamReader(dstream) : new java.io.InputStreamReader(dstream, encoding), startline, startcolumn, buffersize);
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream, int startline,
                          int startcolumn, int buffersize)
  {
    ReInit(new java.io.InputStreamReader(dstream), startline, startcolumn, buffersize);
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream, String encoding) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, 1, 1, 4096);
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream)
  {
    ReInit(dstream, 1, 1, 4096);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream, String encoding, int startline,
                     int startcolumn) throws java.io.UnsupportedEncodingException
  {
    ReInit(dstream, encoding, startline, startcolumn, 4096);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream dstream, int startline,
                     int startcolumn)
  {
    ReInit(dstream, startline, startcolumn, 4096);
  }
  /** Get token literal value. */
  public String GetImage()
  {
    if (bufpos >= tokenBegin)
      return new String(buffer, tokenBegin, bufpos - tokenBegin + 1);
    else
      return new String(buffer, tokenBegin, bufsize - tokenBegin) +
                            new String(buffer, 0, bufpos + 1);
  }

  /** Get the suffix. */
  public char[] GetSuffix(int len)
  {
    char[] ret = new char[len];

    if ((bufpos + 1) >= len)
      System.arraycopy(buffer, bufpos - len + 1, ret, 0, len);
    else
    {
      System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0,
                                                        len - bufpos - 1);
      System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
    }

    return ret;
  }

  /** Reset buffer when finished. */
  public void Done()
  {
    buffer = null;
    bufline = null;
    bufcolumn = null;
  }

  /**
   * Method to adjust line and column numbers for the start of a token.
   */
  public void adjustBeginLineColumn(int newLine, int newCol)
  {
    int start = tokenBegin;
    int len;

    if (bufpos >= tokenBegin)
    {
      len = bufpos - tokenBegin + inBuf + 1;
    }
    else
    {
      len = bufsize - tokenBegin + bufpos + 1 + inBuf;
    }

    int i = 0, j = 0, k = 0;
    int nextColDiff = 0, columnDiff = 0;

    while (i < len && bufline[j = start % bufsize] == bufline[k = ++start % bufsize])
    {
      bufline[j] = newLine;
      nextColDiff = columnDiff + bufcolumn[k] - bufcolumn[j];
      bufcolumn[j] = newCol + columnDiff;
      columnDiff = nextColDiff;
      i++;
    }

    if (i < len)
    {
      bufline[j] = newLine++;
      bufcolumn[j] = newCol + columnDiff;

      while (i++ < len)
      {
        if (bufline[j = start % bufsize] != bufline[++start % bufsize])
          bufline[j] = newLine++;
        else
          bufline[j] = newLine;
      }
    }

    line = bufline[j];
    column = bufcolumn[j];
  }

}






/** Token Manager. */
public class ExpressionParserTokenManager
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 38:
         return jjStartNfaWithStates_0(0, 15, 7);
      case 40:
         return jjStopAtPos(0, 25);
      case 41:
         return jjStopAtPos(0, 26);
      case 42:
         return jjStopAtPos(0, 10);
      case 43:
         return jjStopAtPos(0, 8);
      case 44:
         return jjStopAtPos(0, 24);
      case 45:
         return jjStopAtPos(0, 9);
      case 47:
         return jjStartNfaWithStates_0(0, 11, 39);
      case 59:
         return jjStopAtPos(0, 7);
      case 61:
         return jjStartNfaWithStates_0(0, 23, 0);
      case 124:
         return jjStartNfaWithStates_0(0, 16, 12);
      default :
         return jjMoveNfa_0(1, 0);
   }
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
 final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 45;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 22)
                        kind = 22;
                     jjCheckNAddTwoStates(28, 29);
                  }
                  else if (curChar == 47)
                     jjAddStates(0, 1);
                  else if (curChar == 39)
                     jjCheckNAddStates(2, 4);
                  else if (curChar == 34)
                     jjCheckNAddStates(5, 7);
                  else if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 7;
                  else if (curChar == 33)
                     jjCheckNAdd(0);
                  else if (curChar == 61)
                     jjCheckNAdd(0);
                  break;
               case 39:
                  if (curChar == 47)
                  {
                     if (kind > 6)
                        kind = 6;
                     jjCheckNAdd(44);
                  }
                  else if (curChar == 42)
                     jjCheckNAddTwoStates(40, 42);
                  break;
               case 0:
                  if (curChar == 61 && kind > 12)
                     kind = 12;
                  break;
               case 2:
                  if (curChar == 33)
                     jjCheckNAdd(0);
                  break;
               case 7:
                  if (curChar == 38 && kind > 13)
                     kind = 13;
                  break;
               case 8:
                  if (curChar == 38)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 16:
               case 18:
                  if (curChar == 34)
                     jjCheckNAddStates(5, 7);
                  break;
               case 17:
                  if ((0xfffffffbffffffffL & l) != 0L)
                     jjCheckNAddStates(5, 7);
                  break;
               case 19:
                  if (curChar == 34)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 20:
                  if (curChar == 34 && kind > 17)
                     kind = 17;
                  break;
               case 21:
               case 23:
                  if (curChar == 39)
                     jjCheckNAddStates(2, 4);
                  break;
               case 22:
                  if ((0xffffff7fffffffffL & l) != 0L)
                     jjCheckNAddStates(2, 4);
                  break;
               case 24:
                  if (curChar == 39)
                     jjstateSet[jjnewStateCnt++] = 23;
                  break;
               case 25:
                  if (curChar == 39 && kind > 18)
                     kind = 18;
                  break;
               case 27:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjstateSet[jjnewStateCnt++] = 27;
                  break;
               case 28:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAddTwoStates(28, 29);
                  break;
               case 29:
                  if (curChar == 46)
                     jjCheckNAdd(30);
                  break;
               case 30:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 22)
                     kind = 22;
                  jjCheckNAdd(30);
                  break;
               case 38:
                  if (curChar == 47)
                     jjAddStates(0, 1);
                  break;
               case 40:
                  jjCheckNAddTwoStates(40, 42);
                  break;
               case 41:
                  if (curChar == 47 && kind > 5)
                     kind = 5;
                  break;
               case 42:
                  if (curChar == 42)
                     jjstateSet[jjnewStateCnt++] = 41;
                  break;
               case 43:
                  if (curChar != 47)
                     break;
                  if (kind > 6)
                     kind = 6;
                  jjCheckNAdd(44);
                  break;
               case 44:
                  if ((0xffffffffffffdbffL & l) == 0L)
                     break;
                  if (kind > 6)
                     kind = 6;
                  jjCheckNAdd(44);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((0x7fffffe87fffffeL & l) != 0L)
                  {
                     if (kind > 20)
                        kind = 20;
                     jjCheckNAdd(27);
                  }
                  else if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 12;
                  if (curChar == 71)
                     jjCheckNAddTwoStates(37, 32);
                  else if (curChar == 76)
                     jjCheckNAddTwoStates(32, 34);
                  else if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 14;
                  else if (curChar == 65)
                     jjstateSet[jjnewStateCnt++] = 10;
                  else if (curChar == 69)
                     jjCheckNAdd(5);
                  else if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 3:
                  if (curChar == 69 && kind > 12)
                     kind = 12;
                  break;
               case 4:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 5:
                  if (curChar == 81 && kind > 12)
                     kind = 12;
                  break;
               case 6:
               case 33:
               case 36:
                  if (curChar == 69)
                     jjCheckNAdd(5);
                  break;
               case 9:
                  if (curChar == 68 && kind > 13)
                     kind = 13;
                  break;
               case 10:
                  if (curChar == 78)
                     jjstateSet[jjnewStateCnt++] = 9;
                  break;
               case 11:
                  if (curChar == 65)
                     jjstateSet[jjnewStateCnt++] = 10;
                  break;
               case 12:
                  if (curChar == 124 && kind > 14)
                     kind = 14;
                  break;
               case 13:
                  if (curChar == 124)
                     jjstateSet[jjnewStateCnt++] = 12;
                  break;
               case 14:
                  if (curChar == 82 && kind > 14)
                     kind = 14;
                  break;
               case 15:
                  if (curChar == 79)
                     jjstateSet[jjnewStateCnt++] = 14;
                  break;
               case 17:
                  jjAddStates(5, 7);
                  break;
               case 22:
                  jjAddStates(2, 4);
                  break;
               case 26:
               case 27:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 20)
                     kind = 20;
                  jjCheckNAdd(27);
                  break;
               case 31:
                  if (curChar == 76)
                     jjCheckNAddTwoStates(32, 34);
                  break;
               case 32:
                  if (curChar == 84 && kind > 12)
                     kind = 12;
                  break;
               case 34:
                  if (curChar == 84)
                     jjstateSet[jjnewStateCnt++] = 33;
                  break;
               case 35:
                  if (curChar == 71)
                     jjCheckNAddTwoStates(37, 32);
                  break;
               case 37:
                  if (curChar == 84)
                     jjstateSet[jjnewStateCnt++] = 36;
                  break;
               case 40:
                  jjAddStates(8, 9);
                  break;
               case 44:
                  if (kind > 6)
                     kind = 6;
                  jjstateSet[jjnewStateCnt++] = 44;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 17:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(5, 7);
                  break;
               case 22:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(2, 4);
                  break;
               case 40:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(8, 9);
                  break;
               case 44:
                  if ((jjbitVec0[i2] & l2) == 0L)
                     break;
                  if (kind > 6)
                     kind = 6;
                  jjstateSet[jjnewStateCnt++] = 44;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 45 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
 final int[] jjnextStates = {
   39, 43, 22, 24, 25, 17, 19, 20, 40, 42, 
};

/** Token literal values. */
public  final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, "\73", "\53", "\55", "\52", "\57", 
null, null, null, "\46", "\174", null, null, null, null, null, null, "\75", "\54", 
"\50", "\51", };

/** Lexer state names. */
public  final String[] lexStateNames = {
   "DEFAULT",
};
 final long[] jjtoToken = {
   0x7d7ff81L, 
};
 final long[] jjtoSkip = {
   0x7eL, 
};
 final long[] jjtoSpecial = {
   0x60L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[45];
private final int[] jjstateSet = new int[90];
protected char curChar;
/** Constructor. */
public ExpressionParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public ExpressionParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 45; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = new Token(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token specialToken = null;
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      matchedToken.specialToken = specialToken;
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         matchedToken.specialToken = specialToken;
         return matchedToken;
      }
      else
      {
         if ((jjtoSpecial[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
         {
            matchedToken = jjFillToken();
            if (specialToken == null)
               specialToken = matchedToken;
            else
            {
               matchedToken.specialToken = specialToken;
               specialToken = (specialToken.next = matchedToken);
            }
         }
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}






}
