public interface LoxCallable {
    int arity();
    Object call(Interpreter interpreter, java.util.List<Object> arguments);
}
