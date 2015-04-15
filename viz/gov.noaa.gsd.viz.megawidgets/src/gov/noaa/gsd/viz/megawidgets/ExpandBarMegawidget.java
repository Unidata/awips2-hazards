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

import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.megawidgets.displaysettings.MultiSelectSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import com.google.common.collect.ImmutableSet;

/**
 * Expand bar megawidget, a megawidget that contains bars, each with an
 * associated page of child megawidgets. The bars may be expanded to show their
 * pages.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 20, 2014    4010    Chris.Golden Initial creation.
 * Oct 20, 2014    4818    Chris.Golden Added use of display settings, allowing
 *                                      the saving and restoring of expansion
 *                                      state for pages.
 * Apr 14, 2015    6935    Chris.Golden Added visible page names mutable property.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ExpandBarSpecifier
 */
public class ExpandBarMegawidget extends ContainerMegawidget implements
        IResizer {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                ContainerMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(ExpandBarSpecifier.MEGAWIDGET_EXPANDED_PAGES);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Variables

    /**
     * Expand bar.
     */
    private final ExpandBar expandBar;

    /**
     * Grid layout data of the expand bar.
     */
    private final GridData gridData;

    /**
     * Resize listener.
     */
    private final IResizeListener resizeListener;

    /**
     * Display settings.
     */
    private final MultiSelectSettings<String> displaySettings = new MultiSelectSettings<>(
            getClass());

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected ExpandBarMegawidget(ExpandBarSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier);
        resizeListener = (IResizeListener) paramMap.get(RESIZE_LISTENER);

        /*
         * Create the expand bar to put the pages in, and grid it.
         */
        expandBar = new ExpandBar(parent, SWT.NONE);
        expandBar.setEnabled(specifier.isEnabled());
        gridContainerPanel(expandBar);
        setComposite(expandBar);
        int baseWidth = expandBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

        /*
         * Iterate through the pages, creating for each an expand item with its
         * child megawidgets. Remember the largest width and height required to
         * show any of the pages.
         */
        int largestWidth = 0, largestHeight = 0;
        List<IControl> allChildren = new ArrayList<>();
        for (String pageName : specifier.getPageNames()) {
            ExpandItem expandItem = new ExpandItem(expandBar, SWT.NONE);
            expandItem.setText(pageName);
            Composite expandPage = new Composite(expandBar, SWT.NONE);
            expandItem.setControl(expandPage);
            List<IControl> children = createChildMegawidgets(expandPage,
                    specifier.getColumnCountForPage(pageName),
                    specifier.isEnabled(), specifier.isEditable(),
                    specifier.getChildSpecifiersForPage(pageName), paramMap);
            allChildren.addAll(children);
            Point size = expandPage.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            if (size.x > largestWidth) {
                largestWidth = size.x;
            }
            if (size.y > largestHeight) {
                largestHeight = size.y;
            }
            expandItem.setHeight(size.y);
        }
        setChildren(allChildren);

        /*
         * Ensure that the megawidget has appropriate sizing.
         */
        Point size = expandBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        gridData = (GridData) expandBar.getLayoutData();
        gridData.minimumWidth = (size.x < largestWidth + baseWidth ? largestWidth
                + baseWidth
                : size.x);
        gridData.minimumHeight = gridData.heightHint = size.y;

        /*
         * Set up a listener to respond to expansion and collapse of items by
         * recalculating the size required and notifying any listener.
         */
        expandBar.addExpandListener(new ExpandListener() {

            @Override
            public void itemCollapsed(ExpandEvent e) {
                itemVisibilityChanged((ExpandItem) e.item, false);
            }

            @Override
            public void itemExpanded(ExpandEvent e) {
                itemVisibilityChanged((ExpandItem) e.item, true);
            }
        });

        /*
         * Expand any pages that are to start off visible.
         */
        List<String> expandedPageNames = specifier.getExpandedPageNames();
        Set<String> expanded = new HashSet<>(expandedPageNames.size(), 1.0f);
        for (ExpandItem item : expandBar.getItems()) {
            if (specifier.getExpandedPageNames().contains(item.getText())) {
                expanded.add(item.getText());
                item.setExpanded(true);
                recalculateHeight(item, true);
            }
        }
        displaySettings.clearSelectedItems();
        displaySettings.addSelectedItems(expanded);
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (ExpandBarSpecifier.MEGAWIDGET_EXPANDED_PAGES.equals(name)) {
            return new ArrayList<String>(displaySettings.getSelectedItems());
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (ExpandBarSpecifier.MEGAWIDGET_EXPANDED_PAGES.equals(name)) {
            try {
                setExpandedPages(((ExpandBarSpecifier) getSpecifier())
                        .getVisiblePageNames(value));
            } catch (MegawidgetException e) {
                throw new MegawidgetPropertyException(
                        ExpandBarSpecifier.MEGAWIDGET_EXPANDED_PAGES, e);
            }
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDisplaySettings(IDisplaySettings displaySettings) {
        if ((displaySettings.getMegawidgetClass() == getClass())
                && (displaySettings instanceof MultiSelectSettings)
                && (expandBar.isDisposed() == false)) {
            setExpandedPages(((MultiSelectSettings<String>) displaySettings)
                    .getSelectedItems());
        }
    }

    // Private Methods

    /**
     * Respond to an item's page's expansion or collapse.
     * 
     * @param expandItem
     *            Item whose page was expanded or collapsed.
     * @param visible
     *            Flag indicating whether or not the page is now visible.
     */
    private void itemVisibilityChanged(ExpandItem expandItem, boolean visible) {
        recalculateHeight(expandItem, visible);
        if (visible) {
            displaySettings.addSelectedItem(expandItem.getText());
        } else {
            displaySettings.removeSelectedItem(expandItem.getText());
        }
        if (resizeListener != null) {
            resizeListener.sizeChanged(this);
        }
    }

    /**
     * Change the expanded pages to those specified.
     * 
     * @param expandedPageNames
     *            Names of pages to be expanded.
     */
    private void setExpandedPages(Collection<String> expandedPageNames) {
        boolean changed = false;
        for (ExpandItem item : expandBar.getItems()) {
            boolean wasVisible = item.getExpanded();
            item.setExpanded(expandedPageNames.contains(item.getText()));
            if (wasVisible != item.getExpanded()) {
                recalculateHeight(item, !wasVisible);
                changed = true;
            }
        }
        displaySettings.clearSelectedItems();
        displaySettings.addSelectedItems(expandedPageNames);
        if (changed && (resizeListener != null)) {
            resizeListener.sizeChanged(this);
        }
    }

    /**
     * Recalculate the height given the specified item's page's expansion or
     * collapse.
     * 
     * @param expandItem
     *            Item whose page was expanded or collapsed.
     * @param visible
     *            Flag indicating whether or not the page is now visible.
     */
    private void recalculateHeight(ExpandItem expandItem, boolean visible) {
        gridData.minimumHeight += expandItem.getControl().computeSize(
                SWT.DEFAULT, SWT.DEFAULT).y
                * (visible ? 1 : -1);
        gridData.heightHint = gridData.minimumHeight;
    }
}
