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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Description: Manager of detail field megawidgets that are children of a
 * megawidget. Detail field megawidgets are generally used to specify additional
 * information related to the parent megawidget's state. Instances of this class
 * create and keep track of such child megawidgets so that the parent megawidget
 * may delegate this work.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 10, 2014    2161    Chris.Golden      Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see Megawidget
 */
public class DetailChildrenManager {

    // Public Classes

    /**
     * Encapsulation of any composites created, and any detail megawidgets
     * created, as the result of a call to
     * {@link #createDetailChildMegawidgets(Composite, Composite, int, List)}.
     */
    public static class CompositesAndMegawidgets {

        // Private Variables

        /**
         * List of additional composites created.
         */
        private final List<Composite> composites;

        /**
         * List of detail megawidgets created.
         */
        private final List<IControl> detailMegawidgets;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param composites
         *            List of additional composites created.
         * @param detailMegawidgets
         *            List of detail megawidgets created.
         */
        private CompositesAndMegawidgets(List<Composite> composites,
                List<IControl> detailMegawidgets) {
            this.composites = composites;
            this.detailMegawidgets = detailMegawidgets;
        }

        // Public Methods

        /**
         * Get the list of additional composites created.
         * 
         * @return Composites.
         */
        public List<Composite> getComposites() {
            return composites;
        }

        /**
         * Get the list of detail megawidgets created.
         * 
         * @return Detail megawidgets.
         */
        public List<IControl> getDetailMegawidgets() {
            return detailMegawidgets;
        }
    }

    // Private Static Constants

    /**
     * Empty composites and detail megawidgets object.
     */
    private static final CompositesAndMegawidgets EMPTY_COMPOSITES_AND_MEGAWIDGETS = new CompositesAndMegawidgets(
            Collections.<Composite> emptyList(),
            Collections.<IControl> emptyList());

    // Private Variables

    /**
     * List of all detail child megawidgets that have been created.
     */
    private final List<IControl> detailMegawidgets = new ArrayList<>();

    /**
     * Map of creation-time attribute identifiers to corresponding values; this
     * is passed to each megawidget at construction time.
     */
    private final Map<String, Object> creationParams;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param creationParams
     *            Map of creation-time attribute identifiers to corresponding
     *            values; this is passed to each megawidget at construction
     *            time.
     */
    public DetailChildrenManager(Map<String, Object> creationParams) {
        this.creationParams = new HashMap<>(creationParams);
    }

    // Public Methods

    /**
     * Get the list of all detail child megawidgets.
     * 
     * @return List of all detail child megawidgets.
     */
    public List<IControl> getDetailMegawidgets() {
        return new ArrayList<>(detailMegawidgets);
    }

    /**
     * Create the GUI components for the specified detail child megawidgets,
     * placing them in the provided composite.
     * 
     * @param adjacentComposite
     *            Composite into which to place the child megawidgets' GUI
     *            representations for those children that are to be adjacent to
     *            the some component of the parent megawidget.
     * @param overallComposite
     *            Composite into which to place the child megawidgets' GUI
     *            representations for those children that are to be in
     *            subsequent rows under that of first row, if any. Note that
     *            this composite must hold <code>adjacentComposite</code>.
     * @param newRowLeftOffset
     *            Width in pixels of the left offset to be applied for any new
     *            row created in the <code>overallComposite</code>, leaving that
     *            much blank space to the left of the leftmost megawidget in
     *            each new row.
     * @param detailSpecifiers
     *            List of the choice's detail child megawidget specifiers.
     * @return List of any composites that were created to hold megawidgets on
     *         rows below <code>adjacentComposite</code>, together with a list
     *         of the detail megawidgets created.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing the
     *             associated megawidgets.
     */

    public CompositesAndMegawidgets createDetailChildMegawidgets(
            Composite adjacentComposite, Composite overallComposite,
            int newRowLeftOffset, List<IControlSpecifier> detailSpecifiers)
            throws MegawidgetException {

        /*
         * If there are no child megawidgets, do nothing.
         */
        if (detailSpecifiers.isEmpty()) {
            return EMPTY_COMPOSITES_AND_MEGAWIDGETS;
        }

        /*
         * Determine how many rows of megawidgets are specified, and how many
         * specifiers will be on each row. Specifiers that exist on rows after
         * the first will be placed within a composite, one per row.
         */
        Queue<Integer> specifierCountPerRow = new LinkedBlockingQueue<>();
        int specifierCount = 0;
        for (int j = 0; j < detailSpecifiers.size(); j++) {
            if (detailSpecifiers.get(j).isFullWidthOfColumn()) {
                if ((specifierCount > 0) || specifierCountPerRow.isEmpty()) {
                    specifierCountPerRow.add(specifierCount);
                    specifierCount = 0;
                }
                specifierCountPerRow.add(1);
            } else {
                specifierCount++;
            }
        }
        if (specifierCount > 0) {
            specifierCountPerRow.add(specifierCount);
        }

        /*
         * Iterate through the detail megawidget specifiers, creating each in
         * turn, ensuring that each is nested in the proper composite. First row
         * ones are simply placed in the provided adjacent composite, while any
         * that belong in subsequent rows have composites made for each such row
         * and are placed in those.
         */
        List<Composite> subsequentRowComposites = new ArrayList<>();
        List<IControl> theseDetailMegawidgets = new ArrayList<>();
        Composite composite = adjacentComposite;
        specifierCount = specifierCountPerRow.remove();
        int index = 0;
        for (IControlSpecifier detailSpecifier : detailSpecifiers) {

            /*
             * If this megawidget belongs in a new row, create a new composite
             * in which to place the megawidget and use it for this row.
             */
            if ((index++ == specifierCount)
                    && (specifierCountPerRow.isEmpty() == false)) {
                specifierCount = specifierCountPerRow.remove();
                composite = new Composite(overallComposite, SWT.NONE);
                GridLayout layout = new GridLayout(specifierCount, false);
                layout.horizontalSpacing = 3;
                layout.marginWidth = layout.marginHeight = 0;
                layout.marginLeft = newRowLeftOffset;
                composite.setLayout(layout);
                composite.setLayoutData(new GridData(
                        (specifierCount > 1 ? SWT.LEFT : SWT.FILL), SWT.TOP,
                        true, false));
                subsequentRowComposites.add(composite);
                index = 1;
            }

            /*
             * Create the megawidget.
             */
            IControl megawidget = detailSpecifier.createMegawidget(composite,
                    IControl.class, creationParams);
            theseDetailMegawidgets.add(megawidget);
        }
        detailMegawidgets.addAll(theseDetailMegawidgets);

        /*
         * Return any row composites that were created.
         */
        return new CompositesAndMegawidgets(subsequentRowComposites,
                theseDetailMegawidgets);
    }

    // Protected Methods

    /**
     * Get the creation parameters.
     * 
     * @return Creation parameters.
     */
    protected final Map<String, Object> getCreationParameters() {
        return creationParams;
    }
}
