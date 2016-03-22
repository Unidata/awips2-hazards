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

import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesDrawingAction;
import gov.noaa.gsd.viz.hazards.spatialdisplay.HazardServicesMouseHandlers;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialDisplay;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialView.SpatialViewCursorTypes;
import gov.noaa.gsd.viz.hazards.spatialdisplay.mousehandlers.MouseHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
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
    public void dispose() {
    }

    @Override
    public List contributeToMainUI(Enum type) {
        return null;
    }

    @Override
    public void initialize(SpatialPresenter presenter,
            MouseHandlerFactory mouseFactory) {
    }

    @Override
    public void setSettings(ObservedSettings settings) {
    }

    @Override
    public void redoTimeMatching() {
    }

    @Override
    public void issueRefresh() {
    }

    @Override
    public void setMouseHandler(HazardServicesMouseHandlers mouseHandler,
            String... args) {
    }

    @Override
    public void unregisterCurrentMouseHandler() {
    }

    @Override
    public void modifyShape(HazardServicesDrawingAction drawingAction) {
    }

    @Override
    public void manageViewFrames(Date selectedTime) {
    }

    @Override
    public void addGeometryDisplayResourceToPerspective() {
    }

    @Override
    public void setCursor(SpatialViewCursorTypes cursorType) {
    }

    @Override
    public void drawingActionComplete() {

    }

    @Override
    public SpatialDisplay getSpatialDisplay() {
        return null;
    }

    @Override
    public void loadGeometryOverlayForSelectedEvent() {
    }

    @Override
    public SelectByAreaDbMapResource getSelectableGeometryDisplay() {
        return null;
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

    @Override
    public void drawEvents(Collection events, Map eventOverlapSelectedTime,
            Map forModifyingStormTrack, Map eventEditability,
            boolean toggleAutoHazardChecking, boolean areHatchedAreasDisplayed) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView#recenterRezoomDisplay
     * (com.vividsolutions.jts.geom.Coordinate[],
     * com.vividsolutions.jts.geom.Coordinate)
     */
    @Override
    public void recenterRezoomDisplay(Coordinate[] hull, Coordinate center) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView#
     * setEditEventGeometryEnabled(java.lang.Boolean)
     */
    @Override
    public void setEditEventGeometryEnabled(Boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawSpatialEntities(List spatialEntities,
            Set selectedEventIdentifiers) {
        throw new UnsupportedOperationException();
    }
}
