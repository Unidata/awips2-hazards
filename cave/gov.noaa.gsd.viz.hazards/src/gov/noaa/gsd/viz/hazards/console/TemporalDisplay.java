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

import gov.noaa.gsd.common.utilities.DateStringComparator;
import gov.noaa.gsd.common.utilities.LongStringComparator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.action.SettingsAction;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.setting.SettingsView;
import gov.noaa.gsd.viz.hazards.toolbar.ComboAction;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;
import gov.noaa.gsd.viz.megawidgets.IMenuSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;
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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.Action;
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
import org.eclipse.swt.widgets.ToolTip;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

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
 * June 4, 2013            Chris.Golden      Added support for changing background
 *                                           and foreground colors in order to stay
 *                                           in synch with CAVE mode. Also tightened
 *                                           up area around timeline ruler to make
 *                                           the column header border show up around
 *                                           it as it should.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
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
     * Selected time mode choices.
     */
    public static final ImmutableList<String> SELECTED_TIME_MODE_CHOICES = ImmutableList
            .of(SELECTED_TIME_MODE_SINGLE, SELECTED_TIME_MODE_RANGE);

    /**
     * Toolbar button icon image file names.
     */
    public static final ImmutableList<String> TOOLBAR_BUTTON_IMAGE_FILE_NAMES = ImmutableList
            .of("timeZoomOut.png", "timeJumpBackward.png", "timeBackward.png",
                    "timeCurrent.png", "timeForward.png",
                    "timeJumpForward.png", "timeZoomIn.png");

    /**
     * Descriptions of the toolbar buttons, each of which corresponds to the
     * file name of the button at the same index in <code>
     * TOOLBAR_BUTTON_IMAGE_FILE_NAMES</code>.
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
     * the same index in <code>BUTTON_IMAGE_NAMES</code>.
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
    private static final String DATE_TIME_FORMAT_STRING = "EEE dd-MMM HH:mm";

    /**
     * Filter menu name.
     */
    private static final String FILTER_MENU_NAME = "Filter";

    /**
     * Text displayed in the column header for the time scale widgets.
     */
    private static final String TIME_SCALE_COLUMN_NAME = "Time Scale";

    /**
     * Show time under mouse toggle menu text.
     */
    private static final String SHOW_TIME_UNDER_MOUSE_TOGGLE_MENU_TEXT = "Show Time Under Mouse";

    /**
     * Width in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_WIDTH = 3;

    /**
     * Height in pixels of the margin used in the form layout.
     */
    private static final int FORM_MARGIN_HEIGHT = 3;

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
     * widgets.
     */
    private static final int TIME_HORIZONTAL_PADDING = 10;

    /**
     * Height of vertical padding in pixels above and below the ruler.
     */
    private static final int RULER_VERTICAL_PADDING = 0;

    /**
     * Height of vertical padding in pixels above and below time scales.
     */
    private static final int SCALE_VERTICAL_PADDING = 3;

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
     * The default table width value.
     */
    private static final int TABLE_WIDTH = 700;

    /**
     * The default table height value.
     */
    private static final int TABLE_HEIGHT = 145;

    /**
     * The default height of the button panel.
     */
    private static final int BUTTON_PANEL_HEIGHT = 40;

    /**
     * The default height of the table header.
     */
    private static final int TABLE_COLUMN_HEADER_HEIGHT = 42;

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(TemporalDisplay.class);

    /**
     * Not applicable entry in table.
     */
    private static final String NOT_APPLICABLE = "N/A";

    /**
     * Key into filter megawidget definition used to find the column name with
     * which the megawidget should be associated.
     */
    private static final String COLUMN_NAME = "columnName";

    /**
     * Path to icons.
     */
    private static final String ICONS_PATH;

    // Initialize the icons path.
    static {
        String iconsPath = null;
        try {
            iconsPath = FileLocator.resolve(
                    HazardServicesActivator.getDefault().getBundle()
                            .getEntry("icons")).getPath();
        } catch (Exception e) {
            statusHandler.error("<static init>: Will not be able to load "
                    + "button icons because couldn't resolve location: " + e);
        }
        ICONS_PATH = iconsPath;
    }

    /**
     * Up sort arrow image file.
     */
    private static final String UP_SORT_ARROW_IMAGE_FILE_NAME = "upSortArrow.png";

    /**
     * Down sort arrow image file.
     */
    private static final String DOWN_SORT_ARROW_IMAGE_FILE_NAME = "downSortArrow.png";

    // Private Constants

    /**
     * Ruler border color; this is instance- rather than class-scoped so that it
     * may be disposed of when the widget is disposed.
     */
    private final Color RULER_BORDER_COLOR = new Color(Display.getCurrent(),
            131, 120, 103);

    // Private Variables

    /**
     * Table tool tip, used to display hint text for table cells.
     */
    private ToolTip tableToolTip = null;

    /**
     * Flag indicating whether or not the timeline ruler should display tooltips
     * for all times along its length.
     */
    private boolean showRulerToolTipsForAllTimes = true;

    /**
     * Spacer image, used to make space in column headers for an arrow image to
     * show sort direction, when a column is the sorting column, and to ensure
     * the columns are tall enough to handle the time ruler embedded in the last
     * column header.
     */
    private Image spacerImage = null;

    /**
     * Up arrow image, used to show that a column is sorting upwards.
     */
    private Image upArrowImage = null;

    /**
     * Down arrow image, used to show that a column is sorting downwards.
     */
    private Image downArrowImage = null;

    /**
     * Set of basic resources created for use in this window, to be disposed of
     * when this window is disposed of.
     */
    private final Set<Resource> resources = Sets.newHashSet();

    /**
     * Map of RGB triplets, each consisting of a string of three integers each
     * separated from the next by a space, to the corresponding colors created
     * for use as visual time range indicators. .
     */
    private final Map<String, Color> timeRangeColorsForRGBs = Maps.newHashMap();

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
    private final Map<String, Button> buttonsForIdentifiers = Maps.newHashMap();

    /**
     * Map of button identifiers to the associated toolbar navigation actions.
     */
    private final Map<String, Action> actionsForButtonIdentifiers = Maps
            .newHashMap();

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
     * Selected time as epoch time in milliseconds.
     */
    private long selectedTime;

    /**
     * Start time of the time range, as epoch time in milliseconds. The time
     * range is only used if in time range mode.
     */
    private long timeRangeStart = -1;

    /**
     * End time of the time range, as epoch time in milliseconds. The time range
     * is only used if in time range mode.
     */
    private long timeRangeEnd = -1;

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
     * List of hazard event dictionaries used to populate the table.
     */
    private final List<Dict> eventDictList;

    /**
     * Indices of items that are currently selected.
     */
    private int[] selectedIndices = null;

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
     * Map pairing column names with megawidget managers for those columns that
     * have megawidget-bearing context-sensitive menus associated with their
     * headers.
     */
    private Map<String, MegawidgetManager> headerMegawidgetManagersForColumnNames;

    /**
     * A dictionary containing the current dynamic setting.
     */
    private Dict dynamicSetting;

    /**
     * List of visible column names. The order of this list is the order in
     * which the columns appear in the table, meaning that when the <code>
     * table.getColumnOrder()</code> method is called, the name of the column
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
     * Map of column identifiers to the corresponding column names.
     */
    private final Map<String, String> columnNamesForIdentifiers;

    /**
     * Map of column names to definitions of the corresponding columns. Each
     * definition is a dictionary of key-value pairs defining that column.
     */
    private Dict columnDefinitionsForNames;

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
     * Flag indicating whether or not a notification of dynamic setting
     * modification is scheduled to occur.
     */
    private boolean willNotifyOfSettingChange = false;

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
            if (source == MultiValueScale.ChangeSource.USER_GUI_INTERACTION) {

                // Get the table item that goes with this scale
                // widget, and from it, get the event identifier
                // that it represents.
                TableItem item = (TableItem) widget.getData();
                String eventID = (String) item.getData();

                // Save the new start and end times in the event
                // dictionary.
                for (Dict eventDict : eventDictList) {
                    if (eventDict.get(Utilities.HAZARD_EVENT_IDENTIFIER)
                            .equals(eventID)) {
                        eventDict.put(Utilities.HAZARD_EVENT_START_TIME,
                                values[0]);
                        eventDict.put(Utilities.HAZARD_EVENT_END_TIME,
                                values[1]);
                        break;
                    }
                }

                // Change the start and end time text in the table
                // row, if the columns are showing.
                String[] columnIdentifiers = {
                        Utilities.HAZARD_EVENT_START_TIME,
                        Utilities.HAZARD_EVENT_END_TIME };
                for (int j = 0; j < columnIdentifiers.length; j++) {
                    String columnName = columnNamesForIdentifiers
                            .get(columnIdentifiers[j]);
                    int columnIndex = getIndexOfColumnInTable(columnName);
                    if (columnIndex != -1) {
                        String value = convertToCellValue(values[j],
                                (Dict) columnDefinitionsForNames
                                        .get(columnName));
                        item.setText(columnIndex,
                                (value == null ? NOT_APPLICABLE : value));
                    }
                }

                // Notify listeners of the change.
                fireConsoleActionOccurred(new ConsoleAction(
                        "EventTimeRangeChanged", eventID,
                        Long.toString(values[0]), Long.toString(values[1])));
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
        @SuppressWarnings("unchecked")
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

                // If the newly added column is the sort column,
                // sort by its cells' contents and update the images
                // in the column headers to reflect this.
                if (table.getColumn(table.getColumnCount() - 2) == table
                        .getSortColumn()) {
                    sortRowsByColumn(table.getColumnCount() - 2,
                            (Comparator<? super String>) table.getSortColumn()
                                    .getData());
                    updateTableColumnSortImages();
                }
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

            // Update the table column headers' sort images.
            updateTableColumnSortImages();

            // Finish up following the addition or removal of
            // a column.
            ignoreResize = lastIgnoreResize;
            ignoreMove = lastIgnoreMove;
            visibleColumnCountChanged();

            // Notify listeners of the setting change.
            scheduleNotificationOfSettingDefinitionChange();
        }
    };

    /**
     * Sort listener, used to listen for column-based sorts.
     */
    private final SelectionListener sortListener = new SelectionAdapter() {
        @SuppressWarnings("unchecked")
        @Override
        public void widgetSelected(SelectionEvent e) {

            // If the column that will not be the sorting column is
            // the same as the previous one, then toggle the sorting
            // direction; otherwise, start with an upward sort.
            int tableSortDirection = table.getSortDirection();
            TableColumn sortColumn = (TableColumn) e.widget;
            if (sortColumn == table.getSortColumn()) {
                table.setSortDirection(tableSortDirection == SWT.UP ? SWT.DOWN
                        : SWT.UP);
            } else {
                table.setSortDirection(SWT.UP);
            }

            // Set the column by which to sort.
            table.setSortColumn(sortColumn);

            // Perform the sort.
            sortRowsByColumn(table.indexOf(sortColumn),
                    (Comparator<? super String>) sortColumn.getData());

            // Set the images in the columns to indicate which column
            // is the sorting column, and which direction the sort is
            // in.
            updateTableColumnSortImages();

            // Update the sort column information.
            updateTableSortColumnInSettingDefinition();
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public TemporalDisplay() {

        // Read in the up- and down-arrow images for indicating the
        // direction of a sorting column.
        BufferedImage[] sourceImages = null;
        try {
            sourceImages = new BufferedImage[] {
                    ImageIO.read((new File(ICONS_PATH + File.separator
                            + UP_SORT_ARROW_IMAGE_FILE_NAME)).toURI().toURL()),
                    ImageIO.read((new File(ICONS_PATH + File.separator
                            + DOWN_SORT_ARROW_IMAGE_FILE_NAME)).toURI().toURL()) };
        } catch (Exception e) {
            statusHandler
                    .error("<init>: Cannot find up- and down-arrow icon images: "
                            + e);
        }

        // Create a spacer image to take the place of the arrow
        // images for non-sorting columns.
        BufferedImage finalImage = new BufferedImage((sourceImages == null ? 1
                : sourceImages[0].getWidth()), TABLE_COLUMN_HEADER_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);
        spacerImage = ImageUtilities.convertAwtImageToSwt(finalImage);
        resources.add(spacerImage);

        // If the arrow images were successfully loaded, paint them
        // onto larger transparent images with the same height as the
        // column headers to be created in the table. This is done
        // using the AWT BufferedImage class to allow for easy trans-
        // parent image creation.
        if (sourceImages != null) {
            for (BufferedImage sourceImage : sourceImages) {
                finalImage = new BufferedImage(sourceImage.getWidth(),
                        TABLE_COLUMN_HEADER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                Graphics gc = finalImage.getGraphics();
                gc.drawImage(sourceImage, 0,
                        (finalImage.getHeight() - sourceImage.getHeight()) / 2,
                        null);
                gc.dispose();
                if (upArrowImage == null) {
                    upArrowImage = ImageUtilities
                            .convertAwtImageToSwt(finalImage);
                    resources.add(upArrowImage);
                } else {
                    downArrowImage = ImageUtilities
                            .convertAwtImageToSwt(finalImage);
                    resources.add(downArrowImage);
                }
            }
        } else {
            upArrowImage = downArrowImage = spacerImage;
        }

        // Create the various lists and dictionaries required.
        eventDictList = Lists.newArrayList();
        columnDefinitionsForNames = new Dict();
        columnNamesForIdentifiers = Maps.newHashMap();
        visibleColumnNames = Lists.newArrayList();
        hintTextIdentifiersForVisibleColumnNames = Maps.newHashMap();
        dateIdentifiersForVisibleColumnNames = Maps.newHashMap();
        dynamicSetting = new Dict();
        tableEditorsForIdentifiers = Maps.newHashMap();

        // Configure the date-time formatter.
        dateTimeFormatter = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // Public Methods

    /**
     * Initialize the display.
     * 
     * @param presenter
     *            Presenter managing the view to which this display belongs.
     * @param selectedTime
     *            Selected time as epoch time in milliseconds.
     * @param currentTime
     *            Current time as epoch time in milliseconds.
     * @param visibleTimeRange
     *            Amount of time visible at once in the time line as an epoch
     *            time range in milliseconds.
     * @param hazardEvents
     *            JSON string holding a list of hazard events.
     * @param filterMegawidgets
     *            JSON string holding a list of dictionaries providing filter
     *            megawidget specifiers.
     * @param showControlsInToolBar
     *            Flag indicating whether the controls (navigation buttons,
     *            etc.) are to be shown in the toolbar. If <code>false</code>,
     *            they are provided at the bottom of this composite instead.
     */
    public void initialize(ConsolePresenter presenter, long selectedTime,
            long currentTime, long visibleTimeRange, String hazardEvents,
            String filterMegawidgets, boolean showControlsInToolBar) {

        // Remember the presenter.
        this.presenter = presenter;

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
        this.selectedTime = selectedTime;
        this.currentTime = currentTime;
        this.visibleTimeRange = visibleTimeRange;

        // Set the ruler's visible time range, selected time, and
        // current time. Also send the visible time range back as a
        // model change.
        long lowerTime = currentTime - (visibleTimeRange / 4L);
        if (lowerTime < Utilities.MIN_TIME) {
            lowerTime = Utilities.MIN_TIME;
        }
        long upperTime = lowerTime + visibleTimeRange - 1L;
        if (upperTime > Utilities.MAX_TIME) {
            lowerTime -= upperTime - Utilities.MAX_TIME;
            upperTime = Utilities.MAX_TIME;
        }
        ruler.setVisibleValueRange(lowerTime, upperTime);
        fireConsoleActionOccurred(new ConsoleAction(
                ("VisibleTimeRangeChanged"), Long.toString(lowerTime),
                Long.toString(upperTime)));
        ruler.setFreeMarkedValues(currentTime);
        ruler.setFreeThumbValues(selectedTime);

        // Use the provided hazard events, clearing the old ones first
        // in case this is a re-initialization.
        clearEvents();
        setComponentData(hazardEvents, filterMegawidgets);

        // Add the mouse wheel filter, used to handle mouse wheel
        // events properly when they should apply to the table.
        table.getDisplay().addFilter(SWT.MouseWheel, mouseWheelFilter);
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
     * 
     * @param currentTime
     *            JSON string holding the current time as an epoch time in
     *            milliseconds.
     */
    public void updateCurrentTime(String currentTime) {
        this.currentTime = Long.parseLong(currentTime);
        if (ruler.isDisposed() == false) {
            ruler.setFreeMarkedValue(0, this.currentTime);
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                ((MultiValueScale) tableEditor.getEditor()).setFreeMarkedValue(
                        0, this.currentTime);
            }
        }
    }

    /**
     * Update the selected time.
     * 
     * @param selectedTime
     *            JSON string holding the selected time as an epoch time in
     *            milliseconds.
     */
    public void updateSelectedTime(String selectedTime) {

        // Set the selected time.
        long time = Long.parseLong(selectedTime);
        ruler.setFreeThumbValue(0, time);

        // Ensure that the selected time is visible, and not just at
        // the edge of the ruler.
        long lower = ruler.getLowerVisibleValue();
        long upper = ruler.getUpperVisibleValue();
        long range = upper + 1L - lower;
        if ((time < lower + (range / 8L)) || (time > upper - (range / 8L))) {
            lower = time - (range / 2L);
            upper = lower + range - 1L;
            setVisibleTimeRange(lower, upper, true);
        }
    }

    /**
     * Update the selected time range.
     * 
     * @param range
     *            JSON string holding a list with two elements: the start time
     *            of the selected time range epoch time in milliseconds, and the
     *            end time of the selected time range epoch time in
     *            milliseconds.
     */
    public void updateSelectedTimeRange(String range) {
        DictList rangeList = DictList.getInstance(range);
        String startTime = rangeList.getDynamicallyTypedValue(0);
        String endTime = rangeList.getDynamicallyTypedValue(1);
        ruler.setConstrainedThumbValues(Long.parseLong(startTime),
                Long.parseLong(endTime));
    }

    /**
     * Get the list of the current hazard events.
     * 
     * @return List of the current hazard events.
     */
    public List<Dict> getEvents() {
        return eventDictList;
    }

    /**
     * Get the dictionary defining the dynamic setting currently in use.
     * 
     * @return Dictionary defining the dynamic setting currently in use.
     */
    public Dict getDynamicSetting() {
        return dynamicSetting;
    }

    /**
     * Set the specified hazard events as the events to be shown in the table.
     * 
     * @param hazardEvents
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding an event as a set of key-value pairs.
     */
    public void setComponentData(String hazardEvents) {
        setComponentData(hazardEvents, null);
    }

    /**
     * Clear all hazard events.
     */
    public void clearEvents() {
        if (table.isDisposed() == false) {
            table.removeAll();
        }
        eventDictList.clear();
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
                .getDynamicallyTypedValue(Utilities.HAZARD_EVENT_IDENTIFIER);
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
            if (item.getData().equals(
                    dict.get(Utilities.HAZARD_EVENT_IDENTIFIER))) {

                // Iterate through the changed keys, making the
                // corresponding changes to the row. If the start
                // or end time have changed, make a note of it.
                long startTime = -1L, endTime = -1L;
                for (String key : dict.keySet()) {

                    // If the checked state changed, check or un-
                    // check the item; if the color changed, alter
                    // the color of the range between the two
                    // time scale thumbs; if the selected state
                    // changed, select or deselect the item; other-
                    // wise, as long as the key is not the event
                    // identifier, change the text to match.
                    if (key.equals(Utilities.HAZARD_EVENT_CHECKED)) {
                        item.setChecked((Boolean) dict
                                .get(Utilities.HAZARD_EVENT_CHECKED));
                        continue;
                    } else if (key.equals(Utilities.HAZARD_EVENT_COLOR)) {
                        Color color = getTimeRangeColorForRGB((String) dict
                                .get(key));
                        scale.setConstrainedThumbRangeColor(1, color);
                    } else if (key.equals(Utilities.HAZARD_EVENT_SELECTED)) {

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
                    } else if (!key.equals(Utilities.HAZARD_EVENT_IDENTIFIER)) {

                        // Change the text to match the new value.
                        String columnName = columnNamesForIdentifiers.get(key);
                        if (columnName != null) {
                            int columnIndex = getIndexOfColumnInTable(columnName);
                            if (columnIndex != -1) {
                                Dict columnDefinition = (Dict) columnDefinitionsForNames
                                        .get(columnName);
                                String value = convertToCellValue(
                                        dict.getDynamicallyTypedValue(key),
                                        columnDefinition);
                                item.setText(
                                        columnIndex,
                                        (value == null ? NOT_APPLICABLE : value));
                            }
                        }

                        // If the changed value is the start or
                        // end time, make a note of it.
                        if (key.equals(Utilities.HAZARD_EVENT_START_TIME)) {
                            startTime = ((Number) dict
                                    .get(Utilities.HAZARD_EVENT_START_TIME))
                                    .longValue();
                        } else if (key.equals(Utilities.HAZARD_EVENT_END_TIME)) {
                            endTime = ((Number) dict
                                    .get(Utilities.HAZARD_EVENT_END_TIME))
                                    .longValue();
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
            ruler.setConstrainedThumbValues();
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                ((MultiValueScale) tableEditor.getEditor())
                        .setConstrainedMarkedValues();
            }
            fireConsoleActionOccurred(new ConsoleAction(
                    "SelectedTimeRangeChanged", Long.toString(-1L),
                    Long.toString(-1L)));
        } else {
            if (timeRangeStart == -1) {
                timeRangeStart = selectedTime;
                timeRangeEnd = selectedTime + (HOUR_INTERVAL * 4L);
            }
            ruler.setConstrainedThumbValues(timeRangeStart, timeRangeEnd);
            ruler.setConstrainedThumbColor(0, timeRangeEdgeColor);
            ruler.setConstrainedThumbColor(1, timeRangeEdgeColor);
            ruler.setConstrainedThumbRangeColor(1, timeRangeFillColor);
            for (TableEditor tableEditor : tableEditorsForIdentifiers.values()) {
                MultiValueScale scale = (MultiValueScale) tableEditor
                        .getEditor();
                scale.setConstrainedMarkedValues(timeRangeStart, timeRangeEnd);
                scale.setConstrainedMarkedValueColor(0, timeRangeEdgeColor);
                scale.setConstrainedMarkedValueColor(1, timeRangeEdgeColor);
                scale.setConstrainedMarkedRangeColor(1, timeRangeFillColor);
            }
            fireConsoleActionOccurred(new ConsoleAction(
                    "SelectedTimeRangeChanged", Long.toString(timeRangeStart),
                    Long.toString(timeRangeEnd)));
        }
    }

    // Private Methods

    /**
     * Set the specified hazard events as the events to be shown in the table.
     * 
     * @param hazardEvents
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding an event as a set of key-value pairs.
     * @param filterMegawidgets
     *            JSON string holding an array of dictionaries, each of the
     *            latter holding a filter megawidget as a set of key-value
     *            pairs. This needs to be be present only the first time this
     *            method is called, in order to construct the context menus; it
     *            is ignored otherwise.
     */
    private void setComponentData(String hazardEvents, String filterMegawidgets) {

        // Prepare for the addition and/or removal of columns.
        boolean lastIgnoreResize = ignoreResize;
        boolean lastIgnoreMove = ignoreMove;
        ignoreResize = true;
        ignoreMove = true;
        visibleColumnCountChanging();

        // Remove the sorting column, if any.
        table.setSortColumn(null);

        // Update the events and setting information.
        parseHazardEventsFromJSON(hazardEvents);

        // If the header menus for the various columns have not yet been
        // created, create them now.
        if (headerMenusForColumnNames == null) {

            // Get a list of all the column names for which there are
            // definitions, sorted alphabetically.
            List<String> columnNames = Lists
                    .newArrayList(columnDefinitionsForNames.keySet());
            Collections.sort(columnNames);

            // Copy the list of column names to another list that will be
            // used to track which columns have associated header menus
            // in the loop below.
            List<String> columnNamesToBeGivenMenus = Lists
                    .newArrayList(columnNames);

            // Get the list of provided event filters.
            DictList filters = DictList.getInstance(filterMegawidgets);

            // Create the mapping of column names to their header menus.
            // Make a menu for each column that has an associated filter,
            // making each such menu have both the checklist of column
            // names and the filter, and then make one more menu with
            // just the column names checklist for all the columns that
            // do not have associated filters.
            headerMenusForColumnNames = Maps.newHashMap();
            headerMegawidgetManagersForColumnNames = Maps.newHashMap();
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

                    // If a filter exists for this menu, add it and associate
                    // it with the column. Otherwise, the menu being created
                    // is a catch-all for any column that does not have an
                    // associated filter.
                    if (filterColumnName != null) {
                        try {
                            headerMegawidgetManagersForColumnNames.put(
                                    filterColumnName, new MegawidgetManager(
                                            menu, Lists.newArrayList(filter),
                                            dynamicSetting) {
                                        @Override
                                        protected void commandInvoked(
                                                String identifier,
                                                String extraCallback) {

                                            // No action.
                                        }

                                        @Override
                                        protected void stateElementChanged(
                                                String identifier, Object state) {

                                            // Special case: A translation has
                                            // to be made between the hazard
                                            // categories and types tree
                                            // structure that the user is
                                            // creating as state and the old
                                            // hazard categories list and hazard
                                            // types list. This should be
                                            // removed if we can get rid of the
                                            // visibleTypes and
                                            // hidHazardCategories lists in the
                                            // dynamic setting.
                                            SettingsView
                                                    .translateHazardCategoriesAndTypesToOldLists(dynamicSetting);

                                            // Forward the dynamic setting
                                            // change to the
                                            // presenter.
                                            notifyListenersOfSettingDefinitionChange();
                                        }
                                    });
                        } catch (MegawidgetException e) {
                            statusHandler
                                    .error("TemporalDisplay.setComponentData(): Unable to create megawidget "
                                            + "manager due to megawidget construction problem.",
                                            e);
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
            }
        }

        // Make a list of the column names remaining in the table, and
        // update the widths of these columns based on the widths that
        // were provided, as well as making the column the sort column
        // if that is appropriate.
        List<String> columnNames = Lists.newArrayList();
        for (int j = 0; j < table.getColumnCount(); ++j) {
            TableColumn column = table.getColumn(j);
            String columnName = column.getText();
            if (columnName.equals(TIME_SCALE_COLUMN_NAME)) {
                continue;
            }
            columnNames.add(columnName);
            Dict columnDefinition = columnDefinitionsForNames
                    .getDynamicallyTypedValue(columnName);
            if (columnDefinition != null) {
                Number width = columnDefinition
                        .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_WIDTH);
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

        // Ensure that the checkboxes in the table's rows are in the
        // leftmost column.
        ensureCheckboxesAreInLeftmostColumn(false);

        // Update the table column headers' sort images.
        updateTableColumnSortImages();

        // Sort the incoming event dictionaries based on the table's
        // sort column and direction.
        if (eventDictList.size() > 0) {
            sortEventData();
            createTableRowsFromEventData();
        }

        // Finish up following the addition and/or removal of columns.
        ignoreResize = lastIgnoreResize;
        ignoreMove = lastIgnoreMove;
        visibleColumnCountChanged();
    }

    /**
     * Perform any disposal tasks internally.
     */
    private void disposeInternal() {

        // Clear all events.
        clearEvents();

        // Dispose of any resources that were created.
        for (Resource resource : resources) {
            resource.dispose();
        }

        // Delete the table tooltip.
        if ((tableToolTip != null) && !tableToolTip.isDisposed()) {
            tableToolTip.dispose();
            tableToolTip = null;
        }

        // Remove the mouse wheel filter.
        table.getDisplay().removeFilter(SWT.MouseWheel, mouseWheelFilter);
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
            // client area, which should give the scrollbar width.
            Rectangle tableBounds = table.getBounds();
            int cellTableEndXDiff = tableBounds.width
                    - table.getClientArea().width;

            // Determine the horizontal boundaries of the ruler.
            int cellBeginXPixels = tableBounds.width - columnWidth
                    - cellTableEndXDiff;
            int cellEndXPixels = cellTableEndXDiff;

            // Set up the layout data for the ruler.
            FormData rulerFormData = new FormData();
            rulerFormData.left = new FormAttachment(0, cellBeginXPixels
                    + FORM_MARGIN_WIDTH);
            rulerFormData.top = new FormAttachment(0, rulerTopOffset);
            rulerFormData.right = new FormAttachment(100, -1 * cellEndXPixels);
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

    /**
     * Set the images in the column headers to indicate which direction the sort
     * is in for the sorting column, and that the other columns are not being
     * used for sorting.
     */
    private void updateTableColumnSortImages() {
        TableColumn sortColumn = table.getSortColumn();
        for (TableColumn column : table.getColumns()) {
            column.setImage(sortColumn == column ? (table.getSortDirection() == SWT.DOWN ? downArrowImage
                    : upArrowImage)
                    : spacerImage);
        }
    }

    /**
     * Parse the specified JSON string into a list of hazard event dictionaries.
     * 
     * @param hazardEvents
     *            JSON string holding the hazard event dictionaries as a list.
     */
    private void parseHazardEventsFromJSON(String hazardEvents) {

        // Get the list of hazard events, the dynamic setting, the
        // column definitions, and the visible column names. Also
        // determine which of the visible columns may generate hint
        // text, and which hold date values.
        Dict hazardEventData = Dict.getInstance(hazardEvents);
        List<Dict> eventArray = hazardEventData
                .getDynamicallyTypedValue(Utilities.TEMPORAL_DISPLAY_EVENTS);
        dynamicSetting = hazardEventData
                .getDynamicallyTypedValue(Utilities.TEMPORAL_DISPLAY_DYNAMIC_SETTING);
        columnDefinitionsForNames = dynamicSetting
                .getDynamicallyTypedValue(Utilities.SETTING_COLUMNS);
        visibleColumnNames = dynamicSetting
                .getDynamicallyTypedValue(Utilities.SETTING_VISIBLE_COLUMNS);
        hintTextIdentifiersForVisibleColumnNames.clear();
        dateIdentifiersForVisibleColumnNames.clear();
        for (String columnName : visibleColumnNames) {
            determineSpecialPropertiesOfColumn(columnName);
        }

        // com.google.gson.Gson gson =
        // gov.noaa.gsd.viz.hazards.jsonutilities.JSONUtilities
        // .createPrettyGsonInterpreter();
        // statusHandler.debug(gson.toJson(eventArray));

        // Compile a mapping of column identifiers to their
        // names.
        columnNamesForIdentifiers.clear();
        for (String name : columnDefinitionsForNames.keySet()) {
            columnNamesForIdentifiers.put(
                    (String) ((Dict) columnDefinitionsForNames.get(name))
                            .get(Utilities.SETTING_COLUMN_IDENTIFIER), name);
        }

        // Add each hazard event to the list of event dictio-
        // naries.
        if (eventArray != null) {
            numberOfRows = eventArray.size();
            for (int j = 0; j < eventArray.size(); ++j) {
                Dict dict = eventArray.get(j);
                eventDictList.add(dict);
            }
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
        for (Dict eventDict : eventDictList) {
            if (eventDict.get(Utilities.HAZARD_EVENT_IDENTIFIER).equals(
                    toBeMerged.get(Utilities.HAZARD_EVENT_IDENTIFIER))) {
                for (String key : toBeMerged.keySet()) {
                    if (key.equals(Utilities.HAZARD_EVENT_IDENTIFIER)) {
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
        for (Dict eventDict : eventDictList) {
            eventDict
                    .put(Utilities.HAZARD_EVENT_SELECTED, identifiers
                            .contains(eventDict
                                    .get(Utilities.HAZARD_EVENT_IDENTIFIER)));
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
     * @param color
     *            Event color.
     * @param startTime
     *            Start time of the event, as epoch time in milliseconds.
     * @param endTime
     *            End time of the event, as epoch time in milliseconds.
     * @return Time scale widget.
     */
    private MultiValueScale createTimeScale(TableItem item, Color color,
            long startTime, long endTime) {

        // Create a time scale widget with two thumbs and configure it
        // appropriately.
        MultiValueScale scale = new MultiValueScale(table, Utilities.MIN_TIME,
                Utilities.MAX_TIME);
        scale.setSnapValueCalculator(snapValueCalculator);
        scale.setTooltipTextProvider(thumbTooltipTextProvider);
        scale.setInsets(TIME_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING,
                TIME_HORIZONTAL_PADDING, SCALE_VERTICAL_PADDING);
        scale.setComponentDimensions(SCALE_THUMB_WIDTH, SCALE_THUMB_HEIGHT,
                SCALE_TRACK_THICKNESS);
        scale.setVisibleValueRange(ruler.getLowerVisibleValue(),
                ruler.getUpperVisibleValue());
        scale.setConstrainedThumbValues(startTime, endTime);
        scale.setConstrainedThumbRangeColor(1, color);
        scale.setFreeMarkedValues(currentTime, selectedTime);
        scale.setFreeMarkedValueColor(0, currentTimeColor);
        scale.setFreeMarkedValueColor(1, selectedTimeColor);
        if (selectedTimeMode.equals(SELECTED_TIME_MODE_RANGE)) {
            scale.setConstrainedMarkedValues(timeRangeStart, timeRangeEnd);
            scale.setConstrainedMarkedValueColor(0, timeRangeEdgeColor);
            scale.setConstrainedMarkedValueColor(1, timeRangeEdgeColor);
            scale.setConstrainedMarkedRangeColor(1, timeRangeFillColor);
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
        scaleEditor.minimumHeight = table.getItemHeight() + 3;
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
        tableFormData.height = TABLE_HEIGHT;
        tableFormData.width = TABLE_WIDTH;
        table.setLayoutData(tableFormData);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setBackground(Display.getCurrent().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));

        // Add a listener for check and uncheck events that
        // updates the event dictionary to match, and notifies
        // any listeners of the event, as well as updating the
        // list of selected items whenever a selection or de-
        // selection occurs.
        table.addSelectionListener(new SelectionAdapter() {
            private int lastCheckEventTime = 0;

            @Override
            public void widgetSelected(SelectionEvent e) {

                // If the event is a check event, handle it as
                // such, and remember its timestamp. If it is
                // a selection event, see if the timestamp is
                // the same as the one for the check event, and
                // if so, prevent it from happening and reset
                // the selection to what it last was; otherwise
                // allow it.
                if (e.detail == SWT.CHECK) {
                    lastCheckEventTime = e.time;
                    String identifier = (String) e.item.getData();
                    boolean isChecked = ((TableItem) e.item).getChecked();
                    for (Dict eventDict : eventDictList) {
                        if (eventDict.get(Utilities.HAZARD_EVENT_IDENTIFIER)
                                .equals(identifier)) {
                            eventDict.put(Utilities.HAZARD_EVENT_CHECKED,
                                    isChecked);
                            break;
                        }
                    }
                    fireConsoleActionOccurred(new ConsoleAction("CheckBox",
                            identifier, isChecked));
                } else {
                    if (e.time == lastCheckEventTime) {
                        e.doit = false;
                        if ((selectedIndices == null)
                                || (selectedIndices.length == 0)) {
                            table.deselectAll();
                        } else {
                            table.setSelection(selectedIndices);
                        }
                    } else {
                        TableItem[] selectedItems = table.getSelection();
                        List<String> selectedIdentifiers = Lists.newArrayList();
                        for (int j = 0; j < selectedItems.length; j++) {
                            TableItem item = selectedItems[j];
                            selectedIdentifiers.add((String) item.getData());
                        }
                        selectedIndices = table.getSelectionIndices();
                        updateEventDictListSelection(selectedIdentifiers);
                        fireConsoleActionOccurred(new ConsoleAction(
                                "SelectedEventsChanged", selectedIdentifiers
                                        .toArray(new String[selectedIdentifiers
                                                .size()])));
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

                // Determine whether or not the point clicked is
                // within the header area, and allow the menu to
                // be deployed only if it is.
                Point point = Display.getCurrent().map(null, table,
                        new Point(e.x, e.y));
                int headerTop = table.getClientArea().y;
                int headerBottom = headerTop + table.getHeaderHeight();
                e.doit = ((point.y >= headerTop) && (point.y < headerBottom));

                // If deploying the menu, set its items checkboxes
                // to reflect which columns are showing and which
                // are hidden. If only one column is showing, dis-
                // able that checkbox so that the user cannot un-
                // check it. Then update any associated megawidget
                // manager's state as well, and set the menu as
                // belonging to the table.
                if (e.doit) {
                    TableColumn column = getTableColumnAtPoint(new Point(e.x,
                            e.y));
                    if (column != null) {
                        Menu menu = headerMenusForColumnNames.get(column
                                .getText());
                        for (MenuItem menuItem : menu.getItems()) {
                            if (menuItem.getStyle() == SWT.SEPARATOR) {
                                break;
                            }
                            menuItem.setSelection(visibleColumnNames
                                    .contains(menuItem.getText()));
                            menuItem.setEnabled(!menuItem.getSelection()
                                    || (visibleColumnNames.size() > 1));
                        }
                        MegawidgetManager megawidgetManager = headerMegawidgetManagersForColumnNames
                                .get(column.getText());
                        if (megawidgetManager != null) {
                            try {
                                megawidgetManager.setState(dynamicSetting);
                            } catch (MegawidgetStateException exception) {
                                statusHandler
                                        .error("TemporalDisplay.createTable().MenuDetectListener."
                                                + "menuDetected(): Unable to set megawidget manager "
                                                + "state.", exception);
                            }
                        }
                        table.setMenu(menu);
                    }
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
                            for (Dict eventDict : eventDictList) {
                                if (eventDict.get(
                                        Utilities.HAZARD_EVENT_IDENTIFIER)
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
                            if (text != null) {
                                tableToolTip.setMessage(text);
                                tableToolTip.setData(cellBounds);
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
                    if (!((Rectangle) tableToolTip.getData())
                            .contains(e.x, e.y)) {
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
                            for (Dict eventDict : eventDictList) {
                                if (eventDict.get(
                                        Utilities.HAZARD_EVENT_IDENTIFIER)
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
        tableToolTip = new ToolTip(table.getShell(), SWT.BALLOON);
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
        column.setImage(spacerImage);
        column.setMoveable(false);
        column.setResizable(true);
        column.pack();

        // Add a listener to the time scale column to allow it to
        // respond
        // to column-reordering and -resizing events.
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
        Dict columnDefinition = (Dict) columnDefinitionsForNames.get(name);
        if (columnDefinition == null) {
            statusHandler
                    .error("TemporalDisplay.createTableColumn(): Problem: "
                            + "no column definition for \"" + name + "\".");
            return;
        }

        // Create the column.
        TableColumn column = new TableColumn(table, SWT.NONE,
                (index == -1 ? table.getColumnCount() : index));
        column.setText(name);
        column.setImage(spacerImage);
        column.setMoveable(true);
        column.setResizable(true);

        // Set the table's sort column to match this one, and set its
        // sort direction, if this is the sort column.
        setSortInfoIfSortColumn(columnDefinition, column);

        // Pack the column, and only after packing set its width, if
        // one is specified in the definition. Width setting must occur
        // after packing so as to avoid having the pack operation
        // change the width to something other than that provided by
        // the definition.
        column.pack();
        Number width = columnDefinition
                .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_WIDTH);
        if (width != null) {
            column.setWidth(width.intValue());
        }

        // Create the appropriate comparator for sorting by the
        // contents of the cells in this column, and associate it with
        // the column.
        String type = columnDefinition
                .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_TYPE);
        Comparator<?> comparator = null;
        if (type.equals(Utilities.SETTING_COLUMN_TYPE_STRING)) {
            comparator = Collator.getInstance(Locale.getDefault());
        } else if (type.equals(Utilities.SETTING_COLUMN_TYPE_DATE)) {
            comparator = new DateStringComparator(dateTimeFormatter);
        } else if (type.equals(Utilities.SETTING_COLUMN_TYPE_NUMBER)) {
            comparator = new LongStringComparator();
        } else {
            statusHandler.error("TemporalDisplay.createTableColumn(): Do not "
                    + "know how to compare values of type \"" + type + "\".");
        }
        column.setData(comparator);

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
        separator = new Label(parent, SWT.SEPARATOR + SWT.SHADOW_OUT
                + SWT.HORIZONTAL);
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
        List<IHatchMarkGroup> hatchMarkGroups = Lists.newArrayList();
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
        ruler = new MultiValueRuler(parent, Utilities.MIN_TIME,
                Utilities.MAX_TIME, hatchMarkGroups) {
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
        resources.add(RULER_BORDER_COLOR);
        ruler.setBorderColor(RULER_BORDER_COLOR);
        ruler.setHeightMultiplier(2.95f);
        ruler.setSnapValueCalculator(snapValueCalculator);
        ruler.setTooltipTextProvider(thumbTooltipTextProvider);
        ruler.setInsets(TIME_HORIZONTAL_PADDING, RULER_VERTICAL_PADDING,
                TIME_HORIZONTAL_PADDING, RULER_VERTICAL_PADDING);
        ruler.setViewportDraggable(true);

        long lowerTime = currentTime - (visibleTimeRange / 4L);
        if (lowerTime < Utilities.MIN_TIME) {
            lowerTime = Utilities.MIN_TIME;
        }
        long upperTime = lowerTime + visibleTimeRange - 1L;
        if (upperTime > Utilities.MAX_TIME) {
            lowerTime -= upperTime - Utilities.MAX_TIME;
            upperTime = Utilities.MAX_TIME;
        } else if (upperTime <= lowerTime) {
            upperTime = Utilities.MAX_TIME;
        }
        ruler.setVisibleValueRange(lowerTime, upperTime);
        ruler.setFreeMarkedValues(currentTime);
        ruler.setFreeThumbValues(selectedTime);

        currentTimeColor = new Color(Display.getCurrent(), 50, 130, 50);
        resources.add(currentTimeColor);
        ruler.setFreeMarkedValueColor(0, currentTimeColor);
        ruler.setFreeMarkedValueDirection(0,
                MultiValueRuler.IndicatorDirection.DOWN);
        ruler.setFreeMarkedValueHeight(0, 1.0f);
        selectedTimeColor = new Color(Display.getCurrent(), 170, 56, 56);
        resources.add(selectedTimeColor);
        ruler.setFreeThumbColor(0, selectedTimeColor);
        timeRangeEdgeColor = new Color(Display.getCurrent(), 56, 56, 170);
        resources.add(timeRangeEdgeColor);
        timeRangeFillColor = new Color(Display.getCurrent(), 190, 190, 224);
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
                timeRangeStart = values[0];
                timeRangeEnd = values[1];
                for (TableEditor tableEditor : tableEditorsForIdentifiers
                        .values()) {
                    ((MultiValueScale) tableEditor.getEditor())
                            .setConstrainedMarkedValues(values);
                }
                if (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION) {
                    fireConsoleActionOccurred(new ConsoleAction(
                            "SelectedTimeRangeChanged", Long
                                    .toString(timeRangeStart), Long
                                    .toString(timeRangeEnd)));
                }
            }

            @Override
            public void freeThumbValuesChanged(MultiValueLinearControl widget,
                    long[] values, ChangeSource source) {
                if (values.length == 0) {
                    return;
                }
                selectedTime = values[0];
                for (TableEditor tableEditor : tableEditorsForIdentifiers
                        .values()) {
                    ((MultiValueScale) tableEditor.getEditor())
                            .setFreeMarkedValue(1, selectedTime);
                }
                if (source == MultiValueLinearControl.ChangeSource.USER_GUI_INTERACTION) {
                    fireConsoleActionOccurred(new ConsoleAction(
                            "SelectedTimeChanged", Long.toString(selectedTime)));
                }
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

        // Pack the composite with everything created so far, as the
        // bounds of the time ruler are needed to continue.
        temporalDisplayPanel.pack(true);
        repackRuler();
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
        rulerFormData.left = new FormAttachment(0, xPixels + 3);
        rulerTopOffset = tableYPixels + ((headerHeight - rulerHeight) / 2) - 3;
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
            Image image = new Image(Display.getCurrent(), ICONS_PATH
                    + File.separator + BUTTON_IMAGE_NAMES.get(j)
                    + PNG_FILE_NAME_SUFFIX);
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
        List<TableItem> selectedTableItems = Lists.newArrayList();

        // Create a table item for each row in the table.
        for (int j = 0; j < numberOfRows; j++) {

            // Create the table item for this row.
            TableItem item = new TableItem(table, SWT.NONE);

            // For each column in the row, insert the text appropriate
            // to the column.
            for (String name : visibleColumnNames) {
                Dict columnDefinition = (Dict) columnDefinitionsForNames
                        .get(name);
                String cellValue = getCellValue(j, columnDefinition);
                item.setText(getIndexOfColumnInTable(name),
                        (cellValue == null ? NOT_APPLICABLE : cellValue));
            }

            // Determine whether or not the row is to be selected.
            Dict eventDict = eventDictList.get(j);
            Object selectedObject = eventDict
                    .get(Utilities.HAZARD_EVENT_SELECTED);
            boolean selected = ((selectedObject != null) && ((Boolean) selectedObject)
                    .booleanValue());
            if (selected) {
                selectedTableItems.add(item);
            }

            // Create the event scale. This always goes in the last
            // column of the table.
            MultiValueScale scale = createTimeScale(item,
                    getTimeRangeColorForRGB((String) eventDict
                            .get(Utilities.HAZARD_EVENT_COLOR)),
                    ((Number) eventDict.get(Utilities.HAZARD_EVENT_START_TIME))
                            .longValue(),
                    ((Number) eventDict.get(Utilities.HAZARD_EVENT_END_TIME))
                            .longValue());

            // Set the row's identifier to equal that of the hazard
            // event.
            item.setData(eventDictList.get(j).get(
                    Utilities.HAZARD_EVENT_IDENTIFIER));
            item.setChecked((Boolean) eventDict
                    .get(Utilities.HAZARD_EVENT_CHECKED));

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
     * Find the table column that lies under the specified point.
     * 
     * @param point
     *            Point under which to look for the table column.
     * @return Table column under the point.
     */
    private TableColumn getTableColumnAtPoint(Point point) {
        point = Display.getCurrent().map(null, table, point);
        Rectangle clientArea = table.getClientArea();
        ScrollBar horizontalScrollBar = table.getHorizontalBar();
        int offset = (horizontalScrollBar != null ? horizontalScrollBar
                .getSelection() : 0);
        int xStart = clientArea.x + offset;
        int xPoint = point.x + offset;
        if (xPoint >= xStart) {
            int xCurrent = 0;
            for (int columnIndex : table.getColumnOrder()) {
                TableColumn column = table.getColumn(columnIndex);
                int xNext = xCurrent + column.getWidth();
                if ((xPoint >= xCurrent) && (xPoint < xNext)) {
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
        Dict columnDefinition = (Dict) columnDefinitionsForNames
                .get(columnName);
        int index = getIndexOfColumnInTable(columnName);
        TableItem[] tableItems = table.getItems();
        for (int j = 0; j < tableItems.length; j++) {
            String cellValue = getCellValue(j, columnDefinition);
            tableItems[j].setText(index, (cellValue == null ? NOT_APPLICABLE
                    : cellValue));
        }
    }

    /**
     * Fire the specified console action off to any listeners.
     * 
     * @param ConsoleAction
     *            Action to be fired off to listeners.
     */
    private void fireConsoleActionOccurred(ConsoleAction consoleAction) {
        presenter.fireAction(consoleAction);
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
        if (lower < Utilities.MIN_TIME) {
            altered = true;
            upper += Utilities.MIN_TIME - lower;
            lower = Utilities.MIN_TIME;
        }
        if (upper > Utilities.MAX_TIME) {
            altered = true;
            lower -= upper - Utilities.MAX_TIME;
            upper = Utilities.MAX_TIME;
        }

        // If the time range has changed from what the time line
        // already had, commit to the change.
        if ((lower != ruler.getLowerVisibleValue())
                || (upper != ruler.getUpperVisibleValue())) {
            ruler.setVisibleValueRange(lower, upper);
            if (forwardAction || altered) {
                fireConsoleActionOccurred(new ConsoleAction(
                        ("VisibleTimeRangeChanged"), Long.toString(lower),
                        Long.toString(upper)));
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
                    ("VisibleTimeRangeChanged"), Long.toString(lower),
                    Long.toString(upper)));
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
                    ruler.getLowerVisibleValue() > Utilities.MIN_TIME);
            buttonsForIdentifiers.get(BUTTON_PAN_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > Utilities.MIN_TIME);
            buttonsForIdentifiers.get(BUTTON_PAN_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < Utilities.MAX_TIME);
            buttonsForIdentifiers.get(BUTTON_PAGE_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < Utilities.MAX_TIME);
        }

        // Update the toolbar buttons if they exist.
        if (actionsForButtonIdentifiers.get(BUTTON_ZOOM_OUT) != null) {
            actionsForButtonIdentifiers.get(BUTTON_ZOOM_OUT).setEnabled(
                    getZoomedOutRange() <= MAX_VISIBLE_TIME_RANGE);
            actionsForButtonIdentifiers.get(BUTTON_ZOOM_IN).setEnabled(
                    getZoomedInRange() >= MIN_VISIBLE_TIME_RANGE);
            actionsForButtonIdentifiers.get(BUTTON_PAGE_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > Utilities.MIN_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAN_BACKWARD).setEnabled(
                    ruler.getLowerVisibleValue() > Utilities.MIN_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAN_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < Utilities.MAX_TIME);
            actionsForButtonIdentifiers.get(BUTTON_PAGE_FORWARD).setEnabled(
                    ruler.getUpperVisibleValue() < Utilities.MAX_TIME);
        }
    }

    /**
     * Pan the time line to ensure that the specified time is shown.
     */
    private void showTime(long time) {
        long lower = time - (visibleTimeRange / 2L);
        if (lower < Utilities.MIN_TIME) {
            lower = Utilities.MIN_TIME;
        }
        long upper = lower + visibleTimeRange - 1L;
        if (upper > Utilities.MAX_TIME) {
            lower -= upper - Utilities.MAX_TIME;
            upper = Utilities.MAX_TIME;
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
        List<ColumnIndexAndWidth> columnIndicesAndWidths = Lists.newArrayList();
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

        // Update the order of the columns in the dictionaries.
        updateTableColumnOrderInSettingDefinition();
    }

    /**
     * Retrieve the value to display in a cell in the table.
     * 
     * @param row
     *            Index of the row from which to retrieve the value.
     * @return Value to display in the specified table cell.
     */
    private String getCellValue(int row, Dict columnDefinition) {
        if (columnDefinition == null) {
            statusHandler.error("TemporalDisplay.getCellValue(): Problem: "
                    + "no column definition provided");
            return null;
        }
        return (convertToCellValue(
                eventDictList.get(row).get(
                        columnDefinition
                                .get(Utilities.SETTING_COLUMN_IDENTIFIER)),
                columnDefinition));
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
    private String convertToCellValue(Object value, Dict columnDefinition) {
        if (columnDefinition == null) {
            statusHandler.error("TemporalDisplay.convertToCellValue(): "
                    + "Problem: no column definition provided");
            return null;
        }
        if (columnDefinition.getDynamicallyTypedValue(
                Utilities.SETTING_COLUMN_TYPE).equals(
                Utilities.SETTING_COLUMN_TYPE_DATE)) {
            Number number = (Number) value;
            if (number != null) {
                return getDateTimeString(number.longValue());
            } else {
                return getDateTimeString(0L);
            }
        } else {
            return (String) value;
        }
    }

    /**
     * Notify all listeners of any changes in the dynamic setting configuration
     * caused by the temporal display (e.g. changes in column widths, column
     * orders, etc.).
     */
    private void notifyListenersOfSettingDefinitionChange() {
        SettingsAction action = new SettingsAction("DynamicSettingChanged",
                dynamicSetting.toJSONString());
        presenter.fireAction(action);
    }

    /**
     * Schedule notification of all listeners of any changes in the dynamic
     * setting configuration caused by the temporal display.
     */
    private void scheduleNotificationOfSettingDefinitionChange() {
        if (willNotifyOfSettingChange == false) {
            willNotifyOfSettingChange = true;
            Display.getCurrent().asyncExec(new Runnable() {
                @Override
                public void run() {
                    willNotifyOfSettingChange = false;
                    notifyListenersOfSettingDefinitionChange();
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
        scheduleNotificationOfSettingDefinitionChange();

        // Gson gson = JSONUtilities.createPrettyGsonInterpreter();
        // statusHandler.debug(gson.toJson(visibleColumnNames));
    }

    /**
     * Determine the special properties of the column associated with the
     * specified name, adding records to maps for any such properties.
     * 
     * @param columnName
     *            Column name.
     */
    private void determineSpecialPropertiesOfColumn(String columnName) {
        Dict columnDefinition = (Dict) columnDefinitionsForNames
                .get(columnName);
        if (columnDefinition == null) {
            return;
        }
        String hintTextIdentifier = columnDefinition
                .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_HINT_TEXT_IDENTIFIER);
        if (hintTextIdentifier != null) {
            hintTextIdentifiersForVisibleColumnNames.put(columnName,
                    hintTextIdentifier);
        }
        if (columnDefinition.get(Utilities.SETTING_COLUMN_TYPE).equals(
                Utilities.SETTING_COLUMN_TYPE_DATE)) {
            dateIdentifiersForVisibleColumnNames.put(columnName,
                    (String) columnDefinition
                            .get(Utilities.SETTING_COLUMN_IDENTIFIER));
        }
    }

    /**
     * Update information in the column definition dictionaries related to the
     * sort column and direction.
     */
    private void updateTableSortColumnInSettingDefinition() {

        // Alter the column definitions to record the fact that a new
        // column is the sort column, and its direction.
        String sortName = table.getSortColumn().getText();
        for (String name : columnDefinitionsForNames.keySet()) {
            Dict columnDefinition = (Dict) columnDefinitionsForNames.get(name);
            if (name.equals(sortName)) {
                if (table.getSortDirection() == SWT.UP) {
                    columnDefinition.put(
                            Utilities.SETTING_COLUMN_SORT_DIRECTION,
                            Utilities.SETTING_COLUMN_SORT_DIRECTION_ASCENDING);
                } else {
                    columnDefinition.put(
                            Utilities.SETTING_COLUMN_SORT_DIRECTION,
                            Utilities.SETTING_COLUMN_SORT_DIRECTION_DESCENDING);
                }
            } else {
                columnDefinition.put(Utilities.SETTING_COLUMN_SORT_DIRECTION,
                        Utilities.SETTING_COLUMN_SORT_DIRECTION_NONE);
            }
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange();

        // Gson gson = JSONUtilities.createPrettyGsonInterpreter();
        // statusHandler.debug(gson.toJson(columnDefinitionsForNames));
    }

    /**
     * Update column definition dictionary with a new width for the specified
     * resized table column.
     * 
     * @param column
     *            The table column which has been resized.
     */
    private void updateTableColumnWidthInSettingDefinition(TableColumn column) {
        Dict columnDefinition = (Dict) columnDefinitionsForNames.get(column
                .getText());
        if (columnDefinition != null) {
            columnDefinition.put(Utilities.SETTING_COLUMN_WIDTH,
                    column.getWidth());
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange();

        // Gson gson = JSONUtilities.createPrettyGsonInterpreter();
        // statusHandler.debug(gson.toJson(columnDefinitionsForNames));
    }

    /**
     * Update all table column definition dictionaries for visible columns with
     * new widths.
     */
    private void updateAllTableColumnWidthsInSettingDefinition() {
        for (TableColumn column : table.getColumns()) {
            Dict columnDefinition = (Dict) columnDefinitionsForNames.get(column
                    .getText());
            if (columnDefinition != null) {
                columnDefinition.put(Utilities.SETTING_COLUMN_WIDTH,
                        column.getWidth());
            }
        }

        // Notify listeners of the setting change.
        scheduleNotificationOfSettingDefinitionChange();

        // Gson gson = JSONUtilities.createPrettyGsonInterpreter();
        // statusHandler.debug(gson.toJson(columnDefinitionsForNames));
    }

    /**
     * Get the index at which the specified column is found in the table. This
     * is not the index indicating the current ordering of the columns, but
     * rather the index that, when provided to <code>table.getColumn()</code>,
     * returns the specified column.
     * 
     * @param name
     *            Name of the column for which to find the index.
     * @return Index of the column in the table, or <code>-1</code> if the
     *         column is not currently in the table.
     */
    private int getIndexOfColumnInTable(String name) {
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
    private void setSortInfoIfSortColumn(Dict columnDefinition,
            TableColumn tableColumn) {
        String sortDirection = columnDefinition
                .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_SORT_DIRECTION);
        if (sortDirection != null) {
            if (sortDirection
                    .equals(Utilities.SETTING_COLUMN_SORT_DIRECTION_ASCENDING)) {
                table.setSortColumn(tableColumn);
                table.setSortDirection(SWT.UP);
            } else if (sortDirection
                    .equals(Utilities.SETTING_COLUMN_SORT_DIRECTION_DESCENDING)) {
                table.setSortColumn(tableColumn);
                table.setSortDirection(SWT.DOWN);
            }
        }
    }

    /**
     * Sort the event dictionaries based on the table's sort column and
     * direction, using the Bubble Sort algorithm.
     */
    private void sortEventData() {

        // Determine whether there is a sort column, and do the sort
        // if there is.
        TableColumn column = table.getSortColumn();
        if (column != null) {

            // Get the sort direction, the event dictionary field it
            // represents, and the type of the field.
            int tableSortDirection = table.getSortDirection();
            Dict columnDefinition = (Dict) columnDefinitionsForNames.get(column
                    .getText());
            if (columnDefinition == null) {
                statusHandler
                        .error("TemporalDisplay.sortEventData(): Problem: no "
                                + "column definition for \"" + column.getText()
                                + "\".");
                return;
            }
            String sortByIdentifier = columnDefinition
                    .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_IDENTIFIER);
            String sortByType = columnDefinition
                    .getDynamicallyTypedValue(Utilities.SETTING_COLUMN_TYPE);

            // Determine which is the appropriate comparator.
            Comparator<? super String> comparator = null;
            if (sortByType.equals(Utilities.SETTING_COLUMN_TYPE_STRING)) {
                comparator = Collator.getInstance(Locale.getDefault());
            } else if (sortByType.equals(Utilities.SETTING_COLUMN_TYPE_DATE)
                    || sortByType.equals(Utilities.SETTING_COLUMN_TYPE_NUMBER)) {
                comparator = new LongStringComparator();
            } else {
                statusHandler
                        .error("TemporalDisplay.sortEventData(): Do not know "
                                + "how to compare values of type \""
                                + sortByType + "\".");
            }

            // Perform the sort.
            boolean changed = false;
            do {
                changed = false;
                for (int j = 0; j < eventDictList.size() - 1; ++j) {

                    // Get the objects to be sorted; if either is
                    // missing, do nothing this round.
                    Object object1 = eventDictList.get(j).get(sortByIdentifier);
                    Object object2 = eventDictList.get(j + 1).get(
                            sortByIdentifier);
                    if ((object1 == null) || (object2 == null)) {
                        continue;
                    }
                    String value1 = object1.toString();
                    String value2 = object2.toString();

                    // Switch the two event dictionaries if they are
                    // not already in the correct order.
                    if (((tableSortDirection == SWT.UP) && (comparator.compare(
                            value1, value2) > 0))
                            || ((tableSortDirection == SWT.DOWN) && (comparator
                                    .compare(value1, value2) < 0))) {
                        Dict tempEventDict = eventDictList.get(j);
                        eventDictList.set(j, eventDictList.get(j + 1));
                        eventDictList.set(j + 1, tempEventDict);
                        changed = true;
                    }
                }
            } while (changed);
        }
    }

    /**
     * Sort the rows in the table based upon the contents of the cells in the
     * specified column using the provided comparator.
     * 
     * @param sortColumnIndex
     *            Canonical index of the column to be used for sorting.
     */
    private void sortRowsByColumn(int sortColumnIndex,
            Comparator<? super String> comparator) {

        // If the sort direction exists, do the sort.
        int tableSortDirection = table.getSortDirection();
        if (tableSortDirection != SWT.NONE) {

            // Get the items to be sorted.
            TableItem[] items = table.getItems();

            // Compile a set of the selected event identifiers so that
            // they may be selected again later.
            TableItem[] selectedItems = table.getSelection();
            Set<Object> selectedIdentifiers = Sets.newHashSet();
            for (TableItem item : selectedItems) {
                selectedIdentifiers.add(item.getData());
            }

            // Retrieve a list of the controls, deleting the old table
            // editors that these controls used.
            Map<String, Control> controlsForIdentifiers = Maps.newHashMap();
            for (String key : tableEditorsForIdentifiers.keySet()) {
                TableEditor editor = tableEditorsForIdentifiers.get(key);
                controlsForIdentifiers.put(key, editor.getEditor());
                editor.dispose();
            }
            tableEditorsForIdentifiers.clear();

            // Iterate through the old rows after the first one,
            // finding
            // in each case where the the row belongs, and recreating
            // it at the appropriate index.
            for (int j = 1; j < items.length; j++) {

                // Iterate through all the rows before this one,
                // finding the place to insert the row, and insert a
                // new one that is identical to this one there,
                // deleting this one in the process.
                String value1 = items[j].getText(sortColumnIndex);
                for (int k = 0; k < j; k++) {
                    String value2 = items[k].getText(sortColumnIndex);
                    if (((tableSortDirection == SWT.UP) && (comparator.compare(
                            value1, value2) < 0))
                            || ((tableSortDirection == SWT.DOWN) && (comparator
                                    .compare(value1, value2) > 0))) {

                        // Create the new row at the index at which it
                        // now belongs.
                        TableItem item = new TableItem(table, SWT.NONE, k);

                        // Set the new item's state to match what the
                        // old item had.
                        item.setChecked(items[j].getChecked());
                        item.setData(items[j].getData());

                        // For each column in the row, insert the text
                        // that is found in the old row's corresponding
                        // cell, and associate the field name for that
                        // column with it so that it may be easily
                        // changed if the hazard event is updated
                        // later.
                        for (int q = 0; q < visibleColumnNames.size(); q++) {
                            item.setText(q, items[j].getText(q));
                        }

                        // Get rid of the old item, and fetch the items
                        // again from the table so that the new item
                        // will be included in the array.
                        items[j].dispose();
                        items = table.getItems();
                        break;
                    }
                }
            }

            // Iterate through the table items once more, placing the
            // controls in new editors in the last column, and making
            // a list of any that were previously selected.
            TableItem[] tableItems = table.getItems();
            selectedItems = new TableItem[selectedIdentifiers.size()];
            int selectedIndex = 0;
            for (TableItem item : tableItems) {
                Control control = controlsForIdentifiers.get(item.getData());
                createTableEditorForTimeScale(control, item);
                if (selectedIdentifiers.contains(item.getData())) {
                    selectedItems[selectedIndex++] = item;
                }
            }

            // Select the items that were previously selected.
            table.setSelection(selectedItems);
            selectedIndices = table.getSelectionIndices();
        }
    }
}
