/**
 * A framework for the automated tests.  These tests are run from
 * from within hazard services via selection "Run Automated Tests"
 * in the "View Menu".
 * This selection is only available when the environment variable
 * HAZARD_SERVICES_AUTO_TESTS_ENABLED is set (in eclipse.sh for example).
 * 
 * The framework leverages the same {@link com.google.common.eventbus.EventBus}s as the code under
 * test. The test code will typically subscribe to the same kinds of
 * events as the code under test.  For example, 
 * {@link com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated} 
 * That way, when an event arrives
 * indicating a product has been generated, the test code receives that event
 * and thus knows that it can now test that the product information to 
 * be displayed in the GUI is correct. 
 *
 * The framework assumes the following.
 * Suppose object A registers first for event E on the EventBus.
 * Suppose object B registers for event E on the EventBus.
 * Then when E occurs, A will be notified before B.
 * This seems to be holding up in practice so far.  If we have only been
 * lucky then we will want to figure out a way to allow for the event
 * bus to have this behavior.
 *
 * The solution currently has the down side that the code under
 * test (in {@link gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder}) needs to know about the test
 * classes.  We will want to make this into an extension point to
 * mitigate this dependency.
 * 
 * The {@link gov.noaa.gsd.viz.hazards.display.test.FunctionalTest} uses
 * some JUnit equivalent methods
 * like assertFalse.  I tried to bring in the JUnit jar directly via
 * plugin but then all kinds of strange problems occurred.  We may want to revisit this. 
 *
 * Whenever a test fails, an alert is displayed via AlertViz.  A stack
 * trace is also shown in the Console that pinpoints the line in the
 * test where the assertion failed.
 *
 * If tests all pass you should something like the following in the Eclipse
 * console when it finishes.
 * DEBUG 2013-10-22 17:31:46,577 [main] CaveLogger: SampleFunctionalTest Successful
 * DEBUG 2013-10-22 17:31:46,874 [main] CaveLogger: All tests passed!!!!
 * 
 * To add a test, make a copy of {@link gov.noaa.gsd.viz.hazards.display.test.SampleFunctionalTest} with your chosen name.  
 * Then fill in the details of the test.  Finally, add an else if clause 
 * to {@link gov.noaa.gsd.viz.hazards.display.test.AutomatedTests#testCompleted} that looks
 * like the other clauses.
 */
package gov.noaa.gsd.viz.hazards.display.test;


