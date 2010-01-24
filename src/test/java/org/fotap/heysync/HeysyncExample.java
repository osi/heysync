package org.fotap.heysync;

import org.jetlang.fibers.ThreadFiber;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class HeysyncExample {

    public static void main(String... args) throws InterruptedException {
        // Create our fiber
        ThreadFiber fiber = new ThreadFiber();
        fiber.start();

        // Create heysync hub
        Hub hub = new Hub();

        // Create our FieldMouse and add him as a receiver to our hub
        FieldMouse fieldMouse = new FieldMouse();
        hub.addReceiver(fieldMouse, fiber);

        // Get the dispatcher for all registered Mice
        Mouse dispatcher = hub.dispatcherFor(Mouse.class);

        // Tell our mouse to eat cheese
        dispatcher.eatCheese("cheddar");

        fieldMouse.latch.await();
    }
}