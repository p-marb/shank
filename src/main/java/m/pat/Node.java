package m.pat;

import java.util.Collection;
import java.util.HashMap;

abstract class Node {
    abstract public String toString();
}

// MATH OPERATIONS

class IntegerNode extends Node {
    private int value;
    private int fromRange;
    private int toRange;

    IntegerNode(int value){
        this.value = value;
    }

    IntegerNode(int value, int fromRange, int toRange){
        this.value = value;
        this.fromRange = fromRange;
        this.toRange = toRange;
    }

    @Override
    public String toString() {
        if(fromRange == 0 && toRange == 0){
            return "IntegerNode(value: " + value + ")";
        }
        return "IntegerNode(value: " + value + ", from: " + fromRange + ", to: " + toRange + ")";
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public int getFromRange() {
        return fromRange;
    }

    public int getToRange() {
        return toRange;
    }
}

class FloatNode extends Node {
    private float value;
    private float fromRange;
    private float toRange;

    FloatNode(float value){
        this.value = value;
    }

    FloatNode(float value, float fromRange, float toRange){
        this.value = value;
        this.fromRange = fromRange;
        this.toRange = toRange;
    }
    @Override
    public String toString(){
        if(fromRange == 0.0 && toRange == 0.0){
            return "FloatNode(value: " + value + ")";
        }
        return "FloatNode(value: " + value + ", from: " + fromRange + ", to: " + toRange + ")";
    }

    public void setValue(float newValue){
        this.value = newValue;
    }

    public float getValue(){
        return this.value;
    }
    public float getFromRange() {
        return fromRange;
    }

    public float getToRange() {
        return toRange;
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
        return "MathOpNode(" + left.toString() + " " + operation + " " + right.toString() + ")";
    }
}

// LOGIC
enum BooleanComparison {
    GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL, EQUALS, NOT_EQUALS
}

class BooleanCompareNode extends Node {

    private BooleanComparison comparison;
    private Node left, right;

    BooleanCompareNode(BooleanComparison comparison, Node left, Node right){
        this.comparison = comparison;
        this.left = left;
        this.right = right;
    }
    public BooleanComparison getComparison(){
        return this.comparison;
    }
    public Node getLeft(){
        return this.left;
    }
    public Node getRight(){
        return this.right;
    }
    @Override
    public String toString(){
        return "BooleanCompareNode(comparison: " + comparison + ", left: " + left + ", right: " + right + ")";
    }
}

// FUNCTIONS AND LOCAL VARIABLES

class BooleanNode extends Node {
    private boolean value;

    /**
     * Constructs a BooleanNode.
     * @param value the value of the boolean
     */
    BooleanNode(boolean value) {
        this.value = value;
    }

    private boolean getValue() { return this.value; }

    @Override
    public String toString() {
        return "BooleanNode(value: " + value + ")";
    }
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
    public String toString(){
        return "CharacterNode(value: " + value + ")";
    }
}

class StringNode extends Node {

    private String value;
    private int fromRange;
    private int toRange;

    /**
     * Constructs a StringNode.
     * @param value the value of the string
     */
    StringNode(String value) { this.value = value; }
    StringNode(String value, int fromRange, int toRange){
        this.value = value;
        this.fromRange = fromRange;
        this.toRange = toRange;
    }

    @Override
    public String toString(){
        if(fromRange == 0 && toRange == 0) {
            return "StringNode(value: " + value + ")";
        }
        return "StringNode(value: " + value + ", from: " + fromRange + ", to: " + toRange + ")";
    }

    public void setValue(String newValue){
        this.value = newValue;
    }

    public String getValue() { return this.value; }

    public int getFromRange() {
        return fromRange;
    }

