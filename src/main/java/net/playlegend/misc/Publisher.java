package net.playlegend.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Publisher<B extends Enum<B>, T> {

    private final List<Subscriber<T, B>> subscribers;

    public Publisher() {
        this.subscribers = Collections.synchronizedList(new ArrayList<>());
    }

    public final void subscribe(Subscriber<T, B> subscriber) {
        subscribers.add(subscriber);
    }

    public final void unsubscribe(Subscriber<T, B> subscriber) {
        subscribers.remove(subscriber);
    }

    protected void notifySubscribers(B type, T data) {
        this.subscribers.forEach(s -> s.update(type, data));
    }

}
