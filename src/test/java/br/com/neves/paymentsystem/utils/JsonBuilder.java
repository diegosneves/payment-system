package br.com.neves.paymentsystem.utils;

public abstract class JsonBuilder<T> {

    protected T request;

    public JsonBuilder(final T request) {
        this.request = request;
    }

    public abstract String toJson();
}
