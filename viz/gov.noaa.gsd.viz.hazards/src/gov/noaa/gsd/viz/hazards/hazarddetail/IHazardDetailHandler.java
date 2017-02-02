/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

/**
 * Description: Interface describing the methods that must be implemented by
 * classes that are to act as hazard detail handlers, dealing with notifications
 * from the hazard detail presenter about closing dialogs, etc.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 02, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardDetailHandler {

    /**
     * Examine all hazards looking for potential conflicts, and if there are
     * conflicts, ask the user whether or not to continue in light of said
     * conflicts.
     * 
     * @return <code>true</code> if the user wishes to continue,
     *         <code>false</code> otherwise.
     */
    public boolean shouldContinueIfThereAreHazardConflicts();

    /**
     * Close the product editor, if it is open.
     */
    public void closeProductEditor();
}
