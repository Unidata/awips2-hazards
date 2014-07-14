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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

/**
 * Megawidget specifier manager class, used to instantiate megawidget
 * specifiers. The manager is built using list of maps, each of which provide
 * the parameters for a megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 07, 2014    2925    Chris.Golden Initial creation.
 * May 12, 2014    2925    Chris.Golden Changed to include current time
 *                                      provider.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MegawidgetSpecifier
 */
public class MegawidgetSpecifierManager {

    // Private Variables

    /**
     * List of megawidget specifiers.
     */
    private final ImmutableList<ISpecifier> specifiers;

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider;

    /**
     * Side effects applier, or <code>null</code> if there is none for these
     * megawidgets.
     */
    private final ISideEffectsApplier sideEffectsApplier;

    // Public Constructors

    /**
     * Construct a standard instance with a default current time provider and no
     * side effects applier.
     * 
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param superClass
     *            Class that must be the superclass of any megawidget specifiers
     *            created.
     * @throws MegawidgetSpecificationException
     *             If one of the megawidget specifiers is invalid.
     */
    public MegawidgetSpecifierManager(
            List<? extends Map<String, Object>> specifiers,
            Class<? extends ISpecifier> superClass)
            throws MegawidgetSpecificationException {
        this(specifiers, superClass, null, null);
    }

    /**
     * Construct a standard instance with no side effects applier.
     * 
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param superClass
     *            Class that must be the superclass of any megawidget specifiers
     *            created.
     * @param currentTimeProvider
     *            Current time provider for any time megawidgets specified
     *            within <code>specifiers</code>. If <code>null</code>, a
     *            default current time provider is used. If no time megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @throws MegawidgetSpecificationException
     *             If one of the megawidget specifiers is invalid.
     */
    public MegawidgetSpecifierManager(
            List<? extends Map<String, Object>> specifiers,
            ICurrentTimeProvider currentTimeProvider,
            Class<? extends ISpecifier> superClass)
            throws MegawidgetSpecificationException {
        this(specifiers, superClass, currentTimeProvider, null);
    }

    /**
     * Construct a standard instance.
     * 
     * @param specifiers
     *            List of maps, each of the latter holding the parameters of a
     *            megawidget specifier. Each megawidget specifier must have an
     *            identifier that is unique within this list.
     * @param superClass
     *            Class that must be the superclass of any megawidget specifiers
     *            created.
     * @param currentTimeProvider
     *            Current time provider for any time megawidgets specified
     *            within <code>specifiers</code>. If <code>null</code>, a
     *            default current time provider is used. If no time megawidgets
     *            are included in <code>specifiers</code>, this is ignored.
     * @param sideEffectsApplier
     *            Side effects applier to be used with these megawidgets.
     * @throws MegawidgetSpecificationException
     *             If one of the megawidget specifiers is invalid.
     */
    public MegawidgetSpecifierManager(
            List<? extends Map<String, Object>> specifiers,
            Class<? extends ISpecifier> superClass,
            ICurrentTimeProvider currentTimeProvider,
            ISideEffectsApplier sideEffectsApplier)
            throws MegawidgetSpecificationException {

        /*
         * Iterate through the megawidget specifiers, instantiating each one in
         * turn, and then instantiating the corresponding megawidget.
         */
        MegawidgetSpecifierFactory factory = new MegawidgetSpecifierFactory();
        Set<String> identifiers = new HashSet<>();

        List<ISpecifier> createdSpecifiers = new ArrayList<>(specifiers.size());
        for (Map<String, Object> specifierMap : specifiers) {
            ISpecifier specifier = factory.createMegawidgetSpecifier(
                    superClass, specifierMap);
            ensureMegawidgetIdentifiersAreUnique(specifier, identifiers);
            createdSpecifiers.add(specifier);
        }

        this.specifiers = ImmutableList.copyOf(createdSpecifiers);
        this.currentTimeProvider = currentTimeProvider;
        this.sideEffectsApplier = sideEffectsApplier;
    }

