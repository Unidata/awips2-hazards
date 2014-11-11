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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

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
 *                                           references to "megawidget" in
 *                                           comments and variable names.
 * Apr 24, 2014    2925    Chris.Golden      Changed to work with new
 *                                           validator package, updated
 *                                           Javadoc and other comments.
 * Jun 17, 2014    3982    Chris.Golden      Changed to keep children synced
 *                                           with enabled and editable state.
 * Oct 20, 2014    4818    Chris.Golden      Added option of providing a
 *                                           scrollable panel for child
 *                                           megawidgets. Also added use of
 *                                           display settings, allowing the
 *                                           saving and restoring of scroll
 *                                           origin.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see CompositeSpecifier
 */
public class CompositeMegawidget extends SinglePageMegawidget {

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
         * If the megawidget is to be scrollable, create a copy of the
         * creation-time parameters map, so that alterations to it made below do
         * not affect the original, and then create the scrolled composite and
         * get a reference to its client-area composite in which child
         * megawidgets will be placed. Otherwise, just create the composite
         * directly.
         */
        Composite composite;
        final ScrolledComposite scrolledComposite;
        if (specifier.isScrollable()) {
            paramMap = new HashMap<>(paramMap);
            scrolledComposite = UiBuilder.buildScrolledComposite(this, parent,
                    getSinglePageScrollSettings(), paramMap);
            scrolledComposite.setEnabled(specifier.isEnabled());
            gridContainerPanel(scrolledComposite);
            composite = (Composite) scrolledComposite.getContent();
            setComposite(scrolledComposite);
        } else {
            composite = new Composite(parent, SWT.NONE);
            composite.setEnabled(specifier.isEnabled());
            gridContainerPanel(composite);
            setComposite(composite);
            scrolledComposite = null;
        }

        /*
         * Create the child megawidgets and remember them.
         */
        setChildren(createChildMegawidgets(composite,
                specifier.getColumnCount(), specifier.isEnabled(),
                specifier.isEditable(),
                specifier.getChildMegawidgetSpecifiers(), paramMap));

        /*
         * If this is a scrollable megawidget, give the scrolled composite an
         * opportunity to compute its size.
         */
        if (scrolledComposite != null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    UiBuilder.updateScrolledAreaSize(scrolledComposite);
                }
            });
            setScrolledComposite(scrolledComposite);
        }
    }
}
