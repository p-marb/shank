package m.pat;

import java.util.Collection;
import java.util.HashMap;

abstract class Node {
    abstract public String toString();
}

// MATH OPERATIONS

class IntegerNode extends Node {
    private int value;

    IntegerNode(int value){
        this.value = value;
    }

    private int getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}

class FloatNode extends Node {
    private float value;

    FloatNode(float value){
        this.value = value;
    }

    private float getValue(){
        return this.value;
    }

    @Override
    public String toString(){
        return Float.toString(value);
    }
}

enum MathOp {
    PLUS, MINUS, TIMES, DIVIDE, MOD
}

class MathOpNode extends Node {
    private MathOp operation;
    private Node left, right;

    MathOpNode(MathOp operation, Node left, Node right){
        this.operation = operation;
        this.left = left;
        this.right = right;
    }

    public MathOp getOperation(){
        return this.operation;
    }

    public Node getLeft(){
        return this.left;
    }

    public Node getRight(){
        return this.right;
    }

    @Override
    public String toString(){
        return "(" + left.toString() + " " + operation + " " + right.toString() + ")";
    }
}

// FUNCTIONS AND LOCAL VARIABLES

class BooleanNode extends Node {
    private boolean value;

    /**
     * Constructs a BooleanNode.
     * @param value the value of the boolean
     */
    BooleanNode(boolean value) { this.value = value; }

    private boolean getValue() { return this.value; }

    @Override
    public String toString() { return Boolean.toString(value); }
}

class CharacterNode extends Node {

    private char value;

    /**
     * Constructs a CharacterNode.
     * @param value the character value
     */
    CharacterNode(char value) { this.value = value; }

    private char getValue() { return this.value; }

    @Override
    public String toString(){ return Character.toString(value); }
}

class StringNode extends Node {

    private String value;

    /**
     * Constructs a StringNode.
     * @param value the value of the string
     */
    StringNode(String value) { this.value = value; }

    private String getValue() { return this.value; }

    @Override
    public String toString(){
        return value;
    }
}

class VariableNode extends Node {

    private Node type;
    private String name;
    private boolean isConstant;

    /**
     * Constructs a VariableNode.
     * @param type the type of variable to create (i.e BooleanNode, IntegerNode, StringNode, etc.)
     * @param name the name of the variable
     * @param isConstant whether the variable is a constant or not
     */
    VariableNode(Node type, String name, boolean isConstant){
        this.type = type;
        this.name = name;
        this.isConstant = isConstant;
    }

    @Override
    public String toString(){
        return "(" + name + ":" + type.toString() + ", const:" + isConstant + ")";
    }
}

class StatementNode extends Node {

    StatementNode(){

    }

    @Override
    public String toString(){
        return "";
    }
}

class FunctionNode extends Node {
    private String name;
    private Collection<VariableNode> parameters;
    private Collection<VariableNode> constAndVariables;
    private Collection<StatementNode> statements;

    FunctionNode(String name, Collection<VariableNode> parameters,
                 Collection<VariableNode> constAndVariables,
                 Collection<StatementNode> statements){
        this.name = name;
        this.parameters = parameters;
        this.constAndVariables = constAndVariables;
        this.statements = statements;
    }

    @Override
    public String toString(){
        return "";
    }

}

class ProgramNode extends Node {
    private HashMap<String, FunctionNode> functions;

    ProgramNode(HashMap<String, FunctionNode> functions){
        this.functions = functions;
    }

    @Override
    public String toString(){
        return "";
    }
}