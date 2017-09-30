/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Modification of the attribute properties of an event.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 21, 2017   38072    Chris.Golden Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class EventAttributesModification implements IEventModification {

    // Private Variables

    /**
     * Map of attribute names to their new values. Values may be
     * <code>null</code> if the attributes have been removed.
     */
    private final Map<String, Serializable> attributes;

    /**
     * Read-only view of {@link #attributes}. {@link ImmutableMap} would be used
     * for <code>attributes</code>, which would obviate the need for this
     * read-only view, if <code>ImmutableMap</code> accepted <code>null</code>
     * values.
     */
    private final Map<String, Serializable> attributesView;

    /**
     * As with {@link #attributes}, but holding the old values of the attributes
     * (prior to the changes that triggered this notification). Values may be
     * <code>null</code> if they were previously nonexistent.
     */
    private final Map<String, Serializable> oldAttributes;

    /**
     * Read-only view of {@link #oldAttributes}. {@link ImmutableMap} would be
     * used for <code>oldAttributes</code>, which would obviate the need for
     * this read-only view, if <code>ImmutableMap</code> accepted
     * <code>null</code> values.
     */
    private final Map<String, Serializable> oldAttributesView;

    // Public Constructors

    /**
     * Construct an instance indicating that a single attribute changed.
     * 
     * @param attributeKey
     *            Key of the attribute that changed.
     * @param attributeValue
     *            New value of the attribute that changed; if <code>null</code>,
     *            the attribute has been removed.
     * @param attributeOldValue
     *            Old value of the attribute that changed; if <code>null</code>,
     *            the attribute has been added.
     */
    public EventAttributesModification(String attributeKey,
            Serializable attributeValue, Serializable oldAttributeValue) {
        this.attributes = new HashMap<>(1, 1.0f);
        this.attributes.put(attributeKey, attributeValue);
        this.attributesView = Collections.unmodifiableMap(this.attributes);
        this.oldAttributes = new HashMap<>(1, 1.0f);
        this.oldAttributes.put(attributeKey, oldAttributeValue);
        this.oldAttributesView = Collections
                .unmodifiableMap(this.oldAttributes);
    }

    /**
     * Construct an instance indicating that multiple attributes changed.
     * 
     * @param attributes
     *            Map of attributes that changed with their new values. Values
     *            may be <code>null</code> if attributes have been removed.
     * @param oldAttributes
     *            Map of attributes that changed with their old values. Values
     *            may be <code>null</code> if attributes have been added.
     */
    public EventAttributesModification(Map<String, Serializable> attributes,
            Map<String, Serializable> oldAttributes) {
        this.attributes = new HashMap<>(attributes);
        this.attributesView = Collections.unmodifiableMap(this.attributes);
        this.oldAttributes = new HashMap<>(oldAttributes);
        this.oldAttributesView = Collections
                .unmodifiableMap(this.oldAttributes);
    }

    // Public Methods

    /**
     * Determine whether or not the specified attribute was changed.
     * 
     * @param key
     *            Key of the attribute to be checked.
     * @return <code>true</code> if the value associated with the attribute was
     *         changed, <code>false</code> otherwise.
     */
    public boolean containsAttribute(String key) {
        return attributesView.keySet().contains(key);
    }

    /**
     * Get the attributes that changed. Note that the returned set is not
     * modifiable.
     * 
     * @return Keys of the attributes that changed.
     */
    public Set<String> getAttributeKeys() {
        return attributesView.keySet();
    }

    /**
     * Get the new value of the specified attribute.
     * 
     * @param key
     *            Key of the attribute for which to fetch the value.
     * @return Value of the attribute; may be <code>null</code> if the attribute
     *         has been removed.
     */
    public Serializable getAttribute(String key) {
        return attributesView.get(key);
    }

    /**
     * Get the old value of the specified attribute.
     * 
     * @param key
     *            Key of the attribute for which to fetch the old value.
     * @return Old value of the attribute; may be <code>null</code> if the
     *         attribute has been added.
     */
    public Serializable getOldAttribute(String key) {
        return oldAttributesView.get(key);
    }

    @Override
    public void apply(IHazardEvent sourceEvent, IHazardEvent targetEvent) {
        targetEvent.addHazardAttributes(attributesView);
    }

    @Override
    public MergeResult<IEventModification> merge(IEventModification original,
            IEventModification modified) {
        if (modified instanceof EventAttributesModification) {

            /*
             * Get the new modification's maps of attributes and of old
             * attributes.
             */
            Map<String, Serializable> modifiedAttributes = ((EventAttributesModification) modified).attributes;
            Map<String, Serializable> modifiedOldAttributes = ((EventAttributesModification) modified).oldAttributes;

            /*
             * Copy this modification's attributes, then put all the new
             * modification's attributes into the new map as well. Then remove
             * any entries in the resulting attributes map in which the new
             * value is the same as this modification's old value for that same
             * attribute, as this means that the attribute simply changed to be
             * something new, and then changed back to the old value.
             */
            Map<String, Serializable> newAttributes = new HashMap<>(attributes);
            newAttributes.putAll(modifiedAttributes);
            newAttributes.entrySet()
                    .removeIf(entry -> (entry
                            .getValue() == oldAttributes.get(entry
                                    .getKey())
                    || ((entry.getValue() != null) && entry.getValue()
                            .equals(oldAttributes.get(entry.getKey())))));

            /*
             * Create an old attributes map that goes with the new combined
             * attributes map by choosing, for each key in the new attributes
             * map, a corresponding value taken from the this modification's old
             * attributes map if one is available, otherwise, from the new
             * modificaton's old attributes map.
             * 
             * This is made more complicated by the annoying fact that
             * Collectors.toMap() throws an error if one of the values of the
             * map being created is null. Thus, the names of the new attributes
             * are divided into two sets, those which will have non-null values,
             * and those which will have null values. The former have a map
             * created from them, and then null values are added to that same
             * map for each key in the latter set.
             */
            Set<String> nullAttributeNames = newAttributes.keySet().stream()
                    .filter(name -> (oldAttributes.containsKey(name)
                            ? oldAttributes.get(name)
                            : modifiedOldAttributes.get(name)) == null)
                    .collect(Collectors.toSet());
            Set<String> nonNullAttributeNames = Sets
                    .difference(newAttributes.keySet(), nullAttributeNames);

            Map<String, Serializable> newOldAttributes = nonNullAttributeNames
                    .stream()
                    .collect(Collectors.toMap(Function.identity(),
                            name -> oldAttributes.containsKey(name)
                                    ? oldAttributes.get(name)
                                    : modifiedOldAttributes.get(name)));
            for (String name : nullAttributeNames) {
                newOldAttributes.put(name, null);
            }

            return IMergeable.getSuccessSubjectCancellationResult(
                    new EventAttributesModification(newAttributes,
                            newOldAttributes));
        } else {
            return IMergeable.getFailureResult();
        }
    }
}
