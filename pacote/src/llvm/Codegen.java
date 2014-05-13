/*****************************************************
Esta classe Codegen é a responsável por emitir LLVM-IR. 
Ela possui o mesmo método 'visit' sobrecarregado de
acordo com o tipo do parâmetro. Se o parâmentro for
do tipo 'While', o 'visit' emitirá código LLVM-IR que 
representa este comportamento. 
Alguns métodos 'visit' já estão prontos e, por isso,
a compilação do código abaixo já é possível.

class a{
    public static void main(String[] args){
    	System.out.println(1+2);
    }
}

O pacote 'llvmast' possui estruturas simples 
que auxiliam a geração de código em LLVM-IR. Quase todas 
as classes estão prontas; apenas as seguintes precisam ser 
implementadas: 

// llvmasm/LlvmBranch.java
// llvmasm/LlvmIcmp.java
// llvmasm/LlvmMinus.java
// llvmasm/LlvmTimes.java


Todas as assinaturas de métodos e construtores 
necessárias já estão lá. 


Observem todos os métodos e classes já implementados
e o manual do LLVM-IR (http://llvm.org/docs/LangRef.html) 
como guia no desenvolvimento deste projeto. 

 ****************************************************/

//andre, falta alocar corretamente espaço para as variaveis na declaracao do formal e do vardelcation e corrigit
//o assign que voce fez, abração! :) sorry pelo git

package llvm;


import semant.Env;
import syntaxtree.*;
import llvmast.*;

import java.util.*;

public class Codegen extends VisitorAdapter{
	private Map<String, ClassNode> classes = new HashMap<String, ClassNode>();
	private List<LlvmInstruction> assembler;
	private Codegen codeGenerator;

	private SymTab symTab;
	private ClassNode classEnv; 	// Aponta para a classe atualmente em uso em symTab
	private String actualClass;
	private MethodNode methodEnv; 	// Aponta para a metodo atualmente em uso em symTab

	private static int ord=0;

	public Codegen(){
		assembler = new LinkedList<LlvmInstruction>();
	}

	// Método de entrada do Codegen
	public String translate(Program p, Env env){	
		codeGenerator = new Codegen();

		// Preenchendo a Tabela de Símbolos
		// Quem quiser usar 'env', apenas comente essa linha
		codeGenerator.symTab = new SymTab();
		codeGenerator.symTab.FillTabSymbol(p);

		// Formato da String para o System.out.printlnijava "%d\n"
		codeGenerator.assembler.add(new LlvmConstantDeclaration("@.formatting.string", "private constant [4 x i8] c\"%d\\0A\\00\""));

		// NOTA: sempre que X.accept(Y), então Y.visit(X);
		// NOTA: Logo, o comando abaixo irá chamar codeGenerator.visit(Program), linha 75
		p.accept(codeGenerator);

		// Link do printf
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@printf", LlvmPrimitiveType.I32, pts)); 
		List<LlvmType> mallocpts = new LinkedList<LlvmType>();
		mallocpts.add(LlvmPrimitiveType.I32);
		codeGenerator.assembler.add(new LlvmExternalDeclaration("@malloc", new LlvmPointer(LlvmPrimitiveType.I8),mallocpts)); 


		String r = new String();
		for(LlvmInstruction instr : codeGenerator.assembler)
			r += instr+"\n";
		return r;
	}

	public LlvmValue visit(Program n){
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n){
		// definicao do main 
		assembler.add(new LlvmDefine("@main", LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmLabel(new LlvmLabelValue("entry")));
		LlvmRegister R1 = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I32));
		assembler.add(new LlvmAlloca(R1, LlvmPrimitiveType.I32, new LinkedList<LlvmValue>()));
		assembler.add(new LlvmStore(new LlvmIntegerLiteral(0), R1));

		// Statement é uma classe abstrata
		// Portanto, o accept chamado é da classe que implementa Statement, por exemplo,  a classe "Print". 
		n.stm.accept(this);  

