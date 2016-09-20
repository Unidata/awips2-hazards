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
package com.raytheon.uf.edex.hazards.interop.warngen;

import gov.noaa.gsd.common.utilities.geometry.AdvancedGeometryUtilities;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.interoperability.HazardInteroperabilityConstants.INTEROPERABILITY_TYPE;
import com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord;
import com.raytheon.uf.edex.hazards.interop.AbstractLegacyAppInteropSrv;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Allows for warning compatibility with interoperability. Creates IHazardEvent
 * objects from AbstractWarningRecords.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 11, 2013            mnash        Initial creation
 * Jan 15, 2014 2755       bkowal       Exception handling for failed hazard event
 *                                      id generation.
 * Apr 08, 2014 3357       bkowal       Updated to use the new interoperability tables.
 * Apr 23, 2014 3357       bkowal       Improved interoperability hazard comparison to prevent duplicate hazard creation.
 * Nov 17, 2014 2826       mpduff       Changed back to pass Warngen for interoperability type of new hazards.
 * Dec 04, 2014 2826       dgilling     Revert previous change, remove unneeded methods.
 * Dec 15, 2014 2826       dgilling     Code cleanup, re-factor.
 * Jan 23, 2015 2826       dgilling     Refactor based on AbstractLegacyAppInteropSrv.
 * Sep 14, 2016 15934      Chris.Golden Changed to work with advanced geometries now used in
 *                                      hazard events.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class WarningHazardsCreator extends AbstractLegacyAppInteropSrv {

    private static final String PHEN_FF = "FF";

    private static final String SIG_W = "W";

    private static final String CONVECTIVE_FL_SEVERITY = "0";

    private static final String SUBTYPE_CONVECTIVE = "Convective";

    private static final String SUBTYPE_NONCONVECTIVE = "NonConvective";

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.hazards.interop.AbstractLegacyAppInteropSrv#
     * performAppSpecificValidation
     * (com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord)
     */
    @Override
    public boolean performAppSpecificValidation(AbstractWarningRecord warning) {
        if (warning.getGeometry() == null) {
            statusHandler
                    .warn("Skipping record because it has no valid geometry.");
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.hazards.interop.AbstractLegacyAppInteropSrv#
     * getInteroperabilityType()
     */
    @Override
    public INTEROPERABILITY_TYPE getInteroperabilityType() {
        return INTEROPERABILITY_TYPE.WARNGEN;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.uf.edex.hazards.interop.AbstractLegacyAppInteropSrv#
     * addAppSpecificHazardAttributes
     * (com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent,
     * com.raytheon.uf.common.dataplugin.warning.AbstractWarningRecord,
     * com.raytheon
     * .uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager)
     */
    @Override
    protected IHazardEvent addAppSpecificHazardAttributes(IHazardEvent event,
            AbstractWarningRecord warningRecord, IHazardEventManager manager) {
        event.setGeometry(AdvancedGeometryUtilities.createGeometryWrapper(
                new GeometryFactory()
                        .createGeometryCollection(new Geometry[] { warningRecord
                                .getGeometry() }), 0));

        String floodSeverity = warningRecord.getFloodSeverity();
        if (floodSeverity != null) {
            String subType = determineSubType(warningRecord);
            if (subType != null) {
                event.setSubType(subType);
            }
        }

        return event;
    }

    private static String determineSubType(final AbstractWarningRecord record) {
        if ((record.getFloodSeverity() == null)
                || (!PHEN_FF.equals(record.getPhen()))
                || (!SIG_W.equals(record.getSig()))) {
            return null;
        }

        return CONVECTIVE_FL_SEVERITY.equals(record.getFloodSeverity()) ? SUBTYPE_CONVECTIVE
                : SUBTYPE_NONCONVECTIVE;
    }
}