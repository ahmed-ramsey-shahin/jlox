package lox;

public class Interpreter implements Expr.Visitor<Object> {

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
                checkDivisionByZero(expr.operator, (double) left, (double) right);
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

    void interpret(Expr expr) {

        try {

            Object value = evaluate(expr);
            System.out.println(stringify(value));

        } catch (RuntimeError error) {

            Lox.runtimeError(error);

        }

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

    private void checkDivisionByZero(Token operator, double left, double right) {

        if(right != 0) return;
        throw new RuntimeError(operator, "Division by zero is prohibited. (عايز تخالف شرع ربنا يا ولد؟)");

    }

}
