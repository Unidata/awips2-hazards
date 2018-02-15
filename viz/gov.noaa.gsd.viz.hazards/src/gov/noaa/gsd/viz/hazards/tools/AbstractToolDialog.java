/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.tools.AbstractToolDialogSpecifier;

import gov.noaa.gsd.viz.hazards.ui.BasicDialog;
import gov.noaa.gsd.viz.megawidgets.IMegawidgetManagerListener;
import gov.noaa.gsd.viz.megawidgets.MegawidgetException;
import gov.noaa.gsd.viz.megawidgets.MegawidgetManager;
import gov.noaa.gsd.viz.megawidgets.MegawidgetPropertyException;

/**
 * Base class from which to derive tool dialogs, used to allow the user to
 * specify parameters for tool executions, show results of such executions etc.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo.
 * Jun 20, 2013   1277     Chris.Golden      Added code to support the
 *                                           specification of a side
 *                                           effects applier for the
 *                                           megawidgets showing in the
 *                                           tool dialog.
 * Jul 18, 2013    585     Chris Golden      Changed to support loading
 *                                           from bundle.
 * Aug 21, 2013   1921     daniel.s.schaffer Call recommender framework directly
 * Dec 16, 2013   2545     Chris.Golden      Added current time provider
 *                                           for megawidget use.
 * Apr 14, 2014   2925     Chris.Golden      Minor changes to work with megawidget
 *                                           framework changes.
 * Jun 17, 2014   3982     Chris.Golden      Changed megawidget "side effects" to
 *                                           "interdependencies".
 * Jun 23, 2014   4010     Chris.Golden      Changed to work with megawidget manager
 *                                           changes.
 * Jun 30, 2014   3512     Chris Golden      Changed to work with more megawidget manager
 *                                           changes.
 * Aug 18, 2014   4243     Chris.Golden      Changed to take a file path instead of a
 *                                           Python script for an interdependency script.
 * Sep 05, 2014   4042     Chris.Golden      Added scrollbars to be used when needed to
 *                                           display the megawidgets. Also added maximum
 *                                           size constraints that may be specified as
 *                                           part of the dialog options.
 * Oct 20, 2014   4818     Chris.Golden      Removed scrolled composite from the dialog,
 *                                           since scrolling is now handled by the
 *                                           megawidgets.
 * Dec 13, 2014   4959     Dan Schaffer      Spatial Display cleanup and other bug fixes
 * Jan 30, 2015   3626     Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * Jun 17, 2015   8389     Benjamin.Phillippe Fixed min/max time visible variables to 
 *                                            not expect exception when initializing
 * Nov 10, 2015  12762     Chris.Golden      Added support for use of new recommender
 *                                           manager.
 * Aug 15, 2017  22757     Chris.Golden      Refactored to become an abstract base class
 *                                           for all tool dialogs.
 * Sep 27, 2017  38072     Chris.Golden      Changed to work with new recommender
 *                                           manager.
 * Oct 10, 2017  39151     Chris Golden      Changed to handle new parameter for
 *                                           megawidget manager constructor.
 * May 22, 2018   3782     Chris.Golden      Changed to have configuration options passed
 *                                           in using dedicated objects and having already
 *                                           been vetted, instead of passing them in as
 *                                           raw maps. Also changed to conform somewhat
 *                                           better to the MVP design guidelines. Also added
 *                                           ability to set the dialog's mutable properties
 *                                           while it is showing.
 * Jun 06, 2018  15561     Chris.Golden      Added slop space for width and height.
 * </pre>
 * 
 * @author Chris.Golden
 */
