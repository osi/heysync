package org.fotap.heysync;

import org.jetlang.channels.Publisher;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class DualPublisher<T> implements Publisher<T> {
    private final Publisher<T> one;
    private final Publisher<T> two;

    DualPublisher(Publisher<T> one, Publisher<T> two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public void publish(T msg) {
        one.publish(msg);
        two.publish(msg);
    }
}
