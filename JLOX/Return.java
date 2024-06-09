package jlox;

class Return extends RuntimeException{
    final Token position;
    final Object value;
    Return(Object value,Token position){
        super(null,null,false,false);//disables some error handling overhead from jvm
        this.value = value;
        this.position = position;
    }
}