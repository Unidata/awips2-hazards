/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.viz.megawidgets.validators.GraphValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Description: Graph megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 28, 2016   15931    Chris.Golden Initial creation.
 * Apr 06, 2016   15931    Chris.Golden Added ability to allow the user
 *                                      to draw points via a click, drag,
 *                                      and release mouse operation, if
 *                                      the graph is empty of points.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GraphSpecifier extends StatefulMegawidgetSpecifier implements
        IControlSpecifier, IRapidlyChangingStatefulSpecifier {

    // Public Static Constants

    /**
     * X hatch interval parameter name; a megawidget may include a non-negative
     * integer for this parameter, indicating the interval between vertical
     * hatch marks, or if <code>0</code>, that no such hatch marks are to be
     * drawn. Defaults to <code>0</code>.
     */
    public static final String MEGAWIDGET_X_HATCH_INTERVAL = "xHatchInterval";

    /**
     * Y hatch interval parameter name; a megawidget may include a non-negative
     * integer for this parameter, indicating the interval between horizontal
     * hatch marks, or if <code>0</code>, that no such hatch marks are to be
     * drawn. Defaults to <code>0</code>.
     */
    public static final String MEGAWIDGET_Y_HATCH_INTERVAL = "yHatchInterval";

    /**
     * X label interval parameter name; a megawidget may include a non-negative
     * integer for this parameter, indicating the interval between labels along
     * the X axis, or if <code>0</code>, that no such labels are to be drawn.
     * Defaults to <code>0</code>. The value must be a multiple of the value
     * associated with {@link #MEGAWIDGET_X_HATCH_INTERVAL}; if the latter is
     * <code>0</code>, this must be <code>0</code> as well.
     */
    public static final String MEGAWIDGET_X_LABEL_INTERVAL = "xLabelInterval";

    /**
     * Y label interval parameter name; a megawidget may include a non-negative
     * integer for this parameter, indicating the interval between labels along
     * the Y axis, or if <code>0</code>, that no such labels are to be drawn.
     * Defaults to <code>0</code>. The value must be a multiple of the value
     * associated with {@link #MEGAWIDGET_Y_HATCH_INTERVAL}; if the latter is
     * <code>0</code>, this must be <code>0</code> as well.
     */
    public static final String MEGAWIDGET_Y_LABEL_INTERVAL = "yLabelInterval";

    /**
     * X label suffix parameter name; a megawidget may include a string for this
     * parameter, indicating the suffix to be appended to each label along the X
     * axis. If labels are not to be displayed along the X axis (see
     * {@link #MEGAWIDGET_X_LABEL_INTERVAL} for details), this is ignored. The
     * default is to use no suffix.
     */
    public static final String MEGAWIDGET_X_LABEL_SUFFIX = "xLabelSuffix";

    /**
     * Y label suffix parameter name; a megawidget may include a string for this
     * parameter, indicating the suffix to be appended to each label along the Y
     * axis. If labels are not to be displayed along the Y axis (see
     * {@link #MEGAWIDGET_Y_LABEL_INTERVAL} for details), this is ignored. The
     * default is to use no suffix.
     */
    public static final String MEGAWIDGET_Y_LABEL_SUFFIX = "yLabelSuffix";

    /**
     * Y minimum parameter name; a megawidget must include an integer for this
     * parameter, indicating the minimum value shown on the Y axis. This value
     * must be less than that provided for {@link #MEGAWIDGET_Y_MAXIMUM}.
     */
    public static final String MEGAWIDGET_Y_MINIMUM = "yMinimum";

    /**
     * Y maximum parameter name; a megawidget must include an integer for this
     * parameter, indicating the maximum value shown on the Y axis. This value
     * must be greater than that provided for {@link #MEGAWIDGET_Y_MINIMUM}.
     */
    public static final String MEGAWIDGET_Y_MAXIMUM = "yMaximum";

    /**
     * Y colors parameter name; a megawidget may include a list of maps, with
     * each map providing "red", "green", and "blue" values ranging between
     * <code>0.0</code> and <code>1.0</code> inclusive. The specified colors are
     * used to paint the graph at different Y intervals, with the interval being
     * determined by the number of colors specified in this parameter divided by
     * the delta between the {@link #MEGAWIDGET_Y_MAXIMUM} and
     * {@link #MEGAWIDGET_Y_MINIMUM} values.
     */
    public static final String MEGAWIDGET_VERTICAL_COLORS = "yColors";

    /**
     * <p>
     * X interval for drawn points parameter name; a megawidget may include a
     * non-negative integer for this parameter, indicating the interval between
     * points that are drawn by the user. Note that drawing only may occur if
     * there are no plotted points, and the user clicks, drags, and release the
     * mouse over the graph's body. If <code>0</code>, the drawing capability is
     * disabled. Defaults to <code>0</code>.
     * </p>
     * <p>
     * Note that the megawidget cannot have any points drawn unless it has at
     * least once, prior to having an empty list of points as its state, had a
     * non-empty list. This is because it determines what the X range of the
     * points being drawn is from the previous range.
     * </p>
     */
    public static final String MEGAWIDGET_DRAWN_POINTS_INTERVAL = "drawnPointsInterval";

    // Private Variables

    /**
     * Flag indicating whether or not state changes that are part of a group of
     * rapid changes are to result in notifications to the listener.
     */
    private final boolean sendingEveryChange;

    /**
     * Minimum Y value to be shown on the graph.
     */
    private final int minimumY;

    /**
     * Maximum Y value to be shown on the graph.
     */
    private final int maximumY;

    /**
     * Interval between vertical hatch lines along the X axis, or <code>0</code>
     * if none are to be drawn.
     */
    private final int intervalHatchX;

    /**
     * Interval between horizontal hatch lines along the Y axis, or
     * <code>0</code> if none are to be drawn.
     */
    private final int intervalHatchY;

    /**
     * Interval between labels along the X axis, or <code>0</code> if none are
     * to be drawn. Must be <code>0</code> if {@link #intervalHatchX} is
     * <code>0</code>; otherwise, it can be either <code>0</code> or a multiple
     * of <code>xHatchInterval</code>.
     */
    private final int intervalLabelX;

    /**
     * Interval between labels along the Y axis, or <code>0</code> if none are
     * to be drawn. Must be <code>0</code> if {@link #intervalHatchY} is
     * <code>0</code>; otherwise, it can be either <code>0</code> or a multiple
     * of <code>yHatchInterval</code>.
     */
    private final int intervalLabelY;

    /**
     * Suffix to be appended to any labels along the X axis; may be an empty
     * string.
     */
    private final String suffixLabelX;

    /**
     * Suffix to be appended to any labels along the Y axis; may be an empty
     * string.
     */
    private final String suffixLabelY;

    /**
     * Interval between points drawn by the user when the latter clicks, drags,
     * and releases the mouse over the graph when the latter is empty. If
     * <code>0</code>, drawing capability is disabled.
     */
    private final int drawnPointsInterval;

    /**
     * List of colors, each color represented by a map providing red, green, and
     * blue components between <code>0.0</code> and <code>1.0</code> inclusive.
     */
    private final List<Map<String, Double>> verticalColors;

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public GraphSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new GraphValidator());

        /*
         * Ensure the X and Y hatch, label, and drawn points (X only for the
         * latter) intervals, if provided, are all appropriate.
         */
        intervalHatchX = getNonNegativeIntegerFromObject(parameters,
                MEGAWIDGET_X_HATCH_INTERVAL, 0);
        intervalHatchY = getNonNegativeIntegerFromObject(parameters,
                MEGAWIDGET_Y_HATCH_INTERVAL, 0);
        intervalLabelX = getNonNegativeIntegerFromObject(parameters,
                MEGAWIDGET_X_LABEL_INTERVAL, 0);
        if (((intervalHatchX == 0) && (intervalLabelX != 0))
                || ((intervalHatchX != 0) && (intervalLabelX % intervalHatchX != 0))) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_X_LABEL_INTERVAL, intervalLabelX,
                    "must be 0 if " + MEGAWIDGET_X_HATCH_INTERVAL + " is 0, "
                            + "otherwise must be 0 or a multiple of "
                            + MEGAWIDGET_X_HATCH_INTERVAL);
        }
        intervalLabelY = getNonNegativeIntegerFromObject(parameters,
                MEGAWIDGET_Y_LABEL_INTERVAL, 0);
        if (((intervalHatchY == 0) && (intervalLabelY != 0))
                || ((intervalHatchY != 0) && (intervalLabelY % intervalHatchY != 0))) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_Y_LABEL_INTERVAL, intervalLabelY,
                    "must be 0 if " + MEGAWIDGET_Y_HATCH_INTERVAL + " is 0, "
                            + "otherwise must be 0 or a multiple of "
                            + MEGAWIDGET_Y_HATCH_INTERVAL);
        }
        drawnPointsInterval = getNonNegativeIntegerFromObject(parameters,
                MEGAWIDGET_DRAWN_POINTS_INTERVAL, 0);

        /*
         * Ensure the label suffixes, if provided, are appropriate.
         */
        Object suffix = parameters.get(MEGAWIDGET_X_LABEL_SUFFIX);
        suffixLabelX = (suffix == null ? "" : suffix.toString());
        suffix = parameters.get(MEGAWIDGET_Y_LABEL_SUFFIX);
        suffixLabelY = (suffix == null ? "" : suffix.toString());

        /*
         * Ensure the Y minimum and maximum values are appropriate.
         */
        minimumY = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(),
                parameters.get(MEGAWIDGET_Y_MINIMUM), MEGAWIDGET_Y_MINIMUM,
                null);
        maximumY = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(),
                parameters.get(MEGAWIDGET_Y_MAXIMUM), MEGAWIDGET_Y_MAXIMUM,
                null);
        if (minimumY >= maximumY) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_Y_MINIMUM, minimumY,
                    "must be smaller than value for " + MEGAWIDGET_Y_MAXIMUM);
        }

        /*
         * Ensure that the vertical colors list, if provided, is appropriate.
         */
        Object colorsObj = parameters.get(MEGAWIDGET_VERTICAL_COLORS);
        List<Map<String, Double>> verticalColors = new ArrayList<>();
        boolean invalid = false;
        if (colorsObj != null) {
            if (colorsObj instanceof List) {
                List<?> list = (List<?>) colorsObj;
                for (Object element : list) {
                    try {
                        verticalColors.add(ConversionUtilities
                                .getSpecifierColorAsMapFromObject(
                                        getIdentifier(), getType(), element,
                                        MEGAWIDGET_VERTICAL_COLORS, null));
                    } catch (MegawidgetSpecificationException e) {
                        invalid = true;
                        break;
                    }
                }
            } else {
                invalid = true;
            }
            if (invalid) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_VERTICAL_COLORS, colorsObj,
                        "must be list of maps representing colors, each "
                                + "map holding values ranging from 0.0 to "
                                + "1.0 for red, green, and blue entries");
            }
        }
        this.verticalColors = ImmutableList.copyOf(verticalColors);

        /*
         * Get the standard control option values.
         */
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the rapid change notification flag, if provided, is
         * appropriate.
         */
        sendingEveryChange = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SEND_EVERY_STATE_CHANGE),
                        MEGAWIDGET_SEND_EVERY_STATE_CHANGE, true);
    }

    // Public Methods

    @Override
    public boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public boolean isSendingEveryChange() {
        return sendingEveryChange;
    }

    /**
     * Get the Y minimum.
     * 
     * @return Y minimum.
     */
    public int getMinimumY() {
        return minimumY;
    }

    /**
     * Get the Y maximum.
     * 
     * @return Y maximum.
     */
    public int getMaximumY() {
        return maximumY;
    }

    /**
     * Get the interval between vertical hatch lines along the X axis, or
     * <code>0</code> if none are to be drawn.
     * 
     * @return Interval between vertical hatch lines.
     */
    public int getIntervalHatchX() {
        return intervalHatchX;
    }

    /**
     * Get the interval between horizontal hatch lines along the Y axis, or
     * <code>0</code> if none are to be drawn.
     * 
     * @return Interval between horizontal hatch lines.
     */
    public int getIntervalHatchY() {
        return intervalHatchY;
    }

    /**
     * Get the interval between labels along the X axis, or <code>0</code> if
     * none are to be drawn.
     * 
     * @return Interval between labels along the X axis.
     */
    public int getIntervalLabelX() {
        return intervalLabelX;
    }

    /**
     * Get the interval between labels along the Y axis, or <code>0</code> if
     * none are to be drawn.
     * 
     * @return Interval between labels along the Y axis.
     */
    public int getIntervalLabelY() {
        return intervalLabelY;
    }

    /**
     * Get the suffix to be appended to any labels along the X axis; may be an
     * empty string.
     * 
     * @return Suffix.
     */
    public String getSuffixLabelX() {
        return suffixLabelX;
    }

    /**
     * Get the suffix to be appended to any labels along the Y axis; may be an
     * empty string.
     * 
     * @return Suffix.
     */
    public String getSuffixLabelY() {
        return suffixLabelY;
    }

    /**
     * Get the list of vertical colors.
     * 
     * @return List of vertical colors; may be empty.
     */
    public List<Map<String, Double>> getVerticalColors() {
        return verticalColors;
    }

    /**
     * Get the interval between points drawn by the user when the graph has no
     * plotted points and the user clicks, drags, and releases the mouse over
     * the graph. If <code>0</code>, drawing capability is disabled.
     * 
     * @return Interval between points drawn by the user.
     */
    public int getDrawnPointsInterval() {
        return drawnPointsInterval;
    }

    // Private Methods

    /**
     * Get a non-negative integer from the specified parameters map under the
     * specified key.
     * 
     * @param parameters
     *            Map holding the parameters being used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @param key
     *            Key within <code>parameters</code> under which to find the
     *            non-negative integer value.
     * @param defValue
     *            If present, this is the default value to be returned if <code>
     *            object</code> is <code>null</code>; if this parameter is
     *            <code>null</code>, then finding no value for <code>object
     *            </code> causes an exception.
     * @return Non-negative integer value.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameter is invalid.
     */
    private int getNonNegativeIntegerFromObject(Map<String, Object> parameters,
            String key, Integer defaultValue)
            throws MegawidgetSpecificationException {
        int value = ConversionUtilities.getSpecifierIntegerValueFromObject(
                getIdentifier(), getType(), parameters.get(key), key,
                defaultValue);
        if (value < 0) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), key, value, "must be non-negative integer");
        }
        return value;
    }
}
