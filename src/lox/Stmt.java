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

}
