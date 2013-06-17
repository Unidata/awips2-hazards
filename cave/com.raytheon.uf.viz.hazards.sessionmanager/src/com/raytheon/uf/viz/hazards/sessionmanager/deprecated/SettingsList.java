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
package com.raytheon.uf.viz.hazards.sessionmanager.deprecated;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * * Reverse engineered to represent settingsList as JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class SettingsList {

    private String currentSettingsID;

    private Settings[] settingsList;

    public String getCurrentSettingsID() {
        return currentSettingsID;
    }

    public void setCurrentSettingsID(String currentSettingsID) {
        this.currentSettingsID = currentSettingsID;
    }

    public Settings[] getSettingsList() {
        return settingsList;
    }

    public void setSettingsList(Settings[] settingsList) {
        this.settingsList = settingsList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
