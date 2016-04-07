/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.validators;

import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecificationException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetStateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Validator used to validate graph state, which is a list of maps,
 * each of the latter holding a pair of coordinates at which to plot a point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 28, 2016   15931    Chris.Golden Initial creation.
 * Apr 06, 2016   15931    Chris.Golden Fixed bug that caused empty lists
 *                                      to be incorrectly flagged as illegal
 *                                      when presented as potential state
 *                                      values.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class GraphValidator extends
        SingleStateValidator<List<Map<String, Object>>> {

    // Public Static Constants

    /**
     * Name for the parameter in a plot point map holding the X coordinate of
     * the point.
     */
    public static final String PLOT_POINT_X = "x";

    /**
     * Name for the parameter in a plot point map holding the Y coordinate of
     * the point.
     */
    public static final String PLOT_POINT_Y = "y";

    /**
     * Name for the parameter in a plot point map holding the flag indicating
     * whether or not the point is editable by the user. This parameter is
     * optional; if not specified, it defaults to true.
     */
    public static final String PLOT_POINT_EDITABLE = "editable";

    // Public Constructors

    /**
     * Construct an uninitialized instance.
     */
    public GraphValidator() {
    }

    // Protected Constructors

    /**
     * Construct an instance that is a copy of another already-initialized
     * instance.
     * 
     * @param other
     *            Validator to be copied.
     * @throws IllegalArgumentException
     *             If <code>other</code> has not yet been initialized.
     */
    protected GraphValidator(GraphValidator other) {
        super(other);
    }

    // Public Methods

    @SuppressWarnings("unchecked")
    @Override
    public <V extends StateValidator> V copyOf() {
        return (V) new GraphValidator(this);
    }

    @Override
    public List<Map<String, Object>> convertToStateValue(Object object)
            throws MegawidgetException {
        if (object == null) {
            return new ArrayList<>();
        }
        List<?> listObject = (object instanceof List ? (List<?>) object : null);
        if (listObject != null) {
            if (listObject.isEmpty()) {
                return new ArrayList<>();
            } else {
                boolean success = true;
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(
                        listObject.size());
                Set<Integer> plottedXs = new HashSet<>();
                for (Object itemObject : listObject) {
                    if ((itemObject instanceof Map) == false) {
                        success = false;
                        break;
                    }
                    Map<?, ?> mapObject = (Map<?, ?>) itemObject;
                    Integer x = null, y = null;
                    boolean editable = true;
                    for (Map.Entry<?, ?> entry : mapObject.entrySet()) {
                        if (entry.getKey().equals(PLOT_POINT_X)
                                || entry.getKey().equals(PLOT_POINT_Y)) {
                            if (entry.getValue() instanceof Number == false) {
                                success = false;
                                break;
                            }
                            int value = ((Number) entry.getValue()).intValue();
                            if (entry.getKey().equals(PLOT_POINT_X)) {
                                x = value;
                                if (plottedXs.contains(x)) {
                                    success = false;
                                    break;
                                }
                                plottedXs.add(x);
                            } else {
                                y = value;
                            }
                        } else if (entry.getKey().equals(PLOT_POINT_EDITABLE)) {
                            if (entry.getValue() instanceof Number) {
                                editable = (((Number) entry.getValue())
                                        .intValue() != 0);
                            } else if (entry.getValue() instanceof Boolean) {
                                editable = (Boolean) entry.getValue();
                            }
                        }
                    }
                    if ((success == false) || (x == null) || (y == null)) {
                        break;
                    }
                    Map<String, Object> map = new HashMap<>(mapObject.size(),
                            1.0f);
                    map.put(PLOT_POINT_X, x);
                    map.put(PLOT_POINT_Y, y);
                    map.put(PLOT_POINT_EDITABLE, editable);
                    list.add(map);
                }
                if (success) {
                    return list;
                }
            }
        }
        throw new MegawidgetStateException(getIdentifier(), getType(), object,
                "must be list of zero or more maps, each holding a pair of "
                        + "X and Y coordinates, and optionally an editable "
                        + "flag, with no X values repeated");
    }

    // Protected Methods

    @Override
    protected void doInitialize() throws MegawidgetSpecificationException {

        /*
         * No action.
         */
    }
}
