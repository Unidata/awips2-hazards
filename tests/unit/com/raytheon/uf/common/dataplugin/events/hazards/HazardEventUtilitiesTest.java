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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 27, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardEventUtilitiesTest {

    private String etns1 = "[1,2]";

    private String etns2 = "[1]";

    @Test
    public void parseEtnString() {
        List<String> values1 = HazardEventUtilities.parseEtns(etns1);
        assertThat(values1, hasSize(2));
        assertEquals(values1.get(0), "1");
        assertEquals(values1.get(1), "2");
        List<String> values2 = HazardEventUtilities.parseEtns(etns2);
        assertThat(values2, hasSize(1));
        assertEquals(values1.get(0), "1");
    }
}
