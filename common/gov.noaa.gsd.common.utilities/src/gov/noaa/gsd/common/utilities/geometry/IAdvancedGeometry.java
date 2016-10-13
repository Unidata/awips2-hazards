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

import java.io.Serializable;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: Interface that must be implemented by any classes providing
 * advanced geometry data (that is, geometry that is beyond the capabilities of
 * the JTS {@link Geometry} subclasses such as curved shapes, or that adds
 * information to an existing <code>Geometry</code>).
 * <p>
 * <strong>Note</strong>: Because implementations must be serializable and
 * deserializable using JSON, each concrete implementation class must either
 * have a full complement of getters and setters, meaning that it will be
 * mutable, or else include a constructor annotated with {@literal @}
 * {@link JsonCreator} that sets each member field to one of the constructor's
 * parameters, each of which must be annotated with {@literal @}
 * {@link JsonProperty}. For example, a <code>Point</code> class with member
 * data <code>x</code> and <code>y</code> each of type <code>double</code> would
 * need the following constructor:
 * </p>
 * 
 * <code><pre> {@literal @}JsonCreator
 * public Point({@literal @}JsonProperty("x") double x, {@literal @}JsonProperty("y") double y) {
 *     this.x = x;
 *     this.y = y;
 * }</pre></code>
 * <p>
 * As an alternative, a static factory method may be specified instead, in which
 * case it would be annotated with {@literal @}{@link JsonFactory}.
 * </p>
 * <p>
 * Also, a concrete implementation class must ensure that its component objects
 * are themselves serializable and deserializable using JSON.
 * </p>
 * <p>
 * Note that the {@literal @}{@link JsonTypeInfo} annotation causes any
 * implementations to, when turned into JSON, include their fully qualified
 * class names under the "class" property.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 31, 2016   15934    Chris.Golden Initial creation.
 * Sep 29, 2016   15928    Chris.Golden Added method to retrieve center point.
 * Oct 13, 2016   15928    Chris.Golden Fixed bug caused by no serialization of
 *                                      center point.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface IAdvancedGeometry extends Serializable {

    /**
     * Get a deep copy of the advanced geometry.
     * 
     * @return Copy of the advanced geometry.
     */
    public <G extends IAdvancedGeometry> G copyOf();

    /**
     * Get the center point of the geometry in world coordinates. Note that the
     * center is not necessarily the same as the centroid; it is the center of
     * the geometry's bounding box.
     * 
     * @return Center point of the geometry in world coordinates.
     */
    public Coordinate getCenterPoint();

    /**
     * Determine whether or not the geometry (or one or more of its component
     * subgeometries, if made up of more than one) is essentially single point.
     * 
     * @return <code>true</code> if the geometry is punctual, <code>false</code>
     *         otherwise.
     */
    @JsonIgnore
    public boolean isPunctual();

    /**
     * Determine whether or not the geometry (or one or more of its component
     * subgeometries, if made up of more than one) is lineal (that is, one or
     * more line segments or curves, but not enclosing anything).
     * 
     * @return <code>true</code> if the geometry is lineal, <code>false</code>
     *         otherwise.
     */
    @JsonIgnore
    public boolean isLineal();

    /**
     * Determine whether or not the geometry (or one or more of its component
     * subgeometries, if made up of more than one) is polygonal (that is, it
     * encloses an area).
     * 
     * @return <code>true</code> if the geometry is polygonal,
     *         <code>false</code> otherwise.
     */
    @JsonIgnore
    public boolean isPolygonal();

    /**
     * Determine whether or not the geometry (or one or more of its component
     * subgeometries, if made up of more than one) may include one or more
     * curves.
     * 
     * @return <code>true</code> if the geometry may include one or more curves,
     *         <code>false</code> otherwise.
     */
    @JsonIgnore
    public boolean isPotentiallyCurved();

    /**
     * Get an approximation of the advanced geometry with all curves flattened.
     * 
     * @param geometryFactory
     *            Geometry factory to be used to create the components of the
     *            resulting geometry.
     * @param flatness
     *            Maximum allowable distance between the control points and the
     *            flattened curve, unless <code>limit</code> is exceeded.
     * @param limit
     *            Maximum number of recursive subdivisions allowed for any
     *            curved segment. For example, if given as <code>10</code>,
     *            there will be no more than 2<sup>10</sup>, or 1024, points per
     *            flattened curve in the result.
     */
    public Geometry asGeometry(GeometryFactory geometryFactory,
            double flatness, int limit);

    /**
     * Get the centroid of the geometry. The centroid may not be exact in all
     * cases, but should be fairly close.
     * 
     * @param geometryFactory
     *            Geometry factory to be used to create any components needed to
     *            compute the centroid.
     * @param flatness
     *            Maximum allowable distance between the control points and any
     *            flattened curves, unless <code>limit</code> is exceeded.
     * @param limit
     *            Maximum number of recursive subdivisions allowed for any
     *            curved segment. For example, if given as <code>10</code>,
     *            there will be no more than 2<sup>10</sup>, or 1024, points per
     *            flattened curve in the result.
     * @return Centroid of the geometry.
     */
    public Coordinate getCentroid(GeometryFactory geometryFactory,
            double flatness, int limit);

    /**
     * Determine whether or not the geometry is valid.
     * 
     * @return <code>true</code> if the geometry is valid, <code>false</code>
     *         otherwise.
     */
    @JsonIgnore
    public boolean isValid();

    /**
     * Get a description of the problem with the validity of the geometry, if
     * any.
     * 
     * @return Description of the problem. If {@link #isValid()} returns
     *         <code>true</code>, this method will return <code>null</code>.
     */
    @JsonIgnore
    public String getValidityProblemDescription();
}
