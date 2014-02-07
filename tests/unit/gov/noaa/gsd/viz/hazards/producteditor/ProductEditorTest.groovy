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
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction
import spock.lang.*

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList

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
 * 
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
class ProductEditorTest extends spock.lang.Specification {
    @Shared GeneratedProductList generatedProductList
    @Shared Object[] forecastGroup
    @Shared TestProductEditorView testView;
    @Shared EventBus eventBus;
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
        
        testView = new TestProductEditorView();
        eventBus = new EventBus();
        eventBus.register(this);
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

        ProductEditorPresenter presenter = new ProductEditorPresenter(sessionManager, testView, createEventBus());
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

        ProductEditorPresenter presenter = new ProductEditorPresenter(sessionManager, testView, createEventBus());
        presenter.showProductEditorDetail(generatedProductList);

        when:"The user presses the dismiss button"

        testView.dismissButtonPressed();

        then: "The dialog will close"
    }

    /**
     * Simulate the product display shell being closed.
     * Check for an appropriate message being sent over the
     * event bus.
     */
    def "Shell Closed" (){

        ProductEditorPresenter presenter = new ProductEditorPresenter(sessionManager, testView, createEventBus());
        presenter.showProductEditorDetail(generatedProductList);

        when:"The user closes the shell"

        testView.shellClosed();

        then: "The dialog will close"
    }



    @Subscribe
    public void productStagingActionOccurred(
            ProductEditorAction action) {
        this.action = action;
    }

    private EventBus createEventBus() {
        EventBus eventBus = new EventBus();
        eventBus.register(this);
        return eventBus;
    }
}
