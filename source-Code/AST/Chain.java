package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;


public abstract class Chain extends Statement {
	private TypeName types;
	public Chain(Token firstToken) {
		super(firstToken);
		types=null;
	}
	public TypeName getType()
	{
		return types;
	}
	public void setType(TypeName t)
	{
		types = t;
	}

}