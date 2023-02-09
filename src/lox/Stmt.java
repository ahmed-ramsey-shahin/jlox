// This code is automatically generated using JAVA
package lox;

import java.util.List;

abstract class Stmt {

    abstract <T> T accept(Visitor<T> visitor);

    interface Visitor<T> {

        T visitExpressionStmt(Expression stmt);
        T visitPrintStmt(Print stmt);
        T visitVarStmt(Var stmt);
        T visitBlockStmt(Block stmt);
        T visitIfStmt(If stmt);
        T visitWhileStmt(While stmt);
        T visitFunctionStmt(Function stmt);
        T visitReturnStmt(Return stmt);
        T visitClassStmt(Class stmt);

    }

    static class Expression extends Stmt {

        final Expr expression;

        Expression (Expr expression) {

            this.expression = expression;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitExpressionStmt(this);

        }

    }

    static class Print extends Stmt {

        final Expr expression;

        Print (Expr expression) {

            this.expression = expression;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitPrintStmt(this);

        }

    }

    static class Var extends Stmt {

        final Token name;
        final Expr initializer;

        Var (Token name, Expr initializer) {

            this.name = name;
            this.initializer = initializer;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitVarStmt(this);

        }

    }

    static class Block extends Stmt {

        final List<Stmt> statements;

        Block (List<Stmt> statements) {

            this.statements = statements;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitBlockStmt(this);

        }

    }

    static class If extends Stmt {

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If (Expr condition, Stmt thenBranch, Stmt elseBranch) {

            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitIfStmt(this);

        }

    }

    static class While extends Stmt {

        final Expr condition;
        final Stmt body;

        While (Expr condition, Stmt body) {

            this.condition = condition;
            this.body = body;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitWhileStmt(this);

        }

    }

    static class Function extends Stmt {

        final Token name;
        final List<Token> params;
        final List<Stmt> body;

        Function (Token name, List<Token> params, List<Stmt> body) {

            this.name = name;
            this.params = params;
            this.body = body;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitFunctionStmt(this);

        }

    }

    static class Return extends Stmt {

        final Token name;
        final Expr initializer;

        Return (Token name, Expr initializer) {

            this.name = name;
            this.initializer = initializer;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitReturnStmt(this);

        }

    }

    static class Class extends Stmt {

        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;

        Class (Token name, Expr.Variable superclass, List<Stmt.Function> methods) {

            this.name = name;
            this.superclass = superclass;
            this.methods = methods;

        }

        @Override
        <T> T accept(Visitor<T> visitor) {

            return visitor.visitClassStmt(this);

        }

    }

}
