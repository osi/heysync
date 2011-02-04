package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class ClassCreatingClassloaderTest {
    @Test
    public void shouldGeneratePublisher() throws NoSuchMethodException {
        ClassCreatingClassloader loader = new ClassCreatingClassloader();

        Publisher<String> publisher = mock(Publisher.class);
        Mouse mouse = loader.publisherFor(Mouse.class,
                Collections.<Method, Publisher<?>>singletonMap(
                        Mouse.class.getMethod("eatCheese", String.class), publisher));

        mouse.eatCheese("cheddar");
        verify(publisher).publish("cheddar");
    }

    @Test
    public void shouldNotGetTwoPublishersBackwards() throws NoSuchMethodException {
        ClassCreatingClassloader loader = new ClassCreatingClassloader();

        Publisher<String> yarnPublisher = mock(Publisher.class);
        Publisher<Integer> livesPublisher = mock(Publisher.class);

        Map<Method, Publisher<?>> publishers = new HashMap<Method, Publisher<?>>();
        publishers.put(Cat.class.getMethod("chaseYarn", String.class), yarnPublisher);
        publishers.put(Cat.class.getMethod("updateLives", Integer.TYPE), livesPublisher);

        Cat cat = loader.publisherFor(Cat.class, publishers);

        cat.chaseYarn("white");
        cat.updateLives(7);
        verify(yarnPublisher).publish("white");
        verify(livesPublisher).publish(7);
    }

    @Test
    public void shouldHaveMeaningfulToStringOnGeneratedCallbacks() throws NoSuchMethodException {
        Mouse mouse = mock(Mouse.class);
        when(mouse.toString()).thenReturn("underlying mouse");
        ClassCreatingClassloader classloader = new ClassCreatingClassloader();
        assertEquals("[org.fotap.heysync.Mouse.eatCheese(java.lang.String) on underlying mouse]",
                classloader.callbackFor(Mouse.class.getMethod("eatCheese", String.class), mouse).toString());
        assertEquals("[org.fotap.heysync.Mouse.provokeCatsWithTaunt(int,java.lang.String) on underlying mouse]",
                classloader.callbackFor(Mouse.class.getMethod("provokeCatsWithTaunt", Integer.TYPE, String.class), mouse).toString());
    }

    @Test
    public void shouldCreateCallbackForMethod() throws NoSuchMethodException {
        Mouse mouse = mock(Mouse.class);

        Callback<Object> callback = new ClassCreatingClassloader().callbackFor(Mouse.class.getMethod("eatCheese",
                String.class),
                mouse);
        callback.onMessage("Cheddar");

        verify(mouse).eatCheese("Cheddar");
    }
}
