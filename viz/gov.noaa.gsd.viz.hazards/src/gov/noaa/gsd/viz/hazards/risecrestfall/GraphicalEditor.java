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
package gov.noaa.gsd.viz.hazards.risecrestfall;

import gov.noaa.gsd.viz.hazards.risecrestfall.EventRegion.EventType;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.hydro.HazardSettings;
import com.raytheon.uf.common.hazards.hydro.Hydrograph;
import com.raytheon.uf.common.hazards.hydro.IFloodDAO;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
import com.raytheon.uf.common.hazards.hydro.RiverProDataManager;
import com.raytheon.uf.common.hazards.hydro.SHEFObservation;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IEventApplier;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IRiseCrestFallEditor;
import com.raytheon.viz.ui.dialogs.AwipsCalendar;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Graphical rise/crest/fall editor. Allows the user to set these times via
 * lines on a graph.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 17, 2015    3847    mpduff      Initial creation
 * Mar 13, 2015    6922    Chris.Cody  Changes to constrain Begin, Rise, Crest, Fall and End date time values.
 * Mar 17, 2015    6974    mpduff      FAT fixes.
 * Mar 24, 2015    7205    mpduff      Fixes for missing values and until further notice.
 * Apr 01, 2015    7277    Chris.Cody  Changes to Handle Missing Time values.
 * May 14, 2015    7560    mpduff      Fixes for missing values being returned as 0.
 * 
 * </pre>
 * 
 * 
 * @author mpduff
 * @version 1.0
 */

