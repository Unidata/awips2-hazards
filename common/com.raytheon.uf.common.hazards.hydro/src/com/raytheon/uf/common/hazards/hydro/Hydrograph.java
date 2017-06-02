package com.raytheon.uf.common.hazards.hydro;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 
 * Description: Represents a generic (abstract) hydrograph. This is a time
 * series of river data: Observed (see: HydrographObserved), Forecast (see:
 * HydrographForecast), or Precipitation (see: HydrographPrecip).
 * 
 * This class does not correspond to any database table. It is a pseudo parent
 * (holder) class for SHEF child data (SHEF Observed, SHEF Forecast, or
 * SHEFPrecip). Changes to Hydrograph attribute values do not trigger a requery
 * of SHEF data. Hydrograph (and sub objects) are Data only access objects and
 * do not query for data.
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
 * May 28, 2015 7139       Chris.Cody  Add SHEF Precip sub class. Add get earliest/latest SHEF object
 * May 04, 2016 15584      Kevin.Bisanz Add toString()
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
     * Set Hydrograph Physical Element value.
     * 
     * @param physicalElement
     *            the physicalElement to set
     */
    public void setPhysicalElement(String physicalElement) {
        this.physicalElement = physicalElement;
    }

    /**
     * Get Hydrograph Physical Element value.
     * 
     * @return the physical element
     */
    public String getPhysicalElement() {
        return physicalElement;
    }

    /**
     * Set the Hyrdrograph Type Source value.
     * 
     * @param typeSource
     *            the typeSource to set
     */
    public void setTypeSource(String typeSource) {
        this.typeSource = typeSource;
    }

    /**
     * Set Hydrograph Type Source value.
     * 
     * @return the typeSource
     */
    public String getTypeSource() {
        return typeSource;
    }

    /**
     * Set a new list of SHEF Time Series sub data objects.
     * 
     * @param shefHydroDataList
     *            List of SHEF (Forecast, Observed, Precip) Time Series data
     *            objects.
     */
    public void setShefHydroDataList(List<T> shefHydroDataList) {
        if (shefHydroDataList != null) {
            this.shefHydroDataList = shefHydroDataList;
        } else {
            this.shefHydroDataList = Lists.newArrayList();
        }
    }

    /**
     * Return a list of all queried SHEF Time Series sub data objects.
     * 
     * @return List of SHEF (Forecast, Observed, Precip) Time Series data
     *         objects.
     */
    public List<T> getShefHydroDataList() {
        return (this.shefHydroDataList);
    }

    protected T getShefHydroByValue(boolean isMax) {
        T shefHydroData = null;
        double shefHydroDataValue = 0.0D;

        for (T tempShefHydroData : this.shefHydroDataList) {
            if (shefHydroData != null) {
                double tempShefHydroDataValue = tempShefHydroData.getValue();
                if (isMax == true) {
                    if (tempShefHydroDataValue > shefHydroDataValue) {
                        shefHydroData = tempShefHydroData;
                        shefHydroDataValue = tempShefHydroDataValue;
                    }
                } else {
                    if (tempShefHydroDataValue < shefHydroDataValue) {
                        shefHydroData = tempShefHydroData;
                        shefHydroDataValue = tempShefHydroDataValue;
                    }
                }
            } else {
                shefHydroData = tempShefHydroData;
                shefHydroDataValue = tempShefHydroData.getValue();
            }
        }

        return (shefHydroData);
    }

    /**
     * Get SHEF Hydro Data object with the Maximum value from all of the
     * Hydrograph SHEF objects.
     * 
     * @return SHEF Object with Maximum value
     */
    public T getMaxShefHydroData() {
        return (getShefHydroByValue(true));
    }

    /**
     * Get the Maximum value from all of the Hydrograph SHEF objects.
     * 
     * @return Maximum SHEF sub Object Value
     */
    public double getMaxShefHydroDataValue() {
        T shefHydroData = getShefHydroByValue(true);
        if (shefHydroData != null) {
            return (shefHydroData.getValue());
        } else {
            return (RiverHydroConstants.MISSING_VALUE_DOUBLE);
        }
    }

    public T getShefHydroDataByTime(boolean isEarliest) {
        T shefHydroData = null;
        long shefHydroDataTime = 0L;

        for (T tempShefHydroData : this.shefHydroDataList) {
            if (shefHydroData != null) {
                long tempShefTime = tempShefHydroData.getTime();
                if (isEarliest == true) {
                    if (tempShefTime < shefHydroDataTime) {
                        shefHydroData = tempShefHydroData;
                        shefHydroDataTime = tempShefTime;
                    }
                } else {
                    if (tempShefTime > shefHydroDataTime) {
                        shefHydroData = tempShefHydroData;
                        shefHydroDataTime = tempShefTime;
                    }
                }
            } else {
                shefHydroData = tempShefHydroData;
                shefHydroDataTime = tempShefHydroData.getTime();
            }
        }

        return (shefHydroData);
    }

    public T getEarliestShefHydroData() {
        return (getShefHydroDataByTime(true));
    }

    public T getLatestShefHydroData() {
        return (getShefHydroDataByTime(false));
    }

    public long getEarliestShefHydroDataTime() {
        T shefHydroData = getShefHydroDataByTime(true);
        if (shefHydroData != null) {
            return (shefHydroData.getTime());
        } else {
            return (RiverHydroConstants.MISSING_VALUE);
        }
    }

    public long getLatestShefHydroDataTime() {
        T shefshefHydroData = getShefHydroDataByTime(false);
        if (shefshefHydroData != null) {
            return (shefshefHydroData.getTime());
        } else {
            return (RiverHydroConstants.MISSING_VALUE);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LID PE TS: ");
        sb.append(getLid());
        sb.append(" ");
        sb.append(getPhysicalElement());
        sb.append(" ");
        sb.append(getTypeSource());
        return sb.toString();
    }

}
