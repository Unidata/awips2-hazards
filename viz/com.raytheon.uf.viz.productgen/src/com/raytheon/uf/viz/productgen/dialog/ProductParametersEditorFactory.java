/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.productgen.dialog;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableMap;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.common.utilities.collect.ClassKeyedMap;
import gov.noaa.gsd.viz.megawidgets.ExpandBarSpecifier;
import gov.noaa.gsd.viz.megawidgets.FractionSpinnerSpecifier;
import gov.noaa.gsd.viz.megawidgets.IConverter;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.IntegerSpinnerSpecifier;
import gov.noaa.gsd.viz.megawidgets.LabelSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.TextSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeSpecifier;
import gov.noaa.gsd.viz.megawidgets.UnboundedListBuilderSpecifier;

/**
 * Description: Parameters editor factory, capable of constructing GUI-based
 * editors consisting of megawidgets that allow the manipulation of arbitrary
 * parameters. The factory may be configured to associate certain types of
 * parameters with certain megawidgets, and thus are an easy way to get
 * megawidget-based GUIs to manipulate values.
 * <p>
 * Megawidgets are build using the
 * {@link #buildParametersEditor(Composite, List, Map, long, long, ICurrentTimeProvider, IProductParametersEditorListener)}
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
 * Jun 20, 2014    4010    Chris.Golden      Changed to allow megawidgets to
 *                                           have their visibility toggled via
 *                                           an expand bar if desired. Also
 *                                           changed to work with newest
 *                                           megawidget manager changes.
 * Jun 30, 2014    3512    Chris.Golden      Changed to work with latest
 *                                           megawidget manager changes, and to
 *                                           send multiple simultaneous
 *                                           parameter change notifications to
 *                                           its listener. Also consolidated
 *                                           size change notifications and other
 *                                           notifications into that same single
 *                                           listener.
 * Aug 07, 2014    3777    Robert.Blum       Changed to make SpellCheck Enabled
 *                                           set to true the default for text
 *                                           megawidgets.
 * Dec 15, 2014    3846    Tracy Hansen      small bug fix
 * Apr 28, 2015    7579    Robert.Blum       Changes to allow LabelMegaWidgets on
 *                                           the Product Editor.
 * May 07, 2015    6979    Robert.Blum       Additional changes for LabelMegaWidgets,
 *                                           they are now stateful.
 * Jul 28, 2015    9687    Robert.Blum       Displaying label based on new KeyInfo flag.
 * Jul 29, 2015    9686    Robert.Blum       Sizing text fields based on amount of text
 *                                           they contain.
 * Jul 30, 2015    9681    Robert.Blum       Adding * to labels if the field is required.
 * Aug 31, 2015    9617    Chris.Golden      This library class has been littered with
 *                                           product-editor-specific code, so it has now
 *                                           been copied to the product editor package
 *                                           and the original has reverted to be more
 *                                           generic. This is not a permanent solution
 *                                           to the problem, but it allows the megawidget
 *                                           framework to remain more generic than it was
 *                                           becoming.
 * Oct 08, 2015   12165    Chris.Golden      Changed to use new borderless option for
 *                                           Text megawidgets that are meant to look like
 *                                           labels.
 * Jun 07, 2016   19464    Chris.Golden      Added red labels for required fields.
 * Dec 06, 2016   26855    Chris.Golden      Changed to use wrapping Label megawidget for
 *                                           editor field labels, in case the latter are
 *                                           too long to fit within the width of the
 *                                           product editor. Also ensured that Text and
 *                                           other megawidgets expand horizontally to
 *                                           fill their parents. Added a container for
 *                                           generated megawidgets that is a scrollable
 *                                           Composite megawidget.
 * Jun 05, 2017   29996    Robert.Blum       Removed unused method that no longer compiled.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ProductParametersEditorFactory {

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
     * Default specification parameters for expand bar container megawidgets.
     */
    private static final Map<String, Object> DEFAULT_EXPAND_BAR_SPECIFICATION_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ExpandBarSpecifier.MEGAWIDGET_TYPE, "ExpandBar");
        map.put(ExpandBarSpecifier.MEGAWIDGET_SPACING, 10);
        map.put(ExpandBarSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(ExpandBarSpecifier.EXPAND_VERTICALLY, true);
        map.put(ExpandBarSpecifier.LEFT_MARGIN, 0);
        map.put(ExpandBarSpecifier.RIGHT_MARGIN, 0);
        map.put(ExpandBarSpecifier.TOP_MARGIN, 2);
        map.put(ExpandBarSpecifier.BOTTOM_MARGIN, 2);

        DEFAULT_EXPAND_BAR_SPECIFICATION_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Mapping of commonly used megawidget specifiers to nested maps, the latter
     * holding default specification parameters for those maps.
     */
    private static final Map<Class<? extends ISpecifier>, Map<String, Object>> DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS;
    static {
        Map<Class<? extends ISpecifier>, Map<String, Object>> map = new HashMap<>();

        /*
         * Add default values for text megawidgets.
         */
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(TextSpecifier.MEGAWIDGET_TYPE, "Text");
        defaults.put(TextSpecifier.MEGAWIDGET_SPACING, 6);
        defaults.put(TextSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(TextSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE, false);
        defaults.put(TextSpecifier.SPELLCHECK_ENABLED, true);
        map.put(TextSpecifier.class, defaults);

        /*
         * Add default values for pseudo-label megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(TextSpecifier.MEGAWIDGET_TYPE, "Text");
        defaults.put(TextSpecifier.MEGAWIDGET_SPACING, 10);
        defaults.put(TextSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(TextSpecifier.MEGAWIDGET_EDITABLE, false);
        defaults.put(TextSpecifier.SHOW_BORDER, false);
        map.put(LabelSpecifier.class, defaults);

        /*
         * Add default values for integer spinner megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_TYPE, "IntegerSpinner");
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_SPACING, 10);
        defaults.put(IntegerSpinnerSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_MIN_VALUE,
                Integer.MIN_VALUE);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_MAX_VALUE,
                Integer.MAX_VALUE);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA,
                10);
        defaults.put(IntegerSpinnerSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE,
                false);
        map.put(IntegerSpinnerSpecifier.class, defaults);

        /*
         * Add default values for fraction spinner megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_TYPE,
                "FractionSpinner");
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_SPACING, 10);
        defaults.put(FractionSpinnerSpecifier.EXPAND_HORIZONTALLY, true);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MIN_VALUE,
                MIN_FRACTION_VALUE);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_MAX_VALUE,
                MAX_FRACTION_VALUE);
        defaults.put(FractionSpinnerSpecifier.MEGAWIDGET_PAGE_INCREMENT_DELTA,
                1);
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
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_SPACING, 10);
        defaults.put(UnboundedListBuilderSpecifier.MEGAWIDGET_VISIBLE_LINES, 5);
        map.put(UnboundedListBuilderSpecifier.class, defaults);

        /*
         * Add default values for time megawidgets.
         */
        defaults = new HashMap<>();
        defaults.put(TimeSpecifier.MEGAWIDGET_TYPE, "Time");
        defaults.put(TimeSpecifier.MEGAWIDGET_SPACING, 10);
        defaults.put(TimeSpecifier.MEGAWIDGET_SEND_EVERY_STATE_CHANGE, false);
        map.put(TimeSpecifier.class, defaults);

        DEFAULT_SPECIFICATION_PARAMETERS_FOR_MEGAWIDGETS = ImmutableMap
                .copyOf(map);
    }

    // Private Variables

    /**
     * Map of properties to be used when creating expand bars for any parameter
     * editors that are to be embedded within expandable areas.
     */
    private final Map<String, Object> expandBarSpecifier = new HashMap<>(
            DEFAULT_EXPAND_BAR_SPECIFICATION_PARAMETERS);

    /**
     * Map of parameter classes to nested maps, the latter acting as specifiers
     * for the megawidgets that are to be used to allow manipulation of values
     * that are instances of these parameter classes.
     */
    private final ClassKeyedMap<Map<String, Object>> megawidgetSpecifiersForParameterClasses = new ClassKeyedMap<>();

    /**
     * Map of parameter classes to flags indicating whether or not their
     * megawidgets are to be expandable, that is, to have their visibility
     * toggled via an expand bar.
     */
    private final ClassKeyedMap<Boolean> expandabilityForParameterClasses = new ClassKeyedMap<>();

    /**
     * Map of parameter classes to the converters, if any, that must be used to
     * convert between values of these types and the types expected by the
     * corresponding megawidgets (that is, the values within
     * {@link #megawidgetSpecifiersForParameterClasses}).
     */
    private final ClassKeyedMap<IConverter> convertersForParameterClasses = new ClassKeyedMap<>();

    // Public Constructors

    /**
     * Construct a standard instance. This creates a default registering of
     * parameter classes with associated megawidget classes.
     */
    public ProductParametersEditorFactory() {

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
     * Register expand bar options, to be used for the containers of editors of
     * any parameter types that are to be expandable.
     * 
     * @param expandBarOptions
     *            Map of expand bar specifier options to be used. Any options
     *            that are not specified within this map are taken from the
     *            defaults.
     * @see #registerParameterType(Class, Class, Map, IConverter, boolean)
     */
    public void registerExpandBarOptions(Map<String, ?> expandBarOptions) {
        expandBarSpecifier.clear();
        expandBarSpecifier.putAll(DEFAULT_EXPAND_BAR_SPECIFICATION_PARAMETERS);
        expandBarSpecifier.putAll(expandBarOptions);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class. The latter is configured in some default manner, and
     * expects its state to be specified by an object of the type given by the
     * parameter class. If an association with this class is already in
     * existence, it is overwritten by this new one. The megawidget will not be
     * expandable (i.e. will not have its visiblility toggled via an expand
     * bar).
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
                Collections.<String, Object> emptyMap(), null, false);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class with the specified options. The latter expects its state
     * to be specified by an object of the type given by the parameter class. If
     * an association with this class is already in existence, it is overwritten
     * by this new one. The megawidget will not be expandable (i.e. will not
     * have its visiblility toggled via an expand bar).
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
                megawidgetOptions, null, false);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class. The latter is configured in some default manner. If an
     * association with this class is already in existence, it is overwritten by
     * this new one. The megawidget will not be expandable (i.e. will not have
     * its visibility toggled via an expand bar).
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
                Collections.<String, Object> emptyMap(), converter, false);
    }

    /**
     * Register a parameter type, associating its class with the specified
     * megawidget class. If an association with this class is already in
     * existence, it is overwritten by this new one. The megawidget will not be
     * expandable (i.e. will not have its visiblility toggled via an expand
     * bar).
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
        registerParameterType(parameterClass, megawidgetClass,
                megawidgetOptions, converter, false);
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
     * @param expandable
     *            Flag indicating whether or not the megawidget should be
     *            expandable, that is, its header should be able to be toggled
     *            to show or hide the megawidget.
     */
    public <A> void registerParameterType(Class<A> parameterClass,
            Class<? extends IStatefulSpecifier> megawidgetClass,
            Map<String, ?> megawidgetOptions, IConverter converter,
            boolean expandable) {

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
        specifierMap.putAll(megawidgetOptions);

        /*
         * Associate the resulting specifier map with the parameter type, and if
         * a converter was supplied, remember that as well. Also remember
         * whether or not it should be expandable.
         */
        megawidgetSpecifiersForParameterClasses.put(parameterClass,
                specifierMap);
        convertersForParameterClasses.put(parameterClass, converter);
        expandabilityForParameterClasses.put(parameterClass, expandable);
    }
}