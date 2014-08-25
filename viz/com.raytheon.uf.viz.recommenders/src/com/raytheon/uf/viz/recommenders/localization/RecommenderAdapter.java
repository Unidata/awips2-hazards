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
package com.raytheon.uf.viz.recommenders.localization;

import com.raytheon.uf.viz.python.localization.AbstractNewActionAdapter;
import com.raytheon.uf.viz.python.localization.INewBasedVelocityAction;

/**
 * Adds ability to grab recommender template directly in Localization
 * Perspective.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 18, 2013            mnash       Initial creation
 * Nov 20, 2013            bkowal      Now extends the Copy Python Classes Adapter
 *                                     so that it will be displayed for Recommenders
 *                                     when a Python file is selected.
 * Nov 25, 2013            bkowal      Refactor
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class RecommenderAdapter extends AbstractNewActionAdapter {
    protected INewBasedVelocityAction getLocalizationAction() {
        return new NewRecommenderAction();
    }
}