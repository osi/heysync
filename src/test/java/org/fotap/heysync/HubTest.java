package org.fotap.heysync;

import org.jetlang.core.DisposingExecutor;
import org.jetlang.fibers.FiberStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
