package org.fotap.heysync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated with this will receive the most recent value at the time of invocation.
 * <p/>
 * That is, if the method is called more frequently than the implementation can react, intermediate values will
 * be lost and only the last one will be made available.
 *
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LatestOnly {
}
