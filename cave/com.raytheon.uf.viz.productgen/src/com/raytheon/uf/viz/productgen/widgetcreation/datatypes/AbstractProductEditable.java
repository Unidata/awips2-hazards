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
package com.raytheon.uf.viz.productgen.widgetcreation.datatypes;

import java.io.Serializable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * TODO Add Description
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 8, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public abstract class AbstractProductEditable<T extends Serializable, C extends Control>
        implements IProductEditable<T, C> {

    private Listener listener;

    private String key;

    /**
     * 
     */
    public AbstractProductEditable(Listener listener) {
        this.listener = listener;
    }

    /**
     * Sets the layout info for the underlying widgets
     * 
     * @param cols
     * @param makeColsEqualWidth
     * @param horAlignment
     * @param vertAlignment
     * @param fillHor
     * @param fillVert
     */
    protected void setLayoutInfo(Composite comp, int cols,
            boolean makeColsEqualWidth, int horAlignment, int vertAlignment,
            boolean fillHor, boolean fillVert) {
        GridLayout layout = new GridLayout(cols, false);
        // layout.marginHeight = 0;
        // layout.marginWidth = 0;
        comp.setLayout(layout);

        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comp.setLayoutData(gridData);
    }

    protected void fireListener(Control control) {
        Event event = new Event();
        event.text = (String) control.getData("key");
        listener.handleEvent(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable
     * #setKey(java.lang.String)
     */
    @Override
    public void setKey(String key) {
        this.key = key;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable
     * #getKey()
     */
    @Override
    public String getKey() {
        return this.key;
    }

}
