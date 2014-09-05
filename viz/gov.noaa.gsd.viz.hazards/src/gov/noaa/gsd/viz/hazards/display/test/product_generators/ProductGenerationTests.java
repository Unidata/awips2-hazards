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
import gov.noaa.gsd.viz.hazards.display.test.TestCompleted;

import java.io.File;
import java.util.List;

import net.engio.mbassy.listener.Handler;

import org.codehaus.jackson.map.ObjectMapper;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Description: Handles running of collection of all {@ProductGenerationTest
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * }s
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
        if (testIndex < suite.getTests().size()) {
            runTest();
        }

        else {
            System.out.println("All tests completed");
            appBuilder.getEventBus().unsubscribe(this);
        }
    }

    private void runTest() {
        System.out.println();
        ProductGenerationTest test = new ProductGenerationTest(this.appBuilder,
                suite.getTests().get(testIndex));
        testIndex += 1;
        test.run();
    }
}
