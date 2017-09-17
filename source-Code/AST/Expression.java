package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {

	private Dec decs;
	private TypeName typename;
	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;
	public TypeName getType()
	{
		return typename;
	}
	public void setDec(Dec d)
	{
		decs = d;
	}
	public void setType(TypeName types)
	{
		typename = types;
	}
	public Dec getDec()
	{
		return decs;
	}

}