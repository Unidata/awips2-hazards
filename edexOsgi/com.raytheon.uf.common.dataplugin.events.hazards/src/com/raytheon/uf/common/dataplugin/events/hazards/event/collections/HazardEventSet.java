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
package com.raytheon.uf.common.dataplugin.events.hazards.event.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.IValidator;
import com.raytheon.uf.common.dataplugin.events.ValidationException;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Extends HashSet and utilizes validation before anything can be added to the
 * list. Verifies that the phenomenon and significance match those given in the
 * Collection.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2012            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class HazardEventSet extends HashSet<IHazardEvent> {
    private static final long serialVersionUID = 1L;

    private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

    public HazardEventSet() {
        super();
    }

    public HazardEventSet(HazardEventSet set,
            Map<String, Serializable> attributes) {
        addAll(set);
        this.attributes = attributes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(IHazardEvent e) {
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
    public boolean addAll(Collection<? extends IHazardEvent> c) {
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>();
        events.addAll(c);
        for (IHazardEvent event : events) {
            boolean valid = validate(event);
            if (valid == false) {
                c.remove(event);
            }
        }
        return super.addAll(c);
    }

    /**
     * Does checks whether the phenomenon and the significance match the
     * current.
     * 
     * @param e
     * @return
     */
    private boolean validate(IHazardEvent e) {
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

    /**
     * A {@link HazardEventSet} does not necessarily need to be a single event,
     * it could be multiple types of phenomenon and significances, this will
     * determine whether it can be treated as single or not.
     * 
     * @return
     */
    public boolean isSingleEvent() {
        String phen = null;
        String sig = null;
        Geometry geom = null;
        Iterator<IHazardEvent> iter = iterator();
        while (iter.hasNext()) {
            IHazardEvent event = iter.next();
            if (phen == null) {
                phen = event.getPhenomenon();
            }
            if (sig == null) {
                sig = event.getSignificance();
            }
            if (geom == null) {
                geom = event.getGeometry();
            }

            if (event.getPhenomenon().equals(phen) == false) {
                return false;
            }
            if (event.getSignificance().equals(sig) == false) {
                return false;
            }
            if (event.getGeometry().touches(geom) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Since the phenomenon might be shared by the entire set of hazards, this
     * is a helper method.
     * 
     * @return
     */
    public String getPhenomenon() {
        if (isSingleEvent()) {
            for (Iterator<IHazardEvent> iter = iterator(); iter.hasNext();) {
                IHazardEvent event = iter.next();
                return event.getPhenomenon();
            }
        }
        throw new UnsupportedOperationException(
                "Cannot get phenomenon as this is not a single event");
    }

    /**
     * Since the significance might be shared by the entire set of hazards, this
     * is a helper method.
     * 
     * @return
     */
    public String getSignificance() {
        if (isSingleEvent()) {
            for (Iterator<IHazardEvent> iter = iterator(); iter.hasNext();) {
                IHazardEvent event = iter.next();
                return event.getSignificance();
            }
        }
        throw new UnsupportedOperationException(
                "Cannot get significance as this is not a single event");
    }

    /**
     * Since the phensig might be shared by the entire set of hazards, this is a
     * helper method.
     * 
     * @return
     */
    public String getPhenSig() {
        if (isSingleEvent()) {
            for (Iterator<IHazardEvent> iter = iterator(); iter.hasNext();) {
                IHazardEvent event = iter.next();
                return event.getPhenomenon() + "." + event.getSignificance();
            }
        }
        throw new UnsupportedOperationException(
                "Cannot get phensig as this is not a single event");
    }

    public void addAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    public Serializable getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * @return the attributes
     */
    public Map<String, Serializable> getAttributes() {
        return attributes;
    }

}