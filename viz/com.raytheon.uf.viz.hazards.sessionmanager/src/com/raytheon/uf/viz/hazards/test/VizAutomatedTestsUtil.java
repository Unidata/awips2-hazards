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
package com.raytheon.uf.viz.hazards.test;

/**
 * Utility class to help identify if automated test is running.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 11, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class VizAutomatedTestsUtil {

    private static final String ENVIRONMENT_VARIABLE = "HAZARD_SERVICES_AUTO_TESTS_ENABLED";

    protected VizAutomatedTestsUtil() {

    }

    public static boolean areAutomatedTestsEnabled() {
        return System.getenv(ENVIRONMENT_VARIABLE) != null;
    }

}
