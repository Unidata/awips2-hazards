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

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.display.test.FunctionalTest.StopTesting;
import gov.noaa.gsd.viz.hazards.display.test.TestCompleted;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.io.Files;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Description: Framework for running of collection of all
 * {@link ProductGenerationTest}s. The framework allows you to run in three test
 * modes. In STOP_ON_ERROR mode, the configured tests are run until a difference
 * between the expected and actual values is detected. In RECORD_RESULTS mode,
 * all tests are run even if differences occur but the expected and actual
 * answers are written to a files typically in /tmp for easy comparison using a
 * diff tool such as xxdiff. In UPDATE_EXPECTED_ANSWERS mode, expected answers
 * that differ from the actual answers are updated. This mode should obviously
 * be used with caution.
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 29, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductGenerationTests {

    private enum TEST_MODE {
        STOP_ON_ERROR, RECORD_RESULTS, UPDATE_EXPECTED_ANSWERS
    }

    /**
     * Feel free to change this value during development but remember to leave
     * its value as STOP_ON_ERROR when putting code code up for review.
     */
    private final TEST_MODE testMode = TEST_MODE.STOP_ON_ERROR;

    private static final String ALL_TESTS = "all";

    /**
     * 
     */
    private static final String PRODUCT_GENERATOR_TESTS = "productGenerator_Tests";

    private static final String PRODUCT_GENERATOR_TEST_SUITE_FILE_NAME = "suite.json";

    private static final String TESTS = "tests";

    @SuppressWarnings("unused")
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    private String baseTestDir;

    private ProductGenerationTestSuite suite;

    private ObjectMapper objectMapper;

    private HazardServicesAppBuilder appBuilder;

    private int testIndex;

    private StringBuilder expectedAnswers;

    private StringBuilder actualAnswers;

    private ProductGenerationTest currentTest;

    protected enum Steps {
        START
    }

    public ProductGenerationTests() {

    }

    public void init(HazardServicesAppBuilder appBuilder) {
        try {

            this.appBuilder = appBuilder;
            appBuilder.getEventBus().subscribe(this);
            String hazardServicesSourceDir = System
                    .getenv("HAZARD_SERVICES_SOURCE");
            baseTestDir = FileUtil.join(hazardServicesSourceDir, TESTS,
                    PRODUCT_GENERATOR_TESTS);
            String suiteFilename = FileUtil.join(baseTestDir,
                    PRODUCT_GENERATOR_TEST_SUITE_FILE_NAME);
            File suiteFile = new File(suiteFilename);
            objectMapper = new ObjectMapper();
            suite = objectMapper.readValue(suiteFile,
                    ProductGenerationTestSuite.class);
            List<String> tests = suite.getTests();
            if (tests.size() > 0 && tests.get(0).equals(ALL_TESTS)) {
                tests.clear();
                File baseTestDirFile = new File(baseTestDir);
                File[] files = baseTestDirFile.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) {
                        tests.add(file.getName());
                    }
                }
            }
            this.testIndex = 0;
            suite.setTests(tests);
            expectedAnswers = new StringBuilder();
            actualAnswers = new StringBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Handler(priority = -1)
    public void consoleActionOccurred(final ConsoleAction consoleAction) {
        if (consoleAction.getActionType().equals(
                ConsoleAction.ActionType.RUN_PRODUCT_GENERATION_TESTS)) {
            if (suite.getTests().size() > 0) {
                runTest();
            } else {
                throw new UnsupportedOperationException(
                        "handle the case when none are listed (treat as all");
            }
        }
    }

    @Handler(priority = -1)
    public void testCompleted(final TestCompleted testCompleted) {
        if (testCompleted.getTestClass() == StopTesting.class) {
            System.out.println("Test failure, stopping");
            appBuilder.getEventBus().unsubscribe(this);
            recordResultsIfNecessary();
        } else {
            expectedAnswers.append(currentTest.getExpectedAnswers());
            actualAnswers.append(currentTest.getActualAnswers());
            if (testIndex < suite.getTests().size()) {
                runTest();
            }

            else {
                System.out.println("All tests completed");
                recordResultsIfNecessary();
                appBuilder.getEventBus().unsubscribe(this);
            }
        }
    }

    private void recordResultsIfNecessary() {
        try {
            if (testMode == TEST_MODE.RECORD_RESULTS) {
                File expectedFile = File.createTempFile("expected", null);
                Files.write(expectedAnswers.toString().getBytes(), expectedFile);
                File actualFile = File.createTempFile("actual", null);
                Files.write(actualAnswers.toString().getBytes(), actualFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unexpected error when recording results ", e);
        }
    }

    private void runTest() {
        System.out.println();
        currentTest = new ProductGenerationTest(this.appBuilder, suite
                .getTests().get(testIndex));
        currentTest.setStopOnError(testMode == TEST_MODE.STOP_ON_ERROR);
        currentTest
                .setUpdateCorrectAnswer(testMode == TEST_MODE.UPDATE_EXPECTED_ANSWERS);
        testIndex += 1;
        currentTest.run();
    }
}
