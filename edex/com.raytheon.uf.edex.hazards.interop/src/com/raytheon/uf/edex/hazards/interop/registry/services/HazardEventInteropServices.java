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
package com.raytheon.uf.edex.hazards.interop.registry.services;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.annotations.FastInfoset;
import org.springframework.transaction.annotation.Transactional;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.HazardConflictDict;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventInteropServices;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.GFERecord;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.db.objects.TimeConstraints;
import com.raytheon.uf.common.dataplugin.gfe.discrete.DiscreteKey;
import com.raytheon.uf.common.dataplugin.gfe.slice.DiscreteGridSlice;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypeEntry;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.edex.hazards.interop.gfe.GFERecordUtil;
import com.raytheon.uf.edex.hazards.interop.gfe.GridRequestHandler;
import com.raytheon.uf.edex.registry.ebxml.util.EbxmlObjectUtil;

/**
 * 
 * Service implementation for the Hazard Services Interoperability web services
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@FastInfoset
@WebService(targetNamespace = IHazardEventInteropServices.NAMESPACE, endpointInterface = "com.raytheon.uf.common.dataplugin.events.hazards.registry.services.IHazardEventInteropServices", portName = "HazardEventInteropServicesPort", serviceName = IHazardEventInteropServices.SERVICE_NAME)
@SOAPBinding
@Transactional
public class HazardEventInteropServices implements IHazardEventInteropServices {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardEventInteropServices.class);

    /** Grid request handler for interacting with GFE grids */
    private GridRequestHandler gridRequestHandler;

    /** The hazards conflict dictionary */
    private HazardConflictDict hazardsConflictDict;

    /** Denotes if this is a practice set of services */
    private boolean practice;

    /** The mode (PRACTICE, OPERATIONAL) derived from the practice boolean */
    private Mode mode;

    @Resource
    private WebServiceContext wsContext;

    @Override
    @WebMethod(operationName = "hasConflicts")
    public Boolean hasConflicts(@WebParam(name = "phenSig")
    String phenSig, @WebParam(name = "siteID")
    String siteID, @WebParam(name = "startTime")
    Date startTime, @WebParam(name = "endTime")
    Date endTime) {
        TimeRange timeRange = GFERecordUtil.createGridTimeRange(startTime,
                endTime, new TimeConstraints(TimeUtil.SECONDS_PER_HOUR,
                        TimeUtil.SECONDS_PER_HOUR, 0));
        boolean hasConflicts = hasConflicts(phenSig, timeRange, siteID);
        return hasConflicts;
    }

    @Override
    @WebMethod(operationName = "retrieveHazardsConflictDict")
    public HazardConflictDict retrieveHazardsConflictDict() {
        if (hazardsConflictDict == null) {
            populateHazardsConflictDict();
        }
        return hazardsConflictDict;
    }

    @Override
    @WebMethod(operationName = "ping")
    public String ping() {
        statusHandler.info("Received Ping from "
                + EbxmlObjectUtil.getClientHost(wsContext));
        return "OK";
    }

    private boolean hasConflicts(String phenSig, TimeRange timeRange,
            String siteID) {
        try {
            retrieveHazardsConflictDict();
            final String parmIDFormat = (mode == Mode.OPERATIONAL) ? GridRequestHandler.OPERATIONAL_PARM_ID_FORMAT
                    : GridRequestHandler.PRACTICE_PARM_ID_FORMAT;
            ParmID parmID = new ParmID(String.format(parmIDFormat, siteID));
            List<GFERecord> potentialRecords = gridRequestHandler
                    .findIntersectedGrid(parmID, timeRange);
            // test if hazardEvent will conflict with existing grids
            if (hazardsConflictDict != null
                    && hazardsConflictDict.get(phenSig) != null) {
                List<String> hazardsConflictList = hazardsConflictDict
                        .get(phenSig);
                for (GFERecord record : potentialRecords) {
                    DiscreteGridSlice gridSlice = (DiscreteGridSlice) record
                            .getMessageData();
                    for (DiscreteKey discreteKey : gridSlice.getKeys()) {
                        for (String key : discreteKey.getSubKeys()) {
                            if (hazardsConflictList.contains(key)) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error(
                    "Error trying to retrieve intersecting gfe records", e);
        }
        return false;
    }

    /**
     * Populates hazardConflictsDict with HazardsConflictDict from
     * MergeHazards.py
     */
    private void populateHazardsConflictDict() {
        statusHandler.info("Retrieving the hazard conflict dictionary.");

        hazardsConflictDict = new HazardConflictDict();

        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationFile file = pm
                .getStaticLocalizationFile(HazardsConfigurationConstants.HAZARD_TYPES_PY);
        ConfigLoader<HazardTypes> hazardTypesConfigLoader = new ConfigLoader<HazardTypes>(
                file, HazardTypes.class);
        HazardTypes hazardTypes = hazardTypesConfigLoader.getConfig();
        Iterator<String> hazardTypesIterator = hazardTypes.keySet().iterator();
        while (hazardTypesIterator.hasNext()) {
            final String hazardType = hazardTypesIterator.next();
            HazardTypeEntry entry = hazardTypes.get(hazardType);
            hazardsConflictDict.put(hazardType, entry.getHazardConflictList());
        }

        statusHandler
                .info("Successfully retrieved the hazard conflict dictionary!");
    }

    /**
     * @param practice
     *            the practice to set
     */
    public void setPractice(boolean practice) {
        this.practice = practice;
        this.mode = this.practice ? Mode.PRACTICE : Mode.OPERATIONAL;
    }

    /**
     * @param gridRequestHandler
     *            the gridRequestHandler to set
     */
    public void setGridRequestHandler(GridRequestHandler gridRequestHandler) {
        this.gridRequestHandler = gridRequestHandler;
    }
}
