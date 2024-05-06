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
      "Unary    : Token operator, Expr right"
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
        for(String type:types){
            String ClassName = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer,baseName,ClassName,fields);
        }
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

        //defining all fields
        for (String field : fields) 
        {
            writer.println("    final " + field + ";");
        }

        writer.println("}");
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