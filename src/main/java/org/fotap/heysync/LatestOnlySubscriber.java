package org.fotap.heysync;

import org.jetlang.channels.BaseSubscription;
import org.jetlang.core.Callback;
import org.jetlang.core.DisposingExecutor;

class LatestOnlySubscriber<T> extends BaseSubscription<T> {
    private final DisposingExecutor executor;
    private final Callback<T> callback;

    private boolean flushPending;
    private T message;

    private final Runnable flusher;

    public LatestOnlySubscriber(DisposingExecutor executor, Callback<T> callback) {
        super(executor, null);
        this.executor = executor;
        this.callback = callback;
        this.flusher = new Runnable() {
            @Override
            public void run() {
                flush();
            }

            @Override
            public String toString() {
                return "Flushing " + LatestOnlySubscriber.this + " via " + LatestOnlySubscriber.this.callback.toString();
            }
        };
    }

    @Override
    protected void onMessageOnProducerThread(T msg) {
        boolean executeFlusher = savePending(msg);

        if (executeFlusher) {
            executor.execute(flusher);
        }
    }

    private synchronized boolean savePending(T msg) {
        this.message = msg;

        if (flushPending) {
            return false;
        }

        flushPending = true;
        return true;
    }

    private void flush() {
        callback.onMessage(clearPending());
    }

    private synchronized T clearPending() {
        T message;
        message = this.message;
        this.message = null;
        flushPending = false;
        return message;
    }

}