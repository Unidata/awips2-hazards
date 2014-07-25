/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import static org.junit.Assert.*
import static org.mockito.Mockito.*
import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus
import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction
import net.engio.mbassy.bus.config.BusConfiguration
import net.engio.mbassy.listener.Handler
import spock.lang.*

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction
import com.raytheon.uf.common.hazards.productgen.GeneratedProduct
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager

/**
 * Description: Tests the product display dialog. Simulates 
 *              button presses and makes sure the appropriate
 *              messages are sent across the event bus.
 * 
 * <pre>
 * 
 * SOFTWAREs HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 06, 2012            bryon.lawrence      Initial creation
 * Jul 15, 2013     585    Chris.Golden        Changed to use non-singleton event bus.
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - elimination of IHazardsIF now requires a non-null 
 *                                                            sessionManager sent to presenter constructor
 * Feb 07, 2014 2890       bkowal       Product Generation JSON refactor.
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
class ProductEditorTest extends spock.lang.Specification {
    @Shared GeneratedProductList generatedProductList
    @Shared Object[] forecastGroup
    @Shared TestProductEditorView testView;
    @Shared ProductEditorAction action;
    @Shared ISessionManager sessionManager;

    /**
     * Set up the test...
     * 
     * @param
     * @return
     */
    def setupSpec() {
        generatedProductList = new GeneratedProductList();
        generatedProductList.setProductInfo('Test')
        generatedProductList.add(new GeneratedProduct('Test'))

        testView = new TestProductEditorView();
        sessionManager = mock(ISessionManager.class)
        when(sessionManager.getTimeManager()).thenReturn(mock(ISessionTimeManager.class))
    }

    /**
     * Simulate the continue button being pressed. Test for
     * an appropriate message on the event bus.
     * 
     * @param
     * @return
     */
    def "Issue Button Pressed"() {

        ProductEditorPresenter presenter = new ProductEditorPresenter(sessionManager, createEventBus());
        presenter.setView(testView);
        presenter.showProductEditorDetail(generatedProductList);

        when:"The user presses the issue button"

        testView.issueButtonPressed();

        then: "The action sent over the event bus should not be null"

        this.action != null

        and: "The action should be an Issue action"

        this.action.getHazardAction() == HazardAction.ISSUE
    }

    /**
     * Simulate the dismiss button being pressed.
     * Check for an appropriate message being sent over
     * the event bus.
     * @param
     * @return
     */
    def "Dismiss Button Pressed" (){

        ProductEditorPresenter presenter = new ProductEditorPresenter(sessionManager, createEventBus());
        presenter.setView(testView);
        presenter.showProductEditorDetail(generatedProductList);

        when:"The user presses the dismiss button"

        testView.dismissButtonPressed();

        then: "The dialog will close"
    }

    @Handler
    public void productStagingActionOccurred(
            ProductEditorAction action) {
        this.action = action;
    }

    private BoundedReceptionEventBus<Object> createEventBus() {
        BoundedReceptionEventBus<Object> eventBus = new BoundedReceptionEventBus<>(BusConfiguration.Default(0), new IRunnableAsynchronousScheduler() {
                    public void schedule(Runnable runnable) {
                        /* No action; no asynchronously-published messages are posted to the event bus by the product editor presenter. */
                    }
                });
        eventBus.subscribe(this);
        return eventBus;
    }
}
