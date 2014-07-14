package com.raytheon.uf.common.hazards.hydro;

/**
 * Description: Contains settings which affect the operation and output of the
 * river flood recommender.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * May 1, 2014  3581       bkowal       Relocate to common hazards hydro
 * </pre>
 * 
 * @author Bryon.Lawrence
 */

public class HazardSettings {
    /**
     * Used to determine NR flood status
     */
    public static double DEFAULT_VTECRECORD_STAGE = 2.0;

    public static double DEFAULT_VTECRECORD_FLOW = 5000;

    /**
     * The hydrologic service area id
     */
    private String hsa;

    /**
     * Default expiration hours for various river pro products.
     */
    private int rvsExpirationHours;

    private int flsExpirationHours;

    private int flwExpirationHours;

    /**
     * The default number of hours to look back for observed river data.
     */
    private int obsLookbackHours;

    /**
     * The default number of hours to look forward for forecast data.
     */
    private int forecastLookForwardHours;

    /**
     * The time zone the recommender should use.
     */
    private String defaultTimeZone;

    /**
     * Used to determine near record flood status.
     */
    private double vtecRecordStageOffset = DEFAULT_VTECRECORD_STAGE;

    private double vtecRecordFlowOffeset = DEFAULT_VTECRECORD_FLOW;

    /*
     * Getters...
     */
    /**
     * @return the hydrologic service area id.
     */
    public String getHsa() {
        return hsa;
    }

    /**
     * 
     * @param
     * @return The number of hours after issuance to expire an RVS.
     */
    public int getRvsExpirationHours() {
        return rvsExpirationHours;
    }

    /**
     * 
     * @param
     * @return The number of hours after issuance to expire an FLS.
     */
    public int getFlsExpirationHours() {
        return flsExpirationHours;
    }

    /**
     * 
     * @param
     * @return The number of hours after issuance to expire an FLW.
     */
    public int getFlwExpirationHours() {
        return flwExpirationHours;
    }

    /**
     * 
     * @param
     * @return The time zone.
     */
    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * 
     * @param
     * @return The number of hours to look back for river observations
     */
    public int getObsLookbackHours() {
        return obsLookbackHours;
    }

    /**
     * 
     * @param
     * @return The number of hours to look forward for river forecasts.
     */
    public int getForecastLookForwardHours() {
        return forecastLookForwardHours;
    }

    /*
     * Setters
     */

    /**
     * @param rvsExpirationHours
     *            the expiration hours of an RVS
     */
    public void setRvsExpirationHours(int rvsExpirationHours) {
        this.rvsExpirationHours = rvsExpirationHours;
    }

    /**
     * @param flsExpirationHours
     *            the expiration hours of an FLS
     */
    public void setFlsExpirationHours(int flsExpirationHours) {
        this.flsExpirationHours = flsExpirationHours;
    }

    /**
     * @param flwExpirationHours
     *            the expiration hours of an FLW
     */
    public void setFlwExpirationHours(int flwExpirationHours) {
        this.flwExpirationHours = flwExpirationHours;
    }

    /**
     * @param obsLookbackHours
     *            the number of hours to look back for river observations
     */
    public void setObsLookbackHours(int obsLookbackHours) {
        this.obsLookbackHours = obsLookbackHours;
    }

    /**
     * @param forecastLookForwardHours
     *            the number of hours to look forward for forecasts
     */
    public void setForecastLookForwardHours(int forecastLookForwardHours) {
        this.forecastLookForwardHours = forecastLookForwardHours;
    }

    /**
     * @param defaultTimeZone
     *            the defaultTimeZone to set
     */
    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * @param hsa
     *            the hsa to set
     */
    public void setHsa(String hsa) {
        this.hsa = hsa;
    }

    /**
     * @param vtecRecordStageOffset
     *            the VTEC Record Stage Offset to set
     */
    public void setVtecRecordStageOffset(double vtecRecordStageOffset) {
        this.vtecRecordStageOffset = vtecRecordStageOffset;
    }

    /**
     * @return the VTEC Record Stage Offset
     */
    public double getVtecRecordStageOffset() {
        return vtecRecordStageOffset;
    }

    /**
     * @param vtecRecordFlowOffeset
     *            the VTEC Record Flow Offset to set
     */
    public void setVtecRecordFlowOffset(double vtecRecordFlowOffeset) {
        this.vtecRecordFlowOffeset = vtecRecordFlowOffeset;
    }

    /**
     * @return the VTEC Record Flow Offset
     */
    public double getVtecRecordFlowOffset() {
        return vtecRecordFlowOffeset;
    }
}
