/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities;

/**
 * Interface describing the methods that must be implemented by any object that
 * may be merged with other objects of the same type.
 * <p>
 * Note that in the context of this class, the "subject" of a merge refers to
 * the object into which a merge is being attempted, and the "object" of a merge
 * refers to the object that is being merged into the subject.
 * </p>
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 20, 2017   38072    Chris.Golden Initial creation.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * </pre>
 *
 * @author Chris.Golden
 */
public interface IMergeable<T extends IMergeable<T>> {

    /**
     * Helper class, needed because prior to Java 8 interfaces cannot have
     * static methods.
     * 
     * TODO: When moving to Java 8, take all of these methods and make them
     * static methods of the enclosing interface. Then remove this helper class,
     * as it will now be empty.
     * 
     * @deprecated Unneeded once moving to Java 8.
     */
    @Deprecated
    public class Helper {

        /**
         * Get an instance indicating merge failure.
         * 
         * @return Merge failure result.
         */
        public static <U extends IMergeable<?>> MergeResult<U> getFailureResult() {
            return new MergeResult<>(false, null, null);
        }

        /**
         * Get an instance indicating that the merge succeeded, and the subject
         * and object canceled one another out.
         * 
         * @return Merge success result.
         */
        public static <U extends IMergeable<?>> MergeResult<U> getSuccessMutualCancellationResult() {
            return new MergeResult<>(true, null, null);
        }

        /**
         * Get an instance indicating that the merge succeeded, and the subject
         * has been nullified, but a modified object remains.
         * 
         * @param object
         *            Object to be used in place of the original one from before
         *            the merge.
         * @return Merge success result.
         */
        public static <U extends IMergeable<?>> MergeResult<U> getSuccessSubjectCancellationResult(
                U object) {
            return new MergeResult<>(true, null, object);
        }

        /**
         * Get an instance indicating that the merge succeeded, and the object
         * has been nullified, but a modified subject remains.
         * 
         * @param subject
         *            Subject to be used in place of the original one from
         *            before the merge.
         * @return Merge success result.
         */
        public static <U extends IMergeable<?>> MergeResult<U> getSuccessObjectCancellationResult(
                U subject) {
            return new MergeResult<>(true, subject, null);
        }

        /**
         * Get an instance indicating that the merge succeeded, and replacements
         * have resulted for both the subject and the object.
         * 
         * @param subject
         *            Subject to be used in place of the original one from
         *            before the merge.
         * @param object
         *            Object to be used in place of the original one from before
         *            the merge.
         * @return Merge success result.
         */
        public static <U extends IMergeable<?>> MergeResult<U> getSuccessBothReplacedResult(
                U subject, U object) {
            return new MergeResult<>(true, subject, object);
        }
    }

    // Public Static Methods

    // Public Methods

    /**
     * Merge the specified object with this one (the subject) if possible,
     * returning the result.
     * <p>
     * This method is invoked upon each instance in turn that is queued up in a
     * list of instances. Its first parameter is the new instance as provided to
     * the caller, whereas the second parameter is that same instance modified
     * by any earlier invocations of this method (upon other instances that had
     * been enqueued before this one). This allows, for example, a subject
     * instance to have this method invoked with the argument of an object
     * instance that represents the same thing as the former, and thus have the
     * object instance nullified. Implementations may also partially merge the
     * object into the subject, and return modified versions of themselves as
     * new subject instances as well as modified versions of the object
     * instances.
     * </p>
     * <p>
     * <strong>Note</strong>: The specified instance must be the one that
     * occurred after this one, and not vice versa, since merging is not
     * symmetric (that is, A merging with B is not the same as B merging with A;
     * chronological order matters).
     * </p>
     * 
     * @param original
     *            Object to be merged with this one, if possible. This is the
     *            original version of the object, untouched by any modifications
     *            resulting from other, earlier invocations of this method with
     *            it as an argument
     * @param modified
     *            Version of <code>original</code> as modified by any earlier
     *            invocations of this method with the former as an argument.
     * @return Result of the merge.
     */
    public MergeResult<? extends T> merge(T original, T modified);
}
