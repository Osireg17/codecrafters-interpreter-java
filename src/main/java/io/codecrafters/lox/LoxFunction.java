package io.codecrafters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    private final String name;
    private final List<Token> params;
    private final List<Stmt> body;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(String name, List<Token> params, List<Stmt> body, Environment closure, boolean isInitializer) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    @Override
    public int arity() {
        return params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < params.size(); i++) {
            environment.define(params.get(i).lexeme, arguments.get(i));
        }
        try {
            interpreter.executeBlock(body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + name + ">";
    }

    LoxFunction bind(LoxInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new LoxFunction(name, params, body, environment, isInitializer);
  }
}
