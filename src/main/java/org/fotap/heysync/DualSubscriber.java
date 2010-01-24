package org.fotap.heysync;

import org.jetlang.channels.Subscribable;
import org.jetlang.channels.Subscriber;
import org.jetlang.core.Callback;
import org.jetlang.core.Disposable;
import org.jetlang.core.DisposingExecutor;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class DualSubscriber<T> implements Subscriber<T> {
    private final Subscriber<T> one;
    private final Subscriber<T> two;

    DualSubscriber(Subscriber<T> one, Subscriber<T> two) {
        this.one = one;
        this.two = two;
    }

    @Override
    public Disposable subscribe(DisposingExecutor executor, Callback<T> receive) {
        return new DualDisposable(one.subscribe(executor, receive), two.subscribe(executor, receive));
    }

    @Override
    public Disposable subscribe(Subscribable<T> sub) {
        return new DualDisposable(one.subscribe(sub), two.subscribe(sub));
    }

    private static class DualDisposable implements Disposable {
        private final Disposable one;
        private final Disposable two;

        private DualDisposable(Disposable one, Disposable two) {
            this.one = one;
            this.two = two;
        }

        @Override
        public void dispose() {
            one.dispose();
            two.dispose();
        }
    }
}
