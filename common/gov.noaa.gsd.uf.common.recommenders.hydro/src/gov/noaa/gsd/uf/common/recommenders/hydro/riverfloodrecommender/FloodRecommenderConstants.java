/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender;

/**
 * Description: Contains constants and enumerations specific to the River Flood
 * Recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2014 2960       blawrenc    Initial creation
 * 
 * </pre>
 * 
 * @author blawrenc
 * @version 1.0
 */
public class FloodRecommenderConstants {

    /**
     * Enumeration of possible flood severity values. Based on NWS H-VTEC
     * policy.
     */
    public static enum FloodSeverity {
        NONE("N"), AREAL_OR_FLASH_FLOOD("0"), MINOR("1"), MODERATE("2"), MAJOR(
                "3"), UNKNOWN("U");

        private final String value;

        private FloodSeverity(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Enumeration of possible flood record status values. Based on NWS H-VTEC
     * policy.
     */
    public static enum FloodRecordStatus {
        RECORD_FLOOD_NOT_EXPECTED("NO"), NEAR_RECORD_FLOOD_OR_RECORD_FLOOD_EXPECTED(
                "NR"), NO_PERIOD_OF_RECORD("UU"), AREAL_POINT_FLASH_FLOOD("OO");

        private final String value;

        private FloodRecordStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Enumeration of possible Immediate Cause values. Based on NWS H-VTEC
     * policy
     */
    public static enum ImmediateCause {
        EXCESSIVE_RAINFALL("ER"), SNOWMELT("SM"), RAIN_AND_SNOWMELT("RS"), DAM_OR_LEVEE_FAILURE(
                "DM"), ICE_JAME("IJ"), GLACIER_DAMMED_LAKE_OUTBURST("GO"), RAIN_AND_OR_SNOWMELT_AND_OR_ICE_JAM(
                "IC"), UPSTREAM_FLOODING_PLUS_STORM_SURGE("FS"), UPSTREAM_FLOODING_PLUS_TIDAL_EFFECTS(
                "FT"), ELEVATED_UPSTREAM_FLOW_PLUS_TIDAL_EFFECTS("ET"), WIND_AND_OR_TIDAL_EFFECTS(
                "WT"), UPSTREAM_DAM_AND_OR_RESERVOIR_RELEASE("DR"), OTHER_MULTIPLE_CAUSES(
                "MC"), OTHER_EFFECTS("OT"), UNKNOWN("UU");

        private final String value;

        private ImmediateCause(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
