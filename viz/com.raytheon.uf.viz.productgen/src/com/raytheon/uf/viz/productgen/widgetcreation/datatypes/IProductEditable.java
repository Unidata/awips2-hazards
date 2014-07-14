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

import org.eclipse.swt.widgets.Control;

import com.raytheon.uf.viz.productgen.widgetcreation.ProductEditorComposite;

/**
 * Interface for implementers to use if they want to build a widget for product
 * generation based on data type.
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

public interface IProductEditable<T extends Serializable, C extends Control> {

    abstract void setKey(String key);

    abstract String getKey();

    abstract C getWidget(T object, ProductEditorComposite parent);

    abstract T getData(C control);

    abstract void addListeners(C control);

}
