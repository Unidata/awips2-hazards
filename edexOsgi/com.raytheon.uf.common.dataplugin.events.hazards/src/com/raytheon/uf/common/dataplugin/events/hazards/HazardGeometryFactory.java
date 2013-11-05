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
package com.raytheon.uf.common.dataplugin.events.hazards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.exception.DataRetrievalException;
import com.raytheon.uf.common.dataaccess.exception.TimeAgnosticDataException;
import com.raytheon.uf.common.dataaccess.exception.UnsupportedOutputTypeException;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataaccess.grid.IGridData;
import com.raytheon.uf.common.dataaccess.impl.AbstractDataFactory;
import com.raytheon.uf.common.dataaccess.impl.DefaultGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.time.BinOffset;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Retrieve hazard events from the system.
 * 
 * The parameters are the phensigs, in the form TO.W or FF.W.NonConvective
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 25, 2013            mnash     Initial creation
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardGeometryFactory extends AbstractDataFactory {

    private static final String PLUGIN_NAME = "hazards";

    private static final String MODE = "mode";

    private static Map<IDataRequest, HazardResponse> cachedRequests = new ConcurrentHashMap<IDataRequest, HazardResponse>();

    private static class HazardResponse {
        public Map<String, HazardHistoryList> map;

        public DataTime[] times;
    }

    /**
     * 
     */
    public HazardGeometryFactory() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getAvailableTimes(com.
     * raytheon.uf.common.dataaccess.IDataRequest)
     */
    @Override
    public DataTime[] getAvailableTimes(IDataRequest request)
            throws TimeAgnosticDataException {
        return getHazards(request, new TimeRange()).times;
    }

    /*
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getAvailableTimes(com.
     * raytheon.uf.common.dataaccess.IDataRequest,
     * com.raytheon.uf.common.time.BinOffset)
     */
    @Override
    public DataTime[] getAvailableTimes(IDataRequest request,
            BinOffset binOffset) throws TimeAgnosticDataException {
        return getHazards(request,
                binOffset.getTimeRange(new DataTime(new Date()))).times;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getGridData(com.raytheon
     * .uf.common.dataaccess.IDataRequest,
     * com.raytheon.uf.common.time.DataTime[])
     */
    @Override
    public IGridData[] getGridData(IDataRequest request, DataTime... times) {
        throw new UnsupportedOutputTypeException(request.getDatatype(),
                PLUGIN_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getGridData(com.raytheon
     * .uf.common.dataaccess.IDataRequest,
     * com.raytheon.uf.common.time.TimeRange)
     */
    @Override
    public IGridData[] getGridData(IDataRequest request, TimeRange timeRange) {
        throw new UnsupportedOutputTypeException(request.getDatatype(),
                PLUGIN_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getGeometryData(com.raytheon
     * .uf.common.dataaccess.IDataRequest,
     * com.raytheon.uf.common.time.DataTime[])
     */
    @Override
    public IGeometryData[] getGeometryData(IDataRequest request,
            DataTime... times) {
        Arrays.sort(times);
        TimeRange range = new TimeRange(times[0].getRefTime(),
                times[times.length - 1].getRefTime());
        IGeometryData[] data = getGeometryData(request, range);

        List<IGeometryData> finalData = new ArrayList<IGeometryData>();
        for (IGeometryData d : data) {
            for (DataTime time : times) {
                if (d.getDataTime().equals(time)) {
                    finalData.add(d);
                    break;
                }
            }
        }
        return finalData.toArray(new IGeometryData[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getGeometryData(com.raytheon
     * .uf.common.dataaccess.IDataRequest,
     * com.raytheon.uf.common.time.TimeRange)
     */
    @Override
    public IGeometryData[] getGeometryData(IDataRequest request,
            TimeRange timeRange) {
        Map<String, HazardHistoryList> map = getHazards(request, timeRange).map;
        cachedRequests.remove(request);
        return makeGeometryData(map).toArray(new IGeometryData[0]);
    }

    private HazardResponse getHazards(IDataRequest request, TimeRange timeRange) {
        if (cachedRequests.containsKey(request)) {
            return cachedRequests.get(request);
        }

        if (request.getIdentifiers().get(MODE) == null) {
            throw new DataRetrievalException(
                    "Must pass in mode to determine where to look for hazards");
        }
        HazardEventManager manager = new HazardEventManager(
                Mode.valueOf(request.getIdentifiers().get(MODE).toString()));
        HazardQueryBuilder builder = new HazardQueryBuilder();
        if (timeRange != null && timeRange.getStart().getTime() != 0
                && timeRange.getEnd().getTime() != 0) {
            builder.addKey(HazardConstants.HAZARD_EVENT_START_TIME,
                    timeRange.getStart());
            builder.addKey(HazardConstants.HAZARD_EVENT_END_TIME,
                    timeRange.getEnd());
        }

        if (request.getParameters() != null) {
            for (String param : request.getParameters()) {
                builder.addKey(HazardConstants.PHENSIG, param);
            }
        }

        for (String key : request.getIdentifiers().keySet()) {
            if (MODE.equals(key) == false) {
                builder.addKey(key, request.getIdentifiers().get(key));
            }
        }
        Map<String, HazardHistoryList> list = manager.getEventsByFilter(builder
                .getQuery());
        HazardResponse response = new HazardResponse();
        response.map = new HashMap<String, HazardHistoryList>(list);
        for (String key : list.keySet()) {
            HazardHistoryList lst = new HazardHistoryList();
            lst.add(list.get(key).get(list.get(key).size() - 1));
            response.map.put(key, lst);
            response.times = null;
        }
        cachedRequests.put(request, response);
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataaccess.IDataFactory#getAvailableLocationNames
     * (com.raytheon.uf.common.dataaccess.IDataRequest)
     */
    @Override
    public String[] getAvailableLocationNames(IDataRequest request) {
        Map<String, HazardHistoryList> map = getHazards(request,
                new TimeRange()).map;
        Set<String> sites = new HashSet<String>();
        for (HazardHistoryList list : map.values()) {
            for (IHazardEvent event : list) {
                sites.add(event.getSiteID());
            }
        }
        return sites.toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.dataaccess.impl.AbstractDataFactory#
     * getValidIdentifiers()
     */
    @Override
    public String[] getValidIdentifiers() {
        return new String[] { MODE, HazardConstants.HAZARD_EVENT_IDENTIFIER,
                HazardConstants.SITEID, HazardConstants.HAZARD_EVENT_STATE,
                HazardConstants.HAZARDMODE };
    }

    private List<IGeometryData> makeGeometryData(
            Map<String, HazardHistoryList> list) {
        List<IGeometryData> datas = new ArrayList<IGeometryData>();
        for (HazardHistoryList lst : list.values()) {
            for (IHazardEvent event : lst.getEvents()) {
                DefaultGeometryData data = new DefaultGeometryData();
                data.setGeometry(event.getGeometry());
                Map<String, Object> attrs = new HashMap<String, Object>();
                attrs.put(HazardConstants.HAZARD_EVENT_IDENTIFIER,
                        event.getEventID());
                attrs.put(HazardConstants.HAZARD_EVENT_STATE, event.getState()
                        .name());
                attrs.put(HazardConstants.SITEID, event.getSiteID());
                attrs.put(HazardConstants.HAZARDMODE, event.getHazardMode()
                        .name());
                data.setAttributes(attrs);
                datas.add(data);
            }
        }
        return datas;
    }
}
