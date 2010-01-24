package org.fotap.heysync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that must be applied to any interfaces that are used for asynchronous messaging with heysync.
 *
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Asynchronous {
}
