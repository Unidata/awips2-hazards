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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Time scale megawidget specifier, providing specification of a megawidget that
 * allows the selection of one or more times. Each time is associated with a
 * separate state identifier, of which one or more may be specified via the
 * colon-separated megawidget specifier identifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to implement IControlSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Nov 04, 2013   2336     Chris.Golden      Added implementation of new superclass-
 *                                           specified abstract method, and changed
 *                                           to offer option of not notifying
 *                                           listeners of state changes caused by
 *                                           ongoing thumb drags.
 * Dec 13, 2013   2545     Chris.Golden      Made subclass of new TimeMegawidgetSpecifier.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeScaleMegawidget
 */
public class TimeScaleSpecifier extends TimeMegawidgetSpecifier implements
        IControlSpecifier, IRapidlyChangingStatefulSpecifier {

    // Public Static Constants

    /**
     * Minimum visible time megawidget creation time parameter name; if
     * specified in the map passed to <code>createMegawidget()</code>, its value
     * must be an object of type <code>Long</code> indicating the minimum time
     * in milliseconds that is to be visible at megawidget creation time.
     */
    public static final String MINIMUM_VISIBLE_TIME = "minimumVisibleTime";

    /**
     * Maximum visible time megawidget creation time parameter name; if
     * specified in the map passed to <code>createMegawidget()</code>, its value
     * must be an object of type <code>Long</code> indicating the maximum time
     * in milliseconds that is to be visible at megawidget creation time.
     */
    public static final String MAXIMUM_VISIBLE_TIME = "maximumVisibleTime";

    // Private Variables

    /**
     * Map pairing state identifier keys with their indices in the list provided
     * by the <code>getStateIdentifiers()</code> method.
     */
    private final Map<String, Integer> indicesForIds;

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
    public TimeScaleSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Compile a mapping of state identifiers to their
        // indices (giving their ordering).
        Map<String, Integer> indicesForIds = Maps.newHashMap();
        List<String> stateIdentifiers = getStateIdentifiers();
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            indicesForIds.put(stateIdentifiers.get(j), j);
        }
        this.indicesForIds = ImmutableMap.copyOf(indicesForIds);
    }

    // Public Methods

    /**
     * Get the mapping of state identifier keys to their indices in the list
     * provided by the <code>getStateIdentifiers()</code> method.
     * 
     * @return Mapping of state identifier keys to their indices.
     */
    public final Map<String, Integer> getIndicesForStateIdentifiers() {
        return indicesForIds;
    }

    // Protected Methods

    @Override
    protected int getMaximumStateIdentifierCount() {

        // Return an absurdly (for GUI purposes) large
        // number.
        return Integer.MAX_VALUE;
    }
}
