package io.unconquerable.intercept;

import jakarta.annotation.Nonnull;

import java.math.BigDecimal;

/**
 * DetectedScore
 *
 * @param detectorName
 * @param score
 * @author Rizwan Idrees
 */
public record DetectedScore(@Nonnull String detectorName, @Nonnull BigDecimal score) implements Detected {
}
