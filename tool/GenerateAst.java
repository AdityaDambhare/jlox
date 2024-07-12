package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst{
    public static void main(String args[]) throws IOException{
        if(args.length !=1){
            System.err.println("usage : GenerateAst <outputdirectory>");
            System.exit(64);
        }
        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
      "Binary   : Expr left, Token operator, Expr right",
      "Grouping : Expr expression",
      "Literal  : Object value",
      "Unary    : Token operator, Expr right",
      "Ternary  : Expr condition, Expr if_branch, Expr else_branch",
      "Variable : Token identifier",
      "Assign   : Token name, Expr value",
      "Logical  : Expr left, Token operator, Expr right",
      "Function : List<Token> params, List<Stmt> body",
      "Call     : Expr callee, Token paren, List<Expr> arguments",
      "Get      : Expr object, Token name",
      "Set      : Expr object, Token name, Expr value",
      "This     :Token keyword",
      "Super    : Token keyword, Token method"
    ));
       defineAst(outputDir,"Stmt",Arrays.asList(
        "If : Expr condition , Stmt then_branch , Stmt else_branch",
        "Return     : Token keyword, Expr value",
        "Function   : Token name, Expr.Function function", 
        "While : Expr condition, Stmt statement",
        "Expression : Expr expression",
        "Print : Expr expression",
        "Var : Token name, Expr initializer",
        "Block : List<Stmt> statements",
        "Class : List<Stmt.Function> methods, Expr.Variable superclass , Token name"
       ));
    }
    public static void defineAst(String output,String baseName,List<String> types) 
    throws IOException
    {
        String path = output + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path,"UTF-8");
        writer.println("package jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
        defineVisitor(writer,baseName,types);

        for(String type:types){
            String ClassName = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer,baseName,ClassName,fields);

        }

        writer.println();
        writer.println(" abstract <R> R accept(Visitor<R> visitor);");
        //abstract accept method . to be over-ridden in subclasses
        writer.println("}");
        writer.close();
    }

    public static void defineType(PrintWriter writer,String baseName,String ClassName,String fieldList)
    {   //class definition
        writer.println("static class " + ClassName + " extends " + baseName + "{");
        //class constructor method
        writer.println("    " + ClassName + "(" + fieldList + ") {");
        String[] fields = fieldList.split(", ");

        for (String field : fields) 
        {
           String name = field.split(" ")[1];
           writer.println("      this." + name + " = " + name + ";");
        }
        
        writer.println(" }");
        //defining all fields
        for (String field : fields) 
        {
            writer.println("    final " + field + ";");
        }
    
        //over-riding the accept method for each type
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
        ClassName + baseName + "(this);");
        writer.println("    }");

        
        writer.println("}");
    }

    public static void defineVisitor(PrintWriter writer,String baseName,List<String> types)
    {
        writer.println(" interface Visitor<R> {");
        for(String type:types){
            String typename = type.split(":")[0].trim();
            writer.println(
                " R visit" + typename + baseName+"(" + typename + " " + baseName.toLowerCase() + ");" 
            )
            ;
            //above code prints something like - R visit BinaryExpr(Binary expr);
        }
        writer.println("}");
    }
}
 /*
 the defineType() method will generate something like this :-

 static class Binary extends Expr{
    Binary(Expr left, Token operator, Expr right){
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
    final Expr left;
    final Token operator;
    final Expr right;
 }
 
 */