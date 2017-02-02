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

import gov.noaa.gsd.common.utilities.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Column;

/**
 * Description: Encapsulation of the columns for the console tree, including
 * their ordering and visibility.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 11, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class ConsoleColumns {

    // Package-Private Static Classes

    /**
     * Column definition, one or more instances of which are included in each
     * instance of the enclosing class.
     */
    static class ColumnDefinition {

        // Private Variables

        /**
         * Identifier of the tabular entity attribute from which to pull values
         * to be used as data for the column's cells.
         */
        private final String identifier;

        /**
         * Type of the data held in the column's cells.
         * 
         * TODO: Enumerated type would be better; see {@link Column#type}.
         */
        private final String type;

        /**
         * Width of the column in pixels, if specified.
         */
        private final Integer width;

        /**
         * Identifier of the tabular entity attribute from which to pull values
         * to be used as hint text for the column's cells, if any.
         */
        private final String hintTextIdentifier;

        /**
         * String to be displayed in the column's cells if they are empty, if
         * any.
         */
        private final String displayWhenEmpty;

        // Package-Private Constructors

        /**
         * Construct a standard instance based upon the specified column.
         * 
         * @param column
         *            Column upon which to base the new object.
         */
        ColumnDefinition(Column column) {
            this.identifier = column.getFieldName();
            this.type = column.getType();
            this.width = column.getWidth();
            this.hintTextIdentifier = column.getHintTextFieldName();
            this.displayWhenEmpty = column.getDisplayEmptyAs();
        }

        /**
         * Construct a standard instance as a copy of the specified column
         * definition, but with the new specified width.
         * 
         * @param other
         *            Other column definition upon which to base the new one.
         * @param width
         *            Width of the new column in pixels, if specified.
         */
        ColumnDefinition(ColumnDefinition other, Integer width) {
            this.identifier = other.identifier;
            this.type = other.type;
            this.width = width;
            this.hintTextIdentifier = other.hintTextIdentifier;
            this.displayWhenEmpty = other.displayWhenEmpty;
        }

        // Package-Private Methods

        /**
         * Get the identifier of the tabular entity attribute from which to pull
         * values to be used as data for the column's cells.
         * 
         * @return Identifier.
         */
        String getIdentifier() {
            return identifier;
        }

        /**
         * Get the type of the data held in the column's cells.
         * 
         * @return Type.
         */
        String getType() {
            return type;
        }

        /**
         * Get the width of the column in pixels, if specified.
         * 
         * @return Width, or <code>null</code> if there is none recorded.
         */
        Integer getWidth() {
            return width;
        }

        /**
         * Get the identifier of the tabular entity attribute from which to pull
         * values to be used as hint text for the column's cells, if any.
         * 
         * @return Identifier for hint text.
         */
        String getHintTextIdentifier() {
            return hintTextIdentifier;
        }

        /**
         * Get the string to be displayed in the column's cells if they are
         * empty, if any.
         * 
         * @return String to be displayed in empty cells, if any. /
         */
        String getDisplayWhenEmpty() {
            return displayWhenEmpty;
        }

        /**
         * Get a column that is equivalent to the specified column, but with the
         * values of this object applied if they are different from what is
         * supplied by said column.
         * 
         * @param column
         *            Original column; this same object may be returned if
         *            nothing has changed.
         * @return Column equivalent to the specified column, modified as
         *         appropriate by applying this object's values.
         */
        Column getModifiedColumn(Column column) {
            if (Utils.equal(column.getFieldName(), identifier)
                    && Utils.equal(column.getType(), type)
                    && Utils.equal(column.getWidth(), width)
                    && Utils.equal(column.getHintTextFieldName(),
                            hintTextIdentifier)
                    && Utils.equal(column.getDisplayEmptyAs(), displayWhenEmpty)) {
                return column;
            }
            return new Column(width, type, identifier, hintTextIdentifier,
                    column.getSortDir(), column.getSortPriority(),
                    displayWhenEmpty);
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof ColumnDefinition == false) {
                return false;
            }
            ColumnDefinition otherColumn = (ColumnDefinition) other;
            return (Utils.equal(identifier, otherColumn.identifier)
                    && Utils.equal(type, otherColumn.type)
                    && Utils.equal(width, otherColumn.width)
                    && Utils.equal(hintTextIdentifier,
                            otherColumn.hintTextIdentifier) && Utils.equal(
                    displayWhenEmpty, otherColumn.displayWhenEmpty));
        }

        @Override
        public int hashCode() {
            return (int) ((Utils.getHashCode(identifier)
                    + Utils.getHashCode(type) + Utils.getHashCode(width)
                    + Utils.getHashCode(hintTextIdentifier) + Utils
                        .getHashCode(displayWhenEmpty)) % Integer.MAX_VALUE);
        }
    }

    // Private Variables

    /**
     * Map of column names to the column definitions.
     */
    private final ImmutableMap<String, ColumnDefinition> columnDefinitionsForNames;

    /**
     * List of visible column names, in the order in which they should occur.
     */
    private final ImmutableList<String> visibleColumnNames;

    // Package-Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param columnDefinitionsForNames
     *            Map of column names to the column definitions.
     * @param visibleColumnNames
     *            List of visible column names, in the order in which they
     *            should occur.
     */
    ConsoleColumns(
            ImmutableMap<String, ColumnDefinition> columnDefinitionsForNames,
            ImmutableList<String> visibleColumnNames) {
        this.columnDefinitionsForNames = columnDefinitionsForNames;
        this.visibleColumnNames = visibleColumnNames;
    }

    /**
     * Construct a standard instance.
     * 
     * @param columnsForNames
     *            Map of column names to the columns. This will not be included
     *            in the actual object; it will be translated into a map with
     *            values of type {@link ColumnDefinition}.
     * @param visibleColumnNames
     *            List of visible column names, in the order in which they
     *            should occur.
     */
    ConsoleColumns(Map<String, Column> columnsForNames,
            List<String> visibleColumnNames) {
        Map<String, ColumnDefinition> columnDefinitionsForNames = new HashMap<>(
                columnsForNames.size(), 1.0f);
        for (Map.Entry<String, Column> entry : columnsForNames.entrySet()) {
            columnDefinitionsForNames.put(entry.getKey(), new ColumnDefinition(
                    entry.getValue()));
        }
        this.columnDefinitionsForNames = ImmutableMap
                .copyOf(columnDefinitionsForNames);
        this.visibleColumnNames = (visibleColumnNames instanceof ImmutableList ? (ImmutableList<String>) visibleColumnNames
                : ImmutableList.copyOf(visibleColumnNames));
    }

    // Package-Private Methods

    /**
     * Get the map of column names to the column definitions.
     * 
     * @return Map of column names to the column definitions.
     */
    ImmutableMap<String, ColumnDefinition> getColumnDefinitionsForNames() {
        return columnDefinitionsForNames;
    }

    /**
     * Get the list of visible column names, in the order in which they should
     * occur.
     * 
     * @return List of visible column names, in the order in which they should
     *         occur.
     */
    ImmutableList<String> getVisibleColumnNames() {
        return visibleColumnNames;
    }

    /**
     * Get a new map that is equivalent to the specified map of names to
     * columns, but with the values of this object's column definitions
     * overwriting any in the specified map's columns.
     * 
     * @param columnsForNames
     *            Map of column names to the columns. The individual columns may
     *            be included in the returned map, if those columns have no
     *            modifications.
     * @return New map containing the original entries modified as appropriate
     *         by applying this object's column definitions.
     */
    Map<String, Column> getModifiedColumnsForNames(
            Map<String, Column> columnsForNames) {
        Map<String, Column> newColumnsForNames = new HashMap<>(
                columnsForNames.size(), 1.0f);
        for (Map.Entry<String, Column> entry : columnsForNames.entrySet()) {
            ColumnDefinition columnDefinition = columnDefinitionsForNames
                    .get(entry.getKey());
            newColumnsForNames.put(entry.getKey(), entry.getValue());
            newColumnsForNames.put(
                    entry.getKey(),
                    (columnDefinition == null ? entry.getValue()
                            : columnDefinition.getModifiedColumn(entry
                                    .getValue())));
        }
        return newColumnsForNames;
    }
}
