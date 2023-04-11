package m.pat;

public class InterpreterErrorException extends Exception {

    public InterpreterErrorException(String message) {
        super(message);
    }

    public InterpreterErrorException(InterpreterDataType dataType){
        super("Error while interpreting:  " + dataType);
    }



}
