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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

/**
 * Description: Parent specifier children's manager, used to create and keep
 * track of child megawidget specifiers for a parent megawidget specifier. An
 * instance of this class should be used by each <code>IParentSpecifier</code>
 * implementation.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 24, 2013    2168    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see IParentSpecifier
 */
public class ChildSpecifiersManager<S extends ISpecifier> {

    // Private Variables

    /**
     * Child megawidget specifiers.
     */
    private final List<S> childMegawidgetSpecifiers = Lists.newArrayList();

    /**
     * Superclass of any child specifiers to be managed.
     */
    private final Class<S> superClass;

    /**
     * Megawidget specifier factory, used for building any child megawidget
     * specifiers.
     */
    private final IMegawidgetSpecifierFactory factory;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param factory
     *            Megawidget specifier factory to be used for building any child
     *            megawidget specifiers.
     */
    public ChildSpecifiersManager(Class<S> superClass,
            IMegawidgetSpecifierFactory factory) {
        this.superClass = superClass;
        this.factory = factory;
    }

    // Public Methods

    /**
     * Get the list of all megawidget specifiers that are children of this
     * specifier.
     * 
     * @return List of child megawidget specifiers; this list must not be
     *         modified by the caller.
     */
    public final List<S> getChildMegawidgetSpecifiers() {
        return childMegawidgetSpecifiers;
    }

    /**
     * Construct the megawidget specifiers given the specified parameters.
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
    public final List<S> createMegawidgetSpecifiers(List<?> parameters,
            int numColumns) throws MegawidgetSpecificationException {
        List<S> specifiers = Lists.newArrayList();
        for (Object object : parameters) {
            Map<String, Object> map = (Map<String, Object>) object;
            map.put(IControlSpecifier.MEGAWIDGET_PARENT_COLUMN_COUNT,
                    new Integer(numColumns));
            specifiers.add(factory.createMegawidgetSpecifier(superClass, map));
        }
        return specifiers;
    }

    /**
     * Add the specified child megawidget specifier to the end of the list of
     * all child megawidget specifiers for the managed container. This method or
     * the method <code>addChildMegawidgetSpecifiers()</code> should be used to
     * expose child megawidget specifiers they have created via <code>
     * getChildMegawidgetSpecifiers()</code>.
     * 
     * @param specifier
     *            Child megawidget specifier to be added.
     */
    public final void addChildMegawidgetSpecifier(S specifier) {
        childMegawidgetSpecifiers.add(specifier);
    }

    /**
     * Add the specified child megawidget specifiers to the end of the list of
     * all child megawidget specifiers for the managed container. This method or
     * the method <code>addChildMegawidgetSpecifier()</code> should be used by
     * subclasses to expose child megawidget specifiers they have created via
     * <code>getChildMegawidgetSpecifiers()</code>.
     * 
     * @param specifiers
     *            Child megawidget specifiers to be added.
     */
    public final void addChildMegawidgetSpecifiers(
            Collection<? extends S> specifiers) {
        childMegawidgetSpecifiers.addAll(specifiers);
    }
}