		// Final do Main
		LlvmRegister R2 = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmLoad(R2,R1));
		assembler.add(new LlvmRet(R2));
		assembler.add(new LlvmCloseDefinition());
		return null;
	}

	public LlvmValue visit(Plus n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmPlus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}

	public LlvmValue visit(Print n){

		LlvmValue v =  n.exp.accept(this);

		// getelementptr:
		LlvmRegister lhs = new LlvmRegister(new LlvmPointer(LlvmPrimitiveType.I8));
		LlvmRegister src = new LlvmNamedValue("@.formatting.string",new LlvmPointer(new LlvmArray(4,LlvmPrimitiveType.I8)));
		List<LlvmValue> offsets = new LinkedList<LlvmValue>();
		offsets.add(new LlvmIntegerLiteral(0));
		offsets.add(new LlvmIntegerLiteral(0));
		List<LlvmType> pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		List<LlvmValue> args = new LinkedList<LlvmValue>();
		args.add(lhs);
		args.add(v);
		assembler.add(new LlvmGetElementPointer(lhs,src,offsets));

		pts = new LinkedList<LlvmType>();
		pts.add(new LlvmPointer(LlvmPrimitiveType.I8));
		pts.add(LlvmPrimitiveType.DOTDOTDOT);

		// printf:
		assembler.add(new LlvmCall(new LlvmRegister(LlvmPrimitiveType.I32),
				LlvmPrimitiveType.I32,
				pts,				 
				"@printf",
				args
				));
		return null;
	}

	public LlvmValue visit(IntegerLiteral n){
		return new LlvmIntegerLiteral(n.value);
	};

	// Todos os visit's que devem ser implementados	
	public LlvmValue visit(ClassDeclSimple n){
		//para pegar o nome da classe atual na MethodDeclaration
		actualClass = n.name.s;
		classEnv = symTab.classes.get(actualClass);
		LinkedList<LlvmType> members = new LinkedList<LlvmType>();
		for ( util.List<VarDecl> l = n.varList; l != null; l=l.tail)
			members.add((LlvmType)l.head.type.accept(this));
		LlvmStructure st = new LlvmStructure(members);
		LinkedList<LlvmValue> vars = new LinkedList<LlvmValue>();
		//		for ( util.List<VarDecl> l = n.varList; l != null; l=l.tail)
		//			vars.add(l.head.accept(this));
		classes.put(n.name.s, new ClassNode(n.name.s, st, vars));

		assembler.add(0, new LlvmClass(n.name.s, st));
		for ( util.List<MethodDecl> l = n.methodList; l != null; l=l.tail) {
			l.head.accept(this);
		}
		return null;
	}
	public LlvmValue visit(ClassDeclExtends n){return null;}
	public LlvmValue visit(VarDecl n){
		LlvmRegister reg = new LlvmRegister("%"+n.name.s+"_addr", (LlvmType)n.type.accept(this));
		LlvmAlloca al = new LlvmAlloca(reg, reg.type, new LinkedList<LlvmValue>());
		assembler.add(al);
		return null;
	}
	public LlvmValue visit(MethodDecl n){

		for (MethodNode m : classEnv.methodsList)
		{
			if (m.methodName.equals(n.name.s))
			{
				methodEnv = m;
				break;
			}
		}

		LinkedList<LlvmValue> args = new LinkedList<LlvmValue>();
		LinkedList<LlvmValue> argsDefine = new LinkedList<LlvmValue>();
		argsDefine.add(new LlvmRegister("%this", new LlvmPointer(new LlvmClassStructure(classEnv.nameClass))));
		for ( util.List<Formal> l = n.formals; l != null; l=l.tail)
		{
			args.add(l.head.accept(this));
			argsDefine.add(l.head.accept(this));
		}

		assembler.add(new LlvmDefine(getMethodHash(actualClass, n.name.s), (LlvmType)n.returnType.accept(this), argsDefine));

		for ( util.List<Formal> l = n.formals; l != null; l=l.tail)
		{
			LlvmValue res = l.head.name.accept(this);
			LlvmType resType = (LlvmType)l.head.type.accept(this);
			assembler.add(new LlvmAlloca(res, resType, new LinkedList<LlvmValue>()));
			assembler.add(new LlvmStore(new LlvmNamedValue("%"+l.head.name.s, resType), res));
		}

		for ( util.List<VarDecl> l = n.locals; l != null; l=l.tail)
			l.head.accept(this);

		for ( util.List<Statement> l = n.body; l != null; l=l.tail)
			l.head.accept(this);

		assembler.add(new LlvmRet(n.returnExp.accept(this)));
		assembler.add(new LlvmCloseDefinition());

		return null;
	}
	public LlvmValue visit(Formal n){
		return new LlvmNamedValue("%"+n.name.toString(), (LlvmType)n.type.accept(this));
	}
	public LlvmValue visit(IntArrayType n){
		return null;
	}
	public LlvmValue visit(BooleanType n){
		return LlvmPrimitiveType.I1;
	}
	public LlvmValue visit(IntegerType n){
		return LlvmPrimitiveType.I32;
	}
	public LlvmValue visit(IdentifierType n){
		return new LlvmPointer(new LlvmClassStructure(n.name));
	}
	public LlvmValue visit(Block n){
		//for iterando sobre todos os elementos da lista de statements
		for ( util.List<Statement> l = n.body; l != null; l=l.tail){
			l.head.accept(this);
		}
		return null;
	}
	public LlvmValue visit(If n){
		LlvmValue cond = n.condition.accept(this);
		LlvmLabelValue brTrue = new LlvmLabelValue("estTrue"+ord++);
		LlvmLabelValue brFalse = new LlvmLabelValue("estFalse"+ord++);
		LlvmLabelValue branchInc = new LlvmLabelValue("estInc"+ord++);
		assembler.add(new LlvmBranch(cond,  brTrue,  brFalse));
		assembler.add(new LlvmLabel(brTrue));
		n.thenClause.accept(this);
		assembler.add(new LlvmBranch(branchInc));
		assembler.add(new LlvmLabel(brFalse));
		n.elseClause.accept(this);
		assembler.add(new LlvmBranch(branchInc));
		assembler.add(new LlvmLabel(branchInc));
		return null;
	}
	public LlvmValue visit(While n){
		LlvmValue cond = n.condition.accept(this);
		LlvmLabelValue brIn = new LlvmLabelValue("loopWhile"+ord++);
		LlvmLabelValue brOut = new LlvmLabelValue("outLoop"+ord++);
		//verifica se é true, se for true, vai para dentro do while
		assembler.add(new LlvmBranch(cond,  brIn,  brOut));

		//inicio do "loop while"
		assembler.add(new LlvmLabel(brIn));
		n.body.accept(this);
		//verifica de novo, para ver se vai sair do loop
		cond = n.condition.accept(this);
		assembler.add(new LlvmBranch(cond,  brIn,  brOut));

		//label apos o loop
		assembler.add(new LlvmLabel(brOut));
		return null;
	}
	public LlvmValue visit(Assign n){
		LlvmValue exp = n.exp.accept(this);
		LlvmValue res = n.var.accept(this);
		LlvmStore stor = new LlvmStore(exp, res);
		assembler.add(stor);
		return null;
	}
	public LlvmValue visit(ArrayAssign n){return null;}
	public LlvmValue visit(And n){
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		assembler.add(new LlvmAnd(lhs,LlvmPrimitiveType.I1,v1,v2));
		return lhs;
	}
	public LlvmValue visit(LessThan n){
		LlvmValue op1 = n.lhs.accept(this);
		LlvmValue op2 = n.rhs.accept(this);
		//create new object to save the destiny register name
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmIcmp(lhs,9,LlvmPrimitiveType.I32,op1,op2));
		return lhs;
	}
	public LlvmValue visit(Equal n){
		LlvmValue op1 = n.lhs.accept(this);
		LlvmValue op2 = n.rhs.accept(this);
		//create new object to save the destiny register name
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		assembler.add(new LlvmIcmp(lhs,1,LlvmPrimitiveType.I32,op1,op2));
		return lhs;
	}
	public LlvmValue visit(Minus n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmMinus(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	public LlvmValue visit(Times n){
		LlvmValue v1 = n.lhs.accept(this);
		LlvmValue v2 = n.rhs.accept(this);
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I32);
		assembler.add(new LlvmTimes(lhs,LlvmPrimitiveType.I32,v1,v2));
		return lhs;
	}
	public LlvmValue visit(ArrayLookup n){return null;}
	public LlvmValue visit(ArrayLength n){return null;}
	public LlvmValue visit(Call n){
		String i = n.method.s;
		LlvmRegister lhs; 
		LlvmType type;
		LlvmValue object = n.object.accept(this);
		String fnName = n.method.s;
		//usando o po de diamante
		List<LlvmValue> args = new LinkedList<>();
		//iterando na lista de variaveis do metodo
		args.add(object);
		for ( util.List<Exp> valuesList = n.actuals; valuesList != null; valuesList = valuesList.tail){
			args.add(valuesList.head.accept(this));
		}
		//definido tipo de registrador de retorno
		type = (LlvmType) n.type.accept(this);
		//criando registradopr de retorno
		lhs = new LlvmRegister(type);
		assembler.add(new LlvmCall(lhs, type, getMethodHash(n.object.type.toString(), fnName), args));
		return lhs;

	}
	public LlvmValue visit(True n){
		return new LlvmBool(1);
	}
	public LlvmValue visit(False n){
		return new LlvmBool(0);
	}
	public LlvmValue visit(IdentifierExp n){
		LlvmValue res = n.name.accept(this);
		LlvmRegister reg = new LlvmRegister((LlvmType)n.type.accept(this));
		LlvmLoad load = new LlvmLoad(reg, res);
		assembler.add(load);
		return reg;
	}
	public LlvmValue visit(This n){
		return new LlvmNamedValue("%this", (LlvmType)n.type.accept(this));
	}
	public LlvmValue visit(NewArray n){return null;}
	public LlvmValue visit(NewObject n){
		LlvmPointer type = (LlvmPointer)n.type.accept(this);
		LlvmRegister lhs = new LlvmRegister(type);

		LlvmClassStructure classStruct = (LlvmClassStructure) type.content;
		ClassNode cn = symTab.classes.get(classStruct.name);

		LinkedList<LlvmType> types = new LinkedList<>();
		for (LlvmValue v : cn.varList)
		{
			types.add(((LlvmPointer)v.type).content);
		}
		System.out.println(types);

		assembler.add(new LlvmMalloc(lhs, new LlvmStructure(types), classStruct.toString()));


		return lhs;
	}
	public LlvmValue visit(Not n){
		//carrega o valor da expressao em v1
		LlvmValue v1 = n.exp.accept(this);
		//cria um objeto integerLiteral para colocar o valor 1 nele
		LlvmIntegerLiteral v2 = new LlvmIntegerLiteral(1);
		//cria um objeto register para colocar o valor resultado nele
		LlvmRegister lhs = new LlvmRegister(LlvmPrimitiveType.I1);
		//soma 1 no bit para inverte-lo
		assembler.add(new LlvmPlus(lhs,LlvmPrimitiveType.I1,v2,v1));
		return lhs;
	}
	public String getMethodHash(String className, String MethodName){
		return "@m_" + className + "_" + MethodName;
	}

	public LlvmValue visit(Identifier n){
		if (methodEnv != null)
		{
			System.out.println(methodEnv.formalsList);
			System.out.println(methodEnv.valuesList);
			System.out.println(classEnv.varList);
			for (LlvmValue val : methodEnv.formalsList)
			{
				if (val.toString().equals(n.s))
				{
					return new LlvmNamedValue("%"+val.toString()+"_addr", val.type);
				}
			}
			for (LlvmValue val : methodEnv.valuesList)
			{
				if (val.toString().equals(n.s))
				{
					return new LlvmNamedValue("%"+val.toString()+"_addr", val.type);
				}
			}
			int i = 0;
			for (LlvmValue val : classEnv.varList)
			{
				if (val.toString().equals(n.s))
				{
					LlvmRegister res = new LlvmRegister(val.type);
					LinkedList<LlvmValue> offset = new LinkedList<>();
					offset.add(new LlvmIntegerLiteral(0));
					offset.add(new LlvmIntegerLiteral(i));
					assembler.add(new LlvmGetElementPointer(res, new LlvmNamedValue("%this", new LlvmPointer(new LlvmClassStructure(classEnv.nameClass))), offset));
					return res;
				}
				i++;
			}
		}

		return null;
	}
}


