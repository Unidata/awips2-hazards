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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.requests;

import com.raytheon.uf.common.dataplugin.events.hazards.request.HazardEventQueryRequest;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * 
 * Request object used to query the interoperability table
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2015 6895     Ben.Phillippe Routing registry requests through request server
 * May 06, 2016 18202      Robert.Blum Changes for operational mode.
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@DynamicSerialize
public class InteroperabilityRecordQueryRequest
        extends HazardEventQueryRequest {

    /**
     * Creates a new InteroperabilityRecordQueryRequest
     */
    public InteroperabilityRecordQueryRequest(boolean practice) {
        super(practice);
    }

    /**
     * Creates a new InteroperabilityRecordQueryRequest
     * 
     * @param practice
     *            Practice mode flag
     * @param key
     *            The field to query on
     * @param value
     *            The value of the field
     */
    public InteroperabilityRecordQueryRequest(boolean practice, String key,
            Object value) {
        super(practice, key, value);
    }

    /**
     * Creates a new InteroperabilityRecordQueryRequest
     * 
     * @param practice
     *            Practice mode flag
     * @param key
     *            The field to query on
     * @param values
     *            The array of values of the field
     */
    public InteroperabilityRecordQueryRequest(boolean practice, String key,
            Object[] values) {
        super(practice, key, values);
    }

    /**
     * Creates a new InteroperabilityRecordQueryRequest
     * 
     * @param practice
     *            Practice mode flag
     * @param key
     *            The field to query on
     * @param operand
     *            The operand of the comparison
     * @param value
     *            The value of the field
     */
    public InteroperabilityRecordQueryRequest(boolean practice, String key,
            String operand, Object value) {
        super(practice, key, operand, value);
    }

    /**
     * Creates a new InteroperabilityRecordQueryRequest
     * 
     * @param practice
     *            Practice mode flag
     * @param key
     *            The field to query on
     * @param operand
     *            The operand of the comparison
     * @param values
     *            The array of values of the field
     */
    public InteroperabilityRecordQueryRequest(boolean practice, String key,
            String operand, Object[] values) {
        super(practice, key, operand, values);
    }

}
