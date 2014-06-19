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

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.common.utilities.Utils;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.IConsoleView;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;
import gov.noaa.gsd.viz.hazards.display.action.ConsoleAction;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailViewDelegate;
import gov.noaa.gsd.viz.hazards.producteditor.IProductEditorView;
import gov.noaa.gsd.viz.hazards.producteditor.ProductEditorPresenter;
import gov.noaa.gsd.viz.hazards.productstaging.IProductStagingView;
import gov.noaa.gsd.viz.hazards.productstaging.ProductStagingPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ISpatialView;
import gov.noaa.gsd.viz.hazards.spatialdisplay.SpatialPresenter;
import gov.noaa.gsd.viz.hazards.spatialdisplay.ToolLayer;
import gov.noaa.gsd.viz.hazards.tools.IToolsView;
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;
import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;

/**
 * Description: Base class for automated testing. The approach is to create mock
 * {@link IView}s that are basically beans. When methods are called to render
 * hazard GUIs, the mock versions will, instead, store the information to be
 * displayed into instance fields. The tests then query these instance fields to
 * see if the expected values would have been displayed. The mock {@link IView}s
 * are injected into the corresponding {@link Presenter}s before the test
 * begins. After the test completes, the original {@link IView}s are injected
 * back into the {@link Presenter}s so that Hazards can continue to be run
 * normally.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166       daniel.s.schaffer@noaa.gov      Initial creation
 * Oct 29, 2013 2166       daniel.s.schaffer@noaa.gov      Cleaned up handling of errors
 * Nov 15, 2013 2182       daniel.s.schaffer@noaa.gov      Refactoring JSON - ProductStagingDialog
 * Nov 25, 2013 2336       Chris.Golden                    Altered to handle new location
 *                                                         of utility classes.
 * Apr 09, 2014 2925       Chris.Golden                    Fixed to work with new HID event propagation.
 * May 18, 2014 2925       Chris.Golden                    More changes to get it to work with the new HID.
 *                                                         Also moved the subclasses' steps enums into this
 *                                                         class.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public abstract class FunctionalTest<E extends Enum<E>> {

    private static final String TEST_ERROR = "Test error";

    protected static final String DAM_BREAK_FLOOD_RECOMMENDER = "DamBreakFloodRecommender";

    protected static final String STORM_TRACK_TOOL = "StormTrackTool";

    protected static final String RIVER_FLOOD_RECOMMENDER = "RiverFloodRecommender";

    protected static final String CANNED_TORNADO_SETTING = "TOR";

    protected static final String CANNED_FLOOD_SETTING = "Flood";

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(getClass());

    protected BoundedReceptionEventBus<Object> eventBus;

    protected HazardServicesAppBuilder appBuilder;

    private ToolsPresenter toolsPresenter;

    private ConsolePresenter consolePresenter;

    private ProductStagingPresenter productStagingPresenter;

    private SpatialPresenter spatialPresenter;

    private HazardDetailPresenter hazardDetailPresenter;

    private ProductEditorPresenter productEditorPresenter;

    private IConsoleView<?, ?> realConsoleView;

    private ISpatialView<?, ?> realSpatialView;

    private IToolsView<?, ?> realToolsView;

    private IProductStagingView<?, ?> realProductStagingView;

    private IProductEditorView<?, ?> realProductEditorView;

    private IHazardDetailViewDelegate<?, ?> realHazardDetailView;

    protected ConsoleViewForTesting mockConsoleView;

    protected SpatialViewForTesting mockSpatialView;

    protected ToolsViewForTesting mockToolsView;

    protected ProductStagingViewForTesting mockProductStagingView;

    protected ProductEditorViewForTesting mockProductEditorView;

    protected HazardDetailViewForTesting mockHazardDetailView;

    protected HazardServicesAppBuilder.IQuestionAnswerer realQuestionAnswerer;

    protected ToolLayer toolLayer;

    protected AutoTestUtilities autoTestUtilities;

    protected final ISessionEventManager<ObservedHazardEvent> eventManager;

    protected E step;

    FunctionalTest(HazardServicesAppBuilder appBuilder) {
        this.appBuilder = appBuilder;
        this.eventManager = appBuilder.getSessionManager().getEventManager();
        this.eventBus = appBuilder.getEventBus();
        this.autoTestUtilities = new AutoTestUtilities(appBuilder);
        registerForEvents();

    }

    protected final String getCurrentStep() {
        return step.toString();
    }

    protected final void stepCompleted() {
        statusHandler.debug("Completed step " + step);
    }

    private void registerForEvents() {
        this.eventBus.subscribe(this);
    }

    protected void run() {
        resetEvents();
        mockViews();
        checkForFloodSettings();
        statusHandler.debug(String.format("Starting %s...", this.getClass()
                .getSimpleName()));
        runFirstStep();
    }

    /**
     * Start off the test by executing the first step.
     */
    protected abstract void runFirstStep();

    protected int issuanceTargetCount;

    protected int issuanceCount;

    protected boolean issuanceProductGenerationComplete;

    /**
     * Initialize the issuance tracking. This is used for event issue actions to
     * determine when the issuance is complete, since each issuance may require
     * multiple hazard status change notifications (that number being specified
     * by <code>targetCount</code>) as well as a product generation complete
     * notification.
     * 
     * @param targetCount
     *            Number of event status changes that must occur before the
     *            tracking can be considered to have reached its target.
     */
    protected final void initializeIssuanceTracking(int targetCount) {
        issuanceTargetCount = targetCount;
        issuanceCount = 0;
        issuanceProductGenerationComplete = false;
    }

    /**
     * Determine whether or not issuance tracking has reached its target, the
     * target being that notification of product generation completion was
     * received, as well as <code>targetCount</code> number of event status
     * change notifications.
     * 
     * @param incrementCount
     *            Flag indicating whether or not to increment the status change
     *            notification; if false, this invocation will instead set the
     *            product generation complete flag high.
     * @return True if the issuance is complete, false otherwise.
     */
    protected final boolean isIssuanceComplete(boolean incrementCount) {
        if (incrementCount) {
            issuanceCount++;
        } else {
            issuanceProductGenerationComplete = true;
        }
        if ((issuanceCount >= issuanceTargetCount)
                && issuanceProductGenerationComplete) {
            return true;
        }
        statusHandler.debug(getCurrentStep() + ": state change count = "
                + getIssuanceCount() + " of " + issuanceTargetCount
                + ", generation complete = "
                + issuanceProductGenerationComplete);
        return false;
    }

    /**
     * Get the current issuance count.
     * 
     * @return Current issuance count.
     */
    protected final int getIssuanceCount() {
        return issuanceCount;
    }

    private void checkForFloodSettings() {
        Settings currentSettings = appBuilder.getSessionManager()
                .getConfigurationManager().getSettings();
        String settingsID = currentSettings.getSettingsID();
        if (!settingsID.equals(CANNED_FLOOD_SETTING)) {
            fail("Must run tests from Canned Flood settings");
        }
    }

    private void resetEvents() {
        appBuilder.getSessionManager().getEventManager().getEvents();
        eventBus.publish(new ConsoleAction(ConsoleAction.ActionType.RESET,
                ConsoleAction.RESET_EVENTS));
    }

    protected void mockViews() {
        toolsPresenter = appBuilder.getToolsPresenter();
        realToolsView = toolsPresenter.getView();
        mockToolsView = new ToolsViewForTesting();
        toolsPresenter.setView(mockToolsView);

        consolePresenter = appBuilder.getConsolePresenter();
        realConsoleView = consolePresenter.getView();
        mockConsoleView = new ConsoleViewForTesting();
        mockConsoleView.setCurrentSettings(consolePresenter.getView()
                .getCurrentSettings());
        consolePresenter.setView(mockConsoleView);

        productStagingPresenter = appBuilder.getProductStagingPresenter();
        realProductStagingView = productStagingPresenter.getView();
        mockProductStagingView = new ProductStagingViewForTesting();
        productStagingPresenter.setView(mockProductStagingView);

        spatialPresenter = appBuilder.getSpatialPresenter();
        realSpatialView = spatialPresenter.getView();
        mockSpatialView = new SpatialViewForTesting();
        spatialPresenter.setView(mockSpatialView);

        hazardDetailPresenter = appBuilder.getHazardDetailPresenter();
        realHazardDetailView = hazardDetailPresenter.getView();
        mockHazardDetailView = new HazardDetailViewForTesting();
        hazardDetailPresenter.setView(mockHazardDetailView);

        productEditorPresenter = appBuilder.getProductEditorPresenter();
        realProductEditorView = productEditorPresenter.getView();
        mockProductEditorView = new ProductEditorViewForTesting();
        productEditorPresenter.setView(mockProductEditorView);

        toolLayer = appBuilder.getToolLayer();

        realQuestionAnswerer = appBuilder.getQuestionAnswerer();

        HazardServicesAppBuilder.IQuestionAnswerer questionAnswerer = new HazardServicesAppBuilder.IQuestionAnswerer() {

            @Override
            public boolean getUserAnswerToQuestion(String question) {
                return true;
            }

            @Override
            public boolean getUserAnswerToQuestion(String question,
                    String[] buttonLabels) {
                return true;
            }

        };
        appBuilder.setQuestionAnswerer(questionAnswerer);
    }

    protected ISessionEventManager<ObservedHazardEvent> getEventManager() {
        return appBuilder.getSessionManager().getEventManager();
    }

    protected ObservedHazardEvent getEvent(String eventId) {
        return getEventManager().getEventById(eventId);
    }

    protected void assertEquals(Object actual, Object expected) {
        if (!actual.equals(expected)) {
            String message = String.format("Expected %s, got %s", expected,
                    actual);
            throw new RuntimeException(message);
        }

    }

    protected void assertTrue(Boolean expression) {
        if (!expression) {
            throw new RuntimeException(new Throwable());
        }

    }

    protected void assertFalse(Boolean expression) {
        if (expression) {
            throw new RuntimeException(new Throwable());
        }
    }

    protected void testError() {
        throw new RuntimeException(new Throwable());

    }

    protected void handleException(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(TEST_ERROR + " at step " + getCurrentStep() + "\n");
        sb.append(e.getMessage() + "\n");
        if (e.getCause() != null) {
            sb.append(Utils.stackTraceAsString(e.getCause()));
        } else {
            sb.append(Utils.stackTraceAsString(e));
        }
        fail(sb.toString());
    }

    protected void fail(String message) {
        String errorMessage = String.format("%s ", message);
        statusHandler.error(errorMessage);
        testFailure();
    }

    protected void testSuccess() {
        endTest(true);
    }

    protected void testFailure() {
        endTest(false);
    }

    protected void endTest(boolean success) {
        toolsPresenter.setView(realToolsView);

        consolePresenter.setView(realConsoleView);

        productStagingPresenter.setView(realProductStagingView);

        spatialPresenter.setView(realSpatialView);

        hazardDetailPresenter.setView(realHazardDetailView);

        productEditorPresenter.setView(realProductEditorView);

        appBuilder.setQuestionAnswerer(realQuestionAnswerer);

        if (appBuilder.getSessionManager().isPreviewOngoing()
                || appBuilder.getSessionManager().isIssueOngoing()) {
            statusHandler.error("Preview or issue left ongoing by this test.",
                    new IllegalStateException("Preview ongoing = "
                            + appBuilder.getSessionManager().isPreviewOngoing()
                            + ", issue ongoing = "
                            + appBuilder.getSessionManager().isIssueOngoing()));
            success = false;
        }

        unRegisterForEvents();
        if (success) {
            statusHandler.debug(String.format("%s Successful", this.getClass()
                    .getSimpleName()));
            eventBus.publish(new TestCompleted(this.getClass()));
        } else {
            eventBus.publish(new TestCompleted(StopTesting.class));
        }

    }

    protected List<String> convertContextMenuToString(List<IAction> actions) {
        List<String> contextMenu = new ArrayList<>();
        for (IAction action : actions) {
            contextMenu.add(action.getText());
        }
        return contextMenu;
    }

    protected static class StopTesting {

    }

    private void unRegisterForEvents() {
        this.eventBus.unsubscribe(this);
    }

}
