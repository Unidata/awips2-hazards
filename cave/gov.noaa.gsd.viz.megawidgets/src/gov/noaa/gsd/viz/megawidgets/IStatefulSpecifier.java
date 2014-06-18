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

import java.util.List;
import java.util.Map;

/**
 * Interface describing the methods to be implemented by a megawidget specifier
 * that creates a {@link IStateful} megawidget. Any subclasses of
 * {@link MegawidgetSpecifier} must implement this interface if they are to
 * create such megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 22, 2013   2168     Chris.Golden      Changed to extend INotifierSpecifier.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Removed obsolete properties.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IStateful
 * @see Megawidget
 * @see MegawidgetSpecifier
 */
public interface IStatefulSpecifier extends INotifierSpecifier {

    // Public Static Constants

    /**
     * Megawidget state labels parameter name; a megawidget may include a value
     * associated with this name. The value may be either a label string (if
     * only one state identifier is associated with the specifier), or else a
     * dictionary mapping state identifiers to label strings (with one per state
     * identifier). State identifiers are as specified in the value associated
     * with the {@link #MEGAWIDGET_IDENTIFIER} parameter. There must be the same
     * number of labels as there are states associated with this specifier, one
     * per identifier. If not provided, no labels are used for the individual
     * states.
     */
    public static final String MEGAWIDGET_STATE_LABELS = "valueLabels";

    /**
     * Megawidget state values parameter name; a megawidget may include a value
     * associated with this name. The value may be either a single state value
     * (if only one state identifier is associated with the specifier), or else
     * a dictionary mapping state identifiers to state values (with one per
     * state identifier). State identifiers are as specified in the value
     * associated with the {@link #MEGAWIDGET_IDENTIFIER} parameter. Each state
     * value must be of the type appropriate to a particular stateful specifier;
     * for example, a specifier used to construct a text entry megawidget could
     * require any state value to be a string of text, whereas a specifier for
     * an integer spinner megawidget might need the state value to be an
     * integer.
     */
    public static final String MEGAWIDGET_STATE_VALUES = "values";

    // Public Methods

    /**
     * Get the state identifiers valid for this specifier.
     * 
     * @return List of valid state identifiers.
     */
    public List<String> getStateIdentifiers();

    /**
     * Get the state label for the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the label is desired.
     * @return State label.
     */
    public String getStateLabel(String identifier);

    /**
     * Get the starting value of the specified state.
     * 
     * @param identifier
     *            Identifier of the state for which the starting value is
     *            desired.
     * @return Starting state value.
     */
    public Object getStartingState(String identifier);

    /**
     * Check the specified map for valid starting states for any state
     * identifiers for which it contains values; for any that values that are
     * not valid starting states, replace such values with default values.
     * 
     * @param map
     *            Map in which to find starting states to be checked for
     *            validity, and in which to place replacements for those that
     *            are invalid or missing.
     */
    public void validateAndCorrectStates(Map<String, Object> map);
}