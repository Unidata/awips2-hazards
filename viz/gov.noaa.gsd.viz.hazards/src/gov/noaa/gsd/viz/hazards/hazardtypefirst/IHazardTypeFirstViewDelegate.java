/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazardtypefirst;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;

import java.util.List;

/**
 * Description: Interface that a delegate for the hazard type first view must
 * implement.
 * <p>
 * A hazard type first view must provide a way of displaying hazard categories
 * and types types so that one of the latter may be chosen for creation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 21, 2015    3626    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardTypeFirstViewDelegate<C, E extends Enum<E>> extends
        IView<C, E>, IHazardTypeFirstView {

    /**
     * Initialize the view delegate.
     * 
     * @param categories
     *            List of categories that are to be used.
     */
    public void initialize(List<String> categories);

    /**
     * Get the show dialog invoker. The latter's identifier is ignored.
     * 
     * @return Show dialog command invoker.
     */
    public ICommandInvoker<Object> getShowDialogInvoker();

    /**
     * Show the dialog to allow the user to choose a type for which to create a
     * hazard event.
     * 
     * @param selectedCategory
     *            Category that is to be initially selected.
     * @param types
     *            List of types that is to be initially provided as underlying
     *            choices.
     * @param typeDescriptions
     *            List of type descriptions that is to be initially provided as
     *            visible representations of the underlying type choices; each
     *            is paired with the type at the corresponding index in <code>
     *            types</code>.
     * @param selectedType
     *            Type that is to be initially selected.
     */
    public void show(String selectedCategory, List<String> types,
            List<String> typeDescriptions, String selectedType);

    /**
     * Hide the dialog.
     */
    public void hide();
}
