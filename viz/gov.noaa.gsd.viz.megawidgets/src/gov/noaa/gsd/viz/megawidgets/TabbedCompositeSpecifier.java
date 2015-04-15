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

import java.util.Map;

/**
 * Tabbed panel megawidget specifier, used to construct a megawidget containing
 * tabs, each tab having its own page of megawidgets that are displayed when its
 * tab is brought to the front.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 21, 2013    2168    Chris.Golden      Changed to implement IControlSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Jun 17, 2014    3982    Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Oct 20, 2014    4818    Chris.Golden      Added option of providing a scrollable
 *                                           panel for each page of child
 *                                           megawidgets.
 * Apr 14, 2015    6935    Chris.Golden      Added visible page name parameter.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TabbedCompositeMegawidget
 */
public class TabbedCompositeSpecifier extends MultiPageMegawidgetSpecifier
        implements IPotentiallyScrollableContainerSpecifier<IControlSpecifier> {

    // Public Static Constants

    /**
     * Visible page parameter name; each expand bar may contain a reference to a
     * string associated with this name. The provided string must be one of page
     * names specified within the {@link #MEGAWIDGET_PAGES} parameter. If
     * specified, the page so specified will start off as visible (in front of
     * all other pages).
     */
    public static final String MEGAWIDGET_VISIBLE_PAGE = "visiblePage";

    // Private Variables

    /**
     * Visible page name.
     */
    private final String visiblePageName;

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the client area is to be scrollable.
     */
    private final boolean scrollable;

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
    public TabbedCompositeSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters);
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the scrollable flag, if present, is valid.
         */
        scrollable = ConversionUtilities.getSpecifierBooleanValueFromObject(
                getIdentifier(), getType(), parameters.get(SCROLLABLE),
                SCROLLABLE, false);

        /*
         * Ensure that the visible page, if specified, is valid.
         */
        try {
            visiblePageName = getVisiblePageName(parameters
                    .get(MEGAWIDGET_VISIBLE_PAGE));
        } catch (MegawidgetException e) {
            throw new MegawidgetSpecificationException(MEGAWIDGET_VISIBLE_PAGE,
                    e);
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

    @Override
    public final boolean isScrollable() {
        return scrollable;
    }

    /**
     * Get the name of the page that is to start off as visible.
     * 
     * @return Page name.
     */
    public final String getVisiblePageName() {
        return visiblePageName;
    }

    /**
     * Check the specified string to ensure it is a valid page name, and return
     * it if it is. If no name is specified, the first page is returned.
     * <p>
     * TODO: Better to have the page names, visible page name, etc. be checked
     * by a validator object, but no time to write one now.
     * 
     * @param name
     *            Visible page name.
     * @return Visible page name, assuming it is valid.
     * @throws MegawidgetException
     *             If the name was invalid.
     */
    public final String getVisiblePageName(Object name)
            throws MegawidgetException {
        String visiblePageName;
        try {
            visiblePageName = (String) name;
        } catch (Exception e) {
            throw createBadPageNameMegawidgetException(name);
        }
        if ((visiblePageName != null) && (visiblePageName.isEmpty() == false)) {
            if (getPageNames().contains(visiblePageName) == false) {
                throw createBadPageNameMegawidgetException(name);
            }
        } else {
            visiblePageName = getPageNames().get(0);
        }
        return visiblePageName;
    }

    // Private Methods

    /**
     * Build and return a megawidget exception indicating that a page name was
     * improperly specified.
     * 
     * @param badName
     *            Object that is not a properly specified page name and thus
     *            triggered the problem for which the exception is to be thrown.
     * @return Megawidget exception.
     */
    private MegawidgetException createBadPageNameMegawidgetException(
            Object badName) {
        return new MegawidgetException(getIdentifier(), getType(), badName,
                "must be page name");
    }
}
