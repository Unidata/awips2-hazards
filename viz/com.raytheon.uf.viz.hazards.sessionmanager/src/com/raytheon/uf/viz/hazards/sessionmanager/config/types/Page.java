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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Defines a page in the view configuration dialog
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jul 10, 2013  1257      bsteffen      Initial creation
 * May 04, 2018 50032      Chris.Golden  Added copy constructor.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class Page {

    private String pageName;

    private Integer numColumns;

    private List<Field> pageFields;

    public Page() {
    }

    public Page(Page other) {
        this.pageName = other.pageName;
        this.numColumns = other.numColumns;
        this.pageFields = new ArrayList<>(other.pageFields);
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public Integer getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(Integer numColumns) {
        this.numColumns = numColumns;
    }

    public List<Field> getPageFields() {
        return pageFields;
    }

    public void setPageFields(List<Field> pageFields) {
        this.pageFields = pageFields;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}