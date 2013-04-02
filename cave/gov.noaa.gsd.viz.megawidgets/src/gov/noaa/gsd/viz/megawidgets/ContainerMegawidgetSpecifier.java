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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Container megawidget specifier base class, from which specific types of
 * container megawidget specifiers may be derived.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidget
 */
public abstract class ContainerMegawidgetSpecifier extends MegawidgetSpecifier
        implements IContainerSpecifier {

    // Private Static Constants

    /**
     * Array of margins in the order they are specified in the
     * <code>margins</code> member variable.
     */
    private static final String[] MARGIN_NAMES = { LEFT_MARGIN, TOP_MARGIN,
            RIGHT_MARGIN, BOTTOM_MARGIN };

    /**
     * Array of expand flags in the order they are specified in the
     * <code>expander</code> member variable.
     */
    private static final String[] EXPANDER_NAMES = { EXPAND_HORIZONTALLY,
            EXPAND_VERTICALLY };

    // Private Variables

    /**
     * Child megawidget specifiers.
     */
    private final List<MegawidgetSpecifier> childWidgetSpecifiers = new ArrayList<MegawidgetSpecifier>();

    /**
     * Megawidget specifier factory, used for building any child megawidget
     * specifiers.
     */
    private final IMegawidgetSpecifierFactory factory;

    /**
     * Margins in pixels, the first being left, the second being top, the third
     * right, and the fourth bottom.
     */
    private final int[] margins = new int[4];

    /**
     * Expander flags, indicating whether the megawidget should expand to fill
     * available space horizontally (for the first) and vertically (for the
     * second).
     */
    private final boolean[] expander = new boolean[2];

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
    public ContainerMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

        // Ensure that the factory is present and acceptable.
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

        // Ensure that the margins, if present, are acceptable.
        for (int j = 0; j < MARGIN_NAMES.length; j++) {
            margins[j] = getSpecifierIntegerValueFromObject(
                    parameters.get(MARGIN_NAMES[j]), MARGIN_NAMES[j], 0);
        }

        // Ensure that the expand flags, if present, are
        // acceptable.
        for (int j = 0; j < EXPANDER_NAMES.length; j++) {
            expander[j] = getSpecifierBooleanValueFromObject(
                    parameters.get(EXPANDER_NAMES[j]), EXPANDER_NAMES[j], false);
        }
    }

    // Public Methods

    /**
     * Get the list of all megawidget specifiers that are children of this
     * specifier.
     * 
     * @return List of child megawidget specifiers; this list must not be
     *         modified by the caller.
     */
    @Override
    public final List<MegawidgetSpecifier> getChildMegawidgetSpecifiers() {
        return childWidgetSpecifiers;
    }

    /**
     * Get the left margin.
     * 
     * @return left margin.
     */
    @Override
    public final int getLeftMargin() {
        return margins[0];
    }

    /**
     * Get the top margin.
     * 
     * @return Top margin.
     */
    @Override
    public final int getTopMargin() {
        return margins[1];
    }

    /**
     * Get the right margin.
     * 
     * @return Right margin.
     */
    @Override
    public final int getRightMargin() {
        return margins[2];
    }

    /**
     * Get the bottom margin.
     * 
     * @return bottom margin.
     */
    @Override
    public final int getBottomMargin() {
        return margins[3];
    }

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * horizontal space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         horizontally.
     */
    @Override
    public final boolean isHorizontalExpander() {
        return expander[0];
    }

    /**
     * Determine whether or not the megawidget is to expand to take up available
     * vertical space within its parent.
     * 
     * @return Flag indicating whether or not the megawidget is to expand
     *         vertically.
     */
    @Override
    public final boolean isVerticalExpander() {
        return expander[1];
    }

    /**
     * Get the megawidget specifier factory.
     * 
     * @return Megawidget specifier factory.
     */
    @Override
    public final IMegawidgetSpecifierFactory getMegawidgetSpecifierFactory() {
        return factory;
    }

    // Protected Methods

    /**
     * Construct the megawidget specifiers given the specified parameters. This
     * may be invoked by subclasses to build a list of child megawidget
     * specifiers.
     * 
     * @param parameters
     *            List holding the list of map objects that each provide the
     *            parameters for a megawidget specifier.
     * @param numColumns
     *            Number of columns in which the child megawidgets may be laid
     *            out.
     * @return List of created megawidget specifiers.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    @SuppressWarnings("unchecked")
    protected final List<MegawidgetSpecifier> createMegawidgetSpecifiers(
            List<?> parameters, int numColumns)
            throws MegawidgetSpecificationException {
        List<MegawidgetSpecifier> specifiers = new ArrayList<MegawidgetSpecifier>();
        for (Object object : parameters) {
            Map<String, Object> map = (Map<String, Object>) object;
            map.put(MEGAWIDGET_PARENT_COLUMN_COUNT, new Integer(numColumns));
            specifiers.add(factory.createMegawidgetSpecifier(map));
        }
        return specifiers;
    }

    /**
     * Add the specified child megawidget specifier to the end of the list of
     * all child megawidget specifiers for this container. This method or the
     * method <code>addChildMegawidgetSpecifiers()</code> should be used by
     * subclasses to expose child megawidget specifiers they have created via
     * <code>getChildMegawidgetSpecifiers()</code>.
     * 
     * @param specifier
     *            Child megawidget specifier to be added.
     */
    protected final void addChildMegawidgetSpecifier(
            MegawidgetSpecifier specifier) {
        childWidgetSpecifiers.add(specifier);
    }

    /**
     * Add the specified child megawidget specifiers to the end of the list of
     * all child megawidget specifiers for this container. This method or the
     * method <code>addChildMegawidgetSpecifier()</code> should be used by
     * subclasses to expose child megawidget specifiers they have created via
     * <code>getChildMegawidgetSpecifiers()</code>.
     * 
     * @param specifiers
     *            Child megawidget specifiers to be added.
     */
    protected final void addChildMegawidgetSpecifiers(
            Collection<? extends MegawidgetSpecifier> specifiers) {
        childWidgetSpecifiers.addAll(specifiers);
    }
}