/**********************************************************************************/
/* === Tabela de Símbolos ==== 
 * 
 * 
 */
/**********************************************************************************/

class SymTab extends VisitorAdapter{
	public Map<String, ClassNode> classes;
	private ClassNode classEnv;    //aponta para a classe em uso

	public SymTab()
	{
		classes = new HashMap<>();
	}

	public LlvmValue FillTabSymbol(Program n){
		n.accept(this);
		return null;
	}
	public LlvmValue visit(Program n){
		n.mainClass.accept(this);

		for (util.List<ClassDecl> c = n.classList; c != null; c = c.tail)
			c.head.accept(this);

		return null;
	}

	public LlvmValue visit(MainClass n){
		classes.put(n.className.s, new ClassNode(n.className.s, null, null));
		return null;
	}

	public LlvmValue visit(ClassDeclSimple n){

		//lista de tipos
		List<LlvmType> typesList = new LinkedList<>();
		for (util.List<VarDecl> i = n.varList; i != null; i = i.tail){
			typesList.add((LlvmType)i.head.type.accept(this));
		}
		//lista de variaveis
		List<LlvmValue> valuesList = new LinkedList<>();
		for (util.List<VarDecl> i = n.varList; i != null; i = i.tail){
			valuesList.add(i.head.accept(this));
		}

		//insere no mapa de classes o ClassNode
		classes.put(n.name.s, new ClassNode(n.name.s, new LlvmStructure(typesList), valuesList) );
		// Percorre n.methodList visitando cada método
		classEnv = classes.get(n.name.s);
		for (util.List<MethodDecl> i = n.methodList; i != null; i = i.tail){
			i.head.accept(this);
		}
		return null;

	}

