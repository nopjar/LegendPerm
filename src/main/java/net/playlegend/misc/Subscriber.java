package net.playlegend.misc;

public interface Subscriber<T, B extends Enum<B>> {

    void update(B type, T data);

}
