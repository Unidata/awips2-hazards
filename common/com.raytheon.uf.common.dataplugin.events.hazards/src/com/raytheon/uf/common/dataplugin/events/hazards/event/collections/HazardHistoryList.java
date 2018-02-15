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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * List of a series of hazards events as they have progressed through their
 * lifecycle.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 31, 2012            mnash        Initial creation
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Feb 16, 2017 29138     Chris.Golden  Changed to use HazardEvent instead of
 *                                      IHazardEvent, since only the former
 *                                      has a unique identifier.
 * Mar 23, 2017 28470     Kevin.Bisanz  Added method to getLatestEvent()
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */
@DynamicSerialize
public class HazardHistoryList implements List<HazardEvent> {
    @DynamicSerializeElement
    private List<HazardEvent> events = new LinkedList<HazardEvent>();

    public IEvent getEventAtTime(Date date) {
        for (int i = 0; i < size(); i++) {
            IEvent event = get(i);
            if ((event.getStartTime().before(date)
                    && event.getEndTime().after(date))
                    || event.getStartTime().equals(date)
                    || event.getEndTime().equals(date)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Since the phenomenon MUST be the same for the same weather occurrence, we
     * can add this method to retrieve based on the phenomenon in the first
     * event.
     * 
     * @return
     */
    public String getPhenomenon() {
        if (events.isEmpty() == false) {
            return events.get(0).getPhenomenon();
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#add(int, java.lang.Object)
     */
    @Override
    public void add(int index, HazardEvent element) {
        events.add(index, element);
        sort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#addAll(int, java.util.Collection)
     */
    @Override
    public boolean addAll(int index, Collection<? extends HazardEvent> c) {
        boolean success = false;
        if (c != null) {
            success = events.addAll(index, c);
            sort();
        }
        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends HazardEvent> c) {
        boolean success = false;
        if (c != null) {
            success = events.addAll(c);
            sort();
        }
        return success;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(HazardEvent e) {
        // events are sorted by time, so we need to compare when we add that way
        // they are stored correctly
        boolean success = events.add(e);
        if (success) {
            sort();
        }
        return success;
    }

    private void sort() {
        Collections.sort(events, IHazardEvent.SORT_BY_PERSIST_TIME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#size()
     */
    @Override
    public int size() {
        return events.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return events.contains(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#iterator()
     */
    @Override
    public Iterator<HazardEvent> iterator() {
        return events.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#toArray()
     */
    @Override
    public Object[] toArray() {
        return events.toArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return events.toArray(a);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
        return events.remove(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return events.containsAll(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return events.removeAll(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return events.retainAll(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#clear()
     */
    @Override
    public void clear() {
        events.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#get(int)
     */
    @Override
    public HazardEvent get(int index) {
        return events.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#set(int, java.lang.Object)
     */
    @Override
    public HazardEvent set(int index, HazardEvent element) {
        return events.set(index, element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#remove(int)
     */
    @Override
    public HazardEvent remove(int index) {
        return events.remove(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(Object o) {
        return events.indexOf(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(Object o) {
        return events.lastIndexOf(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#listIterator()
     */
    @Override
    public ListIterator<HazardEvent> listIterator() {
        return events.listIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<HazardEvent> listIterator(int index) {
        return events.listIterator(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#subList(int, int)
     */
    @Override
    public List<HazardEvent> subList(int fromIndex, int toIndex) {
        return events.subList(fromIndex, toIndex);
    }

    /**
     * @return the events
     */
    public List<HazardEvent> getEvents() {
        return events;
    }

    /**
     * @param events
     *            the events to set
     */
    public void setEvents(List<HazardEvent> events) {
        this.events = events;
    }

    /**
     * @return The latest (most recent) event in the history or null if there is
     *         no history.
     */
    public HazardEvent getLatestEvent() {
        if (events.isEmpty()) {
            return null;
        } else {
            int lastIndex = events.size() - 1;
            return events.get(lastIndex);
        }
    }
}
