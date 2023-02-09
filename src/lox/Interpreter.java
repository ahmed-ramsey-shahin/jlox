package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {

        globals.define("clock", new LoxCallable() {

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                return (double) System.currentTimeMillis() / 1000.0;

            }

            @Override
            public int arity() {

                return 0;

            }

            @Override
            public String toString() {

                return "<native fun>";

            }

        });

        globals.define("read", new LoxCallable() {

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                InputStreamReader input = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(input);

                try {

                    return reader.readLine();

                } catch (IOException err) {

                    return null;

                }

            }

            @Override
            public int arity() {

                return 0;

            }

            @Override
            public String toString() {

                return "<native fun>";

            }

        });

        globals.define("printF", new LoxCallable() {

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                System.out.print(arguments.get(0));
                return null;

            }

            @Override
            public int arity() {

                return 1;

            }

            @Override
            public String toString() {

                return "<native fun>";

            }

        });

        globals.define("printFLine", new LoxCallable() {

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {

                System.out.println(arguments.get(0));
                return null;

            }

            @Override
            public int arity() {

                return 1;

            }

            @Override
            public String toString() {

                return "<native fun>";

            }

        });

    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {

        Object value = null;
        if(stmt.initializer != null) value = evaluate(stmt.initializer);
        throw new Return(value);

    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {

        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;

    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {

        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments)
            arguments.add(evaluate(argument));
        if(!(callee instanceof LoxCallable function))
            throw new RuntimeError(
                    expr.paren,
                    "Can't call anything but functions and classes"
            );
        if(arguments.size() != function.arity())
            throw new RuntimeError(
                    expr.paren,
                    String.format(
                            "Expected %d arguments but got %d.",
                            function.arity(),
                            arguments.size()
                    )
            );
        return function.call(this, arguments);

    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {

        while (isTruthy(evaluate(stmt.condition))) {

            execute(stmt.body);

        }
        return null;

    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {

        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR) {

            if(isTruthy(left)) return true;

        } else {

            if(!isTruthy(left)) return left;

        }

        return evaluate(expr.right);

    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {

        if(isTruthy(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if(stmt.elseBranch != null)
            execute(stmt.elseBranch);
        return null;

    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {

        executeBlock(stmt.statements, new Environment(environment));
        return null;

    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {

        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if(distance != null)
            environment.assignAt(distance, expr.name, value);
        else
            globals.assign(expr.name, value);
        return value;

    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {

        Object value = null;
        if(stmt.initializer != null)
            value = evaluate(stmt.initializer);
        environment.define(stmt.name.lexeme, value);
        return null;

    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {

        return lookUpVariable(expr.name, expr);

    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {

        evaluate(stmt.expression);
        return null;

    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {

        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;

    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {

        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                checkDivisionByZero(expr.operator, (double) right);
                return (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            }
            case PLUS -> {
                if(left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if(left instanceof String && right instanceof String)
                    return left + (String) right;
                if(left instanceof Double)
                    left = stringify(left);
                if(right instanceof Double)
                    right = stringify(right);
                return left + (String) right;
            }
            case EQUAL_EQUAL -> { return isEqual(left, right); }
            case BANG_EQUAL -> { return !isEqual(left, right); }

        }

        // unreachable
        return null;

    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {

        return evaluate(expr.expression);

    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {

        return expr.value;

    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {

        Object right = evaluate(expr.right);

        switch (expr.operator.type) {

            case MINUS -> { checkNumberOperand(expr.operator, right); return -(double)right; }
            case BANG -> { return !isTruthy(right); }

        }

        // unreachable
        return null;

    }

    void interpret(List<Stmt> statements) {

        try {

            for(Stmt statement : statements) {

                execute(statement);

            }

        } catch (RuntimeError error) {

            Lox.runtimeError(error);

        }

    }

    private void execute(Stmt statement) {

        statement.accept(this);

    }

    private String stringify(Object object) {

        if(object == null) return "nil";

        if(object instanceof Double) {

            String text = object.toString();
            if(text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;

        }

        return object.toString();

    }

    private Object evaluate(Expr expr) {

        return expr.accept(this);

    }

    private boolean isTruthy(Object object) {

        if(object == null) return false;
        if(object instanceof Boolean) return (boolean) object;
        return true;

    }

    private boolean isEqual(Object left, Object right) {

        if(left == null && right == null) return true;
        if(left == null) return false;
        return left.equals(right);

    }

    private void checkNumberOperand(Token operator, Object operand) {

        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");

    }

    private void checkNumberOperands(Token operator, Object left, Object right) {

        if(left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be a numbers.");

    }

    private void checkDivisionByZero(Token operator, double right) {

        if(right != 0) return;
        throw new RuntimeError(operator, "Division by zero is prohibited. (عايز تخالف شرع ربنا يا ولد؟)");

    }

    void executeBlock(List<Stmt> statements, Environment environment) {


        Environment previousEnv = this.environment;

        try {

            this.environment = environment;

            for(Stmt statement : statements) {

                execute(statement);

            }

        } finally {

            this.environment = previousEnv;

        }

    }

    void resolve(Expr expr, int depth) {

        locals.put(expr, depth);

    }

    private Object lookUpVariable(Token name, Expr expr) {

        Integer distance = locals.get(expr);
        if(distance != null)
            return environment.getAt(distance, name.lexeme);
        else
            return globals.get(name);

    }

}
