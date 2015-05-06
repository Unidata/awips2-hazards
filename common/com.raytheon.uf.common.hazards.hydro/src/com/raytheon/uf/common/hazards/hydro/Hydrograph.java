package com.raytheon.uf.common.hazards.hydro;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * Description: Represents a generic (abstract) hydrograph. This is a time
 * series of river data, either observed (see: HydrographObserved) or forecast
 * (see: HydrographForecast)
 * 
 * This class does not correspond to any database table. It is a pseudo parent
 * (holder) class for SHEF child data (SHEF Observed or SHEF Forecast).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * June 2011               Bryon.Lawrence    Initial creation
 * March 1, 2013           Bryon.Lawrence    Prep for code review
 * May 1, 2014  3581       bkowal      Relocate to common hazards hydro
 * May 08, 2015 6562       Chris.Cody  Restructure River Forecast Points/Recommender
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public abstract class Hydrograph<T extends SHEFBase> {
    /**
     * River forecast point identifier (LID)
     */
    protected String lid;

    /**
     * Physical element (PE)
     */
    protected String physicalElement;

    /**
     * Type source of observation (TS)
     */
    protected String typeSource;

    /**
     * Container for hydro time series data.
     */
    protected List<T> shefHydroDataList;

    public Hydrograph() {
        this.shefHydroDataList = Lists.newArrayList();
    }

    protected Hydrograph(String lid, String physicalElement, String typeSource,
            List<T> shefHydroDataList) {
        this.lid = lid;
        this.physicalElement = physicalElement;
        this.typeSource = typeSource;
        if (shefHydroDataList != null) {
            this.shefHydroDataList = shefHydroDataList;
        } else {
            this.shefHydroDataList = Lists.newArrayList();
        }
    }

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

    public void setShefHydroDataList(List<T> shefHydroDataList) {
        if (shefHydroDataList != null) {
            this.shefHydroDataList = shefHydroDataList;
        } else {
            this.shefHydroDataList = Lists.newArrayList();
        }
    }

    public List<T> getShefHydroDataList() {
        return (this.shefHydroDataList);
    }
}
