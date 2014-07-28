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

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.Command;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.DisplayableEventIdentifier;
import gov.noaa.gsd.viz.hazards.ui.DockTrackingViewPart;
import gov.noaa.gsd.viz.megawidgets.CheckBoxMegawidget;
import gov.noaa.gsd.viz.megawidgets.ComboBoxMegawidget;
import gov.noaa.gsd.viz.megawidgets.ComboBoxSpecifier;
import gov.noaa.gsd.viz.megawidgets.ControlComponentHelper;
import gov.noaa.gsd.viz.megawidgets.IControl;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.IMegawidgetManagerListener;
import gov.noaa.gsd.viz.megawidgets.IParentSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISingleLineSpecifier;
import gov.noaa.gsd.viz.megawidgets.ISpecifier;
import gov.noaa.gsd.viz.megawidgets.IStateChangeListener;
import gov.noaa.gsd.viz.megawidgets.IStateful;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierFactory;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeScaleMegawidget;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
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
 * Jan 31, 2014   2710     Chris.Golden      Fixed bug that caused exceptions to be
 *                                           thrown because the minimum interval between
 *                                           the start and end time of a hazard here was
 *                                           a minute, whereas it was 0 in the HID. Also
 *                                           added use of a constant in HazardConstants
 *                                           to ensure that all time range widgets use
 *                                           the same minimum interval.
 * Feb 10, 2014   2161     Chris.Golden      Added "Until Further Notice" option for end
 *                                           times and fall-below times for time range
 *                                           megawidgets. Also corrected Javadoc, and
 *                                           added code to support TimeMegawidget in
 *                                           addition to TimeScaleMegawidget.
 * Apr 09, 2014   2925     Chris.Golden      Minor changes to support first round of
 *                                           class-based metadata changes, as well as
 *                                           to conform to new event propagation
 *                                           scheme.
 * May 15, 2014   2925     Chris.Golden      Together with changes made in last 2925
 *                                           changeset, essentially rewritten to provide
 *                                           far better separation of concerns between
 *                                           model, view, and presenter; switch to
 *                                           scheme whereby the model is changed directly
 *                                           by the presenter instead of via messages
 *                                           from the presenter or view, and model
 *                                           changes are detected via various event-bus-
 *                                           listener methods; and preparation for
 *                                           multithreading in the future. Also added
 *                                           delegates to separate concerns between
 *                                           view part (the principal) and view (its
 *                                           delegate). Also cleaned up GUI to include
 *                                           horizontal scrollbar when necessary for
 *                                           metadata; use of megawidget manager instead
 *                                           of old direct-megawidget-management code;
 *                                           and removal of hazard category, type, and
 *                                           time range from the scrollable area of the
 *                                           dialog.
 * Jun 23, 2014   4010     Chris.Golden      Changed to work with megawidget manager
 *                                           changes, as well as adding a resize listener
 *                                           that resizes the scrollable area when a
 *                                           megawidget changes its size.
 * Jun 25, 2014   4009     Chris.Golden      Added code to merge the extra data from a
 *                                           megawidget manager that is about to be
 *                                           replaced into the extra data for its
 *                                           replacement, allowing interdependency scripts
 *                                           to keep state in between executions even if
 *                                           the metadata megawidgets have changed.
 * Jun 30, 2014   3512     Chris.Golden      Changed to work with megawidget changes that
 *                                           allow notification to occur of simultaneous
 *                                           state changes. Also changed to work with new
 *                                           MVP widget framework alterations.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardDetailViewPart extends DockTrackingViewPart implements
        IHazardDetailView {

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
     * Start time state identifier.
     */
    private static final String START_TIME_STATE = "__startTime__";

    /**
     * End time state identifier.
     */
    private static final String END_TIME_STATE = "__endTime__";

    /**
     * Type of the combo box megawidget.
     */
    private static final String COMBOBOX_MEGAWIDGET_TYPE = "ComboBox";

    /**
     * Type of the time range megawidget.
     */
    private static final String TIMESCALE_MEGAWIDGET_TYPE = "TimeScale";

    /**
     * Type of the end time "until further notice" megawidget.
     */
    private static final String CHECKBOX_MEGAWIDGET_TYPE = "CheckBox";

    /**
     * Category combo box megawidget identifier.
     */
    private static final String CATEGORY_IDENTIFIER = "category";

    /**
     * Type combo box megawidget identifier.
     */
    private static final String TYPE_IDENTIFIER = "type";

    /**
     * Time range megawidget identifier.
     */
    private static final String TIME_RANGE_IDENTIFIER = START_TIME_STATE + ":"
            + END_TIME_STATE;

    /**
     * Hazard type section text.
     */
    private static final String HAZARD_TYPE_SECTION_TEXT = "Type";

    /**
     * Hazard category text.
     */
    private static final String HAZARD_CATEGORY_TEXT = "Category:";

    /**
     * Hazard category text.
     */
    private static final String HAZARD_TYPE_TEXT = "Type:";

    /**
     * Hazard time range section text.
     */
    private static final String TIME_RANGE_SECTION_TEXT = "Time Range";

    /**
     * Start time text.
     */
    private static final String START_TIME_TEXT = "Start:";

    /**
     * End time text.
     */
    private static final String END_TIME_TEXT = "End:";

    /**
     * Text for "until further notice" checkbox.
     */
    private static final String UNTIL_FURTHER_NOTICE_TEXT = "Until further notice";

    /**
     * Text to display in the date-time fields of the time range megawidget when
     * the "until further notice" value is the current state for the end time.
     */
    private static final String UNTIL_FURTHER_NOTICE_VALUE_TEXT = "N/A";

    /**
     * Details section text.
     */
    private static final String DETAILS_SECTION_TEXT = "Details";

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
     * Conflict image icon file name.
     */
    private static final String CONFLICT_ICON_IMAGE_FILE_NAME = "hidConflict.png";

    /**
     * Conflict tooltip.
     */
    private static final String CONFLICT_TOOLTIP = "Conflicts with other hazard(s)";

    /**
     * Specifier parameters for the category combo box megawidget.
     */
    private static final ImmutableMap<String, Object> CATEGORY_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ISpecifier.MEGAWIDGET_IDENTIFIER, CATEGORY_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, COMBOBOX_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, HAZARD_CATEGORY_TEXT);
        map.put(ISingleLineSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(ComboBoxSpecifier.MEGAWIDGET_VALUE_CHOICES,
                Lists.newArrayList(""));
        CATEGORY_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Specifier parameters for the type combo box megawidget.
     */
    private static final ImmutableMap<String, Object> TYPE_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(ISpecifier.MEGAWIDGET_IDENTIFIER, TYPE_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, COMBOBOX_MEGAWIDGET_TYPE);
        map.put(ISpecifier.MEGAWIDGET_LABEL, HAZARD_TYPE_TEXT);
        map.put(ISingleLineSpecifier.EXPAND_HORIZONTALLY, true);
        map.put(ComboBoxSpecifier.MEGAWIDGET_VALUE_CHOICES,
                Lists.newArrayList(""));
        TYPE_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Specifier parameters for the time range megawidget.
     */
    private static final ImmutableMap<String, Object> TIME_RANGE_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                TIME_RANGE_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, TIMESCALE_MEGAWIDGET_TYPE);
        map.put(IControlSpecifier.MEGAWIDGET_SPACING, 0);
        Map<String, Object> subMap = new HashMap<>();
        subMap.put(START_TIME_STATE, START_TIME_TEXT);
        subMap.put(END_TIME_STATE, END_TIME_TEXT);
        map.put(TimeScaleSpecifier.MEGAWIDGET_STATE_LABELS,
                ImmutableMap.copyOf(subMap));
        map.put(IControlSpecifier.MEGAWIDGET_SPACING, 5);
        map.put(TimeScaleSpecifier.MEGAWIDGET_MINIMUM_TIME_INTERVAL,
                HazardConstants.TIME_RANGE_MINIMUM_INTERVAL);
        map.put(IParentSpecifier.MEGAWIDGET_SPECIFIER_FACTORY,
                new MegawidgetSpecifierFactory());

        /*
         * Specify the "Until further notice" checkbox to be shown next to the
         * end time date-time fields.
         */
        subMap = new HashMap<>();
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_TYPE,
                CHECKBOX_MEGAWIDGET_TYPE);
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                UNTIL_FURTHER_NOTICE_TEXT);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(ImmutableMap.copyOf(subMap));
        subMap = new HashMap<>();
        subMap.put(END_TIME_STATE, ImmutableList.copyOf(list));
        map.put(TimeScaleSpecifier.MEGAWIDGET_DETAIL_FIELDS,
                ImmutableMap.copyOf(subMap));

        /*
         * Ensure that the time range megawidget shows special text if the
         * "Until further notice" value is the current state for its end time.
         */
        Map<Long, String> descriptiveTextForValues = new HashMap<>();
        descriptiveTextForValues.put(
                HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS,
                UNTIL_FURTHER_NOTICE_VALUE_TEXT);
        map.put(TimeScaleSpecifier.MEGAWIDGET_TIME_DESCRIPTORS,
                ImmutableMap.copyOf(descriptiveTextForValues));
        TIME_RANGE_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Maximum number of events for which metadata-related objects may be stored
     * by the cache.
     */
    private static final int MAXIMUM_EVENT_METADATA_CACHE_SIZE = 10;

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
     * Standard unselected tab background color for the event tab folder.
     */
    private Color standardUnselectedColor;

    /**
     * Standard selected tab background color for the event tab folder.
     */
    private Color standardSelectedColor;

    /**
     * Identifier of the event that is currently topmost in the tab folder and
     * thus visible.
     */
    private String visibleEventIdentifier;

    /**
     * Flag indicating whether or not tab pages are currently being created or
     * deleted.
     */
    private boolean tabsBeingChanged;

    /**
     * Tab folder holding the different hazard events, one per tab page.
     */
    private CTabFolder eventTabFolder;

    /**
     * Composite holding the contents of a tab page. The different pages all use
     * the same composite as their controls, with the composite's contents
     * changing each time a new tab page is selected.
     */
    private Composite tabPagePanel;

    /**
     * Hazard category combo box megawidget.
     */
    private ComboBoxMegawidget categoryMegawidget;

    /**
     * Hazard type combo box megawidget.
     */
    private ComboBoxMegawidget typeMegawidget;

    /**
     * Event time range megawidget.
     */
    private TimeScaleMegawidget timeRangeMegawidget;

    /**
     * Event end time "until further notice" toggle megawidget.
     */
    private CheckBoxMegawidget endTimeUntilFurtherNoticeMegawidget;

    /**
     * Metadata group panel.
     */
    private Group metadataGroup;

    /**
     * Grid layout data for the metadata group panel.
     */
    private GridData metadataGroupLayoutData;

    /**
     * Scrolled composite used to hold the metadata megawidgets.
     */
    private ScrolledComposite scrolledComposite;

    /**
     * Map of hazard event identifiers to the scrolled composite origins; the
     * latter are recorded each time the scrolled composite is scrolled.
     */
    private final Map<String, Point> scrollOriginsForEventIds = new HashMap<>();

    /**
     * Flag indicating whether or not the scrolled composite is having its
     * contents changed.
     */
    private boolean scrolledCompositeContentsChanging;

    /**
     * Flag indicating whether or not the scrolled composite is having its size
     * changed.
     */
    private boolean scrolledCompositePageIncrementChanging;

    /**
     * Content panel that holds whichever metadata panel is currently displayed.
     */
    private Composite metadataContentPanel;

    /**
     * Layout manager for the {@link #metadataContentPanel}.
     */
    private StackLayout metadataContentLayout;

    /**
     * List of hazard category identifiers.
     */
    private ImmutableList<String> categories;

    /**
     * Minimum visible time in the time range.
     */
    private long minimumVisibleTime = HazardConstants.MIN_TIME;

    /**
     * Maximum visible time in the time range.
     */
    private long maximumVisibleTime = HazardConstants.MAX_TIME;

    /**
     * Current time provider.
     */
    private ICurrentTimeProvider currentTimeProvider;

    /**
     * <p>
     * Cache of megawidget managers associated with event identifiers. Only the
     * most recently used megawidget managers are cached away; the maximum
     * number that can be cached is {@link #MAXIMUM_EVENT_METADATA_CACHE_SIZE}.
     * </p>
     * <p>
     * Note that this cache must be used carefully; its anonymous class has not
     * had its behavior changed to ensure that every way of dropping an entry
     * from the map results in the disposal of the megawidget manager's panel.
     * Only {@link LinkedHashMap#remove(Object)} performs the disposal. Thus, an
     * entry must be removed using this method, or of course by simply adding
     * enough items to cause the oldest one to be removed; any other way
     * (including a call to {@link LinkedHashMap#put(Object, Object)} that
     * happens to replace an entry with another because they have the same key)
     * will cause SWT resource leakage.
     * </p>
     * <p>
     * Furthermore, since {@link LinkedHashMap#get(Object)} (like
     * <code>put()</code>) changes the subject entry to be the most recently
     * accessed, it should not be used if there is no desire to reorder the
     * entries in the cache with respect to which ones are closest to being
     * dropped; instead, another method such as
     * {@link LinkedHashMap#containsKey(Object)} or
     * {@link LinkedHashMap#values()} may be used in such cases.
     * </p>
     */
    private final LinkedHashMap<String, MegawidgetManager> megawidgetManagersForEventIds = new LinkedHashMap<String, MegawidgetManager>(
            MAXIMUM_EVENT_METADATA_CACHE_SIZE + 1, 0.75f, true) {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 1L;

        // Public Methods

        @Override
        public final MegawidgetManager remove(Object key) {
            if (containsKey(key)) {
                prepareMegawidgetManagerForRemoval((String) key, get(key));
            }
            return super.remove(key);
        }

        // Protected Methods

        @Override
        protected final boolean removeEldestEntry(
                Map.Entry<String, MegawidgetManager> eldest) {
            if (size() > MAXIMUM_EVENT_METADATA_CACHE_SIZE) {
                prepareMegawidgetManagerForRemoval(eldest.getKey(),
                        eldest.getValue());
                return true;
            } else {
                return false;
            }
        }
    };

    /**
     * Map of event identifiers to maps holding extra data for the associated
     * metadata megawidgets. This is provided at initialization, and then used
     * when metadata megawidget managers are instantiated if they are for an
     * event identifier for which an entry is found in this map. It is updated
     * whenever a megawidget manager is disposed of, so as to save any extra
     * data that said manager's megawidgets had. Since it is passed in by
     * reference at initialization time, the client can keep a reference around
     * to it when this instance is disposed of, and pass the same map to the
     * next instance of this class, allowing extra data to persist for the
     * entire session regardless of how many different hazard detail view parts
     * are created and deleted.
     */
    private Map<String, Map<String, Map<String, Object>>> extraDataForEventIds;

    /**
     * Map of commands to buttons that issue these commands.
     */
    private final Map<Command, Button> buttonsForCommands = new EnumMap<>(
            Command.class);

    /**
     * Scroll position state change handler.
     */
    private IStateChangeHandler<String, Point> scrollOriginChangeHandler;

    /**
     * Time range state changer.
     */
    private final IStateChanger<String, Point> scrollOriginChanger = new IStateChanger<String, Point>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable scroll origin");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of scroll origin");
        }

        @Override
        public Point getState(String identifier) {
            return scrollOriginsForEventIds.get(identifier);
        }

        @Override
        public void setState(String identifier, Point value) {
            scrollOriginsForEventIds.put(identifier, value);
        }

        @Override
        public void setStates(Map<String, Point> valuesForIdentifiers) {
            scrollOriginsForEventIds.putAll(valuesForIdentifiers);
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Point> handler) {
            scrollOriginChangeHandler = handler;
        }
    };

    /**
     * Time range state changer.
     */
    private final IStateChanger<String, TimeRange> visibleTimeRangeChanger = new IStateChanger<String, TimeRange>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable visible time range");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of visible time range");
        }

        @Override
        public TimeRange getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of visible time range");
        }

        @Override
        public void setState(String identifier, TimeRange value) {
            minimumVisibleTime = value.getStart().getTime();
            maximumVisibleTime = value.getEnd().getTime();
            visibleTimeRangeChanged();
        }

        @Override
        public void setStates(Map<String, TimeRange> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for visible time range");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, TimeRange> handler) {
            throw new UnsupportedOperationException(
                    "cannot set state change handler for visible time range");
        }
    };

    /**
     * Selected event state change handler.
     */
    private IStateChangeHandler<String, String> tabChangeHandler;

    /**
     * Visible event state changer.
     */
    private final IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> visibleEventChanger = new IChoiceStateChanger<String, String, String, DisplayableEventIdentifier>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable tabs");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of tabs");
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<DisplayableEventIdentifier> choiceDisplayables,
                String value) {
            setTabs(choices, choiceDisplayables, value);
        }

        @Override
        public String getState(String identifier) {
            return visibleEventIdentifier;
        }

        @Override
        public void setState(String identifier, String value) {
            setSelectedTab(value);
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for tabs");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
            tabChangeHandler = handler;
        }
    };

    /**
     * Category combo box state change handler.
     */
    private IStateChangeHandler<String, String> categoryChangeHandler;

    /**
     * Category combo box state changer.
     */
    private final IChoiceStateChanger<String, String, String, String> categoryChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive()) {
                categoryMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive()) {
                categoryMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            setCategories(choices);
            setSelectedCategory(value);
        }

        @Override
        public String getState(String identifier) {
            return getSelectedCategory();
        }

        @Override
        public void setState(String identifier, String value) {
            setSelectedCategory(value);
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for category");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
            categoryChangeHandler = handler;
        }
    };

    /**
     * Type combo box state change handler.
     */
    private IStateChangeHandler<String, String> typeChangeHandler;

    /**
     * Type combo box state changer.
     */
    private final IChoiceStateChanger<String, String, String, String> typeChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive()) {
                typeMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive()) {
                typeMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            setTypes(choices, choiceDisplayables);
            setSelectedType(value);
        }

        @Override
        public String getState(String identifier) {
            return getSelectedType();
        }

        @Override
        public void setState(String identifier, String value) {
            setSelectedType(value);
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for type");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
            typeChangeHandler = handler;
        }
    };

    /**
     * Time range state change handler.
     */
    private IStateChangeHandler<String, TimeRange> timeRangeChangeHandler;

    /**
     * Time range state changer.
     */
    private final IStateChanger<String, TimeRange> timeRangeChanger = new IStateChanger<String, TimeRange>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive()) {
                timeRangeMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive()) {
                timeRangeMegawidget.setEditable(editable);
            }
        }

        @Override
        public TimeRange getState(String identifier) {
            return getTimeRange();
        }

        @Override
        public void setState(String identifier, TimeRange value) {
            setTimeRange(value);
        }

        @Override
        public void setStates(Map<String, TimeRange> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for time range");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, TimeRange> handler) {
            timeRangeChangeHandler = handler;
        }
    };

    /**
     * Metadata state change handler.
     */
    private IStateChangeHandler<String, Serializable> metadataChangeHandler;

    /**
     * Metadata state changer.
     */
    private final IMetadataStateChanger metadataChanger = new IMetadataStateChanger() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            setMetadataEnabledState(identifier, enable);
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            setMetadataEditabilityState(identifier, editable);
        }

        @Override
        public Serializable getState(String identifier) {
            return getMetadataValue(identifier);
        }

        @Override
        public void setState(String identifier, Serializable value) {
            setMetadataValue(identifier, value);
        }

        @Override
        public void setStates(Map<String, Serializable> valuesForIdentifiers) {
            setMetadataValues(valuesForIdentifiers);
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Serializable> handler) {
            metadataChangeHandler = handler;
        }

        @Override
        public void setMegawidgetSpecifierManager(String eventIdentifier,
                MegawidgetSpecifierManager specifierManager,
                Map<String, Serializable> metadataStates) {
            setMetadataSpecifierManager(eventIdentifier, specifierManager,
                    metadataStates);
        }
    };

    /**
     * Button invocation handler, for the buttons at the bottom of the view
     * part.
     */
    private ICommandInvocationHandler<Command> buttonInvocationHandler;

    /**
     * Button invoker, for the buttons at the bottom of the view part.
     */
    private final ICommandInvoker<Command> buttonInvoker = new ICommandInvoker<Command>() {

        @Override
        public void setEnabled(Command identifier, boolean enable) {
            if (isAlive()) {
                Button button = buttonsForCommands.get(identifier);
                if ((button != null) && (button.isDisposed() == false)) {
                    button.setEnabled(enable);
                }
            }
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<Command> handler) {
            buttonInvocationHandler = handler;
        }
    };

    /**
     * Basic state change listener used to listen for changes to all
     * non-metatada megawidgets.
     */
    private final IStateChangeListener basicStateChangeListener = new IStateChangeListener() {

        @Override
        public void megawidgetStateChanged(IStateful megawidget,
                String identifier, Object state) {
            if (identifier.equals(CATEGORY_IDENTIFIER)) {
                if (categoryChangeHandler != null) {
                    categoryChangeHandler.stateChanged(identifier,
                            (String) state);
                }
            } else if (identifier.equals(TYPE_IDENTIFIER)) {
                if (typeChangeHandler != null) {
                    typeChangeHandler.stateChanged(identifier, (String) state);
                }
            } else if (identifier.equals(START_TIME_STATE)
                    || identifier.equals(END_TIME_STATE)) {
                if (timeRangeChangeHandler != null) {
                    TimeRange range;
                    try {
                        range = new TimeRange(
                                (Long) (identifier.equals(START_TIME_STATE) ? state
                                        : timeRangeMegawidget
                                                .getState(START_TIME_STATE)),
                                (Long) (identifier.equals(END_TIME_STATE) ? state
                                        : timeRangeMegawidget
                                                .getState(END_TIME_STATE)));
                    } catch (Exception e) {
                        statusHandler.error(
                                "unexpected problem fetching time range "
                                        + "values from megawidget", e);
                        return;
                    }
                    timeRangeChangeHandler.stateChanged(null, range);
                }
            } else if (identifier
                    .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
                setEndTimeEditability(!Boolean.TRUE.equals(state));
                if (metadataChangeHandler != null) {
                    metadataChangeHandler.stateChanged(identifier,
                            (Serializable) state);
                }
            } else {
                throw new IllegalArgumentException(
                        "unexpected state change for unknown identifier \""
                                + identifier + "\"");
            }
        }

        @Override
        public void megawidgetStatesChanged(IStateful megawidget,
                Map<String, Object> statesForIdentifiers) {
            if (megawidget == timeRangeMegawidget) {
                if (timeRangeChangeHandler != null) {
                    TimeRange range;
                    try {
                        range = new TimeRange(
                                (Long) statesForIdentifiers
                                        .get(START_TIME_STATE),
                                (Long) statesForIdentifiers.get(END_TIME_STATE));
                    } catch (Exception e) {
                        statusHandler.error(
                                "unexpected problem fetching time range "
                                        + "values from megawidget", e);
                        return;
                    }
                    timeRangeChangeHandler.stateChanged(null, range);
                }
            } else {
                throw new IllegalArgumentException(
                        "unexpected state change for unknown megawidget \""
                                + megawidget.getSpecifier().getIdentifier()
                                + "\"");
            }
        }
    };

    // Public Methods

    @Override
    public void initialize(
            ImmutableList<String> hazardCategories,
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers) {

        /*
         * Remember the passed-in parameters.
         */
        this.categories = hazardCategories;
        this.minimumVisibleTime = minVisibleTime;
        this.maximumVisibleTime = maxVisibleTime;
        this.currentTimeProvider = currentTimeProvider;
        this.extraDataForEventIds = extraDataForEventIdentifiers;

        /*
         * Synchronize any time-based widgets with the visible time range.
         */
        visibleTimeRangeChanged();

        /*
         * If the categories combo box is not yet populated, do it now.
         */
        setCategories(categories);
    }

    @Override
    public void dispose() {
        for (Map.Entry<String, MegawidgetManager> entry : megawidgetManagersForEventIds
                .entrySet()) {
            recordExtraDataForEvent(entry.getKey(), entry.getValue());
        }
        for (Resource resource : resources) {
            resource.dispose();
        }
        tabChangeHandler = null;
        categoryChangeHandler = null;
        typeChangeHandler = null;
        timeRangeChangeHandler = null;
        metadataChangeHandler = null;
        buttonInvocationHandler = null;
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);

        /*
         * Create a CAVE mode listener, which will set the foreground and
         * background colors appropriately according to the CAVE mode whenever a
         * paint event occurs.
         */
        new ModeListener(parent);

        /*
         * Configure the parent layout.
         */
        parent.setLayout(new GridLayout(1, false));

        /*
         * Create the main panel of the view part.
         */
        Composite tabTop = new Composite(parent, SWT.NONE);
        tabTop.setLayout(new FillLayout());
        tabTop.setLayoutData(new GridData(GridData.FILL_BOTH));

        /*
         * Create the tab folder that will hold the visual representations of
         * any hazard events, and configure it to manually respond to tab
         * selections since it actually uses only one composite as the control
         * for all its tabs, and simply reconfigures the panels therein for each
         * different tab selection.
         */
        eventTabFolder = new CTabFolder(tabTop, SWT.TOP);
        standardUnselectedColor = eventTabFolder.getBackground();
        standardSelectedColor = eventTabFolder.getSelectionBackground();
        eventTabFolder.setBorderVisible(true);
        eventTabFolder.setTabHeight(eventTabFolder.getTabHeight() + 8);
        eventTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * Do nothing if in the middle of tab manipulation already.
                 */
                if (tabsBeingChanged) {
                    return;
                }

                /*
                 * Occasionally, an event is generated from a tab which is not
                 * the currently selected tab, so ensure that the tab reporting
                 * the event is indeed selected. If so, send off the event
                 * identifier associated with that tab as a notification that a
                 * new tab is showing.
                 */
                CTabItem item = eventTabFolder.getSelection();
                if ((CTabItem) e.item == item) {
                    visibleEventIdentifier = (String) item.getData();
                    if (tabChangeHandler != null) {
                        tabChangeHandler.stateChanged(null,
                                visibleEventIdentifier);
                    }
                }
            }
        });

        /*
         * Create the composite that will be used as the control for every event
         * tab page.
         */
        tabPagePanel = new Composite(eventTabFolder, SWT.NONE);
        GridLayout mainLayout = new GridLayout(1, true);
        mainLayout.marginRight = 5;
        mainLayout.marginLeft = 5;
        mainLayout.marginBottom = 5;
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        tabPagePanel.setLayout(mainLayout);

        /*
         * Put together the creation-time parameters needed for building the
         * non-metadata megawidgets.
         */
        Map<String, Object> megawidgetCreationParams = new HashMap<>();
        megawidgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER,
                basicStateChangeListener);
        megawidgetCreationParams.put(TimeScaleSpecifier.MINIMUM_VISIBLE_TIME,
                minimumVisibleTime);
        megawidgetCreationParams.put(TimeScaleSpecifier.MAXIMUM_VISIBLE_TIME,
                maximumVisibleTime);
        megawidgetCreationParams.put(
                TimeMegawidgetSpecifier.CURRENT_TIME_PROVIDER,
                currentTimeProvider);

        /*
         * Create the group holding the category and type combo box megawidgets.
         */
        Group typeGroup = new Group(tabPagePanel, SWT.NONE);
        typeGroup.setText(HAZARD_TYPE_SECTION_TEXT);
        typeGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginBottom = 5;
        typeGroup.setLayout(gridLayout);

        /*
         * Create the category and type combo box megawidgets.
         */
        List<IControl> megawidgetsToAlign = new ArrayList<>();
        try {
            categoryMegawidget = new ComboBoxSpecifier(
                    CATEGORY_SPECIFIER_PARAMETERS).createMegawidget(typeGroup,
                    ComboBoxMegawidget.class, megawidgetCreationParams);
            setCategories(categories);
            megawidgetsToAlign.add(categoryMegawidget);
            typeMegawidget = new ComboBoxSpecifier(TYPE_SPECIFIER_PARAMETERS)
                    .createMegawidget(typeGroup, ComboBoxMegawidget.class,
                            megawidgetCreationParams);
            megawidgetsToAlign.add(typeMegawidget);
        } catch (Exception e) {
            statusHandler.error(
                    "unexpected problem creating category and type combo box "
                            + "megawidgets", e);
        }

        /*
         * Align the created megawidgets' labels to one another.
         */
        ControlComponentHelper.alignMegawidgetsElements(megawidgetsToAlign);
        typeGroup.layout();
        megawidgetsToAlign.clear();

        /*
         * Create group holding the time range megawidget.
         */
        Group timeRangeGroup = new Group(tabPagePanel, SWT.NONE);
        timeRangeGroup.setText(TIME_RANGE_SECTION_TEXT);
        timeRangeGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
                false));
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginBottom = 5;
        timeRangeGroup.setLayout(gridLayout);

        /*
         * Create the time range megawidget; it in turn creates as a child its
         * "until further notice" checkbox.
         */
        try {
            timeRangeMegawidget = new TimeScaleSpecifier(
                    TIME_RANGE_SPECIFIER_PARAMETERS).createMegawidget(
                    timeRangeGroup, TimeScaleMegawidget.class,
                    megawidgetCreationParams);
            endTimeUntilFurtherNoticeMegawidget = (CheckBoxMegawidget) timeRangeMegawidget
                    .getChildren().get(0);
            megawidgetsToAlign.add(timeRangeMegawidget);
        } catch (Exception e) {
            statusHandler.error(
                    "unexpected problem creating time range and checkbox "
                            + "megawidgets", e);
        }

        /*
         * Align the created megawidget's labels to one another.
         */
        ControlComponentHelper.alignMegawidgetsElements(megawidgetsToAlign);
        timeRangeGroup.layout();

        /*
         * Create the group holding the metadata, and hide it to begin with. It
         * is hidden whenever the visible event has no metadata. Within it,
         * create a scrollable composite to allow scrolling of the metadata
         * panel if the latter is too large to be displayed within the area
         * allotted to it. Within that, in turn, create a panel to show the
         * metadata; it contains a stack of panels, one per tab with metadata,
         * which will always have the topmost (visible) panel be the one that
         * goes with the currently visible tab.
         */
        metadataGroup = new Group(tabPagePanel, SWT.NONE);
        metadataGroup.setText(DETAILS_SECTION_TEXT);
        metadataGroupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        metadataGroupLayoutData.exclude = true;
        metadataGroup.setVisible(false);
        metadataGroup.setLayoutData(metadataGroupLayoutData);
        metadataGroup.setLayout(new FillLayout());
        scrolledComposite = new ScrolledComposite(metadataGroup, SWT.H_SCROLL
                | SWT.V_SCROLL);
        metadataContentPanel = new Composite(scrolledComposite, SWT.NONE);
        metadataContentLayout = new StackLayout();
        metadataContentPanel.setLayout(metadataContentLayout);
        scrolledComposite.setContent(metadataContentPanel);
        scrolledComposite.setExpandHorizontal(true);

        /*
         * Add a listener to the horizontal and vertical scroll bars of the
         * scrolled composite to record the origin each time scrolling occurs,
         * and to forward the new origin onto the handler for such things, if
         * one exists.
         */
        SelectionListener scrollListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if ((scrolledCompositeContentsChanging == false)
                        && (visibleEventIdentifier != null)) {
                    scrollOriginsForEventIds.put(visibleEventIdentifier,
                            scrolledComposite.getOrigin());
                    if (scrollOriginChangeHandler != null) {
                        scrollOriginChangeHandler.stateChanged(
                                visibleEventIdentifier,
                                scrolledComposite.getOrigin());
                    }
                }
            }
        };
        scrolledComposite.getHorizontalBar().addSelectionListener(
                scrollListener);
        scrolledComposite.getVerticalBar().addSelectionListener(scrollListener);

        /*
         * Add a listener to the scrolled composite to make it resize its page
         * increment whenever it changes size.
         */
        scrolledComposite.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                recalculateScrolledCompositePageIncrement();
            }
        });

        /*
         * Pack the main tab folder.
         */
        eventTabFolder.pack();

        /*
         * Create the button bar below the tab composite and populate it with
         * buttons.
         */
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
        createButton(buttonBar, Command.PREVIEW, PREVIEW_BUTTON_TEXT,
                PREVIEW_BUTTON_TOOLTIP_TEXT);
        createButton(buttonBar, Command.PROPOSE, PROPOSE_BUTTON_TEXT,
                PROPOSE_BUTTON_TOOLTIP_TEXT);
        createButton(buttonBar, Command.ISSUE, ISSUE_BUTTON_TEXT,
                ISSUE_BUTTON_TOOLTIP_TEXT);
    }

    @Override
    public void setFocus() {

        /*
         * No action.
         */
    }

    /**
     * Get the detail view scroll origin state changer. This manages the scroll
     * origin points for events that have been shown in the view, so that they
     * can be restored to the same scroll positions that they had when they were
     * last loaded into the view on subsequent loads.
     * 
     * @return Detail view scroll origin state changer.
     */
    public IStateChanger<String, Point> getScrollOriginChanger() {
        return scrollOriginChanger;
    }

    @Override
    public IStateChanger<String, TimeRange> getVisibleTimeRangeChanger() {
        return visibleTimeRangeChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> getVisibleEventChanger() {
        return visibleEventChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getCategoryChanger() {
        return categoryChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getTypeChanger() {
        return typeChanger;
    }

    @Override
    public IStateChanger<String, TimeRange> getTimeRangeChanger() {
        return timeRangeChanger;
    }

    @Override
    public IMetadataStateChanger getMetadataChanger() {
        return metadataChanger;
    }

    @Override
    public ICommandInvoker<Command> getButtonInvoker() {
        return buttonInvoker;
    }

    // Private Methods

    /**
     * Creates a new button and records it as associated with the specified
     * command. The button, when invoked, will execute that command.
     * 
     * @param parent
     *            Parent composite.
     * @param command
     *            Command button is to issue when invoked.
     * @param label
     *            Label for the button.
     * @param tooltipLabel
     *            Label for the tooltip of the button.
     */
    private void createButton(Composite parent, Command command, String label,
            String tooltipLabel) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setToolTipText(tooltipLabel);
        button.setData(command);
        button.setEnabled(false);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (buttonInvocationHandler != null) {
                    buttonInvocationHandler
                            .commandInvoked((Command) event.widget.getData());
                }
            }
        });
        buttonsForCommands.put(command, button);
    }

    /**
     * Determine whether or not the view part is currently alive, meaning that
     * it has at least some non-disposed widgets.
     * 
     * @return True if widgets exist, false otherwise.
     */
    private boolean isAlive() {
        return ((tabPagePanel != null) && (tabPagePanel.isDisposed() == false));
    }

    /**
     * Set the tabs to those specified.
     * 
     * @param choices
     *            Event identifiers; one tab per identifier is needed, in the
     *            given order.
     * @param choiceDisplayables
     *            Displayables for the tabs; each one is the displayable for the
     *            event identifier in <code>choices</code> at the corresponding
     *            index.
     */
    private void setTabs(List<String> choices,
            List<DisplayableEventIdentifier> choiceDisplayables,
            String visibleEventIdentifier) {

        /*
         * Do nothing if the tab folder is nonexistent or disposed.
         */
        if (isAlive() == false) {
            return;
        }

        /*
         * Temporarily turn off redraw.
         */
        eventTabFolder.setRedraw(false);

        /*
         * Determine whether or not the tabs need to be recreated.
         */
        CTabItem[] tabItems = eventTabFolder.getItems();
        boolean recreateTabs = (tabItems.length != choices.size());
        if (recreateTabs == false) {
            for (int j = 0; j < choices.size(); j++) {
                if (choices.get(j).equals(tabItems[j].getData()) == false) {
                    recreateTabs = true;
                    break;
                }
            }
        }

        /*
         * If the tabs need to be recreated, destroy the old ones and create new
         * ones; otherwise just set their text strings.
         */
        boolean conflictExists = false;
        if (recreateTabs) {
            tabsBeingChanged = true;
            scrolledCompositeContentsChanging = true;
            for (CTabItem tabItem : tabItems) {
                tabItem.dispose();
            }
            this.visibleEventIdentifier = null;
            for (int j = 0; j < choices.size(); j++) {
                CTabItem tabItem = new CTabItem(eventTabFolder, SWT.NONE);
                tabItem.setText(choiceDisplayables.get(j).getDescription());
                tabItem.setData(choices.get(j));
                tabItem.setControl(tabPagePanel);
            }
            scrolledCompositeContentsChanging = false;
            tabsBeingChanged = false;
        } else {
            for (int j = 0; j < choiceDisplayables.size(); j++) {
                tabItems[j].setText(choiceDisplayables.get(j).getDescription());
            }
        }

        /*
         * Iterate through the tabs, marking any that are for events with
         * conflicts with the appropriate icon and tooltip.
         */
        tabItems = eventTabFolder.getItems();
        for (int j = 0; j < choiceDisplayables.size(); j++) {
            if (choiceDisplayables.get(j).isConflicting()) {
                conflictExists = true;
                tabItems[j].setImage(CONFLICT_TAB_ICON);
                tabItems[j].setToolTipText(CONFLICT_TOOLTIP);
            } else {
                tabItems[j].setImage(null);
                tabItems[j].setToolTipText(null);
            }
        }

        /*
         * If a conflict exists for at least one of the tabs, hint at this by
         * setting the tab folder to use eye-catching background colors for both
         * selected and unselected tabs; otherwise, use standard colors.
         */
        if (conflictExists) {
            eventTabFolder.setBackground(CONFLICT_UNSELECTED_TAB_COLOR);
            eventTabFolder.setSelectionBackground(CONFLICT_SELECTED_TAB_COLOR);
        } else {
            eventTabFolder.setBackground(standardUnselectedColor);
            eventTabFolder.setSelectionBackground(standardSelectedColor);
        }

        /*
         * Set the currently selected tab, and turn redraw back on.
         */
        setSelectedTab(visibleEventIdentifier);
        eventTabFolder.setRedraw(true);
    }

    /**
     * Set the tab folder to show the tab that goes with the specified event
     * identifier as the topmost (visible) tab page.
     * 
     * @param visibleEventIdentifier
     *            Event identifier associated with the tab page that is to be
     *            visible.
     */
    private void setSelectedTab(String visibleEventIdentifier) {
        if (isAlive()) {
            for (CTabItem tabItem : eventTabFolder.getItems()) {
                if (tabItem.getData().equals(visibleEventIdentifier)) {
                    this.visibleEventIdentifier = visibleEventIdentifier;
                    eventTabFolder.setSelection(tabItem);
                    break;
                }
            }
        }
    }

    /**
     * Set the category combo box choices.
     * 
     * @param categories
     *            List of categories to be used.
     */
    private void setCategories(List<String> categories) {
        setComboBoxChoices(categoryMegawidget, categories, null, "category");
    }

    /**
     * Set the type combo box choices.
     * 
     * @param types
     *            List of types to be used.
     * @param descriptions
     *            Descriptions for the <code>types</code>; each element in this
     *            list is the description for the element at the corresponding
     *            index in <code>types</code>.
     */
    private void setTypes(List<String> types, List<String> descriptions) {
        setComboBoxChoices(typeMegawidget, types, descriptions, "type");
    }

    /**
     * Set the specified combo box's choices.
     * 
     * @param comboBox
     *            Combo box to have its choices set.
     * @param choices
     *            Choice identifiers for the combo box.
     * @param descriptions
     *            Descriptions for the <code>choices</code>; each element in
     *            this list is the description for the element at the
     *            corresponding index in <code>choices</code>. If
     *            <code>null</code>, the <code>choices</code> list is used for
     *            both identifiers and displayables.
     * @param identifier
     *            Identifier of the state the combo box holds.
     */
    private void setComboBoxChoices(ComboBoxMegawidget comboBox,
            List<String> choices, List<String> descriptions, String identifier) {
        if (isAlive() && (comboBox != null) && (choices != null)) {
            List<?> choicesList;
            if (descriptions != null) {
                List<Map<String, Object>> list = new ArrayList<>(choices.size());
                for (int j = 0; j < choices.size(); j++) {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put(ComboBoxSpecifier.CHOICE_IDENTIFIER, choices.get(j));
                    map.put(ComboBoxSpecifier.CHOICE_NAME, descriptions.get(j));
                    list.add(map);
                }
                choicesList = list;
            } else {
                choicesList = choices;
            }
            try {
                comboBox.setChoices(choicesList);
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + identifier + " megawidget choices", e);
            }
        }
    }

    /**
     * Get the selected category.
     * 
     * @return Selected category.
     */
    private String getSelectedCategory() {
        return getComboBoxSelectedChoice(categoryMegawidget,
                CATEGORY_IDENTIFIER);
    }

    /**
     * Get the selected type.
     * 
     * @return Selected type.
     */
    private String getSelectedType() {
        return getComboBoxSelectedChoice(typeMegawidget, TYPE_IDENTIFIER);
    }

    /**
     * Get the specified combo box's selected choice.
     * 
     * @param comboBox
     *            Combo box from which to get the selected choice.
     * @param identifier
     *            Identifier of the state the combo box holds.
     * @return Selected choice.
     */
    private String getComboBoxSelectedChoice(ComboBoxMegawidget comboBox,
            String identifier) {
        if (comboBox != null) {
            try {
                return (String) comboBox.getState(identifier);
            } catch (Exception e) {
                statusHandler.error("unexpected error while getting "
                        + identifier + " megawidget state", e);
            }
        }
        return null;
    }

    /**
     * Set the selected category.
     * 
     * @param category
     *            New category to be used.
     */
    private void setSelectedCategory(String category) {
        setComboBoxSelectedChoice(categoryMegawidget, category,
                CATEGORY_IDENTIFIER);
    }

    /**
     * Set the selected type.
     * 
     * @param type
     *            New type to be used.
     */
    private void setSelectedType(String type) {
        setComboBoxSelectedChoice(typeMegawidget, type, TYPE_IDENTIFIER);
    }

    /**
     * Set the specified combo box's selected choice.
     * 
     * @param comboBox
     *            Combo box to have its choice set.
     * @param choice
     *            Choice for the combo box.
     * @param identifier
     *            Identifier of the state the combo box holds.
     */
    private void setComboBoxSelectedChoice(ComboBoxMegawidget comboBox,
            String choice, String identifier) {
        if (isAlive() && (comboBox != null)) {
            try {
                comboBox.setState(identifier, choice);
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + identifier + " megawidget state", e);
            }
        }
    }

    /**
     * Get the time range.
     * 
     * @return Time range.
     */
    private TimeRange getTimeRange() {
        if (isAlive() && (timeRangeMegawidget != null)) {
            try {
                return new TimeRange(
                        (Long) timeRangeMegawidget.getState(START_TIME_STATE),
                        (Long) timeRangeMegawidget.getState(END_TIME_STATE));
            } catch (Exception e) {
                statusHandler.error("unexpected error while getting "
                        + TIME_RANGE_IDENTIFIER + " megawidget values", e);
            }
        }
        return null;
    }

    /**
     * Set the time range to that specified.
     * 
     * @param range
     *            New time range.
     */
    private void setTimeRange(TimeRange range) {
        if (isAlive() && (timeRangeMegawidget != null)) {
            try {
                timeRangeMegawidget.setUncommittedState(START_TIME_STATE, range
                        .getStart().getTime());
                timeRangeMegawidget.setUncommittedState(END_TIME_STATE, range
                        .getEnd().getTime());
                timeRangeMegawidget.commitStateChanges();
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + TIME_RANGE_IDENTIFIER + " megawidget values", e);
            }
        }
    }

    /**
     * Set the end time "until further notice" checkbox to that specified.
     * 
     * @param untilFurtherNotice
     *            Boolean flag indicating whether "until further notice" should
     *            be checked, or <code>null</code>, which is treated as
     *            {@link Boolean#FALSE}.
     */
    private void setEndTimeUntilFurtherNotice(Object untilFurtherNotice) {
        if (isAlive()) {
            setEndTimeEditability(!Boolean.TRUE.equals(untilFurtherNotice));
            try {
                endTimeUntilFurtherNoticeMegawidget
                        .setState(
                                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                                untilFurtherNotice);
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error while setting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget value", e);
            }
        }
    }

    /**
     * Set the time range megawidget so that its end time state has the
     * specified editability.
     * 
     * @param editable
     *            Flag indicating whether the end time state should be editable.
     */
    private void setEndTimeEditability(boolean editable) {
        Map<String, Boolean> stateEditables = new HashMap<>();
        stateEditables.put(START_TIME_STATE, true);
        stateEditables.put(END_TIME_STATE, editable);
        try {
            timeRangeMegawidget.setMutableProperty(
                    TimeScaleSpecifier.MEGAWIDGET_STATE_EDITABLES,
                    stateEditables);
        } catch (Exception e) {
            statusHandler.error("unexpected error while setting "
                    + END_TIME_STATE + " editability in megawidget", e);
        }
    }

    /**
     * Set the end time "until further notice" checkbox enabled state to that
     * specified.
     * 
     * @param enable
     *            Flag indicating whether "until further notice" should be
     *            enabled.
     */
    private void setEndTimeUntilFurtherNoticeEnabledState(boolean enable) {
        if (isAlive()) {
            try {
                endTimeUntilFurtherNoticeMegawidget.setEnabled(enable);
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error while setting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget enabled state", e);
            }
        }
    }

    /**
     * Set the end time "until further notice" checkbox editability state to
     * that specified.
     * 
     * @param editable
     *            Flag indicating whether "until further notice" should be
     *            editable.
     */
    private void setEndTimeUntilFurtherNoticeEditableState(boolean editable) {
        if (isAlive()) {
            try {
                endTimeUntilFurtherNoticeMegawidget.setEditable(editable);
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error while setting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget editability state", e);
            }
        }
    }

    /**
     * Set the metadata specifier manager as that specified for an event
     * identifier.
     * 
     * @param eventIdentifier
     *            Event identifier for which the metadata specifier manager is
     *            being set.
     * @param specifierManager
     *            Megawidget specifier manager to be used.
     * @param metadataStates
     *            States for the metadata.
     */
    private void setMetadataSpecifierManager(String eventIdentifier,
            MegawidgetSpecifierManager specifierManager,
            Map<String, Serializable> metadataStates) {

        /*
         * Do nothing if widgets are not currently available.
         */
        if (isAlive() == false) {
            return;
        }

        /*
         * If the specifiers for the currently visible event are being set, stop
         * redraws for the moment.
         */
        if (eventIdentifier.equals(visibleEventIdentifier)) {
            metadataContentPanel.setRedraw(false);
        }

        /*
         * If this specifier is not the same as what was used last time, then
         * remove the old specifier (if any), and create the new megawidget
         * manager and accompanying panel (if there are any new specifiers).
         */
        MegawidgetManager megawidgetManager = megawidgetManagersForEventIds
                .get(eventIdentifier);
        Composite panel = null;
        if (((megawidgetManager != null) && (megawidgetManager
                .getSpecifierManager() != specifierManager))
                || ((megawidgetManager == null) && (specifierManager
                        .getSpecifiers().isEmpty() == false))) {

            /*
             * Delete the old megawidget manager, if any. Before deleting it,
             * get any extra data that its megawidgets have stashed away, as
             * this may be being used in interdependency scripts and may be
             * needed for the next megawidget manager's scripts. Delete the
             * entry for it in the extra data cache, since an entry will have
             * been created as part of the remove() call.
             */
            Map<String, Map<String, Object>> oldExtraDataMap = null;
            if (megawidgetManager != null) {
                oldExtraDataMap = megawidgetManager.getExtraData();
                megawidgetManagersForEventIds.remove(eventIdentifier);
                extraDataForEventIds.remove(eventIdentifier);
            }

            /*
             * If no extra data was found above, see if any has been cached from
             * previous views, and if so, use that.
             */
            if (oldExtraDataMap == null) {
                oldExtraDataMap = extraDataForEventIds.get(eventIdentifier);
            }

            /*
             * If there are specifiers from which to create megawidgets, create
             * a panel for them, then the megawidget manager itself. If this
             * creation fails, log an error and reset state so that it is as if
             * there are no specifiers for which to create megawidgets. Then, if
             * extra data was taken from the old megawidget manager (if any)
             * above, merge it with the new manager's megawidgets' extra data.
             * This allows any interdependency scripts that may be looking for
             * saved values in the extra data to continue to function.
             */
            if (specifierManager.getSpecifiers().isEmpty() == false) {
                panel = new Composite(metadataContentPanel, SWT.NONE);
                Map<String, Object> map = new HashMap<String, Object>(
                        metadataStates);
                try {
                    megawidgetManager = new MegawidgetManager(panel,
                            specifierManager, map,
                            new IMegawidgetManagerListener() {

                                @Override
                                public void commandInvoked(
                                        MegawidgetManager manager,
                                        String identifier) {

                                    /*
                                     * No action.
                                     */
                                }

                                @Override
                                public void stateElementChanged(
                                        MegawidgetManager manager,
                                        String identifier, Object state) {
                                    if (metadataChangeHandler != null) {
                                        metadataChangeHandler.stateChanged(
                                                identifier,
                                                (Serializable) state);
                                    }
                                }

                                @Override
                                public void stateElementsChanged(
                                        MegawidgetManager manager,
                                        Map<String, Object> statesForIdentifiers) {
                                    if (metadataChangeHandler != null) {
                                        Map<String, Serializable> map = new HashMap<>(
                                                statesForIdentifiers.size());
                                        for (Map.Entry<String, Object> entry : statesForIdentifiers
                                                .entrySet()) {
                                            map.put(entry.getKey(),
                                                    (Serializable) entry
                                                            .getValue());
                                        }
                                        metadataChangeHandler
                                                .statesChanged(map);
                                    }
                                }

                                @Override
                                public void sizeChanged(
                                        MegawidgetManager manager,
                                        String identifier) {
                                    metadataPanelResized();
                                }

                                @Override
                                public void sideEffectMutablePropertyChangeErrorOccurred(
                                        MegawidgetManager manager,
                                        MegawidgetPropertyException exception) {
                                    statusHandler
                                            .error("HazardDetailViewPart.MegawidgetManager error occurred "
                                                    + "while attempting to apply megawidget interdependencies.",
                                                    exception);
                                }

                            }, minimumVisibleTime, maximumVisibleTime);
                } catch (Exception e) {
                    statusHandler
                            .error("Could not create hazard metadata megawidgets "
                                    + "for event ID = "
                                    + eventIdentifier
                                    + ": " + e.getMessage());
                    panel.dispose();
                    panel = null;
                }
                if (panel != null) {
                    if (oldExtraDataMap != null) {

                        /*
                         * Merge the old and new extra data maps together. If
                         * the new map has no entry for a key found in the old
                         * map, just use the old map's entry. Otherwise, take
                         * the new map's entry's submap and place any entries
                         * found in the the old map's entry's submap into it.
                         */
                        Map<String, Map<String, Object>> newExtraDataMap = megawidgetManager
                                .getExtraData();
                        for (String identifier : oldExtraDataMap.keySet()) {
                            if (newExtraDataMap.containsKey(identifier)) {
                                newExtraDataMap.get(identifier).putAll(
                                        oldExtraDataMap.get(identifier));
                            } else {
                                newExtraDataMap.put(identifier,
                                        oldExtraDataMap.get(identifier));
                            }
                        }
                        megawidgetManager.setExtraData(newExtraDataMap);
                    }
                    megawidgetManagersForEventIds.put(eventIdentifier,
                            megawidgetManager);
                }
            }
        } else if (megawidgetManager != null) {
            panel = megawidgetManager.getParent();
        }

        /*
         * If the specifiers are being set for the currently visible event, set
         * the "until further notice" megawidget appropriately, update the
         * layout, and turn redraw back on.
         */
        if (eventIdentifier.equals(visibleEventIdentifier)) {
            setEndTimeUntilFurtherNotice(metadataStates
                    .get(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE));
            layoutMetadataGroup(panel);
            metadataContentPanel.setRedraw(true);
        }
    }

    /**
     * Prepare the specified megawidget manager for removal.
     * 
     * @param eventIdentifier
     *            Event identifier with which the megawidget manager is
     *            associated.
     * @param megawidgetManager
     *            Megawidget manager to be removed.
     */
    private void prepareMegawidgetManagerForRemoval(String eventIdentifier,
            MegawidgetManager megawidgetManager) {
        recordExtraDataForEvent(eventIdentifier, megawidgetManager);
        megawidgetManager.getParent().dispose();
    }

    /**
     * Record the specified event identifier's extra data from the specified
     * megawidget manager, if any extra data is found.
     * 
     * @param eventIdentifier
     *            Event identifier with which the megawidget manager is
     *            associated.
     * @param megawidgetManager
     *            Megawidget manager to have its extra data recorded.
     */
    private void recordExtraDataForEvent(String eventIdentifier,
            MegawidgetManager megawidgetManager) {
        Map<String, Map<String, Object>> extraData = megawidgetManager
                .getExtraData();
        if (extraData.isEmpty() == false) {
            extraDataForEventIds.put(eventIdentifier, extraData);
        }
    }

    /**
     * Get the specified metadata element value.
     * 
     * @param identifier
     *            Identifier of the metadata element for which to get the value.
     * @return Value of the metadata element.
     */
    private Serializable getMetadataValue(String identifier) {
        if (identifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            try {
                return (Boolean) endTimeUntilFurtherNoticeMegawidget
                        .getState(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
            } catch (Exception e) {
                statusHandler
                        .error("Error while getting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget value", e);
            }
        } else if (visibleEventIdentifier != null) {
            MegawidgetManager manager = megawidgetManagersForEventIds
                    .get(visibleEventIdentifier);
            if (manager != null) {
                try {
                    return (Serializable) manager.getStateElement(identifier);
                } catch (Exception e) {
                    statusHandler.error("Error while trying to get metadata \""
                            + identifier + "\"", e);
                }
            }
        }
        return null;
    }

    /**
     * Set the enabled state of the specified metadata element to the specified
     * value.
     * 
     * @param identifier
     *            Identifier of the metadata element to be set.
     * @param enable
     *            Flag indicating whether or not the element should be enabled.
     */
    private void setMetadataEnabledState(String identifier, boolean enable) {
        if (isAlive() == false) {
            return;
        }
        if (identifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEndTimeUntilFurtherNoticeEnabledState(enable);
        } else {

            /*
             * TODO: Perhaps make other metadata megawidgets able to be enabled
             * or disabled.
             */
            throw new UnsupportedOperationException(
                    "cannot enable/disable metadata widgets");
        }
    }

    /**
     * Set the editability state of the specified metadata element to the
     * specified value.
     * 
     * @param identifier
     *            Identifier of the metadata element to be set.
     * @param editable
     *            Flag indicating whether or not the element should be editable.
     */
    private void setMetadataEditabilityState(String identifier, boolean editable) {
        if (isAlive() == false) {
            return;
        }
        if (identifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEndTimeUntilFurtherNoticeEditableState(editable);
        } else {

            /*
             * TODO: Perhaps make other metadata megawidgets able to be made
             * editable or read-only.
             */
            throw new UnsupportedOperationException(
                    "cannot change editability of metadata widgets");
        }
    }

    /**
     * Set the specified metadata element to the specified value.
     * 
     * @param identifier
     *            Identifier of the metadata element to be set.
     * @param value
     *            New value of the metadata element.
     */
    private void setMetadataValue(String identifier, Serializable value) {
        if (isAlive() == false) {
            return;
        }
        if (identifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEndTimeUntilFurtherNotice(value);
        } else if (visibleEventIdentifier != null) {
            MegawidgetManager manager = megawidgetManagersForEventIds
                    .get(visibleEventIdentifier);
            if (manager != null) {
                Map<String, Object> map = new HashMap<>();
                map.put(identifier, value);
                try {
                    manager.modifyState(map);
                } catch (Exception e) {
                    statusHandler.error("Error while trying to set metadata \""
                            + identifier + "\" to " + value, e);
                }
            }
        }
    }

    /**
     * Set the specified metadata elements to the specified values.
     * 
     * @param valuesForIdentifiers
     *            Map of metadata element identifiers to their new values.
     */
    private void setMetadataValues(
            Map<String, Serializable> valuesForIdentifiers) {
        if (isAlive() == false) {
            return;
        }
        int numElements = valuesForIdentifiers.size();
        if (valuesForIdentifiers
                .containsKey(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            numElements--;
            setEndTimeUntilFurtherNotice(valuesForIdentifiers
                    .get(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE));
        }
        if ((numElements > 0) && (visibleEventIdentifier != null)) {
            MegawidgetManager manager = megawidgetManagersForEventIds
                    .get(visibleEventIdentifier);
            if (manager != null) {
                try {
                    manager.modifyState(valuesForIdentifiers);
                } catch (Exception e) {
                    statusHandler.error(
                            "Error while trying to set metadata values", e);
                }
            }
        }
    }

    /**
     * Update widgets to reflect the new visible time range.
     */
    private void visibleTimeRangeChanged() {
        if (isAlive() == false) {
            return;
        }
        if (timeRangeMegawidget != null) {
            timeRangeMegawidget.setVisibleTimeRange(minimumVisibleTime,
                    maximumVisibleTime);
        }
        for (MegawidgetManager manager : megawidgetManagersForEventIds.values()) {
            manager.setVisibleTimeRange(minimumVisibleTime, maximumVisibleTime);
        }
    }

    /**
     * Layout the metadata group widget to display the specified panel, if any.
     * 
     * @param panel
     *            Panel to be displayed within the metadata group widget, or
     *            <code>null</code> if there is no panel to display.
     */
    private void layoutMetadataGroup(Composite panel) {

        /*
         * If a panel was provided, set up its containers to display it
         * properly; otherwise, hide the metadata group.
         */
        if (panel != null) {

            /*
             * Ensure the metadata group is showing, and that its topmost
             * control is this panel.
             */
            metadataGroupLayoutData.exclude = false;
            metadataGroup.setVisible(true);
            metadataContentLayout.topControl = panel;
            metadataContentPanel.layout();

            /*
             * Set the flag indicating that the scrolled composite's contents
             * are changing, so that any events it generates are ignored.
             */
            scrolledCompositeContentsChanging = true;

            /*
             * Recalculate the client area's size.
             */
            recalculateScrolledCompositeClientArea(panel);

            /*
             * If the event that is showing has a previously-recorded scrolling
             * origin, use it so that the top-left portion of the event's panel
             * that was visible last time it was looked at is made visible
             * again. This must be done asynchronously, since otherwise SWT
             * seems to not use the origin correctly in all cases. Because it is
             * asynchronous, a check is done when setting the origin to ensure
             * that the same event is still visible. If there is no recorded
             * scrolling origin for this event, synchronously set it to the top
             * left, which for reasons passing understanding works fine.
             */
            final Point origin = scrollOriginsForEventIds
                    .get(visibleEventIdentifier);
            if (origin != null) {
                final String originIdentifier = visibleEventIdentifier;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (isAlive()
                                && (originIdentifier
                                        .equals(visibleEventIdentifier))) {
                            scrolledComposite.setOrigin(origin);
                        }
                    }
                });
            } else {
                scrolledComposite.setOrigin(new Point(0, 0));
            }

            /*
             * Reset the flag indicating that the scrolled composite's contents
             * are no longer changing.
             */
            scrolledCompositeContentsChanging = false;
        } else {
            metadataGroupLayoutData.exclude = true;
            metadataGroup.setVisible(false);
        }

        /*
         * Redo the layout of the tab page.
         */
        tabPagePanel.layout();
    }

    /**
     * Respond to a potential resize of the metadata panel because a megawidget
     * changed its size.
     */
    private void metadataPanelResized() {
        scrolledCompositeContentsChanging = true;
        Composite panel = megawidgetManagersForEventIds.get(
                visibleEventIdentifier).getParent();
        recalculateScrolledCompositeClientArea(panel);
        scrolledCompositeContentsChanging = false;
    }

    /**
     * Recalculate the scrolled copmosite's client area, that is, its size.
     * 
     * @param panel
     *            Panel that is displayed within the metadata group widget.
     */
    private void recalculateScrolledCompositeClientArea(Composite panel) {

        /*
         * Determine the required width and height of the metadata panel.
         */
        Point metadataSize = panel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        /*
         * Set the enclosing content panel's size to be appropriate for the new
         * metadata panel, and tell the scrolled composite that in turn holds it
         * of its new size.
         */
        metadataContentPanel.setSize(metadataSize);
        scrolledComposite.setMinSize(metadataSize);

        /*
         * Recalculate the scrolled composite's page size.
         */
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
}
