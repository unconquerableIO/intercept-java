package io.unconquerable.intercept.xgboost.predictor;

import io.unconquerable.intercept.xgboost.inference.Decision;
import io.unconquerable.intercept.xgboost.inference.Inference;
import io.unconquerable.intercept.xgboost.interpreter.Interpreter;
import io.unconquerable.intercept.xgboost.prediction.*;
import io.unconquerable.intercept.xgboost.prediction.decoder.PredictionsDecoder;
import ml.dmlc.xgboost4j.java.DMatrix;

import java.util.Optional;
import java.util.function.Function;

/**
 * Type-state entry point for the XGBoost prediction pipeline.
 *
 * <p>After {@link #translate} registers the feature-extraction function, {@link #predict()}
 * runs inference and returns a {@link Predicted} stage whose type parameters flow forward
 * through {@link Predicted#decode decode} and {@link Decoded#interpret interpret}, preventing
 * out-of-order calls at compile time.  The happy path is:
 *
 * <pre>{@code
 * Predictor.predictor(model, request)
 *     .translate(req -> buildMatrix(req))
 *     .predict()
 *     .decode(new BinaryLogisticObjectiveDecoder())
 *     .interpret(predictions -> Inference.of(MyDecision.fromBatch(predictions)))
 *     .orError(error    -> Inference.of(MyDecision.fallback(error)));
 * }</pre>
 *
 * @param <T> the type of the domain object passed as input to the pipeline
 * @author Rizwan Idrees
 */
public final class Predictor<T> {

    private final XGBoostPredictor model;
    private final T input;
    private Function<T, DMatrix> toFeatures;

    private Predictor(XGBoostPredictor model, T input) {
        this.model = model;
        this.input = input;
    }

    /**
     * Creates the first stage of the pipeline with the given model and input.
     *
     * @param <T>   the type of the domain input object
     * @param model the predictor used to run inference; never {@code null}
     * @param input the domain object that will be converted to a feature matrix; never {@code null}
     * @return a new {@code Predictor} ready for {@link #translate}
     */
    public static <T> Predictor<T> predictor(XGBoostPredictor model, T input) {
        return new Predictor<>(model, input);
    }

    /**
     * Registers the function that converts the domain input to a {@link DMatrix}.
     *
     * @param toFeatures converts {@code T} to a feature matrix; never {@code null}
     * @return {@code this}, ready for {@link #predict()}
     */
    public Predictor<T> translate(Function<T, DMatrix> toFeatures) {
        this.toFeatures = toFeatures;
        return this;
    }

    /**
     * Converts the input to a feature matrix and runs inference.
     *
     * <p>Any XGBoost failure is captured as an {@link Outcome.Failure} inside the returned
     * {@link Predicted}; the pipeline continues and the error is resolved in
     * {@link Interpreted#orError}.
     *
     * @return the next pipeline stage carrying the raw prediction outcome
     */
    public Predicted predict() {
        final Outcome<DefaultPrediction<float[][]>, PredictionError> outcome = Optional
                .ofNullable(toFeatures)
                        .map(tf -> tf.apply(input))
                                .map(model::predict)
                .orElseGet(() -> Outcome.failure(PredictionError.of("No features found")));
        return new Predicted(outcome);
    }


    /**
     * Pipeline stage after {@link Predictor#predict}: holds the raw prediction outcome and
     * advances to {@link Decoded} when {@link #decode} is called.
     */
    public static final class Predicted {

        private final Outcome<DefaultPrediction<float[][]>, PredictionError> outcome;

        Predicted(Outcome<DefaultPrediction<float[][]>, PredictionError> outcome) {
            this.outcome = outcome;
        }

        /**
         * Returns the raw prediction outcome so callers can inspect, log, or branch before
         * committing to a decoder.
         *
         * @return the {@link Outcome} carrying either a {@link DefaultPrediction} or a
         *         {@link PredictionError}; never {@code null}
         */
        public Outcome<DefaultPrediction<float[][]>, PredictionError> outcome() {
            return outcome;
        }

