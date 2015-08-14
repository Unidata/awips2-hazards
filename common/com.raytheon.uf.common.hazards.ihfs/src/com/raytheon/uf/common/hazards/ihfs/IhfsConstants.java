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
 */
package com.raytheon.uf.common.hazards.ihfs;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Class to contain Constants used for ihfs queries.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 28, 2015 8839       Chris.Cody  Initial Creation
 * Aug 14, 2015 9988       Chris.Cody  Add Aggregate query functions
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class IhfsConstants {

    public final static String SELECT = "SELECT";

    public final static String FROM = "FROM";

    public final static String WHERE = "WHERE";

    public final static String ORDER_BY = "ORDER BY";

    public final static String GROUP_BY = "GROUP BY";

    public final static String NOT = "NOT";

    public final static String AND = "AND";

    public final static String OR = "OR";

    public final static String OPEN_PAREN = "(";

    public final static String CLOSE_PAREN = ")";

    public final static String NULL_VALUE = "<NULL>";

    public final static String NULL_SQL_STRING = "null";

    public final static String ASC = "ASC";

    public final static String DESC = "DESC";

    public final static String MIN = "MIN";

    public final static String MAX = "MAX";

    public final static String COUNT = "COUNT";

    public final static String SUM = "SUM";

    public final static String MISSING_DATA_STRING = "-9999";

    public final static Integer MISSING_DATA_INTEGER = new Integer(-9999);

    public final static Long MISSING_DATA_LONG = new Long(-9999);

    public final static Float MISSING_DATA_FLOAT = new Float(-9999.0);

    public final static Double MISSING_DATA_DOUBLE = new Double(-9999.0);

    public final static String DefaultJavaDateStringPattern = "EEE MMM dd HH:mm:ss zzz yyyy";

    public final static String CaveDateStringPattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * ThreadLocal instance of Standard date format for ihfs data.
     */
    public static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sTemp = new SimpleDateFormat(CaveDateStringPattern);
            sTemp.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sTemp;
        }
    };

    /**
     * @return the dateFormat
     */
    public static SimpleDateFormat getDateFormat() {
        return dateFormat.get();

    }

}
