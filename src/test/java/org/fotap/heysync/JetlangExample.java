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
        Channel<String> cheeseMessages = new MemoryChannel<String>();
        Channel<Integer> provokeCatsMessages = new MemoryChannel<Integer>();

        // Create our FieldMouse and subscribe him to the channel
        final FieldMouse fieldMouse = new FieldMouse();
        cheeseMessages.subscribe(fiber, new Callback<String>() {
            @Override
            public void onMessage(String message) {
                fieldMouse.eatCheese(message);
            }
        });
        provokeCatsMessages.subscribe(fiber, new Callback<Integer>() {
            @Override
            public void onMessage(Integer howMany) {
                fieldMouse.provokeCats(howMany);
            }
        });

        // Tell our mouse to eat cheese and provoke cats
        cheeseMessages.publish("cheddar");
        provokeCatsMessages.publish(4);

        fieldMouse.latch.await();
    }
}
