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
import gov.noaa.gsd.viz.megawidgets.CompositeMegawidget;
import gov.noaa.gsd.viz.megawidgets.CompositeSpecifier;
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
import gov.noaa.gsd.viz.megawidgets.MultiTimeMegawidget;
import gov.noaa.gsd.viz.megawidgets.MultiTimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeMegawidgetSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeRangeMegawidget;
import gov.noaa.gsd.viz.megawidgets.TimeRangeSpecifier;
import gov.noaa.gsd.viz.megawidgets.TimeScaleMegawidget;
import gov.noaa.gsd.viz.megawidgets.TimeScaleSpecifier;
import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
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
import com.google.common.collect.Range;
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
 * Jul 03, 2014   3512     Chris.Golden      Added code to allow a duration selector to be
 *                                           displayed instead of an absolute date/time
 *                                           selector for the end time of a hazard event.
 * Aug 15, 2014   4243     Chris.Golden      Added ability to invoke event-modifying
 *                                           scripts via metadata-specified notifier
 *                                           megawidgets.
 * Sep 05, 2014   4277     Chris.Golden      Changed scrollbars' buttons to cause the
 *                                           metadata pane to scroll a reasonable amount
 *                                           instead of barely moving.
 * Sep 08, 2014   4042     Chris.Golden      Fixed minor bugs in setting of metadata
 *                                           scrollbar page increments.
 * Sep 16, 2014   4753     Chris.Golden      Changed event script running to include
 *                                           mutable properties.
 * Oct 20, 2014   4818     Chris.Golden      Changed from tracking of raw metadata scroll
 *                                           origins for each hazard event to more
 *                                           comprehensive megawidget display settings.
 *                                           Also removed scrolled composite from metadata
 *                                           panel, since scrolling is now handled by the
 *                                           megawidgets.
 * Jan 07, 2015   5699     Chris.Golden      Removed persisting of megawidget extraData
 *                                           between refreshes of metadata megawidgets.
 * Feb 03, 2015   2331     Chris.Golden      Added support for limiting the values that an
 *                                           event's start or end time can take on.
 * Feb 11, 2015   6393     Chris.Golden      Added checks to ensure that no errors occur
 *                                           if the tab displayables outnumber the tabs.
 * Mar 06, 2015   3850     Chris.Golden      Added code to make the category and type
 *                                           lists change according to whether the
 *                                           event being shown has a point ID (if not
 *                                           yet issued), or what it can be replaced
 *                                           by (if issued).
 * Apr 09, 2015   7382     Chris.Golden      Added "show start-end time sliders" flag.
 * Apr 15, 2015   3508     Chris.Golden      Added "hazard detail to be wide" flag.
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
     * Type of the composite megawidget.
     */
    private static final String COMPOSITE_MEGAWIDGET_TYPE = "Composite";

    /**
     * Type of the combo box megawidget.
     */
    private static final String COMBOBOX_MEGAWIDGET_TYPE = "ComboBox";

    /**
     * Type of the time range megawidget.
     */
    private static final String TIMERANGE_MEGAWIDGET_TYPE = "TimeRange";

    /**
     * Type of the time scale megawidget.
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
     * Until-further-notice composite wrapper megawidget identifier.
     */
    private static final String UNTIL_FURTHER_NOTICE_WRAPPER_IDENTIFIER = "untilFurtherNoticeWrapper";

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
     * Duration text.
     */
    private static final String DURATION_TEXT = "Duration:";

    /**
     * Text for "until further notice" checkbox.
     */
    private static final String UNTIL_FURTHER_NOTICE_TEXT = "Until further notice";

    /**
     * Text to display in the date-time fields for the end time of the time
     * scale megawidget when the "until further notice" value is the current
     * state for the end time.
     */
    private static final String UNTIL_FURTHER_NOTICE_VALUE_TEXT = "N/A";

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
     * Specifier for the until-further-notice checkbox megawidget.
     */
    private static final ImmutableList<ImmutableMap<String, Object>> UNTIL_FURTHER_NOTICE_DETAIL_FIELD_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(MegawidgetSpecifier.MEGAWIDGET_TYPE, CHECKBOX_MEGAWIDGET_TYPE);
        map.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
        map.put(MegawidgetSpecifier.MEGAWIDGET_LABEL, UNTIL_FURTHER_NOTICE_TEXT);
        List<ImmutableMap<String, Object>> list = new ArrayList<>();
        list.add(ImmutableMap.copyOf(map));
        UNTIL_FURTHER_NOTICE_DETAIL_FIELD_PARAMETERS = ImmutableList
                .copyOf(list);
    }

    /**
     * Specifier for the until-further-notice checkbox megawidget that has its
     * own row as a detail field.
     */
    private static final ImmutableList<ImmutableMap<String, Object>> UNTIL_FURTHER_NOTICE_DETAIL_FIELD_WRAPPED_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(MegawidgetSpecifier.MEGAWIDGET_TYPE, COMPOSITE_MEGAWIDGET_TYPE);
        map.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                UNTIL_FURTHER_NOTICE_WRAPPER_IDENTIFIER);
        map.put(CompositeSpecifier.CHILD_MEGAWIDGETS, ImmutableList
                .copyOf(UNTIL_FURTHER_NOTICE_DETAIL_FIELD_PARAMETERS));
        map.put(CompositeSpecifier.LEFT_MARGIN, 10);
        List<ImmutableMap<String, Object>> list = new ArrayList<>();
        list.add(ImmutableMap.copyOf(map));
        UNTIL_FURTHER_NOTICE_DETAIL_FIELD_WRAPPED_PARAMETERS = ImmutableList
                .copyOf(list);
    }

    /**
     * Specifier parameters for the time range megawidget.
     */
    private static final ImmutableMap<String, Object> TIME_SCALE_SPECIFIER_PARAMETERS;
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
         * Ensure that the time range megawidget shows special text if the
         * "Until further notice" value is the current state for its end time.
         */
        Map<Long, String> descriptiveTextForValues = new HashMap<>();
        descriptiveTextForValues.put(
                HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS,
                UNTIL_FURTHER_NOTICE_VALUE_TEXT);
        map.put(TimeScaleSpecifier.MEGAWIDGET_TIME_DESCRIPTORS,
                ImmutableMap.copyOf(descriptiveTextForValues));
        TIME_SCALE_SPECIFIER_PARAMETERS = ImmutableMap.copyOf(map);
    }

    /**
     * Specifier parameters for the time range megawidget.
     */
    private static final ImmutableMap<String, Object> TIME_RANGE_SPECIFIER_PARAMETERS;
    static {
        Map<String, Object> map = new HashMap<>();
        map.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                TIME_RANGE_IDENTIFIER);
        map.put(ISpecifier.MEGAWIDGET_TYPE, TIMERANGE_MEGAWIDGET_TYPE);
        map.put(IControlSpecifier.MEGAWIDGET_SPACING, 0);
        Map<String, Object> subMap = new HashMap<>();
        subMap.put(START_TIME_STATE, START_TIME_TEXT);
        subMap.put(END_TIME_STATE, DURATION_TEXT);
        map.put(TimeScaleSpecifier.MEGAWIDGET_STATE_LABELS,
                ImmutableMap.copyOf(subMap));
        map.put(IControlSpecifier.MEGAWIDGET_SPACING, 5);
        map.put(IParentSpecifier.MEGAWIDGET_SPECIFIER_FACTORY,
                new MegawidgetSpecifierFactory());
        List<String> list = ImmutableList.of("1 minute");
        map.put(TimeRangeSpecifier.MEGAWIDGET_DURATION_CHOICES, list);

        /*
         * Specify the "Until further notice" checkbox to be shown next to the
         * end time time delta fields.
         */
        subMap = new HashMap<>();
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_TYPE,
                CHECKBOX_MEGAWIDGET_TYPE);
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_IDENTIFIER,
                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
        subMap.put(MegawidgetSpecifier.MEGAWIDGET_LABEL,
                UNTIL_FURTHER_NOTICE_TEXT);
        List<Map<String, Object>> childList = new ArrayList<>();
        childList.add(ImmutableMap.copyOf(subMap));
        subMap = new HashMap<>();
        subMap.put(END_TIME_STATE, ImmutableList.copyOf(childList));
        map.put(TimeScaleSpecifier.MEGAWIDGET_DETAIL_FIELDS,
                ImmutableMap.copyOf(subMap));
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
     * Flag indicating whether or not the view part has been initialized.
     */
    private boolean initialized;

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
     * Time group panel.
     */
    private Group timeGroup;

    /**
     * Layout manager for the time panel.
     */
    private StackLayout timeContentLayout;

    /**
     * Composite within the time panel that holds the time scale megawidget.
     */
    private Composite timeScalePanel;

    /**
     * Composite within the time panel that holds the time range megawidget.
     */
    private Composite timeRangePanel;

    /**
     * List holding the time scale and time range megawidgets, one of which is
     * shown at all times when an event is being displayed to show the start and
     * end times.
     */
    private final List<MultiTimeMegawidget> timeMegawidgets = new ArrayList<>(2);

    /**
     * Event time range megawidget, used when an event shows a start time and a
     * duration.
     */
    private TimeRangeMegawidget timeRangeMegawidget;

    /**
     * List holding the "until further notice" checkbox megawidgets associated
     * with the time scale and time range megawidgets, respectively.
     */
    private final List<CheckBoxMegawidget> untilFurtherNoticeToggleMegawidgets = new ArrayList<>(
            2);

    /**
     * Metadata panel.
     */
    private Composite metadataPanel;

    /**
     * Grid layout data for the {@link #metadataPanel}.
     */
    private GridData metadataPanelLayoutData;

    /**
     * Layout manager for the {@link #metadataPanel}.
     */
    private StackLayout metadataPanelLayout;

    /**
     * Map of hazard event identifiers to their metadata megawidgets' display
     * settings; the latter are recorded whenever a metadata megawidget manager
     * is destroyed.
     */
    private final Map<String, Map<String, IDisplaySettings>> megawidgetDisplaySettingsForEventIds = new HashMap<>();

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
     * Flag indicating whether or not the start-end time UI elements should
     * include a sliders-equipped scale bar.
     */
    private boolean showStartEndTimeScale;

    /**
     * Flag indicating whether or not the view part is to be built in a way that
     * is optimized for wide viewing.
     */
    private boolean buildForWideViewing;

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
     * Metadata megawidgets' display settings state change handler. The
     * identifier is that of the hazard event having its display settings
     * changed.
     */
    private IStateChangeHandler<String, Map<String, IDisplaySettings>> megawidgetDisplaySettingsChangeHandler;

    /**
     * Metadata megawidgets' display settings state changer. The identifier is
     * that of the hazard event.
     */
    private final IStateChanger<String, Map<String, IDisplaySettings>> megawidgetdisplaySettingsChanger = new IStateChanger<String, Map<String, IDisplaySettings>>() {

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
        public Map<String, IDisplaySettings> getState(String identifier) {
            return megawidgetDisplaySettingsForEventIds.get(identifier);
        }

        @Override
        public void setState(String identifier,
                Map<String, IDisplaySettings> value) {
            megawidgetDisplaySettingsForEventIds.put(identifier, value);
        }

        @Override
        public void setStates(
                Map<String, Map<String, IDisplaySettings>> valuesForIdentifiers) {
            megawidgetDisplaySettingsForEventIds.putAll(valuesForIdentifiers);
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Map<String, IDisplaySettings>> handler) {
            megawidgetDisplaySettingsChangeHandler = handler;
        }
    };

    /**
     * Visible time range state changer. The identifier is ignored.
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
     * Selected event state change handler. The identifier is ignored.
     */
    private IStateChangeHandler<String, String> tabChangeHandler;

    /**
     * Visible event state changer. The identifier is ignored.
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
     * Category combo box state change handler. The identifier is that of the
     * hazard event being changed.
     */
    private IStateChangeHandler<String, String> categoryChangeHandler;

    /**
     * Category combo box state changer. The identifier is that of the hazard
     * event.
     */
    private final IChoiceStateChanger<String, String, String, String> categoryChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                categoryMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                categoryMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setCategories(choices);
                setSelectedCategory(value);
            }
        }

        @Override
        public String getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of category");
        }

        @Override
        public void setState(String identifier, String value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setSelectedCategory(value);
            }
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
     * Type combo box state change handler. The identifier is that of the hazard
     * event being changed.
     */
    private IStateChangeHandler<String, String> typeChangeHandler;

    /**
     * Type combo box state changer. The identifier is that of the hazard event.
     */
    private final IChoiceStateChanger<String, String, String, String> typeChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                typeMegawidget.setEnabled(enable);
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                typeMegawidget.setEditable(editable);
            }
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setTypes(choices, choiceDisplayables);
                setSelectedType(value);
            }
        }

        @Override
        public String getState(String identifier) {
            throw new UnsupportedOperationException("cannot get state of type");
        }

        @Override
        public void setState(String identifier, String value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setSelectedType(value);
            }
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
     * Time range state change handler. The identifier is that of the hazard
     * event being changed.
     */
    private IStateChangeHandler<String, TimeRange> timeRangeChangeHandler;

    /**
     * Time range state changer. The identifier is that of the hazard event.
     */
    private final IStateChanger<String, TimeRange> timeRangeChanger = new IStateChanger<String, TimeRange>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                for (MultiTimeMegawidget megawidget : timeMegawidgets) {
                    megawidget.setEnabled(enable);
                }
            }
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            if (isAlive() && identifier.equals(visibleEventIdentifier)) {
                for (MultiTimeMegawidget megawidget : timeMegawidgets) {
                    megawidget.setEditable(editable);
                }
            }
        }

        @Override
        public TimeRange getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of time range");
        }

        @Override
        public void setState(String identifier, TimeRange value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setTimeRange(value);
            }
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
     * Time range allowable boundaries state changer. The qualifier is the
     * identifier of the hazard event, while the identifier indicates the
     * boundary.
     */
    private final IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> timeRangeBoundariesChanger = new IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>>() {

        @Override
        public void setEnabled(String qualifier, TimeRangeBoundary identifier,
                boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable/disable time range boundaries");
        }

        @Override
        public void setEditable(String qualifier, TimeRangeBoundary identifier,
                boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of time range boundaries");
        }

        @Override
        public Range<Long> getState(String qualifier,
                TimeRangeBoundary identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of time range boundaries");
        }

        @Override
        public void setState(String qualifier, TimeRangeBoundary identifier,
                Range<Long> value) {
            throw new UnsupportedOperationException(
                    "cannot change single state for time range boundaries");
        }

        @Override
        public void setStates(String qualifier,
                Map<TimeRangeBoundary, Range<Long>> valuesForIdentifiers) {
            if (qualifier.equals(visibleEventIdentifier)) {
                setTimeRangeBoundaries(valuesForIdentifiers);
            }
        }

        @Override
        public void setStateChangeHandler(
                IQualifiedStateChangeHandler<String, TimeRangeBoundary, Range<Long>> handler) {
            throw new UnsupportedOperationException(
                    "time range boundaries are never modified by the view");
        }
    };

    /**
     * Duration combo box state changer. The identifier is that of the hazard
     * event.
     */
    private final IChoiceStateChanger<String, String, String, String> durationChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable or disable duration");
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editability of duration");
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
            if (identifier.equals(visibleEventIdentifier)) {
                setDurationChoices(choices);
            }
        }

        @Override
        public String getState(String identifier) {
            throw new UnsupportedOperationException(
                    "cannot get state of duration");
        }

        @Override
        public void setState(String identifier, String value) {
            throw new UnsupportedOperationException(
                    "cannot set state of duration");
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
            throw new UnsupportedOperationException(
                    "cannot set state of duration");
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
            throw new UnsupportedOperationException(
                    "cannot set handler for duration changes");
        }
    };

    /**
     * Metadata state change handler. The qualifier is the identifier of the
     * hazard event being changed, while the identifier is the metadata that was
     * changed.
     */
    private IQualifiedStateChangeHandler<String, String, Serializable> metadataChangeHandler;

    /**
     * Metadata state changer. The qualifier is the identifier of the hazard
     * event, while the identifier is that of the metadata.
     */
    private final IMetadataStateChanger metadataChanger = new IMetadataStateChanger() {

        @Override
        public void setEnabled(String qualifier, String identifier,
                boolean enable) {
            setMetadataEnabledState(qualifier, identifier, enable);
        }

        @Override
        public void setEditable(String qualifier, String identifier,
                boolean editable) {
            setMetadataEditabilityState(qualifier, identifier, editable);
        }

        @Override
        public Serializable getState(String qualifier, String identifier) {
            return getMetadataValue(qualifier, identifier);
        }

        @Override
        public void setState(String qualifier, String identifier,
                Serializable value) {
            setMetadataValue(qualifier, identifier, value);
        }

        @Override
        public void setStates(String qualifier,
                Map<String, Serializable> valuesForIdentifiers) {
            setMetadataValues(qualifier, valuesForIdentifiers);
        }

        @Override
        public void setStateChangeHandler(
                IQualifiedStateChangeHandler<String, String, Serializable> handler) {
            metadataChangeHandler = handler;
        }

        @Override
        public void setMegawidgetSpecifierManager(String qualifier,
                MegawidgetSpecifierManager specifierManager,
                Map<String, Serializable> metadataStates) {
            setMetadataSpecifierManager(qualifier, specifierManager,
                    metadataStates);
        }

        @Override
        public void changeMegawidgetMutableProperties(String qualifier,
                Map<String, Map<String, Object>> mutableProperties) {
            changeMetadataMutableProperties(qualifier, mutableProperties);
        }
    };

    /**
     * Notifier invocation handler, for any notifier megawidgets included in the
     * metadata megawidgets. The identifier is that of the hazard event for
     * which the invocation is occurring, coupled with the notifier's
     * identifier.
     */
    private ICommandInvocationHandler<EventScriptInfo> notifierInvocationHandler;

    /**
     * Button invoker, for the buttons at the bottom of the view part. The
     * identifier is that of the hazard event coupled with that of the notifier.
     */
    private final ICommandInvoker<EventScriptInfo> notifierInvoker = new ICommandInvoker<EventScriptInfo>() {

        @Override
        public void setEnabled(EventScriptInfo identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot enable or disable notifier");
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<EventScriptInfo> handler) {
            notifierInvocationHandler = handler;
        }
    };

    /**
     * Button invocation handler, for the buttons at the bottom of the view
     * part. The identifier is the command.
     */
    private ICommandInvocationHandler<Command> buttonInvocationHandler;

    /**
     * Button invoker, for the buttons at the bottom of the view part. The
     * identifier is the command.
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
                    categoryChangeHandler.stateChanged(visibleEventIdentifier,
                            (String) state);
                }
            } else if (identifier.equals(TYPE_IDENTIFIER)) {
                if (typeChangeHandler != null) {
                    typeChangeHandler.stateChanged(visibleEventIdentifier,
                            (String) state);
                }
            } else if (identifier.equals(START_TIME_STATE)
                    || identifier.equals(END_TIME_STATE)) {
                MultiTimeMegawidget otherTimeMegawidget = timeMegawidgets
                        .get(megawidget == timeRangeMegawidget ? 0 : 1);
                TimeRange range;
                try {
                    range = new TimeRange(
                            (Long) (identifier.equals(START_TIME_STATE) ? state
                                    : megawidget.getState(START_TIME_STATE)),
                            (Long) (identifier.equals(END_TIME_STATE) ? state
                                    : megawidget.getState(END_TIME_STATE)));
                } catch (Exception e) {
                    statusHandler.error(
                            "unexpected problem fetching time range "
                                    + "values from megawidget", e);
                    return;
                }
                setTimeRange(otherTimeMegawidget, range);
                if (timeRangeChangeHandler != null) {
                    timeRangeChangeHandler.stateChanged(visibleEventIdentifier,
                            range);
                }
            } else if (identifier
                    .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
                boolean untilFurtherNotice = Boolean.TRUE.equals(state);
                CheckBoxMegawidget otherUntilFurtherNoticeMegawidget = untilFurtherNoticeToggleMegawidgets
                        .get(megawidget == untilFurtherNoticeToggleMegawidgets
                                .get(1) ? 0 : 1);
                setEndTimeUntilFurtherNotice(otherUntilFurtherNoticeMegawidget,
                        untilFurtherNotice);
                if (metadataChangeHandler != null) {
                    metadataChangeHandler.stateChanged(visibleEventIdentifier,
                            identifier, (Serializable) state);
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
            if (megawidget instanceof MultiTimeMegawidget) {
                MultiTimeMegawidget otherTimeMegawidget = timeMegawidgets
                        .get(megawidget == timeRangeMegawidget ? 0 : 1);
                TimeRange range;
                try {
                    range = new TimeRange(
                            (Long) statesForIdentifiers.get(START_TIME_STATE),
                            (Long) statesForIdentifiers.get(END_TIME_STATE));
                } catch (Exception e) {
                    statusHandler.error(
                            "unexpected problem fetching time range "
                                    + "values from megawidget", e);
                    return;
                }
                setTimeRange(otherTimeMegawidget, range);
                if (timeRangeChangeHandler != null) {
                    timeRangeChangeHandler.stateChanged(visibleEventIdentifier,
                            range);
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
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            boolean showStartEndTimeScale,
            boolean buildForWideViewing,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers) {
        initialized = true;

        /*
         * Remember the passed-in parameters.
         */
        this.minimumVisibleTime = minVisibleTime;
        this.maximumVisibleTime = maxVisibleTime;
        this.currentTimeProvider = currentTimeProvider;
        this.showStartEndTimeScale = showStartEndTimeScale;
        this.buildForWideViewing = buildForWideViewing;
        this.extraDataForEventIds = extraDataForEventIdentifiers;

        /*
         * If the view part UI elements have been built, create the time option
         * panels; otherwise, wait for the UI building before doing so.
         */
        if (timeGroup != null) {
            createTimeOptionPanels();
        }

        /*
         * Synchronize any time-based widgets with the visible time range.
         */
        visibleTimeRangeChanged();
    }

    @Override
    public void dispose() {
        for (Map.Entry<String, MegawidgetManager> entry : megawidgetManagersForEventIds
                .entrySet()) {
            recordDisplaySettingsAndExtraDataForEvent(entry.getKey(),
                    entry.getValue());
        }
        for (Resource resource : resources) {
            resource.dispose();
        }
        tabChangeHandler = null;
        categoryChangeHandler = null;
        typeChangeHandler = null;
        timeRangeChangeHandler = null;
        metadataChangeHandler = null;
        notifierInvocationHandler = null;
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
        Map<String, Object> megawidgetCreationParams = new HashMap<>(1, 1.0f);
        megawidgetCreationParams.put(IStateful.STATE_CHANGE_LISTENER,
                basicStateChangeListener);

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
         * Create group holding the time range megawidget, and give it a stack
         * layout so that the two time option panels to be created can be
         * swapped back and forth.
         */
        timeGroup = new Group(tabPagePanel, SWT.NONE);
        timeGroup.setText(TIME_RANGE_SECTION_TEXT);
        timeGroup
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        timeContentLayout = new StackLayout();
        timeGroup.setLayout(timeContentLayout);

        /*
         * If initialization has occurred, create the time option panels;
         * otherwise, wait for initialization before doing so, since these
         * panels are built based upon initialization-specified options.
         */
        if (initialized) {
            createTimeOptionPanels();
        }

        /*
         * Create the panel holding the metadata, and hide it to begin with. It
         * is hidden whenever the visible event has no metadata. Give it a stack
         * layout manager, so that it shows only one of its children at a time.
         * The visible child will be the panel that holds the megawidgets for
         * the currently displayed event.
         */
        metadataPanel = new Composite(tabPagePanel, SWT.NONE);
        metadataPanelLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        metadataPanelLayoutData.exclude = true;
        metadataPanel.setVisible(false);
        metadataPanel.setLayoutData(metadataPanelLayoutData);
        metadataPanelLayout = new StackLayout();
        metadataPanel.setLayout(metadataPanelLayout);

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
    public IStateChanger<String, Map<String, IDisplaySettings>> getMegawidgetDisplaySettingsChanger() {
        return megawidgetdisplaySettingsChanger;
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
    public IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> getTimeRangeBoundariesChanger() {
        return timeRangeBoundariesChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getDurationChanger() {
        return durationChanger;
    }

    @Override
    public IMetadataStateChanger getMetadataChanger() {
        return metadataChanger;
    }

    @Override
    public ICommandInvoker<EventScriptInfo> getNotifierInvoker() {
        return notifierInvoker;
    }

    @Override
    public ICommandInvoker<Command> getButtonInvoker() {
        return buttonInvoker;
    }

    // Private Methods

    /**
     * Create the time option panels.
     */
    private void createTimeOptionPanels() {

        /*
         * Put together the creation-time parameters needed for building the
         * non-metadata megawidgets.
         */
        Map<String, Object> megawidgetCreationParams = new HashMap<>(4, 1.0f);
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
         * Create the time scale and time range panels, holding their respective
         * megawidgets.
         */
        List<IControl> megawidgetsToAlign = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
            createTimeOptionPanel(timeGroup, megawidgetCreationParams,
                    megawidgetsToAlign);
        }

        /*
         * Align the time megawidgets labels to have the same horizontal space
         * as one another, put the time scale panel at the top of the stack, and
         * lay out the time group.
         */
        ControlComponentHelper.alignMegawidgetsElements(megawidgetsToAlign);
        timeScalePanel.layout();
        timeRangePanel.layout();
        timeContentLayout.topControl = timeScalePanel;
        timeGroup.layout();
    }

    /**
     * Create a time option panel. The first time this is called, it creates the
     * panel holding the time scale megawidget, for events that need to allow
     * the user to manipulate the start time and the end time both as absolute
     * date-time values; the second time it is called, it creates the panel
     * holding the time range megawidget, for events that need to allow the user
     * to manipulate the start time as an absolute value, and the end time as a
     * duration (offset) from the start time.
     * 
     * @param parent
     *            Parent composite into which the panel will be inserted.
     * @param megawidgetCreationParams
     *            Parameters needed for megawidget creation.
     * @param megawidgetToAlign
     *            List of megawidgets that are to be visually aligned. The
     *            method adds the time scale or time range megawidget (whichever
     *            it creates) to this list so that they may all be aligned by
     *            the caller.
     */
    private void createTimeOptionPanel(Group parent,
            Map<String, Object> megawidgetCreationParams,
            List<IControl> megawidgetsToAlign) {

        Composite timeSubPanel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginBottom = (showStartEndTimeScale ? 5 : 10);
        timeSubPanel.setLayout(gridLayout);

        /*
         * Create the appropriate time megawidget; it in turn creates as a child
         * its "until further notice" checkbox.
         */
        MultiTimeMegawidget timeMegawidget = null;
        CheckBoxMegawidget checkBoxMegawidget = null;
        Map<String, Object> timeMegawidgetParameters = new HashMap<>(
                timeScalePanel == null ? TIME_SCALE_SPECIFIER_PARAMETERS
                        : TIME_RANGE_SPECIFIER_PARAMETERS);
        timeMegawidgetParameters.put(
                MultiTimeMegawidgetSpecifier.MEGAWIDGET_SHOW_SCALE,
                showStartEndTimeScale);
        Map<String, Object> map = new HashMap<>();
        map.put(END_TIME_STATE,
                (buildForWideViewing ? UNTIL_FURTHER_NOTICE_DETAIL_FIELD_PARAMETERS
                        : UNTIL_FURTHER_NOTICE_DETAIL_FIELD_WRAPPED_PARAMETERS));
        timeMegawidgetParameters.put(
                TimeScaleSpecifier.MEGAWIDGET_DETAIL_FIELDS, map);
        if (timeScalePanel == null) {
            timeScalePanel = timeSubPanel;
            try {
                timeMegawidget = new TimeScaleSpecifier(
                        timeMegawidgetParameters).createMegawidget(
                        timeScalePanel, TimeScaleMegawidget.class,
                        megawidgetCreationParams);
            } catch (Exception e) {
                statusHandler.error(
                        "unexpected problem creating time scale and checkbox "
                                + "megawidgets", e);
            }
        } else if (timeRangePanel == null) {
            timeRangePanel = timeSubPanel;
            try {
                timeMegawidget = timeRangeMegawidget = new TimeRangeSpecifier(
                        timeMegawidgetParameters).createMegawidget(
                        timeRangePanel, TimeRangeMegawidget.class,
                        megawidgetCreationParams);
            } catch (Exception e) {
                statusHandler.error(
                        "unexpected problem creating time range and checkbox "
                                + "megawidgets", e);
            }
        } else {
            throw new IllegalStateException(
                    "method already called twice, no more to create");
        }
        checkBoxMegawidget = (CheckBoxMegawidget) (buildForWideViewing ? timeMegawidget
                .getChildren().get(0) : ((CompositeMegawidget) timeMegawidget
                .getChildren().get(0)).getChildren().get(0));
        megawidgetsToAlign.add(timeMegawidget);
        timeMegawidgets.add(timeMegawidget);
        untilFurtherNoticeToggleMegawidgets.add(checkBoxMegawidget);
    }

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
            tabsBeingChanged = false;
        } else {
            for (int j = 0; j < choiceDisplayables.size(); j++) {
                if (j < tabItems.length) {
                    tabItems[j].setText(choiceDisplayables.get(j)
                            .getDescription());
                }
            }
        }

        /*
         * Iterate through the tabs, marking any that are for events with
         * conflicts with the appropriate icon and tooltip.
         */
        tabItems = eventTabFolder.getItems();
        for (int j = 0; j < choiceDisplayables.size(); j++) {
            if (j < tabItems.length) {
                if (choiceDisplayables.get(j).isConflicting()) {
                    conflictExists = true;
                    tabItems[j].setImage(CONFLICT_TAB_ICON);
                    tabItems[j].setToolTipText(CONFLICT_TOOLTIP);
                } else {
                    tabItems[j].setImage(null);
                    tabItems[j].setToolTipText(null);
                }
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
     * Set the time range to that specified.
     * 
     * @param megawidget
     *            Megawidget for which to set the time range.
     * @param range
     *            New time range.
     */
    private void setTimeRange(MultiTimeMegawidget megawidget, TimeRange range) {
        if (isAlive() && (megawidget != null)) {
            try {

                /*
                 * If the megawidget is the time scale, not the time range, and
                 * its end time is limited to one value, and the new end time
                 * falls outside that value, change its end time allowable
                 * boundaries to equal the new end time. This must be done
                 * because the time range megawidget allows the end time to move
                 * if, for example, the duration is to be frozen but the user
                 * moves the start time, which displaces the end time.
                 */
                if (megawidget != timeRangeMegawidget) {
                    long newEndTime = range.getEnd().getTime();
                    Map<String, Long> minimumAllowableTimes = megawidget
                            .getMinimumAllowableTimes();
                    Map<String, Long> maximumAllowableTimes = megawidget
                            .getMaximumAllowableTimes();
                    long minimumEndTime = minimumAllowableTimes
                            .get(END_TIME_STATE);
                    long maximumEndTime = maximumAllowableTimes
                            .get(END_TIME_STATE);
                    if ((minimumEndTime == maximumEndTime)
                            && ((newEndTime < minimumEndTime) || (newEndTime > maximumEndTime))) {
                        minimumAllowableTimes.put(END_TIME_STATE, newEndTime);
                        maximumAllowableTimes.put(END_TIME_STATE, newEndTime);
                        megawidget.setAllowableRanges(minimumAllowableTimes,
                                maximumAllowableTimes);
                    }
                }

                /*
                 * Give the megawidget the new values it should hold.
                 */
                megawidget.setUncommittedState(START_TIME_STATE, range
                        .getStart().getTime());
                megawidget.setUncommittedState(END_TIME_STATE, range.getEnd()
                        .getTime());
                megawidget.commitStateChanges();
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + TIME_RANGE_IDENTIFIER + " megawidget values", e);
            }
        }
    }

    /**
     * Set the time range to that specified for both megawidgets.
     * 
     * @param range
     *            New time range.
     */
    private void setTimeRange(TimeRange range) {
        for (MultiTimeMegawidget megawidget : timeMegawidgets) {
            setTimeRange(megawidget, range);
        }
    }

    /**
     * Set the time range boundaries to those specified.
     * 
     * @param rangesForBoundaries
     *            Map of time range boundaries to their associated allowable
     *            ranges.
     */
    private void setTimeRangeBoundaries(
            Map<TimeRangeBoundary, Range<Long>> rangesForBoundaries) {
        if (isAlive()) {

            /*
             * Get the minimum and maximum values for the start and end times.
             */
            Map<String, Object> minimumValues = new HashMap<>(2, 1.0f);
            Range<Long> startTimeRange = rangesForBoundaries
                    .get(TimeRangeBoundary.START);
            Range<Long> endTimeRange = rangesForBoundaries
                    .get(TimeRangeBoundary.END);
            minimumValues.put(START_TIME_STATE, startTimeRange.lowerEndpoint());
            minimumValues.put(END_TIME_STATE, endTimeRange.lowerEndpoint());
            Map<String, Object> maximumValues = new HashMap<>(2, 1.0f);
            maximumValues.put(START_TIME_STATE, startTimeRange.upperEndpoint());
            maximumValues.put(END_TIME_STATE, endTimeRange.upperEndpoint());

            /*
             * Determine whether or not the start and end times should be
             * editable; if they have zero-length ranges, they should not be.
             */
            boolean startTimeEditable = (startTimeRange.lowerEndpoint().equals(
                    startTimeRange.upperEndpoint()) == false);
            boolean endTimeEditable = (endTimeRange.lowerEndpoint().equals(
                    endTimeRange.upperEndpoint()) == false);

            /*
             * Get the duration of the event as it stands.
             */
            long duration;
            try {
                duration = (Long) timeRangeMegawidget.getState(END_TIME_STATE)
                        - (Long) timeRangeMegawidget.getState(START_TIME_STATE);
            } catch (Exception e) {
                statusHandler.error("unexpected error while getting "
                        + TIME_RANGE_IDENTIFIER + " megawidget values", e);
                return;
            }

            /*
             * For each megawidget, set the states' editability and ranges.
             */
            for (MultiTimeMegawidget megawidget : timeMegawidgets) {
                try {
                    megawidget.setStateEditable(START_TIME_STATE,
                            startTimeEditable);
                    megawidget
                            .setStateEditable(END_TIME_STATE, endTimeEditable);
                    megawidget.setAllowableRanges(minimumValues, maximumValues);
                } catch (Exception e) {
                    statusHandler.error("unexpected error while setting "
                            + TIME_RANGE_IDENTIFIER
                            + " megawidget value boundaries", e);
                }
            }

            /*
             * To avoid the time range megawidget briefly showing no valid
             * duration, use the duration calculated above as an offset from the
             * new start time for the new end time.
             */
            try {
                long startTime = (Long) timeRangeMegawidget
                        .getState(START_TIME_STATE);
                setTimeRange(timeRangeMegawidget, new TimeRange(startTime,
                        startTime + duration));
            } catch (Exception e) {
                statusHandler.error("unexpected error while setting "
                        + TIME_RANGE_IDENTIFIER + " megawidget values", e);
            }
        }
    }

    /**
     * Set the duration choices for the time range megawidget to those
     * specified.
     * 
     * @param choices
     *            Choices to be used.
     */
    private void setDurationChoices(List<String> choices) {
        timeContentLayout.topControl = (choices.isEmpty() ? timeScalePanel
                : timeRangePanel);
        timeGroup.layout();
        if (choices.isEmpty() == false) {
            try {
                timeRangeMegawidget
                        .setMutableProperty(
                                TimeRangeSpecifier.MEGAWIDGET_DURATION_CHOICES,
                                choices);
            } catch (MegawidgetPropertyException e) {
                statusHandler.error("Error while setting duration choices "
                        + "for time range megawidget.", e);
            }
        }
    }

    /**
     * Set the specified end time "until further notice" checkbox to that
     * specified.
     * 
     * @param megawidget
     *            Megawidget that is to have its state set.
     * @param untilFurtherNotice
     *            Boolean flag indicating whether "until further notice" should
     *            be checked, or <code>null</code>, which is treated as
     *            {@link Boolean#FALSE}.
     */
    private void setEndTimeUntilFurtherNotice(IStateful megawidget,
            Object untilFurtherNotice) {
        if (isAlive()) {
            try {
                megawidget
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
     * Set both "until further notice" checkboxes to the specified value.
     * 
     * @param untilFurtherNotice
     *            Boolean flag indicating whether "until further notice" should
     *            be checked, or <code>null</code>, which is treated as
     *            {@link Boolean#FALSE}.
     */
    private void setEndTimeUntilFurtherNotice(Object untilFurtherNotice) {
        for (CheckBoxMegawidget megawidget : untilFurtherNoticeToggleMegawidgets) {
            setEndTimeUntilFurtherNotice(megawidget, untilFurtherNotice);
        }
    }

    /**
     * Set the specified end time "until further notice" checkbox enabled state
     * to that specified.
     * 
     * @param megawidget
     *            Megawidget to be manipulated.
     * @param enable
     *            Flag indicating whether "until further notice" should be
     *            enabled.
     */
    private void setEndTimeUntilFurtherNoticeEnabledState(
            CheckBoxMegawidget megawidget, boolean enable) {
        if (isAlive()) {
            try {
                megawidget.setEnabled(enable);
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error while setting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget enabled state", e);
            }
        }
    }

    /**
     * Set both end time "until further notice" checkboxes enabled states to
     * that specified.
     * 
     * @param enable
     *            Flag indicating whether "until further notice" should be
     *            enabled.
     */
    private void setEndTimeUntilFurtherNoticeEnabledState(boolean enable) {
        for (CheckBoxMegawidget megawidget : untilFurtherNoticeToggleMegawidgets) {
            setEndTimeUntilFurtherNoticeEnabledState(megawidget, enable);
        }
    }

    /**
     * Set the specified end time "until further notice" checkbox editability
     * state to that specified.
     * 
     * @param megawidget
     *            Megawidget to be manipulated.
     * @param editable
     *            Flag indicating whether "until further notice" should be
     *            editable.
     */
    private void setEndTimeUntilFurtherNoticeEditableState(
            CheckBoxMegawidget megawidget, boolean editable) {
        if (isAlive()) {
            try {
                megawidget.setEditable(editable);
            } catch (Exception e) {
                statusHandler
                        .error("unexpected error while setting "
                                + HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE
                                + " megawidget editability state", e);
            }
        }
    }

    /**
     * Set both end time "until further notice" checkboxes editability states to
     * that specified.
     * 
     * @param editable
     *            Flag indicating whether "until further notice" should be
     *            editable.
     */
    private void setEndTimeUntilFurtherNoticeEditableState(boolean editable) {
        for (CheckBoxMegawidget megawidget : untilFurtherNoticeToggleMegawidgets) {
            setEndTimeUntilFurtherNoticeEditableState(megawidget, editable);
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
            metadataPanel.setRedraw(false);
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
             * Delete the old megawidget manager, if any, and also delete the
             * old manager's extra data entry in the extra data cache, since an
             * entry will have been created as part of the remove() call. If no
             * old megawidget manager was found, use the previously-recorded
             * extra data for this event, if any, since some may have been
             * recorded for this event prior to this view part's existence, and
             * if so, it should be persisted. Note that persistence of the extra
             * data only occurs if no existing megawidget manager is being
             * replaced; if one is being replaced, then the new megawidget
             * manager's extra data must be used, and the old thrown away.
             */
            Map<String, Map<String, Object>> oldExtraDataMap = null;
            if (megawidgetManager != null) {
                megawidgetManagersForEventIds.remove(eventIdentifier);
                extraDataForEventIds.remove(eventIdentifier);
            } else {
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
             * saved values in the extra data to continue to function. Finally,
             * apply any saved display settings to the megawidget manager.
             */
            if (specifierManager.getSpecifiers().isEmpty() == false) {
                panel = new Composite(metadataPanel, SWT.NONE);
                GridLayout layout = new GridLayout(1, false);
                layout.marginWidth = layout.marginHeight = 0;
                panel.setLayout(layout);
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
                                    if (notifierInvocationHandler != null) {
                                        notifierInvocationHandler
                                                .commandInvoked(new EventScriptInfo(
                                                        visibleEventIdentifier,
                                                        identifier,
                                                        manager.getMutableProperties()));
                                    }
                                }

                                @Override
                                public void stateElementChanged(
                                        MegawidgetManager manager,
                                        String identifier, Object state) {
                                    if (metadataChangeHandler != null) {
                                        metadataChangeHandler.stateChanged(
                                                visibleEventIdentifier,
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
                                        metadataChangeHandler.statesChanged(
                                                visibleEventIdentifier, map);
                                    }
                                }

                                @Override
                                public void sizeChanged(
                                        MegawidgetManager manager,
                                        String identifier) {

                                    /*
                                     * No action; size changes of any children
                                     * should be handled by scrollable wrapper
                                     * megawidget.
                                     */
                                }

                                @Override
                                public void sideEffectMutablePropertyChangeErrorOccurred(
                                        MegawidgetManager manager,
                                        MegawidgetPropertyException exception) {
                                    statusHandler
                                            .error("HazardDetailViewPart.MegawidgetManager error occurred "
                                                    + "while attempting to apply megawidget interdependencies: "
                                                    + exception, exception);
                                }

                            }, minimumVisibleTime, maximumVisibleTime);
                } catch (Exception e) {
                    statusHandler.error(
                            "Could not create hazard metadata megawidgets "
                                    + "for event ID = " + eventIdentifier
                                    + ": " + e, e);
                    panel.dispose();
                    panel = null;
                }
                if (panel != null) {

                    /*
                     * If old extra data was found above in the cache, use it
                     * for the new manager.
                     */
                    if (oldExtraDataMap != null) {
                        megawidgetManager.setExtraData(oldExtraDataMap);
                    }
                    Map<String, IDisplaySettings> displaySettings = megawidgetDisplaySettingsForEventIds
                            .get(eventIdentifier);
                    if (displaySettings != null) {
                        megawidgetManager.setDisplaySettings(displaySettings);
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
         * the "until further notice" megawidgets appropriately, update the
         * layout, and turn redraw back on.
         */
        if (eventIdentifier.equals(visibleEventIdentifier)) {
            setEndTimeUntilFurtherNotice(metadataStates
                    .get(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE));
            layoutMetadataPanel(panel);
            metadataPanel.setRedraw(true);
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
    private void changeMetadataMutableProperties(String eventIdentifier,
            Map<String, Map<String, Object>> mutableProperties) {
        if (isAlive() == false) {
            return;
        }
        MegawidgetManager manager = megawidgetManagersForEventIds
                .get(eventIdentifier);
        if (manager != null) {
            try {
                manager.setMutableProperties(mutableProperties);
            } catch (Exception e) {
                statusHandler.error(
                        "Error while trying to set metadata mutable properties: "
                                + e, e);
            }
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
        recordDisplaySettingsAndExtraDataForEvent(eventIdentifier,
                megawidgetManager);
        megawidgetManager.getParent().dispose();
    }

    /**
     * Record the specified event identifier's metadata megawidgets' display
     * settings and extra data from the specified megawidget manager.
     * 
     * @param eventIdentifier
     *            Event identifier with which the megawidget manager is
     *            associated.
     * @param megawidgetManager
     *            Megawidget manager to have its display settings and extra data
     *            recorded.
     */
    private void recordDisplaySettingsAndExtraDataForEvent(
            String eventIdentifier, MegawidgetManager megawidgetManager) {
        Map<String, IDisplaySettings> displaySettings = megawidgetManager
                .getDisplaySettings();
        megawidgetDisplaySettingsForEventIds.put(eventIdentifier,
                displaySettings);
        if (megawidgetDisplaySettingsChangeHandler != null) {
            megawidgetDisplaySettingsChangeHandler.stateChanged(
                    eventIdentifier, displaySettings);
        }
        Map<String, Map<String, Object>> extraData = megawidgetManager
                .getExtraData();
        if (extraData.isEmpty() == false) {
            extraDataForEventIds.put(eventIdentifier, extraData);
        }
    }

    /**
     * Get the specified metadata element value.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param metadataIdentifier
     *            Identifier of the metadata element for which to get the value.
     * @return Value of the metadata element.
     */
    private Serializable getMetadataValue(String eventIdentifier,
            String metadataIdentifier) {
        if (eventIdentifier.equals(visibleEventIdentifier) == false) {
            return null;
        }
        if (metadataIdentifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            try {
                return (Boolean) untilFurtherNoticeToggleMegawidgets
                        .get(0)
                        .getState(
                                HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE);
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
                    return (Serializable) manager
                            .getStateElement(metadataIdentifier);
                } catch (Exception e) {
                    statusHandler.error("Error while trying to get metadata \""
                            + metadataIdentifier + "\": " + e, e);
                }
            }
        }
        return null;
    }

    /**
     * Set the enabled state of the specified metadata element to the specified
     * value.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param metadataIdentifier
     *            Identifier of the metadata element to be set.
     * @param enable
     *            Flag indicating whether or not the element should be enabled.
     */
    private void setMetadataEnabledState(String eventIdentifier,
            String metadataIdentifier, boolean enable) {
        if ((isAlive() == false)
                || (eventIdentifier.equals(visibleEventIdentifier) == false)) {
            return;
        }
        if (metadataIdentifier
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
     * @param eventIdentifier
     *            Event identifier.
     * @param metadataIdentifier
     *            Identifier of the metadata element to be set.
     * @param editable
     *            Flag indicating whether or not the element should be editable.
     */
    private void setMetadataEditabilityState(String eventIdentifier,
            String metadataIdentifier, boolean editable) {
        if ((isAlive() == false)
                || (eventIdentifier.equals(visibleEventIdentifier) == false)) {
            return;
        }
        if (metadataIdentifier
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
     * @param eventIdentifier
     *            Event identifier.
     * @param metadataIdentifier
     *            Identifier of the metadata element to be set.
     * @param value
     *            New value of the metadata element.
     */
    private void setMetadataValue(String eventIdentifier,
            String metadataIdentifier, Serializable value) {
        if ((isAlive() == false)
                || (eventIdentifier.equals(visibleEventIdentifier) == false)) {
            return;
        }
        if (metadataIdentifier
                .equals(HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE)) {
            setEndTimeUntilFurtherNotice(value);
        } else if (visibleEventIdentifier != null) {
            MegawidgetManager manager = megawidgetManagersForEventIds
                    .get(visibleEventIdentifier);
            if (manager != null) {
                Map<String, Object> map = new HashMap<>();
                map.put(metadataIdentifier, value);
                try {
                    manager.modifyState(map);
                } catch (Exception e) {
                    statusHandler.error("Error while trying to set metadata \""
                            + metadataIdentifier + "\" to " + value + ": " + e,
                            e);
                }
            }
        }
    }

    /**
     * Set the specified metadata elements to the specified values.
     * 
     * @param eventIdentifier
     *            Event identifier.
     * @param valuesForIdentifiers
     *            Map of metadata element identifiers to their new values.
     */
    private void setMetadataValues(String eventIdentifier,
            Map<String, Serializable> valuesForIdentifiers) {
        if ((isAlive() == false)
                || (eventIdentifier.equals(visibleEventIdentifier) == false)) {
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
                            "Error while trying to set metadata values: " + e,
                            e);
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
        for (MultiTimeMegawidget megawidget : timeMegawidgets) {
            megawidget.setVisibleTimeRange(minimumVisibleTime,
                    maximumVisibleTime);
        }
        for (MegawidgetManager manager : megawidgetManagersForEventIds.values()) {
            manager.setVisibleTimeRange(minimumVisibleTime, maximumVisibleTime);
        }
    }

    /**
     * Layout the metadata widget to display the specified panel, if any.
     * 
     * @param panel
     *            Panel to be displayed within the metadata widget, or
     *            <code>null</code> if there is no panel to display.
     */
    private void layoutMetadataPanel(Composite panel) {

        /*
         * If a panel was provided, set up its containers to display it
         * properly; otherwise, hide the metadata group.
         */
        if (panel != null) {
            metadataPanelLayoutData.exclude = false;
            metadataPanel.setVisible(true);
            metadataPanelLayout.topControl = panel;
            metadataPanel.layout();
        } else {
            metadataPanelLayoutData.exclude = true;
            metadataPanel.setVisible(false);
        }

        /*
         * Redo the layout of the tab page.
         */
        tabPagePanel.layout();
    }
}
