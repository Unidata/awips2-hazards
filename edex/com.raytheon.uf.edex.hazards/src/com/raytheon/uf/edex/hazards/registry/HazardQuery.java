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
package com.raytheon.uf.edex.hazards.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oasis.names.tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.registry.IResultFormatter;
import com.raytheon.uf.common.registry.ebxml.AdhocRegistryQuery;
import com.raytheon.uf.common.registry.ebxml.StringAttribute;
import com.raytheon.uf.common.registry.ebxml.encoder.IRegistryEncoder;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.vividsolutions.jts.geom.Geometry;

/**
 * The basic ebXML query for HazardEvents
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 4, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class HazardQuery extends AdhocRegistryQuery<HazardEvent> implements
        IResultFormatter<HazardEvent> {

    @DynamicSerializeElement
    private Geometry geometry;

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.registry.RegistryQuery#getObjectType()
     */
    @Override
    public Class<HazardEvent> getObjectType() {
        return HazardEvent.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.common.registry.RegistryQuery#getResultType()
     */
    @Override
    public Class<HazardEvent> getResultType() {
        return HazardEvent.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.registry.IResultFormatter#decodeObject(oasis.names
     * .tc.ebxml.regrep.xsd.rim.v4.RegistryObjectType)
     */
    @Override
    public HazardEvent decodeObject(RegistryObjectType registryObjectType,
            IRegistryEncoder encoder) throws SerializationException {
        HazardEvent object = (HazardEvent) encoder
                .decodeObject(registryObjectType);
        if (geometry != null && object.getGeometry() != null
                && geometry.intersects(object.getGeometry()) == false) {
            return null;
        }
        return object;
    }

    public void setFilters(Map<String, List<Object>> filters) {
        for (Entry<String, List<Object>> entry : filters.entrySet()) {
            List<String> vals = new ArrayList<String>();
            for (Object value : entry.getValue()) {
                vals.add(value.toString());
            }
            StringAttribute attr = new StringAttribute(vals);
            setAttribute(entry.getKey(), attr);
        }
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geom) {
        geometry = geom;
    }
}
