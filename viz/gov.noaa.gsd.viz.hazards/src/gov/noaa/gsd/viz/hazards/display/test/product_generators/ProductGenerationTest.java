/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test.product_generators;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_END_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_PHEN;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SHAPES;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SIG;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_START_TIME;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_STATUS;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HAZARD_EVENT_SUB_TYPE;
import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.POINTS;
import gov.noaa.gsd.common.utilities.DateTimes;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.test.FunctionalTest;
import gov.noaa.gsd.viz.hazards.jsonutilities.ComparableLazilyParsedNumber;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.hazards.productstaging.ProductConstants;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import net.engio.mbassy.listener.Handler;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

import com.raytheon.uf.common.activetable.request.ClearPracticeVTECTableRequest;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.requests.ThriftClient;
import com.raytheon.uf.viz.hazards.sessionmanager.events.InvalidGeometryException;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventAdded;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventStatusModified;
import com.raytheon.uf.viz.hazards.sessionmanager.product.IProductGenerationComplete;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Description: Tests of the product generators to ensure they are producing the
 * expected products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 14, 2014  3525       daniel.s.schaffer@noaa.gov      Initial creation
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductGenerationTest extends
        FunctionalTest<ProductGenerationTest.Steps> {

    private static final String HAZARD_SERVICES_SOURCE = "HAZARD_SERVICES_SOURCE";

    private static final String PRODUCT_GENERATOR_TESTS_FILENAME = "tests.json";

    private static final String PRODUCT_GENERATOR_TESTS_DIR = "productGenerator_Tests";

    private static final String HAZARD_SERVICES_TESTS_DIR = "tests";

    private static final String EXPECTED_ANSWER_FILENAME_SUFFIX = ".expected";

    private static final String HAZARD_EVENT_OVERRIDES = "hazardEventOverrides";

    private static final String TEST_DESCRIPTION = "commentary";

    private static final String TEST_NAME = "name";;

    private final DateTimes dateTimes = new DateTimes();

    private String baseTestDir;

    private String productGeneratorName;

    private String testName;

    private DictList tests;

    private IHazardEvent hazardEvent;

    private int testIndex;

    private boolean newHazard;

    private Boolean stopOnError;

    private StringBuilder expectedAnswers;

    private StringBuilder actualAnswers;

    private boolean updateCorrectAnswer;

    protected enum Steps {
        START, PREVIEW, ISSUE
    }

    public ProductGenerationTest(HazardServicesAppBuilder appBuilder,
            String productGeneratorName) {
        super(appBuilder);
        try {
            this.productGeneratorName = productGeneratorName;
            String hazardServicesSourceDir = System
                    .getenv(HAZARD_SERVICES_SOURCE);
            baseTestDir = FileUtil.join(hazardServicesSourceDir,
                    HAZARD_SERVICES_TESTS_DIR, PRODUCT_GENERATOR_TESTS_DIR);
            hazardEvent = buildBaseHazardEvent();

            this.testIndex = 0;
            step = Steps.START;
            String testConfigFilename = FileUtil.join(baseTestDir,
                    productGeneratorName, PRODUCT_GENERATOR_TESTS_FILENAME);
            String testsAsString = Utils.textFileAsString(testConfigFilename);
            this.tests = DictList.getInstance(testsAsString);
            this.newHazard = true;
            this.expectedAnswers = new StringBuilder();
            this.actualAnswers = new StringBuilder();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Override
    protected void runFirstStep() {
        try {
            clearActiveTableIfNecessary();
            updateHazardForTest();
            hazardEventBuilder.addEvent(hazardEvent);

            /**
             * TODO Will have to be smarter about this once test cases can
             * define more than one event
             */
            hazardEvent = eventManager.getEvents().iterator().next();

        } catch (Exception e) {
            handleException(e);
        }

    }

    @Handler(priority = -1)
    public void sessionEventAddedOccurred(SessionEventAdded action) {

        try {
            preview();
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * TODO Very messy; probably because of extra notifications in the
     * SessionManager. Consider the case of a 2 step test. The first step
     * previews and issues a new hazard. The second continues the hazard. We
     * need to wait for the status to be changed to issued before starting the
     * next test. Otherwise, what can happen is that, asynchronously,
     * SessionHazardNotificationListener#handleNotification calls
     * SessionEventUtilities.mergeHazardEvents. That method takes the contents
     * of the hazard associated that was stored to the DB and merges it into the
     * hazard existing in memory with the corresponding eventID. But that means
     * that if this test changes that memory version before this event occurs
     * then the merge will overwrite that change causing a test failure.
     * However, in a case where the hazard state is not changing from pending to
     * issued then this event does not occur. In that case we rely on the
     * handleProductGeneratorResult below to do the next test. That's why there
     * is messy logic involving the instance variable "newHazard".
     */
    @Handler(priority = -1)
    public void handleSessionEventStatusModified(
            SessionEventStatusModified action) {
        if (newHazard) {
            nextTestIfExists();
            newHazard = false;
        }
    }

    private void nextTestIfExists() {
        if (testIndex < tests.size()) {
            updateHazardForTest();
            preview();
        }

        else {
            resetEvents();
            testSuccess();
        }
    }

    @Handler(priority = -1)
    public void handleProductGeneratorResult(
            final IProductGenerationComplete productGenerationComplete) {
        try {
            if (step == Steps.PREVIEW) {
                checkOrUpdateAnswer();
                step = Steps.ISSUE;
                autoTestUtilities.issueFromProductEditor(mockProductEditorView);

            } else {
                if (!newHazard) {
                    nextTestIfExists();
                }
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    void setStopOnError(Boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    private void preview() {
        step = Steps.PREVIEW;
        autoTestUtilities.previewFromHID();
    }

    private void checkOrUpdateAnswer() {

        try {
            String expectedAnswerFilename = testName
                    + EXPECTED_ANSWER_FILENAME_SUFFIX;
            String expectedAnswerPath = FileUtil.join(baseTestDir,
                    productGeneratorName, expectedAnswerFilename);
            String expectedAnswer = Utils.textFileAsString(expectedAnswerPath,
                    true);
            expectedAnswers.append(expectedAnswer);

            final String actualAnswer = productFromGeneratorResult();
            actualAnswers.append(actualAnswer);

            if (stopOnError) {
                assertEquals(actualAnswer, expectedAnswer);
            } else if (!actualAnswer.equals(expectedAnswer)
                    && updateCorrectAnswer) {
                Files.write(Paths.get(expectedAnswerPath),
                        (actualAnswer + "\n").getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected exception updating expected answer ", e);
        }
    }

    private String productFromGeneratorResult() {
        List<GeneratedProductList> generatedProductListStorage = mockProductEditorView
                .getGeneratedProductsList();
        GeneratedProductList generatedProductList = generatedProductListStorage
                .get(0);

        IGeneratedProduct generatedProduct0 = generatedProductList
                .get(NumberUtils.INTEGER_ZERO);
        final String product = generatedProduct0.getEntries()
                .get(ProductConstants.ASCII_PRODUCT_KEY)
                .get(NumberUtils.INTEGER_ZERO).toString();
        return product;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private IHazardEvent buildBaseHazardEvent() throws InvalidGeometryException {
        String json = Utils.textFileAsString(
                FileUtil.join(baseTestDir, productGeneratorName, "base.json"),
                true);
        DictList dl = DictList.getInstance(json);
        Dict dict = (Dict) dl.get(0);
        List<Coordinate> coordinates = coordinatesFromShapes(dict);
        IHazardEvent hazardEvent = hazardEventBuilder
                .buildPolygonHazardEvent(coordinates);
        Date date = extractDate(dict, HazardConstants.HAZARD_EVENT_START_TIME);
        hazardEvent.setStartTime(date);
        date = extractDate(dict, HazardConstants.HAZARD_EVENT_END_TIME);
        hazardEvent.setEndTime(date);
        String str = dict
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_PHEN);
        hazardEvent.setPhenomenon(str);
        str = dict.getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_SIG);
        hazardEvent.setSignificance(str);
        str = dict
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_SUB_TYPE);
        hazardEvent.setSubType(str);
        hazardEvent.setSiteID("OAX");
        Dict attributes = dict.getDynamicallyTypedValue("attributes");
        for (Entry<String, Object> entry : attributes.entrySet()) {
            hazardEvent.addHazardAttribute(entry.getKey(),
                    (Serializable) entry.getValue());
        }
        return hazardEvent;
    }

    @SuppressWarnings("unchecked")
    private List<Coordinate> coordinatesFromShapes(Dict dict) {
        List<Dict> shapes = (List<Dict>) (dict.get(HAZARD_EVENT_SHAPES));
        List<List<ComparableLazilyParsedNumber>> points = (List<List<ComparableLazilyParsedNumber>>) shapes
                .get(0).get(POINTS);
        List<Coordinate> coordinates = new ArrayList<>();
        for (List<ComparableLazilyParsedNumber> coordinate : points) {
            coordinates.add(new Coordinate(coordinate.get(0).doubleValue(),
                    coordinate.get(1).doubleValue()));
        }
        return coordinates;
    }

    private Date extractDate(Dict dict, String key) {
        String dateAsString = dict.getDynamicallyTypedValue(key);
        DateTime dateTime = dateTimes.newDateTime(dateAsString);
        Date date = dateTime.toDate();
        return date;
    }

    private void updateHazardForTest() {
        Dict test = (Dict) tests.get(testIndex);
        testIndex += 1;

        testName = test.getDynamicallyTypedValue(TEST_NAME);
        String string = "Test: " + testName;
        log(string);
        System.out.println(string);

        string = "Description: "
                + test.getDynamicallyTypedValue(TEST_DESCRIPTION);
        log(string);
        System.out.println(string);

        List<Dict> allHazardEventOverrides = test
                .getDynamicallyTypedValue(HAZARD_EVENT_OVERRIDES);
        if (allHazardEventOverrides != null) {
            Dict hazardEventOverrides = allHazardEventOverrides.get(0);
            for (String key : hazardEventOverrides.keySet()) {
                switch (key) {

                case HAZARD_EVENT_START_TIME:
                    Date startTime = new DateTime(
                            hazardEventOverrides.getDynamicallyTypedValue(key))
                            .toDate();
                    hazardEvent.setStartTime(startTime);
                    break;

                case HAZARD_EVENT_END_TIME:
                    Date endTime = new DateTime(
                            hazardEventOverrides.getDynamicallyTypedValue(key))
                            .toDate();
                    hazardEvent.setEndTime(endTime);
                    break;

                case HAZARD_EVENT_PHEN:
                    String phenonmenon = hazardEventOverrides
                            .getDynamicallyTypedValue(key);
                    hazardEvent.setPhenomenon(phenonmenon);
                    break;

                case HAZARD_EVENT_SIG:
                    String significance = hazardEventOverrides
                            .getDynamicallyTypedValue(key);
                    hazardEvent.setSignificance(significance);
                    break;

                case HAZARD_EVENT_SUB_TYPE:
                    String subType = hazardEventOverrides
                            .getDynamicallyTypedValue(key);
                    hazardEvent.setSubType(subType);
                    break;

                case HAZARD_EVENT_SHAPES:
                    List<Coordinate> coordinates = coordinatesFromShapes(hazardEventOverrides);
                    hazardEvent.setGeometry(hazardEventBuilder
                            .geometryFromCoordinates(coordinates));
                    break;

                case HAZARD_EVENT_STATUS:
                    String statusAsString = hazardEventOverrides
                            .getDynamicallyTypedValue(key);
                    HazardStatus status = HazardConstants
                            .hazardStatusFromString(statusAsString);
                    if (status.equals(HazardStatus.ENDING)) {
                        hazardEvent.setStatus(status);

                    }

                    break;

                default:
                    Serializable value = hazardEventOverrides
                            .getDynamicallyTypedValue(key);
                    hazardEvent.addHazardAttribute(key, value);
                }
            }
        }

    }

    private void log(String string) {
        expectedAnswers.append(string);
        expectedAnswers.append("\n");
        actualAnswers.append(string);
        actualAnswers.append("\n");
    }

    private void clearActiveTableIfNecessary() throws VizException {
        /**
         * TODO Make this run only if requested from test config rather than
         * every time.
         */
        ClearPracticeVTECTableRequest request = new ClearPracticeVTECTableRequest(
                SiteMap.getInstance().getSite4LetterId(hazardEvent.getSiteID()),
                VizApp.getWsId());
        ThriftClient.sendRequest(request);
    }

    public StringBuilder getExpectedAnswers() {
        return expectedAnswers;
    }

    public StringBuilder getActualAnswers() {
        return actualAnswers;
    }

    public void setUpdateCorrectAnswer(boolean updateCorrectAnswer) {
        this.updateCorrectAnswer = updateCorrectAnswer;
    }

}
