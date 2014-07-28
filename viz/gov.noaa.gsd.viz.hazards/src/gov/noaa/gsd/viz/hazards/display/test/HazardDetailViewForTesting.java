/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.Command;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.DisplayableEventIdentifier;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailView;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailViewDelegate;
import gov.noaa.gsd.viz.hazards.hazarddetail.IMetadataStateChanger;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;

import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Description: Mock {@link IHazardDetailView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov  Initial creation
 * Nov 14, 2013  1463      bryon.lawrence Updated to support hazard conflict
 *                                        detection.
 * May 18, 2014  2925      Chris.Golden   Substantially rewrote to work with the new HID.
 * Jun 25, 2014  4009      Chris.Golden   Changed to work with new initialize() signature.
 * Jun 30, 2014  3512      Chris.Golden   Changed to work with modified IStateChanger and
 *                                        ICommandInvoker.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardDetailViewForTesting implements
        IHazardDetailViewDelegate<Action, RCPMainUserInterfaceElement> {

    private final IStateChanger<String, Boolean> detailViewVisibilityChanger = new IStateChanger<String, Boolean>() {

        private boolean visible;

        @Override
        public void setEnabled(String identifier, boolean enable) {
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
        }

        @Override
        public Boolean getState(String identifier) {
            return visible;
        }

        @Override
        public void setState(String identifier, Boolean value) {
            visible = value;
        }

        @Override
        public void setStates(Map<String, Boolean> valuesForIdentifiers) {
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Boolean> handler) {
        }
    };

    private List<String> selectedEventIds;

    private final IChoiceStateChanger<String, String, String, HazardDetailPresenter.DisplayableEventIdentifier> visibleEventChanger = new IChoiceStateChanger<String, String, String, HazardDetailPresenter.DisplayableEventIdentifier>() {

        @Override
        public void setEditable(String identifier, boolean editable) {
        }

        @Override
        public String getState(String identifier) {
            return null;
        }

        @Override
        public void setState(String identifier, String value) {
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
        }

        @Override
        public void setEnabled(String identifier, boolean enable) {
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<DisplayableEventIdentifier> choiceDisplayables,
                String value) {
            selectedEventIds = choices;
        }
    };

    private final IChoiceStateChanger<String, String, String, String> categoryAndTypeChanger = new IChoiceStateChanger<String, String, String, String>() {

        @Override
        public void setEditable(String identifier, boolean editable) {
        }

        @Override
        public String getState(String identifier) {
            return null;
        }

        @Override
        public void setState(String identifier, String value) {
        }

        @Override
        public void setStates(Map<String, String> valuesForIdentifiers) {
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, String> handler) {
        }

        @Override
        public void setEnabled(String identifier, boolean enable) {
        }

        @Override
        public void setChoices(String identifier, List<String> choices,
                List<String> choiceDisplayables, String value) {
        }
    };

    private final IStateChanger<String, TimeRange> timeRangeChanger = new IStateChanger<String, TimeRange>() {

        @Override
        public void setEnabled(String identifier, boolean enable) {
        }

        @Override
        public void setEditable(String identifier, boolean editable) {
        }

        @Override
        public TimeRange getState(String identifier) {
            return null;
        }

        @Override
        public void setState(String identifier, TimeRange value) {
        }

        @Override
        public void setStates(Map<String, TimeRange> valuesForIdentifiers) {
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, TimeRange> handler) {
        }
    };

    private final IMetadataStateChanger metadataChanger = new IMetadataStateChanger() {

        @Override
        public void setEditable(String identifier, boolean editable) {
        }

        @Override
        public Serializable getState(String identifier) {
            return null;
        }

        @Override
        public void setState(String identifier, Serializable value) {
        }

        @Override
        public void setStates(Map<String, Serializable> valuesForIdentifiers) {
        }

        @Override
        public void setStateChangeHandler(
                IStateChangeHandler<String, Serializable> handler) {
        }

        @Override
        public void setEnabled(String identifier, boolean enable) {
        }

        @Override
        public void setMegawidgetSpecifierManager(String eventIdentifier,
                MegawidgetSpecifierManager specifierManager,
                Map<String, Serializable> metadataStates) {
        }
    };

    private final ICommandInvoker<HazardDetailPresenter.Command> buttonInvoker = new ICommandInvoker<HazardDetailPresenter.Command>() {

        @Override
        public void setEnabled(Command identifier, boolean enable) {
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<Command> handler) {
        }
    };

    @Override
    public void dispose() {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize(
            ImmutableList<String> hazardCategories,
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers) {
    }

    @Override
    public IStateChanger<String, Boolean> getDetailViewVisibilityChanger() {
        return detailViewVisibilityChanger;
    }

    @Override
    public IStateChanger<String, TimeRange> getVisibleTimeRangeChanger() {
        return timeRangeChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> getVisibleEventChanger() {
        return visibleEventChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getCategoryChanger() {
        return categoryAndTypeChanger;
    }

    @Override
    public IChoiceStateChanger<String, String, String, String> getTypeChanger() {
        return categoryAndTypeChanger;
    }

    @Override
    public IStateChanger<String, TimeRange> getTimeRangeChanger() {
        return timeRangeChanger;
    }

    @Override
    public IMetadataStateChanger getMetadataChanger() {
        return metadataChanger;
    }

    @Override
    public ICommandInvoker<HazardDetailPresenter.Command> getButtonInvoker() {
        return buttonInvoker;
    }

    public List<String> getSelectedEventIdentifiers() {
        return selectedEventIds;
    }
}
