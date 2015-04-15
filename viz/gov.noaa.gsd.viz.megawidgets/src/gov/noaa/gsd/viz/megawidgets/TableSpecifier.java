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

import gov.noaa.gsd.viz.megawidgets.validators.TableValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

/**
 * Description: Table megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 02, 2015    4162    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TableSpecifier extends StatefulMegawidgetSpecifier implements
        IControlSpecifier, IMultiLineSpecifier {

    // Public Static Constants

    /**
     * Column header labels parameter name; a megawidget must include an array
     * of one or more strings giving the labels to be placed in the column
     * headers. Note that the number of strings in the array will be the number
     * of columns shown.
     */
    public static final String MEGAWIDGET_COLUMN_HEADER_LABELS = "columnHeaders";

    // Private Variables

    /**
     * Control options manager.
     */
    private final ControlSpecifierOptionsManager optionsManager;

    /**
     * Number of lines that should be visible.
     */
    private final int numVisibleLines;

    /**
     * Column header labels.
     */
    private final List<String> headerLabels;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param parameters
     *            Map holding the parameters that will be used to configure a
     *            megawidget created by this specifier as a set of key-value
     *            pairs.
     * @throws MegawidgetSpecificationException
     *             If the megawidget specifier parameters are invalid.
     */
    public TableSpecifier(Map<String, Object> parameters)
            throws MegawidgetSpecificationException {
        super(parameters, new TableValidator());
        optionsManager = new ControlSpecifierOptionsManager(this, parameters,
                ControlSpecifierOptionsManager.BooleanSource.TRUE);

        /*
         * Ensure that the visible lines count, if present, is acceptable, and
         * if not present is assigned a default value.
         */
        numVisibleLines = ConversionUtilities
                .getSpecifierIntegerValueFromObject(getIdentifier(), getType(),
                        parameters.get(MEGAWIDGET_VISIBLE_LINES),
                        MEGAWIDGET_VISIBLE_LINES, 6);
        if (numVisibleLines < 1) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_VISIBLE_LINES, numVisibleLines,
                    "must be positive integer");
        }

        /*
         * Ensure that the column header labels are present as an array of
         * strings.
         */
        List<?> headersList = null;
        List<String> headers = null;
        try {
            headersList = (List<?>) parameters
                    .get(MEGAWIDGET_COLUMN_HEADER_LABELS);
            headers = new ArrayList<>(headersList.size());
            for (Object label : headersList) {
                headers.add((String) label);
            }
        } catch (Exception e) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_COLUMN_HEADER_LABELS,
                    parameters.get(MEGAWIDGET_COLUMN_HEADER_LABELS),
                    "must be list of strings");
        }
        if ((headersList == null) || headersList.isEmpty()) {
            throw new MegawidgetSpecificationException(getIdentifier(),
                    getType(), MEGAWIDGET_COLUMN_HEADER_LABELS, null, null);
        }
        headerLabels = ImmutableList.copyOf(headers);
    }

    // Public Methods

    @Override
    public boolean isEditable() {
        return optionsManager.isEditable();
    }

    @Override
    public int getWidth() {
        return optionsManager.getWidth();
    }

    @Override
    public boolean isFullWidthOfDetailPanel() {
        return optionsManager.isFullWidthOfDetailPanel();
    }

    @Override
    public int getSpacing() {
        return optionsManager.getSpacing();
    }

    @Override
    public int getNumVisibleLines() {
        return numVisibleLines;
    }

    /**
     * Get the list of column header labels.
     * 
     * @return List of column header labels.
     */
    public final List<String> getColumnHeaderLabels() {
        return headerLabels;
    }
}
