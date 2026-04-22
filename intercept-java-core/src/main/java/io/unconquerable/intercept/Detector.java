package io.unconquerable.intercept;

/**
 * Detector
 *
 * @param <T>
 * @author Rizwan Idrees
 */
public interface Detector<T> {

    String name();

    Detected detect(T target);

}
