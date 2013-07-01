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

import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.megawidgets.HierarchicalChoicesTreeSpecifier;
import gov.noaa.gsd.viz.megawidgets.IExplicitCommitStateful;
import gov.noaa.gsd.viz.megawidgets.INotificationListener;
import gov.noaa.gsd.viz.megawidgets.INotifier;
import gov.noaa.gsd.viz.megawidgets.IStateChangeListener;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.IStatefulSpecifier;
import gov.noaa.gsd.viz.megawidgets.Megawidget;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierFactory;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.megawidgets.StatefulMegawidget;
import gov.noaa.gsd.viz.megawidgets.StatefulMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeScaleMegawidget;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.ui.part.ViewPart;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailViewPart extends ViewPart implements
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
     * Points table widget identifier, a special string used as an identifier
     * for a points metadata table widget, if one is showing.
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
     * Event time range widget identifier, a special string used as an
     * identifier for the time range widget.
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

    // Private Classes

    /**
     * Points table megawidget.
     * 
     * @see PointsTableSpecifier
     */
    private class PointsTableMegawidget extends StatefulMegawidget {

        // Private Variables

        /**
         * Component associated with this widget.
         */
        private Table table = null;

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
                    updatePointWidgetValues((Table) e.widget);
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

        /**
         * Populate the table.
         */
        public final void populate() {
            populatePointsTable(table);
        }

        /**
         * Update the table to match the widgets.
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
         * Change the component widgets to ensure their state matches that of
         * the editable flag.
         * 
         * @param editable
         *            Flag indicating whether the component widgets are to be
         *            editable or read-only.
         */
        @Override
        protected final void doSetEditable(boolean editable) {
            table.setBackground(getBackgroundColor(editable, table, null));
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
            Map<String, Map<String, Object>> state = new HashMap<String, Map<String, Object>>();

            // Iterate through the lines, adding points
            // to the list.
            for (int line = 0; line < table.getItemCount(); line++) {

                // Create the hash table mapping point
                // field names to their values, and then
                // iterate through the columns, adding
                // all these pairings to the table.
                TableItem item = table.getItem(line);
                Map<String, Object> map = new HashMap<String, Object>();
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
         *             <code> StatefulWidget</code> implementation.
         */
        @Override
        protected final String doGetStateDescription(String identifier,
                Object state) throws MegawidgetStateException {

            // Not implemented.
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Points table megawidget specifier. Unlike other megawidget specifiers,
     * this one is only specified internally within the enclosing class, never
     * by configuration files.
     * 
     * @see PointsTableMegawidget
     */
    private class PointsTableSpecifier extends StatefulMegawidgetSpecifier {

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
        }

        // Public Methods

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
        @Override
        public <P extends Widget> Megawidget createMegawidget(P parent,
                Map<String, Object> creationParams) throws MegawidgetException {

            // Return the created widget.
            return new PointsTableMegawidget(this, (Composite) parent,
                    widgetCreationParams);
        }
    }

    // Private Variables

    /**
     * Parent composite.
     */
    private Composite parent = null;

    /**
     * Tab folder holding the different hazard events, one per tab page.
     */
    private CTabFolder eventTabFolder = null;

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
    private long minimumVisibleTime = Utilities.MIN_TIME;

    /**
     * Maximum visible time in the time range.
     */
    private long maximumVisibleTime = Utilities.MAX_TIME;

    /**
     * Widget specifier factory.
     */
    private final MegawidgetSpecifierFactory widgetSpecifierFactory = new MegawidgetSpecifierFactory();

    /**
     * Event time range widget.
     */
    private TimeScaleMegawidget timeRangeWidget = null;

    /**
     * Set of all time scale widgets currently in existence.
     */
    private final Set<TimeScaleMegawidget> timeScaleWidgets = new HashSet<TimeScaleMegawidget>();

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
    private final List<String> categories = new ArrayList<String>();

    /**
     * Hash table pairing categories with lists of hazard type identifiers.
     */
    private final Map<String, List<String>> typesForCategories = new HashMap<String, List<String>>();

    /**
     * Hash table pairing hazard types with categories.
     */
    private final Map<String, String> categoriesForTypes = new HashMap<String, String>();

    /**
     * Hash table pairing widget creation time parameter identifiers with their
     * corresponding values.
     */
    private final Map<String, Object> widgetCreationParams = new HashMap<String, Object>();

    /**
     * Hash table pairing hazard types with lists of the associated widget
     * specifiers.
     */
    private final Map<String, List<MegawidgetSpecifier>> widgetsForTypes = new HashMap<String, List<MegawidgetSpecifier>>();

    /**
     * Hash table pairing hazard types with lists of the associated widget
     * specifiers for the individual points of the hazard.
     */
    private final Map<String, List<MegawidgetSpecifier>> pointWidgetsForTypes = new HashMap<String, List<MegawidgetSpecifier>>();

    /**
     * Hash table pairing hazard types with metadata panels used by those types.
     */
    private final Map<String, Composite> panelsForTypes = new HashMap<String, Composite>();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * metadata widget identifiers with their associated widgets.
     */
    private final Map<String, Map<String, Megawidget>> widgetsForIdsForTypes = new HashMap<String, Map<String, Megawidget>>();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * point-specific metadata widget identifiers with their associated widgets.
     */
    private final Map<String, Map<String, Megawidget>> pointWidgetsForIdsForTypes = new HashMap<String, Map<String, Megawidget>>();

    /**
     * Hash table pairing hazard types with hash tables, the latter pairing
     * point-specific metadata state identifiers with their associated widgets.
     */
    private final Map<String, Map<String, Megawidget>> pointWidgetsForStateIdsForTypes = new HashMap<String, Map<String, Megawidget>>();

    /**
     * List of event dictionaries for the primary shape(s) in the events, each
     * dictionary being in the form of a hash table that maps identifier keys to
     * values that are to be used for various parameters of the hazard.
     */
    private final List<Map<String, Object>> primaryParamValues = new ArrayList<Map<String, Object>>();

    /**
     * List of lists of event dictionaries for the auxiliary shapes in the
     * events, if any, each of which is a hash table mapping identifier keys to
     * values that are to be used for various parameters of the hazard.
     */
    private final List<List<Map<String, Object>>> auxiliaryParamValues = new ArrayList<List<Map<String, Object>>>();

    /**
     * List of lists of event dictionaries for the points in the events, if any,
     * each of which is a hash table mapping identifier keys to values that are
     * to be used for various parameters of the individual points of the hazard.
     */
    private final List<List<Map<String, Object>>> pointsParamValues = new ArrayList<List<Map<String, Object>>>();

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
    private final Map<String, Point> scrollOriginsForEventIDs = new HashMap<String, Point>();

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

    /**
     * Flag indicating whether or not this view part is currently docked.
     */
    private boolean docked;

    // Public Methods

    /**
     * Initialize the view part.
     * 
     * @param hazardDetailView
     *            View managing this view part.
     * @param generalWidgets
     *            JSON specifying the general widgets that the view part must
     *            contain.
     * @param hazardWidgets
     *            JSON specifying the widgets that the view part must contain
     *            for each of the hazards that the general widgets allow the
     *            user to select.
     * @param minVisibleTime
     *            Minimum visible time.
     * @param maxVisibleTime
     *            Maximum visible time.
     */
    public void initialize(HazardDetailView hazardDetailView,
            String generalWidgets, String hazardWidgets, long minVisibleTime,
            long maxVisibleTime) {

        // Remember the minimum and maximum visible times.
        minimumVisibleTime = minVisibleTime;
        maximumVisibleTime = maxVisibleTime;
        this.hazardDetailView = hazardDetailView;

        // Iterate through the time scale widgets, changing
        // their visible time ranges to match the current
        // range.
        for (TimeScaleMegawidget widget : timeScaleWidgets) {
            widget.setVisibleTimeRange(minimumVisibleTime, maximumVisibleTime);
        }

        // Determine whether or not the view is currently docked.
        docked = determineWhetherDocked();

        // Parse the strings into a JSON object and a JSON array.
        Dict jsonGeneralWidgets = null;
        DictList jsonHazardWidgets = null;
        try {
            jsonGeneralWidgets = Dict.getInstance(generalWidgets);
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.initialize(): Error "
                    + "parsing JSON for general megawidgets.", e);
        }
        try {
            jsonHazardWidgets = DictList.getInstance(hazardWidgets);
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
                .get(Utilities.HAZARD_INFO_GENERAL_CONFIG_WIDGETS)) {

            // Get the category.
            Dict jsonItem = (Dict) item;
            String category = jsonItem
                    .getDynamicallyTypedValue(HierarchicalChoicesTreeSpecifier.CHOICE_NAME);
            categories.add(category);

            // Get the types that go with this category. Each JSON
            // array will contain dictionaries which in turn hold
            // strings as one of their values naming the types; the
            // resulting list must then be alphabetized.
            List<String> types = new ArrayList<String>();
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

        // Get the widget specifier lists for the event types.
        widgetsForTypes.clear();
        pointWidgetsForTypes.clear();
        for (int j = 0; j < jsonHazardWidgets.size(); j++) {

            // Get the event types to which this list of widget
            // specifiers applies, ignoring any that already
            // have an associated widget specifier list due to
            // a previous iteration. This last part is neces-
            // sary because an event type might appear in more
            // than one set of event types (each set being
            // associated with a list of widget specifiers), in
            // which case the first set in which it appears is
            // considered to be the set in which it actually
            // belongs.
            Dict jsonItem = jsonHazardWidgets.getDynamicallyTypedValue(j);
            List<String> types = new ArrayList<String>();
            for (Object child : (List<?>) jsonItem
                    .get(Utilities.HAZARD_INFO_METADATA_TYPES)) {
                types.add((String) child);
            }
            Set<String> typesSet = new HashSet<String>();
            for (String type : types) {
                if (widgetsForTypes.containsKey(type) == false) {
                    typesSet.add(type);
                }
            }

            // If after dropping any event types that have
            // already been taken care of in previous itera-
            // tions there is still a non-empty set of event
            // types left, put together any widget specifiers
            // for this set.
            if (typesSet.isEmpty() == false) {

                // Create the widget specifiers for the event
                // type as a whole.
                List<MegawidgetSpecifier> widgetSpecifiers = new ArrayList<MegawidgetSpecifier>();
                List<Dict> objects = getJsonObjectList(jsonItem
                        .get(Utilities.HAZARD_INFO_METADATA_MEGAWIDGETS_LIST));
                try {
                    for (Dict object : objects) {
                        widgetSpecifiers.add(widgetSpecifierFactory
                                .createMegawidgetSpecifier(object));
                    }
                } catch (Exception e) {
                    statusHandler.error("HazardDetailViewPart.initialize(): "
                            + "Error parsing JSON for hazard megawidgets.", e);
                }
                for (String type : typesSet) {
                    widgetsForTypes.put(type, widgetSpecifiers);
                }

                // Create the widget specifiers for the event
                // type's individual points, if any. Any
                // widget specifiers for points must be pre-
                // ceded by a points table widget specifier,
                // which is synthesized here and placed at
                // the head of the list. The widget immedi-
                // ately following the table is padded so
                // that it does not get too far into the
                // table's personal space.
                Object pointObject = jsonItem
                        .get(Utilities.HAZARD_INFO_METADATA_MEGAWIDGETS_POINTS_LIST);
                if (pointObject != null) {
                    widgetSpecifiers = new ArrayList<MegawidgetSpecifier>();
                    objects = getJsonObjectList(pointObject);
                    Dict tableObject = new Dict();
                    tableObject.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                            POINTS_TABLE_IDENTIFIER);
                    tableObject.put(MegawidgetSpecifier.MEGAWIDGET_SPACING, 5);
                    try {
                        widgetSpecifiers.add(new PointsTableSpecifier(
                                tableObject));
                        boolean first = true;
                        for (Dict object : objects) {
                            if (first) {
                                first = false;
                                int spacing = (object
                                        .get(MegawidgetSpecifier.MEGAWIDGET_SPACING) == null ? 0
                                        : ((Number) object
                                                .get(MegawidgetSpecifier.MEGAWIDGET_SPACING))
                                                .intValue());
                                if (spacing < 10) {
                                    object.put(
                                            MegawidgetSpecifier.MEGAWIDGET_SPACING,
                                            10);
                                }
                            }
                            widgetSpecifiers.add(widgetSpecifierFactory
                                    .createMegawidgetSpecifier(object));
                        }
                    } catch (Exception e) {
                        statusHandler
                                .error("HazardDetailViewPart.initialize(): "
                                        + "Error parsing JSON for hazard point megawidgets.",
                                        e);
                    }
                    for (String type : typesSet) {
                        pointWidgetsForTypes.put(type, widgetSpecifiers);
                    }
                }
            }
        }
    }

    /**
     * Create the part control for the view.
     * 
     * @param parent
     *            Parent panel to populate with widgets.
     */
    @Override
    public void createPartControl(Composite parent) {

        // Create a CAVE mode listener, which will
        // set the foreground and background colors
        // appropriately according to the CAVE mode
        // whenever a paint event occurs.
        new ModeListener(parent);

        // Remember the parent for use later, and
        // keep track of resize events to determine
        // whether or not the part is currently
        // docked.
        this.parent = parent;
        parent.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {

                // Determine whether or not the part
                // is now docked.
                boolean docked = determineWhetherDocked();
                if (docked != HazardDetailViewPart.this.docked) {
                    HazardDetailViewPart.this.docked = docked;
                }
            }
        });

        // Configure the parent layout.
        parent.setLayout(new GridLayout(1, false));

        // Fill in the widget creation parameters
        // hash table, used to provide parameters
        // to widgets created via widget speci-
        // fiers at the widgets' creation time.
        widgetCreationParams.put(INotifier.NOTIFICATION_LISTENER, this);
        widgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER, this);
        widgetCreationParams.put(TimeScaleSpecifier.MINIMUM_TIME,
                Utilities.MIN_TIME);
        widgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_TIME,
                Utilities.MAX_TIME);
        widgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minimumVisibleTime);
        widgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maximumVisibleTime);

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
        // widgets therein for each different
        // tab selection.
        eventTabFolder = new CTabFolder(tabTop, SWT.TOP);
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
                    (String) eventDict.get(Utilities.HAZARD_EVENT_IDENTIFIER),
                    (String) eventDict.get(Utilities.HAZARD_EVENT_FULL_TYPE));
            tabItem.setData(eventDict.get(Utilities.HAZARD_EVENT_IDENTIFIER));
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

        // Create the category and hazard type
        // grouping, and a subpanel within it
        // that will hold the category and type
        // widgets. The latter is needed be-
        // cause the subpanel will be enabled
        // and disabled to give the combo boxes
        // a read-only status without making
        // them look disabled.
        Group hazardGroup = new Group(top, SWT.NONE);
        hazardGroup.setText("Hazard Type");
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
        categoryLabel.setText("Hazard Category:");
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
        label.setText("Hazard Type:");
        gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        label.setLayoutData(gridData);
        typeCombo = new Combo(hazardSubGroup, SWT.READ_ONLY);
        if (primaryParamValues.size() > 0) {
            populateHazardTypesList((String) primaryParamValues.get(
                    visibleHazardIndex).get(Utilities.HAZARD_EVENT_CATEGORY));
            if (primaryParamValues.get(visibleHazardIndex).get(
                    Utilities.HAZARD_EVENT_FULL_TYPE) != null) {
                typeCombo.setText((String) primaryParamValues.get(
                        visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
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
        timeRangePanel.setText("Time Range");
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
            Map<String, String> stateLabels = new HashMap<String, String>();
            stateLabels.put(START_TIME_STATE, "Start Time:");
            stateLabels.put(END_TIME_STATE, "End Time:");
            scaleObject.put(TimeScaleSpecifier.MEGAWIDGET_STATE_LABELS,
                    stateLabels);
            scaleObject.put(MegawidgetSpecifier.MEGAWIDGET_SPACING, 5);
            timeRangeWidget = (TimeScaleMegawidget) (new TimeScaleSpecifier(
                    scaleObject)).createMegawidget(timeRangePanel,
                    widgetCreationParams);
            timeScaleWidgets.add(timeRangeWidget);
            if (primaryParamValues.size() > 0) {
                timeRangeWidget.setUncommittedState(
                        START_TIME_STATE,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_START_TIME));
                timeRangeWidget.setUncommittedState(
                        END_TIME_STATE,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_END_TIME));
                timeRangeWidget.commitStateChanges();
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
                        Utilities.HAZARD_EVENT_CATEGORY,
                        categoryCombo.getText());
                typeCombo.select(0);
                typeCombo.notifyListeners(SWT.Selection, new Event());
            }
        });

        // Clear the various data structures holding metadata
        // widget information, since any such widgets should
        // be recreated at this point.
        panelsForTypes.clear();
        widgetsForIdsForTypes.clear();
        pointWidgetsForIdsForTypes.clear();

        // Show the currently selected hazard type's metadata
        // panel.
        if (primaryParamValues.size() > 0) {
            showMetadataForType((String) primaryParamValues.get(
                    visibleHazardIndex).get(Utilities.HAZARD_EVENT_FULL_TYPE));
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
                        Utilities.HAZARD_EVENT_CATEGORY,
                        categoryCombo.getText());
                primaryParamValues.get(visibleHazardIndex).put(
                        Utilities.HAZARD_EVENT_TYPE,
                        typeCombo.getText().split(" ")[0]);
                primaryParamValues.get(visibleHazardIndex).put(
                        Utilities.HAZARD_EVENT_FULL_TYPE, typeCombo.getText());
                Dict eventInfo = new Dict();
                eventInfo.put(
                        Utilities.HAZARD_EVENT_IDENTIFIER,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_IDENTIFIER));
                eventInfo.put(Utilities.HAZARD_EVENT_CATEGORY,
                        categoryCombo.getText());
                eventInfo.put(Utilities.HAZARD_EVENT_FULL_TYPE,
                        typeCombo.getText());
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
                fireHIDAction(new HazardDetailAction("updateEventType",
                        jsonText));

                // Show the metadata widgets for this type,
                // and synch them up with the event infor-
                // mation.
                if (showMetadataForType(typeCombo.getText()) == false) {
                    synchMetadataWidgetsWithEventInfo();
                }
            }
        });

        // Ensure that this panel is told when the window
        // is being hidden, and notifies any listener that
        // it is being dismissed in response.
        top.getShell().addListener(SWT.Hide, new Listener() {
            @Override
            public void handleEvent(Event event) {
                fireHIDAction(new HazardDetailAction("Dismiss"));
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
                                            Utilities.HAZARD_EVENT_IDENTIFIER),
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

    /**
     * Receive notification that the given specifier's widget has been invoked
     * or has changed state. Widgets will only call this method if they were
     * marked as being notifiers when they were constructed.
     * 
     * @param widget
     *            Widget that was invoked.
     * @param extraCallback
     *            Extra callback information associated with this widget, or
     *            <code>null</code> if no such extra information is provided.
     */
    @Override
    public void megawidgetInvoked(INotifier widget, String extraCallback) {

        // If there are no event dictionaries, do
        // nothing.
        if (primaryParamValues.size() == 0) {
            return;
        }

        // Create an event dictionary and add the event
        // identifier to it.
        Dict eventInfo = new Dict();
        eventInfo
                .put(Utilities.HAZARD_EVENT_IDENTIFIER,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_IDENTIFIER));

        // If the widget that was invoked is stateful,
        // get its state's value and place it in the
        // dictionary.
        if (widget instanceof IStateful) {
            List<String> identifiers = ((IStatefulSpecifier) widget
                    .getSpecifier()).getStateIdentifiers();

            // If the widget has one state identifier,
            // just place the state directly in the
            // dictionary; otherwise, create a sub-
            // dictionary that holds each state
            // identifier and the corresponding state
            // as key-value pairs, and place the sub-
            // dictionary in the dictionary.
            if (identifiers.size() == 1) {
                try {
                    eventInfo.put("value", ((IStateful) widget).getState(widget
                            .getSpecifier().getIdentifier()));
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.megawidgetInvoked(): "
                                    + "Error: Could not get value for state identifier \""
                                    + widget.getSpecifier().getIdentifier()
                                    + "\" for " + "megawidget \""
                                    + widget.getSpecifier().getIdentifier()
                                    + "\".", e);
                }
            } else {
                Dict stateInfo = new Dict();
                for (String identifier : identifiers) {
                    try {
                        stateInfo.put(identifier,
                                ((IStateful) widget).getState(identifier));
                    } catch (Exception e) {
                        statusHandler
                                .error("HazardDetailViewPart.megawidgetInvoked(): "
                                        + "Error: Could not get value for state "
                                        + "identifier \""
                                        + identifier
                                        + "\" for "
                                        + "widget \""
                                        + widget.getSpecifier().getIdentifier()
                                        + "\".", e);
                    }
                    eventInfo.put("value", stateInfo);
                }
            }
        }

        // Add in callback information if provided.
        if (extraCallback != null) {
            eventInfo.put("callback", extraCallback);
        }

        // Fire off an action indicating that this
        // widget was invoked.
        String eventString = null;
        try {
            eventString = eventInfo.toJSONString();
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.megawidgetInvoked(): "
                    + "Could not serialize JSON.", e);
        }
        fireHIDAction(new HazardDetailAction(widget.getSpecifier()
                .getIdentifier(), eventString));
    }

    /**
     * Receive notification that the given specifier's widget's state has
     * changed.
     * 
     * @param widget
     *            Widget that experienced the state change.
     * @param identifier
     *            Identifier of the state that has changed.
     * @param state
     *            New state.
     */
    @Override
    public void megawidgetStateChanged(IStateful widget, String identifier,
            Object state) {

        // If the widget that experienced a state
        // change is the time range widget, handle
        // it separately. If there are no event
        // dictionaries, do nothing.
        if (primaryParamValues.size() == 0) {
            return;
        } else if (widget == timeRangeWidget) {
            timeRangeChanged();
            return;
        }

        // Get the point widgets for the current type,
        // if any.
        boolean isPointWidget = false;
        Map<String, Megawidget> widgetsForIds = pointWidgetsForIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
        if (widgetsForIds != null) {

            // Determine whether this is a point
            // widget or an areal widget.
            for (Megawidget otherWidget : widgetsForIds.values()) {
                if (widget == otherWidget) {
                    isPointWidget = true;
                    break;
                }
            }

            // If this is a point widget, the table
            // needs updating.
            if (isPointWidget) {
                ((PointsTableMegawidget) widgetsForIds
                        .get(POINTS_TABLE_IDENTIFIER)).update();
            }
        }

        // If the widget is a point widget, then up-
        // date the value for the corresponding point;
        // otherwise, just update the main event's
        // corresponding value.
        String eventID;
        if (isPointWidget) {
            eventID = (String) pointsParamValues
                    .get(visibleHazardIndex)
                    .get(((PointsTableMegawidget) widgetsForIds
                            .get(POINTS_TABLE_IDENTIFIER))
                            .getSelectedRowIndex())
                    .get(Utilities.HAZARD_EVENT_IDENTIFIER);
            pointsParamValues
                    .get(visibleHazardIndex)
                    .get(((PointsTableMegawidget) widgetsForIds
                            .get(POINTS_TABLE_IDENTIFIER))
                            .getSelectedRowIndex()).put(identifier, state);
        } else {
            eventID = (String) primaryParamValues.get(visibleHazardIndex).get(
                    Utilities.HAZARD_EVENT_IDENTIFIER);
            primaryParamValues.get(visibleHazardIndex).put(identifier, state);
        }

        // Put together an action to be sent along to indicate
        // that the appropriate key's value has changed for this
        // event, and send it off.
        Dict eventInfo = new Dict();
        eventInfo.put(Utilities.HAZARD_EVENT_IDENTIFIER, eventID);
        eventInfo.put(identifier, state);
        String jsonText = null;
        try {
            jsonText = eventInfo.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "HazardDetailViewPart.megawidgetStateChanged(): conversion "
                            + "of event info to JSON string failed.", e);
        }
        fireHIDAction(new HazardDetailAction("updateEventMetadata", jsonText));
    }

    /**
     * Get the event information based upon the current values of the various
     * widgets.
     * 
     * @return JSON string.
     */
    public String getHidEventInfo() {

        // For each primary event, add it and any associated point
        // or auxiliary events to a list of dictionaries to be
        // returned.
        DictList eventsList = new DictList();
        for (int j = 0; j < primaryParamValues.size(); j++) {

            // Create the JSON object for the primary shape(s),
            // populating it with all the key-value pairings of
            // the original.
            Dict eventInfo = new Dict();
            for (String key : primaryParamValues.get(j).keySet()) {
                eventInfo.put(key, primaryParamValues.get(j).get(key));
            }

            // Add the primary event dictionary to the list.
            eventsList.add(eventInfo);

            // If there are point events and/or auxiliary
            // events associated with the main event, add
            // them to the events list as well.
            if (pointsParamValues.get(j) != null) {
                for (Map<String, Object> pointParamValues : pointsParamValues
                        .get(j)) {
                    eventsList.add(convertToJsonObject(pointParamValues));
                }
            }
            if (auxiliaryParamValues.get(j) != null) {
                for (Map<String, Object> auxParamValues : auxiliaryParamValues
                        .get(j)) {
                    eventsList.add(convertToJsonObject(auxParamValues));
                }
            }
        }

        // Write the event dictionaries list out to a
        // string.
        String jsonText = null;
        try {
            jsonText = eventsList.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "HazardDetailViewPart.getHidEventInfo(): write "
                            + "of JSON string failed.", e);
        }

        // Print out diagnostic info.
        // Gson gson = JSONUtilities.createPrettyGsonInterpreter();
        // statusHandler.debug("HID: JSON output of values: "
        // + gson.toJson(eventsList));

        // Return the result.
        return jsonText;
    }

    /**
     * Use the specified JSON string to set the values of the various widgets in
     * the window.
     * 
     * @param eventDictList
     *            List of dictionaries, each providing the key-value pairs that
     *            define a hazard event to be displayed.
     * @param jsonEventID
     *            JSON string specifying the identifier of the event that should
     *            be brought to the top of the tab list, or <code>null</code> if
     *            it should not be changed.
     */
    public void setHidEventInfo(DictList eventDictList, String jsonEventID) {

        // If there are no event dictionaries, ensure that
        // a zero-length list of them exists at least.
        if (eventDictList == null) {
            eventDictList = new DictList();
        }

        // Parse the passed-in JSON and configure the
        // widgets accordingly.
        List<String> eventIDs = new ArrayList<String>();
        List<String> types = new ArrayList<String>();
        try {

            // Determine whether the events should be
            // interpreted as a single primary and zero
            // or more point events, or as a list of
            // primary events.
            Map<?, ?> firstEvent = (Map<?, ?>) (eventDictList.size() > 0 ? eventDictList
                    .get(0) : null);
            boolean isArealAndPoints = ((firstEvent != null) && firstEvent
                    .containsKey(Utilities.HAZARD_EVENT_GROUP_IDENTIFIER));

            // Clear the event tracking lists.
            primaryParamValues.clear();
            pointsParamValues.clear();
            auxiliaryParamValues.clear();

            // Iterate through the primary events, getting
            // the information for each in turn.
            for (int j = 0; j < (isArealAndPoints ? 1 : eventDictList.size()); j++) {

                // Add the primary event dictionary for
                // this event.
                Map<String, Object> eventDict = eventDictList
                        .getDynamicallyTypedValue(j);
                primaryParamValues.add(eventDict);

                // If the JSON does not include the hazard
                // type, set it to nothing; it should then
                // have a category listed, and if it does
                // not, complain.
                String fullType = (String) eventDict
                        .get(Utilities.HAZARD_EVENT_FULL_TYPE);
                if ((fullType == null) || (fullType.length() == 0)) {
                    fullType = "";
                    String category = (String) eventDict
                            .get(Utilities.HAZARD_EVENT_CATEGORY);
                    if (category == null) {
                        statusHandler
                                .warn("HazardDetailViewPart.setHidEventInfo(): "
                                        + "Problem: for empty hazard type, could "
                                        + "not find hazard category!");
                    } else {
                        categoriesForTypes.put("", category);
                    }
                    eventDict.put(Utilities.HAZARD_EVENT_FULL_TYPE, fullType);
                }
                if (eventDict.get(Utilities.HAZARD_EVENT_CATEGORY) == null) {
                    eventDict.put(Utilities.HAZARD_EVENT_CATEGORY,
                            categoriesForTypes.get(fullType));
                }

                // Add this event identifier and type to the
                // lists of these respective parameters being
                // compiled.
                eventIDs.add((String) eventDict
                        .get(Utilities.HAZARD_EVENT_IDENTIFIER));
                types.add((String) eventDict
                        .get(Utilities.HAZARD_EVENT_FULL_TYPE));

                // Ensure that the start and end times are long
                // integer objects; they may be generic number ob-
                // jects due to JSON parsing.
                eventDict.put(Utilities.HAZARD_EVENT_START_TIME,
                        ((Number) eventDict
                                .get(Utilities.HAZARD_EVENT_START_TIME))
                                .longValue());
                eventDict.put(Utilities.HAZARD_EVENT_END_TIME,
                        ((Number) eventDict
                                .get(Utilities.HAZARD_EVENT_END_TIME))
                                .longValue());

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
                    boolean keepPointsSeparate = pointWidgetsForTypes
                            .containsKey(fullType);
                    if (keepPointsSeparate) {
                        statusHandler
                                .debug("HazardDetailViewPart.setHidEventInfo(): "
                                        + "For hazard type of \""
                                        + fullType
                                        + "\", points (if found in JSON) should be "
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
                                .get(Utilities.HAZARD_EVENT_SHAPES);
                        boolean listAsPoint = false;
                        if (keepPointsSeparate && (shapeList != null)) {
                            Dict shape = (Dict) shapeList.get(0);
                            if (shape
                                    .get(Utilities.HAZARD_EVENT_SHAPE_TYPE)
                                    .equals(Utilities.HAZARD_EVENT_SHAPE_TYPE_CIRCLE)
                                    || shape.get(
                                            Utilities.HAZARD_EVENT_SHAPE_TYPE)
                                            .equals(Utilities.HAZARD_EVENT_SHAPE_TYPE_POINT)) {
                                listAsPoint = true;
                            }
                        }
                        if (listAsPoint) {
                            if (thisPointsParamValues == null) {
                                thisPointsParamValues = new ArrayList<Map<String, Object>>();
                            }
                            thisPointsParamValues.add(auxiliary);
                        } else {
                            if (thisAuxiliaryParamValues == null) {
                                thisAuxiliaryParamValues = new ArrayList<Map<String, Object>>();
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
                    + "Problem reading event dictionary JSON.", e);
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

            // Bring the tab to the top for the event that
            // belongs in the front.
            if (eventTabFolder.getItemCount() > 0) {
                CTabItem tabItemToSelect = null;
                if (jsonEventID != null) {
                    for (CTabItem tabItem : eventTabFolder.getItems()) {
                        if (tabItem.getData().equals(jsonEventID)) {
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

        // Iterate through the time scale widgets, changing their
        // visible time ranges to match the new range.
        for (TimeScaleMegawidget widget : timeScaleWidgets) {
            widget.setVisibleTimeRange(minimumVisibleTime, maximumVisibleTime);
        }
    }

    // Package Methods

    /**
     * Determine whether or not the view is currently docked.
     * 
     * @return True if the view is currently docked, false otherwise.
     */
    final boolean isDocked() {
        return docked;
    }

    // Private Methods

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
                if (((String) eventDict.get(Utilities.HAZARD_EVENT_FULL_TYPE))
                        .length() == 0) {
                    enable = false;
                    break;
                }
            }
        }
        previewButton = createButton(buttonBar, PREVIEW_ID, "  Preview...  ");
        previewButton.setEnabled(enable);
        previewButton.setToolTipText("Preview the text product");
        proposeButton = createButton(buttonBar, PROPOSE_ID, "  Propose  ");
        proposeButton.setEnabled(enable);
        proposeButton.setToolTipText("Propose the event");
        issueButton = createButton(buttonBar, ISSUE_ID, "  Issue...  ");
        issueButton.setEnabled(enable);
        issueButton.setToolTipText("Issue the event");
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
            fireHIDAction(new HazardDetailAction("Propose"));
        } else if (buttonId == ISSUE_ID) {
            fireHIDAction(new HazardDetailAction("Issue"));
        } else if (buttonId == PREVIEW_ID) {
            fireHIDAction(new HazardDetailAction("Preview"));
        }
    }

    /**
     * Determine whether or not the view is now docked.
     * 
     * @return True if the view is docked, false otherwise.
     */
    private boolean determineWhetherDocked() {
        return (parent.getShell().getText().length() > 0);
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

        // Get the point widget specifiers for the
        // current type.
        List<MegawidgetSpecifier> widgetSpecifiers = pointWidgetsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
        if (widgetSpecifiers == null) {
            statusHandler
                    .info("HazardDetailViewPart.configurePointsTable(): Could "
                            + "not find point megawidget specifiers for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    Utilities.HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }

        // Count the number of state identifiers
        // that any stateful widgets in the point
        // widget specifiers include.
        int numColumns = 0;
        for (MegawidgetSpecifier widgetSpecifier : widgetSpecifiers) {
            if ((widgetSpecifier instanceof IStatefulSpecifier)
                    && ((widgetSpecifier instanceof PointsTableSpecifier) == false)) {
                numColumns += ((IStatefulSpecifier) widgetSpecifier)
                        .getStateIdentifiers().size();
            }
        }

        // Create the headers and identifiers
        // arrays, and the array holding the rela-
        // tive weights of the columns. The first
        // column is always reserved for the name
        // of the point, which does not have a
        // corresponding widget specifier.
        String[] titles = new String[numColumns + 1];
        String[] identifiers = new String[numColumns + 1];
        int[] relativeWeights = new int[numColumns + 1];
        titles[0] = "Name";
        identifiers[0] = POINTS_TABLE_NAME_COLUMN_IDENTIFIER;

        // Skipping the first column, which is al-
        // ways for the name of the point, iterate
        // through the state identifiers of the
        // stateful widget specifiers, dedicating
        // a column to each.
        int col = 1;
        int totalWeight = 0;
        for (MegawidgetSpecifier widgetSpecifier : widgetSpecifiers) {
            if ((widgetSpecifier instanceof IStatefulSpecifier) == false) {
                continue;
            }
            IStatefulSpecifier statefulSpecifier = (IStatefulSpecifier) widgetSpecifier;
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

        // Get the point widgets for the current type.
        Map<String, Megawidget> widgetsForIds = pointWidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
        if (widgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.populatePointsTable(): Could "
                            + "not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    Utilities.HAZARD_EVENT_FULL_TYPE) + "\".");
            return;
        }
        if (pointsParamValues.get(visibleHazardIndex) == null) {
            statusHandler
                    .info("HazardDetailViewPart.populatePointsTable(): No "
                            + "points parameter values were found for type \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    Utilities.HAZARD_EVENT_FULL_TYPE) + "\".");
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
                            .get(Utilities.HAZARD_EVENT_SHAPES)).get(0))
                            .get(identifier);
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
                        description = ((IStateful) widgetsForIds
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
                    //
                    // // Set the item as checked.
                    // item.setChecked(true);
                }
            }
        }

        // Select the first row.
        table.setSelection(0);

        // Update the point widgets' values to
        // match the first row of the table.
        updatePointWidgetValues(table);
    }

    /**
     * Update the points table's selected row to match those of the points
     * widgets.
     * 
     * @param table
     *            Points table.
     */
    private void updatePointsTable(Table table) {

        // If the table has no lines, do nothing.
        if (table.getItemCount() == 0) {
            return;
        }

        // Get the point widgets for the current type.
        Map<String, Megawidget> widgetsForIds = pointWidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
        if (widgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.updatePointsTable(): Could "
                            + "not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    Utilities.HAZARD_EVENT_FULL_TYPE) + "\".");
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
        // the widgets.
        for (int col = 1; col < table.getColumnCount(); col++) {
            String identifier = (String) table.getColumn(col).getData();
            IStateful widget = (IStateful) widgetsForIds.get(identifier);
            try {
                Object state = widget.getState(identifier);
                items[0].setData(identifier, state);
                items[0].setText(col,
                        widget.getStateDescription(identifier, state));
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
     * Update the point widgets' values to match those of the points table's
     * selected row.
     * 
     * @param table
     *            Points table.
     */
    private void updatePointWidgetValues(Table table) {

        // If the table has no lines, do nothing.
        if (table.getItemCount() == 0) {
            return;
        }

        // Get the point widgets for the current type.
        Map<String, Megawidget> widgetsForIds = pointWidgetsForStateIdsForTypes
                .get(primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_FULL_TYPE));
        if (widgetsForIds == null) {
            statusHandler
                    .info("HazardDetailViewPart.updatePointWidgetValues(): "
                            + "Could not find point megawidgets for type = \""
                            + primaryParamValues.get(visibleHazardIndex).get(
                                    Utilities.HAZARD_EVENT_FULL_TYPE) + "\".");
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

        // Iterate through the point widgets, setting
        // their states to match the states found in
        // the selected row of the table.
        Set<IExplicitCommitStateful> widgetsNeedingCommit = new HashSet<IExplicitCommitStateful>();
        for (String identifier : widgetsForIds.keySet()) {
            if ((widgetsForIds.get(identifier) instanceof IStateful) == false) {
                continue;
            }
            IStateful widget = (IStateful) widgetsForIds.get(identifier);
            if (widget instanceof PointsTableMegawidget) {
                continue;
            }
            try {
                if (widget instanceof IExplicitCommitStateful) {
                    ((IExplicitCommitStateful) widget).setUncommittedState(
                            identifier, items[0].getData(identifier));
                    widgetsNeedingCommit.add((IExplicitCommitStateful) widget);
                } else {
                    widget.setState(identifier, items[0].getData(identifier));
                }
            } catch (Exception e) {
                statusHandler.error(
                        "HazardDetailViewPart.updatePointsTable(): "
                                + "Unable to update point megawidget "
                                + identifier + " to have state = "
                                + items[0].getData(identifier), e);
            }
        }
        for (IExplicitCommitStateful widget : widgetsNeedingCommit) {
            try {
                widget.commitStateChanges();
            } catch (Exception e) {
                statusHandler
                        .error("HazardDetailViewPart.updatePointsTable(): "
                                + "Unable to commit change(s) to state in point "
                                + "megawidget "
                                + widget.getSpecifier().getIdentifier(), e);
            }
        }
    }

    /**
     * Set the part's widgets' values to match the current event info.
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

        // Set the category and type combo boxes.
        String category = getCategoryOfCurrentEvent();
        categoryCombo.setText(category);
        populateHazardTypesList(category);
        String type = (String) primaryParamValues.get(visibleHazardIndex).get(
                Utilities.HAZARD_EVENT_FULL_TYPE);
        typeCombo.setText(type);

        // Set the start and end time in the time
        // range widgets.
        try {
            timeRangeWidget.setUncommittedState(
                    START_TIME_STATE,
                    primaryParamValues.get(visibleHazardIndex).get(
                            Utilities.HAZARD_EVENT_START_TIME));
            timeRangeWidget.setUncommittedState(
                    END_TIME_STATE,
                    primaryParamValues.get(visibleHazardIndex).get(
                            Utilities.HAZARD_EVENT_END_TIME));
            timeRangeWidget.commitStateChanges();
        } catch (Exception e) {
            statusHandler
                    .error("HazardDetailViewPart.synchWithEventInfo(): "
                            + "Error: Could not set the state for time range megawidget.",
                            e);
        }

        // Show the metadata for this type; if it
        // was already showing, then the widgets'
        // states still have to be synced with
        // the current values.
        if (showMetadataForType(type) == false) {
            synchMetadataWidgetsWithEventInfo();
        }
    }

    /**
     * Synch the metadata widgets with current hazard event information.
     */
    private void synchMetadataWidgetsWithEventInfo() {

        // Synch the main metadata widgets.
        String fullType = (String) primaryParamValues.get(visibleHazardIndex)
                .get(Utilities.HAZARD_EVENT_FULL_TYPE);
        setWidgetsStates(widgetsForIdsForTypes.get(fullType),
                primaryParamValues.get(visibleHazardIndex));

        // If point widgets exist, synch them as well, in-
        // cluding the points table.
        Map<String, Megawidget> pointWidgetsForIds = pointWidgetsForIdsForTypes
                .get(fullType);
        Map<String, Object> pointParamValues = (pointsParamValues
                .get(visibleHazardIndex) != null ? pointsParamValues.get(
                visibleHazardIndex).get(0) : null);
        if (pointWidgetsForIds != null) {
            setPointWidgetsEnabled(pointWidgetsForIds,
                    (pointParamValues != null));
            setWidgetsStates(pointWidgetsForIds, pointParamValues);
            ((PointsTableMegawidget) pointWidgetsForIds
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
                .get(Utilities.HAZARD_EVENT_CATEGORY);
        if (category == null) {
            category = categoriesForTypes.get(primaryParamValues.get(
                    visibleHazardIndex).get(Utilities.HAZARD_EVENT_FULL_TYPE));
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
            // fic widgets are needed for this hazard
            // type; if so, construct a tab folder
            // with two tabs, one for general meta-
            // data widgets, the other for the point-
            // specific widgets; otherwise, just
            // construct a single group holding the
            // general metadata widgets if the latter
            // are required, or an empty panel other-
            // wise.
            List<MegawidgetSpecifier> pointWidgetSpecifiers = pointWidgetsForTypes
                    .get(type);
            if (pointWidgetSpecifiers != null) {

                // Create the tab folder widget.
                panel = new CTabFolder(metadataContentPanel, SWT.TOP);
                ((CTabFolder) panel).setBorderVisible(true);
                ((CTabFolder) panel).setTabHeight(((CTabFolder) panel)
                        .getTabHeight() + 8);

                // Create the area tab page to hold the
                // general area widgets.
                CTabItem tabItem = new CTabItem((CTabFolder) panel, SWT.NONE);
                tabItem.setText(" Area Details ");
                Composite areaPage = new Composite(panel, SWT.NONE);
                Map<String, Megawidget> widgetsForIds = new HashMap<String, Megawidget>();
                addWidgetsToPanel(widgetsForTypes.get(type), areaPage,
                        widgetsForIds, null,
                        primaryParamValues.get(visibleHazardIndex));
                widgetsForIdsForTypes.put(type, widgetsForIds);
                tabItem.setControl(areaPage);

                // Create points tab page to hold the
                // points-specific widgets.
                tabItem = new CTabItem((CTabFolder) panel, SWT.NONE);
                tabItem.setText("Points Details");
                Composite pointsPage = new Composite(panel, SWT.NONE);
                widgetsForIds = new HashMap<String, Megawidget>();
                Map<String, Megawidget> widgetsForStateIds = new HashMap<String, Megawidget>();
                addWidgetsToPanel(
                        pointWidgetSpecifiers,
                        pointsPage,
                        widgetsForIds,
                        widgetsForStateIds,
                        (pointsParamValues.get(visibleHazardIndex) == null ? null
                                : pointsParamValues.get(visibleHazardIndex)
                                        .get(0)));
                pointWidgetsForIdsForTypes.put(type, widgetsForIds);
                pointWidgetsForStateIdsForTypes.put(type, widgetsForStateIds);
                ((PointsTableMegawidget) widgetsForIds
                        .get(POINTS_TABLE_IDENTIFIER)).populate();
                if (pointsParamValues.get(visibleHazardIndex) == null) {
                    setPointWidgetsEnabled(widgetsForIds, false);
                }
                tabItem.setControl(pointsPage);

                // Lay out the tab folder.
                panel.pack();
            } else if ((widgetsForTypes.get(type) != null)
                    && (widgetsForTypes.get(type).size() > 0)) {

                // Create the group panel to hold all the
                // widgets.
                panel = new Group(metadataContentPanel, SWT.NONE);
                ((Group) panel).setText("Details");
                Map<String, Megawidget> widgetsForIds = new HashMap<String, Megawidget>();
                addWidgetsToPanel(widgetsForTypes.get(type), panel,
                        widgetsForIds, null,
                        primaryParamValues.get(visibleHazardIndex));
                widgetsForIdsForTypes.put(type, widgetsForIds);
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
                visibleHazardIndex).get(Utilities.HAZARD_EVENT_IDENTIFIER));
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
     * Enable or disable the specified point widgets.
     * 
     * @param widgetsForIds
     *            Hash table mapping identifiers to widgets, the latter forming
     *            collectively the set of widgets to be enabled or disabled.
     * @param enabled
     *            Flag indicating whether to enable or disable the widgets.
     */
    private void setPointWidgetsEnabled(Map<String, Megawidget> widgetsForIds,
            boolean enabled) {
        for (Megawidget widget : widgetsForIds.values()) {
            widget.setEnabled(enabled);
        }
    }

    /**
     * Respond to the start or end times changing.
     */
    private void timeRangeChanged() {

        // Do nothing if the appropriate widget is not
        // found.
        if (timeRangeWidget == null) {
            return;
        }

        // Get the times and save them.
        try {
            Long startTime = (Long) timeRangeWidget.getState(START_TIME_STATE);
            if (startTime == null) {
                return;
            }
            primaryParamValues.get(visibleHazardIndex).put(
                    Utilities.HAZARD_EVENT_START_TIME, startTime);
            Long endTime = (Long) timeRangeWidget.getState(END_TIME_STATE);
            if (endTime == null) {
                return;
            }
            primaryParamValues.get(visibleHazardIndex).put(
                    Utilities.HAZARD_EVENT_END_TIME, endTime);
        } catch (Exception e) {
            statusHandler.error("HazardDetailViewPart.timeRangeChanged(): "
                    + "could not get state from time range megawidgets.", e);
        }

        // Generate a HID action and fire it off.
        Dict eventInfo = new Dict();
        eventInfo
                .put(Utilities.HAZARD_EVENT_IDENTIFIER,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_IDENTIFIER));
        eventInfo
                .put(Utilities.HAZARD_EVENT_START_TIME,
                        primaryParamValues.get(visibleHazardIndex).get(
                                Utilities.HAZARD_EVENT_START_TIME));
        eventInfo.put(
                Utilities.HAZARD_EVENT_END_TIME,
                primaryParamValues.get(visibleHazardIndex).get(
                        Utilities.HAZARD_EVENT_END_TIME));
        String jsonText = null;
        try {
            jsonText = eventInfo.toJSONString();
        } catch (Exception e) {
            statusHandler.error(
                    "HazardDetailViewPart.timeRangeChanged(): conversion "
                            + "of event info to JSON string failed.", e);
        }
        fireHIDAction(new HazardDetailAction("updateTimeRange", jsonText));
    }

    /**
     * Add metadata widgets as specified to the provided panel.
     * 
     * @param widgetSpecifiers
     *            List of widget specifiers indicating the widgets to be added.
     * @param panel
     *            Panel to which to add the widgets.
     * @param widgetsForIds
     *            Hash table pairing widget identifiers with their associated
     *            widgets. All created widgets have entries created within this
     *            table during the course of the invocation of this method, one
     *            per widget.
     * @param widgetsForStateIds
     *            Hash table pairing state identifiers with their associated
     *            widgets. All created widgets have entries created within this
     *            table during the course of the invocation of this method, one
     *            per state held by a widget, so if a widget has two states, it
     *            will have two entries in the table. If <code>null</code>, no
     *            recording of widgets paired with state identifiers occurs.
     * @param paramValues
     *            Hash table pairing state identifiers with their starting state
     *            values, if any.
     */
    private void addWidgetsToPanel(List<MegawidgetSpecifier> widgetSpecifiers,
            Composite panel, Map<String, Megawidget> widgetsForIds,
            Map<String, Megawidget> widgetsForStateIds,
            Map<String, Object> paramValues) {

        // Create the grid layout for the panel.
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 7;
        panel.setLayout(gridLayout);

        // Create the widgets based on the specifica-
        // tions, adding an entry for each to the
        // hash table pairing them with their identi-
        // fiers; if they are also stateful, then
        // add entries to the other table pairing
        // them with their states, one entry per
        // state that each contains.
        Set<Megawidget> megawidgets = new HashSet<Megawidget>();
        for (MegawidgetSpecifier widgetSpecifier : widgetSpecifiers) {
            Megawidget widget = null;
            try {
                widget = widgetSpecifier.createMegawidget(panel,
                        widgetCreationParams);
            } catch (MegawidgetException e) {
                statusHandler
                        .error("HazardDetailViewPart.addWidgetsToPanel(): Hazard "
                                + "event type metadata megawidget creation error.",
                                e);
            }
            megawidgets.add(widget);
            widgetsForIds.put(widgetSpecifier.getIdentifier(), widget);
            if ((widgetsForStateIds != null) && (widget instanceof IStateful)) {
                for (String identifier : ((IStatefulSpecifier) widgetSpecifier)
                        .getStateIdentifiers()) {
                    widgetsForStateIds.put(identifier, widget);
                }
            }
            if (widget instanceof TimeScaleMegawidget) {
                timeScaleWidgets.add((TimeScaleMegawidget) widget);
            }
        }

        // Align the megawidgets visually with one
        // another.
        Megawidget.alignMegawidgetsElements(megawidgets);

        // Set the widgets' states to match the
        // initial values.
        setWidgetsStates(widgetsForIds, paramValues);

        // Lay out the panel.
        panel.pack();
    }

    /**
     * Set the metadata widgets' states as specified.
     * 
     * @param widgetsForIds
     *            Hash table pairing widget identifiers with their associated
     *            widgets. All created widgets have entries created within the
     *            table during the course of the invocation of this method.
     * @param paramValues
     *            Hash table pairing widget identifiers with their starting
     *            state values, if any.
     */
    private void setWidgetsStates(Map<String, Megawidget> widgetsForIds,
            Map<String, Object> paramValues) {

        // If there are initial state values, set the
        // widgets to match them. If there is no state
        // value for a particular state identifier and
        // the widget has a default value for it, make
        // an entry in the hazard event mapping that
        // identifier to the default value. This ensures
        // that default values for megawidgets are used
        // as default values for hazard events.
        if ((paramValues != null) && (widgetsForIds != null)) {

            // Iterate through the widgets, finding any
            // that are stateful and for those, setting
            // the states to match those given in the
            // parameter values dictionary, or if the
            // latter has no value for a given state
            // identifier and the widget has a starting
            // value, adding a mapping for that state
            // identifier and default value to the
            // dictionary.
            Set<String> identifiersGivenDefaultValues = new HashSet<String>();
            Set<IExplicitCommitStateful> widgetsNeedingCommit = new HashSet<IExplicitCommitStateful>();
            for (String widgetIdentifier : widgetsForIds.keySet()) {
                Megawidget widget = widgetsForIds.get(widgetIdentifier);
                if ((widget instanceof IStateful) == false) {
                    continue;
                }
                for (String identifier : ((IStatefulSpecifier) widget
                        .getSpecifier()).getStateIdentifiers()) {
                    if (paramValues.containsKey(identifier)) {
                        Object value = paramValues.get(identifier);
                        try {
                            setWidgetState((IStateful) widget, identifier,
                                    value, widgetsNeedingCommit);
                        } catch (Exception e) {
                            statusHandler
                                    .error("HazardDetailViewPart.setWidgetsStates(): "
                                            + "Unable to set state for "
                                            + identifier + " to " + value + ".",
                                            e);
                        }
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
                            Object defaultValue = ((IStatefulSpecifier) widget
                                    .getSpecifier())
                                    .getStartingState(identifier);
                            if (defaultValue != null) {
                                identifiersGivenDefaultValues.add(identifier);
                                paramValues.put(identifier, defaultValue);
                                Object widgetState = ((IStateful) widget)
                                        .getState(identifier);
                                if ((widgetState == null)
                                        || (widgetState.equals(defaultValue) == false)) {
                                    setWidgetState((IStateful) widget,
                                            identifier, defaultValue,
                                            widgetsNeedingCommit);
                                }
                            } else {
                                defaultValue = ((IStateful) widget)
                                        .getState(identifier);
                                if (defaultValue != null) {
                                    identifiersGivenDefaultValues
                                            .add(identifier);
                                    paramValues.put(identifier, defaultValue);
                                }
                            }
                        } catch (Exception e) {
                            statusHandler
                                    .error("HazardDetailViewPart.setWidgetsStates(): "
                                            + "Unable to fetch default state for "
                                            + identifier + ".", e);
                        }
                    }
                }
            }

            // Commit changes to any widgets that must
            // be explicitly commit.
            for (IExplicitCommitStateful widget : widgetsNeedingCommit) {
                try {
                    widget.commitStateChanges();
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.setWidgetStates(): "
                                    + "Unable to commit change(s) to state for "
                                    + widget.getSpecifier().getIdentifier(), e);
                }
            }

            // If any default values were taken from the
            // widgets and placed in the event dictionary,
            // send off a notification that these values
            // changed.
            if (identifiersGivenDefaultValues.size() > 0) {
                Dict eventInfo = new Dict();
                eventInfo.put(Utilities.HAZARD_EVENT_IDENTIFIER,
                        paramValues.get(Utilities.HAZARD_EVENT_IDENTIFIER));
                for (String identifier : identifiersGivenDefaultValues) {
                    eventInfo.put(identifier, paramValues.get(identifier));
                }
                String jsonText = null;
                try {
                    jsonText = eventInfo.toJSONString();
                } catch (Exception e) {
                    statusHandler
                            .error("HazardDetailViewPart.setWidgetsStates(): conversion "
                                    + "of event info to JSON string failed.", e);
                }
                hazardDetailView.fireAction(new HazardDetailAction(
                        "updateEventMetadata", jsonText), true);
            }
        }
    }

    /**
     * Set the specified stateful megawidget to hold the specified value for the
     * specified state identifier, recording it as requiring an explicit commit
     * if this is the case.
     * 
     * @param widget
     *            Megawidget to have its state set.
     * @param identifier
     *            State identifier to be set.
     * @param value
     *            New value of the state identifier.
     * @param widgetsNeedingCommit
     *            Set of megawidgets needing an explicit commit; <code>widget
     *            </code> will be added to this set by this invocation if it is
     *            an <code>IExplicitCommitStateful</code> instance.
     * @throws MegawidgetStateException
     *             If a state exception occurs while attempting to set the
     *             megawidget's state.
     */
    private void setWidgetState(IStateful widget, String identifier,
            Object value, Set<IExplicitCommitStateful> widgetsNeedingCommit)
            throws MegawidgetStateException {
        if (widget instanceof IExplicitCommitStateful) {
            ((IExplicitCommitStateful) widget).setUncommittedState(identifier,
                    value);
            widgetsNeedingCommit.add((IExplicitCommitStateful) widget);
        } else {
            widget.setState(identifier, value);
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
        List<Dict> objects = new ArrayList<Dict>();
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
     * Convert the specified object to a JSON object, as appropriate.
     * 
     * @param object
     *            Object to be converted.
     * @return JSON version.
     */
    @SuppressWarnings("unchecked")
    private Object convertToJsonObject(Object object) {
        if ((object instanceof DictList) || (object instanceof Dict)) {
            return object;
        } else if (object instanceof List) {
            DictList jsonArray = new DictList();
            for (Object item : (List<?>) object) {
                jsonArray.add(convertToJsonObject(item));
            }
            return jsonArray;
        } else if (object instanceof Map) {
            Dict jsonObject = new Dict();
            Map<String, ?> map = (Map<String, ?>) object;
            for (String key : map.keySet()) {
                jsonObject.put(key, convertToJsonObject(map.get(key)));
            }
            return jsonObject;
        } else {
            return object;
        }
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
