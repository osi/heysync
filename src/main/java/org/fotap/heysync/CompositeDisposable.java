package org.fotap.heysync;

import org.jetlang.core.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
 */
class CompositeDisposable implements Disposable {
    private final List<Disposable> disposables = new ArrayList<Disposable>();

    void add(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void dispose() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
    }
}
