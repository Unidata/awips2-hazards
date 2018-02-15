/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.IControlSpecifier;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.MultiTimeMegawidget;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

/**
 * Abstract class representing a specification for a tool dialog.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * May 18, 2018    3782    Chris.Golden  Initial creation.
 *
 * </pre>
 *
 * @author Chris.Golden
 */
public abstract class AbstractToolDialogSpecifier {

    // Private Variables

    /**
     * Megawidget specifier manager holding the megawidgets to be shown in the
     * dialog.
     */
    private final MegawidgetSpecifierManager megawidgetSpecifierManager;

    /**
     * Map holding initial state values for the megawidgets to be built from the
     * specifiers within {@link #megawidgetSpecifierManager}.
     */
    private final Map<String, Object> initialStatesForMegawidgets;

    /**
     * Optional title of the dialog; if <code>null</code>, no title is
     * specified.
     */
    private final String title;

    /**
     * Optional minimum initial width of the dialog in pixels. If
     * <code>-1</code>, no minimum initial width is specified.
     */
    private final int minInitialWidth;

    /**
     * Optional maximum initial width of the dialog in pixels. If
     * <code>-1</code>, no maximum initial width is specified.
     */
    private final int maxInitialWidth;

    /**
     * Optional maximum initial height of the dialog in pixels. If
     * <code>-1</code>, no maximum initial height is specified.
     */
    private final int maxInitialHeight;

    /**
     * Minimum visible epoch time in milliseconds for any
     * {@link MultiTimeMegawidget} subclass instances within the dialog.
     */
    private final long minVisibleTime;

    /**
     * Maximum visible epoch time in milliseconds for any
     * {@link MultiTimeMegawidget} subclass instances within the dialog.
     */
    private final long maxVisibleTime;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetSpecifierManager
     *            Megawidget specifier manager holding the megawidgets to be
     *            shown in the dialog.
     * @param initialStatesForMegawidgets
     *            Map holding initial state values for the megawidgets to be
     *            built from the specifiers within
     *            <code>megawidgetSpecifierManager</code>.
     * @param title
     *            Optional title of the dialog; if <code>null</code>, no title
     *            is specified.
     * @param minInitialWidth
     *            Optional minimum initial width of the dialog in pixels. If
     *            <code>-1</code>, no minimum initial width is specified.
     * @param maxInitialWidth
     *            Optional maximum initial width of the dialog in pixels. If
     *            <code>-1</code>, no maximum initial width is specified.
     * @param maxInitialHeight
     *            Optional maximum initial height of the dialog in pixels. If
     *            <code>-1</code>, no maximum initial height is specified.
     * @param minVisibleTime
     *            Minimum visible epoch time in milliseconds for any
     *            {@link MultiTimeMegawidget} subclass instances within the
     *            dialog.
     * @param maxVisibleTime
     *            Maximum visible epoch time in milliseconds for any
     *            {@link MultiTimeMegawidget} subclass instances within the
     *            dialog.
     */
    public AbstractToolDialogSpecifier(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            Map<String, Object> initialStatesForMegawidgets, String title,
            int minInitialWidth, int maxInitialWidth, int maxInitialHeight,
            long minVisibleTime, long maxVisibleTime) {
        this.megawidgetSpecifierManager = megawidgetSpecifierManager;
        this.initialStatesForMegawidgets = initialStatesForMegawidgets;
        this.title = title;
        this.minInitialWidth = minInitialWidth;
        this.maxInitialWidth = maxInitialWidth;
        this.maxInitialHeight = maxInitialHeight;
        this.minVisibleTime = minVisibleTime;
        this.maxVisibleTime = maxVisibleTime;
    }

