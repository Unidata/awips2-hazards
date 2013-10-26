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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;

/**
 * Tabbed composite megawidget, a megawidget that contains tabs, each associated
 * with a page of child megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names, and to use new IControl
 *                                           interface.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TabbedCompositeSpecifier
 */
public class TabbedCompositeMegawidget extends ContainerMegawidget {

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
    protected TabbedCompositeMegawidget(TabbedCompositeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier);

        // Create the tab folder to put the tabs in, and
        // grid it.
        CTabFolder tabFolder = new CTabFolder(parent, SWT.NONE);
        tabFolder.setBorderVisible(true);
        tabFolder.setTabHeight(tabFolder.getTabHeight() + 8);
        tabFolder.setEnabled(specifier.isEnabled());
        gridContainerPanel(tabFolder);
        composite = tabFolder;

        // Iterate through the tabs, creating for each a page
        // with its child megawidgets, and assigning each to the
        // corresponding tab.
        List<IControl> allChildren = Lists.newArrayList();
        for (String pageName : specifier.getPageNames()) {
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
            tabItem.setText(" " + pageName + " ");
            Composite tabPage = new Composite(tabFolder, SWT.NONE);
            tabItem.setControl(tabPage);
            List<IControl> children = createChildMegawidgets(tabPage,
                    specifier.getColumnCountForPage(pageName),
                    specifier.getChildSpecifiersForPage(pageName), paramMap);
            allChildren.addAll(children);
        }
        this.children = allChildren;

        // Select the first tab.
        tabFolder.setSelection(0);
    }
}
