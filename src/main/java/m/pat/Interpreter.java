package m.pat;

import java.util.Collection;
import java.util.HashMap;

public class Interpreter {

    public void interpretFunction(FunctionNode functionNode) throws InterpreterErrorException {
        HashMap<String, InterpreterDataType> localVariables = new HashMap<>();
        // Create local variable IDT HashMap.
        for(VariableNode constOrVar : functionNode.getConstAndVariables()){
            String name = constOrVar.getName();
            InterpreterDataType IDT = constOrVar.getDataType();
            localVariables.put(name, IDT);
        }

        // Pass the local variables and function statements to interpretBlock()
        interpretBlock(localVariables, functionNode.getStatements());

    }

    public void interpretBlock(HashMap<String, InterpreterDataType> localVariables, Collection<StatementNode> statements) throws InterpreterErrorException{
        for(StatementNode statement : statements){
            // StatementNode can be IfNode, ForNode, WhileNode, RepeatNode, AssignmentNode, FunctionCallNode or ParameterNode.
            if(statement instanceof IfNode){

            } else if(statement instanceof ForNode){

            } else if(statement instanceof WhileNode){

            } else if(statement instanceof RepeatNode){

            } else if(statement instanceof AssignmentNode){

            } else if(statement instanceof FunctionCallNode){

            }
        }
    }

    public InterpreterDataType expression(Node node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        if(node instanceof IntegerNode ) {
            return new IntegerDataType(((IntegerNode) node).getValue());
        } else if(node instanceof FloatNode) {
            return new RealDataType(((FloatNode) node).getValue());
        } else if(node instanceof StringNode) {
            return new StringDataType(((StringNode) node).getValue());
        } else if(node instanceof VariableReferenceNode){
            VariableReferenceNode varRef = (VariableReferenceNode) node;
            if(localVariables.get(varRef.getName()) == null){
                throw new InterpreterErrorException("Variable '" + varRef.getName() + "' does not exist or hasn't been declared.");
            }
            return localVariables.get(varRef);
        } else if(node instanceof MathOpNode){
            // Evaluate MathOpNode, both sides must be the same type.
            MathOpNode mathOp = (MathOpNode) node;
            InterpreterDataType left = expression(mathOp.getLeft(), localVariables);
            InterpreterDataType right = expression(mathOp.getRight(), localVariables);
            if(left instanceof IntegerDataType && right instanceof IntegerDataType){
                int leftValue = ((IntegerDataType) left).getInteger();
                int rightValue = ((IntegerDataType) right).getInteger();
                switch(mathOp.getOperation()){
                    case PLUS -> {
                        return new IntegerDataType(leftValue + rightValue);
                    }
                    case MINUS -> {
                        return new IntegerDataType(leftValue - rightValue);
                    }
                    case TIMES -> {
                        return new IntegerDataType(leftValue * rightValue);
                    }
                    case DIVIDE -> {
                        if(rightValue == 0)
                            throw new InterpreterErrorException("Division by zero.");
                        return new IntegerDataType(leftValue / rightValue);
                    }
                    case MOD -> {
                        if(rightValue == 0)
                            throw new InterpreterErrorException("Division by zero.");
                        return new IntegerDataType(leftValue % rightValue);
                    }
                    default -> throw new InterpreterErrorException("Invalid math operator.");
                }
            } else if(left instanceof RealDataType && right instanceof RealDataType){
                float leftValue = ((RealDataType) left).getReal();
                float rightValue = ((RealDataType) right).getReal();
                switch (mathOp.getOperation()) {
                    case PLUS:
                        return new RealDataType(leftValue + rightValue);
                    case MINUS:
                        return new RealDataType(leftValue - rightValue);
                    case TIMES:
                        return new RealDataType(leftValue * rightValue);
                    case DIVIDE:
                        if (rightValue == 0) {
                            throw new InterpreterErrorException("Division by zero.");
                        }
                        return new RealDataType(leftValue / rightValue);
                    default:
                        throw new InterpreterErrorException("Invalid math operator.");
                }
            } else if(left instanceof StringDataType || right instanceof StringDataType){
                String leftValue = left instanceof StringDataType ? ((StringDataType) left).getString() : left.toString();
                String rightValue = right instanceof StringDataType ? ((StringDataType) right).getString() : right.toString();
                return new StringDataType(leftValue + rightValue);
            } else {
                throw new InterpreterErrorException("Invalid expression");
            }
        } else {
            throw new InterpreterErrorException("Invalid expression.");
        }
    }

