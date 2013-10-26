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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Multi-page megawidget specifier base class, from which specific types of
 * container megawidget specifiers that have multiple pages or panels of child
 * megawidgets may be derived.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Changed to work with new
 *                                           child specifier manager.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ContainerMegawidget
 */
public abstract class MultiPageMegawidgetSpecifier extends
        ContainerMegawidgetSpecifier<IControlSpecifier> {

    // Public Static Constants

    /**
     * Pages parameter name; each multi-page megawidget must contain a reference
     * to a <code>List</code> object associated with this name. The provided
     * list must contain zero or more <code>Map</code> objects, each one holding
     * an entry for <code>PAGE_NAME</code>, and an entry for
     * <code>PAGE_FIELDS</code>, and optionally, an entry for
     * <code>MEGAWIDGET_COLUMN_COUNT</code>.
     */
    public static final String MEGAWIDGET_PAGES = "pages";

    /**
     * Page name parameter name; each map in the list associated with
     * <code>MEGAWIDGET_PAGES</code> must contain a reference to a string
     * associated with this name. The string serves to label and identify the
     * page.
     */
    public static final String PAGE_IDENTIFIER = "pageName";

    /**
     * Page fields parameter name; each map in the list associated with
     * <code>MEGAWIDGET_PAGES</code> must contain a reference to a
     * <code>List</code> object associated with this name. The provided list
     * must contain zero or more child megawidget specifier parameter maps, each
     * in the form of a <code>Map</code>, from which a megawidget specifier will
     * be constructed.
     */
    public static final String PAGE_FIELDS = "pageFields";

    /**
     * Page column count parameter name; each map in the list associated with
     * <code>MEGAWIDGET_PAGES</code> may contain a reference to a positive
     * integer associated with this name. The integer indicates the number of
     * columns that this page provides in which its children may lay themselves
     * out. If not specified, the default is 1.
     */
    public static final String PAGE_COLUMN_COUNT = "numColumns";

    // Private Variables

    /**
     * List of page names.
     */
    private final List<String> pageNames;

    /**
     * Hash table pairing page names with their column counts.
     */
    private final Map<String, Integer> columnCountsForPages = Maps.newHashMap();

    /**
     * Hash table pairing page names with lists of their child megawidget
     * specifiers.
     */
    private final Map<String, List<IControlSpecifier>> childrenForPages = Maps
            .newHashMap();

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this notifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public MultiPageMegawidgetSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(IControlSpecifier.class, parameters);

        // Ensure that the page list is acceptable, and store it.
        List<?> pagesObject = null;
        List<String> pageNames = Lists.newArrayList();
        try {
            pagesObject = (List<?>) parameters.get(MEGAWIDGET_PAGES);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_PAGES,
                    parameters.get(MEGAWIDGET_PAGES),
                    "must be list of page specifiers");
        }
        if (pagesObject == null) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_PAGES, null, null);
        }
        for (int j = 0; j < pagesObject.size(); j++) {
            Object object = pagesObject.get(j);
            try {

                // Ensure that the page identifier is acceptable, and
                // store it.
                Map<?, ?> pageObject = (Map<?, ?>) object;
                String pageName = null;
                try {
                    pageName = (String) pageObject.get(PAGE_IDENTIFIER);
                } catch (Exception e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_IDENTIFIER,
                            pageObject.get(PAGE_IDENTIFIER), "must be string");
                }
                if (pageName == null) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_IDENTIFIER, null, null);
                }

                // Ensure that the column count, if present, is acceptable,
                // and store it.
                int columnCount = getSpecifierIntegerValueFromObject(
                        pageObject.get(PAGE_COLUMN_COUNT), PAGE_COLUMN_COUNT, 1);
                if (columnCount < 1) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_COLUMN_COUNT, columnCount,
                            "must be positive integer");
                }

                // Ensure that the list of children is acceptable and store
                // it.
                List<?> childrenObject = null;
                try {
                    childrenObject = (List<?>) pageObject.get(PAGE_FIELDS);
                } catch (Exception e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_FIELDS,
                            pageObject.get(PAGE_FIELDS),
                            "must be list of child megawidget specifiers");
                }
                if (childrenObject == null) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_FIELDS, null, null);
                }
                List<IControlSpecifier> children = null;
                try {
                    children = getChildManager().createMegawidgetSpecifiers(
                            childrenObject, columnCount);
                } catch (MegawidgetSpecificationException e) {
                    throw new MegawidgetSpecificationException(getIdentifier(),
                            getType(), PAGE_FIELDS, childrenObject,
                            "bad child megawidget specifier", e);
                }

                // Add the child megawidget specifiers for this page to the
                // list of all children, and remember the parameters for
                // this page.
                getChildManager().addChildMegawidgetSpecifiers(children);
                pageNames.add(pageName);
                columnCountsForPages.put(pageName, columnCount);
                childrenForPages.put(pageName, children);
            } catch (MegawidgetSpecificationException e) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_PAGES + "[" + j + "]",
                        pagesObject, "bad list of page specifiers", e);
            }
        }
        this.pageNames = ImmutableList.copyOf(pageNames);
    }

    // Public Methods

    /**
     * Get the list of page names.
     * 
     * @return List of page names.
     */
    public final List<String> getPageNames() {
        return pageNames;
    }

    /**
     * Get the column count for the specified page.
     * 
     * @param pageName
     *            Name of the page.
     * @return Column count.
     */
    public final int getColumnCountForPage(String pageName) {
        return columnCountsForPages.get(pageName);
    }

    /**
     * Get the child megawidget specifiers for the specified page.
     * 
     * @param pageName
     *            Name of the page.
     * @return Child megawidget specifiers.
     */
    public final List<IControlSpecifier> getChildSpecifiersForPage(
            String pageName) {
        return childrenForPages.get(pageName);
    }
}
