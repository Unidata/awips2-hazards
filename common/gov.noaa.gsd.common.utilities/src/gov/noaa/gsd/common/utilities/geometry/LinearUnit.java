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

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Various linear units, each providing a method to determine the
 * lat-lon point offset from a starting lat-lon point.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 31, 2016   15934    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public enum LinearUnit {

    FEET("feet", 3959.0 * 5280 * 12), MILES("miles", 3959.0), NAUTICAL_MILES(
            "nauticalMiles", 3440.0), KILOMETERS("kilometers", 6371.0), METERS(
            "meters", 6371000);

    // Private Static Constants

    /**
     * Map of identifiers to instances.
     */
    private static final Map<String, LinearUnit> INSTANCES_FOR_IDENTIFIERS;
    static {
        Map<String, LinearUnit> map = new HashMap<>();
        for (LinearUnit value : values()) {
            map.put(value.toString(), value);
        }
        INSTANCES_FOR_IDENTIFIERS = ImmutableMap.copyOf(map);
    }

    // Private Variables

    /**
     * Identifier of the type.
     */
    private final String identifier;

    /**
     * Radius of the earth in this unit, for the purposes of determing lat-lon
     * offsets.
     */
    private final double earthRadius;

    // Public Static Methods

    /**
     * Get the instance with the specified identifier.
     * 
     * @param identifier
     *            Identifier.
     * @return Instance, or <code>null</code> if there is no instance with the
     *         specified identifier.
     */
    public static LinearUnit getInstanceWithIdentifier(String identifier) {
        return INSTANCES_FOR_IDENTIFIERS.get(identifier);
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this unit.
     * @param earthRadius
     *            Radius of earth in this unit.
     */
    private LinearUnit(String identifier, double earthRadius) {
        this.identifier = identifier;
        this.earthRadius = earthRadius;
    }

    // Public Methods

    @Override
    public String toString() {
        return identifier;
    }

    /**
     * Get the radius of the earth in this unit.
     * 
     * @return Radius of the earth in this unit.
     */
    public double getEarthRadius() {
        return earthRadius;
    }

    /**
     * Get the latitude-longitude coordinate resulting from offsetting the
     * specified latitude-longitude by the offset with the specified magnitude
     * (measured in this unit) and direction (measured in degrees, with
     * <code>0</code> being east, <code>90</code> being north, etc.
     * 
     * @param coordinate
     *            Latitude-longitude coordinate from which to offset.
     * @param magnitude
     *            Magnitude, measured in these units.
     * @param direction
     *            Direction, measured in radians counterclockwise from
     *            horizontal.
     * @return Offset latitude-longitude coordinate.
     */
    public Coordinate getLatLonOffsetBy(Coordinate coordinate,
            double magnitude, double direction) {

        /*
         * This algorithm is taken from:
         * 
         * http://williams.best.vwh.net/avform.htm#LL
         * 
         * It is the more general version, which should work for cases where the
         * longitudinal distance is of any size.
         */

        /*
         * Convert the latitude, longitude, and distance to radians.
         */
        double lonRadians = Math.toRadians(coordinate.x);
        double latRadians = Math.toRadians(coordinate.y);
        double magnitudeRadians = magnitude / earthRadius;

        /*
         * Calculate the sines and cosines that will be used at least twice
         * apiece.
         */
        double sineLatRadians = Math.sin(latRadians);
        double cosineLatRadians = Math.cos(latRadians);
        double sineMagnitudeRadians = Math.sin(magnitudeRadians);
        double cosineMagnitudeRadians = Math.cos(magnitudeRadians);

        /*
         * Perform the conversion.
         */
        double newLatRadians = (Math.asin(sineLatRadians
                * cosineMagnitudeRadians) + (cosineLatRadians
                * sineMagnitudeRadians * Math.cos(direction)));
        double lonDistanceRadians = Math.atan2(
                Math.sin(direction) * sineMagnitudeRadians * cosineLatRadians,
                cosineMagnitudeRadians
                        - (sineLatRadians * Math.sin(newLatRadians)));
        double newLonRadians = ((lonRadians - lonDistanceRadians + Math.PI) % 2.0 * Math.PI)
                - Math.PI;

        /*
         * Return the result, converted back to degrees.
         */
        return new Coordinate(Math.toDegrees(newLonRadians),
                Math.toDegrees(newLatRadians));
    }
}
