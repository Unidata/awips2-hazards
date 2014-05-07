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
import gov.noaa.gsd.common.utilities.collect.IParameterInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableMap;

/**
 * Description: Parameters editor factory, capable of constructing GUI-based
 * editors consisting of megawidgets that allow the manipulation of arbitrary
 * parameters. The factory may be configured to associate certain types of
 * parameters with certain megawidgets, and thus are an easy way to get
 * megawidget-based GUIs to manipulate values.
 * <p>
 * Megawidgets are build using the
 * {@link #buildParametersEditor(Composite, List, Map, long, long, ICurrentTimeProvider, IParametersEditorListener)}
 * method which returns a {@link MegawidgetManager}. Each megawidget managed by
 * the latter is given as its identifier the label with which the corresponding
 * parameter is associated. Note that the labels passed to the editor should not
 * include colons (:) or greater-than signs (&gt;), since these have special
 * meanings when used as stateful megawidget identifiers. When actually used as
 * labels for the megawidgets, a colon (:) is appended to each such label.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 18, 2013    2336    Chris.Golden      Initial creation
 * Dec 15, 2013    2545    Chris.Golden      Changed to use new TimeMegawidget
 *                                           instead of TimeScaleMegawidget
 *                                           for Dates and Longs.
 * Dec 16, 2013    2545    Chris.Golden      Added current time provider for
 *                                           megawidget use.
 * Apr 10, 2014    2336    Chris.Golden      Changed to parameterize builder
 *                                           method to allow objects of other
 *                                           classes besides String to be used
 *                                           as keys.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ParametersEditorFactory {

    // Private Static Constants

    /**
     * Minimum fraction value allowed.
     */
    private static final Double MIN_FRACTION_VALUE = -10000.0;

    /**
     * Maximum fraction value allowed.
     */
    private static final Double MAX_FRACTION_VALUE = 10000.0;

    /**
     * Mapping of commonly used stateful megawidget specifiers to nested maps,
     * the latter holding default specification parameters for those maps.
     */
    private static final Map<Class<? extends IStatefulSpecifier>, Map<String, Object>> DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS;
    static {
        Map<Class<? extends IStatefulSpecifier>, Map<String, Object>> map = new HashMap<>();

        /*
         * Add default values for text megawidgets.
         */
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(TextSpecifier.MEGAWIDGET_TYPE, "Text");
        defaults.put(TextSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(TextSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(TextSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE, false);
        defaults.put(TextSpecifier.MEGAWIDGET_VISIBLE_LINES, 5);
        map.put(TextSpecifier.class, defaults);

        /*
         * Add default values for integer spinner megawidgets.
         */
        defaults = new HashMap<>();
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

        /*
         * Add default values for fraction spinner megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_TYPE,
                "FractionSpinner");
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(FractionSpinnerSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MIN_VALUE,
                MIN_FRACTION_VALUE);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MAX_VALUE,
                MAX_FRACTION_VALUE);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_INCREMENT_DELTA, 1);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_DECIMAL_PRECISION, 2);
        defaults.put(
                FractionSpinnerSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE,
                false);
        map.put(FractionSpinnerSpecifier.class, defaults);

        /*
         * Add default values for unbounded list builder megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_TYPE,
                "UnboundedListBuilder");
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_VISIBLE_LINES, 5);
        map.put(UnboundedListBuilderSpecifier.class, defaults);

        /*
         * Add default values for time megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(TimeSpecifier.MEGAWIDGET_TYPE, "Time");
        defaults.put(TimeSpecifier.MEGAWIDGET_SPACING, 5);
        defaults.put(TimeSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE, false);
        map.put(TimeSpecifier.class, defaults);

        DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS = ImmutableMap
                .copyOf(map);
    }

    // Private Classes

    /**
     * Megawidget manager used as a parameters editor.
     */
    private class ParametersEditor<K extends IParameterInfo> extends
            MegawidgetManager {

        // Private Variables

        /**
         * Map pairing keys with their associated parameters.
         */
        private final Map<String, K> parametersForKeys;

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
         *            <code>specifiers</code> should have an entry in this map,
         *            mapping the specifier's identifier to the value that the
         *            megawidget will take on (with conversions between
         *            megawidget state and state element being performed by
         *            {@link #convertStateElementToMegawidgetState(String, Object)}
         *            and
         *            {@link #convertMegawidgetStateToStateElement(String, Object)}
         *            ).
         * @param parametersForKeys
         *            Map pairing keys (megawidget identifiers) to the
         *            associated parameters.
         * @param minTime
         *            Minimum time for any time megawidgets specified within
         *            <code>specifiers</code>. If no time megawidgets are
         *            included in <code>specifiers</code>, this is ignored.
         * @param maxTime
         *            Maximum time for any time megawidgets specified within
         *            <code>specifiers</code>. If no time megawidgets are
         *            included in <code>specifiers</code>, this is ignored.
         * @param currentTimeProvider
         *            Current time provider for any time megawidgets specified
         *            within <code>specifiers</code>. If <code>null</code>, a
         *            default current time provider is used. If no time
         *            megawidgets are included in <code>specifiers</code>, this
         *            is ignored.
         * @throws MegawidgetException
         *             If one of the megawidget specifiers is invalid, or if an
         *             error occurs while creating or initializing one of the
         *             megawidgets.
         */
        public ParametersEditor(Composite parent,
                List<Map<String, Object>> specifiers,
                Map<String, Object> state, Map<String, K> parametersForKeys,
                long minTime, long maxTime,
                ICurrentTimeProvider currentTimeProvider)
                throws MegawidgetException {
            super(parent, specifiers, state, minTime, maxTime,
                    currentTimeProvider);
            this.parametersForKeys = parametersForKeys;
        }

        // Public Methods

        @Override
        protected void commandInvoked(String identifier, String extraCallback) {

            /*
             * No action.
             */
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void stateElementChanged(String identifier, Object state) {
            ((IParametersEditorListener<K>) listenersForParents
                    .get(getParent())).parameterValueChanged(
                    parametersForKeys.get(identifier), state);
        }

        @Override
        protected Object convertStateElementToMegawidgetState(
                String identifier, Object value) {
            IConverter converter = convertersForKeysForParents.get(getParent())
                    .get(identifier);
            if (converter != null) {
                return converter.toSecond(value);
            }
            return value;
        }

        @Override
        protected Object convertMegawidgetStateToStateElement(
                String identifier, Object value) {
            IConverter converter = convertersForKeysForParents.get(getParent())
                    .get(identifier);
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
    private final ClassKeyedMap<Map<String, Object>> megawidgetSpecifiersForParameterClasses = new ClassKeyedMap<>();

    /**
     * Map of parameter classes to the converters, if any, that must be used to
     * convert between values of these types and the types expected by the
     * corresponding megawidgets (that is, the values within
     * {@link #megawidgetSpecifiersForParameterClasses}).
     */
    private final ClassKeyedMap<IConverter> convertersForParameterClasses = new ClassKeyedMap<>();

    /**
     * Map of parent composites to nested maps, with each nested map pairing
     * parameter identifiers for that composite's editor to their associated
     * converters, used to translate between the parameter types and the states
     * of their associated megawidget states. If a mapping has a value of
     * <code>null</code>, it means no conversion is required for that parameter.
     */
    private final Map<Composite, Map<String, IConverter>> convertersForKeysForParents = new HashMap<>();

    /**
     * Map of parent composites to their listeners.
     */
    private final Map<Composite, IParametersEditorListener<? extends IParameterInfo>> listenersForParents = new HashMap<>();

    // Public Constructors

    /**
     * Construct a standard instance. This creates a default registering of
     * parameter classes with associated megawidget classes.
     */
    public ParametersEditorFactory() {

        /*
         * Register some default mappings: strings are edited with multiline
         * text megawidgets, integers and floating-point numbers with spinners,
         * longs and date objects with time scales, and lists of strings with
         * unbounded list builders. Floats and dates need converters, since
         * their associated megawidgets expect their state values to be
         * specified as doubles or longs, respectively.
         */
        registerParameterType(String.class, TextSpecifier.class);
        registerParameterType(Integer.class, IntegerSpinnerSpecifier.class);
        registerParameterType(Long.class, TimeSpecifier.class);
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
        registerParameterType(Date.class, TimeSpecifier.class,
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

        /*
         * Create a map to act as a megawidget specifier, and fill it with
         * default values if such exist for this type of megawidget; otherwise,
         * make it simply a map containing the megawidget type specifier.
         */
        Map<String, Object> specifierMap;
        if (DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS
                .containsKey(megawidgetClass)) {
            specifierMap = new HashMap<>(
                    DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS
                            .get(megawidgetClass));
        } else {
            specifierMap = new HashMap<>();

            /*
             * Get the type of the megawidget from the specifier class name, and
             * add that to the map as the megawidget type.
             */
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

        /*
         * Place the option key-value pairs in the specifier map, overriding any
         * default values that exist for such parameters.
         */
        for (String key : megawidgetOptions.keySet()) {
            specifierMap.put(key, megawidgetOptions.get(key));
        }

        /*
         * Associate the resulting specifier map with the parameter type, and if
         * a converter was supplied, remember that as well.
         */
        megawidgetSpecifiersForParameterClasses.put(parameterClass,
                specifierMap);
        convertersForParameterClasses.put(parameterClass, converter);
    }

    /**
     * Instantiate megawidgets for the specified parameters in the given
     * composite.
     * <p>
     * The parameter <code>K</code> specifies the type of class to be used for
     * the labels/identifiers of the parameters. Note that <code>K</code> must
     * implement <code>toString()</code> to return a {@link String} that, when
     * compared to other <code>String</code> instances returned by other
     * instances of <code>K.toString()</code>, adheres to the contract that the
     * <code>K.equals()</code> uses. That is, if to instances of <code>K</code>
     * are considered equivalent by <code>equals()</code>, their <code>
     * toString()</code> should return the same <code>String</code>; and if they
     * are not considered equivalent, their <code>toString()</code> should
     * return different <code>String</code>s.
     * </p>
     * 
     * @param parent
     *            Parent composite in which to create the megawidgets.
     * @param parameters
     *            List of parameters for the megawidgets to be created, in the
     *            order in which they should be displayed top to bottom.
     * @param valuesForParameters
     *            Map of parameters to their starting values. Note that each
     *            time a value is changed by the editor, the value will be
     *            changed within this map to match.
     * @param minimumTime
     *            Minimum time for any time megawidgets.
     * @param maximumTime
     *            Maximum time for any time megawidgets.
     * @param currentTimeProvider
     *            Current time provider for any time megawidgets. If <code>
     *            null</code>, a default current time provider is used which
     *            always provides the current system time.
     * @param listener
     *            Listener to be notified each time the one of the parameter
     *            values has changed, if any. If no listener is desired, the
     *            caller may track the life of <code>parent</code> and simply
     *            check the values within <code>valuesForParameters</code> to
     *            see their updated values once <code>parent</code> is disposed.
     * @return Megawidget manager that is acting as the parameters editor.
     * @throws MegawidgetSpecificationException
     *             If the megawidgets cannot be constructed.
     */
    public <K extends IParameterInfo> MegawidgetManager buildParametersEditor(
            Composite parent, List<K> parameters,
            Map<K, Object> valuesForParameters, long minimumTime,
            long maximumTime, ICurrentTimeProvider currentTimeProvider,
            IParametersEditorListener<K> listener) throws MegawidgetException {

        /*
         * Assemble a list of the megawidget specifiers, and any converters
         * required as well.
         */
        List<Map<String, Object>> specifiers = new ArrayList<>();
        Map<String, IConverter> convertersForKeys = new HashMap<>();
        Map<String, K> parametersForKeys = new HashMap<>();
        Map<String, Object> valuesForKeys = new HashMap<>();
        for (K parameter : parameters) {

            /*
             * Get the key and the label for this parameter.
             */
            String key = parameter.getKey();
            String label = parameter.getLabel();

            /*
             * Get the base specifier for this parameter type.
             */
            Object value = valuesForParameters.get(parameter);
            valuesForKeys.put(key, value);
            if (value == null) {
                throw new MegawidgetException(key, null, null,
                        "no associated parameter value");
            }
            Class<?> parameterClass = value.getClass();
            Map<String, Object> baseSpecifier = megawidgetSpecifiersForParameterClasses
                    .getProximate(parameterClass);
            if (baseSpecifier == null) {
                throw new MegawidgetException(key, null, null,
                        "no megawidget specifier associated with type \""
                                + parameterClass + "\"");
            }

            /*
             * Create an association between this parameter's key and this
             * parameter object, as the megawidget manager needs this.
             */
            parametersForKeys.put(key, parameter);

            /*
             * Modify the base specifier by adding the label as the megawidget
             * label and the key as its identifier, and add it to the list.
             */
            Map<String, Object> specifier = new HashMap<>(baseSpecifier);
            specifier.put(IStatefulSpecifier.MEGAWIDGET_LABEL, label + ":");
            specifier.put(IStatefulSpecifier.MEGAWIDGET_IDENTIFIER, key);
            specifiers.add(specifier);

            /*
             * If a converter exists for this parameter class, associate it with
             * this megawidget identifier.
             */
            convertersForKeys.put(key,
                    convertersForParameterClasses.getProximate(parameterClass));
        }

        /*
         * Associate the parent composite with the converters map created above,
         * as well as the specified listener. This is done so as to allow the
         * megawidget manager that is about to be created to have access to
         * these items when it is constructing itself.
         */
        convertersForKeysForParents.put(parent, convertersForKeys);
        listenersForParents.put(parent, listener);

        /*
         * Ensure that when the parent composite is disposed of, the
         * associations just created between the parent and the converter map
         * and listener are removed, so that garbage collection of the parent
         * and the associated objects may be done.
         */
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                convertersForKeysForParents.remove(e.widget);
                listenersForParents.remove(e.widget);
            }
        });

        /*
         * Create and return a megawidget manager to be used as a parameters
         * editor. The former creates all the megawidgets that are specified and
         * notifies the listener of any parameter value changes.
         */
        return new ParametersEditor<K>(parent, specifiers, valuesForKeys,
                parametersForKeys, minimumTime, maximumTime,
                currentTimeProvider);
    }
}