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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Expand bar megawidget specifier, used to construct a megawidget containing
 * expand bars, each such bar having its own page of megawidgets that are
 * displayed when its bar is expanded.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 20, 2014    4010    Chris.Golden Initial creation.
 * Apr 14, 2015    6935    Chris.Golden Added visible page names parameter.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ExpandBarMegawidget
 */
public class ExpandBarSpecifier extends MultiPageMegawidgetSpecifier {

    // Public Static Constants

    /**
     * Visible pages parameter name; each expand bar may contain a reference to
     * a {@link List} object associated with this name. The provided list must
     * contain zero or more page names, with each such name being one of the
     * names specified within the {@link #MEGAWIDGET_PAGES} parameter. If
     * specified, the pages so specified will start off as visible (expanded).
     */
    public static final String MEGAWIDGET_EXPANDED_PAGES = "expandedPages";

    // Private Variables

    /**
     * List of visible page names.
     */
    private final List<String> expandedPageNames;

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

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
    public ExpandBarSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the list of expanded pages, if specified, is valid.
         */
        try {
            expandedPageNames = ImmutableList
                    .copyOf(getVisiblePageNames(parameters
                            .get(MEGAWIDGET_EXPANDED_PAGES)));
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(
                    MEGAWIDGET_EXPANDED_PAGES, e);
        }
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    /**
     * Get the list of names of pages that are to start off as expanded.
     * 
     * @return List of page names.
     */
    public final List<String> getExpandedPageNames() {
        return expandedPageNames;
    }

    /**
     * Check the specified collection of visible page names to ensure it is
     * valid, and return a list of these names if it is.
     * <p>
     * TODO: Better to have the page names, visible page names, etc. be checked
     * by a validator object, but no time to write one now.
     * 
     * @param collection
     *            Collection of visible page names.
     * @return List of page names, assuming the collection was valid.
     * @throws MegawidgetException
     *             If the collection was invalid.
     */
    public final List<String> getVisiblePageNames(Object collection)
            throws MegawidgetException {
        Collection<?> expandedPagesObject = null;
        List<String> expandedPages = null;
        try {
            expandedPagesObject = (Collection<?>) collection;
            if (expandedPagesObject != null) {
                expandedPages = new ArrayList<>(expandedPagesObject.size());
                for (Object label : expandedPagesObject) {
                    expandedPages.add((String) label);
                }
            } else {
                expandedPages = Collections.emptyList();
            }
        } catch (Exception e) {
            throw createBadPageNamesMegawidgetException(collection);
        }
        if (getPageNames().containsAll(expandedPages) == false) {
            throw createBadPageNamesMegawidgetException(collection);
        }
        return expandedPages;
    }

    // Private Methods

    /**
     * Build and return a megawidget exception indicating that a list of page
     * names was improperly specified.
     * 
     * @param badList
     *            Object that is not a properly specified list of page names and
     *            thus triggered the problem for which the exception is to be
     *            thrown.
     * @return Megawidget exception.
     */
    private MegawidgetException createBadPageNamesMegawidgetException(
            Object badList) {
        return new MegawidgetException(getIdentifier(), getType(), badList,
                "must be list of page names");
    }
}
