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
 * Encapsulation of the result of a merge attempt of two objects of the type
 * given by the generic parameter <code>T</code>.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 20, 2017   38072    Chris.Golden Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 * @see IMergeable
 */
public final class MergeResult<T> {

    // Private Variables

    /**
     * Flag indicating whether or not the merge was successful.
     */
    private final boolean success;

    /**
     * Instance of <code>T</code> to be used to replace the subject of the
     * merge. If this is <code>null</code>, no merge was possible. If this is
     * {@link IMergeable#NULLIFIED_OBJECT}, the subject is no longer needed.
     */
    private final T subjectReplacement;

    /**
     * <code>T</code> to be used to replace the object of the merge. This should
     * never be <code>null</code> if the merge was successful. If this is
     * {@link IMergeable#NULLIFIED_OBJECT}, the object is no longer needed.
     */
    private final T objectReplacement;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param success
     *            Flag indicating whether or not the merge was a success.
     * @param subjectReplacement
     *            Subject replacement, if any. Ignored if <code>success</code>
     *            is <code>false</code>.
     * @param objectReplacement
     *            Object replacement, if any. Ignored if <code>success</code> is
     *            <code>false</code>.
     */
    public MergeResult(boolean success, T subjectReplacement,
            T objectReplacement) {
        this.success = success;
        this.subjectReplacement = (success ? subjectReplacement : null);
        this.objectReplacement = (success ? objectReplacement : null);
    }

    // Public Methods

    /**
     * Determine whether or not the merge was successful.
     * 
     * @return <code>true</code> if the merge was successful and a subject
     *         and/or object replacement was generated, <code>false</code>
     *         otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the instance of <code>T</code> to be used to replace the subject of
     * the merge. If this is <code>null</code>, no merge was possible. If this
     * is <code>null</code>, the subject is no longer needed.
     * 
     * @return Instance to be used to replace the subject of the merge, if any.
     */
    public T getSubjectReplacement() {
        return subjectReplacement;
    }

    /**
     * Get the instance of <code>T</code> to be used to replace the object of
     * the merge. This should never be <code>null</code> if the merge was
     * successful. If this is <code>null</code>, the object is no longer needed.
     * 
     * @return Instance to be used to replace the subject of the merge, if any.
     */
    public T getObjectReplacement() {
        return objectReplacement;
    }
}