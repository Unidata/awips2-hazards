/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryCollection;
import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;
import gov.noaa.gsd.common.utilities.geometry.GeometryWrapper;
import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.common.visuals.SpatialEntity;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SequencePosition;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter.SpatialEntityType;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.IDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.ManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.PathDrawable;
import gov.noaa.gsd.viz.hazards.spatialdisplay.drawables.VertexManipulationPoint;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.gsd.viz.hazards.spatialdisplay.input.BaseInputHandler;
import gov.noaa.gsd.viz.hazards.spatialdisplay.input.InputHandlerFactory;
import gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea.SelectByAreaDbMapResource;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IListStateChanger;
import gov.noaa.nws.ncep.ui.pgen.display.LinePatternManager;
import gov.noaa.nws.ncep.ui.pgen.elements.AbstractDrawableComponent;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Maps;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.GeometryType;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.AbstractTimeMatcher;
import com.raytheon.uf.viz.core.IDisplayPaneContainer;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.datastructure.PerspectiveSpecificProperties;
import com.raytheon.uf.viz.core.drawables.IDescriptor.FramesInfo;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.IResourceDataChanged;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory;
import com.raytheon.uf.viz.core.rsc.RenderingOrderFactory.ResourceOrder;
import com.raytheon.uf.viz.core.rsc.tools.AbstractMovableToolLayer;
import com.raytheon.uf.viz.d2d.core.D2DProperties;
import com.raytheon.uf.viz.d2d.core.time.D2DTimeMatcher;
import com.raytheon.viz.hydro.perspective.HydroPerspectiveManager;
import com.raytheon.viz.hydro.resource.MultiPointResource;
import com.raytheon.viz.hydrocommon.data.GageData;
import com.raytheon.viz.ui.EditorUtil;
import com.raytheon.viz.ui.VizWorkbenchManager;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;
import com.raytheon.viz.ui.cmenu.IContextMenuContributor;
import com.raytheon.viz.ui.editor.AbstractEditor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * CAVE viz resource used for the display of hazards and associated visuals.
 * This resource is managed by the {@link SpatialView}.
 * <p>
 * The spatial display receives notifications of changes to
 * {@link SpatialEntity} lists of different types, and responds by updating its
 * collections of PGEN drawables. The latter are then rendered to the display.
 * An exception is <code>SpatialEntity</code> instances that represent hatched
 * areas; these are displayed using the CAVE drawing API directly.
 * </p>
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * Jun 21, 2011            Xiangbao       Initial creation
 * Jul 21, 2012            Xiangbao       Added multiple events selection
 * May 29, 2013            Bryon.Lawrence Added code to handle multiple
 *                                        deselection of hazards.
 * Jun 04, 2013            Bryon.Lawrence Added support for events with multiple
 *                                        hazard polygons.
 * Jun 14, 2013            Bryon.Lawrence Modified the drawing of the polygon
 *                                        handlebars to use IGraphicsTarget.drawPoints
 *                                        to improve drawing performance.
 * Jun 24, 2013            Bryon.Lawrence Removed the 'Move Entire Element' option from
 *                                        from the right-click context menu.
 * Jul 10, 2013     585    Chris.Golden   Changed to support loading from bundle.
 * Jul 12, 2013            Bryon.Lawrence Added ability to draw persistent shapes.
 * Jul 18, 2013   1264     Chris.Golden   Added support for drawing lines and
 *                                        points.
 * Aug  9, 2013 1921       Dan Schaffer   Support of replacement of JSON with POJOs
 * Aug 22, 2013    1936    Chris.Golden   Added console countdown timers.
 * Aug 27, 2013 1921       Bryon.Lawrence Replaced code to support multi-hazard selection using
 *                                        Shift and Ctrl keys.
 * Sep 10, 2013  752       Bryon.Lawrence Modified to use static method 
 *                                        forModifyingStormTrack in HazardServicesDrawableBuilder
 * Nov  04, 2013 2182      Dan Schaffer   Started refactoring
 * Nov 15, 2013  2182      Dan Schaffer   Refactoring JSON - ProductStagingDialog
 * Nov  18, 2013 1462      Bryon.Lawrence Added hazard area hatching.
 * Nov  23, 2013 2474      Bryon.Lawrence Made fix to prevent NPE when using right
 *                                        click context menu.
 * Nov 29, 2013  2378      Bryon.Lawrence Changed to use hazard event modified flag instead of
 *                                        test for PENDING when testing whether or not to show
 *                                        End Selected Hazard context menu item.
 * Sep 09, 2014  3994      Robert.Blum    Added handleMouseEnter to reset the cursor type.
 * Oct 20, 2014  4780      Robert.Blum    Made fix to Time Matching to update to the Time Match Basis.
 * Nov 18, 2014  4124      Chris.Golden   Adapted to new time manager.
 * Dec 01, 2014  4188      Dan Schaffer   Now allowing hazards to be shrunk or expanded when appropriate.
 * Dec 05, 2014  4124      Chris.Golden   Changed to work with newly parameterized config manager
 *                                        and with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer   Spatial Display cleanup and other bug fixes
 * Feb 09, 2015 6260       Dan Schaffer   Fixed bugs in multi-polygon handling
 * Feb 10, 2015  3961      Chris.Cody     Add Context Menu (R-Click) for River Point (GageData) objects
 * Feb 12, 2015 4959       Dan Schaffer   Modify MB3 add/remove UGCs to match Warngen
 * Feb 15, 2015 2271       Dan Schaffer   Incur recommender/product generator init costs immediately
 * Feb 24, 2015 6499       Dan Schaffer   Disable moving/drawing of point hazards
 * Mar 13, 2015 6090       Dan Schaffer   Relaxed geometry validity check.
 * Mar 19, 2015 6938       mduff          Increased size of handlebars to 1.5 mag.
 * Apr 03, 2015 6815       mduff          Fix memory leak.
 * May 05, 2015 7624       mduff          Drawing Optimizations.
 * May 21, 2015 7730       Chris.Cody     Move Add/Delete Vertex to top of Context Menu
 * Jun 11, 2015 7921       Chris.Cody     Correctly render hazard events in D2D when Map Scale changes 
 * Jun 17, 2015 6730       Robert.Blum    Fixed invalid geometry (duplicate rings) bug caused by duplicate
 *                                        ADCs being drawn for one hazard.
 * Jun 22, 2015 7203       Chris.Cody     Prevent Event Text Data overlap in a single county
 * Jun 24, 2015 6601       Chris.Cody     Change Create by Hazard Type display text
 * Jul 07, 2015 7921       Chris.Cody     Re-scale hatching areas and handle bar points
 * Sep 18, 2015 9905       Chris.Cody     Correct Spatial Display selection error
 * Oct 26, 2015 12754      Chris.Golden   Fixed drawing bug that caused only one hazard to be drawn at a
 *                                        time, regardless of how many should have been displayed.
 * Nov 10, 2015 12762      Chris.Golden   Added support for use of new recommender manager.
 * Mar 16, 2016 15676      Chris.Golden   Changed to make visual features work. Will be refactored to
 *                                        remove numerous existing kludges.
 * Mar 24, 2016 15676      Chris.Golden   Numerous bug fixes for handlebar points with respect to multiple
 *                                        selection, selection itself of non-editable/movable shapes,
 *                                        visual feature stuff, etc. For the most part, what worked prior
 *                                        to visual features should now work again, with some enhancements
 *                                        in terms of better handlebar point usage and selectability of
 *                                        immutable shapes.
 * Mar 26, 2016 15676      Chris.Golden   Added method to check Geometry objects' validity, and method
 *                                        to allow the display to be notified of changes made by the user
 *                                        to visual features. Also fixed selection-tracking bug causing
 *                                        handlebars to appear around deselected hazard event geometries.
 * Apr 05, 2016 15676      Chris.Golden   Made finding all geometries "containing" a point more relaxed
 *                                        about what containment entails; there is now slop distance
 *                                        when looking for a point in a polygon, so that if the point
 *                                        lies slightly outside of the polygon, it still "contains" it.
 *                                        This makes editing polygons much easier.
 * Jun 23, 2016 19537      Chris.Golden   Removed storm-track-specific code. Also added hatching for
 *                                        hazard events that have visual features, and added ability to
 *                                        use visual features to request spatial info for recommenders.
 * Jul 25, 2016 19537      Chris.Golden   Extensively refactored as the move toward MVP compliance
 *                                        continues. Added Javadoc comments, continued separation of
 *                                        concerns between view, presenter, display, and mouse handlers.
 * Aug 23, 2016 19537      Chris.Golden   Continued extensive spatial display refactor; moved drawable
 *                                        code into new DrawableManager class, added code to speed up
 *                                        performance when in the midst of a display zoom, and replaced
 *                                        the brute-force "recreate all drawables each time the display
 *                                        is refreshed" approach with one that only recreates drawables
 *                                        that represent spatial entities that have changed. Also
 *                                        added code to cancel edits when a drawable is updated by the
 *                                        system while the user is changing it.
 * Sep 12, 2016 15934      Chris.Golden   Changed to work with advanced geometries.
 * Sep 21, 2016 15934      Chris.Golden   Replaced MultiPointDrawable with new PathDrawable, and
 *                                        added support for ellipse drawing. Also added a new
 *                                        buildModifiedGeometry() method that takes advanced geometry
 *                                        instead of a list of points.
 * Sep 29, 2016 15928      Chris.Golden   Added ability to use correct cursor for different
 *                                        manipulation points. Changed to have handlebars only show up
 *                                        for active (selected) spatial entities, and to have unselected
 *                                        ones glow subtly when the mouse passes over them. Also changed
 *                                        to disallow editing of unselected ones.
 * Oct 05, 2016 22870      Chris.Golden   Fixed bug in getDataTimes() that caused an index-out-of-bounds
 *                                        exception if H.S. was the Time Match Basis and the number of
 *                                        frames were increased when there were no data times previously
 *                                        recorded. Also fixed bug in same method that did not decrease
 *                                        the number of data times (and thus frames) when H.S. was the
 *                                        Time Match Basis and the number of frames was decreased by the
 *                                        user.
 * </pre>
 * 
 * @author Xiangbao Jing
 */
