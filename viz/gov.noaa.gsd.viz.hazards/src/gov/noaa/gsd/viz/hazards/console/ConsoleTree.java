/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_BOOLEAN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_COUNTDOWN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_DATE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_NUMBER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS;
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.common.utilities.Sort;
import gov.noaa.gsd.common.utilities.Sort.SortDirection;
import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimer;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimersDisplayManager;
import gov.noaa.gsd.viz.hazards.alerts.ICountdownTimersDisplayListener;
import gov.noaa.gsd.viz.hazards.console.ConsoleColumns.ColumnDefinition;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter.TimeRangeType;
import gov.noaa.gsd.viz.hazards.console.ITemporalDisplay.SelectedTimeMode;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.megawidgets.IMenuSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManagerAdapter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;
import gov.noaa.gsd.viz.widgets.CustomToolTip;
import gov.noaa.gsd.viz.widgets.IMultiValueLinearControlListener;
import gov.noaa.gsd.viz.widgets.IMultiValueTooltipTextProvider;
import gov.noaa.gsd.viz.widgets.ImageUtilities;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl.ChangeSource;
import gov.noaa.gsd.viz.widgets.MultiValueRuler;
import gov.noaa.gsd.viz.widgets.MultiValueScale;
import gov.noaa.gsd.viz.widgets.WidgetUtilities;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.Pair;
import com.raytheon.uf.viz.core.icon.IconUtil;

