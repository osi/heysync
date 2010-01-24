package org.fotap.heysync;

import java.util.Arrays;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Instantiator<T> {
    private final Class<? extends T> type;
    private final Object[] initargs;

    Instantiator(Class<? extends T> type, Object... initargs) {
        this.type = type;
        this.initargs = initargs;
    }

    T newInstance() {
        try {
            return type.cast(type.getConstructors()[0].newInstance(initargs));
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate " + type + " with " + Arrays.toString(initargs), e);
        }
    }
}