    // Public Methods

    /**
     * Get the list of megawidget specifiers.
     * 
     * @return List of megawidget specifiers.
     */
    public final ImmutableList<ISpecifier> getSpecifiers() {
        return specifiers;
    }

    /**
     * Get the current time provider.
     * 
     * @return Current time provider.
     */
    public final ICurrentTimeProvider getCurrentTimeProvider() {
        return currentTimeProvider;
    }

    /**
     * Get the side effects applier, if any.
     * 
     * @return Side effects applier for these megawidgets, or <code>null</code>
     *         if there is none.
     */
    public final ISideEffectsApplier getSideEffectsApplier() {
        return sideEffectsApplier;
    }

    /**
     * Get the starting states of all state identifiers of all stateful
     * megawidget specifiers, including those that are descendants of other
     * megawidgets, using the values in the specified map where possible. Any
     * megawidget identifier that implements {@link IStatefulSpecifier} is
     * considered stateful.
     * 
     * @param map
     *            Map from which to take starting states where possible, and to
     *            be modified with the corrected starting states where a state
     *            is missing or incorrect.
     */
    public final void populateWithStartingStates(Map<String, Object> map) {
        for (ISpecifier specifier : specifiers) {
            populateWithStartingStates(specifier, map);
        }
    }

    // Private Methods

    /**
     * Ensure that the provided megawidget specifier and any child specifiers it
     * has have identifiers that are unique with respect to one another and are
     * not found in the provided set of identifiers, and if they are indeed
     * unique, add them to the identifiers set.
     * 
     * @param specifier
     *            Megawidget specifier to be checked.
     * @param identifiers
     *            Set of megawidget specifier identifiers collected so far.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    @SuppressWarnings("unchecked")
    private boolean ensureMegawidgetIdentifiersAreUnique(ISpecifier specifier,
            Set<String> identifiers) throws MegawidgetSpecificationException {

        /*
         * Ensure that this specifier's identifier is unique.
         */
        if (identifiers.contains(specifier.getIdentifier())) {
            throw new MegawidgetSpecificationException(
                    specifier.getIdentifier(), specifier.getType(), null, null,
                    "duplicate " + MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER
                            + " value");
        }
        identifiers.add(specifier.getIdentifier());

        /*
         * If this specifier is a container, recursively check all its
         * children's identifiers.
         */
        if (specifier instanceof IParentSpecifier) {
            for (ISpecifier childSpecifier : ((IParentSpecifier<? extends ISpecifier>) specifier)
                    .getChildMegawidgetSpecifiers()) {
                if (ensureMegawidgetIdentifiersAreUnique(childSpecifier,
                        identifiers) == false) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get the starting states of all state identifiers of the specified
     * specifier and any descendants that are stateful, using the values in the
     * specified map where possible. Any megawidget identifier that implements
     * {@link IStatefulSpecifier} is considered stateful.
     * 
     * @param specifier
     *            Specifier from which to take values to populate the map, and
     *            against which to check values found in the map.
     * @param map
     *            Map from which to take starting states where possible, and to
     *            be modified with the corrected starting states where a state
     *            is missing or incorrect.
     */
    @SuppressWarnings("unchecked")
    private void populateWithStartingStates(ISpecifier specifier,
            Map<String, Object> map) {

        /*
         * If the specifier is stateful, iterate through its state identifiers,
         * saving the starting state for each if one is provided.
         */
        if (specifier instanceof IStatefulSpecifier) {
            IStatefulSpecifier statefulSpecifier = (IStatefulSpecifier) specifier;
            statefulSpecifier.validateAndCorrectStates(map);
        }

        /*
         * If the specifier is a parent, do the same for its children.
         */
        if (specifier instanceof IParentSpecifier) {
            for (ISpecifier childSpecifier : ((IParentSpecifier<ISpecifier>) specifier)
                    .getChildMegawidgetSpecifiers()) {
                populateWithStartingStates(childSpecifier, map);
            }
        }
    }
}