package cop5556sp17;


import java.util.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import cop5556sp17.Scanner.*;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import static cop5556sp17.AST.Type.TypeName.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes
{
	int count;
	Stack<Integer> stack;
	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	MethodVisitor mv;
	final boolean DEVEL;
	final boolean GRADE;
	//Parameterized Constructor for the class
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName)
	{
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		count = 1;                    //Useful variable for visitDec
		stack = new Stack<Integer>(); //Stack as symbolTable for visitDec
	}

	//Already Given Method
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception
	{
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		int i = 0;
		for (ParamDec dec : program.getParams())
		{
			dec.setNumber(i++);
			cw.visitField(0, dec.getIdent().getText(), dec.getType().getJVMTypeDesc(), null, null);
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		cw.visitEnd();
		return cw.toByteArray();
	}

	//Already given with the source file
	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception
	{
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	//Verified and Completed
	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception
	{
		binaryChain.getE0().visit(this, 0);
		if (binaryChain.getArrow().isKind(Kind.BARARROW)) mv.visitInsn(DUP);
		else
		{
			if (binaryChain.getE0().getType() == URL)
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",PLPRuntimeImageIO.readFromURLSig, false);

			else if (binaryChain.getE0().getType() == FILE)
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",PLPRuntimeImageIO.readFromFileDesc, false);
		}

		if (binaryChain.getArrow().isKind(Kind.BARARROW)) binaryChain.getE1().visit(this, 3);
		else  binaryChain.getE1().visit(this, 1);
		if (binaryChain.getE1() instanceof IdentChain)
		{
			IdentChain identChain = (IdentChain) binaryChain.getE1();
			if ((identChain.getDec() instanceof ParamDec))
			{
				if (identChain.getDec().getType() == TypeName.INTEGER)
				{
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType().getJVMTypeDesc());
				}
			}
			else
			{
				if (identChain.getDec().getType() == TypeName.INTEGER) mv.visitVarInsn(ILOAD, identChain.getDec().getNumber());
				else mv.visitVarInsn(ALOAD, identChain.getDec().getNumber());
			}
		}
		return null;
	}

	//Verified and Completed
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception
	{
		TypeName t = binaryExpression.getE0().getType();
		TypeName tt = binaryExpression.getE1().getType();
		Token token = binaryExpression.getOp();
		switch (token.getKind())
		{
		case PLUS:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t == TypeName.INTEGER) mv.visitInsn(IADD);
			else 	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			break;

		case MINUS:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t == TypeName.INTEGER) 	mv.visitInsn(ISUB);
			else 	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			break;

		case TIMES:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t == TypeName.INTEGER) && (tt == TypeName.INTEGER)) mv.visitInsn(IMUL);
			else if ((t == TypeName.INTEGER) && (tt == TypeName.IMAGE))
			{
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);

			}
			else 	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
			break;

		case DIV:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t == TypeName.INTEGER) && (tt == TypeName.INTEGER)) mv.visitInsn(IDIV);
			else 	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
			break;

		case MOD:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if ((t == TypeName.INTEGER) && (tt == TypeName.INTEGER)) mv.visitInsn(IREM);
			else 	mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			break;

		case LE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label l_LE = new Label();
			mv.visitJumpInsn(IF_ICMPGT, l_LE);
			mv.visitInsn(ICONST_1);
			Label ll_LE = new Label();
			mv.visitJumpInsn(GOTO, ll_LE);
			mv.visitLabel(l_LE);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ll_LE);
			break;

		case LT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label l_LT = new Label();
			mv.visitJumpInsn(IF_ICMPGE, l_LT);
			mv.visitInsn(ICONST_1);
			Label ll_LT = new Label();
			mv.visitJumpInsn(GOTO, ll_LT);
			mv.visitLabel(l_LT);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ll_LT);
			break;


		case OR:
			binaryExpression.getE0().visit(this, arg);
			Label l = new Label();
			mv.visitJumpInsn(IFNE, l);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFNE, l);
			mv.visitInsn(ICONST_0);
			Label ll = new Label();
			mv.visitJumpInsn(GOTO, ll);
			mv.visitLabel(l);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(ll);
			break;

		case AND:
			binaryExpression.getE0().visit(this, arg);
			Label l_AND = new Label();
			mv.visitJumpInsn(IFEQ, l_AND);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFEQ, l_AND);
			mv.visitInsn(ICONST_1);
			Label ll_AND = new Label();
			mv.visitJumpInsn(GOTO, ll_AND);
			mv.visitLabel(l_AND);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ll_AND);
			break;

		case GT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label l_GT = new Label();
			mv.visitJumpInsn(IF_ICMPLE, l_GT);
			mv.visitInsn(ICONST_1);
			Label ll_GT = new Label();
			mv.visitJumpInsn(GOTO, ll_GT);
			mv.visitLabel(l_GT);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ll_GT);
			break;

		case GE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			Label l_GE = new Label();
			mv.visitJumpInsn(IF_ICMPLT, l_GE);
			mv.visitInsn(ICONST_1);
			Label ll_GE = new Label();
			mv.visitJumpInsn(GOTO, ll_GE);
			mv.visitLabel(l_GE);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(ll_GE);
			break;

		case NOTEQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t == TypeName.INTEGER || t == BOOLEAN)
			{
				Label l_N = new Label();
				mv.visitJumpInsn(IF_ICMPEQ, l_N);
				mv.visitInsn(ICONST_1);
				Label ll_N = new Label();
				mv.visitJumpInsn(GOTO, ll_N);
				mv.visitLabel( l_N);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ll_N);
			}
			else
			{
				Label l_N = new Label();
				mv.visitJumpInsn(IF_ACMPEQ, l_N);
				mv.visitInsn(ICONST_1);
				Label ll_N = new Label();
				mv.visitJumpInsn(GOTO, ll_N);
				mv.visitLabel(l_N);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ll_N);
			}
			break;

		case EQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			if (t == TypeName.INTEGER || t == BOOLEAN)
			{
				Label l_E = new Label();
				mv.visitJumpInsn(IF_ICMPNE, l_E);
				mv.visitInsn(ICONST_1);
				Label ll_E = new Label();
				mv.visitJumpInsn(GOTO, ll_E);
				mv.visitLabel(l_E);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ll_E);
			}
			else
			{
				Label l_E = new Label();
				mv.visitJumpInsn(IF_ACMPNE, l_E);
				mv.visitInsn(ICONST_1);
				Label ll_E = new Label();
				mv.visitJumpInsn(GOTO, ll_E);
				mv.visitLabel(l_E);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(ll_E);
			}
			break;

		default:
			break;
		}
		return null;
	}

	//Done
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception
	{
		Label l = new Label();
		mv.visitLineNumber(block.getFirstToken().getLinePos().line, l);
		mv.visitLabel(l);
		for (Dec d : block.getDecs()) 	d.visit(this, mv);
		for (Statement s : block.getStatements())
		{
			s.visit(this, mv);
			if (s instanceof BinaryChain) 	mv.visitInsn(POP);
		}

		Label ll = new Label();
		mv.visitLineNumber(0, ll);
		mv.visitLabel(ll);
		for (Dec dec : block.getDecs())
		{
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getType().getJVMTypeDesc(), null, l,ll, dec.getNumber());
			count--;
			stack.pop();
		}
		return null;
	}

	//Done
	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception
	{
		if (booleanLitExpression.getValue()) 	mv.visitInsn(ICONST_1);
		else 	mv.visitInsn(ICONST_0);
		return null;
	}

	//Done
	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg)
	{
		if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT))
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
	   else if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH))
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		return null;
	}

	//Done
	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception
	{
		stack.push(count++);
		declaration.setNumber(stack.peek());
		return null;
	}

	//Done
	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception
	{
		switch (filterOpChain.getFirstToken().getKind())
		{
		case OP_BLUR:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			break;

		case OP_GRAY:
			if ((int) arg != 3) mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			break;

		case OP_CONVOLVE:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,false);
			break;

		default:
			break;
		}
		return null;
	}

	//Done
	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception
	{
		frameOpChain.getArg().visit(this, arg);
		switch (frameOpChain.getFirstToken().getKind())
		{

		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,false);
			break;

		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,false);
			break;

		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,false);
			break;

		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
			break;

		case KW_SHOW:
	    mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,false);
    	break;

		default:
			break;

		}
		return null;
	}

	//Done
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception
	{
		if ((int) arg == 1)
		{
			if (identChain.getDec() instanceof ParamDec)
			{
				switch (identChain.getDec().getType())
				{
				case INTEGER:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getType().getJVMTypeDesc());
					identChain.getDec().setBoolean(true);
					break;

				case FILE:
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(), identChain.getDec().getType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setBoolean(true);
					break;

				default:
					break;
				}
			}
			else
			{
				switch (identChain.getDec().getType())
				{

				case FILE:
					mv.visitVarInsn(ALOAD, identChain.getDec().getNumber());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setBoolean(true);
					break;

				case IMAGE:
					mv.visitVarInsn(ASTORE, identChain.getDec().getNumber());
					identChain.getDec().setBoolean(true);
					break;

				case INTEGER:
					mv.visitVarInsn(ISTORE, identChain.getDec().getNumber());
					identChain.getDec().setBoolean(true);
					break;

				case FRAME:
					if (identChain.getDec().getBoolean())
					{
						mv.visitVarInsn(ALOAD, identChain.getDec().getNumber());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getNumber());
					}
					else
					{
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getDec().getNumber());
						identChain.getDec().setBoolean(true);
					break;
					}

				default:
					break;
				}
			}
		}
		else
		{
			if (identChain.getDec() instanceof ParamDec)
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
						identChain.getDec().getType().getJVMTypeDesc());


			}
			else
			{
				if (identChain.getDec().getType() == FRAME)
				{
					if (identChain.getDec().getBoolean())
					{
						mv.visitVarInsn(ALOAD, identChain.getDec().getNumber());

					}
					else
					{
						mv.visitInsn(ACONST_NULL);
					}

				}
				else 	mv.visitVarInsn(ALOAD, identChain.getDec().getNumber());
			}
		}
		return null;
	}

	//Done
	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception
	{
		if (identExpression.getDec() instanceof ParamDec)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),
					identExpression.getDec().getType().getJVMTypeDesc());
		}
		else
		{
			if (identExpression.getType() == TypeName.INTEGER || identExpression.getType() == TypeName.BOOLEAN)
				mv.visitVarInsn(ILOAD, identExpression.getDec().getNumber());
			else
				mv.visitVarInsn(ALOAD, identExpression.getDec().getNumber());
		}
		return null;
	}

	//Done
	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception
	{
		if (identX.getDec() instanceof ParamDec)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),
					identX.getDec().getType().getJVMTypeDesc());
		}
		else
		{
			if (identX.getDec().getType() == IMAGE)
			{
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getNumber());
				identX.getDec().setBoolean(true);
			}
			else if (identX.getDec().getType() == TypeName.INTEGER || identX.getDec().getType() == TypeName.BOOLEAN)
			{
				mv.visitVarInsn(ISTORE, identX.getDec().getNumber());
				identX.getDec().setBoolean(true);
			}
			else
			{
				mv.visitVarInsn(ASTORE, identX.getDec().getNumber());
				identX.getDec().setBoolean(true);
			}
		}
		return null;
	}

	//Done
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception
	{
		ifStatement.getE().visit(this, arg);
		Label l = new Label();
		mv.visitJumpInsn(IFEQ, l);
		Label ll = new Label();
		mv.visitLabel(ll);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(l);
		return null;
	}

	//Done
	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception
	{
		imageOpChain.getArg().visit(this, arg);
		switch (imageOpChain.getFirstToken().getKind())
		{
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
         	break;

		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
			break;

		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
			break;

		default:
			break;
		}
		return null;
	}

	//Done
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception
	{
		mv.visitLdcInsn(intLitExpression.firstToken.intVal());
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception
	{
		MethodVisitor mv = (MethodVisitor) arg;
		TypeName typeName = paramDec.getType();
		switch (typeName)
		{
		case INTEGER:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			aux(paramDec.getNumber(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			break;

		case BOOLEAN:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			aux(paramDec.getNumber(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
			break;

		case FILE:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			aux(paramDec.getNumber(), mv);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
			break;

		case URL:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			aux(paramDec.getNumber(), mv);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
			break;

		default:
			break;
		}
		return null;
	}

	//Done
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception
	{
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	//Tested
	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception
	{
		for (Expression i : tuple.getExprList())
			i.visit(this, arg);
		return null;
	}

	//Verified
	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception
	{
		Label l = new Label();
		mv.visitJumpInsn(GOTO, l);
		Label ll = new Label();
		mv.visitLabel(ll);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(l);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, ll);
		return null;
	}

	//Auxilliary method for visit Param Dec

			public void aux(int marker, MethodVisitor mv)
			{
				mv.visitIntInsn(BIPUSH, marker);
			}

}