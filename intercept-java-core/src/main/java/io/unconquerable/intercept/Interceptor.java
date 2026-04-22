package io.unconquerable.intercept;

import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * Interceptor
 *
 * @author Rizwan Idrees
 */
public class Interceptor {

    private final Map<Class<?>, List<Detector<?>>> registry = new HashMap<>();

    public static Interceptor interceptor() {
        return new Interceptor();
    }

    public <T> Interceptor detector(@Nonnull Class<T> clazz, @Nonnull Detector<T> detector) {
        Optional.of(clazz)
                .filter(registry::containsKey)
                .ifPresentOrElse(c -> registry.get(c).add(detector),
                        () -> registry.put(clazz, new ArrayList<>(List.of(detector))));
        return this;
    }

}
