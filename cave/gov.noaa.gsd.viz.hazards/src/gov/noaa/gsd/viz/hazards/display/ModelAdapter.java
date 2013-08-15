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
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.pythonjoblistener.HazardServicesGeneratorJobListener;
import gov.noaa.gsd.viz.hazards.pythonjoblistener.HazardServicesRecommenderJobListener;

import java.util.List;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;

/**
 * Transforms ISessionManager into an IHazardServicesModel, most of the code
 * exists in a deprecated pacakge om the new SessionManager plugin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * Jul 24, 2013  585       C. Golden   Changed to allow loading from bundles.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class ModelAdapter extends
        com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ModelAdapter
        implements IHazardServicesModel {

    private EventBus eventBus;

    @Deprecated
    @Override
    public void initialize(String selectedTime, String currentTime,
            String staticSettingID, String dynamicSetting_json,
            String caveMode, String siteID, EventBus eventBus, String state) {
        this.eventBus = eventBus;
        super.initialize(selectedTime, currentTime, staticSettingID,
                dynamicSetting_json, caveMode, siteID, eventBus, state);
    }

    @Override
    protected IPythonJobListener<EventSet<IEvent>> getRecommenderListener(
            String toolName) {
        return new HazardServicesRecommenderJobListener(eventBus, toolName);
    }

    @Override
    protected IPythonJobListener<List<IGeneratedProduct>> getProductGenerationListener(
            String toolName) {
        return new HazardServicesGeneratorJobListener(eventBus, toolName);
    }
}
