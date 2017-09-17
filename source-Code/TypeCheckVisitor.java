package cop5556sp17;
import java.util.*;
import java.lang.*;
import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.*;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor
{
	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception
	{
		TypeCheckException(String message)
		{
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	//Done
	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		Chain chain = binaryChain.getE0();
		chain.visit(this, arg);
		ChainElem elem = binaryChain.getE1();
		elem.visit(this, arg);
		Token token = binaryChain.getArrow();
		if (token.isKind(ARROW))
		{
			if (chain.getType().equals(URL) && elem.getType().equals(IMAGE))
				binaryChain.setType(IMAGE);

			else if (chain.getType().equals(FILE) && elem.getType().equals(IMAGE))
			     	binaryChain.setType(IMAGE);

			else if (chain.getType().equals(FRAME) &&
					 (elem.getFirstToken().isKind(KW_XLOC) || elem.getFirstToken().isKind(KW_YLOC)))
				binaryChain.setType(INTEGER);

			else if (chain.getType().equals(FRAME) &&
					(elem.getFirstToken().isKind(KW_SHOW) || elem.getFirstToken().isKind(KW_HIDE) || elem.getFirstToken().isKind(KW_MOVE) ))
			    	binaryChain.setType(FRAME);

			else if (chain.getType().equals(IMAGE) && (elem.getFirstToken().isKind(OP_WIDTH) || elem.getFirstToken().isKind(OP_HEIGHT) ))
 					binaryChain.setType(INTEGER);

			else if (chain.getType().equals(IMAGE) && elem.getType().equals(FRAME))
			         binaryChain.setType(FRAME);

			else if (chain.getType().equals(IMAGE) && elem.getType().equals(FILE))
		             binaryChain.setType(NONE);

			else if (chain.getType().equals(TypeName.IMAGE) &&
					(elem.getFirstToken().isKind(OP_GRAY) || elem.getFirstToken().isKind(OP_BLUR) || elem.getFirstToken().isKind(OP_CONVOLVE)))

				     binaryChain.setType(IMAGE);

			else if (chain.getType().equals(IMAGE) && (elem.getFirstToken().isKind(KW_SCALE)))
			         binaryChain.setType(IMAGE);

			else if (chain.getType().equals(IMAGE) && elem.getFirstToken().isKind(IDENT)&& elem.getType().equals(IMAGE))
					 binaryChain.setType(IMAGE);

			else if (chain.getType().equals(INTEGER) && elem.getFirstToken().isKind(IDENT)&& elem.getType().equals(INTEGER))
			         binaryChain.setType(INTEGER);
			else
				throw new TypeCheckException("Error");

		}
		else if(token.isKind(BARARROW))
		{
			if (elem.getType().equals(TypeName.IMAGE) &&(elem.getFirstToken().isKind(OP_GRAY) || elem.getFirstToken().isKind(OP_BLUR) || elem.getFirstToken().isKind(OP_CONVOLVE)))
				binaryChain.setType(IMAGE);
			else
				throw new TypeCheckException("Error : visitBinaryChain");
		}
		else
			throw new TypeCheckException("Error : visitBinaryChain");

		return null;
	}

	//Done
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		Expression expr1, expr2;
		expr1 = binaryExpression.getE0();
		expr2 = binaryExpression.getE1();
		expr1.visit(this, arg);
		expr2.visit(this, arg);
		Token token = binaryExpression.getOp();
		switch (token.kind)
		{
		case PLUS:
		case MINUS:
			if (expr1.getType().equals(INTEGER) && expr2.getType().equals(INTEGER))
				binaryExpression.setType(INTEGER);
			else if (expr1.getType().equals(IMAGE) && expr2.getType().equals(IMAGE))
				binaryExpression.setType(IMAGE);
			else
				throw new TypeCheckException("Error : Type Mismatch, visitBinaryExpression, PLUS, MINUS");
		break;

		case TIMES:
			if (expr1.getType().equals(INTEGER) && expr2.getType().equals(INTEGER))
				binaryExpression.setType(INTEGER);
			else if (expr1.getType().equals(INTEGER) && expr2.getType().equals(IMAGE))
				binaryExpression.setType(IMAGE);
			else if (expr1.getType().equals(IMAGE) && expr2.getType().equals(INTEGER))
				binaryExpression.setType(IMAGE);
			else
				throw new TypeCheckException("Error : Type Mismatch, visitBinaryExpression, Times");
		break;

		case DIV:
		case MOD:
			if ((expr1.getType().equals(INTEGER)) && (expr2.getType().equals(INTEGER)))
				binaryExpression.setType(INTEGER);
			else if ((expr1.getType().equals(IMAGE)) && (expr2.getType().equals(INTEGER)))
				binaryExpression.setType(IMAGE);
			else
				throw new TypeCheckException("Error : Type Mismatch, visitBinaryExpression, DIV");
		break;

		case LT:
		case GT:
		case LE:
		case GE:
			if (expr1.getType().equals(INTEGER) && expr2.getType().equals(INTEGER))
				binaryExpression.setType(BOOLEAN);
			else if (expr1.getType().equals(BOOLEAN) && expr2.getType().equals(BOOLEAN))
				binaryExpression.setType(BOOLEAN);
			else
				throw new TypeCheckException("Error : Type Mismatch, visitBinaryExpression, Relational Operators");
		break;

		case EQUAL:
		case NOTEQUAL:
			if (expr1.getType().equals(expr2.getType()))
				binaryExpression.setType(BOOLEAN);
			else
				throw new TypeCheckException("Error : Type Mismatch, visitBinaryExpression, Equals and Notequals");
		break;
		case AND:
		case OR:
				if (expr1.getType().equals(TypeName.BOOLEAN) && expr2.getType().equals(TypeName.BOOLEAN))
				    binaryExpression.setType(BOOLEAN);
				else
					throw new TypeCheckException("Type check Error");

		break;

		default:
			throw new TypeCheckException("Error : visitBinaryExpression");
		}
		return null;
	}

	//Done
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception
	{
		int i,j;
		symtab.enterScope();
		ArrayList<Dec> d = block.getDecs();
		ArrayList<Statement> s = block.getStatements();
		for (i=0,j=0; i<d.size() && j<s.size();)
		{
			if (d.get(i).firstToken.pos < s.get(j).firstToken.pos)
			{
				d.get(i).visit(this, arg);
				i++;
			}
			else
			{
				s.get(j).visit(this, arg);
				j++;
			}
		}

		for (; i<d.size(); i++)
		{
			d.get(i).visit(this, arg);
		}

		for (; j<s.size(); j++)
		{
			s.get(j).visit(this, arg);
		}
		symtab.leaveScope();
		return null;
	}

	//Done
	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		booleanLitExpression.setType(BOOLEAN);
		return null;
	}

	//Done
	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception
	{
		if (filterOpChain.getArg().getExprList().size()!= 0)
			throw new TypeCheckException("Error : visitFilterOpChain");
		filterOpChain.setType(TypeName.IMAGE);
		return null;
	}

	//Done
	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception
	{
		Token token = frameOpChain.firstToken;
		Tuple tuple = frameOpChain.getArg();
		if (token.isKind(KW_SHOW) ||token.isKind(KW_HIDE))
		{
			if (tuple.getExprList().size()!= 0)
				throw new TypeCheckException("Error : visitFrameOpChain, KW_SHOW or KW_HIDE");
			frameOpChain.setType(NONE);
		}
		else if (token.isKind(KW_XLOC) || token.isKind(KW_YLOC))
		{
			if (tuple.getExprList().size()!= 0)
				throw new TypeCheckException("Error : visitFrameOpChain, KW_LOC");
			frameOpChain.setType(TypeName.INTEGER);
		}
		else if(token.isKind(KW_MOVE))
		{
			if (tuple.getExprList().size() != 2)
				throw new TypeCheckException("Error : visitFrameOpChain, KW_MOVE");
			frameOpChain.setType(NONE);
			tuple.visit(this,arg);
		}
		else    throw new TypeCheckException("Error : visitFrameOpChain");
		return null;
	}

	//Done
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception
	{
		Token token = identChain.getFirstToken();
		Dec dec = symtab.lookup(token.getText());
		if (dec == null) throw new TypeCheckException("Error : visitIdentChain ");
		identChain.setType(dec.getType());
		identChain.dec = dec;
		return null;
	}

	//Done
	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception
	{
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if (dec == null)	throw new TypeCheckException("Error : visisIdentExpression");
		identExpression.setType(dec.getType());
		identExpression.setDec(dec);
		return null;
	}

	//Done
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception
	{
		Expression expr = ifStatement.getE();
		expr.visit(this, arg);
		if (!expr.getType().equals(TypeName.BOOLEAN)) throw new TypeCheckException("Error : visitIfStatement");
		Block block = ifStatement.getB();
		block.visit(this, arg);
		return null;
	}

	//Done
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception
	{
		intLitExpression.setType(INTEGER);
		return null;
	}

	//Done
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception
	{
		Expression expr = sleepStatement.getE();
		expr.visit(this, arg);
		if ((expr.getType().equals(TypeName.INTEGER)) == false) throw new TypeCheckException("Error:visitSleepStatement");
		return null;
	}

	//Done
 	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception
	{
 		Expression expr = whileStatement.getE();
		expr.visit(this, arg);
		if ((expr.getType().equals(TypeName.BOOLEAN)) == false ) throw new TypeCheckException("Error: visitWhileStatement");
		Block block = whileStatement.getB();
		block.visit(this, arg);
		return null;
	}

	//Done
	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception
	{
		symtab.insert(declaration.getIdent().getText(), declaration);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> arg_params = program.getParams();
		for (int i=0; i<arg_params.size(); i++){
			arg_params.get(i).visit(this, arg);
		}
		Block block = program.getB();
		block.visit(this, arg);
		return null;
	}

	//Done
	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception
	{
		IdentLValue l = assignStatement.getVar();
		l.visit(this, arg);
		Expression expr = assignStatement.getE();
		expr.visit(this, arg);
		if ((expr.getType().equals( l.getType())) == false ) throw new TypeCheckException("Error: visitAssignment Statement");
		return null;
	}

	//Done
	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception
	{
		Dec dec = symtab.lookup(identX.getText());
		if (dec == null) throw new TypeCheckException("Error: Dec is null");
		else  identX.setDec(dec);
		return null;
	}

	//Done
	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception
	{
		if(symtab.insert(paramDec.getIdent().getText(), paramDec))
			return null;
		else
			throw new TypeCheckException("Error:Visit ParamDec");
	}

	//Done
	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		constantExpression.setType(INTEGER);
		return null;
	}

	//Done
	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception
	{
		Token token = imageOpChain.firstToken;
		Tuple tuple = imageOpChain.getArg();
		if (token.isKind(OP_WIDTH) || token.isKind(OP_HEIGHT))
		{
			if (tuple.getExprList().size() != 0) throw new TypeCheckException("Error");
			imageOpChain.setType(TypeName.INTEGER);
		}
		else if (token.isKind(KW_SCALE))
		{
			if (tuple.getExprList().size() != 1) throw new TypeCheckException("Error");
			imageOpChain.setType(TypeName.IMAGE);
			tuple.visit(this,arg);
		}
		return null;
	}

	//Done
	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception
	{
		List<Expression> expr = tuple.getExprList();
		for(int k=0;k<expr.size();k++)
			{
			expr.get(k).visit(this, arg);
       		if(expr.get(k).getType().equals(TypeName.INTEGER)==false)
			throw new TypeCheckException("Error: visitTuple");
			}
		return null;
	}
}
