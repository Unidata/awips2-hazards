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
import org.junit.Ignore;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager.Mode;
import com.raytheon.uf.common.serialization.comm.RequestRouterTest;

/**
 * Tests registry interaction of IHazardEvents
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 30, 2013            mnash     Initial creation
 * Dec 3, 2013  1472       bkowal    Re-enable the test.
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@Ignore
public class HazardRegistryTest extends AbstractHazardStorageTest {

    @BeforeClass
    public static void classSetUp() {
        RequestRouterTest.setDeployInstance();
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
        return Mode.OPERATIONAL;
    }

}
