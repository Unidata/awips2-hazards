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
package com.raytheon.uf.viz.hazards.sessionmanager.product;

import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Contains the preview and issue formats of a product.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 9, 2013            jsanchez     Initial creation
 * Mar 18, 2014 2917      jsanchez     Separated out issue and preview formats.
 * Apr 23, 2014 1480      jsanchez     Made class into a simple getter/setter class.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class ProductFormats {
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(ProductFormats.class);

    private List<String> issueFormats;

    private List<String> previewFormats;

    public List<String> getIssueFormats() {
        return issueFormats;
    }

    public void setIssueFormats(List<String> issueFormats) {
        this.issueFormats = issueFormats;
    }

    public List<String> getPreviewFormats() {
        return previewFormats;
    }

    public void setPreviewFormats(List<String> previewFormats) {
        this.previewFormats = previewFormats;
    }
}
