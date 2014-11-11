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

import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter.Command;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import java.util.List;

/**
 * Description: Defines the interface that a concrete ProductStagingView must
 * implement.
 * <p>
 * A product staging view must provide a way of displaying potential products
 * for issuance.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence    Initial creation
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Apr 11, 2014  2819      Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 08, 2014  2925      Chris.Golden      Changed to work with MVP framework changes.
 * Oct 06, 2014  4042      Chris.Golden      Completely revamped to work with new two-
 *                                           step version of product staging dialog. Also
 *                                           fixed to adhere more closely to the MVP
 *                                           design that has evolved substantially.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IProductStagingView {

    // Public Methods

    /**
     * Set the associated events state change handler, which is notified when
     * the list of hazard events to be associated with a specific product
     * changes due to user manipulation. The identifier of the handler is the
     * product name.
     * 
     * @param handler
     *            Associated events state change handler to be used.
     */
    public void setAssociatedEventsChangeHandler(
            IStateChangeHandler<String, List<String>> handler);

    /**
     * Set the product metadata state change handler, which is notified when
     * metadata for a specific product changes due to user manipulation. The
     * qualifier is the product having its associated metadata state changed,
     * while the identifier is the metadata identifier that is experiencing the
     * state change.
     * 
     * @param handler
     *            Product metadata state change handler to be used.
     */
    public void setProductMetadataChangeHandler(
            IQualifiedStateChangeHandler<String, String, Object> handler);

    /**
     * Set the button invocation handler, which is notified when the buttons at
     * the bottom of the dialog are invoked by the user. The identifier is the
     * command.
     * 
     * @param handler
     *            Button invocation handler to be used.
     */
    public void setButtonInvocationHandler(
            ICommandInvocationHandler<Command> handler);
}
