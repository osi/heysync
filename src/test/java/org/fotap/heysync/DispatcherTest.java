package org.fotap.heysync;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class DispatcherTest {
    @Test
    public void shouldHaveMeaningfulToStringOnDispatchers() {
        Mouse mouse = new Dispatcher<Mouse>(Mouse.class, new Creators()).proxy();

        assertTrue(mouse.toString(), mouse.toString().matches("Dispatcher@\\(\\d+\\) for " + Mouse.class.getName()));
    }

    @Test
    public void twoOfTheSameDispatcherShouldBeEquals() {
        Mouse mouse = new Dispatcher<Mouse>(Mouse.class, new Creators()).proxy();
        assertEquals(mouse, mouse);
    }

    @Test
    public void twoOfTheSameDispatcherShouldHaveTheSameHashCode() {
        Mouse mouse = new Dispatcher<Mouse>(Mouse.class, new Creators()).proxy();
        assertEquals(mouse.hashCode(), mouse.hashCode());
    }
}
