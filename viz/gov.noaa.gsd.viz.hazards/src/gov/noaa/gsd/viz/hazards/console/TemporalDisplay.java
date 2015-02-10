/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_CHECKED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_COLOR;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_IDENTIFIER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SELECTED;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_ASCENDING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_DESCENDING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_DIRECTION_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_SORT_PRIORITY_NONE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_COUNTDOWN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_DATE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_NUMBER;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.SETTING_COLUMN_TYPE_STRING;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.TIME_RANGE_MINIMUM_INTERVAL;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS;
import gov.noaa.gsd.common.utilities.JSONConverter;
import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimersDisplayListener;
import gov.noaa.gsd.viz.hazards.alerts.CountdownTimersDisplayManager;
import gov.noaa.gsd.viz.hazards.contextmenu.ContextMenuHelper;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.setting.SettingsView;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.megawidgets.IMenuSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManagerAdapter;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
import gov.noaa.gsd.viz.widgets.CustomToolTip;
import gov.noaa.gsd.viz.widgets.DayHatchMarkGroup;
import gov.noaa.gsd.viz.widgets.IHatchMarkGroup;
import gov.noaa.gsd.viz.widgets.IMultiValueLinearControlListener;
import gov.noaa.gsd.viz.widgets.IMultiValueTooltipTextProvider;
import gov.noaa.gsd.viz.widgets.ISnapValueCalculator;
import gov.noaa.gsd.viz.widgets.IVisibleValueZoomCalculator;
import gov.noaa.gsd.viz.widgets.ImageUtilities;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl;
import gov.noaa.gsd.viz.widgets.MultiValueLinearControl.ChangeSource;
import gov.noaa.gsd.viz.widgets.MultiValueRuler;
import gov.noaa.gsd.viz.widgets.MultiValueScale;
import gov.noaa.gsd.viz.widgets.TimeHatchMarkGroup;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.time.DateUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.icon.IconUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: Temporal display, providing the user the ability to view and
 * manipulate hazard events, with an emphasis on their time ranges. The temporal
 * display is comprised of a table of hazard events, including time range
 * widgets and temporal navigation controls to allow navigation of an included
 * timeline ruler.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jun 04, 2013            Chris.Golden      Added support for changing background
 *                                           and foreground colors in order to stay
 *                                           in synch with CAVE mode. Also tightened
 *                                           up area around timeline ruler to make
 *                                           the column header border show up around
 *                                           it as it should.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Aug 09, 2013    1936    Chris.Golden      Added console countdown timers.
 * Nov 04, 2013    2182    daniel.s.schaffer Started refactoring
 * Nov 05, 2013    2336    Chris.Golden      Changed to work with new multi-thumbed
 *                                           slider and ruler listener parameters.
 *                                           Also altered to handle new location of
 *                                           utility classes.
 * Nov 29, 2013    2380    daniel.s.schaffer Minor cleanup
 * Dec 04, 2013    2377    Chris.Golden      Fixed bug that caused time-remaining
 *                                           column's cells to not be formatted,
 *                                           right-adjusted, etc. appropriately if
 *                                           Hazard Services was started with a
 *                                           setting that filtered out an existing
 *                                           event, and that event was then shown
 *                                           by having the filter removed. Also
 *                                           fixed bug causing exception when
 *                                           switching back to a single selected
 *                                           time in the timeline. Finally, fixed
 *                                           bug causing exceptions when right-
 *                                           clicking on the table column headers
 *                                           in certain cases if the table's
 *                                           horizontal scrollbar was showing and
 *                                           scrolled over at least partway to the
 *                                           right.
 * Jan 14, 2014    2704    Chris.Golden      Removed sort-direction-arrow image from
 *                                           the header of the column that is the
 *                                           sort column, since these are now pro-
 *                                           vided by SWT column headers. Also
 *                                           adjusted timeline widget and time scale
 *                                           widgets positioning to be more appro-
 *                                           priate for different table font sizes.
 *                                           Fixed bug that caused timeline to
 *                                           rapidly shift its viewport left or
 *                                           right when the selected time was
 *                                           dragged close to the left or right edge
 *                                           respectively. Finally, fixed bug that
 *                                           caused a null pointer exception when
 *                                           the area just outside the timeline, but
 *                                           within its enclosing column header, was
 *                                           right-clicked.
 * Jan 27, 2014    2155    Chris.Golden      Fixed bug that intermittently occurred
 *                                           because an asynchronous execution of
 *                                           code that expected non-disposed widgets
 *                                           encountered widgets that had been dis-
 *                                           posed between the scheduling and run-
 *                                           ning of the aysnchronous code.
 * Jan 31, 2014    2710    Chris.Golden      Fixed bug that caused exceptions to be
 *                                           thrown because the minimum interval
 *                                           between the start and end time of a
 *                                           hazard here was 0, whereas it was a
 *                                           minute in the HID. Also added use of a
 *                                           constant in HazardConstants to ensure
 *                                           that all time range widgets use the same
 *                                           minimum interval.
 * Feb 14, 2014    2161    Chris.Golden      Added "Until Further Notice" option for
 *                                           end times for event time range widgets
 *                                           and right-click context-sensitive menu. 
 *                                           Also fixed Javadoc comments, and added
 *                                           usage of JDK 1.7 features.
 * Jun 23, 2014   4010     Chris.Golden      Changed to work with megawidget manager
 *                                           changes.
 * Jun 30, 2014   3512     Chris.Golden      Changed to work with more megawidget
 *                                           manager changes.
 * Jul 03, 2014   3512     Chris.Golden      Changed to add locking of the intervals
 *                                           between start and end times in the
 *                                           scale widgets for hazard events that
 *                                           have duration options and that do not
 *                                           have their end times set to "until
 *                                           further notice."
 * Sep 11, 2014   1283     Robert.Blum       Changed out the ToolTip with a custom one
 *                                           that displays on the correct monitor.
 * Sep 12, 2014   3511     Robert.Blum       Changed the format/timezone of the 
 *                                           Console time to match CAVE time.
 * Nov 18, 2014   4124     Chris.Golden      Changed to only show one of the selected
 *                                           time options at once (either a single
 *                                           selected time instant, or a selected
 *                                           time range).
 * Dec 05, 2014   4124     Chris.Golden      Fixed sorting bugs. Also added multi-level
 *                                           sorting (column header menus limit it to
 *                                           two levels, primary and secondary sort,
 *                                           for now, but it could be expanded by simply
 *                                           expanding the menus).
 * Dec 13, 2014   4959     Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan 08, 2015   2394     Chris.Golden      Fixed bug that caused column cell values
 *                                           that were not strings or dates to throw an
 *                                           exception. Also changed number-populated
 *                                           columns to be right-adjusted.
 * Feb 09, 2015   6370     Chris.Golden      Fixed problem with header menu in Console
 *                                           not always showing columns that are
 *                                           available in the current settings if the
 *                                           latter has been changed.
 * Feb 09, 2015   2331     Chris.Golden      Changed current time marker to always show
 *                                           the current minute, instead of rounding up
 *                                           to next minute when the current time was
 *                                           30 seconds or more into a minute. Also
 *                                           changed to use time range boundaries for
 *                                           the events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class TemporalDisplay {

    // Public Static Constants

    /**
     * Selected time mode tooltip text.
     */
    public static final String SELECTED_TIME_MODE_TEXT = "Selected Time Mode";

    /**
     * Single selected time mode.
     */
    public static final String SELECTED_TIME_MODE_SINGLE = "Single";

    /**
     * Range selected time mode.
     */
    public static final String SELECTED_TIME_MODE_RANGE = "Range";

    /**
     * Text to show in date-time cells holding the "until further notice" value.
     */
    public static final String UNTIL_FURTHER_NOTICE_COLUMN_TEXT = "until further notice";

    /**
     * Text to show in the event-specific context-sensitive menu to provide the
     * "until further notice" toggle option.
     */
    public static final String UNTIL_FURTHER_NOTICE_MENU_TEXT = "Until Further Notice";

    /**
     * Selected time mode choices.
     */
    public static final ImmutableList<String> SELECTED_TIME_MODE_CHOICES = ImmutableList
            .of(SELECTED_TIME_MODE_SINGLE, SELECTED_TIME_MODE_RANGE);

    /**
     * Unchecked menu item image file name.
     */
    public static final String UNCHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemUnchecked.png";

    /**
     * Semi-checked menu item image file name.
     */
    public static final String SEMI_CHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemSemiChecked.png";

    /**
     * Checked menu item image file name.
     */
    public static final String CHECKED_MENU_ITEM_IMAGE_FILE_NAME = "menuItemChecked.png";

    /**
     * Toolbar button icon image file names.
     */
    public static final ImmutableList<String> TOOLBAR_BUTTON_IMAGE_FILE_NAMES = ImmutableList
            .of("timeZoomOut.png", "timeJumpBackward.png", "timeBackward.png",
                    "timeCurrent.png", "timeForward.png",
                    "timeJumpForward.png", "timeZoomIn.png");

    /**
     * Descriptions of the toolbar buttons, each of which corresponds to the
     * file name of the button at the same index in
     * {@link #TOOLBAR_BUTTON_IMAGE_FILE_NAMES}.
     */
    public static final ImmutableList<String> TOOLBAR_BUTTON_DESCRIPTIONS = ImmutableList
            .of("Zoom Out Timeline", "Page Back Timeline", "Pan Back Timeline",
                    "Show Current Time", "Pan Forward Timeline",
                    "Page Forward Timeline", "Zoom In Timeline");

    /**
     * Zoom out button identifier.
     */
    public static final String BUTTON_ZOOM_OUT = "zoomOut";

    /**
     * Pan back one day button identifier.
     */
    public static final String BUTTON_PAGE_BACKWARD = "backwardDay";

    /**
     * Pan back button identifier.
     */
    public static final String BUTTON_PAN_BACKWARD = "backward";

    /**
     * Center on current time button identifier.
     */
    public static final String BUTTON_CURRENT_TIME = "currentTime";

    /**
     * Pan forward button identifier.
     */
    public static final String BUTTON_PAN_FORWARD = "forward";

    /**
     * Pan forward one day button identifier.
     */
    public static final String BUTTON_PAGE_FORWARD = "forwardDay";

    /**
     * Zoom in button identifier.
     */
    public static final String BUTTON_ZOOM_IN = "zoomIn";

    /**
     * List of button image names, each of which is also the name of the image
     * file (without its type specifier suffix), for the buttons.
     */
    public static final ImmutableList<String> BUTTON_IMAGE_NAMES = ImmutableList
            .of(BUTTON_ZOOM_OUT, BUTTON_PAGE_BACKWARD, BUTTON_PAN_BACKWARD,
                    BUTTON_CURRENT_TIME, BUTTON_PAN_FORWARD,
                    BUTTON_PAGE_FORWARD, BUTTON_ZOOM_IN);

    /**
     * Descriptions of the buttons, each of which corresponds to the button at
     * the same index in {@link #BUTTON_IMAGE_NAMES}.
     */
    public static final ImmutableList<String> BUTTON_DESCRIPTIONS = ImmutableList
            .of("Zoom Out", "Page Back", "Pan Back", "Show Current Time",
                    "Pan Forward", "Page Forward", "Zoom In");

    // Private Static Constants

    /**
     * PNG file name suffix.
     */
    private static final String PNG_FILE_NAME_SUFFIX = ".png";

    /**
     * Date-time format string.
     */
    private static final String DATE_TIME_FORMAT_STRING = "HH:mm'Z' dd-MMM-yy";

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
     * Text displayed in the column header for the time scale widgets.
     */
    private static final String TIME_SCALE_COLUMN_NAME = "Time Scale";

    /**
     * Show time under mouse toggle menu text.
     */
    private static final String SHOW_TIME_UNDER_MOUSE_TOGGLE_MENU_TEXT = "Show Time Under Mouse";

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
     * Width in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_WIDTH = 2;

    /**
     * Height in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_HEIGHT = 1;

    /**
     * Left padding of table cell in which scale widgets are placed. Where this
     * value comes from is unknown; hopefully it will not change with shifts in
     * window managers, etc.
     */
    private static final int CELL_PADDING_LEFT = 3;

    /**
     * Width of horizontal padding in pixels to the left and right of time
     * widgets (both ruler and scales).
     */
    private static final int TIME_HORIZONTAL_PADDING = 10;

    /**
     * The default height of the button panel.
     */
    private static final int BUTTON_PANEL_HEIGHT = 40;

    /**
     * Number of milliseconds in a minute.
     */
    private static final long MINUTE_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    /**
     * Number of milliseconds in an hour.
     */
    private static final long HOUR_INTERVAL = TimeUnit.HOURS.toMillis(1);

    /**
     * Number of milliseconds in a day.
     */
    private static final long DAY_INTERVAL = TimeUnit.DAYS.toMillis(1);

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
     * Minimum visible time range as an epoch time delta in milliseconds.
     */
    private static final long MIN_VISIBLE_TIME_RANGE = 2L * HOUR_INTERVAL;

    /**
     * Maximum visible time range as an epoch time delta in milliseconds.
     */
    private static final long MAX_VISIBLE_TIME_RANGE = 8L * DAY_INTERVAL;

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(TemporalDisplay.class);

    /**
     * Empty text representation in hazard event table cell.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Key into filter megawidget definition used to find the column name with
     * which the megawidget should be associated.
     */
    private static final String COLUMN_NAME = "columnName";

    // Private Enumerated Types

    /**
     * Sort direction.
     */
    private enum SortDirection {

        // Values

        ASCENDING("Ascending"), DESCENDING("Descending");

        // Private Variables

        /**
         * Description.
         */
        private final String description;

        // Private Constructors

        /**
         * Construct a standard instance.
         */
        private SortDirection(String description) {
            this.description = description;
        }

        // Public Methods

        /**
         * Get a string description of this object.
         * 
         * @return String description.
         */
        @Override
        public String toString() {
            return description;
        }
    };

    /**
     * Settings changes.
     */
    private enum SettingsChange {
        COLUMNS, VISIBLE_COLUMNS
    }

    // Private Classes

    /**
     * Encapsulation of a sort that may be performed, including the name of the
     * column by which one would sort, the priority of this sort is compared to
     * others, and the direction in which to sort. They are comparable by said
     * priority.
     */
    private class Sort implements Comparable<Sort> {

        // Private Constants

        /**
         * Name of column by which to sort.
         */
        private final String columnName;

        /**
         * Sort direction to be used.
         */
        private final SortDirection sortDirection;

        /**
         * Priority as compared to other instances.
         */
        private final int priority;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param columnName
         *            Name of column by which to sort.
         * @param sortDirection
         *            Direction in which to sort.
         */
        public Sort(String columnName, SortDirection sortDirection, int priority) {
            this.columnName = columnName;
            this.sortDirection = sortDirection;
            this.priority = priority;
        }

        // Public Methods

        /**
         * Get the name of the column by which to sort.
         * 
         * @return Name.
         */
        public String getColumnName() {
            return columnName;
        }

        /**
         * Get the direction in which to sort.
         * 
         * @return Sort direction.
         */
        public SortDirection getSortDirection() {
            return sortDirection;
        }

        /**
         * Get the priority of this instance as compared to others.
         * 
         * @return Priority.
         */
        public int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(Sort o) {
            return priority - o.priority;
        }
    }

    // Private Constants

    /**
     * Ruler border color; this is instance- rather than class-scoped so that it
     * may be disposed of when the widget is disposed.
     */
    private final Color RULER_BORDER_COLOR = new Color(Display.getCurrent(),
            131, 120, 103);

    /**
     * Unchecked menu item image.
     */
    private final Image UNCHECKED_MENU_ITEM_IMAGE = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            UNCHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Semi-checked menu item image.
     */
    private final Image SEMI_CHECKED_MENU_ITEM_IMAGE = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            SEMI_CHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    /**
     * Checked menu item image.
     */
    private final Image CHECKED_MENU_ITEM_IMAGE = IconUtil.getImage(
            HazardServicesActivator.getDefault().getBundle(),
            CHECKED_MENU_ITEM_IMAGE_FILE_NAME, Display.getCurrent());

    // Private Variables

    /**
     * Table tool tip, used to display hint text for table cells.
     */
    private CustomToolTip tableToolTip = null;

    /**
     * Flag indicating whether or not the timeline ruler should display tooltips
     * for all times along its length.
     */
    private boolean showRulerToolTipsForAllTimes = true;

    /**
     * Spacer image, used to ensure that column headers are tall enough to
     * handle the time ruler embedded in the last column header.
     */
    private Image spacerImage = null;

    /**
     * Set of basic resources created for use in this window, to be disposed of
     * when this window is disposed of.
     */
    private final Set<Resource> resources = Sets.newHashSet(RULER_BORDER_COLOR,
            UNCHECKED_MENU_ITEM_IMAGE, SEMI_CHECKED_MENU_ITEM_IMAGE,
            CHECKED_MENU_ITEM_IMAGE);

    /**
     * Map of RGB triplets, each consisting of a string of three integers each
     * separated from the next by a space, to the corresponding colors created
     * for use as visual time range indicators. .
     */
    private final Map<String, Color> timeRangeColorsForRGBs = new HashMap<>();

    /**
     * JSON-encoded string holding a list of maps, each of the latter being a
     * filter megawidget specifier to be shown in column header menus.
     */
    private String filterMegawidgets = null;

    /**
     * Selected time mode combo box.
     */
    private Combo selectedTimeModeCombo = null;

    /**
     * Current time color.
     */
    private Color currentTimeColor = null;

    /**
     * Selected time color.
     */
    private Color selectedTimeColor = null;

    /**
     * Time range (along the ruler) edge color.
     */
    private Color timeRangeEdgeColor = null;

    /**
     * Time range (along the ruler) fill color.
     */
    private Color timeRangeFillColor = null;

    /**
     * Map of button identifiers to the associated navigation buttons.
     */
    private final Map<String, Button> buttonsForIdentifiers = new HashMap<>();

    /**
     * Map of button identifiers to the associated toolbar navigation actions.
     */
    private final Map<String, Action> actionsForButtonIdentifiers = new HashMap<>();

    /**
     * Selected time mode action, built for the toolbar and passed to this
     * object if appropriate.
     */
    private ComboAction selectedTimeModeAction = null;

    /**
     * Time line ruler widget.
     */
    private MultiValueRuler ruler = null;

    /**
     * Selected time mode.
     */
    private String selectedTimeMode = SELECTED_TIME_MODE_SINGLE;

    /**
     * Date used for generating date strings.
     */
    private final Date date = new Date();

    /**
     * Date formatter for date-time strings.
     */
    private SimpleDateFormat dateTimeFormatter = null;

    /**
     * Current time as epoch time in milliseconds.
     */
    private long currentTime;

    /**
     * Start time of the selected time range, as epoch time in milliseconds.
     */
    private long selectedTimeStart;

    /**
     * End time of the selected time range, as epoch time in milliseconds. If
     * the mode is "single" and not "range", this will be equal to
     * {@link #selectedTimeStart}.
     */
    private long selectedTimeEnd;

    /**
     * Delta between the lower and upper bounds of the selected time range, as
     * recorded before the last switch was made to single-selected-time mode.
     */
    private long lastSelectedTimeRangeDelta = 0L;

    /**
     * Amount of time visible at once in the time line as an epoch time range in
     * milliseconds.
     */
    private long visibleTimeRange;

    /**
     * Flag indicating whether the zoom level of the time line is currently odd
     * or not. (Zoom levels are designated "even" and "odd" as shorthand for the
     * different zoom factors that are applied to them when zooming in or out
     * from a particular level.)
     */
    private boolean zoomLevelIsOdd = false;

    /**
     * List of identifiers of hazard events used to populate the table.
     */
    private final List<String> eventIdentifiers;

    /**
     * Map of hazard event identifiers to dictionaries providing the hazard
     * events themselves. All keys are found in {@link #eventIdentifiers}.
     */
    private final Map<String, Dict> dictsForEventIdentifiers;

    /**
     * Map of hazard event identifiers to allowable start time ranges.
     */
    private Map<String, Range<Long>> startTimeBoundariesForEventIds;

    /**
     * Map of hazard event identifiers to allowable end time ranges.
     */
    private Map<String, Range<Long>> endTimeBoundariesForEventIds;

    /**
     * Indices of items that are currently selected.
     */
    private int[] selectedIndices = null;

    /**
     * Timestamp (epoch time in milliseconds) of the last mouse or other event
     * that occurred that should be ignored for the purposes of changing the
     * table selection.
     */
    private long lastIgnorableEventTime = 0L;

    /**
     * The number of rows in this table.
     */
    private int numberOfRows;

    /**
     * The table which contains all of the event related columns and the event
     * scale bars.
     */
    private Table table;

    /**
     * Separator label between the table and the controls below it, if any.
     */
    private Label separator;

    /**
     * Panel which contains the time control buttons.
     */
    private Composite buttonsPanel;

    /**
     * The number of pixels between the time ruler and the top of the parent
     * composite.
     */
    private int rulerTopOffset;

    /**
     * Map of event identifiers to table editors holding the time scales
     * associated with the events.
     */
    private final Map<String, TableEditor> tableEditorsForIdentifiers;

    /**
     * The panel in which the table is constructed.
     */
    private Composite temporalDisplayPanel;

    /**
     * The panel which contains the selected time mode combo button.
     */
    private Composite comboBoxPanel;

    /**
     * Map pairing column names with context-sensitive menus to be popped up
     * over the columns' headers.
     */
    private Map<String, Menu> headerMenusForColumnNames;

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
     * Map pairing column names with megawidget managers for those columns that
     * have megawidget-bearing context-sensitive menus associated with their
     * headers.
     */
    private Map<String, MegawidgetManager> headerMegawidgetManagersForColumnNames;

    /**
     * the current dynamic settings
     */
    private ObservedSettings currentSettings;

    /**
     * List of visible column names. The order of this list is the order in
     * which the columns appear in the table, meaning that when the
     * {@link Table#getColumnOrder()} method is called, the name of the column
     * found in the table at the index specified by the returned array's Nth
     * item will be the same as that found as the Nth item in this list.
     */
    private List<String> visibleColumnNames;

    /**
     * Map pairing visible column names that may need to display hint text as
     * tooltips with the hazard event parameter identifiers indicating what hint
     * text should be displayed.
     */
    private final Map<String, String> hintTextIdentifiersForVisibleColumnNames;

    /**
     * Map pairing visible column names that hold dates as their cell data to
     * the hazard event parameter identifiers indicating the hazard event
     * parameter identifiers that hold their date values.
     */
    private final Map<String, String> dateIdentifiersForVisibleColumnNames;

    /**
     * Countdown timer display manager.
     */
    private ConsoleCountdownTimersDisplayManager countdownTimersDisplayManager = null;

    /**
     * Countdown timer display listener, a listener for notifications that the
     * countdown timer displays need updating.
     */
    private final CountdownTimersDisplayListener countdownTimersDisplayListener = new CountdownTimersDisplayListener() {
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
     * Display properties for the countdown timer table cell that has
     * experienced the {@link SWT#EraseItem} event but not the
     * {@link SWT#PaintItem}. For each cell, the latter event immediately
     * follows the former, before any other cell is erased or painted, so this
     * reference is merely used to save the display properties for a cell in the
     * very short time delta between the erasing and painting of said cell. It
     * is never used beyond this.
     */
    private ConsoleCountdownTimerDisplayProperties countdownTimerDisplayProperties = null;

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
     * Last recorded width of the column holding the countdown timers; this is
     * merely the last width given by the {@link SWT#EraseItem} event when the
     * column in question was the countdown timer column.
     */
    private int countdownTimerColumnWidth;

    /**
     * Map of column identifiers to the corresponding column names.
     */
    private final Map<String, String> columnNamesForIdentifiers;

    /**
     * Map of column names to definitions of the corresponding columns. Each
     * definition is a dictionary of key-value pairs defining that column.
     */
    private Map<String, Column> columnsForNames;

    /**
     * Map of column names to the comparators to be used when sorting by the
     * columns. If the comparator for a particular column is <code>null</code>,
     * it is a countdown timer column.
     */
    private final Map<String, Comparator<?>> comparatorsForNames = new HashMap<>();

    /**
     * Flag indicating whether or not column move events should be ignored.
     */
    private boolean ignoreMove = false;

    /**
     * Flag indicating whether or not column resize events should be ignored.
     */
    private boolean ignoreResize = false;

    /**
     * Flag indicating whether or not a refitting of the timeline ruler its
     * column header is scheduled to occur.
     */
    private boolean willRefitRulerToColumn = false;

    /**
     * Presenter managing the view of which this display is a part.
     */
    private ConsolePresenter presenter = null;

    /**
     * Set of identifiers for hazard events that allow "until further notice".
     * This will be kept up to date elsewhere, and is unmodifiable by the
     * temporal display; it is read-only.
     */
    private Set<String> eventIdentifiersAllowingUntilFurtherNotice;

    /**
     * Flag indicating whether or not a notification of dynamic setting
     * modification is scheduled to occur.
     */
    private boolean willNotifyOfSettingChange = false;

    /**
     * JSON converter.
     */
    private final JSONConverter jsonConverter = new JSONConverter();

    /**
     * List, in the order in which to apply them, of sorts that are currently in
     * effect.
     */
    private final List<Sort> sorts = new ArrayList<>();

    /**
     * Multi-value linear control listener for thumb movements on the hazard
     * event time scales.
     */
    private final IMultiValueLinearControlListener timeScaleListener = new IMultiValueLinearControlListener() {
        @Override
        public void visibleValueRangeChanged(MultiValueLinearControl widget,
                long lowerValue, long upperValue, ChangeSource source) {

            // No action.
        }

        @Override
        public void constrainedThumbValuesChanged(
                MultiValueLinearControl widget, long[] values,
                ChangeSource source) {
            if ((source == MultiValueScale.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                    || (source == MultiValueScale.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {

                // Get the table item that goes with this scale
                // widget, and from it, get the event identifier
                // that it represents.
                TableItem item = (TableItem) widget.getData();
                String eventID = (String) item.getData();

                // Save the new start and end times in the event
                // dictionary.
                for (Dict eventDict : dictsForEventIdentifiers.values()) {
                    if (eventDict.get(HAZARD_EVENT_IDENTIFIER).equals(eventID)) {
                        eventDict.put(HAZARD_EVENT_START_TIME, values[0]);
                        eventDict.put(HAZARD_EVENT_END_TIME, values[1]);
                        break;
                    }
                }

                // Change the start and end time text in the table
                // row, if the columns are showing.
                String[] columnIdentifiers = { HAZARD_EVENT_START_TIME,
                        HAZARD_EVENT_END_TIME };
                for (int j = 0; j < columnIdentifiers.length; j++) {
                    String columnName = columnNamesForIdentifiers
                            .get(columnIdentifiers[j]);
                    int columnIndex = getIndexOfColumnInTable(columnName);
                    if (columnIndex != -1) {
                        updateCell(columnIndex,
                                columnsForNames.get(columnName), values[j],
                                item);
                    }
                }

                // Notify listeners of the change.
                fireConsoleActionOccurred(new ConsoleAction(
                        ConsoleAction.ActionType.EVENT_TIME_RANGE_CHANGED,
                        eventID, Long.toString(values[0]),
                        Long.toString(values[1])));
            }
        }

        @Override
        public void freeThumbValuesChanged(MultiValueLinearControl widget,
                long[] values, ChangeSource source) {

            // No action.
        }
    };

    /**
     * Snap value calculator, used to generate snap-to values for the hazard
     * event time scales and the time line ruler.
     */
    private final ISnapValueCalculator snapValueCalculator = new ISnapValueCalculator() {
        private final long INTERVAL = MINUTE_INTERVAL;

        private final long HALF_INTERVAL = INTERVAL / 2L;

        @Override
        public long getSnapThumbValue(long value, long minimum, long maximum) {
            long remainder = value % INTERVAL;
            if (remainder < HALF_INTERVAL) {
                value -= remainder;
            } else {
                value += INTERVAL - remainder;
            }
            if (value < minimum) {
                value += INTERVAL
                        * (((minimum - value) / INTERVAL) + ((minimum - value)
                                % INTERVAL == 0 ? 0L : 1L));
            } else if (value > maximum) {
                value -= INTERVAL
                        * (((value - maximum) / INTERVAL) + ((value - maximum)
                                % INTERVAL == 0 ? 0L : 1L));
            }
            return value;
        }
    };

    /**
     * Thumb tooltip text provider for the time line ruler.
     */
    private final IMultiValueTooltipTextProvider thumbTooltipTextProvider = new IMultiValueTooltipTextProvider() {
        private final String[] SELECTED_TIME_TEXT = { "Selected Time:", null };

        private final String[] TIME_RANGE_START_TEXT = { "Time Range Start:",
                null };

        private final String[] TIME_RANGE_END_TEXT = { "Time Range End:", null };

        private final String[] START_TIME_TEXT = { "Event Start Time:", null };

        private final String[] END_TIME_TEXT = { "Event End Time:", null };

        private final String[] OTHER_VALUE_TEXT = { null };

        @Override
        public String[] getTooltipTextForValue(MultiValueLinearControl widget,
                long value) {
            if ((widget == ruler) && showRulerToolTipsForAllTimes) {
                OTHER_VALUE_TEXT[0] = getDateTimeString(value);
                return OTHER_VALUE_TEXT;
            } else {
                return null;
            }
        }

        @Override
        public String[] getTooltipTextForConstrainedThumb(
                MultiValueLinearControl widget, int index, long value) {
            String[] text = (widget == ruler ? (index == 0 ? TIME_RANGE_START_TEXT
                    : TIME_RANGE_END_TEXT)
                    : (index == 0 ? START_TIME_TEXT : END_TIME_TEXT));
            text[1] = getDateTimeString(value);
            return text;
        }

        @Override
        public String[] getTooltipTextForFreeThumb(
                MultiValueLinearControl widget, int index, long value) {
            SELECTED_TIME_TEXT[1] = getDateTimeString(value);
            return SELECTED_TIME_TEXT;
        }
    };

    /**
     * Mouse wheel filter, used to scroll the table by one line up or down.
     */
    private final Listener mouseWheelFilter = new Listener() {
        @Override
        public void handleEvent(Event event) {

            // If the widget receiving the event is one of the
            // time scales in the table, or is the table itself,
            // scroll the table up or down as appropriate, and
            // ensure that the widget will not handle the event
            // itself by canceling the latter.
            if ((event.widget == table)
                    || (event.widget instanceof MultiValueScale)) {

                // Set the table top visible index to be the next
                // or previous one from the current one. If the
                // topmost index is now 0, set the vertical
                // scrollbar to show the topmost pixel, since the
                // setTopIndex() method does not scroll to the
                // very top if only part of the topmost row is
                // showing.
                int delta = (event.count < 0 ? 1 : -1);
                table.setTopIndex(table.getTopIndex() + delta);
                if ((delta == -1) && (table.getTopIndex() == 0)) {
                    table.getVerticalBar().setSelection(0);
                }

                // Do not allow further processing of this event.
                event.doit = false;
            }
        }
    };

    /**
     * Runnable that ensures that the last column in the table is the time scale
     * column. This is scheduled to run asynchronously when a column is detected
     * to have been moved to the end of the table, past the time scale column.
     */
    private final Runnable ensureTimeScaleIsLastColumnAction = new Runnable() {
        @Override
        public void run() {
            if ((table != null) && (table.isDisposed() == false)) {

                // If the column order is wrong, move the
                // time scale column to the end.
                int[] columnOrder = table.getColumnOrder();
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
                    table.setColumnOrder(columnOrder);
                }

                // Handle the reordering of the columns.
                handleColumnReorderingViaDrag(false);

                // Turn on redraw for the table, since it was
                // turned off by the handler that scheduled
                // the invocation of this runnable.
                table.setRedraw(true);
            }
        }
    };

    /**
     * Time scale columm width before resize of column started.
     */
    private int timeScaleColumnWidthBeforeResize = -1;

    /**
     * Control listener that responds to resizes and moves of table columns.
     */
    private final ControlListener columnControlListener = new ControlListener() {
        @Override
        public void controlResized(ControlEvent e) {

            // Do nothing if resizes are to be ignored.
            if (!ignoreResize) {
                ignoreResize = true;

                // Handle the resize one way if this is the
                // time scale column, and another way otherwise.
                final TableColumn column = (TableColumn) e.getSource();
                if (column.getText().equals(TIME_SCALE_COLUMN_NAME)) {

                    // If the ruler is displaying, resize it
                    // appropriately.
                    boolean rescheduled = false;
                    if ((ruler != null) && !ruler.isDisposed()
                            && ruler.isVisible()) {

                        // Get the table bounds for calculations
                        // to be made as to the size and position
                        // of the timeline ruler. Since the width
                        // is sometimes given as 0, schedule an-
                        // other resize event to fire off later
                        // in this case. If this is not done,
                        // the timeline ruler is given the wrong
                        // bounds.
                        Rectangle tableBounds = table.getBounds();
                        if (tableBounds.width == 0) {
                            Display.getCurrent().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    visibleColumnCountChanged();
                                }
                            });
                            rescheduled = true;
                        } else {
                            fitRulerToColumn(column);
                        }
                    }

                    // If the second to last column is being re-
                    // sized, then the last time scale column
                    // width will have been recorded; if this is
                    // the case, then schedule a proportional
                    // resize of the other columns to occur after
                    // this event is processed, so that the delta
                    // representing the change in the time scale
                    // column's width may be shared out to the
                    // other columns.
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

                    // If the second-to-last column is being re-
                    // sized, then record the time scale column's
                    // current width, so that its width change
                    // may be calculated subsequently, and the
                    // delta shared out among the other columns.
                    // Otherwise, do not record the time scale
                    // column's width, and update the settings
                    // definition to include the new column
                    // widths.
                    if ((visibleColumnNames.size() > 0)
                            && (visibleColumnNames.get(visibleColumnNames
                                    .size() - 1).equals(column.getText()))) {
                        if (timeScaleColumnWidthBeforeResize == -1) {
                            for (TableColumn otherColumn : table.getColumns()) {
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
                        updateTableColumnWidthInSettingDefinition(column);
                    }
                }
                ignoreResize = false;
            }
        }

        @Override
        public void controlMoved(ControlEvent e) {

            // Only react if this move should not be ignored.
            if (!ignoreMove) {

                // Ensure that if a columnn was dragged beyond
                // the last column (the one holding the time
                // scales), a runnable is scheduled to be exe-
                // cuted later in order to reposition the
                // dragged column before the latter, so as to
                // always leave the time scales in the last
                // column. Display.asyncExec(), when executed
                // from within the UI thread, places the run-
                // nable on the queue to be executed when all
                // outstanding requests have been handled by
                // SWT. This is important because attempting
                // to call Table.setColumnOrder() from within
                // this handler (i.e. changing the column
                // order while responding to a change in the
                // column order) appears to have catastrophic
                // results, including occasional GTK crashes
                // that bring down the JVM! If the columns
                // are not in the wrong order, handle the new
                // ordering.
                int[] columnOrder = table.getColumnOrder();
                if (columnOrder[columnOrder.length - 1] != columnOrder.length - 1) {
                    table.setRedraw(false);
                    Display.getCurrent().asyncExec(
                            ensureTimeScaleIsLastColumnAction);
                } else {
                    handleColumnReorderingViaDrag(true);
                }
            }
        }
    };

    /**
     * Header menu item selection listener, for toggling column visibility.
     */
    private final SelectionListener headerMenuListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {

            // Prepare for the addition or removal of a column.
            boolean lastIgnoreResize = ignoreResize;
            boolean lastIgnoreMove = ignoreMove;
            ignoreResize = true;
            ignoreMove = true;
            visibleColumnCountChanging();

            // Determine which column is to be toggled on or off,
            // and whether it is to be added or removed, and then
            // perform the addition or removal.
            String columnName = ((MenuItem) e.widget).getText();
            TableColumn[] tableColumns = table.getColumns();
            if (visibleColumnNames.contains(columnName)) {

                // Remove the column from the table, and remove
                // its name from the visible columns list, as
                // well as the hint text columns set if it is
                // there.
                for (int j = 0; j < tableColumns.length; ++j) {
                    if (tableColumns[j].getText().equals(columnName)) {
                        tableColumns[j].dispose();
                        break;
                    }
                }
                visibleColumnNames.remove(columnName);
                hintTextIdentifiersForVisibleColumnNames.remove(columnName);
                dateIdentifiersForVisibleColumnNames.remove(columnName);
                if (columnName.equals(countdownTimerColumnName)) {
                    countdownTimerColumnIndex = -1;
                }

                // Ensure that the checkboxes in the table's
                // rows are in the leftmost column.
                scheduleEnsureCheckboxesAreInLeftmostColumn(false);
            } else {

                // Create the column and place it at the end,
                // just before the time scale column; then add
                // its name to the visible columns list.
                createTableColumn(columnName, table.getColumnCount() - 1);
                visibleColumnNames.add(columnName);
                determineSpecialPropertiesOfColumn(columnName);

                // Fill in the text for the cells in the table's
                // rows that fall within the newly created column.
                updateCellsForColumn(columnName);
            }

            // Move the table editors over to the appropriate
            // column.
            for (TableItem item : table.getItems()) {
                TableEditor scaleEditor = tableEditorsForIdentifiers.get(item
                        .getData());
                scaleEditor.setEditor(scaleEditor.getEditor(), item,
                        table.getColumnCount() - 1);
            }

            // Update the column order to match that given by
            // the visible columns list.
            updateColumnOrder();

            // Finish up following the addition or removal of
            // a column.
            ignoreResize = lastIgnoreResize;
            ignoreMove = lastIgnoreMove;
            visibleColumnCountChanged();

            // Notify listeners of the setting change.
            scheduleNotificationOfSettingDefinitionChange(SettingsChange.VISIBLE_COLUMNS);
        }
    };

    /**
     * Sort menu item selection listener, for changing sort characteristics.
     */
    private final SelectionListener sortMenuListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {

            /*
             * Determine whether this is a primary or secondary sort
             * configuration, and using which column and in what direction is
             * sorting to occur.
             */
            Menu columnLevelMenu = ((MenuItem) e.widget).getParent();
            Menu sortLevelMenu = columnLevelMenu.getParentItem().getParent();
            int sortIndex = (sortLevelMenu.getParentItem().getText()
                    .equals(TemporalDisplay.PRIMARY_SORT_MENU_NAME) ? 0 : 1);
            String columnName = ((MenuItem) e.widget).getParent()
                    .getParentItem().getText();
            SortDirection sortDirection = (SortDirection) e.widget.getData();

            /*
             * Set the flag indicating whether or not the table will have to
             * have its visual cues related to sorting altered; this should only
             * happen if primary sort is changing.
             */
            boolean changeTableSortCues = ((sortIndex == 0) || sorts.isEmpty());

            /*
             * If replacing a sort, do so; otherwise, add the sort. In the
             * latter case, if a secondary sort was chosen but there is not a
             * primary sort, set both sorts to be the same.
             */
            boolean directionChanged = false;
            if (sortIndex < sorts.size()) {
                Sort oldSort = sorts.remove(sortIndex);
                Sort newSort = new Sort(columnName, sortDirection,
                        sortIndex + 1);
                sorts.add(sortIndex, newSort);
                directionChanged = (oldSort.getColumnName().equals(columnName) && (oldSort
                        .getSortDirection() != sortDirection));
            } else {
                for (int j = sorts.size(); j <= sortIndex; j++) {
                    sorts.add(new Sort(columnName, sortDirection, j + 1));
                }
            }

            /*
             * Show the table sort cues if necessary.
             */
            if (changeTableSortCues) {
                if (directionChanged == false) {
                    for (TableColumn column : table.getColumns()) {
                        if (columnName.equals(column.getText())) {
                            table.setSortColumn(column);
                            break;
                        }
                    }
                }
                table.setSortDirection(sortDirection == SortDirection.ASCENDING ? SWT.UP
                        : SWT.DOWN);
            }

            /*
             * Perform the sort.
             */
            sortEventData(directionChanged);

            /*
             * Update the sort column information in the settings.
             */
            updateTableSortColumnInSettingDefinition();
        }
    };

    /**
     * Row menu item selection listener, handling actions to be performed on a
     * single .
     */
    private final SelectionListener rowMenuListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {

            // If the menu item that was invoked was the until further notice
            // pseudo-checkbox, respond accordingly.
            if (untilFurtherNoticeMenuItem == e.widget) {

                // Determine whether until further notice has been toggled
                // on or off. Since the menu item is not actually a checkbox,
                // but rather a push button masquerading as a tri-state
                // checkbox, use the icon it was displaying to determine
                // whether the new toggle state is on or off.
                boolean newUntilFurtherNotice = (untilFurtherNoticeMenuItem
                        .getImage() == UNCHECKED_MENU_ITEM_IMAGE);

                // Iterate through the selected hazard events, ensuring that
                // each in turn has the correct until further notice state.
                TableItem[] items = table.getItems();
                for (int index : selectedIndices) {

                    // Do nothing unless the old until further notice state
                    // of this hazard event is not the same as the new state.
                    String identifier = (String) items[index].getData();
                    Dict eventDict = dictsForEventIdentifiers.get(identifier);
                    boolean oldUntilFurtherNotice = (eventDict
                            .containsKey(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE) ? (Boolean) eventDict
                            .get(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)
                            : false);
                    if (oldUntilFurtherNotice != newUntilFurtherNotice) {

                        // Set the end time thumb in the time scale widget
                        // to be read-only.
                        MultiValueScale scale = (MultiValueScale) tableEditorsForIdentifiers
                                .get(identifier).getEditor();
                        eventDict.put(
                                HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                                newUntilFurtherNotice);
                        configureScaleIntervalLockingForEvent(scale, eventDict);

                        // Notify the model of the change.
                        fireConsoleActionOccurred(new ConsoleAction(
                                ConsoleAction.ActionType.EVENT_END_TIME_UNTIL_FURTHER_NOTICE_CHANGED,
                                identifier, newUntilFurtherNotice));
                    }
                }
            }
        }
    };

    /**
     * Sort listener, used to listen for column-based sorts.
     */
    private final SelectionListener sortListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {

            /*
             * If the column that will be the sorting column is the same as the
             * previous one, and it is the only sorting column, then toggle the
             * sorting direction; otherwise, start with an upward sort.
             */
            TableColumn sortColumn = (TableColumn) e.widget;
            String sortName = sortColumn.getText();
            boolean directionChanged = false;
            if ((sorts.size() == 1)
                    && sortName.equals(sorts.get(0).getColumnName())) {
                directionChanged = true;
                Sort oldSort = sorts.remove(0);
                table.setSortDirection(oldSort.getSortDirection() == SortDirection.ASCENDING ? SWT.DOWN
                        : SWT.UP);
                sorts.add(
                        0,
                        new Sort(
                                oldSort.getColumnName(),
                                (oldSort.getSortDirection() == SortDirection.ASCENDING ? SortDirection.DESCENDING
                                        : SortDirection.ASCENDING), 1));
            } else {
                table.setSortDirection(SWT.UP);
                sorts.clear();
                sorts.add(new Sort(sortName, SortDirection.ASCENDING, 1));
            }

            /*
             * Set the column by which to sort.
             */
            table.setSortColumn(sortColumn);

            /*
             * Perform the sort.
             */
            sortEventData(directionChanged);

            /*
             * Update the sort column information in the settings.
             */
            updateTableSortColumnInSettingDefinition();
        }
    };

    private ISessionEventManager<ObservedHazardEvent> eventManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public TemporalDisplay() {

        // Create the various lists and dictionaries required.
        eventIdentifiers = new ArrayList<>();
        dictsForEventIdentifiers = new HashMap<>();
        columnsForNames = new HashMap<>();
        columnNamesForIdentifiers = new HashMap<>();
        visibleColumnNames = new ArrayList<>();
        hintTextIdentifiersForVisibleColumnNames = new HashMap<>();
        dateIdentifiersForVisibleColumnNames = new HashMap<>();
        tableEditorsForIdentifiers = new HashMap<>();

        // Configure the date-time formatter.
        dateTimeFormatter = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    // Public Methods

    /**
     * Initialize the display.
     * 
     * @param presenter
     *            Presenter managing the view to which this display belongs.
     * @param selectedTime
     *            Selected time.
     * @param currentTime
     *            Current time.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param hazardEvents
     *            Hazard events, each in dictionary form.
     * @param startTimeBoundariesForEventIds
     *            Map of event identifiers to their start time range boundaries.
     * @param endTimeBoundariesForEventIds
     *            Map of event identifiers to their end time range boundaries.
     * @param currentSettings
     * @param filterMegawidgets
     *            JSON string holding a list of dictionaries providing filter
     *            megawidget specifiers.
     * @param activeAlerts
     *            Currently active alerts.
     * @param eventIdentifiersAllowingUntilFurtherNotice
     *            Set of the hazard event identifiers that at any given moment
     *            allow the toggling of their "until further notice" mode. The
     *            set is unmodifiable; attempts to modify it will result in an
     *            {@link UnsupportedOperationException}. Note that this set is
     *            kept up-to-date, and thus will always contain only those
     *            events that can have their "until further notice" mode toggled
     *            at the instant at which it is checked.
     * @param showControlsInToolBar
     *            Flag indicating whether the controls (navigation buttons,
     *            etc.) are to be shown in the toolbar. If false, they are
     *            provided at the bottom of this composite instead.
     */
    public void initialize(ConsolePresenter presenter, Date selectedTime,
            Date currentTime, long visibleTimeRange, List<Dict> hazardEvents,
            Map<String, Range<Long>> startTimeBoundariesForEventIds,
            Map<String, Range<Long>> endTimeBoundariesForEventIds,
            ObservedSettings currentSettings, String filterMegawidgets,
            ImmutableList<IHazardAlert> activeAlerts,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice,
            boolean showControlsInToolBar) {

        // Remember the presenter, and the set of event identifiers
        // allowing "until further notice".
        this.presenter = presenter;
        this.eventManager = presenter.getSessionManager().getEventManager();
        this.eventIdentifiersAllowingUntilFurtherNotice = eventIdentifiersAllowingUntilFurtherNotice;
        this.filterMegawidgets = filterMegawidgets;
        this.startTimeBoundariesForEventIds = startTimeBoundariesForEventIds;
        this.endTimeBoundariesForEventIds = endTimeBoundariesForEventIds;

        // If the controls are to be shown in the toolbar, hide the
        // ones in the composite; otherwise, delete the ones in the
        // toolbar.
        if (showControlsInToolBar) {

            // Make the two panels and the separator at the bottom
            // invisible.
            separator.dispose();
            comboBoxPanel.dispose();
            buttonsPanel.dispose();

            // Tell the table to fill the space that was taken up by
            // the bottom row of controls, and lay out the temporal
            // display again.
            FormData tableFormData = (FormData) table.getLayoutData();
            tableFormData.bottom = new FormAttachment(100, 0);
            temporalDisplayPanel.layout(true);
        }

        // Remember the time values.
        this.selectedTimeStart = this.selectedTimeEnd = selectedTime.getTime();
        this.currentTime = currentTime.getTime();
        this.visibleTimeRange = visibleTimeRange;

        // Set the ruler's visible time range, selected time, and
        // current time. Also send the visible time range back as a
        // model change.
        long lowerTime = this.currentTime - (visibleTimeRange / 4L);
        if (lowerTime < HazardConstants.MIN_TIME) {
            lowerTime = HazardConstants.MIN_TIME;
        }
        long upperTime = lowerTime + visibleTimeRange - 1L;
        if (upperTime > HazardConstants.MAX_TIME) {
            lowerTime -= upperTime - HazardConstants.MAX_TIME;
            upperTime = HazardConstants.MAX_TIME;
        }
        ruler.setVisibleValueRange(lowerTime, upperTime);
        fireConsoleActionOccurred(new ConsoleAction(
                ConsoleAction.ActionType.VISIBLE_TIME_RANGE_CHANGED,
                Long.toString(lowerTime), Long.toString(upperTime)));
        ruler.setFreeMarkedValues(this.currentTime);
        ruler.setFreeThumbValues(this.selectedTimeStart);

        // Use the provided hazard events, clearing the old ones first
        // in case this is a re-initialization.
        clearEvents();
        updateHazardEvents(hazardEvents);
        updateSettings(currentSettings);
        updateConsole();

        // Add the mouse wheel filter, used to handle mouse wheel
        // events properly when they should apply to the table.
        table.getDisplay().addFilter(SWT.MouseWheel, mouseWheelFilter);

        // Create a countdown timer display manager.
        countdownTimersDisplayManager = new ConsoleCountdownTimersDisplayManager(
                countdownTimersDisplayListener);
        if (table != null) {
            countdownTimersDisplayManager.setBaseFont(table.getFont());
        }

        // Update the active alerts.
        updateActiveAlerts(activeAlerts);
    }

    /**
     * Set the map of toolbar widget identifiers to their actions and the
     * selected time mode action. These are constructed elsewhere and provided
     * to this object if appropriate.
     * 
     * @param map
     *            Map of toolbar widget identifiers to their actions.
     * @param selectedTimeModeAction
     *            Selected time mode action.
     */
    public void setToolBarActions(final Map<String, Action> map,
            ComboAction selectedTimeModeAction) {
        actionsForButtonIdentifiers.clear();
        actionsForButtonIdentifiers.putAll(map);
        for (Action action : actionsForButtonIdentifiers.values()) {
            ((ConsoleView.ITemporalDisplayAware) action)
                    .setTemporalDisplay(this);
        }
        this.selectedTimeModeAction = selectedTimeModeAction;
        ((ConsoleView.ITemporalDisplayAware) this.selectedTimeModeAction)
                .setTemporalDisplay(this);
        this.selectedTimeModeAction.setSelectedChoice(selectedTimeMode);
    }

    /**
     * Get the minimum visible time.
     * 
     * @return Minimum visible time.
     */
    public long getMinimumVisibleTime() {
        return ruler.getLowerVisibleValue();
    }

    /**
     * Get the maximum visible time.
     * 
     * @return Maximum visible time.
     */
    public long getMaximumVisibleTime() {
        return ruler.getUpperVisibleValue();
    }

    /**
     * Update the visible time delta.
     * 
     * @param newVisibleTimeDelta
     *            JSON string holding the amount of time visible at once in the
     *            time line as an epoch time range in milliseconds.
     */
    public void updateVisibleTimeDelta(String newVisibleTimeDelta) {

        // Get the new visible time range boundaries.
        long range = Long.parseLong(newVisibleTimeDelta);
        long lower = ruler.getFreeThumbValue(0) - (range / 4L);
        long upper = lower + range - 1L;

        // Use the new visible time range boundaries.
        setVisibleTimeRange(lower, upper, true);
    }

    /**
     * Update the visible time range.
     * 
     * @param newEarliestVisibleTime
     *            JSON string holding the earliest visible time in the time line
     *            as an epoch time range in milliseconds.
     * @param newLatestVisibleTime
     *            JSON string holding the latest visible time in the time line
     *            as an epoch time range in milliseconds.
     */
    public final void updateVisibleTimeRange(String newEarliestVisibleTime,
            String newLatestVisibleTime) {

        // Get the new visible time range boundaries.
        long lower = Long.parseLong(newEarliestVisibleTime);
        long upper = Long.parseLong(newLatestVisibleTime);

        // Use the new visible time range boundaries.
        setVisibleTimeRange(lower, upper, false);
    }

    /**
     * Update the current time.
     */
    public void updateCurrentTime(Date currentTime) {

        /*
         * Round the current time down to the nearest minute before using it.
         */
        this.currentTime = DateUtils.truncate(currentTime, Calendar.MINUTE)
                .getTime();

        /*
         * Update the current time marker on the time ruler and the hazard event
         * scale widgets.
         */
        if (ruler.isDisposed() == false) {
            ruler.setFreeMarkedValue(0, this.currentTime);
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                ((MultiValueScale) tableEditor.getEditor()).setFreeMarkedValue(
                        0, this.currentTime);
            }
        }
    }

    /**
     * Update the selected time range.
     * 
     * @param start
     *            Start time of the selected time range, or <code>null</code> if
     *            there is no selected time range.
     * @param end
     *            End time of the selected time range, or <code>null</code> if
     *            there is no selected time range.
     */
    public void updateSelectedTimeRange(Date start, Date end) {
        setSelectedTimeRange(start.getTime(), end.getTime());
    }

    /**
     * Update the time range boundaries for the events.
     * 
     * @param eventIds
     *            Identifiers of the events that have had their time range
     *            boundaries changed.
     */
    public void updateEventTimeRangeBoundaries(Set<String> eventIds) {
        for (String identifier : eventIds) {
            Range<Long> startTimeRange = (startTimeBoundariesForEventIds
                    .containsKey(identifier) == false ? Ranges.closed(
                    HazardConstants.MIN_TIME, HazardConstants.MAX_TIME)
                    : startTimeBoundariesForEventIds.get(identifier));
            MultiValueScale scale = (MultiValueScale) tableEditorsForIdentifiers
                    .get(identifier).getEditor();
            scale.setAllowableConstrainedValueRange(0,
                    startTimeRange.lowerEndpoint(),
                    startTimeRange.upperEndpoint());
            scale.setConstrainedThumbEditable(
                    0,
                    (startTimeRange.lowerEndpoint().equals(
                            startTimeRange.upperEndpoint()) == false));
            Range<Long> endTimeRange = (scale
                    .isConstrainedThumbIntervalLocked()
                    || (endTimeBoundariesForEventIds.containsKey(identifier) == false) ? Ranges
                    .closed(HazardConstants.MIN_TIME, HazardConstants.MAX_TIME)
                    : endTimeBoundariesForEventIds.get(identifier));
            scale.setAllowableConstrainedValueRange(1,
                    endTimeRange.lowerEndpoint(), endTimeRange.upperEndpoint());

            /*
             * If the end time can only be one value, or the interval is locked
             * and the start time can only be one value, make the end time
             * uneditable.
             */
            scale.setConstrainedThumbEditable(
                    1,
                    (endTimeRange.lowerEndpoint().equals(
                            endTimeRange.upperEndpoint()) == false)
                            && ((scale.isConstrainedThumbIntervalLocked() == false) || (startTimeRange
                                    .lowerEndpoint().equals(
                                            startTimeRange.upperEndpoint()) == false)));
        }
    }

    /**
     * @return List of the current hazard events.
     */
    public List<Dict> getEvents() {
        List<Dict> dictList = new ArrayList<>();
        for (String eventId : eventIdentifiers) {
            dictList.add(dictsForEventIdentifiers.get(eventId));
        }
        return dictList;
    }

    public ObservedSettings getCurrentSettings() {
        return currentSettings;
    }

    /**
     * Clear all hazard events.
     */
    public void clearEvents() {
        if (table.isDisposed() == false) {
            table.removeAll();
        }
        eventIdentifiers.clear();
        dictsForEventIdentifiers.clear();
        for (TableEditor editor : tableEditorsForIdentifiers.values()) {
            editor.getEditor().dispose();
            editor.dispose();
        }
        table.setMenu(null);
        tableEditorsForIdentifiers.clear();
        selectedIndices = null;
        disposeOfTimeRangeColors();
    }

    /**
     * Update the specified event.
     * 
     * @param hazardEvent
     *            JSON string holding a dictionary defining an event. The
     *            dictionary must contain an <code>eventID</code> key mapping to
     *            the event identifier as a value. All other mappings specify
     *            properties that are to have their values changed to those
     *            associated with the properties in the dictionary.
     */
    public void updateEvent(String hazardEvent) {

        // Get the dictionary from the JSON, and from it, the event
        // identifier and the matching hazard event.
        Dict dict = Dict.getInstance(hazardEvent);
        String identifier = dict
                .getDynamicallyTypedValue(HAZARD_EVENT_IDENTIFIER);
        MultiValueScale scale = (MultiValueScale) tableEditorsForIdentifiers
                .get(identifier).getEditor();

        // Update the event dictionary with these values.
        mergeIntoExistingEventDict(dict);

        // Iterate through the metadata that has changed, finding
        // the corresponding cells in the row for that event and
        // changing their strings to match the new values.
        for (TableItem item : table.getItems()) {

            // If this row has the same event identifier as the
            // changed event, perform the updates upon it.
            if (item.getData().equals(dict.get(HAZARD_EVENT_IDENTIFIER))) {

                // Iterate through the changed keys, making the
                // corresponding changes to the row. If the start
                // or end time have changed, make a note of it.
                long startTime = -1L, endTime = -1L;
                for (String key : dict.keySet()) {

                    // If the checked state changed, check or un-
                    // check the item; if the color changed, alter
                    // the color of the range between the two
                    // time scale thumbs; if the selected state
                    // changed, select or deselect the item; if
                    // the "until further notice" end time flag
                    // changed, enable or disable the end time
                    // thumb on the time scale; otherwise, as long
                    // as the key is not the event identifier,
                    // change the text to match.
                    if (key.equals(HAZARD_EVENT_CHECKED)) {
                        item.setChecked((Boolean) dict
                                .get(HAZARD_EVENT_CHECKED));
                        continue;
                    } else if (key.equals(HAZARD_EVENT_COLOR)) {
                        Color color = getTimeRangeColorForRGB((String) dict
                                .get(key));
                        scale.setConstrainedThumbRangeColor(1, color);
                    } else if (key.equals(HAZARD_EVENT_SELECTED)) {

                        // Determine the new selection state, as well
                        // as the current state.
                        Object selectedObject = dict.get(key);
                        boolean selected = ((selectedObject != null) && ((Boolean) selectedObject)
                                .booleanValue());
                        TableItem[] selectedItems = table.getSelection();
                        int index = -1;
                        for (int j = 0; j < selectedItems.length; j++) {
                            if (selectedItems[j] == item) {
                                index = j;
                                break;
                            }
                        }

                        // If the selection has been toggled on,
                        // handle it one way; otherwise, if toggled
                        // off, handle it another.
                        if (selected && (index == -1)) {
                            TableItem[] newSelectedItems = new TableItem[selectedItems.length + 1];
                            if (selectedItems != null) {
                                System.arraycopy(selectedItems, 0,
                                        newSelectedItems, 0,
                                        selectedItems.length);
                            }
                            newSelectedItems[newSelectedItems.length - 1] = item;
                            table.setSelection(newSelectedItems);
                            selectedIndices = table.getSelectionIndices();
                        } else if (!selected && (index != -1)) {
                            if (selectedItems.length == 1) {
                                table.deselectAll();
                                selectedIndices = null;
                            } else {
                                TableItem[] newSelectedItems = new TableItem[selectedItems.length - 1];
                                if (index != 0) {
                                    System.arraycopy(selectedItems, 0,
                                            newSelectedItems, 0, index);
                                }
                                if (index < selectedItems.length - 1) {
                                    System.arraycopy(selectedItems, index + 1,
                                            newSelectedItems, index,
                                            selectedItems.length - (index + 1));
                                }
                                table.setSelection(newSelectedItems);
                                selectedIndices = table.getSelectionIndices();
                            }
                        }
                    } else if (key
                            .equals(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
                        configureScaleIntervalLockingForEvent(scale, dict);
                    } else if (!key.equals(HAZARD_EVENT_IDENTIFIER)) {

                        // Change the text to match the new
                        // value.
                        String columnName = columnNamesForIdentifiers.get(key);
                        if (columnName != null) {
                            int columnIndex = getIndexOfColumnInTable(columnName);
                            if (columnIndex != -1) {
                                updateCell(columnIndex,
                                        columnsForNames.get(columnName),
                                        dict.get(key), item);
                            }
                        }

                        // If the changed value is the start or
                        // end time, make a note of it.
                        if (key.equals(HAZARD_EVENT_START_TIME)) {
                            startTime = ((Number) dict
                                    .get(HAZARD_EVENT_START_TIME)).longValue();
                        } else if (key.equals(HAZARD_EVENT_END_TIME)) {
                            endTime = ((Number) dict.get(HAZARD_EVENT_END_TIME))
                                    .longValue();
                        } else if (key.equals(HAZARD_EVENT_TYPE)) {
                            configureScaleIntervalLockingForEvent(scale, dict);
                        }
                    }
                }

                // If the start time or end time have been updated,
                // update its time scale widget.
                if ((startTime != -1L) || (endTime != -1L)) {
                    if (startTime == -1) {
                        startTime = scale.getConstrainedThumbValue(0);
                    }
                    if (endTime == -1) {
                        endTime = scale.getConstrainedThumbValue(1);
                    }
                    scale.setConstrainedThumbValues(startTime, endTime);
                }
                break;
            }
        }
    }

    /**
     * Create the area of the dialog window.
     * 
     * @param parent
     *            Parent of the area to be created.
     * @return Dialog area that was created.
     */
    public Composite createDisplayComposite(Composite parent) {

        // Create the composite holding all the widgets.
        temporalDisplayPanel = new Composite(parent, SWT.NONE);
        temporalDisplayPanel.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                disposeInternal();
            }
        });
        GridData panelLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        temporalDisplayPanel.setLayoutData(panelLayoutData);

        // Use a form layout for the new composite.
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = FORM_MARGIN_WIDTH;
        formLayout.marginHeight = FORM_MARGIN_HEIGHT;
        temporalDisplayPanel.setLayout(formLayout);

        // Create the various components.
        createTable(temporalDisplayPanel);
        createTableColumns();
        createTimeRuler(temporalDisplayPanel);
        createComboBox(temporalDisplayPanel);
        createButtonsPanel(temporalDisplayPanel);

        // Ensure the buttons start off with the right state.
        updateRulerButtonsState();

        // Return the overall composite holding the components.
        return temporalDisplayPanel;
    }

    /**
     * Set the focus to the table.
     */
    public void setFocus() {
        table.setFocus();
    }

    /**
     * Update the currently active alerts.
     * 
     * @param activeAlerts
     *            Currently active alerts.
     */
    public void updateActiveAlerts(
            ImmutableList<? extends IHazardAlert> activeAlerts) {
        if (countdownTimersDisplayManager == null) {
            return;
        }
        countdownTimersDisplayManager.updateActiveAlerts(activeAlerts);
        updateAllCountdownTimers();
    }

    // Package Methods

    /**
     * Get the currently selected time mode.
     * 
     * @return Currently selected time mode.
     */
    String getSelectedTimeMode() {
        return selectedTimeMode;
    }

    /**
     * Zoom the visible time range out by one level.
     */
    void zoomTimeOut() {
        long newVisibleTimeRange = getZoomedOutRange();
        if (newVisibleTimeRange <= MAX_VISIBLE_TIME_RANGE) {
            zoomVisibleTimeRange(newVisibleTimeRange);
        }
    }

    /**
     * Page the time range backward.
     */
    void pageTimeBack() {
        panTime(PAGE_TIME_DELTA_MULTIPLIER * -1.0f);
    }

    /**
     * Pan the time range backward.
     */
    void panTimeBack() {
        panTime(PAN_TIME_DELTA_MULTIPLIER * -1.0f);
    }

    /**
     * Pan the time line to ensure that the current time is shown.
     */
    void showCurrentTime() {
        long centerTime = ruler.getFreeMarkedValue(0);
        long lower = centerTime - (visibleTimeRange / 8L);
        long upper = lower + visibleTimeRange - 1L;
        setVisibleTimeRange(lower, upper, true);
    }

    /**
     * Pan the time range forward.
     */
    void panTimeForward() {
        panTime(PAN_TIME_DELTA_MULTIPLIER);
    }

    /**
     * Page the time range forward.
     */
    void pageTimeForward() {
        panTime(PAGE_TIME_DELTA_MULTIPLIER);
    }

    /**
     * Zoom the visible time range in by one level.
     */
    void zoomTimeIn() {
        long newVisibleTimeRange = getZoomedInRange();
        if (newVisibleTimeRange >= MIN_VISIBLE_TIME_RANGE) {
            zoomVisibleTimeRange(newVisibleTimeRange);
        }
    }

    /**
     * Set the selected time mode.
     * 
     * @param mode
     *            New selected time mode.
     */
    void setSelectedTimeMode(String mode) {
        if (selectedTimeMode.equals(mode)) {
            return;
        }
        selectedTimeMode = mode;
        if (mode.equals(SELECTED_TIME_MODE_SINGLE)) {
            lastSelectedTimeRangeDelta = selectedTimeEnd - selectedTimeStart;
            selectedTimeEnd = selectedTimeStart;
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                MultiValueScale scale = (MultiValueScale) tableEditor
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
                                : HOUR_INTERVAL * 4L);
            }
            setSelectedTimeRange(selectedTimeStart, selectedTimeEnd);
        }
        notifyPresenterOfSelectedTimeRangeChange();
    }

    // Private Methods

    /**
     * Update the console table to show the events that it knows about.
     */
    void updateConsole() {

        // Prepare for the addition and/or removal of columns.
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;
        visibleColumnCountChanging();

        // Remove the sorting column, if any.
        table.setSortColumn(null);

        // If the header menus for the various columns have not yet been
        // created, create them now.
        if (headerMenusForColumnNames == null) {

            // Get a list of all the column names for which there are
            // definitions, sorted alphabetically.
            List<String> columnNames = new ArrayList<>(columnsForNames.keySet());
            Collections.sort(columnNames);

            // Copy the list of column names to another list that will be
            // used to track which columns have associated header menus
            // in the loop below.
            List<String> columnNamesToBeGivenMenus = new ArrayList<>(
                    columnNames);

            // Get the list of provided event filters.
            DictList filters = DictList.getInstance(filterMegawidgets);

            // Create the mapping of column names to their header menus.
            // Make a menu for each column that has an associated filter,
            // making each such menu have both the checklist of column
            // names and the filter, and then make one more menu with
            // just the column names checklist for all the columns that
            // do not have associated filters.
            headerMenusForColumnNames = new HashMap<>();
            headerMegawidgetManagersForColumnNames = new HashMap<>();
            for (int j = 0; j < filters.size() + 1; j++) {

                // If there are no more columns needing menus, do nothing
                // more.
                if (columnNamesToBeGivenMenus.size() == 0) {
                    break;
                }

                // If there is a filter to be processed, get it and con-
                // figure it, and find its associated column name.
                Dict filter = null;
                String filterColumnName = null;
                if (j < filters.size()) {
                    filter = filters.getDynamicallyTypedValue(j);
                    filter.put(IMenuSpecifier.MEGAWIDGET_SHOW_SEPARATOR, true);
                    filter.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                            FILTER_MENU_NAME);
                    filterColumnName = filter
                            .getDynamicallyTypedValue(COLUMN_NAME);
                }

                // If the no-filter menu needs to be built, or a filter
                // was found and its associated column is found in the
                // list of defined columns, create a menu and associate
                // it with the column name.
                if ((filterColumnName == null)
                        || (columnNamesToBeGivenMenus.remove(filterColumnName))) {

                    // Create the column name checklist, allowing the
                    // user to toggle column visibility.
                    Menu menu = new Menu(table);
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

                    // If a filter exists for this menu, add it and associate
                    // it with the column. Otherwise, the menu being created
                    // is a catch-all for any column that does not have an
                    // associated filter.
                    if (filterColumnName != null) {
                        try {
                            final Dict settingsAsDict = settingsAsDict();
                            headerMegawidgetManagersForColumnNames.put(
                                    filterColumnName, new MegawidgetManager(
                                            menu, Lists.newArrayList(filter),
                                            settingsAsDict,
                                            new MegawidgetManagerAdapter() {

                                                @Override
                                                public void stateElementChanged(
                                                        MegawidgetManager manager,
                                                        String identifier,
                                                        Object state) {

                                                    /*
                                                     * Special case: A
                                                     * translation has to be
                                                     * made between the hazard
                                                     * categories and types tree
                                                     * structure that the user
                                                     * is creating as state and
                                                     * the old hazard categories
                                                     * list and hazard types
                                                     * list. This should be
                                                     * removed if we can get rid
                                                     * of the visibleTypes and
                                                     * hidHazardCategories lists
                                                     * in the dynamic setting.
                                                     */
                                                    Dict settingsAsDict = (Dict) manager
                                                            .getState();
                                                    SettingsView
                                                            .translateHazardCategoriesAndTypesToOldLists(settingsAsDict);
                                                    Settings updatedSettings = jsonConverter.fromJson(
                                                            settingsAsDict
                                                                    .toJSONString(),
                                                            Settings.class);

                                                    /*
                                                     * TODO: Once the console
                                                     * presenter receives all
                                                     * notifications via
                                                     * handlers instead of
                                                     * modelChanged(), there
                                                     * will be no need for a
                                                     * CONSOLE_HEADER_FILTER
                                                     * originator, since it will
                                                     * react to changes to event
                                                     * lists directly. But for
                                                     * now, since it does not,
                                                     * it needs to understand
                                                     * that the notification of
                                                     * the settings change did
                                                     * not come from the console
                                                     * per se so that it does
                                                     * react.
                                                     */
                                                    TemporalDisplay.this.currentSettings
                                                            .apply(updatedSettings,
                                                                    UIOriginator.CONSOLE_HEADER_FILTER);
                                                }
                                            }));

                        } catch (MegawidgetException e) {
                            statusHandler
                                    .error(getClass().getName()
                                            + " Unable to create megawidget "
                                            + "manager due to megawidget construction problem: "
                                            + e, e);
                        }

                        // Associate this header menu with the column
                        // name.
                        headerMenusForColumnNames.put(filterColumnName, menu);
                    } else {
                        for (String columnName : columnNamesToBeGivenMenus) {
                            headerMenusForColumnNames.put(columnName, menu);
                        }
                    }
                }
            }
        }

        // Test for columns to remove; these would be those found in
        // the table but no longer in the column definition list. The
        // time scale column should not be removed, since it is always
        // visible.
        TableColumn[] tableColumns = table.getColumns();
        for (int j = 0; j < tableColumns.length; ++j) {
            TableColumn tableColumn = tableColumns[j];
            String columnName = tableColumn.getText();
            if (!columnName.equals(TIME_SCALE_COLUMN_NAME)
                    && !visibleColumnNames.contains(columnName)) {
                tableColumn.dispose();
                if (columnName.equals(countdownTimerColumnName)) {
                    countdownTimerColumnIndex = -1;
                }
            }
        }

        // Make a list of the column names remaining in the table, and
        // update the widths of these columns based on the widths that
        // were provided, as well as making the column the sort column
        // if that is appropriate.
        List<String> columnNames = new ArrayList<>();
        for (int j = 0; j < table.getColumnCount(); ++j) {
            TableColumn column = table.getColumn(j);
            String columnName = column.getText();
            if (columnName.equals(TIME_SCALE_COLUMN_NAME)) {
                continue;
            }
            columnNames.add(columnName);
            Column columnDefinition = columnsForNames.get(columnName);
            if (columnDefinition != null) {
                Number width = columnDefinition.getWidth();
                if (width != null) {
                    table.getColumn(j).setWidth(width.intValue());
                }
                setSortInfoIfSortColumn(columnDefinition, column);
            }
        }

        // Add any columns that have not yet been added, inserting
        // each where it belongs (using the canonical order).
        for (String columnName : visibleColumnNames) {
            if (!columnNames.contains(columnName)) {
                createTableColumn(columnName, table.getColumnCount() - 1);
            }
        }

        // Set the column order to match the order of the visible
        // column names list.
        updateColumnOrder();

        // Create the list of sorts that should be applied.
        createSorts();

        // Ensure that the checkboxes in the table's rows are in the
        // leftmost column.
        ensureCheckboxesAreInLeftmostColumn(false);

        // Sort the incoming event identifiers based on the table's
        // sort column and direction.
        if (eventIdentifiers.size() > 0) {
            sortEventData(false);
            createTableRowsFromEventData();
        }

        // Finish up following the addition and/or removal of columns.
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        visibleColumnCountChanged();
    }

    /**
     * Clear and create the list of sorts to be applied to table rows.
     */
    private void createSorts() {

        /*
         * Clear any sorts left around.
         */
        sorts.clear();

        /*
         * Iterate through the columns, creating a sort for each column that has
         * a sort priority and direction.
         */
        for (Map.Entry<String, Column> entry : columnsForNames.entrySet()) {
            String name = entry.getKey();
            int priority = entry.getValue().getSortPriority();
            String sortDirection = entry.getValue().getSortDir();
            if ((priority > 0) && (sortDirection != null)) {
                if (sortDirection
                        .equals(SETTING_COLUMN_SORT_DIRECTION_ASCENDING)) {
                    sorts.add(new Sort(name, SortDirection.ASCENDING, priority));
                } else {
                    sorts.add(new Sort(name, SortDirection.DESCENDING, priority));
                }
            }
        }

        /*
         * Order the sorts by priority, so that the lowest-numbered one happens
         * first when performing a row sort.
         */
        Collections.sort(sorts);
    }

    /**
     * Set the selected time range to that specified, showing it if it is
     * hidden.
     * 
     * @param startTime
     *            Start time of the selected range.
     * @param endTime
     *            End time of the selected range.
     */
    private void setSelectedTimeRange(long startTime, long endTime) {

        /*
         * Remember the new values.
         */
        selectedTimeStart = startTime;
        selectedTimeEnd = endTime;

        /*
         * Determine whether the selected time mode was single or range, and if
         * it was single, whether it needs to be changed to range due to the new
         * values.
         */
        boolean singleMode = (ruler.getFreeThumbValueCount() != 0);
        boolean newSingleMode = ((startTime == endTime) && singleMode);

        /*
         * If the selected time mode must be changed to range, reconfigure the
         * ruler as appropriate, as well as the time scales for the events. Also
         * change the drop-down selector to show the new mode.
         */
        if (singleMode && (startTime != endTime)) {
            ruler.setFreeThumbValues();
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                ((MultiValueScale) tableEditor.getEditor())
                        .setFreeMarkedValues(currentTime);
            }
            selectedTimeMode = SELECTED_TIME_MODE_RANGE;
            if ((selectedTimeModeCombo != null)
                    && (selectedTimeModeCombo.isDisposed() == false)) {
                selectedTimeModeCombo.setText(selectedTimeMode);
            }
            if (selectedTimeModeAction != null) {
                selectedTimeModeAction.setSelectedChoice(selectedTimeMode);
            }
        }

        /*
         * Set the ruler to show the new time range. If it has just changed to
         * range mode from single mode, configure the ruler some more, as well
         * as the event time scales.
         */
        if (newSingleMode) {
            ruler.setFreeThumbValues(startTime);
        } else {
            ruler.setConstrainedThumbValues(startTime, endTime);
            if (singleMode) {
                ruler.setConstrainedThumbColor(0, timeRangeEdgeColor);
                ruler.setConstrainedThumbColor(1, timeRangeEdgeColor);
                ruler.setConstrainedThumbRangeColor(1, timeRangeFillColor);
                for (TableEditor tableEditor : tableEditorsForIdentifiers
                        .values()) {
                    MultiValueScale scale = (MultiValueScale) tableEditor
                            .getEditor();
                    scale.setConstrainedMarkedValueColor(0, timeRangeEdgeColor);
                    scale.setConstrainedMarkedValueColor(1, timeRangeEdgeColor);
                    scale.setConstrainedMarkedRangeColor(1, timeRangeFillColor);
                }
            }
        }
    }

    private Dict settingsAsDict() {
        final Dict result = Dict.getInstance(settingsAsJSON());
        return result;
    }

    private String settingsAsJSON() {
        return new JSONConverter().toJson(currentSettings);
    }

    /**
     * Perform any disposal tasks internally.
     */
    private void disposeInternal() {

        // Delete any header menus for the columns.
        deleteColumnHeaderMenus();

        // Dispose of the countdown timer display manager.
        if (countdownTimersDisplayManager != null) {
            countdownTimersDisplayManager.dispose();
        }

        // Clear all events.
        clearEvents();

        // Dispose of any resources that were created.
        for (Resource resource : resources) {
            resource.dispose();
        }

        // Delete the table tooltip.
        if ((tableToolTip != null) && !tableToolTip.isVisible()) {
            tableToolTip.dispose();
            tableToolTip = null;
        }

        // Remove the mouse wheel filter.
        table.getDisplay().removeFilter(SWT.MouseWheel, mouseWheelFilter);
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
    private void fitRulerToColumn(final TableColumn column) {

        // If this is a refit (second attempt), reset the refit flag;
        // otherwise, set it, so that another fit will be scheduled.
        // This is because SWT sometimes seems to report the wrong
        // widths (presumably because the table is in an interim
        // state where it thinks it needs a scrollbar, but will soon
        // find it does not); in these cases, a retry later on seems
        // to resize the ruler to its correct width.
        willRefitRulerToColumn = !willRefitRulerToColumn;

        // If the table is disposed, just schedule another refit to
        // occur later; otherwise, do the fitting.
        if (table.isDisposed()) {
            willRefitRulerToColumn = true;
        } else {

            // Layout the table.
            table.layout(true);
            temporalDisplayPanel.layout(true);

            // Calculate the column boundaries.
            table.setRedraw(false);
            int columnWidth = column.getWidth();

            // Get the difference between the table bounds and the
            // client area, which should give the vertical scrollbar
            // width.
            Rectangle tableBounds = table.getBounds();
            int clientAreaWidth = table.getClientArea().width;
            int cellTableEndXDiff = tableBounds.width - clientAreaWidth;

            // Determine how much of the width of the time range
            // cell is horizontally scrolled out of view, if any.
            // This will only occur if a horizontal scrollbar is
            // showing.
            int cellWidthScrolledOutOfView = 0;
            ScrollBar scrollBar = table.getHorizontalBar();
            if ((scrollBar != null)
                    && (scrollBar.getMaximum() > clientAreaWidth)) {
                cellWidthScrolledOutOfView = (scrollBar.getMaximum() - clientAreaWidth)
                        - scrollBar.getSelection();
            }

            // Determine the horizontal boundaries of the ruler.
            int cellBeginXPixels = tableBounds.width
                    + cellWidthScrolledOutOfView - columnWidth
                    - cellTableEndXDiff;
            int cellEndXPixels = cellTableEndXDiff;

            // Set up the layout data for the ruler.
            FormData rulerFormData = new FormData();
            rulerFormData.left = new FormAttachment(0, cellBeginXPixels
                    + FORM_MARGIN_WIDTH);
            rulerFormData.top = new FormAttachment(0, rulerTopOffset);
            rulerFormData.right = new FormAttachment(100,
                    (FORM_MARGIN_WIDTH + cellEndXPixels) * -1);
            ruler.setLayoutData(rulerFormData);

            // If the navigation controls are present below the
            // table, adjust them as well.
            if (!comboBoxPanel.isDisposed() && comboBoxPanel.isVisible()) {
                FormData buttonPanelFormData = new FormData();
                buttonPanelFormData.left = new FormAttachment(0,
                        cellBeginXPixels + FORM_MARGIN_WIDTH);
                buttonPanelFormData.right = new FormAttachment(100, -1
                        * cellEndXPixels);
                buttonPanelFormData.top = new FormAttachment(comboBoxPanel, 0,
                        SWT.TOP);
                buttonPanelFormData.bottom = new FormAttachment(comboBoxPanel,
                        0, SWT.BOTTOM);
                buttonsPanel.setLayoutData(buttonPanelFormData);
                temporalDisplayPanel.layout(true);
            }

            // Redraw the table.
            table.setRedraw(true);
        }

        // If a refit will be needed, schedule one to run.
        if (willRefitRulerToColumn) {
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    fitRulerToColumn(column);
                }
            });
        }
    }

    void updateHazardEvents(List<Dict> hazardEvents) {

        /*
         * Add each hazard event to the list of event dictionaries
         */
        if (hazardEvents != null) {
            numberOfRows = hazardEvents.size();
            for (Dict dict : hazardEvents) {
                String eventId = (String) dict.get(HAZARD_EVENT_IDENTIFIER);
                eventIdentifiers.add(eventId);
                dictsForEventIdentifiers.put(eventId, dict);
            }
        }
    }

    void updateSettings(ObservedSettings currentSettings) {

        /*
         * Delete any header menus for the columns.
         */
        deleteColumnHeaderMenus();

        /*
         * Get the column definitions, and the visible column names.
         */
        this.currentSettings = currentSettings;
        columnsForNames = currentSettings.getColumns();
        visibleColumnNames = currentSettings.getVisibleColumns();

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
         * Determine which is the appropriate comparator and class for the data
         * associated with each column.
         * 
         * TODO: When switching over to attributes directly from hazard events,
         * instead of JSONed-and-then-deserialized attributes, columns of type
         * "date" will need to use a Date ordering object, etc.
         */
        comparatorsForNames.clear();
        for (Map.Entry<String, Column> entry : columnsForNames.entrySet()) {
            String sortByType = entry.getValue().getType();
            Ordering<?> comparator = null;
            if (sortByType.equals(SETTING_COLUMN_TYPE_STRING)) {
                comparator = Ordering.<String> natural();
            } else if (sortByType.equals(SETTING_COLUMN_TYPE_DATE)
                    || sortByType.equals(SETTING_COLUMN_TYPE_NUMBER)) {
                comparator = Ordering.<Double> natural();
            } else if (sortByType.equals(SETTING_COLUMN_TYPE_COUNTDOWN)) {

                /*
                 * No action; comparator should be null.
                 */
            } else {
                statusHandler
                        .error("Do not know how to compare values of type \""
                                + sortByType + "\" for event table "
                                + "sorting purposes.");
            }
            comparatorsForNames.put(entry.getKey(),
                    (comparator != null ? comparator.nullsFirst() : null));
        }

        /*
         * Compile a mapping of column identifiers to their names.
         */
        columnNamesForIdentifiers.clear();
        for (String name : columnsForNames.keySet()) {
            Column column = columnsForNames.get(name);
            columnNamesForIdentifiers.put(column.getFieldName(), name);
        }
    }

    /**
     * Modify existing event dictionary list by merging the provided dictionary
     * into the existing one, replacing any values in the existing one with
     * those found at the same keys in the provided dictionary.
     * 
     * @param toBeMerged
     *            Dictionary to be merged in with the existing one.
     */
    private void mergeIntoExistingEventDict(Dict toBeMerged) {
        for (Dict eventDict : dictsForEventIdentifiers.values()) {
            if (eventDict.get(HAZARD_EVENT_IDENTIFIER).equals(
                    toBeMerged.get(HAZARD_EVENT_IDENTIFIER))) {
                for (String key : toBeMerged.keySet()) {
                    if (key.equals(HAZARD_EVENT_IDENTIFIER)) {
                        continue;
                    }
                    eventDict.put(key, toBeMerged.get(key));
                }
                break;
            }
        }
    }

    /**
     * Modify the existing event dictionary list to indicate only the specified
     * selected items as selected.
     * 
     * @param identifiers
     *            Identifiers of events that are currently selected.
     */
    private void updateEventDictListSelection(List<String> identifiers) {
        for (Dict eventDict : dictsForEventIdentifiers.values()) {
            eventDict.put(HAZARD_EVENT_SELECTED, identifiers.contains(eventDict
                    .get(HAZARD_EVENT_IDENTIFIER)));
        }
    }

    /**
     * Get a color to be used for visual time range indication matching the
     * specified RGB triplet.
     * 
     * @param rgb
     *            String consisting of three integers specifying the red, green,
     *            and blue components of the desired color, each separated from
     *            the next by a space.
     * @return Color to be used for visual time range indication.
     */
    private Color getTimeRangeColorForRGB(String rgb) {

        // See if the color has already been created, and if so, reuse
        // it.
        Color color = timeRangeColorsForRGBs.get(rgb);

        // If the color is not on record, create a new color with the
        // specified RGB parameters, and add it to the record so that
        // it can be reused and disposed of later.
        if ((color == null) || color.isDisposed()) {
            try {
                String[] rgbs = rgb.split(" ");
                color = new Color(table.getDisplay(), Integer.valueOf(rgbs[0]),
                        Integer.valueOf(rgbs[1]), Integer.valueOf(rgbs[2]));
                timeRangeColorsForRGBs.put(rgb, color);
            } catch (IllegalArgumentException e) {
                statusHandler.error(
                        "TemporalDisplay.getTimeRangeColorForRGB(): bad "
                                + "RGB specification \"" + rgb + "\".", e);
            }
        }

        // Return the created or reused color.
        return color;
    }

    /**
     * Dispose of any colors created for use as visual time range specifiers,
     * and clear any records of them.
     */
    private void disposeOfTimeRangeColors() {
        for (Color color : timeRangeColorsForRGBs.values()) {
            color.dispose();
        }
        timeRangeColorsForRGBs.clear();
    }

    /**
     * Create a command button.
     * 
     * @param panel
     *            Panel in which to place the button.
     * @param identifier
     *            Identifier of the button.
     * @param image
     *            Image to be used for the button.
     * @param description
     *            Description for use in a tooltip for the button.
     * @param listener
     *            Listener for selection events for the button.
     */
    private void createCommandButton(Composite panel, String identifier,
            Image image, String description, SelectionListener listener) {
        Button button = new Button(panel, SWT.PUSH);
        button.setImage(image);
        button.setToolTipText(description);
        button.setData(identifier);
        button.addSelectionListener(listener);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        button.setLayoutData(gridData);
        buttonsForIdentifiers.put(identifier, button);
    }

    /**
     * Create the time scale widget for the specified hazard event.
     * 
     * @param item
     *            Table item that this scale will reside within.
     * @param eventId
     *            Event identifier.
     * @param color
     *            Event color.
     * @param startTime
     *            Start time of the event, as epoch time in milliseconds.
     * @param endTime
     *            End time of the event, as epoch time in milliseconds.
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the end time is currently
     *            "Until further notice".
     * @return Time scale widget.
     */
    private MultiValueScale createTimeScale(TableItem item, String eventId,
            Color color, long startTime, long endTime,
            boolean endTimeUntilFurtherNotice) {

        // Create a time scale widget with two thumbs and configure it
        // appropriately. Note that the right inset of the widget is
        // larger than the left because it must match the width of the
        // time ruler, which has the form margin width subtracted from
        // its right edge. Furthermore, the left inset of the widget is
        // reduced by the difference between the cell left-side padding
        // and the form margin width, since the time ruler is only in-
        // set by the form margin width from the left of the column.
        MultiValueScale scale = new MultiValueScale(table,
                HazardConstants.MIN_TIME, HazardConstants.MAX_TIME);
        scale.setSnapValueCalculator(snapValueCalculator);
        scale.setTooltipTextProvider(thumbTooltipTextProvider);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        int verticalPadding = (table.getItemHeight() - scale.computeSize(
                SWT.DEFAULT, SWT.DEFAULT).y) / 2;
        scale.setInsets(TIME_HORIZONTAL_PADDING
                - (CELL_PADDING_LEFT - FORM_MARGIN_WIDTH), verticalPadding,
                TIME_HORIZONTAL_PADDING + FORM_MARGIN_WIDTH, verticalPadding);
        scale.setVisibleValueRange(ruler.getLowerVisibleValue(),
                ruler.getUpperVisibleValue());
        scale.setMinimumDeltaBetweenConstrainedThumbs(TIME_RANGE_MINIMUM_INTERVAL);
        scale.setConstrainedThumbValues(startTime, endTime);
        scale.setConstrainedThumbRangeColor(1, color);
        Range<Long> startTimeRange = (startTimeBoundariesForEventIds
                .containsKey(eventId) == false ? Ranges.closed(
                HazardConstants.MIN_TIME, HazardConstants.MAX_TIME)
                : startTimeBoundariesForEventIds.get(eventId));
        scale.setAllowableConstrainedValueRange(0,
                startTimeRange.lowerEndpoint(), startTimeRange.upperEndpoint());
        scale.setConstrainedThumbEditable(0, (startTimeRange.lowerEndpoint()
                .equals(startTimeRange.upperEndpoint()) == false));
        if (selectedTimeMode.equals(SELECTED_TIME_MODE_RANGE)) {
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
        return scale;
    }

    /**
     * Create a table editor for the specified time scale and associate it with
     * the specified table row.
     * 
     * @param scale
     *            Time scale widget for which to create the table editor.
     * @param item
     *            Table row with which to associate the new table editor.
     */
    private void createTableEditorForTimeScale(Control scale, TableItem item) {
        scale.setData(item);
        TableEditor scaleEditor = new TableEditor(table);
        scaleEditor.grabHorizontal = scaleEditor.grabVertical = true;
        scaleEditor.verticalAlignment = SWT.CENTER;
        scaleEditor.minimumHeight = table.getItemHeight()/* + 3 */;
        scaleEditor.setEditor(scale, item, table.getColumnCount() - 1);
        tableEditorsForIdentifiers.put((String) item.getData(), scaleEditor);
    }

    /**
     * Create the table for the temporal display.
     * 
     * @param parent
     *            The composite in which to create the table.
     */
    private void createTable(Composite parent) {

        // Create the table and configure it.
        table = new Table(parent, SWT.CHECK | SWT.MULTI | SWT.VIRTUAL);
        FormData tableFormData = new FormData();
        tableFormData.left = new FormAttachment(0, 0);
        tableFormData.top = new FormAttachment(0, 0);
        tableFormData.bottom = new FormAttachment(100, -1 * BUTTON_PANEL_HEIGHT);
        tableFormData.right = new FormAttachment(100, 0);
        table.setLayoutData(tableFormData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));

        // Add a listener to handle painting of the background
        // of table cells when those cells are for active, non-
        // selected countdown timers.
        if (countdownTimersDisplayManager != null) {
            countdownTimersDisplayManager.setBaseFont(table.getFont());
        }
        table.addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                // If the cell is not a countdown timer cell, or
                // the cell does not have custom display proper-
                // ties, handle the erase normally.
                countdownTimerDisplayProperties = null;
                if ((event.index != countdownTimerColumnIndex)
                        || (countdownTimersDisplayManager == null)) {
                    return;
                }
                countdownTimerDisplayProperties = countdownTimersDisplayManager
                        .getDisplayPropertiesForEvent((String) event.item
                                .getData());
                if (countdownTimerDisplayProperties == null) {
                    return;
                }

                // Save the width of this column, because the
                // PaintItem event that follows for this cell
                // will only hold the width of the text to be
                // drawn, not the width of the entire column.
                countdownTimerColumnWidth = event.width;

                // If the cell is selected or the background
                // color is white, use the standard background,
                // but make sure the foreground is not drawn in
                // the default manner.
                if (((event.detail & SWT.SELECTED) != 0)
                        || (countdownTimerDisplayProperties
                                .getBackgroundColor().equals(Display
                                .getCurrent().getSystemColor(SWT.COLOR_WHITE)))) {
                    event.detail &= ~(SWT.FOREGROUND | SWT.HOT);
                    return;
                }

                // Paint the background using the appropriate
                // color.
                Color oldBackground = event.gc.getBackground();
                event.gc.setBackground(countdownTimerDisplayProperties
                        .getBackgroundColor());
                event.gc.fillRectangle(event.x, event.y, event.width,
                        event.height);
                event.gc.setBackground(oldBackground);
                event.detail &= ~(SWT.BACKGROUND | SWT.FOREGROUND | SWT.HOT);
            }
        });

        // Add a listener to handle painting of the foreground
        // of table cells when those cells are for active count-
        // down timers.
        table.addListener(SWT.PaintItem, new Listener() {
            @Override
            public void handleEvent(Event event) {

                // If the cell is not a countdown timer cell, or
                // the cell does not have custom display proper-
                // ties, handle the erase normally.
                if ((event.index != countdownTimerColumnIndex)
                        || (countdownTimerDisplayProperties == null)) {
                    return;
                }

                // Paint the foreground using the appropriate
                // color. If the foreground color is black, no
                // blinking is occurring, and the item is se-
                // lected, use white instead.
                Color oldForeground = event.gc.getForeground();
                Font oldFont = event.gc.getFont();
                Color foreground = countdownTimerDisplayProperties
                        .getForegroundColor();
                if (((event.detail & SWT.SELECTED) != 0)
                        && (countdownTimerDisplayProperties.isBlinking() == false)
                        && foreground.equals(Display.getCurrent()
                                .getSystemColor(SWT.COLOR_BLACK))) {
                    foreground = Display.getCurrent().getSystemColor(
                            SWT.COLOR_WHITE);
                }
                event.gc.setForeground(foreground);
                event.gc.setFont(countdownTimerDisplayProperties.getFont());
                String text = ((TableItem) event.item).getText(event.index);
                Point size = event.gc.stringExtent(text);
                int textWidth = size.x + 5;
                int yOffset = (event.height - size.y) / 2;
                event.gc.drawText(text, event.x + countdownTimerColumnWidth
                        - textWidth, event.y + yOffset, true);
                event.gc.setForeground(oldForeground);
                event.gc.setFont(oldFont);
            }
        });

        // Add a listener for check and uncheck events that
        // updates the event dictionary to match, and notifies
        // any listeners of the event, as well as updating the
        // list of selected items whenever a selection or de-
        // selection occurs.
        table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                /*
                 * If the event is a check event, handle it as such, and
                 * remember its timestamp. If it is a selection event, see if
                 * the timestamp is the same as a previous event that should not
                 * change the selection, and if so, prevent it from happening
                 * and reset the selection to what it last was; otherwise allow
                 * it.
                 */
                if (e.detail == SWT.CHECK) {
                    lastIgnorableEventTime = e.time;
                    String identifier = (String) e.item.getData();
                    boolean isChecked = ((TableItem) e.item).getChecked();
                    for (Dict eventDict : dictsForEventIdentifiers.values()) {
                        if (eventDict.get(HAZARD_EVENT_IDENTIFIER).equals(
                                identifier)) {
                            eventDict.put(HAZARD_EVENT_CHECKED, isChecked);
                            break;
                        }
                    }
                    fireConsoleActionOccurred(new ConsoleAction(
                            ConsoleAction.ActionType.CHECK_BOX, identifier,
                            isChecked));
                } else {
                    if (e.time == lastIgnorableEventTime) {
                        e.doit = false;
                        if ((selectedIndices == null)
                                || (selectedIndices.length == 0)) {
                            table.deselectAll();
                        } else {
                            table.setSelection(selectedIndices);
                        }
                    } else {
                        TableItem[] selectedItems = table.getSelection();
                        List<String> selectedIdentifiers = new ArrayList<>();
                        for (int j = 0; j < selectedItems.length; j++) {
                            TableItem item = selectedItems[j];
                            selectedIdentifiers.add((String) item.getData());
                        }
                        selectedIndices = table.getSelectionIndices();
                        updateEventDictListSelection(selectedIdentifiers);
                        fireConsoleActionOccurred(new ConsoleAction(
                                ConsoleAction.ActionType.SELECTED_EVENTS_CHANGED,
                                selectedIdentifiers
                                        .toArray(new String[selectedIdentifiers
                                                .size()])));
                    }
                }
            }
        });

        // Add a listener for horizontal scrollbar movements that
        // prompt the repositioning of the timeline in the last
        // column's header.
        table.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if ((ruler != null) && !ruler.isDisposed() && ruler.isVisible()) {

                    // Get the table bounds for calculations
                    // to be made as to the size and position
                    // of the timeline ruler. Since the width
                    // is sometimes given as 0, schedule an-
                    // other resize event to fire off later
                    // in this case. If this is not done,
                    // the timeline ruler is given the wrong
                    // bounds.
                    Rectangle tableBounds = table.getBounds();
                    if (tableBounds.width == 0) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                visibleColumnCountChanged();
                            }
                        });
                    } else {
                        fitRulerToColumn(table
                                .getColumn(getIndexOfColumnInTable(TIME_SCALE_COLUMN_NAME)));
                    }
                }
            }
        });

        // Add a listener for popup menu request events, to be
        // told when the user has attempted to pop up a menu
        // from the table.
        table.addMenuDetectListener(new MenuDetectListener() {
            @Override
            public void menuDetected(MenuDetectEvent e) {

                /*
                 * Determine whether or not the point clicked is within the
                 * header area, and allow the menu to be deployed only if it is.
                 */
                Point point = Display.getCurrent().map(null, table,
                        new Point(e.x, e.y));
                int headerTop = table.getClientArea().y;
                int headerBottom = headerTop + table.getHeaderHeight();
                boolean headerClicked = ((point.y >= headerTop) && (point.y < headerBottom));

                /*
                 * Deploy the header menu if appropriate, or the item menu if an
                 * item was right-clicked.
                 */
                if (headerClicked) {

                    /*
                     * Set the menu's items checkboxes to reflect which columns
                     * are showing and which are hidden. If only one column is
                     * showing, disable that checkbox so that the user cannot
                     * uncheck it. Then update the sort menus to reflect the
                     * current sorts. Finally, update any associated megawidget
                     * manager's state as well, and set the menu as belonging to
                     * the table. If no menu is found for the specified column,
                     * do nothing.
                     */
                    TableColumn column = getTableColumnAtPoint(new Point(e.x,
                            e.y));
                    if (column != null) {
                        Menu menu = headerMenusForColumnNames.get(column
                                .getText());
                        if (menu == null) {
                            e.doit = false;
                            return;
                        }
                        for (MenuItem menuItem : menu.getItems()) {
                            if (menuItem.getStyle() == SWT.SEPARATOR) {
                                break;
                            }
                            menuItem.setSelection(visibleColumnNames
                                    .contains(menuItem.getText()));
                            menuItem.setEnabled(!menuItem.getSelection()
                                    || (visibleColumnNames.size() > 1));
                        }
                        prepareSortMenuForDisplay(
                                primarySortMenusForColumnMenus.get(menu),
                                (sorts.isEmpty() == false ? sorts.get(0) : null));
                        prepareSortMenuForDisplay(
                                secondarySortMenusForColumnMenus.get(menu),
                                (sorts.size() > 1 ? sorts.get(1) : null));
                        MegawidgetManager megawidgetManager = headerMegawidgetManagersForColumnNames
                                .get(column.getText());
                        if (megawidgetManager != null) {
                            try {
                                megawidgetManager.setState(settingsAsDict());
                            } catch (MegawidgetStateException exception) {
                                statusHandler
                                        .error("TemporalDisplay.createTable().MenuDetectListener."
                                                + "menuDetected(): Unable to set megawidget manager "
                                                + "state: " + e, exception);
                            }
                        }
                        table.setMenu(menu);
                    }
                } else {
                    if (rowMenu != null && rowMenu.isDisposed() == false) {
                        rowMenu.dispose();
                    }

                    /*
                     * Create a context-sensitive menu to be deployed for table
                     * items as appropriate.
                     */
                    rowMenu = new Menu(table);
                    untilFurtherNoticeMenuItem = new MenuItem(rowMenu, SWT.PUSH);
                    untilFurtherNoticeMenuItem
                            .setText(UNTIL_FURTHER_NOTICE_MENU_TEXT);
                    untilFurtherNoticeMenuItem
                            .addSelectionListener(rowMenuListener);
                    new MenuItem(rowMenu, SWT.SEPARATOR);
                    TableItem item = table.getItem(point);

                    if (item != null) {
                        String eventID = (String) item.getData();
                        eventManager.setCurrentEvent(eventID);
                    }
                    createHazardMenu();

                    /*
                     * Unfortunately, SWT tables fire these events before the
                     * selection events, so at the point where this code is
                     * executing, any modification made to the table selection
                     * has not yet occurred. Since this menu should only be
                     * displayed after such a selection change has been made,
                     * the actual display of the menu is delayed until the
                     * selection event has been handled.
                     */
                    table.setMenu(rowMenu);
                    rowMenu.setLocation(e.x, e.y);

                    Display.getCurrent().asyncExec(new Runnable() {
                        @Override
                        public void run() {

                            /*
                             * Only display the menu if at least one table row
                             * is selected, and if the table has not itself been
                             * disposed of.
                             */
                            if (table.isDisposed() == false) {

                                /*
                                 * Iterate through the table rows, determining
                                 * for each one whether its until further notice
                                 * state may be changed, and whether or not it
                                 * is currently in until further notice mode.
                                 */
                                TableItem[] items = table.getItems();
                                boolean untilFurtherNoticeAllowed = true;
                                int numUntilFurtherNotice = 0;
                                if (selectedIndices == null
                                        || selectedIndices.length == 0) {
                                    untilFurtherNoticeAllowed = false;
                                } else {
                                    for (int index : selectedIndices) {
                                        String identifier = (String) items[index]
                                                .getData();
                                        if (eventIdentifiersAllowingUntilFurtherNotice
                                                .contains(identifier) == false) {
                                            untilFurtherNoticeAllowed = false;
                                        }
                                        if (Boolean.TRUE
                                                .equals(dictsForEventIdentifiers
                                                        .get(identifier)
                                                        .get(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE))) {
                                            numUntilFurtherNotice++;
                                        }
                                    }
                                }

                                /*
                                 * Set the image for the menu item as checked,
                                 * semi-checked, or unchecked, depending upon
                                 * whether all, some, or none of the selected
                                 * events are in until further notice mode. Then
                                 * enable it if all the selected events may have
                                 * this option toggled.
                                 * 
                                 * NOTE: This is an SWT kludge; it is using a
                                 * push-button menu item to simulate a tri-state
                                 * checkbox menu item, because SWT, in its
                                 * infinite wisdom, does not offer a tri-state
                                 * checkbox menu item.
                                 */
                                untilFurtherNoticeMenuItem
                                        .setImage(numUntilFurtherNotice == 0 ? UNCHECKED_MENU_ITEM_IMAGE
                                                : (numUntilFurtherNotice == selectedIndices.length ? CHECKED_MENU_ITEM_IMAGE
                                                        : SEMI_CHECKED_MENU_ITEM_IMAGE));

                                /*
                                 * Enable the menu item if all selected events
                                 * can have their until further notice mode
                                 * toggled, and show the menu.
                                 */
                                untilFurtherNoticeMenuItem
                                        .setEnabled(untilFurtherNoticeAllowed);
                                rowMenu.setVisible(true);
                            }
                        }
                    });
                    e.doit = false;
                }
            }
        });

        // Add a mouse hover listener to pop up tooltips when
        // appropriate, and to close them when the mouse exits
        // the table boundaries.
        table.addMouseTrackListener(new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {

                // No action.
            }

            @Override
            public void mouseExit(MouseEvent e) {
                if ((tableToolTip != null) && tableToolTip.isVisible()) {
                    tableToolTip.setVisible(false);
                }
            }

            @Override
            public void mouseHover(MouseEvent e) {

                // If the tooltip is already showing or does not
                // exist, do nothing.
                if ((tableToolTip == null) || tableToolTip.isVisible()) {
                    return;
                }

                // If the hover occurred over a table row, see
                // if it needs a tooltip.
                Point point = new Point(e.x, e.y);
                TableItem item = table.getItem(point);
                if (item != null) {

                    // Iterate through the visible columns that
                    // show tooltips, seeing for each whether
                    // the point is within that column's bounds,
                    // and putting up a tooltip if it does.
                    for (String columnName : hintTextIdentifiersForVisibleColumnNames
                            .keySet()) {
                        int columnIndex = getIndexOfColumnInTable(columnName);
                        Rectangle cellBounds = item.getBounds(columnIndex);
                        if (cellBounds.contains(point)) {

                            // Find the event dictionary for the
                            // appropriate hazard event (the one
                            // represented by the row), and from
                            // it, retrieve the value needed for
                            // the hint text. If it is missing
                            // this value, no hint text is to be
                            // shown.
                            String eventID = (String) item.getData();
                            String text = null;
                            for (Dict eventDict : dictsForEventIdentifiers
                                    .values()) {
                                if (eventDict.get(HAZARD_EVENT_IDENTIFIER)
                                        .equals(eventID)) {
                                    String hintTextIdentifier = hintTextIdentifiersForVisibleColumnNames
                                            .get(columnName);
                                    Object value = eventDict
                                            .get(hintTextIdentifier);
                                    if (value != null) {
                                        text = value.toString();
                                    }
                                    break;
                                }
                            }

                            // Show the hint text if some has
                            // been found to be displayed. Also
                            // associate the bounds of the cell
                            // with the tooltip, so that later
                            // mouse coordinates can be tested
                            // to see if they still fall within
                            // the cell.
                            if (text != null && (!text.equals(""))) {
                                tableToolTip.setMessage(text);
                                tableToolTip.setToolTipBounds(cellBounds);
                                tableToolTip.setLocation(table.toDisplay(e.x,
                                        e.y));
                                tableToolTip.setVisible(true);
                            }
                            break;
                        }
                    }
                }
            }
        });

        // Add mouse listeners to close tooltips when the mouse
        // moves out of the cell boundaries, or when the mouse
        // is clicked. Also include handling of double-clicks to
        // pan the timeline ruler to the date value in the cell
        // in which the double-click occurred, if a date value
        // is found there.
        table.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                if ((tableToolTip != null) && tableToolTip.isVisible()) {
                    if (!(tableToolTip.getToolTipBounds().contains(e.x, e.y))) {
                        tableToolTip.setVisible(false);
                    }
                }
            }
        });
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {

                // If the hover occurred over a table row, see
                // if it is over a date cell.
                Point point = new Point(e.x, e.y);
                TableItem item = table.getItem(point);
                if (item != null) {

                    // Iterate through the visible columns that
                    // have date values, seeing for each whether
                    // the point is within that column's bounds.
                    for (String columnName : dateIdentifiersForVisibleColumnNames
                            .keySet()) {
                        int columnIndex = getIndexOfColumnInTable(columnName);
                        Rectangle cellBounds = item.getBounds(columnIndex);
                        if (cellBounds.contains(point)) {

                            // Find the event dictionary for the
                            // appropriate hazard event (the one
                            // represented by the row), and from
                            // it, retrieve the date value.
                            String eventID = (String) item.getData();
                            long date = -1L;
                            for (Dict eventDict : dictsForEventIdentifiers
                                    .values()) {
                                if (eventDict.get(HAZARD_EVENT_IDENTIFIER)
                                        .equals(eventID)) {
                                    String dateIdentifier = dateIdentifiersForVisibleColumnNames
                                            .get(columnName);
                                    Object value = eventDict
                                            .get(dateIdentifier);
                                    if (value != null) {
                                        date = ((Number) value).longValue();
                                    }
                                    break;
                                }
                            }

                            // If a date value was found, pan
                            // the timeline to show it.
                            if (date != -1L) {
                                showTime(date);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseDown(MouseEvent e) {

                /*
                 * Remember this timestamp as ignorable if this is a
                 * right-button press, since these should not cause table row
                 * selection changes.
                 */
                if (e.button == 3) {
                    lastIgnorableEventTime = e.time;
                }

                if ((tableToolTip != null) && tableToolTip.isVisible()) {
                    tableToolTip.setVisible(false);
                }
            }

            @Override
            public void mouseUp(MouseEvent e) {

                // No action.
            }
        });

        // Create the table tooltip.
        tableToolTip = new CustomToolTip(table.getShell(),
                PopupDialog.HOVER_SHELLSTYLE);
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
        for (MenuItem columnMenuItem : menu.getItems()) {
            for (MenuItem directionMenuItem : columnMenuItem.getMenu()
                    .getItems()) {
                directionMenuItem.setSelection((sort != null)
                        && sort.getColumnName()
                                .equals(columnMenuItem.getText())
                        && (sort.getSortDirection() == directionMenuItem
                                .getData()));
            }
        }
    }

    /**
     * Create the columns in the table.
     */
    private void createTableColumns() {

        // Get the number of columns, and add one for the time scale
        // column.
        int numberOfColumns = visibleColumnNames.size();

        // Create the user-specified columns.
        for (int j = 0; j < numberOfColumns; j++) {
            createTableColumn(visibleColumnNames.get(j), -1);
        }

        // Create the time scale column.
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(TIME_SCALE_COLUMN_NAME);
        if (spacerImage != null) {
            column.setImage(spacerImage);
        }
        column.setMoveable(false);
        column.setResizable(true);
        column.pack();

        // Add a listener to the time scale column to allow it to
        // respond to column-reordering and -resizing events.
        column.addControlListener(columnControlListener);
    }

    /**
     * Creates the column with the specified name.
     * 
     * @param name
     *            Column name.
     * @param index
     *            Index at which to place the column, or <code>-1</code> if the
     *            column should be appended to the table.
     */
    private void createTableColumn(String name, int index) {

        // Get the column definition dictionary corresponding to this
        // name.
        Column columnDefinition = columnsForNames.get(name);
        if (columnDefinition == null) {
            statusHandler
                    .error("TemporalDisplay.createTableColumn(): Problem: "
                            + "no column definition for \"" + name + "\".");
            return;
        }

        // Create the column.
        if (index == -1) {
            index = table.getColumnCount();
        }
        TableColumn column = new TableColumn(table, (columnDefinition.getType()
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

        // Set the table's sort column to match this one, and set its
        // sort direction, if this is the sort column.
        setSortInfoIfSortColumn(columnDefinition, column);

        // Pack the column, and only after packing set its width, if
        // one is specified in the definition. Width setting must occur
        // after packing so as to avoid having the pack operation
        // change the width to something other than that provided by
        // the definition.
        column.pack();
        Number width = columnDefinition.getWidth();
        if (width != null) {
            column.setWidth(width.intValue());
        }

        // Add the sort listener to this column, to detect the
        // user clicking on the column header and to respond
        // by sorting.
        column.addSelectionListener(sortListener);

        // Add a listener for column-reordering and -resizing
        // events.
        column.addControlListener(columnControlListener);
    }

    /**
     * Create the selected time mode combo box.
     * 
     * @param parent
     *            The composite to contain the combo box.
     * @return
     */
    private void createComboBox(Composite parent) {
        comboBoxPanel = new Composite(parent, SWT.NONE);
        GridLayout headerLayout = new GridLayout(1, false);
        headerLayout.horizontalSpacing = headerLayout.verticalSpacing = 0;
        headerLayout.marginWidth = 10;
        headerLayout.marginHeight = 0;
        headerLayout.marginTop = 3;
        comboBoxPanel.setLayout(headerLayout);

        // Create the selected time mode combo box.
        selectedTimeModeCombo = new Combo(comboBoxPanel, SWT.READ_ONLY);
        selectedTimeModeCombo.removeAll();
        selectedTimeModeCombo.setToolTipText(SELECTED_TIME_MODE_TEXT);
        for (String choice : SELECTED_TIME_MODE_CHOICES) {
            selectedTimeModeCombo.add(choice);
        }
        selectedTimeModeCombo.setText(selectedTimeMode);
        selectedTimeModeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setSelectedTimeMode(selectedTimeModeCombo.getText());
            }
        });

        // Create a visual separator between the combo box and
        // anything else next to it, and the table above.
        separator = new Label(parent, SWT.SEPARATOR | SWT.SHADOW_OUT
                | SWT.HORIZONTAL);
        FormData separatorFormData = new FormData();
        separatorFormData.left = new FormAttachment(0, 0);
        separatorFormData.top = new FormAttachment(table, 0);
        separatorFormData.width = 10000;
        separatorFormData.height = 5;
        separator.setLayoutData(separatorFormData);

        // Lay out the combo box.
        FormData comboTimeFormData = new FormData();
        comboTimeFormData.left = new FormAttachment(0, 0);
        comboTimeFormData.top = new FormAttachment(separator, 0);
        comboBoxPanel.setLayoutData(comboTimeFormData);
    }

    /**
     * Create the time ruler widget.
     * 
     * @param parent
     *            Composite in which to place the time ruler.
     */
    private void createTimeRuler(Composite parent) {
        Composite headerRulerPanel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.verticalSpacing = 0;
        layout.marginWidth = layout.marginHeight = 0;
        headerRulerPanel.setLayout(layout);

        // Create the colors for the time line ruler hatch marks.
        Color[] hatchMarkColors = { new Color(Display.getCurrent(), 128, 0, 0),
                new Color(Display.getCurrent(), 0, 0, 128),
                new Color(Display.getCurrent(), 0, 128, 0),
                new Color(Display.getCurrent(), 0, 128, 0) };
        for (Color color : hatchMarkColors) {
            resources.add(color);
        }

        // Create the time line ruler's hatch mark groups.
        List<IHatchMarkGroup> hatchMarkGroups = new ArrayList<>();
        hatchMarkGroups.add(new DayHatchMarkGroup());
        hatchMarkGroups.add(new TimeHatchMarkGroup(6L * HOUR_INTERVAL, 0.25f,
                hatchMarkColors[0], null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(HOUR_INTERVAL, 0.18f,
                hatchMarkColors[1], null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(30L * MINUTE_INTERVAL,
                0.11f, hatchMarkColors[2], null));
        hatchMarkGroups.add(new TimeHatchMarkGroup(10L * MINUTE_INTERVAL,
                0.05f, hatchMarkColors[3], null));

        // Create the time line widget. It is configured to snap
        // to values at increments of five minutes. The actual
        // widget is an instance of an anonymous subclass; the
        // latter is needed because background and foreground
        // color changes must be ignored, since the ModeListener
        // objects may try to change the colors when the CAVE
        // mode changes, which in this case is undesirable.
        ruler = new MultiValueRuler(parent, HazardConstants.MIN_TIME,
                HazardConstants.MAX_TIME, hatchMarkGroups) {
            @Override
            public void setBackground(Color background) {

                // No action.
            }

            @Override
            public void setForeground(Color foreground) {

                // No action.
            }
        };
        FontData fontData = ruler.getFont().getFontData()[0];
        Font minuteFont = new Font(Display.getCurrent(), fontData.getName(),
                (fontData.getHeight() * 7) / 10, fontData.getStyle());
        resources.add(minuteFont);
        for (int j = 1; j < hatchMarkGroups.size(); j++) {
            ((TimeHatchMarkGroup) hatchMarkGroups.get(j))
                    .setMinuteFont(minuteFont);
        }
        ruler.setVisibleValueZoomCalculator(new IVisibleValueZoomCalculator() {
            @Override
            public long getVisibleValueRangeForZoom(MultiValueRuler ruler,
                    boolean zoomIn, int amplitude) {
                long range;
                if (zoomIn) {
                    range = getZoomedInRange();
                    if (range < MIN_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                } else {
                    range = getZoomedOutRange();
                    if (range > MAX_VISIBLE_TIME_RANGE) {
                        return 0L;
                    }
                }
                return range;
            }
        });
        ruler.setBorderColor(RULER_BORDER_COLOR);
        ruler.setHeightMultiplier(2.95f);
        ruler.setSnapValueCalculator(snapValueCalculator);
        ruler.setTooltipTextProvider(thumbTooltipTextProvider);
        ruler.setInsets(TIME_HORIZONTAL_PADDING, 0, TIME_HORIZONTAL_PADDING, 0);
        ruler.setViewportDraggable(true);

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

        currentTimeColor = new Color(Display.getCurrent(), 50, 130, 50);
        resources.add(currentTimeColor);
        ruler.setFreeMarkedValueColor(0, currentTimeColor);
        ruler.setFreeMarkedValueDirection(0,
                MultiValueRuler.IndicatorDirection.DOWN);
        ruler.setFreeMarkedValueHeight(0, 1.0f);
        selectedTimeColor = new Color(Display.getCurrent(), 170, 56, 56);
        resources.add(selectedTimeColor);
        ruler.setFreeThumbColor(0, selectedTimeColor);
        timeRangeEdgeColor = new Color(Display.getCurrent(), 170, 56, 56);
        resources.add(timeRangeEdgeColor);
        timeRangeFillColor = new Color(Display.getCurrent(), 224, 190, 190);
        resources.add(timeRangeFillColor);
        ruler.setConstrainedThumbsDrawnAsBookends(true);

        ruler.addMultiValueLinearControlListener(new IMultiValueLinearControlListener() {
            @Override
            public void visibleValueRangeChanged(
                    MultiValueLinearControl widget, long lowerValue,
                    long upperValue, ChangeSource source) {
                visibleTimeRangeChanged(lowerValue, upperValue,
                        (source != ChangeSource.METHOD_INVOCATION));
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
                for (TableEditor tableEditor : tableEditorsForIdentifiers
                        .values()) {
                    ((MultiValueScale) tableEditor.getEditor())
                            .setConstrainedMarkedValues(values);
                }
                notifyPresenterOfSelectedTimeRangeChange(source);
            }

            @Override
            public void freeThumbValuesChanged(MultiValueLinearControl widget,
                    long[] values, ChangeSource source) {
                if (values.length == 0) {
                    return;
                }
                selectedTimeStart = selectedTimeEnd = values[0];
                for (TableEditor tableEditor : tableEditorsForIdentifiers
                        .values()) {
                    ((MultiValueScale) tableEditor.getEditor())
                            .setFreeMarkedValue(1, selectedTimeStart);
                }
                notifyPresenterOfSelectedTimeRangeChange(source);
            }
        });

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

        // Create a spacer image of a sufficient height to avoid having
        // the column headers smaller than they need to be in order to
        // have the last column header visually surround the time ruler,
        // and assign it as the image for each column.
        BufferedImage finalImage = new BufferedImage(1, ruler.computeSize(
                SWT.DEFAULT, SWT.DEFAULT).y, BufferedImage.TYPE_INT_ARGB);
        spacerImage = ImageUtilities.convertAwtImageToSwt(finalImage);
        resources.add(spacerImage);
        for (TableColumn column : table.getColumns()) {
            column.setImage(spacerImage);
        }

        // Pack the composite with everything created so far, as the
        // bounds of the time ruler are needed to continue.
        temporalDisplayPanel.pack(true);
        repackRuler();
    }

    /**
     * Notify the presenter of a change in the selected time range if it is due
     * to user manipulation of the GUI.
     * 
     * @param source
     *            Source of the change.
     */
    private void notifyPresenterOfSelectedTimeRangeChange(ChangeSource source) {
        if ((source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_ONGOING)
                || (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION_COMPLETE)) {
            notifyPresenterOfSelectedTimeRangeChange();
        }
    }

    /**
     * Notify the presenter of a change in the selected time range.
     */
    private void notifyPresenterOfSelectedTimeRangeChange() {
        fireConsoleActionOccurred(new ConsoleAction(
                ConsoleAction.ActionType.SELECTED_TIME_RANGE_CHANGED,
                Long.toString(selectedTimeStart),
                Long.toString(selectedTimeEnd)));
    }

    /**
     * Lay out the ruler according to whether or not the table's vertical
     * scrollbar exists.
     */
    private void repackRuler() {
        Rectangle tableRectangle = table.getBounds();
        int tableYPixels = tableRectangle.y;
        int columnWidth = table.getColumn(
                getIndexOfColumnInTable(TIME_SCALE_COLUMN_NAME)).getWidth();
        int xPixels = tableRectangle.width - columnWidth;
        int headerHeight = table.getHeaderHeight();
        Rectangle rectangle = ruler.getBounds();
        int rulerHeight = rectangle.height;
        FormData rulerFormData = new FormData();
        rulerFormData.left = new FormAttachment(0, xPixels + FORM_MARGIN_WIDTH);
        rulerTopOffset = tableYPixels + ((headerHeight - rulerHeight) / 2)
                - FORM_MARGIN_HEIGHT;
        rulerFormData.top = new FormAttachment(0, rulerTopOffset);
        rulerFormData.right = new FormAttachment(100, 0);
        ruler.setLayoutData(rulerFormData);
        ruler.moveAbove(table);
    }

    /**
     * Create the panel of buttons for controlling the time ruler.
     * 
     * @param parent
     *            Composite which will contain the button panel.
     */
    private void createButtonsPanel(Composite parent) {

        // Create the button panel.
        buttonsPanel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(7, false);
        layout.horizontalSpacing = layout.verticalSpacing = 0;
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.marginTop = 3;
        buttonsPanel.setLayout(layout);
        SelectionListener buttonListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(0))) {
                    zoomTimeOut();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(1))) {
                    pageTimeBack();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(2))) {
                    panTimeBack();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(3))) {
                    showCurrentTime();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(4))) {
                    panTimeForward();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(5))) {
                    pageTimeForward();
                } else if (e.widget.getData().equals(BUTTON_IMAGE_NAMES.get(6))) {
                    zoomTimeIn();
                }
            }
        };
        for (int j = 0; j < BUTTON_IMAGE_NAMES.size(); j++) {
            Image image = IconUtil.getImage(HazardServicesActivator
                    .getDefault().getBundle(), BUTTON_IMAGE_NAMES.get(j)
                    + PNG_FILE_NAME_SUFFIX, Display.getCurrent());
            resources.add(image);
            createCommandButton(buttonsPanel, BUTTON_IMAGE_NAMES.get(j), image,
                    BUTTON_DESCRIPTIONS.get(j), buttonListener);
        }
        FormData buttonFormData = new FormData();
        buttonFormData.left = new FormAttachment(comboBoxPanel, 20);
        buttonFormData.top = new FormAttachment(comboBoxPanel, 0, SWT.TOP);
        buttonFormData.bottom = new FormAttachment(comboBoxPanel, 0, SWT.BOTTOM);
        buttonsPanel.setLayoutData(buttonFormData);
    }

    /**
     * Create the rows in the table.
     */
    private void createTableRowsFromEventData() {

        // Create a list of table items to be selected.
        List<TableItem> selectedTableItems = new ArrayList<>();

        // Create a table item for each row in the table.
        for (int j = 0; j < numberOfRows; j++) {

            // Create the table item for this row.
            TableItem item = new TableItem(table, SWT.NONE);

            // Set the row's identifier to equal that of the hazard event.
            Dict eventDict = dictsForEventIdentifiers.get(eventIdentifiers
                    .get(j));
            String eventId = (String) eventDict.get(HAZARD_EVENT_IDENTIFIER);
            item.setData(eventId);
            item.setChecked((Boolean) eventDict.get(HAZARD_EVENT_CHECKED));

            // For each column in the row, insert the text appropriate
            // to the column.
            for (String name : visibleColumnNames) {
                updateCell(j, getIndexOfColumnInTable(name),
                        columnsForNames.get(name), item);
            }

            // Determine whether or not the row is to be selected.
            Object selectedObject = eventDict.get(HAZARD_EVENT_SELECTED);
            boolean selected = ((selectedObject != null) && ((Boolean) selectedObject)
                    .booleanValue());
            if (selected) {
                selectedTableItems.add(item);
            }

            // Create the event scale. This always goes in the last
            // column of the table.
            boolean untilFurtherNotice = Boolean.TRUE.equals(eventDict
                    .get(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE));
            MultiValueScale scale = createTimeScale(
                    item,
                    eventId,
                    getTimeRangeColorForRGB((String) eventDict
                            .get(HAZARD_EVENT_COLOR)),
                    ((Number) eventDict.get(HAZARD_EVENT_START_TIME))
                            .longValue(),
                    ((Number) eventDict.get(HAZARD_EVENT_END_TIME)).longValue(),
                    untilFurtherNotice);
            configureScaleIntervalLockingForEvent(scale, eventDict);

            // Create the table editor for this time scale.
            createTableEditorForTimeScale(scale, item);
        }

        // Remember the current count of events that are selected, and
        // select the items representing events that require selection.
        if (selectedTableItems.size() > 0) {
            table.setSelection(selectedTableItems
                    .toArray(new TableItem[selectedTableItems.size()]));
            selectedIndices = table.getSelectionIndices();
        } else {
            selectedIndices = null;
        }
    }

    /**
     * Configure the specified scale interval's locking mode for the specified
     * event.
     * 
     * @param scale
     *            Multi-value scale to have its interval locking mode
     *            configured.
     * @param eventDict
     *            Event dictionary with which the scale is associated.
     */
    private void configureScaleIntervalLockingForEvent(MultiValueScale scale,
            Dict eventDict) {

        /*
         * The interval between the start and end times should be locked if the
         * hazard event has duration choices, and if its end time is not
         * currently "until further notice".
         */
        String eventId = (String) eventDict.get(HAZARD_EVENT_IDENTIFIER);
        IHazardEvent event = presenter.getSessionManager().getEventManager()
                .getEventById(eventId);
        boolean lockedByDefault = (presenter.getSessionManager()
                .getConfigurationManager().getDurationChoices(event).size() > 0);
        boolean lock = (lockedByDefault && !Boolean.TRUE.equals(eventDict
                .get(HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)));
        scale.setConstrainedThumbIntervalLocked(lock);
        Range<Long> endTimeRange = (lock
                || (endTimeBoundariesForEventIds.containsKey(eventId) == false) ? Ranges
                .closed(HazardConstants.MIN_TIME, HazardConstants.MAX_TIME)
                : endTimeBoundariesForEventIds.get(eventId));
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
     * Find the table column that lies under the specified point.
     * 
     * @param point
     *            Point under which to look for the table column.
     * @return Table column under the point.
     */
    private TableColumn getTableColumnAtPoint(Point point) {
        point = Display.getCurrent().map(null, table, point);
        Rectangle clientArea = table.getClientArea();
        if (point.x >= clientArea.x) {
            int xCurrent = 0;
            for (int columnIndex : table.getColumnOrder()) {
                TableColumn column = table.getColumn(columnIndex);
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
     * Schedule the ensuring of checkboxes in the table's rows being in the
     * farthest left column.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not table redraw is currently
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
     * Ensure that the checkboxes in the table's rows are in the farthest left
     * column.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not table redraw is currently
     *            enabled. If it is, then if this method needs to disable it for
     *            some reason, it will turn it back on before returning.
     */
    private void ensureCheckboxesAreInLeftmostColumn(boolean redrawEnabled) {
        if (table.isDisposed()) {
            return;
        }

        // If the first column is not the first actual column,
        // delete it and recreate it in order to ensure that
        // it has the checkboxes in it.
        int[] columnOrder = table.getColumnOrder();
        if (columnOrder[0] != 0) {

            // If redraw is enabled, disable it while making
            // the column changes.
            if (redrawEnabled) {
                table.setRedraw(false);
            }

            // Delete the old column.
            TableColumn column = table.getColumn(columnOrder[0]);
            String columnName = column.getText();
            column.dispose();

            // Recreate the column and place it at the left
            // side of the table.
            createTableColumn(columnName, 0);

            // Fill in the text for the cells in the table's
            // rows that fall within the newly created column.
            updateCellsForColumn(columnName);

            // If redraw was enabled, re-enable it.
            if (redrawEnabled) {
                table.setRedraw(true);
            }
        }
    }

    /**
     * Update the text in the cells that fall within the specified column.
     * 
     * @param columnName
     *            Name of the column for which to update the corresponding
     *            cells.
     */
    private void updateCellsForColumn(String columnName) {

        // Iterate through the rows of the table, setting
        // the text of the cells within the new column as
        // appropriate.
        int index = getIndexOfColumnInTable(columnName);
        if (index != -1) {
            Column columnDefinition = columnsForNames.get(columnName);
            TableItem[] tableItems = table.getItems();
            for (int j = 0; j < tableItems.length; j++) {
                updateCell(j, index, columnDefinition, tableItems[j]);
            }
        }
    }

    /**
     * Update the specified cell to display the correct value with the correct
     * display attributes (font, etc.).
     * 
     * @param row
     *            Row to be updated.
     * @param col
     *            Column to be updated.
     * @param columnDefinition
     * @param item
     *            Table item holding the cell that is to be updated.
     */
    private void updateCell(int row, int col, Column columnDefinition,
            TableItem item) {

        // If the cell is in the countdown column, update the display
        // properties for any countdown timer associated with this
        // row's event.
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_COUNTDOWN)
                && (countdownTimersDisplayManager != null)) {
            countdownTimersDisplayManager.updateDisplayPropertiesForEvent(
                    (String) item.getData(), item.getFont());
        }

        // Set the cell text to the appropriate value.
        setCellText(col, item, getCellValue(row, columnDefinition),
                getEmptyFieldText(columnDefinition));
    }

    /**
     * Update the specified cell to display the specified value.
     * 
     * @param col
     *            Column to be updated.
     * @param columnDefinition
     * @param value
     *            Value to be displayed.
     * @param item
     *            Table item holding the cell that is to be updated.
     */
    private void updateCell(int col, Column columnDefinition, Object value,
            TableItem item) {
        setCellText(col, item, convertToCellValue(value, columnDefinition),
                getEmptyFieldText(columnDefinition));
    }

    /**
     * Set the specified cell's text to the specified value.
     * 
     * @param col
     *            Column of the cell to have its text set.
     * @param item
     *            Table item holding the cell to have its text set.
     * @param text
     *            Text to be used.
     * @param emptyValueDisplayString
     */
    private void setCellText(int col, TableItem item, String text,
            String emptyValueDisplayString) {

        item.setText(col,
                (text == null || text.length() == 0 ? emptyValueDisplayString
                        : text));
    }

    /**
     * Fire the specified console action off to any listeners.
     * 
     * @param ConsoleAction
     *            Action to be fired off to listeners.
     */
    private void fireConsoleActionOccurred(ConsoleAction consoleAction) {
        consoleAction.setOriginator(UIOriginator.CONSOLE);
        presenter.publish(consoleAction);
    }

    /**
     * Get a date-time string for the specified time.
     * 
     * @param time
     *            Time for which to fetch the string.
     * @return Date-time string.
     */
    private String getDateTimeString(long time) {
        date.setTime(time);
        return dateTimeFormatter.format(date);
    }

    /**
     * Zoom to the specified visible time range.
     * 
     * @param newVisibleTimeRange
     *            New visible time range as an epoch time range in milliseconds.
     */
    private void zoomVisibleTimeRange(long newVisibleTimeRange) {

        // Invert the zoom level flag.
        zoomLevelIsOdd = !zoomLevelIsOdd;

        // If the zoom resulted in a visible time range change, record
        // the new range and its boundaries.
        if (ruler.zoomVisibleValueRange(newVisibleTimeRange)) {
            long lower = ruler.getLowerVisibleValue();
            long upper = ruler.getUpperVisibleValue();
            visibleTimeRangeChanged(lower, upper, true);
        }
    }

    /**
     * Set the visible time range as specified.
     * 
     * @param lower
     *            Lower boundary of the visible time range as an epoch time in
     *            milliseconds.
     * @param upper
     *            Upper boundary of the visible time range as an epoch time in
     *            milliseconds.
     * @param forwardAction
     *            Flag indicating whether or not the change should be forwarded
     *            to listeners. If false, the change will still be forwarded if
     *            the specified boundaries needed alteration before being used.
     */
    private void setVisibleTimeRange(long lower, long upper,
            boolean forwardAction) {

        // Sanity check the bounds.
        boolean altered = false;
        if (lower < HazardConstants.MIN_TIME) {
            altered = true;
            upper += HazardConstants.MIN_TIME - lower;
            lower = HazardConstants.MIN_TIME;
        }
        if (upper > HazardConstants.MAX_TIME) {
            altered = true;
            lower -= upper - HazardConstants.MAX_TIME;
            upper = HazardConstants.MAX_TIME;
        }

        // If the time range has changed from what the time line
        // already had, commit to the change.
        if ((lower != ruler.getLowerVisibleValue())
                || (upper != ruler.getUpperVisibleValue())) {
            ruler.setVisibleValueRange(lower, upper);
            if (forwardAction || altered) {
                fireConsoleActionOccurred(new ConsoleAction(
                        ConsoleAction.ActionType.VISIBLE_TIME_RANGE_CHANGED,
                        Long.toString(lower), Long.toString(upper)));
            }
        }
    }

    /**
     * Respond to the visible time range changing in the time line ruler.
     * 
     * @param lower
     *            Lower boundary of the visible time range as an epoch time in
     *            milliseconds.
     * @param upper
     *            Upper boundary of the visible time range as an epoch time in
     *            milliseconds.
     * @param forwardAction
     *            Flag indicating whether or not the change should be forwarded
     *            to listeners.
     */
    private void visibleTimeRangeChanged(long lower, long upper,
            boolean forwardAction) {
        visibleTimeRange = (upper - lower) + 1L;
        for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
            ((MultiValueScale) tableEditor.getEditor()).setVisibleValueRange(
                    lower, upper);
        }
        updateRulerButtonsState();
        if (forwardAction) {
            fireConsoleActionOccurred(new ConsoleAction(
                    ConsoleAction.ActionType.VISIBLE_TIME_RANGE_CHANGED,
                    Long.toString(lower), Long.toString(upper)));
        }
    }

    /**
     * Get the visible time range that would result if the time line as it is
     * currently was zoomed out one level.
     * 
     * @return Visible time range that would result if the time line was zoomed
     *         out.
     */
    private long getZoomedOutRange() {
        return (visibleTimeRange * (zoomLevelIsOdd ? 3L : 4L))
                / (zoomLevelIsOdd ? 2L : 3L);
    }

    /**
     * Get the visible time range that would result if the time line as it is
     * currently was zoomed in one level.
     * 
     * @return Visible time range that would result if the time line was zoomed
     *         in.
     */
    private long getZoomedInRange() {
        return (visibleTimeRange * (zoomLevelIsOdd ? 3L : 2L))
                / (zoomLevelIsOdd ? 4L : 3L);
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
        setVisibleTimeRange(lower + delta, upper + delta, true);
    }

    /**
     * Update the enabled or disabled state of the time line manipulation
     * buttons.
     */
    private void updateRulerButtonsState() {

        // Update the buttons along the bottom of the view if they
        // exist.
        if (!comboBoxPanel.isDisposed()) {
            buttonsForIdentifiers.get(BUTTON_ZOOM_OUT).setEnabled(
                    getZoomedOutRange() <= MAX_VISIBLE_TIME_RANGE);
            buttonsForIdentifiers.get(BUTTON_ZOOM_IN).setEnabled(
                    getZoomedInRange() >= MIN_VISIBLE_TIME_RANGE);
            buttonsForIdentifiers.get(BUTTON_PAGE_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > HazardConstants.MIN_TIME);
            buttonsForIdentifiers.get(BUTTON_PAN_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > HazardConstants.MIN_TIME);
            buttonsForIdentifiers.get(BUTTON_PAN_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < HazardConstants.MAX_TIME);
            buttonsForIdentifiers.get(BUTTON_PAGE_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < HazardConstants.MAX_TIME);
        }

        // Update the toolbar buttons if they exist.
        if (actionsForButtonIdentifiers.get(BUTTON_ZOOM_OUT) != null) {
            actionsForButtonIdentifiers.get(BUTTON_ZOOM_OUT).setEnabled(
                    getZoomedOutRange() <= MAX_VISIBLE_TIME_RANGE);
            actionsForButtonIdentifiers.get(BUTTON_ZOOM_IN).setEnabled(
                    getZoomedInRange() >= MIN_VISIBLE_TIME_RANGE);
            actionsForButtonIdentifiers.get(BUTTON_PAGE_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > HazardConstants.MIN_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAN_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > HazardConstants.MIN_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAN_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < HazardConstants.MAX_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAGE_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < HazardConstants.MAX_TIME);
        }
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
        setVisibleTimeRange(lower, upper, true);
    }

    /**
     * Respond to addition(s) to and/or removals from the set of visible columns
     * being about to occur.
     */
    private void visibleColumnCountChanging() {

        // Turn off table redraw and make the time ruler invisible.
        table.setRedraw(false);
        ruler.setVisible(false);
    }

    /**
     * Respond to addition(s) to and/or removals from the set of visible columns
     * having occurred.
     */
    private void visibleColumnCountChanged() {
        if (table.isDisposed()) {
            return;
        }

        // Turn on table redraw, and make the time ruler visible and
        // fit it to its column.
        table.setRedraw(true);
        ruler.setVisible(true);
        TableColumn column = table
                .getColumn(getIndexOfColumnInTable(TIME_SCALE_COLUMN_NAME));
        fitRulerToColumn(column);
    }

    /**
     * Resize all non-time-scale columns proportionally to in response to the
     * time scale column being resized.
     */
    private void resizeColumnsProportionally() {
        if (table.isDisposed()) {
            return;
        }

        // Define a class used to pair column indices and the widths
        // of said columns, and which is sortable by the widths (with
        // largest widths sorting to the top).
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

        // Turn off redrawing and set the ignore resizing and move
        // flags to avoid bad recursive behavior (since this method
        // call is triggered by certain resize events).
        table.setRedraw(false);
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;

        // Get the columns, and find the current width of the time
        // scale column in order to calculate the delta between
        // the old time
        TableColumn[] columns = table.getColumns();
        int delta = 0;
        for (TableColumn column : columns) {
            if (column.getText().equals(TIME_SCALE_COLUMN_NAME)) {
                delta = timeScaleColumnWidthBeforeResize - column.getWidth();
                break;
            }
        }

        // Create a list of the non-time-scale columns and their
        // widths, and sort it so that the largest columns are at
        // the top of the list; this allows the proportional re-
        // sizing to be applied in order from largest to smallest
        // columns, ensuring a good distribution of the delta.
        int totalWidth = 0;
        List<ColumnIndexAndWidth> columnIndicesAndWidths = new ArrayList<>();
        for (int j = 0; j < columns.length; j++) {

            // Only add a record for this column if it is not
            // the time scale column.
            if (!columns[j].getText().equals(TIME_SCALE_COLUMN_NAME)) {

                // Get the column's width, subtracting the delta
                // from it if the column is the last one in the
                // table before the time scale column (since this
                // means that the resize of the time scale column
                // has already added the delta to this column's
                // size).
                int width = columns[j].getWidth();
                if (columns[j].getText().equals(
                        visibleColumnNames.get(visibleColumnNames.size() - 1))) {
                    width -= delta;
                }

                // Add the width to the total width, and add a re-
                // cord of the column index and width to the list.
                totalWidth += width;
                columnIndicesAndWidths.add(new ColumnIndexAndWidth(j, width));
            }
        }
        Collections.sort(columnIndicesAndWidths);

        // As long as there is delta remaining to be applied, iter-
        // ate through all the columns, applying a proportional
        // amount of the delta to each. Stop when there is no more
        // delta remaining, or when a pass through the loop changes
        // none of the widths.
        int deltaRemaining = Math.abs(delta);
        int lastDeltaRemaining;
        do {

            // Remember the current delta remaining so that it may
            // be compared with its new value to determine if it
            // has not changed in this pass through the loop.
            lastDeltaRemaining = deltaRemaining;

            // Iterate through the columns, sorted by size in des-
            // cending order, and determine for each its new width,
            // stopping when there is no more delta left to apply.
            for (ColumnIndexAndWidth indexAndWidth : columnIndicesAndWidths) {

                // If there is no delta remaining to apply, stop.
                if (deltaRemaining == 0) {
                    break;
                }

                // Get the number expressing this column's width as
                // a fraction of the total width of all the non-
                // time-scale columns, and calculate the width
                // change that should be applied, subtracting it
                // from the delta remaining so as to keep a record
                // of how much delta is left to apply the next time
                // through the loop.
                float proportionalWidth = ((float) indexAndWidth.width)
                        / (float) totalWidth;
                int widthChange = ((int) ((proportionalWidth * lastDeltaRemaining) + 0.5f))
                        * (delta < 0 ? -1 : 1);
                if (Math.abs(widthChange) > deltaRemaining) {
                    widthChange = deltaRemaining * (widthChange < 0 ? -1 : 1);
                }
                deltaRemaining -= Math.abs(widthChange);

                // Remember the new width for this column.
                indexAndWidth.width += widthChange;
            }
        } while ((deltaRemaining > 0) && (lastDeltaRemaining != deltaRemaining));

        // If there is still delta remaining, just apply it to the
        // largest column.
        if ((deltaRemaining > 0) && (columnIndicesAndWidths.size() > 0)) {
            columnIndicesAndWidths.get(0).width += deltaRemaining
                    * (delta < 0 ? -1 : 1);
        }

        // For each column, change its width if it has had a portion
        // of the delta applied to it.
        for (ColumnIndexAndWidth indexAndWidth : columnIndicesAndWidths) {
            if (columns[indexAndWidth.index].getWidth() != indexAndWidth.width) {
                columns[indexAndWidth.index].setWidth(indexAndWidth.width);
            }
        }

        // Reset the ignore resizing and moving flags and turn back on
        // table redrawing, and then update the setting definition to
        // include the new column widths.
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        table.setRedraw(true);
        updateAllTableColumnWidthsInSettingDefinition();
    }

    /**
     * Update the order of the table columns to match the order of the visible
     * column names list.
     */
    private void updateColumnOrder() {

        // Iterate through the column names in the order they should
        // be displayed, determining for each which column index in the
        // table corresponds to that name. Once this array of indices is
        // compiled, set the column order so that the table's columns are
        // in the order specified.
        int[] columnOrder = new int[table.getColumnCount()];
        for (int j = 0; j < columnOrder.length; j++) {
            int index = visibleColumnNames
                    .indexOf(table.getColumn(j).getText());
            columnOrder[index == -1 ? columnOrder.length - 1 : index] = j;
        }
        boolean lastIgnoreMove = ignoreMove;
        ignoreMove = true;
        table.setColumnOrder(columnOrder);
        ignoreMove = lastIgnoreMove;
    }

    /**
     * Handle the reordering of the table columns via a drag performed by the
     * user.
     * 
     * @param redrawEnabled
     *            Flag indicating whether or not table redraw is currently
     *            enabled. If it is, then if this method needs to disable it for
     *            some reason, it will turn it back on before returning.
     */
    private void handleColumnReorderingViaDrag(boolean redrawEnabled) {

        // Ensure that the checkboxes in the table's rows are
        // in the leftmost column.
        scheduleEnsureCheckboxesAreInLeftmostColumn(redrawEnabled);

        // This is needed for the special case where nothing
        // is selected and the user drags a column. Without
        // this, the topmost table row is selected.
        if ((selectedIndices == null) || (selectedIndices.length == 0)) {
            table.deselectAll();
        }

        // Get the index of the countdown timer column, if it
        // is showing.
        countdownTimerColumnIndex = getIndexOfColumnInTable(countdownTimerColumnName);

        // Update the order of the columns in the dictionaries.
        updateTableColumnOrderInSettingDefinition();
    }

    /**
     * Retrieve the value to display in a cell in the table.
     * 
     * @param row
     *            Index of the row from which to retrieve the value.
     * @param columnDefinition
     * @return Value to display in the specified table cell.
     */
    private String getCellValue(int row, Column columnDefinition) {
        if (columnDefinition == null) {
            statusHandler.error("TemporalDisplay.getCellValue(): Problem: "
                    + "no column definition provided");
            return null;
        }
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_COUNTDOWN)) {
            if (countdownTimersDisplayManager == null) {
                return null;
            }
            return countdownTimersDisplayManager
                    .getTextForEvent(eventIdentifiers.get(row));
        }
        return (convertToCellValue(
                dictsForEventIdentifiers.get(eventIdentifiers.get(row)).get(
                        columnDefinition.getFieldName()), columnDefinition));
    }

    /**
     * Convert the specified value to a proper cell value for the table.
     * 
     * @param value
     *            Value to be converted.
     * @param columnDefinition
     *            Column definition for this cell.
     * @return Value to display in a table cell.
     */
    private String convertToCellValue(Object value, Column columnDefinition) {
        if (columnDefinition == null) {
            statusHandler.error("TemporalDisplay.convertToCellValue(): "
                    + "Problem: no column definition provided");
            return null;
        }
        if (columnDefinition.getType().equals(SETTING_COLUMN_TYPE_DATE)) {
            Number number = (Number) value;
            if (number != null) {
                if (columnDefinition.getFieldName().equals(
                        HAZARD_EVENT_END_TIME)
                        && (number.longValue() == UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
                    return UNTIL_FURTHER_NOTICE_COLUMN_TEXT;
                }
                return getDateTimeString(number.longValue());
            } else {
                return getDateTimeString(0L);
            }
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    /**
     * Notify all listeners of the specified change in the dynamic setting
     * configuration caused by the temporal display (e.g. changes in column
     * widths, column orders, etc.).
     */
    private void notifyListenersOfSettingDefinitionChange(SettingsChange change) {
        if (change == SettingsChange.COLUMNS) {
            currentSettings.setColumns(columnsForNames, UIOriginator.CONSOLE);
        } else {
            currentSettings.setVisibleColumns(visibleColumnNames,
                    UIOriginator.CONSOLE);
        }

    }

    /**
     * Schedule notification of all listeners of the specified change in the
     * dynamic setting configuration caused by the temporal display.
     * 
     * @param change
     *            Settings change.
     */
    private void scheduleNotificationOfSettingDefinitionChange(
            final SettingsChange change) {
        if (willNotifyOfSettingChange == false) {
            willNotifyOfSettingChange = true;
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    willNotifyOfSettingChange = false;
                    notifyListenersOfSettingDefinitionChange(change);
                }
            });
        }
    }

    /**
     * Update the setting definition to include a new table column ordering.
     */
    private void updateTableColumnOrderInSettingDefinition() {

        // Rebuild the visible column names list and the map
        // of special column properties.
        visibleColumnNames.clear();
        hintTextIdentifiersForVisibleColumnNames.clear();
        dateIdentifiersForVisibleColumnNames.clear();
        for (int order : table.getColumnOrder()) {
            String columnName = table.getColumn(order).getText();
            if (!columnName.equals(TIME_SCALE_COLUMN_NAME)) {
                visibleColumnNames.add(columnName);
                determineSpecialPropertiesOfColumn(columnName);
            }
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange(SettingsChange.VISIBLE_COLUMNS);
    }

    /**
     * Determine the special properties of the column associated with the
     * specified name, adding records to maps for any such properties.
     * 
     * @param columnName
     *            Column name.
     */
    private void determineSpecialPropertiesOfColumn(String columnName) {
        Column columnDefinition = columnsForNames.get(columnName);
        if (columnDefinition == null) {
            return;
        }
        String hintTextIdentifier = columnDefinition.getHintTextFieldName();
        if (hintTextIdentifier != null) {
            hintTextIdentifiersForVisibleColumnNames.put(columnName,
                    hintTextIdentifier);
        }
        String columnType = columnDefinition.getType();
        if (columnType.equals(SETTING_COLUMN_TYPE_DATE)) {
            dateIdentifiersForVisibleColumnNames.put(columnName,
                    columnDefinition.getFieldName());
        }
    }

    /**
     * Update information in the column definition dictionaries related to the
     * sort column and direction.
     */
    private void updateTableSortColumnInSettingDefinition() {

        /*
         * Clear all the column definitions of any sort-related information.
         */
        for (Column columnDefinition : columnsForNames.values()) {
            columnDefinition.setSortPriority(SETTING_COLUMN_SORT_PRIORITY_NONE);
            columnDefinition.setSortDir(SETTING_COLUMN_SORT_DIRECTION_NONE);
        }

        /*
         * Alter the column definitions to include the current sorts.
         */
        for (Sort sort : sorts) {
            Column columnDefinition = columnsForNames.get(sort.getColumnName());
            columnDefinition.setSortPriority(sort.getPriority());
            columnDefinition
                    .setSortDir(sort.getSortDirection() == SortDirection.ASCENDING ? SETTING_COLUMN_SORT_DIRECTION_ASCENDING
                            : SETTING_COLUMN_SORT_DIRECTION_DESCENDING);
        }

        /*
         * Notify listeners of the setting change.
         */
        scheduleNotificationOfSettingDefinitionChange(SettingsChange.COLUMNS);
    }

    /**
     * Update column definition dictionary with a new width for the specified
     * resized table column.
     * 
     * @param column
     *            The table column which has been resized.
     */
    private void updateTableColumnWidthInSettingDefinition(TableColumn column) {
        Column columnDefinition = columnsForNames.get(column.getText());
        if (columnDefinition != null) {
            columnDefinition.setWidth(column.getWidth());
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange(SettingsChange.COLUMNS);

    }

    /**
     * Update all table column definition dictionaries for visible columns with
     * new widths.
     */
    private void updateAllTableColumnWidthsInSettingDefinition() {
        for (TableColumn column : table.getColumns()) {
            Column columnDefinition = columnsForNames.get(column.getText());
            if (columnDefinition != null) {
                columnDefinition.setWidth(column.getWidth());
            }
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange(SettingsChange.COLUMNS);

    }

    /**
     * Get the index at which the specified column is found in the table. This
     * is not the index indicating the current ordering of the columns, but
     * rather the index that, when provided to {@link Table#getColumn(int)},
     * returns the specified column.
     * 
     * @param name
     *            Name of the column for which to find the index.
     * @return Index of the column in the table, or <code>-1</code> if the
     *         column is not currently in the table.
     */
    private int getIndexOfColumnInTable(String name) {
        if (name == null) {
            return -1;
        }
        TableColumn[] columns = table.getColumns();
        for (int j = 0; j < columns.length; j++) {
            if (name.equals(columns[j].getText())) {
                return j;
            }
        }
        return -1;
    }

    /**
     * Set the table's sort column to match this one, and set its sort
     * direction, if this is the sort column.
     * 
     * @param columnDefinition
     *            Column definition to be checked to see if it is the sort
     *            column.
     * @param tableColumn
     *            Table column for this definition.
     */
    private void setSortInfoIfSortColumn(Column columnDefinition,
            TableColumn tableColumn) {
        String sortDirection = columnDefinition.getSortDir();
        if ((columnDefinition.getSortPriority() == 1)
                && (sortDirection != null)) {
            if (sortDirection.equals(SETTING_COLUMN_SORT_DIRECTION_ASCENDING)) {
                table.setSortColumn(tableColumn);
                table.setSortDirection(SWT.UP);
            } else if (sortDirection
                    .equals(SETTING_COLUMN_SORT_DIRECTION_DESCENDING)) {
                table.setSortColumn(tableColumn);
                table.setSortDirection(SWT.DOWN);
            }
        }
    }

    /**
     * Sort the event dictionaries based on the table's sort column and
     * direction, using the Bubble Sort algorithm.
     * 
     * @param directionChanged
     *            Flag indicating whether or not this sort was prompted by a
     *            mere sort direction change for one of the sorts.
     */
    private void sortEventData(boolean directionChanged) {

        /*
         * Determine whether there is at least one sort column, and do the sort
         * if there is.
         */
        if (sorts.isEmpty() == false) {

            /*
             * For each sort, compile the sort direction, the event attribute it
             * represents, and the comparator to be used.
             */
            List<String> attributeIdentifiers = new ArrayList<>(sorts.size());
            List<SortDirection> sortDirections = new ArrayList<>(sorts.size());
            List<Comparator<?>> comparators = new ArrayList<>(sorts.size());
            for (Sort sort : sorts) {
                Column columnDefinition = columnsForNames.get(sort
                        .getColumnName());
                if (columnDefinition == null) {
                    statusHandler.error("No column definition for \""
                            + sort.getColumnName() + "\".");
                    return;
                }
                attributeIdentifiers.add(columnDefinition.getFieldName());
                sortDirections.add(sort.getSortDirection());
                comparators.add(comparatorsForNames.get(sort.getColumnName()));
            }

            /*
             * Perform the bubble sort.
             */
            boolean lastChanged = true, changed = false;
            for (int numPasses = 1; lastChanged; numPasses++) {
                lastChanged = false;
                for (int j = 0; j < eventIdentifiers.size() - numPasses; ++j) {
                    int result = compareEvents(eventIdentifiers.get(j),
                            eventIdentifiers.get(j + 1), attributeIdentifiers,
                            sortDirections, comparators);
                    if (result > 0) {
                        String tempEventId = eventIdentifiers.get(j);
                        eventIdentifiers.set(j, eventIdentifiers.get(j + 1));
                        eventIdentifiers.set(j + 1, tempEventId);
                        lastChanged = true;
                        changed = true;
                    }
                }
            }

            /*
             * If this sort was the result of a mere direction change in one of
             * the sorts, iterate through the sorted items, finding any ranges
             * that are (from a sort perspective) equivalent, and swap those
             * ranges' orders. This needs to be done because otherwise, for
             * example, anytime the user changes the sort direction on a column
             * that has the same value for every row (or the same value for two
             * or more fields), the rows with equivalent values will not swap
             * vertical positions with respect to one another.
             */
            if (directionChanged) {
                int startOfRange = -1;
                List<Range<Integer>> ranges = new ArrayList<>();
                for (int j = 0; j < eventIdentifiers.size() - 1; j++) {
                    int result = compareEvents(eventIdentifiers.get(j),
                            eventIdentifiers.get(j + 1), attributeIdentifiers,
                            sortDirections, comparators);
                    if (result == 0) {
                        if (startOfRange == -1) {
                            startOfRange = j;
                        }
                    } else if (startOfRange != -1) {
                        ranges.add(Ranges.closed(startOfRange, j));
                        startOfRange = -1;
                    }
                }
                if (startOfRange != -1) {
                    ranges.add(Ranges.closed(startOfRange,
                            eventIdentifiers.size() - 1));
                }
                if (ranges.isEmpty() == false) {
                    changed = true;
                    for (Range<Integer> range : ranges) {
                        int start = range.lowerEndpoint();
                        int end = range.upperEndpoint();
                        int numSwaps = (end + 1 - start) / 2;
                        for (int j = 0; j < numSwaps; j++) {
                            String tempEventId = eventIdentifiers
                                    .get(start + j);
                            eventIdentifiers.set(start + j,
                                    eventIdentifiers.get(end - j));
                            eventIdentifiers.set(end - j, tempEventId);
                        }
                    }
                }
            }

            /*
             * If a change occurred and there are rows in the table, recreate
             * the rows as necessary to reorder them in a corresponding manner.
             */
            if (changed && (table.getItemCount() > 0)) {

                /*
                 * If there are existing table items, create a map of event
                 * identifiers to the corresponding items so that they can be
                 * sorted after the event dictionaries are sorted.
                 */
                Map<String, TableItem> tableItemsForEventIdentifiers = new HashMap<>(
                        table.getItemCount());
                for (TableItem item : table.getItems()) {
                    tableItemsForEventIdentifiers.put((String) item.getData(),
                            item);
                }

                /*
                 * Compile a set of the selected event identifiers so that they
                 * may be selected again later.
                 */
                TableItem[] selectedItems = table.getSelection();
                Set<Object> selectedIdentifiers = new HashSet<>();
                for (TableItem item : selectedItems) {
                    selectedIdentifiers.add(item.getData());
                }

                /*
                 * Retrieve a list of the controls, deleting the old table
                 * editors that these controls used.
                 */
                Map<String, Control> controlsForIdentifiers = new HashMap<>();
                for (String key : tableEditorsForIdentifiers.keySet()) {
                    TableEditor editor = tableEditorsForIdentifiers.get(key);
                    controlsForIdentifiers.put(key, editor.getEditor());
                    editor.dispose();
                }
                tableEditorsForIdentifiers.clear();

                /*
                 * Iterate through the newly-sorted event identifiers, moving
                 * each one's corresponding row to the appropriate place.
                 */
                for (int j = 0; j < eventIdentifiers.size(); j++) {

                    /*
                     * Get the event identifier, and the old row that went with
                     * it.
                     */
                    String identifier = eventIdentifiers.get(j);
                    TableItem oldItem = tableItemsForEventIdentifiers
                            .get(identifier);

                    /*
                     * Create the new row at the index at which it now belongs.
                     */
                    TableItem newItem = new TableItem(table, SWT.NONE, j);

                    /*
                     * Set the new row's state to match what the old row had.
                     */
                    newItem.setChecked(oldItem.getChecked());
                    newItem.setData(oldItem.getData());

                    /*
                     * For each column in the row, insert the text that is found
                     * in the old row's corresponding cell, and associate the
                     * field name for that column with it so that it may be
                     * easily changed if the hazard event is updated later.
                     */
                    for (int k = 0; k < visibleColumnNames.size(); k++) {
                        newItem.setText(k, oldItem.getText(k));
                    }

                    /*
                     * Get rid of the old row.
                     */
                    oldItem.dispose();
                }

                /*
                 * Iterate through the table items once more, placing the
                 * controls in new editors in the last column, and making a list
                 * of any that were previously selected.
                 */
                TableItem[] tableItems = table.getItems();
                selectedItems = new TableItem[selectedIdentifiers.size()];
                int selectedIndex = 0;
                for (TableItem item : tableItems) {
                    Control control = controlsForIdentifiers
                            .get(item.getData());
                    createTableEditorForTimeScale(control, item);
                    if (selectedIdentifiers.contains(item.getData())) {
                        selectedItems[selectedIndex++] = item;
                    }
                }

                /*
                 * Select the items that were previously selected.
                 */
                table.setSelection(selectedItems);
                selectedIndices = table.getSelectionIndices();

                /*
                 * Update countdown timers, since the sort may have changed the
                 * rows of any existing timers' cells.
                 */
                updateAllCountdownTimers();
            }
        }
    }

    /**
     * Compare the hazard events associated with the specified identifiers,
     * using the specified attributes, sort directions, and comparators,
     * returning less than 0, 0, or greater than 0 depending upon whether the
     * first event is less than, equal to, or greater than the second event,
     * respectively. The sorts are performed starting with the first parameters
     * in the attributes, sort directions, and comparators lists, and proceeding
     * to subsequent ones only if the sort by previous ones yields equality.
     * 
     * @param identifier1
     *            Identifier of the first event.
     * @param identifier2
     *            Identifier of the second event.
     * @param attributeIdentifiers
     *            List of identifiers of the hazard attributes to be used to
     *            make the comparison, in the order of sort priority (that is,
     *            the first item in the list identifiers the most important
     *            attribute by which to sort).
     * @param sortDirections
     *            List of directions of the sorts, in the order of sort
     *            priority.
     * @param comparators
     *            Comparators to be used to perform the sorts, in the order of
     *            sort priority; if a particular entry is <code>null</code>, the
     *            alert expiration times will be compared for that sort.
     * @return Integer that is less than 0, 0, or greater than 0, depending upon
     *         whether the first event is less than, equal to, or greater than
     *         the second event, respectively.
     */
    private int compareEvents(String identifier1, String identifier2,
            List<String> attributeIdentifiers,
            List<SortDirection> sortDirections, List<Comparator<?>> comparators) {

        /*
         * Perform the comparison starting with the highest-priority sort, and
         * proceeding on down from there only if the previous comparison yielded
         * an equality result.
         */
        for (int j = 0; j < attributeIdentifiers.size(); j++) {

            /*
             * Compare the objects using a comparator if supplied, or as alert
             * expiration times if not.
             */
            String attributeIdentifier = attributeIdentifiers.get(j);
            SortDirection sortDirection = sortDirections.get(j);
            Comparator<?> comparator = comparators.get(j);
            int result;
            if (comparator != null) {

                /*
                 * Get the objects to be compared; if they are equal (or both
                 * null, return the value indicating equality.
                 */
                Object value1 = dictsForEventIdentifiers.get(identifier1).get(
                        attributeIdentifier);
                Object value2 = dictsForEventIdentifiers.get(identifier2).get(
                        attributeIdentifier);
                if (value1 == value2) {
                    continue;
                }

                /*
                 * TODO: Remove this code; it is only needed while using
                 * attributes that have been JSONed and then deserialized, since
                 * any deserialized numbers are not guaranteed to be of any
                 * particular subclass of numbers when deserialized from JSON,
                 * but will be of explicit subtypes once attributes are no
                 * longer JSONified when passed to this object.
                 */
                if (value1 instanceof Number) {
                    value1 = ((Number) value1).doubleValue();
                }
                if (value2 instanceof Number) {
                    value2 = ((Number) value2).doubleValue();
                }

                /*
                 * Perform the comparison, returning the result if it is
                 * anything other than equality.
                 */
                result = compareFirstToSecond(sortDirection, value1, value2,
                        comparator);
            } else {

                /*
                 * Compare the alert expiration times.
                 */
                result = compareFirstToSecond(sortDirection,
                        getAlertExpirationTime(identifier1),
                        getAlertExpirationTime(identifier2));
            }

            /*
             * If the result is not equality, return it.
             */
            if (result != 0) {
                return result;
            }
        }

        /*
         * Return equality if all the sorts yielded such.
         */
        return 0;
    }

    /**
     * Compare the first and second values in the table.
     * 
     * @param sortDirection
     *            Direction of sorting within the table.
     * @param value1
     *            First value to be compared.
     * @param value2
     *            Second value to be compared.
     * @param comparator
     *            Comparator to be used.
     * @return A value less than, equal to, or greater than 0 indicating that
     *         the first value is less than, equal to, or greater than the
     *         second, respectively.
     */
    @SuppressWarnings("unchecked")
    private int compareFirstToSecond(SortDirection sortDirection,
            Object value1, Object value2, Comparator<?> comparator) {

        /*
         * Cast the objects and comparator appropriately.
         */
        Object typeable = (value1 == null ? value2 : value1);
        int result = 0;
        if (typeable instanceof String) {
            result = ((Comparator<? super String>) comparator).compare(
                    (String) value1, (String) value2);
        } else if (typeable instanceof Boolean) {
            result = ((Comparator<Boolean>) comparator).compare(
                    (Boolean) value1, (Boolean) value2);
        } else if (typeable instanceof Short) {
            result = ((Comparator<Short>) comparator).compare((Short) value1,
                    (Short) value2);
        } else if (typeable instanceof Integer) {
            result = ((Comparator<Integer>) comparator).compare(
                    (Integer) value1, (Integer) value2);
        } else if (typeable instanceof Long) {
            result = ((Comparator<Long>) comparator).compare((Long) value1,
                    (Long) value2);
        } else if (typeable instanceof Float) {
            result = ((Comparator<Float>) comparator).compare((Float) value1,
                    (Float) value2);
        } else if (typeable instanceof Double) {
            result = ((Comparator<Double>) comparator).compare((Double) value1,
                    (Double) value2);
        } else {
            result = ((Comparator<Date>) comparator).compare((Date) value1,
                    (Date) value2);
        }
        return result * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
    }

    /**
     * Compare the first and second values in the table.
     * 
     * @param sortDirection
     *            Direction of sorting within the table.
     * @param value1
     *            First value to be compared.
     * @param value2
     *            Second value to be compared.
     * @return A value less than, equal to, or greater than 0 indicating that
     *         the first value is less than, equal to, or greater than the
     *         second, respectively.
     */
    private int compareFirstToSecond(SortDirection sortDirection, long value1,
            long value2) {
        int result = (value1 > value2 ? 1 : (value1 < value2 ? -1 : 0));
        return result * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
    }

    /**
     * Get the expiration time of the alert for the specified event identifier.
     * 
     * @param eventId
     *            Event identifier for which to fetch the expiration time.
     * @return Expiration time as an epoch time in milliseconds, or
     *         {@link Long#MAX_VALUE} if there is no expiration time.
     */
    private long getAlertExpirationTime(String eventId) {
        if (countdownTimersDisplayManager == null) {
            return Long.MAX_VALUE;
        }
        return countdownTimersDisplayManager
                .getAlertExpirationTimeForEvent(eventId);
    }

    /**
     * Update any countdown timer cells that need updating.
     */
    private void updateCountdownTimers() {

        // Do nothing unless the countdown timer column is showing and the
        // countdown timer display manager exists.
        if ((temporalDisplayPanel == null) || temporalDisplayPanel.isDisposed()
                || (countdownTimersDisplayManager == null)) {
            return;
        }
        int columnIndex = getIndexOfColumnInTable(countdownTimerColumnName);
        if (columnIndex != -1) {

            // Find out which countdown timers need updating.
            Map<String, CountdownTimersDisplayManager.UpdateType> updateTypesForEventIdentifiers = countdownTimersDisplayManager
                    .getEventsNeedingUpdateAndRefreshRedrawTimes();

            // Iterate through the countdown timers, updating the
            // display of any that have corresponding table cells.
            Column columnDefinition = columnsForNames
                    .get(countdownTimerColumnName);
            TableItem[] items = table.getItems();
            for (String eventId : updateTypesForEventIdentifiers.keySet()) {

                // If this event is not found in the table, skip it.
                int rowIndex = eventIdentifiers.indexOf(eventId);
                if (rowIndex == -1) {
                    continue;
                }

                // Update the text and/or redraw the table cell, as
                // appropriate.
                CountdownTimersDisplayManager.UpdateType type = updateTypesForEventIdentifiers
                        .get(eventId);
                if ((type == CountdownTimersDisplayManager.UpdateType.TEXT)
                        || (type == CountdownTimersDisplayManager.UpdateType.TEXT_AND_COLOR)) {
                    updateCell(rowIndex, columnIndex, columnDefinition,
                            items[rowIndex]);
                }
                if ((type == CountdownTimersDisplayManager.UpdateType.COLOR)
                        || (type == CountdownTimersDisplayManager.UpdateType.TEXT_AND_COLOR)) {
                    Rectangle bounds = items[rowIndex].getBounds(columnIndex);
                    table.redraw(bounds.x - 1, bounds.y - 1, bounds.width + 1,
                            bounds.height + 1, false);
                }
            }

            // Force the table to redraw immediately, so that blinking
            // of any countdown timers that should blink occurs.
            table.update();

            // Schedule the next invocation of this method if there
            // is anything to be updated.
            countdownTimersDisplayManager
                    .scheduleNextDisplayUpdate(eventIdentifiers);
        }
    }

    /**
     * Update all countdown timer cells.
     */
    private void updateAllCountdownTimers() {

        // Do nothing unless the countdown timer column is showing and the
        // countdown timer display manager exists.
        if ((temporalDisplayPanel == null) || temporalDisplayPanel.isDisposed()
                || (countdownTimersDisplayManager == null)) {
            return;
        }

        // Turn off redraw, update all the cells, and turn redraw back on
        // to force redrawing in case of blinking cells.
        table.setRedraw(false);
        updateCellsForColumn(countdownTimerColumnName);
        table.setRedraw(true);
        table.update();

        // Calculate the next display update time for each of the countdown
        // timers.
        countdownTimersDisplayManager.refreshAllRedrawTimes();

        // Schedule the next countdown timer column redraw.
        countdownTimersDisplayManager
                .scheduleNextDisplayUpdate(eventIdentifiers);
    }

    /**
     * Retrieves the text to display in Hazard Event Table fields which are
     * empty.
     * 
     * @param columnDefinition
     * @return The text to display in this field if it is empty.
     */
    private String getEmptyFieldText(Column columnDefinition) {
        if (columnDefinition.getDisplayEmptyAs() != null) {
            return columnDefinition.getDisplayEmptyAs();
        } else {
            return EMPTY_STRING;
        }
    }

    /**
     * Create the hazard menu
     */
    private void createHazardMenu() {
        ContextMenuHelper helper = new ContextMenuHelper(presenter,
                presenter.getSessionManager());
        List<MenuItem> items = new ArrayList<>();
        for (IContributionItem item : helper.getSelectedHazardManagementItems()) {
            MenuItem menuItem = null;
            if (item instanceof ActionContributionItem) {
                ActionContributionItem actionItem = (ActionContributionItem) item;
                menuItem = new MenuItem(rowMenu, SWT.PUSH);
                menuItem.setText(actionItem.getAction().getText());
                menuItem.setData(actionItem.getAction());
                menuItem.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Action act = (Action) ((MenuItem) e.widget).getData();
                        act.run();
                    }
                });
            } else if (item instanceof Separator) {
                menuItem = new MenuItem(rowMenu, SWT.SEPARATOR);
            }
            items.add(menuItem);
        }
    }
}
