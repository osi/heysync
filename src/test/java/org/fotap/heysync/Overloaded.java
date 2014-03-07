package org.fotap.heysync;

@Asynchronous
public interface Overloaded {

    void doSomething(String one);

    void doSomething(String one, String two);
}
