package org.fotap.heysync;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
@Asynchronous
public interface Mouse {
    void eatCheese(String flavor);
    void provokeCats(int howManyCats);
    void provokeCatsWithTaunt(int howManyCats, String word);
    void shoutWords(String... words);
    void reciteNumbers(int... numbers);
}
