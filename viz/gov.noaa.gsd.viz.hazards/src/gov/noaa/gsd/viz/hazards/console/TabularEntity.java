/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.common.utilities.Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.raytheon.uf.common.colormap.Color;

/**
 * Description: Tabular entity, instances of which are used to track hazard
 * events in the console and to populate the console's tree table.
 * <p>
 * Tabular entities may be created via the various <code>build()</code> methods,
 * most of which take a <code>TabularEvent</code> as a parameter so that it may
 * be reused if it turns out that the entity to be built is equivalent to the
 * one provided.
 * </p>
 * <p>
 * Tabular entities are designed to be immutable, and should be used as such.
 * This means that care must be taken to avoid changing underlying component
 * objects that may at some level be mutable, such as {@link Color} and
 * {@link Date}.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 12, 2016   15556    Chris.Golden Initial creation.
 * Mar 16, 2017   15528    Chris.Golden Added unsaved flag.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class TabularEntity {

    // Private Variables

    /**
     * Event identifier of this entity.
     */
    private final String identifier;

    /**
     * Index of this entity in the reversed history list of the event, if it has
     * been persisted.
     */
    private final Integer historyIndex;

    /**
     * Timestamp indicating when this entity was persisted, if it has been. This
     * is ignored if {@link #historyIndex} is <code>null</code>.
     */
    private final Date persistedTimestamp;

    /**
     * Flag indicating whether or not this entity has unsaved changes.
     */
    private boolean unsaved;

    /**
     * Time range covered by the event.
     */
    private Range<Long> timeRange;

    /**
     * Flag indicating whether or not the upper end of the {@link #timeRange} is
     * currently "until further notice".
     */
    private boolean endTimeUntilFurtherNotice;

    /**
     * Flag indicating whether or not the interval between the
     * {@link #timeRange} end points should be fixed.
     */
    private boolean timeRangeIntervalLocked;

    /**
     * Boundaries of allowable values for the lower end of the
     * {@link #timeRange}.
     */
    private Range<Long> lowerTimeBoundaries;

    /**
     * Boundaries of allowable values for the upper end of the
     * {@link #timeRange}.
     */
    private Range<Long> upperTimeBoundaries;

    /**
     * Time resolution in use for the {@link #timeRange}.
     */
    private TimeResolution timeResolution;

    /**
     * Flag indicating whether or not "until further notice" is allowable for
     * this event's upper value of its {@link #timeRange}.
     */
    private boolean allowUntilFurtherNotice;

    /**
     * Color.
     */
    private Color color;

    /**
     * Flag indicating whether or not the event is selected.
     */
    private boolean selected;

    /**
     * Flag indicating whether or not the event is checked.
     */
    private boolean checked;

    /**
     * Map pairing miscellaneous attribute names with their values. This is used
     * by the console tree table to get values for the corresponding row's
     * various cells, with the column identifiers being treated as keys into
     * this map.
     */
    private ImmutableMap<String, Serializable> attributes;

    /**
     * Children of this tabular entity; may be <code>null</code>.
     */
    private ImmutableList<TabularEntity> children;

    // Public Static Methods

    /**
     * Create a tabular entity to have the specified parameters.
     * 
     * @param identifier
     *            Identifier of the tabular entity to be created.
     * @param historyIndex
     *            Index of this entity in the reversed history list of the
     *            event, if it has been persisted; may be <code>null</code>.
     * @param persistedTimestamp
     *            Timestamp for when the tabular entity to be created was
     *            persisted; may be <code>null</code>.
     * @param unsaved
     *            Flag indicating whether or not this entity has unsaved
     *            changes.
     * @param timeRange
     *            Time range to be used.
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the upper end of the
     *            <code>timeRange</code> is currently "until further notice".
     * @param timeRangeIntervalLocked
     *            Flag indicating whether or not the interval between the
     *            <code>timeRange</code> end points should by default be fixed.
     * @param lowerTimeBoundaries
     *            Boundaries of allowable values for the lower end of the
     *            <code>timeRange</code>.
     * @param upperTimeBoundaries
     *            Boundaries of allowable values for the upper end of the
     *            <code>timeRange</code>.
     * @param timeResolution
     *            Time resolution of the <code>timeRange</code> to be used.
     * @param allowUntilFurtherNotice
     *            Flag indicating whether or not the upper end of the
     *            <code>timeRange</code> is allowed to be
     *            "until further notice".
     * @param selected
     *            Flag indicating whether or not the entity is selected.
     * @param checked
     *            Flag indicating whether or not the entity is checked.
     * @param attributes
     *            Map pairing miscellaneous attribute names with their values.
     *            This is used by the console tree table to get values for the
     *            corresponding row's various cells, with the column identifiers
     *            being treated as keys into this map.
     * @param color
     *            Color to be used.
     * @param children
     *            Children of this tabular entity; may be <code>null</code>.
     * @return Tabular entity with the specified parameters.
     */
    public static TabularEntity build(String identifier, Integer historyIndex,
            Date persistedTimestamp, boolean unsaved, Range<Long> timeRange,
            boolean endTimeUntilFurtherNotice, boolean timeRangeIntervalLocked,
            Range<Long> lowerTimeBoundaries, Range<Long> upperTimeBoundaries,
            TimeResolution timeResolution, boolean allowUntilFurtherNotice,
            boolean selected, boolean checked,
            Map<String, Serializable> attributes, Color color,
            List<TabularEntity> children) {
        TabularEntity tabularEntity = new TabularEntity(identifier,
                historyIndex, persistedTimestamp);
        tabularEntity.setUnsaved(unsaved);
        tabularEntity.setTimeRange(timeRange);
        tabularEntity.setTimeRangeIntervalLocked(timeRangeIntervalLocked);
        tabularEntity.setEndTimeUntilFurtherNotice(endTimeUntilFurtherNotice);
        tabularEntity.setLowerTimeBoundaries(lowerTimeBoundaries);
        tabularEntity.setUpperTimeBoundaries(upperTimeBoundaries);
        tabularEntity.setTimeResolution(timeResolution);
        tabularEntity.setAllowUntilFurtherNotice(allowUntilFurtherNotice);
        tabularEntity.setColor(color);
        tabularEntity.setSelected(selected);
        tabularEntity.setChecked(checked);
        ImmutableMap<String, Serializable> immutableAttributes = (attributes instanceof ImmutableMap ? (ImmutableMap<String, Serializable>) attributes
                : ImmutableMap
                        .copyOf(Utils.pruneNullEntriesFromMap(attributes)));
        tabularEntity.setAttributes(immutableAttributes);
        ImmutableList<TabularEntity> immutableChildren = (children instanceof ImmutableList ? (ImmutableList<TabularEntity>) children
                : (children != null ? ImmutableList.copyOf(children)
                        : ImmutableList.<TabularEntity> of()));
        tabularEntity.setChildren(immutableChildren);
        return tabularEntity;
    }

    /**
     * Create a tabular entity to have the specified parameters, reusing the
     * specified tabular entity if appropriate.
     * 
     * @param tabularEntity
     *            Tabular entity which, if not <code>null</code>, is to be
     *            returned if it is equivalent to what a newly generated tabular
     *            entity with these parameters would be. If <code>null</code>, a
     *            new tabular entity will always be generated.
     * @param identifier
     *            Identifier of the tabular entity to be created, used if
     *            <code>tabularEntity</code> is specified as <code>null</code>
     *            when creating the new tabular entity. If
     *            <code>tabularEntity</code> is not <code>null</code>, this
     *            identifier must be the equivalent of that returned by the
     *            latter's {@link #getIdentifier()}.
     * @param historyIndex
     *            Index of this entity in the reversed history list of the
     *            event, if it has been persisted; used if
     *            <code>tabularEntity</code> is specified as <code>null</code>
     *            when creating the new tabular entity. If
     *            <code>tabularEntity</code> is not <code>null</code>, this
     *            index must be the equivalent of that returned by the latter's
     *            {@link #getHistoryIndex()}.
     * @param persistedTimestamp
     *            Timestamp for when the tabular entity to be created was
     *            persisted, if any; used if <code>tabularEntity</code> is
     *            specified as <code>null</code> when creating the new tabular
     *            entity. If <code>tabularEntity</code> is not <code>null</code>
     *            , this timestamp must be the equivalent of that returned by
     *            the latter's {@link #getPersistedTimestamp()}.
     * @param unsaved
     *            Flag indicating whether or not this entity has unsaved
     *            changes.
     * @param timeRange
     *            Time range to be used.
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the upper end of the
     *            <code>timeRange</code> is currently "until further notice".
     * @param timeRangeIntervalLocked
     *            Flag indicating whether or not the interval between the
     *            <code>timeRange</code> end points should be fixed.
     * @param lowerTimeBoundaries
     *            Boundaries of allowable values for the lower end of the
     *            <code>timeRange</code>.
     * @param upperTimeBoundaries
     *            Boundaries of allowable values for the upper end of the
     *            <code>timeRange</code>.
     * @param timeResolution
     *            Time resolution of the <code>timeRange</code> to be used.
     * @param allowUntilFurtherNotice
     *            Flag indicating whether or not the upper end of the
     *            <code>timeRange</code> is allowed to be
     *            "until further notice".
     * @param selected
     *            Flag indicating whether or not the entity is selected.
     * @param checked
     *            Flag indicating whether or not the entity is checked.
     * @param attributes
     *            Map pairing miscellaneous attribute names with their values.
     *            This is used by the console tree table to get values for the
     *            corresponding row's various cells, with the column identifiers
     *            being treated as keys into this map.
     * @param color
     *            Color to be used.
     * @param children
     *            Children of this tabular entity; may be <code>null</code>.
     * @return Tabular entity with the specified parameters, either the one
     *         provided as an input parameter (if the latter was not
     *         <code>null</code> and was equivalent to the tabular entity that
     *         would have been generated with these parameters), or a newly
     *         created one.
     */
    public static TabularEntity build(TabularEntity tabularEntity,
            String identifier, Integer historyIndex, Date persistedTimestamp,
            boolean unsaved, Range<Long> timeRange,
            boolean endTimeUntilFurtherNotice, boolean timeRangeIntervalLocked,
            Range<Long> lowerTimeBoundaries, Range<Long> upperTimeBoundaries,
            TimeResolution timeResolution, boolean allowUntilFurtherNotice,
            boolean selected, boolean checked,
            Map<String, Serializable> attributes, Color color,
            List<TabularEntity> children) {

        /*
         * Remember the original, and create a new tabular entity if an original
         * was not provided.
         */
        TabularEntity original = tabularEntity;
        if (tabularEntity == null) {
            tabularEntity = new TabularEntity(identifier, historyIndex,
                    persistedTimestamp);
        }

        /*
         * For each desired property value of the tabular entity, check to see
         * if the original has this value already, creating a new tabular entity
         * if not. If one has already been created in the course of this method,
         * it will simply be reused. Then set the property to the desired value.
         */
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.isUnsaved()), unsaved);
        tabularEntity.setUnsaved(unsaved);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getTimeRange()), timeRange);
        tabularEntity.setTimeRange(timeRange);
        tabularEntity = createIfNeeded(
                original,
                tabularEntity,
                (original == null ? null : original
                        .isEndTimeUntilFurtherNotice()),
                endTimeUntilFurtherNotice);
        tabularEntity.setEndTimeUntilFurtherNotice(endTimeUntilFurtherNotice);
        tabularEntity = createIfNeeded(
                original,
                tabularEntity,
                (original == null ? null : original.isTimeRangeIntervalLocked()),
                timeRangeIntervalLocked);
        tabularEntity.setTimeRangeIntervalLocked(timeRangeIntervalLocked);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getLowerTimeBoundaries()),
                lowerTimeBoundaries);
        tabularEntity.setLowerTimeBoundaries(lowerTimeBoundaries);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getUpperTimeBoundaries()),
                upperTimeBoundaries);
        tabularEntity.setUpperTimeBoundaries(upperTimeBoundaries);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getTimeResolution()),
                timeResolution);
        tabularEntity.setTimeResolution(timeResolution);
        tabularEntity = createIfNeeded(
                original,
                tabularEntity,
                (original == null ? null : original.isAllowUntilFurtherNotice()),
                allowUntilFurtherNotice);
        tabularEntity.setAllowUntilFurtherNotice(allowUntilFurtherNotice);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getColor()), color);
        tabularEntity.setColor(color);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.isSelected()), selected);
        tabularEntity.setSelected(selected);
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.isChecked()), checked);
        tabularEntity.setChecked(checked);
        ImmutableMap<String, Serializable> immutableAttributes = (attributes instanceof ImmutableMap ? (ImmutableMap<String, Serializable>) attributes
                : ImmutableMap
                        .copyOf(Utils.pruneNullEntriesFromMap(attributes)));
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getAttributes()),
                immutableAttributes);
        tabularEntity.setAttributes(immutableAttributes);
        ImmutableList<TabularEntity> immutableChildren = (children instanceof ImmutableList ? (ImmutableList<TabularEntity>) children
                : (children != null ? ImmutableList.copyOf(children)
                        : ImmutableList.<TabularEntity> of()));
        tabularEntity = createIfNeeded(original, tabularEntity,
                (original == null ? null : original.getChildren()),
                immutableChildren);
        tabularEntity.setChildren(immutableChildren);
        return tabularEntity;
    }

    /**
     * Build a new version of the specified tabular entity that is the same as
     * the original, but with the specified children instead.
     * 
     * @param tabularEntity
     *            Tabular entity which is to be returned if it is equivalent to
     *            what a newly generated tabular entity with the new children
     *            would be. Must not be <code>null</code>.
     * @param children
     *            Children of this tabular entity; may be <code>null</code>.
     * @return Tabular entity with the specified parameters, either the one
     *         provided as an input parameter (if the latter was equivalent to
     *         the tabular entity that would have been generated with these
     *         parameters), or a newly created one.
     */
    public static TabularEntity build(TabularEntity tabularEntity,
            List<TabularEntity> children) {

        /*
         * Remember the original, and ensure it is not null.
         */
        TabularEntity original = tabularEntity;
        if (tabularEntity == null) {
            throw new IllegalArgumentException("tabularEntity must be non-null");
        }

        /*
         * Check to see if the original has these children already, creating a
         * new tabular entity if not. Then set the children to those specified.
         */
        ImmutableList<TabularEntity> immutableChildren = (children instanceof ImmutableList ? (ImmutableList<TabularEntity>) children
                : (children != null ? ImmutableList.copyOf(children)
                        : ImmutableList.<TabularEntity> of()));
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.getChildren(), immutableChildren);
        tabularEntity.setChildren(immutableChildren);
        return tabularEntity;
    }

    /**
     * Build a new version of the specified tabular entity that is the same as
     * the original, but with the specified selection state, checked state, end
     * time until further notice state, time range, and children.
     * 
     * @param tabularEntity
     *            Tabular entity which is to be returned if it is equivalent to
     *            what a newly generated tabular entity with the new children
     *            would be. Must not be <code>null</code>.
     * @param timeRange
     *            Time range to be used.
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the upper end of the
     *            <code>timeRange</code> is currently "until further notice".
     * @param selected
     *            Flag indicating whether or not the entity is selected.
     * @param checked
     *            Flag indicating whether or not the entity is checked.
     * @param addedAttributes
     *            Map pairing miscellaneous attribute names with their values
     *            that are to be added to the attributes provided by the origina
     *            entity. May be <code>null</code>.
     * @param children
     *            Children of this tabular entity; may be <code>null</code>.
     * @return Tabular entity with the specified parameters, either the one
     *         provided as an input parameter (if the latter was equivalent to
     *         the tabular entity that would have been generated with these
     *         parameters), or a newly created one.
     */
    public static TabularEntity build(TabularEntity tabularEntity,
            Range<Long> timeRange, boolean endTimeUntilFurtherNotice,
            boolean selected, boolean checked,
            Map<String, Serializable> addedAttributes,
            List<TabularEntity> children) {

        /*
         * Remember the original, and ensure it is not null.
         */
        TabularEntity original = tabularEntity;
        if (tabularEntity == null) {
            throw new IllegalArgumentException("tabularEntity must be non-null");
        }

        /*
         * For each desired property value of the tabular entity, check to see
         * if the original has this value already, creating a new tabular entity
         * if not. If one has already been created in the course of this method,
         * it will simply be reused. Then set the property to the desired value.
         */
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.getTimeRange(), timeRange);
        tabularEntity.setTimeRange(timeRange);
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.isEndTimeUntilFurtherNotice(),
                endTimeUntilFurtherNotice);
        tabularEntity.setEndTimeUntilFurtherNotice(endTimeUntilFurtherNotice);
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.isSelected(), selected);
        tabularEntity.setSelected(selected);
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.isChecked(), checked);
        tabularEntity.setChecked(checked);
        if ((addedAttributes != null) && (addedAttributes.isEmpty() == false)) {
            Map<String, Serializable> newAttributes = new HashMap<>(original
                    .getAttributes().size() + addedAttributes.size(), 1.0f);
            newAttributes.putAll(original.getAttributes());
            newAttributes.putAll(addedAttributes);
            ImmutableMap<String, Serializable> immutableAttributes = ImmutableMap
                    .copyOf(Utils.pruneNullEntriesFromMap(newAttributes));
            tabularEntity = createIfNeeded(original, tabularEntity,
                    original.getAttributes(), immutableAttributes);
            tabularEntity.setAttributes(immutableAttributes);
        }
        ImmutableList<TabularEntity> immutableChildren = (children instanceof ImmutableList ? (ImmutableList<TabularEntity>) children
                : (children != null ? ImmutableList.copyOf(children)
                        : ImmutableList.<TabularEntity> of()));
        tabularEntity = createIfNeeded(original, tabularEntity,
                original.getChildren(), immutableChildren);
        tabularEntity.setChildren(immutableChildren);
        return tabularEntity;
    }

    // Private Static Methods

    /**
     * Create a tabular entity if needed based upon the given parameters. There
     * is such a need if:
     * <ul>
     * <li>the specified original and under-construction tabular entity
     * parameters are the same object; and</li>
     * <li>the specified first and second objects are not equivalent.</li>
     * </ul>
     * 
     * @param original
     *            Original tabular entity.
     * @param underConstruction
     *            Tabular entity currently under construction; this will be
     *            returned if either it does not reference the same object as
     *            <code>original</code>, or if it does, but <code>first</code>
     *            and <code>second</code> are equivalent.
     * @param first
     *            First object to be compared.
     * @param second
     *            Second object to be compared.
     * @return Tabular entity that was created, if needed, otherwise simply a
     *         reference to the provided under-construction tabular entity.
     */
    private static TabularEntity createIfNeeded(TabularEntity original,
            TabularEntity underConstruction, Object first, Object second) {
        if ((original == underConstruction)
                && (Utils.equal(first, second) == false)) {
            underConstruction = new TabularEntity(original);
        }
        return underConstruction;
    }

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier of this instance.
     * @param persistedIndex
     *            Index of this entity in the reversed history list of the
     *            event, if it has been persisted; may be <code>null</code>.
     * @param persistedTimestamp
     *            Timestamp indicating when this instance was persisted; may be
     *            <code>null</code>.
     */
    TabularEntity(String identifier, Integer persistedIndex,
            Date persistedTimestamp) {
        this.identifier = identifier;
        this.historyIndex = persistedIndex;
        this.persistedTimestamp = persistedTimestamp;
    }

    // Private Constructors

    /**
     * Construct a copy instance.
     * 
     * @param other
     *            Spatial entity to be copied.
     */
    private TabularEntity(TabularEntity other) {
        identifier = other.identifier;
        historyIndex = other.historyIndex;
        persistedTimestamp = other.persistedTimestamp;
        unsaved = other.unsaved;
        timeRange = other.timeRange;
        endTimeUntilFurtherNotice = other.endTimeUntilFurtherNotice;
        timeRangeIntervalLocked = other.timeRangeIntervalLocked;
        lowerTimeBoundaries = other.lowerTimeBoundaries;
        upperTimeBoundaries = other.upperTimeBoundaries;
        timeResolution = other.timeResolution;
        allowUntilFurtherNotice = other.allowUntilFurtherNotice;
        color = other.color;
        selected = other.selected;
        checked = other.checked;
        attributes = other.attributes;
        children = other.children;
    }

    // Public Methods

    @Override
    public boolean equals(Object other) {
        if (other instanceof TabularEntity == false) {
            return false;
        }
        TabularEntity otherEntity = (TabularEntity) other;
        return (Utils.equal(identifier, otherEntity.identifier)
                && Utils.equal(historyIndex, otherEntity.historyIndex)
                && Utils.equal(persistedTimestamp,
                        otherEntity.persistedTimestamp)
                && Utils.equal(unsaved, otherEntity.unsaved)
                && Utils.equal(timeRange, otherEntity.timeRange)
                && Utils.equal(endTimeUntilFurtherNotice,
                        otherEntity.endTimeUntilFurtherNotice)
                && Utils.equal(timeRangeIntervalLocked,
                        otherEntity.timeRangeIntervalLocked)
                && Utils.equal(lowerTimeBoundaries,
                        otherEntity.lowerTimeBoundaries)
                && Utils.equal(upperTimeBoundaries,
                        otherEntity.upperTimeBoundaries)
                && (timeResolution == otherEntity.timeResolution)
                && (allowUntilFurtherNotice == otherEntity.allowUntilFurtherNotice)
                && Utils.equal(color, otherEntity.color)
                && (selected == otherEntity.selected)
                && (checked == otherEntity.checked)
                && Utils.equal(attributes, otherEntity.attributes) && Utils
                    .equal(children, otherEntity.children));
    }

    @Override
    public int hashCode() {
        return (int) ((Utils.getHashCode(identifier)
                + Utils.getHashCode(historyIndex)
                + Utils.getHashCode(persistedTimestamp)
                + Utils.getHashCode(unsaved) + Utils.getHashCode(timeRange)
                + Utils.getHashCode(endTimeUntilFurtherNotice)
                + Utils.getHashCode(timeRangeIntervalLocked)
                + Utils.getHashCode(lowerTimeBoundaries)
                + Utils.getHashCode(upperTimeBoundaries)
                + Utils.getHashCode(timeResolution)
                + Boolean.valueOf(allowUntilFurtherNotice).hashCode()
                + Utils.getHashCode(color)
                + Boolean.valueOf(selected).hashCode()
                + Boolean.valueOf(checked).hashCode()
                + Utils.getHashCode(attributes) + Utils.getHashCode(children)) % Integer.MAX_VALUE);
    }

    @Override
    public String toString() {
        return getIdentifier()
                + (getHistoryIndex() == null ? "" : " (" + getHistoryIndex()
                        + " (" + getPersistedTimestamp() + "))");
    }

    /**
     * Get the identifier.
     * 
     * @return Identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the index of this entity in the reversed history list of the event,
     * if it has been persisted.; may be <code>null</code>.
     * 
     * @return History index.
     */
    public Integer getHistoryIndex() {
        return historyIndex;
    }

    /**
     * Get the persisted timestamp; may be <code>null</code>.
     * 
     * @return Persisted timestamp.
     */
    public Date getPersistedTimestamp() {
        return persistedTimestamp;
    }

    /**
     * Determine whether or not the entity has unsaved changes.
     * 
     * @return <code>true</code> if the entity has unsaved changes,
     *         <code>false</code> otherwise.
     */
    public boolean isUnsaved() {
        return unsaved;
    }

    /**
     * Get the time range.
     * 
     * @return Time range.
     */
    public Range<Long> getTimeRange() {
        return timeRange;
    }

    /**
     * Determine whether or not the upper bound of the range provided by
     * {@link #getTimeRange()} is currently "until further notice".
     * 
     * @return <code>true</code> if the end time is currently
     *         "until further notice", <code>false</code> otherwise.
     */
    public boolean isEndTimeUntilFurtherNotice() {
        return endTimeUntilFurtherNotice;
    }

    /**
     * Determine whether or not the interval between the range provided by
     * {@link #getTimeRange()} end points should be fixed.
     * 
     * @return <code>true</code> if the interval should be fixed,
     *         <code>false</code> otherwise.
     */
    public boolean isTimeRangeIntervalLocked() {
        return timeRangeIntervalLocked;
    }

    /**
     * Get the boundaries of the allowable values for the lower end of the time
     * range.
     * 
     * @return Boundaries of the allowable values.
     */
    public Range<Long> getLowerTimeBoundaries() {
        return lowerTimeBoundaries;
    }

    /**
     * Get the boundaries of the allowable values for the upper end of the time
     * range.
     * 
     * @return Boundaries of the allowable values.
     */
    public Range<Long> getUpperTimeBoundaries() {
        return upperTimeBoundaries;
    }

    /**
     * Get the time resolution for the time range.
     * 
     * @return Time resolution for the time range.
     */
    public TimeResolution getTimeResolution() {
        return timeResolution;
    }

    /**
     * Determine whether or not "until further notice" is allowable for this
     * event's upper value of its time range.
     * 
     * @return Flag indicating whether or not "until further notice" is
     *         allowable.
     */
    public boolean isAllowUntilFurtherNotice() {
        return allowUntilFurtherNotice;
    }

    /**
     * Get the color.
     * 
     * @return Color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Determine whether or not the entity is selected.
     * 
     * @return Flag indicating whether or not the entity is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Determine whether or not the entity is checked.
     * 
     * @return Flag indicating whether or not the entity is checked.
     */
    public boolean isChecked() {
        return checked;
    }

    /**
     * Get the map pairing miscellaneous attribute names with their values. This
     * is used by the console tree table to get values for the corresponding
     * row's various cells, with the column identifiers being treated as keys
     * into this map.
     * 
     * @return Map pairing miscellaneous attriubte names with their values.
     */
    public ImmutableMap<String, Serializable> getAttributes() {
        return attributes;
    }

    /**
     * Get the children of this entity; may be <code>null</code>.
     * 
     * @return Children of this entity.
     */
    public ImmutableList<TabularEntity> getChildren() {
        return children;
    }

    // Private Methods

    /**
     * Set the flag that indicates whether or not the entity has unsaved
     * changes.
     * 
     * @param unsaved
     *            Flag indicating whether or not the entity has unsaved changes.
     */
    public void setUnsaved(boolean unsaved) {
        this.unsaved = unsaved;
    }

    /**
     * Set the time range.
     * 
     * @param timeRange
     *            New value.
     */
    private void setTimeRange(Range<Long> timeRange) {
        this.timeRange = timeRange;
    }

    /**
     * Set the flag indicating whether or not the upper bound of the range
     * provided by {@link #getTimeRange()} is currently "until further notice".
     * 
     * @param endTimeUntilFurtherNotice
     *            Flag indicating whether or not the end time is currently
     *            "until further notice".
     */
    public void setEndTimeUntilFurtherNotice(boolean endTimeUntilFurtherNotice) {
        this.endTimeUntilFurtherNotice = endTimeUntilFurtherNotice;
    }

    /**
     * Set the flag indicating whether or not the interval between the range
     * provided by {@link #getTimeRange()} end points should be fixed.
     * 
     * @param timeRangeIntervalLocked
     *            Flag indicating whether or not the interval should be fixed.
     */
    public void setTimeRangeIntervalLocked(boolean timeRangeIntervalLocked) {
        this.timeRangeIntervalLocked = timeRangeIntervalLocked;
    }

    /**
     * Set the boundaries of allowable values for the lower end of the time
     * range.
     * 
     * @param lowerTimeBoundaries
     *            New value.
     */
    private void setLowerTimeBoundaries(Range<Long> lowerTimeBoundaries) {
        this.lowerTimeBoundaries = lowerTimeBoundaries;
    }

    /**
     * Set the boundaries of allowable values for the upper end of the time
     * range.
     * 
     * @param upperTimeBoundaries
     *            New value.
     */
    private void setUpperTimeBoundaries(Range<Long> upperTimeBoundaries) {
        this.upperTimeBoundaries = upperTimeBoundaries;
    }

    /**
     * Set the time resolution of the time range.
     * 
     * @param timeResolution
     *            New value.
     */
    private void setTimeResolution(TimeResolution timeResolution) {
        this.timeResolution = timeResolution;
    }

    /**
     * Set the flag indicating whether or not the upper end of the time range
     * may have the value of "until further notice".
     * 
     * @param allowUntilFurtherNotice
     *            New value.
     */
    private void setAllowUntilFurtherNotice(boolean allowUntilFurtherNotice) {
        this.allowUntilFurtherNotice = allowUntilFurtherNotice;
    }

    /**
     * Set the color.
     * 
     * @param color
     *            New value.
     */
    private void setColor(Color color) {
        this.color = color;
    }

    /**
     * Set the flag indicating whether or not the entity is selected.
     * 
     * @param selected
     *            New value.
     */
    private void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Set the flag indicating whether or not the entity is checked.
     * 
     * @param checked
     *            New value.
     */
    private void setChecked(boolean checked) {
        this.checked = checked;
    }

    /**
     * Set the map pairing miscellaneous attribute names with their values. This
     * is used by the console tree table to get values for the corresponding
     * row's various cells, with the column identifiers being treated as keys
     * into this map.
     * 
     * @param attributes
     *            New value.
     */
    private void setAttributes(ImmutableMap<String, Serializable> attributes) {
        this.attributes = attributes;
    }

    /**
     * Set the list of children of this entity.
     * 
     * @param children
     *            New value; may be <code>null</code>.
     */
    private void setChildren(ImmutableList<TabularEntity> children) {
        this.children = children;
    }
}