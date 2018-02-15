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

import java.util.Map;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.MultiTimeMegawidget;

/**
 * Class representing a specification for a tool results dialog.
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
public class ToolResultDialogSpecifier extends AbstractToolDialogSpecifier {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetSpecifierManager
     *            Megawidget specifier manager holding the megawidgets to be
     *            shown in the dialog.
     * @param initialStatesForMegawidgets
     *            Dictionary holding initial state values for the megawidgets to
     *            be built from the specifiers within
     *            <code>megawidgetSpecifierManager</code>.
     * @param title
     *            Optional title of the dialog; if <code>null</code>, no title
     *            is specified.
     * @param minInitialWidth
     *            Optional minimum initial width of the dialog. If
     *            <code>-1</code>, no minimum initial width is specified.
     * @param maxInitialWidth
     *            Optional maximum initial width of the dialog. If
     *            <code>-1</code>, no maximum initial width is specified.
     * @param maxInitialHeight
     *            Optional maximum initial height of the dialog. If
     *            <code>-1</code>, no maximum initial height is specified.
     * @param minVisibleTime
     *            Minimum visible time for any {@link MultiTimeMegawidget}
     *            subclass instances within the dialog.
     * @param maxVisibleTime
     *            Maximum visible time for any {@link MultiTimeMegawidget}
     *            subclass instances within the dialog.
     */
    public ToolResultDialogSpecifier(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            Map<String, Object> initialStatesForMegawidgets, String title,
            int minInitialWidth, int maxInitialWidth, int maxInitialHeight,
            long minVisibleTime, long maxVisibleTime) {
        super(megawidgetSpecifierManager, initialStatesForMegawidgets, title,
                minInitialWidth, maxInitialWidth, maxInitialHeight,
                minVisibleTime, maxVisibleTime);
    }

    /**
     * Construct an instance based upon the specified map. The map must conform
     * to the provisions laid out by
     * {@link AbstractToolDialogSpecifier#AbstractToolDialogSpecifier(Map, String, long, long, ICurrentTimeProvider)}
     * .
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
    public ToolResultDialogSpecifier(Map<String, ?> rawSpecifier,
            String scriptFilePath, long minVisibleTime, long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider)
            throws IllegalArgumentException {
        super(rawSpecifier, scriptFilePath, minVisibleTime, maxVisibleTime,
                currentTimeProvider);
    }
}
