/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazardtypefirst;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.hazardtypefirst.HazardTypeFirstPresenter.Command;
import gov.noaa.gsd.viz.hazards.toolbar.BasicAction;
import gov.noaa.gsd.viz.hazards.ui.BasicWidgetDelegateHelper;
import gov.noaa.gsd.viz.hazards.ui.ChoiceStateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.CommandInvokerDelegate;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IWidget;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.raytheon.uf.viz.core.VizApp;

/**
 * Description: Hazard type first view, a delegate for the hazard type first
 * dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 21, 2015    3626    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see HazardTypeFirstDialog
 */
public class HazardTypeFirstView implements
        IHazardTypeFirstViewDelegate<Action, RCPMainUserInterfaceElement> {

    // Private Static Constants

    /**
     * Name of the file holding the image for the create hazard toolbar button
     * icon.
     */
    private static final String CREATE_HAZARD_BY_TYPE_TOOLBAR_IMAGE_FILE_NAME = "exclamation.png";

    /**
     * Text describing the create hazard toolbar button.
     */
    private static final String CREATE_HAZARD_BY_TYPE_TEXT = "Create Hazard by Type";

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
     * Hazard type first dialog.
     */
    private HazardTypeFirstDialog hazardTypeFirstDialog;

    /**
     * Show dialog action.
     */
    private BasicAction showDialogAction;

    /**
     * Show dialog command invocation handler.
     */
    private ICommandInvocationHandler<Object> setShowDialogInvocationHandler;

    /**
     * List of categories that are to be available.
     */
    private List<String> categories;

    /**
     * Flag indicating whether the show dialog action is enabled.
     */
    private boolean showDialogActionEnabled = true;

    /**
     * Show dialog command invoker. The identifier is ignored.
     */
    private final ICommandInvoker<Object> showDialogInvoker = new ICommandInvoker<Object>() {

        @Override
        public void setEnabled(Object identifier, boolean enable) {
            showDialogActionEnabled = enable;
            if (showDialogAction != null) {
                showDialogAction.setEnabled(enable);
            }
        }

        @Override
        public void setCommandInvocationHandler(
                ICommandInvocationHandler<Object> handler) {
            setShowDialogInvocationHandler = handler;
        }
    };

    /**
     * Show dialog command invoker delegate.
     */
    private final ICommandInvoker<Object> showDialogInvokerDelegate = new CommandInvokerDelegate<>(
            new BasicWidgetDelegateHelper<>(showDialogInvoker),
            RUNNABLE_ASYNC_SCHEDULER);

    /**
     * Category choice changer delegate. This must be recreated each time the
     * dialog is shown, since it has no principal for which to act as a delegate
     * unless the dialog exists.
     */
    private IChoiceStateChanger<Object, String, String, String> categoryChangerDelegate;

    /**
     * Type choice changer delegate. Like {@link #categoryChangerDelegate}, this
     * must be recreated each time the dialog is shown.
     */
    private IChoiceStateChanger<Object, String, String, String> typeChangerDelegate;

    /**
     * Command invoker delegate, for dialog-issued commands. Like
     * {@link #categoryChangerDelegate}, this must be recreated each time the
     * dialog is shown.
     */
    private ICommandInvoker<Command> commandInvokerDelegate;

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public HazardTypeFirstView() {

        /*
         * No action.
         */
    }

    // Public Methods

    @Override
    public void initialize(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        if (type == RCPMainUserInterfaceElement.TOOLBAR) {

            /*
             * Create the toolbar button to initiate the creation of a hazard
             * event starting with type.
             */
            showDialogAction = new BasicAction("",
                    CREATE_HAZARD_BY_TYPE_TOOLBAR_IMAGE_FILE_NAME,
                    Action.AS_PUSH_BUTTON, CREATE_HAZARD_BY_TYPE_TEXT) {

                @Override
                public void run() {
                    if (setShowDialogInvocationHandler != null) {
                        setShowDialogInvocationHandler.commandInvoked(null);
                    }
                }
            };
            if (showDialogActionEnabled == false) {
                showDialogAction.setEnabled(false);
            }
            return Lists.newArrayList(showDialogAction);
        }
        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        closeDialog();
    }

    @Override
    public void show(String selectedCategory, List<String> types,
            List<String> typeDescriptions, String selectedType) {

        /*
         * Create the dialog.
         */
        if (hazardTypeFirstDialog == null) {
            createDialog();
        }

        /*
         * Create the delegates for the dialog state changers and command
         * invoker.
         */
        categoryChangerDelegate = new ChoiceStateChangerDelegate<>(
                new BasicWidgetDelegateHelper<>(
                        hazardTypeFirstDialog.getCategoryChanger()),
                RUNNABLE_ASYNC_SCHEDULER);
        typeChangerDelegate = new ChoiceStateChangerDelegate<>(
                new BasicWidgetDelegateHelper<>(
                        hazardTypeFirstDialog.getTypeChanger()),
                RUNNABLE_ASYNC_SCHEDULER);
        commandInvokerDelegate = new CommandInvokerDelegate<>(
                new BasicWidgetDelegateHelper<>(
                        hazardTypeFirstDialog.getCommandInvoker()),
                RUNNABLE_ASYNC_SCHEDULER);

        /*
         * Show the dialog.
         */
        hazardTypeFirstDialog.open();

        /*
         * Set the category choices for the dialog; if there are no more than
         * one, make the category chooser uneditable.
         */
        categoryChangerDelegate.setChoices(null, categories, categories,
                selectedCategory);
        categoryChangerDelegate.setEditable(null, (categories.size() > 1));

        /*
         * Set the type choices for the dialog.
         */
        typeChangerDelegate.setChoices(null, types, typeDescriptions,
                selectedType);
    }

    @Override
    public void hide() {
        closeDialog();
    }

    @Override
    public ICommandInvoker<Object> getShowDialogInvoker() {
        return showDialogInvokerDelegate;
    }

    @Override
    public IChoiceStateChanger<Object, String, String, String> getCategoryChanger() {
        return categoryChangerDelegate;
    }

    @Override
    public IChoiceStateChanger<Object, String, String, String> getTypeChanger() {
        return typeChangerDelegate;
    }

    @Override
    public ICommandInvoker<Command> getCommandInvoker() {
        return commandInvokerDelegate;
    }

    // Private Methods

    /**
     * Create the hazard type first dialog.
     */
    private void createDialog() {
        hazardTypeFirstDialog = new HazardTypeFirstDialog(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell());
    }

    /**
     * Close the hazard type first dialog.
     */
    private void closeDialog() {
        if ((hazardTypeFirstDialog != null)
                && (hazardTypeFirstDialog.getShell() != null)
                && (!hazardTypeFirstDialog.getShell().isDisposed())) {
            hazardTypeFirstDialog.close();
            hazardTypeFirstDialog = null;
        }
    }
}
