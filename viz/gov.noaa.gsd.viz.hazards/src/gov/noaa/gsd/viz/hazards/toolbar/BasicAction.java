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

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;

/**
 * Abstract class from which more specific actions may be derived.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 04, 2013            Chris.Golden  Initial induction into repo
 * Jul 19, 2013     585    Chris.Golden  Replaced string literals in
 *                                       code with constants.
 * Jan 17, 2018   33428    Chris.Golden  Added new constructor.
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

    /**
     * Icons directory name.
     */
    private static final String ICONS_DIRECTORY_NAME = "icons";

    /**
     * URL base for finding icon images.
     */
    private static final URL ICONS_URL_BASE;

    static {
        URL iconsURL = null;
        try {
            iconsURL = FileLocator.find(
                    HazardServicesActivator.getDefault().getBundle(),
                    new Path(ICONS_DIRECTORY_NAME + File.separator), null);
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

    /**
     * Construct a standard instance.
     * 
     * @param text
     *            Text to be displayed.
     * @param style
     *            Style; one of the <code>IAction</code> style constants.
     * @param imageDescriptor
     *            Image descriptor, or <code>null</code> if no image is to be
     *            associated with this action.
     * @param toolTipText
     *            Tool tip text, or <code>null</code> if none is required.
     */
    public BasicAction(String text, int style, ImageDescriptor imageDescriptor,
            String toolTipText) {
        super(text, style);
        if (imageDescriptor != null) {
            setImageDescriptor(imageDescriptor);
        }
        if (toolTipText != null) {
            setToolTipText(toolTipText);
        }
    }

    // Public Methods

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
            statusHandler
                    .error("BasicAction.getImageDescriptor(): Unable to resolve "
                            + "URL for " + name + ".", e);
            return null;
        }
    }
}
