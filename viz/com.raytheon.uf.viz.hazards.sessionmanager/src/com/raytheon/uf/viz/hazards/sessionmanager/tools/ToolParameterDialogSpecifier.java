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

import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.DialogButtonsSpecifier;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.megawidgets.MultiTimeMegawidget;

/**
 * Class representing a specification for a tool parameters gathering dialog.
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
public class ToolParameterDialogSpecifier extends AbstractToolDialogSpecifier {

    /**
     * Specifier for customized buttons, if the dialog is to have such. If
     * <code>null</code>, the dialog will have the standard "Run" and "Cancel"
     * buttons.
     */
    private final DialogButtonsSpecifier customButtonsSpecifier;

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
     * @param customButtonsSpecifier
     *            Optional specifier for customized buttons, if the dialog is to
     *            have such. If <code>null</code>, the dialog will have the
     *            standard "Run" and "Cancel" buttons.
     */
    public ToolParameterDialogSpecifier(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            Map<String, Object> initialStatesForMegawidgets, String title,
            int minInitialWidth, int maxInitialWidth, int maxInitialHeight,
            long minVisibleTime, long maxVisibleTime,
            DialogButtonsSpecifier customButtonsSpecifier) {
        super(megawidgetSpecifierManager, initialStatesForMegawidgets, title,
                minInitialWidth, maxInitialWidth, maxInitialHeight,
                minVisibleTime, maxVisibleTime);
        this.customButtonsSpecifier = customButtonsSpecifier;
    }

    /**
     * Construct an instance based upon the specified map. The map must conform
     * to the provisions laid out by
     * {@link AbstractToolDialogSpecifier#AbstractToolDialogSpecifier(Map, String, long, long, ICurrentTimeProvider)}
     * . Additionally, it has the following entries:
     * <dl>
     * <dt><code>buttons</code></dt>
     * <dd>Optional parameter that, if provided, must be a list as is provided
     * to the {@link DialogButtonsSpecifier#DialogButtonsSpecifier(List)} as its
     * parameter.</dd>
     * <dt><code>visualFeatures</code></dt>
     * <dd>Optional parameter that, if provided, must be a list of visual
     * features to be displayed along with the dialog. Note that if this is
     * included, there must be a HiddenField megawidget specifier found within
     * the "fields" entry of <code>rawSpecifier</code> for each such visual
     * feature, with the "fieldName" being the same as the visual feature's
     * identifier in each case. This way, when a visual feature is changed, the
     * corresponding hidden field's value is changed to include the updated
     * version, and any interdependency script that was provided may be used to
     * react to the change.</dd>
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
    public ToolParameterDialogSpecifier(Map<String, ?> rawSpecifier,
            String scriptFilePath, long minVisibleTime, long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider)
            throws IllegalArgumentException {

        super(rawSpecifier, scriptFilePath, minVisibleTime, maxVisibleTime,
                currentTimeProvider);

        List<? extends Map<String, ?>> customButtonsRawSpecifier = null;
        try {
            customButtonsRawSpecifier = (List<? extends Map<String, ?>>) rawSpecifier
                    .get(HazardConstants.BUTTONS_KEY);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "invalid custom buttons specifier", e);
        }
        if (customButtonsRawSpecifier != null) {
            this.customButtonsSpecifier = new DialogButtonsSpecifier(
                    customButtonsRawSpecifier);
        } else {
            this.customButtonsSpecifier = null;
        }
    }

    // Public Methods

    /**
     * Get the custom buttons specifier, if the dialog is to have custom
     * buttons.
     * 
     * @return Custom buttons specifier, or <code>null</code> if the dialog is
     *         to have the standard "Run" and "Cancel" buttons.
     */
    public DialogButtonsSpecifier getCustomButtonsSpecifier() {
        return customButtonsSpecifier;
    }
}
