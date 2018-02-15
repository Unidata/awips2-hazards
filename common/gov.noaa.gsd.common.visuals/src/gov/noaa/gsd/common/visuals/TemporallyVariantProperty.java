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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
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
 * Mar 26, 2016   15676    Chris.Golden Added method to set the property for
 *                                      whichever time range encompasses a
 *                                      given timestamp.
 * May 05, 2016   15676    Chris.Golden Added ability to be serialized to
 *                                      support Thrift serialiation and
 *                                      deserialization. This in turn allows
 *                                      two H.S. instances sharing an edex
 *                                      to see each other's stored events.
 * Jun 23, 2016   19537    Chris.Golden Changed to allow default property
 *                                      values to be altered when setting
 *                                      the property for a specific time
 *                                      and there are no time-range-specific
 *                                      properties.
 * Mar 02, 2018   47312    Chris.Golden Changed to allow the time range
 *                                      enclosing a specified time to be
 *                                      retrieved, if there is a property
 *                                      value defined for that time range.
 * May 22, 2018   15561    Chris.Golden Added ability to determine whether
 *                                      a property value exists for any
 *                                      time range at all, and added
 *                                      implementation of toString().
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class TemporallyVariantProperty<P extends Serializable>
        implements Serializable {

    // Public Static Constants

    /**
     * Default time range, that is, the time range returned by
     * {@link #getTimeRange(Date)} if the time range to be returned is for the
     * default property.
     */
    public static final Range<Date> DEFAULT_TIME_RANGE = Range.all();

    // Private Static Constants

    /**
     * Serialization version UID.
     */
    private static final long serialVersionUID = -6512977547161493713L;

    // Private Variables

    /**
     * Default property value; may be <code>null</code>.
     */
    private P defaultProperty;

    /**
     * Map of time ranges to their corresponding property values. It may be
     * <code>null</code> if this object has only a default property.
     */
    private RangeMap<Date, P> propertiesForTimeRanges = null;

    // Package-Private Constructors

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
     * Get the time range for the specified time.
     * 
     * @param time
     *            Time for which to fetch the time range.
     * @return Time range enclosing this time, or {@link #DEFAULT_TIME_RANGE} if
     *         there is no range-specific value for this time but there is a
     *         default property value, or <code>null</code> if there is no
     *         range-specific value for this time nor is there a default
     *         property value.
     */
    public Range<Date> getTimeRange(Date time) {
        Range<Date> timeRange = (propertiesForTimeRanges == null ? null
                : propertiesForTimeRanges.getEntry(time).getKey());
        return (timeRange != null ? timeRange
                : (defaultProperty != null ? DEFAULT_TIME_RANGE : null));
    }

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

    /**
     * Determine whether or not the property value exists for any time range at
     * all.
     * 
     * @return <code>true</code> if the property value exists for any time range
     *         at all, <code>false</code> otherwise.
     */
    public boolean isPropertyExistingForAnyTimeRange() {
        return ((defaultProperty != null) || ((propertiesForTimeRanges != null)
                && (propertiesForTimeRanges.asMapOfRanges()
                        .isEmpty() == false)));
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
                && ((defaultProperty == null)
                        || (compareAvoidingRecursion(defaultProperty,
                                otherProperty.defaultProperty) == false))) {
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<Range<Date>, P> valuesForRanges = (propertiesForTimeRanges == null
                ? Collections.<Range<Date>, P> emptyMap()
                : propertiesForTimeRanges.asMapOfRanges());
        if ((defaultProperty == null) && valuesForRanges.isEmpty()) {
            stringBuilder.append("null");
        } else {
            P sampleValue = (defaultProperty != null ? defaultProperty
                    : valuesForRanges.values().iterator().next());
            stringBuilder.append(sampleValue.getClass().getSimpleName());
            stringBuilder.append(": ");
            if (valuesForRanges.isEmpty()) {
                stringBuilder.append(defaultProperty);
            } else {
                stringBuilder.append("default = ");
                stringBuilder.append(defaultProperty);
                stringBuilder.append(", ranges = [ ");
                for (Map.Entry<Range<Date>, P> entry : valuesForRanges
                        .entrySet()) {
                    stringBuilder.append(entry.getKey());
                    stringBuilder.append(" = ");
                    stringBuilder.append(entry.getValue());
                    stringBuilder.append(" ");
                }
                stringBuilder.append("]");
            }
        }
        return stringBuilder.toString();
    }

    // Package-Private Methods

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
     * {@link #TemporallyVariantProperty(Serializable) creation time}.
     * 
     * @return Default property value, or <code>null</code> if there is no
     *         default.
     */
    P getDefaultProperty() {
        return defaultProperty;
    }

    /**
     * Get the time-range-bound property values (those added via
     * {@link #addPropertyForTimeRange(Range, Serializable)} in the form of an
     * unmodifiable mapping of time ranges to property values.
     * 
     * @return Map of time ranges to property values.
     */
    Map<Range<Date>, P> getPropertiesForTimeRanges() {
        return (propertiesForTimeRanges == null
                ? Collections.<Range<Date>, P> emptyMap()
                : propertiesForTimeRanges.asMapOfRanges());
    }

    // Private Methods

    /**
     * Write out the object for serialization purposes. This is required because
     * {@link #propertiesForTimeRanges} is not serializable.
     * 
     * @param stream
     *            Stream to which to write out the object.
     * @throws IOException
     *             If the object cannot be written out.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(defaultProperty);
        if (propertiesForTimeRanges == null) {
            stream.writeObject(null);
        } else {
            ImmutableMap<Range<Date>, P> map = ImmutableMap
                    .copyOf(propertiesForTimeRanges.asMapOfRanges());
            stream.writeObject(map);
        }
    }

    /**
     * Read in the object for deserialization purposes. This is required because
     * {@link #propertiesForTimeRanges} is not serializable.
     * 
     * @param stream
     *            Stream from which to read in the object.
     * @throws IOException
     *             If the object cannot be read in.
     * @throws ClassNotFoundException
     *             If the class of a serialized object cannot be found.
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        defaultProperty = (P) stream.readObject();
        ImmutableMap<Range<Date>, P> map = (ImmutableMap<Range<Date>, P>) stream
                .readObject();
        if (map != null) {
            propertiesForTimeRanges = TreeRangeMap.create();
            for (Map.Entry<Range<Date>, P> entry : map.entrySet()) {
                propertiesForTimeRanges.put(entry.getKey(), entry.getValue());
            }
        }
    }

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
     * @return <code>true</code> if the two objects are equivalent or are both
     *         <code>null</code>, <code>false</code> otherwise.
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
            return ((VisualFeature) object1).getIdentifier()
                    .equals(((VisualFeature) object2).getIdentifier());
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
     * @return Hash code of the object, or <code>0</code> if the object is
     *         <code>null</code>.
     */
    private long getHashCodeAvoidingRecursion(Object object) {
        return (object == null ? 0L
                : (object instanceof VisualFeature
                        ? ((VisualFeature) object).getIdentifier().hashCode()
                        : object.hashCode()));
    }
}