public class SpatialDisplay extends AbstractMovableToolLayer<Object> implements
        IContextMenuContributor, IResourceDataChanged {

    // Public Static Constants

    /**
     * Distance in pixels of "slop" used when testing for hits on clickable
     * elements, etc.
     */
    public static final int SLOP_DISTANCE_PIXELS = 15;

    // Public Enumerated Types

    /**
     * Types of cursors.
     */
    public static enum CursorType {

        MOVE_SHAPE_CURSOR(SWT.CURSOR_SIZEALL), MOVE_VERTEX_CURSOR(
                SWT.CURSOR_HAND), ROTATE_CURSOR(SWT.CURSOR_CROSS), ARROW_CURSOR(
                SWT.CURSOR_ARROW), DRAW_CURSOR(SWT.CURSOR_CROSS), WAIT_CURSOR(
                SWT.CURSOR_WAIT), SCALE_DOWN_AND_RIGHT(SWT.CURSOR_SIZESE), SCALE_RIGHT(
                SWT.CURSOR_SIZEE), SCALE_UP_AND_RIGHT(SWT.CURSOR_SIZENE), SCALE_UP(
                SWT.CURSOR_SIZEN), SCALE_UP_AND_LEFT(SWT.CURSOR_SIZENW), SCALE_LEFT(
                SWT.CURSOR_SIZEW), SCALE_DOWN_AND_LEFT(SWT.CURSOR_SIZESW), SCALE_DOWN(
                SWT.CURSOR_SIZES);

        // Private Variables

        /**
         * SWT cursor type that goes with this cursor.
         */
        private final int swtType;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param swtType
         *            SWT type of cursor.
         */
        private CursorType(int swtType) {
            this.swtType = swtType;
        }

        // Public Methods

        /**
         * Get the SWT cursor type.
         * 
         * @return SWT cursor type.
         */
        public int getSwtType() {
            return swtType;
        }
    };

    // Private Classes

    /**
     * Action used to process the selection of {@link MultiPointResource} gage
     * data in a Hydro perspective.
     */
    private class RiverGageAction extends AbstractRightClickAction {

        // Public Methods

        @Override
        public String getText() {
            return "Create Hazard";
        }

        @Override
        public boolean isHidden() {
            IWorkbenchWindow window = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            IPerspectiveDescriptor perspectiveDescriptor = page
                    .getPerspective();
            if (perspectiveDescriptor != null) {
                String perspectiveId = perspectiveDescriptor.getId();
                if ((perspectiveId != null)
                        && (perspectiveId
                                .equals(HydroPerspectiveManager.HYDRO_PERSPECTIVE))) {
                    return false;
                }
            }
            return (getSelectedGageIdentifier() == null);
        }

        @Override
        public void run() {
            spatialView
                    .handleUserInvocationOfGageAction(getSelectedGageIdentifier());
        }

        // Private Methods

        /**
         * Find the first multi-point resource in the descriptor's resource list
         * and get the selected gage identifier from it.
         * 
         * @return Selected gage identifier, or <code>null</code> if no
         *         multi-point resource is loaded, or if no gage is selected.
         */
        private String getSelectedGageIdentifier() {
            for (ResourcePair pair : descriptor.getResourceList()) {
                if (pair.getResource() instanceof MultiPointResource) {
                    MultiPointResource multiPointResource = (MultiPointResource) pair
                            .getResource();
                    GageData gageData = multiPointResource.getSelectedGage();
                    if (gageData != null) {
                        return gageData.getLid();
                    }
                }
            }
            return null;
        }
    }

    // Package-Private Static Constants

    /**
     * Layer name.
     */
    static final String LAYER_NAME = "Hazard Services";

    // Private Static Constants

    /**
     * Distance tolerance used for topology preserving simplification of paths
     * and polygons.
     */
    private static final double SIMPLIFIER_DISTANCE_TOLERANCE = 0.0001;

    /**
     * Default map scale name. Does not match any valid scale name. This is by
     * design. This is necessary for adjusting to map scale (not zoom) changes
     * for the D2D Perspective.
     */
    private static final String DEFAULT_MAP_SCALE_NAME = "OTHER";

    /**
     * Small zoom level change factor to force redraw. The intent is for the
     * drawing methods to recognize a change, but have a change small enough not
     * to alter the rendered image.
     */
    private static final float IOTA_ZOOM_LEVEL_FACTOR_DELTA = 0.00001f;

    /**
     * Data time increment in milliseconds.
     */
    private static final int DATA_TIME_INCREMENT_MILLIS = 15 * 60 * 1000;

    // Private Static Variables

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SpatialDisplay.class);

    // Private Variables

    /**
     * Map associating spatial entity types with the lists of spatial entities
     * of those types.
     */
    private final Map<SpatialEntityType, List<SpatialEntity<? extends IEntityIdentifier>>> spatialEntitiesForTypes = new EnumMap<>(
            SpatialEntityType.class);

    /**
     * Map associating spatial entity types with read-only views of the lists of
     * spatial entities of those types, that is, unmodifiable views of the lists
     * contained as values by {@link #spatialEntitiesForTypes}.
     */
    private final Map<SpatialEntityType, List<SpatialEntity<? extends IEntityIdentifier>>> readOnlySpatialEntitiesForTypes = new EnumMap<>(
            SpatialEntityType.class);

    /**
     * Spatial entity list state changer.
     */
    private final IListStateChanger<SpatialEntityType, SpatialEntity<? extends IEntityIdentifier>> spatialEntitiesChanger = new IListStateChanger<SpatialEntityType, SpatialEntity<? extends IEntityIdentifier>>() {

        @Override
        public void setEnabled(SpatialEntityType identifier, boolean enable) {
            throw new UnsupportedOperationException(
                    "cannot change enabled state of spatial entity lists");
        }

        @Override
        public void setEditable(SpatialEntityType identifier, boolean editable) {
            throw new UnsupportedOperationException(
                    "cannot change editable state of spatial entity lists");
        }

        @Override
        public List<SpatialEntity<? extends IEntityIdentifier>> get(
                SpatialEntityType identifier) {
            return readOnlySpatialEntitiesForTypes.get(identifier);
        }

        @Override
        public void clear(SpatialEntityType identifier) {

            /*
             * Clear the appropriate list, and repopulate the active entity
             * identifiers set if necessary.
             */
            List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities = spatialEntitiesForTypes
                    .get(identifier);
            List<SpatialEntity<? extends IEntityIdentifier>> removedEntities = new ArrayList<>(
                    spatialEntities);
            spatialEntities.clear();
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Remove any drawables associated with the removed spatial
             * entities.
             */
            if (drawableManager
                    .removeDrawablesForSpatialEntities(removedEntities)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void set(
                SpatialEntityType identifier,
                List<? extends SpatialEntity<? extends IEntityIdentifier>> elements) {

            /*
             * Clear the appropriate list and set its contents to be those
             * specified, and repopulate the active entity identifiers set if
             * necessary.
             */
            List<SpatialEntity<? extends IEntityIdentifier>> spatialEntities = spatialEntitiesForTypes
                    .get(identifier);
            List<SpatialEntity<? extends IEntityIdentifier>> removedEntities = new ArrayList<>(
                    spatialEntities);
            spatialEntities.clear();
            spatialEntities.addAll(elements);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Replace any drawables associated with the removed spatial
             * entities with new ones now occupying the list.
             */
            if (drawableManager.replaceDrawablesForSpatialEntities(
                    removedEntities, elements)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void addElement(SpatialEntityType identifier,
                SpatialEntity<? extends IEntityIdentifier> element) {

            /*
             * Append the specified spatial entity to the appropriate list, and
             * repopulate the active entity identifiers set if necessary.
             */
            spatialEntitiesForTypes.get(identifier).add(element);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Create any drawables needed for the new spatial entity.
             */
            drawableManager.addDrawablesForSpatialEntity(element);
        }

        @Override
        public void addElements(
                SpatialEntityType identifier,
                List<? extends SpatialEntity<? extends IEntityIdentifier>> elements) {

            /*
             * Append the specified spatial entities to the appropriate list,
             * and repopulate the active entity identifiers set if necessary.
             */
            spatialEntitiesForTypes.get(identifier).addAll(elements);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Create any drawables needed for the new spatial entities.
             */
            drawableManager.addDrawablesForSpatialEntities(elements);
        }

        @Override
        public void insertElement(SpatialEntityType identifier, int index,
                SpatialEntity<? extends IEntityIdentifier> element) {

            /*
             * Insert the specified spatial entity into the appropriate list,
             * and repopulate the active entity identifiers set if necessary.
             */
            spatialEntitiesForTypes.get(identifier).add(index, element);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Create any drawables needed for the new spatial entity.
             */
            drawableManager.addDrawablesForSpatialEntity(element);
        }

        @Override
        public void insertElements(
                SpatialEntityType identifier,
                int index,
                List<? extends SpatialEntity<? extends IEntityIdentifier>> elements) {

            /*
             * Insert the specified spatial entities into the appropriate list,
             * and repopulate the active entity identifiers set if necessary.
             */
            insertSpatialEntities(identifier, index, elements);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Create any drawables needed for the new spatial entities.
             */
            drawableManager.addDrawablesForSpatialEntities(elements);
        }

        @Override
        public void replaceElement(SpatialEntityType identifier, int index,
                SpatialEntity<? extends IEntityIdentifier> element) {

            /*
             * Remove the specified spatial entity from the appropriate list and
             * replace it with the specified new spatial entity, then repopulate
             * the active entity identifiers set if necessary.
             */
            SpatialEntity<? extends IEntityIdentifier> removedEntity = spatialEntitiesForTypes
                    .get(identifier).set(index, element);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Replace any drawables associated with the removed spatial entity
             * with new ones for the inserted spatial entity.
             */
            if (drawableManager.replaceDrawablesForSpatialEntity(removedEntity,
                    element)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void replaceElements(
                SpatialEntityType identifier,
                int index,
                int count,
                List<? extends SpatialEntity<? extends IEntityIdentifier>> elements) {

            /*
             * Remove the specified spatial entities from the appropriate list
             * and replace them with the specified new spatial entities, then
             * repopulate the active entity identifiers set if necessary.
             */
            List<SpatialEntity<? extends IEntityIdentifier>> removedEntities = new ArrayList<>(
                    spatialEntitiesForTypes.get(identifier).subList(index,
                            index + count));
            removeSpatialEntities(identifier, index, count);
            insertSpatialEntities(identifier, index, elements);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Replace any drawables associated with the removed spatial
             * entities with new ones for the inserted spatial entities.
             */
            if (drawableManager.replaceDrawablesForSpatialEntities(
                    removedEntities, elements)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void removeElement(SpatialEntityType identifier, int index) {

            /*
             * Remove the specified spatial entity from the appropriate list and
             * repopulate the active entity identifiers set if necessary.
             */
            SpatialEntity<? extends IEntityIdentifier> removedEntity = spatialEntitiesForTypes
                    .get(identifier).remove(index);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Remove any drawables associated with the removed spatial entity.
             */
            if (drawableManager.removeDrawablesForSpatialEntity(removedEntity)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void removeElements(SpatialEntityType identifier, int index,
                int count) {

            /*
             * Remove the specified spatial entities from the appropriate list
             * and repopulate the active entity identifiers set if necessary.
             */
            List<SpatialEntity<? extends IEntityIdentifier>> removedEntities = new ArrayList<>(
                    spatialEntitiesForTypes.get(identifier).subList(index,
                            index + count));
            removeSpatialEntities(identifier, index, count);
            repopulateActiveSpatialEntityIdentifiers(identifier);

            /*
             * Remove any drawables associated with the removed spatial
             * entities.
             */
            if (drawableManager
                    .removeDrawablesForSpatialEntities(removedEntities)) {
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        }

        @Override
        public void setListStateChangeHandler(
                IListStateChangeHandler<SpatialEntityType, SpatialEntity<? extends IEntityIdentifier>> handler) {
            throw new UnsupportedOperationException(
                    "cannot set spatial entity lists state change handler");
        }
    };

    /**
     * Currently active spatial entity identifiers. Active entities are those
     * that are either selected or are tool-based entities, and thus should
     * indicate they are movable, editable, etc. via visual cues when moused
     * over, and should also allow moving, editing, etc. if the user clicks and
     * drags them.
     */
    private final Set<IEntityIdentifier> activeSpatialEntityIdentifiers = new HashSet<>();

    /**
     * Unmodifiable view of the currently active spatial entity identifiers
     * found in {@link #activeSpatialEntityIdentifiers}.
     */
    private final Set<IEntityIdentifier> readOnlyActiveSpatialEntityIdentifiers = Collections
            .unmodifiableSet(activeSpatialEntityIdentifiers);

    /**
     * Drawable manager assisting this spatial display.
     */
    private final DrawableManager drawableManager;

    /**
     * Flag indicating whether or not this viz resource is acting as the basis
     * for time matching. This is declared <code>volatile</code> since the
     * method accessing it cannot be guaranteed to be running only within a
     * single thread during the lifetime of this object.
     */
    private volatile boolean timeMatchBasis;

    /**
     * Flag indicating whether or not this viz resource was previously acting as
     * the basis for time matching. This is declared <code>volatile</code> since
     * the method accessing it cannot be guaranteed to be running only within a
     * single thread during the lifetime of this object.
     */
    private volatile boolean prevTimeMatchBasis;

    /**
     * Current frame count. This is declared <code>volatile</code> since the
     * method accessing it cannot be guaranteed to be running only within a
     * single thread during the lifetime of this object.
     */
    private volatile int frameCount = -1;

    /**
     * Map of cursor types to the corresponding cursors.
     */
    private final Map<CursorType, Cursor> cursorsForCursorTypes = Maps
            .newEnumMap(CursorType.class);

    /**
     * Input handler factory.
     */
    private final InputHandlerFactory inputHandlerFactory;

    /**
     * Input handler. The strategy pattern is used to control the input (mouse
     * and keyboard) behavior in this viz resource.
     */
    private BaseInputHandler inputHandler = null;

    /**
     * Flag indicating whether or not visual cues indicating reactive and active
     * drawables should be updated at the next display refresh.
     */
    private boolean visualCuesNeedUpdating;

    /**
     * Spatial view that manages this object.
     */
    private SpatialView spatialView;

    /**
     * Flag indicating whether or not the perspective is currently changing.
     * This is declared <code>volatile</code> since the methods accessing it
     * cannot be guaranteed to be running only within a single thread during the
     * lifetime of this object.
     */
    private volatile boolean perspectiveChanging;

    /**
     * Current map scale factor, necessary for adjusting to map scale (not zoom)
     * changes.
     */
    double mapScaleFactor = 0.0;

    /**
     * Track Current Map Scale Name. This is necessary for adjusting to Map
     * Scale (not zoom) changes for D2D Perspective.
     */
    private String mapScale = DEFAULT_MAP_SCALE_NAME;

    /**
     * Current map zoom Level. Changing paint properties zoom level will force a
     * redraw of spatial display components.
     */
    private float zoomLevel = 1.0f;

    /**
     * River gage menu item.
     */
    private RiverGageAction riverGageAction;

    /**
     * Pixel X coordinate of location from which right-click context menu was
     * last deployed.
     */
    private int contextMenuX;

    /**
     * Pixel Y coordinate of location from which right-click context menu was
     * last deployed.
     */
    private int contextMenuY;

    // Public Methods

    /**
     * Construct a standard instance.
     * 
     * @param resourceData
     *            Resource data for the display.
     * @param loadProperties
     *            Properties describing this resource.
     */
    public SpatialDisplay(final SpatialDisplayResourceData resourceData,
            LoadProperties loadProperties) {

        super(resourceData, loadProperties);
        spatialEntitiesForTypes
                .put(SpatialEntityType.HATCHING,
                        new ArrayList<SpatialEntity<? extends IEntityIdentifier>>(
                                SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                                        .get(SpatialEntityType.HATCHING)));
        spatialEntitiesForTypes
                .put(SpatialEntityType.UNSELECTED,
                        new ArrayList<SpatialEntity<? extends IEntityIdentifier>>(
                                SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                                        .get(SpatialEntityType.UNSELECTED)));
        spatialEntitiesForTypes
                .put(SpatialEntityType.SELECTED,
                        new ArrayList<SpatialEntity<? extends IEntityIdentifier>>(
                                SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                                        .get(SpatialEntityType.SELECTED)));
        spatialEntitiesForTypes
                .put(SpatialEntityType.TOOL,
                        new ArrayList<SpatialEntity<? extends IEntityIdentifier>>(
                                SpatialPresenter.INITIAL_SIZES_FOR_SPATIAL_ENTITIES_LISTS
                                        .get(SpatialEntityType.TOOL)));
        for (Map.Entry<SpatialEntityType, List<SpatialEntity<? extends IEntityIdentifier>>> entry : spatialEntitiesForTypes
                .entrySet()) {
            readOnlySpatialEntitiesForTypes.put(entry.getKey(),
                    Collections.unmodifiableList(entry.getValue()));
        }

        /*
         * Test if the developer/user wants to set the CAVE clock to the canned
         * data time. This is really for developers to facilitate their testing
         * during development. This will be removed for IOC.
         */
        String useCannedTime = System.getenv("HAZARD_SERVICES_USE_CANNED_TIME");
        if (useCannedTime != null && useCannedTime.equalsIgnoreCase("true")) {
            Date date = new Date();
            date.setTime(Long.parseLong(HazardServicesAppBuilder.CANNED_TIME));
            Calendar simulatedDate = TimeUtil.newCalendar(TimeZone
                    .getTimeZone("UTC"));
            simulatedDate.setTime(date);
            // SimulatedTime.getSystemTime().setFrozen(true);
            SimulatedTime.getSystemTime().setFrozen(false);
            SimulatedTime.getSystemTime().setTime(simulatedDate.getTime());
        }

        /*
         * Create objects necessary for drawing and time-tracking.
         */
        drawableManager = new DrawableManager(this);
        dataTimes = new ArrayList<>();

        /*
         * Create the input handler factory.
         */
        inputHandlerFactory = new InputHandlerFactory(this);

        /*
         * This resource may be instantiated from within the UI thread, or from
         * another thread (for example, a non-UI thread is used for creating the
         * tool layer as part of a bundle load). Ensure that the rest of the
         * initialization happens on the UI thread. Note that if instantiated as
         * part of a bundle load, this instance will be created before the rest
         * of Hazard Services, whereas otherwise it is created
         */
        Runnable initializationFinisher = new Runnable() {

            @Override
            public void run() {

                /*
                 * Let the resource data finish its construction.
                 */
                resourceData.finishConstruction(SpatialDisplay.this);

                /*
                 * Create the cursors that will be used on the display.
                 */
                Display display = Display.getDefault();
                for (CursorType cursor : CursorType.values()) {
                    cursorsForCursorTypes.put(cursor,
                            display.getSystemCursor(cursor.getSwtType()));
                }

                /*
                 * Set the default input handler.
                 */
                setCurrentInputHandlerToNonDrawing(InputHandlerType.SINGLE_SELECTION);
            }
        };
        if (Display.getDefault().getThread() == Thread.currentThread()) {
            initializationFinisher.run();
        } else {
            Display.getDefault().asyncExec(initializationFinisher);
        }
    }

    // Public Methods

    @Override
    public String getName() {
        return LAYER_NAME;
    }

    @Override
    public void addContextMenuItems(IMenuManager menuManager, int x, int y) {

        ensureExecutingThreadIsMainUiThread();

        /*
         * Remember the point at which the menu was deployed.
         */
        contextMenuX = x;
        contextMenuY = y;

        /*
         * Cancel any ongoing input mode operation.
         */
        spatialView.handleUserResetOfInputMode();

        /*
         * Get the spatial entity under the point.
         */
        IEntityIdentifier identifier = getSpatialEntityAtPoint(x, y);

        /*
         * Create the list of actions to be turned into menu items. Any null
         * that is added to the list is treated as a separator when making menu
         * items.
         */
        List<IAction> actions = new ArrayList<>();

        /*
         * Determine whether the point clicked is over a vertex of a multi-point
         * editable drawable, and if not, whether it is over a multi-point
         * editable drawable's border. If the former is true, deleting the
         * vertex is a possibility; if the latter, adding a vertex is possible.
         * In either of those cases, add a menu item.
         */
        MutableDrawableInfo info = getMutableDrawableInfoUnderPoint(x, y, true);
        if (info.getManipulationPoint() instanceof VertexManipulationPoint) {

            /*
             * Ensure there are at least three points left for paths, or five
             * points for polygons (since the latter need to have the last point
             * be identical to the first point, so they need four points at a
             * minimum). If there are, then add the delete vertex menu item.
             */
            if (info.getDrawable().getPoints().size() > (isPolygon(info
                    .getDrawable()) ? 4 : 2)) {
                actions.add(new Action(
                        HazardConstants.CONTEXT_MENU_DELETE_VERTEX) {
                    @Override
                    public void run() {
                        deleteVertex(contextMenuX, contextMenuY);
                    }
                });
            }
        } else if (info.getEdgeIndex() > -1) {
            actions.add(new Action(HazardConstants.CONTEXT_MENU_ADD_VERTEX) {
                @Override
                public void run() {
                    addVertex(contextMenuX, contextMenuY);
                }
            });
        }

        /*
         * Get the rest of the context menu items.
         */
        actions.addAll(spatialView.getContextMenuActions(identifier));

        /*
         * Add the river gage action if appropriate.
         */
        if (riverGageAction == null) {
            riverGageAction = new RiverGageAction();
        }
        if (riverGageAction.isHidden() == false) {
            actions.add(riverGageAction);
        }
        actions.add(null);

        /*
         * For each action, create a menu item; null placeholders result in the
         * creation of separators.
         */
        for (IAction action : actions) {
            if (action == null) {
                menuManager.add(new Separator());
            } else {
                menuManager.add(action);
            }
        }
    }

    @Override
    public boolean handleMouseDown(int x, int y, int mouseButton) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseDown(x, y, mouseButton);
        }
        return false;
    }

    @Override
    public boolean handleMouseDownMove(int x, int y, int button) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseDownMove(x, y, button);
        }
        return false;
    }

    @Override
    public boolean handleMouseUp(int x, int y, int mouseButton) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseUp(x, y, mouseButton);
        }
        return false;
    }

    @Override
    public boolean handleMouseMove(int x, int y) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseMove(x, y);
        }
        return false;
    }

    @Override
    public boolean handleKeyDown(int key) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleKeyDown(key);
        }
        return false;
    }

    @Override
    public boolean handleKeyUp(int key) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleKeyUp(key);
        }
        return false;
    }

    @Override
    public boolean handleMouseEnter(Event event) {
        ensureExecutingThreadIsMainUiThread();
        if ((inputHandler != null) && isEditable()) {
            return inputHandler.handleMouseEnter(event);
        }
        return handleMouseMove(event.x, event.y);
    }

    @Override
    public DataTime[] getDataTimes() {

        /*
         * Save off previous state
         */
        prevTimeMatchBasis = timeMatchBasis;

        /*
         * Determine if this resource is the time match basis.
         */
        AbstractTimeMatcher time = descriptor.getTimeMatcher();
        if (time instanceof D2DTimeMatcher) {
            if (((D2DTimeMatcher) time).getTimeMatchBasis() == this) {
                timeMatchBasis = true;
            } else {
                timeMatchBasis = false;
            }
        }

        DataTime currentTime = null;
        FramesInfo info = descriptor.getFramesInfo();
        if (timeMatchBasis) {

            /*
             * Calculate more data times if the user has selected more frames
             * than there have been in the past; if instead the user has
             * selected less frames, chop off the earliest frames from the old
             * frames.
             */
            if (descriptor.getNumberOfFrames() > frameCount) {
                int variance = descriptor.getNumberOfFrames() - frameCount;

                frameCount = descriptor.getNumberOfFrames();

                DataTime earliestTime = (dataTimes.isEmpty() ? new DataTime(
                        SimulatedTime.getSystemTime().getTime()) : dataTimes
                        .get(0));
                fillDataTimeArray(earliestTime, variance);
            } else if (descriptor.getNumberOfFrames() < frameCount) {
                frameCount = descriptor.getNumberOfFrames();

                while (dataTimes.size() > frameCount) {
                    dataTimes.remove(0);
                }
            }

            /*
             * Just switched Time Match Basis update frames.
             */
            if (prevTimeMatchBasis == false && timeMatchBasis == true) {
                dataTimes.clear();
                currentTime = new DataTime(SimulatedTime.getSystemTime()
                        .getTime());

                dataTimes.add(currentTime);
                fillDataTimeArray(currentTime,
                        descriptor.getNumberOfFrames() - 1);
            }
        } else {
            dataTimes.clear();

            /*
             * First time called.
             */
            if (info.getFrameTimes() != null) {
                for (DataTime dt : info.getFrameTimes()) {
                    dataTimes.add(dt);
                }
                descriptor.getTimeMatchingMap().put(this, info.getFrameTimes());
            }
            if (dataTimes.size() == 0) {

                /*
                 * Case where this tool is time match basis or no data loaded.
                 */
                if (dataTimes.size() > 0) {
                    currentTime = dataTimes.get(dataTimes.size() - 1);
                } else {
                    currentTime = new DataTime(SimulatedTime.getSystemTime()
                            .getTime());
                }

                dataTimes.add(currentTime);
                fillDataTimeArray(currentTime,
                        descriptor.getNumberOfFrames() - 1);
            }
        }
        Collections.sort(dataTimes);
        return dataTimes.toArray(new DataTime[dataTimes.size()]);
    }

    @Override
    public ResourceOrder getResourceOrder() {
        return RenderingOrderFactory.ResourceOrder.HIGHEST;
    }

    /**
     * Receive notification that the perspective is changing.
     */
    public void perspectiveChanging() {
        perspectiveChanging = true;
    }

    /**
     * Handle an attempt by the user to select or deselect a drawable on the
     * spatial display.
     * 
     * @param drawable
     *            Drawable that was clicked.
     * @param multipleSelection
     *            Indicates whether or not this is a part of a multiple
     *            selection action.
     */
    public void handleUserSingleDrawableSelection(
            AbstractDrawableComponent drawable, boolean multipleSelection) {
        if (drawable instanceof IDrawable) {
            spatialView.handleUserSingleSpatialEntitySelection(
                    ((IDrawable<?>) drawable).getIdentifier(),
                    multipleSelection);
        }
    }

    /**
     * Handle an attempt by the user to set the selection set to include the
     * specified drawables on the spatial display.
     * 
     * @param drawables
     *            Drawables that the user is attempting to make the selection
     *            set.
     */
    public void handleUserMultipleDrawablesSelection(
            Set<AbstractDrawableComponent> drawables) {
        Set<IEntityIdentifier> identifiers = new HashSet<>(drawables.size(),
                1.0f);
        for (AbstractDrawableComponent drawable : drawables) {
            if (drawable instanceof IDrawable) {
                identifiers.add(((IDrawable<?>) drawable).getIdentifier());
            }
        }
        spatialView.handleUserMultipleSpatialEntitiesSelection(identifiers);
    }

    /**
     * Handle user modification of the geometry of the specified drawable.
     * 
     * @param drawable
     *            Drawable to be modified.
     * @param geometry
     *            New geometry to be used by the drawable.
     */
    public void handleUserModificationOfDrawable(
            AbstractDrawableComponent drawable, IAdvancedGeometry geometry) {
        if (drawable instanceof IDrawable) {
            spatialView.handleUserModificationOfSpatialEntity(
                    ((IDrawable<?>) drawable).getIdentifier(), geometry);
        }
        visualCuesNeedUpdatingAtNextRefresh();
        issueRefresh();
    }

    /**
     * Handle the selection of a location on the display.
     * 
     * @param location
     *            Location selected.
     */
    public void handleUserSelectionOfLocation(Coordinate location) {
        spatialView.handleUserLocationSelection(location);
    }

    /**
     * Handle the user creation of a point shape.
     * <p>
     * TODO: When the deprecated method
     * {@link #handleUserMultiPointDrawingActionComplete(GeometryType, List)} is
     * removed, the <code>location</code> parameter should be turned into an
     * {@link IAdvancedGeometry} for consistency's sake.
     * </p>
     * 
     * @param location
     *            Location at which the point is to be created.
     * @param sequencePosition
     *            Position in the point-creation sequence that this creation
     *            occupies. If {@link SequencePosition#LAST}, the input mode
     *            will be reset as well.
     */
    public void handleUserCreationOfPointShape(Coordinate location,
            SequencePosition sequencePosition) {
        spatialView.handleUserCreationOfShape(AdvancedGeometryUtilities
                .createGeometryWrapper(AdvancedGeometryUtilities
                        .getGeometryFactory().createPoint(location), 0));

        /*
         * If this is the first point drawn in a sequence of points, tell the
         * view that until further notice, subsequent created shapes should be
         * added to the selected set. If it is the last point drawn in such a
         * sequence, reset the input mode, which will as part of its routine
         * tell the view that no more created events should be added to the
         * selected set.
         */
        if (sequencePosition == SequencePosition.FIRST) {
            spatialView.handleSetAddCreatedEventsToSelected(true);
        } else if (sequencePosition == SequencePosition.LAST) {
            spatialView.handleUserResetOfInputMode();
        }
    }

    /**
     * Handle the user ending a sequence of point shape creation.
     */
    public void handleUserEndSequenceOfPointShapeCreation() {
        spatialView.handleSetAddCreatedEventsToSelected(false);
    }

    /**
     * Handle the completion of a multi-point drawing action, creating a
     * multi-point shape.
     * 
     * @param shapeType
     *            Type of shape being drawn; must be either
     *            {@link GeometryType#LINE} or {@link GeometryType#POLYGON}.
     * @param points
     *            List of coordinates making up the shape.
     * @deprecated When
     *             {@link #handleUserDrawingActionComplete(IAdvancedGeometry)}
     *             is used for creation of points, lines, and polygons, and when
     *             multi-vertex edits are removed (the latter once improved
     *             editing tools are added), this method should be removed.
     *             There is no reason for the spatial display to be used to turn
     *             a list of points into a geometry.
     */
    @Deprecated
    public void handleUserMultiPointDrawingActionComplete(
            GeometryType shapeType, List<Coordinate> points) {
        if (spatialView.isDrawingOfNewShapeInProgress()) {
            handleUserDrawNewShapeCompletion(shapeType, points);
        } else {
            handleUserMultiVertexEditCompletion(points);
        }
        spatialView.handleUserResetOfInputMode();
        visualCuesNeedUpdatingAtNextRefresh();
        issueRefresh();
    }

    /**
     * Handle the completion of a drawing action creating a geometry.
     * 
     * @param geometry
     *            Geometry that was created; may be <code>null</code> if the
     *            creation edit was canceled.
     */
    public void handleUserDrawingActionComplete(IAdvancedGeometry geometry) {
        if (geometry != null) {
            spatialView.handleUserCreationOfShape(geometry);
        }
        spatialView.handleUserResetOfInputMode();
        visualCuesNeedUpdatingAtNextRefresh();
        issueRefresh();
    }

    /**
     * Handle the completion of a select-by-area drawing action, creating a new
     * polygonal shape or modifying an existing shape.
     * 
     * @param identifier
     *            Identifier of the entity being edited; if <code>null</code>,
     *            no entity is being edited, and a new geometry is being
     *            created.
     * @param selectedGeometries
     *            Geometries selected during the select-by-area process; these
     *            may be combined to create the new geometry.
     */
    public void handleUserSelectByAreaDrawingActionComplete(
            IEntityIdentifier identifier, Set<Geometry> selectedGeometries) {
        spatialView.handleUserSelectByAreaDrawingActionComplete(identifier,
                selectedGeometries);
    }

    /**
     * Handle the user finishing with the use of an input mode, and having said
     * mode reset to default.
     */
    public void handleUserResetOfInputMode() {
        spatialView.handleUserResetOfInputMode();
    }

    /**
     * Make note of the fact that the next display refresh should include a
     * notification to the current mouse handler that drawables have changed, so
     * that it may update any visual cues it wishes in response.
     */
    public void visualCuesNeedUpdatingAtNextRefresh() {
        visualCuesNeedUpdating = true;
    }

    /**
     * Get the drawable currently being edited.
     * 
     * @return Drawable currently being edited.
     */
    public DrawableElement getDrawableBeingEdited() {
        return drawableManager.getDrawableBeingEdited();
    }

    /**
     * Sets the drawable that is being edited to that specified.
     * 
     * @param drawable
     *            Drawable that is now being edited.
     */
    public void setDrawableBeingEdited(AbstractDrawableComponent drawable) {
        drawableManager.setDrawableBeingEdited(drawable);
    }

    /**
     * Set the ghost of the drawable being edited to that specified.
     * 
     * @param ghost
     *            New ghost of the drawable being edited.
     */
    public void setGhostOfDrawableBeingEdited(AbstractDrawableComponent ghost) {
        drawableManager.setGhostOfDrawableBeingEdited(ghost);
    }

    /**
     * Update any bounding box drawable associated with the specified drawable
     * to bound the specified modified drawable.
     * 
     * @param associatedDrawable
     *            Drawable with which the bounding box drawable to be updated is
     *            associated.
     * @param modifiedDrawable
     *            Modified version of <code>drawable</code> that is to be used
     *            to generate any new bounding box.
     */
    public void updateBoundingBoxDrawable(
            AbstractDrawableComponent associatedDrawable,
            AbstractDrawableComponent modifiedDrawable) {
        drawableManager.updateBoundingBoxDrawable(associatedDrawable,
                modifiedDrawable);
    }

    /**
     * Get the highlit drawable.
     * 
     * @return Highlit drawable, or <code>null</code> if there is none.
     */
    public AbstractDrawableComponent getHighlitDrawable() {
        return drawableManager.getHighlitDrawable();
    }

    /**
     * Set the highlit drawable.
     * 
     * @param drawable
     *            New highlit drawable.
     * @param active
     *            Flag indicating whether or not the drawable that is to be
     *            highlit is currently active.
     */
    public void setHighlitDrawable(AbstractDrawableComponent drawable,
            boolean active) {
        drawableManager.setHighlitDrawable(drawable, active);
    }

    /**
     * Clear any recorded highlit drawable.
     */
    public void clearHighlitDrawable() {
        drawableManager.clearHighlitDrawable();
    }

    /**
     * Set the handlebar points to those specified.
     * 
     * @param points
     *            Points to be used.
     */
    public void setHandlebarPoints(List<ManipulationPoint> points) {
        drawableManager.setHandlebarPoints(points);
    }

    /**
     * Given the specified point, find all drawables which contain it.
     * 
     * @param point
     *            Point to test in geographic space.
     * @param x
     *            X coordinate of point in pixel space.
     * @param y
     *            Y coordinate of point in pixel space.
     * @return List of drawables which contain this point.
     */
    public List<AbstractDrawableComponent> getContainingDrawables(
            Coordinate point, int x, int y) {
        return drawableManager.getContainingDrawables(point, x, y);
    }

    /**
     * Given the specified geometry, find all drawables which intersect it.
     * 
     * @param geometry
     *            Geometry to test for intersection in geographic space.
     * @return List of drawables which intersect this geometry.
     */
    public List<AbstractDrawableComponent> getIntersectingDrawables(
            Geometry geometry) {
        return drawableManager.getIntersectingDrawables(geometry);
    }

    /**
     * Determine whether or not the drawable is movable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is movable, <code>false</code>
     *         otherwise.
     */
    public boolean isDrawableMovable(AbstractDrawableComponent drawable) {
        return drawableManager.isDrawableMovable(drawable);
    }

    /**
     * Determine whether or not the drawable is editable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is editable, <code>false</code>
     *         otherwise.
     */
    public boolean isDrawableEditable(AbstractDrawableComponent drawable) {
        return drawableManager.isDrawableEditable(drawable);
    }

    /**
     * Determine whether or not the drawable is rotatable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is rotatable,
     *         <code>false</code> otherwise.
     */
    public boolean isDrawableRotatable(AbstractDrawableComponent drawable) {
        return drawableManager.isDrawableRotatable(drawable);
    }

    /**
     * Determine whether or not the drawable is resizable.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is resizable,
     *         <code>false</code> otherwise.
     */
    public boolean isDrawableResizable(AbstractDrawableComponent drawable) {
        return drawableManager.isDrawableResizable(drawable);
    }

    /**
     * Determine whether or not the specified drawable is modifiable (that is,
     * movable, editable, resizable, and/or rotatable).
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is modifiable,
     *         <code>false</code> otherwise.
     */
    public boolean isDrawableModifiable(AbstractDrawableComponent drawable) {
        return drawableManager.isDrawableModifiable(drawable);
    }

    /**
     * Get the mutable drawable, and if editable and with a vertex nearby, the
     * index of said vertex under the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @param activeOnly
     *            Flag indicating whether or not only active mutable drawables
     *            should be considered.
     * @return Information including the drawable and vertex index if an
     *         editable drawable is under the point and said drawable has a
     *         vertex that lies under the point as well; drawable and
     *         <code>-1</code> for the index if a mutable (editable and/or
     *         movable) drawable lies under the point, with a <code>true</code>
     *         value for close-to-edge if the point is near the edge of the
     *         drawable and the drawable is editable; and an empty object if no
     *         mutable drawable lies under the point.
     */
    public MutableDrawableInfo getMutableDrawableInfoUnderPoint(int x, int y,
            boolean activeOnly) {
        return drawableManager.getMutableDrawableInfoUnderPoint(x, y,
                activeOnly);
    }

    /**
     * Determine whether the specified drawable is a polygon.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a polygon,
     *         <code>false</code> otherwise.
     */
    public boolean isPolygon(AbstractDrawableComponent drawable) {
        return drawableManager.isPolygon(drawable);
    }

    /**
     * Get the identifiers of active spatial entities. The resulting set is
     * unmodifiable.
     * 
     * @return Identifiers of active spatial entities.
     */
    public Set<IEntityIdentifier> getActiveSpatialEntityIdentifiers() {
        return readOnlyActiveSpatialEntityIdentifiers;
    }

    /**
     * Get the reactive drawables, that is, those that may react when moused
     * over to indicate that they are editable, movable, etc.
     * 
     * @return Reactive drawables.
     */
    public Set<AbstractDrawableComponent> getReactiveDrawables() {
        return drawableManager.getReactiveDrawables();
    }

    /**
     * Determine whether the specified geometry is valid or not, outputting a
     * warning message if it is not.
     * 
     * @param geometry
     *            Geometry to be checked for validity via the
     *            {@link Geometry#isValid()} method.
     * @return True if the geometry is valid, false otherwise.
     */
    public boolean checkGeometryValidity(IAdvancedGeometry geometry) {
        if (geometry.isValid() == false) {
            statusHandler.warn("Invalid geometry: "
                    + geometry.getValidityProblemDescription()
                    + ". Geometry modification ignored.");
            return false;
        }
        return true;
    }

    /**
     * Set the mouse cursor to the specified type.
     * 
     * @param cursorType
     *            Type of cursor to set.
     */
    public void setCursor(CursorType cursorType) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getShell();
        shell.setCursor(cursorsForCursorTypes.get(cursorType));
    }

    /**
     * Add a new vertex to the editable drawable at the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Manipulation point representing the vertext that was created, or
     *         <code>null</code> if no vertex was added.
     */
    public VertexManipulationPoint addVertex(int x, int y) {
        AbstractEditor editor = EditorUtil
                .getActiveEditorAs(AbstractEditor.class);
        AbstractDrawableComponent selectedDrawable = getHighlitDrawable();
        if ((selectedDrawable != null) && isDrawableEditable(selectedDrawable)) {

            /*
             * Get the index of the edge of the drawable that is close to or
             * under the point; if there is no drawable with an edge that
             * qualifies, do nothing more.
             */
            int edgeIndex = getMutableDrawableInfoUnderPoint(x, y, true)
                    .getEdgeIndex();
            if (edgeIndex == -1) {
                return null;
            }

            /*
             * Get the coordinates of the part of the geometry of the drawable
             * to which the point was close. For a polygon, this may be the
             * exterior shell, or one of the interior rings (if it has holes).
             * For other geometries, only one group of coordinates is possible.
             */
            IDrawable<?> drawable = (IDrawable<?>) selectedDrawable;
            Geometry geometry = ((GeometryWrapper) drawable.getGeometry())
                    .getGeometry();
            List<Coordinate[]> overallCoordinates = AdvancedGeometryUtilities
                    .getCoordinates(geometry);
            Coordinate[] coordinates = overallCoordinates.get(edgeIndex);

            /*
             * Iterate through the line segments, in each case stretching from a
             * lower-indexed one to a higher-indexed one (except when the
             * lower-indexed one is the last one, in which case the line segment
             * to be checked runs between it and the 0th indexed point).
             */
            double minDistance = Double.MAX_VALUE;
            int minPosition = Integer.MIN_VALUE;
            Coordinate colinearCoordinate = null;
            Coordinate pixelPoint = new Coordinate(x, y);
            for (int j = 1; j < coordinates.length; j++) {

                /*
                 * Get the first segment coordinate in pixels.
                 */
                double pixelCoordinates[] = editor
                        .translateInverseClick(coordinates[j - 1]);
                Coordinate pixelCoordinateA = new Coordinate(
                        pixelCoordinates[0], pixelCoordinates[1]);

                /*
                 * Get the second segment coordinate in pixels.
                 */
                if (j < coordinates.length) {
                    pixelCoordinates = editor
                            .translateInverseClick(coordinates[j]);
                } else {
                    pixelCoordinates = editor
                            .translateInverseClick(coordinates[0]);
                }
                Coordinate pixelCoordinateB = new Coordinate(
                        pixelCoordinates[0], pixelCoordinates[1]);

                /*
                 * Build the line segment, and get the distance from it to the
                 * point.
                 */
                LineSegment lineSegment = new LineSegment(pixelCoordinateA,
                        pixelCoordinateB);
                double distance = lineSegment.distance(pixelPoint);

                /*
                 * If the distance is less than the minimum required to count,
                 * and less than any previously remembered segment's distance,
                 * remember it, and calculate the point along the line segment
                 * that is closest to the mouse cursor.
                 */
                if (distance <= SpatialDisplay.SLOP_DISTANCE_PIXELS) {
                    if (distance < minDistance) {
                        minDistance = distance;
                        minPosition = j;
                        colinearCoordinate = lineSegment
                                .closestPoint(pixelPoint);
                    }
                }
            }

            /*
             * If a point along one of the line segments was found, add it as a
             * vertex.
             */
            if (colinearCoordinate != null) {

                /*
                 * Translate the new coordinate to lat-lon space, and create the
                 * new coordinates array.
                 */
                Coordinate newCoordinate = editor.translateClick(
                        colinearCoordinate.x, colinearCoordinate.y);
                Coordinate[] newCoordinates = new Coordinate[coordinates.length + 1];

                /*
                 * If the new coordinate is being added at the end of the array
                 * and the drawable is a polygon, use the new coordinate at both
                 * the beginning and the end of the array, and remove the last
                 * point from the old array, since that was a duplicate of the
                 * old first point (which is now the second point). Otherwise,
                 * just add the point in the array at the appropriate point.
                 */
                if (isPolygon(selectedDrawable)
                        && (minPosition == coordinates.length)) {
                    newCoordinates[0] = newCoordinate;
                    for (int j = 0; j < coordinates.length - 1; j++) {
                        newCoordinates[j + 1] = coordinates[j];
                    }
                    newCoordinates[newCoordinates.length - 1] = (Coordinate) newCoordinate
                            .clone();
                    minPosition = 0;
                } else {
                    int j = 0;
                    while (j < minPosition) {
                        newCoordinates[j] = coordinates[j];
                        j++;
                    }
                    newCoordinates[j] = newCoordinate;
                    for (j++; j < newCoordinates.length; j++) {
                        newCoordinates[j] = coordinates[j - 1];
                    }
                }

                /*
                 * Replace the original coordinate array with the new one, and
                 * create a geometry using the list of coordinate arrays.
                 */
                overallCoordinates.set(edgeIndex, newCoordinates);
                IAdvancedGeometry modifiedGeometry = buildModifiedGeometryForSpatialEntity(
                        drawable, overallCoordinates);

                /*
                 * Modify the original drawable and refresh the display.
                 */
                handleUserModificationOfDrawable(selectedDrawable,
                        modifiedGeometry);
                visualCuesNeedUpdatingAtNextRefresh();
                issueRefresh();

                /*
                 * Return the manipulation point that encapsulates the new
                 * vertex.
                 */
                return new VertexManipulationPoint(selectedDrawable,
                        newCoordinate, edgeIndex, minPosition);
            }
        }
        return null;
    }

    /**
     * Delete the vertex from the editable drawable at the specified point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return <code>true</code> if a vertex was deleted, <code>false</code>
     *         otherwise.
     */
    public boolean deleteVertex(int x, int y) {

        /*
         * Get the editable drawable under the point, if any, and remove its
         * vertex.
         */
        MutableDrawableInfo info = getMutableDrawableInfoUnderPoint(x, y, true);
        if ((info.getDrawable() != null)
                && (info.getManipulationPoint() instanceof VertexManipulationPoint)) {

            /*
             * Get the coordinates of the part of the geometry of the drawable
             * in which the vertex is found. For a polygon, this may be the
             * exterior shell, or one of the interior rings (if it has holes).
             * For other geometries, only one group of coordinates is possible.
             */
            IDrawable<?> drawable = (IDrawable<?>) info.getDrawable();
            Geometry geometry = ((GeometryWrapper) drawable.getGeometry())
                    .getGeometry();
            List<Coordinate[]> overallCoordinates = AdvancedGeometryUtilities
                    .getCoordinates(geometry);
            Coordinate[] coordinates = overallCoordinates.get(info
                    .getEdgeIndex());

            /*
             * Ensure there are at least three points left for paths, or five
             * points for polygons (since the latter need to have the last point
             * be identical to the first point, so they need four points at a
             * minimum).
             */
            boolean isPolygon = isPolygon(info.getDrawable());
            if (coordinates.length > (isPolygon ? 4 : 2)) {

                /*
                 * Remove the coordinate at the appropriate index. If the first
                 * or last coordinate was removed and the shape is a polygon,
                 * copy the second-to-last coordinate to be the new first
                 * coordinate, and use the rest except for the last of the old
                 * coordinates, so that the old second-to-last coordinate is now
                 * both the first and the last. Otherwise, just copy the
                 * coordinates other than the one to be removed to the new
                 * array.
                 */
                VertexManipulationPoint vertex = (VertexManipulationPoint) info
                        .getManipulationPoint();
                int vertexIndex = vertex.getVertexIndex();
                Coordinate[] newCoordinates = new Coordinate[coordinates.length - 1];
                if (isPolygon
                        && ((vertexIndex == 0) || (vertexIndex == coordinates.length - 1))) {
                    newCoordinates[0] = (Coordinate) coordinates[coordinates.length - 2]
                            .clone();
                    for (int j = 1; j < coordinates.length - 1; j++) {
                        newCoordinates[j] = coordinates[j];
                    }
                } else {
                    int j = 0;
                    while (j < vertexIndex) {
                        newCoordinates[j] = coordinates[j];
                        j++;
                    }
                    for (j++; j < coordinates.length; j++) {
                        newCoordinates[j - 1] = coordinates[j];
                    }
                }

                /*
                 * Replace the original coordinate array with the new one, and
                 * create a geometry using the list of coordinate arrays.
                 */
                overallCoordinates.set(vertex.getLinearRingIndex(),
                        newCoordinates);
                IAdvancedGeometry modifiedGeometry = buildModifiedGeometryForSpatialEntity(
                        drawable, overallCoordinates);

                /*
                 * If the geometry is valid, modify it and refresh the display.
                 * Otherwise, just refresh.
                 */
                if (checkGeometryValidity(modifiedGeometry)) {
                    handleUserModificationOfDrawable(info.getDrawable(),
                            modifiedGeometry);
                    issueRefresh();
                    return true;
                } else {
                    visualCuesNeedUpdatingAtNextRefresh();
                    issueRefresh();
                }
            }
        }
        return false;
    }

    /**
     * Build a modified geometry using the specified geometry for the spatial
     * entity associated with the specified drawable.
     * 
     * @param drawable
     *            Original drawable to be modified.
     * @param geometry
     *            Geometry to be used for the modification.
     * @return Modified geometry.
     */
    public IAdvancedGeometry buildModifiedGeometryForSpatialEntity(
            IDrawable<?> drawable, IAdvancedGeometry geometry) {

        /*
         * If the geometry being replaced is not the whole geometry of the
         * spatial entity, but instead just a sub-geometry within the entity's
         * geometry, create a new geometry collection with all the old
         * geometries as before for all other indices, and the new geometry at
         * its index.
         */
        SpatialEntity<? extends IEntityIdentifier> spatialEntity = drawableManager
                .getAssociatedSpatialEntity((AbstractDrawableComponent) drawable);
        IAdvancedGeometry originalGeometry = spatialEntity.getGeometry();
        int newIndex = drawable.getGeometryIndex();
        if ((newIndex == -1)
                && (originalGeometry instanceof AdvancedGeometryCollection)) {
            newIndex = 0;
        }
        if (newIndex != -1) {
            AdvancedGeometryCollection collection = (AdvancedGeometryCollection) originalGeometry;
            List<IAdvancedGeometry> newGeometries = new ArrayList<>(collection
                    .getChildren().size());
            for (int j = 0; j < collection.getChildren().size(); j++) {
                newGeometries.add(j == newIndex ? geometry : collection
                        .getChildren().get(j));
            }
            geometry = AdvancedGeometryUtilities
                    .createCollection(newGeometries);
        }
        return geometry;
    }

    /**
     * Build a modified geometry using the specified coordinates for the spatial
     * entity associated with the specified drawable.
     * 
     * @param drawable
     *            Original drawable, representing part or all of the spatial
     *            entity for which the geometry is to be built.
     * @param coordinates
     *            List of arrays of coordinates. The list will hold one array if
     *            the geometry being built is not a polygon, or is a polygon
     *            without holes; it will hold more than one if the geometry
     *            being built is a polygon with holes, with each one after the
     *            first array describing one of the holes.
     * @return Modified geometry.
     */
    public IAdvancedGeometry buildModifiedGeometryForSpatialEntity(
            IDrawable<?> drawable, List<Coordinate[]> coordinates) {
        IAdvancedGeometry geometry = buildModifiedGeometryForDrawable(drawable,
                coordinates);
        return buildModifiedGeometryForSpatialEntity(drawable, geometry);
    }

    /**
     * Build a modified geometry using the specified coordinates to be used to
     * modify the specified drawable. Note that this yields different results
     * from {@link #buildModifiedGeometryForSpatialEntity(IDrawable, List)}, as
     * the latter creates the geometry for an entire spatial entity, which might
     * be associated with several drawables, whereas this method builds a
     * geometry for just the specified drawable, which may only represent part
     * of a spatial entity.
     * 
     * @param drawable
     *            Original drawable to be modified.
     * @param coordinates
     *            List of arrays of coordinates. The list will hold one array if
     *            the geometry being built is not a polygon, or is a polygon
     *            without holes; it will hold more than one if the geometry
     *            being built is a polygon with holes, with each one after the
     *            first array describing one of the holes.
     * @return Modified geometry.
     */
    public IAdvancedGeometry buildModifiedGeometryForDrawable(
            IDrawable<?> drawable, List<Coordinate[]> coordinates) {
        return createMultiPointGeometryFromCoordinates(drawable, coordinates);
    }

    // Protected Methods

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);

        /*
         * Initialize the line pattern manager here. This saves time with
         * drawing later. Note that this is safe to do in whatever random thread
         * the Job uses, since LinePatternManager.getInstance() is synchronized.
         */
        new Job("Loading PGEN LinePatternManager...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                LinePatternManager.getInstance();
                return Status.OK_STATUS;
            }

        }.schedule();
    }

    @Override
    protected void disposeInternal() {

        /*
         * If the perspective is not changing, notify the view that this display
         * was disposed of.
         */
        if (perspectiveChanging == false) {
            spatialView.handleSpatialDisplayClosed();
        }

        inputHandler = null;

        drawableManager.dispose();

        super.disposeInternal();
    }

    @Override
    protected void paint(IGraphicsTarget target, PaintProperties paintProps,
            Object object, AbstractMovableToolLayer.SelectionStatus status)
            throws VizException {

        /*
         * No action; this class does not rely upon the superclass to do its
         * drawing, as it uses a custom iterator to draw the drawables in the
         * right order.
         */
    }

    @Override
    protected boolean isClicked(IDisplayPaneContainer container,
            Coordinate mouseLoc, Object object) {
        return false;
    }

    @Override
    protected AbstractDrawableComponent makeLive(Object object) {
        return null;
    }

    @Override
    protected AbstractDrawableComponent move(Coordinate lastMouseLoc,
            Coordinate mouseLoc, Object object) {
        return null;
    }

    @Override
    protected String getDefaultName() {
        return null;
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        if (visualCuesNeedUpdating) {
            visualCuesNeedUpdating = false;
            inputHandler.updateVisualCues();
        }
        drawableManager.paint(target, paintProps,
                checkForMapRescale(paintProps));
    }

    // Package-Private Methods

    /**
     * Get the spatial entities list for the specified type. Note that the
     * resulting list is an unmodifiable view of the actual list, so it must be
     * treated as read-only.
     * 
     * @param type
     *            Type for which the spatial entities list is to be fetched.
     * @return Unmodifiable view of the spatial entities list for the specified
     *         type.
     */
    List<SpatialEntity<? extends IEntityIdentifier>> getSpatialEntities(
            SpatialEntityType type) {
        return readOnlySpatialEntitiesForTypes.get(type);
    }

    /**
     * Set the spatial view.
     * 
     * @param view
     *            New spatial view.
     */
    void setSpatialView(SpatialView view) {
        spatialView = view;
    }

    /**
     * Get the spatial entities list state changer.
     * 
     * @return Spatial entities list state changer.
     */
    IListStateChanger<SpatialEntityType, SpatialEntity<? extends IEntityIdentifier>> getSpatialEntitiesChanger() {
        return spatialEntitiesChanger;
    }

    /**
     * Set the current input handler to be the specified type of non-drawing
     * input handler.
     * 
     * @param type
     *            Type of input handler being requested; must be either
     *            {@link InputHandlerType#SINGLE_SELECTION},
     *            {@link InputHandlerType#FREEHAND_MULTI_SELECTION}, or
     *            {@link InputHandlerType#RECTANGLE_MULTI_SELECTION}.
     */
    void setCurrentInputHandlerToNonDrawing(InputHandlerType type) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getNonDrawingInputHandler(type);
    }

    /**
     * Set the current input handler to be the specified type of drawing input
     * handler.
     * 
     * @param handlerType
     *            Type of input handler being requested.
     * @param geometryType
     *            Type of geometry that is to be drawn by the requested handler.
     */
    void setCurrentInputHandlerToDrawing(InputHandlerType handlerType,
            GeometryType geometryType) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getDrawingInputHandler(handlerType,
                geometryType);
    }

    /**
     * Set the current input handler to be the select-by-area input handler.
     * 
     * @param vizResource
     *            Select-by-area viz resource to be used with this handler.
     * @param selectedGeometries
     *            Select-by-area geometries that should start off as selected.
     * @param identifier
     *            Identifier of the entity that is to be edited using
     *            select-by-area; if <code>null</code>, a new geometry is to be
     *            created.
     */
    void setCurrentInputHandlerToSelectByArea(
            SelectByAreaDbMapResource vizResource,
            Set<Geometry> selectedGeometries, IEntityIdentifier identifier) {
        if (inputHandler != null) {
            inputHandler.reset();
        }
        inputHandler = inputHandlerFactory.getSelectByAreaInputHandler(
                vizResource, selectedGeometries, identifier);
    }

    /**
     * Repopulate the active spatial entity identifiers record if the specified
     * type of entity that was changed is relevant. Said record is a combination
     * of the currently selected entities and tool-related entities.
     * 
     * @param type
     *            Type of spatial entities that changed; if
     *            {@link SpatialEntityType#TOOL}, repopulation will be done.
     */
    void repopulateActiveSpatialEntityIdentifiers(SpatialEntityType type) {
        if (type != SpatialEntityType.TOOL) {
            return;
        }
        activeSpatialEntityIdentifiers.clear();
        Set<IEntityIdentifier> selectedSpatialEntityIdentifiers = spatialView
                .getSelectedSpatialEntityIdentifiers();
        if (selectedSpatialEntityIdentifiers != null) {
            activeSpatialEntityIdentifiers
                    .addAll(selectedSpatialEntityIdentifiers);
        }
        for (SpatialEntity<? extends IEntityIdentifier> spatialEntity : spatialEntitiesForTypes
                .get(SpatialEntityType.TOOL)) {
            activeSpatialEntityIdentifiers.add(spatialEntity.getIdentifier());
        }
    }

    // Private Methods

    /**
     * Insert the specified spatial entities of the specified type at the
     * specified index of the appropriate list.
     * 
     * @param type
     *            Type of spatial entities being inserted.
     * @param index
     *            Index at which to insert the spatial entities.
     * @param spatialEntities
     *            Spatial entities to be inserted.
     */
    private void insertSpatialEntities(
            SpatialEntityType type,
            int index,
            List<? extends SpatialEntity<? extends IEntityIdentifier>> spatialEntities) {
        spatialEntitiesForTypes.get(type).addAll(index, spatialEntities);
    }

    /**
     * Remove the specified number of spatial entities of the specified type
     * starting at the specified index of the appropriate list.
     * 
     * @param type
     *            Type of spatial entities being removed.
     * @param index
     *            Index from which to start removing the spatial entities.
     * @param count
     *            Number of spatial entities to be removed.
     */
    private void removeSpatialEntities(SpatialEntityType type, int index,
            int count) {
        List<SpatialEntity<? extends IEntityIdentifier>> list = spatialEntitiesForTypes
                .get(type);
        for (int j = 0; j < count; j++) {
            list.remove(index);
        }
    }

    /**
     * Get the identifier of the spatial entity under the specified point, if
     * only one is under the point.
     * 
     * @param x
     *            Pixel X coordinate.
     * @param y
     *            Pixel Y coordinate.
     * @return Identifier of the spatial entity if that is the only entity under
     *         the point, or <code>null</code> if less or more than one entity
     *         are under said point.
     */
    private IEntityIdentifier getSpatialEntityAtPoint(int x, int y) {
        AbstractEditor editor = ((AbstractEditor) VizWorkbenchManager
                .getInstance().getActiveEditor());
        List<AbstractDrawableComponent> containingDrawables = getContainingDrawables(
                editor.translateClick(x, y), x, y);
        if (containingDrawables.size() == 1) {
            AbstractDrawableComponent drawable = containingDrawables.get(0);
            if (drawable instanceof IDrawable) {
                return ((IDrawable<?>) drawable).getIdentifier();
            }
        }
        return null;
    }

    /**
     * Ensure that the thread executing this invocation is the main UI thread.
     * 
     * @throws IllegalStateException
     *             If the thread currently executing is not the main UI thread.
     */
    private void ensureExecutingThreadIsMainUiThread() {
        if (Display.getDefault().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("inappropriate thread "
                    + Thread.currentThread()
                    + " used to add context menu items");
        }
    }

    /**
     * Determine whether the specified drawable is a polygon.
     * 
     * @param drawable
     *            Drawable to be checked.
     * @return <code>true</code> if the drawable is a polygon,
     *         <code>false</code> otherwise.
     */
    private boolean isPolygon(IDrawable<?> drawable) {
        return ((drawable instanceof PathDrawable) && ((PathDrawable) drawable)
                .isClosedLine());
    }

    /**
     * Creaet a geometry from the specified list of coordinate arrays, with the
     * geometry being the same type as the specified original drawable.
     * 
     * @param drawable
     *            Original drawable, providing the type of geometry that the
     *            coordinates should form.
     * @param coordinates
     *            List of arrays of coordinates. The list will hold one array if
     *            the geometry being built is not a polygon, or is a polygon
     *            without holes; it will hold more than one if the geometry
     *            being built is a polygon with holes, with each one after the
     *            first array describing one of the holes.
     * @return Geometry.
     */
    private IAdvancedGeometry createMultiPointGeometryFromCoordinates(
            IDrawable<?> drawable, List<Coordinate[]> coordinates) {
        Geometry result;
        GeometryFactory geometryFactory = AdvancedGeometryUtilities
                .getGeometryFactory();
        if (isPolygon(drawable)) {
            LinearRing exteriorRing = geometryFactory
                    .createLinearRing(coordinates.get(0));
            LinearRing[] interiorRings = (coordinates.size() > 1 ? new LinearRing[coordinates
                    .size() - 1] : null);
            if (interiorRings != null) {
                for (int j = 0; j < coordinates.size() - 1; j++) {
                    interiorRings[j] = geometryFactory
                            .createLinearRing(coordinates.get(j + 1));
                }
            }
            result = geometryFactory.createPolygon(exteriorRing, interiorRings);
        } else if (drawable.getClass() == PathDrawable.class) {
            result = geometryFactory.createLineString(coordinates.get(0));
        } else {
            throw new IllegalArgumentException("Unexpected geometry "
                    + drawable.getClass());
        }
        return AdvancedGeometryUtilities.createGeometryWrapper(result, 0);
    }

    /**
     * Handle the completion of the drawing of the new shape by the user.
     * 
     * @param shapeType
     *            Type of shape the points make up.
     * @param points
     *            Points to be used to construct the shape.
     */
    private void handleUserDrawNewShapeCompletion(GeometryType shapeType,
            List<Coordinate> points) {

        /*
         * Do nothing if user hasn't drawn enough points to create a path or
         * polygon.
         */
        if (points.size() < (shapeType == GeometryType.LINE ? 2 : 3)) {
            return;
        }

        /*
         * Simplify the number of points in the path or polygon.
         * 
         * TODO: This may eventually need to be user-configurable.
         */
        Geometry newGeometry = null;
        GeometryFactory geometryFactory = AdvancedGeometryUtilities
                .getGeometryFactory();
        if (shapeType == GeometryType.POLYGON) {
            AdvancedGeometryUtilities.addDuplicateLastCoordinate(points);
            LinearRing linearRing = geometryFactory.createLinearRing(points
                    .toArray(new Coordinate[points.size()]));
            Geometry polygon = geometryFactory.createPolygon(linearRing, null);
            newGeometry = TopologyPreservingSimplifier.simplify(polygon,
                    SIMPLIFIER_DISTANCE_TOLERANCE);
        } else {
            LineString lineString = geometryFactory.createLineString(points
                    .toArray(new Coordinate[points.size()]));
            newGeometry = TopologyPreservingSimplifier.simplify(lineString,
                    SIMPLIFIER_DISTANCE_TOLERANCE);
        }

        /*
         * Create the entity for the path or polygon.
         */
        spatialView.handleUserCreationOfShape(AdvancedGeometryUtilities
                .createGeometryWrapper(newGeometry, 0));
    }

    /**
     * Apply the specified path to the first selected polygon that is found as a
     * multi-vertex edit. Note that the direction in which the user draws the
     * replacement points matters. It is assumed they are drawing the
     * replacement points in the same direction as the original polygon. For
     * select-by-area and as recommended by the recommenders, this direction is
     * clockwise.
     * <p>
     * TODO: There are a number of limitations with this method's current
     * implementation. First, it does not allow paths (lines) to be modified,
     * only polygons. Second, it only modifies the first polygon found within
     * the geometry collection for the hazard event. And third, when attempting
     * to find out where the new path joins at each end with the polygon being
     * edited, it only finds the closest vertices in the polygon for each end of
     * the path, not the closest points along the line segments making up the
     * polygon. All these limitations should be corrected, or...
     * </p>
     * <p>
     * It might be better to dispense with this approach entirely, and instead
     * add a drop-down to the console toolbar allowing the choice of one of four
     * drawing modes:
     * </p>
     * <ul>
     * <li>New, meaning new hazard events are created with any lines, points, or
     * polygons that are drawn.</li>
     * <li>Add, meaning that any lines, points or polygons that are drawn are
     * added to the currently selected hazard event. The results are ORed with
     * the existing geometries.</li>
     * <li>Intersect, meaning that any lines, points or polygons that are drawn
     * are ANDed with the current geometry, thus resulting in a new geometry
     * that is an intersection of what was there before and the new geometry.
     * How would this work with points? With lines? Maybe points and lines are
     * not allowed to be drawn in this mode?</li>
     * <li>Remove, meaning that any lines, points and polygons that are drawn
     * are ORed with the current geometry, and the result is removed from the
     * current geometry, leaving only the parts of the old geometry that did not
     * intersect with the new shapes. Again, maybe don't allow points or lines
     * to be drawn in this mode?</li>
     * </ul>
     * <p>
     * If this is done, we could remove the "Add new geometries to selected"
     * button, as it would become superfluous. Also, of course, the last three
     * of the above choices would be unavailable when either zero, or two or
     * more, hazard events were selected.
     * </p>
     * 
     * @param points
     *            Latitude-longitude points making up the path to be applied.
     */
    private void handleUserMultiVertexEditCompletion(List<Coordinate> points) {
        PathDrawable selectedPolygon = drawableManager
                .getFirstReactivePolygon();
        if (selectedPolygon == null) {
            return;
        }
        Geometry geometry = selectedPolygon.getGeometry().getGeometry();
        Polygon polygon = getFirstPolygon(geometry);
        if (polygon != null) {
            Coordinate[] origCoordinatesAsArray = polygon.getCoordinates();
            List<Coordinate> origCoordinates = new ArrayList<>(
                    Arrays.asList(origCoordinatesAsArray));
            AdvancedGeometryUtilities
                    .removeDuplicateLastCoordinate(origCoordinates);
            int indexOfFirstPointToRemove = getIndexOfClosestPoint(
                    origCoordinates, points.get(0));
            int indexOfLastPointToRemove = getIndexOfClosestPoint(
                    origCoordinates, points.get(points.size() - 1));

            List<Coordinate> newCoordinates = new ArrayList<>();

            if (indexOfFirstPointToRemove <= indexOfLastPointToRemove) {
                for (int i = 0; i < indexOfFirstPointToRemove; i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
                for (int i = 0; i < points.size(); i++) {
                    newCoordinates.add(points.get(i));
                }

                for (int i = indexOfLastPointToRemove + 1; i < origCoordinates
                        .size(); i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
            } else {

                /*
                 * This deals with the case when the user chooses a section to
                 * replace that bounds the first point of the original polygon
                 * (i.e. the replacement section is crossing over from first to
                 * last point or from last to first).
                 */
                for (int i = 0; i < points.size(); i++) {
                    newCoordinates.add(points.get(i));
                }
                for (int i = indexOfLastPointToRemove + 1; i < indexOfFirstPointToRemove; i++) {
                    newCoordinates.add(origCoordinates.get(i));
                }
            }

            /*
             * Only modify the geometry if the result is a polygon.
             */
            if (newCoordinates.size() >= 3) {
                AdvancedGeometryUtilities
                        .addDuplicateLastCoordinate(newCoordinates);
                GeometryFactory geometryFactory = AdvancedGeometryUtilities
                        .getGeometryFactory();
                IAdvancedGeometry newGeometry = AdvancedGeometryUtilities
                        .createGeometryWrapper(geometryFactory.createPolygon(
                                geometryFactory.createLinearRing(newCoordinates
                                        .toArray(new Coordinate[newCoordinates
                                                .size()])), null), 0);
                if (checkGeometryValidity(newGeometry)) {
                    handleUserModificationOfDrawable(selectedPolygon,
                            newGeometry);
                }
            }
        }
    }

    /**
     * Extract the first polygon from the specified geometry, meaning either the
     * geometry itself if it is a polygon, or the first polygon found when
     * iterating through the geometry if it is a collection.
     * 
     * @param geometry
     *            Geometry from which to get the first polygon.
     * @return First polygon within the specified geometry, or <code>null</code>
     *         if there is no polygon found.
     */
    private Polygon getFirstPolygon(Geometry geometry) {
        if (geometry instanceof Polygon) {
            return (Polygon) geometry;
        } else if (geometry instanceof GeometryCollection) {
            GeometryCollection geometryCollection = (GeometryCollection) geometry;
            for (int j = 0; j < geometryCollection.getNumGeometries(); j++) {
                Geometry subGeometry = geometryCollection.getGeometryN(j);
                if (subGeometry instanceof Polygon) {
                    return (Polygon) subGeometry;
                }
            }
        }
        return null;
    }

    /**
     * Get the index of the closest point out of the specified coordinates to
     * the given coordinate.
     * 
     * @param origCoordinates
     *            Coordinates in which to find the closest point.
     * @param coordinate
     *            Coordinate for which the closest point is to be found.
     * @return Index of the closest point in the specified coordinates.
     */
    private int getIndexOfClosestPoint(List<Coordinate> origCoordinates,
            Coordinate coordinate) {
        int result = 0;
        double minDistance = coordinate.distance(origCoordinates.get(0));
        for (int i = 1; i < origCoordinates.size(); i++) {
            double distance = coordinate.distance(origCoordinates.get(i));
            if (distance < minDistance) {
                result = i;
                minDistance = distance;
            }
        }
        return result;
    }

    /**
     * Initialize data time frames.
     * 
     * @param startDataTime
     *            The start time to use.
     * @param numberOfDataTimes
     *            The number of data times to initialize behind the start time.
     */
    private void fillDataTimeArray(DataTime startDataTime, int numberOfDataTimes) {
        long time = startDataTime.getRefTime().getTime();
        DataTime currentDataTime = null;

        for (int i = 0; i < numberOfDataTimes; i++) {
            time -= DATA_TIME_INCREMENT_MILLIS;
            currentDataTime = new DataTime(new Date(time));
            dataTimes.add(currentDataTime);
        }
    }

    /**
     * Check to see if the last action set the client map to a new scale factor.
     * <p>
     * Setting the map scale factor is part of the D2D perspective, and is not
     * supported by the Hydro perspective.
     * </p>
     * <p>
     * This method causes the displayables to be recreated for the new scale
     * just as does a zoom level change. This allows the displayables to resize
     * their geometry coordinates to the correct pixels on the displayed maps.
     * </p>
     * 
     * @param paintProperties
     *            Display paint properties.
     * @return <code>true</code> if the scale changed, <code>false</code>
     *         otherwise.
     */
    private boolean checkForMapRescale(PaintProperties paintProperties) {
        double newScaleFactor = 0.0d;
        double canvasX = paintProperties.getCanvasBounds().width;
        double viewX = paintProperties.getView().getExtent().getWidth();
        if (viewX != 0.0d) {
            newScaleFactor = canvasX / viewX;
        } else {
            newScaleFactor = -1.0d;
        }

        boolean scaleChange = false;
        if (newScaleFactor != mapScaleFactor) {
            mapScaleFactor = newScaleFactor;
            scaleChange = true;
        } else {

            /*
             * Only do this for the D2D perspective.
             */
            String newMapScale = null;
            PerspectiveSpecificProperties perspectiveProps = paintProperties
                    .getPerspectiveProps();
            if (perspectiveProps != null) {
                if (perspectiveProps instanceof D2DProperties) {
                    D2DProperties d2dPerspectiveProps = (D2DProperties) perspectiveProps;
                    newMapScale = d2dPerspectiveProps.getScale();
                }
            }
            if (newMapScale == null) {
                newMapScale = DEFAULT_MAP_SCALE_NAME;
            }

            if (newMapScale.equals(mapScale) == false) {
                mapScale = newMapScale;
                scaleChange = true;
            } else {
                zoomLevel = paintProperties.getZoomLevel();
            }
        }

        if (scaleChange == true) {
            float curZoomLevel = paintProperties.getZoomLevel();
            if (zoomLevel == curZoomLevel) {
                paintProperties.setZoomLevel(curZoomLevel
                        + IOTA_ZOOM_LEVEL_FACTOR_DELTA);
            }
        }
        return scaleChange;
    }
}
