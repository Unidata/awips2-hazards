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
import gov.noaa.gsd.viz.megawidgets.displaysettings.SelectableMultiPageScrollSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.ImmutableMap;

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
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to keep children synced
 *                                           with enabled and editable state.
 * Oct 20, 2014    4818    Chris.Golden      Added option of providing
 *                                           scrollable panels for each page of
 *                                           child megawidgets. Also added use
 *                                           of display settings, allowing the
 *                                           saving and restoring of scroll
 *                                           origins and of the currently
 *                                           selected tab.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TabbedCompositeSpecifier
 */
public class TabbedCompositeMegawidget extends ContainerMegawidget implements
        IResizer {

    // Private Variables

    /**
     * Tab folder.
     */
    private final CTabFolder tabFolder;

    /**
     * Map pairing tab page identifiers with their associated scrolled
     * composites; if <code>null</code>, the megawidget is not scrollable.
     */
    private final Map<String, ScrolledComposite> scrolledCompositesForTabPages;

    /**
     * Display settings.
     */
    private final SelectableMultiPageScrollSettings<Point, Integer> displaySettings = new SelectableMultiPageScrollSettings<>(
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
    protected TabbedCompositeMegawidget(TabbedCompositeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier);

        /*
         * Create the tab folder to put the tabs in, and grid it.
         */
        tabFolder = new CTabFolder(parent, SWT.NONE);
        tabFolder.setBorderVisible(true);
        tabFolder.setTabHeight(tabFolder.getTabHeight() + 8);
        tabFolder.setEnabled(specifier.isEnabled());
        gridContainerPanel(tabFolder);
        setComposite(tabFolder);

        /*
         * Respond to the tab folder selection changing by tracking the change
         * in the display settings, and by setting the scroll origin for the
         * newly visible tab page.
         */
        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int selectedIndex = tabFolder.getSelectionIndex();
                displaySettings.setSelection(selectedIndex);
                if (selectedIndex != -1) {
                    String tabName = (String) tabFolder.getItem(
                            tabFolder.getSelectionIndex()).getData();
                    Point origin = displaySettings
                            .getScrollOriginForPage(tabName);
                    if (origin != null) {
                        scrolledCompositesForTabPages.get(tabName).setOrigin(
                                origin);
                    }
                }
            }
        });

        /*
         * Create the map of tab pages to their scrolled composites if
         * appropriate.
         */
        Map<String, ScrolledComposite> scrolledCompositesForTabPages = (specifier
                .isScrollable() ? new HashMap<String, ScrolledComposite>(
                specifier.getPageNames().size(), 1.0f) : null);

        /*
         * Iterate through the tabs, creating for each a page with its child
         * megawidgets, and assigning each to the corresponding tab.
         */
        List<IControl> allChildren = new ArrayList<>();
        for (String pageName : specifier.getPageNames()) {

            /*
             * Create the tab page.
             */
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
            tabItem.setText(" " + pageName + " ");
            tabItem.setData(pageName);
            Composite tabPage;

            /*
             * Create a scrolled composite for the tab page if the megawidget is
             * to be scrollable, or a standard composite if not. If the former,
             * use a copy of the creation-time parameters, because the building
             * of the scrolled composite will change the map it is given to have
             * a new resize listener.
             */
            final ScrolledComposite scrolledComposite;
            Map<String, Object> pageParamMap;
            if (specifier.isScrollable()) {
                pageParamMap = new HashMap<>(paramMap);
                scrolledComposite = UiBuilder.buildScrolledComposite(this,
                        tabFolder, displaySettings, pageName, pageParamMap);
                tabItem.setControl(scrolledComposite);
                tabPage = (Composite) scrolledComposite.getContent();
                scrolledCompositesForTabPages.put(pageName, scrolledComposite);
            } else {
                pageParamMap = paramMap;
                tabPage = new Composite(tabFolder, SWT.NONE);
                tabItem.setControl(tabPage);
                scrolledComposite = null;
            }

            /*
             * Create the children and remember them.
             */
            List<IControl> children = createChildMegawidgets(tabPage,
                    specifier.getColumnCountForPage(pageName),
                    specifier.isEnabled(), specifier.isEditable(),
                    specifier.getChildSpecifiersForPage(pageName), pageParamMap);
            allChildren.addAll(children);

            /*
             * Update the scrolled client area's size, if the megawidget is
             * scrollable.
             */
            if (scrolledComposite != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UiBuilder.updateScrolledAreaSize(scrolledComposite);
                    }
                });
            }
        }
        setChildren(allChildren);

        /*
         * Remember the scrolled composites for the tab pages if the megawidget
         * is scrollable.
         */
        this.scrolledCompositesForTabPages = (specifier.isScrollable() ? ImmutableMap
                .copyOf(scrolledCompositesForTabPages) : null);

        /*
         * Select the first tab.
         */
        tabFolder.setSelection(0);
    }

    // Public Methods

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @Override
    public void setDisplaySettings(final IDisplaySettings displaySettings) {
        Display.getDefault().asyncExec(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                if ((displaySettings.getMegawidgetClass() == TabbedCompositeMegawidget.this
                        .getClass())
                        && (displaySettings instanceof SelectableMultiPageScrollSettings)) {
                    Integer selectedIndex = ((SelectableMultiPageScrollSettings<Point, Integer>) displaySettings)
                            .getSelection();
                    if ((tabFolder.isDisposed() == false)
                            && ((selectedIndex == null) || (tabFolder
                                    .getItemCount() > selectedIndex))) {
                        tabFolder.setSelection(selectedIndex == null ? -1
                                : selectedIndex);
                        TabbedCompositeMegawidget.this.displaySettings
                                .setSelection(selectedIndex);
                    }
                    if (scrolledCompositesForTabPages != null) {
                        Map<String, Point> scrollOriginsForTabPages = ((SelectableMultiPageScrollSettings<Point, Integer>) displaySettings)
                                .getScrollOriginsForPages();
                        for (Map.Entry<String, Point> entry : scrollOriginsForTabPages
                                .entrySet()) {
                            ScrolledComposite scrolledComposite = scrolledCompositesForTabPages
                                    .get(entry.getKey());
                            if ((scrolledComposite != null)
                                    && (scrolledComposite.isDisposed() == false)) {
                                scrolledComposite.setOrigin(entry.getValue());
                                TabbedCompositeMegawidget.this.displaySettings
                                        .setScrollOriginForPage(entry.getKey(),
                                                entry.getValue());
                            }
                        }
                    }
                }
            }
        });
    }
}
