package cop5556sp17;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import cop5556sp17.Scanner.Kind;

import java.util.HashMap;

public class Scanner
{
	public static enum STATE{ START,IN_DIGIT,IN_IDENT,AFTER_EQ,COMMENT; }

	public static enum Kind
	{
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"),
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"),
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"),
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"),
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"),
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="),
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"),
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"),
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"),
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"),
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"),
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) { this.text = text; }
		final String text;
		public String getText() {  return text;  }
	}

	protected static final Map<String, Kind> repo;
	static
	{
		repo = new HashMap<String, Scanner.Kind>();

		for(Kind k: Kind.values())
		{
			String lit = k.getText();
			if(lit.matches("^[a-z]+"))
				repo.put(lit, k);
		}
	}

	protected boolean inRepo(String str){ return (repo.containsKey(str)); }

	protected Kind Key(String kindText) { return (repo.get(kindText)); }

	@SuppressWarnings("serial")

	public static class IllegalNumberException extends Exception
	{
	public IllegalNumberException(String message)
	{
		super(message);
	}
	}

	/*Class to deal with the situation when an illegal character is encountered */

	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception
	{
		public IllegalCharException(String message)
		{
			super(message);
		}
	}

	/* Holds the line and position in the line of a token. */

	static class LinePos
	{
		public final int line;
		public final int posInLine;
		public LinePos(int line, int posInLine)
		{
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString()
		{
			return " Line position [line = " + line + ", Position in Line = " + posInLine + "]";
		}
	}

	public class Token
	{
		public final Kind kind;
		public final int pos;        //position in input array
		public final int length;
		                              //returns the text of this Token
		public String getText()
		{
			if(kind == Kind.EOF)
			   return Kind.EOF.getText();
			return chars.substring(pos, pos + length);
		}


		//returns a LinePos object representing the line and column of this Token

		LinePos getLinePos()
		{
			return new LinePos(FindLineNumber(pos), pos - startpos.get(FindLineNumber(pos)));
		}

        Token(Kind kind, int pos, int length)
		{
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public int intVal() throws NumberFormatException
		{
			return Integer.parseInt(chars.substring(pos, pos + length));
		}

		public boolean isKind(Kind k) {
			return (k == this.kind);
		}

		public Kind getKind()
		{
			return kind;
		}

		 @Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }



		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
	}

	private int skipWhiteSpace(int pos, int length)
	{
		int start_Pos = pos;
		if(start_Pos < length)
		{
			while(Character.isSpaceChar(chars.charAt(start_Pos)))
			{
				start_Pos ++;
			if(start_Pos == length)
			break;
			}
		}
		return start_Pos;
	}

	private int FindLineNumber(int pos)
	{
		int target;
		int a=0;
		for(a=0; a < startpos.size(); a++)
		{
			target = startpos.get(a);
			if(pos > target)
			{
				continue;
			}
			else if(pos == target)
			{
				return a;
			}
			else
			{
				break;
			}
		}
		return (a-1);
	}

	Scanner(String chars)
	{
		this.chars = chars;
		tokens = new ArrayList<Token>();
	}

	public Scanner scan() throws IllegalCharException, IllegalNumberException
	{
		    int pos = 0;
		    int length = chars.length();
		    startpos.add(0);
		    STATE state = STATE.START;
		    int startPos = 0;
		    int ch;
		    while (pos <= length)
		    {
		        ch = pos < length ? chars.charAt(pos) : -1;
		        switch (state)
		        {
		        case START:
		        {
		            pos = skipWhiteSpace(pos, length);
		            startPos = pos;
		            ch = pos < length ? chars.charAt(pos) : -1;
		            switch (ch)
		            {
		                case -1:
		                {
		                tokens.add(new Token(Kind.EOF, startPos, 0));
		                pos++;
		                }
		                break;

		                case '+':
		                {
		                	tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;
		                }
		                break;

		                case '*':
		                {
		                	tokens.add(new Token(Kind.TIMES, startPos, 1));
		                	state = STATE.START;
		                	pos++;
		                }
		                break;

		                case '0':
		                {
		                	tokens.add(new Token(Kind.INT_LIT,startPos, 1));
		                	pos++;
		                }
		                break;
		                case ';':
		                {
		                	tokens.add(new Token(Kind.SEMI,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '(':
		                {
		                	tokens.add(new Token(Kind.LPAREN,startPos, 1));
		                	pos++;
		                }
		                break;
		                case ')':
		                {
		                	tokens.add(new Token(Kind.RPAREN,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '{':
		                {
		                	tokens.add(new Token(Kind.LBRACE,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '}':
		                {
		                	tokens.add(new Token(Kind.RBRACE,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '&':
		                {
		                	tokens.add(new Token(Kind.AND,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '%':
		                {
		                	tokens.add(new Token(Kind.MOD,startPos, 1));
		                	pos++;
		                }
		                break;
		                case ',':
		                {
		                	tokens.add(new Token(Kind.COMMA,startPos, 1));
		                	pos++;
		                }
		                break;
		                case '!':
		                {
                        if(((pos+1)<length)&&(chars.charAt(pos+1) == '='))
                          {
                        	tokens.add(new Token(Kind.NOTEQUAL,startPos, 2));
                        	pos++;
		                  }
                        else
                        {
                        	tokens.add(new Token(Kind.NOT, startPos, 1));
                        }
                        pos++;
                        state = STATE.START;
		                }
		                break;

		                case '|':
		                {
                        if(((pos+1)<length)&&(chars.charAt(pos+1) == '-'))
                          {
                        	 if(((pos+2)<length)&&(chars.charAt(pos+2) == '>'))
                             {
                        	 tokens.add(new Token(Kind.BARARROW,startPos, 3));
                        	 pos+=2;
		                     }
                        	 else
                        	 {
                        		 tokens.add(new Token(Kind.OR,startPos, 1));
                        		 tokens.add(new Token(Kind.MINUS,startPos+1, 1));
                        		 pos+=1;
                        	 }
                          }
                        else
                        {
                        	tokens.add(new Token(Kind.OR, startPos, 1));
                        }
                        pos++;
                        state = STATE.START;
		                }
		                break;

		                case '>':
		                {
		                	if(((pos+1)<length)&&(chars.charAt(pos+1) == '='))
		                          {
		                        	tokens.add(new Token(Kind.GE,startPos, 2));
		                        	pos++;
				                  }
		                        else
		                        {
		                        	tokens.add(new Token(Kind.GT, startPos, 1));
		                        }
		                        pos++;
		                        state = STATE.START;
		                }
		                break;

		                case '<':
		                {
		                	if(((pos+1)<length)&&(chars.charAt(pos+1) == '='))
		                          {
		                        	tokens.add(new Token(Kind.LE,startPos, 2));
		                        	pos++;
				                  }
		                        else if(((pos+1)<length)&&(chars.charAt(pos+1) == '-'))
		                          {
		                        	tokens.add(new Token(Kind.ASSIGN,startPos, 2));
		                        	pos++;
				                  }
		                        else
		                        {
		                        	tokens.add(new Token(Kind.LT, startPos, 1));
		                        }
		                        pos++;
		                        state = STATE.START;
		                }
		                break;

		                case '-':
		                {
		                	if(((pos+1)<length)&&(chars.charAt(pos+1) == '>'))
		                          {
		                        	tokens.add(new Token(Kind.ARROW,startPos, 2));
		                        	pos++;
				                  }
		                        else
		                        {
		                        	tokens.add(new Token(Kind.MINUS, startPos, 1));
		                        }
		                        pos++;
		                        state = STATE.START;
		                }
	                    break;

		                case '=':
		                {
		                       state = STATE.AFTER_EQ;
		                }
	                    break;

		                case '/':
		                {
		                	 if(((pos+1)<length)&&(chars.charAt(pos+1) == '*'))
		                	 {
		                		 state = STATE.COMMENT;
		                		 pos+=2;
		                	 }
		                	 else
		                	 {
		                		 tokens.add(new Token(Kind.DIV,startPos,1));
		                		 pos++;
			                	 state = STATE.START;
		                	 }
		                }
		                break;

		                case '\n':
		                {
							line++;
							pos++;
							startpos.add(pos);
						} break;

						case '\t':
						{
							pos++;
						} break;

						case '\r':
						{
							pos++;
						}
							break;

						case '\b':
						{
							pos++;
						}
							break;

		                default:
		                {
		                if (Character.isDigit(ch))
		                {
		                	state = STATE.IN_DIGIT;
		                	pos++;
		                }
		                    else if (Character.isJavaIdentifierStart(ch))
		                    {
		                         state = STATE.IN_IDENT;pos++;
		                    }
		                     else
		                     {
		                    	 throw new IllegalCharException("illegal char " +ch+" at pos "+pos);
		                     }
		                  }
		            }
		        }
		        break;
		        case COMMENT:
		        {
				if(((pos)<length)&&(chars.charAt(pos)=='*'))
					{
					pos++;
					if(((pos)<length)&&(chars.charAt(pos) == '/'))
					{
						state = STATE.START;
						pos++;
					}
					else
					{
						state = STATE.COMMENT;
					}
				}
				 else if(((pos)<length)&&(chars.charAt(pos)) == '\n')
					{
						line++;
						pos++;
						startpos.add(pos);
					}
					else
					{
						pos++;
						state = STATE.COMMENT;
					}
				}
					break;

		        case IN_DIGIT:
		        {
					if(Character.isDigit(ch))
					{
						pos++;
					}
					else
					{
						String inputNum = chars.substring(startPos, pos);
						try
						{
							Integer.parseInt(inputNum);
							tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
							state = STATE.START;
						}
						catch (NumberFormatException e)
						{
							throw new IllegalNumberException("The number at the line" +
									FindLineNumber(pos) + " and at the position " +
									(pos - startpos.get(FindLineNumber(pos))) +
									" the given input number " + inputNum + " is beyond the range of the limits");
						}

					}
				}
					break;

		        	case IN_IDENT:
		        	{
						if(Character.isJavaIdentifierPart(ch))
						{
							pos++;
						}
						else
						{
							String ident = chars.substring(startPos, pos);
							if(inRepo(ident))
							{
								tokens.add(new Token(Key(ident), startPos, pos - startPos));
							}
							else
							{
								tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
							}

							state = STATE.START;
						}
					}
						break;

		            case AFTER_EQ:
		            {
		              if(((pos+1)<length)&&(chars.charAt(pos+1) == '='))
                      {
                       	tokens.add(new Token(Kind.EQUAL,startPos, 2));
                       	pos++;
		              }
                      else
                      {
                       	throw new IllegalCharException("The character at line" + FindLineNumber(pos) + "and at the position " +
       							(pos - startpos.get(FindLineNumber(pos))) +
       							" contains " + Character.toString((char)ch) + " instead of '=' ");
                      }
                       pos++;
                       state = STATE.START;
		            }
		            break;

		            default:
		            assert false: "Default and unknown state" +state;
		        }
		    }
		return this;
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum = 0;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..
	 */

	int line = 0;
	protected List<Integer> startpos = new ArrayList<Integer>();

	public Token nextToken()
	{
		if (tokenNum >= tokens.size())
		{
			return null;
		}
		else
		{
		return tokens.get(tokenNum++);
		}
	}

	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */

	public Token peek()
	{
		if (tokenNum >= tokens.size())
		{
			return null;
		}
		else
		{
		return tokens.get(tokenNum);
		}
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 *
	 * Line numbers start counting at 0
	 *
	 * @param t
	 * @return
	 */

	public LinePos getLinePos(Token t)
	{
		return t.getLinePos();
	}
}