/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite megawidget, a simple megawidget that itself contains other
 * megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Sep 24, 2013    2168    Chris.Golden      Changed erroneous "widget"
 *                                           references to "megawidget"
 *                                           in comments and variable
 *                                           names.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new
 *                                           validator package, updated
 *                                           Javadoc and other comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to keep children synced
 *                                           with enabled and editable state.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CompositeSpecifier
 */
public class CompositeMegawidget extends ContainerMegawidget {

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing child
     *             megawidgets.
     */
    protected CompositeMegawidget(CompositeSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier);

        /*
         * Create a composite widget and its child megawidgets.
         */
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setEnabled(specifier.isEnabled());
        gridContainerPanel(composite);
        setComposite(composite);
        setChildren(createChildMegawidgets(composite,
                specifier.getColumnCount(), specifier.isEnabled(),
                specifier.isEditable(),
                specifier.getChildMegawidgetSpecifiers(), paramMap));
    }
}
