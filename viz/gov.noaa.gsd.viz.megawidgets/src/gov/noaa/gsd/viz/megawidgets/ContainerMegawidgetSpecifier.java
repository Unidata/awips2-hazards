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
 * Container megawidget specifier base class, from which specific types of
 * container megawidget specifiers may be derived. The <code>C</code> parameter
 * indicates what type of {@link IControlSpecifier} each child specifier must
 * be.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Added columnSpacing parameter.
 *                                           Also moved child-specifier-
 *                                           tracking functionality into
 *                                           ChildSpecifierManager to allow
 *                                           it to be reused in classes that
 *                                           are not derived from this one.
 *                                           Finally, implemented new
 *                                           IControlSpecifier interface.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidget
 */
public abstract class ContainerMegawidgetSpecifier<C extends IControlSpecifier>
        extends MegawidgetSpecifier implements IContainerSpecifier<C>,
        IControlSpecifier {

    // Private Static Constants

    /**
     * Array of margins in the order they are specified in the {@link #margins}
     * member variable.
     */
    private static final String[] MARGIN_NAMES = { LEFT_MARGIN, TOP_MARGIN,
            RIGHT_MARGIN, BOTTOM_MARGIN };

    /**
     * Array of expand flags in the order they are specified in the
     * {@link #expander} member variable.
     */
    private static final String[] EXPANDER_NAMES = { EXPAND_HORIZONTALLY,
            EXPAND_VERTICALLY };

    // Private Variables

    /**
     * Child megawidget specifiers manager.
     */
    private final ChildSpecifiersManager<C> childManager;

    /**
     * Margins in pixels, the first being left, the second being top, the third
     * right, and the fourth bottom.
     */
    private final int[] margins = new int[4];

    /**
     * Column spacing in pixels.
     */
    private final int columnSpacing;

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
     * @param superClass
     *            Class that must be the superclass of any child megawidget
     *            specifier.
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public ContainerMegawidgetSpecifier(Class<C> superClass,
            Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);

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
        childManager = new ChildSpecifiersManager<C>(superClass, factory);

        /*
         * Ensure that the margins, if present, are acceptable.
         */
        for (int j = 0; j < MARGIN_NAMES.length; j++) {
            margins[j] = ConversionUtilities
                    .getSpecifierIntegerValueFromObject(getIdentifier(),
                            getType(), parameters.get(MARGIN_NAMES[j]),
                            MARGIN_NAMES[j], 0);
        }

        /*
         * Ensure that the column spacing, if present, is acceptable.
         */
        columnSpacing = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(), parameters.get(COLUMN_SPACING),
                COLUMN_SPACING, 15);

        /*
         * Ensure that the expand flags, if present, are acceptable.
         */
        for (int j = 0; j < EXPANDER_NAMES.length; j++) {
            expander[j] = ConversionUtilities
                    .getSpecifierBooleanValueFromObject(getIdentifier(),
                            getType(), parameters.get(EXPANDER_NAMES[j]),
                            EXPANDER_NAMES[j], false);
        }
    }

    // Public Methods

    @Override
    public final List<C> getChildMegawidgetSpecifiers() {
        return childManager.getChildMegawidgetSpecifiers();
    }

    @Override
    public final int getLeftMargin() {
        return margins[0];
    }

    @Override
    public final int getTopMargin() {
        return margins[1];
    }

    @Override
    public final int getRightMargin() {
        return margins[2];
    }

    @Override
    public final int getBottomMargin() {
        return margins[3];
    }

    @Override
    public final int getColumnSpacing() {
        return columnSpacing;
    }

    @Override
    public final boolean isHorizontalExpander() {
        return expander[0];
    }

    @Override
    public final boolean isVerticalExpander() {
        return expander[1];
    }

    // Protected Methods

    /**
     * Get the container child specifiers manager.
     * 
     * @return Container child specifiers manager.
     */
    protected final ChildSpecifiersManager<C> getChildManager() {
        return childManager;
    }
}