abstract class AbstractToolDialog<S extends AbstractToolDialogSpecifier>
        extends BasicDialog {

    // Protected Static Constants

    /**
     * Logging mechanism.
     */
    protected static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(AbstractToolDialog.class);

    // Private Variables

    /**
     * Dialog specifier.
     */
    private final S dialogSpecifier;

    /**
     * Tool dialog listener, or <code>null</code> if none was supplied.
     */
    private final IToolDialogListener toolDialogListener;

    /**
     * Megawidget manager.
     */
    private MegawidgetManager megawidgetManager;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell.
     * @param dialogSpecifier
     *            Specifier for this dialog.
     * @param toolDialogListener
     *            Tool dialog listener, or <code>null</code> if the caller does
     *            not need to be listening.
     */
    public AbstractToolDialog(Shell parent, S dialogSpecifier,
            IToolDialogListener toolDialogListener) {
        super(parent);
        this.dialogSpecifier = dialogSpecifier;
        this.toolDialogListener = toolDialogListener;
        setShellStyle(
                SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    // Public Methods

    /**
     * Update the mutable properties of the dialog.
     * 
     * @param changedMutableProperties
     *            Map of identifiers of megawidgets to their changed mutable
     *            properties.
     */
    public void updateMutableProperties(
            Map<String, Map<String, Object>> changedMutableProperties) {
        try {
            megawidgetManager.setMutableProperties(changedMutableProperties);
        } catch (MegawidgetPropertyException exception) {
            statusHandler.error("ToolDialog.MegawidgetManager error occurred "
                    + "while attempting to change mutable " + "properties: "
                    + exception, exception);
        }
    }

    // Protected Methods

    /**
     * Get the current state held by the dialog. The current state is specified
     * as a dictionary that pairs field name keys with those fields' values.
     * 
     * @return Current state held by the dialog.
     */
    protected abstract Map<String, Serializable> getState();

    /**
     * Get the dialog specifier.
     * 
     * @return Dialog specifier.
     */
    protected S getSpecifier() {
        return dialogSpecifier;
    }

    @Override
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (dialogSpecifier.getTitle() != null) {
            shell.setText(dialogSpecifier.getTitle());
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        /*
         * Let the superclass create the area.
         */
        Composite top = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) top.getLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 0;

        /*
         * Create a megawidget manager, which will create the megawidgets and
         * manage their displaying, and allowing of manipulation, of the state
         * values.
         */
        try {
            megawidgetManager = new MegawidgetManager(top,
                    dialogSpecifier.getMegawidgetSpecifierManager(),
                    dialogSpecifier.getInitialStatesForMegawidgets(),
                    new IMegawidgetManagerListener() {

                        @Override
                        public void commandInvoked(MegawidgetManager manager,
                                String identifier) {
                            toolDialogListener.toolDialogCommandInvoked(
                                    identifier, manager.getMutableProperties());
                        }

                        @Override
                        public void stateElementChanged(
                                MegawidgetManager manager, String identifier,
                                Object state) {
                            toolDialogListener.toolDialogStateElementChanged(
                                    identifier, state,
                                    manager.getMutableProperties());
                        }

                        @Override
                        public void stateElementsChanged(
                                MegawidgetManager manager,
                                Map<String, ?> statesForIdentifiers) {
                            toolDialogListener.toolDialogStateElementsChanged(
                                    statesForIdentifiers,
                                    manager.getMutableProperties());
                        }

                        @Override
                        public void sizeChanged(MegawidgetManager manager,
                                String identifier) {

                            /*
                             * No action.
                             */
                        }

                        @Override
                        public void visibleTimeRangeChanged(
                                MegawidgetManager manager, String identifier,
                                long lower, long upper) {
                            toolDialogListener
                                    .toolDialogVisibleTimeRangeChanged(
                                            identifier, lower, upper);
                        }

                        @Override
                        public void sideEffectMutablePropertyChangeErrorOccurred(
                                MegawidgetManager manager,
                                MegawidgetPropertyException exception) {
                            statusHandler.error(
                                    "ToolDialog.MegawidgetManager error occurred "
                                            + "while attempting to apply megawidget "
                                            + "interdependencies: " + exception,
                                    exception);
                        }
                    }, null, dialogSpecifier.getMinVisibleTime(),
                    dialogSpecifier.getMaxVisibleTime());
        } catch (MegawidgetException e) {
            statusHandler
                    .error("ToolDialog.createDialogArea(): Unable to create megawidget "
                            + "manager due to megawidget construction problem: "
                            + e, e);
        }

        toolDialogListener.toolDialogInitialized(
                megawidgetManager.getMutableProperties());

        /*
         * Return the created area.
         */
        return top;
    }

    @Override
    protected Point getInitialSize() {

        /*
         * Get the bounds of the display (meaning all monitors combined).
         */
        Rectangle bounds = Display.getDefault().getBounds();

        /*
         * Let the superclass's implementation determine the initial size, add a
         * bit of buffering pixels in each direction, and then shrink it down to
         * fit within both the bounds of the display as determined above, and
         * also the maximum initial size of the dialog, if such was provided as
         * part of its configuration. Also ensure it is at least the minimum
         * initial width, if that was provided.
         */
        Point size = super.getInitialSize();
        size.x += 2;
        size.y += 2;
        int min = (dialogSpecifier.getMinInitialWidth() > 0
                ? dialogSpecifier.getMinInitialWidth() : 0);
        if (min > size.x) {
            size.x = min;
        }
        int max = ((dialogSpecifier.getMaxInitialWidth() != -1)
                && (dialogSpecifier.getMaxInitialWidth() < bounds.width)
                        ? dialogSpecifier.getMaxInitialWidth() : bounds.width);
        if (max < size.x) {
            size.x = max;
        }
        max = ((dialogSpecifier.getMaxInitialHeight() != -1)
                && (dialogSpecifier.getMaxInitialHeight() < bounds.height)
                        ? dialogSpecifier.getMaxInitialHeight()
                        : bounds.height);
        if (max < size.y) {
            size.y = max;
        }

        /*
         * Calculate the minimum size to which the user may shrink this dialog
         * down. A reasonable minimum width is assumed to be the width of the
         * dialog if it is to contain the bottom button bar fully. A reasonable
         * minimum height is assumed to be the dialog if it is to contain a
         * height for the megawidgets equal to the button bar width, plus the
         * height of the button bar, plus the dialog trim. If either of the
         * dimensions thus calculated are larger than the corresponding initial
         * dimensions, use the appropriate initial dimension.
         */
        Rectangle trimRect = getShell().computeTrim(0, 0, 0, 0);
        Point buttonBarSize = getButtonBar().computeSize(SWT.DEFAULT,
                SWT.DEFAULT);
        getShell().setMinimumSize(
                Math.min(trimRect.width + buttonBarSize.x, size.x),
                Math.min(trimRect.height + buttonBarSize.x + buttonBarSize.y,
                        size.y));

        return size;
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        toolDialogListener.toolDialogClosed(getState(), false);
    }

    @Override
    protected void cancelPressed() {
        super.okPressed();
        toolDialogListener.toolDialogClosed(null, true);
    }
}
