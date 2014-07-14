/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

/**
 * Description: Implements a comparable lazily parsed number. The
 * LazilyParsedNumber class does not override the equals method and hence relies
 * on object equality. This class delegates many of its method calls to a Number
 * object, but it does override 'equals' providing a way to compare two
 * ComparableLazilyParsedNumber objects for equality.
 * 
 * Note that this class does not override hashCode which is a violation of Item
 * 9 in the Second Edition of Effective Java. This means that if a
 * ComparableLazilyParsed object is used as a key in a map, there could be big
 * problems. This probably should be remedied to be safe.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 11, 2013            Bryon.Lawrence    Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
@SuppressWarnings("serial")
public final class ComparableLazilyParsedNumber extends Number implements
        Comparable<ComparableLazilyParsedNumber> {
    /**
     * The original number. Many method calls are just delegated to the original
     * Number object. This reduces duplication of logic. It also helps in the
     * maintenance of this object (changes only need to be made in one place).
     */
    private final Number number;

    /**
     * The original number represented as a double. The double value is used in
     * comparisons.
     */
    private final double doubleValue;

    /**
     * Creates an instance of a ComparableLazilyParsedNumber.
     * 
     */
    public ComparableLazilyParsedNumber(Number number) {
        this.number = number;
        doubleValue = number.doubleValue();
    }

    /**
     * @return An integer representation of this object.
     */
    @Override
    public int intValue() {
        return number.intValue();
    }

    /**
     * @return A long representation of this object.
     */
    @Override
    public long longValue() {
        return number.longValue();
    }

    /**
     * @return A float representation of this object.
     */
    @Override
    public float floatValue() {
        return number.floatValue();
    }

    /**
     * @return A double representation of this object.
     */
    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    /**
     * Tests equality based on the double value of the two
     * ComparableLazilyParsed
     * 
     * @return true - the two ComparableLazilyParsed objects are equal false
     *         -the two ComparatbleLazilyParsed objects are not equal
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ComparableLazilyParsedNumber)
                && (((ComparableLazilyParsedNumber) obj).doubleValue == this.doubleValue);
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * 
     * @param other
     *            Other object to which this one is to be compared.
     * @return A negative integer, zero, or a positive integer indicating that
     *         this object is less than, equal to, or greater than the other
     *         object, respectively.
     */
    @Override
    public int compareTo(ComparableLazilyParsedNumber other) {
        double delta = doubleValue() - other.doubleValue();
        return (delta < 0.0 ? -1 : (delta == 0.0 ? 0 : 1));
    }

    /**
     * @return A string representation of this object.
     */
    @Override
    public String toString() {
        return number.toString();
    }
}
