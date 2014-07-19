/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.viz.megawidgets.validators.BoundedComparableValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;

/**
 * Time delta specifier, used to create a spinner-based megawidget that allows
 * manipulation of time deltas. megawidget created by this specifier expresses
 * its state as a standard integer (not a long integer) in milliseconds, or if
 * given a parameter value keyed to the {@link #MEGAWIDGET_STATE_UNIT} key, in
 * the specified unit. The {@link #MEGAWIDGET_MIN_VALUE} and
 * {@link #MEGAWIDGET_MAX_VALUE} are expressed in the state unit, so if for
 * example the {@link #MEGAWIDGET_STATE_UNIT} is given as "minutes", then the
 * minimum and maximum values are treated as minutes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 21, 2013   2168     Chris.Golden      Changed to implement ISingleLineSpecifier
 *                                           and use ControlSpecifierOptionsManager
 *                                           (composition over inheritance).
 * Nov 04, 2013   2336     Chris.Golden      Added implementation of new superclass-
 *                                           specified abstract method. Also changed
 *                                           to offer option of not notifying
 *                                           listeners of state changes caused by
 *                                           ongoing spinner button presses.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982     Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel".
 * Jun 27, 2014   3512     Chris.Golden      Extracted Unit and made a new
 *                                           non-inner class out of it (TimeDeltaUnit).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeDeltaMegawidget
 */
