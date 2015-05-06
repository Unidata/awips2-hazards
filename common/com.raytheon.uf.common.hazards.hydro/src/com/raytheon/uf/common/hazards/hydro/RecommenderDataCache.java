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
package com.raytheon.uf.common.hazards.hydro;

import java.util.Timer;
import java.util.TimerTask;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.util.TimeUtil;

/**
 * This class is intended to be an "in memory cache" for River Pro River Flood
 * Recommender data objects.
 * 
 * The goal is to have ALL of the data necessary to generate and manipulate
 * River Flood Recommender objects in a single repository that resides within
 * the CAVE application Address Space. Furthermore, this class can be set to
 * dispose of is repository data when a ProductGenerationComplete message is
 * processed (by the Handler of said message. This will allow the application
 * and Python scripts to re-use River Flood Recommender data items without
 * having to requery for data.
 * <p>
 * Caveat: Currently, there does not exist a way for a Non River Flood
 * Recommender specific Python script to determine whether it is executed as
 * part of a Recommender or if it is being exected independently. So, while it
 * may be necessary to requery individual objects for independent scripts; it is
 * not necessary to requery a complete set of data for each operation on River
 * Flood Recommender Data.
 * <p>
 * A later implementation of the RiverForecastManager should bridge this gap and
 * check to see if CURRENT data exists for requested items and return the cached
 * data instead of beginning a new query.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 08, 2015 6562       Chris.Cody  Initial creation: Restructure River Forecast Points/Recommender
 * </pre>
 * 
 * @author Chris.Cody
 */

public class RecommenderDataCache {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(RecommenderDataCache.class);

    /**
     * Configurable value for how long to retain cached data.
     */
    public static long holdTimeMils = TimeUtil.MILLIS_PER_MINUTE * 5;

    /**
     * Flag denoting whether to start a Timer (based on absoluteTime values and
     * system clock) to automatically purge cached data.
     */
    // TODO Make this option configurable
    private static boolean autoPurgeCache = true;

    /**
     * Flag denoting whether to purge Recommender Cache when called upon
     * Recommender generation completion. This class does NOT add itself ] as a
     * Message Handler. It must be called by a completion handler.
     */
    // TODO Make this option configurable
    private static boolean PurgeOnCompletion = true;

    /**
     * Current cache begin time Unix timestamp.
     */
    private long currentCacheTime = Long.MIN_VALUE;

    private Timer recommenderCacheExpirationTimer = new Timer(true);

    /**
     * Cached Recommender Data.
     */
    private RecommenderData recommenderData = null;

    /**
     * Singleton class instance.
     */
    private static RecommenderDataCache recommenderDataCacheInstance = null;

    /**
     * Private singleton constructor.
     * 
     */
    private RecommenderDataCache() {
    }

    public synchronized static RecommenderDataCache getInstance() {
        if (recommenderDataCacheInstance == null) {
            recommenderDataCacheInstance = new RecommenderDataCache();
        }
        return (recommenderDataCacheInstance);
    }

    /**
     * Check to see if cached data is still within its validity window of time.
     * 
     * @param cacheSystemTime
     * @return True if hold time has not elapsed; False otherwise or if cache is
     *         empty.
     */
    public boolean isCacheValid(long cacheSystemTime) {

        if ((this.recommenderData != null)
                && (this.currentCacheTime != Long.MIN_VALUE)
                && (cacheSystemTime != 0L)) {
            long low = this.currentCacheTime - holdTimeMils;
            long high = this.currentCacheTime + holdTimeMils;
            if ((cacheSystemTime >= low) || (cacheSystemTime <= high)) {
                return (true);
            }
        }
        return (false);
    }

    public RecommenderData getCachedData(long cacheSystemTime,
            boolean overrideCacheValid) {

        if ((overrideCacheValid == true)
                || (isCacheValid(cacheSystemTime) == true)) {
            return (this.recommenderData);
        } else {
            return (null);
        }
    }

    public RecommenderData getCachedData(long cacheSystemTime) {

        return getCachedData(cacheSystemTime, false);
    }

    public void putCachedData(long cacheSystemTime,
            RecommenderData recommenderData) {
        this.currentCacheTime = cacheSystemTime;
        this.recommenderData = recommenderData;

        if (autoPurgeCache == true) {
            startCachePurgeTimer();
        }
    }

    /**
     * Purge Cached Recommender Data on Product (Recommender) Completion
     * notification.
     * 
     * This method will dispose of the Recommender Cache. It is intended to be
     * called upon completion of the generation of recommendation or product
     * data. This method will check a flag to see if it has been configured to
     * purge upon completion or not.
     * 
     * This will dispose of any timer as well.
     * 
     */
    public void purgeCachedDataOnComplete() {
        statusHandler
                .info("Purging RecommenderDataCache On Generation Completion ");

        if (PurgeOnCompletion == true) {
            this.purgeCachedData();
        }
    }

    /**
     * Purge Cached Recommender Data.
     * 
     * Empty Recommender Data Cache. This will dispose of any timer as well.
     * 
     * </pre>
     */
    public void purgeCachedData() {
        statusHandler.info("Purging RecommenderDataCache");

        this.currentCacheTime = Long.MIN_VALUE;
        this.recommenderData = null;

        if (this.recommenderCacheExpirationTimer != null) {
            this.recommenderCacheExpirationTimer.cancel();
        }
    }

    /**
     * Set auto purge flag.
     * 
     * Setting this flag to True will enabled timed auto purge.
     * 
     * @param isEnabled
     *            boolean autopurge enabled flag
     */
    public void setAutoPurge(boolean isEnabled) {
        setAutoPurge(isEnabled, -1);
    }

    /**
     * Set auto purge flag.
     * 
     * Setting this flag to True will enabled timed auto purge.
     * 
     * @param isEnabled
     *            boolean auto-purge enabled flag
     * @param holdMinutes
     *            Number of minutes to retain cache. Must be >1
     */
    public void setAutoPurge(boolean isEnabled, int holdMinutes) {
        autoPurgeCache = isEnabled;

        if (holdMinutes > 0) {
            holdTimeMils = TimeUtil.MILLIS_PER_MINUTE * holdMinutes;
        }

        if ((isEnabled == false)
                && (this.recommenderCacheExpirationTimer != null)) {
            this.recommenderCacheExpirationTimer.cancel();
        }
    }

    private void startCachePurgeTimer() {

        if (this.recommenderCacheExpirationTimer != null) {
            this.recommenderCacheExpirationTimer.cancel();
        }

        this.recommenderCacheExpirationTimer = new Timer();
        this.recommenderCacheExpirationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (RecommenderDataCache.autoPurgeCache == true) {
                    RecommenderDataCache recommenderDataCache = RecommenderDataCache
                            .getInstance();
                    recommenderDataCache.purgeCachedData();
                }
            }
        }, RecommenderDataCache.holdTimeMils);
    }

    /**
     * Set auto purge flag.
     * 
     * Setting this flag to True will enabled Completion cache purge. This class
     * is not automatically added as a completion message handler. It must be
     * added.
     * 
     * @param isEnabled
     *            boolean PurgeOnCompletion enabled flag
     */
    public void setPurgeOnCompletion(boolean isEnabled) {
        PurgeOnCompletion = isEnabled;
    }

}
