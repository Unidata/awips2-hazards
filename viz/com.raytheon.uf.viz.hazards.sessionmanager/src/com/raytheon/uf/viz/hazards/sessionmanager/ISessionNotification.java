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
package com.raytheon.uf.viz.hazards.sessionmanager;

import com.raytheon.uf.viz.hazards.sessionmanager.impl.ISessionNotificationSender;

import gov.noaa.gsd.common.utilities.IMergeable;

/**
 * Methods that must be implemented in order to create a class that embodies
 * session notifications that are posted synchronously or asynchronously, or are
 * enqueued for later posting in a batch, by an instance of
 * {@link ISessionNotificationSender}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Made extend new IMergeable interface.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public interface ISessionNotification extends IMergeable<ISessionNotification> {
}
