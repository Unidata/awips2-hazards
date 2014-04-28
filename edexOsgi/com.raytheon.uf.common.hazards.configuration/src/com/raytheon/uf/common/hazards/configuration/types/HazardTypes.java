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
package com.raytheon.uf.common.hazards.configuration.types;

import java.util.HashMap;

/**
 * JSon compatible object for loading and storing Hazard Types.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2013 1257       bsteffen    Initial creation
 * Apr 28, 2014 3556       bkowal      Relocate to a common plugin.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class HazardTypes extends HashMap<String, HazardTypeEntry> {

    private static final long serialVersionUID = 6324263616750393400L;
}
