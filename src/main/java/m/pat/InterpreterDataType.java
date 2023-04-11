package m.pat;

import java.util.ArrayList;

abstract class InterpreterDataType {
    public abstract String toString();
    public abstract void FromString(String input);
}

class IntegerDataType extends InterpreterDataType {

    private int integer;

    IntegerDataType(){

    }

    IntegerDataType(int integer){
        this.integer = integer;
    }

    public int getInteger(){
        return this.integer;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}

class RealDataType extends InterpreterDataType {

    private float real;

    RealDataType(){}

    RealDataType(float real){
        this.real = real;
    }

    public float getReal(){
        return this.real;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}
class ArrayDataType<T extends InterpreterDataType> extends InterpreterDataType {

    private ArrayList<T> dataTypeList = new ArrayList<>();

    ArrayDataType(T dataType){
        this.dataTypeList.add(dataType);
    }

    ArrayDataType(ArrayList<T> dataTypes){
        this.dataTypeList = dataTypes;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}

class StringDataType extends InterpreterDataType {

    private String string;

    StringDataType(String string){
        this.string = string;
    }

    public String getString(){
        return this.string;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}

class CharacterDataType extends InterpreterDataType {

    private char character;

    CharacterDataType(){}

    CharacterDataType(char character){
        this.character = character;
    }

    public char getCharacter(){
        return this.character;
    }

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}

class BooleanDataType extends InterpreterDataType {

    @Override
    public String toString() {
        return null;
    }

    @Override
    public void FromString(String input) {

    }
}