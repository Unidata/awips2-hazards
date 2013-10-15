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
package com.raytheon.uf.viz.productgen.widgetcreation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.DateProductEditable;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.IProductEditable;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.NumberProductEditable;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.StringListProductEditable;
import com.raytheon.uf.viz.productgen.widgetcreation.datatypes.StringProductEditable;

/**
 * The registry that handles the lookup and creation of the
 * {@link IProductEditable} classes by data type.
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

public class WidgetCreationRegistry {
    private static final IUFStatusHandler handler = UFStatus
            .getHandler(WidgetCreationRegistry.class);

    private static WidgetCreationRegistry registry;

    private Map<Class<?>, IProductEditable<? extends Serializable, ? extends Control>> editables;

    /**
     * Private constructor
     */
    private WidgetCreationRegistry(Listener listener) {
        editables = new HashMap<Class<?>, IProductEditable<? extends Serializable, ? extends Control>>();
        setupEditables(listener);
    }

    public static synchronized WidgetCreationRegistry getInstance(
            Listener listener) {
        if (registry == null) {
            registry = new WidgetCreationRegistry(listener);
        }
        return registry;
    }

    /**
     * Registers the {@link IProductEditable} for use by data type. Future may
     * do this in Spring.
     */
    private void setupEditables(Listener listener) {
        registerType(Date.class, new DateProductEditable(listener));
        registerType(String.class, new StringProductEditable(listener));
        registerType(ArrayList.class, new StringListProductEditable(listener));
        registerType(Double.class, new NumberProductEditable(listener));
        registerType(Float.class, new NumberProductEditable(listener));
        registerType(Integer.class, new NumberProductEditable(listener));
    }

    /**
     * Gets the {@link IProductEditable} class to retrieve the widget and the
     * value.
     * 
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable, C extends Control> IProductEditable<T, C> getProductEditable(
            Serializable data) {
        IProductEditable<T, C> editable = (IProductEditable<T, C>) editables
                .get(data.getClass());
        if (editable == null) {
            handler.info("Data type " + data.getClass().getName()
                    + " is not registered");
        }
        return editable;
    }

    public void registerType(Class<?> clazz,
            IProductEditable<? extends Serializable, ? extends Control> editable) {
        if (editables.containsKey(clazz) == false) {
            editables.put(clazz, editable);
        } else {
            throw new UnsupportedOperationException(
                    "Cannot add multiple product editables for same data type");
        }
    }
}
