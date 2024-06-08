package jlox;

class Return extends RuntimeException{
    final Object value;
    Return(Object value){
        super(null,null,false,false);//disables some error handling overhead from jvm
        this.value = value;
    }
}