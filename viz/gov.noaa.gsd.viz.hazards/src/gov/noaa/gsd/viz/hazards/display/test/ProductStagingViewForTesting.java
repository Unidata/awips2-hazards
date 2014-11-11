/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.productstaging.IProductStagingView;
import gov.noaa.gsd.viz.hazards.productstaging.IProductStagingViewDelegate;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter.Command;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;

/**
 * Description: Mock {@link IProductStagingView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov      Initial creation
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Apr 11, 2014  2819      Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 18, 2014  2925      Chris.Golden      Changed to work with new MVP framework.
 * Jun 30, 2014  3512      Chris.Golden      Changed to work with changes to
 *                                           ICommandInvoker.
 * Oct 02, 2014  4042      Chris.Golden      Changed to support two-step product
 *                                           staging dialog.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ProductStagingViewForTesting implements
        IProductStagingViewDelegate<Action, RCPMainUserInterfaceElement> {

    private List<String> productNames;

    private Map<String, List<String>> possibleEventIdsForProductNames;

    private Map<String, List<String>> possibleEventDescriptionsForProductNames;

    private Map<String, List<String>> selectedEventIdsForProductNames;

    @Override
    public void dispose() {

        /*
         * No action.
         */
    }

    @Override
    public List<Action> contributeToMainUI(RCPMainUserInterfaceElement type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public void setAssociatedEventsChangeHandler(
            IStateChangeHandler<String, List<String>> handler) {

        /*
         * No action.
         */
    }

    @Override
    public void setProductMetadataChangeHandler(
            IQualifiedStateChangeHandler<String, String, Object> handler) {

        /*
         * No action.
         */
    }

    @Override
    public void setButtonInvocationHandler(
            ICommandInvocationHandler<Command> handler) {

        /*
         * This is weird but deliberate. It seems weird, but essentially what is
         * being done here is that as soon as the mock dialog is brought up, the
         * presenter's process of binding event handlers to the dialog causes
         * the dialog to immediately fire the invocation handler supplied. This
         * allows the mock product editor to come up, which in turn causes a
         * ProductGenerationComplete notification to go out, which can in turn
         * be caught by the functional test using this mock view.
         */
        handler.commandInvoked(Command.CONTINUE);
    }

    @Override
    public void showFirstStep(List<String> productNames,
            Map<String, List<String>> possibleEventIdsForProductNames,
            Map<String, List<String>> possibleEventDescriptionsForProductNames,
            Map<String, List<String>> selectedEventIdsForProductNames) {
        this.productNames = productNames;
        this.possibleEventIdsForProductNames = possibleEventIdsForProductNames;
        this.possibleEventDescriptionsForProductNames = possibleEventDescriptionsForProductNames;
        this.selectedEventIdsForProductNames = selectedEventIdsForProductNames;
    }

    @Override
    public void showSecondStep(
            List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime,
            boolean firstStepSkipped) {

        /*
         * No action.
         */
    }

    @Override
    public void hide() {

        /*
         * No action.
         */
    }

    /**
     * Get the names of the products being staged.
     * 
     * @return Product names.
     */
    public List<String> getProductNames() {
        return productNames;
    }

    /**
     * Get the list of all possible event identifiers for the specified product.
     * 
     * @param productName
     *            Product name.
     * @return List of all possible event identifiers.
     */
    public List<String> getAllPossibleEventIdentifiersForProductName(
            String productName) {
        return possibleEventIdsForProductNames.get(productName);
    }

    /**
     * Get the list of all possible event descriptions for the specified
     * product.
     * 
     * @param productName
     *            Product name.
     * @return List of all possible event descriptions.
     */
    public List<String> getAllPossibleEventDescriptionsForProductName(
            String productName) {
        return possibleEventDescriptionsForProductNames.get(productName);
    }

    /**
     * Get the list of selected event identifiers for the specified product.
     * 
     * @param productName
     *            Product name.
     * @return List of selected event identifiers.
     */
    public List<String> getSelectedEventIdentifiersForProductName(
            String productName) {
        return selectedEventIdsForProductNames.get(productName);
    }
}
