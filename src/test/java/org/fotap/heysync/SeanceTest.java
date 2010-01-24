package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.core.Callback;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class SeanceTest {
    @Test
    public void shouldGenerateMedium() throws NoSuchMethodException {
        Creators creators = new Creators();

        Publisher<String> publisher = mock(Publisher.class);
        Mouse mouse = creators.publisherFor(Mouse.class,
                                            Collections.<Method, Publisher<?>>singletonMap(
                                                Mouse.class.getMethod("eatCheese", String.class), publisher));

        mouse.eatCheese("cheddar");
        verify(publisher).publish("cheddar");
    }

    @Test
    public void shouldNotGetTwoPublishersBackwards() throws NoSuchMethodException {
        Creators creators = new Creators();

        Publisher<String> yarnPublisher = mock(Publisher.class);
        Publisher<Integer> livesPublisher = mock(Publisher.class);

        Map<Method, Publisher<?>> publishers = new HashMap<Method, Publisher<?>>();
        publishers.put(Cat.class.getMethod("chaseYarn", String.class), yarnPublisher);
        publishers.put(Cat.class.getMethod("updateLives", Integer.class), livesPublisher);

        Cat cat = creators.publisherFor(Cat.class, publishers);

        cat.chaseYarn("white");
        cat.updateLives(7);
        verify(yarnPublisher).publish("white");
        verify(livesPublisher).publish(7);
    }

    @Test
    @Ignore
    public void shouldBoxPrimitiveArgument() {
    }

    @Test
    @Ignore
    public void shouldSendObjectForNoArgMethods() {
    }

    @Test
    public void shouldCreateCallbackForMethod() throws NoSuchMethodException {
        Mouse mouse = mock(Mouse.class);

        Callback<Object> callback = new Creators().callbackFor(Mouse.class.getMethod("eatCheese", String.class), mouse);
        callback.onMessage("Cheddar");

        verify(mouse).eatCheese("Cheddar");
    }


}
