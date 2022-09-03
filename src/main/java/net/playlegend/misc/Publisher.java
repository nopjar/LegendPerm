package net.playlegend.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Publisher<B extends Enum<B>, T> {

    private final Map<Enum<B>, List<Subscriber<T, B>>> subscribers;

    @SafeVarargs
    public Publisher(Enum<B>... enumerations) {
        this.subscribers = new ConcurrentHashMap<>();
        for (Enum<B> e : enumerations) {
            this.subscribers.put(e, new ArrayList<>());
        }
    }

    @SafeVarargs
    public final void subscribe(Subscriber<T, B> subscriber, Enum<B>... types) {
        for (Enum<B> type : types) {
            List<Subscriber<T, B>> list = subscribers.get(type);
            if (!list.contains(subscriber))
                list.add(subscriber);
        }
    }

    @SafeVarargs
    public final void unsubscribe(Subscriber<T, B> subscriber, Enum<B>... types) {
        for (Enum<B> type : types) {
            subscribers.get(type).remove(subscriber);
        }
    }

    protected void notifySubscribers(B type, T data) {
        this.subscribers.get(type).forEach(s -> s.update(type, data));
    }

}
