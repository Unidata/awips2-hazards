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
package com.raytheon.uf.common.dataplugin.events.hazards.event;


/**
 * Misc functionality for reformatting attributes for hazard events.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 24, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class HazardEventUtilities {

    public static String getPhenSigSubType(IHazardEvent event) {
        return getPhenSigSubType(event.getPhenomenon(),
                event.getSignificance(), event.getSubtype());
    }

    public static String getPhenSigSubType(String phen, String sig,
            String subType) {
        if (phen == null || sig == null) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        str.append(phen);
        str.append('.');
        str.append(sig);
        if (subType != null) {
            str.append('.');
            str.append(subType);
        }
        return str.toString();
    }

    public static void populateEventForPhenSigSubtype(IHazardEvent event,
            String phenSigSubtype) {
        int endPhen = phenSigSubtype.indexOf('.');
        event.setPhenomenon(phenSigSubtype.substring(0, endPhen));
        int endSig = phenSigSubtype.indexOf('.', endPhen + 1);
        if (endSig > 0) {
            event.setSignificance(phenSigSubtype.substring(endPhen + 1, endSig));
            event.setSubtype(phenSigSubtype.substring(endSig + 1));
        } else {
            event.setSignificance(phenSigSubtype.substring(endPhen + 1));
        }
    }

}
