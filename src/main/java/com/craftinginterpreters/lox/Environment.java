package com.craftinginterpreters.lox;

import java.util.Map;
import java.util.HashMap;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    void define(String name, Object value){
        values.put(name, value);
    }

    void assign(Token name, Object value){
        if (values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int distance, Token name, Object value){
        ancestor(distance).values.put(name.lexeme, value);
    }

    Object get(Token name){
        if (values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if (enclosing != null){
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object getAt(int distance, Token name){
        return ancestor(distance).values.get(name.lexeme);
    }

    Environment ancestor(int distance){
        Environment environment = this;
        for (int i = 0; i < distance; i++){
            environment = environment.enclosing;
        }
        return environment;
    }

}
