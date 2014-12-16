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
package gov.noaa.gsd.viz.hazards;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Originator for UI elements
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 01, 2014            mnash        Initial creation
 * Oct 09, 2014    4042    Chris.Golden Added product staging dialog.
 * Dec 05, 2014    4124    Chris.Golden Added new UI elements.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public enum UIOriginator implements IOriginator {
    SPATIAL_DISPLAY, HAZARD_INFORMATION_DIALOG, STAGING_DIALOG, SETTINGS_MENU, SETTINGS_DIALOG, CONSOLE,

    /*
     * TODO: CONSOLE_HEADER_FILTER is required because the Console has not yet
     * been refactored; see the TODO comment in the anonymous
     * MegawidgetManagerAdapter instance within TemporalDisplay.updateConsole()
     */
    CONSOLE_HEADER_FILTER;
}
