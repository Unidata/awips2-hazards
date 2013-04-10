/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Abstract class from which more specific actions may be derived.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 */
public abstract class BasicAction extends Action {

    // Private Static Constants

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(BasicAction.class);

    // Private Static Constants

    /**
     * URL base for finding icon images.
     */
    private static final URL ICONS_URL_BASE;

    // Initialize icons URL base.
    static {
        URL iconsURL = null;
        try {
            iconsURL = FileLocator.find(HazardServicesActivator.getDefault()
                    .getBundle(), new Path("icons" + File.separator), null);
        } catch (Exception e) {
            statusHandler.error(
                    "BasicAction.<static init>: Will not be able to "
                            + "get button icons because couldn't resolve URL.",
                    e);
        }
        ICONS_URL_BASE = iconsURL;
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param text
     *            Text to be displayed.
     * @param iconFileName
     *            File name of the icon to be displayed, or <code>null</code> if
     *            no icon is to be associated with this action.
     * @param style
     *            Style; one of the <code>IAction</code> style constants.
     * @param toolTipText
     *            Tool tip text, or <code>null</code> if none is required.
     */
    public BasicAction(String text, String iconFileName, int style,
            String toolTipText) {
        super(text, style);
        if (iconFileName != null) {
            setImageDescriptor(getImageDescriptorForFile(iconFileName));
        }
        if (toolTipText != null) {
            setToolTipText(toolTipText);
        }
    }

    // Public Methods

    /**
     * Run the action.
     */
    @Override
    public abstract void run();

    // Protected Methods

    /**
     * Create an image descriptor from the specified icon file name.
     * 
     * @param name
     *            Name of the icon file.
     * @return Image descriptor, or <code>null</code> if none was found.
     */
    protected final ImageDescriptor getImageDescriptorForFile(String name) {
        try {
            return ImageDescriptor.createFromURL(new URL(ICONS_URL_BASE, name));
        } catch (Exception e) {
            statusHandler.error(
                    "BasicAction.getImageDescriptor(): Unable to resolve "
                            + "URL for " + name + ".", e);
            return null;
        }
    }
}
