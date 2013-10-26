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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ChoicesMegawidgetSpecifier
 */
public abstract class SingleChoiceMegawidget extends ChoicesMegawidget {

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
    protected SingleChoiceMegawidget(ChoicesMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
    }

    // Protected Methods

    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Set the state to that which has been supplied.
        try {
            this.state = (String) state;
        } catch (Exception e) {
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be single choice");
        }
        if ((this.state != null)
                && (getChoiceIdentifiers().contains(this.state) == false)) {
            this.state = null;
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be one of ["
                    + getChoicesAsString() + "]");
        }

        // Synchronize the widgets to the new state.
        synchronizeWidgetsToState();
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