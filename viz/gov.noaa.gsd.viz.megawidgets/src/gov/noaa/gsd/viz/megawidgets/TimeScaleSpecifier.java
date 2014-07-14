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

import gov.noaa.gsd.viz.megawidgets.validators.BoundedMultiLongValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.ImmutableMap;

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
 * Dec 13, 2013   2545     Chris.Golden      Made subclass of TimeMegawidgetSpecifier.
 * Jan 31, 2014   2710     Chris.Golden      Added minimum interval parameter, to allow
 *                                           the minimum interval between adjacent state
 *                                           values to be configured.
 * Jan 31, 2014   2161     Chris.Golden      Added option to change editability of
 *                                           each state value individually. Also
 *                                           added ability to include detail mega-
 *                                           widgets for each state value. Also added
 *                                           custom strings to be displayed in place
 *                                           of date-time values for specified values.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 04, 2014   2155     Chris.Golden      Fixed bug that caused specifier to treat
 *                                           time descriptors as mandatory.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeScaleMegawidget
 */
public class TimeScaleSpecifier extends TimeMegawidgetSpecifier implements
        IParentSpecifier<IControlSpecifier>, IControlSpecifier,
        IRapidlyChangingStatefulSpecifier {

    // Public Static Constants

    /**
     * Minimum interval parameter name; a time scale megawidget may include a
     * positive long value associated with this name, providing the minimum
     * interval in milliseconds between adjacent thumbs. If not specified, the
     * megawidget allows adjacent values to be a minimum of one minute apart.
     */
    public static final String MEGAWIDGET_MINIMUM_TIME_INTERVAL = "minimumTimeInterval";

    /**
     * Time editability parameter name; a time scale megawidget may include a
     * value associated with this name. The value may be either a boolean (if
     * only one state identifier is associated with the specifier), or else a
     * {link Map} pairing state identifiers to booleans (with one per state
     * identifier). There must be the same number of booleans as there are
     * states associated with this specifier, one per identifier. If not
     * provided, it is assumed that all states are editable. Note that if
     * {@link #MEGAWIDGET_EDITABLE} is provided with a value of false, then the
     * editability of individual states is ignored, and the entire megawidget is
     * read-only.
     */
    public static final String MEGAWIDGET_STATE_EDITABLES = "valueEditables";

    /**
     * Time descriptions parameter name; a time scale megawidget may include a
     * value associated with this name. The value must be a {@link Map} of time
     * values as longs to strings used to describe those values. If provided,
     * then whenever one of the states is set (whether via user interaction or
     * programmatically) to be equal to any of the keys in this map, the
     * corresponding description for that value is used in place of the usual
     * date value in the date-time entry fields.
     */
    public static final String MEGAWIDGET_TIME_DESCRIPTORS = "timeDescriptors";

    /**
     * Detail fields parameter name; a time scale megawidget may include a value
     * associated with this name. The value must be a {@link Map} of pairs, with
     * each pair being a state identifier as a key to a {@link List} of zero or
     * more detail megawidget specifiers as the value. Each of the detail
     * megawidget specifiers must be in the form of a {@link Map} of key-value
     * pairings from which a megawidget specifier will be constructed. Any state
     * identifier that does not have an entry in the map associated with this
     * name is assumed to have no detail fields. The default is an empty map.
     */
    public static final String MEGAWIDGET_DETAIL_FIELDS = "detailFields";

    /**
     * Minimum visible time megawidget creation time parameter name; if
     * specified in the map passed to
     * {@link #createMegawidget(Widget, Class, Map)}, its value must be an
     * object of type {@link Long} indicating the minimum time in milliseconds
     * that is to be visible at megawidget creation time.
     */
    public static final String MINIMUM_VISIBLE_TIME = "minimumVisibleTime";

    /**
     * Maximum visible time megawidget creation time parameter name; if
     * specified in the map passed to
     * {@link #createMegawidget(Widget, Class, Map)}, its value must be an
     * object of type {@link Long} indicating the maximum time in milliseconds
     * that is to be visible at megawidget creation time.
     */
    public static final String MAXIMUM_VISIBLE_TIME = "maximumVisibleTime";

    // Private Variables

    /**
     * Child megawidget specifiers manager.
     */
    private final ChildSpecifiersManager<IControlSpecifier> childManager;

    /**
     * Map pairing state identifier keys with lists of detail megawidgets as
     * values.
     */
    private final Map<String, List<IControlSpecifier>> fieldListsForStates = new HashMap<>();

    /**
     * Map pairing state identifier keys with editability flags as values.
     */
    private final Map<String, Boolean> editabilityForStateIdentifiers;

    /**
     * Map pairing long values with descriptive strings to be used in place of
     * the usual date-time strings when a state takes on one of the values.
     */
    private final Map<Long, String> descriptiveTextForValues;

    /**
     * Map pairing state identifier keys with their indices in the list provided
     * by the {@link #getStateIdentifiers()} method.
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
    @SuppressWarnings("unchecked")
    public TimeScaleSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new BoundedMultiLongValidator(parameters,
                MEGAWIDGET_MINIMUM_TIME_INTERVAL, 0L, Long.MAX_VALUE / 2L));

        /*
         * Ensure that the editability flags, if present, are acceptable.
         */
        Set<Class<?>> classes = new HashSet<>();
        classes.add(Boolean.class);
        editabilityForStateIdentifiers = getStateMappedParametersFromObject(
                parameters, MEGAWIDGET_STATE_EDITABLES, "boolean", classes,
                new Boolean(true), null, null, true);

        /*
         * Ensure that the descriptive strings for dates map, if present, is
         * acceptable. Note that a conversion is done from strings to longs for
         * the map keys, since maps are sometimes allowed only strings as keys
         * (e.g. within JSON).
         */
        try {
            Map<?, String> map = (Map<?, String>) parameters
                    .get(TimeScaleSpecifier.MEGAWIDGET_TIME_DESCRIPTORS);
            descriptiveTextForValues = new HashMap<>();
            if (map != null) {
                for (Object key : map.keySet()) {
                    if (key instanceof Number) {
                        descriptiveTextForValues.put(
                                ((Number) key).longValue(), map.get(key));
                    } else {
                        descriptiveTextForValues.put(
                                Long.valueOf((String) key), map.get(key));
                    }
                }
            }
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_TIME_DESCRIPTORS,
                    parameters.get(MEGAWIDGET_TIME_DESCRIPTORS),
                    "must be map of longs (or longs in string form) to strings");
        }

        /*
         * Compile a mapping of state identifiers to their indices (giving their
         * ordering).
         */
        Map<String, Integer> indicesForIds = new HashMap<>();
        List<String> stateIdentifiers = getStateIdentifiers();
        for (int j = 0; j < stateIdentifiers.size(); j++) {
            indicesForIds.put(stateIdentifiers.get(j), j);
        }
        this.indicesForIds = ImmutableMap.copyOf(indicesForIds);

        /*
         * Ensure that the factory is present and acceptable.
         */
        IMegawidgetSpecifierFactory factory = null;
        try {
            factory = (IMegawidgetSpecifierFactory) parameters
                    .get(MEGAWIDGET_SPECIFIER_FACTORY);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SPECIFIER_FACTORY,
                    parameters.get(MEGAWIDGET_SPECIFIER_FACTORY),
                    "must be IMegawidgetSpecifierFactory");
        }
        if (factory == null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_SPECIFIER_FACTORY, null, null);
        }

        /*
         * Create the children manager.
         */
        childManager = new ChildSpecifiersManager<IControlSpecifier>(
                IControlSpecifier.class, factory);

        /*
         * If detail fields are included, ensure they are specified correctly.
         */
        if (parameters.containsKey(MEGAWIDGET_DETAIL_FIELDS)) {

            /*
             * Ensure the detail fields map is in fact a map.
             */
            classes = new HashSet<>();
            classes.add(List.class);
            Map<String, ? extends List<Map<String, Object>>> detailFieldsForStateIdentifiers = getStateMappedParametersFromObject(
                    parameters, MEGAWIDGET_DETAIL_FIELDS,
                    "list of detail megawidget specifiers", classes,
                    new ArrayList<Map<String, Object>>(), null, null, false);

            /*
             * Iterate through the state identifiers, getting any detail
             * megawidgets for each of them.
             */
            for (String identifier : stateIdentifiers) {

                /*
                 * If this state identifier has no detail fields, skip it.
                 */
                List<Map<String, Object>> fields = detailFieldsForStateIdentifiers
                        .get(identifier);
                if ((fields == null) || fields.isEmpty()) {
                    continue;
                }

                /*
                 * Convert the maps to child megawidget specifiers.
                 */
                List<IControlSpecifier> children = null;
                try {
                    children = childManager.createMegawidgetSpecifiers(fields,
                            fields.size());
                } catch (MegawidgetSpecificationException e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), MEGAWIDGET_DETAIL_FIELDS + "["
                                    + identifier + "]", fields,
                            "bad child megawidget specifier", e);
                }

                /*
                 * Add the child megawidget specifiers for this choice to the
                 * list of all children, and remember the parameters for this
                 * choice's detail fields.
                 */
                childManager.addChildMegawidgetSpecifiers(children);
                fieldListsForStates.put(identifier, children);
            }
        }
    }

    // Public Methods

    @Override
    public final List<IControlSpecifier> getChildMegawidgetSpecifiers() {
        return childManager.getChildMegawidgetSpecifiers();
    }

    /**
     * Get the minimum interval in milliseconds between adjacent state values.
     * 
     * @return Minimum interval in milliseconds.
     */
    public final long getMinimumInterval() {
        return ((BoundedMultiLongValidator) getStateValidator())
                .getMinimumInterval();
    }

    /**
     * Get the mapping of state identifier keys to their indices in the list
     * provided by the {@link #getStateIdentifiers()} method.
     * 
     * @return Mapping of state identifier keys to their indices.
     */
    public final Map<String, Integer> getIndicesForStateIdentifiers() {
        return indicesForIds;
    }

    /**
     * Determine whether or not the specified state is editable. Note that if
     * {@link #isEditable()} returns false, then the the individual editability
     * flags for different states are ignored, and the entire megawidget is
     * read-only.
     * 
     * @param identifier
     *            Identifier of the state for which the editability is to be
     *            determined.
     * @return True if the state is editable, false otherwise.
     */
    public final boolean isStateEditable(String identifier) {
        return editabilityForStateIdentifiers.get(identifier);
    }

    /**
     * Get the descriptive string for the specified state value, if any.
     * 
     * @param value
     *            State value for which the descriptive string is to be fetched.
     * @return Descriptive string, or <code>null</code> if no such string is
     *         found.
     */
    public final String getStateDescriptiveText(long value) {
        return descriptiveTextForValues.get(value);
    }

    /**
     * Get the list of detail field megawidget specifiers for the specified
     * state identifier.
     * 
     * @param identifier
     *            State identifier.
     * @return List of detail field megawidget specifiers for this state
     *         identifier.
     */
    public final List<IControlSpecifier> getDetailFieldsForState(
            String identifier) {
        return fieldListsForStates.get(identifier);
    }

    // Protected Methods

    @Override
    protected int getMaximumStateIdentifierCount() {

        /*
         * Return an absurdly (for GUI purposes) large number.
         */
        return Integer.MAX_VALUE;
    }
}
