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

import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;

/**
 * The {@link IProductEditable} to handle a date field, adding both a date and a
 * time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 3, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class DateProductEditable extends
        AbstractProductEditable<Date, Composite> {

    /**
     * 
     */
    public DateProductEditable(Listener listener) {
        super(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getComposite
     * (java.io.Serializable)
     */
    @Override
    public Composite getWidget(Date object, final ProductEditorComposite parent) {
        setLayoutInfo(parent, 2, false, SWT.FILL, SWT.NONE, true, false);

        Date date = (Date) object;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        DateTime theDate = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN
                | SWT.BORDER);
        theDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        theDate.setLayoutData(gridData);

        DateTime theTime = new DateTime(parent, SWT.TIME | SWT.BORDER
                | SWT.SHORT);
        theTime.setTime(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND));
        gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        theTime.setLayoutData(gridData);

        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#getData(org
     * .eclipse.swt.widgets.Control)
     */
    @Override
    public Date getData(Composite date) {
        DateTime theDate = (DateTime) date.getChildren()[0];
        DateTime theTime = (DateTime) date.getChildren()[1];
        Calendar cal = Calendar.getInstance();
        cal.set(theDate.getYear(), theDate.getMonth(), theDate.getDay(),
                theTime.getHours(), theTime.getMinutes(), theTime.getSeconds());
        return cal.getTime();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.datatypes.IProductEditable#addListener
     * (org.eclipse.swt.widgets.Control)
     */
    @Override
    public void addListeners(final Composite control) {
        DateTime theDate = (DateTime) control.getChildren()[0];
        DateTime theTime = (DateTime) control.getChildren()[1];
        theDate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fireListener(control);
            }
        });
        theTime.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fireListener(control);
            }
        });
    }

}
