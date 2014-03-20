package com.raytheon.uf.common.hazards.productgen;

import static org.junit.Assert.assertEquals;
import jep.Jep;
import jep.JepException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Description: Tests the hazard recommendation based on a complex hydrograph
 * which oscillates around flood stage.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 7, 2012            bryon.lawrence      Initial creation
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class RiverProTemplateVariableTest {

    static private Jep jep;

    /*
     * For these tests, we only care about the base version of the
     * RiverForecastPoints module.
     */
    private static final String PYTHON_PATHS = "/awips2/edex/data/utility/common_static/base/python/events/productgen/riverPointUtilities:/awips2/edex/data/utility/common_static/base/python";

    @BeforeClass
    static public void setUp() throws Exception {
        /*
         * Build a JEP instance
         */
        try {
            jep = new Jep(false, PYTHON_PATHS,
                    RiverProTemplateVariableTest.class.getClassLoader());
            jep.eval("import JavaImporter");
            jep.eval("from gov.noaa.gsd.uf.common.recommenders.hydro import FloodRecommenderDCTN1TestDAO");
            jep.eval("from RiverForecastPoints import RiverForecastPoints");
            jep.eval("floodDAO = FloodRecommenderDCTN1TestDAO()");
            jep.eval("riverForecastPoints = RiverForecastPoints(floodDAO)");
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    static public void testsComplete() {
        jep.close();
    }

    /**
     * Tests the retrieval of the observed stage for the <ObsStg> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedStageTest() {
        try {
            jep.eval("obsStage, qualityCode = riverForecastPoints.getObsStg('DCTN1')");
            Object obsStage = jep.getValue("obsStage");
            Object qualityCode = jep.getValue("qualityCode");
            assertEquals(39.04f, obsStage);
            assertEquals("Z", qualityCode);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsCat> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedCategoryTest() {
        try {
            jep.eval("obsCategory = riverForecastPoints.getObsCat('DCTN1')");
            Object obsCategory = jep.getValue("obsCategory");
            assertEquals(2, obsCategory);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsCatName> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedCategoryNameTest() {
        try {
            jep.eval("obsCategoryName = riverForecastPoints.getObsCatName('DCTN1')");
            Object obsCategoryName = jep.getValue("obsCategoryName");
            assertEquals("MODERATE", obsCategoryName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsTime> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedTimeTest() {
        try {
            jep.eval("obsTime = riverForecastPoints.getObsTime('DCTN1')");
            Object obsTime = jep.getValue("obsTime");
            assertEquals(1297137600000L, obsTime);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MaxFcstStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMaximumForecastStageTest() {
        try {
            jep.eval("maximumForecastStage, qualityCode = riverForecastPoints.getMaxFcstStage('DCTN1')");
            Object maximumForecastStage = jep.getValue("maximumForecastStage");
            Object qualityCode = jep.getValue("qualityCode");
            assertEquals(39.91f, maximumForecastStage);
            assertEquals("Z", qualityCode);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MaxFcstStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMaximumForecastCategoryTest() {
        try {
            jep.eval("maximumForecastCategory = riverForecastPoints.getMaxFcstCat('DCTN1')");
            Object maximumForecastCategory = jep
                    .getValue("maximumForecastCategory");
            assertEquals(2, maximumForecastCategory);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MaxFcstStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMaximumForecastCategoryNameTest() {
        try {
            jep.eval("maximumForecastCategoryName = riverForecastPoints.getMaxFcstCatName('DCTN1')");
            Object maximumForecastCategoryName = jep
                    .getValue("maximumForecastCategoryName");
            assertEquals("MODERATE", maximumForecastCategoryName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MaxFcstStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMaximumForecastForecastTimeTest() {
        try {
            jep.eval("maximumForecastTime = riverForecastPoints.getMaxFcstTime('DCTN1')");
            Object maximumForecastTime = jep.getValue("maximumForecastTime");
            assertEquals(1297188000000L, maximumForecastTime);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <getOMFVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedMaximumForecastValueTest() {
        try {
            jep.eval("observedMaximumForecastValue = riverForecastPoints.getOMFVal('DCTN1')");
            Object observedMaximumForecastValue = jep
                    .getValue("observedMaximumForecastValue");
            assertEquals(39.91f, observedMaximumForecastValue);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <getOMFVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObservedMaximumForecastCatNameTest() {
        try {
            jep.eval("observedMaximumForecastCatName = riverForecastPoints.getOMFCatName('DCTN1')");
            Object observedMaximumForecastCatName = jep
                    .getValue("observedMaximumForecastCatName");
            assertEquals("MODERATE", observedMaximumForecastCatName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <getOMFVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getStageTrendTest() {
        try {
            jep.eval("stageTrend = riverForecastPoints.getStgTrend('DCTN1')");
            Object stageTrend = jep.getValue("stageTrend");
            assertEquals("RISING", stageTrend);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsCrestStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObsCrestTest() {
        try {
            jep.eval("obsCrestStg = riverForecastPoints.getObsCrestStg('DCTN1')");
            Object obsCrestStg = jep.getValue("obsCrestStg");
            assertEquals(-9999.0f, obsCrestStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsCrestTime> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObsCrestTimeTest() {
        try {
            jep.eval("obsCrestTime = riverForecastPoints.getObsCrestTime('DCTN1')");
            Object obsCrestTime = jep.getValue("obsCrestTime");
            assertEquals(null, obsCrestTime);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <FcstCrestStg> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getFcstCrestStgTest() {
        try {
            jep.eval("fcstCrestStg = riverForecastPoints.getFcstCrestStg('DCTN1')");
            Object fcstCrestStg = jep.getValue("fcstCrestStg");
            assertEquals(39.91f, fcstCrestStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <FcstCrestTime> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getFcstCrestTimeTest() {
        try {
            jep.eval("fcstCrestTime = riverForecastPoints.getFcstCrestTime('DCTN1')");
            Object fcstCrestTime = jep.getValue("fcstCrestTime");
            assertEquals("Tue Feb 08 18:00:00 GMT 2011",
                    fcstCrestTime.toString());
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsRiseFSTime> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObsRiseFSTimeTest() {
        try {
            jep.eval("obsRiseFSTime = riverForecastPoints.getObsRiseFSTime('DCTN1')");
            Object obsRiseFSTime = jep.getValue("obsRiseFSTime");
            assertEquals("Mon Feb 07 19:15:24 GMT 2011",
                    obsRiseFSTime.toString());
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsRiseFSTime> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObsFallFSTimeTest() {
        try {
            jep.eval("obsFallFSTime = riverForecastPoints.getObsFallFSTime('DCTN1')");
            Object obsFallFSTime = jep.getValue("obsFallFSTime");
            assertEquals(null, obsFallFSTime);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <FcstRiseFSTime> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getFcstRiseFSTimeTest() {
        try {
            jep.eval("fcstRiseFSTime = riverForecastPoints.getFcstRiseFSTime('DCTN1')");
            Object fcstRiseFSTime = jep.getValue("fcstRiseFSTime");
            assertEquals(null, fcstRiseFSTime);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <FcstFallFSTime> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getFcstFallFSTimeTest() {
        try {
            jep.eval("fcstFallFSTime = riverForecastPoints.getFcstFallFSTime('DCTN1')");
            Object fcstFallFSTime = jep.getValue("fcstFallFSTime");
            assertEquals("Wed Feb 09 05:07:49 GMT 2011",
                    fcstFallFSTime.toString());
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ObsFSDeparture> Template
     * variable
     * 
     * @param
     * @return
     */
    @Test
    public void getObsFSDepartureTest() {
        try {
            jep.eval("obsFSDeparture = riverForecastPoints.getObsFSDeparture('DCTN1')");
            Object obsFSDeparture = jep.getValue("obsFSDeparture");
            assertEquals(4.040001f, obsFSDeparture);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <NumObsH> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getNumObsHTest() {
        try {
            jep.eval("numObsStg = riverForecastPoints.getNumObsStg('DCTN1')");
            Object numObsStg = jep.getValue("numObsStg");
            assertEquals(5, numObsStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <NumFcstH> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getNumFcstHTest() {
        try {
            jep.eval("numFcstStg = riverForecastPoints.getNumFcstStg('DCTN1')");
            Object numFcstStg = jep.getValue("numFcstStg");
            assertEquals(20, numFcstStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MinCatVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMinCatValTest() {
        try {
            jep.eval("minCatVal = riverForecastPoints.getMinCatVal('DCTN1')");
            Object minCatVal = jep.getValue("minCatVal");
            assertEquals(35.0f, minCatVal);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <ModCatVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getModCatValTest() {
        try {
            jep.eval("modCatVal = riverForecastPoints.getModCatVal('DCTN1')");
            Object modCatVal = jep.getValue("modCatVal");
            assertEquals(38.0f, modCatVal);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <MajCatVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getMajCatValTest() {
        try {
            jep.eval("majCatVal = riverForecastPoints.getMajCatVal('DCTN1')");
            Object majCatVal = jep.getValue("majCatVal");
            assertEquals(41.0f, majCatVal);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the value for the <RecCatVal> Template variable
     * 
     * @param
     * @return
     */
    @Test
    public void getRecCatValTest() {
        try {
            jep.eval("recCatVal = riverForecastPoints.getRecCatVal('DCTN1')");
            Object recCatVal = jep.getValue("recCatVal");
            assertEquals(43.5f, recCatVal);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <Id> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getIdTest() {
        try {
            jep.eval("Id = riverForecastPoints.getId('DCTN1')");
            Object Id = jep.getValue("Id");
            assertEquals("DCTN1", Id);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <IdName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getIdNameTest() {
        try {
            jep.eval("IdName = riverForecastPoints.getIdName('DCTN1')");
            Object IdName = jep.getValue("IdName");
            assertEquals("Decatur", IdName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <County> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getCountyTest() {
        try {
            jep.eval("county = riverForecastPoints.getCounty('DCTN1')");
            Object county = jep.getValue("county");
            assertEquals("Burt", county);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <StateId> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getStateIdTest() {
        try {
            jep.eval("stateId = riverForecastPoints.getStateId('DCTN1')");
            Object stateId = jep.getValue("stateId");
            assertEquals("NE", stateId);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <StateName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getStateNameTest() {
        try {
            jep.eval("stateName = riverForecastPoints.getStateName('DCTN1')");
            Object stateName = jep.getValue("stateName");
            assertEquals("Nebraska", stateName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <River> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getRiverTest() {
        try {
            jep.eval("river = riverForecastPoints.getRiver('DCTN1')");
            Object river = jep.getValue("river");
            assertEquals("Missouri River", river);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <Proximity> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getProximityTest() {
        try {
            jep.eval("proximity = riverForecastPoints.getProximity('DCTN1')");
            Object proximity = jep.getValue("proximity");
            assertEquals("At", proximity);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <FldStg> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getFldStgTest() {
        try {
            jep.eval("fldStg = riverForecastPoints.getFldStg('DCTN1')");
            Object fldStg = jep.getValue("fldStg");
            assertEquals(35.0f, fldStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <BankStg> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getBankStgTest() {
        try {
            jep.eval("bankStg = riverForecastPoints.getBankStg('DCTN1')");
            Object bankStg = jep.getValue("bankStg");
            assertEquals(35.0f, bankStg);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <WStg> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getWStgTest() {
        try {
            jep.eval("warningStage = riverForecastPoints.getWStag('DCTN1')");
            Object warningStage = jep.getValue("warningStage");
            assertEquals(33.0f, warningStage);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <FldFlow> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getFldFlowTest() {
        try {
            jep.eval("floodFlow = riverForecastPoints.getFldFlow('DCTN1')");
            Object floodFlow = jep.getValue("floodFlow");
            assertEquals(115688.0f, floodFlow);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <ZDatum> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getZDatumTest() {
        try {
            jep.eval("zDatum = riverForecastPoints.getZDatum('DCTN1')");
            Object zDatum = jep.getValue("zDatum");
            assertEquals(1010, zDatum);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <FldFlow> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getStgFlowNameTest() {
        try {
            jep.eval("stageFlowName = riverForecastPoints.getStgFlowName('DCTN1')");
            Object stageFlowName = jep.getValue("stageFlowName");
            assertEquals("stage", stageFlowName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <StgFlowUnits> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getStgFlowUnitsTest() {
        try {
            jep.eval("stageFlowUnits = riverForecastPoints.getStgFlowUnits('DCTN1')");
            Object stageFlowUnits = jep.getValue("stageFlowUnits");
            assertEquals("feet", stageFlowUnits);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <ImpCompUnits> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getImpCompUnitsTest() {
        try {
            jep.eval("impCompUnits = riverForecastPoints.getImpCompUnits('DCTN1')");
            Object impCompUnits = jep.getValue("impCompUnits");
            assertEquals("feet", impCompUnits);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <LocLat> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getLocLatTest() {
        try {
            jep.eval("latitude = riverForecastPoints.getLocLat('DCTN1')");
            Object latitude = jep.getValue("latitude");
            assertEquals(42.007928f, latitude);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <LocLon> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getLocLonTest() {
        try {
            jep.eval("longitude = riverForecastPoints.getLocLon('DCTN1')");
            Object longitude = jep.getValue("longitude");
            assertEquals(96.242898f, longitude);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpId> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpIdTest() {
        try {
            jep.eval("groupId = riverForecastPoints.getGrpId('DCTN1')");
            Object groupId = jep.getValue("groupId");
            assertEquals("MISORIV", groupId);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpIdName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpIdNameTest() {
        try {
            jep.eval("groupName = riverForecastPoints.getGrpIdName('DCTN1')");
            Object groupName = jep.getValue("groupName");
            assertEquals("Missouri River", groupName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpIdName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpFPListTest() {
        try {
            jep.eval("forecastPointList = riverForecastPoints.getGrpFPList('DCTN1')");
            Object forecastPointList = jep.getValue("forecastPointList");
            assertEquals("Decatur", forecastPointList);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpMaxCurCat> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpMaxCurCatTest() {
        try {
            jep.eval("maxCat = riverForecastPoints.getGrpMaxCurCat('DCTN1')");
            Object maxCat = jep.getValue("maxCat");
            assertEquals(2, maxCat);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpMaxCurCatName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpMaxCurCatNameTest() {
        try {
            jep.eval("maxCatName = riverForecastPoints.getGrpMaxCurCatName('DCTN1')");
            Object maxCatName = jep.getValue("maxCatName");
            assertEquals("MODERATE", maxCatName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpMaxFcstCat> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpMaxFcstCatTest() {
        try {
            jep.eval("maxCat = riverForecastPoints.getGrpMaxFcstCat('DCTN1')");
            Object maxCat = jep.getValue("maxCat");
            assertEquals(2, maxCat);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpMaxFcstCatName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpMaxFcstCatNameTest() {
        try {
            jep.eval("maxCatName = riverForecastPoints.getGrpMaxFcstCatName('DCTN1')");
            Object maxCatName = jep.getValue("maxCatName");
            assertEquals("MODERATE", maxCatName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpOMFCat> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpOMFCatTest() {
        try {
            jep.eval("maxCat = riverForecastPoints.getGrpOMFCat('DCTN1')");
            Object maxCat = jep.getValue("maxCat");
            assertEquals(2, maxCat);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpOMFCatName> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpOMFCatNameTest() {
        try {
            jep.eval("maxCatName = riverForecastPoints.getGrpOMFCatName('DCTN1')");
            Object maxCatName = jep.getValue("maxCatName");
            assertEquals("MODERATE", maxCatName);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpFcstFound> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpFcstFoundTest() {
        try {
            jep.eval("fcstFound = riverForecastPoints.getGrpFcstFound('DCTN1')");
            Object fcstFound = jep.getValue("fcstFound");
            assertEquals(true, fcstFound);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <NumGrps> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getNumGrpsTest() {
        try {
            jep.eval("numberOfGroups = riverForecastPoints.getNumGrps()");
            Object numberOfGroups = jep.getValue("numberOfGroups");
            assertEquals(1, numberOfGroups);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpList> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpList() {
        try {
            jep.eval("groupList = riverForecastPoints.getGrpList()");
            Object groupList = jep.getValue("groupList");
            assertEquals("Missouri River", groupList);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <RiverList> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getRiverList() {
        try {
            jep.eval("riverList = riverForecastPoints.getRiverList()");
            Object riverList = jep.getValue("riverList");
            assertEquals("Missouri River", riverList);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests the retrieval of the <GrpsFPList> template variable.
     * 
     * @param
     * @return
     */
    @Test
    public void getGrpsFPList() {
        try {
            jep.eval("riverList = riverForecastPoints.getGrpsFPList()");
            Object riverList = jep.getValue("riverList");
            assertEquals("Missouri River At Decatur", riverList);
        } catch (JepException e) {
            e.printStackTrace();
        }
    }

}
