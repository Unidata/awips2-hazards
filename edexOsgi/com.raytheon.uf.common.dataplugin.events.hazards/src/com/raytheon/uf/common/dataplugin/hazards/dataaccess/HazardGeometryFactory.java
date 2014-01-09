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
package com.raytheon.uf.common.dataplugin.hazards.dataaccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanMap;

import com.raytheon.uf.common.dataaccess.IDataRequest;
import com.raytheon.uf.common.dataaccess.exception.DataRetrievalException;
import com.raytheon.uf.common.dataaccess.exception.TimeAgnosticDataException;
import com.raytheon.uf.common.dataaccess.exception.UnsupportedOutputTypeException;
import com.raytheon.uf.common.dataaccess.geom.IGeometryData;
import com.raytheon.uf.common.dataaccess.grid.IGridData;
import com.raytheon.uf.common.dataaccess.impl.AbstractDataFactory;
import com.raytheon.uf.common.dataaccess.impl.DefaultGeometryData;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardQueryBuilder;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.collections.HazardHistoryList;
import com.raytheon.uf.common.time.BinOffset;
import com.raytheon.uf.common.time.DataTime;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Retrieve hazard events from the system.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 25, 2013            mnash     Initial creation
 * Oct 31, 2013 #2177      bkowal    Relocated to a different package to match other factories.
 *                                   Altered usage of the parameter field - now corresponds to
 *                                   the JavaDoc in IDataRequest. The parameter field now may
 *                                   contain a list of attributes / fields to retrieve; if it does not,
 *                                   all attributes & fields will be returned.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardGeometryFactory extends AbstractDataFactory {

    private static final String PLUGIN_NAME = "hazards";

    private static final String MODE = "mode";

    private static final String SITEID = HazardConstants.SITE_ID;

    private static final String EVENTID = HazardConstants.HAZARD_EVENT_IDENTIFIER;

    private static final String HAZARDMODE = HazardConstants.HAZARD_MODE;

    private static final String STATE = HazardConstants.HAZARD_EVENT_STATE;

    private static final String HAZARD_TYPE = "hazardType";

    private static final String FIELD_HAZARD_ATTRIBUTES_SERIALIZABLE = "hazardAttributesSerializable";

    private static final String FIELD_HAZARD_ATTRIBUTES = "hazardAttributes";

    private static final String[] VALID_INDENTIFIERS = new String[] { MODE,
            HAZARD_TYPE, EVENTID, SITEID, STATE, HAZARDMODE };

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
        return makeGeometryData(map, request.getParameters()).toArray(
                new IGeometryData[0]);
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

        if (request.getIdentifiers().get(HAZARD_TYPE) != null) {
            if ((request.getIdentifiers().get(HAZARD_TYPE) instanceof List<?>) == false) {
                throw new DataRetrievalException(
                        "Phensigs were not provided in the correct format; expecting List<String>.");
            }
            List<?> phensigs = (List<?>) request.getIdentifiers().get(
                    HAZARD_TYPE);
            for (Object phensig : phensigs) {
                builder.addKey(HazardConstants.PHEN_SIG, phensig.toString());
            }
        }

        for (String key : request.getIdentifiers().keySet()) {
            if (MODE.equals(key) == false && HAZARD_TYPE.equals(key) == false) {
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
        return VALID_INDENTIFIERS;
    }

    private List<IGeometryData> makeGeometryData(
            Map<String, HazardHistoryList> list, String[] hazardAttributes) {
        boolean retrieveAllAttributes = hazardAttributes.length == 0;
        List<IGeometryData> datas = new ArrayList<IGeometryData>();
        for (HazardHistoryList lst : list.values()) {
            for (IHazardEvent event : lst.getEvents()) {
                BeanMap beanMap = new BeanMap(event);
                if (retrieveAllAttributes) {
                    List<String> allAttributes = new ArrayList<String>();
                    Iterator<?> keyIterator = beanMap.keyIterator();
                    while (keyIterator.hasNext()) {
                        String key = keyIterator.toString();
                        /*
                         * skip the hazard attributes because they are added
                         * separately.
                         */
                        if (FIELD_HAZARD_ATTRIBUTES_SERIALIZABLE.equals(key)
                                || FIELD_HAZARD_ATTRIBUTES.equals(key)) {
                            continue;
                        }
                        allAttributes.add(key);
                    }
                    /*
                     * add the attribute names for THIS particular hazard to the
                     * list.
                     */
                    allAttributes.addAll(event.getHazardAttributes().keySet());

                    hazardAttributes = allAttributes
                            .toArray(new String[allAttributes.size()]);
                }

                DefaultGeometryData data = new DefaultGeometryData();
                data.setGeometry(event.getGeometry());
                Map<String, Object> attrs = new HashMap<String, Object>();
                for (String hazardAttribute : hazardAttributes) {
                    // special case for phensig
                    if (hazardAttribute.equals(HAZARD_TYPE)) {
                        attrs.put(hazardAttribute,
                                HazardEventUtilities.getHazardType(event));
                        continue;
                    }

                    // is it an attribute?
                    if (event.getHazardAttributes()
                            .containsKey(hazardAttribute)) {
                        attrs.put(hazardAttribute,
                                event.getHazardAttribute(hazardAttribute));
                    }
                    // is it an actual field associated with the hazard object?
                    else if (beanMap.containsKey(hazardAttribute)) {
                        attrs.put(hazardAttribute, beanMap.get(hazardAttribute));
                    }
                }
                data.setAttributes(attrs);
                datas.add(data);
            }
        }
        return datas;
    }
}