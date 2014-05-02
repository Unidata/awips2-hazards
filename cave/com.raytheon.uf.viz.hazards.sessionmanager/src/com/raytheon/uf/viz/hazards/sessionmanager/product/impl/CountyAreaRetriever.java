/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.dataplugin.warning.WarningConstants;
import com.raytheon.uf.common.dataplugin.warning.gis.GenerateGeospatialDataRequest;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialData;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialDataSet;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialMetadata;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * Description: Retrieves representation of county areas for a localization
 * site.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class CountyAreaRetriever {

    static final String FIPS = "FIPS";

    private final IUFStatusHandler statusHandler = UFStatus.getHandler(this
            .getClass());

    public GeospatialData[] getCountyAreasForSite(String site) {
        try {
            GeospatialData[] countyAreas;
            /*
             * We need to construct a GeospatialMetadata object, and then
             * request a GeospatialDataSet object.
             */
            GeospatialMetadata gmd = new GeospatialMetadata();
            gmd.setAreaSource("County");
            gmd.setFipsField(FIPS);
            List<String> areaFields = new ArrayList<>();
            areaFields.add(WarningConstants.GID);
            areaFields.add(FIPS);
            gmd.setAreaFields(areaFields);
            GenerateGeospatialDataRequest request = new GenerateGeospatialDataRequest();
            GeospatialDataSet dataSet = null;
            request.setMetaData(gmd);
            request.setSite(site);
            dataSet = (GeospatialDataSet) RequestRouter.route(request);
            countyAreas = dataSet.getAreas();
            return countyAreas;
        } catch (Exception e) {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "Request for county GeospatialDataSet failed.", e);
            return null;
        }
    }

}