public class GraphicalEditor extends CaveSWTDialog implements
        IRiseCrestFallEditor, IDataUpdate {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GraphicalEditor.class);

    private final String CURRENT = "Set Current";

    private final String MISSING = "Set Missing";

    private final String SET_DATE_TIME = "Set Date/Time";

    private final String MISSING_VAL = "Missing";

    private final String UNTIL_FURTHER_NOTICE = "Until Further Notice";

    private Composite canvasComp;

    private GraphData graphData;

    private Text crestValTxt;

    private Text beginTimeTxt;

    private Date beginDate;

    private Text endTimeTxt;

    private Date endDate;

    private Text riseTxt;

    private Date riseDate;

    private Text crestTxt;

    private Date crestDate;

    private Text fallTxt;

    private Date fallDate;

    private Button beginCurrentRdo;

    private Button beginSetRdo;

    private Button endUntilFurtherNoticeRdo;

    private Button endCurrentRdo;

    private Button endSetRdo;

    private Button crestMsgChk;

    private Button riseMsgChk;

    private Button fallMsgChk;

    private final IHazardEvent event;

    private RiverProDataManager riverProDataManager;

    private SimpleDateFormat dateFormat;

    private EditorCanvas displayCanvas;

    private AwipsCalendar calendarDlg;

    private final String crestValLblTxt = "Crest Value: ";

    private final String beginTimeLblTxt = "Start Time: ";

    private final String endTimeLblTxt = "End Time: ";

    private final String riseLblTxt = "Rise Above Time: ";

    private final String crestLblTxt = "Crest Time: ";

    private final String fallLblTxt = "Fall Below Time: ";

    private final IEventApplier applier;

    public GraphicalEditor(Shell parentShell, IHazardEvent event,
            IEventApplier applier) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL
                | SWT.RESIZE);
        this.event = event;
        this.applier = applier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayout()
     */
    @Override
    protected Layout constructShellLayout() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginBottom = 0;
        gl.marginTop = 0;
        gl.marginLeft = 0;
        gl.marginRight = 0;

        return gl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#constructShellLayoutData()
     */
    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#opened()
     */
    @Override
    protected void opened() {
        Rectangle bounds = this.getShell().getBounds();
        this.getShell().setMinimumSize(bounds.width, bounds.height);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        this.getShell().setText("Rise/Crest/Fall Editor");
        riverProDataManager = new RiverProDataManager();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dateFormat.setLenient(false);
        createGraphData();
        createCanvas(shell);
        createDataEntryComposite(shell);
        createBottomButtons(shell);
        populate();
    }

    private void populate() {

        double crestValue = graphData.getCrestValue();
        if (crestValue != RiverForecastPoint.MISSINGVAL) {
            crestValTxt.setText(String.valueOf(crestValue));
        } else {
            crestValTxt.setText(MISSING_VAL);
        }

        beginDate = graphData.getBeginDate();
        if ((beginDate != null) && (beginDate.getTime() != 0L)) {
            beginTimeTxt.setText(dateFormat.format(beginDate));
            beginCurrentRdo.setSelection(false);
            beginSetRdo.setSelection(true);
        } else {
            beginTimeTxt.setText(MISSING_VAL);
            beginCurrentRdo.setSelection(false);
            beginSetRdo.setSelection(false);
        }

        endDate = graphData.getEndDate();
        if ((endDate != null) && (endDate.getTime() != 0L)) {
            if (endDate.getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
                endTimeTxt.setText(UNTIL_FURTHER_NOTICE);
                endUntilFurtherNoticeRdo.setSelection(true);
                endSetRdo.setSelection(false);
                endCurrentRdo.setSelection(false);
            } else {
                endTimeTxt.setText(dateFormat.format(endDate));
                endUntilFurtherNoticeRdo.setSelection(false);
                endSetRdo.setSelection(true);
                endCurrentRdo.setSelection(false);
            }
        } else {
            endTimeTxt.setText(MISSING_VAL);
            endUntilFurtherNoticeRdo.setSelection(false);
            endSetRdo.setSelection(false);
            endCurrentRdo.setSelection(false);
        }

        riseDate = graphData.getRiseDate();
        if ((riseDate != null) && (riseDate.getTime() != 0L)) {
            riseTxt.setText(dateFormat.format(riseDate));
            riseMsgChk.setSelection(false);
        } else {
            riseTxt.setText(MISSING_VAL);
            riseMsgChk.setSelection(true);
        }

        crestDate = graphData.getCrestDate();
        if ((crestDate != null) && (crestDate.getTime() != 0L)) {
            crestTxt.setText(dateFormat.format(crestDate));
            crestMsgChk.setSelection(false);
        } else {
            crestTxt.setText(MISSING_VAL);
            crestMsgChk.setSelection(true);
        }

        fallDate = graphData.getFallDate();
        if ((fallDate != null) && (fallDate.getTime() != 0L)) {
            fallTxt.setText(dateFormat.format(fallDate));
            fallMsgChk.setSelection(false);
        } else {
            fallTxt.setText(MISSING_VAL);
            fallMsgChk.setSelection(true);
        }

    }

    private void createGraphData() {
        this.graphData = new GraphData();

        String lid = (String) event.getHazardAttribute(HazardConstants.POINTID);
        graphData.setLid(lid);

        graphData.setBeginDate(new Date(event.getStartTime().getTime()));
        graphData.setEndDate(new Date(event.getEndTime().getTime()));

        Calendar cal = TimeUtil.newGmtCalendar();
        long riseTime = getAttributeTime(HazardConstants.RISE_ABOVE);
        if (riseTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS
                && riseTime != HazardConstants.MISSING_VALUE) {
            cal.setTimeInMillis(riseTime);
            graphData.setRiseDate(cal.getTime());
        }

        long crestTime = getAttributeTime(HazardConstants.CREST);
        if (crestTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS
                && crestTime != HazardConstants.MISSING_VALUE) {
            cal.setTimeInMillis(crestTime);
            graphData.setCrestDate(cal.getTime());
        }

        long fallTime = getAttributeTime(HazardConstants.FALL_BELOW);
        if (fallTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS
                && fallTime != HazardConstants.MISSING_VALUE) {
            cal.setTimeInMillis(fallTime);
            graphData.setFallDate(cal.getTime());
        }

        IFloodDAO dao = riverProDataManager.getFloodDAO();
        HazardSettings hazardSettings = riverProDataManager.getHazardSettings();
        List<RiverForecastPoint> forecastPointList = dao
                .getForecastPointInfo(hazardSettings);
        RiverForecastPoint selectedPoint = null;
        for (RiverForecastPoint point : forecastPointList) {
            if (lid.equals(point.getId())) {
                selectedPoint = point;
                break;
            }
        }

        if (selectedPoint == null) {
            statusHandler.warn("No river forecast point for " + lid);
            return;
        }

        graphData.setName(selectedPoint.getName());
        graphData.setActionFlow(selectedPoint.getActionFlow());
        graphData.setActionStage(selectedPoint.getActionStage());
        graphData.setFloodFlow(selectedPoint.getFloodFlow());
        graphData.setFloodStage(selectedPoint.getFloodStage());
        graphData.setModerateFlow(selectedPoint.getModerateFlow());
        graphData.setModerateStage(selectedPoint.getModerateStage());
        graphData.setMajorFlow(selectedPoint.getMajorFlow());
        graphData.setMajorStage(selectedPoint.getMajorStage());

        graphData.setPe(selectedPoint.getPhysicalElement());

        selectedPoint.loadTimeSeries(graphData.getPe());
        Hydrograph observedHydrograph = selectedPoint.getObservedHydrograph();
        List<SHEFObservation> dataList = observedHydrograph
                .getShefHydroDataList();
        if (!dataList.isEmpty()) {
            SHEFObservation so = dataList.get(0);
            graphData.setObservedTs(so.getTypeSource());

            for (SHEFObservation ob : dataList) {
                GraphPoint point = new GraphPoint();
                point.setX(new Date(ob.getValidTime()));
                point.setY(ob.getValue());
                graphData.addObservedPoint(point);
            }
        }

        Hydrograph fcstHydrograph = selectedPoint.getForecastHydrograph();
        dataList = fcstHydrograph.getShefHydroDataList();
        if (!dataList.isEmpty()) {
            SHEFObservation so = dataList.get(0);
            graphData.setForecastTs(so.getTypeSource());
            graphData.setDur(String.valueOf(so.getDuration()));
            for (SHEFObservation ob : dataList) {
                GraphPoint point = new GraphPoint();
                point.setX(new Date(ob.getValidTime()));
                point.setY(ob.getValue());
                graphData.addFcstPoint(point);
            }
        }

        graphData.setCrestValue(selectedPoint.getForecastCrestValue());
    }

    private void createCanvas(Shell shell) {
        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        canvasComp = new Composite(shell, SWT.NONE);
        canvasComp.setLayout(gl);
        canvasComp.setLayoutData(gd);

        displayCanvas = new EditorCanvas(canvasComp, graphData, this, SWT.NONE);
    }

    private long getAttributeTime(String attribute) {
        long timeLong = HazardConstants.MISSING_VALUE;
        Object obj = event.getHazardAttribute(attribute);
        if (obj instanceof Integer) {
            int timeInt = (int) obj;
            timeLong = timeInt;
        } else if (obj instanceof Long) {
            timeLong = (long) obj;
        }

        return timeLong;
    }

    private void createDataEntryComposite(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite comp = new Composite(shell, SWT.NONE);
        comp.setLayout(gl);
        comp.setLayoutData(gd);

        // Left comp
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite leftComp = new Composite(comp, SWT.NONE);
        leftComp.setLayout(gl);
        leftComp.setLayoutData(gd);

        Label crestValLbl = new Label(leftComp, SWT.NONE);
        crestValLbl.setText(crestValLblTxt);
        crestValLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
                false));

        gl = new GridLayout(3, false);
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.horizontalSpan = 2;
        crestValTxt = new Text(leftComp, SWT.BORDER);
        crestValTxt.setLayoutData(gd);

        Label beginTimeLbl = new Label(leftComp, SWT.NONE);
        beginTimeLbl.setText(beginTimeLblTxt);
        beginTimeLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
                false));

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        beginTimeTxt = new Text(leftComp, SWT.BORDER);
        beginTimeTxt.setLayoutData(gd);
        beginTimeTxt.setEditable(false);
        beginTimeTxt.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                // no-op
            }

            @Override
            public void mouseDown(MouseEvent e) {
                Date date = setTime(beginDate);
                if (date != null) {
                    beginTimeTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.BEGIN, date);
                    beginDate = date;
                    beginSetRdo.setSelection(true);
                    beginCurrentRdo.setSelection(false);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no-op
            }
        });

        gl = new GridLayout(3, false);
        gl.marginBottom = 0;
        gl.marginHeight = 0;
        gl.marginLeft = 0;
        gl.marginRight = 0;
        gl.marginTop = 0;
        gl.marginWidth = 0;
        gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        Composite beginChkComp = new Composite(leftComp, SWT.NONE);
        beginChkComp.setLayout(gl);
        beginChkComp.setLayoutData(gd);

        beginCurrentRdo = new Button(beginChkComp, SWT.RADIO);
        beginCurrentRdo.setText(CURRENT);
        beginCurrentRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date currentTime = SimulatedTime.getSystemTime().getTime();
                beginTimeTxt.setText(dateFormat.format(currentTime));
                beginDate = currentTime;
                displayCanvas.setVisible(EventType.BEGIN, true);
                displayCanvas.setDate(EventType.BEGIN, beginDate);
                displayCanvas.redraw();
            }
        });

        beginSetRdo = new Button(beginChkComp, SWT.RADIO);
        beginSetRdo.setText(SET_DATE_TIME);
        beginSetRdo.setSelection(true);
        beginSetRdo.setToolTipText("Click to select a date and time");
        beginSetRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (beginSetRdo.getSelection()) {
                    Date date = setTime(beginDate);
                    if (date != null) {
                        beginTimeTxt.setText(dateFormat.format(date));
                        displayCanvas.setDate(EventType.BEGIN, date);
                        displayCanvas.setVisible(EventType.BEGIN, true);
                        beginDate = date;
                    }
                }
            }
        });

        Label endTimeLbl = new Label(leftComp, SWT.NONE);
        endTimeLbl.setText(endTimeLblTxt);
        endTimeLbl
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        endTimeTxt = new Text(leftComp, SWT.BORDER);
        endTimeTxt.setLayoutData(gd);
        endTimeTxt.setEditable(false);
        endTimeTxt.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                // no-op
            }

            @Override
            public void mouseDown(MouseEvent e) {
                Date date = setTime(endDate);
                if (date != null) {
                    endTimeTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.END, date);
                    endDate = date;
                    endSetRdo.setSelection(true);
                    endUntilFurtherNoticeRdo.setSelection(false);
                    endCurrentRdo.setSelection(false);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no-op
            }
        });

        gl = new GridLayout(3, false);
        gl.marginBottom = 0;
        gl.marginHeight = 0;
        gl.marginLeft = 0;
        gl.marginRight = 5;
        gl.marginTop = 0;
        gl.marginWidth = 0;
        gd = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
        Composite endBtnComp = new Composite(leftComp, SWT.NONE);
        endBtnComp.setLayout(gl);
        endBtnComp.setLayoutData(gd);

        endCurrentRdo = new Button(endBtnComp, SWT.RADIO);
        endCurrentRdo.setText(CURRENT);
        endCurrentRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date currentTime = SimulatedTime.getSystemTime().getTime();
                endTimeTxt.setText(dateFormat.format(currentTime));
                endDate = currentTime;
                displayCanvas.setDate(EventType.END, endDate);
                displayCanvas.setVisible(EventType.END, true);
                displayCanvas.redraw();

            }
        });

        endSetRdo = new Button(endBtnComp, SWT.RADIO);
        endSetRdo.setText(SET_DATE_TIME);
        endSetRdo.setSelection(true);
        endSetRdo.setToolTipText("Click to select a date and time");

        endSetRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (endSetRdo.getSelection()) {
                    Date date = setTime(endDate);
                    if (date != null) {
                        endTimeTxt.setText(dateFormat.format(date));
                        displayCanvas.setDate(EventType.END, date);
                        displayCanvas.setVisible(EventType.END, true);
                        endDate = date;
                    }
                }
            }
        });

        endUntilFurtherNoticeRdo = new Button(endBtnComp, SWT.RADIO);
        endUntilFurtherNoticeRdo.setText(UNTIL_FURTHER_NOTICE);
        endUntilFurtherNoticeRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                endTimeTxt.setText(UNTIL_FURTHER_NOTICE);
                displayCanvas.setVisible(EventType.END, false);
                displayCanvas.redraw();
                endDate = null;
            }
        });

        // Right Comp
        gl = new GridLayout(3, false);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite rightComp = new Composite(comp, SWT.NONE);
        rightComp.setLayout(gl);
        rightComp.setLayoutData(gd);

        Label riseLbl = new Label(rightComp, SWT.NONE);
        riseLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        riseLbl.setText(riseLblTxt);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        riseTxt = new Text(rightComp, SWT.BORDER);
        riseTxt.setLayoutData(gd);
        riseTxt.setEditable(false);
        riseTxt.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
                // no-op
            }

            @Override
            public void mouseDown(MouseEvent e) {
                Date date = setTime(riseDate);
                if (date != null) {
                    riseTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.RISE, date);
                    riseDate = date;
                    riseMsgChk.setSelection(false);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no-op
            }
        });

        riseMsgChk = new Button(rightComp, SWT.CHECK);
        riseMsgChk.setText(MISSING);
        riseMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (riseMsgChk.getSelection()) {
                    riseTxt.setText(MISSING_VAL);
                    riseDate = null;
                } else {
                    if (riseDate == null || riseDate.getTime() == 0) {
                        Date date = graphData.getRiseDate();
                        date = setTime(date);
                        if (date != null) {
                            riseTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.RISE, date);
                            riseDate = date;
                            riseMsgChk.setSelection(false);
                        } else {
                            riseMsgChk.setSelection(true);
                        }
                    } else {
                        riseTxt.setText(dateFormat.format(graphData
                                .getRiseDate()));
                    }
                }
                displayCanvas.setVisible(EventType.RISE,
                        !riseMsgChk.getSelection());
            }
        });

        Label crestLbl = new Label(rightComp, SWT.NONE);
        crestLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        crestLbl.setText(crestLblTxt);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        crestTxt = new Text(rightComp, SWT.BORDER);
        crestTxt.setLayoutData(gd);
        crestTxt.setEditable(false);
        crestTxt.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
                // no-op
            }

            @Override
            public void mouseDown(MouseEvent e) {
                Date date = setTime(crestDate);
                if (date != null) {
                    crestTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.CREST, date);
                    crestDate = date;
                    crestMsgChk.setSelection(false);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no-op
            }
        });

        crestMsgChk = new Button(rightComp, SWT.CHECK);
        crestMsgChk.setText(MISSING);
        crestMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (crestMsgChk.getSelection()) {
                    crestTxt.setText(MISSING_VAL);
                    crestDate = null;
                } else {
                    if (crestDate == null || crestDate.getTime() == 0) {
                        Date date = graphData.getCrestDate();
                        date = setTime(date);
                        if (date != null) {
                            crestTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.CREST, date);
                            crestDate = date;
                            crestMsgChk.setSelection(false);
                        } else {
                            crestMsgChk.setSelection(true);
                        }
                    } else {
                        crestTxt.setText(dateFormat.format(graphData
                                .getCrestDate()));
                    }
                }
                displayCanvas.setVisible(EventType.CREST,
                        !crestMsgChk.getSelection());
            }
        });

        Label fallLbl = new Label(rightComp, SWT.NONE);
        fallLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        fallLbl.setText(fallLblTxt);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        fallTxt = new Text(rightComp, SWT.BORDER);
        fallTxt.setLayoutData(gd);
        fallTxt.setEditable(false);
        fallTxt.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
                // no-op
            }

            @Override
            public void mouseDown(MouseEvent e) {
                Date date = setTime(fallDate);
                if (date != null) {
                    fallTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.FALL, date);
                    fallDate = date;
                    fallMsgChk.setSelection(false);
                }
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // no-op
            }
        });

        fallMsgChk = new Button(rightComp, SWT.CHECK);
        fallMsgChk.setText(MISSING);
        fallMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fallMsgChk.getSelection()) {
                    fallTxt.setText(MISSING_VAL);
                    fallDate = null;
                } else {
                    if (fallDate == null || fallDate.getTime() == 0) {
                        Date date = graphData.getFallDate();
                        date = setTime(date);
                        if (date != null) {
                            fallTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.FALL, date);
                            fallDate = date;
                            fallMsgChk.setSelection(false);
                        } else {
                            fallMsgChk.setSelection(true);
                        }
                    } else {
                        fallTxt.setText(dateFormat.format(graphData
                                .getFallDate()));
                    }
                }
                displayCanvas.setVisible(EventType.FALL,
                        !fallMsgChk.getSelection());
            }
        });
    }

    private void createBottomButtons(Shell shell) {
        GridLayout gl = new GridLayout(4, false);
        GridData gd = new GridData(SWT.CENTER, SWT.DEFAULT, false, false);
        Composite btnComp = new Composite(shell, SWT.NONE);
        btnComp.setLayout(gl);
        btnComp.setLayoutData(gd);

        gd = new GridData(75, SWT.DEFAULT);
        Button okBtn = new Button(btnComp, SWT.PUSH);
        okBtn.setText("OK");
        okBtn.setLayoutData(gd);
        okBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (handleApply()) {
                    close();
                }
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button applyBtn = new Button(btnComp, SWT.PUSH);
        applyBtn.setLayoutData(gd);
        applyBtn.setText("Apply");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (handleApply()) {
                    applier.apply((IHazardEvent) getReturnValue());
                }
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setLayoutData(gd);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setReturnValue(null);
                close();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button resetBtn = new Button(btnComp, SWT.PUSH);
        resetBtn.setLayoutData(gd);
        resetBtn.setText("Reset");
        resetBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                createGraphData();
                populate();
                displayCanvas.setGraphData(graphData);
                displayCanvas.redraw();
            }
        });
    }

    private boolean handleApply() {

        if (verifyDialogInput() != true) {
            return (false);
        }
        Map<String, Serializable> attributes = this.event.getHazardAttributes();
        Map<String, Serializable> newAttributes = new HashMap<>(
                attributes.size(), 1);
        for (String name : attributes.keySet()) {
            newAttributes.put(name, attributes.get(name));
        }

        if (fallDate == null) {
            newAttributes.put(HazardConstants.FALL_BELOW,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.FALL_BELOW,
                    this.fallDate.getTime());
        }

        if (riseDate == null) {
            newAttributes.put(HazardConstants.RISE_ABOVE,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.RISE_ABOVE,
                    this.riseDate.getTime());
        }

        if (crestDate == null) {
            newAttributes.put(HazardConstants.CREST,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.CREST, this.crestDate.getTime());
        }

        if (this.beginDate == null) {
            this.event.setStartTime(new Date(HazardConstants.MISSING_VALUE));
        } else {
            this.event.setStartTime(beginDate);
        }

        if (this.endDate == null) {
            this.event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else {
            this.event.setEndTime(endDate);
        }

        String crestValue = this.crestValTxt.getText();
        if (crestValue == null) {
            newAttributes.put(HazardConstants.CREST_STAGE,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.CREST_STAGE, crestValue);
        }

        this.event.setHazardAttributes(newAttributes);

        setReturnValue(event);
        return true;
    }

    private Date setTime(Date date) {
        if (calendarDlg == null || calendarDlg.isDisposed()) {
            if (date != null) {
                calendarDlg = new AwipsCalendar(this.shell, date, 2);
            } else {
                calendarDlg = new AwipsCalendar(this.shell, 2);
            }
            Object obj = calendarDlg.open();
            if (obj != null) {
                date = (Date) obj;
            }
        }

        return date;
    }

    @Override
    public IHazardEvent getRiseCrestFallEditor(IHazardEvent event,
            IEventApplier applier) {
        // TODO Here because it's required by the interface
        return null;
    }

    @Override
    public void setDate(EventType eventType, Date date) {
        if (date != null) {
            if (eventType == EventType.BEGIN) {
                this.beginTimeTxt.setText(dateFormat.format(date));
                this.beginDate = date;
            } else if (eventType == EventType.RISE) {
                this.riseTxt.setText(dateFormat.format(date));
                this.riseDate = date;
            } else if (eventType == EventType.CREST) {
                this.crestTxt.setText(dateFormat.format(date));
                this.crestDate = date;
            } else if (eventType == EventType.FALL) {
                this.fallTxt.setText(dateFormat.format(date));
                this.fallDate = date;
            } else if (eventType == EventType.END) {
                this.endTimeTxt.setText(dateFormat.format(date));
                this.endDate = date;
            }
        }
    }

    private boolean verifyDialogInput() {
        boolean isValid = true;

        StringBuffer errMsgSB = new StringBuffer();

        if (endDate != null && endDate.before(beginDate)) {
            errMsgSB.append("Invalid date time:\n");
            errMsgSB.append(beginTimeLblTxt);
            errMsgSB.append(": ");
            errMsgSB.append(beginTimeTxt.getText());
            errMsgSB.append(" cannot precede\n");
            errMsgSB.append(endTimeLblTxt);
            errMsgSB.append(": ");
            errMsgSB.append(endTimeTxt.getText());
            errMsgSB.append("\n");
            isValid = false;
        }
        /*
         * TODO Currently there are no date time constraints other than Start
         * and must precede End. Rules have not been established to otherwise
         * constrain Rise, Crest, Fall, Start and End.
         */
        if (errMsgSB.length() > 0) {
            String msgString = "Invalid date time or date time format:\n"
                    + errMsgSB.toString() + "Correct Format is "
                    + dateFormat.toLocalizedPattern();

            MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.BORDER
                    | SWT.ON_TOP | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.OK);
            mb.setText("Invalid Input");
            mb.setMessage(msgString);
            mb.open();
            isValid = false;
        }

        return (isValid);
    }
}
