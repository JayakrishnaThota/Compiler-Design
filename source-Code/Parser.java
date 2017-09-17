package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import java.util.ArrayList;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.*;

public class Parser {
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception
	{
		public SyntaxException(String message)
		{
			super(message);
		}
	}
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException
	{
		public UnimplementedFeatureException()
		{
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner)
	{
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	ASTNode parse() throws SyntaxException
	{
		ASTNode ast;
		ast = program();
		matchEOF();
		return ast;
	}

	Expression expression() throws SyntaxException
	{
		Token x = t;
		Token y;
		Expression e1, e2;
		e1 = term();
		while(t.isKind(LT)||t.isKind(LE)||t.isKind(GT)||t.isKind(GE)||t.isKind(EQUAL)||t.isKind(NOTEQUAL))
		{
			y = t;
	        consume();
		    e2 = term();
		    e1 = new BinaryExpression(x,e1,y,e2);
		}
		return e1;
	}

	Expression term() throws SyntaxException
	{
		Token x = t;
		Token y;
		Expression e1, e2;
		e1 = elem();
		while(t.isKind(PLUS)||t.isKind(MINUS)||t.isKind(OR))
		{
			y = t;
			consume();
			e2 = elem();
			e1 = new BinaryExpression(x, e1, y, e2);
		}
		return e1;
	}

	Expression elem() throws SyntaxException
	{
		Token x = t;
		Token y;
		Expression e1, e2;
		e1 = factor();
		while(t.isKind(TIMES)||t.isKind(DIV)||t.isKind(AND)||t.isKind(MOD))
		{
			y = t;
			consume();
			e2 = factor();
			e1 = new BinaryExpression(x,e1,y,e2);
		}
		return e1;
	}

	Expression factor() throws SyntaxException
	{
		Kind kind = t.kind;
		Token x = t;
		switch (kind)
		{
		case IDENT:
		{
			consume();
			return new IdentExpression(x);
		}
		case INT_LIT:
		{
			consume();
			return new IntLitExpression(x);
		}
		case KW_TRUE:
		case KW_FALSE:
		{
			consume();
			return new BooleanLitExpression(x);
		}
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT:
		{
			consume();
			return new ConstantExpression(x);
		}
		case LPAREN:
		{
			consume();
			Expression e = expression();
			match(RPAREN);
			return e;
		}
		default:
			throw new SyntaxException(" Unknown kind encountered " + kind);
		}
	}

	Block block() throws SyntaxException
	{
		Token x = t;
	    match(LBRACE);
	    ArrayList<Dec> d = new ArrayList<Dec>();
		ArrayList<Statement> s = new ArrayList<Statement>();
	    	while(!t.isKind(RBRACE))
	    			{
	    		        if(t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN)||t.isKind(KW_IMAGE)||t.isKind(KW_FRAME))
	    		        {
	    		        	d.add(dec());
	    		        }
	    		        else
	    		        {
	    		        	s.add(statement());
	    		        }
	                }
	    	match(RBRACE);
	    	return new Block(x,d,s);
	     }

	Program program() throws SyntaxException
	{
		ArrayList<ParamDec> a = new ArrayList<>();
		Block b;
		Token x = t;
     	match(IDENT);
     	if(t.isKind(LBRACE))
     	{
     		b = block();
     	}
     		else if(t.isKind(KW_URL)||t.isKind(KW_FILE)||t.isKind(KW_INTEGER)||t.isKind(KW_BOOLEAN))
     		{
     	       	a.add(paramDec());
     	       	while(t.isKind(COMMA))
     	       	{
     	       		consume();
     	       		a.add(paramDec());
     		    }
     	       b =	block();
     	}
     		else
     		{
        		throw new SyntaxException("illegal token"+t.kind+"expected: " +" a statements");
        	}
     	return new Program(x,a,b);
	}

	ParamDec paramDec() throws SyntaxException
	{
		Token x = t;
	 if(t.isKind(KW_URL)) {  consume(); }
	 else if(t.isKind(KW_FILE)) { consume(); }
	 else if(t.isKind(KW_INTEGER)) { consume(); }
	 else if(t.isKind(KW_BOOLEAN)) { consume(); }
	 Token y = t;
	 match(IDENT);
	 return new ParamDec(x, y);
	}

	Dec dec() throws SyntaxException
	{
		Token x = t;
		if(t.isKind(KW_INTEGER)) {  consume(); }
		 else if(t.isKind(KW_BOOLEAN)) { consume(); }
		 else if(t.isKind(KW_IMAGE)) { consume(); }
		 else if(t.isKind(KW_FRAME)) { consume(); }
		Token y = t;
		 match(IDENT);
		 return new Dec(x,y);
	}

	Statement statement() throws SyntaxException
	{
		Token x = t;
		Statement s;
		if(t.isKind(OP_SLEEP))
		{
			match(OP_SLEEP);
			s = new SleepStatement(x, expression());
			match(SEMI);
		}
		else if(t.isKind(KW_WHILE)) {  s = whileStatement();}
		else if(t.isKind(KW_IF))    {  s = ifStatement();   }
		else if(t.isKind(IDENT))
				{
				if (scanner.peek().isKind(ASSIGN)){ s =  assign(); }
				else {  s =  chain(); }
				match(SEMI);
				}
				else {
		        	if (t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) ||t.isKind(KW_XLOC) || t.isKind(KW_YLOC) || t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)){
			          s = chain();
			        	match(SEMI);
		        	}
		        	else{
		        		throw new SyntaxException("illegal token"+t.kind+"expected: " +" a statements");
		        	}
		        	}
		return s;
	}

	AssignmentStatement assign() throws SyntaxException
	{
		Token x = t;
	    match(IDENT);
	    match(ASSIGN);
	    Expression e = expression();
	    return new AssignmentStatement(x, new IdentLValue(x), e);
	}

	WhileStatement whileStatement() throws SyntaxException
	{
	   Token x =t;
	   match(KW_WHILE);
	   match(LPAREN);
	   Expression e =  expression();
	   match(RPAREN);
	   Block b = block();
	   return new WhileStatement(x, e, b);
	}

	IfStatement ifStatement() throws SyntaxException
	{
	   Token x =t;
	   match(KW_IF);
	   match(LPAREN);
	   Expression e = expression();
	   match(RPAREN);
	   Block b = block();
	   return new IfStatement(x,e,b);
	}

	void arrowOp() throws SyntaxException
	{
	 if(t.isKind(ARROW))
	 {
		 match(ARROW);
	 }
	 else if(t.isKind(BARARROW))
	 {
		 match(BARARROW);
	 }
	}

	Chain chain() throws SyntaxException
	{
	 Token x = t;
     Chain c  = chainElem();
     Token y = t;
     arrowOp();
     ChainElem cc = chainElem();
     c = new BinaryChain(x, c, y, cc);
     while(t.isKind(ARROW)||t.isKind(BARARROW))
	  {
    	  y = t;
		  consume();
		  cc = chainElem();
		  c = new BinaryChain(x, c, y, cc);
		}
		return c;
	}

	ChainElem chainElem() throws SyntaxException
	{
		Token x = t;
		if(t.isKind(IDENT))
		{
			consume();
			return new IdentChain(x);
		}

		if(t.isKind(OP_BLUR)||t.isKind(OP_GRAY)||t.isKind(OP_CONVOLVE))
		{
			consume();
			return new FilterOpChain(x, arg());
		}

		if(t.isKind(KW_SHOW)||t.isKind(KW_HIDE)||t.isKind(KW_MOVE)||t.isKind(KW_XLOC)||t.isKind(KW_YLOC))
		{
			consume();
			return new FrameOpChain(x, arg());
		}

		if(t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE))
		{
			consume();
			return new ImageOpChain(x, arg());
		}
		else
    		throw new SyntaxException("illegal token"+t.kind+ "looking for" + "chain element");
	}

	Tuple arg() throws SyntaxException
	{
		ArrayList<Expression> a = new ArrayList<Expression>();
		Token x = t;
		if(t.isKind(LPAREN))
		{
			consume();
			a.add(expression());
			while(t.isKind(COMMA))
			{
				consume();
				a.add(expression());
			}
			match(RPAREN);
		}
		return new Tuple(x, a);
	}

	private Token matchEOF() throws SyntaxException
	{
		if (t.isKind(EOF))
		{
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	private Token match(Kind kind) throws SyntaxException
	{
		if (t.isKind(kind))
		{
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	private Token consume() throws SyntaxException
	{
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}