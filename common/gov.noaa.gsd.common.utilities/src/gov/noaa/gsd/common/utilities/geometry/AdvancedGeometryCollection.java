/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.utilities.geometry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeTypeAdapter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

import gov.noaa.gsd.common.utilities.IBinarySerializable;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator;
import gov.noaa.gsd.common.utilities.PrimitiveAndStringBinaryTranslator.ByteOrder;

/**
 * Description: Container of one or more instances of {@link IAdvancedGeometry},
 * similar to a JTS {@link GeometryCollection}'s relationship to
 * {@link Geometry}. Note that this class is serializable both in the
 * conventional sense (implementing {@link Serializable}, and in the byte stream
 * sense (implementing {@link IBinarySerializable}).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 02, 2016   15934    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Added center point calculation.
 * Oct 13, 2016   15928    Chris.Golden Fixed bug caused by serialization problems
 *                                      with NaN in coordinates.
 * Feb 01, 2017   15556    Chris.Golden Improved generic parameter usage.
 * Feb 13, 2017   28892    Chris.Golden Changed to implement IBinarySerializable.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@DynamicSerialize
@DynamicSerializeTypeAdapter(factory = AdvancedGeometrySerializationAdapter.class)
public class AdvancedGeometryCollection implements IAdvancedGeometry {

    // Private Static Constants

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1373433503009489337L;

    // Private Variables

    /**
     * Child advanced geometries.
     */
    private final ImmutableList<IAdvancedGeometry> children;

    /**
     * Center point in world coordinates.
     */
    private final Coordinate centerPoint;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param children
     *            Child advanced geometries; must be a non-empty list.
     */
    @JsonCreator
    public AdvancedGeometryCollection(
            @JsonProperty("children") List<? extends IAdvancedGeometry> children) {
        this.children = ImmutableList.<IAdvancedGeometry> copyOf(children);

        /*
         * Calculate the center point of the envelope holding all the child
         * geometries.
         */
        Envelope envelope = null;
        for (IAdvancedGeometry child : children) {
            Envelope childEnvelope = AdvancedGeometryUtilities
                    .getJtsGeometry(child).getEnvelopeInternal();
            if (envelope == null) {
                envelope = childEnvelope;
            } else {
                envelope.expandToInclude(childEnvelope);
            }
        }
        Coordinate center = envelope.centre();
        centerPoint = new Coordinate(center.x, center.y, 0.0);
    }

    /**
     * Construct an instance by deserializing from the specified input stream.
     * It is assumed that the serialization that is being deserialized was
     * produced by {@link #toBinary(OutputStream)}. This constructor must be
     * included because this class implements {@link IBinarySerializable}.
     * 
     * @param bytesInputStream
     *            Byte array input stream from which to deserialize.
     * @throws IOException
     *             If a deserialization error occurs.
     */
    public AdvancedGeometryCollection(ByteArrayInputStream bytesInputStream)
            throws IOException {
        int count = PrimitiveAndStringBinaryTranslator
                .readInteger(bytesInputStream, ByteOrder.BIG_ENDIAN);
        List<IAdvancedGeometry> children = new ArrayList<>(count);
        for (int j = 0; j < count; j++) {
            children.add(AdvancedGeometryBinaryTranslator
                    .deserializeFromBinaryStream(bytesInputStream));
        }
        this.children = ImmutableList.copyOf(children);
        this.centerPoint = new Coordinate(
                PrimitiveAndStringBinaryTranslator.readDouble(bytesInputStream,
                        ByteOrder.BIG_ENDIAN),
                PrimitiveAndStringBinaryTranslator.readDouble(bytesInputStream,
                        ByteOrder.BIG_ENDIAN));
    }

    // Public Methods

    @Override
    public Coordinate getCenterPoint() {
        return new Coordinate(centerPoint);
    }

    /**
     * Get the child advanced geometries.
     * 
     * @return Child advanced geometries.
     */
    public List<IAdvancedGeometry> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof AdvancedGeometryCollection == false) {
            return false;
        }
        AdvancedGeometryCollection otherAdvancedGeometryCollection = (AdvancedGeometryCollection) other;
        return ((children == otherAdvancedGeometryCollection.children)
                || ((children != null) && children
                        .equals(otherAdvancedGeometryCollection.children)));
    }

    @Override
    public int hashCode() {
        return (children == null ? 0 : children.hashCode());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <G extends IAdvancedGeometry> G copyOf() {
        List<IAdvancedGeometry> childrenCopy = new ArrayList<>(children.size());
        for (IAdvancedGeometry child : children) {
            childrenCopy.add(child.copyOf());
        }
        return (G) new AdvancedGeometryCollection(childrenCopy);
    }

    @Override
    public boolean isPunctual() {
        for (IAdvancedGeometry child : children) {
            if (child.isPunctual()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isLineal() {
        for (IAdvancedGeometry child : children) {
            if (child.isLineal()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPolygonal() {
        for (IAdvancedGeometry child : children) {
            if (child.isPolygonal()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPotentiallyCurved() {
        for (IAdvancedGeometry child : children) {
            if (child.isPotentiallyCurved()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Geometry asGeometry(GeometryFactory geometryFactory, double flatness,
            int limit) {
        List<Geometry> geometries = new ArrayList<>(children.size());
        for (IAdvancedGeometry child : children) {
            geometries.add(child.asGeometry(geometryFactory, flatness, limit));
        }
        return geometryFactory.buildGeometry(geometries);
    }

    @Override
    public Coordinate getCentroid(GeometryFactory geometryFactory,
            double flatness, int limit) {

        /*
         * It is not good enough to just average together the child geometry's
         * centroids; this will not take their relative weights into account.
         * Instead, union approximations of all the children together, and take
         * the centroid of the result.
         */
        Geometry geometry = null;
        for (IAdvancedGeometry child : children) {
            Geometry childGeometry = child.asGeometry(geometryFactory, flatness,
                    limit);
            if (geometry == null) {
                geometry = childGeometry;
            } else {
                geometry.union(childGeometry);
            }
        }
        return geometry.getCentroid().getCoordinate();
    }

    @Override
    public boolean isValid() {
        for (IAdvancedGeometry child : children) {
            if (child.isValid() == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getValidityProblemDescription() {
        if (isValid()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean nonEmpty = false;
        for (IAdvancedGeometry child : children) {
            String problem = child.getValidityProblemDescription();
            if (problem != null) {
                if (nonEmpty) {
                    builder.append(", ");
                }
                builder.append(problem);
                nonEmpty = true;
            }
        }
        return builder.toString();
    }

    @Override
    public void toBinary(OutputStream outputStream) throws IOException {
        PrimitiveAndStringBinaryTranslator.writeInteger(children.size(),
                outputStream, ByteOrder.BIG_ENDIAN);
        for (IAdvancedGeometry geometry : children) {
            AdvancedGeometryBinaryTranslator.serializeToBinaryStream(geometry,
                    outputStream);
        }
        PrimitiveAndStringBinaryTranslator.writeDouble(centerPoint.x,
                outputStream, ByteOrder.BIG_ENDIAN);
        PrimitiveAndStringBinaryTranslator.writeDouble(centerPoint.y,
                outputStream, ByteOrder.BIG_ENDIAN);
    }

    @Override
    public String toString() {
        return "COLLECTION(" + Joiner.on(", ").join(children) + ")";
    }
}
