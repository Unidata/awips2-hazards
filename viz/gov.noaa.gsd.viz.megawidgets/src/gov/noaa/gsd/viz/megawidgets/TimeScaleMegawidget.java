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

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

/**
 * Time scale megawidget, providing the user the ability to select one or more
 * times using absolute date-time selectors.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Apr 30, 2013   1277     Chris.Golden      Added support for mutable properties.
 * Oct 18, 2013   2168     Chris.Golden      Fixed bug that caused notification
 *                                           of state change to be fired before
 *                                           construction of the megawidget was
 *                                           complete, and changed to implement
 *                                           new IControl interface.
 * Nov 05, 2013   2336     Chris.Golden      Added option to not notify listeners
 *                                           of state changes caused by ongoing
 *                                           thumb drags.
 * Dec 16, 2013   2545     Chris.Golden      Changed to use new DateTimeComponent
 *                                           objects instead of text fields for
 *                                           viewing/manipulating each state
 *                                           value above the scale widget.
 * Jan 31, 2014   2710     Chris.Golden      Added minimum interval parameter, to
 *                                           allow the minimum interval between
 *                                           adjacent state values to be configured.
 *                                           Also changed to only send notifications
 *                                           of scale-caused changes after all the
 *                                           values have been recorded in the mega-
 *                                           widget to avoid bugs caused by only
 *                                           one value of N being updated when the
 *                                           state of all values is checked.
 * Jan 31, 2014   2161     Chris.Golden      Added option to change editability of
 *                                           each state value individually. Also
 *                                           added ability to include detail mega-
 *                                           widgets for each state value. Also
 *                                           added custom strings to be displayed
 *                                           in place of date-time values for
 *                                           specified values.
 * Mar 08, 2014    2155    Chris.Golden      Fixed bugs with date-time fields in
 *                                           time megawidgets that caused unexpected
 *                                           date-times to be selected when the user
 *                                           manipulated the drop-down calendar.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed "isFullWidthOfColumn"
 *                                           property to "isFullWidthOfDetailPanel",
 *                                           and changed to disable detail children
 *                                           if it gets disabled.
 * Jun 24, 2014    4010    Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * Jun 25, 2014    4009    Chris.Golden      Fixed bug that caused descriptive text
 *                                           strings to be used for certain times as
 *                                           per specifier configuration to be
 *                                           ignored when the megawidget was created
 *                                           with one or more of those times as a
 *                                           current state.
 * Jul 01, 2014    3512    Chris.Golden      Made subclass of new base class
 *                                           MultiTimeMegawidget.
 * Jan 28, 2015    2331    Chris.Golden      Added mutable properties allowing the
 *                                           defining of valid boundaries for the
 *                                           values, with potentially a different
 *                                           boundary for each state identifier.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TimeScaleSpecifier
 */
public class TimeScaleMegawidget extends MultiTimeMegawidget {

    // Private Variables

    /**
     * Manager of the date-time components.
     */
    private final IDateTimeComponentHolder dateTimeManager = new IDateTimeComponentHolder() {
        @Override
        public long getCurrentTime() {
            return getCurrentTimeProvider().getCurrentTime();
        }

        @Override
        public long renderValueChangeAcceptable(String identifier, long value) {

            /*
             * Make the value valid for the scale widget.
             */
            value = convertToValueAcceptableToScale(identifier, value);

            /*
             * Ensure that the new value is not too close to or beyond a
             * neighboring thumb's value.
             */
            int index = ((TimeScaleSpecifier) getSpecifier())
                    .getIndicesForStateIdentifiers().get(identifier);
            if (isValueBetweenNeighboringValues(index, value)) {
                return value;
            } else {
                return -1L;
            }
        }

        @Override
        public void valueChanged(String identifier, long value,
                boolean rapidChange) {

            /*
             * If the value has changed and is acceptable to the scale widget,
             * use it as the new value; otherwise, set the value of the
             * date-time component back to what the scale had for this
             * identifier.
             */
            int index = ((TimeScaleSpecifier) getSpecifier())
                    .getIndicesForStateIdentifiers().get(identifier);
            if (setValueIfChanged(identifier, index, value)) {

                /*
                 * Notify listeners if appropriate.
                 */
                if ((isOnlySendEndStateChanges() == false)
                        || (rapidChange == false)) {
                    notifyListener(identifier, value);
                }
            }
        }
    };

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing any
     *             megawidgets acting as detail fields for the various states.
     */
    protected TimeScaleMegawidget(TimeScaleSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier, paramMap);
        createWidgetComponents(specifier, parent, paramMap,
                specifier.getMinimumInterval());
    }

    // Protected Methods

    @Override
    protected ITimeComponent createTimeComponent(int stateIndex,
            Composite parent, String text, int verticalIndent) {
        String identifier = ((StatefulMegawidgetSpecifier) getSpecifier())
                .getStateIdentifiers().get(stateIndex);
        DateTimeComponent dateTime = new DateTimeComponent(identifier, parent,
                text, (IControlSpecifier) getSpecifier(),
                getStateInternally(identifier), false, verticalIndent,
                isOnlySendEndStateChanges(), dateTimeManager);
        setDateTimeComponentValues(dateTime, getStateInternally(identifier));
        return dateTime;
    }

    @Override
    protected void synchronizeTimeComponentToState(String identifier) {
        setDateTimeComponentValues(
                (DateTimeComponent) getTimeComponent(identifier),
                getStateInternally(identifier));
    }

    @Override
    protected void unlockedScaleThumbChanged() {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Set the specified date-time component to show the specified state.
     * 
     * @param dateTime
     *            Date-time component to have its value set.
     * @param value
     *            New value of the date-time component.
     */
    private void setDateTimeComponentValues(DateTimeComponent dateTime,
            long value) {
        dateTime.setState(value);
        String descriptiveString = ((TimeScaleSpecifier) getSpecifier())
                .getStateDescriptiveText(value);
        if (descriptiveString != null) {
            dateTime.setNullText(descriptiveString);
            dateTime.setShowNullText(true);
        } else {
            dateTime.setShowNullText(false);
        }
    }
}