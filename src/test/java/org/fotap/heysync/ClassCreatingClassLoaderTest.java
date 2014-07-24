package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class ClassCreatingClassLoaderTest {
    @Test
    public void shouldGeneratePublisher() throws NoSuchMethodException {
        ClassCreatingClassLoader<Mouse> loader = new ClassCreatingClassLoader(Mouse.class);

        Publisher<String> publisher = mock(Publisher.class);
        Map<Method, Publisher<?>> publishers = new HashMap<>();
        for (Method method : loader.methods()) {
            publishers.put(method, null);
        }
        publishers.put(Mouse.class.getMethod("eatCheese", String.class), publisher);
        Mouse mouse = loader.publisher(publishers);

        mouse.eatCheese("cheddar");
        verify(publisher).publish("cheddar");
    }

    @Test
    public void shouldNotGetTwoPublishersBackwards() throws NoSuchMethodException {
        ClassCreatingClassLoader<Cat> loader = new ClassCreatingClassLoader(Cat.class);

        Publisher<String> yarnPublisher = mock(Publisher.class);
        Publisher<Integer> livesPublisher = mock(Publisher.class);

        Map<Method, Publisher<?>> publishers = new HashMap<>();
        publishers.put(Cat.class.getMethod("chaseYarn", String.class), yarnPublisher);
        publishers.put(Cat.class.getMethod("updateLives", Integer.TYPE), livesPublisher);

        Cat cat = loader.publisher(publishers);

        cat.chaseYarn("white");
        cat.updateLives(7);
        verify(yarnPublisher).publish("white");
        verify(livesPublisher).publish(7);
    }

    @Test
    public void shouldHaveMeaningfulToStringOnGeneratedCallbacks() throws NoSuchMethodException {
        Mouse mouse = mock(Mouse.class);
        when(mouse.toString()).thenReturn("underlying mouse");
        ClassCreatingClassLoader classloader = new ClassCreatingClassLoader(Mouse.class);
        assertEquals("[org.fotap.heysync.Mouse.eatCheese(java.lang.String) on underlying mouse]",
                classloader.callbackFor(Mouse.class.getMethod("eatCheese", String.class), mouse).toString());
        assertEquals("[org.fotap.heysync.Mouse.provokeCatsWithTaunt(int,java.lang.String) on underlying mouse]",
                classloader.callbackFor(Mouse.class.getMethod("provokeCatsWithTaunt", Integer.TYPE, String.class), mouse).toString());
    }

    @Test
    public void shouldCreateCallbackForMethod() throws NoSuchMethodException {
        Mouse mouse = mock(Mouse.class);

        Callback<Object> callback = new ClassCreatingClassLoader(Mouse.class).callbackFor(Mouse.class.getMethod("eatCheese",
                        String.class),
                mouse);
        callback.onMessage("Cheddar");

        verify(mouse).eatCheese("Cheddar");
    }

}
