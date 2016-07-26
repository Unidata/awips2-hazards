/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea;

import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.raytheon.uf.common.dataquery.db.QueryResult;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.DrawableString;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.IGraphicsTarget.HorizontalAlignment;
import com.raytheon.uf.viz.core.IGraphicsTarget.VerticalAlignment;
import com.raytheon.uf.viz.core.PixelExtent;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery.QueryLanguage;
import com.raytheon.uf.viz.core.drawables.IShadedShape;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.maps.rsc.AbstractDbMapResource;
import com.raytheon.uf.viz.core.maps.rsc.AbstractDbMapResourceData.ColumnDefinition;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.capabilities.ColorableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.LabelableCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.MagnificationCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.OutlineCapability;
import com.raytheon.uf.viz.core.rsc.capabilities.ShadeableCapability;
import com.raytheon.viz.core.rsc.jts.JTSCompiler;
import com.raytheon.viz.core.rsc.jts.JTSCompiler.PointStyle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBReader;

/**
 * Database map resource for the select-by-area tool. An instances of this class
 * loads polygons from the maps database for a specified maps database table,
 * and keeps track of the loaded geometries and the ones selected by the user.
 * The union of these selected geometries forms the basis for a hazard polygon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer        Description
 * ------------ ---------- --------------- --------------------------
 * Dec 2011                Bryon.Lawrence  Initial creation.
 * Jul 25, 2016   19537    Chris.Golden    Completely revamped, adding comments
 *                                         and cleaning up throughout.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class SelectByAreaDbMapResource extends
        AbstractDbMapResource<SelectByAreaDbMapResourceData, MapDescriptor> {

    // Private Static Constants

    /**
     * RGB values for white.
     */
    private static final RGB RGB_COLOR_WHITE = new RGB(255, 255, 255);

    /**
     * Maps database name.
     */
    private static final String MAPS_DATABASE_NAME = "maps";

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectByAreaDbMapResource.class);

    // Private Classes

    /**
     * Encapsulation of a label, its display bounding box, and its location.
     */
    private class LabelNode {

        // Private Variables

        /**
         * Label text.
         */
        private final String label;

        /**
         * Location of the label in pixels as a two-element array, the first
         * element being the X coordinate, the second the Y coordinate.
         */
        private final double[] location;

        /**
         * Bounding box of the label on the display.
         */
        private final Rectangle2D boundingBox;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param label
         *            Label text.
         * @param coordinates
         *            Point geometry in world units.
         * @param target
         *            Graphics target.
         */
        public LabelNode(String label, Point coordinates, IGraphicsTarget target) {
            this.label = label;
            this.location = getDescriptor().worldToPixel(
                    new double[] { coordinates.getCoordinate().x,
                            coordinates.getCoordinate().y });
            DrawableString drawableString = new DrawableString(label, null);
            drawableString.font = font;
            boundingBox = target.getStringsBounds(drawableString);
        }

        // Public Methods

        /**
         * Get the label text.
         * 
         * @return Label text.
         */
        public String getLabel() {
            return label;
        }

        /**
         * Get the location of the label.
         * 
         * @return Location of the label in pixels as a two-element array, the
         *         first element being the X coordinate, the second the Y
         *         coordinate.
         */
        public double[] getLocation() {
            return location;
        }

        /**
         * Get the bounding box of the label in pixels.
         * 
         * @return Bounding box of the label in pixels.
         */
        public Rectangle2D getBoundingBox() {
            return boundingBox;
        }
    }

    /**
     * Query job for retrieving data from the AWIPS II geo database. When run,
     * an instance produces a wireframe shape which can then be rendered on the
     * CAVE display. It also buffers the individual geometries for use in
     * draw-by-area operations.
     */
    private class MapQueryJob extends Job {

        // Private Static Constants

        /**
         * Maximum size of the request and result queues.
         */
        private static final int QUEUE_LIMIT = 1;

        // Public Classes

        /**
         * Request to be submitted as the query.
         */
        public class Request {

            // Private Variables

            /**
             * Random number generator, used to generate color.
             */
            private final Random randomNumberGenerator = new Random(
                    System.currentTimeMillis());

            /**
             * Graphics target.
             */
            private final IGraphicsTarget target;

            /**
             * Query to be submitted.
             */
            private final String query;

            /**
             * Label field.
             */
            private final String labelField;

            /**
             * Shading field.
             */
            private final String shadingField;

            // Public Constructors

            /**
             * Construct a standard instance.
             * 
             * @param target
             *            Graphics target.
             * @param query
             *            Query string.
             * @param labelField
             *            Label field.
             * @param shadingField
             *            Shading field.
             */
            public Request(IGraphicsTarget target, String query,
                    String labelField, String shadingField) {
                this.target = target;
                this.query = query;
                this.labelField = labelField;
                this.shadingField = shadingField;
            }

            // Public Methods

            /**
             * Get the color associated with the specified key.
             * 
             * @param key
             *            Key for which to fetch the color.
             * @return Color in RGB form.
             */
            public RGB getColor(Object key) {

                /*
                 * Create the color map if this has not been done already.
                 */
                if (colorMap == null) {
                    colorMap = new HashMap<Object, RGB>();
                }

                /*
                 * Get the color from the map, creating a new one with random
                 * values and recording it if none is found.
                 */
                RGB color = colorMap.get(key);
                if (color == null) {
                    color = new RGB(randomNumberGenerator.nextInt(206) + 50,
                            randomNumberGenerator.nextInt(206) + 50,
                            randomNumberGenerator.nextInt(206) + 50);
                    colorMap.put(key, color);
                }

                return color;
            }

            // Public Methods

            /**
             * Get the query submitted for this request.
             * 
             * @return Query submitted for this request.
             */
            public String getQuery() {
                return query;
            }

            /**
             * Get the graphics target.
             * 
             * @return Graphics target.
             */
            public IGraphicsTarget getTarget() {
                return target;
            }

            /**
             * Get the label field.
             * 
             * @return Label field.
             */
            public String getLabelField() {
                return labelField;
            }

            /**
             * Get the shading field.
             * 
             * @return Shading field.
             */
            public String getShadingField() {
                return shadingField;
            }
        }

        /**
         * Result of the submitted query.
         */
        public class Result {

            // Private Variables

            /**
             * Query that was submitted and provided this result.
             */
            private final String query;

            /**
             * Outline shape; if <code>null</code>, the query failed.
             */
            private final IWireframeShape outlineShape;

            /**
             * Shaded shape; if <code>null</code>, the query failed.
             */
            private final IShadedShape shadedShape;

            /**
             * Labels; if <code>null</code>, the query failed.
             */
            private final List<LabelNode> labels;

            /**
             * Cause of failure; if <code>null</code>, there was no failure.
             */
            private final Throwable failureCause;

            // Public Constructors

            /**
             * Construct a standard instance indicating a successful result.
             * 
             * @param query
             *            Query that yielded this result.
             * @param outlineShape
             *            Outline shape.
             * @param shadedShape
             *            Shaded shape.
             * @param labels
             *            Labels.
             */
            public Result(String query, IWireframeShape outlineShape,
                    IShadedShape shadedShape, List<LabelNode> labels) {
                this.query = query;
                this.outlineShape = outlineShape;
                this.shadedShape = shadedShape;
                this.labels = labels;
                this.failureCause = null;
            }

            /**
             * Construct a standard instance indicating a failed result.
             * 
             * @param query
             *            Query that yielded this result.
             * @param failureCause
             *            Cause of the failure.
             */
            public Result(String query, Throwable failureCause) {
                this.query = query;
                this.outlineShape = null;
                this.shadedShape = null;
                this.labels = null;
                this.failureCause = failureCause;
            }

            // Public Methods

            /**
             * Get the query that yielded this result.
             * 
             * @return Query that yielded this result.
             */
            public String getQuery() {
                return query;
            }

            /**
             * Get the outline shape.
             * 
             * @return Outline shape.
             */
            public IWireframeShape getOutlineShape() {
                return outlineShape;
            }

            /**
             * Get the shaded shape.
             * 
             * @return Shaded shape.
             */
            public IShadedShape getShadedShape() {
                return shadedShape;
            }

            /**
             * Get the labels.
             * 
             * @return Labels.
             */
            public List<LabelNode> getLabels() {
                return labels;
            }

            /**
             * Get the failure cause, if any.
             * 
             * @return Failure cause; if <code>null</code>, there was no
             *         failure.
             */
            public Throwable getFailureCause() {
                return failureCause;
            }
        }

        // Private Variables

        /**
         * Request queue.
         */
        private final ArrayBlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(
                QUEUE_LIMIT);

        /**
         * Result queue.
         */
        private final ArrayBlockingQueue<Result> resultQueue = new ArrayBlockingQueue<Result>(
                QUEUE_LIMIT);

        /**
         * Flag indicating whether or not the job has been canceled.
         */
        private boolean canceled;

        // Public Constructors

        /**
         * Construct a standard instance.
         */
        public MapQueryJob() {
            super("Retrieving map...");
        }

        // Public Methods

        /**
         * Make a request with the specified parameters.
         * 
         * @param target
         *            Graphics target.
         * @param query
         *            Query to be submitted.
         * @param labelField
         *            Label field.
         * @param shadingField
         *            Shading field.
         */
        public void request(IGraphicsTarget target, String query,
                String labelField, String shadingField) {

            /*
             * If there is already a request in the queue, remove it. Then add
             * this new request.
             */
            if (requestQueue.size() == QUEUE_LIMIT) {
                requestQueue.poll();
            }
            requestQueue.add(new Request(target, query, labelField,
                    shadingField));

            /*
             * Stop (if necessary) and start the job.
             */
            cancel();
            schedule();
        }

        /**
         * Get the latest result.
         * 
         * @return Latest result.
         */
        public Result getLatestResult() {
            return resultQueue.poll();
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Request request = requestQueue.poll();
            while (request != null) {
                Result result = null;
                try {

                    /*
                     * Submit the query and get the result.
                     */
                    QueryResult mappedResult = DirectDbQuery
                            .executeMappedQuery(request.getQuery(),
                                    MAPS_DATABASE_NAME, QueryLanguage.SQL);

                    /*
                     * Clear the geometries.
                     */
                    originalGeometries.clear();

                    /*
                     * Create a new labels list.
                     */
                    List<LabelNode> newLabels = new ArrayList<LabelNode>();

                    /*
                     * Create the wireframe and shaded shapes as necessary.
                     */
                    IWireframeShape newOutlineShape = request.getTarget()
                            .createWireframeShape(false, getDescriptor());
                    IShadedShape newShadedShape = null;
                    if (request.getShadingField() != null) {
                        newShadedShape = request.getTarget().createShadedShape(
                                false, getDescriptor().getGridGeometry());
                    }

                    /*
                     * Build a JTS compiler to be used to compile the
                     * geometries.
                     */
                    JTSCompiler jtsCompiler = new JTSCompiler(newShadedShape,
                            newOutlineShape, getDescriptor());

                    /*
                     * Iterate through the query result geometries, handling
                     * each in turn.
                     */
                    List<Geometry> resultGeometries = new ArrayList<Geometry>(
                            mappedResult.getResultCount());
                    int numPoints = 0;
                    WKBReader wkbReader = new WKBReader();
                    for (int j = 0; j < mappedResult.getResultCount(); j++) {

                        /*
                         * If marked as canceled, do nothing more.
                         */
                        if (canceled) {
                            canceled = false;
                            return Status.CANCEL_STATUS;
                        }

                        /*
                         * Get the geometry from the result.
                         */
                        Geometry geometry = null;
                        Object geometryObject = mappedResult.getRowColumnValue(
                                j, 0);
                        if (geometryObject instanceof byte[]) {
                            geometry = wkbReader.read((byte[]) geometryObject);
                        } else {
                            statusHandler.handle(
                                    Priority.ERROR,
                                    "Expected byte[] but received "
                                            + geometryObject.getClass()
                                                    .getName() + ": "
                                            + geometryObject.toString()
                                            + "\n  query=\""
                                            + request.getQuery() + "\"");
                        }

                        /*
                         * Get the label, if one was provided, and use it once
                         * for each sub-geometry within the geometry.
                         */
                        Object labelObject = null;
                        if (request.getLabelField() != null) {
                            labelObject = mappedResult.getRowColumnValue(j,
                                    request.getLabelField().toLowerCase());
                        }
                        if ((labelObject != null) && (geometry != null)) {

                            /*
                             * Get the label string.
                             */
                            String label;
                            if (labelObject instanceof BigDecimal) {
                                label = Double.toString(((Number) labelObject)
                                        .doubleValue());
                            } else {
                                label = labelObject.toString();
                            }

                            /*
                             * Sort the sub-geometries of the geometry so that
                             * they are ordered in decreasing size.
                             */
                            int numGeometries = geometry.getNumGeometries();
                            List<Geometry> geometries = new ArrayList<Geometry>(
                                    numGeometries);
                            for (int k = 0; k < numGeometries; k++) {
                                Geometry poly = geometry.getGeometryN(k);
                                geometries.add(poly);
                            }
                            Collections.sort(geometries,
                                    new Comparator<Geometry>() {
                                        @Override
                                        public int compare(Geometry g1,
                                                Geometry g2) {
                                            return (int) Math.signum(g2
                                                    .getEnvelope().getArea()
                                                    - g1.getEnvelope()
                                                            .getArea());
                                        }
                                    });

                            for (Geometry polygon : geometries) {
                                Point point = polygon.getInteriorPoint();
                                if (point.getCoordinate() != null) {
                                    LabelNode node = new LabelNode(label,
                                            point, request.getTarget());
                                    newLabels.add(node);
                                }
                            }
                        }

                        /*
                         * Add the number of points in the geometry to the
                         * total, and add the geometry to the results. Also
                         * remember its shading field if it has one.
                         */
                        if (geometry != null) {
                            numPoints += geometry.getNumPoints();
                            resultGeometries.add(geometry);

                            if (request.getShadingField() != null) {
                                geometry.setUserData(mappedResult
                                        .getRowColumnValue(j, request
                                                .getShadingField()
                                                .toLowerCase()));
                            }

                            /*
                             * A copy must be made, or else the geometry will be
                             * compiled.
                             */
                            originalGeometries.add((Geometry) geometry.clone());
                        }
                    }

                    /*
                     * Compile the geometries into the outline shape, and if
                     * needed, the shaded shape as well.
                     */
                    newOutlineShape.allocate(numPoints);
                    for (Geometry geometry : resultGeometries) {
                        RGB color = null;
                        Object shadedField = geometry.getUserData();
                        if (shadedField != null) {
                            color = request.getColor(shadedField);
                        }
                        try {
                            JTSCompiler.JTSGeometryData jtsGeometryData = jtsCompiler
                                    .createGeometryData();
                            jtsGeometryData.setGeometryColor(color);
                            jtsGeometryData.setPointStyle(PointStyle.CROSS);
                            jtsCompiler.handle(geometry, jtsGeometryData);
                        } catch (VizException e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Error reprojecting map outline", e);
                        }
                    }
                    newOutlineShape.compile();
                    if (request.getShadingField() != null) {
                        newShadedShape.compile();
                    }

                    /*
                     * Put together the result, including the shapes, the
                     * original query, and the labels.
                     */
                    result = new Result(request.getQuery(), newOutlineShape,
                            newShadedShape, newLabels);

                } catch (Throwable e) {

                    result = new Result(request.getQuery(), e);

                } finally {

                    /*
                     * Throw away any previous result, and queue up this new
                     * one. Then get the enclosing class to refresh its display.
                     */
                    if (result != null) {
                        if (resultQueue.size() == QUEUE_LIMIT) {
                            resultQueue.poll();
                        }
                        resultQueue.add(result);
                        issueRefresh();
                    }
                }

                /*
                 * Dequeue the next request for processing.
                 */
                request = requestQueue.poll();
            }

            return Status.OK_STATUS;
        }

        @Override
        protected void canceling() {
            super.canceling();
            this.canceled = true;
        }
    }

    // Private Variables

    /**
     * Original geometries.
     */
    private final List<Geometry> originalGeometries;

    /**
     * Selected geometries.
     */
    private List<Geometry> selectedGeometries;

    /**
     * Outline shape for the resource.
     */
    private IWireframeShape outlineShape;

    /**
     * Shaded shape for the resource.
     */
    private IShadedShape shadedShape;

    /**
     * Labels for the resource.
     */
    private List<LabelNode> labels;

    /**
     * Map of shaded fields to RGB color values.
     */
    private Map<Object, RGB> colorMap;

    /**
     * Available levels.
     */
    private double[] levels;

    /**
     * Simple level when last checked.
     */
    private double lastSimpLev;

    /**
     * Label field when last checked.
     */
    private String lastLabelField;

    /**
     * Shading field when last checked.
     */
    private String lastShadingField;

    /**
     * Query job to submit the request and get the result.
     */
    private final MapQueryJob queryJob;

    /**
     * Geometry type as a string.
     */
    protected String geometryType;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param data
     *            Data about this resource.
     * @param loadProperties
     *            Load properties for the resource.
     */
    public SelectByAreaDbMapResource(SelectByAreaDbMapResourceData data,
            LoadProperties loadProperties) {
        super(data, loadProperties);
        originalGeometries = new CopyOnWriteArrayList<Geometry>();
        selectedGeometries = Collections.emptyList();
        queryJob = new MapQueryJob();
    }

    // Public Methods

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        super.project(crs);
        if (this.outlineShape != null) {
            this.outlineShape.dispose();
            this.outlineShape = null;
        }
        if (this.shadedShape != null) {
            this.shadedShape.dispose();
            this.shadedShape = null;
        }
    }

    /**
     * Get the geometry for this layer in which the specified coordinates are
     * found.
     * 
     * @param coordinates
     *            Latitude-longitude coordinates for which to find the enclosing
     *            geometry.
     * @return Enclosing geometry, or <code>null</code> if no geometry encloses
     *         the coordinates.
     */
    public Geometry getGeometryContainingLocation(Coordinate coordinates) {
        Point point = new GeometryFactory().createPoint(coordinates);
        for (Geometry geometry : originalGeometries) {
            if (geometry.contains(point)) {
                return geometry;
            }
        }
        return null;
    }

    /**
     * Set the currently selected geometries to those specified.
     * 
     * @param selectedGeometries
     *            New selected geometries.
     */
    public void setSelectedGeometries(List<Geometry> selectedGeometries) {
        this.selectedGeometries = selectedGeometries;
    }

    // Protected Methods

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        getCapability(ShadeableCapability.class).setAvailableShadingFields(
                getLabelFields().toArray(new String[0]));
    }

    @Override
    protected void disposeInternal() {
        if (outlineShape != null) {
            outlineShape.dispose();
        }
        if (shadedShape != null) {
            shadedShape.dispose();
        }
        super.disposeInternal();
    }

    @Override
    protected void paintInternal(IGraphicsTarget target,
            PaintProperties paintProps) throws VizException {
        PixelExtent screenExtent = (PixelExtent) paintProps.getView()
                .getExtent();

        /*
         * Get the current simple level.
         */
        double simpLev = getSimpLev();

        /*
         * Determine whether or not the resource is labeled.
         */
        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        boolean isLabeled = (labelField != null);

        /*
         * Get the shading field and compile shading geometries.
         */
        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        IShadedShape groupShape = null;
        if (selectedGeometries.size() > 0) {
            groupShape = target.createShadedShape(false, getDescriptor()
                    .getGridGeometry());
            JTSCompiler groupCompiler = new JTSCompiler(groupShape, null,
                    getDescriptor());
            JTSCompiler.JTSGeometryData jtsGeometryData = groupCompiler
                    .createGeometryData();
            jtsGeometryData.setGeometryColor(RGB_COLOR_WHITE);
            jtsGeometryData.setPointStyle(PointStyle.CROSS);
            for (Geometry geometry : selectedGeometries) {
                groupCompiler.handle((Geometry) geometry.clone(),
                        jtsGeometryData);
            }
            groupShape.compile();
        }

        /*
         * Determine whether or not shading is needed.
         */
        boolean isShaded = (isPolygonal() && (shadingField != null));

        /*
         * If the simple level is smaller, or the label has changed, or the
         * shading has changed, or the extent has changed, and zooming is not
         * occurring, submit a request for geometries.
         */
        if (((simpLev < lastSimpLev)
                || (isLabeled && (labelField.equals(lastLabelField) == false))
                || (isShaded && (shadingField.equals(lastShadingField) == false))
                || (lastExtent == null) || (lastExtent.getEnvelope().contains(
                clipToProjExtent(screenExtent).getEnvelope()) == false))
                && (paintProps.isZooming() == false)) {
            PixelExtent expandedExtent = getExpandedExtent(screenExtent);
            String query = buildQuery(expandedExtent, simpLev);
            queryJob.request(target, query, labelField, shadingField);
            lastExtent = expandedExtent;
            lastSimpLev = simpLev;
            lastLabelField = labelField;
            lastShadingField = shadingField;
        }

        /*
         * Get the result from the query.
         */
        MapQueryJob.Result result = queryJob.getLatestResult();
        if (result != null) {

            /*
             * If the query failed, an error has occurred.
             */
            if (result.getFailureCause() != null) {
                lastExtent = null;
                throw new VizException("Error processing map query request: "
                        + result.getQuery(), result.getFailureCause());
            }

            /*
             * Dispose of the outline and shaded shapes, if any, and get the new
             * result's shapes and labels.
             */
            if (outlineShape != null) {
                outlineShape.dispose();
            }
            if (shadedShape != null) {
                shadedShape.dispose();
            }
            outlineShape = result.getOutlineShape();
            shadedShape = result.getShadedShape();
            labels = result.getLabels();
        }

        /*
         * Get the transparency component of the layer.
         */
        float alpha = paintProps.getAlpha();

        /*
         * Draw the group shape if one is available, and the shaded shape if
         * available and appropriate.
         */
        if ((groupShape != null) && groupShape.isDrawable()) {
            target.drawShadedShape(groupShape, alpha);
        }
        if ((shadedShape != null) && shadedShape.isDrawable() && isShaded) {
            target.drawShadedShape(shadedShape, alpha);
        }

        /*
         * Draw the outline shape if one is available.
         */
        if (getCapability(OutlineCapability.class).isOutlineOn()) {
            if ((outlineShape != null) && outlineShape.isDrawable()) {
                target.drawWireframeShape(outlineShape,
                        getCapability(ColorableCapability.class).getColor(),
                        getCapability(OutlineCapability.class)
                                .getOutlineWidth(),
                        getCapability(OutlineCapability.class).getLineStyle());
            } else if (outlineShape == null) {
                issueRefresh();
            }
        }

        /*
         * If there are labels and they should be showing, draw them.
         */
        double labelMagnification = getCapability(MagnificationCapability.class)
                .getMagnification();
        if ((labels != null) && isLabeled && (labelMagnification != 0)) {

            /*
             * Create the font if not already done. This member variable is part
             * of the superclass, and is disposed of by the latter if necessary,
             * so no need to do that in this class.
             */
            if (font == null) {
                font = target
                        .initializeFont(target.getDefaultFont().getFontName(),
                                (float) (10 * labelMagnification), null);
            }
            double screenToWorldRatio = paintProps.getView().getExtent()
                    .getWidth()
                    / paintProps.getCanvasBounds().width;

            double offsetX = getCapability(LabelableCapability.class)
                    .getxOffset() * screenToWorldRatio;
            double offsetY = getCapability(LabelableCapability.class)
                    .getyOffset() * screenToWorldRatio;

            /*
             * Get the color to use, and the extent in which the labels must
             * fall to be drawn.
             */
            RGB color = getCapability(ColorableCapability.class).getColor();
            IExtent extent = paintProps.getView().getExtent();

            /*
             * Create a list to hold the drawable strings, and one to hold their
             * extents.
             */
            List<DrawableString> strings = new ArrayList<DrawableString>(
                    labels.size());
            List<IExtent> extents = new ArrayList<IExtent>();

            /*
             * Iterate through the labels, including any that are within the
             * display extent but that do not intersect the previously drawn
             * labels with the same text.
             */
            String lastLabel = null;
            for (LabelNode node : labels) {
                if (extent.contains(node.getLocation())) {

                    /*
                     * Construct the drawable.
                     */
                    DrawableString string = new DrawableString(node.getLabel(),
                            color);
                    string.setCoordinates(node.getLocation()[0] + offsetX,
                            node.getLocation()[1] - offsetY);
                    string.font = font;
                    string.horizontalAlignment = HorizontalAlignment.CENTER;
                    string.verticallAlignment = VerticalAlignment.MIDDLE;
                    boolean add = true;

                    /*
                     * Get the drawable's extent.
                     */
                    IExtent strExtent = new PixelExtent(
                            node.getLocation()[0],
                            node.getLocation()[0]
                                    + (node.getBoundingBox().getWidth() * screenToWorldRatio),
                            node.getLocation()[1],
                            node.getLocation()[1]
                                    + ((node.getBoundingBox().getHeight() - node
                                            .getBoundingBox().getY()) * screenToWorldRatio));

                    /*
                     * If this label has the same text as the previous one,
                     * iterate through any saved extents of previous ones with
                     * the same text in order to determine if this new label's
                     * extent intersects any of them. If it does, do not draw
                     * the label. If this label is not the same as the previous
                     * one, clear the saved extents, as no comparison to labels
                     * that have different text should be done the next time
                     * around.
                     */
                    if ((lastLabel != null)
                            && lastLabel.equals(node.getLabel())) {
                        for (IExtent ext : extents) {
                            if (ext.intersects(strExtent)) {
                                add = false;
                                break;
                            }
                        }
                    } else {
                        extents.clear();
                    }

                    /*
                     * Remember this label for next time, and its extent.
                     */
                    lastLabel = node.getLabel();
                    extents.add(strExtent);

                    /*
                     * Add the label if appropriate.
                     */
                    if (add) {
                        strings.add(string);
                    }
                }
            }

            target.drawStrings(strings);
        }
    }

    @Override
    protected double[] getLevels() {

        /*
         * Get the levels if they are not cached.
         */
        if (levels == null) {
            try {

                /*
                 * Put together the levels query and submit it.
                 */
                int p = resourceData.getTable().indexOf('.');
                String schema = resourceData.getTable().substring(0, p);
                String table = resourceData.getTable().substring(p + 1);
                StringBuilder query = new StringBuilder(
                        "SELECT f_geometry_column FROM public.geometry_columns WHERE f_table_schema='");
                query.append(schema);
                query.append("' AND f_table_name='");
                query.append(table);
                query.append("' AND f_geometry_column LIKE '");
                query.append(resourceData.getGeomField());
                query.append("_%';");
                List<Object[]> results = DirectDbQuery
                        .executeQuery(query.toString(), MAPS_DATABASE_NAME,
                                QueryLanguage.SQL);

                /*
                 * Get the results and interpret them as floating point numbers.
                 */
                levels = new double[results.size()];
                int i = 0;
                for (Object[] obj : results) {
                    String s = ((String) obj[0]).substring(
                            resourceData.getGeomField().length() + 1).replace(
                            '_', '.');
                    levels[i++] = Double.parseDouble(s);
                }

                /*
                 * Sort the levels in increasing order.
                 */
                Arrays.sort(levels);
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying available levels", e);
            }

        }
        return levels;
    }

    @Override
    protected String getGeometryType() {

        /*
         * Get the geometry type if it is not cached.
         */
        if (geometryType == null) {
            try {

                /*
                 * Put together the query and submit it.
                 */
                int p = resourceData.getTable().indexOf('.');
                String schema = resourceData.getTable().substring(0, p);
                String table = resourceData.getTable().substring(p + 1);
                StringBuilder query = new StringBuilder(
                        "SELECT type FROM geometry_columns WHERE f_table_schema='");
                query.append(schema);
                query.append("' AND f_table_name='");
                query.append(table);
                query.append("' LIMIT 1;");
                List<Object[]> results = DirectDbQuery
                        .executeQuery(query.toString(), MAPS_DATABASE_NAME,
                                QueryLanguage.SQL);

                /*
                 * Get the type from the result.
                 */
                geometryType = (String) results.get(0)[0];
            } catch (Throwable e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying geometry type", e);
            }
        }

        return geometryType;
    }

    @Override
    protected boolean isLineal() {
        return getGeometryType().endsWith("LINESTRING");
    }

    @Override
    protected boolean isPolygonal() {
        return getGeometryType().endsWith("POLYGON");
    }

    // Private Methods

    /**
     * Build the query to be submitted to get the geometries.
     * 
     * @param extent
     *            Pixel extent for which the query should apply.
     * @param simpLev
     *            Simple (first) level.
     * @return Query string.
     * @throws VizException
     *             If an error occurs when attempting to transform the extent to
     *             latitude-longitude coordinates.
     */
    private String buildQuery(PixelExtent extent, double simpLev)
            throws VizException {

        /*
         * Transform the provided extent to latitude-longitude coordinates.
         */
        Envelope envelope = null;
        try {
            Envelope e = getDescriptor().pixelToWorld(extent,
                    descriptor.getCRS());
            ReferencedEnvelope ref = new ReferencedEnvelope(e, getDescriptor()
                    .getCRS());
            envelope = ref.transform(MapUtil.LATLON_PROJECTION, true);
        } catch (Exception e) {
            throw new VizException("Error transforming extent", e);
        }

        /*
         * Put together the geometry field from the geometry field and the
         * simple level.
         */
        DecimalFormat decimalFormat = new DecimalFormat("0.######");
        String suffix = "_"
                + StringUtils.replaceChars(decimalFormat.format(simpLev), '.',
                        '_');
        String geometryField = resourceData.getGeomField() + suffix;

        /*
         * Ensure the query gets the geometry field.
         */
        StringBuilder query = new StringBuilder("SELECT AsBinary(");
        query.append(geometryField);
        query.append(") as ");
        query.append(geometryField);

        /*
         * Ensure the query gets any additional columns from the resource data.
         */
        List<String> additionalColumns = new ArrayList<String>();
        if (resourceData.getColumns() != null) {
            for (ColumnDefinition column : resourceData.getColumns()) {
                query.append(", ");
                query.append(column);
                additionalColumns.add(column.getName());
            }
        }

        /*
         * Ensure the query fetches the label field.
         */
        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        if (labelField != null && !additionalColumns.contains(labelField)) {
            query.append(", ");
            query.append(labelField);
        }

        /*
         * Ensure the query fetches the shading field.
         */
        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        if (shadingField != null && !additionalColumns.contains(shadingField)) {
            query.append(", ");
            query.append(shadingField);
        }

        /*
         * Indicate that the query should get these from the geometry table.
         */
        query.append(" FROM ");
        query.append(resourceData.getTable());

        /*
         * Add the geospatial constraint on the results.
         */
        query.append(" WHERE ");
        query.append(getGeospatialConstraint(geometryField, envelope));

        /*
         * Add any additional constraints.
         */
        if (resourceData.getConstraints() != null) {
            for (String constraint : resourceData.getConstraints()) {
                query.append(" AND ");
                query.append(constraint);
            }
        }

        query.append(';');
        return query.toString();
    }

    /**
     * Get the specified envelope as a geospatial constraint on the specified
     * geometry field.
     * 
     * @param geometryField
     *            Field to be constrained.
     * @param envelope
     *            Envelope describing the geospatial bounds to be used.
     * @return Query-ready geospatial constraint description.
     */
    private String getGeospatialConstraint(String geometryField,
            Envelope envelope) {
        String geoConstraint = String.format(
                "%s && ST_SetSrid('BOX3D(%f %f, %f %f)'::box3d,4326)",
                geometryField, envelope.getMinX(), envelope.getMinY(),
                envelope.getMaxX(), envelope.getMaxY());
        return geoConstraint;
    }

    /**
     * Get the simple (first) level.
     * 
     * @return Simple level.
     */
    private double getSimpLev() {
        double[] levels = getLevels();
        return levels[0];
    }
}