        /**
         * Registers the decoder that converts raw {@code float[][]} scores to
         * typed {@link Predictions}.
         *
         * @param <OT>    the Java type of a single normalized prediction value
         * @param <P>     the concrete {@link Prediction} wrapper type
         * @param decoder objective-specific decoder; never {@code null}
         * @return the next pipeline stage with {@code OT} and {@code P} threaded through
         */
        public <OT, P extends Prediction<OT>> Decoded<OT, P> decode(PredictionsDecoder<OT, P> decoder) {
            return new Decoded<>(outcome, decoder);
        }
    }


    /**
     * Pipeline stage after {@link Predicted#decode}: carries both the raw outcome and the decoder,
     * and advances to {@link Interpreted} when {@link #interpret} is called.
     *
     * @param <OT> the Java type of a single normalized prediction value
     * @param <P>  the concrete {@link Prediction} wrapper type
     */
    public static final class Decoded<OT, P extends Prediction<OT>> {

        private final Outcome<DefaultPrediction<float[][]>, PredictionError> outcome;
        private final PredictionsDecoder<OT, P> decoder;

        Decoded(Outcome<DefaultPrediction<float[][]>, PredictionError> outcome,
                PredictionsDecoder<OT, P> decoder) {
            this.outcome = outcome;
            this.decoder = decoder;
        }

        /**
         * Returns the raw prediction outcome, bypassing decoding entirely.
         *
         * @return the {@link Outcome} carrying either a {@link DefaultPrediction} or a
         *         {@link PredictionError}; never {@code null}
         */
        public Outcome<DefaultPrediction<float[][]>, PredictionError> outcome() {
            return outcome;
        }

        /**
         * Applies the decoder and returns the typed outcome, without requiring a full
         * interpreter chain.  Useful for callers that want to inspect decoded predictions
         * directly or feed them into their own logic.
         *
         * @return an {@link Outcome} carrying either typed {@link Predictions} or a
         *         {@link PredictionError}; never {@code null}
         */
        public Outcome<Predictions<OT, P>, PredictionError> decoded() {
            return outcome.map(r -> decoder.decode(r.value()));
        }

        /**
         * Registers the interpreter that maps decoded {@link Predictions} to an
         * {@link Inference} carrying a {@link Decision}.
         *
         * @param <D>         the concrete {@link Decision} type
         * @param interpreter maps a {@link Predictions} batch to an {@link Inference}; never {@code null}
         * @return the terminal pipeline stage
         */
        public <D extends Decision> Interpreted<OT, P, D> interpret(
                Interpreter<Predictions<OT, P>, D> interpreter) {
            return new Interpreted<>(outcome, decoder, interpreter);
        }
    }


    /**
     * Terminal pipeline stage: executes the full pipeline and produces an {@link Inference}.
     *
     * @param <OT> the Java type of a single normalized prediction value
     * @param <P>  the concrete {@link Prediction} wrapper type
     * @param <D>  the concrete {@link Decision} type
     */
    public static final class Interpreted<OT, P extends Prediction<OT>, D extends Decision> {

        private final Outcome<DefaultPrediction<float[][]>, PredictionError> outcome;
        private final PredictionsDecoder<OT, P> decoder;
        private final Interpreter<Predictions<OT, P>, D> interpreter;

        Interpreted(Outcome<DefaultPrediction<float[][]>, PredictionError> outcome,
                    PredictionsDecoder<OT, P> decoder,
                    Interpreter<Predictions<OT, P>, D> interpreter) {
            this.outcome = outcome;
            this.decoder = decoder;
            this.interpreter = interpreter;
        }

        /**
         * Executes the pipeline and returns the final {@link Inference}.
         *
         * <p>On success, decodes the raw prediction and passes the result to the success
         * interpreter.  On failure, passes the {@link PredictionError} to
         * {@code errorInterpreter}.  Either path returns an {@link Inference} carrying a
         * {@link Decision} — no exception is ever thrown.
         *
         * @param errorInterpreter maps a {@link PredictionError} to an {@link Inference};
         *                         never {@code null}
         * @return the final inference; never {@code null}
         */
        public Inference<D> orError(Interpreter<PredictionError, D> errorInterpreter) {
            return outcome
                    .map(r -> decoder.decode(r.value()))
                    .fold(interpreter::interpret, errorInterpreter::interpret);
        }
    }
}