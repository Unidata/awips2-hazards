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

import java.util.List;

/**
 * Description: List of all {@ProductGenerationTest]s to be run.
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
public class ProductGenerationTestSuite {

    private List<String> tests;

    /**
     * @return the tests
     */
    public List<String> getTests() {
        return tests;
    }

    /**
     * @param tests
     *            the tests to set
     */
    public void setTests(List<String> tests) {
        this.tests = tests;
    }

}
