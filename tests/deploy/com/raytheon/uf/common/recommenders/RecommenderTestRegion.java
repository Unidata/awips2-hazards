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
package com.raytheon.uf.common.recommenders;

/**
 * Tests a recommender with a region override. Base recommender is read and
 * merged with a region recommender.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 19, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class RecommenderTestRegion extends AbstractRecommenderOverrideTest {
    private static final String RECOMMENDER_NAME = "RegionOverrideRecommender";

    private static final String EXPECTED_KEY_VALUE = "TEST REGION";

    /**
     * @param recommenderName
     * @param expectedKeyValue
     */
    public RecommenderTestRegion() {
        super(RECOMMENDER_NAME, EXPECTED_KEY_VALUE);
    }
}