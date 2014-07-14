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
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see ExpandBarSpecifier
 */
public class ExpandBarMegawidget extends ContainerMegawidget implements
        IResizer {

    // Private Variables

    /**
     * Grid layout data of the expand bar.
     */
    private final GridData gridData;

    /**
     * Resize listener.
     */
    private final IResizeListener resizeListener;

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
        ExpandBar expandBar = new ExpandBar(parent, SWT.NONE);
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
        gridData.minimumHeight += expandItem.getControl().computeSize(
                SWT.DEFAULT, SWT.DEFAULT).y
                * (visible ? 1 : -1);
        gridData.heightHint = gridData.minimumHeight;
        if (resizeListener != null) {
            resizeListener.sizeChanged(this);
        }
    }
}
