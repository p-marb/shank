package m.pat;

import java.util.Collection;
import java.util.Random;
import java.util.Scanner;

abstract class BuiltIn extends FunctionNode {

    BuiltIn(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }
    abstract void execute(Collection<InterpreterDataType> dataTypes);
}

// IO Functions

class BuiltInRead extends BuiltIn {

    BuiltInRead(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Scanner inputScan = new Scanner(System.in);
        for(InterpreterDataType dataType : dataTypes){
            // read input into string data types
            String input = inputScan.nextLine();
            if(dataType instanceof StringDataType stringDataType){
                stringDataType.FromString(input);
            } else if(dataType instanceof CharacterDataType characterDataType){
                characterDataType.FromString(input);
            } else if(dataType instanceof RealDataType realDataType){
                realDataType.FromString(input);
            } else if(dataType instanceof IntegerDataType integerDataType){
                integerDataType.FromString(input);
            } else if(dataType instanceof BooleanDataType booleanDataType){
                booleanDataType.FromString(input);
            }
        }
    }
}

class BuiltInWrite extends BuiltIn {

    BuiltInWrite(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        for(InterpreterDataType dataType : dataTypes){
            // write values from data types to output
            if(dataType instanceof StringDataType stringDataType){
                System.out.print(stringDataType + " ");
            } else if(dataType instanceof CharacterDataType characterDataType){
                System.out.print(characterDataType + " ");
            } else if(dataType instanceof RealDataType realDataType){
                System.out.print(realDataType + " ");
            } else if(dataType instanceof IntegerDataType integerDataType){
                System.out.print(integerDataType + " ");
            } else if(dataType instanceof BooleanDataType booleanDataType){
                System.out.print(booleanDataType + " ");
            }
        }
    }

}

// String Functions.

class BuiltInLeft extends BuiltIn {

    BuiltInLeft(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        StringBuilder result = new StringBuilder();
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof StringDataType){
            String input = dTypes[0].toString();
            if(dTypes[1] instanceof IntegerDataType){
                int length = Integer.parseInt(dTypes[1].toString());
                result.append(input.substring(0, length));
                if(dTypes[2] instanceof StringDataType){
                    ((StringDataType) dTypes[2]).FromString(result.toString());
                }
            }
        }
    }
}
class BuiltInRight extends BuiltIn {

    BuiltInRight(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        StringBuilder result = new StringBuilder();
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof StringDataType){
            // Read string input
            String input = dTypes[0].toString();
            if(dTypes[1] instanceof IntegerDataType){
                // Read length
                int length = Integer.parseInt(dTypes[1].toString());
                result.append(input.substring(input.length() - length));
                if(dTypes[2] instanceof StringDataType){
                    // Store result
                    ((StringDataType) dTypes[2]).FromString(result.toString());
                }
            }
        }
    }
}
class BuiltInSubstring extends BuiltIn {

    BuiltInSubstring(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        StringBuilder result = new StringBuilder();
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof StringDataType){
            String input = dTypes[0].toString();
            if(dTypes[1] instanceof IntegerDataType){
                int index = Integer.parseInt(dTypes[1].toString());
                if(dTypes[2] instanceof IntegerDataType){
                    int length = Integer.parseInt(dTypes[2].toString());
                    if(dTypes[3] instanceof StringDataType resultVar){
                        result.append(input.substring(index, index + length));
                        resultVar.FromString(result.toString());
                    }
                }
            }
        }
    }
}

// Number Functions.

class BuiltInSquareRoot extends BuiltIn {

    BuiltInSquareRoot(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof RealDataType){
            float sqrt = (float) Math.sqrt(((RealDataType) dTypes[0]).getReal());
            if(dTypes[1] instanceof RealDataType){
                ((RealDataType) dTypes[1]).FromString(sqrt + "");
            } else {
                System.err.println("Expected to find a Float Data Type for sqrt function.");
            }
        }else {
            System.err.println("Expected to find a Float Data Type for sqrt function.");
        }
    }
}

class BuiltInGetRandom extends BuiltIn {

    BuiltInGetRandom(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof IntegerDataType){
            Random random = new Random();
            ((IntegerDataType) dTypes[0]).FromString(random.nextInt() + "");
        }
    }
}

class BuiltInIntegerToReal extends BuiltIn {

    BuiltInIntegerToReal(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof RealDataType){
            if(dTypes[1] instanceof IntegerDataType){
                ((RealDataType) dTypes[0]).FromString(dTypes[1].toString());
            }
        }
    }
}

class BuiltInRealToInteger extends BuiltIn {

    BuiltInRealToInteger(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof IntegerDataType){
            if(dTypes[1] instanceof RealDataType){
                ((IntegerDataType) dTypes[0]).FromString(dTypes[1].toString());
            }
        }
    }
}

// Array functions.

class BuiltInStart extends BuiltIn {

    BuiltInStart(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {
        Object[] dTypes = dataTypes.toArray();
        if(dTypes[0] instanceof ArrayDataType){
            
        }
    }
}

class BuiltInEnd extends BuiltIn {

    BuiltInEnd(String name, Collection<VariableNode> parameters, Collection<VariableNode> constAndVariables, Collection<StatementNode> statements) {
        super(name, parameters, constAndVariables, statements);
    }

    @Override
    void execute(Collection<InterpreterDataType> dataTypes) {

    }
}