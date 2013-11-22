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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Description: List builder megawidget specifier, used to create megawidgets
 * that allow the user to build up an orderable list from an open set of
 * choices, meaning that the user may add any arbitrary choice to the list, as
 * long as it is unique.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013   2336     Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see UnboundedListBuilderMegawidget
 */
public class UnboundedListBuilderSpecifier extends
        UnboundedChoicesMegawidgetSpecifier implements IControlSpecifier,
        IMultiLineSpecifier {

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public UnboundedListBuilderSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        // Ensure that the visible lines count, if present,
        // is acceptable, and if not present is assigned a
        // default value.
        numVisibleLines = getSpecifierIntegerValueFromObject(
                parameters.get(MEGAWIDGET_VISIBLE_LINES),
                MEGAWIDGET_VISIBLE_LINES, 6);
        if (numVisibleLines < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_LINES, numVisibleLines,
                    "must be positive integer");
        }
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfColumn() {
        return optionsManager.isFullWidthOfColumn();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final int getNumVisibleLines() {
        return numVisibleLines;
    }

    @Override
    protected String getChoicesDataStructureDescription() {
        return "list";
    }

    @Override
    protected IllegalChoicesProblem evaluateChoicesMapLegality(
            String parameterName, Map<?, ?> map, int index) {
        return NO_ILLEGAL_CHOICES_PROBLEM;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object> createChoicesCopy(List<?> list) {

        // Create a new list, and copy each element into it from
        // the old list. If an element is a map, then make a
        // copy of the map instead of using the original.
        List<Object> listCopy = Lists.newArrayList();
        for (Object item : list) {
            if (item instanceof Map) {
                listCopy.add(Maps.newHashMap((Map<String, Object>) item));
            } else {
                listCopy.add(item);
            }
        }
        return listCopy;
    }
}