public class TimeDeltaSpecifier extends BoundedValueMegawidgetSpecifier<Long>
        implements ISingleLineSpecifier, IRapidlyChangingStatefulSpecifier {

    // Public Static Constants

    /**
     * Possible value units parameter name; a megawidget must include an array
     * of one or more of the following strings associated with this name:
     * "seconds", "minutes", "hours", and "days". These indicate which units may
     * be used within the megawidget's GUI to specify the state value; if, for
     * example, the user chooses to use "minutes", then this label will be
     * displayed next to the spinner, and the number entered will be multiplied
     * by 60000 in order to yield the state value in milliseconds.
     */
    public static final String MEGAWIDGET_UNIT_CHOICES = "unitChoices";

    /**
     * Current value unit parameter name; a megawidget may include a string
     * associated with this name. The string may be any string specified in the
     * array associated with {@link #MEGAWIDGET_UNIT_CHOICES}. If not specified,
     * it is assumed to be the smallest unit provided in said array.
     */
    public static final String MEGAWIDGET_CURRENT_UNIT_CHOICE = "currentUnit";

    /**
     * Megawidget state unit parameter name; a megawidget may include one of the
     * following strings associated with this name: "ms", "seconds", "minutes",
     * "hours", or "days". The specified unit is the one in which the state will
     * be specified when queried via a created megawidget's
     * {@link TimeDeltaMegawidget#getState(String)} or passed via callback to a
     * state change listener. If not specified, it is assumed to be "ms",
     * meaning the state value will always be given in milliseconds. If the unit
     * is anything other than "ms", then any remainders will be dropped when the
     * state value is converted to the specified unit; for example, if the state
     * is 1500 and the state unit is "seconds", the provided state will be 1.
     * Note that the time slice represented by the state unit must be less than
     * or equal to the time slice represented by the smallest unit provided in
     * the {@link #MEGAWIDGET_UNIT_CHOICES} list.
     */
    public static final String MEGAWIDGET_STATE_UNIT = "valueUnit";

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Flag indicating whether or not the megawidget is to expand to fill all
     * available horizontal space within its parent.
     */
    private final boolean horizontalExpander;

    /**
     * Flag indicating whether or not state changes that are part of a group of
     * rapid changes are to result in notifications to the listener.
     */
    private final boolean sendingEveryChange;

    /**
     * List of units to be used, in the order they are to be displayed in the
     * list of units available.
     */
    private final List<TimeDeltaUnit> units;

    /**
     * Unit to be used when megawidget is first created.
     */
    private final TimeDeltaUnit currentUnit;

    /**
     * Unit to be used for the state value.
     */
    private final TimeDeltaUnit stateUnit;

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
    public TimeDeltaSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new BoundedComparableValidator<Long>(parameters,
                MEGAWIDGET_MIN_VALUE, MEGAWIDGET_MAX_VALUE, Long.class, 0L,
                Long.MAX_VALUE));
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.FALSE);

        /*
         * Ensure that the rapid change notification flag, if provided, is
         * appropriate.
         */
        sendingEveryChange = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_SEND_EVERY_STATE_CHANGE),
                        MEGAWIDGET_SEND_EVERY_STATE_CHANGE, true);

        /*
         * Get the horizontal expansion flag if available.
         */
        horizontalExpander = ConversionUtilities
                .getSpecifierBooleanValueFromObject(getIdentifier(), getType(),
                        parameters.get(EXPAND_HORIZONTALLY),
                        EXPAND_HORIZONTALLY, false);

        /*
         * Ensure that the possible units are present as an array of strings.
         */
        List<?> choicesList = null;
        try {
            choicesList = (List<?>) parameters.get(MEGAWIDGET_UNIT_CHOICES);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_UNIT_CHOICES,
                    parameters.get(MEGAWIDGET_UNIT_CHOICES),
                    "must be list of unit choices");
        }
        if ((choicesList == null) || choicesList.isEmpty()) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_UNIT_CHOICES, null, null);
        }
        Set<TimeDeltaUnit> unitSet = new HashSet<>();
        for (int j = 0; j < choicesList.size(); j++) {
            TimeDeltaUnit unit = TimeDeltaUnit.get((String) choicesList.get(j));
            if (unit == null) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_UNIT_CHOICES + "[" + j + "]",
                        choicesList, "invalid unit choice");
            }
            unitSet.add(unit);
        }
        List<TimeDeltaUnit> units = new ArrayList<>(unitSet);
        Collections.sort(units);
        this.units = ImmutableList.copyOf(units);

        /*
         * Ensure that if a current unit choice was given, it is valid.
         */
        String value = null;
        try {
            value = (String) parameters.get(MEGAWIDGET_CURRENT_UNIT_CHOICE);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_CURRENT_UNIT_CHOICE,
                    parameters.get(MEGAWIDGET_CURRENT_UNIT_CHOICE),
                    "must be unit choice");
        }
        if (value == null) {
            currentUnit = units.get(0);
        } else {
            currentUnit = TimeDeltaUnit.get(value);
            if (currentUnit == null) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_CURRENT_UNIT_CHOICE, value,
                        "invalid unit choice");
            }
            if (units.contains(currentUnit) == false) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_CURRENT_UNIT_CHOICE, value,
                        "current unit choice must be found within "
                                + MEGAWIDGET_UNIT_CHOICES + " list");
            }
        }

        /*
         * Ensure that if a state unit was given, it is valid.
         */
        value = null;
        try {
            value = (String) parameters.get(MEGAWIDGET_STATE_UNIT);
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_STATE_UNIT,
                    parameters.get(MEGAWIDGET_STATE_UNIT),
                    "must be unit choice");
        }
        if (value == null) {
            stateUnit = TimeDeltaUnit.MILLISECOND;
        } else {
            stateUnit = TimeDeltaUnit.get(value);
            if (stateUnit == null) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_STATE_UNIT, value,
                        "invalid unit choice");
            }
        }

        /*
         * Ensure that the state unit is smaller than or equal in size to all
         * the possible units.
         */
        for (TimeDeltaUnit unit : units) {
            if (stateUnit.ordinal() > unit.ordinal()) {
                throw new MegawidgetSpecificationException(getIdentifier(),
                        getType(), MEGAWIDGET_STATE_UNIT, value,
                        "state unit must be no larger than any "
                                + "unit choices found within "
                                + MEGAWIDGET_UNIT_CHOICES + " list");
            }
        }
    }

    // Public Methods

    @Override
    public final boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public final int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public final boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public final int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public final boolean isSendingEveryChange() {
        return sendingEveryChange;
    }

    @Override
    public final boolean isHorizontalExpander() {
        return horizontalExpander;
    }

    /**
     * Get the list of units.
     * 
     * @return List of units; the list should be considered read-only by the
     *         caller.
     */
    public final List<TimeDeltaUnit> getUnits() {
        return units;
    }

    /**
     * Get the current unit.
     * 
     * @return current unit.
     */
    public final TimeDeltaUnit getCurrentUnit() {
        return currentUnit;
    }

    /**
     * Get the unit used for the state.
     * 
     * @return Unit used for the state.
     */
    public final TimeDeltaUnit getStateUnit() {
        return stateUnit;
    }
}
