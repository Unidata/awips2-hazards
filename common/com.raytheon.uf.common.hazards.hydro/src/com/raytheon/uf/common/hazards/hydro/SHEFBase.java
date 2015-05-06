/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.hazards.hydro;

/**
 * This represents a base class for SHEF Forecast and SHEF Observed data.
 * Hydrometeorological data written to the hydro database (Forecast: fcstheight,
 * fcstdischarge) (Observed: height, discharge) are in SHEF format.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * July 2012               Bryon.Lawrence    Initial creation
 * May 1, 2014  3581       bkowal      Relocate to common hazards hydro
 * May 08, 2015 6562       Chris.Cody  Restructure River Forecast Points/Recommender
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public abstract class SHEFBase {

    /**
     * Forecast Point Identifier (LID)
     */
    protected String lid;

    /**
     * Physical element (PE)
     */
    protected String physicalElement;

    /**
     * Duration of observation (DUR)
     */
    protected long duration;

    /**
     * Type source of observation
     */
    protected String typeSource;

    /**
     * Extremum of observation (e.g. max, min) (EXTREMUM)
     */
    protected char extremum;

    /**
     * Observation value (VALUE)
     */
    protected double value;

    /**
     * SHEF quality code (SHEF_QUAL_CODE)
     */
    protected String shefQualCode;

    /**
     * Observation quality code (QUALITY_CODE)
     */
    protected long qualityCode;

    /**
     * revision number (REVISION)
     */
    protected int revision;

    /**
     * Product Identifier (PRODUCT_ID)
     */
    protected String productId;

    /**
     * Product Time (PRODUCTTIME)
     */
    protected long productTime;

    /**
     * Posting Time (POSTINGTIME)
     */
    protected long postingTime;

    /**
     * Get Forecast Point Identifier.
     * 
     * @return lid
     */
    public String getLid() {
        return (this.lid);
    }

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
     * Get Revision number
     * 
     * @return revision
     */
    public int getRevision() {
        return (this.revision);
    }

    /**
     * Get Product Identifier.
     * 
     * @return productId
     */
    public String getProductId() {
        return (this.productId);
    }

    /**
     * Get Product Time.
     * 
     * @return productTime
     */
    public long getProductTime() {
        return (this.productTime);
    }

    /**
     * Get Posting Time.
     * 
     * @return postingTime
     */
    public long getPostingTime() {
        return (this.postingTime);
    }

}
