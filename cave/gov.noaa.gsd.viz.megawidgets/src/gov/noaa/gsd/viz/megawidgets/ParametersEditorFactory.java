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

import gov.noaa.gsd.common.utilities.collect.ClassKeyedMap;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Description: Parameters editor factory, capable of constructing GUI-based
 * editors consisting of megawidgets that allow the manipulation of arbitrary
 * parameters. The factory may be configured to associate certain types of
 * parameters with certain megawidgets, and thus are an easy way to get
 * megawidget-based GUIs to manipulate values.
 * <p>
 * Megawidgets are build using the <code>buildParametersEditor()</code> method
 * which returns a {@link MegawidgetManager}. Each megawidget managed by the
 * latter is given as its identifier the label with which the corresponding
 * parameter is associated. Note that the labels passed to the editor should not
 * include colons (:) or greater-than signs (>), since these have special
 * meanings when used as stateful megawidget identifiers. When actually used as
 * labels for the megawidgets, a colon (:) is appended to each such label.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2013    2336    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ParametersEditorFactory {

    // Private Static Constants

    /**
     * Mapping of commonly used stateful megawidget specifiers to nested maps,
     * the latter holding default specification parameters for those maps.
     */
    private static final Map<Class<? extends IStatefulSpecifier>, Map<String, Object>> DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS;

    // Initialized the map of default specification parameters for stateful
    // megawidget specifiers.
    static {
        Map<Class<? extends IStatefulSpecifier>, Map<String, Object>> map = Maps
                .newHashMap();

        // Add default values for text megawidgets.
        Map<String, Object> defaults = Maps.newHashMap();
        defaults.put(TextSpecifier.MEGAWIDGET_TYPE, "Text");
        defaults.put(TextSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(TextSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(TextSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE, false);
        defaults.put(TextSpecifier.MEGAWIDGET_VISIBLE_LINES, 5);
        map.put(TextSpecifier.class, defaults);

        // Add default values for integer spinner megawidgets.
        defaults = Maps.newHashMap();
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_TYPE, "IntegerSpinner");
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(IntegerSpinnerSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_MIN_VALUE,
                Integer.MIN_VALUE);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_MAX_VALUE,
                Integer.MAX_VALUE);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, 10);
        defaults.put(
                IntegerSpinnerSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE,
                false);
        map.put(IntegerSpinnerSpecifier.class, defaults);

        // Add default values for fraction spinner megawidgets.
        defaults = Maps.newHashMap();
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_TYPE,
                "FractionSpinner");
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(FractionSpinnerSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MIN_VALUE,
                Integer.MIN_VALUE / 100.0);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MAX_VALUE,
                Integer.MAX_VALUE / 100.0);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, 1);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_DECIMAL_PRECISION, 2);
        defaults.put(
                FractionSpinnerSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE,
                false);
        map.put(FractionSpinnerSpecifier.class, defaults);

        // Add default values for unbounded list builder megawidgets.
        defaults = Maps.newHashMap();
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_TYPE,
                "UnboundedListBuilder");
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_VISIBLE_LINES, 5);
        map.put(UnboundedListBuilderSpecifier.class, defaults);

        // Add default values for time scale megawidgets.
        defaults = Maps.newHashMap();
        defaults.put(TimeScaleSpecifier.MEGAWIDGET_TYPE, "TimeScale");
        defaults.put(TimeScaleSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(TimeScaleSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE,
                false);
        map.put(TimeScaleSpecifier.class, defaults);

        DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS = ImmutableMap
                .copyOf(map);
    }

    // Private Classes

    /**
     * Megawidget manager used as a parameters editor.
     */
    private class ParametersEditor extends MegawidgetManager {

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param parent
         *            Parent composite in which the megawidgets are to be
         *            created.
         * @param specifiers
         *            List of maps, each of the latter holding the parameters of
         *            a megawidget specifier. Each megawidget specifier must
         *            have an identifier that is unique within this list.
         * @param state
         *            State to be viewed and/or modified via the megawidgets
         *            that are constructed. Each megawidget specifier defined by
         *            <code>
         *            specifiers</code> should have an entry in this map,
         *            mapping the specifier's identifier to the value that the
         *            megawidget will take on (with conversions between
         *            megawidget state and state element being performed by
         *            <code>
         *            convertStateElementToMegawidgetState()</code> and <code>
         *            convertMegawidgetStateToStateElement()</code>).
         * @param minTime
         *            Minimum time for any time scale megawidgets specified
         *            within <code>specifiers</code>. If no time scale
         *            megawidgets are included in <code>specifiers</code>, this
         *            is ignored.
         * @param maxTime
         *            Maximum time for any time scale megawidgets specified
         *            within <code>specifiers</code>. If no time scale
         *            megawidgets are included in <code>specifiers</code>, this
         *            is ignored.
         * @throws MegawidgetException
         *             If one of the megawidget specifiers is invalid, or if an
         *             error occurs while creating or initializing one of the
         *             megawidgets.
         */
        public ParametersEditor(Composite parent,
                List<Map<String, Object>> specifiers,
                Map<String, Object> state, long minTime, long maxTime)
                throws MegawidgetException {
            super(parent, specifiers, state, minTime, maxTime, minTime, maxTime);
        }

        // Public Methods

        @Override
        protected void commandInvoked(String identifier, String extraCallback) {

            // No action.
        }

        @Override
        protected void stateElementChanged(String identifier, Object state) {
            listenersForParents.get(getParent()).parameterValueChanged(
                    identifier, state);
        }

        @Override
        protected Object convertStateElementToMegawidgetState(
                String identifier, Object value) {
            IConverter converter = convertersForIdentifiersForParents.get(
                    getParent()).get(identifier);
            if (converter != null) {
                return converter.toSecond(value);
            }
            return value;
        }

        @Override
        protected Object convertMegawidgetStateToStateElement(
                String identifier, Object value) {
            IConverter converter = convertersForIdentifiersForParents.get(
                    getParent()).get(identifier);
            if (converter != null) {
                return converter.toFirst(value);
            }
            return value;
        }
    };

    // Private Variables

    /**
     * Map of parameter classes to nested maps, the latter acting as specifiers
     * for the megawidgets that are to be used to allow manipulation of values
     * that are instances of these parameter classes.
     */
    private final ClassKeyedMap<Map<String, Object>> megawidgetSpecifiersForParameterClasses = new ClassKeyedMap<Map<String, Object>>();

    /**
     * Map of parameter classes to the converters, if any, that must be used to
     * convert between values of these types and the types expected by the
     * corresponding megawidgets (that is, the values within <code>
     * megawidgetSpecifiersForParameterClasses</code>).
     */
    private final ClassKeyedMap<IConverter> convertersForParameterClasses = new ClassKeyedMap<IConverter>();

    /**
     * Map of parent composites to nested maps, with each nested map pairing
     * parameter identifiers for that composite's editor to their associated
     * converters, used to translate between the parameter types and the states
     * of their associated megawidget states. If a mapping has a value of
     * <code>null</code>, it means no conversion is required for that parameter.
     */
    private final Map<Composite, Map<String, IConverter>> convertersForIdentifiersForParents = Maps
            .newHashMap();

    /**
     * Map of parent composites to their listeners.
     */
    private final Map<Composite, IParametersEditorListener> listenersForParents = Maps
            .newHashMap();

    // Public Constructors

    /**
     * Construct a standard instance. This creates a default registering of
     * parameter classes with associated megawidget classes.
     */
    public ParametersEditorFactory() {

        // Register some default mappings: strings are edited with multiline
        // text megawidgets, integers and floating-point numbers with spinners,
        // longs and date objects with time scales, and lists of strings with
        // unbounded list builders. Floats and dates need converters, since
        // their associated megawidgets expect their state values to be
        // specified as doubles or longs, respectively.
        registerParameterType(String.class, TextSpecifier.class);
        registerParameterType(Integer.class, IntegerSpinnerSpecifier.class);
        registerParameterType(Long.class, TimeScaleSpecifier.class);
        registerParameterType(Float.class, FractionSpinnerSpecifier.class,
                new IConverter() {
                    @Override
                    public Object toFirst(Object value) {
                        return ((Double) value).floatValue();
                    }

                    @Override
                    public Object toSecond(Object value) {
                        return ((Float) value).doubleValue();
                    }
                });
        registerParameterType(Double.class, FractionSpinnerSpecifier.class);
        registerParameterType(List.class, UnboundedListBuilderSpecifier.class);
        registerParameterType(Date.class, TimeScaleSpecifier.class,
                new IConverter() {
                    @Override
                    public Object toFirst(Object value) {
                        return new Date((Long) value);
                    }

                    @Override
                    public Object toSecond(Object value) {
                        return ((Date) value).getTime();
                    }

                });
    }

    // Public Methods

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class. The latter is configured in some default manner, and
     * expects its state to be specified by an object of the type given by the
     * parameter class. If an association with this class is already in
     * existence, it is overwritten by this new one.
     * 
     * @param parameterClass
     *            Class of the parameter.
     * @param megawidgetClass
     *            Class of the megawidget with which this parameter class is to
     *            be associated.
     */
    public void registerParameterType(Class<?> parameterClass,
            Class<? extends IStatefulSpecifier> megawidgetClass) {
        registerParameterType(parameterClass, megawidgetClass,
                Collections.<String, Object> emptyMap(), null);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class with the specified options. The latter expects its state
     * to be specified by an object of the type given by the parameter class. If
     * an association with this class is already in existence, it is overwritten
     * by this new one.
     * 
     * @param parameterClass
     *            Class of the parameter.
     * @param megawidgetClass
     *            Class of the megawidget with which this parameter class is to
     *            be associated.
     * @param megawidgetOptions
     *            Map of specifier options to be used to configure the
     *            megawidget.
     */
    public void registerParameterType(Class<?> parameterClass,
            Class<? extends IStatefulSpecifier> megawidgetClass,
            Map<String, ?> megawidgetOptions) {
        registerParameterType(parameterClass, megawidgetClass,
                megawidgetOptions, null);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class. The latter is configured in some default manner. If an
     * association with this class is already in existence, it is overwritten by
     * this new one.
     * 
     * @param parameterClass
     *            Class of the parameter.
     * @param megawidgetClass
     *            Class of the megawidget with which this parameter class is to
     *            be associated.
     * @param converter
     *            Converter to be used to translate between objects of the type
     *            given by <code>parameterClass</code> and the objects expected
     *            and provided by the megawidget of type <code>megawidgetClass
     *            </code> as state.
     */
    public <A> void registerParameterType(Class<A> parameterClass,
            Class<? extends IStatefulSpecifier> megawidgetClass,
            IConverter converter) {
        registerParameterType(parameterClass, megawidgetClass,
                Collections.<String, Object> emptyMap(), converter);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class with the specified options. If an association with this
     * class is already in existence, it is overwritten by this new one.
     * 
     * @param parameterClass
     *            Class of the parameter.
     * @param megawidgetClass
     *            Class of the megawidget with which this parameter class is to
     *            be associated.
     * @param megawidgetOptions
     *            Map of specifier options to be used to configure the
     *            megawidget.
     * @param converter
     *            Converter to be used to translate between objects of the type
     *            given by <code>parameterClass</code> and the objects expected
     *            and provided by the megawidget of type <code>megawidgetClass
     *            </code> as state.
     */
    public <A> void registerParameterType(Class<A> parameterClass,
            Class<? extends IStatefulSpecifier> megawidgetClass,
            Map<String, ?> megawidgetOptions, IConverter converter) {

        // Create a map to act as a megawidget specifier, and fill it with
        // default values if such exist for this type of megawidget; other-
        // wise, make it simply a map containing the megawidget type speci-
        // fier.
        Map<String, Object> specifierMap;
        if (DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS
                .containsKey(megawidgetClass)) {
            specifierMap = Maps
                    .newHashMap(DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS
                            .get(megawidgetClass));
        } else {
            specifierMap = Maps.newHashMap();

            // Get the type of the megawidget from the specifier class
            // name, and add that to the map as the megawidget type.
            String specifierClassName = megawidgetClass.getSimpleName();
            int index = specifierClassName.lastIndexOf("Specifier");
            if (index == -1) {
                throw new IllegalStateException("megawidget specifier class \""
                        + megawidgetClass
                        + "\" does not have name ending in \"Specifier\"");
            }
            specifierMap.put(IStatefulSpecifier.MEGAWIDGET_TYPE,
                    specifierClassName.substring(0, index));
        }

        // Place the option key-value pairs in the specifier map, over-
        // riding any default values that exist for such parameters.
        for (String key : megawidgetOptions.keySet()) {
            specifierMap.put(key, megawidgetOptions.get(key));
        }

        // Associate the resulting specifier map with the parameter type,
        // and if a converter was supplied, remember that as well.
        megawidgetSpecifiersForParameterClasses.put(parameterClass,
                specifierMap);
        convertersForParameterClasses.put(parameterClass, converter);
    }

    /**
     * Instantiate megawidgets for the specified parameters in the given
     * composite.
     * 
     * @param parent
     *            Parent composite in which to create the megawidgets.
     * @param labels
     *            List of labels for the megawidgets to be created, in the order
     *            in which they should be displayed top to bottom.
     * @param parametersForLabels
     *            Map of labels identifying the parameters to their starting
     *            values. Each label is used as a visual label as well. Note
     *            that each time a value is changed by the editor, the value
     *            will be changed within this map to match.
     * @param minimumTime
     *            Minimum time for any time scale megawidgets.
     * @param maximumTime
     *            Maximum time for any time scale megawidgets.
     * @param listener
     *            Listener to be notified each time the one of the parameter
     *            values has changed, if any. If no listener is desired, the
     *            caller may track the life of <code>parent</code> and simply
     *            check the parameters within <code>parametersForLabels</code>
     *            to see their updated values once <code>parent</code> is
     *            disposed.
     * @return Megawidget manager that is acting as the parameters editor.
     * @throws MegawidgetSpecificationException
     *             If the megawidgets cannot be constructed.
     */
    public MegawidgetManager buildParametersEditor(Composite parent,
            List<String> labels, Map<String, Object> parametersForLabels,
            long minimumTime, long maximumTime,
            IParametersEditorListener listener) throws MegawidgetException {

        // Assemble a list of the megawidget specifiers, and any
        // converters required as well.
        List<Map<String, Object>> specifiers = Lists.newArrayList();
        Map<String, IConverter> convertersForLabels = Maps.newHashMap();
        for (String label : labels) {

            // Get the base specifier for this parameter type.
            Object parameter = parametersForLabels.get(label);
            if (parameter == null) {
                throw new MegawidgetException(label, null, null,
                        "no associated parameter");
            }
            Class<?> parameterClass = parameter.getClass();
            Map<String, Object> baseSpecifier = megawidgetSpecifiersForParameterClasses
                    .getProximate(parameterClass);
            if (baseSpecifier == null) {
                throw new MegawidgetException(label, null, null,
                        "no megawidget specifier associated with type \""
                                + parameter.getClass() + "\"");
            }

            // Modify the base specifier by adding the label as both
            // the megawidget label and its identifier, and add it
            // to the list.
            Map<String, Object> specifier = Maps.newHashMap(baseSpecifier);
            specifier.put(IStatefulSpecifier.MEGAWIDGET_LABEL, label + ":");
            specifier.put(IStatefulSpecifier.MEGAWIDGET_IDENTIFIER, label);
            specifiers.add(specifier);

            // If a converter exists for this parameter class, asso-
            // ciate it with this megawidget identifier.
            convertersForLabels.put(label,
                    convertersForParameterClasses.getProximate(parameterClass));
        }

        // Associate the parent composite with the converters map
        // created above, as well as the specified listener. This
        // is done so as to allow the megawidget manager that is
        // about to be created to have access to these items when
        // it is constructing itself.
        convertersForIdentifiersForParents.put(parent, convertersForLabels);
        listenersForParents.put(parent, listener);

        // Ensure that when the parent composite is disposed of,
        // the associations just created between the parent and the
        // converter map and listener are removed, so that garbage
        // collection of the parent and the associated objects may
        // be done.
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                convertersForIdentifiersForParents.remove(e.widget);
                listenersForParents.remove(e.widget);
            }
        });

        // Create and return a megawidget manager to be used as a
        // parameters editor. The former creates all the megawidgets
        // that are specified and notifies the listener of any para-
        // meter value changes.
        return new ParametersEditor(parent, specifiers, parametersForLabels,
                minimumTime, maximumTime);
    }
}