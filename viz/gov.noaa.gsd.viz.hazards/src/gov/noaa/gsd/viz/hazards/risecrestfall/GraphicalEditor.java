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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.hydro.HydrographForecast;
import com.raytheon.uf.common.hazards.hydro.HydrographObserved;
import com.raytheon.uf.common.hazards.hydro.RiverForecastManager;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants;
import com.raytheon.uf.common.hazards.hydro.SHEFForecast;
import com.raytheon.uf.common.hazards.hydro.SHEFObserved;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IEventApplier;
import com.raytheon.uf.viz.hazards.sessionmanager.messenger.IMessenger.IRiseCrestFallEditor;
import com.raytheon.viz.ui.dialogs.CalendarDialog;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

import gov.noaa.gsd.viz.hazards.risecrestfall.EventRegion.EventType;

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
 * May 18, 2015    6562    Chris.Cody  Restructure River Forecast Points/Recommender
 * Jan 08, 2016   12499    Roger.Ferrel Fix handling of time variables and code clean up.
 * Jan 15, 2016    9387    Robert.Blum Fix UFN so that the HID checkbox stays in sync.
 * Feb 08, 2016   15422    mduff       Removed references (all comments) to SWTDialogBase.
 * Mar 23, 2016   16050    Robert.Blum Fixed case where startTime could be set in the passed for
 *                                     hazards that have not been issued yet.
 * May 02, 2016   18247    Robert.Blum Graphical Editor now looks at observed crests as well.
 * May 10, 2016   16869    mduff       Remove checks for starttime before current time if the product 
 *                                     has already been issued.
 * Aug 16, 2016   15017    Robert.Blum Updates for setting the crest value/time correctly.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class GraphicalEditor extends CaveSWTDialog
        implements IRiseCrestFallEditor, IDataUpdate {

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

    private long beginTime;

    private Text endTimeTxt;

    private long endTime;

    private Text riseTxt;

    private long riseTime;

    private Text crestTxt;

    private long crestTime;

    private Text fallTxt;

    private long fallTime;

    private Button beginCurrentRdo;

    private Button beginSetRdo;

    private Button endUntilFurtherNoticeRdo;

    private Button endCurrentRdo;

    private Button endSetRdo;

    private Button crestMsgChk;

    private Button riseMsgChk;

    private Button fallMsgChk;

    private final IHazardEvent event;

    private SimpleDateFormat dateFormat;

    private EditorCanvas displayCanvas;

    private final String crestValLblTxt = "Crest Value: ";

    private final String beginTimeLblTxt = "Start Time: ";

    private final String endTimeLblTxt = "End Time: ";

    private final String riseLblTxt = "Rise Above Time: ";

    private final String crestLblTxt = "Crest Time: ";

    private final String fallLblTxt = "Fall Below Time: ";

    private final IEventApplier applier;

    public GraphicalEditor(Shell parentShell, IHazardEvent event,
            IEventApplier applier) {
        super(parentShell,
                SWT.DIALOG_TRIM | SWT.MIN | SWT.PRIMARY_MODAL | SWT.RESIZE);
        this.event = event;
        this.applier = applier;
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout gl = new GridLayout(1, false);
        gl.marginBottom = 0;
        gl.marginTop = 0;
        gl.marginLeft = 0;
        gl.marginRight = 0;

        return gl;
    }

    @Override
    protected Object constructShellLayoutData() {
        return new GridData(SWT.FILL, SWT.FILL, true, true);
    }

    @Override
    protected void opened() {
        Rectangle bounds = this.getShell().getBounds();
        this.getShell().setMinimumSize(bounds.width, bounds.height);
    }

    @Override
    protected void initializeComponents(Shell shell) {
        this.getShell().setText("Rise/Crest/Fall Editor");
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
        if (crestValue != RiverHydroConstants.MISSING_VALUE_DOUBLE) {
            crestValTxt.setText(String.valueOf(crestValue));
        } else {
            crestValTxt.setText(MISSING_VAL);
        }

        Date beginDate = graphData.getBeginDate();
        if ((beginDate != null) && isValidTime(beginDate.getTime())) {
            beginTimeTxt.setText(dateFormat.format(beginDate));
            beginCurrentRdo.setSelection(false);
            beginSetRdo.setSelection(true);
            beginTime = beginDate.getTime();
        } else {
            beginTimeTxt.setText(MISSING_VAL);
            beginCurrentRdo.setSelection(false);
            beginSetRdo.setSelection(false);
            beginTime = HazardConstants.MISSING_VALUE;
        }

        Date endDate = graphData.getEndDate();
        if ((endDate != null)
                && (endDate.getTime() != HazardConstants.MIN_TIME)) {
            endTime = endDate.getTime();
            if (endTime == HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
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
            endTime = HazardConstants.MISSING_VALUE;
        }

        Date riseDate = graphData.getRiseDate();
        if ((riseDate != null) && isValidTime(riseDate.getTime())) {
            riseTxt.setText(dateFormat.format(riseDate));
            riseTime = riseDate.getTime();
            riseMsgChk.setSelection(false);
        } else {
            riseTxt.setText(MISSING_VAL);
            riseTime = HazardConstants.MISSING_VALUE;
            riseMsgChk.setSelection(true);
        }

        Date crestDate = graphData.getCrestDate();
        if ((crestDate != null) && isValidTime(crestDate.getTime())) {
            crestTxt.setText(dateFormat.format(crestDate));
            crestTime = crestDate.getTime();
            crestMsgChk.setSelection(false);
        } else {
            crestTxt.setText(MISSING_VAL);
            crestMsgChk.setSelection(true);
        }

        Date fallDate = graphData.getFallDate();
        if ((fallDate != null) && isValidTime(fallDate.getTime())) {
            fallTxt.setText(dateFormat.format(fallDate));
            fallTime = fallDate.getTime();
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
        if (isValidTime(riseTime)) {
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

        RiverForecastManager riverForecastManager = new RiverForecastManager();
        RiverForecastPoint riverForecastPoint = riverForecastManager
                .getRiverForecastPoint(lid, true);
        if (riverForecastPoint == null) {
            statusHandler.warn("No river forecast point for " + lid);
            return;
        }

        graphData.setName(riverForecastPoint.getName());
        graphData.setActionFlow(riverForecastPoint.getActionFlow());
        graphData.setActionStage(riverForecastPoint.getActionStage());
        graphData.setFloodFlow(riverForecastPoint.getFloodFlow());
        graphData.setFloodStage(riverForecastPoint.getFloodStage());
        graphData.setModerateFlow(riverForecastPoint.getModerateFlow());
        graphData.setModerateStage(riverForecastPoint.getModerateStage());
        graphData.setMajorFlow(riverForecastPoint.getMajorFlow());
        graphData.setMajorStage(riverForecastPoint.getMajorStage());

        graphData.setPe(riverForecastPoint.getPhysicalElement());

        HydrographObserved hydrographObserved = riverForecastPoint
                .getHydrographObserved();
        List<SHEFObserved> observedDataList = hydrographObserved
                .getShefHydroDataList();
        if (!observedDataList.isEmpty()) {
            SHEFObserved so = observedDataList.get(0);
            graphData.setObservedTs(so.getTypeSource());

            for (SHEFObserved ob : observedDataList) {
                GraphPoint point = new GraphPoint();
                point.setX(new Date(ob.getObsTime()));
                point.setY(ob.getValue());
                graphData.addObservedPoint(point);
            }
        }

        HydrographForecast hydrographForecast = riverForecastPoint
                .getHydrographForecast();
        List<SHEFForecast> forecastDataList = hydrographForecast
                .getShefHydroDataList();
        if (!forecastDataList.isEmpty()) {
            SHEFForecast shefForecast = forecastDataList.get(0);
            graphData.setForecastTs(shefForecast.getTypeSource());
            graphData.setDur(String.valueOf(shefForecast.getDuration()));
            for (SHEFForecast shefForecastData : forecastDataList) {
                GraphPoint point = new GraphPoint();
                point.setX(new Date(shefForecastData.getValidTime()));
                point.setY(shefForecastData.getValue());
                graphData.addFcstPoint(point);
            }
        }

        double forecastCrest = (Double) event
                .getHazardAttribute(HazardConstants.CREST_STAGE_FORECAST);

        double maxForecastStage = (Double) event
                .getHazardAttribute(HazardConstants.MAX_FORECAST_STAGE);
        graphData.setCrestValueMaxForecastValue(
                maxForecastStage == forecastCrest);
        double observedCrest = (Double) event
                .getHazardAttribute(HazardConstants.CREST_STAGE_OBSERVED);
        graphData.setCrestValue(Math.max(observedCrest, forecastCrest));
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
        crestValLbl.setLayoutData(
                new GridData(SWT.LEFT, SWT.CENTER, false, false));

        gl = new GridLayout(3, false);
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 200;
        gd.horizontalSpan = 2;
        crestValTxt = new Text(leftComp, SWT.BORDER);
        crestValTxt.setLayoutData(gd);

        Label beginTimeLbl = new Label(leftComp, SWT.NONE);
        beginTimeLbl.setText(beginTimeLblTxt);
        beginTimeLbl
                .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        beginTimeTxt = new Text(leftComp, SWT.BORDER);
        beginTimeTxt.setLayoutData(gd);
        beginTimeTxt.setEditable(false);
        beginTimeTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                long time = setTime(beginTime);
                setBeginTime(time);
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
                Date currentDate = TimeUtil.newDate();
                beginTimeTxt.setText(dateFormat.format(currentDate));
                beginTime = currentDate.getTime();
                displayCanvas.setVisible(EventType.BEGIN, true);
                displayCanvas.setDate(EventType.BEGIN, currentDate);
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
                    long time = setTime(beginTime);
                    if (isValidTime(time)) {
                        Date date = convertTimeToDate(time);
                        beginTimeTxt.setText(dateFormat.format(date));
                        beginTime = time;
                        displayCanvas.setDate(EventType.BEGIN, date);
                        displayCanvas.setVisible(EventType.BEGIN, true);
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
        endTimeTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                long time = setTime(endTime);
                if (isValidTime(time)) {
                    Date date = convertTimeToDate(time);
                    endTimeTxt.setText(dateFormat.format(date));
                    endTime = time;
                    displayCanvas.setDate(EventType.END, date);
                    endSetRdo.setSelection(true);
                    endUntilFurtherNoticeRdo.setSelection(false);
                    endCurrentRdo.setSelection(false);
                }
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
                Date currentTime = TimeUtil.newDate();
                endTimeTxt.setText(dateFormat.format(currentTime));
                endTime = currentTime.getTime();
                displayCanvas.setDate(EventType.END, currentTime);
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
                    long time = setTime(endTime);
                    if (isValidTime(time)) {
                        Date date = convertTimeToDate(time);
                        endTimeTxt.setText(dateFormat.format(date));
                        displayCanvas.setDate(EventType.END, date);
                        displayCanvas.setVisible(EventType.END, true);
                        endTime = time;
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
                endTime = HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS;
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
        riseTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                riseTime = setTime(riseTime);
                if (isValidTime(riseTime)) {
                    Date date = convertTimeToDate(riseTime);
                    riseTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.RISE, date);
                    riseMsgChk.setSelection(false);
                }
            }
        });

        riseMsgChk = new Button(rightComp, SWT.CHECK);
        riseMsgChk.setText(MISSING);
        riseMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (riseMsgChk.getSelection()) {
                    riseTxt.setText(MISSING_VAL);
                    riseTime = HazardConstants.MISSING_VALUE;
                } else {
                    if (!isValidTime(riseTime)) {
                        Date date = graphData.getRiseDate();
                        if (date != null) {
                            riseTime = date.getTime();
                        }
                        riseTime = setTime(riseTime);
                        if (isValidTime(riseTime)) {
                            date = convertTimeToDate(riseTime);
                            riseTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.RISE, date);
                            riseMsgChk.setSelection(false);
                        } else {
                            riseMsgChk.setSelection(true);
                        }
                    } else {
                        riseTxt.setText(
                                dateFormat.format(graphData.getRiseDate()));
                    }
                }
                displayCanvas.setVisible(EventType.RISE,
                        !riseMsgChk.getSelection());
            }
        });

        Label crestLbl = new Label(rightComp, SWT.NONE);
        crestLbl.setLayoutData(
                new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        crestLbl.setText(crestLblTxt);

        gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
        gd.widthHint = 125;
        crestTxt = new Text(rightComp, SWT.BORDER);
        crestTxt.setLayoutData(gd);
        crestTxt.setEditable(false);
        crestTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                crestTime = setTime(crestTime);
                if (isValidTime(crestTime)) {
                    Date date = convertTimeToDate(crestTime);
                    crestTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.CREST, date);
                    crestMsgChk.setSelection(false);
                }
            }
        });

        crestMsgChk = new Button(rightComp, SWT.CHECK);
        crestMsgChk.setText(MISSING);
        crestMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (crestMsgChk.getSelection()) {
                    crestTxt.setText(MISSING_VAL);
                    crestTime = HazardConstants.MISSING_VALUE;
                } else {
                    if (!isValidTime(crestTime)) {
                        Date date = graphData.getCrestDate();
                        if (date != null) {
                            crestTime = date.getTime();
                        }
                        crestTime = setTime(crestTime);
                        if (isValidTime(crestTime)) {
                            date = convertTimeToDate(crestTime);
                            crestTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.CREST, date);
                            crestMsgChk.setSelection(false);
                        } else {
                            crestMsgChk.setSelection(true);
                        }
                    } else {
                        crestTxt.setText(
                                dateFormat.format(graphData.getCrestDate()));
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
        fallTxt.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                fallTime = setTime(fallTime);
                if (isValidTime(fallTime)) {
                    Date date = convertTimeToDate(fallTime);
                    fallTxt.setText(dateFormat.format(date));
                    displayCanvas.setDate(EventType.FALL, date);
                    fallMsgChk.setSelection(false);
                }
            }
        });

        fallMsgChk = new Button(rightComp, SWT.CHECK);
        fallMsgChk.setText(MISSING);
        fallMsgChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (fallMsgChk.getSelection()) {
                    fallTxt.setText(MISSING_VAL);
                    fallTime = HazardConstants.MISSING_VALUE;
                } else {
                    if (!isValidTime(fallTime)) {
                        Date date = graphData.getFallDate();
                        if (date != null) {
                            fallTime = date.getTime();
                        }
                        fallTime = setTime(fallTime);
                        if (isValidTime(fallTime)) {
                            date = convertTimeToDate(fallTime);
                            fallTxt.setText(dateFormat.format(date));
                            displayCanvas.setDate(EventType.FALL, date);
                            fallMsgChk.setSelection(false);
                        } else {
                            fallMsgChk.setSelection(true);
                        }
                    } else {
                        fallTxt.setText(
                                dateFormat.format(graphData.getFallDate()));
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
                    applier.apply((IHazardEvent) getReturnValue());
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

        if (!isValidTime(this.fallTime)) {
            newAttributes.put(HazardConstants.FALL_BELOW,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.FALL_BELOW, this.fallTime);
        }

        if (!isValidTime(this.riseTime)) {
            newAttributes.put(HazardConstants.RISE_ABOVE,
                    HazardConstants.MISSING_VALUE);
        } else {
            newAttributes.put(HazardConstants.RISE_ABOVE, this.riseTime);
        }

        if (!isValidTime(this.crestTime)) {
            newAttributes.put(HazardConstants.CREST,
                    HazardConstants.MISSING_VALUE);
            /*
             * Should also set the either obs or forecast crest time here, but
             * which one? Since it is missing, we can,t compare with
             * currentTime.
             */
        } else {
            newAttributes.put(HazardConstants.CREST, this.crestTime);

            // Also need to update either the obs or forecast crest time
            if (crestTime > SimulatedTime.getSystemTime().getMillis()) {
                newAttributes.put(HazardConstants.CREST_TIME_FORECAST,
                        this.crestTime);
            } else {
                newAttributes.put(HazardConstants.CREST_TIME_OBSERVED,
                        this.crestTime);
            }
        }

        Date beginDate = convertTimeToDate(beginTime);
        if (beginDate == null) {
            this.event.setStartTime(new Date(HazardConstants.MISSING_VALUE));
        } else {
            this.event.setStartTime(beginDate);
        }

        Date endDate = convertTimeToDate(endTime);
        if (endDate == null) {
            this.event.setEndTime(new Date(
                    HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS));
            newAttributes.put(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    true);
        } else {
            this.event.setEndTime(endDate);
            newAttributes.put(
                    HazardConstants.HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE,
                    false);
        }

        Double crestValue = Double.valueOf(HazardConstants.MISSING_VALUE);
        if (this.crestValTxt.getText().isEmpty() == false) {
            crestValue = Double.valueOf(this.crestValTxt.getText());
        }

        if (crestTime > SimulatedTime.getSystemTime().getMillis()) {
            newAttributes.put(HazardConstants.CREST_STAGE_FORECAST, crestValue);
            newAttributes.put(HazardConstants.CREST_STAGE,
                    Math.max(crestValue, (Double) attributes
                            .get(HazardConstants.CREST_STAGE_OBSERVED)));
            if (graphData.isCrestValueMaxForecastValue()) {
                newAttributes.put(HazardConstants.MAX_FORECAST_STAGE,
                        crestValue);
                newAttributes.put(HazardConstants.MAX_FORECAST_TIME,
                        this.crestTime);
            }
        } else {
            newAttributes.put(HazardConstants.CREST_STAGE_OBSERVED, crestValue);
            newAttributes.put(HazardConstants.CREST_STAGE,
                    Math.max(crestValue, (Double) attributes
                            .get(HazardConstants.CREST_STAGE_FORECAST)));
        }

        this.event.setHazardAttributes(newAttributes);

        setReturnValue(event);
        return true;
    }

    /**
     * Get selected from AWIPS calendar
     * 
     * @param time
     * @return newTime or time if selection canceled.
     */
    private long setTime(long time) {
        long t = TimeUtil.currentTimeMillis();
        if (isValidTime(time)) {
            t = time;
        } else {
            time = HazardConstants.MISSING_VALUE;
        }
        Date date = new Date(t);
        CalendarDialog calendarDlg = new CalendarDialog(this.shell, date, 2);
        Object obj = calendarDlg.open();
        if (obj != null) {
            time = ((Date) obj).getTime();
            if (time == 0) {
                time = HazardConstants.MISSING_VALUE;
            }
        }

        return time;
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
            switch (eventType) {
            case BEGIN:
                this.beginTimeTxt.setText(dateFormat.format(date));
                this.beginTime = date.getTime();
                break;
            case RISE:
                this.riseTxt.setText(dateFormat.format(date));
                this.riseTime = date.getTime();
                break;
            case CREST:
                this.crestTxt.setText(dateFormat.format(date));
                this.crestTime = date.getTime();
                break;
            case FALL:
                this.fallTxt.setText(dateFormat.format(date));
                this.fallTime = date.getTime();
                break;
            case END:
                this.endTimeTxt.setText(dateFormat.format(date));
                this.endTime = date.getTime();
                break;

            default:
                statusHandler.error("Ignore unknown event type: " + eventType);
                break;
            }
        }
    }

    private boolean verifyDialogInput() {
        boolean isValid = true;

        StringBuffer errMsgSB = new StringBuffer();
        Date beginDate = convertTimeToDate(beginTime);
        Date endDate = convertTimeToDate(endTime);

        if (beginDate == null) {
            isValid = false;
            invalidTimeHeader(errMsgSB);
            errMsgSB.append("Bad formatted or missing Start time.\n");
        }

        if ((endDate == null)
                && (endTime != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS)) {
            invalidTimeHeader(errMsgSB);
            errMsgSB.append(endTimeLblTxt);
            errMsgSB.append(": ");
            errMsgSB.append(endTimeTxt.getText());
            errMsgSB.append("\nBad formatted or missing End time.\n");
            isValid = false;
        }

        if (beginDate != null) {
            Calendar currentCal = TimeUtil.newCalendar();
            currentCal.set(Calendar.SECOND, 0);
            currentCal.set(Calendar.MILLISECOND, 0);
            if (!HazardStatus.hasEverBeenIssued(event.getStatus())) {
                // Not issued yet, just set it to current time.
                beginDate = currentCal.getTime();
                beginTime = beginDate.getTime();
                event.setStartTime(beginDate);
                setBeginTime(beginTime);
            }
        }

        if ((endDate != null) && (beginDate != null)) {
            if (endDate.before(beginDate)) {
                invalidTimeHeader(errMsgSB);
                errMsgSB.append(endTimeLblTxt);
                errMsgSB.append(": ");
                errMsgSB.append(endTimeTxt.getText());
                errMsgSB.append("\nEnd Time must be after StartTime.\n");
                isValid = false;
            }
        }

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

    /**
     * Convince method to add the header only one time.
     * 
     * @param errMsgSB
     */
    private void invalidTimeHeader(StringBuffer errMsgSB) {
        if (errMsgSB.length() == 0) {
            errMsgSB.append("Invalid date time:\n");
            errMsgSB.append(beginTimeLblTxt);
            errMsgSB.append(": ");
            errMsgSB.append(beginTimeTxt.getText());
            errMsgSB.append("\n");
        }
    }

    private void setBeginTime(long time) {
        if (isValidTime(time)) {
            Date date = convertTimeToDate(time);
            beginTimeTxt.setText(dateFormat.format(date));
            displayCanvas.setDate(EventType.BEGIN, date);
            beginTime = time;
            beginSetRdo.setSelection(true);
            beginCurrentRdo.setSelection(false);
        }
    }

    /**
     * 
     * @param time
     * @return if valid time's date else null
     */
    private Date convertTimeToDate(long time) {
        Date date = null;
        if (isValidTime(time)) {
            date = TimeUtil.newDate();
            date.setTime(time);
        }
        return date;
    }

    /**
     * 
     * @param time
     * @return true when time is valid
     */
    private boolean isValidTime(long time) {
        return (time != HazardConstants.MIN_TIME)
                && (time != HazardConstants.MISSING_VALUE)
                && (time != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS);
    }
}
