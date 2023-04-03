package m.pat;

public class InterpreterErrorException extends Exception {

    public InterpreterErrorException(String message) {
        super(message);
    }

    public InterpreterErrorException(InterpreterDataType dataType){
        super("Unexpected error while interpreting at: " + dataType);
    }



}
