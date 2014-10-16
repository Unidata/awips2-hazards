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
package com.raytheon.uf.edex.hazards.interoperability.handlers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.VtecInteroperabilityWarningRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.requests.VtecInteroperabilityWarningResponse;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.PracticeWarningRecord;
import com.raytheon.uf.common.dataplugin.warning.WarningRecord;
import com.raytheon.uf.common.dataquery.requests.DbQueryRequest;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestConstraint.ConstraintType;
import com.raytheon.uf.common.dataquery.responses.DbQueryResponse;
import com.raytheon.uf.common.serialization.comm.IRequestHandler;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Handles requests for warning records to hazard event mappings for a specified
 * site.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 18, 2014            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class VtecInteroperabilityWarningHandler extends
        AbstractInteroperabilityHandler implements
        IRequestHandler<VtecInteroperabilityWarningRequest> {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(VtecInteroperabilityWarningHandler.class);

    private static final String[] FIELDS = { ETN_DICT_FIELD,
            SITE_ID_DICT_FIELD, SEGTEXT_DICT_FIELD, VTECSTR_FIELD, SIG_FIELD,
            PHEN_FIELD, OVERVIEW_FIELD, PHENSIG_FIELD, ACTION_FIELD, SEG_FIELD,
            START_TIME_FIELD, END_TIME_FIELD, UFN_FIELD, OFFICEID_FIELD,
            PURGE_TIME_FIELD, ISSUE_TIME_FIELD, PIL_FIELD, PRODUCT_CLASS_FIELD,
            ID_FIELD, RAW_MESSAGE_FIELD, COUNTY_HEADER_FIELD, WMO_ID_FIELD,
            FLOOD_CREST_FIELD, FLOOD_BEGIN_FIELD, FLOOD_RECORD_STATUS_FIELD,
            FLOOD_SEVERITY_FIELD, GEOM_FIELD, IMMEDIATE_CAUSE_FIELD, LOC_FIELD,
            LOCATION_ID_FIELD, MOTION_DIRECTION_FIELD, MOTION_SPEED_FIELD,
            UGC_ZONE_LIST_FIELD };

    public VtecInteroperabilityWarningHandler() {
        statusHandler.info("Initialized!");
    }

    @Override
    public VtecInteroperabilityWarningResponse handleRequest(
            VtecInteroperabilityWarningRequest request) throws Exception {
        VtecInteroperabilityWarningResponse response = new VtecInteroperabilityWarningResponse();

        List<Map<String, Object>> warningRecordsDictList = retrieveRecords(
                request.getSiteID(), request.getPhensig(), request.isPractice());
        statusHandler.info("Found " + warningRecordsDictList.size()
                + " Warning Records.");
        try {
            List<Map<String, Object>> dictListResult = validateRecords(
                    warningRecordsDictList, request.isPractice());

            response.setPhensig(request.getPhensig());
            response.setWarnings(dictListResult);
            response.setSuccess(true);
        } catch (Exception e) {
            response.setSuccess(false);
            response.setExceptionText(e.getLocalizedMessage());
            statusHandler.error(e.getLocalizedMessage());
        }
        return response;
    }

    @Override
    protected List<Map<String, Object>> retrieveRecords(String siteID,
            String phensig, boolean practice) throws Exception {
        Map<String, RequestConstraint> constraints = new HashMap<String, RequestConstraint>();
        constraints.put("geometry", new RequestConstraint(null,
                ConstraintType.ISNOTNULL));
        constraints.put("phensig", new RequestConstraint(phensig,
                ConstraintType.EQUALS));
        constraints.put("officeid", new RequestConstraint(siteID,
                ConstraintType.EQUALS));

        DbQueryRequest request = new DbQueryRequest();
        request.setConstraints(constraints);
        request.setEntityClass(getWarningClass(practice));
        request.addFields(FIELDS);
        DbQueryResponse response = (DbQueryResponse) RequestRouter
                .route(request);

        return convertToDict(response.getResults());
    }

    private List<Map<String, Object>> convertToDict(
            List<Map<String, Object>> queryResults) {
        List<Map<String, Object>> dicts = new ArrayList<Map<String, Object>>(
                queryResults.size());
        /*
         * This code is very similar to ActiveTableUtil.convertToDict. The
         * difference is that the record is of type AbstaractWarnignRecord.
         */
        for (Map<String, Object> result : queryResults) {
            String ugcZoneList = String
                    .valueOf(result.get(UGC_ZONE_LIST_FIELD));
            for (String ugc : ugcZoneList.split(",")) {
                Map<String, Object> dict = new HashMap<String, Object>();
                dict.put(VTECSTR_FIELD, result.get(VTECSTR_FIELD));
                dict.put(ETN_DICT_FIELD, Integer.valueOf(String.valueOf(result
                        .get(ETN_DICT_FIELD))));
                dict.put(SIG_FIELD, result.get(SIG_FIELD));
                dict.put(PHEN_FIELD, result.get(PHEN_FIELD));
                if (result.get(SEGTEXT_DICT_FIELD) != null) {
                    dict.put(SEGTEXT_DICT_FIELD, result.get(SEGTEXT_DICT_FIELD));
                }
                if (result.get(OVERVIEW_FIELD) != null) {
                    dict.put(OVERVIEW_FIELD, result.get(OVERVIEW_FIELD));
                    dict.put(HEADLINE_FIELD, result.get(OVERVIEW_FIELD));
                }
                dict.put(PHENSIG_FIELD, result.get(PHENSIG_FIELD));
                dict.put(ACTION_FIELD, result.get(ACTION_FIELD));
                dict.put(SEG_FIELD, result.get(SEG_FIELD));
                Calendar startTime = (Calendar) result.get(START_TIME_FIELD);
                dict.put(START_TIME_FIELD, startTime.getTimeInMillis() / 1000);
                Calendar endTime = (Calendar) result.get(END_TIME_FIELD);
                dict.put(END_TIME_FIELD, endTime.getTimeInMillis() / 1000);
                dict.put(UFN_FIELD, result.get(UFN_FIELD));
                dict.put(OFFICEID_FIELD, result.get(OFFICEID_FIELD));
                Calendar purgeTime = (Calendar) result.get(PURGE_TIME_FIELD);
                dict.put(PURGE_TIME_FIELD, purgeTime.getTimeInMillis() / 1000);
                Calendar issueTime = (Calendar) result.get(ISSUE_TIME_FIELD);
                dict.put(ISSUE_TIME_FIELD, issueTime.getTimeInMillis() / 1000);
                dict.put("state", "Decoded");
                dict.put(SITE_ID_DICT_FIELD, result.get(SITE_ID_DICT_FIELD));

                dict.put(PIL_FIELD, result.get(PIL_FIELD));
                dict.put(PRODUCT_CLASS_FIELD, result.get(PRODUCT_CLASS_FIELD));

                dict.put(ID_FIELD, ugc.trim());

                dict.put(RAW_MESSAGE_DICT_FIELD, result.get(RAW_MESSAGE_FIELD));
                dict.put(COUNTY_HEADER_FIELD, result.get(COUNTY_HEADER_FIELD));
                Calendar floodBegin = (Calendar) result.get(FLOOD_BEGIN_FIELD);
                if (floodBegin != null) {
                    long floodBeginMillis = floodBegin.getTimeInMillis();
                    if (floodBeginMillis != 0) {
                        dict.put(FLOOD_BEGIN_FIELD, floodBeginMillis / 1000);
                    }
                }
                dict.put(WMO_ID_FIELD, result.get(WMO_ID_FIELD));

                // Warngen fields
                Calendar floodCrest = (Calendar) result.get(FLOOD_CREST_FIELD);
                if (floodCrest != null) {
                    long floodCrestMillis = floodCrest.getTimeInMillis();
                    if (floodCrestMillis != 0) {
                        dict.put(FLOOD_CREST_FIELD, floodCrestMillis / 1000);
                    }
                }
                Calendar floodEnd = (Calendar) result.get(FLOOD_BEGIN_FIELD);
                if (floodEnd != null) {
                    long floodEndMillis = floodEnd.getTimeInMillis();
                    if (floodEndMillis != 0) {
                        dict.put(FLOOD_BEGIN_FIELD, floodEndMillis / 1000);
                    }
                }
                String floodStatus = (String) result
                        .get(FLOOD_RECORD_STATUS_FIELD);
                if (floodStatus != null && !"".equals(floodStatus.trim())) {
                    dict.put(FLOOD_RECORD_STATUS_DICT_FIELD.toLowerCase(),
                            floodStatus);
                }
                String floodSeverity = (String) result
                        .get(FLOOD_SEVERITY_FIELD);
                if (floodSeverity != null && !"".equals(floodSeverity.trim())) {
                    dict.put(FLOOD_SEVERITY_FIELD.toLowerCase(), floodSeverity);
                }

                Geometry geometry = (Geometry) result.get(GEOM_FIELD);
                if (geometry != null && !geometry.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    String sep = "";
                    long lat;
                    long lon;
                    for (Coordinate coordinate : geometry.getCoordinates()) {
                        sb.append(sep);
                        sep = " ";
                        lat = Math.round(Math.abs(coordinate.y) * 100.0);
                        lon = Math.round(Math.abs(coordinate.x) * 100.0);
                        sb.append(String.format("%d %d", lat, lon));
                    }
                    dict.put(GEOM_FIELD, sb.toString());
                }

                String immediateCause = (String) result
                        .get(IMMEDIATE_CAUSE_FIELD);
                if (immediateCause != null && !"".equals(immediateCause.trim())) {
                    dict.put(IMMEDIATE_CAUSE_FIELD, immediateCause);
                }

                String loc = (String) result.get(LOC_FIELD);
                if (loc != null && !"".equals(loc.trim())) {
                    dict.put(LOC_FIELD, loc);
                }

                String locationId = (String) result.get(LOCATION_ID_FIELD);
                if (locationId != null && !"".equals(locationId.trim())) {
                    dict.put(LOCATION_ID_DICT_FIELD, locationId);
                }

                Integer motdir = (Integer) result.get(MOTION_DIRECTION_FIELD);
                if (motdir != null) {
                    dict.put(MOTION_DIRECTION_FIELD, motdir);
                }

                Integer motspd = (Integer) result.get(MOTION_SPEED_FIELD);
                if (motspd != null) {
                    dict.put(MOTION_SPEED_FIELD, motspd);
                }

                dicts.add(dict);
            }

        }
        return dicts;
    }

    private Class<? extends AbstractWarningRecord> getWarningClass(
            boolean practice) {
        return practice ? PracticeWarningRecord.class : WarningRecord.class;
    }

    @Override
    protected Map<String, String> getHeadlineLookUpMap() {
        // TODO Auto-generated method stub
        return null;
    }

}
