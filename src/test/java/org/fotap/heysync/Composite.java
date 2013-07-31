package org.fotap.heysync;

public interface Composite {
    interface A {
        void something(String message);

        void somethingElse(String message);
    }

    interface B {
        void something(String message);

        void somethingElseEntirely(String message);
    }
}
