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
package com.raytheon.uf.common.dataplugin.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extends {@link HashSet} and utilizes validation before anything can be added
 * to the list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 27, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class EventSet<E extends IEvent> extends HashSet<E> {

    private static final long serialVersionUID = 1L;

    private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

    /**
     * 
     */
    public EventSet() {
        super();
    }

    public EventSet(Set<E> set, Map<String, Serializable> attributes) {
        addAll(set);
        this.attributes = attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(E e) {
        boolean valid = validate(e);
        if (valid) {
            return super.add(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        Collection<E> events = new ArrayList<E>();
        events.addAll(c);
        for (E event : events) {
            boolean valid = validate(event);
            if (valid == false) {
                c.remove(event);
            }
        }
        return super.addAll(c);
    }

    /**
     * Add the attribute with the key and the serializable value
     * 
     * @param key
     * @param value
     */
    public void addAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    /**
     * Get the attribute by the key.
     * 
     * @param key
     * @return
     */
    public Serializable getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * @return the attributes
     */
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

    /**
     * Does a validation check before it gets added to the set
     * 
     * @param e
     * @return
     */
    private boolean validate(E e) {
        boolean valid = true;
        if (e instanceof IValidator) {
            try {
                valid = ((IValidator) e).isValid();
            } catch (ValidationException e1) {
                valid = false;
            }
        }

        return valid;
    }
}
