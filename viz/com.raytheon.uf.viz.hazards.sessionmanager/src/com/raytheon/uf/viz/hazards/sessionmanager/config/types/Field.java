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
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.Lists;

/**
 * 
 * Defines the fields to modify in the view configuration dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2013 1257       bsteffen    Initial creation
 * Nov 15, 2013 2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Feb 23, 2015 3618       Chris.Golden Added expandVertically.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class Field {
    private Integer lines;

    private List<Choice> choices = Lists.newArrayList();

    private String columnName;

    private String fieldName;

    private String fieldType;

    private String selectedLabel;

    private String label;

    private Integer maxChars;

    private Integer visibleChars;

    private Boolean expandHorizontally;

    private Boolean expandVertically;

    private List<Field> fields = Lists.newArrayList();

    private Integer leftMargin;

    private Integer rightMargin;

    private Integer topMargin;

    private Integer bottomMargin;

    public Field() {

    }

    public Field(Field other) {
        this.lines = other.lines;
        this.choices = other.choices;
        this.columnName = other.columnName;
        this.fieldName = other.fieldName;
        this.fieldType = other.fieldType;
        this.selectedLabel = other.selectedLabel;
        this.label = other.label;
        this.fields = other.fields;
        this.leftMargin = other.leftMargin;
        this.rightMargin = other.rightMargin;
        this.topMargin = other.topMargin;
        this.bottomMargin = other.bottomMargin;
    }

    public Integer getLines() {
        return lines;
    }

    public void setLines(Integer lines) {
        this.lines = lines;
    }

    public boolean addChoice(Choice choice) {
        return choices.add(choice);
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSelectedLabel() {
        return selectedLabel;
    }

    public void setSelectedLabel(String selectedLabel) {
        this.selectedLabel = selectedLabel;
    }

    public Integer getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(Integer leftMargin) {
        this.leftMargin = leftMargin;
    }

    public Integer getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(Integer rightMargin) {
        this.rightMargin = rightMargin;
    }

    public Integer getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(Integer topMargin) {
        this.topMargin = topMargin;
    }

    public Integer getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(Integer bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Integer getMaxChars() {
        return maxChars;
    }

    public void setMaxChars(Integer maxChars) {
        this.maxChars = maxChars;
    }

    public Integer getVisibleChars() {
        return visibleChars;
    }

    public void setVisibleChars(Integer visibleChars) {
        this.visibleChars = visibleChars;
    }

    public Boolean getExpandHorizontally() {
        return expandHorizontally;
    }

    public void setExpandHorizontally(Boolean expandHorizontally) {
        this.expandHorizontally = expandHorizontally;
    }

    public Boolean getExpandVertically() {
        return expandVertically;
    }

    public void setExpandVertically(Boolean expandVertically) {
        this.expandVertically = expandVertically;
    }
}