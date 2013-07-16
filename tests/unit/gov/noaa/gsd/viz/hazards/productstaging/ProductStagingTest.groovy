/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.productstaging;

import static org.junit.Assert.*
import gov.noaa.gsd.viz.hazards.display.action.ProductStagingAction
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict
import spock.lang.*

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe

/**
 * Description: Tests the product staging dialog. Simulates 
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
class ProductStagingTest extends spock.lang.Specification {
    @Shared String testProductJSONString = "{string: 'Test Product'}"
    @Shared Object[] forecastGroup
    @Shared TestProductStagingView testView;
    @Shared EventBus eventBus;
    @Shared ProductStagingAction action;
    @Shared Dict testDict;

    def setupSpec() {
        testView = new TestProductStagingView();
        eventBus = new EventBus();
        eventBus.register(this);
        testDict = Dict.getInstance(testProductJSONString);
    }

    def "Continue Button Pressed"() {

        ProductStagingPresenter presenter = new ProductStagingPresenter(null, testView, createEventBus());
        presenter.showProductStagingDetail(true, testDict);

        when:"The user presses the continue button"

        testView.continueButtonPressed();

        then: "the 'to be issued flag' should be true"

        testView.isToBeIssued() == true

        and: "The action sent over the event bus should not be null"

        this.action != null

        and: "The action should be a Continue action"

        this.action.getAction() == "Continue"

        and: "The action should contain product information from the test dict"

        String jsonText = action.getJSONText();
        Dict dict = Dict.getInstance(jsonText);
        dict == testDict;
    }

    @Subscribe
    public void productStagingActionOccurred(
            ProductStagingAction action) {
        this.action = action;
    }

    private EventBus createEventBus() {
        EventBus eventBus = new EventBus();
        eventBus.register(this);
        return eventBus;
    }
}
