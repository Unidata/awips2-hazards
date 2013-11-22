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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * Stateful megawidget created by a megawidget specifier that has a set of zero
 * or more choices as its state, selected from a closed set of choices.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 23, 2013   2168     Chris.Golden      Minor cleanup.
 * Oct 31, 2013   2336     Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidgetSpecifier
 */
public abstract class MultipleBoundedChoicesMegawidget extends
        BoundedChoicesMegawidget {

    // Protected Variables

    /**
     * List of strings making up the current state; the strings are the choices
     * currently selected.
     */
    protected final List<String> state = Lists.newArrayList();

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
    protected MultipleBoundedChoicesMegawidget(
            BoundedChoicesMegawidgetSpecifier specifier,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
    }

    // Protected Methods

    @Override
    protected final Object doGetState(String identifier) {
        return Lists.newArrayList(state);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {

        // Set the state to that supplied.
        this.state.clear();
        if (state instanceof String) {
            this.state.add((String) state);
        } else if (state != null) {
            try {
                this.state.addAll((Collection<? extends String>) state);
            } catch (Exception e) {
                throw new MegawidgetStateException(identifier, getSpecifier()
                        .getType(), state, "must be list of choices");
            }
        }
        if (getChoiceIdentifiers().containsAll(this.state) == false) {
            this.state.clear();
            throw new MegawidgetStateException(identifier, getSpecifier()
                    .getType(), state, "must be subset of ["
                    + getChoicesAsString() + "]");
        }

        // Synchronize the widgets to the new state.
        synchronizeWidgetsToState();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        if (state instanceof String) {
            return (String) state;
        } else {
            try {
                StringBuilder description = new StringBuilder();
                for (String element : (Collection<? extends String>) state) {
                    if (description.length() > 0) {
                        description.append("; ");
                    }
                    description.append(element);
                }
                return description.toString();
            } catch (Exception e) {
                throw new MegawidgetStateException(identifier, getSpecifier()
                        .getType(), state, "must be list of choices");
            }
        }
    }
}