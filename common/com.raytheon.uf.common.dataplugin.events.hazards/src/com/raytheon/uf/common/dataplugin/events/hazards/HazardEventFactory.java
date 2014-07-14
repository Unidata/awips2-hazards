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

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Concrete implementation of {@link IHazardEventFactory} to return a
 * {@link HazardEvent}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 12, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardEventFactory implements IHazardEventFactory {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataplugin.events.hazards.IHazardEventFactory#
     * getHazardEvent()
     */
    @Override
    public IHazardEvent getHazardEvent() {
        return new HazardEvent();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.dataplugin.events.hazards.IHazardEventFactory#
     * getHazardEvent
     * (com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent)
     */
    @Override
    public IHazardEvent getHazardEvent(IHazardEvent event) {
        return new HazardEvent(event);
    }

}
