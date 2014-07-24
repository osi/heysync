package org.fotap.heysync;

import org.jetlang.fibers.FiberStub;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class LatestOnlyTest {

    @Test
    public void shouldExecuteCallbackWithMostRecentValue() throws Exception {
        InvokedFrequently subscription = mock(InvokedFrequently.class);

        Protocol<InvokedFrequently> protocol = Protocol.create(InvokedFrequently.class);
        FiberStub fiberStub = new FiberStub();
        protocol.subscribe(fiberStub, subscription);
        protocol.publisher().calledFasterThanCanBeProcessed("one");
        protocol.publisher().calledFasterThanCanBeProcessed("two");
        protocol.publisher().calledFasterThanCanBeProcessed("three");
        protocol.publisher().calledFasterThanCanBeProcessed("four");

        fiberStub.executeAllPending();

        verify(subscription).calledFasterThanCanBeProcessed("four");
        verifyNoMoreInteractions(subscription);
    }
}
