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
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction
import spock.lang.*

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe

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
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
class ProductEditorTest extends spock.lang.Specification {
    @Shared String testProductJSONString = "{generatedProducts:[A,B,C], hazardEventSets:[D,E,F]}"
    @Shared Object[] forecastGroup
    @Shared TestProductEditorView testView;
    @Shared EventBus eventBus;
    @Shared ProductEditorAction action;

    /**
     * Set up the test...
     * 
     * @param
     * @return
     */
    def setupSpec() {
        testView = new TestProductEditorView();
        eventBus = new EventBus();
        eventBus.register(this);
    }

    /**
     * Simulate the continue button being pressed. Test for
     * an appropriate message on the event bus.
     * 
     * @param
     * @return
     */
    def "Issue Button Pressed"() {

        ProductEditorPresenter presenter = new ProductEditorPresenter(null, testView, createEventBus());
        presenter.showProductEditorDetail(testProductJSONString);

        when:"The user presses the issue button"

        testView.issueButtonPressed();

        then: "The action sent over the event bus should not be null"

        this.action != null

        and: "The action should be an Issue action"

        this.action.getAction() == "Issue"
    }

    /**
     * Simulate the dismiss button being pressed.
     * Check for an appropriate message being sent over
     * the event bus.
     * @param
     * @return
     */
    def "Dismiss Button Pressed" (){

        ProductEditorPresenter presenter = new ProductEditorPresenter(null, testView, createEventBus());
        presenter.showProductEditorDetail(testProductJSONString);

        when:"The user presses the dismiss button"

        testView.dismissButtonPressed();

        then: "The action sent over the event bus should not be null"

        this.action != null

        and: "The action should be an Issue action"

        this.action.getAction() == "Dismiss"
    }

    /**
     * Simulate the product display shell being closed.
     * Check for an appropriate message being sent over the
     * event bus.
     */
    def "Shell Closed" (){

        ProductEditorPresenter presenter = new ProductEditorPresenter(null, testView, createEventBus());
        presenter.showProductEditorDetail(testProductJSONString);

        when:"The user closes the shell"

        testView.shellClosed();

        then: "The action sent over the event bus should not be null"

        this.action != null

        and: "The action should be an Issue action"

        this.action.getAction() == "Dismiss"
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
