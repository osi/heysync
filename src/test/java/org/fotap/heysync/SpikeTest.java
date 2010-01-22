package org.fotap.heysync;

import org.jetlang.fibers.FiberStub;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class SpikeTest {
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
}
