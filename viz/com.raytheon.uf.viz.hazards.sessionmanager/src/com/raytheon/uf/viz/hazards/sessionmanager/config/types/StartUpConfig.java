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
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Built from the objects defined in StartupConfig localization file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 23, 2013 1257       bsteffen    Initial creation
 * Jan 19, 2015 4193       rferrel     Added disseminationOrder.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class StartUpConfig {
    @JsonProperty("Console")
    private Console console;

    @JsonProperty("disseminationOrder")
    private String[] disseminationOrder;

    public Console getConsole() {
        return console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public String[] getDisseminationOrder() {
        return disseminationOrder;
    }

    public void setDisseminationOrder(String[] disseminationOrder) {
        this.disseminationOrder = disseminationOrder;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
