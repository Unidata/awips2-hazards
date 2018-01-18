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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.viz.core.VizApp;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.display.RcpMainUiElement;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter.Command;
import gov.noaa.gsd.viz.hazards.ui.CommandInvocationHandlerDelegate;
import gov.noaa.gsd.viz.hazards.ui.QualifiedStateChangeHandlerDelegate;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChangeHandler;
import gov.noaa.gsd.viz.mvp.widgets.IWidget;

/**
 * Description: Product staging view, which creates and uses a
 * {@link ProductStagingDialog}, acting as the latter's delegate.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            Bryon.Lawrence    Initial creation.
 * Jul 15, 2013     585    Chris.Golden      Changed to support loading from bundle.
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Apr 11, 2014   2819     Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 08, 2014   2925     Chris.Golden      Changed to work with MVP framework changes.
 * Oct 07, 2014   4042     Chris.Golden      Completely refactored to work with a two-step
 *                                           dialog, with the first stage allowing the
 *                                           user to choose additional events to go into
 *                                           each of the products (if applicable), and
 *                                           the second step allowing the user to change
 *                                           any product-generator-specific parameters
 *                                           specified for the products (again, if
 *                                           applicable).
 * Feb 24, 2016   13929    Robert.Blum       Remove first part of staging dialog.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible
 *                                           toolbar contribution code.
 * </pre>
 * 
 * @author bryon.lawrence
 * @version 1.0
 */
public class ProductStagingView implements
        IProductStagingViewDelegate<String, IAction, RcpMainUiElement> {

    // Private Static Constants

    /**
     * Scheduler to be used to make {@link IWidget} handlers get get executed on
     * the main thread. For now, the main thread is the UI thread; when this is
     * changed, this will be rendered obsolete, as at that point there will need
     * to be a blocking queue of {@link Runnable} instances available to allow
     * the new worker thread to be fed jobs. At that point, this should be
     * replaced with an object that enqueues the <code>Runnable</code>s,
     * probably a singleton that may be accessed by the various components in
     * gov.noaa.gsd.viz.hazards and perhaps elsewhere.
     */
    @Deprecated
    private static final IRunnableAsynchronousScheduler RUNNABLE_ASYNC_SCHEDULER = new IRunnableAsynchronousScheduler() {

        @Override
        public void schedule(Runnable runnable) {

            /*
             * Since the UI thread is currently the thread being used for nearly
             * everything, just run any asynchronous tasks there.
             */
            VizApp.runAsync(runnable);
        }
    };

    // Private Variables

    /**
     * Product staging dialog.
     */
    private ProductStagingDialog productStagingDialog = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public ProductStagingView() {

        /*
         * No action.
         */
    }

    // Public Methods

    @Override
    public final void dispose() {
        closeDialog();
    }

    @Override
    public final Map<? extends String, List<? extends IAction>> contributeToMainUi(
            RcpMainUiElement type) {
        return Collections.emptyMap();
    }

    @Override
    public void showStagingDialog(List<String> productNames,
            Map<String, MegawidgetSpecifierManager> megawidgetSpecifierManagersForProductNames,
            long minimumVisibleTime, long maximumVisibleTime) {
        if (productStagingDialog == null) {
            createDialog();
        }
        productStagingDialog.initialize(productNames,
                megawidgetSpecifierManagersForProductNames, minimumVisibleTime,
                maximumVisibleTime);
        productStagingDialog.open();
    }

    @Override
    public void hide() {
        closeDialog();
    }

    @Override
    public void setProductMetadataChangeHandler(
            IQualifiedStateChangeHandler<String, String, Object> handler) {
        productStagingDialog.setProductMetadataChangeHandler(
                new QualifiedStateChangeHandlerDelegate<String, String, Object>(
                        handler, RUNNABLE_ASYNC_SCHEDULER));
    }

    @Override
    public void setButtonInvocationHandler(
            ICommandInvocationHandler<Command> handler) {
        productStagingDialog.setButtonInvocationHandler(
                new CommandInvocationHandlerDelegate<Command>(handler,
                        RUNNABLE_ASYNC_SCHEDULER));
    }

    // Private Methods

    /**
     * Create the product staging dialog.
     */
    private void createDialog() {
        productStagingDialog = new ProductStagingDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell());
    }

    /**
     * Close the product staging dialog.
     */
    private void closeDialog() {
        if ((productStagingDialog != null)
                && (productStagingDialog.getShell() != null)
                && (!productStagingDialog.getShell().isDisposed())) {
            productStagingDialog.close();
            productStagingDialog = null;
        }
    }
}
