package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

/**
 * 
 * Description: Represents a SHEF observation. Hydrometeorological data written
 * to the hydro database are in SHEF format.
 * 
 * This class is not meant to be subclassed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * July 2012               Bryon.Lawrence    Initial creation
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public final class SHEFObservation implements Cloneable {
    /*
     * PEDTSEP items
     */
    /**
     * Physical element
     */
    private String physicalElement;

    /**
     * Duration of observation
     */
    private long duration;

    /**
     * Type source of observation
     */
    private String typeSource;

    /**
     * Extremum of observation (e.g. max, min)
     */
    private char extremum;

    /**
     * Probability of observation
     */
    private double probability;

    /**
     * SHEF quality code
     */
    private String shefQualCode;

    /**
     * Observation quality code
     */
    private long qualityCode;

    /**
     * Observation value
     */
    private double value;

    /**
     * valid time of observation
     */
    private long validTime;

    /**
     * basis time of observation
     */
    private long basisTime;

    /**
     * @param physicalElement
     *            the physicalElement to set
     */
    public void setPhysicalElement(String physicalElement) {
        this.physicalElement = physicalElement;
    }

    /**
     * @return the physical element
     */
    public String getPhysicalElement() {
        return physicalElement;
    }

    /**
     * @param duration
     *            the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param typeSource
     *            the typeSource to set
     */
    public void setTypeSource(String typeSource) {
        this.typeSource = typeSource;
    }

    /**
     * @return the typeSource
     */
    public String getTypeSource() {
        return typeSource;
    }

    /**
     * @param extremum
     *            the extremum to set
     */
    public void setExtremum(char extremum) {
        this.extremum = extremum;
    }

    /**
     * @return the extremum
     */
    public char getExtremum() {
        return extremum;
    }

    /**
     * @param probability
     *            the probability to set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    /**
     * @return the probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @param shefQualCode
     *            the shefQualCode to set
     */
    public void setShefQualCode(String shefQualCode) {
        this.shefQualCode = shefQualCode;
    }

    /**
     * @return the shefQualCode
     */
    public String getShefQualCode() {
        return shefQualCode;
    }

    /**
     * @param qualityCode
     *            the qualityCode to set
     */
    public void setQualityCode(long qualityCode) {
        this.qualityCode = qualityCode;
    }

    /**
     * @return the qualityCode
     */
    public long getQualityCode() {
        return qualityCode;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param validTime
     *            the validTime to set
     */
    public void setValidTime(long validTime) {
        this.validTime = validTime;
    }

    /**
     * @return the validTime
     */
    public long getValidTime() {
        return validTime;
    }

    /**
     * @param basisTime
     *            the basisTime to set
     */
    public void setBasisTime(long basisTime) {
        this.basisTime = basisTime;
    }

    /**
     * @return the basisTime
     */
    public long getBasisTime() {
        return basisTime;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
