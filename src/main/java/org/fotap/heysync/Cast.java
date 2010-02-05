package org.fotap.heysync;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class Cast {
    static <T> T as(Object instance) {
        @SuppressWarnings({"unchecked"}) T t = (T) instance;
        return t;
    }
}
