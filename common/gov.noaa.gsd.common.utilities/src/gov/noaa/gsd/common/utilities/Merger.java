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

import java.util.List;

/**
 * Merger, used to merge {@link IMergeable} objects
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 20, 2017   38072    Chris.Golden Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class Merger {

    // Public Static Methods

    /**
     * Merge the specified object with the specified subjects if possible,
     * appending any remaining object (after all subjects have had a merge
     * attempt made with them) to the end of the subjects list.
     * 
     * @param subjects
     *            Subjects of the merge.
     * @param object
     *            Object of the merge.
     */
    public static <T extends IMergeable<T>> void merge(List<T> subjects,
            T object) {

        /*
         * Iterate through the accumulated mergeables, attempting with each one
         * in turn to merge this new mergeable with it. If a merge is
         * successful, further process. If the subject is to be replaced, do so,
         * or remove it if the replacement is null. If the object is to be
         * replaced, remember it so that it can fed to the next merge attempt.
         * 
         * Merging is attempted with each existing subject in turn because this
         * allows each of the latter to modify themselves accordingly.
         * 
         * At the same time, each merge is given the result of the previous
         * merge's modifications to the new object, since some merges will mean
         * that the new object gets modified or nullified.
         */
        T modified = object;
        for (int j = 0; j < subjects.size(); j++) {
            MergeResult<T> mergeResult = subjects.get(j).merge(object,
                    modified);
            if (mergeResult.isSuccess()) {
                if (mergeResult.getSubjectReplacement() == null) {
                    subjects.remove(j--);
                } else {
                    subjects.set(j, mergeResult.getSubjectReplacement());
                }
                modified = mergeResult.getObjectReplacement();
            }
        }

        /*
         * If the object has survived the merge in some form, it has not been
         * entirely merged, and thus must be added to the end of the list of
         * subjects.
         */
        if (modified != null) {
            subjects.add(modified);
        }
    }
}
