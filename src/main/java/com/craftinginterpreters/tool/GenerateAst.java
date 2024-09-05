package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.err.println("Usage: generate_ast <output_director>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign   : Token name, Expr value",
                "Binary   : Expr left, Token operator, Expr right",
                "Call     : Expr callee, Token paren, List<Expr> arguments",
                "Get      : Expr object, Token name",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Logical  : Expr left, Token operator, Expr right",
                "Set      : Expr object, Token name, Expr value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expr right",
                "Variable : Token name"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block      : List<Stmt> statements",
                "Class      : Token name, Expr.Variable superclass, List<Stmt.Function> methods",
                "Expression : Expr expression",
                "Function   : Token name, List<Token> params, List<Stmt> body",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
                "Print      : Expr expression",
                "Return     : Token keyword, Expr value",
                "Var        : Token name, Expr initializer",
                "While      : Expr condition, Stmt body"
        ));

    }
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{

        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);

        // Package and imports
        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");

        // Base class signature.
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // Visitor interface declaration.
        writer.println();
        defineVisitor(writer, baseName, types);

        // Base class abstract accept() method declaration.
        writer.println();
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        // The AST classes.
        for (String type : types){
            writer.println();
            String className = type.split(":")[0].strip();
            String fields = type.split(":")[1].strip();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.println();
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type: types){
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }
        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        String[] fields = fieldList.split(", ");

        // Class declaration
        writer.println("    static class " + className + " extends " + baseName + " {");

        // Fields declarations
        writer.println();
        for (String field : fields){
            writer.println("        final " + field + ";");
        }

        // Constructor
        writer.println();
        writer.println("        " + className + "(" + fieldList + ") {");
        for (String field : fields){
            String fieldName = field.split(" ")[1];
            writer.println("            this." + fieldName + " = " + fieldName + "; ");
        }
        writer.println("        }");

        // Abstract accept() method implementation (Visitor pattern).
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName +"(this);");
        writer.println("        }");

        writer.println("    }");
    }
}