    public int getToRange() {
        return toRange;
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

    public boolean isConstant(){
        return this.isConstant;
    }

    public Node getType(){
        return this.type;
    }

    public String getName() { return this.name; }

    public void setType(Node type){
        this.type = type;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setConstant(boolean isConstant){
        this.isConstant = isConstant;
    }

    /**
     * Helper function to create IDT out of VariableNode.
     * @return corresponding IDT from VariableNode, or null.
     */
    public InterpreterDataType getDataType() {
        if (this.type instanceof BooleanNode) {
            return new BooleanDataType();
        } else if (this.type instanceof IntegerNode) {
            return new IntegerDataType();
        } else if (this.type instanceof StringNode) {
            return new StringDataType("");
        } else if(this.type instanceof FloatNode) {
            return new RealDataType();
        } else if(this.type instanceof CharacterNode){
            return new CharacterDataType();
        }
        return null;
    }


    @Override
    public String toString(){
        return "(name: " + name + ", type: " + type.toString() + ", const:" + isConstant + ")";
    }
}

class VariableReferenceNode extends Node {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    private Node index;

    VariableReferenceNode(String name){
        this.name = name;
        this.index = null;
        System.out.println("Create VarRefNode " + this);
    }

    VariableReferenceNode(String name, Node node){
        this.name = name;
        this.index = node;
        System.out.println("Create VarRefNode " + this);
    }

    public void setIndex(Node node){
        this.index = node;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        if (index != null)
            return "VariableReferenceNode((name: " + name + ", (node: " + index.toString() + "))";
        else
            return "VariableReferenceNode(name: " + name + ")";
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

class IfNode extends StatementNode {
    private BooleanCompareNode condition;

    public void setCondition(BooleanCompareNode condition) {
        this.condition = condition;
    }

    public void setStatements(Collection<StatementNode> statements) {
        this.statements = statements;
    }

    public void setNextIf(IfNode nextIf) {
        this.nextIf = nextIf;
    }

    private Collection<StatementNode> statements;
    private IfNode nextIf;

    IfNode(BooleanCompareNode condition, Collection<StatementNode> statements){
        this.condition = condition;
        this.statements = statements;
    }

    IfNode(BooleanCompareNode condition, Collection<StatementNode> statements, IfNode nextIf){
        this.condition = condition;
        this.statements = statements;
        this.nextIf = nextIf;
    }

    public BooleanCompareNode getCondition(){
        return this.condition;
    }

    public Collection<StatementNode> getStatements(){
        return this.statements;
    }

    public boolean hasNext(){
        return this.nextIf != null;
    }

    public IfNode next(){
        return this.nextIf;
    }

    @Override
    public String toString(){
        return "IfNode(condition: " + condition + ", statements: " + statements + ", nextIf: " + nextIf + ")";
    }
}

class WhileNode extends StatementNode {
    private BooleanCompareNode condition;
    private Collection<StatementNode> statements;

    WhileNode(BooleanCompareNode condition, Collection<StatementNode> statements){
        this.condition = condition;
        this.statements = statements;
    }

    public BooleanCompareNode getCondition(){
        return this.condition;
    }

    public Collection<StatementNode> getStatements(){
        return this.statements;
    }

    @Override
    public String toString(){
        return "WhileNode(condition: " + condition + ", statements: " + statements + ")";
    }
}

class RepeatNode extends StatementNode {
    private BooleanCompareNode condition;
    private Collection<StatementNode> statements;

    RepeatNode(BooleanCompareNode condition, Collection<StatementNode> statements){
        this.condition = condition;
        this.statements = statements;
    }

    public BooleanCompareNode getCondition(){
        return this.condition;
    }

    public Collection<StatementNode> getStatements(){
        return this.statements;
    }

    @Override
    public String toString(){
        return "RepeatNode(condition: " + condition + ", statements: " + statements + ")";
    }
}

class ForNode extends StatementNode {
    private Node from;
    private Node to;
    private VariableReferenceNode varReference;
    private Collection<StatementNode> statements;

    ForNode(Node from, Node to, Collection<StatementNode> statements){
        this.from = from;
        this.to = to;
        this.statements = statements;
    }

    ForNode(VariableReferenceNode varReference, Node from, Node to, Collection<StatementNode> statements){
        this.varReference = varReference;
        this.from = from;
        this.to = to;
        this.statements = statements;
    }

    public Node getFrom(){
        return this.from;
    }

    public Node getTo(){
        return this.to;
    }

    public Collection<StatementNode> getStatements(){
        return this.statements;
    }

    public String toString(){
        if(this.varReference != null)
            return "ForNode(varReference: " + varReference + ", from: " + from + ", to: " + to + ", statements: " + statements + ")";
        else
            return "ForNode(from: " + from + ", to: " + from + ", statements: " + statements + ")";
    }


}

class FunctionCallNode extends StatementNode {
    private String name;
    private Collection<ParameterNode> parameters;

    FunctionCallNode(String name, Collection<ParameterNode> parameters){
        this.name = name;
        this.parameters = parameters;
        if(Shank.DEBUG) System.out.println("Built FunctionCallNode: " + this);
    }

    @Override
    public String toString(){
        return "FunctionCallNode(name: " + name + ", parameters: " + parameters + ")";
    }

    public Collection<ParameterNode> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    // To check if the function name is language-specific function.
    public boolean isVariadic(){
        switch (getName()){
            case "write", "read" -> { // Write/Read can take at least 1 and potentially any number of variables.
                return true;
            }
        }
        return false;
    }
}

class ParameterNode extends StatementNode {
    private VariableReferenceNode variableRef;
    private Node node;

    ParameterNode(VariableReferenceNode variableRef, Node node){
        this.variableRef = variableRef;
        this.node = node;
    }

    ParameterNode(VariableReferenceNode variableRef){
        this.variableRef = variableRef;
    }

    ParameterNode (Node node){
        this.node = node;
    }

    @Override
    public String toString(){
        if(variableRef == null){
            // Assume Node is not null.
            return "ParameterNode(node: " + node + ")";
        }
        return "ParameterNode(variableRef: " + variableRef + ")";
    }

    public VariableReferenceNode getVariableRef() {
        return variableRef;
    }

    public Node getNode() {
        return node;
    }
}

class AssignmentNode extends StatementNode {

    private VariableReferenceNode target;
    private Node value;

    AssignmentNode(VariableReferenceNode target, Node value){
        this.target = target;
        this.value = value;
    }

    /**
     * Target defines what is actually going to be changed.
     * @return
     */
    public VariableReferenceNode getTarget(){
        return this.target;
    }

    /**
     * Value is what the target will be assigned to.
     * @return
     */
    public Node getValue(){
        return this.value;
    }

    @Override
    public String toString(){ return "AssignmentNode(target: " + target + ", value: " + value + ")"; }
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

    public String getName(){
        return this.name;
    }

    public Collection<VariableNode> getParameters(){
        return this.parameters;
    }

    public Collection<VariableNode> getConstAndVariables(){
        return this.constAndVariables;
    }

    public Collection<StatementNode> getStatements(){
        return this.statements;
    }

    @Override
    public String toString(){
        return "FunctionNode(name: " + getName() + ", params: " + getParameters()
                + ", constants & variables: (" + getConstAndVariables() +"), " +
                "statements: " + statements.size() + ")";
    }

}

class ProgramNode extends Node {
    private HashMap<String, FunctionNode> functions;

    ProgramNode(HashMap<String, FunctionNode> functions){
        this.functions = functions;
    }

    @Override
    public String toString(){
        String programNodeString = "ProgramNode(" + functions.size() + ")\n";
        int functionCount = 0;
        for(FunctionNode functionNode : functions.values()){
            functionCount++;
            programNodeString += "[" + functionCount + "] " + functionNode + "\n";
        }
        return programNodeString;
    }
}