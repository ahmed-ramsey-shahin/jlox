package lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    Environment() {

        enclosing = null;

    }

    Environment(Environment enclosing) {

        this.enclosing = enclosing;

    }

    void define(Token name, Object value) {

        if(!values.containsKey(name.lexeme)) {

            values.put(name.lexeme, value);
            return;

        }

        throw new RuntimeError(
                name,
                String.format("Variable '%s' is already defined.(شوفت الفاريبول ده فين جبل اكده؟)", name.lexeme)
        );

    }

    Object get(Token name) {

        if(values.containsKey(name.lexeme))
            return values.get(name.lexeme);
        if(enclosing != null)
            return enclosing.get(name);
        throw new RuntimeError(
                name,
                String.format("Undefined variable '%s'.(اجيبه منين ده بقا انااا ؟)", name.lexeme)
        );

    }

    void assign(Token name, Object value) {

        if(values.containsKey(name.lexeme)) {

            values.put(name.lexeme, value);
            return;

        }

        if(enclosing != null) {

            enclosing.assign(name, value);
            return;

        }

        throw new RuntimeError(
                name,
                String.format("Undefined variable '%s'. (يعني معتجولش الفاريبول صوح؟)", name.lexeme)
        );

    }

}