    public boolean booleanCompare(BooleanCompareNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        InterpreterDataType leftIDT = expression(node.getLeft(), localVariables);
        InterpreterDataType rightIDT = expression(node.getRight(), localVariables);

        // Evaluate boolean comparison.
        switch(node.getComparison()){
            case EQUALS -> {
                return leftIDT.equals(rightIDT);
            }
            case NOT_EQUALS -> {
                return !leftIDT.equals(rightIDT);
            }
            case LESS_THAN -> {
                // Verify that IDT is IntegerDataType or RealDataType.
                if (leftIDT instanceof IntegerDataType && rightIDT instanceof IntegerDataType
                        || leftIDT instanceof RealDataType && rightIDT instanceof RealDataType) {
                    if (leftIDT instanceof IntegerDataType){ // Don't really need to check the right side since it was checked.
                        return ((IntegerDataType) leftIDT).getInteger() < ((IntegerDataType) rightIDT).getInteger();
                    } else {
                        // leftIDT is a real.
                        return ((RealDataType) leftIDT).getReal() < ((RealDataType) rightIDT).getReal();
                    }
                } else {
                    throw new InterpreterErrorException("Less than comparison can only be done on integers or floats.");
                }
            }
            case GREATER_THAN -> {
                if (leftIDT instanceof IntegerDataType && rightIDT instanceof IntegerDataType
                        || leftIDT instanceof RealDataType && rightIDT instanceof RealDataType) {
                    if (leftIDT instanceof IntegerDataType){ // Don't really need to check the right side since it was checked.
                        return ((IntegerDataType) leftIDT).getInteger() > ((IntegerDataType) rightIDT).getInteger();
                    } else {
                        // leftIDT is a real.
                        return ((RealDataType) leftIDT).getReal() > ((RealDataType) rightIDT).getReal();
                    }
                } else {
                    throw new InterpreterErrorException("Greater than comparison can only be done on integers or floats.");
                }
            }
            case LESS_OR_EQUAL -> {
                if (leftIDT instanceof IntegerDataType && rightIDT instanceof IntegerDataType
                        || leftIDT instanceof RealDataType && rightIDT instanceof RealDataType) {
                    if (leftIDT instanceof IntegerDataType){ // Don't really need to check the right side since it was checked.
                        return ((IntegerDataType) leftIDT).getInteger() <= ((IntegerDataType) rightIDT).getInteger();
                    } else {
                        // leftIDT is a real.
                        return ((RealDataType) leftIDT).getReal() <= ((RealDataType) rightIDT).getReal();
                    }
                } else {
                    throw new InterpreterErrorException("Less than or equal comparison can only be done on integers or floats.");
                }
            }
            case GREATER_OR_EQUAL -> {
                if (leftIDT instanceof IntegerDataType && rightIDT instanceof IntegerDataType
                        || leftIDT instanceof RealDataType && rightIDT instanceof RealDataType) {
                    if (leftIDT instanceof IntegerDataType){ // Don't really need to check the right side since it was checked.
                        return ((IntegerDataType) leftIDT).getInteger() >= ((IntegerDataType) rightIDT).getInteger();
                    } else {
                        // leftIDT is a real.
                        return ((RealDataType) leftIDT).getReal() >= ((RealDataType) rightIDT).getReal();
                    }
                } else {
                    throw new InterpreterErrorException("Greater than or equal comparison can only be done on integers or floats.");
                }
            }
            default -> throw new InterpreterErrorException("Invalid boolean comparison operation.");
        }
    }

    public InterpreterDataType variableReference(String name, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        InterpreterDataType varIDT = localVariables.get(name);
        if(varIDT == null)
            throw new InterpreterErrorException("Variable '" + name + "' does not exist or wasn't declared.");
        else
            return varIDT;
    }

    public void ifNode(IfNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        //Collection<StatementNode> statements = node.getStatements();
        IfNode localIfNode = null;
        // Check that the condition for the IfNode is evaluated to be true.
        // If the IfNode condition is false, must follow the chain of else blocks.

        if(!node.hasNext()){
            // No else blocks to evaluate.
            if(booleanCompare(node.getCondition(), localVariables)){
                interpretBlock(localVariables, node.getStatements());
            }
        } else {
            // IfNode is linked.
            while(node.hasNext() && (node = node.next()) != null){
                // Evaluate boolean comparison condition.
                if(booleanCompare(node.getCondition(), localVariables)){
                    interpretBlock(localVariables, node.getStatements());
                } else if(node.hasNext()){
                    // Move on to the next IfNode!
                    node = node.next();
                }
            }
        }
    }

    public void forNode(ForNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        if(node.getFrom() instanceof IntegerNode && node.getTo() instanceof IntegerNode){
            int fromRange = ((IntegerNode) node.getFrom()).getFromRange();
            int toRange = ((IntegerNode) node.getTo()).getToRange();
            //FIXME: Evaluate whether we should use i <= toRange OR i < toRange
            for(int i = fromRange; i <= toRange; i++){
                interpretBlock(localVariables, node.getStatements());
            }

        } else if(node.getFrom() instanceof FloatNode && node.getTo() instanceof FloatNode){
            float fromRange = ((FloatNode) node.getFrom()).getFromRange();
            float toRange = ((FloatNode) node.getTo()).getToRange();

            for(float i = fromRange; i <= toRange; i++){
                interpretBlock(localVariables, node.getStatements());
            }
        } else {
            throw new InterpreterErrorException("Incompatible from -> to range in for loop, must be integer or float. ");
        }
    }

    public void repeatNode(RepeatNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        // Only run & repeat statements if the RepeatNode condition is still satisfied.
        while(booleanCompare(node.getCondition(), localVariables)){
            interpretBlock(localVariables, node.getStatements());
        }
    }

    public void whileNode(WhileNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        while(booleanCompare(node.getCondition(), localVariables)){
            interpretBlock(localVariables, node.getStatements());
        }
    }

    public InterpreterDataType constantNodes(VariableNode node, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        if(!node.isConstant())
            throw new InterpreterErrorException("Node '" + node + "' was not found to be constant.");
        else
            return node.getDataType();
    }

    public void assignment(AssignmentNode assign, HashMap<String, InterpreterDataType> localVariables) throws InterpreterErrorException {
        InterpreterDataType targetIDT = expression(assign.getTarget(), localVariables);
        InterpreterDataType valueIDT = expression(assign.getValue(), localVariables);
        // Assign the target IDT to value IDT.
        targetIDT.FromString(valueIDT.toString());
    }



}
