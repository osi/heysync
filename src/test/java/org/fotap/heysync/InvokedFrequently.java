package org.fotap.heysync;

@Asynchronous
public interface InvokedFrequently {

    @LatestOnly
    void calledFasterThanCanBeProcessed(String value);
}
