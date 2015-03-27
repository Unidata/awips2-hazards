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
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Anytime tools are modified directly (by the UI element, this will be fired)
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 12, 2014            mnash     Initial creation
 * Dec 05, 2014 4124       Chris.Golden Changed to work with parameterized config manager,
 *                                      and to include originator.
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class SettingsToolsModified extends SettingsModified {

    public SettingsToolsModified(IOriginator originator) {
        super(originator);
    }
}
