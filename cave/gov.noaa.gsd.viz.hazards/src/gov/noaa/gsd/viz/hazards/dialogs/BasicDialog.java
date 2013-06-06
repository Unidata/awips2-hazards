/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.dialogs;

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.viz.ui.dialogs.CaveJFACEDialog;

/**
 * Base class for dialogs that provides additional functionality over the <code>
 * CaveJFACEDialog</code>, upon which this is based.
 * <p>
 * First, it provides on a per-subclass basis the saving and restoring of dialog
 * sizes and positions. This also functions with modeless dialogs that are
 * hidden and then re-shown (via this class's <code>setVisible()</code> method)
 * as opposed to simply destroyed after being shown once.
 * <p>
 * Second, it provides proper sizing of dialogs that include menubars. The
 * <code>Dialog</code> class sizes menubar-clad instances to be bizarrely large.
 * <p>
 * Third, it horizontally centers the row of buttons at the bottom of the dialog
 * box.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jun 04, 2013            Chris.Golden      Changed to subclass CaveJFACEDialog
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class BasicDialog extends CaveJFACEDialog {

    // Private Static Constants

    /**
     * Copied from org.eclipse.jface.Dialog:
     * 
     * The dialog settings key name for stored dialog x location.
     */
    private static final String DIALOG_ORIGIN_X = "DIALOG_X_ORIGIN"; //$NON-NLS-1$

    /**
     * Copied from org.eclipse.jface.Dialog:
     * 
     * The dialog settings key name for stored dialog y location.
     */
    private static final String DIALOG_ORIGIN_Y = "DIALOG_Y_ORIGIN"; //$NON-NLS-1$

    /**
     * Copied from org.eclipse.jface.Dialog:
     * 
     * The dialog settings key name for stored dialog width.
     */
    private static final String DIALOG_WIDTH = "DIALOG_WIDTH"; //$NON-NLS-1$

    /**
     * Copied from org.eclipse.jface.Dialog:
     * 
     * The dialog settings key name for stored dialog height.
     */
    private static final String DIALOG_HEIGHT = "DIALOG_HEIGHT"; //$NON-NLS-1$

    /**
     * Copied from org.eclipse.jface.Dialog:
     * 
     * The dialog settings key name for the font used when the dialog height and
     * width was stored.
     */
    private static final String DIALOG_FONT_DATA = "DIALOG_FONT_NAME"; //$NON-NLS-1$

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parent
     *            Parent shell.
     */
    public BasicDialog(Shell parent) {
        super(parent, false);
    }

    // Public Methods

    /**
     * Show or hide the dialog. This method must be used if the dialog is to
     * have the same bounds (size and location) when shown as when it was last
     * hidden. If the dialog is never hidden and then shown again, there is no
     * need to use this method.
     * 
     * @param visible
     *            Flag indicating whether or not the dialog is to be made
     *            visible.
     */
    public void setVisible(boolean visible) {

        // If the visibility is already that requested, do nothing.
        if (visible == getShell().isVisible()) {
            return;
        }

        // Set the visibility as requested, restoring the saved boundaries
        // if showing the dialog, and saving the current boundaries if
        // hiding it.
        if (visible) {

            // This code is copied verbatim from jface Window's
            // initializeBounds() method.
            Point size = getInitialSize();
            Point location = getInitialLocation(size);
            getShell().setBounds(
                    getConstrainedShellBounds(new Rectangle(location.x,
                            location.y, size.x, size.y)));
        } else {

            // This code is copied mostly verbatim from the
            // org.eclipse.jface.Dialog's saveDialogBounds() method.
            IDialogSettings settings = getDialogBoundsSettings();
            if (settings != null) {
                Point shellLocation = getShell().getLocation();
                Point shellSize = getShell().getSize();
                Shell parent = getParentShell();
                if (parent != null) {
                    Point parentLocation = parent.getLocation();
                    shellLocation.x -= parentLocation.x;
                    shellLocation.y -= parentLocation.y;
                }
                int strategy = getDialogBoundsStrategy();
                if ((strategy & DIALOG_PERSISTLOCATION) != 0) {
                    settings.put(DIALOG_ORIGIN_X, shellLocation.x);
                    settings.put(DIALOG_ORIGIN_Y, shellLocation.y);
                }
                if ((strategy & DIALOG_PERSISTSIZE) != 0) {
                    settings.put(DIALOG_WIDTH, shellSize.x);
                    settings.put(DIALOG_HEIGHT, shellSize.y);
                    FontData[] fontDatas = JFaceResources.getDialogFont()
                            .getFontData();
                    if (fontDatas.length > 0) {
                        settings.put(DIALOG_FONT_DATA, fontDatas[0].toString());
                    }
                }
            }
        }

        // Set the visibility of the shell.
        getShell().setVisible(visible);
    }

    // Protected Methods

    /**
     * Get the dialog settings to be used for remembering the bounds of the
     * dialog. This method is guaranteed to provide a single dialog settings
     * object per subclass. If subclasses need to have multiple instances, each
     * with their own dialog boundaries settings object, they must override this
     * method and provide differentiation between their instances.
     * 
     * @return Dialog settings to be used.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = HazardServicesActivator.getDefault()
                .getDialogSettings();
        IDialogSettings section = settings.getSection(getClass().toString());
        if (section == null) {
            section = settings.addNewSection(getClass().toString());
        }
        return section;
    }

    /**
     * Initializes the location and size of this window's shell after it has
     * been created. This implementation ignores any resizing that happened
     * before this if the dialog has a menubar, because for some reason the
     * superclass implementation will use an absurdly large width and height if
     * a menubar is attached to the dialog.
     */
    @Override
    protected void initializeBounds() {
        if (getShell().getMenuBar() != null) {
            Point size = getInitialSize();
            Point location = getInitialLocation(size);
            getShell().setBounds(
                    getConstrainedShellBounds(new Rectangle(location.x,
                            location.y, size.x, size.y)));
        } else {
            super.initializeBounds();
        }
    }
}
