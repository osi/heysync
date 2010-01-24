package org.fotap.heysync;

import org.jetlang.channels.Channel;
import org.jetlang.channels.MemoryChannel;
import org.jetlang.core.Callback;
import org.jetlang.fibers.ThreadFiber;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class JetlangExample {

    public static void main(String... args) throws InterruptedException {
        // Create our fiber
        ThreadFiber fiber = new ThreadFiber();
        fiber.start();

        // Channel for messages to flow on
        Channel<String> messages = new MemoryChannel<String>();

        // Create our FieldMouse and subscribe him to the channel
        final FieldMouse fieldMouse = new FieldMouse();
        messages.subscribe(fiber, new Callback<String>() {
            @Override
            public void onMessage(String message) {
                fieldMouse.eatCheese(message);
            }
        });

        // Tell our mouse to eat cheese
        messages.publish("cheddar");

        fieldMouse.latch.await();
    }
}