    /**
     * Construct an instance based upon the specified map holding the following
     * entries:
     * 
     * <dl>
     * <dt><code>fields</code></dt>
     * <dd>List of maps, with each of the latter defining a megawidget
     * specifier. Alternatively, this may be a single megawidget specifier map.
     * </dd>
     * <dt><code>valueDict</code></dt>
     * <dd>Map pairing identifiers from any megawidget specifiers from the
     * <code>fields</code> entry that are stateful with their initial states.
     * </dd>
     * <dt><code>title</code></dt>
     * <dd>Optional string giving the dialog title.</dd>
     * <dt><code>minInitialWidth</code></dt>
     * <dd>Optional integer giving the minimum initial width the dialog should
     * be allowed in pixels.</dd>
     * <dt><code>maxInitialWidth</code></dt>
     * <dd>Optional integer giving the maximum initial width the dialog should
     * be allowed in pixels.</dd>
     * <dt><code>maxInitialHeight</code></dt>
     * <dd>Optional integer giving the maximum initial height the dialog should
     * be allowed in pixels.</dd>
     * </dl>
     * 
     * @param rawSpecifier
     *            Map to be parsed to create the specifier.
     * @param scriptFilePath
     *            Path to the tool script file.
     * @param minVisibleTime
     *            Minimum visible epoch time in milliseconds; this may be used
     *            by any time-range-displaying megawidgets.
     * @param maxVisibleTime
     *            Maximum visible epoch time in milliseconds; this may be used
     *            by any time-range-displaying megawidgets.
     * @param currentTimeProvider
     *            Current time provider, to be used for the megawidget
     *            specifiers as needed.
     * @throws IllegalArgumentException
     *             If <code>rawSpecifier</code> does not hold a valid
     *             specification.
     */
    @SuppressWarnings("unchecked")
    public AbstractToolDialogSpecifier(Map<String, ?> rawSpecifier,
            String scriptFilePath, long minVisibleTime, long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider)
            throws IllegalArgumentException {

        /*
         * Get the megawidget specifiers, ensuring that they end up embedded
         * within a scrollable container megawidget specifier.
         */
        Object megawidgetSpecifiersObj = rawSpecifier
                .get(HazardConstants.FIELDS);
        if (megawidgetSpecifiersObj == null) {
            throw new IllegalArgumentException(
                    "no megawidget specifiers provided");
        }
        List<Map<String, Object>> megawidgetSpecifiersList = new ArrayList<>();
        try {
            if (megawidgetSpecifiersObj instanceof List) {
                for (Object megawidgetSpecifier : (List<?>) megawidgetSpecifiersObj) {
                    megawidgetSpecifiersList
                            .add((Map<String, Object>) megawidgetSpecifier);
                }
            } else {
                megawidgetSpecifiersList
                        .add((Map<String, Object>) megawidgetSpecifiersObj);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "invalid megawidget specifiers (must be list of maps or single map, with said map(s) holding specifiers)");
        }
        megawidgetSpecifiersList = MegawidgetSpecifierManager
                .makeRawSpecifiersScrollable(megawidgetSpecifiersList, 10, 10,
                        10, 0);

        /*
         * Get the script file path, and from it the interdependency applier, if
         * one is appropriate.
         */
        File scriptFile = null;
        try {
            scriptFile = new File(scriptFilePath);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        PythonSideEffectsApplier sideEffectsApplier = null;
        if (PythonSideEffectsApplier
                .containsSideEffectsEntryPointFunction(scriptFile)) {
            sideEffectsApplier = new PythonSideEffectsApplier(scriptFile);
        }

        /*
         * Create the megawidget specifier manager with the parameters compiled
         * above.
         */
        try {
            this.megawidgetSpecifierManager = new MegawidgetSpecifierManager(
                    megawidgetSpecifiersList, IControlSpecifier.class,
                    currentTimeProvider, sideEffectsApplier);
        } catch (MegawidgetSpecificationException e) {
            throw new IllegalArgumentException("invalid megawidget specifiers",
                    e);
        }

        /*
         * Get the initial states, including any entries for hidden fields that
         * have been requested.
         */
        Map<String, Object> initialStatesForMegawidgets = null;
        try {
            initialStatesForMegawidgets = (Map<String, Object>) rawSpecifier
                    .get(HazardConstants.VALUES_DICTIONARY_KEY);
            if (initialStatesForMegawidgets == null) {
                initialStatesForMegawidgets = Collections.emptyMap();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid initial states map");
        }
        this.initialStatesForMegawidgets = initialStatesForMegawidgets;

        /*
         * Get the title, if any.
         */
        try {
            this.title = (String) rawSpecifier.get(HazardConstants.TITLE_KEY);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid non-string title");
        }

        /*
         * Get the width and height parameters, if any.
         */
        this.minInitialWidth = getOptionalIntegerValue(rawSpecifier,
                HazardConstants.MIN_INITIAL_WIDTH_KEY, "minimum initial width");
        this.maxInitialWidth = getOptionalIntegerValue(rawSpecifier,
                HazardConstants.MAX_INITIAL_WIDTH_KEY, "maximum initial width");
        this.maxInitialHeight = getOptionalIntegerValue(rawSpecifier,
                HazardConstants.MAX_INITIAL_HEIGHT_KEY,
                "maximum initial height");

        /*
         * Get the minimum and maximum visible times.
         */
        this.minVisibleTime = minVisibleTime;
        this.maxVisibleTime = maxVisibleTime;
    }

    // Public Methods

    /**
     * Get the megawidget specifier manager holding the megawidgets to be shown
     * in the dialog.
     * 
     * @return Megawidget specifier manager.
     */
    public MegawidgetSpecifierManager getMegawidgetSpecifierManager() {
        return megawidgetSpecifierManager;
    }

    /**
     * Get the map holding initial state values for the megawidgets to be built
     * from the specifiers given by {@link #getMegawidgetSpecifierManager()}.
     * 
     * @return Map holding initial state values for the megawidgets.
     */
    public Map<String, Object> getInitialStatesForMegawidgets() {
        return initialStatesForMegawidgets;
    }

    /**
     * Get the title of the dialog, if any.
     * 
     * @return Title of the dialog, or <code>null</code> if no custom title is
     *         to be used.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the minimum initial width of the dialog in pixels, if any.
     * 
     * @return Minimum initial width. If <code>-1</code>, no minimum initial
     *         width is specified.
     */
    public int getMinInitialWidth() {
        return minInitialWidth;
    }

    /**
     * Get the maximum initial width of the dialog in pixels, if any.
     * 
     * @return Maximum initial width. If <code>-1</code>, no maximum initial
     *         width is specified.
     */
    public int getMaxInitialWidth() {
        return maxInitialWidth;
    }

    /**
     * Get the maximum initial height of the dialog in pixels, if any.
     * 
     * @return Maximum initial height. If <code>-1</code>, no maximum initial
     *         height is specified.
     */
    public int getMaxInitialHeight() {
        return maxInitialHeight;
    }

    /**
     * Get the minimum visible epoch time in milliseconds for any
     * {@link MultiTimeMegawidget} subclass instances within the dialog.
     * 
     * @return Minimum visible time.
     */
    public long getMinVisibleTime() {
        return minVisibleTime;
    }

    /**
     * Get the maximum visible epoch time in milliseconds for any
     * {@link MultiTimeMegawidget} subclass instances within the dialog.
     * 
     * @return Maximum visible time.
     */
    public long getMaxVisibleTime() {
        return maxVisibleTime;
    }

    // Private Methods

    /**
     * Get an optional integer value from the specified map under the specified
     * key.
     * 
     * @param rawSpecifier
     *            Specifier from which to fetch the value.
     * @param key
     *            Key under which to find the value.
     * @param parameterDescription
     *            Description of the parameter being fetched.
     * @return Integer value; this will be <code>-1</code> if the value was not
     *         provided within the map.
     * @throws IllegalArgumentException
     *             If the specifier provides the parameter incorrectly.
     */
    private int getOptionalIntegerValue(Map<String, ?> rawSpecifier, String key,
            String parameterDescription) throws IllegalArgumentException {
        if (rawSpecifier.containsKey(key)) {
            try {
                return ((Number) rawSpecifier.get(key)).intValue();
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "invalid null or non-numerical "
                                + parameterDescription);
            }
        }
        return -1;
    }
}
