package org.fotap.heysync;

import org.jetlang.channels.Publisher;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;
import org.jetlang.fibers.FiberStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.util.Collection;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class HubTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldSendAsyncMessage() {
        Hub hub = new Hub();
        FiberStub executor = new FiberStub();

        Mouse receiver = mock(Mouse.class);
        hub.addReceiver(receiver, executor);

        hub.dispatcherFor(Mouse.class).eatCheese("cheddar");
        verifyZeroInteractions(receiver);
        executor.executeAllPending();
        verify(receiver).eatCheese("cheddar");
    }

    @Test
    public void shouldSendAsyncSignal() {
        Hub hub = new Hub();
        FiberStub executor = new FiberStub();

        Pinger receiver = mock(Pinger.class);
        hub.addReceiver(receiver, executor);

        hub.dispatcherFor(Pinger.class).ping();
        verifyZeroInteractions(receiver);
        executor.executeAllPending();
        verify(receiver).ping();
    }

    @Test
    public void shouldUseProvidedSubscriberInAdditionToGeneratedOne() throws NoSuchMethodException {
        Hub hub = new Hub();
        FiberStub executor = new FiberStub();

        Subscriber<String> provided = mock(Subscriber.class);
        hub.addSubscriber(Mouse.class.getMethod("eatCheese", String.class), provided);

        Mouse receiver = mock(Mouse.class);
        hub.addReceiver(receiver, executor);

        Class<Callback<String>> callbackClass = (Class<Callback<String>>) (Class) Callback.class;
        ArgumentCaptor<Callback<String>> captor = ArgumentCaptor.forClass(callbackClass);
        verify(provided).subscribe(eq(executor), captor.capture());

        captor.getValue().onMessage("cheddar");
        executor.executeAllPending();
        verify(receiver).eatCheese("cheddar");
    }

    @Test
    public void shouldUseProvidedPublisherInAdditionToGeneratedOne() throws NoSuchMethodException {
        Hub hub = new Hub();
        FiberStub executor = new FiberStub();

        Publisher<String> provided = mock(Publisher.class);
        hub.addPublisher(Mouse.class.getMethod("eatCheese", String.class), provided);

        Mouse receiver = mock(Mouse.class);
        hub.addReceiver(receiver, executor);

        hub.dispatcherFor(Mouse.class).eatCheese("cheddar");
        verifyZeroInteractions(receiver);
        executor.executeAllPending();
        verify(receiver).eatCheese("cheddar");
        verify(provided).publish("cheddar");
    }

    @Test
    public void shouldFailWhenTryingToGetDispatcherForNotAtAsynchronousInterface() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot create a dispatcher for " + Collection.class.getName() + ". It must be an interface that is marked with the " + Asynchronous.class
            .getName() + " annotation");

        new Hub().dispatcherFor(Collection.class);
    }

    @Test
    public void shouldFailWhenTryingToGetDispatcherForClass() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot create a dispatcher for " + Object.class.getName() + ". It must be an interface that is marked with the " + Asynchronous.class
            .getName() + " annotation");

        new Hub().dispatcherFor(Object.class);
    }

    @Test
    public void shouldFailWithAddingReceiverThatImplementsNoInterfaces() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(Object.class.getName() + " does not implement any " + Asynchronous.class.getName() + " interfaces");

        new Hub().addReceiver(new Object(), mock(DisposingExecutor.class));
    }
}
