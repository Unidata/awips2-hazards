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
package com.raytheon.uf.common.dataplugin.events.hazards;

import java.util.ArrayList;
import java.util.List;

/**
 * Contants to be used by both Java and Python code, Python will have a class
 * that mirrors many of the values within this class for use by the tool writers
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class HazardConstants {

    // part of the hazard lifecycle that the user will see
    public enum HazardState {
        PENDING, POTENTIAL, PROPOSED, ISSUED, ENDED;
    }

    public static HazardState hazardStateFromString(String value) {
        return HazardState.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> hazardStatesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (HazardState state : HazardState.values()) {
            vals.add(state.toString().toLowerCase());
        }
        return vals;
    }

    public static enum ProductClass {
        OPERATIONAL("O"), TEST("T"), EXPERIMENTAL("E"), EXPERIMENTAL_IN_CURRENT(
                "X");

        private final String abbreviation;

        private ProductClass(String value) {
            this.abbreviation = value;
        }

        /**
         * @return the abbreviation
         */
        public String getAbbreviation() {
            return abbreviation;
        }
    }

    public static ProductClass productClassFromAbbreviation(String value) {
        for (ProductClass clazz : ProductClass.values()) {
            if (clazz.getAbbreviation().equals(value)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("No enum const "
                + ProductClass.class.getName() + "." + value);
    }

    public static ProductClass productClassFromName(String value) {
        return ProductClass.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> productClassesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (ProductClass clazz : ProductClass.values()) {
            vals.add(clazz.getAbbreviation());
        }
        return vals;
    }

    public static enum Significance {
        WARNING("W"), WATCH("A"), ADVISORY("Y"), OUTLOOK("O"), STATEMENT("S"), FORECAST(
                "F"), SYNOPSIS("N");
        private final String abbreviation;

        private Significance(String value) {
            this.abbreviation = value;
        }

        /**
         * @return the abbreviation
         */
        public String getAbbreviation() {
            return abbreviation;
        }
    }

    public static Significance significanceFromAbbreviation(String value) {
        for (Significance clazz : Significance.values()) {
            if (clazz.getAbbreviation().equals(value)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("No enum const "
                + Significance.class.getName() + "." + value);
    }

    public static Significance significanceFromName(String value) {
        return Significance.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> significancesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (Significance sig : Significance.values()) {
            vals.add(sig.getAbbreviation());
        }
        return vals;
    }

    /*
     * The following constants are for use with the hazard attributes object,
     * for use with keys. Also used with the required fields.
     */

    public static final String CALL_TO_ACTION = "Call To Action";

    public static final String BASIS_FOR_WARNING = "Basis For Warning";

    public static final String THREAT = "Threat";

    public static final String FLOOD_CREST_TIME = "Flood Crest Time";

    public static final String FLOOD_SEVERITY = "Flood Severity";

    public static final String FLOOD_BEGIN_TIME = "Flood Begin Time";

    public static final String FLOOD_END_TIME = "Flood End Time";

    public static final String FLOOD_RECORD_STATUS = "Flood Record Status";

    public static final String FLOOD_IMMEDIATE_CAUSE = "Flood Immediate Cause";

    /*
     * The following are used for any further filters that are required using
     * the getEventsByFilter() method, as well as defining the fields in both
     * the database implementation and the registry implementation
     */

    public static final String SITEID = "siteID";

    public static final String GEOMETRY = "geometry";

    public static final String STARTTIME = "startTime";

    public static final String PHENOMENON = "phenomenon";

    public static final String SIGNIFICANCE = "significance";

    public static final String EVENTID = "eventID";

    public static final String STATE = "state";

    public static final String ENDTIME = "endTime";

    public static final String ISSUETIME = "issueTime";

    public static final String HAZARDMODE = "hazardMode";
}
