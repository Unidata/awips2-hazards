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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
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
import com.raytheon.uf.viz.core.map.IMapDescriptor;
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
 * Database map resource for Hazard Services select-by-area tool. This loads
 * polygons from the maps database for a specified maps db table. This keeps
 * track of the loaded geometries and the ones selected by the user. The union
 * of these selected geometries forms the basis for a hazard polygon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec.    2011            Bryon.Lawrence   Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class SelectByAreaDbMapResource extends
        AbstractDbMapResource<SelectByAreaDbMapResourceData, MapDescriptor> {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SelectByAreaDbMapResource.class);

    protected class LabelNode {
        private final Rectangle2D rect;

        private final String label;

        private final double[] location;

        public LabelNode(String label, Point c, IGraphicsTarget target) {
            this.label = label;
            this.location = descriptor.worldToPixel(new double[] {
                    c.getCoordinate().x, c.getCoordinate().y });
            DrawableString ds = new DrawableString(label, null);
            ds.font = font;
            rect = target.getStringsBounds(ds);
        }

        /**
         * @return the rect
         */
        public Rectangle2D getRect() {
            return rect;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @return the location
         */
        public double[] getLocation() {
            return location;
        }
    }

    /**
     * 
     * Description: Query job for retrieving data from the AWIPS II geo
     * database. Produces a wireframe shape which can then be rendered on the
     * CAVE display. This job also buffers the individual geometries for use in
     * Hazard Services draw-by-area operations.
     * 
     * <pre>
     * 
     * SOFTWARE HISTORY
     * Date         Ticket#    Engineer    Description
     * ------------ ---------- ----------- --------------------------
     * April 01, 2013          Bryon.Lawrence      Initial creation
     * 
     * </pre>
     * 
     * @author Bryon.Lawrence
     * @version 1.0
     */
    private class MapQueryJob extends Job {

        private static final int QUEUE_LIMIT = 1;

        public class Request {
            Random rand = new Random(System.currentTimeMillis());

            IGraphicsTarget target;

            IMapDescriptor descriptor;

            SelectByAreaDbMapResource rsc;

            String labelField;

            String shadingField;

            String query;

            Map<Object, RGB> colorMap;

            Request(IGraphicsTarget target, IMapDescriptor descriptor,
                    SelectByAreaDbMapResource rsc, String query,
                    String labelField, String shadingField,
                    Map<Object, RGB> colorMap) {
                this.target = target;
                this.descriptor = descriptor;
                this.rsc = rsc;
                this.query = query;
                this.labelField = labelField;
                this.shadingField = shadingField;
                this.colorMap = colorMap;
            }

            RGB getColor(Object key) {
                if (colorMap == null) {
                    colorMap = new HashMap<Object, RGB>();
                }
                RGB color = colorMap.get(key);
                if (color == null) {
                    color = new RGB(rand.nextInt(206) + 50,
                            rand.nextInt(206) + 50, rand.nextInt(206) + 50);
                    colorMap.put(key, color);
                }

                return color;
            }
        }

        public class Result {
            public IWireframeShape outlineShape;

            public List<LabelNode> labels;

            public IShadedShape shadedShape;

            public Map<Object, RGB> colorMap;

            public boolean failed;

            public Throwable cause;

            public String query;

            private Result(String query) {
                this.query = query;
                failed = true;
            }
        }

        private final ArrayBlockingQueue<Request> requestQueue = new ArrayBlockingQueue<Request>(
                QUEUE_LIMIT);

        private final ArrayBlockingQueue<Result> resultQueue = new ArrayBlockingQueue<Result>(
                QUEUE_LIMIT);

        private boolean canceled;

        public MapQueryJob() {
            super("Retrieving map...");
        }

        public void request(IGraphicsTarget target, IMapDescriptor descriptor,
                SelectByAreaDbMapResource rsc, String query, String labelField,
                String shadingField, Map<Object, RGB> colorMap) {
            if (requestQueue.size() == QUEUE_LIMIT) {
                requestQueue.poll();
            }
            requestQueue.add(new Request(target, descriptor, rsc, query,
                    labelField, shadingField, colorMap));

            this.cancel();
            this.schedule();
        }

        public Result getLatestResult() {
            return resultQueue.poll();
        }

        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
         * IProgressMonitor)
         */
        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Request req = requestQueue.poll();
            while (req != null) {
                Result result = new Result(req.query);
                try {
                    QueryResult mappedResult = DirectDbQuery
                            .executeMappedQuery(req.query, "maps",
                                    QueryLanguage.SQL);

                    // Clear the corresponding geometry map.
                    SelectByAreaDbMapResource.this.getGeometryMap().clear();

                    IWireframeShape newOutlineShape = req.target
                            .createWireframeShape(false, req.descriptor, 0.0f);

                    List<LabelNode> newLabels = new ArrayList<LabelNode>();

                    IShadedShape newShadedShape = null;
                    if (req.shadingField != null) {
                        newShadedShape = req.target.createShadedShape(false,
                                req.descriptor.getGridGeometry(), true);
                    }

                    JTSCompiler jtsCompiler = new JTSCompiler(newShadedShape,
                            newOutlineShape, req.descriptor, PointStyle.CROSS);

                    List<Geometry> resultingGeoms = new ArrayList<Geometry>(
                            mappedResult.getResultCount());
                    int numPoints = 0;
                    WKBReader wkbReader = new WKBReader();
                    for (int i = 0; i < mappedResult.getResultCount(); i++) {
                        if (canceled) {
                            canceled = false;
                            result = null;
                            return Status.CANCEL_STATUS;
                        }
                        Geometry g = null;
                        Object obj = mappedResult.getRowColumnValue(i, 0);
                        if (obj instanceof byte[]) {
                            byte[] wkb = (byte[]) obj;
                            g = wkbReader.read(wkb);
                        } else {
                            statusHandler.handle(Priority.ERROR,
                                    "Expected byte[] received "
                                            + obj.getClass().getName() + ": "
                                            + obj.toString() + "\n  query=\""
                                            + req.query + "\"");
                        }

                        obj = null;
                        if (req.labelField != null) {
                            obj = mappedResult.getRowColumnValue(i,
                                    req.labelField.toLowerCase());
                        }

                        if (obj != null && g != null) {
                            String label;
                            if (obj instanceof BigDecimal) {
                                label = Double.toString(((Number) obj)
                                        .doubleValue());
                            } else {
                                label = obj.toString();
                            }
                            int numGeometries = g.getNumGeometries();
                            List<Geometry> gList = new ArrayList<Geometry>(
                                    numGeometries);
                            for (int polyNum = 0; polyNum < numGeometries; polyNum++) {
                                Geometry poly = g.getGeometryN(polyNum);
                                gList.add(poly);
                            }
                            // Sort polygons in g so biggest comes first.
                            Collections.sort(gList, new Comparator<Geometry>() {
                                @Override
                                public int compare(Geometry g1, Geometry g2) {
                                    return (int) Math.signum(g2.getEnvelope()
                                            .getArea()
                                            - g1.getEnvelope().getArea());
                                }
                            });

                            for (Geometry poly : gList) {
                                Point point = poly.getInteriorPoint();
                                if (point.getCoordinate() != null) {
                                    LabelNode node = new LabelNode(label,
                                            point, req.target);
                                    newLabels.add(node);
                                }
                            }
                        }

                        if (g != null) {
                            numPoints += g.getNumPoints();
                            resultingGeoms.add(g);

                            if (req.shadingField != null) {
                                g.setUserData(mappedResult.getRowColumnValue(i,
                                        req.shadingField.toLowerCase()));
                            }

                            // Need to make a copy. Otherwise, the geometry will
                            // be compiled.
                            SelectByAreaDbMapResource.this.getGeometryMap()
                                    .add((Geometry) g.clone());
                        }
                    }

                    newOutlineShape.allocate(numPoints);

                    for (Geometry g : resultingGeoms) {
                        RGB color = null;
                        Object shadedField = g.getUserData();
                        if (shadedField != null) {
                            color = req.getColor(shadedField);
                        }

                        try {
                            jtsCompiler.handle(g, color);
                        } catch (VizException e) {
                            statusHandler.handle(Priority.PROBLEM,
                                    "Error reprojecting map outline", e);
                        }
                    }

                    newOutlineShape.compile();

                    if (req.shadingField != null) {
                        newShadedShape.compile();
                    }

                    result.outlineShape = newOutlineShape;
                    result.labels = newLabels;
                    result.shadedShape = newShadedShape;
                    result.colorMap = req.colorMap;
                    result.failed = false;

                } catch (Throwable e) {
                    result.cause = e;
                } finally {
                    if (result != null) {
                        if (resultQueue.size() == QUEUE_LIMIT) {
                            resultQueue.poll();
                        }
                        resultQueue.add(result);
                        req.rsc.issueRefresh();
                    }
                }

                req = requestQueue.poll();
            }

            return Status.OK_STATUS;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.jobs.Job#canceling()
         */
        @Override
        protected void canceling() {
            super.canceling();

            this.canceled = true;
        }
    }

    // List of the original geometries.
    private List<Geometry> geometryMap;

    // List of the selected geometries
    private List<Geometry> selectedGeometries;

    // The shapes for the colored groups
    protected ArrayList<IShadedShape> groupShapes;

    public static final int NO_GROUP = -1;

    protected IWireframeShape outlineShape;

    protected List<LabelNode> labels;

    protected IShadedShape shadedShape;

    protected Map<Object, RGB> colorMap;

    protected double[] levels;

    protected double lastSimpLev;

    protected String lastLabelField;

    protected String lastShadingField;

    private final MapQueryJob queryJob;

    protected String geometryType;

    public SelectByAreaDbMapResource(SelectByAreaDbMapResourceData data,
            LoadProperties loadProperties) {
        super(data, loadProperties);
        groupShapes = new ArrayList<IShadedShape>();
        geometryMap = new ArrayList<Geometry>();
        selectedGeometries = new ArrayList<Geometry>();
        queryJob = new MapQueryJob();
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

        // Let the IHIS layer know that this layer is gone.
        // HazardServicesAppBuilder ihisLayer =
        // HazardServicesAppBuilder.getCurrentInstance();
        //
        // if ( ihisLayer != null )
        // {
        // ihisLayer.dbMapResourceUnloaded();
        // }

    }

    @Override
    protected void initInternal(IGraphicsTarget target) throws VizException {
        super.initInternal(target);
        getCapability(ShadeableCapability.class).setAvailableShadingFields(
                getLabelFields().toArray(new String[0]));
    }

    protected String buildQuery(PixelExtent extent, double simpLev)
            throws VizException {

        Envelope env = null;
        try {
            Envelope e = descriptor.pixelToWorld(extent, descriptor.getCRS());
            ReferencedEnvelope ref = new ReferencedEnvelope(e,
                    descriptor.getCRS());
            env = ref.transform(MapUtil.LATLON_PROJECTION, true);
        } catch (Exception e) {
            throw new VizException("Error transforming extent", e);
        }

        DecimalFormat df = new DecimalFormat("0.######");
        String suffix = "_"
                + StringUtils.replaceChars(df.format(simpLev), '.', '_');

        String geometryField = resourceData.getGeomField() + suffix;

        // get the geometry field
        StringBuilder query = new StringBuilder("SELECT AsBinary(");
        query.append(geometryField);
        query.append(") as ");
        query.append(geometryField);

        // add any additional columns
        List<String> additionalColumns = new ArrayList<String>();
        if (resourceData.getColumns() != null) {
            for (ColumnDefinition column : resourceData.getColumns()) {
                query.append(", ");
                query.append(column);

                additionalColumns.add(column.getName());
            }
        }

        // add the label field
        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        if (labelField != null && !additionalColumns.contains(labelField)) {
            query.append(", ");
            query.append(labelField);
        }

        // add the shading field
        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();
        if (shadingField != null && !additionalColumns.contains(shadingField)) {
            query.append(", ");
            query.append(shadingField);
        }

        // add the geometry table
        query.append(" FROM ");
        query.append(resourceData.getTable());

        // add the geospatial constraint
        query.append(" WHERE ");
        query.append(getGeospatialConstraint(geometryField, env));

        // add any additional constraints
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
     * @return
     */
    protected Object getGeospatialConstraint(String geometryField, Envelope env) {
        // create the geospatial constraint from the envelope
        String geoConstraint = String.format(
                "%s && ST_SetSrid('BOX3D(%f %f, %f %f)'::box3d,4326)",
                geometryField, env.getMinX(), env.getMinY(), env.getMaxX(),
                env.getMaxY());
        return geoConstraint;
    }

    /**
     * @param dpp
     * @return
     */
    protected double getSimpLev(double dpp) {
        double[] levels = getLevels();
        return levels[0];
    }

    @Override
    protected void paintInternal(IGraphicsTarget aTarget,
            PaintProperties paintProps) throws VizException {
        PixelExtent screenExtent = (PixelExtent) paintProps.getView()
                .getExtent();

        // compute an estimate of degrees per pixel
        double yc = screenExtent.getCenter()[1];
        double x1 = screenExtent.getMinX();
        double x2 = screenExtent.getMaxX();
        double[] c1 = descriptor.pixelToWorld(new double[] { x1, yc });
        double[] c2 = descriptor.pixelToWorld(new double[] { x2, yc });
        Rectangle canvasBounds = paintProps.getCanvasBounds();
        int screenWidth = canvasBounds.width;
        double dppX = Math.abs(c2[0] - c1[0]) / screenWidth;

        double simpLev = getSimpLev(dppX);

        String labelField = getCapability(LabelableCapability.class)
                .getLabelField();
        boolean isLabeled = labelField != null;

        String shadingField = getCapability(ShadeableCapability.class)
                .getShadingField();

        // Draw shaded geometries...
        IShadedShape groupShape = null;

        if (selectedGeometries.size() > 0) {
            groupShape = aTarget.createShadedShape(false,
                    descriptor.getGridGeometry(), true);
            JTSCompiler groupCompiler = new JTSCompiler(groupShape, null,
                    descriptor, PointStyle.CROSS);

            for (Geometry geometry : selectedGeometries) {
                groupCompiler.handle((Geometry) geometry.clone(), new RGB(255,
                        255, 255));
            }

            groupShape.compile();

        }

        boolean isShaded = isPolygonal() && shadingField != null;

        if (simpLev < lastSimpLev
                || (isLabeled && !labelField.equals(lastLabelField))
                || (isShaded && !shadingField.equals(lastShadingField))
                || lastExtent == null
                || !lastExtent.getEnvelope().contains(
                        clipToProjExtent(screenExtent).getEnvelope())) {
            if (!paintProps.isZooming()) {
                PixelExtent expandedExtent = getExpandedExtent(screenExtent);
                String query = buildQuery(expandedExtent, simpLev);
                queryJob.request(aTarget, descriptor, this, query, labelField,
                        shadingField, colorMap);
                lastExtent = expandedExtent;
                lastSimpLev = simpLev;
                lastLabelField = labelField;
                lastShadingField = shadingField;
            }
        }

        MapQueryJob.Result result = queryJob.getLatestResult();
        if (result != null) {
            if (result.failed) {
                lastExtent = null; // force to re-query when re-enabled
                throw new VizException("Error processing map query request: "
                        + result.query, result.cause);
            }
            if (outlineShape != null) {
                outlineShape.dispose();
            }

            if (shadedShape != null) {
                shadedShape.dispose();
            }
            outlineShape = result.outlineShape;
            labels = result.labels;
            shadedShape = result.shadedShape;
            colorMap = result.colorMap;
        }

        float alpha = paintProps.getAlpha();

        if ((groupShape != null) && (groupShape.isDrawable())) {
            aTarget.drawShadedShape(groupShape, alpha);
        }

        if (shadedShape != null && shadedShape.isDrawable() && isShaded) {
            aTarget.drawShadedShape(shadedShape, alpha);
        }

        if (outlineShape != null && outlineShape.isDrawable()
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            aTarget.drawWireframeShape(outlineShape,
                    getCapability(ColorableCapability.class).getColor(),
                    getCapability(OutlineCapability.class).getOutlineWidth(),
                    getCapability(OutlineCapability.class).getLineStyle());
        } else if (outlineShape == null
                && getCapability(OutlineCapability.class).isOutlineOn()) {
            issueRefresh();
        }

        double labelMagnification = getCapability(MagnificationCapability.class)
                .getMagnification();

        if (labels != null && isLabeled && labelMagnification != 0) {
            if (font == null) {
                font = aTarget
                        .initializeFont(aTarget.getDefaultFont().getFontName(),
                                (float) (10 * labelMagnification), null);
            }
            double screenToWorldRatio = paintProps.getView().getExtent()
                    .getWidth()
                    / paintProps.getCanvasBounds().width;

            double offsetX = getCapability(LabelableCapability.class)
                    .getxOffset() * screenToWorldRatio;
            double offsetY = getCapability(LabelableCapability.class)
                    .getyOffset() * screenToWorldRatio;
            RGB color = getCapability(ColorableCapability.class).getColor();
            IExtent extent = paintProps.getView().getExtent();
            List<DrawableString> strings = new ArrayList<DrawableString>(
                    labels.size());
            List<IExtent> extents = new ArrayList<IExtent>();
            String lastLabel = null;
            for (LabelNode node : labels) {
                if (extent.contains(node.location)) {
                    DrawableString string = new DrawableString(node.label,
                            color);
                    string.setCoordinates(node.location[0] + offsetX,
                            node.location[1] - offsetY);
                    string.font = font;
                    string.horizontalAlignment = HorizontalAlignment.CENTER;
                    string.verticallAlignment = VerticalAlignment.MIDDLE;
                    boolean add = true;

                    IExtent strExtent = new PixelExtent(
                            node.location[0],
                            node.location[0]
                                    + (node.rect.getWidth() * screenToWorldRatio),
                            node.location[1], node.location[1]
                                    + ((node.rect.getHeight() - node.rect
                                            .getY()) * screenToWorldRatio));

                    if (lastLabel != null && lastLabel.equals(node.label)) {
                        // check intersection of extents
                        for (IExtent ext : extents) {
                            if (ext.intersects(strExtent)) {
                                add = false;
                                break;
                            }
                        }
                    } else {
                        extents.clear();
                    }
                    lastLabel = node.label;
                    extents.add(strExtent);

                    if (add) {
                        strings.add(string);
                    }
                }
            }

            aTarget.drawStrings(strings);
        }
    }

    @Override
    public void project(CoordinateReferenceSystem crs) throws VizException {
        super.project(crs);

        if (this.outlineShape != null) {
            outlineShape.dispose();
            this.outlineShape = null;
        }

        if (this.shadedShape != null) {
            shadedShape.dispose();
            this.shadedShape = null;
        }
    }

    /**
     * @return the levels
     */
    protected double[] getLevels() {
        if (levels == null) {
            try {
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

                List<Object[]> results = DirectDbQuery.executeQuery(
                        query.toString(), "maps", QueryLanguage.SQL);

                levels = new double[results.size()];
                int i = 0;
                for (Object[] obj : results) {
                    String s = ((String) obj[0]).substring(
                            resourceData.getGeomField().length() + 1).replace(
                            '_', '.');
                    levels[i++] = Double.parseDouble(s);
                }
                Arrays.sort(levels);
            } catch (VizException e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying available levels", e);
            }

        }
        return levels;
    }

    protected String getGeometryType() {
        if (geometryType == null) {
            try {
                int p = resourceData.getTable().indexOf('.');
                String schema = resourceData.getTable().substring(0, p);
                String table = resourceData.getTable().substring(p + 1);
                StringBuilder query = new StringBuilder(
                        "SELECT type FROM geometry_columns WHERE f_table_schema='");
                query.append(schema);
                query.append("' AND f_table_name='");
                query.append(table);
                query.append("' LIMIT 1;");
                List<Object[]> results = DirectDbQuery.executeQuery(
                        query.toString(), "maps", QueryLanguage.SQL);

                geometryType = (String) results.get(0)[0];
            } catch (Throwable e) {
                statusHandler.handle(Priority.PROBLEM,
                        "Error querying geometry type", e);
            }
        }

        return geometryType;
    }

    protected boolean isLineal() {
        return getGeometryType().endsWith("LINESTRING");
    }

    protected boolean isPolygonal() {
        return getGeometryType().endsWith("POLYGON");
    }

    public Geometry clickOnExistingGeometry(Coordinate aLatLon) {

        Point p = new GeometryFactory().createPoint(aLatLon);

        for (Geometry g : geometryMap) {
            if (g.contains(p)) {
                return g;
            }
        }
        return null;
    }

    public void setSelectedGeometries(List<Geometry> selectedGeometries) {
        this.selectedGeometries = selectedGeometries;
    }

    public List<Geometry> getSelectedGeometries() {
        return selectedGeometries;
    }

    /**
     * @param geometryMap
     *            the geometryMap to set
     */
    public void setGeometryMap(List<Geometry> geometryMap) {
        this.geometryMap = geometryMap;
    }

    /**
     * @return the geometryMap
     */
    public List<Geometry> getGeometryMap() {
        return geometryMap;
    }

}