/**
 * Description: Encapsulation of a tree widget in the console that holds tabular
 * representations of hazard events, including a column holding time scales with
 * sliders allowing events' start and end times to be viewed and manipulated.
 * <p>
 * Note that following construction, two methods must be called to complete the
 * creation:
 * {@link #initialize(ImmutableList, Date, Date, long, TimeResolution)} and
 * {@link #createWidgets(Composite, Object)} . They may be called in either
 * order, but both must be called once and only once.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 20, 2016   15556    Chris.Golden Initial creation.
 * Mar 16, 2017   15528    Chris.Golden Added ability to show issued events with unsaved
 *                                      changes in bold.
 * Apr 20, 2017   33376    Chris.Golden Fixed bug causing the Until Further Notice
 *                                      checkbox menu item to be ignored when the user
 *                                      toggled it.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class ConsoleTree implements IConsoleTree {

    // Package-Private Interfaces

    /**
     * Interface that must be implemented by a class that is using a
     * {@link ConsoleTree}.
     */
    interface IConsoleTreeUser {

        /**
         * Respond to a change in the time line ruler size within the console
         * tree.
         * 
         * @param startX
         *            New starting X pixel value (left edge of the ruler) within
         *            the parent window.
         * @param endX
         *            New ending X pixel value (right edge of the ruler) within
         *            the parent window.
         */
        public void timeLineRulerSizeChanged(int startX, int endX);

        /**
         * Respond to a change in the visible time range.
         * 
         * @param lowerVisibleValue
         *            Lower boundary (inclusive) of the new visible value range,
         *            as an epoch time in milliseconds.
         * @param upperVisibleValue
         *            Upper boundary (inclusive) of the new visible value range,
         *            as an epoch time in milliseconds.
         * @param zoomedOutRange
         *            Theoretical range that would be used if the time line
         *            ruler was zoomed out by one step, in milliseconds.
         * @param zoomedInRange
         *            Theoretical range that would be used if the time line
         *            ruler was zoomed in by one step, in milliseconds.
         */
        public void timeLineRulerVisibleRangeChanged(long lowerVisibleValue,
                long upperVisibleValue, long zoomedOutRange, long zoomedInRange);

        /**
         * Respond to a change in the selected time mode.
         * 
         * @param mode
         *            New selected time mode.
         */
        public void selectedTimeModeChanged(SelectedTimeMode mode);

        /**
         * Get the context menu items appropriate to the specified event.
         * 
         * @param identifier
         *            Identifier of the tabular entity that was chosen with the
         *            context menu invocation, or <code>null</code> if none was
         *            chosen.
         * @param persistedTimestamp
         *            Timestamp indicating when the entity was persisted; may be
         *            <code>null</code>.
         * @return Actions for the menu items to be shown.
         * @deprecated See
         *             {@link ConsolePresenter#getContextMenuItems(String, Date, IRunnableAsynchronousScheduler)}
         *             .
         */
        @Deprecated
        List<IContributionItem> getContextMenuItems(String identifier,
                Date persistedTimestamp);
    }

    // Private Static Constants

    /**
     * Unchecked menu item image file name.
     */
    private static final String UNCHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemUnchecked.png";

    /**
     * Semi-checked menu item image file name.
     */
    private static final String SEMI_CHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemSemiChecked.png";

    /**
     * Checked menu item image file name.
     */
    private static final String CHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemChecked.png";

    /**
     * Filter menu name.
     */
    private static final String FILTER_MENU_NAME = "Filter";

    /**
     * Primary sort menu name.
     */
    private static final String PRIMARY_SORT_MENU_NAME = "Sort by";

    /**
     * Secondary sort menu name.
     */
    private static final String SECONDARY_SORT_MENU_NAME = "Secondary Sort by";

    /**
     * Text to be displayed in a cell of a boolean column with no value.
     */
    private static final String BOOLEAN_COLUMN_NULL_TEXT = "N/A";

    /**
     * Text to be displayed in a cell of a boolean column with a value of true.
     */
    private static final String BOOLEAN_COLUMN_TRUE_TEXT = "yes";

    /**
     * Text to be displayed in a cell of a boolean column with a value of false.
     */
    private static final String BOOLEAN_COLUMN_FALSE_TEXT = "no";

    /**
     * Text to show in date-time cells holding the "until further notice" value.
     */
    private static final String UNTIL_FURTHER_NOTICE_COLUMN_TEXT = "until further notice";

    /**
     * Text to show in the event-specific context-sensitive menu to provide the
     * "until further notice" toggle option.
     */
    private static final String UNTIL_FURTHER_NOTICE_MENU_TEXT = "Until Further Notice";

    /**
     * Show time under mouse toggle menu text.
     */
    private static final String SHOW_TIME_UNDER_MOUSE_TOGGLE_MENU_TEXT = "Show Time Under Mouse";

    /**
     * Empty text representation in hazard event tree cell.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Key into filter megawidget definition used to find the column name with
     * which the megawidget should be associated.
     */
    private static final String COLUMN_NAME = "columnName";

    /**
     * Width in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_WIDTH = 13;

    /**
     * Height in pixels of the time scale thumbs.
     */
    private static final int SCALE_THUMB_HEIGHT = 17;

    /**
     * Thickness in pixels of the time scale tracks.
     */
    private static final int SCALE_TRACK_THICKNESS = 11;

    /**
     * Width of horizontal padding in pixels to the left and right of time
     * widgets (both ruler and scales).
     */
    private static final int TIME_HORIZONTAL_PADDING = 10;

    /**
     * Left padding of tree cell in which scale widgets are placed. Where this
     * value comes from is unknown; hopefully it will not change with shifts in
     * window managers, etc.
     */
    private static final int CELL_PADDING_LEFT = 3;

    /**
     * Text displayed in the column header for the time scale widgets.
     */
    private static final String TIME_SCALE_COLUMN_NAME = "Time Scale";

    /**
     * Default selected time range in milliseconds.
     */
    private static final long DEFAULT_SELECTED_TIME_RANGE = TimeUnit.HOURS
            .toMillis(4);

    /**
     * Multiplier of the current visible time range to apply when panning
     * backward or forward via a button press.
     */
    private static final float PAN_TIME_DELTA_MULTIPLIER = 0.5f;

    /**
     * Multiplier of the current visible time range to apply when paging
     * backward or forward via a button press
     */
    private static final float PAGE_TIME_DELTA_MULTIPLIER = 1.0f;

    /**
     * Identifiers of tree columns used to display time-range-related
     * information. This list must be ordered so that the start time comes
     * before the end time.
     */
    private static final List<String> TIME_RANGE_COLUMN_IDENTIFIERS = ImmutableList
            .of(HAZARD_EVENT_START_TIME, HAZARD_EVENT_END_TIME);

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConsoleTree.class);

    // Private Variables

    /**
     * User of this console tree.
     */
    private final IConsoleTreeUser user;

    /**
     * Time ruler margin width in pixels.
     */
    private final int timeRulerMarginWidth;

    /**
     * Time ruler margin height in pixels.
     */
    private final int timeRulerMarginHeight;

    /**
     * Parent composite, provided by {@link #createWidgets(Composite, Object)}.
     */
    private Composite parent;

    /**
     * Tree widget.
     */
    private Tree tree;

    /**
     * List of maps, each of the latter being a filter megawidget specifier in
     * raw form that is to be shown in column header menus.
     */
    private List<Map<String, Object>> filterMegawidgets;

    /**
     * Time line ruler widget.
     */
    private MultiValueRuler ruler;

    /**
     * Tree tool tip, used to display hint text for tree cells.
     */
    private CustomToolTip treeToolTip;

    /**
     * Current time as epoch time in milliseconds.
     */
    private long currentTime = HazardConstants.MIN_TIME;

    /**
     * Start time of the selected time range, as epoch time in milliseconds. The
     * initial value is merely a placeholder in case the ruler widget is created
     * before initialization.
     */
    private long selectedTimeStart = currentTime;

    /**
     * End time of the selected time range, as epoch time in milliseconds. If
     * the mode is "single" and not "range", this will be equal to
     * {@link #selectedTimeStart}. The initial value is merely a placeholder in
     * case the ruler widget is created before initialization.
     */
    private long selectedTimeEnd = currentTime;

    /**
     * Selected time mode.
     */
    private SelectedTimeMode selectedTimeMode = SelectedTimeMode.SINGLE;

    /**
     * Amount of time visible at once in the time line as a time range in
     * milliseconds. The initial value is merely a placeholder in case the ruler
     * widget is created before initialization.
     */
    private long visibleTimeRange = TimeUnit.HOURS.toMillis(1);

    /**
     * Time resolution for the time line ruler.
     */
    private TimeResolution timeResolution = TimeResolution.MINUTES;

    /**
     * Date formatter for date-time strings with minutes resolution.
     */
    private final DateFormat minutesDateTimeFormatter;

    /**
     * Date formatter for date-time strings with seconds resolution.
     */
    private final DateFormat secondsDateTimeFormatter;

    /**
     * Bidirectional map pairing column identifiers with the corresponding
     * column names.
     */
    private final BiMap<String, String> columnNamesForIdentifiers = HashBiMap
            .create();

    /**
     * Map of column names to definitions of the corresponding columns.
     */
    private ImmutableMap<String, ColumnDefinition> columnDefinitionsForNames = ImmutableMap
            .of();

    /**
     * Columns information.
     */
    private ConsoleColumns columns = null;

    /**
     * List of visible column names. The order of this list is the order in
     * which the columns appear in the tree, meaning that when the
     * {@link Tree#getColumnOrder()} method is called, the name of the column
     * found in the tree at the index specified by the returned array's Nth item
     * will be the same as that found as the Nth item in this list.
     */
    private List<String> visibleColumnNames = new ArrayList<>();

    /**
     * Map pairing column names with context-sensitive menus to be popped up
     * over the columns' headers.
     */
    private Map<String, Menu> headerMenusForColumnNames;

    /**
     * Map pairing visible column names that may need to display hint text as
     * tooltips with the hazard event parameter identifiers indicating what hint
     * text should be displayed.
     */
    private final Map<String, String> hintTextIdentifiersForVisibleColumnNames = new HashMap<>();

    /**
     * Map pairing visible column names that hold dates as their cell data to
     * the identifiers indicating the hazard event parameters that hold their
     * date values.
     */
    private final Map<String, String> dateIdentifiersForVisibleColumnNames = new HashMap<>();

    /**
     * Map pairing column names with megawidget managers for those columns that
     * have megawidget-bearing context-sensitive menus associated with their
     * headers.
     */
    private Map<String, MegawidgetManager> headerMegawidgetManagersForColumnNames;

    /**
     * Map pairing column identifiers with the states of the column-based
     * filters for those columns.
     */
    private final Map<String, Object> headerFilterStatesForColumnIdentifiers = new HashMap<>();

    /**
     * Map pairing column menus with their primary sort submenus.
     */
    private final Map<Menu, Menu> primarySortMenusForColumnMenus = new HashMap<>();

    /**
     * Map pairing column menus with their secondary sort submenus.
     */
    private final Map<Menu, Menu> secondarySortMenusForColumnMenus = new HashMap<>();

    /**
     * Hazard event context-sensitive menu.
     */
    private Menu rowMenu;

    /**
     * "Until further notice" menu item.
     */
    private MenuItem untilFurtherNoticeMenuItem;

    /**
     * List of root entities, each of which is represented by a tree item.
     */
    private final List<TabularEntity> tabularEntities = new ArrayList<>();

    /**
     * Map of root entity identifiers to the indices at which their entities are
     * found in {@link #tabularEntities}.
     */
    private final Map<String, Integer> indicesForRootIdentifiers = new HashMap<>();

    /**
     * Map of root entity identifiers to the corresponding entities, each of
     * which is found within {@link #tabularEntities}.
     */
    private final Map<String, TabularEntity> tabularEntitiesForRootIdentifiers = new HashMap<>();

    /**
     * Map of tabular entities to the tree items that represent them.
     */
    private final Map<TabularEntity, TreeItem> treeItemsForEntities = new IdentityHashMap<>();

    /**
     * Map of tree items to the tabular entities that the former represent.
     * <p>
     * TODO: Note that if an identity bi-map class existed, an instance of such
     * could be used to replace this and {@link #treeItemsForEntities}.
     * </p>
     */
    private final Map<TreeItem, TabularEntity> entitiesForTreeItems = new IdentityHashMap<>();

    /**
     * Map of scale widgets used to display allow manipulation of the time
     * ranges of tabular entities to the tree items in which they are embedded.
     */
    private final Map<MultiValueScale, TreeItem> treeItemsForScales = new IdentityHashMap<>();

    /**
     * Map of entities, all of which are either found in
     * {@link #tabularEntities} or as children of the entities found within that
     * list, to the editors used to contain their time range scale widgets.
     */
    private final Map<TabularEntity, TreeEditor> treeEditorsForEntities = new IdentityHashMap<>();

    /**
     * Countdown timer display manager.
     */
    private ConsoleCountdownTimersDisplayManager countdownTimersDisplayManager;

    /**
     * Countdown timer display listener, a listener for notifications that the
     * countdown timer displays need updating.
     */
    private final ICountdownTimersDisplayListener countdownTimersDisplayListener = new ICountdownTimersDisplayListener() {
        @Override
        public void countdownTimerDisplaysChanged(
                CountdownTimersDisplayManager<?, ?> manager) {
            updateCountdownTimers();
        }

        @Override
        public void allCountdownTimerDisplaysChanged(
                CountdownTimersDisplayManager<?, ?> manager) {
            updateAllCountdownTimers();
        }
    };

    /**
     * Display properties for the countdown timer tree cell that has experienced
     * the {@link SWT#EraseItem} event but not the {@link SWT#PaintItem}. For
     * each cell, the latter event immediately follows the former, before any
     * other cell is erased or painted, so this reference is merely used to save
     * the display properties for a cell in the very short time delta between
     * the erasing and painting of said cell. It is never used beyond this.
     */
    private ConsoleCountdownTimerDisplayProperties countdownTimerDisplayProperties;

    /**
     * Name of the column holding the countdown timers, if any.
     */
    private String countdownTimerColumnName;

    /**
     * Index of the column holding the countdown timers, or <code>-1</code> if
     * no such column is visible.
     */
    private int countdownTimerColumnIndex = -1;

    /**
     * Map of column indices to the last recorded widths of the columns; each
     * such value is merely the last width given by the {@link SWT#EraseItem}
     * event for that column.
     */
    private final Map<Integer, Integer> columnWidthsForColumnIndices = new HashMap<>();

    /**
     * List of sorts currently in use.
     */
    private ImmutableList<Sort> sorts = ImmutableList.of();

    /**
     * Flag indicating whether or not the time line ruler should display
     * tooltips for all times along its length.
     */
    private boolean showRulerToolTipsForAllTimes = true;

    /**
     * Epoch time in milliseconds of the last mouse or other event that occurred
     * that should be ignored for the purposes of changing the tree selection.
     */
    private long lastIgnorableInputEventTime;

    /**
     * Delta between the lower and upper bounds of the selected time range, as
     * recorded before the last switch was made to single-selected-time mode.
     */
    private long lastSelectedTimeRangeDelta;

    /**
     * Flag indicating whether or not column move events should be ignored.
     */
    private boolean ignoreMove;

    /**
     * Flag indicating whether or not column resize events should be ignored.
     */
    private boolean ignoreResize;

    /**
     * Flag indicating whether or not a refitting of the timeline ruler its
     * column header is scheduled to occur.
     */
    private boolean willRefitRulerToColumn;

    /**
     * Flag indicating whether or not a notification of a column modification is
     * scheduled to occur.
     */
    private boolean willNotifyOfColumnChange;

    /**
     * Flag indicating whether or not the vertical scrollbar of the tree was
     * showing the last time the ruler's layout was done.
     */
    private boolean verticalScrollbarShowing;

    /**
     * Flag indicating that the visible column count just changed.
     */
    private boolean visibleColumnCountJustChanged;

    /**
     * Time scale columm width before resize of column started.
     */
    private int timeScaleColumnWidthBeforeResize = -1;

    /**
     * The number of pixels between the time ruler and the top of the parent
     * composite.
     */
    private int rulerTopOffset;

    /**
     * Unchecked menu item image.
     */
    private final Image uncheckedMenuItemImage = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            UNCHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Semi-checked menu item image.
     */
    private final Image semiCheckedMenuItemImage = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            SEMI_CHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Checked menu item image.
     */
    private final Image checkedMenuItemImage = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            CHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Current time color.
     */
    private final Color currentTimeColor = new Color(Display.getCurrent(), 50,
            130, 50);

    /**
     * Selected time color.
     */
    private final Color selectedTimeColor = new Color(Display.getCurrent(),
            170, 56, 56);

    /**
     * Time range (along the ruler) edge color.
     */
    private final Color timeRangeEdgeColor = new Color(Display.getCurrent(),
            170, 56, 56);

    /**
     * Time range (along the ruler) fill color.
     */
    private final Color timeRangeFillColor = new Color(Display.getCurrent(),
            224, 190, 190);

    /**
     * Resources that must be disposed of when disposing of this class.
     */
    private final Set<Resource> resources = Sets.newHashSet(
            uncheckedMenuItemImage, semiCheckedMenuItemImage,
            checkedMenuItemImage, currentTimeColor, selectedTimeColor,
            timeRangeEdgeColor, timeRangeFillColor);

    /**
     * Map of RGB triplets to the corresponding colors created for use as visual
     * time range indicators.
     */
    private final Map<RGB, Color> timeRangeColorsForRgbs = new HashMap<>();

    /**
     * Spacer image, used to ensure that column headers are tall enough to
     * handle the time ruler embedded in the last column header.
     */
    private Image spacerImage;

    /**
     * Bold font for table rows representing entities that have unsaved changes.
     */
    private Font boldFont;

    /**
     * Tree rows that are currently selected.
     */
    private TreeItem[] selectedItems;

    /**
     * Temporal display implementation, allowing external elements to manipulate
     * the time line ruler.
     */
    private final ITemporalDisplay temporalDisplay = new ITemporalDisplay() {

        @Override
        public void zoomTimeOut() {
            long newVisibleTimeRange = WidgetUtilities
                    .getTimeLineRulerZoomedOutRange(ruler);
            if (newVisibleTimeRange <= WidgetUtilities
                    .getTimeLineRulerMaximumVisibleTimeRange()) {
                zoomVisibleTimeRange(newVisibleTimeRange);
            }
        }

        @Override
        public void pageTimeBack() {
            panTime(PAGE_TIME_DELTA_MULTIPLIER * -1.0f);
        }

        @Override
        public void panTimeBack() {
            panTime(PAN_TIME_DELTA_MULTIPLIER * -1.0f);
        }

        @Override
        public void showCurrentTime() {
            long centerTime = ruler.getFreeMarkedValue(0);
            long lower = centerTime - (visibleTimeRange / 8L);
            long upper = lower + visibleTimeRange - 1L;
            setVisibleTimeRange(Range.closed(lower, upper), true);
        }

        @Override
        public void panTimeForward() {
            panTime(PAN_TIME_DELTA_MULTIPLIER);
        }

        @Override
        public void pageTimeForward() {
            panTime(PAGE_TIME_DELTA_MULTIPLIER);
        }

        @Override
        public void zoomTimeIn() {
            long newVisibleTimeRange = WidgetUtilities
                    .getTimeLineRulerZoomedInRange(ruler);
            if (newVisibleTimeRange >= WidgetUtilities
                    .getTimeLineRulerMinimumVisibleTimeRange()) {
                zoomVisibleTimeRange(newVisibleTimeRange);
            }
        }

        @Override
        public SelectedTimeMode getSelectedTimeMode() {
            return selectedTimeMode;
        }

        @Override
        public void setSelectedTimeMode(SelectedTimeMode mode) {
            if (selectedTimeMode == mode) {
                return;
            }
            selectedTimeMode = mode;
            if (mode == SelectedTimeMode.SINGLE) {
                lastSelectedTimeRangeDelta = selectedTimeEnd
                        - selectedTimeStart;
                selectedTimeEnd = selectedTimeStart;
                for (TreeEditor editor : treeEditorsForEntities.values()) {
                    MultiValueScale scale = (MultiValueScale) editor
                            .getEditor();
                    scale.setConstrainedMarkedValues();
                    scale.setFreeMarkedValues(currentTime, selectedTimeStart);
                    scale.setFreeMarkedValueColor(1, selectedTimeColor);
                }
                ruler.setConstrainedThumbValues();
                ruler.setFreeThumbValues(selectedTimeStart);
                ruler.setFreeThumbColor(0, selectedTimeColor);
            } else {
                if (selectedTimeStart == selectedTimeEnd) {
                    selectedTimeEnd = selectedTimeStart
                            + (lastSelectedTimeRangeDelta > 0L ? lastSelectedTimeRangeDelta
                                    : DEFAULT_SELECTED_TIME_RANGE);
                }
                setSelectedTimeRange(Range.closed(selectedTimeStart,
                        selectedTimeEnd));
            }
            notifyHandlerOfSelectedTimeRangeChange();
        }
    };

    /**
     * Sort invocation handler. The identifier is the sort that has been
     * invoked.
     */
    private ICommandInvocationHandler<Sort> sortInvocationHandler;

    /**
     * Sort invoker. The identifier is the sort that has been invoked.
     */
    private final ICommandInvoker<Sort> sortInvoker = new ICommandInvoker<Sort>() {

        @Override
        public void setEnabled(Sort identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable sort command");
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<Sort> handler) {
            sortInvocationHandler = handler;
        }
    };

    /**
     * Time range state change handler. The identifier indicates the type.
     */
    private IStateChangeHandler<TimeRangeType, Range<Long>> timeRangeChangeHandler;

    /**
     * Time range state changer. The identifier indicates the type.
     */
    private final IStateChanger<TimeRangeType, Range<Long>> timeRangeChanger = new IStateChanger<TimeRangeType, Range<Long>>() {

        @Override
        public void setEnabled(TimeRangeType identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable time range");
        }

        @Override
        public void setEditable(TimeRangeType identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of time range");
        }

        @Override
        public Range<Long> getState(TimeRangeType identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of time range");
        }

        @Override
        public void setState(TimeRangeType identifier, Range<Long> value) {
            if (identifier == TimeRangeType.VISIBLE) {
                setVisibleTimeRange(value, false);
            } else {
                setSelectedTimeRange(value);
            }
        }

        @Override
        public void setStates(
                Map<TimeRangeType, Range<Long>> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for time range");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<TimeRangeType, Range<Long>> handler) {
            timeRangeChangeHandler = handler;
        }
    };

    /**
     * Columns state change handler. The identifier is ignored.
     */
    private IStateChangeHandler<String, ConsoleColumns> columnsChangeHandler;

    /**
     * Columns state changer. The identifier is ignored.
     */
    private final IStateChanger<String, ConsoleColumns> columnsChanger = new IStateChanger<String, ConsoleColumns>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable columns");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of columns");
        }

        @Override
        public ConsoleColumns getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of columns");
        }

        @Override
        public void setState(String identifier, ConsoleColumns value) {
            setColumns(value);
        }

        @Override
        public void setStates(Map<String, ConsoleColumns> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot change multiple states for columns");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, ConsoleColumns> handler) {
            columnsChangeHandler = handler;
        }
    };

    /**
     * Column-based filters change handler. The identifier is the column
     * identifier.
     */
    private IStateChangeHandler<String, Object> columnFiltersChangeHandler;

    /**
     * Column-based filters state changer. The identifier is the column
     * identifier.
     */
    private final IStateChanger<String, Object> columnFiltersChanger = new IStateChanger<String, Object>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable column filters");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of column filters");
        }

        @Override
        public ConsoleColumns getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of column filters");
        }

        @Override
        public void setState(String identifier, Object value) {
            headerFilterStatesForColumnIdentifiers.put(identifier, value);
        }

        @Override
        public void setStates(Map<String, Object> valuesForIdentifiers) {
            for (Map.Entry<String, Object> entry : valuesForIdentifiers
                    .entrySet()) {
                headerFilterStatesForColumnIdentifiers.put(entry.getKey(),
                        entry.getValue());
            }
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Object> handler) {
            columnFiltersChangeHandler = handler;
        }
    };

    /**
     * Tree contents state change handler. The identifier is ignored.
     */
    private IListStateChangeHandler<String, TabularEntity> treeContentsChangeHandler;

    /**
     * Tree contents state changer. The identifier is ignored.
     */
    private final IListStateChanger<String, TabularEntity> treeContentsChanger = new IListStateChanger<String, TabularEntity>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable tree contents");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of tree contents");
        }

        @Override
        public List<TabularEntity> get(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of tree contents");
        }

        @Override
        public void clear(String identifier) {
            clearEntities();
        }

        @Override
        public void set(String identifier,
                List<? extends TabularEntity> elements) {
            setEntities(elements);
        }

        @Override
        public void addElement(String identifier, TabularEntity element) {
            addEntity(element);
        }

        @Override
        public void addElements(String identifier,
                List<? extends TabularEntity> elements) {
            throw new UnsupportedOperationException(
                    "cannot simultaneously add multiple entities to tree contents");
        }

        @Override
        public void insertElement(String identifier, int index,
                TabularEntity element) {
            insertEntity(index, element);
        }

        @Override
        public void insertElements(String identifier, int index,
                List<? extends TabularEntity> elements) {
            throw new UnsupportedOperationException(
                    "cannot simultaneously insert multiple entities to tree contents");
        }

        @Override
        public void replaceElement(String identifier, int index,
                TabularEntity element) {
            replaceEntity(index, element);
        }

        @Override
        public void replaceElements(String identifier, int index, int count,
                List<? extends TabularEntity> elements) {
            throw new UnsupportedOperationException(
                    "cannot simultaneously replace multiple entities to tree contents");
        }

        @Override
        public void removeElement(String identifier, int index) {
            removeEntity(index);
        }

        @Override
        public void removeElements(String identifier, int index, int count) {
            throw new UnsupportedOperationException(
                    "cannot simultaneously remove multiple entities to tree contents");
        }

        @Override
        public void setListStateChangeHandler(
                IListStateChangeHandler<String, TabularEntity> handler) {
            treeContentsChangeHandler = handler;
        }
    };

    /**
     * Mouse wheel filter, used to scroll the tree by one line up or down.
     */
    private final Listener mouseWheelFilter = new Listener() {
        @Override
        public void handleEvent(Event event) {

            /*
             * If the widget receiving the event is one of the time scales in
             * the tree, or is the tree itself, scroll the tree up or down as
             * appropriate, and ensure that the widget will not handle the event
             * itself by canceling the latter.
             */
            if ((event.widget == tree)
                    || (event.widget instanceof MultiValueScale)) {

                /*
                 * Set the tree topmost visible item to be the next or previous
                 * one from the current one.
                 */
                int delta = (event.count < 0 ? 1 : -1);
                TreeItem topItem = tree.getTopItem();
                TreeItem newTopItem = null;
                if (topItem != null) {

                    /*
                     * Handle this one way if the old topmost item is a root
                     * item, another way if it is a child item.
                     */
                    if (topItem.getParentItem() == null) {

                        /*
                         * Find out where the old topmost item is in the list of
                         * items for the tree.
                         */
                        int index = Utils.getIndexOfElementInArray(topItem,
                                tree.getItems());

                        /*
                         * If the delta is negative, handle it one way; if
                         * positive, handle it another.
                         */
                        if (delta == -1) {

                            /*
                             * Get the next item up in the tree; if that item
                             * has children and its child list is expanded, make
                             * the last item in the child list the topmost.
                             * Otherwise, use the parent item as the topmost.
                             */
                            if (index > 0) {
                                newTopItem = tree.getItem(index - 1);
                                if (newTopItem.getExpanded()) {
                                    newTopItem = newTopItem.getItem(newTopItem
                                            .getItemCount() - 1);
                                }
                            }
                        } else {

                            /*
                             * Get the next item down in the tree, unless the
                             * old topmost item has children and its child list
                             * is expanded, in which case make its first child
                             * the topmost.
                             */
                            if ((index < tree.getItemCount() - 1)
                                    || (topItem.getExpanded() && (topItem
                                            .getItemCount() > 0))) {
                                newTopItem = (topItem.getExpanded()
                                        && (topItem.getItemCount() > 0) ? topItem
                                        .getItem(0) : tree.getItem(index + 1));
                            }
                        }
                    } else {

                        /*
                         * Find out where the old topmost item is in the list of
                         * sibling items for the current topmost item.
                         */
                        TreeItem parentItem = topItem.getParentItem();
                        TreeItem[] siblingItems = parentItem.getItems();
                        int index = Utils.getIndexOfElementInArray(topItem,
                                siblingItems);

                        /*
                         * If the delta is negative, handle it one way; if
                         * positive, handle it another.
                         */
                        if (delta == -1) {

                            /*
                             * If this is the first child in the siblings list,
                             * make the parent the topmost item; otherwise, use
                             * the previous child in the siblings list.
                             */
                            if (index == 0) {
                                newTopItem = parentItem;
                            } else {
                                newTopItem = siblingItems[index - 1];
                            }
                        } else {

                            /*
                             * If this is the last child in the siblings list,
                             * make the next sibling of the parent the topmost
                             * item (if such a sibling exists); otherwise, use
                             * the next child in the siblings list.
                             */
                            if (index == parentItem.getItemCount() - 1) {
                                index = Utils.getIndexOfElementInArray(
                                        parentItem, tree.getItems());
                                if (index < tree.getItemCount() - 1) {
                                    newTopItem = tree.getItem(index + 1);
                                }
                            } else {
                                newTopItem = siblingItems[index + 1];
                            }
                        }
                    }

                    /*
                     * If a new topmost item was found, use it. If the new
                     * topmost item is the first one in the tree, set the
                     * vertical scrollbar to show the topmost pixel, since the
                     * setTopItem() method does not scroll to the very top if
                     * only part of the topmost row is showing.
                     */
                    if (newTopItem != null) {
                        tree.setTopItem(newTopItem);
                        if ((delta == -1) && (newTopItem == tree.getItem(0))) {
                            tree.getVerticalBar().setSelection(0);
                        }
                    }
                }

                /*
                 * Do not allow further processing of this event.
                 */
                event.doit = false;
            }
        }
    };

    /**
     * Header menu item selection listener, for toggling column visibility.
     */
    private final SelectionListener headerMenuListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            handleColumnAdditionOrRemoval(((MenuItem) e.widget).getText());
        }
    };

    /**
     * Control listener that responds to resizes and moves of tree columns.
     */
    private final ControlListener columnControlListener = new ControlListener() {
        @Override
        public void controlResized(ControlEvent e) {
            handleColumnResized((TreeColumn) e.getSource());
        }

        @Override
        public void controlMoved(ControlEvent e) {
            handleColumnMoved();
        }
    };

    /**
     * Runnable that ensures that the last column in the tree is the time scale
     * column. This is scheduled to run asynchronously when a column is detected
     * to have been moved to the end of the tree, past the time scale column.
     */
    private final Runnable ensureTimeScaleIsLastColumnAction = new Runnable() {
        @Override
        public void run() {
            if ((tree != null) && (tree.isDisposed() == false)) {

                /*
                 * If the column order is wrong, move the time scale column to
                 * the end.
                 */
                int[] columnOrder = tree.getColumnOrder();
                if (columnOrder[columnOrder.length - 1] != columnOrder.length - 1) {
                    boolean foundLastColumn = false;
                    for (int j = 0; j < columnOrder.length - 1; j++) {
                        if (columnOrder[j] == columnOrder.length - 1) {
                            foundLastColumn = true;
                        }
                        columnOrder[j] = columnOrder[foundLastColumn ? j + 1
                                : j];
                    }
                    columnOrder[columnOrder.length - 1] = columnOrder.length - 1;
                    tree.setColumnOrder(columnOrder);
                }

                /*
                 * Handle the reordering of the columns.
                 */
                handleColumnReorderingViaDrag(false);

                /*
                 * Turn on redraw for the tree, since it was turned off by the
                 * handler that scheduled the invocation of this runnable.
                 */
                tree.setRedraw(true);
            }
        }
    };

    /**
     * Sort listener, used to listen for column-based sorts.
     */
    private final SelectionListener sortListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            handleColumnChosenForPrimarySort((TreeColumn) e.widget);
        }
    };

    /**
     * Thumb tooltip text provider for the time line ruler and time scales.
     */
    private final IMultiValueTooltipTextProvider thumbTooltipTextProvider = new IMultiValueTooltipTextProvider() {

        // Private Variables

        /**
         * Array of text strings to be shown when displaying a tooltip for
         * selected time.
         */
        private final String[] selectedTimeText = { "Selected Time:", null };

        /**
         * Array of text strings to be shown when displaying a tooltip for
         * selected time range start.
         */
        private final String[] selectedRangeStartText = {
                "Selected Range Start:", null };

        /**
         * Array of text strings to be shown when displaying a tooltip for
         * selected time range end.
         */
        private final String[] selectedRangeEndText = { "Selected Range End:",
                null };

        /**
         * Array of text strings to be shown when displaying a tooltip for event
         * time range start.
         */
        private final String[] eventStartTimeText = { "Event Start Time:", null };

        /**
         * Array of text strings to be shown when displaying a tooltip for event
         * time range end.
         */
        private final String[] eventEndTimeText = { "Event End Time:", null };

        /**
         * Array of text strings to be shown when displaying any other tooltip.
         */
        private final String[] otherValueText = { null };

        // Public Methods

        @Override
        public String[] getTooltipTextForValue(MultiValueLinearControl widget,
                long value) {
            if ((widget == ruler) && showRulerToolTipsForAllTimes) {
                otherValueText[0] = getDateTimeString(value, timeResolution);
                return otherValueText;
            } else {
                return null;
            }
        }

        @Override
        public String[] getTooltipTextForConstrainedThumb(
                MultiValueLinearControl widget, int index, long value) {
            String[] text = (widget == ruler ? (index == 0 ? selectedRangeStartText
                    : selectedRangeEndText)
                    : (index == 0 ? eventStartTimeText : eventEndTimeText));
            if (widget == ruler) {
                text[1] = getDateTimeString(value, timeResolution);
            } else {
                text[1] = getDateTimeString(value,
                        entitiesForTreeItems
                                .get(treeItemsForScales.get(widget))
                                .getTimeResolution());
            }
            return text;
        }

        @Override
        public String[] getTooltipTextForFreeThumb(
                MultiValueLinearControl widget, int index, long value) {
            selectedTimeText[1] = getDateTimeString(value, timeResolution);
            return selectedTimeText;
        }
    };

    /**
     * Multi-value linear control listener for thumb movements on the hazard
     * event time scales.
     */
    private final IMultiValueLinearControlListener timeScaleListener = new IMultiValueLinearControlListener() {
        @Override
        public void visibleValueRangeChanged(MultiValueLinearControl widget,
                long lowerValue, long upperValue, ChangeSource source) {

            /*
             * No action.
             */
        }

        @Override
        public void constrainedThumbValuesChanged(
                MultiValueLinearControl widget, long[] values,
                ChangeSource source) {
            if ((source == MultiValueScale.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    || (source == MultiValueScale.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                /*
                 * Get the tree item that goes with this scale widget, and from
                 * it, get the entity that it represents.
                 */
                TreeItem item = treeItemsForScales.get(widget);
                TabularEntity entity = entitiesForTreeItems.get(item);

                /*
                 * Create a new entity that replaces the old one.
                 */
                entity = handleUserChangeOfEntity(entity,
                        Range.closed(values[0], values[1]),
                        entity.isEndTimeUntilFurtherNotice(),
                        entity.isSelected(), entity.isChecked(),
                        entity.getChildren(), item);

                /*
                 * Change the start and end time text in the tree item's cells,
                 * if the columns are showing.
                 */
                for (int j = 0; j < TIME_RANGE_COLUMN_IDENTIFIERS.size(); j++) {
                    String columnName = columnNamesForIdentifiers
                            .get(TIME_RANGE_COLUMN_IDENTIFIERS.get(j));
                    int columnIndex = getIndexOfColumnInTree(columnName);
                    if (columnIndex != -1) {
                        updateCell(columnIndex,
                                columnDefinitionsForNames.get(columnName),
                                item, entity);
                    }
                }

                /*
                 * Let the change handler know about the modification.
                 */
                if (treeContentsChangeHandler != null) {
                    treeContentsChangeHandler.listElementChanged(null, entity);
                }
            }
        }

        @Override
        public void freeThumbValuesChanged(MultiValueLinearControl widget,
                long[] values, ChangeSource source) {

            /*
             * No action.
             */
        }
    };

    /**
     * Sort menu item selection listener, for changing sort characteristics.
     */
    private final SelectionListener sortMenuListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {

            /*
             * Determine which column and in what direction the sort is to
             * occur, as well as whether this is a primary or secondary sort
             * configuration.
             */
            Menu columnLevelMenu = ((MenuItem) e.widget).getParent();
            Menu sortLevelMenu = columnLevelMenu.getParentItem().getParent();
            String sortName = ((MenuItem) e.widget).getParent().getParentItem()
                    .getText();
            SortDirection sortDirection = (SortDirection) e.widget.getData();
            int sortPriority = (sortLevelMenu.getParentItem().getText()
                    .equals(PRIMARY_SORT_MENU_NAME) ? 1 : 2);

            /*
             * Handle the choice of the new sort column.
             */
            handleColumnChosenForSort(sortName, sortDirection, sortPriority);
        }
    };

    /**
     * Row menu item selection listener, handling actions to be performed on a
     * row.
     */
    private final SelectionListener rowMenuListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (untilFurtherNoticeMenuItem == e.widget) {
                handleUserUntilFurtherNoticeToggle();
            }
        }
    };

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param user
     *            User of this console tree.
     * @param timeRulerMarginWidth
     *            Time ruler margin width in pixels.
     * @param timeRulerMarginHeight
     *            Time ruler margin height in pixels.
     */
    ConsoleTree(IConsoleTreeUser user, int timeRulerMarginWidth,
            int timeRulerMarginHeight) {
        this.user = user;
        this.timeRulerMarginWidth = timeRulerMarginWidth;
        this.timeRulerMarginHeight = timeRulerMarginHeight;

        /*
         * Initialize the date-time formatters.
         */
        minutesDateTimeFormatter = Utils
                .getGmtDateTimeFormatterWithMinutesResolution();
        secondsDateTimeFormatter = Utils
                .getGmtDateTimeFormatterWithSecondsResolution();
    }

    // Public Methods

    @Override
    public ICommandInvoker<Sort> getSortInvoker() {
        return sortInvoker;
    }

    @Override
    public IStateChanger<TimeRangeType, Range<Long>> getTimeRangeChanger() {
        return timeRangeChanger;
    }

    @Override
    public IStateChanger<String, ConsoleColumns> getColumnsChanger() {
        return columnsChanger;
    }

    @Override
    public IStateChanger<String, Object> getColumnFiltersChanger() {
        return columnFiltersChanger;
    }

    @Override
    public IListStateChanger<String, TabularEntity> getTreeContentsChanger() {
        return treeContentsChanger;
    }

    @Override
    public void setCurrentTime(Date currentTime) {

        /*
         * Round the current time down to the nearest unit before using it.
         */
        this.currentTime = truncateTimeForResolution(currentTime).getTime();

        /*
         * Update the current time marker on the time ruler and the hazard event
         * scale widgets.
         */
        if (ruler.isDisposed() == false) {
            ruler.setFreeMarkedValue(0, this.currentTime);
            for (TreeEditor editor : treeEditorsForEntities.values()) {
                ((MultiValueScale) editor.getEditor()).setFreeMarkedValue(0,
                        this.currentTime);
            }
        }
    }

    @Override
    public void setTimeResolution(TimeResolution timeResolution,
            Date currentTime) {

        /*
         * If the time resolution has changed, make a note of it. If the new
         * time resolution is seconds, update the current time, as the time line
         * ruler will otherwise keep showing a current time that has a
         * resolution of seconds instead of minutes. Also update the ruler's
         * snap value calculator.
         */
        if (this.timeResolution != timeResolution) {
            this.timeResolution = timeResolution;
            if (timeResolution == TimeResolution.MINUTES) {
                setCurrentTime(currentTime);
            }
            updateRulerSnapValueCalculator();
        }
    }

    @Override
    public void setSorts(ImmutableList<Sort> sorts) {
        this.sorts = sorts;
        updateColumnSortVisualCues();
    }

    @Override
    public void setActiveCountdownTimers(
            ImmutableMap<String, CountdownTimer> countdownTimersForEventIdentifiers) {
        if (countdownTimersDisplayManager == null) {
            return;
        }
        countdownTimersDisplayManager
                .updateCountdownTimers(countdownTimersForEventIdentifiers);
        updateAllCountdownTimers();
    }

    // Package-Private Methods

    /**
     * Initialize the tree. Note that due to the fact that a view part is the
     * ancestral widget, sometimes this method is called before
     * {@link #createWidgets(Composite, Object)}, and sometimes after.
     * 
     * @param filterMegawidgets
     *            List of maps, each of the latter being a filter megawidget
     *            specifier in raw form that is to be shown in column header
     *            menus.
     * @param currentTime
     *            Current time.
     * @param selectedTime
     *            Selected time.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as a time
     *            range in milliseconds.
     * @param timeResolution
     *            Time resolution.
     */
    void initialize(ImmutableList<Map<String, Object>> filterMegawidgets,
            Date currentTime, Date selectedTime, long visibleTimeRange,
            TimeResolution timeResolution) {

        this.filterMegawidgets = new ArrayList<>(filterMegawidgets.size());
        for (Map<String, Object> filterMegawidget : filterMegawidgets) {
            this.filterMegawidgets.add(new HashMap<>(filterMegawidget));
        }
        this.selectedTimeStart = this.selectedTimeEnd = selectedTime.getTime();
        this.visibleTimeRange = visibleTimeRange;
        this.timeResolution = timeResolution;

        /*
         * Round the current time down to the nearest unit before using it.
         */
        this.currentTime = truncateTimeForResolution(currentTime).getTime();
        updateRulerSnapValueCalculator();

        /*
         * If the ruler has been created, set its initial values.
         */
        if (ruler != null) {
            setInitialRulerValues();
        }

        /*
         * Create a countdown timer display manager.
         */
        countdownTimersDisplayManager = new ConsoleCountdownTimersDisplayManager(
                countdownTimersDisplayListener);
        if (tree != null) {
            countdownTimersDisplayManager.setBaseFont(tree.getFont());
        }
    }

    /**
     * Create the widgets required to build the user interface. Note that due to
     * the fact that a view part is the ancestral widget, sometimes this method
     * is called before
     * {@link #initialize(ImmutableList, Date, Date, long, TimeResolution)}, and
     * sometimes after.
     * 
     * @param parent
     *            The composite in which to create the widgets.
     * @param layoutData
     *            Optional layout data to be set via
     *            {@link Tree#setLayoutData(Object)} if provided.
     * @return Tree widget that was created as one of the widgets.
     */
    Control createWidgets(Composite parent, Object layoutData) {
        this.parent = parent;
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                dispose();
            }
        });
        createTree(parent, layoutData);
        createTreeColumns();
        createTimeRuler(parent);
        return tree;
    }

    /**
     * Set the focus to the tree widget.
     */
    void setFocus() {
        tree.setFocus();
    }

    /**
     * Get the minimum visible time.
     * 
     * @return Minimum visible time.
     */
    long getMinimumVisibleTime() {
        return ruler.getLowerVisibleValue();
    }

    /**
     * Get the maximum visible time.
     * 
     * @return Maximum visible time.
     */
    long getMaximumVisibleTime() {
        return ruler.getUpperVisibleValue();
    }

    /**
     * Get the theoretical range that would be used if the time line ruler was
     * zoomed out by one step, in milliseconds.
     * 
     * @return Theoretical zoomed out range.
     */
    long getZoomedOutRange() {
        return WidgetUtilities.getTimeLineRulerZoomedOutRange(ruler);
    }

    /**
     * Get the theoretical range that would be used if the time line ruler was
     * zoomed in by one step, in milliseconds.
     * 
     * @return Theoretical zoomed in range.
     */
    long getZoomedInRange() {
        return WidgetUtilities.getTimeLineRulerZoomedInRange(ruler);
    }

    /**
     * Get the temporal display, which allows external objects to manipulate the
     * time line.
     * 
     * @return Temporal display.
     */
    ITemporalDisplay getTemporalDisplay() {
        return temporalDisplay;
    }

    // Private Methods

    /**
     * Create the tree widget.
     * 
     * @param parent
     *            The composite in which to create the tree.
     * @param layoutData
     *            Optional layout data to be set via
     *            {@link Tree#setLayoutData(Object)} if provided.
     */
    private void createTree(Composite parent, Object layoutData) {

        /*
         * Create the tree and configure it, using the layout data if any was
         * provided.
         */
        tree = new Tree(parent, SWT.CHECK | SWT.MULTI | SWT.VIRTUAL);
        if (layoutData != null) {
            tree.setLayoutData(layoutData);
        }
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));

        /*
         * Give the countdown timer manager its base font if initialization has
         * already occurred.
         */
        if (countdownTimersDisplayManager != null) {
            countdownTimersDisplayManager.setBaseFont(tree.getFont());
        }

        /*
         * Create the bold font to be used for rows representing entities that
         * have unsaved changes.
         */
        TreeItem sampleTreeItem = new TreeItem(tree, SWT.NONE);
        Font baseFont = sampleTreeItem.getFont();
        FontData fontData = baseFont.getFontData()[0];
        boldFont = new Font(baseFont.getDevice(), fontData.getName(),
                fontData.getHeight(), SWT.BOLD);
        resources.add(boldFont);
        sampleTreeItem.dispose();

        /*
         * Add a listener to handle painting of the background of tree cells
         * when those cells are for active, unselected countdown timers, or are
         * part of bolded rows.
         */
        tree.addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                /*
                 * Determine whether this is a countdown timer cell; if it is
                 * not, or the cell does not have custom display properties, or
                 * the row is not a bolded row, handle the paint normally.
                 */
                countdownTimerDisplayProperties = null;
                boolean countdown = ((event.index == countdownTimerColumnIndex) && (countdownTimersDisplayManager != null));
                boolean unsaved = entitiesForTreeItems.get(event.item)
                        .isUnsaved();
                if ((countdown == false) && (unsaved == false)) {
                    return;
                }

                /*
                 * Save the width of this column, because the PaintItem event
                 * that follows for this cell will need it (that event only
                 * holds the width of the text to be drawn, not the width of the
                 * entire column).
                 */
                columnWidthsForColumnIndices.put(event.index, event.width);

                /*
                 * If the cell is a countdown timer cell, but does not have
                 * custom display properties, handle the erase normally unless
                 * it is also part of a bolded row, in which case just treat it
                 * as the latter.
                 */
                if (countdown) {
                    countdownTimerDisplayProperties = countdownTimersDisplayManager
                            .getDisplayPropertiesForEvent(entitiesForTreeItems
                                    .get(event.item).getIdentifier());
                    if (countdownTimerDisplayProperties == null) {
                        if (unsaved) {
                            countdown = false;
                        } else {
                            return;
                        }
                    }
                }

                /*
                 * If the cell is a countdown timer cell and it is selected or
                 * the background color is white, use the standard background,
                 * but make sure the foreground is not drawn in the default
                 * manner.
                 */
                if (countdown
                        && (((event.detail & SWT.SELECTED) != 0) || countdownTimerDisplayProperties
                                .getBackgroundColor().equals(
                                        Display.getCurrent().getSystemColor(
                                                SWT.COLOR_WHITE)))) {
                    event.detail &= ~(SWT.FOREGROUND | SWT.HOT);
                    return;
                }

                /*
                 * Paint the background, using a custom color if this is a
                 * countdown timer cell.
                 */
                Color oldBackground = null;
                if (countdown) {
                    oldBackground = event.gc.getBackground();
                    event.gc.setBackground(countdownTimerDisplayProperties
                            .getBackgroundColor());
                }
                event.gc.fillRectangle(event.x, event.y, event.width,
                        event.height);
                if (countdown) {
                    event.gc.setBackground(oldBackground);
                }
                event.detail &= ~(SWT.BACKGROUND | SWT.FOREGROUND | SWT.HOT);
            }
        });

        /*
         * Add a listener to handle painting of the foreground of tree cells
         * when those cells are for active countdown timers, and to use bold
         * font for those items that represent entities that have unsaved
         * changes.
         */
        tree.addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                /*
                 * Determine whether this is a countdown timer cell; if it is
                 * not, or the cell does not have custom display properties, or
                 * the row is not a bolded row, handle the paint normally.
                 */
                boolean countdown = ((event.index == countdownTimerColumnIndex) && (countdownTimerDisplayProperties != null));
                if ((countdown == false)
                        && (entitiesForTreeItems.get(event.item).isUnsaved() == false)) {
                    return;
                }

                /*
                 * Paint the foreground using the appropriate color. If this is
                 * a countdown timer column and the foreground color is black,
                 * no blinking is occurring, and the item is selected, use white
                 * instead; if the row is a bolded row, use white instead of
                 * black if the row is selected.
                 */
                Color oldForeground = event.gc.getForeground();
                Font oldFont = event.gc.getFont();
                Color foreground = null;
                if (countdown) {
                    foreground = countdownTimerDisplayProperties
                            .getForegroundColor();
                    if (((event.detail & SWT.SELECTED) != 0)
                            && (countdownTimerDisplayProperties.isBlinking() == false)
                            && foreground.equals(Display.getCurrent()
                                    .getSystemColor(SWT.COLOR_BLACK))) {
                        foreground = Display.getCurrent().getSystemColor(
                                SWT.COLOR_WHITE);
                    }
                } else {
                    foreground = Display
                            .getCurrent()
                            .getSystemColor(
                                    ((event.detail & SWT.SELECTED) != 0 ? SWT.COLOR_WHITE
                                            : SWT.COLOR_BLACK));
                }
                event.gc.setForeground(foreground);

                /*
                 * Use the countdown timer font if appropriate, or the bold font
                 * otherwise.
                 */
                event.gc.setFont(countdown ? countdownTimerDisplayProperties
                        .getFont() : boldFont);

                /*
                 * Get the extent of the text, and paint the text in the
                 * vertical center of the row, and either left-, right-, or
                 * horizontal-center-adjusted.
                 */
                String text = ((TreeItem) event.item).getText(event.index);
                Point size = event.gc.stringExtent(text);
                int textWidth = size.x + 5;
                int yOffset = (event.height - size.y) / 2;
                int alignment = tree.getColumn(event.index).getAlignment();
                event.gc.drawText(
                        text,
                        event.x
                                + (alignment == SWT.LEFT ? 0
                                        : (columnWidthsForColumnIndices
                                                .get(event.index) - textWidth)
                                                / (alignment == SWT.RIGHT ? 1
                                                        : 2)), event.y
                                + yOffset, true);

                /*
                 * Reset the color and font.
                 */
                event.gc.setForeground(oldForeground);
                event.gc.setFont(oldFont);
            }
        });

        /*
         * Add a listener for check and uncheck events that updates the tabular
         * event to match, and notifies any listeners of the event, as well as
         * updating the list of selected items whenever a selection or
         * deselection occurs.
         */
        tree.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * If the event is a check event, handle it as such, and
                 * remember its timestamp. If a child item was checked, uncheck
                 * it and ignore the event; otherwise, change the record for the
                 * entity and notify the listener. If it is a selection event,
                 * see if the timestamp is the same as a previous event that
                 * should not change the selection, and if so, prevent it from
                 * happening and reset the selection to what it last was;
                 * otherwise allow it.
                 */
                if (e.detail == SWT.CHECK) {
                    lastIgnorableInputEventTime = e.time;
                    TreeItem item = (TreeItem) e.item;
                    if (handleUserChangeOfCheckedState(item) == false) {
                        e.doit = false;
                    }
                } else {

                    /*
                     * If this event is to be ignored, do nothing with it;
                     * otherwise, process the selection and/or deselection of
                     * items.
                     */
                    if (e.time == lastIgnorableInputEventTime) {
                        e.doit = false;
                        if ((selectedItems == null)
                                || (selectedItems.length == 0)) {
                            tree.deselectAll();
                        } else {
                            tree.setSelection(selectedItems);
                        }
                    } else {
                        handleUserChangeOfSelectedState();
                    }
                }
            }
        });

        /*
         * Add a listener for horizontal scrollbar movements that prompt the
         * repositioning of the timeline in the last column's header.
         */
        tree.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if ((ruler != null) && (ruler.isDisposed() == false)
                        && ruler.isVisible()) {

                    /*
                     * Get the tree bounds for calculations be made as to the
                     * size and position of the timeline ruler. Since the width
                     * is sometimes given as 0, schedule another resize event to
                     * fire off later in this case. If this is not done, the
                     * timeline ruler is given the wrong bounds.
                     */
                    Rectangle treeBounds = tree.getBounds();
                    if (treeBounds.width == 0) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                visibleColumnsChanged(false);
                            }
                        });
                    } else {
                        fitRulerToColumn(tree
                                .getColumn(getIndexOfColumnInTree(TIME_SCALE_COLUMN_NAME)));
                    }
                }
            }
        });

        /*
         * Add a listener for popup menu request events, to be told when the
         * user has attempted to pop up a menu from the tree.
         */
        tree.addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {

                /*
                 * Determine whether or not the point clicked is within the
                 * header area, and allow the menu to be deployed only if it is.
                 */
                Point point = Display.getCurrent().map(null, tree,
                        new Point(e.x, e.y));
                int headerTop = tree.getClientArea().y;
                int headerBottom = headerTop + tree.getHeaderHeight();
                boolean headerClicked = ((point.y >= headerTop) && (point.y < headerBottom));

                /*
                 * Deploy the header menu if appropriate, or the item menu if an
                 * item was right-clicked.
                 */
                if (headerClicked) {

                    /*
                     * If a menu is created, go ahead with the menu deployment;
                     * otherwise, cancel it.
                     */
                    e.doit = createHeaderContextMenu(point);
                } else {

                    /*
                     * Create the menu and deploy it.
                     */
                    createRowContextMenu(point);
                }
            }
        });

        /*
         * Add a mouse hover listener to pop up tooltips when appropriate, and
         * to close them when the mouse exits the tree boundaries.
         */
        tree.addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {

                /*
                 * No action.
                 */
            }

            @Override
            public void mouseExit(MouseEvent e) {
                if ((treeToolTip != null) && treeToolTip.isVisible()) {
                    treeToolTip.setVisible(false);
                }
            }

            @Override
            public void mouseHover(MouseEvent e) {

                /*
                 * If the tooltip is already showing or does not exist, do
                 * nothing.
                 */
                if ((treeToolTip == null) || treeToolTip.isVisible()) {
                    return;
                }

                /*
                 * If the hover occurred over a tree row, see if it needs a
                 * tooltip.
                 */
                Point point = new Point(e.x, e.y);
                TreeItem item = tree.getItem(point);
                if (item != null) {

                    /*
                     * Iterate through the visible columns that show tooltips,
                     * seeing for each whether the point is within that column's
                     * bounds, and putting up a tooltip if it does.
                     */
                    for (String columnName : hintTextIdentifiersForVisibleColumnNames
                            .keySet()) {
                        int columnIndex = getIndexOfColumnInTree(columnName);
                        Rectangle cellBounds = item.getBounds(columnIndex);
                        if (cellBounds.contains(point)) {

                            /*
                             * Find the entity associated with the tree item,
                             * and from it, retrieve the value needed for the
                             * hint text. If it is missing this value, no hint
                             * text is to be shown.
                             */
                            TabularEntity entity = entitiesForTreeItems
                                    .get(item);
                            String hintTextIdentifier = hintTextIdentifiersForVisibleColumnNames
                                    .get(columnName);
                            Object value = entity.getAttributes().get(
                                    hintTextIdentifier);
                            String text = (value != null ? value.toString()
                                    : null);

                            /*
                             * Show the hint text if some has been found to be
                             * displayed. Also associate the bounds of the cell
                             * with the tooltip, so that later mouse coordinates
                             * can be tested to see if they still fall within
                             * the cell.
                             */
                            if ((text != null) && (text.equals("") == false)) {
                                treeToolTip.setMessage(text);
                                treeToolTip.setToolTipBounds(cellBounds);
                                treeToolTip.setLocation(tree
                                        .toDisplay(e.x, e.y));
                                treeToolTip.setVisible(true);
                            }
                            break;
                        }
                    }
                }
            }
        });

        /*
         * Add mouse listeners to close tooltips when the mouse moves out of the
         * cell boundaries, or when the mouse is clicked. Also include handling
         * of double-clicks to pan the timeline ruler to the date value in the
         * cell in which the double-click occurred, if a date value is found
         * there.
         */
        tree.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if ((treeToolTip != null) && treeToolTip.isVisible()) {
                    if (!(treeToolTip.getToolTipBounds().contains(e.x, e.y))) {
                        treeToolTip.setVisible(false);
                    }
                }
            }
        });
        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {

                /*
                 * If the hover occurred over a tree row, see if it is over a
                 * date cell.
                 */
                Point point = new Point(e.x, e.y);
                TreeItem item = tree.getItem(point);
                if (item != null) {

                    /*
                     * Iterate through the visible columns that have date
                     * values, seeing for each whether the point is within that
                     * column's bounds.
                     */
                    for (String columnName : dateIdentifiersForVisibleColumnNames
                            .keySet()) {
                        int columnIndex = getIndexOfColumnInTree(columnName);
                        Rectangle cellBounds = item.getBounds(columnIndex);
                        if (cellBounds.contains(point)) {

                            /*
                             * Find the entity associated with the tree item,
                             * and from it, retrieve the date value.
                             */
                            TabularEntity entity = entitiesForTreeItems
                                    .get(item);
                            String dateIdentifier = dateIdentifiersForVisibleColumnNames
                                    .get(columnName);
                            Object value = entity.getAttributes().get(
                                    dateIdentifier);
                            if (value != null) {
                                showTime(((Number) value).longValue());
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {

                /*
                 * Remember this timestamp as ignorable if this is a
                 * right-button press, since these should not cause tree row
                 * selection changes.
                 */
                if (e.button == 3) {
                    lastIgnorableInputEventTime = e.time;
                }

                if ((treeToolTip != null) && treeToolTip.isVisible()) {
                    treeToolTip.setVisible(false);
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {

                /*
                 * No action.
                 */
            }
        });

        /*
         * Create the tree tooltip.
         */
        treeToolTip = new CustomToolTip(tree.getShell(),
                PopupDialog.HOVER_SHELLSTYLE);

        /*
         * Install the mouse wheel filter for the tree.
         */
        tree.getDisplay().addFilter(SWT.MouseWheel, mouseWheelFilter);
    }

    /**
     * Create the time ruler widget.
     * 
     * @param parent
     *            Composite in which to place the time ruler.
     */
    private void createTimeRuler(Composite parent) {

        /*
         * Create the time line ruler.
         */
        ruler = WidgetUtilities.createTimeLineRuler(parent,
                HazardConstants.MIN_TIME, HazardConstants.MAX_TIME);

        /*
         * Use the appropriate snap value calculator.
         */
        updateRulerSnapValueCalculator();

        /*
         * Show appropriate tooltips for the selected time, current time, and
         * anywhere along the time line if so configured.
         */
        ruler.setTooltipTextProvider(thumbTooltipTextProvider);

        /*
         * Give the ruler appropriate padding.
         */
        ruler.setInsets(TIME_HORIZONTAL_PADDING, 0, TIME_HORIZONTAL_PADDING, 0);

        /*
         * Set the ruler's initial values.
         */
        setInitialRulerValues();

        /*
         * Configure the thumbs.
         */
        ruler.setFreeMarkedValueColor(0, currentTimeColor);
        ruler.setFreeMarkedValueDirection(0,
                MultiValueRuler.IndicatorDirection.DOWN);
        ruler.setFreeMarkedValueHeight(0, 1.0f);
        ruler.setFreeThumbColor(0, selectedTimeColor);
        ruler.setConstrainedThumbsDrawnAsBookends(true);

        /*
         * Ensure that changes to the visible time range or the selected
         * time/time range propagate appropriately.
         */
        ruler.addMultiValueLinearControlListener(new IMultiValueLinearControlListener() {
            @Override
            public void visibleValueRangeChanged(
                    MultiValueLinearControl widget, long lowerValue,
                    long upperValue, ChangeSource source) {
                updateWidgetsForNewVisibleTimeRange(lowerValue, upperValue);
                if (source != ChangeSource.METHOD_INVOCATION) {
                    notifyHandlerOfVisibleTimeRangeChange(lowerValue,
                            upperValue);
                }
            }

            @Override
            public void constrainedThumbValuesChanged(
                    MultiValueLinearControl widget, long[] values,
                    ChangeSource source) {
                if (values.length == 0) {
                    return;
                }
                selectedTimeStart = values[0];
                selectedTimeEnd = values[1];
                for (TreeEditor editor : treeEditorsForEntities.values()) {
                    ((MultiValueScale) editor.getEditor())
                            .setConstrainedMarkedValues(values);
                }
                notifyHandlerOfSelectedTimeRangeChange(source);
            }

            @Override
            public void freeThumbValuesChanged(MultiValueLinearControl widget,
                    long[] values, ChangeSource source) {
                if (values.length == 0) {
                    return;
                }
                selectedTimeStart = selectedTimeEnd = values[0];
                for (TreeEditor editor : treeEditorsForEntities.values()) {
                    ((MultiValueScale) editor.getEditor()).setFreeMarkedValue(
                            1, selectedTimeStart);
                }
                notifyHandlerOfSelectedTimeRangeChange(source);
            }
        });

        /*
         * Give the ruler a right-click popup menu that allows the user to
         * toggle the showing of tooltips anywhere along the ruler's length.
         */
        Menu contextMenu = new Menu(ruler);
        MenuItem item = new MenuItem(contextMenu, SWT.CHECK);
        item.setText(SHOW_TIME_UNDER_MOUSE_TOGGLE_MENU_TEXT);
        item.setSelection(true);
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showRulerToolTipsForAllTimes = ((MenuItem) e.widget)
                        .getSelection();
            }
        });
        ruler.setMenu(contextMenu);

        /*
         * Create a spacer image of a sufficient height to avoid having the
         * column headers smaller than they need to be in order to have the last
         * column header visually surround the time ruler, and assign it as the
         * image for each column.
         */
        spacerImage = ImageUtilities.convertAwtImageToSwt(new BufferedImage(1,
                ruler.computeSize(SWT.DEFAULT, SWT.DEFAULT).y,
                BufferedImage.TYPE_INT_ARGB));
        resources.add(spacerImage);
        for (TreeColumn column : tree.getColumns()) {
            column.setImage(spacerImage);
        }

        /*
         * Pack the composite with everything created so far, as the bounds of
         * the time ruler are needed to continue.
         */
        parent.pack(true);
        repackRuler();
    }

    /**
     * Set the time ruler's initial values.
     */
    private void setInitialRulerValues() {
        long lowerTime = currentTime - (visibleTimeRange / 4L);
        if (lowerTime < HazardConstants.MIN_TIME) {
            lowerTime = HazardConstants.MIN_TIME;
        }
        long upperTime = lowerTime + visibleTimeRange - 1L;
        if (upperTime > HazardConstants.MAX_TIME) {
            lowerTime -= upperTime - HazardConstants.MAX_TIME;
            upperTime = HazardConstants.MAX_TIME;
        } else if (upperTime <= lowerTime) {
            upperTime = HazardConstants.MAX_TIME;
        }
        ruler.setVisibleValueRange(lowerTime, upperTime);
        ruler.setFreeMarkedValues(currentTime);
        ruler.setFreeThumbValues(selectedTimeStart);

        /*
         * If the tree has been initialized, notify the handler of the visible
         * time range change.
         */
        if (filterMegawidgets != null) {
            notifyHandlerOfVisibleTimeRangeChange(lowerTime, upperTime);
        }
    }

    /**
     * Create the columns in the tree.
     */
    private void createTreeColumns() {

        /*
         * Get the number of columns, and add one for the time scale column.
         */
        int numberOfColumns = visibleColumnNames.size();

        /*
         * Create the user-specified columns.
         */
        for (int j = 0; j < numberOfColumns; j++) {
            createTreeColumn(visibleColumnNames.get(j), -1);
        }
        updateColumnSortVisualCues();

        /*
         * Create the time scale column.
         */
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        column.setText(TIME_SCALE_COLUMN_NAME);
        column.setMoveable(false);
        column.setResizable(true);
        column.pack();

        /*
         * Add a listener to the time scale column to allow it to respond to
         * column-reordering and -resizing events.
         */
        column.addControlListener(columnControlListener);
    }

    /**
     * Update the visual cues of the tree columns to indicate the current
     * primary sort column, if any.
     */
    private void updateColumnSortVisualCues() {
        if (sorts.isEmpty() == false) {
            Sort primarySort = sorts.get(0);
            String columnName = columnNamesForIdentifiers.get(primarySort
                    .getAttributeIdentifier());
            if (columnName != null) {
                for (TreeColumn column : tree.getColumns()) {
                    if (columnName.equals(column.getText())) {
                        tree.setSortColumn(column);
                        tree.setSortDirection(primarySort.getSortDirection() == SortDirection.ASCENDING ? SWT.UP
                                : SWT.DOWN);
                        return;
                    }
                }
            }
        }
        tree.setSortColumn(null);
    }

    /**
     * Get a color to be used for visual time range indication for the specified
     * entity.
     * 
     * @param entity
     *            Entity for which to generate the time range color.
     * @return Color to be used for visual time range indication.
     */
    private Color getTimeRangeColorForEntity(TabularEntity entity) {

        /*
         * See if the color has already been created, and if so, reuse it.
         */
        com.raytheon.uf.common.colormap.Color entityColor = entity.getColor();
        RGB rgb = new RGB((int) ((entityColor.getRed() * 255.0f) + 0.5f),
                (int) ((entityColor.getGreen() * 255.0f) + 0.5f),
                (int) ((entityColor.getBlue() * 255.0f) + 0.5f));
        Color color = timeRangeColorsForRgbs.get(rgb);

        /*
         * If the color is not on record, create a new color with the specified
         * RGB parameters, and add it to the record so that it can be reused and
         * disposed of later.
         */
        if ((color == null) || color.isDisposed()) {
            color = new Color(tree.getDisplay(), rgb.red, rgb.green, rgb.blue);
            timeRangeColorsForRgbs.put(rgb, color);
        }

        /*
         * Return the created or reused color.
         */
        return color;
    }

    /**
     * Dispose of any colors created for use as visual time range specifiers,
     * and clear any records of them.
     */
    private void disposeOfTimeRangeColors() {
        for (Color color : timeRangeColorsForRgbs.values()) {
            color.dispose();
        }
        timeRangeColorsForRgbs.clear();
    }

    /**
     * Dispose of the tree.
     */
    private void dispose() {

        /*
         * Delete any header menus for the columns.
         */
        deleteColumnHeaderMenus();

        /*
         * Dispose of the countdown timer display manager.
         */
        if (countdownTimersDisplayManager != null) {
            countdownTimersDisplayManager.dispose();
        }

        /*
         * Clear all entities and the tree items that go with them.
         */
        clearEntities();

        /*
         * Dispose of any resources that were created.
         */
        for (Resource resource : resources) {
            resource.dispose();
        }

        /*
         * Delete the tree tooltip.
         */
        if ((treeToolTip != null) && !treeToolTip.isVisible()) {
            treeToolTip.dispose();
            treeToolTip = null;
        }

        /*
         * Remove the mouse wheel filter.
         */
        tree.getDisplay().removeFilter(SWT.MouseWheel, mouseWheelFilter);
    }

    /**
     * Clear the entities in the tree.
     */
    private void clearEntities() {
        tabularEntities.clear();
        indicesForRootIdentifiers.clear();
        tabularEntitiesForRootIdentifiers.clear();
        treeItemsForEntities.clear();
        entitiesForTreeItems.clear();
        clearTree();
    }

    /**
     * Set the entities in the tree to those specified.
     * 
     * @param entities
     *            Entities to be used.
     */
    private void setEntities(List<? extends TabularEntity> entities) {

        tree.setRedraw(false);
        ruler.setRedraw(false);

        /*
         * Get the identifiers for entities whose corresponding tree items are
         * currently visible.
         */
        Set<Pair<String, Integer>> previouslyVisibleEntityIdentifiers = getEntityIdentifiersWithVisibleItems();

        /*
         * Determine which entities' associated root tree items are expanded and
         * showing children, so that any such items may be configured to be
         * expanded again if recreated.
         */
        Set<String> expandedEntityIdentifiers = getExpandedEntityIdentifiers();

        /*
         * Clear any previous entities and associated tree items.
         */
        clearEntities();

        /*
         * Iterate through the new entities, recording each one in turn and
         * creating tree items for them and their children.
         */
        Set<TreeItem> selectedTreeItems = Sets.newIdentityHashSet();
        int index = 0;
        for (TabularEntity entity : entities) {
            tabularEntities.add(entity);
            indicesForRootIdentifiers.put(entity.getIdentifier(), index);
            tabularEntitiesForRootIdentifiers.put(entity.getIdentifier(),
                    entity);
            addTreeItemsForEntity(index++, entity, null,
                    expandedEntityIdentifiers.contains(entity.getIdentifier()),
                    selectedTreeItems);
        }

        /*
         * Select the items representing entities that are to be selected.
         */
        if (selectedTreeItems.size() > 0) {
            tree.setSelection(selectedTreeItems
                    .toArray(new TreeItem[selectedTreeItems.size()]));
            selectedItems = tree.getSelection();
        } else {
            selectedItems = null;
        }

        /*
         * Prune the previously visible entity identifiers by removing any
         * identifiers corresponding to entities that are either no longer
         * represented in the tree, or else their corresponding tree items are
         * hidden by collapsed parents. Then prune them further to remove any
         * unselected entities.
         */
        Set<Pair<String, Integer>> potentiallyVisibleEntityIdentifiers = getVisibleEntityIdentifiers(previouslyVisibleEntityIdentifiers);
        Set<Pair<String, Integer>> potentiallyVisibleSelectedEntityIdentifiers = getSelectedVisibleEntityIdentifiers(previouslyVisibleEntityIdentifiers);

        /*
         * Ensure that the proper tree items are visible; these may be one or
         * more of the potentially visible selected ones as found above, or
         * potentially visible unselected ones, or just a selected one that was
         * not visible previously.
         */
        showAppropriateTreeItem(potentiallyVisibleSelectedEntityIdentifiers
                .isEmpty() ? potentiallyVisibleEntityIdentifiers
                : potentiallyVisibleSelectedEntityIdentifiers);

        ruler.setRedraw(true);
        tree.setRedraw(true);
    }

    /**
     * Add the specified entity to the end of the tree's items.
     * 
     * @param entity
     *            Entity to be added.
     */
    private void addEntity(TabularEntity entity) {

        /*
         * Add the entity to the list and record pertinent information.
         */
        int index = tabularEntities.size();
        tabularEntities.add(entity);
        indicesForRootIdentifiers.put(entity.getIdentifier(), index);
        tabularEntitiesForRootIdentifiers.put(entity.getIdentifier(), entity);

        /*
         * Create tree items representing it and any child entities it has.
         */
        Set<TreeItem> selectedTreeItems = Sets.newIdentityHashSet();
        addTreeItemsForEntity(index, entity, null, false, selectedTreeItems);

        /*
         * Ensure that if the new items need selection, they are selected.
         */
        addItemsToSelection(selectedTreeItems);

        /*
         * Ensure that the parent item is visible in the tree if it was
         * selected.
         */
        ensureTreeItemVisibilityIfSelected(entity);
    }

    /**
     * Insert the specified entity into the list of tree items at the specified
     * index.
     * 
     * @param index
     *            Index at which to insert the entity.
     * @param entity
     *            Entity to be inserted.
     */
    private void insertEntity(int index, TabularEntity entity) {

        /*
         * Insert the entity to the list, add one to each index recorded for
         * entities following it, and record pertinent information about the new
         * entity.
         */
        tabularEntities.add(index, entity);
        for (Map.Entry<String, Integer> entry : indicesForRootIdentifiers
                .entrySet()) {
            if (entry.getValue() >= index) {
                entry.setValue(entry.getValue() + 1);
            }
        }
        indicesForRootIdentifiers.put(entity.getIdentifier(), index);
        tabularEntitiesForRootIdentifiers.put(entity.getIdentifier(), entity);

        /*
         * Create tree items representing it and any child entities it has.
         */
        Set<TreeItem> selectedTreeItems = Sets.newIdentityHashSet();
        addTreeItemsForEntity(index, entity, null, false, selectedTreeItems);

        /*
         * Ensure that if the new items need selection, they are selected.
         */
        addItemsToSelection(selectedTreeItems);

        /*
         * Ensure that the parent item is visible in the tree if it was
         * selected.
         */
        ensureTreeItemVisibilityIfSelected(entity);
    }

    /**
     * Replace the entity at the specified index with the specified entity.
     * 
     * @param index
     *            Index at which to replace the entity.
     * @param entity
     *            New entity to be used as a replacement.
     */
    private void replaceEntity(int index, TabularEntity entity) {

        /*
         * Replace the entity in the list, remove all records pertaining to the
         * old entity and replace them with records for the new entity, and
         * replacing tree items representing the old entity and any child
         * entities it had with ones representing the new entity and any child
         * entities it has.
         */
        TabularEntity oldEntity = tabularEntities.set(index, entity);
        indicesForRootIdentifiers.remove(oldEntity.getIdentifier());
        indicesForRootIdentifiers.put(entity.getIdentifier(), index);
        tabularEntitiesForRootIdentifiers.remove(oldEntity.getIdentifier());
        tabularEntitiesForRootIdentifiers.put(entity.getIdentifier(), entity);
        Set<TreeItem> selectedTreeItems = Sets.newIdentityHashSet();
        Set<TreeItem> deselectedTreeItems = Sets.newIdentityHashSet();
        replaceTreeItemsForEntity(index, oldEntity, entity, null,
                selectedTreeItems, deselectedTreeItems);

        /*
         * Ensure that the selection set reflects the changes.
         */
        updateItemSelection(selectedTreeItems, deselectedTreeItems);

        /*
         * Ensure that the parent item is visible in the tree if it is selected
         * and the old one is either completely different (not the same
         * identifier) or it has the same identifier but was not selected.
         */
        if ((oldEntity.getIdentifier().equals(entity.getIdentifier()) == false)
                || (entity.isSelected() != oldEntity.isSelected())) {
            ensureTreeItemVisibilityIfSelected(entity);
        }
    }

    /**
     * Remove the entity at the specified index.
     * 
     * @param index
     *            Index at which to remove an entity.
     */
    private void removeEntity(int index) {

        /*
         * Remove the entity at the specified index from the list, remove any
         * record of the entity, subtract one from each index recorded for
         * entities that were formerly following the removed one.
         */
        TabularEntity entity = tabularEntities.remove(index);
        indicesForRootIdentifiers.remove(entity.getIdentifier());
        for (Map.Entry<String, Integer> entry : indicesForRootIdentifiers
                .entrySet()) {
            if (entry.getValue() > index) {
                entry.setValue(entry.getValue() - 1);
            }
        }
        tabularEntitiesForRootIdentifiers.remove(entity.getIdentifier());

        /*
         * Remove the tree items representing the removed entity and any child
         * entities it had.
         */
        Set<TreeItem> deselectedTreeItems = Sets.newIdentityHashSet();
        removeTreeItemsForEntity(entity, deselectedTreeItems);

        /*
         * Ensure that if any of the removed items were selected, they are
         * deselected.
         */
        removeItemsFromSelection(deselectedTreeItems);
    }

    /**
     * Add the tree items for the specified entity and any child entities they
     * have, inserting it at the specified index.
     * 
     * @param index
     *            Index at which to insert the tree item.
     * @param entity
     *            Entity for which to create the tree item.
     * @param parent
     *            Parent tree item, if this item is to be a child item.
     * @param expanded
     *            Flag indicating whether or not the created tree item is to be
     *            expanded if it has children. This is ignored if
     *            <code>parent</code> is not <code>null</code>.
     * @param selectedTreeItems
     *            Set of tree items to which to add any that are created in the
     *            course of this invocation that are to be selected themselves.
     */
    private void addTreeItemsForEntity(int index, TabularEntity entity,
            TreeItem parent, boolean expanded, Set<TreeItem> selectedTreeItems) {

        /*
         * Create the tree item as either a child of the tree itself, if it is a
         * root item, or as a child of the specified parent, and associate it
         * with the entity. Also set its checked state as appropriate.
         */
        TreeItem item = (parent == null ? new TreeItem(tree, SWT.NONE, index)
                : new TreeItem(parent, SWT.NONE, index));
        treeItemsForEntities.put(entity, item);
        entitiesForTreeItems.put(item, entity);
        item.setChecked(entity.isChecked());

        /*
         * For each cell in the row, insert the text appropriate to the column.
         */
        for (String name : visibleColumnNames) {
            updateCell(getIndexOfColumnInTree(name),
                    columnDefinitionsForNames.get(name), item, entity);
        }

        /*
         * Determine whether or not the row is to be selected.
         */
        if (entity.isSelected()) {
            selectedTreeItems.add(item);
        }

        /*
         * Create and configure the time scale and the editor that holds it.
         */
        MultiValueScale scale = createTimeScale(entity, item);
        configureScaleIntervalLockingForEntity(scale, entity);
        createTreeEditorForTimeScale(scale, entity, item);

        /*
         * If the entity has children, create them as well.
         */
        int childIndex = 0;
        for (TabularEntity childEntity : entity.getChildren()) {
            addTreeItemsForEntity(childIndex++, childEntity, item, false,
                    selectedTreeItems);
        }
        if (expanded && (item.getItemCount() > 0)) {
            item.setExpanded(true);
        }
    }

    /**
     * Replace the tree items for the entity at the specified index, and any
     * child entities said entity has, with tree items for the specified entity,
     * and any child entities the new entity has.
     * 
     * @param index
     *            Index at which the old entity was found.
     * @param oldEntity
     *            Entity to be replaced.
     * @param entity
     *            Entity to be used as the replacement.
     * @param parent
     *            Parent tree item, if this item is to be a child item.
     * @param selectedTreeItems
     *            Set of tree items to which to add any that are created in the
     *            course of this invocation that are to be selected themselves.
     * @param deselectedTreeItems
     *            Set of tree items to which to add any that are removed in the
     *            course of this invocation and which were selected.
     */
    private void replaceTreeItemsForEntity(int index, TabularEntity oldEntity,
            TabularEntity entity, TreeItem parent,
            Set<TreeItem> selectedTreeItems, Set<TreeItem> deselectedTreeItems) {

        /*
         * Remove any tree items for child entities that will not be needed with
         * the new child list, if any.
         */
        List<TabularEntity> oldChildEntities = oldEntity.getChildren();
        List<TabularEntity> childEntities = entity.getChildren();
        for (int childIndex = childEntities.size(); childIndex < oldChildEntities
                .size(); childIndex++) {
            removeTreeItemsForEntity(oldChildEntities.get(childIndex),
                    deselectedTreeItems);
        }

        /*
         * Get the tree item that was associated with the old entity, and remove
         * said association; then associate it with the new entity. If the
         * replacement is of a parent entity and the new entity does not have
         * the same identifier as the old one, collapse the reused item.
         */
        TreeItem item = treeItemsForEntities.remove(oldEntity);
        if ((parent == null)
                && (oldEntity.getIdentifier().equals(entity.getIdentifier()) == false)) {
            item.setExpanded(false);
        }
        treeItemsForEntities.put(entity, item);
        entitiesForTreeItems.put(item, entity);
        item.setChecked(entity.isChecked());

        /*
         * For each cell in the row, set the text appropriate to the column.
         */
        for (String name : visibleColumnNames) {
            updateCell(getIndexOfColumnInTree(name),
                    columnDefinitionsForNames.get(name), item, entity);
        }

        /*
         * If the unsaved flag changed value, force the tree to redraw the row,
         * because this will ensure that all cells in the row get redrawn,
         * wehther or not the individual cells changed their contents.
         */
        if (oldEntity.isUnsaved() != entity.isUnsaved()) {
            Rectangle rowBounds = item.getBounds();
            Rectangle treeBounds = tree.getClientArea();
            tree.redraw(treeBounds.x, rowBounds.y, treeBounds.width,
                    rowBounds.height, false);
        }

        /*
         * Determine whether or not selection or deselection is to occur.
         */
        if (oldEntity.isSelected() != entity.isSelected()) {
            (entity.isSelected() ? selectedTreeItems : deselectedTreeItems)
                    .add(item);
        }

        /*
         * Reconfigure the time scale and the editor that holds it that are
         * associated with the item being reused so that they represent the new
         * entity.
         */
        TreeEditor editor = treeEditorsForEntities.get(oldEntity);
        MultiValueScale scale = (MultiValueScale) editor.getEditor();
        reuseTimeScale(scale, entity);
        configureScaleIntervalLockingForEntity(scale, entity);
        reuseTreeEditorForTimeScale(oldEntity, entity);

        /*
         * Iterate through the new child entities, reusing old child tree items
         * for as many as possible, and creating new items for any remaining
         * entities for which there were no tree items to reuse.
         */
        for (int childIndex = 0; childIndex < childEntities.size(); childIndex++) {
            if (childIndex < oldChildEntities.size()) {
                replaceTreeItemsForEntity(childIndex,
                        oldChildEntities.get(childIndex),
                        childEntities.get(childIndex), item, selectedTreeItems,
                        deselectedTreeItems);
            } else {
                addTreeItemsForEntity(childIndex,
                        childEntities.get(childIndex), item, false,
                        selectedTreeItems);
            }
        }
    }

    /**
     * Remove the tree items for the specified entity and any child entities
     * they have.
     * 
     * @param entity
     *            Entity for which to remove the tree item.
     * @param deselectedTreeItems
     *            Set of tree items to which to add any that are removed in the
     *            course of this invocation and that were selected.
     */
    private void removeTreeItemsForEntity(TabularEntity entity,
            Set<TreeItem> deselectedTreeItems) {

        /*
         * If the entity has children, remove them first.
         */
        for (TabularEntity childEntity : entity.getChildren()) {
            removeTreeItemsForEntity(childEntity, deselectedTreeItems);
        }

        /*
         * Remove the time scale and associated editor.
         */
        removeTimeScaleAndAssociatedEditor(entity);

        /*
         * Get the item that goes with the entity, removing its linkage with the
         * entity, and then dispose of it.
         */
        TreeItem item = treeItemsForEntities.remove(entity);
        entitiesForTreeItems.remove(item);
        item.dispose();

        /*
         * Determine whether or not the row was selected.
         */
        if (entity.isSelected()) {
            deselectedTreeItems.add(item);
        }
    }

    /**
     * Get the set of parent entity identifiers for which the associated tree
     * items are expanded and have children.
     * 
     * @return Set of expanded entity identifiers.
     */
    private Set<String> getExpandedEntityIdentifiers() {
        Set<String> expandedEntityIdentifiers = new HashSet<>(
                tabularEntities.size(), 1.0f);
        for (TreeItem item : tree.getItems()) {
            if (item.getExpanded() && (item.getItemCount() > 0)) {
                expandedEntityIdentifiers.add(entitiesForTreeItems.get(item)
                        .getIdentifier());
            }
        }
        return expandedEntityIdentifiers;
    }

    /**
     * Add the specified selected tree items, if any, to the selection set.
     * 
     * @param selectedTreeItems
     *            Newly selected tree items.
     */
    private void addItemsToSelection(Set<TreeItem> selectedTreeItems) {

        /*
         * Ensure there is at least one item to be selected.
         */
        if (selectedTreeItems.isEmpty()) {
            return;
        }

        /*
         * Copy the old selection array to a new one, and add the newly selected
         * items to the end of the array. Then set the selection set to the new
         * array.
         */
        TreeItem[] oldSelectedTreeItems = tree.getSelection();
        TreeItem[] newSelectedTreeItems = new TreeItem[oldSelectedTreeItems.length
                + selectedTreeItems.size()];
        System.arraycopy(oldSelectedTreeItems, 0, newSelectedTreeItems, 0,
                oldSelectedTreeItems.length);
        TreeItem[] addedSelectedTreeItems = selectedTreeItems
                .toArray(new TreeItem[selectedTreeItems.size()]);
        System.arraycopy(addedSelectedTreeItems, 0, newSelectedTreeItems,
                oldSelectedTreeItems.length, addedSelectedTreeItems.length);
        tree.setSelection(newSelectedTreeItems);
        selectedItems = tree.getSelection();
    }

    /**
     * Add the specified selected tree items, if any, to the selection set,
     * while also removing the specified deselected tree items, if any, from the
     * selection set.
     * <p>
     * <strong>Note</strong>: The specified sets must be identity hash sets.
     * </p>
     * 
     * @param selectedTreeItems
     *            Newly selected tree items; must be an identity hash set
     *            created using {@link Sets#newIdentityHashSet()}.
     * @param deselectedTreeItems
     *            Newly deselected tree items; must be an identity hash set
     *            created using {@link Sets#newIdentityHashSet()}
     */
    private void updateItemSelection(Set<TreeItem> selectedTreeItems,
            Set<TreeItem> deselectedTreeItems) {

        /*
         * Ensure a change is requested before doing anything.
         */
        if (selectedTreeItems.isEmpty() && deselectedTreeItems.isEmpty()) {
            return;
        }

        /*
         * Create a new identity hash set and populate it with the old selected
         * items. Then remove any items that are to be deselected, and add any
         * items that are to be selected. Finally, set the tree selection to
         * match the new set.
         */
        Set<TreeItem> newSelectedTreeItems = Sets.newIdentityHashSet();
        newSelectedTreeItems.addAll(Sets.newHashSet(tree.getSelection()));
        newSelectedTreeItems = Sets.union(
                Sets.difference(newSelectedTreeItems, deselectedTreeItems),
                selectedTreeItems);
        tree.setSelection(newSelectedTreeItems
                .toArray(new TreeItem[newSelectedTreeItems.size()]));
        selectedItems = tree.getSelection();
    }

    /**
     * Remove the specified deselected tree items, if any, from the selection
     * set.
     * <p>
     * <strong>Note</strong>: The specified set must be an identity hash set.
     * </p>
     * 
     * @param deselectedTreeItems
     *            Newly deselected tree items; must be an identity hash set
     *            created using {@link Sets#newIdentityHashSet()}
     */
    private void removeItemsFromSelection(Set<TreeItem> deselectedTreeItems) {

        /*
         * Ensure at least one item is to be deselected before doing anything.
         */
        if (deselectedTreeItems.isEmpty()) {
            return;
        }

        /*
         * Create a new identity hash set and populate it with the old selected
         * items. Then remove any items that are to be deselected, and set the
         * tree selection to match the resulting set.
         */
        Set<TreeItem> newSelectedTreeItems = Sets.newIdentityHashSet();
        newSelectedTreeItems.addAll(Sets.newHashSet(tree.getSelection()));
        newSelectedTreeItems = Sets.difference(newSelectedTreeItems,
                deselectedTreeItems);
        tree.setSelection(newSelectedTreeItems
                .toArray(new TreeItem[newSelectedTreeItems.size()]));
        selectedItems = tree.getSelection();
    }

    /**
     * Get the identifiers of entities for which the corresponding tree items
     * are currently visible in the tree, that is, they are not hidden by a
     * collapsed parent, and the viewport of the tree is scrolled so that they
     * can be seen.
     * 
     * @return Identifiers of entities for which the corresponding tree items
     *         are visible.
     */
    private Set<Pair<String, Integer>> getEntityIdentifiersWithVisibleItems() {
        Set<Pair<String, Integer>> entityIdentifiers = new HashSet<>();
        Point size = tree.getSize();
        Rectangle bounds = new Rectangle(0, 0, size.x, size.y
                - tree.getHeaderHeight());
        for (Map.Entry<TreeItem, TabularEntity> entry : entitiesForTreeItems
                .entrySet()) {
            if (entry.getKey().getBounds().intersects(bounds)) {
                entityIdentifiers.add(new Pair<>(entry.getValue()
                        .getIdentifier(), entry.getValue().getHistoryIndex()));
            }
        }
        return entityIdentifiers;
    }

    /**
     * Prune the specified previously visible entity identifiers by removing any
     * identifiers corresponding to entities that are either no longer
     * represented in the tree, or else their corresponding tree items are
     * hidden by collapsed parents.
     * 
     * @param previouslyVisibleEntityIdentifiers
     *            Identifiers of entities that were previously visible and are
     *            to be pruned.
     * @return Specified entity identifiers set, pruned of any that are no
     *         longer possibly visible.
     */
    private Set<Pair<String, Integer>> getVisibleEntityIdentifiers(
            Set<Pair<String, Integer>> previouslyVisibleEntityIdentifiers) {
        if (previouslyVisibleEntityIdentifiers.isEmpty() == false) {
            Set<Pair<String, Integer>> entityIdentifiers = new HashSet<>();
            for (TreeItem item : tree.getItems()) {
                TabularEntity entity = entitiesForTreeItems.get(item);
                entityIdentifiers.add(new Pair<String, Integer>(entity
                        .getIdentifier(), null));
                if (item.getExpanded() && (item.getItemCount() > 0)) {
                    for (TreeItem childItem : item.getItems()) {
                        entity = entitiesForTreeItems.get(childItem);
                        entityIdentifiers.add(new Pair<>(
                                entity.getIdentifier(), entity
                                        .getHistoryIndex()));
                    }
                }
            }
            return new HashSet<>(Sets.intersection(
                    previouslyVisibleEntityIdentifiers, entityIdentifiers));
        }
        return previouslyVisibleEntityIdentifiers;
    }

    /**
     * Prune the specified previously visible entity identifiers by removing any
     * identifiers corresponding to entities that are either no longer
     * represented in the tree, or else their corresponding tree items are
     * hidden by collapsed parents, or their corresponding tree items are not
     * selected.
     * 
     * @param previouslyVisibleEntityIdentifiers
     *            Identifiers of entities that were previously visible and are
     *            to be pruned.
     * @return Specified entity identifiers set, pruned of any that are no
     *         longer possibly visible or that are not selected.
     */
    private Set<Pair<String, Integer>> getSelectedVisibleEntityIdentifiers(
            Set<Pair<String, Integer>> previouslyVisibleEntityIdentifiers) {
        if (previouslyVisibleEntityIdentifiers.isEmpty() == false) {
            Set<Pair<String, Integer>> entityIdentifiers = new HashSet<>();
            for (TreeItem item : tree.getItems()) {
                TabularEntity entity = entitiesForTreeItems.get(item);
                if (entity.isSelected()) {
                    entityIdentifiers.add(new Pair<String, Integer>(entity
                            .getIdentifier(), null));
                }
                if (item.getExpanded() && (item.getItemCount() > 0)) {
                    for (TreeItem childItem : item.getItems()) {
                        entity = entitiesForTreeItems.get(childItem);
                        if (entity.isSelected()) {
                            entityIdentifiers
                                    .add(new Pair<>(entity.getIdentifier(),
                                            entity.getHistoryIndex()));
                        }
                    }
                }
            }
            return new HashSet<>(Sets.intersection(
                    previouslyVisibleEntityIdentifiers, entityIdentifiers));
        }
        return previouslyVisibleEntityIdentifiers;
    }

    /**
     * Show the appropriate tree item.
     * 
     * @param potentiallyVisibleEntityIdentifiers
     *            Identifiers of entities for which the corresponding tree items
     *            are found in the tree, and which are potentially visible (that
     *            is, their parents, if any, are not collapsed).
     */
    private void showAppropriateTreeItem(
            Set<Pair<String, Integer>> potentiallyVisibleEntityIdentifiers) {

        /*
         * Find the item to be made visible. If there are no potentially visible
         * entity identifiers in the provided set, just make the first selected
         * item, if any, the one to be made visible; otherwise, find the
         * middlemost item in the potentially visible set.
         */
        TreeItem itemToBeVisible = null;
        if (potentiallyVisibleEntityIdentifiers.isEmpty()) {
            if (selectedItems != null) {
                itemToBeVisible = selectedItems[0];
            }
        } else {
            itemToBeVisible = getMiddlemostItemFromSetFoundInList(
                    tree.getItems(), potentiallyVisibleEntityIdentifiers,
                    potentiallyVisibleEntityIdentifiers.size() / 2);
        }

        /*
         * If an item was found to be made visible, ensure it shows.
         */
        if (itemToBeVisible != null) {
            final TreeItem item = itemToBeVisible;
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    tree.showItem(item);
                }
            });
        }
    }

    /**
     * Given the specified array of tree items and the specified set of items to
     * look for in the array, get the middlemost item, that is, the one which
     * when found in the array and removed from the set leaves the set's
     * remaining item count to be that specified.
     * <p>
     * The point of this is to iterate through an array of items and find the
     * middlemost item of the set of items to be searched for.
     * </p>
     * 
     * @param items
     *            Array of tree items associated with the entities in which to
     *            look for the identifiers from the set.
     * @param entityIdentifiers
     *            Set of entity identifiers to look for. This set is modified
     *            during the course of the method's execution by removing any
     *            entity identifiers associated with items found in the array.
     * @param sizeOfSetWhenHalfRemoved
     *            Size that the set of entity identifiers will be when half of
     *            the original identifiers found within the set have been
     *            removed.
     * @return Item that is associated with the entity whose identifier is
     *         removed from the set, rendering the set the target size, or
     *         <code>null</code> if no such item is found.
     */
    private TreeItem getMiddlemostItemFromSetFoundInList(TreeItem[] items,
            Set<Pair<String, Integer>> entityIdentifiers,
            int sizeOfSetWhenHalfRemoved) {
        for (TreeItem item : items) {
            TabularEntity entity = entitiesForTreeItems.get(item);
            if (entityIdentifiers.remove(new Pair<>(entity.getIdentifier(),
                    entity.getHistoryIndex()))) {
                if (entityIdentifiers.size() == sizeOfSetWhenHalfRemoved) {
                    return item;
                }
                if (item.getExpanded() && (item.getItemCount() > 0)) {
                    TreeItem middlemostItem = getMiddlemostItemFromSetFoundInList(
                            item.getItems(), entityIdentifiers,
                            sizeOfSetWhenHalfRemoved);
                    if (middlemostItem != null) {
                        return middlemostItem;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Ensure that the specified entity's associated tree item is visible if the
     * entity is selected.
     * 
     * @param entity
     *            Entity for which to make the associated tree item visible if
     *            selected.
     */
    private void ensureTreeItemVisibilityIfSelected(TabularEntity entity) {
        if (entity.isSelected()) {
            tree.showItem(treeItemsForEntities.get(entity));
        }
    }

    /**
     * Handle the user attempting to change the checked state of the specified
     * tree item.
     * 
     * @param item
     *            Item that has had its checked state changed by the user.
     * @return <code>true</code> if the checked state change has been accepted,
     *         <code>false</code> otherwise.
     */
    private boolean handleUserChangeOfCheckedState(TreeItem item) {

        /*
         * Only accept the change if the item is a root item.
         */
        boolean isChecked = item.getChecked();
        if (isChecked && (item.getParentItem() != null)) {
            item.setChecked(false);
            return false;
        } else {

            /*
             * Create a new entity that replaces the old one, with the new one
             * being checked or unchecked as per the event that just occurred.
             */
            TabularEntity entity = entitiesForTreeItems.get(item);
            entity = handleUserChangeOfEntity(entity, entity.getTimeRange(),
                    entity.isEndTimeUntilFurtherNotice(), entity.isSelected(),
                    isChecked, entity.getChildren(), item);

            /*
             * Let the change handler know about the modification.
             */
            if (treeContentsChangeHandler != null) {
                treeContentsChangeHandler.listElementChanged(null, entity);
            }
            return true;
        }
    }

    /**
     * Handle the user changing the selection state of one or more tree items.
     */
    private void handleUserChangeOfSelectedState() {

        /*
         * Compare the previously selected items with the latest selected items;
         * if they are not the same set, handle the changes.
         */
        Set<TreeItem> oldSelectedItems = Sets.newIdentityHashSet();
        if (selectedItems != null) {
            oldSelectedItems.addAll(Lists.newArrayList(selectedItems));
        }
        selectedItems = tree.getSelection();
        Set<TreeItem> newSelectedItems = Sets.newIdentityHashSet();
        if (selectedItems != null) {
            newSelectedItems.addAll(Lists.newArrayList(selectedItems));
        }
        if (oldSelectedItems.equals(newSelectedItems) == false) {

            /*
             * Divide all the items that changed selection state into parents
             * and children.
             */
            Set<TreeItem> changedItems = Sets.symmetricDifference(
                    oldSelectedItems, newSelectedItems);
            Set<TreeItem> changedParentItems = Sets.newIdentityHashSet();
            Set<TreeItem> changedChildItems = Sets.newIdentityHashSet();
            for (TreeItem item : changedItems) {
                (item.getParentItem() == null ? changedParentItems
                        : changedChildItems).add(item);
            }

            /*
             * Create new versions of any child entities, and place any that
             * share the same parent in the same set of changed children. Also
             * record the parent item of any such children, so that any children
             * that have been selected or deselected and that have parents whose
             * selection state did not change will have their parent entities
             * changed regardless, since child entities are not changed
             * directly; their parents are regenerated instead.
             */
            Map<String, Map<Integer, TabularEntity>> changedChildrenForIndicesForRootIdentifiers = new HashMap<>(
                    changedItems.size(), 1.0f);
            Set<TreeItem> parentItemsWithChangedChildren = Sets
                    .newIdentityHashSet();
            for (TreeItem item : changedChildItems) {

                /*
                 * Note this item's parent as having at least one child that has
                 * changed.
                 */
                parentItemsWithChangedChildren.add(item.getParentItem());

                /*
                 * Create a new entity to replace the existing one, with the new
                 * selection state.
                 */
                TabularEntity entity = entitiesForTreeItems.get(item);
                entity = handleUserChangeOfEntity(entity,
                        entity.getTimeRange(),
                        entity.isEndTimeUntilFurtherNotice(),
                        newSelectedItems.contains(item), entity.isChecked(),
                        entity.getChildren(), item);

                /*
                 * Get the map of historical indices to entities for this
                 * entity's parent, and place an entry in the map for this child
                 * entity and its historical index.
                 */
                Map<Integer, TabularEntity> changedChildrenForIndices = changedChildrenForIndicesForRootIdentifiers
                        .get(entity.getIdentifier());
                if (changedChildrenForIndices == null) {
                    changedChildrenForIndices = new HashMap<>();
                    changedChildrenForIndicesForRootIdentifiers.put(
                            entity.getIdentifier(), changedChildrenForIndices);
                }
                changedChildrenForIndices.put(entity.getHistoryIndex(), entity);
            }

            /*
             * Create new versions of any parent entities that either
             * experienced a selection change, or that have children that have
             * experienced a selection change, or both. For each such parent
             * entity, use any new versions of its children as created above.
             */
            Set<TabularEntity> changedEntities = new HashSet<>(
                    changedParentItems.size()
                            + parentItemsWithChangedChildren.size(), 1.0f);
            for (TreeItem item : Sets.union(changedParentItems,
                    parentItemsWithChangedChildren)) {

                /*
                 * Merge any new children into the list of children taken from
                 * the old entity.
                 */
                TabularEntity entity = entitiesForTreeItems.get(item);
                List<TabularEntity> oldChildren = entity.getChildren();
                List<TabularEntity> children = null;
                Map<Integer, TabularEntity> changedChildrenForIndices = changedChildrenForIndicesForRootIdentifiers
                        .remove(entity.getIdentifier());
                if (changedChildrenForIndices != null) {
                    children = new ArrayList<>(oldChildren.size());
                    for (TabularEntity childEntity : oldChildren) {
                        TabularEntity newChildEntity = changedChildrenForIndices
                                .remove(childEntity.getHistoryIndex());
                        children.add(newChildEntity != null ? newChildEntity
                                : childEntity);
                    }
                } else {
                    children = oldChildren;
                }

                /*
                 * Create the new version of the entity.
                 */
                changedEntities.add(handleUserChangeOfEntity(
                        entity,
                        entity.getTimeRange(),
                        entity.isEndTimeUntilFurtherNotice(),
                        (changedParentItems.contains(item) ? newSelectedItems
                                .contains(item) : entity.isSelected()), entity
                                .isChecked(), children, item));
            }

            /*
             * Let the change handler know about the selection changes.
             */
            if (treeContentsChangeHandler != null) {
                treeContentsChangeHandler.listElementsChanged(null,
                        changedEntities);
            }
        }
    }

    /**
     * Create a new entity to take the place of the specified entity, with the
     * specified properties, and update all records to use the new entity
     * instead of the old one.
     * 
     * @param entity
     *            Entity upon which to base the new entity for most property
     *            values.
     * @param timeRange
     *            Time range for the new entity.
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the new entity's end time is
     *            "until further notice".
     * @param selected
     *            Flag indicating whether or not the new entity is selected.
     * @param checked
     *            Flag indicating whether or not the new entity is checked.
     * @param item
     *            Tree item that goes with the old entity and will need to be
     *            paired with the new entity.
     * @return New entity, which has replaced the old entity.
     */
    private TabularEntity handleUserChangeOfEntity(TabularEntity entity,
            Range<Long> timeRange, boolean endTimeUntilFurtherNotice,
            boolean selected, boolean checked, List<TabularEntity> children,
            TreeItem item) {

        /*
         * If the time range is changing, then add the new time range boundaries
         * to the added attributes map.
         */
        Map<String, Serializable> addedAttributes = null;
        if (entity.getTimeRange().equals(timeRange) == false) {
            addedAttributes = new HashMap<>(2, 1.0f);
            addedAttributes.put(HazardConstants.HAZARD_EVENT_START_TIME,
                    timeRange.lowerEndpoint());
            addedAttributes.put(HazardConstants.HAZARD_EVENT_END_TIME,
                    timeRange.upperEndpoint());
        }

        /*
         * Create the new entity.
         */
        TabularEntity oldEntity = entity;
        entity = TabularEntity.build(entity, timeRange,
                endTimeUntilFurtherNotice, selected, checked, addedAttributes,
                children);

        /*
         * If the entity is a root, replace it in the list of root entities, and
         * associate it with its identifier.
         */
        if (entity.getHistoryIndex() == null) {
            tabularEntities.set(
                    indicesForRootIdentifiers.get(entity.getIdentifier()),
                    entity);
            tabularEntitiesForRootIdentifiers.put(entity.getIdentifier(),
                    entity);
        }

        /*
         * Associate the new entity with its tree item.
         */
        treeItemsForEntities.put(entity, item);
        entitiesForTreeItems.put(item, entity);

        /*
         * Associate the new entity with its tree editor.
         */
        TreeEditor editor = treeEditorsForEntities.remove(oldEntity);
        treeEditorsForEntities.put(entity, editor);

        return entity;
    }

    /**
     * Schedule the ensuring of checkboxes in the tree's rows being in the
     * farthest left column.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not tree redraw is currently
     *            enabled. If it is, then if the scheduled method method needs
     *            to disable it for some reason, it will turn it back on before
     *            returning.
     */
    private void scheduleEnsureCheckboxesAreInLeftmostColumn(
            final boolean redrawEnabled) {
        Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
                ensureCheckboxesAreInLeftmostColumn(redrawEnabled);
            }
        });
    }

    /**
     * Ensure that the checkboxes in the tree's rows are in the farthest left
     * column.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not tree redraw is currently
     *            enabled. If it is, then if this method needs to disable it for
     *            some reason, it will turn it back on before returning.
     */
    private void ensureCheckboxesAreInLeftmostColumn(boolean redrawEnabled) {
        if (tree.isDisposed()) {
            return;
        }

        /*
         * If the first column is not the first actual column, delete it and
         * recreate it in order to ensure that it has the checkboxes in it.
         */
        int[] columnOrder = tree.getColumnOrder();
        if (columnOrder[0] != 0) {

            /*
             * If redraw is enabled, disable it while making the column changes.
             */
            if (redrawEnabled) {
                tree.setRedraw(false);
            }

            /*
             * Delete the old column.
             */
            TreeColumn column = tree.getColumn(columnOrder[0]);
            String columnName = column.getText();
            column.dispose();

            /*
             * Recreate the column and place it at the left side of the tree.
             */
            createTreeColumn(columnName, 0);

            /*
             * Fill in the text for the cells in the tree's rows that fall
             * within the newly created column.
             */
            updateCellsForColumn(columnName);

            /*
             * Ensure that the primary sort column, if any, has the appropriate
             * visual cues.
             */
            updateColumnSortVisualCues();

            /*
             * If redraw was enabled, re-enable it.
             */
            if (redrawEnabled) {
                tree.setRedraw(true);
            }
        }
    }

    /**
     * Creates the column with the specified name.
     * 
     * @param name
     *            Column name.
     * @param index
     *            Index at which to place the column, or <code>-1</code> if the
     *            column should be appended to the tree.
     */
    private void createTreeColumn(String name, int index) {

        /*
         * Get the column definition dictionary corresponding to this name.
         */
        ColumnDefinition columnDefinition = columnDefinitionsForNames.get(name);
        if (columnDefinition == null) {
            statusHandler.error("Problem: no column definition for \"" + name
                    + "\".");
            return;
        }

        /*
         * Create the column.
         */
        if (index == -1) {
            index = tree.getColumnCount();
        }
        TreeColumn column = new TreeColumn(tree, (columnDefinition.getType()
                .equals(SETTING_COLUMN_TYPE_NUMBER) ? SWT.RIGHT : SWT.NONE),
                index);
        column.setText(name);
        if (spacerImage != null) {
            column.setImage(spacerImage);
        }
        column.setMoveable(true);
        column.setResizable(true);
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_COUNTDOWN)) {
            countdownTimerColumnIndex = index;
            countdownTimerColumnName = name;
            updateCountdownTimers();
        }

        /*
         * Ensure the visual cues for the primary sort column, if any, are in
         * place.
         */
        updateColumnSortVisualCues();

        /*
         * Pack the column, and only after packing set its width, if one is
         * specified in the definition. Width setting must occur after packing
         * so as to avoid having the pack operation change the width to
         * something other than that provided by the definition.
         */
        column.pack();
        Number width = columnDefinition.getWidth();
        if (width != null) {
            column.setWidth(width.intValue());
        }

        /*
         * Add the sort listener to this column, to detect the user clicking on
         * the column header and to respond by sorting.
         */
        column.addSelectionListener(sortListener);

        /*
         * Add a listener for column-reordering and -resizing events.
         */
        column.addControlListener(columnControlListener);
    }

    /**
     * Determine the special properties of the column associated with the
     * specified name, adding records to maps for any such properties.
     * 
     * @param columnName
     *            Column name.
     */
    private void determineSpecialPropertiesOfColumn(String columnName) {
        ColumnDefinition columnDefinition = columnDefinitionsForNames
                .get(columnName);
        if (columnDefinition == null) {
            return;
        }
        String hintTextIdentifier = columnDefinition.getHintTextIdentifier();
        if (hintTextIdentifier != null) {
            hintTextIdentifiersForVisibleColumnNames.put(columnName,
                    hintTextIdentifier);
        }
        String columnType = columnDefinition.getType();
        if (columnType.equals(SETTING_COLUMN_TYPE_DATE)) {
            dateIdentifiersForVisibleColumnNames.put(columnName,
                    columnDefinition.getIdentifier());
        }
    }

    /**
     * Handle the resizing of the specified column.
     * 
     * @param column
     *            Column that has been resized.
     */
    private void handleColumnResized(TreeColumn column) {

        /*
         * Do nothing if resizes are to be ignored.
         */
        if (ignoreResize == false) {
            ignoreResize = true;

            /*
             * Handle the resize one way if this is the time scale column, and
             * another way otherwise.
             */
            if (column.getText().equals(TIME_SCALE_COLUMN_NAME)) {

                /*
                 * If the ruler is displaying, resize it appropriately.
                 */
                boolean rescheduled = false;
                if ((ruler != null) && (ruler.isDisposed() == false)
                        && ruler.isVisible()) {

                    /*
                     * Get the tree bounds for calculations to be made as to the
                     * size and position of the timeline ruler. Since the width
                     * is sometimes given as 0, schedule another resize event to
                     * fire off later in this case. If this is not done, the
                     * timeline ruler is given the wrong bounds.
                     */
                    Rectangle treeBounds = tree.getBounds();
                    if (treeBounds.width == 0) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                visibleColumnsChanged(false);
                            }
                        });
                        rescheduled = true;
                    } else {
                        fitRulerToColumn(column);
                    }
                }

                /*
                 * If the second to last column is being resized, then the last
                 * time scale column width will have been recorded; if this is
                 * the case, then schedule a proportional resize of the other
                 * columns to occur after this event is processed, so that the
                 * delta representing the change in the time scale column's
                 * width may be shared out to the other columns.
                 */
                if ((rescheduled == false)
                        && (timeScaleColumnWidthBeforeResize != -1)) {
                    Display.getCurrent().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            if (timeScaleColumnWidthBeforeResize != -1) {
                                resizeColumnsProportionally();
                                timeScaleColumnWidthBeforeResize = -1;
                            }
                        }
                    });
                }
            } else {

                /*
                 * If the second-to-last column is being resized, then record
                 * the time scale column's current width, so that its width
                 * change may be calculated subsequently, and the delta shared
                 * out among the other columns. Otherwise, do not record the
                 * time scale column's width, and update the column definitions
                 * to include the new column widths.
                 */
                if ((visibleColumnNames.size() > 0)
                        && (visibleColumnNames
                                .get(visibleColumnNames.size() - 1)
                                .equals(column.getText()))) {
                    if (timeScaleColumnWidthBeforeResize == -1) {
                        for (TreeColumn otherColumn : tree.getColumns()) {
                            if (otherColumn.getText().equals(
                                    TIME_SCALE_COLUMN_NAME)) {
                                timeScaleColumnWidthBeforeResize = otherColumn
                                        .getWidth();
                                break;
                            }
                        }
                    }
                } else {
                    timeScaleColumnWidthBeforeResize = -1;
                    updateTreeColumnWidth(column);
                }
            }
            ignoreResize = false;
        }
    }

    /**
     * Handle the moving of at least one column in the column order.
     */
    private void handleColumnMoved() {

        /*
         * Only react if this move should not be ignored.
         */
        if (ignoreMove == false) {

            /*
             * Ensure that if a columnn was dragged beyond the last column (the
             * one holding the time scales), a runnable is scheduled to be
             * executed later in order to reposition the dragged column before
             * the latter, so as to always leave the time scales in the last
             * column. Display.asyncExec(), when executed from within the UI
             * thread, places the runnable on the queue to be executed when all
             * outstanding requests have been handled by SWT. This is important
             * because attempting to call Tree.setColumnOrder() from within this
             * handler (i.e. changing the column order while responding to a
             * change in the column order) appears to have catastrophic results,
             * including occasional GTK crashes that bring down the JVM! If the
             * columns are not in the wrong order, handle the new ordering.
             */
            int[] columnOrder = tree.getColumnOrder();
            if (columnOrder[columnOrder.length - 1] != columnOrder.length - 1) {
                tree.setRedraw(false);
                Display.getCurrent().asyncExec(
                        ensureTimeScaleIsLastColumnAction);
            } else {
                handleColumnReorderingViaDrag(true);
            }
        }
    }

    /**
     * Handle the reordering of the tree columns via a drag performed by the
     * user.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not tree redraw is currently
     *            enabled. If it is, then if this method needs to disable it for
     *            some reason, it will turn it back on before returning.
     */
    private void handleColumnReorderingViaDrag(boolean redrawEnabled) {

        /*
         * Ensure that the checkboxes in the tree's rows are in the leftmost
         * column.
         */
        scheduleEnsureCheckboxesAreInLeftmostColumn(redrawEnabled);

        /*
         * This is needed for the special case where nothing is selected and the
         * user drags a column. Without this, the topmost tree row is selected.
         */
        if ((selectedItems == null) || (selectedItems.length == 0)) {
            tree.deselectAll();
        }

        /*
         * Get the index of the countdown timer column, if it is showing.
         */
        countdownTimerColumnIndex = getIndexOfColumnInTree(countdownTimerColumnName);

        /*
         * Update the order of the columns in the dictionaries.
         */
        updateTreeColumnOrder();
    }

    /**
     * Handle the addition or removal of the specified column.
     * 
     * @param columnName
     *            Name of the column to be added if it is not found within the
     *            visible column names list, or removed if it is found within
     *            said list.
     */
    private void handleColumnAdditionOrRemoval(String columnName) {

        /*
         * Prepare for the addition or removal of a column.
         */
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;
        visibleColumnCountChanging();

        /*
         * Determine which column is to be toggled on or off, and whether it is
         * to be added or removed, and then perform the addition or removal.
         */
        TreeColumn[] treeColumns = tree.getColumns();
        if (visibleColumnNames.contains(columnName)) {

            /*
             * Remove the column from the tree, and remove its name from the
             * visible columns list, as well as the hint text columns set if it
             * is there.
             */
            for (int j = 0; j < treeColumns.length; ++j) {
                if (treeColumns[j].getText().equals(columnName)) {
                    treeColumns[j].dispose();
                    break;
                }
            }
            visibleColumnNames.remove(columnName);
            hintTextIdentifiersForVisibleColumnNames.remove(columnName);
            dateIdentifiersForVisibleColumnNames.remove(columnName);
            if (columnName.equals(countdownTimerColumnName)) {
                countdownTimerColumnIndex = -1;
            }

            /*
             * Ensure that the checkboxes in the tree's rows are in the leftmost
             * column.
             */
            scheduleEnsureCheckboxesAreInLeftmostColumn(false);
        } else {

            /*
             * Create the column and place it at the end, just before the time
             * scale column; then add its name to the visible columns list.
             */
            createTreeColumn(columnName, tree.getColumnCount() - 1);
            visibleColumnNames.add(columnName);
            determineSpecialPropertiesOfColumn(columnName);

            /*
             * Fill in the text for the cells in the tree's rows that fall
             * within the newly created column.
             */
            updateCellsForColumn(columnName);
        }

        /*
         * Move the tree editors over to the appropriate column.
         */
        for (Map.Entry<TabularEntity, TreeItem> entry : treeItemsForEntities
                .entrySet()) {
            TreeEditor editor = treeEditorsForEntities.get(entry.getKey());
            if (editor != null) {
                editor.setEditor(editor.getEditor(), entry.getValue(),
                        tree.getColumnCount() - 1);
            }
        }

        /*
         * Update the column order to match that given by the visible columns
         * list.
         */
        updateColumnOrder();

        /*
         * Ensure the primary sort column, if any, has the appropriate visual
         * cues.
         */
        updateColumnSortVisualCues();

        /*
         * Finish up following the addition or removal of a column.
         */
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        visibleColumnsChanged(true);

        /*
         * Notify listeners of the column change.
         */
        scheduleNotificationOfColumnChange();
    }

    /**
     * Handle the specified column being chosen as the primary sort column.
     * 
     * @param sortColumn
     *            Column chosen to be the primary sort column.
     */
    private void handleColumnChosenForPrimarySort(TreeColumn sortColumn) {

        /*
         * Deterrmine which column is to be the new primary sort column; if it
         * has changed, notify the tree.
         */
        String sortName = sortColumn.getText();
        if (sortColumn != tree.getSortColumn()) {
            tree.setSortColumn(sortColumn);
        }

        /*
         * If the sort column is the same as before, invert its sort direction;
         * otherwise, use the ascending direction.
         */
        SortDirection primarySortDirection = ((sortColumn == tree
                .getSortColumn()) && (tree.getSortDirection() == SWT.UP) ? SortDirection.DESCENDING
                : SortDirection.ASCENDING);
        tree.setSortDirection(primarySortDirection == SortDirection.ASCENDING ? SWT.UP
                : SWT.DOWN);

        /*
         * Notify the handler of the new sort.
         */
        if (sortInvocationHandler != null) {
            sortInvocationHandler.commandInvoked(new Sort(
                    columnNamesForIdentifiers.inverse().get(sortName),
                    primarySortDirection, 1));
        }
    }

    /**
     * Handle the choice of the new sort column.
     * 
     * @param sortName
     *            Name of the column to be used for sorting.
     * @param sortDirection
     *            Direction of the sort within the column.
     * @param sortPriority
     *            Priority of the column for sorting.
     */
    private void handleColumnChosenForSort(String sortName,
            SortDirection sortDirection, int sortPriority) {

        /*
         * If the tree needs its visual cues related to sorting altered, alter
         * them now. This should only happen if primary sort is changing, or if
         * there was previously no sort column.
         */
        TreeColumn oldSortColumn = tree.getSortColumn();
        if ((sortPriority == 1) || (oldSortColumn == null)) {
            if ((oldSortColumn == null)
                    || (oldSortColumn.getText().equals(sortName) == false)) {
                for (TreeColumn column : tree.getColumns()) {
                    if (sortName.equals(column.getText())) {
                        tree.setSortColumn(column);
                        break;
                    }
                }
            }
            tree.setSortDirection(sortDirection == SortDirection.ASCENDING ? SWT.UP
                    : SWT.DOWN);
        }

        /*
         * Notify the handler of the new sort.
         */
        if (sortInvocationHandler != null) {
            sortInvocationHandler.commandInvoked(new Sort(
                    columnNamesForIdentifiers.inverse().get(sortName),
                    sortDirection, sortPriority));
        }
    }

    /**
     * Handle a row menu's until further notice checkbox having been toggled.
     */
    private void handleUserUntilFurtherNoticeToggle() {

        /*
         * Determine whether until further notice has been toggled on or off.
         * Since the menu item is not actually a checkbox, but rather a push
         * button masquerading as a tri-state checkbox, use the icon it was
         * displaying to determine whether the new toggle state is on or off.
         */
        boolean newUntilFurtherNotice = (untilFurtherNoticeMenuItem.getImage() == uncheckedMenuItemImage);

        /*
         * Iterate through the selected entities, ensuring that each in turn has
         * the correct until further notice state.
         */
        Set<TabularEntity> changedEntities = new HashSet<>(
                selectedItems.length, 1.0f);
        for (TreeItem item : selectedItems) {

            /*
             * Do nothing unless the old until further notice state of this
             * entity not the same as the new state.
             */
            TabularEntity entity = entitiesForTreeItems.get(item);
            boolean oldUntilFurtherNotice = entity
                    .isEndTimeUntilFurtherNotice();
            if (oldUntilFurtherNotice != newUntilFurtherNotice) {

                /*
                 * Create a new entity that replaces the old one.
                 */
                entity = handleUserChangeOfEntity(entity,
                        entity.getTimeRange(), newUntilFurtherNotice,
                        entity.isSelected(), entity.isChecked(),
                        entity.getChildren(), item);
                changedEntities.add(entity);

                /*
                 * Set the end time thumb in the time scale widget to be
                 * read-only if appropriate.
                 */
                MultiValueScale scale = (MultiValueScale) treeEditorsForEntities
                        .get(entity).getEditor();
                configureScaleIntervalLockingForEntity(scale, entity);
            }

            /*
             * If any entities have changed, let the change handler know.
             */
            if ((treeContentsChangeHandler != null)
                    && (changedEntities.isEmpty() == false)) {
                treeContentsChangeHandler.listElementsChanged(null,
                        changedEntities);
            }
        }
    }

    /**
     * Resize all non-time-scale columns proportionally to in response to the
     * time scale column being resized.
     */
    private void resizeColumnsProportionally() {
        if (tree.isDisposed()) {
            return;
        }

        /*
         * Define a class used to pair column indices and the widths of said
         * columns, and which is sortable by the widths (with largest widths
         * sorting to the top).
         */
        class ColumnIndexAndWidth implements Comparable<ColumnIndexAndWidth> {
            public int index;

            public int width;

            public ColumnIndexAndWidth(int index, int width) {
                this.index = index;
                this.width = width;
            }

            @Override
            public int compareTo(ColumnIndexAndWidth o) {
                return o.width - width;
            }

        }

        /*
         * Turn off redrawing and set the ignore resizing and move flags to
         * avoid bad recursive behavior (since this method call is triggered by
         * certain resize events).
         */
        tree.setRedraw(false);
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;

        /*
         * Get the columns, and find the current width of the time scale column
         * in order to calculate the delta between the old column width and the
         * new one.
         */
        TreeColumn[] columns = tree.getColumns();
        int delta = 0;
        for (TreeColumn column : columns) {
            if (column.getText().equals(TIME_SCALE_COLUMN_NAME)) {
                delta = timeScaleColumnWidthBeforeResize - column.getWidth();
                break;
            }
        }

        /*
         * Create a list of the non-time-scale columns and their widths, and
         * sort it so that the largest columns are at the top of the list; this
         * allows the proportional resizing to be applied in order from largest
         * to smallest columns, ensuring a good distribution of the delta.
         */
        int totalWidth = 0;
        List<ColumnIndexAndWidth> columnIndicesAndWidths = new ArrayList<>();
        for (int j = 0; j < columns.length; j++) {

            /*
             * Only add a record for this column if it is not the time scale
             * column.
             */
            if (!columns[j].getText().equals(TIME_SCALE_COLUMN_NAME)) {

                /*
                 * Get the column's width, subtracting the delta from it if the
                 * column is the last one in the tree before the time scale
                 * column (since this means that the resize of the time scale
                 * column has already added the delta to this column's size).
                 */
                int width = columns[j].getWidth();
                if (columns[j].getText().equals(
                        visibleColumnNames.get(visibleColumnNames.size() - 1))) {
                    width -= delta;
                }

                /*
                 * Add the width to the total width, and add a record of the
                 * column index and width to the list.
                 */
                totalWidth += width;
                columnIndicesAndWidths.add(new ColumnIndexAndWidth(j, width));
            }
        }
        Collections.sort(columnIndicesAndWidths);

        /*
         * As long as there is delta remaining to be applied, iterate through
         * all the columns, applying a proportional amount of the delta to each.
         * Stop when there is no more delta remaining, or when a pass through
         * the loop changes none of the widths.
         */
        int deltaRemaining = Math.abs(delta);
        int lastDeltaRemaining;
        do {

            /*
             * Remember the current delta remaining so that it may be compared
             * with its new value to determine if it has not changed in this
             * pass through the loop.
             */
            lastDeltaRemaining = deltaRemaining;

            /*
             * Iterate through the columns, sorted by size in descending order,
             * and determine for each its new width, stopping when there is no
             * more delta left to apply.
             */
            for (ColumnIndexAndWidth indexAndWidth : columnIndicesAndWidths) {

                /*
                 * If there is no delta remaining to apply, stop.
                 */
                if (deltaRemaining == 0) {
                    break;
                }

                /*
                 * Get the number expressing this column's width as a fraction
                 * of the total width of all the non-time-scale columns, and
                 * calculate the width change that should be applied,
                 * subtracting it from the delta remaining so as to keep a
                 * record of how much delta is left to apply the next time
                 * through the loop.
                 */
                float proportionalWidth = ((float) indexAndWidth.width)
                        / (float) totalWidth;
                int widthChange = ((int) ((proportionalWidth * lastDeltaRemaining) + 0.5f))
                        * (delta < 0 ? -1 : 1);
                if (Math.abs(widthChange) > deltaRemaining) {
                    widthChange = deltaRemaining * (widthChange < 0 ? -1 : 1);
                }
                deltaRemaining -= Math.abs(widthChange);

                /*
                 * Remember the new width for this column.
                 */
                indexAndWidth.width += widthChange;
            }
        } while ((deltaRemaining > 0) && (lastDeltaRemaining != deltaRemaining));

        /*
         * If there is still delta remaining, just apply it to the largest
         * column.
         */
        if ((deltaRemaining > 0) && (columnIndicesAndWidths.size() > 0)) {
            columnIndicesAndWidths.get(0).width += deltaRemaining
                    * (delta < 0 ? -1 : 1);
        }

        /*
         * For each column, change its width if it has had a portion of the
         * delta applied to it.
         */
        for (ColumnIndexAndWidth indexAndWidth : columnIndicesAndWidths) {
            if (columns[indexAndWidth.index].getWidth() != indexAndWidth.width) {
                columns[indexAndWidth.index].setWidth(indexAndWidth.width);
            }
        }

        /*
         * Reset the ignore resizing and moving flags and turn back on tree
         * redrawing, and then update to include the new column widths.
         */
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        tree.setRedraw(true);
        updateAllTreeColumnWidths();
    }

    /**
     * Update the order of the tree columns to match the order of the visible
     * column names list.
     */
    private void updateColumnOrder() {

        /*
         * Iterate through the column names in the order they should be
         * displayed, determining for each which column index in the tree
         * corresponds to that name. Once this array of indices is compiled, set
         * the column order so that the tree's columns are in the order
         * specified.
         */
        int[] columnOrder = new int[tree.getColumnCount()];
        for (int j = 0; j < columnOrder.length; j++) {
            int index = visibleColumnNames.indexOf(tree.getColumn(j).getText());
            columnOrder[index == -1 ? columnOrder.length - 1 : index] = j;
        }
        boolean lastIgnoreMove = ignoreMove;
        ignoreMove = true;
        tree.setColumnOrder(columnOrder);
        ignoreMove = lastIgnoreMove;
    }

    /**
     * Update all tree column definition dictionaries for visible columns with
     * new widths.
     */
    private void updateAllTreeColumnWidths() {

        /*
         * See if any of the widths in the tree columns are different from the
         * ones held in the column definitions; if they are, create a new
         * immutable map holding the column definitions with the new widths.
         */
        Map<String, ColumnDefinition> newColumnDefinitionsForNames = null;
        for (TreeColumn column : tree.getColumns()) {
            ColumnDefinition columnDefinition = columnDefinitionsForNames
                    .get(column.getText());
            if ((columnDefinition != null)
                    && ((columnDefinition.getWidth() == null) || (columnDefinition
                            .getWidth() != column.getWidth()))) {
                if (newColumnDefinitionsForNames == null) {
                    newColumnDefinitionsForNames = new HashMap<>(
                            columnDefinitionsForNames);
                }
                newColumnDefinitionsForNames.put(
                        column.getText(),
                        new ColumnDefinition(columnDefinition, column
                                .getWidth()));
            }
        }

        /*
         * If a change occurred, remember the new column definitions for names
         * map, and get ready to send a notification as to the change.
         */
        if (newColumnDefinitionsForNames != null) {
            columnDefinitionsForNames = ImmutableMap
                    .copyOf(newColumnDefinitionsForNames);
            scheduleNotificationOfColumnChange();
        }
    }

    /**
     * Update column definition dictionary with a new width for the specified
     * resized tree column.
     * 
     * @param column
     *            The tree column which has been resized.
     */
    private void updateTreeColumnWidth(TreeColumn column) {

        /*
         * See if the width of the specified column is different from the column
         * definition's record of the width; if it is, create a new map of
         * column names to definitions holding the old column definitions,
         * except for this column, for which a new definition is created with
         * the new width. Record the new immutable map and get ready to send out
         * a notification as to the change.
         */
        ColumnDefinition columnDefinition = columnDefinitionsForNames
                .get(column.getText());
        if ((columnDefinition != null)
                && ((columnDefinition.getWidth() == null) || (columnDefinition
                        .getWidth() != column.getWidth()))) {
            Map<String, ColumnDefinition> newColumnDefinitionsForNames = new HashMap<>(
                    columnDefinitionsForNames);
            newColumnDefinitionsForNames.put(column.getText(),
                    new ColumnDefinition(columnDefinition, column.getWidth()));
            columnDefinitionsForNames = ImmutableMap
                    .copyOf(newColumnDefinitionsForNames);
            scheduleNotificationOfColumnChange();
        }
    }

    /**
     * Update the tree column ordering.
     */
    private void updateTreeColumnOrder() {

        /*
         * Rebuild the visible column names list and the map of special column
         * properties.
         */
        visibleColumnNames.clear();
        hintTextIdentifiersForVisibleColumnNames.clear();
        dateIdentifiersForVisibleColumnNames.clear();
        for (int order : tree.getColumnOrder()) {
            String columnName = tree.getColumn(order).getText();
            if (!columnName.equals(TIME_SCALE_COLUMN_NAME)) {
                visibleColumnNames.add(columnName);
                determineSpecialPropertiesOfColumn(columnName);
            }
        }

        /*
         * Notify listeners of the column change.
         */
        scheduleNotificationOfColumnChange();
    }

    /**
     * Respond to addition(s) to and/or removals from the set of visible columns
     * being about to occur.
     */
    private void visibleColumnCountChanging() {

        /*
         * Turn off tree redraw and make the time ruler invisible.
         */
        tree.setRedraw(false);
        ruler.setVisible(false);
    }

    /**
     * Schedule the asynchronous sending of a notification out indicating a
     * change in the columns' definitions, visibility or ordering.
     */
    private void scheduleNotificationOfColumnChange() {
        if (willNotifyOfColumnChange == false) {
            willNotifyOfColumnChange = true;
            Display.getCurrent().asyncExec(new Runnable() {

                @Override
                public void run() {
                    willNotifyOfColumnChange = false;
                    notifyOfColumnChange();
                }
            });
        }
    }

    /**
     * Send a notification out indicating a change in the columns' definitions,
     * visibility or ordering.
     */
    private void notifyOfColumnChange() {
        if (columnsChangeHandler != null) {
            columnsChangeHandler.stateChanged(null, columns);
        }
    }

    /**
     * Create the time scale widget for the specified entity.
     * 
     * @param entity
     *            Entity for which to create the widget.
     * @param item
     *            Tree item representing the entity.
     * @return Created time scale widget.
     */
    private MultiValueScale createTimeScale(TabularEntity entity, TreeItem item) {

        /*
         * Create a time scale widget with two thumbs and configure it
         * appropriately. Note that the right inset of the widget is larger than
         * the left because it must match the width of the time ruler, which has
         * the form margin width subtracted from its right edge. Furthermore,
         * the left inset of the widget is reduced by the difference between the
         * cell left-side padding and the form margin width, since the time
         * ruler is only inset by the form margin width from the left of the
         * column.
         */
        MultiValueScale scale = new MultiValueScale(tree,
                HazardConstants.MIN_TIME, HazardConstants.MAX_TIME);
        scale.setSnapValueCalculator(entity.getTimeResolution() == TimeResolution.SECONDS ? WidgetUtilities
                .getTimeLineSnapValueCalculatorWithSecondsResolution()
                : WidgetUtilities
                        .getTimeLineSnapValueCalculatorWithMinutesResolution());
        scale.setTooltipTextProvider(thumbTooltipTextProvider);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        int verticalPadding = (tree.getItemHeight() - scale.computeSize(
                SWT.DEFAULT, SWT.DEFAULT).y) / 2;
        scale.setInsets(TIME_HORIZONTAL_PADDING
                - (CELL_PADDING_LEFT - timeRulerMarginWidth), verticalPadding,
                TIME_HORIZONTAL_PADDING + timeRulerMarginWidth, verticalPadding);
        scale.setMinimumDeltaBetweenConstrainedThumbs(TIME_RANGE_MINIMUM_INTERVAL);

        /*
         * Associate the tree item with the created time scale.
         */
        treeItemsForScales.put(scale, item);

        /*
         * Finish the configuration.
         */
        configureTimeScale(scale, entity);

        return scale;
    }

    /**
     * Reuse the specified time scale widget for the specified entity.
     * 
     * @param scale
     *            Time scale widget that was created to be used for another
     *            entity, but which is to be reused for this entity.
     * @param entity
     *            Entity for which to reuse the widget.
     */
    private void reuseTimeScale(MultiValueScale scale, TabularEntity entity) {

        /*
         * Reset values for the scale that might interfere with configuring it
         * to represent the new entity.
         */
        scale.setSnapValueCalculator(null);
        for (int j = 0; j < 2; j++) {
            scale.setAllowableConstrainedValueRange(j,
                    HazardConstants.MIN_TIME, HazardConstants.MAX_TIME);
        }
        scale.removeMultiValueLinearControlListener(timeScaleListener);

        /*
         * Finish the reconfiguration.
         */
        configureTimeScale(scale, entity);
    }

    /**
     * Configure the specified time scale to be used to represent the time range
     * of the specified entity.
     * 
     * @param scale
     *            Time scale widget to be configured.
     * @param entity
     *            Entity for which to configure the widget.
     */
    private void configureTimeScale(MultiValueScale scale, TabularEntity entity) {
        scale.setVisibleValueRange(ruler.getLowerVisibleValue(),
                ruler.getUpperVisibleValue());
        scale.setConstrainedThumbValues(entity.getTimeRange().lowerEndpoint(),
                entity.getTimeRange().upperEndpoint());
        scale.setConstrainedThumbRangeColor(1,
                getTimeRangeColorForEntity(entity));
        Range<Long> startTimeRange = (entity.getLowerTimeBoundaries() == null ? Range
                .closed(HazardConstants.MIN_TIME, HazardConstants.MAX_TIME)
                : entity.getLowerTimeBoundaries());
        scale.setAllowableConstrainedValueRange(0,
                startTimeRange.lowerEndpoint(), startTimeRange.upperEndpoint());
        scale.setConstrainedThumbEditable(0, (startTimeRange.lowerEndpoint()
                .equals(startTimeRange.upperEndpoint()) == false));
        if (selectedTimeMode == SelectedTimeMode.RANGE) {
            scale.setFreeMarkedValues(currentTime);
            scale.setFreeMarkedValueColor(0, currentTimeColor);
            scale.setConstrainedMarkedValues(selectedTimeStart, selectedTimeEnd);
            scale.setConstrainedMarkedValueColor(0, timeRangeEdgeColor);
            scale.setConstrainedMarkedValueColor(1, timeRangeEdgeColor);
            scale.setConstrainedMarkedRangeColor(1, timeRangeFillColor);
        } else {
            scale.setFreeMarkedValues(currentTime, selectedTimeStart);
            scale.setFreeMarkedValueColor(0, currentTimeColor);
            scale.setFreeMarkedValueColor(1, selectedTimeColor);
        }
        scale.addMultiValueLinearControlListener(timeScaleListener);
    }

    /**
     * Configure the specified scale interval's locking mode for the specified
     * entity.
     * 
     * @param scale
     *            Multi-value scale to have its interval locking mode
     *            configured.
     * @param entity
     *            Entity with which the scale is associated.
     */
    private void configureScaleIntervalLockingForEntity(MultiValueScale scale,
            TabularEntity entity) {

        /*
         * The interval between the start and end times should be locked if it
         * is marked as such by the entity, and if its end time is not currently
         * "until further notice".
         */
        boolean lock = (entity.isTimeRangeIntervalLocked() && (entity
                .isEndTimeUntilFurtherNotice() == false));
        scale.setConstrainedThumbIntervalLocked(lock);
        Range<Long> endTimeRange = (lock
                || (entity.getUpperTimeBoundaries() == null) ? Range.closed(
                HazardConstants.MIN_TIME, HazardConstants.MAX_TIME) : entity
                .getUpperTimeBoundaries());
        scale.setAllowableConstrainedValueRange(1,
                endTimeRange.lowerEndpoint(), endTimeRange.upperEndpoint());

        /*
         * If the end time can only be one value, or the interval is locked and
         * the start time can only be one value, make the end time uneditable.
         */
        scale.setConstrainedThumbEditable(
                1,
                (endTimeRange.lowerEndpoint().equals(
                        endTimeRange.upperEndpoint()) == false)
                        && ((lock == false) || (scale
                                .getMinimumAllowableConstrainedValue(0) != scale
                                .getMaximumAllowableConstrainedValue(0))));
    }

    /**
     * Create a tree editor for the specified time scale and associate it with
     * the specified entity.
     * 
     * @param scale
     *            Time scale widget for which to create the tree editor.
     * @param entity
     *            Entity represented by the tree editor to be created.
     * @param item
     *            Tree item with which to associate the new tree editor.
     */
    private void createTreeEditorForTimeScale(Control scale,
            TabularEntity entity, TreeItem item) {
        TreeEditor editor = new TreeEditor(tree);
        editor.grabHorizontal = editor.grabVertical = true;
        editor.verticalAlignment = SWT.CENTER;
        editor.minimumHeight = tree.getItemHeight();
        editor.setEditor(scale, item, tree.getColumnCount() - 1);
        treeEditorsForEntities.put(entity, editor);
    }

    /**
     * Reuse the tree editor associated with the specified old entity by
     * removing said assocation and instead associating it with the specified
     * new entity.
     * 
     * @param oldEntity
     *            Entity that was associated with the editor.
     * @param entity
     *            Entity that is to be associated with the editor.
     */
    private void reuseTreeEditorForTimeScale(TabularEntity oldEntity,
            TabularEntity entity) {
        TreeEditor editor = treeEditorsForEntities.remove(oldEntity);
        treeEditorsForEntities.put(entity, editor);
    }

    /**
     * Remove the time scale widget and editor for the specified entity.
     * 
     * @param entity
     *            Entity for which to remove the widget.
     */
    private void removeTimeScaleAndAssociatedEditor(TabularEntity entity) {
        TreeEditor editor = treeEditorsForEntities.remove(entity);
        MultiValueScale scale = (MultiValueScale) editor.getEditor();
        TreeItem treeItem = treeItemsForScales.remove(scale);
        scale.dispose();
        editor.dispose();

        /*
         * Annoyingly, it seems that any time scale widgets acting as editors
         * for the last column in rows below this row do not get moved up, and
         * furthermore that they cannot be forced to do so by being asked to
         * redraw, or having their enclosing TreeEditors lay themselves out. The
         * only way found thus far to get them to update their visuals is to
         * have each one's enclosing TreeEditor remove the time scale and then
         * re-add it.
         */
        boolean foundItem = false;
        for (TreeItem item : tree.getItems()) {
            if (treeItem == item) {
                foundItem = true;
            } else if (foundItem) {
                TabularEntity thisEntity = entitiesForTreeItems.get(item);
                if (thisEntity != null) {
                    TreeEditor thisEditor = treeEditorsForEntities
                            .get(thisEntity);
                    if (thisEditor != null) {
                        Control thisScale = thisEditor.getEditor();
                        thisEditor.setEditor(null);
                        thisEditor.setEditor(thisScale, item,
                                tree.getColumnCount() - 1);
                    }
                }
            }
        }
    }

    /**
     * Set the ruler snap value calculator as appropriate given the current time
     * resolution.
     */
    private void updateRulerSnapValueCalculator() {
        if (ruler == null) {
            return;
        }
        ruler.setSnapValueCalculator(timeResolution == TimeResolution.SECONDS ? WidgetUtilities
                .getTimeLineSnapValueCalculatorWithSecondsResolution()
                : WidgetUtilities
                        .getTimeLineSnapValueCalculatorWithMinutesResolution());
    }

    /**
     * Update the text in the cells that fall within the specified column.
     * 
     * @param columnName
     *            Name of the column for which to update the corresponding
     *            cells.
     */
    private void updateCellsForColumn(String columnName) {

        /*
         * Iterate through the rows of the tree, setting the text of the cells
         * within the new column as appropriate.
         */
        int index = getIndexOfColumnInTree(columnName);
        if (index != -1) {
            ColumnDefinition columnDefinition = columnDefinitionsForNames
                    .get(columnName);
            for (Map.Entry<TreeItem, TabularEntity> entry : entitiesForTreeItems
                    .entrySet()) {
                updateCell(index, columnDefinition, entry.getKey(),
                        entry.getValue());
            }
        }
    }

    /**
     * Get a date-time string with the specified resolution for the specified
     * time.
     * 
     * @param time
     *            Time for which to fetch the string.
     * @param resolution
     *            Time resolution to be used.
     * @return Date-time string.
     */
    private String getDateTimeString(long time, TimeResolution resolution) {
        Date date = new Date(time);
        if (resolution == TimeResolution.SECONDS) {
            return secondsDateTimeFormatter.format(date);
        } else {
            return minutesDateTimeFormatter.format(date);
        }
    }

    /**
     * Update the specified cell to display the correct value with the correct
     * display attributes (font, etc.).
     * 
     * @param columnIndex
     *            Index of column in which the cell is to be updated.
     * @param columnDefinition
     *            Definition of the column in which the cell is to be updated.
     * @param item
     *            Tree item holding the cell that is to be updated.
     * @param entity
     *            Entity represented by the cell.
     */
    private void updateCell(int columnIndex, ColumnDefinition columnDefinition,
            TreeItem item, TabularEntity entity) {

        /*
         * If the cell is in the countdown column, update the display properties
         * for any countdown timer associated with this row's event.
         */
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_COUNTDOWN)
                && (countdownTimersDisplayManager != null)) {
            countdownTimersDisplayManager.updateDisplayPropertiesForEvent(
                    entitiesForTreeItems.get(item).getIdentifier(),
                    item.getFont());
        }

        /*
         * Set the cell text to the appropriate value.
         */
        setCellText(columnIndex, item, getCellValue(entity, columnDefinition),
                getEmptyFieldText(columnDefinition));
    }

    /**
     * Retrieve the value to display in a cell in the tree.
     * 
     * @param entity
     *            Entity represented by this row.
     * @param columnDefinition
     *            Definition of the column in which the cell lies.
     * @return Value to display in the specified tree cell.
     */
    private String getCellValue(TabularEntity entity,
            ColumnDefinition columnDefinition) {

        /*
         * Ensure a column definition was passed in.
         */
        if (columnDefinition == null) {
            throw new IllegalArgumentException("no column definition provided");
        }

        /*
         * If the column is a countdown-timer type, return the text to be
         * displayed unless the countdown timer manager does not exist or the
         * entity is not a root entity, in which case nothing should be
         * returned.
         */
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_COUNTDOWN)) {
            if ((countdownTimersDisplayManager == null)
                    || (entity.getHistoryIndex() != null)) {
                return null;
            }
            return countdownTimersDisplayManager.getTextForEvent(entity
                    .getIdentifier());
        }

        /*
         * Convert the appropriate value to the right type for the cell and
         * return it.
         */
        return convertToCellValue(
                entity.getAttributes().get(columnDefinition.getIdentifier()),
                entity.getTimeResolution(), columnDefinition);
    }

    /**
     * Convert the specified value to a proper cell value for the tree.
     * 
     * @param value
     *            Value to be converted.
     * @param timeResolution
     *            Time resolution of the entity represented by this cell.
     * @param columnDefinition
     *            Column definition for this cell.
     * @return Value to display in a tree cell.
     */
    private String convertToCellValue(Object value,
            TimeResolution timeResolution, ColumnDefinition columnDefinition) {

        /*
         * Ensure a column definition is provided.
         */
        if (columnDefinition == null) {
            throw new IllegalArgumentException("no column definition provided");
        }
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_DATE)) {
            Number number = (Number) value;
            if (number != null) {
                if (columnDefinition.getIdentifier().equals(
                        HAZARD_EVENT_END_TIME)
                        && (number.longValue() == UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
                    return UNTIL_FURTHER_NOTICE_COLUMN_TEXT;
                }
                return getDateTimeString(number.longValue(), timeResolution);
            } else {
                return EMPTY_STRING;
            }
        } else if (columnDefinition.getType().equals(
                SETTING_COLUMN_TYPE_BOOLEAN)) {
            Boolean flag = (value instanceof Number ? (((Number) value)
                    .intValue() != 0 ? Boolean.TRUE : Boolean.FALSE)
                    : (Boolean) value);
            return (flag == null ? BOOLEAN_COLUMN_NULL_TEXT : (Boolean.TRUE
                    .equals(flag) ? BOOLEAN_COLUMN_TRUE_TEXT
                    : BOOLEAN_COLUMN_FALSE_TEXT));
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Retrieves the text to display in hazard event tree fields which are
     * empty.
     * 
     * @param columnDefinition
     *            Column definition indicating how to display empty fields.
     * @return The text to display in this field if it is empty.
     */
    private String getEmptyFieldText(ColumnDefinition columnDefinition) {
        if (columnDefinition.getDisplayWhenEmpty() != null) {
            return columnDefinition.getDisplayWhenEmpty();
        } else if (columnDefinition.getType().equals(
                SETTING_COLUMN_TYPE_BOOLEAN)) {
            return BOOLEAN_COLUMN_NULL_TEXT;
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * Set the specified cell's text to the specified value.
     * 
     * @param columnIndex
     *            Index of the column in which the cell that is to have its text
     *            set is found.
     * @param item
     *            Tree item holding the cell to have its text set.
     * @param text
     *            Text to be used; may be <code>null</code>.
     * @param emptyValueDisplayString
     *            String to be displayed if <code>text</code> is
     *            <code>null</code>.
     */
    private void setCellText(int columnIndex, TreeItem item, String text,
            String emptyValueDisplayString) {
        item.setText(columnIndex,
                (text == null || text.length() == 0 ? emptyValueDisplayString
                        : text));
    }

    /**
     * Respond to the visible time range changing in the time line ruler by
     * updating other UI widgets.
     * 
     * @param lower
     *            Lower boundary of the visible time range as an epoch time in
     *            milliseconds.
     * @param upper
     *            Upper boundary of the visible time range as an epoch time in
     *            milliseconds.
     */
    private void updateWidgetsForNewVisibleTimeRange(long lower, long upper) {

        /*
         * Remember the new range.
         */
        visibleTimeRange = (upper - lower) + 1L;

        /*
         * Set the visible value range of the time scale widgets used by the
         * rows in the tree.
         */
        for (TreeEditor editor : treeEditorsForEntities.values()) {
            ((MultiValueScale) editor.getEditor()).setVisibleValueRange(lower,
                    upper);
        }

        /*
         * Notify the body of the change so that it may adjust its user
         * interface elements as appropriate.
         */
        rulerVisibleRangeChanged();
    }

    /**
     * Handle the visible range of the time line ruler having changed.
     */
    private void rulerVisibleRangeChanged() {
        user.timeLineRulerVisibleRangeChanged(ruler.getLowerVisibleValue(),
                ruler.getUpperVisibleValue(),
                WidgetUtilities.getTimeLineRulerZoomedOutRange(ruler),
                WidgetUtilities.getTimeLineRulerZoomedInRange(ruler));
    }

    /**
     * Respond to addition(s) to and/or removals from and/or resizings of the
     * set of visible columns having occurred.
     * 
     * @param addedOrRemoved
     *            Flag indicating whether or not an addition and/or subtraction
     *            was made from the visible columns.
     */
    private void visibleColumnsChanged(boolean addedOrRemoved) {
        if (tree.isDisposed()) {
            return;
        }

        /*
         * Turn on tree redraw, and make the time ruler visible and fit it to
         * its column.
         */
        tree.setRedraw(true);
        ruler.setVisible(true);
        TreeColumn column = tree
                .getColumn(getIndexOfColumnInTree(TIME_SCALE_COLUMN_NAME));
        if (addedOrRemoved) {
            visibleColumnCountJustChanged = true;
        }
        fitRulerToColumn(column);
    }

    /**
     * Pan the time line to ensure that the specified time is shown.
     */
    private void showTime(long time) {
        long lower = time - (visibleTimeRange / 2L);
        if (lower < HazardConstants.MIN_TIME) {
            lower = HazardConstants.MIN_TIME;
        }
        long upper = lower + visibleTimeRange - 1L;
        if (upper > HazardConstants.MAX_TIME) {
            lower -= upper - HazardConstants.MAX_TIME;
            upper = HazardConstants.MAX_TIME;
        }
        setVisibleTimeRange(Range.closed(lower, upper), true);
    }

    /**
     * Find the tree column that lies under the specified point.
     * 
     * @param point
     *            Point under which to look for the tree column.
     * @return Tree column under the point.
     */
    private TreeColumn getTreeColumnAtPoint(Point point) {
        Rectangle clientArea = tree.getClientArea();
        if (point.x >= clientArea.x) {
            int xCurrent = 0;
            for (int columnIndex : tree.getColumnOrder()) {
                TreeColumn column = tree.getColumn(columnIndex);
                int xNext = xCurrent + column.getWidth();
                if ((point.x >= xCurrent) && (point.x < xNext)) {
                    return column;
                }
                xCurrent = xNext;
            }
        }
        return null;
    }

    /**
     * Prepare the specified column sort menu for display.
     * 
     * @param menu
     *            Menu to be prepared.
     * @param sort
     *            Sort that this menu represents; if <code>null</code>, there is
     *            no sort for this menu.
     */
    private void prepareSortMenuForDisplay(Menu menu, Sort sort) {
        String columnName = (sort == null ? null : columnNamesForIdentifiers
                .get(sort.getAttributeIdentifier()));
        for (MenuItem columnMenuItem : menu.getItems()) {
            for (MenuItem directionMenuItem : columnMenuItem.getMenu()
                    .getItems()) {
                directionMenuItem.setSelection((sort != null)
                        && columnName.equals(columnMenuItem.getText())
                        && (sort.getSortDirection() == directionMenuItem
                                .getData()));
            }
        }
    }

    /**
     * Create the header context menu.
     * 
     * @param point
     *            Tree-widget-relative point from which to deploy the context
     *            menu.
     * @return <code>true</code> if a menu is set to be deployed immediately,
     *         <code>false</code> otherwise.
     */
    private boolean createHeaderContextMenu(Point point) {

        /*
         * Set the menu's items checkboxes to reflect which columns are showing
         * and which are hidden. If only one column is showing, disable that
         * checkbox so that the user cannot uncheck it. Then update the sort
         * menus to reflect the current sorts. Finally, update any associated
         * megawidget manager's state as well, and set the menu as belonging to
         * the tree. If no menu is found for the specified column, do nothing.
         */
        TreeColumn column = getTreeColumnAtPoint(point);
        if (column != null) {
            Menu menu = headerMenusForColumnNames.get(column.getText());
            if (menu == null) {
                return false;
            }
            for (MenuItem menuItem : menu.getItems()) {
                if (menuItem.getStyle() == SWT.SEPARATOR) {
                    break;
                }
                menuItem.setSelection(visibleColumnNames.contains(menuItem
                        .getText()));
                menuItem.setEnabled(!menuItem.getSelection()
                        || (visibleColumnNames.size() > 1));
            }
            prepareSortMenuForDisplay(primarySortMenusForColumnMenus.get(menu),
                    (sorts.isEmpty() == false ? sorts.get(0) : null));
            Menu secondarySortMenu = secondarySortMenusForColumnMenus.get(menu);
            secondarySortMenu.getParentItem().setEnabled(
                    sorts.isEmpty() == false);
            if (sorts.isEmpty() == false) {
                prepareSortMenuForDisplay(
                        secondarySortMenusForColumnMenus.get(menu),
                        (sorts.size() > 1 ? sorts.get(1) : null));
            }
            MegawidgetManager megawidgetManager = headerMegawidgetManagersForColumnNames
                    .get(column.getText());
            if (megawidgetManager != null) {
                try {
                    megawidgetManager
                            .setState(headerFilterStatesForColumnIdentifiers);
                } catch (MegawidgetStateException exception) {
                    statusHandler.error("Unable to set megawidget manager "
                            + "state.", exception);
                }
            }
            tree.setMenu(menu);
            return true;
        }
        return false;
    }

    /**
     * Create the row context menu.
     * 
     * @param point
     *            Tree-widget-relative point from which to deploy the context
     *            menu.
     */
    private void createRowContextMenu(Point point) {

        /*
         * Dispose of any existing row menu first.
         */
        if ((rowMenu != null) && (rowMenu.isDisposed() == false)) {
            rowMenu.dispose();
        }

        /*
         * Create a context-sensitive menu to be deployed for tree items as
         * appropriate, and add the until further notice menu item.
         * 
         * TODO: Consider moving all the until-further-notice menu item creation
         * and configuration to the presenter's context menu generation method.
         * Why should it be here?
         */
        rowMenu = new Menu(tree);
        untilFurtherNoticeMenuItem = new MenuItem(rowMenu, SWT.PUSH);
        untilFurtherNoticeMenuItem.setText(UNTIL_FURTHER_NOTICE_MENU_TEXT);
        untilFurtherNoticeMenuItem.addSelectionListener(rowMenuListener);

        /*
         * Iterate through the selected items, determining for each one whether
         * its until further notice state may be changed, and whether or not it
         * is currently in until further notice mode. Do not bother checking
         * those that are child items.
         */
        boolean untilFurtherNoticeAllowed = true;
        int numUntilFurtherNotice = 0;
        if ((selectedItems == null) || (selectedItems.length == 0)) {
            untilFurtherNoticeAllowed = false;
        } else {
            boolean selectionIncludesParentItem = false;
            for (TreeItem item : selectedItems) {
                TabularEntity entity = entitiesForTreeItems.get(item);
                if (entity.getHistoryIndex() != null) {
                    continue;
                }
                selectionIncludesParentItem = true;
                if (entity.isAllowUntilFurtherNotice() == false) {
                    untilFurtherNoticeAllowed = false;
                }
                if (entity.isEndTimeUntilFurtherNotice()) {
                    numUntilFurtherNotice++;
                }
            }
            if (selectionIncludesParentItem == false) {
                untilFurtherNoticeAllowed = false;
            }
        }

        /*
         * Set the image for the menu item as checked, semi-checked, or
         * unchecked, depending upon whether all, some, or none of the selected
         * entities are in until further notice mode. Then enable it if all the
         * selected entities may have this option toggled.
         * 
         * NOTE: This is an SWT kludge; it is using a push-button menu item to
         * simulate a tri-state checkbox menu item, because SWT, in its infinite
         * wisdom, does not offer a tri-state checkbox menu item.
         */
        untilFurtherNoticeMenuItem
                .setImage(numUntilFurtherNotice == 0 ? uncheckedMenuItemImage
                        : (numUntilFurtherNotice == selectedItems.length ? checkedMenuItemImage
                                : semiCheckedMenuItemImage));

        /*
         * Enable the menu item if all selected entities can have their until
         * further notice mode toggled, and show the menu.
         */
        untilFurtherNoticeMenuItem.setEnabled(untilFurtherNoticeAllowed);

        /*
         * Iterate through the contribution items needed, creating a menu item
         * for each in turn.
         */
        TreeItem treeItem = tree.getItem(point);
        List<MenuItem> items = new ArrayList<>();
        boolean separatorCreated = false;
        for (IContributionItem item : user.getContextMenuItems(
                (treeItem != null ? entitiesForTreeItems.get(treeItem)
                        .getIdentifier() : null),
                (treeItem != null ? entitiesForTreeItems.get(treeItem)
                        .getPersistedTimestamp() : null))) {
            MenuItem menuItem = null;
            if (item instanceof ActionContributionItem) {
                if (separatorCreated == false) {
                    new MenuItem(rowMenu, SWT.SEPARATOR);
                    separatorCreated = true;
                }
                ActionContributionItem actionItem = (ActionContributionItem) item;
                menuItem = new MenuItem(rowMenu, SWT.PUSH);
                menuItem.setText(actionItem.getAction().getText());
                menuItem.setEnabled(actionItem.isEnabled());
                final IAction action = actionItem.getAction();
                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        action.run();
                    }
                });
            } else if (item instanceof Separator) {
                menuItem = new MenuItem(rowMenu, SWT.SEPARATOR);
            }
            if (menuItem != null) {
                items.add(menuItem);
            }
        }
        tree.setMenu(rowMenu);
    }

    /**
     * Update any countdown timer cells that need updating.
     */
    private void updateCountdownTimers() {

        /*
         * Do nothing unless the countdown timer column is showing and the
         * countdown timer display manager exists.
         */
        if ((tree == null) || tree.isDisposed()
                || (countdownTimersDisplayManager == null)) {
            return;
        }
        int columnIndex = getIndexOfColumnInTree(countdownTimerColumnName);
        if (columnIndex != -1) {

            /*
             * Find out which countdown timers need updating.
             */
            Map<String, CountdownTimersDisplayManager.UpdateType> updateTypesForEventIdentifiers = countdownTimersDisplayManager
                    .getEventsNeedingUpdateAndRefreshRedrawTimes();

            /*
             * Iterate through the countdown timers, updating the display of any
             * that have corresponding tree cells.
             */
            ColumnDefinition columnDefinition = columnDefinitionsForNames
                    .get(countdownTimerColumnName);
            for (Map.Entry<String, CountdownTimersDisplayManager.UpdateType> entry : updateTypesForEventIdentifiers
                    .entrySet()) {

                /*
                 * If this event is not found in the tree, skip it.
                 */
                TabularEntity entity = tabularEntitiesForRootIdentifiers
                        .get(entry.getKey());
                if (entity == null) {
                    continue;
                }
                TreeItem item = treeItemsForEntities.get(entity);
                if (item == null) {
                    continue;
                }

                /*
                 * Update the text and/or redraw the tree cell, as appropriate.
                 */
                if ((entry.getValue() == CountdownTimersDisplayManager.UpdateType.TEXT)
                        || (entry.getValue() == CountdownTimersDisplayManager.UpdateType.TEXT_AND_COLOR)) {
                    updateCell(columnIndex, columnDefinition, item, entity);
                }
                if ((entry.getValue() == CountdownTimersDisplayManager.UpdateType.COLOR)
                        || (entry.getValue() == CountdownTimersDisplayManager.UpdateType.TEXT_AND_COLOR)) {
                    Rectangle bounds = item.getBounds(columnIndex);
                    tree.redraw(bounds.x - 1, bounds.y - 1, bounds.width + 1,
                            bounds.height + 1, false);
                }
            }

            /*
             * Force the tree to redraw immediately, so that blinking of any
             * countdown timers that should blink occurs.
             */
            tree.update();

            /*
             * Schedule the next countdown timers display update.
             */
            scheduleNextCountdownTimerDisplayUpdate();
        }
    }

    /**
     * Update all countdown timer cells.
     */
    private void updateAllCountdownTimers() {

        /*
         * Do nothing unless the countdown timer column is showing and the
         * countdown timer display manager exists.
         */
        if ((tree == null) || tree.isDisposed()
                || (countdownTimersDisplayManager == null)) {
            return;
        }

        /*
         * Turn off redraw, update all the cells, and turn redraw back on to
         * force redrawing in case of blinking cells.
         */
        tree.setRedraw(false);
        updateCellsForColumn(countdownTimerColumnName);
        tree.setRedraw(true);
        tree.update();

        /*
         * Calculate the next display update time for each of the countdown
         * timers.
         */
        countdownTimersDisplayManager.refreshAllRedrawTimes();

        /*
         * Schedule the next countdown timers display update.
         */
        scheduleNextCountdownTimerDisplayUpdate();
    }

    /**
     * Schedule the next countdown timer display update.
     */
    private void scheduleNextCountdownTimerDisplayUpdate() {
        List<String> identifiers = new ArrayList<>(tabularEntities.size());
        for (TabularEntity entity : tabularEntities) {
            identifiers.add(entity.getIdentifier());
        }
        countdownTimersDisplayManager.scheduleNextDisplayUpdate(identifiers);
    }

    /**
     * Get the index at which the specified column is found in the tree. This is
     * not the index indicating the current ordering of the columns, but rather
     * the index that, when provided to {@link Tree#getColumn(int)}, returns the
     * specified column.
     * 
     * @param name
     *            Name of the column for which to find the index.
     * @return Index of the column in the tree, or <code>-1</code> if the column
     *         is not currently in the tree.
     */
    private int getIndexOfColumnInTree(String name) {
        if (name == null) {
            return -1;
        }
        TreeColumn[] columns = tree.getColumns();
        for (int j = 0; j < columns.length; j++) {
            if (name.equals(columns[j].getText())) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Delete the column header menus, if they were created.
     */
    private void deleteColumnHeaderMenus() {
        if (headerMenusForColumnNames != null) {
            for (Menu menu : headerMenusForColumnNames.values()) {
                menu.dispose();
            }
            headerMenusForColumnNames.clear();
            headerMenusForColumnNames = null;
        }
    }

    /**
     * Fit the timeline ruler to the specified column's header.
     * 
     * @param column
     *            Column with the header in which the timeline ruler is to be
     *            placed.
     */
    private void fitRulerToColumn(final TreeColumn column) {

        /*
         * If this is a refit (second attempt), reset the refit flag; otherwise,
         * set it, so that another fit will be scheduled. This is because SWT
         * sometimes seems to report the wrong widths (presumably because the
         * tree is in an interim state where it thinks it needs a scrollbar, but
         * will soon find it does not); in these cases, a retry later on seems
         * to resize the ruler to its correct width.
         */
        willRefitRulerToColumn = !willRefitRulerToColumn;

        /*
         * If the tree is disposed, just schedule another refit to occur later;
         * otherwise, do the fitting.
         */
        if (tree.isDisposed()) {
            willRefitRulerToColumn = true;
        }

        if (willRefitRulerToColumn) {
            Display.getCurrent().asyncExec(new Runnable() {

                @Override
                public void run() {
                    fitRulerToColumn(column);
                }
            });
        } else {

            /*
             * Layout the tree.
             */
            tree.layout(true);
            parent.layout(true);

            /*
             * Calculate the column boundaries.
             */
            tree.setRedraw(false);
            int columnWidth = column.getWidth();

            /*
             * Get the difference between the tree bounds and the client area,
             * which should give the vertical scrollbar width.
             */
            Rectangle treeBounds = tree.getBounds();
            int clientAreaWidth = tree.getClientArea().width;
            int cellTreeEndXDiff = treeBounds.width - clientAreaWidth;

            /*
             * Determine how much of the width of the time range cell is
             * horizontally scrolled out of view, if any. This will only occur
             * if a horizontal scrollbar is showing.
             */
            int cellWidthScrolledOutOfView = 0;
            ScrollBar scrollBar = tree.getHorizontalBar();
            if ((scrollBar != null)
                    && (scrollBar.getMaximum() > clientAreaWidth)) {
                cellWidthScrolledOutOfView = (scrollBar.getMaximum() - clientAreaWidth)
                        - scrollBar.getSelection();
            }

            /*
             * Determine the horizontal boundaries of the ruler.
             */
            int cellBeginXPixels = treeBounds.width
                    + cellWidthScrolledOutOfView - columnWidth
                    - cellTreeEndXDiff;
            int cellEndXPixels = cellTreeEndXDiff;

            /*
             * Set up the layout data for the ruler.
             */
            FormData rulerFormData = new FormData();
            rulerFormData.left = new FormAttachment(0, cellBeginXPixels
                    + timeRulerMarginWidth);
            rulerFormData.top = new FormAttachment(0, rulerTopOffset);
            rulerFormData.right = new FormAttachment(100,
                    (timeRulerMarginWidth + cellEndXPixels) * -1);
            ruler.setLayoutData(rulerFormData);

            /*
             * Notify the using class of the change in the time line ruler's
             * horizontal bounds.
             */
            user.timeLineRulerSizeChanged(cellBeginXPixels, cellEndXPixels);

            /*
             * Redraw the tree.
             */
            tree.setRedraw(true);

            /*
             * If the vertical scrollbar was showing before but is not now, or
             * vice versa, or if one or more columns were added or removed,
             * asynchronously invoke this method again, since when said
             * scrollbar changes visibility or the visible columns set is
             * changed the laying out of the ruler seems to lag behind, despite
             * the fact that it gets the right dimensions from the calculations
             * above for its form layout.
             */
            if ((verticalScrollbarShowing != ((tree.getVerticalBar() != null) && (tree
                    .getVerticalBar().isVisible())))
                    || visibleColumnCountJustChanged) {
                verticalScrollbarShowing = !verticalScrollbarShowing;
                visibleColumnCountJustChanged = false;
                willRefitRulerToColumn = true;
                Display.getCurrent().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        fitRulerToColumn(column);
                    }
                });
            }
        }

        /*
         * If a refit will be needed, schedule one to run.
         */
        // if (willRefitRulerToColumn) {
        // Display.getCurrent().asyncExec(new Runnable() {
        //
        // @Override
        // public void run() {
        // fitRulerToColumn(column);
        // }
        // });
        // }
    }

    /**
     * Lay out the ruler according to whether or not the tree's vertical
     * scrollbar exists.
     */
    private void repackRuler() {
        Rectangle treeRectangle = tree.getBounds();
        int treeYPixels = treeRectangle.y;
        int columnWidth = tree.getColumn(
                getIndexOfColumnInTree(TIME_SCALE_COLUMN_NAME)).getWidth();
        int xPixels = treeRectangle.width - columnWidth;
        int headerHeight = tree.getHeaderHeight();
        Rectangle rectangle = ruler.getBounds();
        int rulerHeight = rectangle.height;
        FormData rulerFormData = new FormData();
        rulerFormData.left = new FormAttachment(0, xPixels
                + timeRulerMarginWidth);
        rulerTopOffset = treeYPixels + ((headerHeight - rulerHeight) / 2)
                - timeRulerMarginHeight;
        rulerFormData.top = new FormAttachment(0, rulerTopOffset);
        rulerFormData.right = new FormAttachment(100, 0);
        ruler.setLayoutData(rulerFormData);
        ruler.moveAbove(tree);
    }

    /**
     * Truncate the specified time for the current time resolution.
     * 
     * @param date
     *            Date-time to be truncated.
     * @return Truncated time.
     */
    private Date truncateTimeForResolution(Date date) {
        return DateUtils.truncate(date,
                HazardConstants.TRUNCATION_UNITS_FOR_TIME_RESOLUTIONS
                        .get(timeResolution));
    }

    /**
     * Zoom to the specified visible time range.
     * 
     * @param newVisibleTimeRange
     *            New visible time range as an epoch time range in milliseconds.
     */
    private void zoomVisibleTimeRange(long newVisibleTimeRange) {

        /*
         * If the zoom resulted in a visible time range change, notify the time
         * range change handler of the change.
         */
        if (ruler.zoomVisibleValueRange(newVisibleTimeRange)) {
            notifyHandlerOfVisibleTimeRangeChange(ruler.getLowerVisibleValue(),
                    ruler.getUpperVisibleValue());
        }
    }

    /**
     * Pan the time range by an amount equal to the current visible time range
     * multiplied by the specified value.
     * 
     * @param multiplier
     *            Multiplier to apply to the current visible time range in order
     *            to determine how far and in which direction to pan the time
     *            range.
     */
    private void panTime(float multiplier) {
        long lower = ruler.getLowerVisibleValue();
        long upper = ruler.getUpperVisibleValue();
        long delta = Math.round((double) (multiplier * (upper + 1 - lower)));
        setVisibleTimeRange(Range.closed(lower + delta, upper + delta), true);
    }

    /**
     * Set the visible time range as specified.
     * 
     * @param range
     *            New visible time range with lower and upper bounds measured in
     *            epoch times in milliseconds.
     * @param forwardAction
     *            Flag indicating whether or not the change should be forwarded
     *            to listeners. If <code>false</code>, the change will still be
     *            forwarded if the specified boundaries needed alteration before
     *            being used.
     */
    private void setVisibleTimeRange(Range<Long> range, boolean forwardAction) {

        /*
         * Sanity check the new bounds, marking the change as requiring
         * notification of the view if said bounds need shifting.
         */
        long lower = range.lowerEndpoint();
        long upper = range.upperEndpoint();
        if (lower < HazardConstants.MIN_TIME) {
            forwardAction = true;
            upper += HazardConstants.MIN_TIME - lower;
            lower = HazardConstants.MIN_TIME;
        }
        if (upper > HazardConstants.MAX_TIME) {
            forwardAction = true;
            lower -= upper - HazardConstants.MAX_TIME;
            upper = HazardConstants.MAX_TIME;
        }

        /*
         * If the time range has changed from what the time line already had,
         * commit to the change and if necessary notify the view.
         */
        if ((lower != ruler.getLowerVisibleValue())
                || (upper != ruler.getUpperVisibleValue())) {
            ruler.setVisibleValueRange(lower, upper);
            if (forwardAction) {
                notifyHandlerOfVisibleTimeRangeChange(lower, upper);
            }
        }
    }

    /**
     * Set the selected time range to that specified.
     * 
     * @param timeRange
     *            New time range.
     */
    private void setSelectedTimeRange(Range<Long> timeRange) {

        /*
         * Remember the new values.
         */
        selectedTimeStart = truncateTimeForResolution(
                new Date(timeRange.lowerEndpoint())).getTime();
        selectedTimeEnd = truncateTimeForResolution(
                new Date(timeRange.upperEndpoint())).getTime();

        /*
         * Determine whether the selected time mode was single or range, and if
         * it was single, whether it needs to be changed to range due to the new
         * values.
         */
        boolean oldSingleMode = (ruler.getFreeThumbValueCount() != 0);
        boolean newSingleMode = oldSingleMode;

        /*
         * If the selected time mode must be changed to range, reconfigure the
         * ruler as appropriate, as well as the time scales for the events. Also
         * change the drop-down selector to show the new mode.
         */
        SelectedTimeMode oldMode = selectedTimeMode;
        if (oldSingleMode && (selectedTimeStart != selectedTimeEnd)) {
            newSingleMode = false;
            ruler.setFreeThumbValues();
            for (TreeEditor editor : treeEditorsForEntities.values()) {
                ((MultiValueScale) editor.getEditor())
                        .setFreeMarkedValues(currentTime);
            }
            selectedTimeMode = SelectedTimeMode.RANGE;
        }

        /*
         * Set the ruler to show the new time range. If it has just changed to
         * single mode from range mode or vice versa, configure the ruler some
         * more, as well as the event time scales.
         */
        if (newSingleMode) {
            ruler.setFreeThumbValues(selectedTimeStart);
            if (oldSingleMode == false) {
                ruler.setConstrainedThumbValues();
                for (TreeEditor editor : treeEditorsForEntities.values()) {
                    ((MultiValueScale) editor.getEditor())
                            .setConstrainedMarkedValues();
                }
                selectedTimeMode = SelectedTimeMode.SINGLE;
            }
        } else {
            ruler.setConstrainedThumbValues(selectedTimeStart, selectedTimeEnd);
            if (oldSingleMode) {
                ruler.setConstrainedThumbColor(0, timeRangeEdgeColor);
                ruler.setConstrainedThumbColor(1, timeRangeEdgeColor);
                ruler.setConstrainedThumbRangeColor(1, timeRangeFillColor);
                for (TreeEditor editor : treeEditorsForEntities.values()) {
                    MultiValueScale scale = (MultiValueScale) editor
                            .getEditor();
                    scale.setConstrainedMarkedValueColor(0, timeRangeEdgeColor);
                    scale.setConstrainedMarkedValueColor(1, timeRangeEdgeColor);
                    scale.setConstrainedMarkedRangeColor(1, timeRangeFillColor);
                }
            }
        }

        /*
         * Notify the user class if the time mode has changed.
         */
        if (selectedTimeMode != oldMode) {
            user.selectedTimeModeChanged(selectedTimeMode);
        }
    }

    /**
     * Notify the time range change handler of the specified visible time range
     * change.
     * 
     * @param lower
     *            Lower bound of the new visible time range.
     * @param upper
     *            Upper bound of the new visible time range.
     */
    private void notifyHandlerOfVisibleTimeRangeChange(long lower, long upper) {
        notifyHandlerOfTimeRangeChange(TimeRangeType.VISIBLE,
                Range.closed(lower, upper));
    }

    /**
     * Notify the handler of a change in the selected time range if it is due to
     * user manipulation of the GUI.
     * 
     * @param source
     *            Source of the change.
     */
    private void notifyHandlerOfSelectedTimeRangeChange(ChangeSource source) {
        if ((source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                || (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
            notifyHandlerOfSelectedTimeRangeChange();
        }
    }

    /**
     * Notify the handler of any selected time range change.
     */
    private void notifyHandlerOfSelectedTimeRangeChange() {
        notifyHandlerOfTimeRangeChange(TimeRangeType.SELECTED,
                Range.closed(selectedTimeStart, selectedTimeEnd));
    }

    /**
     * Notify the time range change handler of the specified change.
     * 
     * @param type
     *            Type of the range that is changing.
     * @param timeRange
     *            New time range.
     */
    private void notifyHandlerOfTimeRangeChange(TimeRangeType type,
            Range<Long> timeRange) {
        if (timeRangeChangeHandler != null) {
            timeRangeChangeHandler.stateChanged(type, timeRange);
        }
    }

    /**
     * Clear the tree of items.
     */
    private void clearTree() {

        /*
         * Remove the tree items from the tree, which will cause them to be
         * disposed of.
         */
        if (tree.isDisposed() == false) {
            tree.removeAll();
        }

        /*
         * Dispose of the editors holding the time scales, and the time scales
         * themselves, then clear the records of both.
         */
        treeItemsForScales.clear();
        for (TreeEditor editor : treeEditorsForEntities.values()) {
            editor.getEditor().dispose();
            editor.dispose();
        }
        treeEditorsForEntities.clear();

        /*
         * Clear the menu and the selected items, and dispose of any colors.
         */
        tree.setMenu(null);
        selectedItems = null;
        disposeOfTimeRangeColors();
    }

    /**
     * Set the columns to those specified.
     * 
     * @param columns
     *            New columns.
     */
    @SuppressWarnings("unchecked")
    private void setColumns(ConsoleColumns columns) {
        this.columns = columns;

        /*
         * Delete any header menus for the columns.
         */
        deleteColumnHeaderMenus();

        /*
         * Get the column definitions, and the visible column names.
         */
        columnDefinitionsForNames = columns.getColumnDefinitionsForNames();
        visibleColumnNames = new ArrayList<>(columns.getVisibleColumnNames());

        /*
         * Determine which of the visible columns may generate hint text, and
         * which hold date values.
         */
        hintTextIdentifiersForVisibleColumnNames.clear();
        dateIdentifiersForVisibleColumnNames.clear();
        for (String columnName : visibleColumnNames) {
            determineSpecialPropertiesOfColumn(columnName);
        }

        /*
         * Compile a mapping of column identifiers to their names.
         */
        columnNamesForIdentifiers.clear();
        for (String name : columnDefinitionsForNames.keySet()) {
            ColumnDefinition column = columnDefinitionsForNames.get(name);
            columnNamesForIdentifiers.put(column.getIdentifier(), name);
        }

        /*
         * Now begin altering the tree itself. First, prepare for the addition
         * and/or removal of columns.
         */
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;
        visibleColumnCountChanging();

        /*
         * Remove the sorting column, if any.
         */
        tree.setSortColumn(null);

        /*
         * If the header menus for the various columns have not yet been
         * created, create them now.
         */
        if (headerMenusForColumnNames == null) {

            /*
             * Get a list of all the column names for which there are
             * definitions, sorted alphabetically.
             */
            List<String> columnNames = new ArrayList<>(
                    columnDefinitionsForNames.keySet());
            Collections.sort(columnNames);

            /*
             * Copy the list of column names to another list that will be used
             * to track which columns have associated header menus in the loop
             * below.
             */
            List<String> columnNamesToBeGivenMenus = new ArrayList<>(
                    columnNames);

            /*
             * Create the mapping of column names to their header menus. Make a
             * menu for each column that has an associated filter, making each
             * such menu have both the checklist of column names and the filter,
             * and then make one more menu with just the column names checklist
             * for all the columns that do not have associated filters.
             */
            headerMenusForColumnNames = new HashMap<>();
            headerMegawidgetManagersForColumnNames = new HashMap<>();
            for (int j = 0; j < filterMegawidgets.size() + 1; j++) {

                /*
                 * If there are no more columns needing menus, do nothing more.
                 */
                if (columnNamesToBeGivenMenus.size() == 0) {
                    break;
                }

                /*
                 * If there is a filter to be processed, get it and configure
                 * it, and find its associated column name.
                 */
                Map<String, Object> filter = null;
                String filterColumnName = null;
                if (j < filterMegawidgets.size()) {
                    filter = filterMegawidgets.get(j);
                    filter.put(IMenuSpecifier.MEGAWIDGET_SHOW_SEPARATOR, true);
                    filter.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                            FILTER_MENU_NAME);
                    filterColumnName = (String) filter.get(COLUMN_NAME);
                }

                /*
                 * If the no-filter menu needs to be built, or a filter was
                 * found and its associated column is found in the list of
                 * defined columns, create a menu and associate it with the
                 * column name.
                 */
                if ((filterColumnName == null)
                        || (columnNamesToBeGivenMenus.remove(filterColumnName))) {

                    /*
                     * Create the column name checklist, allowing the user to
                     * toggle column visibility.
                     */
                    Menu menu = new Menu(tree);
                    for (String name : columnNames) {
                        MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
                        menuItem.setText(name);
                        menuItem.addSelectionListener(headerMenuListener);
                    }

                    /*
                     * Create the primary and secondary sort menu items and
                     * their submenus.
                     */
                    for (int sortPriority = 0; sortPriority < 2; sortPriority++) {
                        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
                        Menu sortMenu = new Menu(menuItem);
                        menuItem.setMenu(sortMenu);
                        for (String columnName : columnNames) {
                            MenuItem columnNameMenuItem = new MenuItem(
                                    sortMenu, SWT.CASCADE);
                            columnNameMenuItem.setText(columnName);
                            Menu subMenu = new Menu(columnNameMenuItem);
                            columnNameMenuItem.setMenu(subMenu);
                            for (SortDirection direction : SortDirection
                                    .values()) {
                                MenuItem directionMenuItem = new MenuItem(
                                        subMenu, SWT.RADIO);
                                directionMenuItem.setText(direction.toString());
                                directionMenuItem.setData(direction);
                                directionMenuItem
                                        .addSelectionListener(sortMenuListener);
                            }
                        }
                        if (sortPriority == 0) {
                            menuItem.setText(PRIMARY_SORT_MENU_NAME);
                            primarySortMenusForColumnMenus.put(menu, sortMenu);
                        } else {
                            menuItem.setText(SECONDARY_SORT_MENU_NAME);
                            secondarySortMenusForColumnMenus
                                    .put(menu, sortMenu);
                        }
                    }

                    /*
                     * If a filter exists for this menu, add it and associate it
                     * with the column. Otherwise, the menu being created is a
                     * catch-all for any column that does not have an associated
                     * filter.
                     */
                    if (filterColumnName != null) {
                        try {
                            headerMegawidgetManagersForColumnNames
                                    .put(filterColumnName,
                                            new MegawidgetManager(
                                                    menu,
                                                    Lists.newArrayList(filter),
                                                    headerFilterStatesForColumnIdentifiers,
                                                    new MegawidgetManagerAdapter() {

                                                        @Override
                                                        public void stateElementChanged(
                                                                MegawidgetManager manager,
                                                                String identifier,
                                                                Object state) {
                                                            if (columnFiltersChangeHandler != null) {
                                                                columnFiltersChangeHandler
                                                                        .stateChanged(
                                                                                identifier,
                                                                                state);
                                                            }
                                                        }
                                                    }));

                        } catch (MegawidgetException e) {
                            statusHandler
                                    .error("Unable to create megawidget "
                                            + "manager due to megawidget construction problem: "
                                            + e, e);
                        }

                        /*
                         * Associate this header menu with the column name.
                         */
                        headerMenusForColumnNames.put(filterColumnName, menu);
                    } else {
                        for (String columnName : columnNamesToBeGivenMenus) {
                            headerMenusForColumnNames.put(columnName, menu);
                        }
                    }
                }
            }
        }

        /*
         * Remove any columns that should no longer be visible. The time scale
         * column should not be removed, since it is always visible.
         */
        TreeColumn[] treeColumns = tree.getColumns();
        for (int j = 0; j < treeColumns.length; ++j) {
            TreeColumn treeColumn = treeColumns[j];
            String columnName = treeColumn.getText();
            if ((columnName.equals(TIME_SCALE_COLUMN_NAME) == false)
                    && (visibleColumnNames.contains(columnName) == false)) {
                treeColumn.dispose();
                if (columnName.equals(countdownTimerColumnName)) {
                    countdownTimerColumnIndex = -1;
                }
            }
        }

        /*
         * Make a list of the column names remaining in the tree, and update the
         * widths of these columns based on the widths that were provided.
         */
        List<String> columnNames = new ArrayList<>();
        for (int j = 0; j < tree.getColumnCount(); ++j) {
            TreeColumn column = tree.getColumn(j);
            String columnName = column.getText();
            if (columnName.equals(TIME_SCALE_COLUMN_NAME)) {
                continue;
            }
            columnNames.add(columnName);
            ColumnDefinition columnDefinition = columnDefinitionsForNames
                    .get(columnName);
            if (columnDefinition != null) {
                Integer width = columnDefinition.getWidth();
                if (width != null) {
                    tree.getColumn(j).setWidth(width);
                }
            }
        }

        /*
         * Add any columns that have not yet been added, inserting each where it
         * belongs (using the canonical order).
         */
        for (String columnName : visibleColumnNames) {
            if (columnNames.contains(columnName) == false) {
                createTreeColumn(columnName, tree.getColumnCount() - 1);
                updateCellsForColumn(columnName);
            }
        }

        /*
         * Ensure the primary sort column, if any, has the appropriate visual
         * cues.
         */
        updateColumnSortVisualCues();

        /*
         * Set the column order to match the order of the visible column names
         * list.
         */
        updateColumnOrder();

        /*
         * Ensure that the checkboxes in the tree's rows are in the leftmost
         * column.
         */
        ensureCheckboxesAreInLeftmostColumn(false);

        /*
         * Finish up following the addition and/or removal of columns.
         */
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        visibleColumnsChanged(true);
    }
}
