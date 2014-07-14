/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Map;

/**
 * Stateful megawidget created by a megawidget specifier that has a single
 * choice as its state.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Replaced erroneous references
 *                                           (variable names, comments, etc.) to
 *                                           "widget" with "megawidget" to avoid
 *                                           confusion.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidgetSpecifier
 */
public abstract class SingleBoundedChoiceMegawidget extends
        BoundedChoicesMegawidget<String> {

    // Protected Variables

    /**
     * Strings making up the current state.
     */
    protected String state = null;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected SingleBoundedChoiceMegawidget(
            BoundedChoicesMegawidgetSpecifier<String> specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        state = (String) specifier.getStartingState(specifier.getIdentifier());
    }

    // Protected Methods

    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        /*
         * Convert the provided state to a valid value, and record it.
         */
        try {
            this.state = getStateValidator().convertToStateValue(state);
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }

        /*
         * Synchronize user-facing widgets to the new state.
         */
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        try {
            return (state == null ? "" : (String) state);
        } catch (Exception e) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be single choice");
        }
    }
}