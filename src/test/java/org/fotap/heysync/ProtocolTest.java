package org.fotap.heysync;

import org.jetlang.core.SynchronousDisposingExecutor;
import org.jetlang.fibers.FiberStub;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class ProtocolTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldSendAsyncMessage() {
        FiberStub executor = new FiberStub();

        Protocol<Mouse> protocol = Protocol.create(Mouse.class);
        Mouse receiver = mock(Mouse.class);
        protocol.subscribe(executor, receiver);

        protocol.publisher().eatCheese("cheddar");
        verifyZeroInteractions(receiver);
        executor.executeAllPending();
        verify(receiver).eatCheese("cheddar");
    }

    @Test
    public void shouldBeAbleToCreateMultipleProxiesFromFactory() {
        Protocol.Factory<Mouse> factory = Protocol.Factory.create(Mouse.class);
        factory.create();
        factory.create();
    }

    @Test
    public void shouldHandleMultipleSubscribers() {
        FiberStub executor = new FiberStub();

        Protocol<Mouse> protocol = Protocol.create(Mouse.class);
        protocol.subscribe(executor, mock(Mouse.class, "one"));
        protocol.subscribe(executor, mock(Mouse.class, "two"));
    }

    @Test
    public void shouldSendAsyncSignal() {
        FiberStub executor = new FiberStub();

        Protocol<Pinger> protocol = Protocol.create(Pinger.class);
        Pinger receiver = mock(Pinger.class);
        protocol.subscribe(executor, receiver);

        protocol.publisher().ping();
        verifyZeroInteractions(receiver);
        executor.executeAllPending();
        verify(receiver).ping();
    }

    @Test
    public void shouldFailWhenTryingToGetDispatcherForNotAtAsynchronousInterface() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot create a protocol for " + Collection.class.getName() + ". It must be an interface that is marked with the " + Asynchronous.class
                .getName() + " annotation");

        Protocol.create(Collection.class);
    }

    @Test
    public void shouldFailWhenTryingToGetDispatcherForClass() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot create a protocol for " + Object.class.getName() + ". It must be an interface that is marked with the " + Asynchronous.class
                .getName() + " annotation");

        Protocol.create(Object.class);
    }

    @Test
    public void shouldGetChannelForMethod() throws NoSuchMethodException {
        Protocol<Mouse> protocol = Protocol.create(Mouse.class);
        assertNotNull(protocol.channelFor(Mouse.class.getMethod("eatCheese", String.class), String.class));
    }

    @Test
    public void shouldFailWhenSpecifyingInvalidParameterType() throws NoSuchMethodException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "Specified parameter type java.lang.Object is not what the method requires: public abstract void org.fotap.heysync.Mouse.eatCheese(java.lang.String)");

        Protocol<Mouse> protocol = Protocol.create(Mouse.class);
        assertNotNull(protocol.channelFor(Mouse.class.getMethod("eatCheese", String.class), Object.class));
    }

    @Test
    public void shouldFailWhenTryingToGetChannelForInvalidMethod() throws NoSuchMethodException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(
                "public java.lang.String java.lang.Object.toString() is not a method on org.fotap.heysync.Mouse");

        Protocol<Mouse> protocol = Protocol.create(Mouse.class);
        protocol.channelFor(Object.class.getMethod("toString"), Object.class);
    }

    @Test
    public void shouldHandleExtendedInterfaces() {
        LabMouse subscription = mock(LabMouse.class);

        Protocol<LabMouse> protocol = Protocol.create(LabMouse.class);
        protocol.subscribe(new SynchronousDisposingExecutor(), subscription);
        protocol.publisher().runThroughMaze();
        protocol.publisher().eatCheese("reward");
        
        verify(subscription).runThroughMaze();
        verify(subscription).eatCheese("reward");
    }
}
