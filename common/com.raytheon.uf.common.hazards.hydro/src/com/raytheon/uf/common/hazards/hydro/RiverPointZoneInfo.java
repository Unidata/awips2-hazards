package com.raytheon.uf.common.hazards.hydro;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class represents the zone information for a river forecast point.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2015 4959       Dan Schaffer Initial creation
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 */

public class RiverPointZoneInfo {

    /**
     * River station identifier
     */
    private String lid;

    /**
     * River station state
     */
    private String state;

    /**
     * Zone number
     */
    private String zoneNum;

    /**
     * Description
     */
    private String descr;

    /**
     * Default constructor
     */
    public RiverPointZoneInfo() {
    }

    /**
     * Creates a river forecast point object
     * 
     * @param zoneInfo
     *            Zone Information specific to this river point
     * @param floodDAO
     *            data accessor object
     */
    public RiverPointZoneInfo(String lid, String state, String zoneNum,
            String descr, IFloodDAO floodDAO) {
        this.lid = lid;
        this.state = state;
        this.zoneNum = zoneNum;
        this.descr = descr;
    }

    /**
     * 
     * @param
     * @return the identifier of this forecast point
     */
    public String getLid() {
        return lid;
    }

    /**
     * @return the state this point resides in
     */
    public String getState() {
        return state;
    }

    /**
     * @return the zoneNum
     */
    public String getZoneNum() {
        return zoneNum;
    }

    /**
     * @return the descr
     */
    public String getDescr() {
        return descr;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
