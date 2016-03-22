/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * Description: Property, with type <code>P</code>, that varies over time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 12, 2016   15676    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class TemporallyVariantProperty<P> {

    // Private Variables

    /**
     * Default property value; may be <code>null</code>.
     */
    private final P defaultProperty;

    /**
     * Map of time ranges to their corresponding property values. It may be
     * <code>null</code> if this object has only a default property.
     */
    private RangeMap<Date, P> propertiesForTimeRanges = null;

    // Package Constructors

    /**
     * Construct a standard instance.
     * 
     * @param defaultProperty
     *            Default property that applies when a specified time lies any
     *            time ranges specified with <code>timeRangedProperties</code>;
     *            may be <code>null</code>.
     */
    TemporallyVariantProperty(P defaultProperty) {
        this.defaultProperty = defaultProperty;
    }

    // Public Methods

    /**
     * Get the property value for the specified time.
     * 
     * @param time
     *            Time for which to fetch the property.
     * @return Property value for this time, or <code>null</code> if there is no
     *         property value associated with this time.
     */
    public P getProperty(Date time) {
        P property = (propertiesForTimeRanges == null ? null
                : propertiesForTimeRanges.get(time));
        return (property == null ? defaultProperty : property);
    }

    @Override
    public boolean equals(Object other) {

        /*
         * Both must be temporally variant properties to be potentially
         * equivalent.
         */
        if (other instanceof TemporallyVariantProperty == false) {
            return false;
        }
        TemporallyVariantProperty<?> otherProperty = (TemporallyVariantProperty<?>) other;

        /*
         * Compare the default properties. Actual comparison is done with a
         * method that handles the special case of the objects being of type
         * VisualFeature by only comparing their identifiers, since otherwise
         * potentially infinite and certainly wasteful recursion will occur.
         */
        if ((defaultProperty != otherProperty.defaultProperty)
                && ((defaultProperty == null) || (compareAvoidingRecursion(
                        defaultProperty, otherProperty.defaultProperty) == false))) {
            return false;
        }

        /*
         * Compare the maps of time ranges to properties. As with the default
         * property, any VisualFeature property values are compared in a way
         * that avoids recursive comparisons.
         */
        if (propertiesForTimeRanges != otherProperty.propertiesForTimeRanges) {
            if ((propertiesForTimeRanges == null)
                    || (otherProperty.propertiesForTimeRanges == null)) {
                return false;
            }
            Map<Range<Date>, ?> map = propertiesForTimeRanges.asMapOfRanges();
            Map<Range<Date>, ?> otherMap = otherProperty.propertiesForTimeRanges
                    .asMapOfRanges();
            if (map.size() != otherMap.size()) {
                return false;
            }
            for (Map.Entry<Range<Date>, ?> entry : map.entrySet()) {
                if ((otherMap.containsKey(entry.getKey()) == false)
                        || (compareAvoidingRecursion(entry.getValue(),
                                otherMap.get(entry.getKey())) == false)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        long hashCode = getHashCodeAvoidingRecursion(defaultProperty);
        if (propertiesForTimeRanges != null) {
            for (Map.Entry<Range<Date>, P> entry : propertiesForTimeRanges
                    .asMapOfRanges().entrySet()) {
                hashCode += (entry.getKey().hashCode())
                        + getHashCodeAvoidingRecursion(entry.getValue());
            }
        }
        return (int) (hashCode % Integer.MAX_VALUE);
    }

    // Package Methods

    /**
     * Add the specified property value for the specified time range.
     * 
     * @param timeRange
     *            Time range for which the new property value is valid.
     * @param property
     *            Property value associated with the time range; must not be
     *            <code>null</code>.
     */
    void addPropertyForTimeRange(Range<Date> timeRange, P property) {
        if (propertiesForTimeRanges == null) {
            propertiesForTimeRanges = TreeRangeMap.create();
        }
        propertiesForTimeRanges.put(timeRange, property);
    }

    /**
     * Get the default property value specified at
     * {@link #TemporallyVariantProperty(Object) creation time}.
     * 
     * @return Default property value, or <code>null</code> if there is no
     *         default.
     */
    P getDefaultProperty() {
        return defaultProperty;
    }

    /**
     * Get the time-range-bound property values (those added via
     * {@link #addPropertyForTimeRange(Range, Object)} in the form of an
     * unmodifiable mapping of time ranges to property values.
     * 
     * @return Map of time ranges to property values.
     */
    Map<Range<Date>, P> getPropertiesForTimeRanges() {
        return (propertiesForTimeRanges == null ? Collections
                .<Range<Date>, P> emptyMap() : propertiesForTimeRanges
                .asMapOfRanges());
    }

    // Private Methods

    /**
     * Determine whether or not two objects are equivalent while avoiding
     * recursion in the case where the first object is a {@link VisualFeature}
     * (since instances of the latter includes instances of this class, which in
     * turn might hold instances of <code>VisualFeature</code>, and so on).
     * 
     * @param object1
     *            First object to be compared; may be <code>null</code>.
     * @param object2
     *            Second object to be compared; may be <code>null</code>.
     * @return True if the two objects are equivalent or are both
     *         <code>null</code>, false otherwise.
     */
    private boolean compareAvoidingRecursion(Object object1, Object object2) {

        /*
         * If both objects are visual features, just compare their identifiers
         * to determine whether they are equal. Otherwise, compare the two
         * objects normally.
         */
        if (object1 instanceof VisualFeature) {
            if (object2 instanceof VisualFeature == false) {
                return false;
            }
            return ((VisualFeature) object1).getIdentifier().equals(
                    ((VisualFeature) object2).getIdentifier());
        }
        return (object1 == null ? object2 == null : object1.equals(object2));
    }

    /**
     * Get the specified object's hash code, while avoiding recursive calls to
     * {@link #hashCode()} in the case where the object is a
     * {@link VisualFeature} (since instances of the latter includes instances
     * of this class, which in turn might hold instances of
     * <code>VisualFeature</code>, and so on).
     * 
     * @param object
     *            Object for which to generate the hash code, or
     *            <code>null</code>.
     * 
     * @return Hash code of the object, or 0 if the object is <code>null</code>.
     */
    private long getHashCodeAvoidingRecursion(Object object) {
        return (object == null ? 0L
                : (object instanceof VisualFeature ? ((VisualFeature) object)
                        .getIdentifier().hashCode() : object.hashCode()));
    }
}
