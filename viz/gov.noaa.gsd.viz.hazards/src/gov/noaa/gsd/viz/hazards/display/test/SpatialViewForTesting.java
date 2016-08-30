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

import gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaContext;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Mock {@link ISpatialView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Dec 05, 2014 4124       Chris.Golden      Changed to work with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Feb 27, 2015 6000       Dan Schaffer      Improved centering behavior
 * Mar 16, 2016 15676      Chris.Golden      Changed to work with latest spatial view.
 * Mar 24, 2016 15676      Chris.Golden      Ditto.
 * Aug 23, 2016 19537      Chris.Golden      Ditto again.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class SpatialViewForTesting implements ISpatialView {

    @Override
    public void initialize(SpatialPresenter presenter,
            Set selectedSpatialEntityIdentifiers) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public List contributeToMainUI(Enum type) {
        return null;
    }

    @Override
    public void setSelectedTime(Date selectedTime) {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void loadSelectByAreaVizResourceAndInputHandler(
            SelectByAreaContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void centerAndZoomDisplay(List hull, Coordinate center) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh() {

        /*
         * No action.
         */
    }

    @Override
    public IStateChanger getSelectedSpatialEntityIdentifiersChanger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IListStateChanger getSpatialEntitiesChanger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getSelectByAreaInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStateChanger getToggleChanger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getCommandInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEditMultiPointGeometryEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getCreateShapeInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getModifyGeometryInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getSelectLocationInvoker() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICommandInvoker getGageActionInvoker() {
        throw new UnsupportedOperationException();
    }
}
