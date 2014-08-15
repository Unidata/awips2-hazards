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
package com.raytheon.uf.common.hazards.storage;

import org.junit.BeforeClass;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.geospatial.adapter.GeometryTypeAdapter;
import com.raytheon.uf.common.serialization.DynamicSerializationManager;
import com.raytheon.uf.common.serialization.comm.RequestRouterTest;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Tests database interaction of IHazardEvents
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 30, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardDatabaseTest extends AbstractHazardStorageTest {

    @BeforeClass
    public static void classSetUp() {
        RequestRouterTest.setDeployInstance();
        DynamicSerializationManager.registerAdapter(Geometry.class, new GeometryTypeAdapter());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.hazards.storage.AbstractHazardStorageTest#getMode
     * ()
     */
    @Override
    Mode getMode() {
        return Mode.PRACTICE;
    }
}
