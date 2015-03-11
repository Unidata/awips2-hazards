/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.productgen.dialog;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * 
 * Utility class used by the Product Editor and it's associated classes for
 * commonly used functions.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 03/11/2015   6889       bphillip     Modifications to allow more than one undo action in the Product Editor
 *                                      Slightly increased button width to allow the number of undo actions 
 *                                      available in the Product editor to be displayed
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class ProductEditorUtil {

    /** The standard button width */
    private static final int BUTTON_WIDTH = 65;

    /**
     * The entry tab label format used when more than one format tab is
     * displaying the same formatted data
     */
    protected static final String ENTRY_TAB_LABEL_FORMAT = "%s (%d)";

    /** The current time provider */
    protected static final ICurrentTimeProvider currentTimeProvider = new ICurrentTimeProvider() {
        @Override
        public long getCurrentTime() {
            return SimulatedTime.getSystemTime().getMillis();
        }
    };

    /**
     * Sets the layout information for a Composite.
     * 
     * @param composite
     *            The composite to be formatted
     * @param columns
     *            Number of columns in the grid layout
     * @param makeColumnsEqualWidth
     *            Whether to make the columns uniform width
     * @param horizontalAlignment
     *            The horizontal alignment hint
     * @param verticalAlignment
     *            The vertical alignment hint
     * @param grabExcessHorizontalSpace
     *            Whether components should grab any extra horizontal space
     * @param grabExcessVerticalSpace
     *            Whether components should grab any extra vertical space
     */
    protected static void setLayoutInfo(Composite composite, int columns,
            boolean makeColumnsEqualWidth, int horizontalAlignment,
            int verticalAlignment, boolean grabExcessHorizontalSpace,
            boolean grabExcessVerticalSpace) {
        setLayoutInfo(composite, columns, makeColumnsEqualWidth,
                horizontalAlignment, verticalAlignment,
                grabExcessHorizontalSpace, grabExcessVerticalSpace, null, null);
    }

    /**
     * Sets the layout information for a Composite.
     * 
     * @param composite
     *            The composite to be formatted
     * @param columns
     *            Number of columns in the grid layout
     * @param makeColumnsEqualWidth
     *            Whether to make the columns uniform width
     * @param horizontalAlignment
     *            The horizontal alignment hint
     * @param verticalAlignment
     *            The vertical alignment hint
     * @param grabExcessHorizontalSpace
     *            Whether components should grab any extra horizontal space
     * @param grabExcessVerticalSpace
     *            Whether components should grab any extra vertical space
     * @param widthHint
     *            The desired width of the composite
     * @param heightHint
     *            The desired height of the composite
     */
    protected static void setLayoutInfo(Composite composite, int columns,
            boolean makeColumnsEqualWidth, int horizontalAlignment,
            int verticalAlignment, boolean grabExcessHorizontalSpace,
            boolean grabExcessVerticalSpace, Integer widthHint,
            Integer heightHint) {
        GridLayout layout = new GridLayout(columns, makeColumnsEqualWidth);
        GridData layoutData = new GridData(horizontalAlignment,
                verticalAlignment, grabExcessHorizontalSpace,
                grabExcessVerticalSpace);
        if (widthHint != null) {
            layoutData.widthHint = widthHint;
        }
        if (heightHint != null) {
            layoutData.heightHint = heightHint;
        }
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        composite.setLayoutData(layoutData);
    }

    /**
     * Assigns GridData to a button in order to make all buttons in the
     * ProductEditor of uniform size
     * 
     * @param button
     *            The button to format
     */
    protected static void setButtonGridData(Button button) {
        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        button.setLayoutData(data);
        button.pack();
        if (button.getSize().x < BUTTON_WIDTH) {
            data.widthHint = BUTTON_WIDTH;
            button.setLayoutData(data);
        }
    }

    /**
     * Gets the formatted text label when more than one entry exists for a given
     * format
     * 
     * @param product
     *            The product containing the formatted text
     * @param format
     *            The format name
     * @param index
     *            The index into the list containing the formatted texts
     * @return The formatted label
     */
    protected static String getFormattedTextTabLabel(IGeneratedProduct product,
            String format, int index) {
        return product.getEntry(format).size() > 1 ? String.format(
                ENTRY_TAB_LABEL_FORMAT, format, index) : format;
    }
}