	public LlvmValue visit(ClassDeclExtends n){return null;}
	public LlvmValue visit(VarDecl n){
		return new LlvmNamedValue(n.name.s, new LlvmPointer((LlvmType)n.type.accept(this)));
	}
	public LlvmValue visit(Formal n){
		return new LlvmNamedValue(n.name.s, new LlvmPointer((LlvmType)n.type.accept(this)));
	}
	public LlvmValue visit(MethodDecl n){
		String methodName = n.name.s; 
		List<LlvmValue> valuesList = new LinkedList<>();
		List<LlvmValue> formalsList= new LinkedList<>();
		for ( util.List<Formal> l = n.formals; l != null; l=l.tail)
			formalsList.add(l.head.accept(this));
		for ( util.List<VarDecl> l = n.locals; l != null; l=l.tail)
			valuesList.add(l.head.accept(this));
		//insere o metodo na lista de metodos da classEnv
		//montando o methodNode
		classEnv.methodsList.add(new MethodNode(methodName, valuesList, formalsList ));
		return null;
	}
	public LlvmValue visit(IdentifierType n){ return new LlvmPointer(new LlvmClassStructure(n.name));}
	public LlvmValue visit(IntArrayType n){return null;}
	public LlvmValue visit(BooleanType n){return LlvmPrimitiveType.I1;}
	public LlvmValue visit(IntegerType n){return LlvmPrimitiveType.I32;}
}

class ClassNode extends LlvmType {
	public String nameClass;
	public LlvmStructure classType;
	public List<LlvmValue> varList;
	public List<MethodNode> methodsList = new LinkedList<>();
	ClassNode (String nameClass, LlvmStructure classType, List<LlvmValue> varList){
		this.nameClass = nameClass;
		this.classType = classType;
		this.varList = varList;
	}
}

class MethodNode {
	public String methodName;
	public List<LlvmValue> valuesList;
	public List<LlvmValue> formalsList;
	public MethodNode(String methodName, List<LlvmValue> valuesList, List<LlvmValue> formalsList ){
		this.methodName = methodName;
		this.valuesList = valuesList;
		this.formalsList = formalsList;
	}	
}




