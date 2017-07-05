package com.android.launcher3.util;

public abstract class Provider {

    static final class ProviderValue extends Provider {
        final Object val$value;

        ProviderValue(Object obj) {
            val$value = obj;
        }

        @Override
        public Object get() {
            return val$value;
        }
    }

    public abstract Object get();

    public static Provider of(Object obj) {
        return new ProviderValue(obj);
    }
}