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
        for (int i = 0; i < howManyCats; i++) {
            System.out.println("hey mr stinky cat... look at me");
        }
    }
}
