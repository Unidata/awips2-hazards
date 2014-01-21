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
package com.raytheon.uf.common.recommenders;

import java.io.Serializable;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;

/**
 * Tests the recommender python override / merge capability.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 24, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractRecommenderOverrideTest extends
        AbstractRecommenderTest {
    private static final String DICT_KEY_TEST = "test";

    private String recommenderName;

    private String expectedKeyValue;

    /**
     * 
     */
    public AbstractRecommenderOverrideTest(String recommenderName,
            String expectedKeyValue) {
        this.recommenderName = recommenderName;
        this.expectedKeyValue = expectedKeyValue;
    }

    @Test
    public void run() {
        super.runRecommender(this.recommenderName, this.getPythonJobListener());
        while (proceed == false) {
            // sit and wait
        }
    }

    @Test
    public void runGetDialogInfo() {
        Map<String, Serializable> vals = getDialogInfo(this.recommenderName);
        assertNotNull(vals);
        assertThat((String) vals.get(DICT_KEY_TEST),
                equalTo(this.expectedKeyValue));
    }

    @Test
    public void runGetSpatialInfo() {
        Map<String, Serializable> vals = getDialogInfo(this.recommenderName);
        assertNotNull(vals);
        assertThat((String) vals.get(DICT_KEY_TEST),
                equalTo(this.expectedKeyValue));
    }

    private IPythonJobListener<EventSet<IEvent>> getPythonJobListener() {
        IPythonJobListener<EventSet<IEvent>> listener = new IPythonJobListener<EventSet<IEvent>>() {
            @Override
            public void jobFailed(Throwable e) {
                fail(e.getMessage());
                proceed = true;
            }

            @Override
            public void jobFinished(EventSet<IEvent> result) {
                assertNotNull(result);
                assertThat(result, hasSize(1));
                assertTrue("Event not of type BaseHazardEvent", result
                        .iterator().next() instanceof BaseHazardEvent);
                proceed = true;
            }
        };

        return listener;
    }
}