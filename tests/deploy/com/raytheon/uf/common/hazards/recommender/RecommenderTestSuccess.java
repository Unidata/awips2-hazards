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
package com.raytheon.uf.common.hazards.recommender;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;

/**
 * Unit test for recommenders, successfully returns an IEvent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderTestSuccess extends AbstractRecommenderTest {

    @Test
    public void run() {
        IPythonJobListener<List<IEvent>> listener = new IPythonJobListener<List<IEvent>>() {
            @Override
            public void jobFailed(Throwable e) {
                fail(e.getMessage());
            }

            @Override
            public void jobFinished(List<IEvent> result) {
                assertNotNull(result);
                assertThat(result, hasSize(1));
                assertTrue("Event not of type BaseHazardEvent",
                        result.get(0) instanceof BaseHazardEvent);
                proceed = true;
            }
        };
        runRecommender("TestRecommenderSuccess", listener);
        while (proceed == false) {
            // sit and wait
        }
    }
}
