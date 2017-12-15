/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.producteditor;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SiteChanged;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.SessionEventsLockStatusModified;
import com.raytheon.viz.ui.VizWorkbenchManager;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.ProductAction;
import gov.noaa.gsd.viz.hazards.display.action.ProductEditorAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import net.engio.mbassy.listener.Handler;

/**
 * Description: Product Editor presenter, used to mediate between the model and
 * the product editor view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer       Description
 * ------------ ---------- -------------- --------------------------
 * Feb 19, 2013            bryon.lawrence    Initial creation
 * Jul 15, 2013      585   Chris.Golden      Changed to support loading from bundle,
 *                                           including the passing in of the event
 *                                           bus so that the latter is no longer a
 *                                           singleton.
 * Sep 19, 2013     2046   mnash             Update for product generation.
 * Nov 16, 2013     2166   Dan Schaffer      Some tidying.
 * Dec 03, 2013     2182   Dan Schaffer      Refactoring - eliminated IHazardsIF
 * Feb 07, 2014     2890   bkowal            Product Generation JSON refactor.
 * Apr 11, 2014     2819   Chris.Golden      Fixed bugs with the Preview and Issue
 *                                           buttons in the HID remaining grayed out
 *                                           when they should be enabled.
 * May 17, 2014     2925   Chris.Golden      Changed to work with MVP framework
 *                                           widget changes. Also added newly
 *                                           required implementation of
 *                                           reinitialize(), and made initialize()
 *                                           protected as it is called by setView().
 * Apr 23, 2014     1480   jsanchez          Added product editor action CORRECT.
 * Jun 30, 2014     3512   Chris.Golden      Changed to work with changes to
 *                                           ICommandInvoker.
 * Jul 14, 2014     4187   jsanchez          Check if the generatedProductsList is valid.
 * Jul 28, 2014     3412   jsanchez          Close the product editor on regeneration request.
 * Dec 05, 2014     4124   Chris.Golden      Changed to work with newly parameterized
 *                                           config manager.
 * Dec 13, 2014     4959   Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Feb 15, 2015     2271   Dan Schaffer      Incur recommender/product generator init costs
 *                                           immediately
 * Feb 26, 2015     6306   mduff             Pass site id to product editor.
 * Apr 10, 2015     6898   Chris.Cody        Removed modelChanged legacy messaging method
 * May 13, 2015     6899   Robert.Blum       Removed sessionEventsModified handler.
 * Jul 01, 2015     6726   Robert.Blum       IssueAll button no longer closes the Editor.
 * Dec 04, 2015    12981   Roger.Ferrel      Checks to prevent issuing unwanted
 *                                           expiration product.
 * Jan 26, 2016     7623   Ben.Phillippe     Implemented locking of HazardEvents
 * Mar 30, 2016     8837   Robert.Blum       Added siteChanged() for service backup.
 * Sep 20, 2016    21622   Ben.Phillippe     Prevent preview of other site's hazards
 * Dec 12, 2016    21504   Robert.Blum       Refactored locking code.
 * Feb 02, 2017    15556   Chris.Golden      Minor changes to support console refactor.
 * Apr 05, 2017    32733   Robert.Blum       Changed lock status modified handler to
 *                                           deal with new version of notification that
 *                                           notifies of one or more lock statuses
 *                                           changing.
 * Apr 27, 2017    11853   Chris.Golden      Added public method to close product editor.
 * Dec 17, 2017    20739   Chris.Golden      Refactored away access to directly mutable
 *                                           session events.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class ProductEditorPresenter
        extends HazardServicesPresenter<IProductEditorView<?, ?>> {

    // Public Constructors

    /**
     * Construct a standard instance of a ProductEditorPresenter.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public ProductEditorPresenter(ISessionManager<ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);
    }

    // Public Methods

    /**
     * Receive notification of a model change. For the moment, the product
     * editor dialog doesn't care about model events.
     * 
     * @param changes
     *            Set of elements within the model that have changed.
     */
    @Override
    public void modelChanged(EnumSet<HazardConstants.Element> changed) {

        // No action.
    }

    public final void showProductEditorDetail(
            List<GeneratedProductList> generatedProductsList, String siteId) {
        boolean showProductEditor = true;
        if (generatedProductsList == null) {
            showProductEditor = false;
        } else if (generatedProductsList.isEmpty()) {
            showProductEditor = false;
        } else if (generatedProductsList.size() == 1) {
            GeneratedProductList g = generatedProductsList.get(0);
            if (g.isEmpty()) {
                showProductEditor = false;
            }
        }

        /*
         * Verify we are attempting to preview products from our own site
         */
        for (int i = 0; i < generatedProductsList.size()
                && showProductEditor; i++) {
            for (IEvent event : generatedProductsList.get(i).getEventSet()) {
                IReadableHazardEvent hazardEvent = (IReadableHazardEvent) event;
                if (!siteId.equals(hazardEvent.getSiteID())) {
                    showProductEditor = false;
                    MessageBox mb1 = new MessageBox(VizWorkbenchManager
                            .getInstance().getCurrentWindow().getShell(),
                            SWT.ICON_ERROR | SWT.OK);
                    mb1.setText("Cannot Preview");
                    mb1.setMessage(
                            "Must be in Service Backup to modify Hazard Events from "
                                    + hazardEvent.getSiteID() + ".");
                    mb1.open();
                    break;
                }
            }
        }
        if (showProductEditor) {
            this.getView().showProductEditor(generatedProductsList, siteId,
                    getModel().getConfigurationManager().getHazardTypes());
            this.bind();
            this.getView().openDialog();
        } else {
            getModel().setPreviewOngoing(false);
        }
    }

    public final void closeProductEditor() {
        if (getView().isProductEditorOpen()) {
            getModel().setPreviewOngoing(false);
            getView().closeProductEditor();
        }
    }

    /**
     * Respond to the current site changing.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void siteChanged(SiteChanged change) {
        getView().changeSite(change.getSiteIdentifier());
    }

    // Protected Methods

    @Override
    protected void initialize(IProductEditorView<?, ?> view) {
        view.initialize();
    }

    @Override
    protected final void reinitialize(IProductEditorView<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Binds the presenter to the view which implements the IProductEditorView
     * interface. The interface is the contract, and it is all the presenter
     * needs to know about the view. This allows different views to easily be
     * created and given to this presenter.
     * <p>
     * By binding to the view, the presenter handles all of the view's events.
     * 
     * @param
     * @return
     */
    private void bind() {
        getView().getIssueInvoker().setCommandInvocationHandler(
                new ICommandInvocationHandler<String>() {

                    @Override
                    public void commandInvoked(String identifier) {
                        ProductEditorAction action = null;
                        if (identifier != null && identifier.equalsIgnoreCase(
                                HazardConstants.CORRECTION_FLAG)) {
                            action = new ProductEditorAction(
                                    HazardAction.CORRECT);
                        } else {
                            action = new ProductEditorAction(
                                    HazardAction.ISSUE);
                        }

                        action.setGeneratedProductsList(
                                getView().getGeneratedProductsList());
                        getModel().setIssueOngoing(true);
                        ProductEditorPresenter.this.publish(action);
                    }
                });

        getView().getDismissInvoker().setCommandInvocationHandler(
                new ICommandInvocationHandler<String>() {

                    @Override
                    public void commandInvoked(String identifier) {
                        closeProductEditor();
                        if (identifier != null && identifier.equalsIgnoreCase(
                                HazardConstants.REGENERATE_FLAG)) {
                            ProductAction action = new ProductAction(
                                    ProductAction.ActionType.PREVIEW);
                            ProductEditorPresenter.this.publish(action);
                        }
                    }
                });

    }

    @Handler
    public void sessionEventsLockStatusModified(
            SessionEventsLockStatusModified change) {

        /*
         * Determine if any locks of the events currently in the Product Editor
         * have been broken. If so disable the Issue and Save buttons.
         */
        if (getView().isProductEditorOpen()) {
            for (GeneratedProductList productList : getView()
                    .getGeneratedProductsList()) {
                for (IEvent event : productList.getEventSet()) {
                    IReadableHazardEvent hazard = (IReadableHazardEvent) event;
                    for (String eventIdentifier : change
                            .getEventIdentifiers()) {
                        if (hazard.getEventID().equals(eventIdentifier)) {
                            getView().handleHazardEventLock();
                            return;
                        }
                    }
                }
            }
        }
    }
}
