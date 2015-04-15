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
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 23, 2013 1257       bsteffen     Initial creation
 * Jan 19, 2015 4193       rferrel      Added disseminationOrder.
 * Feb 10, 2015 3961       chris.cody   Add gagePointFirstRecommender startup property
 * Feb 10, 2015 6393       Chris.Golden Added hazard detail tab text.
 * Apr 09, 2015 7382       Chris.Golden Added hazard detail "show sliders" flag.
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

    @JsonProperty("gagePointFirstRecommender")
    private String gagePointFirstRecommender;

    @JsonProperty("hazardDetailTabText")
    private String[] hazardDetailTabText;

    @JsonProperty("showHazardDetailStartEndTimeScale")
    private boolean showHazardDetailStartEndTimeScale;

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

    public String getGagePointFirstRecommender() {
        return gagePointFirstRecommender;
    }

    public void setGagePointFirstRecommender(String gagePointFirstRecommender) {
        this.gagePointFirstRecommender = gagePointFirstRecommender;
    }

    public String[] getHazardDetailTabText() {
        return hazardDetailTabText;
    }

    public void setHazardDetailTabText(String[] hazardDetailTabText) {
        this.hazardDetailTabText = hazardDetailTabText;
    }

    public boolean isShowingHazardDetailStartEndTimeScale() {
        return showHazardDetailStartEndTimeScale;
    }

    public void setShowingHazardDetailStartEndTimeScale(
            boolean showHazardDetailStartEndTimeScale) {
        this.showHazardDetailStartEndTimeScale = showHazardDetailStartEndTimeScale;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
