package io.unconquerable.intercept;

import jakarta.annotation.Nonnull;

/**
 * DetectedStatus
 *
 * @param detectorName
 * @param status
 * @author Rizwan Idrees
 */
public record DetectedStatus(@Nonnull String detectorName, @Nonnull Status status) implements Detected {

    public enum Status {DETECTED, NOT_DETECTED, SKIPPED}
}
