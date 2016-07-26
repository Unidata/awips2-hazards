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
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class SpatialViewForTesting implements ISpatialView {

    @Override
    public void initialize(SpatialPresenter presenter) {
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
    public void setUndoEnabled(Boolean undoFlag) {
    }

    @Override
    public void setRedoEnabled(Boolean redoFlag) {
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView#
     * setEditEventGeometryEnabled(java.lang.Boolean)
     */
    @Override
    public void setEditMultiPointGeometryEnabled(Boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawSpatialEntities(List spatialEntities,
            Set selectedEventIdentifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadSelectByAreaVizResourceAndInputHandler(
            SelectByAreaContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAddNewGeometryToSelectedToggleState(boolean enable,
            boolean check) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void centerAndZoomDisplay(List hull, Coordinate center) {
        throw new UnsupportedOperationException();
    }
}
