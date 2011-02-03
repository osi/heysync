package org.fotap.heysync;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
public class FieldMouse implements Mouse {
    final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void eatCheese(String flavor) {
        System.out.println(flavor + " cheese! my favorite! nom nom nom");
        latch.countDown();
    }

    @Override
    public void provokeCats(int howManyCats) {
        provokeCatsWithTaunt(howManyCats, "look at me");
    }

    @Override
    public void provokeCatsWithTaunt(int howManyCats, String message) {
        for (int i = 0; i < howManyCats; i++) {
            System.out.println("hey mr stinky cat... " + message);
        }
    }

    @Override
    public void shoutWords(String... words) {
        for (String word : words) {
            System.out.println("Shouting: '" + word.toUpperCase() + "!'");
        }
    }

    @Override
    public void reciteNumbers(int... numbers) {
        for (int number : numbers) {
            System.out.println("Reciting number " + number);
        }
    }
}
