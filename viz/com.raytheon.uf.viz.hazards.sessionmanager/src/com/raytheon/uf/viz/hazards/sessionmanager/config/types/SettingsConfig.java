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

/**
 * Built from the objects defined in the viewConfig of the defaultConfig
 * localization file.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 10, 2013 1257       bsteffen    Initial creation
 * Feb 23, 2015 3618       Chris.Golden Added expandVertically and changed
 *                                      expandHorizontally to boolean.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SettingsConfig {

    private String fieldName;

    private String fieldType;

    private int leftMargin;

    private int rightMargin;

    private int topMargin;

    private int bottomMargin;

    private int spacing;

    private boolean expandHorizontally;

    private boolean expandVertically;

    private List<Page> pages;

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

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public boolean getExpandHorizontally() {
        return expandHorizontally;
    }

    public void setExpandHorizontally(boolean expandHorizontally) {
        this.expandHorizontally = expandHorizontally;
    }

    public boolean getExpandVertically() {
        return expandVertically;
    }

    public void setExpandVerticallyl(boolean expandVertically) {
        this.expandVertically = expandVertically;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
