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
 * Jul 23, 2014    4122    Chris.Golden      Extracted most of class to form
 *                                           ContainerSpecifierOptionsManager
 *                                           in order to allow reuse by new
 *                                           DetailedComboBoxSpecifier.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidget
 */
public abstract class ContainerMegawidgetSpecifier<C extends IControlSpecifier>
        extends MegawidgetSpecifier implements IContainerSpecifier<C>,
        IControlSpecifier {

    // Private Variables

    /**
     * Child megawidget specifiers manager.
     */
    private final ChildSpecifiersManager<C> childManager;

    /**
     * Container specifier options manager.
     */
    private final ContainerSpecifierOptionsManager<C> optionsManager;

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
        optionsManager = new ContainerSpecifierOptionsManager<>(this,
                superClass, parameters);

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
    }

    // Public Methods

    @Override
    public final List<C> getChildMegawidgetSpecifiers() {
        return childManager.getChildMegawidgetSpecifiers();
    }

    @Override
    public final int getLeftMargin() {
        return optionsManager.getLeftMargin();
    }

    @Override
    public final int getTopMargin() {
        return optionsManager.getTopMargin();
    }

    @Override
    public final int getRightMargin() {
        return optionsManager.getRightMargin();
    }

    @Override
    public final int getBottomMargin() {
        return optionsManager.getBottomMargin();
    }

    @Override
    public final int getColumnSpacing() {
        return optionsManager.getColumnSpacing();
    }

    @Override
    public final boolean isHorizontalExpander() {
        return optionsManager.isHorizontalExpander();
    }

    @Override
    public final boolean isVerticalExpander() {
        return optionsManager.isVerticalExpander();
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
