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
 * Sep 21, 2016   15934    Chris.Golden Added new method to calculate the
 *                                      distance between two lat-lon points
 *                                      using a given unit, and corrected the
 *                                      method used to calculate an offset
 *                                      lat-lon point (it was yielding results
 *                                      with incorrect signs in some cases).
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
     * Get the distance between the specified latitude-longitude coordinates.
     * 
     * @param firstCoordinate
     *            First coordinate.
     * @param secondCoordinate
     *            Second coordinate.
     * @return Distance measured in this unit between the coordinates.
     */
    public double getDistanceBetween(Coordinate firstCoordinate,
            Coordinate secondCoordinate) {

        /*
         * This algorithm is taken from:
         * 
         * http://williams.best.vwh.net/avform.htm#Dist
         * 
         * It is the second version, which has less rounding error for short
         * distances.
         */

        /*
         * Convert the latitudes and longitudes to radians.
         */
        double firstLonRadians = Math.toRadians(firstCoordinate.x);
        double firstLatRadians = Math.toRadians(firstCoordinate.y);
        double secondLonRadians = Math.toRadians(secondCoordinate.x);
        double secondLatRadians = Math.toRadians(secondCoordinate.y);

        /*
         * Perform the conversion, converting back from radians.
         */
        return earthRadius
                * 2.0
                * Math.asin(Math.sqrt(Math.pow(
                        Math.sin((firstLatRadians - secondLatRadians) / 2.0),
                        2.0)
                        + (Math.cos(firstLatRadians)
                                * Math.cos(secondLatRadians) * Math.pow(
                                Math.sin((firstLonRadians - secondLonRadians) / 2.0),
                                2.0))));
    }

    /**
     * Get the latitude-longitude coordinate resulting from offsetting the
     * specified latitude-longitude by the offset with the specified magnitude
     * (measured in this unit) and direction (measured in radians, with
     * <code>0</code> being east, <code>PI / 2</code> being north, etc.
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
         * This algorithm is taken from the function
         * Point_At_Distance_And_Bearing() from the Javascript page:
         * 
         * http://www.gpsvisualizer.com/calculators.js
         * 
         * Which in turn was adapted from the "Sprong" algorithm written by Dale
         * Bickel at the FCC.
         */

        /*
         * Eastern half of circle is true, western half is false.
         */
        boolean isEastern = ((direction <= Math.PI * 0.5) || (direction >= Math.PI * 1.5));

        /*
         * Convert the direction from the original scheme to one in which 0
         * degrees is north, 90 degrees is west, etc.
         */
        direction = (direction + (1.5 * Math.PI)) % (Math.PI * 2.0);

        /*
         * Convert the latitude and longitude to radians.
         */
        double lonRadians = Math.toRadians(coordinate.x);
        double latRadians = Math.toRadians(coordinate.y);

        /*
         * Perform the meat of the calculation.
         */
        double bb = (Math.PI / 2.0) - latRadians;
        double magnitudeRadians = magnitude / earthRadius;

        double sineBb = Math.sin(bb);
        double cosineBb = Math.cos(bb);
        double cosineMagnitudeRadians = Math.cos(magnitudeRadians);

        double cosineAa = (cosineBb * cosineMagnitudeRadians)
                + (sineBb * Math.sin(magnitudeRadians) * Math.cos(direction));
        if (cosineAa < -1.0) {
            cosineAa = -1.0;
        } else if (cosineAa > 1) {
            cosineAa = 1.0;
        }
        double aa = (cosineAa == 1.0 ? 0.0 : Math.acos(cosineAa));

        double cosineC = (cosineMagnitudeRadians - (cosineAa * cosineBb))
                / (Math.sin(aa) * sineBb);
        if (cosineC < -1.0) {
            cosineC = -1.0;
        } else if (cosineC > 1.0) {
            cosineC = 1.0;
        }
        double c = (cosineC == 1.0 ? 0.0 : Math.acos(cosineC));

        double newLatRadians = (Math.PI / 2.0) - aa;
        double newLonRadians = lonRadians + (c * (isEastern ? 1.0 : -1.0));
        if (newLonRadians > Math.PI) {
            newLonRadians -= 2.0 * Math.PI;
        } else if (newLonRadians < Math.PI * -1.0) {
            newLonRadians += 2.0 * Math.PI;
        }

        /*
         * Return the result, converted back to degrees.
         */
        return new Coordinate(Math.toDegrees(newLonRadians),
                Math.toDegrees(newLatRadians));
    }
}
