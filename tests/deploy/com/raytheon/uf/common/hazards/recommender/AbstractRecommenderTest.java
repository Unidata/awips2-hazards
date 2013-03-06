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

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import jep.JepException;

import org.junit.Before;
import org.junit.BeforeClass;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.PathManagerFactoryTest;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.recommenders.AbstractRecommenderEngine;
import com.raytheon.uf.common.recommenders.EventRecommender;
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
 * Feb 27, 2013            mnash     Initial creation
 * 
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
    }

    private AbstractRecommenderEngine<CAVERecommenderScriptManager> engine;

    @BeforeClass
    public static void classSetUp() {
        PathManagerFactoryTest.initLocalization();
        fillFiles();
    }

    @Before
    public void setUp() throws JepException {
        engine = new CAVERecommenderEngine();
    }

    public List<IEvent> runRecommender(String name,
            IPythonJobListener<List<IEvent>> listener) {
        try {
            for (EventRecommender rec : engine.getInventory()) {
                if (rec.getName().equals(name)) {
                    engine.runEntireRecommender(rec.getName(), listener);
                }
            }
        } catch (Throwable t) {
            fail("Could not run recommender " + t);
        }
        return null;
    }

    public Map<String, String> getDialogInfo(String name) {
        try {
            for (EventRecommender rec : engine.getInventory()) {
                if (rec.getName().equals(name)) {
                    Map<String, String> vals = engine.getDialogInfo(name);
                    return vals;
                }
            }
        } catch (Throwable t) {
            fail("Could not run get dialog info " + t);
        }
        return null;
    }

    public Map<String, String> getSpatialInfo(String name) {
        try {
            for (EventRecommender rec : engine.getInventory()) {
                if (rec.getName().equals(name)) {
                    Map<String, String> vals = engine.getSpatialInfo(name);
                    return vals;
                }
            }
        } catch (Throwable t) {
            fail("Could not run get dialog info " + t);
        }
        return null;
    }

    /**
     * Method to help bring in correct localization files.
     */
    private static void fillFiles() {
        IPathManager manager = PathManagerFactory.getPathManager();
        manager.listFiles(manager.getContext(LocalizationType.COMMON_STATIC,
                LocalizationLevel.BASE), "python", null, true, false);
    }
}
