/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.uf.common.recommenders.hydro;

import gov.noaa.gsd.uf.common.recommenders.hydro.riverfloodrecommender.RiverProFloodRecommender;
import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import com.raytheon.uf.common.hazards.hydro.HazardSettings;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
import com.raytheon.uf.common.hazards.hydro.RiverProDataManager;

/**
 * Description: TODO
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * MMM DD, YYYY            bryon.lawrence      Initial creation
 * May 1, 2014  3581       bkowal      Updated to use common hazards hydro
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class RiverFloodRecommenderRecordTest extends TestCase {
    @Test
    public void testRecordStatus() {
        Mockery context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final RiverForecastPoint forecastPoint = context
                .mock(RiverForecastPoint.class);
        final HazardSettings hazardSettings = context
                .mock(HazardSettings.class);

        context.checking(new Expectations() {
            {
                one(forecastPoint).getPhysicalElement();
                will(returnValue("HG"));
                one(forecastPoint).getMaximumObservedForecastValue();
                will(returnValue(20.0));
                one(forecastPoint).getFloodCategory();
                will(returnValue(new double[] { 0d, 5d, 10d, 15d, 20d }));
                one(hazardSettings).getVtecRecordStageOffset();
                will(returnValue(2.0d));
            }
        });

        RiverProDataManager riverProDataManager = new RiverProDataManager(
                new FloodRecommenderDCTN1TestDAO());
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                riverProDataManager);
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings,
                forecastPoint);
        assertEquals("NR", recordStatus);
    }

    @Test
    public void testNearRecordStatus() {
        Mockery context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final RiverForecastPoint forecastPoint = context
                .mock(RiverForecastPoint.class);
        final HazardSettings hazardSettings = context
                .mock(HazardSettings.class);

        context.checking(new Expectations() {
            {
                one(forecastPoint).getPhysicalElement();
                will(returnValue("HG"));
                one(forecastPoint).getMaximumObservedForecastValue();
                will(returnValue(18.0));
                one(forecastPoint).getFloodCategory();
                will(returnValue(new double[] { 0d, 5d, 10d, 15d, 20d }));
                one(hazardSettings).getVtecRecordStageOffset();
                will(returnValue(2.0d));
            }
        });

        RiverProDataManager riverProDataManager = new RiverProDataManager(
                new FloodRecommenderDCTN1TestDAO());
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                riverProDataManager);
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings,
                forecastPoint);
        assertEquals("NR", recordStatus);
    }

    @Test
    public void testNearRecordStatus2() {
        Mockery context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final RiverForecastPoint forecastPoint = context
                .mock(RiverForecastPoint.class);
        final HazardSettings hazardSettings = context
                .mock(HazardSettings.class);

        context.checking(new Expectations() {
            {
                one(forecastPoint).getPhysicalElement();
                will(returnValue("HG"));
                one(forecastPoint).getMaximumObservedForecastValue();
                will(returnValue(17.9));
                one(forecastPoint).getFloodCategory();
                will(returnValue(new double[] { 0d, 5d, 10d, 15d, 20d }));
                one(hazardSettings).getVtecRecordStageOffset();
                will(returnValue(2.0d));
            }
        });

        RiverProDataManager riverProDataManager = new RiverProDataManager(
                new FloodRecommenderDCTN1TestDAO());
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                riverProDataManager);
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings,
                forecastPoint);
        assertEquals("NO", recordStatus);
    }

    @Test
    public void testFloodWithoutPeriodOfRecord() {
        Mockery context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final RiverForecastPoint forecastPoint = context
                .mock(RiverForecastPoint.class);
        final HazardSettings hazardSettings = context
                .mock(HazardSettings.class);

        context.checking(new Expectations() {
            {
                one(forecastPoint).getPhysicalElement();
                will(returnValue("HG"));
                one(forecastPoint).getMaximumObservedForecastValue();
                will(returnValue(17.9));
                one(forecastPoint).getFloodCategory();
                will(returnValue(new double[] { 0d, 5d, 10d, 15d,
                        RiverForecastPoint.MISSINGVAL }));
                one(hazardSettings).getVtecRecordStageOffset();
                will(returnValue(2.0d));
            }
        });

        RiverProDataManager riverProDataManager = new RiverProDataManager(
                new FloodRecommenderDCTN1TestDAO());
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                riverProDataManager);
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings,
                forecastPoint);
        assertEquals("UU", recordStatus);
    }

    @Test
    public void testFloodWithoutPeriodOfRecord2() {
        Mockery context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };

        final RiverForecastPoint forecastPoint = context
                .mock(RiverForecastPoint.class);
        final HazardSettings hazardSettings = context
                .mock(HazardSettings.class);

        context.checking(new Expectations() {
            {
                one(forecastPoint).getPhysicalElement();
                will(returnValue("HG"));
                one(forecastPoint).getMaximumObservedForecastValue();
                will(returnValue(RiverForecastPoint.MISSINGVAL));
                one(forecastPoint).getFloodCategory();
                will(returnValue(new double[] { 0d, 5d, 10d, 15d,
                        RiverForecastPoint.MISSINGVAL }));
                one(hazardSettings).getVtecRecordStageOffset();
                will(returnValue(2.0d));
            }
        });

        RiverProDataManager riverProDataManager = new RiverProDataManager(
                new FloodRecommenderDCTN1TestDAO());
        RiverProFloodRecommender recommender = new RiverProFloodRecommender(
                riverProDataManager);
        String recordStatus = recommender.retrieveFloodRecord(hazardSettings,
                forecastPoint);
        assertEquals("UU", recordStatus);
    }

}
