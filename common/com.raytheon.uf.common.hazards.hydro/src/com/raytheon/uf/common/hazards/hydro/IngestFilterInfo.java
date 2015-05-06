package com.raytheon.uf.common.hazards.hydro;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * This class represents Ingest Filter information (INGESTFILTER) for a River
 * Forecast Point. It is used to determine the best Type Source for a River
 * Forecast Point.
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

public class IngestFilterInfo {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(IngestFilterInfo.class);

    public static final String TABLE_NAME = "IngestFilter";

    public static final String DISTINCT_SETTING_COLUMN_NAME_STRING = "DISTINCT ts_rank, ts ";

    public static final String DISTINCT_COLUMN_NAME_STRING = "DISTINCT ts_rank, ts, lid, pe ";

    public static final String COLUMN_NAME_STRING = "ts_rank, ts, lid, pe, dur, extremum, "
            + " ingest, ofs_input, stg2_input";

    // NOTE : This is NOT their order of occurrence in the database
    private final int TS_RANK = 0;

    private final int TS_IDX = 1;

    private final int LID_FIELD_IDX = 2;

    private final int PE_FIELD_IDX = 3;

    private final int DUR_IDX = 4;

    private final int EXTREMUM_IDX = 5;

    private final int INGEST_IDX = 6;

    private final int OFS_INPUT_IDX = 7;

    private final int STG2_INPUT_IDX = 8;

    /**
     * Forecast Point Identifier (LID)
     */
    protected String lid;

    /**
     * Physical element (PE)
     */
    protected String physicalElement;

    /**
     * Duration of observation (DUR)
     */
    protected int duration;

    /**
     * Type source of observation (TS)
     */
    protected String typeSource;

    /**
     * Extremum of observation (e.g. max, min) (EXTREMUM)
     */
    protected char extremum = ' ';

    /**
     * Rank of the Type Source (TS_RANK)
     */
    private int tsRank;

    /**
     * Boolean ingest flag (INGEST)
     */
    private boolean ingest;

    /**
     * Boolean ofs_input flag (OFS_INPUT)
     */
    private boolean ofsInput;

    /**
     * Boolean ofs_input flag (STG2_INPUT)
     */
    private boolean stg2Input;

    /**
     * Default constructor
     */
    public IngestFilterInfo() {
        super();
    }

    public IngestFilterInfo(Object[] queryResult) {
        super();
        if (queryResult != null) {
            int queryResultSize = queryResult.length;
            Object queryValue = null;
            for (int i = 0; i < queryResultSize; i++) {
                queryValue = queryResult[i];
                if (queryValue == null) {
                    continue;
                }
                switch (i) {
                case TS_RANK:
                    this.tsRank = (Integer) queryValue;
                    break;
                case TS_IDX:
                    this.typeSource = (String) queryValue;
                    break;
                case LID_FIELD_IDX:
                    this.lid = (String) queryValue;
                    break;
                case PE_FIELD_IDX:
                    this.physicalElement = (String) queryValue;
                    break;
                case DUR_IDX:
                    this.duration = (Integer) queryValue;
                    break;
                case EXTREMUM_IDX:
                    String extremumString = (String) queryValue;
                    if (extremumString.length() > 0) {
                        this.extremum = extremumString.charAt(0);
                    }
                    break;
                case INGEST_IDX:
                    String ingestString = (String) queryValue;
                    if (ingestString.startsWith("T") == true) {
                        this.ingest = true;
                    } else {
                        this.ingest = false;
                    }
                    break;
                case OFS_INPUT_IDX:
                    String ofsInputString = (String) queryValue;
                    if (ofsInputString.startsWith("T") == true) {
                        this.ofsInput = true;
                    } else {
                        this.ofsInput = false;
                    }
                    break;
                case STG2_INPUT_IDX:
                    String stg2InputString = (String) queryValue;
                    if (stg2InputString.startsWith("T") == true) {
                        this.stg2Input = true;
                    } else {
                        this.stg2Input = false;
                    }
                    break;
                default:
                    statusHandler
                            .error("IngestFilter Constructor array out of sync with number of data fields. Unknown field for value "
                                    + (String) queryValue);
                }
            }
        }
    }

    /**
     * Get Forecast Point Identifier.
     * 
     * @return lid
     */
    public String getLid() {
        return (this.lid);
    }

    /**
     * @return the physical element
     */
    public String getPhysicalElement() {
        return physicalElement;
    }

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return the typeSource
     */
    public String getTypeSource() {
        return typeSource;
    }

    /**
     * @return the extremum
     */
    public char getExtremum() {
        return extremum;
    }

    /**
     * @return the tsRank
     */
    public int getTsRank() {
        return (this.tsRank);
    }

    /**
     * @return the ingest flag
     */
    public boolean getIngest() {
        return (this.ingest);
    }

    /**
     * @return the ofsInput flag
     */
    public boolean getOfsInput() {
        return (this.ofsInput);
    }

    /**
     * @return the stg2Input flag
     */
    public boolean getStg2Input() {
        return (this.stg2Input);
    }

}
