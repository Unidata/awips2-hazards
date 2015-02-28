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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
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
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class GraphicalEditor extends CaveSWTDialog implements
        IRiseCrestFallEditor, IDataUpdate {
    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(GraphicalEditor.class);

    private final String BEGIN = "begin";

    private final String END = "end";

    private final String CURRENT = "Set Current";

    private final String MISSING = "Set Missing";

    private final String SET_DATE_TIME = "Set Date/Time";

    private final String MISSING_VAL = "MSG";

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

    private Button beginMsgRdo;

    private Button beginCurrentRdo;

    private Button beginSetRdo;

    private Button endMsgRdo;

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

    public GraphicalEditor(Shell parentShell, IHazardEvent event) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL
                | SWT.RESIZE);
        this.event = event;
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
        createGraphData();
        createCanvas(shell);
        createDataEntryComposite(shell);
        createBottomButtons(shell);
        populate();
    }

    private void populate() {
        if (graphData.getCrestValue() != RiverForecastPoint.MISSINGVAL) {
            crestValTxt.setText(String.valueOf(graphData.getCrestValue()));
        }

        if (graphData.getBeginDate() != null) {
            beginTimeTxt.setText(dateFormat.format(graphData.getBeginDate()));
            beginDate = graphData.getBeginDate();
        }

        if (graphData.getEndDate() != null) {
            if (graphData.getEndDate().getTime() == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
                endTimeTxt.setText("");
                endMsgRdo.setSelection(true);
                endSetRdo.setSelection(false);
                endCurrentRdo.setSelection(false);
            } else {
                endTimeTxt.setText(dateFormat.format(graphData.getEndDate()));
            }
            endDate = graphData.getEndDate();
        }

        if (graphData.getRiseDate() != null) {
            riseTxt.setText(dateFormat.format(graphData.getRiseDate()));
            riseDate = graphData.getRiseDate();
        }

        if (graphData.getCrestDate() != null) {
            crestTxt.setText(dateFormat.format(graphData.getCrestDate()));
            crestDate = graphData.getCrestDate();
        }

        if (graphData.getFallDate() != null) {
            fallTxt.setText(dateFormat.format(graphData.getFallDate()));
            fallDate = graphData.getFallDate();
        }
    }

    private void createGraphData() {
        this.graphData = new GraphData();

        String lid = (String) event.getHazardAttribute(HazardConstants.POINTID);
        graphData.setLid(lid);

        graphData.setBeginDate(new Date(event.getStartTime().getTime()));
        graphData.setEndDate(new Date(event.getEndTime().getTime()));

        long riseTime = (Long) event
                .getHazardAttribute(HazardConstants.RISE_ABOVE);
        Calendar cal = TimeUtil.newGmtCalendar();
        cal.setTimeInMillis(riseTime);
        graphData.setRiseDate(cal.getTime());

        long crestTime = (Long) event.getHazardAttribute(HazardConstants.CREST);
        cal.setTimeInMillis(crestTime);
        graphData.setCrestDate(cal.getTime());

        long fallTime = (Long) event
                .getHazardAttribute(HazardConstants.FALL_BELOW);
        if (fallTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
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
        crestValLbl.setText("Crest Value: ");
        crestValLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
                false));

        gl = new GridLayout(3, false);
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.horizontalSpan = 2;
        crestValTxt = new Text(leftComp, SWT.BORDER);
        crestValTxt.setLayoutData(gd);

        Label beginTimeLbl = new Label(leftComp, SWT.NONE);
        beginTimeLbl.setText("Start Time: ");
        beginTimeLbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
                false));

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        beginTimeTxt = new Text(leftComp, SWT.BORDER);
        beginTimeTxt.setLayoutData(gd);
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

        beginMsgRdo = new Button(beginChkComp, SWT.RADIO);
        beginMsgRdo.setText(MISSING);
        beginMsgRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                beginTimeTxt.setText(MISSING_VAL);
                displayCanvas.setVisible(EventType.BEGIN, false);
                displayCanvas.redraw();
                beginDate = null;
            }
        });

        beginCurrentRdo = new Button(beginChkComp, SWT.RADIO);
        beginCurrentRdo.setText(CURRENT);
        beginCurrentRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date currentTime = SimulatedTime.getSystemTime().getTime();
                beginTimeTxt.setText(dateFormat.format(currentTime));
                beginDate = currentTime;
                displayCanvas.setVisible(EventType.BEGIN, true);
                displayCanvas.redraw();
            }
        });

        beginSetRdo = new Button(beginChkComp, SWT.RADIO);
        beginSetRdo.setText(SET_DATE_TIME);
        beginSetRdo.setSelection(true);
        beginSetRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (beginSetRdo.getSelection()) {
                    setTime(BEGIN);
                }
            }
        });

        Label endTimeLbl = new Label(leftComp, SWT.NONE);
        endTimeLbl.setText("End Time: ");
        endTimeLbl
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        endTimeTxt = new Text(leftComp, SWT.BORDER);
        endTimeTxt.setLayoutData(gd);

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

        endMsgRdo = new Button(endBtnComp, SWT.RADIO);
        endMsgRdo.setText(MISSING);
        endMsgRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                endTimeTxt.setText(MISSING_VAL);
                displayCanvas.setVisible(EventType.END, false);
                displayCanvas.redraw();
                endDate = null;
            }
        });

        endCurrentRdo = new Button(endBtnComp, SWT.RADIO);
        endCurrentRdo.setText(CURRENT);
        endCurrentRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Date currentTime = SimulatedTime.getSystemTime().getTime();
                endTimeTxt.setText(dateFormat.format(currentTime));
                endDate = currentTime;
                displayCanvas.setVisible(EventType.END, true);
                displayCanvas.redraw();

            }
        });

        endSetRdo = new Button(endBtnComp, SWT.RADIO);
        endSetRdo.setText(SET_DATE_TIME);
        endSetRdo.setSelection(true);
        endSetRdo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (endSetRdo.getSelection()) {
                    setTime(END);
                }
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
        riseLbl.setText("Rise Above Time: ");

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        riseTxt = new Text(rightComp, SWT.BORDER);
        riseTxt.setLayoutData(gd);

        riseMsgChk = new Button(rightComp, SWT.CHECK);
        riseMsgChk.setText(MISSING);
        riseMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (riseMsgChk.getSelection()) {
                    riseTxt.setText(MISSING_VAL);
                    riseDate = null;
                } else {
                    if (riseDate != null) {
                        riseTxt.setText(dateFormat.format(riseDate));
                    } else {
                        riseTxt.setText("");
                    }
                }
                displayCanvas.setVisible(EventType.RISE,
                        !riseMsgChk.getSelection());
                displayCanvas.redraw();
            }
        });

        Label crestLbl = new Label(rightComp, SWT.NONE);
        crestLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        crestLbl.setText("Crest Time: ");

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        crestTxt = new Text(rightComp, SWT.BORDER);
        crestTxt.setLayoutData(gd);

        crestMsgChk = new Button(rightComp, SWT.CHECK);
        crestMsgChk.setText(MISSING);
        crestMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (crestMsgChk.getSelection()) {
                    crestTxt.setText(MISSING_VAL);
                    crestDate = null;
                } else {
                    if (crestDate != null) {
                        crestTxt.setText(dateFormat.format(crestDate));
                    } else {
                        crestTxt.setText("");
                    }
                }
                displayCanvas.setVisible(EventType.CREST,
                        !crestMsgChk.getSelection());
                displayCanvas.redraw();
            }
        });

        Label fallLbl = new Label(rightComp, SWT.NONE);
        fallLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        fallLbl.setText("Fall Below Time: ");

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        fallTxt = new Text(rightComp, SWT.BORDER);
        fallTxt.setLayoutData(gd);

        fallMsgChk = new Button(rightComp, SWT.CHECK);
        fallMsgChk.setText(MISSING);
        fallMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fallMsgChk.getSelection()) {
                    fallTxt.setText(MISSING_VAL);
                    fallDate = null;
                } else {
                    if (fallDate != null) {
                        fallTxt.setText(dateFormat.format(fallDate));
                    } else {
                        fallTxt.setText("");
                    }
                }
                displayCanvas.setVisible(EventType.FALL,
                        !fallMsgChk.getSelection());
                displayCanvas.redraw();
            }
        });
    }

    private void createBottomButtons(Shell shell) {
        GridLayout gl = new GridLayout(2, false);
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
                handleOk();
            }
        });

        gd = new GridData(75, SWT.DEFAULT);
        Button cancelBtn = new Button(btnComp, SWT.PUSH);
        cancelBtn.setLayoutData(gd);
        cancelBtn.setText("Cancel");
        cancelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    private void handleOk() {
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
            this.event.setStartTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else {
            this.event.setStartTime(beginDate);
        }

        if (this.endDate == null) {
            this.event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
        } else {
            this.event.setEndTime(endDate);
        }

        this.event.setHazardAttributes(newAttributes);
        String crest = this.crestValTxt.getText();

        newAttributes.put(HazardConstants.CREST_STAGE, crest);

        setReturnValue(event);
        close();
    }

    private void setTime(String type) {
        if (calendarDlg == null || calendarDlg.isDisposed()) {
            if (type.equals(BEGIN)) {
                if (beginDate != null) {
                    calendarDlg = new AwipsCalendar(this.shell, beginDate, 2);
                } else {
                    calendarDlg = new AwipsCalendar(this.shell, 2);
                }
                Object d = calendarDlg.open();
                if (d != null) {
                    Date date = (Date) d;
                    beginTimeTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.BEGIN, date);
                    beginDate = date;
                }
            } else {
                if (endDate != null) {
                    calendarDlg = new AwipsCalendar(this.shell, endDate, 2);
                } else {
                    calendarDlg = new AwipsCalendar(this.shell, 2);
                }
                Object d = calendarDlg.open();
                if (d != null) {
                    Date date = (Date) d;
                    endTimeTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.END, date);
                    endDate = date;
                }
            }
        }
    }

    @Override
    public IHazardEvent getRiseCrestFallEditor(IHazardEvent event) {
        // TODO Here because it's required by the interface
        return null;
    }

    @Override
    public void setDate(EventType eventType, Date date) {
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
