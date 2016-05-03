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

import static org.junit.Assert.fail;
import gov.noaa.gsd.viz.hazards.utilities.FileUtilities;

import java.io.Serializable;
import java.util.Map;

import jep.JepException;

import org.junit.Before;
import org.junit.BeforeClass;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.localization.PathManagerFactoryTest;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.viz.recommenders.CAVERecommenderEngine;
import com.raytheon.uf.viz.recommenders.CAVERecommenderScriptManager;

/**
 * Tests the recommenders
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 27, 2013            mnash       Initial creation
 * Jul 19, 2013 1257       bsteffen    Convert recommender dialog info to use
 *                                     Serializeables for values instead of
 *                                     Strings.
 * Dec 3, 2013  1472       bkowal      Remove ignore annotation.
 * Apr 14, 2014 3422       bkowal      Updated to use the alternate getInventory method.
 * Aug 18, 2014 4243       Chris.Golden Changed to use new version of getInventory().
 * Jan 29, 2015 3626       Chris.Golden Changes to allow event type to
 *                                      be passed to a recommender.
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
public abstract class AbstractRecommenderTest {

    protected volatile boolean proceed = false;

    /**
     * 
     */
    public AbstractRecommenderTest() {
        FileUtilities.fillFiles();
    }

    private AbstractRecommenderEngine<CAVERecommenderScriptManager> engine;

    @BeforeClass
    public static void classSetUp() {
        PathManagerFactoryTest.initLocalization();
    }

    @Before
    public void setUp() throws JepException {
        engine = CAVERecommenderEngine.getInstance();
    }

    public EventSet<IEvent> runRecommender(String name,
            EventSet<IEvent> eventSet,
            IPythonJobListener<EventSet<IEvent>> listener) {
        try {
            EventRecommender rec = engine.getInventory(name);
            if (rec != null) {
                engine.runEntireRecommender(rec.getName(), eventSet, listener);
            }
        } catch (Throwable t) {
            fail("Could not run recommender " + t);
        }
        return null;
    }

    public Map<String, Serializable> getDialogInfo(String name,
            EventSet<IEvent> eventSet) {
        try {
            if (engine.getInventory(name) != null) {
                return engine.getDialogInfo(name, eventSet);
            }
        } catch (Throwable t) {
            fail("Could not run get dialog info " + t);
        }
        return null;
    }

    public Map<String, Serializable> getSpatialInfo(String name) {
        try {
            if (engine.getInventory(name) != null) {
                return engine.getSpatialInfo(name);
            }
        } catch (Throwable t) {
            fail("Could not run get dialog info " + t);
        }
        return null;
    }

}
