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
        Channel<String[]> shoutWordsMessages = new MemoryChannel<String[]>();
        Channel<int[]> reciteNumbersMessages = new MemoryChannel<int[]>();

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
        shoutWordsMessages.subscribe(fiber, new Callback<String[]>() {
            @Override
            public void onMessage(String[] words) {
                fieldMouse.shoutWords(words);
            }
        });
        reciteNumbersMessages.subscribe(fiber, new Callback<int[]>() {
            @Override
            public void onMessage(int[] numbers) {
                fieldMouse.reciteNumbers(numbers);
            }
        });

        // Tell our mouse to eat cheese and provoke cats
        cheeseMessages.publish("cheddar");
        provokeCatsMessages.publish(4);
        shoutWordsMessages.publish(new String[]{"cats", "really", "stink"});
        reciteNumbersMessages.publish(new int[]{2, 4, 6, 8});

        fieldMouse.latch.await();
    }
}
