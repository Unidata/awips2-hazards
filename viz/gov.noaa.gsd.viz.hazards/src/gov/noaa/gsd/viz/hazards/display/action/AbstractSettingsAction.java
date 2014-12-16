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
package gov.noaa.gsd.viz.hazards.display.action;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;

/**
 * An abstraction of the Settings Actions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2014 2915       bkowal      Initial creation
 * Dec 05, 2014 4124       Chris.Golden Changed to work with ISettings.
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class AbstractSettingsAction {
    private final ISettings settings;

    /**
     * 
     */
    public AbstractSettingsAction(ISettings settings) {
        this.settings = settings;
    }

    public ISettings getSettings() {
        return settings;
    }
}