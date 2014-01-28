/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.*;
import gov.noaa.gsd.viz.hazards.display.DockTrackingViewPart;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.megawidgets.ControlComponentHelper;
import gov.noaa.gsd.viz.megawidgets.ControlSpecifierOptionsManager;
import gov.noaa.gsd.viz.megawidgets.HierarchicalChoicesTreeSpecifier;
import gov.noaa.gsd.viz.megawidgets.IControl;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IExplicitCommitStateful;
import gov.noaa.gsd.viz.megawidgets.IMegawidget;
import gov.noaa.gsd.viz.megawidgets.INotificationListener;
import gov.noaa.gsd.viz.megawidgets.INotifier;
import gov.noaa.gsd.viz.megawidgets.IParent;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.IStateChangeListener;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierFactory;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.megawidgets.StatefulMegawidget;
import gov.noaa.gsd.viz.megawidgets.StatefulMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeScaleMegawidget;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.viz.core.icon.IconUtil;
import com.raytheon.viz.ui.dialogs.ModeListener;

/**
 * This class represents an instance of the Hazard Detail view part, which is
 * the primary interface the user uses for entering information pertinent to the
 * event being created or modified. It also allows the user to preview the
 * event, propose it or issue it.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 10, 2013            Chris.Golden      Initial creation
 * Jun 04, 2013            Chris.Golden      Added support for changing background
 *                                           and foreground colors in order to stay
 *                                           in synch with CAVE mode.
 * Jun 25, 2013            Chris.Golden      Added functionality that uses the default
 *                                           value for any metadata megawidget as the
 *                                           value for any identifier that has no
 *                                           entry yet in the event dictionary.
 * Jul 12, 2013    585     Chris.Golden      Changed to support loading from bundle.
 * Jul 31, 2013   1298     Chris.Golden      Fixed problem with metadata values for
 *                                           hazard event being incorrectly carried
 *                                           over to a new hazard type when that event
 *                                           was assigned said new type, leading to
 *                                           exceptions.
 * Aug 22, 2013   1921     Bryon.Lawrence    Added a check for whether or not HID actions
 *                                           should be fired in the setMegawidgetsStates()
 *                                           method. This was resulting in a HID action
 *                                           message being sent when the HID was updated
 *                                           due to a model state change.
 * Sep 11, 2013   1298     Chris.Golden      Fixed bugs causing time scale megawidgets to
 *                                           not show the correct visible time range.
 *                                           Added code to give good default values for
 *                                           time scale megawidgets. Replaced erroneous
 *                                           references (variable names, comments, etc.)
 *                                           to "widget" with "megawidget" to avoid
 *                                           confusion.
 * Nov 04, 2013   2182     daniel.s.schaffer Started refactoring
 * Nov 04, 2013   2336     Chris.Golden      Added implementation of new superclass-
 *                                           specified abstract method for table mega-
 *                                           widget specifier. Also fixed bug that caused
 *                                           detail fields for megawidgets to be ignored
 *                                           when getting or setting hazard event
 *                                           parameters.
 * Nov 14, 2013   1463     Bryon.Lawrence    Added code to support hazard conflict
 *                                           detection.
 * Nov 16, 2013   2166     daniel.s.schaffer Some tidying
 * Dec 16, 2013   2545     Chris.Golden      Added current time provider for megawidget
 *                                           use.
 * Jan 14, 2014   2704     Chris.Golden      Removed conflict label and replaced it with
 *                                           coloring of all tabs if there is a conflict
 *                                           in at least one tab, together with placing
 *                                           an icon in each tab that conflicts. This had
 *                                           the side effect of fixing the visual glitch
 *                                           whereby the bottom border of the time range
 *                                           group was not displayed at certain times.
 * Jan 27, 2014   2155     Chris.Golden      Fixed bug that intermittently occurred be-
 *                                           cause an asynchronous execution of code that
 *                                           expected non-disposed widgets encountered
 *                                           widgets that had been disposed between the
 *                                           scheduling and running of the aysnchronous
 *                                           code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailViewPart extends DockTrackingViewPart implements
        INotificationListener, IStateChangeListener {

    // Public Static Constants

    /**
     * Identifier of the view.
     */
    public static final String ID = "gov.noaa.gsd.viz.hazards.hazarddetail.view";

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardDetailViewPart.class);

    /**
     * Points table megawidget identifier, a special string used as an
     * identifier for a points metadata table megawidget, if one is showing.
     */
    private static final String POINTS_TABLE_IDENTIFIER = "__pointsTable__";

    /**
     * Start time state identifier.
     */
    private static final String START_TIME_STATE = "__startTime__";

    /**
     * End time state identifier.
     */
    private static final String END_TIME_STATE = "__endTime__";

    /**
     * Points table name column identifier.
     */
    private static final String POINTS_TABLE_NAME_COLUMN_IDENTIFIER = "name";

    /**
     * Event time range megawidget identifier, a special string used as an
     * identifier for the time range megawidget.
     */
    private static final String TIME_RANGE_IDENTIFIER = START_TIME_STATE + ":"
            + END_TIME_STATE;

    /**
     * Propose button identifier.
     */
    private static final int PROPOSE_ID = 1;

    /**
     * Issue button identifier.
     */
    private static final int ISSUE_ID = 2;

    /**
     * Preview button identifier.
     */
    private static final int PREVIEW_ID = 3;

    /**
     * Hazard type section text.
     */
    private static final String HAZARD_TYPE_SECTION_TEXT = "Hazard Type";

    /**
     * Hazard category text.
     */
    private static final String HAZARD_CATEGORY_TEXT = "Hazard Category:";

    /**
     * Hazard category text.
     */
    private static final String HAZARD_TYPE_TEXT = "Hazard Type:";

    /**
     * Hazard time range section text.
     */
    private static final String TIME_RANGE_SECTION_TEXT = "Time Range";

    /**
     * Start time text.
     */
    private static final String START_TIME_TEXT = "Start Time:";

    /**
     * End time text.
     */
    private static final String END_TIME_TEXT = "End Time:";

    /**
     * Preview button text.
     */
    private static final String PREVIEW_BUTTON_TEXT = "  Preview...  ";

    /**
     * Propose button text.
     */
    private static final String PROPOSE_BUTTON_TEXT = "  Propose  ";

    /**
     * Issue button text.
     */
    private static final String ISSUE_BUTTON_TEXT = "  Issue...  ";

    /**
     * Preview button tooltip text.
     */
    private static final String PREVIEW_BUTTON_TOOLTIP_TEXT = "Preview the text product";

    /**
     * Propose button tooltip text.
     */
    private static final String PROPOSE_BUTTON_TOOLTIP_TEXT = "Propose the event";

    /**
     * Issue button tooltip text.
     */
    private static final String ISSUE_BUTTON_TOOLTIP_TEXT = "Issue the event";

    /**
     * Details section text.
     */
    private static final String DETAILS_SECTION_TEXT = "Details";

    /**
     * Area details tab text.
     */
    private static final String AREA_DETAILS_TAB_TEXT = " Area Details ";

    /**
     * Points details tab text.
     */
    private static final String POINTS_DETAILS_TAB_TEXT = "Points Details";

    /**
     * Conflict image icon file name.
     */
    private static final String CONFLICT_ICON_IMAGE_FILE_NAME = "hidConflict.png";

    /**
     * Conflict tooltip.
     */
    private static final String CONFLICT_TOOLTIP = "Conflicts with other hazard(s)";

    // Private Classes

    /**
     * Points table megawidget.
     * 
     * @see PointsTableSpecifier
     */
    private class PointsTableMegawidget extends StatefulMegawidget implements
            IControl {

        // Private Variables

        /**
         * Component associated with this megawidget.
         */
        private Table table = null;

        /**
         * Control component helper.
         */
        private final ControlComponentHelper helper;

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
        protected PointsTableMegawidget(PointsTableSpecifier specifier,
                Composite parent, Map<String, Object> paramMap) {
            super(specifier, paramMap);
            helper = new ControlComponentHelper(specifier);

            // Create a composite in which the table resides;
            // this is done because the table needs to be inside
            // a composite with nothing else in it, so that it
            // can use a table column layout (which is, rather
            // strangely, assigned as the layout for the parent
            // of the table, not the table itself).
            Composite subParent = new Composite(parent, SWT.NONE);
            TableColumnLayout layout = new TableColumnLayout();
            subParent.setLayout(layout);
            GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
            gridData.heightHint = 85;
            gridData.horizontalSpan = specifier.getWidth();
            gridData.verticalIndent = specifier.getSpacing();
            subParent.setLayoutData(gridData);

            // Create a table to hold the metadata of the indi-
            // vidual points.
            Table table = new Table(subParent, SWT.BORDER | SWT.FULL_SELECTION);
            table.setLinesVisible(true);
            table.setHeaderVisible(true);
            configurePointsTable(table, layout);
            table.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updatePointMegawidgetValues((Table) e.widget);
                }
            });
            table.setEnabled(specifier.isEnabled());

            // Remember the table component.
            this.table = table;

            // Render the table uneditable if necessary.
            if (isEditable() == false) {
                doSetEditable(false);
            }
        }

        // Public Methods

        @Override
        public final boolean isEditable() {
            return helper.isEditable();
        }

        @Override
        public final void setEditable(boolean editable) {
            helper.setEditable(editable);
            doSetEditable(editable);
        }

        @Override
        public final int getLeftDecorationWidth() {
            return 0;
        }

        @Override
        public final void setLeftDecorationWidth(int width) {

            // No action.
        }

        @Override
        public final int getRightDecorationWidth() {
            return 0;
        }

        @Override
        public final void setRightDecorationWidth(int width) {

            // No action.
        }

        /**
         * Populate the table.
         */
        public final void populate() {
            populatePointsTable(table);
        }

        /**
         * Update the table to match the megawidgets.
         */
        public final void update() {
            updatePointsTable(table);
        }

        /**
         * Get the selected row index, if any.
         * 
         * @return Selected row index, or <code>-1</code> if none is currently
         *         selected.
         */
        public final int getSelectedRowIndex() {
            return table.getSelectionIndex();
        }

        // Protected Methods

        /**
         * Change the component widgets to ensure their state matches that of
         * the enabled flag.
         * 
         * @param enable
         *            Flag indicating whether the component widgets are to be
         *            enabled or disabled.
         */
        @Override
        protected final void doSetEnabled(boolean enable) {
            table.setEnabled(enable);
        }

        /**
         * Get the current state for the specified identifier. This method is
         * called by <code>
         * getState()</code> only after the latter has ensured that the supplied
         * state identifier is valid.
         * 
         * @param identifier
         *            Identifier for which state is desired. Implementations may
         *            assume that the state identifier supplied by this
         *            parameter is valid for this megawidget.
         * @return Object making up the current state for the specified
         *         identifier.
         */
        @Override
        protected final Object doGetState(String identifier) {

            // Create a hash table of hash tables, with
            // the keys of the main hash table being the
            // point identifiers of any points included
            // in the current state, and the values be-
            // ing the hash table pairing that point's
            // field names and values.
            Map<String, Map<String, Object>> state = Maps.newHashMap();

            // Iterate through the lines, adding points
            // to the list.
            for (int line = 0; line < table.getItemCount(); line++) {

                // Create the hash table mapping point
                // field names to their values, and then
                // iterate through the columns, adding
                // all these pairings to the table.
                TableItem item = table.getItem(line);
                Map<String, Object> map = Maps.newHashMap();
                String name = null;
                for (int col = 0; col < table.getColumnCount(); col++) {
                    String columnIdentifier = (String) table.getColumn(col)
                            .getData();
                    Object value = item.getData(columnIdentifier);
                    if (columnIdentifier
                            .equals(POINTS_TABLE_NAME_COLUMN_IDENTIFIER)) {
                        name = (String) value;
                    }
                    if (value != null) {
                        map.put(columnIdentifier, value);
                    }
                }

                // Add the hash table to the list.
                state.put(name, map);
            }

            // Return the resulting state.
            return state;
        }

        /**
         * Set the current state for the specified identifier. This method is
         * called by <code> setState()</code> only after the latter has ensured
         * that the supplied state identifier is valid, and has set a flag that
         * indicates that this setting of the state will not trigger the
         * megawidget to notify its listener of an invocation.
         * 
         * @param identifier
         *            Identifier for which state is to be set. Implementations
         *            may assume that the state identifier supplied by this
         *            parameter is valid for this megawidget.
         * @param state
         *            Object making up the state to be used for this identifier,
         *            or <code>null</code> if this state should be reset.
         */
        @Override
        protected final void doSetState(String identifier, Object state) {
            if (state == null) {
                for (TableItem item : table.getItems()) {
                    item.dispose();
                }
                table.clearAll();
            } else {
                throw new UnsupportedOperationException();
            }
        }

        /**
         * Get a shortened description of the specified state for the specified
         * identifier. This method is called by <code>getStateDescription() only
         * after the latter has ensured that the supplied
         * state identifier is valid.
         * 
         * @param identifier
         *            Identifier to which the state would be assigned.
         *            Implementations may assume that the state identifier
         *            supplied by this parameter is valid for this megawidget.
         * @param state
         *            State for which to generate a shortened description.
         * @return Description of the specified state.
         * @throws MegawidgetStateException
         *             If the specified state is not of a valid type for this
         *             <code>StatefulMegawidget</code> implementation.
         */
        @Override
        protected final String doGetStateDescription(String identifier,
                Object state) throws MegawidgetStateException {

            // Not implemented.
            throw new UnsupportedOperationException();
        }

        // Private Methods

        /**
         * Change the component widgets to ensure their state matches that of
         * the editable flag.
         * 
         * @param editable
         *            Flag indicating whether the component widgets are to be
         *            editable or read-only.
         */
        private void doSetEditable(boolean editable) {
            table.setBackground(helper
                    .getBackgroundColor(editable, table, null));
        }
    }

    /**
     * Points table megawidget specifier. Unlike other megawidget specifiers,
     * this one is only specified internally within the enclosing class, never
     * by configuration files.
     * 
     * @see PointsTableMegawidget
     */
    private class PointsTableSpecifier extends StatefulMegawidgetSpecifier
            implements IControlSpecifier {

        // Private Variables

        /**
         * Control options manager.
         */
        private final ControlSpecifierOptionsManager optionsManager;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param parameters
         *            Map containing the parameters to be used to construct the
         *            megawidget specifier.
         * @throws MegawidgetSpecificationException
         *             If the megawidget specifier parameters are invalid.
         */
        public PointsTableSpecifier(Map<String, Object> parameters)
                throws MegawidgetSpecificationException {
            super(parameters);
            optionsManager = new ControlSpecifierOptionsManager(this,
                    parameters,
                    ControlSpecifierOptionsManager.BooleanSource.TRUE);
        }

        // Public Methods

        /**
         * Get the flag indicating whether or not the megawidget is to be
         * created in an editable state.
         * 
         * @return True if the megawidget is to be created as editable, false
         *         otherwise.
         */
        @Override
        public final boolean isEditable() {
            return optionsManager.isEditable();
        }

        /**
         * Get the width of the megawidget in columns within its parent.
         * 
         * @return Number of columns it should span.
         */
        @Override
        public final int getWidth() {
            return optionsManager.getWidth();
        }

        /**
         * Determine whether or not the megawidget fills the width of the column
         * it is occupying within its parent. This may be used by parent
         * megawidgets to determine whether their children may be laid out side
         * by side in the same column or not.
         * 
         * @return True if the megawidget fills the width of the column it
         *         occupies, false otherwise.
         */
        @Override
        public final boolean isFullWidthOfColumn() {
            return optionsManager.isFullWidthOfColumn();
        }

        /**
         * Get the spacing between this megawidget and the one above it in
         * pixels.
         * 
         * @return Spacing.
         */
        @Override
        public final int getSpacing() {
            return optionsManager.getSpacing();
        }

        /**
         * Create the GUI components making up the specified megawidget. This
         * subclass overrides this method because the reflection used in the
         * superclass to find the megawidget class will not work with inner
         * classes.
         * 
         * @param parent
         *            Parent in which to place the megawidget; for this
         *            implementation, this must be a <code>Composite</code>.
         * @param creationParams
         *            Hash table mapping identifiers to values that subclasses
         *            might require when creating a megawidget. This class looks
         *            for a value associated with the key <code>
         *            NOTIFICATION_LISTENER</code> that is of type <code>
         *            INotificationListener</code>; if not found, then the
         *            created megawidget will not attempt to notify when it is
         *            invoked. Furthermore, it looks for a value associated with
         *            the key <code>STATE_CHANGED_LISTENER</code> that is of
         *            type <code>IStateChangeListener</code>; if not found, then
         *            the created megawidget will not attempt to notify of state
         *            changes.
         * @return Created megawidget.
         * @throws MegawidgetException
         *             If an error occurs while creating or initializing the
         *             megawidget.
         */
        @SuppressWarnings("unchecked")
        @Override
        public <P extends Widget, M extends IMegawidget> M createMegawidget(
                P parent, Class<M> superClass,
                Map<String, Object> creationParams) throws MegawidgetException {

            // Return the created megawidget.
            return (M) new PointsTableMegawidget(this, (Composite) parent,
                    megawidgetCreationParams);
        }

        // Protected Methods

        @Override
        protected final Set<Class<?>> getClassesOfState() {
            Set<Class<?>> classes = Sets.newHashSet();
            classes.add(Object.class);
            return classes;
        }
    }

    // Private Constants

    /**
     * Conflict tab image icon.
     */
    private final Image CONFLICT_TAB_ICON = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            CONFLICT_ICON_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Conflict unselected tab background color.
     */
    private final Color CONFLICT_UNSELECTED_TAB_COLOR = new Color(
            Display.getCurrent(), 237, 185, 144);

    /**
     * Conflict selected tab background color.
     */
    private final Color CONFLICT_SELECTED_TAB_COLOR = new Color(
            Display.getCurrent(), 237, 218, 208);

    // Private Variables

    /**
     * Set of basic resources created for use in this window, to be disposed of
     * when this window is disposed of.
     */
    private final Set<Resource> resources = Sets.newHashSet(CONFLICT_TAB_ICON,
            CONFLICT_UNSELECTED_TAB_COLOR, CONFLICT_SELECTED_TAB_COLOR);

    /**
     * Tab folder holding the different hazard events, one per tab page.
     */
    private CTabFolder eventTabFolder = null;

    /**
     * Standard unselected tab background color for the event tab folder.
     */
    private Color standardUnselectedColor;

    /**
     * Standard selected tab background color for the event tab folder.
     */
    private Color standardSelectedColor;

    /**
     * Index into hazard information array indicating which element has its tab
     * currently showing in the tab folder.
     */
    private int visibleHazardIndex = 0;

    /**
     * Flag indicating whether or not tab pages are currently being created or
     * deleted.
     */
    private boolean tabsBeingChanged = false;

    /**
     * Composite holding the contents of a tab page. The different pages all use
     * the same composite as their controls, with the composite's contents
     * changing each time a new tab page is selected.
     */
    private Composite top = null;

    /**
     * Hazard category combo box.
     */
    private Combo categoryCombo = null;

    /**
     * Hazard type combo box.
     */
    private Combo typeCombo = null;

    /**
     * Minimum visible time in the time range.
     */
    private long minimumVisibleTime = HazardConstants.MIN_TIME;

    /**
     * Maximum visible time in the time range.
     */
    private long maximumVisibleTime = HazardConstants.MAX_TIME;

    /**
     * Megawidget specifier factory.
     */
    private final MegawidgetSpecifierFactory megawidgetSpecifierFactory = new MegawidgetSpecifierFactory();

    /**
     * Event time range megawidget.
     */
    private TimeScaleMegawidget timeRangeMegawidget = null;

    /**
     * Set of all time scale megawidgets currently in existence.
     */
    private final Set<TimeScaleMegawidget> timeScaleMegawidgets = Sets
            .newHashSet();

    /**
     * Current time provider.
     */
    private final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    /**
     * Content panel that holds whichever metadata panel is currently displayed.
     */
    private Composite metadataContentPanel = null;

    /**
     * Layout manager for the <code>metadataContentPanel</code>.
     */
    private StackLayout metadataContentLayout = null;

    /**
     * Base tab page height, calculated at part content creation time and used
     * when determining how large to make the scrolled composite holding the tab
     * page contents.
     */
    private int baseTabPageHeight = 0;

    /**
     * List of hazard category identifiers.
     */
    private final List<String> categories = Lists.newArrayList();

    /**
     * Hash table pairing categories with lists of hazard type identifiers.
     */
    private final Map<String, List<String>> typesForCategories = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with categories.
     */
    private final Map<String, String> categoriesForTypes = Maps.newHashMap();

    /**
     * Hash table pairing megawidget creation time parameter identifiers with
     * their corresponding values.
     */
    private final Map<String, Object> megawidgetCreationParams = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with lists of the associated megawidget
     * specifiers.
     */
    private final Map<String, List<ISpecifier>> megawidgetsForTypes = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with lists of the associated megawidget
     * specifiers for the individual points of the hazard.
     */
    private final Map<String, List<ISpecifier>> pointMegawidgetsForTypes = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with metadata panels used by those types.
     */
    private final Map<String, Composite> panelsForTypes = Maps.newHashMap();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * metadata megawidget identifiers with their associated megawidgets.
     */
    private final Map<String, Map<String, IControl>> megawidgetsForIdsForTypes = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * point-specific metadata megawidget identifiers with their associated
     * megawidgets.
     */
    private final Map<String, Map<String, IControl>> pointMegawidgetsForIdsForTypes = Maps
            .newHashMap();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * point-specific metadata state identifiers with their associated
     * megawidgets.
     */
    private final Map<String, Map<String, IControl>> pointMegawidgetsForStateIdsForTypes = Maps
            .newHashMap();

    /**
     * List of event dictionaries for the primary shape(s) in the events, each
     * dictionary being in the form of a hash table that maps identifier keys to
     * values that are to be used for various parameters of the hazard.
     */
    private final List<Map<String, Object>> primaryParamValues = Lists
            .newArrayList();

    /**
     * List of lists of event dictionaries for the auxiliary shapes in the
     * events, if any, each of which is a hash table mapping identifier keys to
     * values that are to be used for various parameters of the hazard.
     */
    private final List<List<Map<String, Object>>> auxiliaryParamValues = Lists
            .newArrayList();

    /**
     * List of lists of event dictionaries for the points in the events, if any,
     * each of which is a hash table mapping identifier keys to values that are
     * to be used for various parameters of the individual points of the hazard.
     */
    private final List<List<Map<String, Object>>> pointsParamValues = Lists
            .newArrayList();

    /**
     * Map of event identifiers displayed in the HID and lists of conflicting
     * events.
     */
    private Map<String, Collection<IHazardEvent>> eventConflictMap = Maps
            .newHashMap();

    /**
     * Propose button.
     */
    private Button proposeButton = null;

    /**
     * Issue button.
     */
    private Button issueButton = null;

    /**
     * Preview button.
     */
    private Button previewButton = null;

    /**
     * Scrolled composite.
     */
    private ScrolledComposite scrolledComposite = null;

    /**
     * Map of hazard event identifiers to the scrolled composite origins; the
     * latter are recorded each time the scrolled composite is scrolled.
     */
    private final Map<String, Point> scrollOriginsForEventIDs = Maps
            .newHashMap();

    /**
     * Flag indicating whether or not the scrolled composite is having its
     * contents changed.
     */
    private boolean scrolledCompositeContentsChanging = false;

    /**
     * Flag indicating whether or not the scrolled composite is having its size
     * changed.
     */
    private boolean scrolledCompositePageIncrementChanging = false;

    /**
     * Flag indicating whether or not the scrolled composite has had its size
     * calculated at least once.
     */
    private boolean scrolledCompositeSizeCalculated = false;

    /**
     * Hazard detail view that is managing this part.
     */
    private HazardDetailView hazardDetailView = null;

    // Public Methods

    /**
     * Initialize the view part.
     * 
     * @param hazardDetailView
     *            View managing this view part.
     * @param generalWidgets
     *            JSON specifying the general widgets that the view part must
     *            contain.
     * @param hazardMegawidgets
     *            JSON specifying the widgets that the view part must contain
     *            for each of the hazards that the general widgets allow the
     *            user to select.
     * @param minVisibleTime
     *            Minimum visible time.
     * @param maxVisibleTime
     *            Maximum visible time.
     */
    public void initialize(HazardDetailView hazardDetailView,
            String generalWidgets, String hazardMegawidgets,
            long minVisibleTime, long maxVisibleTime) {

        // Remember the minimum and maximum visible times.
        minimumVisibleTime = minVisibleTime;
        maximumVisibleTime = maxVisibleTime;
        this.hazardDetailView = hazardDetailView;

        // Fill in the megawidget creation parameters
        // hash table, used to provide parameters
        // to megawidgets created via megawidget speci-
        // fiers at the megawidgets' creation time.
        // Depending upon how this part has been
        // instantiated, this invocation may occur be-
        // fore or after createPartControl() has been
        // called, so this is done here to ensure that
        // either way, the creation parameters are
        // filled in properly.
        initializeMegawidgetCreationParams();

        // Iterate through the time scale megawidgets, changing
        // their visible time ranges to match the current
        // range.
        for (TimeScaleMegawidget megawidget : timeScaleMegawidgets) {
            megawidget.setVisibleTimeRange(minimumVisibleTime,
                    maximumVisibleTime);
        }

        // Parse the strings into a JSON object and a JSON array.
        Dict jsonGeneralWidgets = null;
        DictList jsonHazardMegawidgets = null;
        try {
            jsonGeneralWidgets = Dict.getInstance(generalWidgets);
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.initialize(): Error "
                    + "parsing JSON for general megawidgets.", e);
        }
        try {
            jsonHazardMegawidgets = DictList.getInstance(hazardMegawidgets);
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.initialize(): Error "
                    + "parsing JSON for hazard megawidgets.", e);
        }

        // Get the list of event categories, and the list of event
        // types going with each category, and the category going
        // with each set of event types.
        categories.clear();
        typesForCategories.clear();
        categoriesForTypes.clear();
        for (Object item : (List<?>) jsonGeneralWidgets
                .get(HazardConstants.HAZARD_INFO_GENERAL_CONFIG_WIDGETS)) {

            // Get the category.
            Dict jsonItem = (Dict) item;
            String category = jsonItem
                    .getDynamicallyTypedValue(HierarchicalChoicesTreeSpecifier.CHOICE_NAME);
            categories.add(category);

            // Get the types that go with this category. Each JSON
            // array will contain dictionaries which in turn hold
            // strings as one of their values naming the types; the
            // resulting list must then be alphabetized.
            List<String> types = Lists.newArrayList();
            for (Object child : (List<?>) jsonItem
                    .get(HierarchicalChoicesTreeSpecifier.CHOICE_CHILDREN)) {
                types.add((String) ((Map<?, ?>) child)
                        .get(HierarchicalChoicesTreeSpecifier.CHOICE_NAME));
            }
            Collections.sort(types);

            // Remember the association of the category and the
            // types, placing an empty string type at the head of
            // the types list before saving it, so that each cate-
            // gory has an empty type with no associated metadata.
            for (String type : types) {
                categoriesForTypes.put(type, category);
            }
            types.add(0, "");
            typesForCategories.put(category, types);
        }

        // If the categories combo box is not yet populated, do
        // it now.
        if ((categoryCombo != null) && (categoryCombo.getItemCount() == 0)) {
            for (String category : categories) {
                categoryCombo.add(category);
            }
        }

        // Get the megawidget specifier lists for the event types.
        megawidgetsForTypes.clear();
        pointMegawidgetsForTypes.clear();
        for (int j = 0; j < jsonHazardMegawidgets.size(); j++) {

            // Get the event types to which this list of megawidget
            // specifiers applies, ignoring any that already
            // have an associated megawidget specifier list due to
            // a previous iteration. This last part is neces-
            // sary because an event type might appear in more
            // than one set of event types (each set being
            // associated with a list of megawidget specifiers), in
            // which case the first set in which it appears is
            // considered to be the set in which it actually
            // belongs.
            Dict jsonItem = jsonHazardMegawidgets.getDynamicallyTypedValue(j);
            List<String> types = Lists.newArrayList();
            for (Object child : (List<?>) jsonItem
                    .get(HazardConstants.HAZARD_INFO_METADATA_TYPES)) {
                types.add((String) child);
            }
            Set<String> typesSet = Sets.newHashSet();
            for (String type : types) {
                if (megawidgetsForTypes.containsKey(type) == false) {
                    typesSet.add(type);
                }
            }

            // If after dropping any event types that have
            // already been taken care of in previous itera-
            // tions there is still a non-empty set of event
            // types left, put together any megawidget specifiers
            // for this set.
            if (typesSet.isEmpty() == false) {

                // Create the megawidget specifiers for the event
                // type as a whole.
                List<ISpecifier> megawidgetSpecifiers = Lists.newArrayList();
                List<Dict> objects = getJsonObjectList(jsonItem
                        .get(HazardConstants.HAZARD_INFO_METADATA_MEGAWIDGETS_LIST));
                try {
                    for (Dict object : objects) {
                        megawidgetSpecifiers.add(megawidgetSpecifierFactory
                                .createMegawidgetSpecifier(
                                        IControlSpecifier.class, object));
                    }
                } catch (Exception e) {
                    statusHandler.error("HazardDetailViewPart.initialize(): "
                            + "Error parsing JSON for hazard megawidgets.", e);
                }
                for (String type : typesSet) {
                    megawidgetsForTypes.put(type, megawidgetSpecifiers);
                }

                // Create the megawidget specifiers for the event
                // type's individual points, if any. Any
                // megawidget specifiers for points must be pre-
                // ceded by a points table megawidget specifier,
                // which is synthesized here and placed at
                // the head of the list. The megawidget immedi-
                // ately following the table is padded so
                // that it does not get too far into the
                // table's personal space.
                Object pointObject = jsonItem
                        .get(HazardConstants.HAZARD_INFO_METADATA_MEGAWIDGETS_POINTS_LIST);
                if (pointObject != null) {
                    megawidgetSpecifiers = Lists.newArrayList();
                    objects = getJsonObjectList(pointObject);
                    Dict tableObject = new Dict();
                    tableObject.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                            POINTS_TABLE_IDENTIFIER);
                    tableObject.put(IControlSpecifier.MEGAWIDGET_SPACING, 5);
                    try {
                        megawidgetSpecifiers.add(new PointsTableSpecifier(
                                tableObject));
                        boolean first = true;
                        for (Dict object : objects) {
                            if (first) {
                                first = false;
                                int spacing = (object
                                        .get(IControlSpecifier.MEGAWIDGET_SPACING) == null ? 0
                                        : ((Number) object
                                                .get(IControlSpecifier.MEGAWIDGET_SPACING))
                                                .intValue());
                                if (spacing < 10) {
                                    object.put(
                                            IControlSpecifier.MEGAWIDGET_SPACING,
                                            10);
                                }
                            }
                            megawidgetSpecifiers.add(megawidgetSpecifierFactory
                                    .createMegawidgetSpecifier(
                                            IControlSpecifier.class, object));
                        }
                    } catch (Exception e) {
                        statusHandler
                                .error("HazardDetailViewPart.initialize(): "
                                        + "Error parsing JSON for hazard point megawidgets.",
                                        e);
                    }
                    for (String type : typesSet) {
                        pointMegawidgetsForTypes
                                .put(type, megawidgetSpecifiers);
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        for (Resource resource : resources) {
            resource.dispose();
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        // Create a CAVE mode listener, which will
        // set the foreground and background colors
        // appropriately according to the CAVE mode
        // whenever a paint event occurs.
        new ModeListener(parent);

        // Configure the parent layout.
        parent.setLayout(new GridLayout(1, false));

        // Fill in the megawidget creation parameters
        // hash table, used to provide parameters
        // to megawidgets created via megawidget speci-
        // fiers at the megawidgets' creation time.
        // Depending upon how this part has been
        // instantiated, this invocation may occur be-
        // fore or after initialize() has been called,
        // so this is done here to ensure that either
        // way, the creation parameters are filled in
        // properly.
        initializeMegawidgetCreationParams();

        // Create the main panel of the view part.
        Composite tabTop = new Composite(parent, SWT.NONE);
        tabTop.setLayout(new FillLayout());
        tabTop.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create the tab folder that will hold
        // the hazard event(s), and configure it
        // to manually respond to tab selections
        // since it actually uses only one com-
        // posite as the control for all its
        // tabs, and simply reconfigures the
        // megawidgets therein for each different
        // tab selection.
        eventTabFolder = new CTabFolder(tabTop, SWT.TOP);
        standardUnselectedColor = eventTabFolder.getBackground();
        standardSelectedColor = eventTabFolder.getSelectionBackground();
        eventTabFolder.setBorderVisible(true);
        eventTabFolder.setTabHeight(eventTabFolder.getTabHeight() + 8);
        eventTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                // Do nothing if in the middle of
                // tab manipulation already.
                if (tabsBeingChanged) {
                    return;
                }

                // Occasionally, an event is ge-
                // nerated from a tab which is not
                // the currently selected tab, so
                // ensure that the tab reporting
                // the event is indeed selected.
                CTabItem item = eventTabFolder.getSelection();
                if ((CTabItem) e.item == item) {
                    visibleHazardIndex = eventTabFolder
                            .indexOf((CTabItem) e.item);
                    synchWithEventInfo();
                }
            }
        });
        scrolledComposite = new ScrolledComposite(eventTabFolder, SWT.H_SCROLL
                | SWT.V_SCROLL);

        // Create the composite that will be used as
        // the control for every event tab page.
        top = new Composite(scrolledComposite, SWT.NONE);

        // If there are events already, iterate
        // through them, creating a tab for each.
        tabsBeingChanged = true;
        for (Map<String, Object> eventDict : primaryParamValues) {
            CTabItem tabItem = new CTabItem(eventTabFolder, SWT.NONE);
            setTabText(tabItem,
                    (String) eventDict.get(HAZARD_EVENT_IDENTIFIER),
                    (String) eventDict.get(HAZARD_EVENT_FULL_TYPE));
            tabItem.setData(eventDict.get(HAZARD_EVENT_IDENTIFIER));
        }
        tabsBeingChanged = false;

        // Create the layout for the main panel.
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginRight = 5;
        mainLayout.marginLeft = 5;
        mainLayout.marginBottom = 5;
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        top.setLayout(mainLayout);

        // Add the margins and spacing within the
        // main panel to the base tab page height
        // tracker.
        baseTabPageHeight = (mainLayout.marginHeight * 2)
                + mainLayout.marginBottom + mainLayout.marginTop
                + (mainLayout.verticalSpacing * 2);

        Group hazardGroup = new Group(top, SWT.NONE);
        hazardGroup.setText(HAZARD_TYPE_SECTION_TEXT);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = 426;
        hazardGroup.setLayoutData(gridData);
        hazardGroup.setLayout(new FillLayout());
        Composite hazardSubGroup = new Composite(hazardGroup, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        hazardSubGroup.setLayout(gridLayout);

        // Create the hazard category combo box.
        Label categoryLabel = new Label(hazardSubGroup, SWT.NONE);
        categoryLabel.setText(HAZARD_CATEGORY_TEXT);
        gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        categoryLabel.setLayoutData(gridData);
        categoryCombo = new Combo(hazardSubGroup, SWT.READ_ONLY);
        categoryCombo.removeAll();
        for (String category : categories) {
            categoryCombo.add(category);
        }
        if (primaryParamValues.size() > 0) {
            categoryCombo.setText(getCategoryOfCurrentEvent());
        }
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        categoryCombo.setLayoutData(gridData);

        // Create the hazard type combo box.
        Label label = new Label(hazardSubGroup, SWT.NONE);
        label.setText(HAZARD_TYPE_TEXT);
        gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        label.setLayoutData(gridData);
        typeCombo = new Combo(hazardSubGroup, SWT.READ_ONLY);
        if (primaryParamValues.size() > 0) {
            populateHazardTypesList((String) primaryParamValues.get(
                    visibleHazardIndex).get(HAZARD_EVENT_CATEGORY));
            if (primaryParamValues.get(visibleHazardIndex).get(
                    HAZARD_EVENT_FULL_TYPE) != null) {
                typeCombo.setText((String) primaryParamValues.get(
                        visibleHazardIndex).get(HAZARD_EVENT_FULL_TYPE));
            } else {
                typeCombo.setText("");
            }
        }
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        typeCombo.setLayoutData(gridData);

        // Add the hazard category/type group to
        // the base tab page height tracker.
        baseTabPageHeight += hazardGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        // Create the time range panel.
        Group timeRangePanel = new Group(top, SWT.NONE);
        timeRangePanel.setText(TIME_RANGE_SECTION_TEXT);
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        timeRangePanel.setLayoutData(gridData);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        timeRangePanel.setLayout(gridLayout);

        // Create the time range sliders.
        try {
            Dict scaleObject = new Dict();
            scaleObject.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                    TIME_RANGE_IDENTIFIER);
            Map<String, String> stateLabels = Maps.newHashMap();
            stateLabels.put(START_TIME_STATE, START_TIME_TEXT);
            stateLabels.put(END_TIME_STATE, END_TIME_TEXT);
            scaleObject.put(TimeScaleSpecifier.MEGAWIDGET_STATE_LABELS,
                    stateLabels);
            scaleObject.put(IControlSpecifier.MEGAWIDGET_SPACING, 5);
            timeRangeMegawidget = new TimeScaleSpecifier(scaleObject)
                    .createMegawidget(timeRangePanel,
                            TimeScaleMegawidget.class, megawidgetCreationParams);
            timeScaleMegawidgets.add(timeRangeMegawidget);
            ControlComponentHelper
                    .alignMegawidgetsElements(timeScaleMegawidgets);
            if (primaryParamValues.size() > 0) {
                timeRangeMegawidget.setUncommittedState(
                        START_TIME_STATE,
                        primaryParamValues.get(visibleHazardIndex).get(
                                HAZARD_EVENT_START_TIME));
                timeRangeMegawidget.setUncommittedState(
                        END_TIME_STATE,
                        primaryParamValues.get(visibleHazardIndex).get(
                                HAZARD_EVENT_END_TIME));
                timeRangeMegawidget.commitStateChanges();
            }
        } catch (Exception e) {
            statusHandler
                    .error("HazardDetailViewPart.createPartControl(): Exception "
                            + "during time range megawidget creation; should not occur.",
                            e);
        }

        // Ensure that the first column of the time range
        // panel has the same width as the first column
        // of the previous panel, for visual consistency's
        // sake.
        GridData timeLabelGridData = (GridData) timeRangePanel.getChildren()[0]
                .getLayoutData();
        timeLabelGridData.widthHint = categoryLabel.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, false).x;

        // Add the hazard category/type group to
        // the base tab page height tracker.
        baseTabPageHeight += timeRangePanel.computeSize(SWT.DEFAULT,
                SWT.DEFAULT).y;

        // Create the metadata options panel, which will
        // hold the actual metadata panel that is appro-
        // priate for the currently selected hazard type.
        metadataContentPanel = new Composite(top, SWT.NONE);
        GridData contentPanelGridData = new GridData(SWT.FILL, SWT.FILL, true,
                true);
        metadataContentPanel.setLayoutData(contentPanelGridData);
        metadataContentLayout = new StackLayout();
        metadataContentPanel.setLayout(metadataContentLayout);

        // Add a listener for hazard category changes which
        // alters the list of hazard types.
        categoryCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                populateHazardTypesList(categoryCombo.getText());
                primaryParamValues.get(visibleHazardIndex).put(
                        HAZARD_EVENT_CATEGORY, categoryCombo.getText());
                typeCombo.select(0);
                typeCombo.notifyListeners(SWT.Selection, new Event());
            }
        });

        // Clear the various data structures holding metadata
        // megawidget information, since any such megawidgets should
        // be recreated at this point.
        panelsForTypes.clear();
        megawidgetsForIdsForTypes.clear();
        pointMegawidgetsForIdsForTypes.clear();

        // Show the currently selected hazard type's metadata
        // panel.
        if (primaryParamValues.size() > 0) {
            showMetadataForType((String) primaryParamValues.get(
                    visibleHazardIndex).get(HAZARD_EVENT_FULL_TYPE));
        }

        // Add a listener for hazard type changes which shows
        // the metadata panel appropriate to the new hazard
        // type.
        typeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {

                // Update the records for this event of its
                // category and type.
                primaryParamValues.get(visibleHazardIndex).put(
                        HAZARD_EVENT_CATEGORY, categoryCombo.getText());
                primaryParamValues.get(visibleHazardIndex).put(
                        HAZARD_EVENT_TYPE, typeCombo.getText().split(" ")[0]);
                primaryParamValues.get(visibleHazardIndex).put(
                        HAZARD_EVENT_FULL_TYPE, typeCombo.getText());
                Dict eventInfo = new Dict();
                eventInfo.put(
                        HAZARD_EVENT_IDENTIFIER,
                        primaryParamValues.get(visibleHazardIndex).get(
                                HAZARD_EVENT_IDENTIFIER));
                eventInfo.put(HAZARD_EVENT_CATEGORY, categoryCombo.getText());
                eventInfo.put(HAZARD_EVENT_FULL_TYPE, typeCombo.getText());
                String jsonText = null;
                try {
                    jsonText = eventInfo.toJSONString();
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.createPartControl(): conversion "
                                    + "of event info to JSON string failed.", e);
                }

                // Send off the JSON to notify listeners of
                // the change.
                fireHIDAction(new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_EVENT_TYPE,
                        jsonText));
            }
        });

        // Ensure that this panel is told when the window
        // is being hidden, and notifies any listener that
        // it is being dismissed in response.
        top.getShell().addListener(SWT.Hide, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fireHIDAction(new HazardDetailAction(
                        HazardDetailAction.ActionType.DISMISS));
            }
        });

        // Configure the scrolled composite that will allow
        // each tab page to scroll if it gets too tall.
        scrolledComposite.setContent(top);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setShowFocusedControl(true);

        // Make the scrolled composite the control of each
        // tab page.
        CTabItem[] tabItems = eventTabFolder.getItems();
        for (CTabItem tabItem : tabItems) {
            tabItem.setControl(scrolledComposite);
        }

        // Pack the main tab folder.
        eventTabFolder.pack();

        // Add a listener to the vertical scroll bar of the
        // scrolled composite to record the origin each time
        // scrolling occurs.
        scrolledComposite.getVerticalBar().addSelectionListener(
                new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if ((scrolledCompositeContentsChanging == false)
                                && (visibleHazardIndex != -1)
                                && (visibleHazardIndex < primaryParamValues
                                        .size())) {
                            scrollOriginsForEventIDs.put(
                                    (String) primaryParamValues.get(
                                            visibleHazardIndex).get(
                                            HAZARD_EVENT_IDENTIFIER),
                                    scrolledComposite.getOrigin());
                        }
                    }
                });

        // Create the button bar below the tab composite.
        createButtonBar(parent);

        // Add a listener to the scrolled composite to make
        // it resize its page increment whenever it changes
        // size, and schedule a recalculation to happen when
        // the GUI has been completely constructed so that
        // the scrolled composite will know how large its
        // child widget is to be.
        scrolledComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                recalculateScrolledCompositePageIncrement();
            }
        });
        configureScrolledCompositeForSelectedEvent();
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                recalculateScrolledCompositePageIncrement();
            }
        });
    }

    @Override
    public void setFocus() {

        // No action.
    }

    @Override
    public void megawidgetInvoked(INotifier megawidget, String extraCallback) {
        /**
         * No action.
         */
    }

    @Override
    public void megawidgetStateChanged(IStateful megawidget, String identifier,
            Object state) {

        // If the megawidget that experienced a state
        // change is the time range megawidget, handle
        // it separately. If there are no event
        // dictionaries, do nothing.
        if (primaryParamValues.size() == 0) {
            return;
        } else if (megawidget == timeRangeMegawidget) {
            timeRangeChanged();
            return;
        }

        // Get the point megawidgets for the current type,
        // if any.
        boolean isPointMegawidget = false;
        Map<String, IControl> megawidgetsForIds = pointMegawidgetsForIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_FULL_TYPE));
        if (megawidgetsForIds != null) {

            // Determine whether this is a point
            // megawidget or an areal megawidget.
            for (IControl otherMegawidget : megawidgetsForIds.values()) {
                if (megawidget == otherMegawidget) {
                    isPointMegawidget = true;
                    break;
                }
            }

            // If this is a point megawidget, the table
            // needs updating.
            if (isPointMegawidget) {
                ((PointsTableMegawidget) megawidgetsForIds
                        .get(POINTS_TABLE_IDENTIFIER)).update();
            }
        }

        // If the megawidget is a point megawidget, then up-
        // date the value for the corresponding point;
        // otherwise, just update the main event's
        // corresponding value.
        String eventID;
        if (isPointMegawidget) {
            eventID = (String) pointsParamValues
                    .get(visibleHazardIndex)
                    .get(((PointsTableMegawidget) megawidgetsForIds
                            .get(POINTS_TABLE_IDENTIFIER))
                            .getSelectedRowIndex())
                    .get(HAZARD_EVENT_IDENTIFIER);
            pointsParamValues
                    .get(visibleHazardIndex)
                    .get(((PointsTableMegawidget) megawidgetsForIds
                            .get(POINTS_TABLE_IDENTIFIER))
                            .getSelectedRowIndex()).put(identifier, state);
        } else {
            eventID = (String) primaryParamValues.get(visibleHazardIndex).get(
                    HAZARD_EVENT_IDENTIFIER);
            primaryParamValues.get(visibleHazardIndex).put(identifier, state);
        }

        // Put together an action to be sent along to indicate
        // that the appropriate key's value has changed for this
        // event, and send it off.
        Dict eventInfo = new Dict();
        eventInfo.put(HAZARD_EVENT_IDENTIFIER, eventID);
        eventInfo.put(identifier, state);
        String jsonText = null;
        try {
            jsonText = eventInfo.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "HazardDetailViewPart.megawidgetStateChanged(): conversion "
                            + "of event info to JSON string failed.", e);
        }
        fireHIDAction(new HazardDetailAction(
                HazardDetailAction.ActionType.UPDATE_EVENT_METADATA, jsonText));
    }

    /**
     * Set the HID event info.
     * 
     * @param eventDictList
     *            List of dictionaries, each providing the key-value pairs that
     *            define a hazard event to be displayed.
     * @param eventConflictMap
     *            Map of selected events and corresponding lists of conflicting
     *            events.
     * @param topmostEvent
     *            Identifier of the event that should be brought to the top of
     *            the tab list, or <code>null</code> if it should not be
     *            changed.
     */
    public void setHidEventInfo(DictList eventDictList,
            Map<String, Collection<IHazardEvent>> eventConflictMap,
            String topmostEvent) {

        // If there are no event dictionaries, ensure that
        // a zero-length list of them exists at least.
        if (eventDictList == null) {
            eventDictList = new DictList();
        }

        // Parse the passed-in events and conflicts and
        // configure the megawidgets accordingly.
        List<String> eventIDs = Lists.newArrayList();
        List<String> types = Lists.newArrayList();
        try {

            // Determine whether the events should be
            // interpreted as a single primary and zero
            // or more point events, or as a list of
            // primary events.
            Map<?, ?> firstEvent = (Map<?, ?>) (eventDictList.size() > 0 ? eventDictList
                    .get(0) : null);
            boolean isArealAndPoints = ((firstEvent != null) && firstEvent
                    .containsKey(HAZARD_EVENT_GROUP_IDENTIFIER));

            // Clear the event tracking lists.
            primaryParamValues.clear();
            pointsParamValues.clear();
            auxiliaryParamValues.clear();
            this.eventConflictMap = eventConflictMap;

            // Iterate through the primary events, getting
            // the information for each in turn.
            for (int j = 0; j < (isArealAndPoints ? 1 : eventDictList.size()); j++) {

                // Add the primary event dictionary for
                // this event.
                Map<String, Object> eventDict = eventDictList
                        .getDynamicallyTypedValue(j);
                primaryParamValues.add(eventDict);

                // If the map does not include the hazard
                // type, set it to nothing; it shoul: d then
                // have a category listed, and if it does
                // not, complain.
                String fullType = (String) eventDict
                        .get(HAZARD_EVENT_FULL_TYPE);
                if ((fullType == null) || (fullType.length() == 0)) {
                    fullType = "";
                    String category = (String) eventDict
                            .get(HAZARD_EVENT_CATEGORY);
                    if (category == null) {
                        statusHandler
                                .warn("HazardDetailViewPart.setHidEventInfo(): "
                                        + "Problem: for empty hazard type, could "
                                        + "not find hazard category!");
                    } else {
                        categoriesForTypes.put("", category);
                    }
                    eventDict.put(HAZARD_EVENT_FULL_TYPE, fullType);
                }
                if (eventDict.get(HAZARD_EVENT_CATEGORY) == null) {
                    eventDict.put(HAZARD_EVENT_CATEGORY,
                            categoriesForTypes.get(fullType));
                }

                // Add this event identifier and type to the
                // lists of these respective parameters being
                // compiled.
                eventIDs.add((String) eventDict.get(HAZARD_EVENT_IDENTIFIER));
                types.add((String) eventDict.get(HAZARD_EVENT_FULL_TYPE));

                // Ensure that the start and end times are long
                // integer objects; they may be generic number ob-
                // jects due to JSON parsing.
                eventDict.put(HAZARD_EVENT_START_TIME, ((Number) eventDict
                        .get(HAZARD_EVENT_START_TIME)).longValue());
                eventDict.put(HAZARD_EVENT_END_TIME, ((Number) eventDict
                        .get(HAZARD_EVENT_END_TIME)).longValue());

                // If the passed-in event list is a single
                // areal event and zero or more point events,
                // add the point (and any other auxiliary)
                // events to the appropriate lists; other-
                // wise, add a null placeholder for each of
                // the appropriate lists.
                List<Map<String, Object>> thisPointsParamValues = null;
                List<Map<String, Object>> thisAuxiliaryParamValues = null;
                if (isArealAndPoints) {

                    // Determine whether or not points are
                    // to be kept separately so as to allow
                    // them to have metadata specified for
                    // them individually in the points table.
                    boolean keepPointsSeparate = pointMegawidgetsForTypes
                            .containsKey(fullType);
                    if (keepPointsSeparate) {
                        statusHandler
                                .debug("HazardDetailViewPart.setHidEventInfo(): "
                                        + "For hazard type of \""
                                        + fullType
                                        + "\", points (if found in list) should be "
                                        + "on separate tab.");
                    } else {
                        statusHandler
                                .debug("HazardDetailViewPart.setHidEventInfo(): "
                                        + "For hazard type of \""
                                        + fullType
                                        + "\", no points tabs should be used.");
                    }
                    for (int k = 1; k < eventDictList.size(); k++) {
                        Dict auxiliary = eventDictList
                                .getDynamicallyTypedValue(k);
                        List<?> shapeList = (List<?>) auxiliary
                                .get(HAZARD_EVENT_SHAPES);
                        boolean listAsPoint = false;
                        if (keepPointsSeparate && (shapeList != null)) {
                            Dict shape = (Dict) shapeList.get(0);
                            if (shape.get(HAZARD_EVENT_SHAPE_TYPE).equals(
                                    HAZARD_EVENT_SHAPE_TYPE_CIRCLE)
                                    || shape.get(HAZARD_EVENT_SHAPE_TYPE)
                                            .equals(GeometryType.POINT
                                                    .getValue())) {
                                listAsPoint = true;
                            }
                        }
                        if (listAsPoint) {
                            if (thisPointsParamValues == null) {
                                thisPointsParamValues = Lists.newArrayList();
                            }
                            thisPointsParamValues.add(auxiliary);
                        } else {
                            if (thisAuxiliaryParamValues == null) {
                                thisAuxiliaryParamValues = Lists.newArrayList();
                            }
                            thisAuxiliaryParamValues.add(auxiliary);
                        }
                    }
                }
                pointsParamValues.add(thisPointsParamValues);
                auxiliaryParamValues.add(thisAuxiliaryParamValues);
            }
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.setHidEventInfo():"
                    + "Problem reading event dictionaries.", e);
        }

        // If the event tab folder exists yet, recreate
        // the tabs if necessary, and bring the proper
        // one to the top.
        if (eventTabFolder != null) {

            // Turn tab redraw off.
            eventTabFolder.setRedraw(false);

            // Determine whether or not the tabs need to
            // be recreated.
            CTabItem[] items = eventTabFolder.getItems();
            boolean recreateTabs = (items.length != eventIDs.size());
            if (recreateTabs == false) {
                for (int j = 0; j < eventIDs.size(); j++) {
                    if (eventIDs.get(j).equals(items[j].getData()) == false) {
                        recreateTabs = true;
                        break;
                    }
                }
            }

            // If the tabs need to be recreated, destroy
            // the old ones and create new ones; otherwise
            // just set their text strings.
            if (recreateTabs) {
                tabsBeingChanged = true;
                scrolledCompositeContentsChanging = true;
                for (CTabItem item : items) {
                    item.dispose();
                }
                visibleHazardIndex = 0;
                for (int j = 0; j < eventIDs.size(); j++) {
                    String eventID = eventIDs.get(j);
                    CTabItem tabItem = new CTabItem(eventTabFolder, SWT.NONE);
                    setTabText(tabItem, eventID, types.get(j));
                    tabItem.setData(eventID);
                    tabItem.setControl(scrolledComposite);
                }
                scrolledCompositeContentsChanging = false;
                tabsBeingChanged = false;
            } else {
                for (int j = 0; j < eventIDs.size(); j++) {
                    setTabText(items[j], eventIDs.get(j), types.get(j));
                }
            }

            // Show any conflicts visually within the tab
            // folder.
            updateEventTabsToReflectConflicts();

            // Bring the tab to the top for the event that
            // belongs in the front.
            if (eventTabFolder.getItemCount() > 0) {
                CTabItem tabItemToSelect = null;
                if (topmostEvent != null) {
                    for (CTabItem tabItem : eventTabFolder.getItems()) {
                        if (tabItem.getData().equals(topmostEvent)) {
                            tabItemToSelect = tabItem;
                            break;
                        }
                    }
                } else if (eventTabFolder.getSelection() == null) {
                    tabItemToSelect = eventTabFolder.getItems()[0];
                }
                if (tabItemToSelect != null) {
                    visibleHazardIndex = eventTabFolder
                            .indexOf(tabItemToSelect);
                    eventTabFolder.setSelection(tabItemToSelect);
                }
            }
            synchWithEventInfo();

            // Turn redraw back on.
            eventTabFolder.setRedraw(true);
        }
    }

    /**
     * Get the number of events showing.
     * 
     * @return Number of events showing.
     */
    public int getEventCount() {
        return primaryParamValues.size();
    }

    /**
     * Update the specified event's time range. If the specified event is not
     * the one being viewed/edited, ignore this change.
     * 
     * @param minVisibleTime
     *            Minimum visible time.
     * @param maxVisibleTime
     *            Maximum visible time.
     */
    public void setVisibleTimeRange(long minVisibleTime, long maxVisibleTime) {
        minimumVisibleTime = minVisibleTime;
        maximumVisibleTime = maxVisibleTime;

        // Remember the new minimum and maximum visible times in
        // the megawidget creation parameters, so that any time
        // scale megawidgets created later have the correct visible
        // time ranges.
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minimumVisibleTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maximumVisibleTime);

        // Iterate through the time scale megawidgets, changing their
        // visible time ranges to match the new range.
        for (TimeScaleMegawidget megawidget : timeScaleMegawidgets) {
            megawidget.setVisibleTimeRange(minimumVisibleTime,
                    maximumVisibleTime);
        }
    }

    // Private Methods

    /**
     * Initialize the megawidget creation parameters map.
     */
    private void initializeMegawidgetCreationParams() {
        megawidgetCreationParams.put(INotifier.NOTIFICATION_LISTENER, this);
        megawidgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER, this);
        megawidgetCreationParams.put(TimeMegawidgetSpecifier.MINIMUM_TIME,
                HazardConstants.MIN_TIME);
        megawidgetCreationParams.put(TimeMegawidgetSpecifier.MAXIMUM_TIME,
                HazardConstants.MAX_TIME);
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minimumVisibleTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maximumVisibleTime);
        megawidgetCreationParams.put(
                TimeMegawidgetSpecifier.CURRENT_TIME_PROVIDER,
                currentTimeProvider);
    }

    /**
     * Create a button bar for the action buttons.
     * 
     * @param parent
     *            Parent composite of the new button bar.
     */
    private void createButtonBar(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        RowLayout layout = new RowLayout();
        layout.center = true;
        layout.justify = true;
        layout.pack = false;
        layout.wrap = true;
        layout.spacing = 10;
        layout.marginWidth = 5;
        layout.marginHeight = 5;
        buttonBar.setLayout(layout);
        boolean enable = (primaryParamValues.size() > 0);
        if (enable) {
            for (Map<String, Object> eventDict : primaryParamValues) {
                if (((String) eventDict.get(HAZARD_EVENT_FULL_TYPE)).length() == 0) {
                    enable = false;
                    break;
                }
            }
        }
        previewButton = createButton(buttonBar, PREVIEW_ID, PREVIEW_BUTTON_TEXT);
        previewButton.setEnabled(enable);
        previewButton.setToolTipText(PREVIEW_BUTTON_TOOLTIP_TEXT);
        proposeButton = createButton(buttonBar, PROPOSE_ID, PROPOSE_BUTTON_TEXT);
        proposeButton.setEnabled(enable);
        proposeButton.setToolTipText(PROPOSE_BUTTON_TOOLTIP_TEXT);
        issueButton = createButton(buttonBar, ISSUE_ID, ISSUE_BUTTON_TEXT);
        issueButton.setEnabled(enable);
        issueButton.setToolTipText(ISSUE_BUTTON_TOOLTIP_TEXT);
    }

    /**
     * Creates a new button with the given identifier.
     * 
     * @param parent
     *            Parent composite.
     * @param id
     *            Identifier of the button.
     * @param label
     *            Label from the button.
     * @return Button that was created.
     */
    private Button createButton(Composite parent, int id, String label) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(((Integer) event.widget.getData()).intValue());
            }
        });
        return button;
    }

    /**
     * Respond to a command button being pressed in the button bar.
     * 
     * @param buttonId
     *            Identifier of the button that was pressed.
     */
    private void buttonPressed(int buttonId) {
        if (buttonId == PROPOSE_ID) {
            fireHIDAction(new HazardDetailAction(
                    HazardDetailAction.ActionType.PROPOSE));
        } else if (buttonId == ISSUE_ID) {
            fireHIDAction(new HazardDetailAction(
                    HazardDetailAction.ActionType.ISSUE));
        } else if (buttonId == PREVIEW_ID) {
            fireHIDAction(new HazardDetailAction(
                    HazardDetailAction.ActionType.PREVIEW));
        }
    }

    /**
     * Update the event tabs to visually indicate any conflicts.
     */
    private void updateEventTabsToReflectConflicts() {

        // Iterate through the tabs, ensuring that each
        // has an icon if it is a conflicting hazard, or
        // does not have an icon if it is not.
        boolean conflictExists = false;
        Set<String> conflictingHazards = eventConflictMap.keySet();
        for (CTabItem tabItem : eventTabFolder.getItems()) {
            if (conflictingHazards.contains(tabItem.getData())) {
                conflictExists = true;
                tabItem.setImage(CONFLICT_TAB_ICON);
                tabItem.setToolTipText(CONFLICT_TOOLTIP);
            } else {
                tabItem.setImage(null);
                tabItem.setToolTipText(null);
            }
        }

        // If a conflict exists, hint at this by setting
        // the tab folder to use eye-catching background
        // colors for both selected and unselected tabs.
        if (conflictExists) {
            eventTabFolder.setBackground(CONFLICT_UNSELECTED_TAB_COLOR);
            eventTabFolder.setSelectionBackground(CONFLICT_SELECTED_TAB_COLOR);
        } else {
            eventTabFolder.setBackground(standardUnselectedColor);
            eventTabFolder.setSelectionBackground(standardSelectedColor);
        }
    }

    /**
     * Configure the specified points table with the proper headers and columns.
     * 
     * @param table
     *            Table to be configured.
     * @param layout
     *            Table column layout used to lay out the table's columns.
     */
    private void configurePointsTable(Table table, TableColumnLayout layout) {

        // Get the point megawidget specifiers for the
        // current type.
        List<ISpecifier> megawidgetSpecifiers = pointMegawidgetsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_FULL_TYPE));
        if (megawidgetSpecifiers == null) {
            statusHandler
                    .info("HazardDetailViewPart.configurePointsTable(): Could "
                            + "not find point megawidget specifiers for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }

        // Count the number of state identifiers
        // that any stateful megawidgets in the point
        // megawidget specifiers include.
        int numColumns = 0;
        for (ISpecifier megawidgetSpecifier : megawidgetSpecifiers) {
            if ((megawidgetSpecifier instanceof IStatefulSpecifier)
                    && ((megawidgetSpecifier instanceof PointsTableSpecifier) == false)) {
                numColumns += ((IStatefulSpecifier) megawidgetSpecifier)
                        .getStateIdentifiers().size();
            }
        }

        // Create the headers and identifiers
        // arrays, and the array holding the rela-
        // tive weights of the columns. The first
        // column is always reserved for the name
        // of the point, which does not have a
        // corresponding megawidget specifier.
        String[] titles = new String[numColumns + 1];
        String[] identifiers = new String[numColumns + 1];
        int[] relativeWeights = new int[numColumns + 1];
        titles[0] = "Name";
        identifiers[0] = POINTS_TABLE_NAME_COLUMN_IDENTIFIER;

        // Skipping the first column, which is al-
        // ways for the name of the point, iterate
        // through the state identifiers of the
        // stateful megawidget specifiers, dedicating
        // a column to each.
        int col = 1;
        int totalWeight = 0;
        for (ISpecifier megawidgetSpecifier : megawidgetSpecifiers) {
            if ((megawidgetSpecifier instanceof IStatefulSpecifier) == false) {
                continue;
            }
            IStatefulSpecifier statefulSpecifier = (IStatefulSpecifier) megawidgetSpecifier;
            for (String identifier : statefulSpecifier.getStateIdentifiers()) {
                titles[col] = statefulSpecifier.getStateShortLabel(identifier);
                if ((titles[col] == null) || titles[col].isEmpty()) {
                    titles[col] = null;
                    continue;
                }
                identifiers[col] = identifier;
                relativeWeights[col] = statefulSpecifier
                        .getRelativeWeight(identifier);
                totalWeight += relativeWeights[col];
                col++;
            }
        }

        // Ensure that the name column weighs 1/4th
        // of the total weight.
        relativeWeights[0] = ((totalWeight * 5) / 4) - totalWeight;

        // Iterate through the headers compiled above,
        // adding each as a column to the table.
        for (col = 0; col < titles.length; col++) {
            if (titles[col] != null) {
                TableColumn column = new TableColumn(table, SWT.NONE);
                column.setText(titles[col]);
                if (identifiers[col] != null) {
                    column.setData(identifiers[col]);
                }
                layout.setColumnData(column, new ColumnWeightData(
                        relativeWeights[col]));
            }
        }
    }

    /**
     * Populate the specified point metadata table.
     * 
     * @param table
     *            Table to be populated with data.
     */
    private void populatePointsTable(Table table) {

        // Clear the table.
        for (TableItem item : table.getItems()) {
            item.dispose();
        }
        table.clearAll();

        // Get the point megawidgets for the current type.
        Map<String, IControl> megawidgetsForIds = pointMegawidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_FULL_TYPE));
        if (megawidgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.populatePointsTable(): Could "
                            + "not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }
        if (pointsParamValues.get(visibleHazardIndex) == null) {
            statusHandler
                    .info("HazardDetailViewPart.populatePointsTable(): No "
                            + "points parameter values were found for type \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }

        // Iterate through the lines, setting the data
        // and text for each cell.
        for (int line = 0; line < pointsParamValues.get(visibleHazardIndex)
                .size(); line++) {
            TableItem item = new TableItem(table, SWT.NONE);
            for (int col = 0; col < table.getColumnCount(); col++) {

                // Get the identifier associated with
                // this column.
                String identifier = (String) table.getColumn(col).getData();

                // Get the value as specified by the
                // points parameter values for this
                // line and column. If this is the
                // first column, the name must be
                // fetched from within the shapes
                // list.
                Object value = null;
                if (col == 0) {
                    value = ((Map<?, ?>) ((List<?>) pointsParamValues
                            .get(visibleHazardIndex).get(line)
                            .get(HAZARD_EVENT_SHAPES)).get(0)).get(identifier);
                } else {
                    value = pointsParamValues.get(visibleHazardIndex).get(line)
                            .get(identifier);
                }

                // Save the value in the hash table
                // attached to this line, keyed by the
                // identifier of the column.
                item.setData(identifier, value);

                // Get the text description of the
                // value, and set the line to hold it
                // in this column.
                String description = null;
                if (col == 0) {
                    description = (String) value;
                } else {
                    try {
                        description = ((IStateful) megawidgetsForIds
                                .get(identifier)).getStateDescription(
                                identifier, value);
                    } catch (Exception e) {
                        statusHandler.error("HazardDetailViewPart."
                                + "populatePointsTable(): Unable to format "
                                + value + " for table column " + identifier
                                + ".", e);
                    }
                }
                if (description != null) {
                    item.setText(col, description);
                }
            }
        }

        // Select the first row.
        table.setSelection(0);

        // Update the point megawidgets' values to
        // match the first row of the table.
        updatePointMegawidgetValues(table);
    }

    /**
     * Update the points table's selected row to match those of the points
     * megawidgets.
     * 
     * @param table
     *            Points table.
     */
    private void updatePointsTable(Table table) {

        // If the table has no lines, do nothing.
        if (table.getItemCount() == 0) {
            return;
        }

        // Get the point megawidgets for the current type.
        Map<String, IControl> megawidgetsForIds = pointMegawidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_FULL_TYPE));
        if (megawidgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.updatePointsTable(): Could "
                            + "not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }

        // Get the selected row from the table; since
        // the table only allows single selection, it
        // is assumed that the array is one item in
        // length.
        TableItem[] items = table.getSelection();
        if (items.length == 0) {
            statusHandler.info("HazardDetailViewPart.updatePointsTable(): No "
                    + "points table item selected.");
            return;
        }

        // Iterate through the table cells, setting
        // their states to match the states found in
        // the megawidgets.
        for (int col = 1; col < table.getColumnCount(); col++) {
            String identifier = (String) table.getColumn(col).getData();
            IStateful megawidget = (IStateful) megawidgetsForIds
                    .get(identifier);
            try {
                Object state = megawidget.getState(identifier);
                items[0].setData(identifier, state);
                items[0].setText(col,
                        megawidget.getStateDescription(identifier, state));
            } catch (Exception e) {
                statusHandler.error(
                        "HazardDetailViewPart.updatePointsTable(): "
                                + "Unable to update table column " + identifier
                                + " to have state = "
                                + items[0].getData(identifier), e);
            }
        }
    }

    /**
     * Update the point megawidgets' values to match those of the points table's
     * selected row.
     * 
     * @param table
     *            Points table.
     */
    private void updatePointMegawidgetValues(Table table) {

        // If the table has no lines, do nothing.
        if (table.getItemCount() == 0) {
            return;
        }

        // Get the point megawidgets for the current type.
        Map<String, IControl> megawidgetsForIds = pointMegawidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_FULL_TYPE));
        if (megawidgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.updatePointMegawidgetValues(): "
                            + "Could not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }

        // Get the selected row from the table; since
        // the table only allows single selection, it
        // is assumed that the array is one item in
        // length.
        TableItem[] items = table.getSelection();
        if (items.length == 0) {
            statusHandler.info("HazardDetailViewPart.updatePointsTable(): No "
                    + "points table item selected.");
            return;
        }

        // Iterate through the point megawidgets, setting
        // their states to match the states found in
        // the selected row of the table.
        Set<IExplicitCommitStateful> megawidgetsNeedingCommit = Sets
                .newHashSet();
        for (String identifier : megawidgetsForIds.keySet()) {
            if ((megawidgetsForIds.get(identifier) instanceof IStateful) == false) {
                continue;
            }
            IStateful megawidget = (IStateful) megawidgetsForIds
                    .get(identifier);
            if (megawidget instanceof PointsTableMegawidget) {
                continue;
            }
            try {
                if (megawidget instanceof IExplicitCommitStateful) {
                    ((IExplicitCommitStateful) megawidget).setUncommittedState(
                            identifier, items[0].getData(identifier));
                    megawidgetsNeedingCommit
                            .add((IExplicitCommitStateful) megawidget);
                } else {
                    megawidget.setState(identifier,
                            items[0].getData(identifier));
                }
            } catch (Exception e) {
                statusHandler.error(
                        "HazardDetailViewPart.updatePointsTable(): "
                                + "Unable to update point megawidget "
                                + identifier + " to have state = "
                                + items[0].getData(identifier), e);
            }
        }
        for (IExplicitCommitStateful megawidget : megawidgetsNeedingCommit) {
            try {
                megawidget.commitStateChanges();
            } catch (Exception e) {
                statusHandler
                        .error("HazardDetailViewPart.updatePointsTable(): "
                                + "Unable to commit change(s) to state in point "
                                + "megawidget "
                                + megawidget.getSpecifier().getIdentifier(), e);
            }
        }
    }

    /**
     * Set the part's megawidgets' values to match the current event info.
     */
    private void synchWithEventInfo() {

        // If there are no hazard events, do nothing.
        if (eventTabFolder.getItemCount() == 0) {
            if (previewButton != null) {
                previewButton.setEnabled(false);
                proposeButton.setEnabled(false);
                issueButton.setEnabled(false);
            }
            return;
        }

        // Update the tab folder to visually indicate
        // any conflicts.
        updateEventTabsToReflectConflicts();

        // Set the category and type combo boxes.
        String category = getCategoryOfCurrentEvent();
        categoryCombo.setText(category);
        populateHazardTypesList(category);
        String type = (String) primaryParamValues.get(visibleHazardIndex).get(
                HAZARD_EVENT_FULL_TYPE);
        typeCombo.setText(type);

        // Set the start and end time in the time
        // range megawidgets.
        try {
            timeRangeMegawidget.setUncommittedState(
                    START_TIME_STATE,
                    primaryParamValues.get(visibleHazardIndex).get(
                            HAZARD_EVENT_START_TIME));
            timeRangeMegawidget.setUncommittedState(
                    END_TIME_STATE,
                    primaryParamValues.get(visibleHazardIndex).get(
                            HAZARD_EVENT_END_TIME));
            timeRangeMegawidget.commitStateChanges();
        } catch (Exception e) {
            statusHandler
                    .error("HazardDetailViewPart.synchWithEventInfo(): "
                            + "Error: Could not set the state for time range megawidget.",
                            e);
        }

        // Show the metadata for this type; if it
        // was already showing, then the megawidgets'
        // states still have to be synced with
        // the current values.
        if (showMetadataForType(type) == false) {
            synchMetadataMegawidgetsWithEventInfo();
        }
    }

    /**
     * Synch the metadata megawidgets with current hazard event information.
     */
    private void synchMetadataMegawidgetsWithEventInfo() {

        // Synch the main metadata megawidgets.
        String fullType = (String) primaryParamValues.get(visibleHazardIndex)
                .get(HAZARD_EVENT_FULL_TYPE);
        setMegawidgetsStates(megawidgetsForIdsForTypes.get(fullType),
                primaryParamValues.get(visibleHazardIndex));

        // If point megawidgets exist, synch them as well, in-
        // cluding the points table.
        Map<String, IControl> pointMegawidgetsForIds = pointMegawidgetsForIdsForTypes
                .get(fullType);
        Map<String, Object> pointParamValues = (pointsParamValues
                .get(visibleHazardIndex) != null ? pointsParamValues.get(
                visibleHazardIndex).get(0) : null);
        if (pointMegawidgetsForIds != null) {
            setPointMegawidgetsEnabled(pointMegawidgetsForIds,
                    (pointParamValues != null));
            setMegawidgetsStates(pointMegawidgetsForIds, pointParamValues);
            ((PointsTableMegawidget) pointMegawidgetsForIds
                    .get(POINTS_TABLE_IDENTIFIER)).populate();
        }
    }

    /**
     * Populate the hazard type combo box's list with types appropriate to the
     * specified category.
     * 
     * @param category
     *            Category for which hazard types are to be retrieved and used
     *            to populate the list.
     */
    private void populateHazardTypesList(String category) {
        final List<String> typeList = typesForCategories.get(category);
        typeCombo.removeAll();
        for (String hazardType : typeList) {
            typeCombo.add(hazardType);
        }
    }

    /**
     * Get the category of the current event.
     * 
     * @return Category of the current event.
     */
    private String getCategoryOfCurrentEvent() {
        String category = (String) primaryParamValues.get(visibleHazardIndex)
                .get(HAZARD_EVENT_CATEGORY);
        if (category == null) {
            category = categoriesForTypes.get(primaryParamValues.get(
                    visibleHazardIndex).get(HAZARD_EVENT_FULL_TYPE));
        }
        return category;
    }

    /**
     * Display the metadata panel for the specified hazard type.
     * 
     * @param type
     *            Hazard type.
     * @return True if the metadata pane had to be created during this
     *         invocation, false otherwise.
     */
    private boolean showMetadataForType(String type) {

        // If no type has been chosen, do nothing
        if (type == null) {
            statusHandler.info("HazardDetailViewPart.showMetadataForType(): "
                    + "Problem: switching to null hazard type, ignoring.");
            return false;
        }

        // If the type is an empty string, disable the
        // appropriate buttons; otherwise, enable them.
        boolean enable = (type.length() > 0);
        if (previewButton != null) {
            previewButton.setEnabled(enable);
        }
        if (proposeButton != null) {
            proposeButton.setEnabled(enable);
        }
        if (issueButton != null) {
            issueButton.setEnabled(enable);
        }

        // If the type chosen is the same as what was
        // already showing, do nothing more.
        if ((type.length() > 0)
                && (metadataContentLayout.topControl != null)
                && (panelsForTypes.get(type) == metadataContentLayout.topControl)) {
            return false;
        }

        // Get the panel for this type; if it has not
        // yet been created, create it now.
        Composite panel = panelsForTypes.get(type);
        boolean creationOccurred = false;
        if ((type.length() > 0) && (panel == null)) {
            creationOccurred = true;

            // Determine whether or not point-speci-
            // fic megawidgets are needed for this hazard
            // type; if so, construct a tab folder
            // with two tabs, one for general meta-
            // data megawidgets, the other for the point-
            // specific megawidgets; otherwise, just
            // construct a single group holding the
            // general metadata megawidgets if the latter
            // are required, or an empty panel other-
            // wise.
            List<ISpecifier> pointMegawidgetSpecifiers = pointMegawidgetsForTypes
                    .get(type);
            if (pointMegawidgetSpecifiers != null) {

                // Create the tab folder widget.
                panel = new CTabFolder(metadataContentPanel, SWT.TOP);
                ((CTabFolder) panel).setBorderVisible(true);
                ((CTabFolder) panel).setTabHeight(((CTabFolder) panel)
                        .getTabHeight() + 8);

                // Create the area tab page to hold the
                // general area megawidgets.
                CTabItem tabItem = new CTabItem((CTabFolder) panel, SWT.NONE);
                tabItem.setText(AREA_DETAILS_TAB_TEXT);
                Composite areaPage = new Composite(panel, SWT.NONE);
                Map<String, IControl> megawidgetsForIds = Maps.newHashMap();
                addMegawidgetsToPanel(megawidgetsForTypes.get(type), areaPage,
                        megawidgetsForIds, null,
                        primaryParamValues.get(visibleHazardIndex));
                megawidgetsForIdsForTypes.put(type, megawidgetsForIds);
                tabItem.setControl(areaPage);

                // Create points tab page to hold the
                // points-specific megawidgets.
                tabItem = new CTabItem((CTabFolder) panel, SWT.NONE);
                tabItem.setText(POINTS_DETAILS_TAB_TEXT);
                Composite pointsPage = new Composite(panel, SWT.NONE);
                megawidgetsForIds = Maps.newHashMap();
                Map<String, IControl> megawidgetsForStateIds = Maps
                        .newHashMap();
                addMegawidgetsToPanel(
                        pointMegawidgetSpecifiers,
                        pointsPage,
                        megawidgetsForIds,
                        megawidgetsForStateIds,
                        (pointsParamValues.get(visibleHazardIndex) == null ? null
                                : pointsParamValues.get(visibleHazardIndex)
                                        .get(0)));
                pointMegawidgetsForIdsForTypes.put(type, megawidgetsForIds);
                pointMegawidgetsForStateIdsForTypes.put(type,
                        megawidgetsForStateIds);
                ((PointsTableMegawidget) megawidgetsForIds
                        .get(POINTS_TABLE_IDENTIFIER)).populate();
                if (pointsParamValues.get(visibleHazardIndex) == null) {
                    setPointMegawidgetsEnabled(megawidgetsForIds, false);
                }
                tabItem.setControl(pointsPage);

                // Lay out the tab folder.
                panel.pack();
            } else if ((megawidgetsForTypes.get(type) != null)
                    && (megawidgetsForTypes.get(type).size() > 0)) {

                // Create the group panel to hold all the
                // megawidgets.
                panel = new Group(metadataContentPanel, SWT.NONE);
                ((Group) panel).setText(DETAILS_SECTION_TEXT);
                Map<String, IControl> megawidgetsForIds = Maps.newHashMap();
                addMegawidgetsToPanel(megawidgetsForTypes.get(type), panel,
                        megawidgetsForIds, null,
                        primaryParamValues.get(visibleHazardIndex));
                megawidgetsForIdsForTypes.put(type, megawidgetsForIds);
            } else {
                panel = new Composite(metadataContentPanel, SWT.NONE);
            }
            panelsForTypes.put(type, panel);

            // Create a CAVE mode listener, which will
            // set the foreground and background colors
            // appropriately according to the CAVE mode
            // whenever a paint event occurs.
            new ModeListener(panel);
        }

        // Show this type's panel and lay out the content
        // panel to display it. This is done asynchronously
        // the first time this method is called because
        // sometimes when the view part first comes up the
        // metadata panel requests a bizarrely large height.
        if (scrolledCompositeSizeCalculated == false) {
            final Composite thePanel = panel;
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    configureScrolledCompositeToHoldPanel(thePanel);
                }
            });
            scrolledCompositeSizeCalculated = true;
        } else {
            configureScrolledCompositeToHoldPanel(panel);
        }
        return creationOccurred;
    }

    /**
     * Configure the scrolled composite to hold the specified panel.
     * 
     * @param panel
     *            Panel to be held.
     */
    private void configureScrolledCompositeToHoldPanel(Composite panel) {
        if (metadataContentPanel.isDisposed()) {
            return;
        }
        scrolledCompositeContentsChanging = true;
        metadataContentLayout.topControl = panel;
        metadataContentPanel.layout();
        configureScrolledCompositeForSelectedEvent();
        scrolledCompositeContentsChanging = false;
    }

    /**
     * Resize the scrolled composite to be appropriate for the content pane.
     */
    private void configureScrolledCompositeForSelectedEvent() {

        // If there are no events, do nothing.
        if (primaryParamValues.size() == 0) {
            return;
        }

        // Determine the required height of the metadata panel.
        int metadataHeight = (metadataContentLayout.topControl == null ? 0
                : metadataContentLayout.topControl.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT).y);

        // Set the tab page's size to be appropriate for the
        // new metadata panel, and tell the scrolled composite
        // that holds it of its new size.
        top.setSize(top.getSize().x, baseTabPageHeight + metadataHeight);
        scrolledComposite.setMinHeight(baseTabPageHeight + metadataHeight);

        // If the event that is showing has a previously-recorded
        // scrolling origin, use it so that the top-left portion
        // of the event's panel that was visible last time it was
        // looked at is made visible again. Otherwise, just make
        // the top left corner of the panel is visible.
        Point origin = scrollOriginsForEventIDs.get(primaryParamValues.get(
                visibleHazardIndex).get(HAZARD_EVENT_IDENTIFIER));
        if (origin == null) {
            origin = new Point(0, 0);
        }
        scrolledComposite.setOrigin(origin);

        // Recalculate the page size.
        recalculateScrolledCompositePageIncrement();
    }

    /**
     * Recalculate the scrolled composite page increment.
     */
    private void recalculateScrolledCompositePageIncrement() {
        if (scrolledComposite.isDisposed()) {
            return;
        }
        if (scrolledCompositePageIncrementChanging) {
            return;
        }
        scrolledCompositePageIncrementChanging = true;
        scrolledComposite.getVerticalBar().setPageIncrement(
                scrolledComposite.getVerticalBar().getThumb());
        scrolledCompositePageIncrementChanging = false;
    }

    /**
     * Enable or disable the specified point megawidgets.
     * 
     * @param megawidgetsForIds
     *            Hash table mapping identifiers to megawidgets, the latter
     *            forming collectively the set of megawidgets to be enabled or
     *            disabled.
     * @param enabled
     *            Flag indicating whether to enable or disable the megawidgets.
     */
    private void setPointMegawidgetsEnabled(
            Map<String, IControl> megawidgetsForIds, boolean enabled) {
        for (IControl megawidget : megawidgetsForIds.values()) {
            megawidget.setEnabled(enabled);
        }
    }

    /**
     * Respond to the start or end times changing.
     */
    private void timeRangeChanged() {

        // Do nothing if the appropriate megawidget is not
        // found.
        if (timeRangeMegawidget == null) {
            return;
        }

        // Get the times and save them.
        try {
            Long startTime = (Long) timeRangeMegawidget
                    .getState(START_TIME_STATE);
            if (startTime == null) {
                return;
            }
            primaryParamValues.get(visibleHazardIndex).put(
                    HAZARD_EVENT_START_TIME, startTime);
            Long endTime = (Long) timeRangeMegawidget.getState(END_TIME_STATE);
            if (endTime == null) {
                return;
            }
            primaryParamValues.get(visibleHazardIndex).put(
                    HAZARD_EVENT_END_TIME, endTime);
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.timeRangeChanged(): "
                    + "could not get state from time range megawidgets.", e);
        }

        // Generate a HID action and fire it off.
        Dict eventInfo = new Dict();
        eventInfo.put(
                HAZARD_EVENT_IDENTIFIER,
                primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_IDENTIFIER));
        eventInfo.put(
                HAZARD_EVENT_START_TIME,
                primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_START_TIME));
        eventInfo.put(
                HAZARD_EVENT_END_TIME,
                primaryParamValues.get(visibleHazardIndex).get(
                        HAZARD_EVENT_END_TIME));
        String jsonText = null;
        try {
            jsonText = eventInfo.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "HazardDetailViewPart.timeRangeChanged(): conversion "
                            + "of event info to JSON string failed.", e);
        }
        fireHIDAction(new HazardDetailAction(
                HazardDetailAction.ActionType.UPDATE_TIME_RANGE, jsonText));
    }

    /**
     * Add metadata megawidgets as specified to the provided panel.
     * 
     * @param megawidgetSpecifiers
     *            List of megawidget specifiers indicating the megawidgets to be
     *            added.
     * @param panel
     *            Panel to which to add the megawidgets.
     * @param megawidgetsForIds
     *            Hash table pairing megawidget identifiers with their
     *            associated megawidgets. All created megawidgets have entries
     *            created within this table during the course of the invocation
     *            of this method, one per megawidget.
     * @param megawidgetsForStateIds
     *            Hash table pairing state identifiers with their associated
     *            megawidgets. All created megawidgets have entries created
     *            within this table during the course of the invocation of this
     *            method, one per state held by a megawidget, so if a megawidget
     *            has two states, it will have two entries in the table. If
     *            <code>
     *            null</code>, no recording of megawidgets paired with state
     *            identifiers occurs.
     * @param paramValues
     *            Hash table pairing state identifiers with their starting state
     *            values, if any.
     */
    private void addMegawidgetsToPanel(List<ISpecifier> megawidgetSpecifiers,
            Composite panel, Map<String, IControl> megawidgetsForIds,
            Map<String, IControl> megawidgetsForStateIds,
            Map<String, Object> paramValues) {

        // Create the grid layout for the panel.
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 7;
        panel.setLayout(gridLayout);

        // Create the megawidgets based on the specifica-
        // tions, adding an entry for each to the
        // hash table pairing them with their identi-
        // fiers; if they are also stateful, then
        // add entries to the other table pairing
        // them with their states, one entry per
        // state that each contains.
        Set<IControl> megawidgets = Sets.newHashSet();
        for (ISpecifier megawidgetSpecifier : megawidgetSpecifiers) {
            IControl megawidget = null;
            try {
                megawidget = megawidgetSpecifier.createMegawidget(panel,
                        IControl.class, megawidgetCreationParams);
            } catch (MegawidgetException e) {
                statusHandler
                        .error("HazardDetailViewPart.addMegawidgetsToPanel(): Hazard "
                                + "event type metadata megawidget creation error.",
                                e);
            }
            Set<IControl> newMegawidgets = Sets.newHashSet();
            findAllDescendantMegawidgets(megawidget, newMegawidgets);
            recordNewMegawidgets(newMegawidgets, megawidgets,
                    megawidgetsForIds, megawidgetsForStateIds,
                    timeScaleMegawidgets);
        }

        // Align the megawidgets visually with one
        // another.
        ControlComponentHelper.alignMegawidgetsElements(megawidgets);

        // Set the megawidgets' states to match the
        // initial values.
        setMegawidgetsStates(megawidgetsForIds, paramValues);

        // Lay out the panel.
        panel.pack();
    }

    /**
     * Find all descendant megawidgets of the specified megawidget.
     * 
     * @param megawidget
     *            Potential parent megawidget.
     * @param descendants
     *            Set into which all descendants of <code>megawidget</code> will
     *            be placed, including <code>megawidget</code> itself.
     */
    @SuppressWarnings("unchecked")
    private void findAllDescendantMegawidgets(IControl megawidget,
            Set<IControl> descendants) {
        descendants.add(megawidget);
        if (megawidget instanceof IParent) {
            for (IControl descendant : ((IParent<? extends IControl>) megawidget)
                    .getChildren()) {
                findAllDescendantMegawidgets(descendant, descendants);
            }
        }
    }

    /**
     * Record the existence of the specified new megawidgets.
     * 
     * @param megawidgets
     *            Set of megawidgets that were just created and need recording.
     * @param allMegawidgets
     *            Set of all megawidgets being created for this panel. All new
     *            megawidgets are placed within this set in the course of the
     *            invocation of this method.
     * @param megawidgetsForIds
     *            Hash table pairing megawidget identifiers with their
     *            associated megawidgets. All new megawidgets have entries
     *            created within this table during the course of the invocation
     *            of this method, one per megawidget.
     * @param megawidgetsForStateIds
     *            Hash table pairing state identifiers with their associated
     *            megawidgets. All new megawidgets have entries created within
     *            this table during the course of the invocation of this method,
     *            one per state held by a megawidget, so if a megawidget has two
     *            states, it will have two entries in the table. If <code>
     *            null</code>, no recording of megawidgets paired with state
     *            identifiers occurs.
     * @param timeScaleMegawidgets
     *            Set of all megawidgets that are time scales. If any of the new
     *            megawidgets are of this type, they will be added to this set
     *            in the course of the invocation of this method.
     */
    private void recordNewMegawidgets(Set<IControl> megawidgets,
            Set<IControl> allMegawidgets,
            Map<String, IControl> megawidgetsForIds,
            Map<String, IControl> megawidgetsForStateIds,
            Set<TimeScaleMegawidget> timeScaleMegawidgets) {
        for (IControl megawidget : megawidgets) {
            allMegawidgets.add(megawidget);
            megawidgetsForIds.put(megawidget.getSpecifier().getIdentifier(),
                    megawidget);
            if ((megawidgetsForStateIds != null)
                    && (megawidget instanceof IStateful)) {
                for (String identifier : ((IStatefulSpecifier) megawidget
                        .getSpecifier()).getStateIdentifiers()) {
                    megawidgetsForStateIds.put(identifier, megawidget);
                }
            }
            if (megawidget instanceof TimeScaleMegawidget) {
                timeScaleMegawidgets.add((TimeScaleMegawidget) megawidget);
            }
        }
    }

    /**
     * Set the metadata megawidgets' states as specified.
     * 
     * @param megawidgetsForIds
     *            Hash table pairing megawidget identifiers with their
     *            associated megawidgets. All created megawidgets have entries
     *            created within the table during the course of the invocation
     *            of this method.
     * @param paramValues
     *            Hash table pairing megawidget identifiers with their starting
     *            state values, if any.
     */
    private void setMegawidgetsStates(Map<String, IControl> megawidgetsForIds,
            Map<String, Object> paramValues) {

        // If there are initial state values, set the
        // megawidgets to match them. If there is no state
        // value for a particular state identifier and
        // the megawidget has a default value for it, make
        // an entry in the hazard event mapping that
        // identifier to the default value. This ensures
        // that default values for megawidgets are used
        // as default values for hazard events.
        if ((paramValues != null) && (megawidgetsForIds != null)) {

            // Iterate through the megawidgets, finding any
            // that are stateful and for those, setting
            // the states to match those given in the
            // parameter values dictionary, or if the
            // latter has no value for a given state
            // identifier and the megawidget has a starting
            // value, adding a mapping for that state
            // identifier and default value to the
            // dictionary.
            Set<String> identifiersGivenDefaultValues = Sets.newHashSet();
            Set<IExplicitCommitStateful> megawidgetsNeedingCommit = Sets
                    .newHashSet();
            for (String megawidgetIdentifier : megawidgetsForIds.keySet()) {
                IControl megawidget = megawidgetsForIds
                        .get(megawidgetIdentifier);
                if ((megawidget instanceof IStateful) == false) {
                    continue;
                }
                boolean timeScaleMegawidgetStatesNeedSetting = false;
                for (String identifier : ((IStatefulSpecifier) megawidget
                        .getSpecifier()).getStateIdentifiers()) {

                    // Use the value from the parameter
                    // values dictionary if it works, but
                    // if it invalid, or if there is no
                    // such value available, use the
                    // default value instead.
                    boolean useDefaultValue = !paramValues
                            .containsKey(identifier);
                    if (megawidget instanceof TimeScaleMegawidget) {
                        useDefaultValue = (useDefaultValue || ((Number) paramValues
                                .get(identifier)).longValue() == 0L);
                    }
                    if (useDefaultValue == false) {
                        Object value = paramValues.get(identifier);
                        try {
                            setMegawidgetState((IStateful) megawidget,
                                    identifier, value, megawidgetsNeedingCommit);
                        } catch (MegawidgetStateException e) {
                            useDefaultValue = true;
                            statusHandler
                                    .info("HazardDetailViewPart.setMegawidgetsStates(): "
                                            + "Unable to set state for "
                                            + identifier
                                            + " to "
                                            + value
                                            + " (value is invalid).");
                        }
                    }
                    if (useDefaultValue) {

                        // If this is a time scale megawidget, remember
                        // that its starting values need setting. Other-
                        // wise, use the default starting value.
                        if (megawidget instanceof TimeScaleMegawidget) {
                            timeScaleMegawidgetStatesNeedSetting = true;
                            break;
                        } else {
                            try {

                                // Use the default (starting) value of the
                                // megawidget specifier, not the current
                                // value of the megawidget itself, since
                                // it may have been changed by other
                                // events previously. If a default value
                                // exists, make this the current value of
                                // the event dictionary for that identifier,
                                // and set the megawidget to match. If no
                                // default starting value is found, use
                                // the current value of the megawidget in-
                                // stead.
                                Object defaultValue = ((IStatefulSpecifier) megawidget
                                        .getSpecifier())
                                        .getStartingState(identifier);
                                if (defaultValue != null) {
                                    identifiersGivenDefaultValues
                                            .add(identifier);
                                    paramValues.put(identifier, defaultValue);
                                    Object megawidgetState = ((IStateful) megawidget)
                                            .getState(identifier);
                                    if ((megawidgetState == null)
                                            || (megawidgetState
                                                    .equals(defaultValue) == false)) {
                                        setMegawidgetState(
                                                (IStateful) megawidget,
                                                identifier, defaultValue,
                                                megawidgetsNeedingCommit);
                                    }
                                } else {
                                    defaultValue = ((IStateful) megawidget)
                                            .getState(identifier);
                                    if (defaultValue != null) {
                                        identifiersGivenDefaultValues
                                                .add(identifier);
                                        paramValues.put(identifier,
                                                defaultValue);
                                    }
                                }
                            } catch (Exception e) {
                                statusHandler
                                        .error("HazardDetailViewPart.setMegawidgetsStates(): "
                                                + "Unable to fetch default state for "
                                                + identifier + ".", e);
                            }
                        }
                    }
                }

                // If this is a time scale megawidget and its state
                // needs setting, set the starting values of its
                // thumbs to equidistant temporal points along the
                // current start-end time range of the event. This
                // ensures that their starting values are not bogus.
                if (timeScaleMegawidgetStatesNeedSetting) {
                    try {
                        long start = (Long) timeRangeMegawidget
                                .getState(START_TIME_STATE);
                        long end = (Long) timeRangeMegawidget
                                .getState(END_TIME_STATE);
                        List<String> identifiers = ((IStatefulSpecifier) megawidget
                                .getSpecifier()).getStateIdentifiers();
                        long interval = (identifiers.size() == 1 ? 0L
                                : (end - start) / (identifiers.size() - 1L));
                        long defaultValue = (identifiers.size() == 1 ? (start + end) / 2
                                : start);
                        for (int j = 0; j < identifiers.size(); j++, defaultValue += interval) {
                            String identifier = identifiers.get(j);
                            identifiersGivenDefaultValues.add(identifier);
                            paramValues.put(identifier, defaultValue);
                            setMegawidgetState((IStateful) megawidget,
                                    identifier, defaultValue,
                                    megawidgetsNeedingCommit);
                        }
                    } catch (Exception e) {
                        statusHandler.error(
                                "HazardDetailViewPart.setMegawidgetsStates(): "
                                        + "Unable to set starting states for "
                                        + megawidget.getSpecifier()
                                                .getIdentifier() + ".", e);
                    }
                }
            }

            // Commit changes to any megawidgets that must
            // be explicitly commit.
            for (IExplicitCommitStateful megawidget : megawidgetsNeedingCommit) {
                try {
                    megawidget.commitStateChanges();
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.setMegawidgetsStates(): "
                                    + "Unable to commit change(s) to state for "
                                    + megawidget.getSpecifier().getIdentifier(),
                                    e);
                }
            }

            // If any default values were taken from the
            // megawidgets (or in the case of time scale
            // megawidgets, calculated on the fly) and
            // placed in the event dictionary, send off a
            // notification that these values changed.
            if (identifiersGivenDefaultValues.size() > 0) {
                Dict eventInfo = new Dict();
                eventInfo.put(HAZARD_EVENT_IDENTIFIER,
                        paramValues.get(HAZARD_EVENT_IDENTIFIER));
                for (String identifier : identifiersGivenDefaultValues) {
                    eventInfo.put(identifier, paramValues.get(identifier));
                }
                String jsonText = null;
                try {
                    jsonText = eventInfo.toJSONString();
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.setMegawidgetsStates(): conversion "
                                    + "of event info to JSON string failed.", e);
                }

                /*
                 * It is important to distinguish here between information which
                 * was modified by the forecaster in the Hazard Detail View and
                 * information which was updated programmatically by the
                 * software basically doing some book keeping.
                 */
                hazardDetailView.fireAction(new HazardDetailAction(
                        HazardDetailAction.ActionType.UPDATE_EVENT_METADATA,
                        jsonText, false), true);
            }
        }
    }

    /**
     * Set the specified stateful megawidget to hold the specified value for the
     * specified state identifier, recording it as requiring an explicit commit
     * if this is the case.
     * 
     * @param megawidget
     *            Megawidget to have its state set.
     * @param identifier
     *            State identifier to be set.
     * @param value
     *            New value of the state identifier.
     * @param megawidgetsNeedingCommit
     *            Set of megawidgets needing an explicit commit;
     *            <code>megawidget
     *            </code> will be added to this set by this invocation if it is
     *            an <code>IExplicitCommitStateful</code> instance.
     * @throws MegawidgetStateException
     *             If a state exception occurs while attempting to set the
     *             megawidget's state.
     */
    private void setMegawidgetState(IStateful megawidget, String identifier,
            Object value, Set<IExplicitCommitStateful> megawidgetsNeedingCommit)
            throws MegawidgetStateException {
        if (megawidget instanceof IExplicitCommitStateful) {
            ((IExplicitCommitStateful) megawidget).setUncommittedState(
                    identifier, value);
            megawidgetsNeedingCommit.add((IExplicitCommitStateful) megawidget);
        } else {
            megawidget.setState(identifier, value);
        }

    }

    /**
     * Given the specified object, get a list of JSON objects it represents. The
     * object may either be a single JSON object, or a JSON array of JSON
     * objects.
     * 
     * @param object
     *            Object.
     * @return List of JSON objects.
     */
    private List<Dict> getJsonObjectList(Object object) {
        List<Dict> objects = Lists.newArrayList();
        if (object instanceof Dict) {
            objects.add((Dict) object);
        } else {
            for (Object subObject : (List<?>) object) {
                objects.add((Dict) subObject);
            }
        }
        return objects;
    }

    /**
     * When an action is fire in HID, this method will be called to notify all
     * listeners.
     */
    private void fireHIDAction(HazardDetailAction action) {
        hazardDetailView.fireAction(action, false);
    }

    /**
     * Set the specified tab's text to include the specified event identifier
     * and type.
     * 
     * @param item
     *            Tab to have its text set.
     * @param eventID
     *            Event identifier.
     * @param type
     *            Type, or an empty string or <code>null</code> if no type has
     *            been chosen for this event.
     */
    private void setTabText(CTabItem item, String eventID, String type) {
        if (type.length() == 0) {
            type = null;
        }
        item.setText(eventID + (type != null ? " " + type : ""));
    }
}
