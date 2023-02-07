package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {

    public static void main(String[] args) throws IOException {

        if(args.length != 1) {

            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);

        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right",
                "Variable : Token name",
                "Assign   : Token name, Expr value",
                "Logical  : Expr left, Token operator, Expr right"
        ));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer",
                "Block      : List<Stmt> statements",
                "If         : Expr condition, Stmt thenBranch, Stmt elseBranch"
        ));

    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {

        // Define the base class
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, StandardCharsets.UTF_8);
        writer.println("// This code is automatically generated using JAVA");
        writer.println("package lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.printf("abstract class %s {\n", baseName);
        writer.println();

        // Define the accept method
        writer.println("    abstract <T> T accept(Visitor<T> visitor);");
        writer.println();

        // Define the visitor interface
        defineVisitor(writer, baseName, types);
        writer.println();

        // Define the subclasses
        for (String type : types) {

            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
            writer.println();

        }

        writer.println("}");
        writer.close();

    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {

        writer.println("    interface Visitor<T> {");
        writer.println();

        for (String type : types) {

            String typeName = type.split(":")[0].trim();
            writer.printf("        T visit%s%s(%s %s);\n", typeName, baseName, typeName, baseName.toLowerCase());

        }

        writer.println();
        writer.println("    }");

    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {

        String[] fields = fieldList.split(", ");
        writer.printf("    static class %s extends %s {\n", className, baseName);
        writer.println();

        // Define the fields
        for(String field : fields) {

            writer.printf("        final %s;\n", field);

        }

        writer.println();
        writer.printf("        %s (%s) {\n", className, fieldList);
        writer.println();

        // Define the constructor
        for (String field : fields) {

            String name = field.split(" ")[1];
            writer.printf("            this.%s = %s;\n", name, name);

        }

        writer.println();
        writer.println("        }");
        writer.println();

        // Visitor pattern
        writer.println("        @Override");
        writer.println("        <T> T accept(Visitor<T> visitor) {");
        writer.println();
        writer.printf("            return visitor.visit%s%s(this);\n", className, baseName);
        writer.println();
        writer.println("        }");
        writer.println();
        writer.println("    }");

    }

}
