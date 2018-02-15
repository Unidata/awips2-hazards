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
package com.raytheon.uf.viz.productgen.validation;

import java.util.HashMap;
import java.util.TimeZone;

/**
 * Constants used in conjunction with text processing for warning related
 * products.
 * <p>
 * Imported and integrated from Warn Gen: com.raytheon.viz.texteditor
 * 
 * AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon
 * /viz/texteditor/qc
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2015 6617       Chris.Cody  Initial Import. Integrate WarnGen Product Validation.
 * </pre>
 * 
 * @author grichard
 * @version 1.0
 */

public final class TextWarningConstants {

    /**
     * The VTEC Action enumeration
     */
    public static enum vtecActionEnum {
        NEW, CON, EXT, EXA, EXB, UPG, CAN, EXP, COR, ROU;
    }

    public static HashMap<String, TimeZone> timeZoneAbbreviationMap = null;

    public static HashMap<String, TimeZone> timeZoneShortNameMap = null;

    static {
        // build the abbreviation map
        timeZoneAbbreviationMap = new HashMap<String, TimeZone>();
        timeZoneAbbreviationMap.put("A", TimeZone.getTimeZone("US/Alaska"));
        timeZoneAbbreviationMap.put("C", TimeZone.getTimeZone("CST6CDT"));
        timeZoneAbbreviationMap.put("E", TimeZone.getTimeZone("EST5EDT"));
        timeZoneAbbreviationMap.put("G", TimeZone.getTimeZone("Pacific/Guam"));
        timeZoneAbbreviationMap.put("H", TimeZone.getTimeZone("HST"));
        timeZoneAbbreviationMap.put("M", TimeZone.getTimeZone("MST7MDT"));
        timeZoneAbbreviationMap.put("m", TimeZone.getTimeZone("MST"));
        timeZoneAbbreviationMap.put("P", TimeZone.getTimeZone("PST8PDT"));
        timeZoneAbbreviationMap.put("S", TimeZone.getTimeZone("US/Samoa"));
        timeZoneAbbreviationMap.put("V",
                TimeZone.getTimeZone("America/Puerto_Rico"));

        HashMap<String, TimeZone> t = timeZoneAbbreviationMap;
        timeZoneShortNameMap = new HashMap<String, TimeZone>();
        timeZoneShortNameMap.put("AKST", t.get("A"));
        timeZoneShortNameMap.put("AKDT", t.get("A"));
        timeZoneShortNameMap.put("CST", t.get("C"));
        timeZoneShortNameMap.put("CDT", t.get("C"));
        timeZoneShortNameMap.put("EST", t.get("E"));
        timeZoneShortNameMap.put("EDT", t.get("E"));
        timeZoneShortNameMap.put("CHST", t.get("G"));
        timeZoneShortNameMap.put("ChST", t.get("G"));
        timeZoneShortNameMap.put("HST", t.get("H"));
        timeZoneShortNameMap.put("MST", t.get("m"));
        timeZoneShortNameMap.put("MDT", t.get("M"));
        timeZoneShortNameMap.put("PST", t.get("P"));
        timeZoneShortNameMap.put("PDT", t.get("P"));
        timeZoneShortNameMap.put("SST", t.get("S"));
        timeZoneShortNameMap.put("AST", t.get("V"));
    }

    /**
     * Constructor.
     */
    private TextWarningConstants() {
    }

}
