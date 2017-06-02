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

import gov.noaa.gsd.common.hazards.utilities.hazardservices.StageDischargeUtils;
import gov.noaa.gsd.viz.hazards.risecrestfall.EventRegion.EventType;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.hydro.RiverHydroConstants;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.uf.common.time.util.TimeUtil;
import com.raytheon.uf.viz.core.RGBColors;

/**
 * Drawing canvas for the rise/crest/fall editor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 17, 2015    3847    mpduff     Initial creation
 * Mar 13, 2015    6922    Chris.Cody Changes for dragging vertical graph lines
 * Mar 17, 2015    6974    mpduff      FAT fixes.
 * Mar 26, 2015    7205    Robert.Blum Added discharge to graph.
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class EditorCanvas extends Canvas {

    private final Composite parentComp;

    private final int LINE_SPACING = 3;

    private final int HORIZONTAL_SPACING = 13;

    /**
     * Canvas width.
     */
    private int canvasWidth = 850;

    /**
     * Canvas height.
     */
    private int canvasHeight = 400;// 675;

    private final int GRAPHBORDER_RIGHT = 60;

    private final int GRAPHBORDER_LEFT = 60;

    private final int GRAPHBORDER_BOTTOM = 50;

    private final int GRAPHBORDER_TOP = 60;

    /**
     * Graph Area Width in pixels.
     */
    private int graphAreaWidth = canvasWidth - GRAPHBORDER_LEFT
            + GRAPHBORDER_RIGHT;

    /**
     * Graph Area Height in pixels.
     */
    private int graphAreaHeight = canvasHeight - GRAPHBORDER_BOTTOM
            - GRAPHBORDER_TOP;

    private Font boldFont;

    private Font normalFont;

    private int fontHeight = -999;

    private GraphData graphData;

    private final Cursor eastWestCursor;

    private boolean mouseDown;

    private final int regionWidth = 12;

    private Color yellow;

    private Color orange;

    private Color red;

    private Color purple;

    private Color begin;

    private Color rise;

    private Color crest;

    private Color fall;

    private Color end;

    private long xMin;

    private long xMax;

    private double yMin;

    private double yMax;

    private ScaleManager scaleMgr;

    private final IDataUpdate dataUpdate;

    /** Graph Popup Context menu. */
    private Menu graphPopupMenu;

    /**
     * Show or hide Graph Point Data flag.
     */
    private Boolean showGraphPointData = Boolean.FALSE;

    /**
     * Graph Point Data Text: Date Time, Stage.
     */
    private String graphPointDataText = "";

    /**
     * Current graph canvas pointer data X coordinate.
     */
    private int graphPointDataX = -1;

    /**
     * Current graph canvas pointer data Y coordinate.
     */
    private int graphPointDataY = -1;

    private NumberFormat formatter = new DecimalFormat("0");

    /**
     * Date Formatter used for display fields and graph pointer data.
     */
    private SimpleDateFormat dateFormat = null;

    /**
     * Last X coordinate of Canvas Region drag action.
     */
    private int lastX = Integer.MIN_VALUE;

    /**
     * Last Y coordinate of Canvas Region drag action. Here for completeness.
     */
    private int lastY = Integer.MIN_VALUE;

    /**
     * Event Region for existing mouse drag.
     */
    private EventRegion dragEventRegion = null;

    /** Flag for existence of rating curve */
    private boolean ratingCurveExist = false;

    public EditorCanvas(Composite parent, GraphData graphData,
            IDataUpdate dataUpdate, int style) {
        super(parent, style);
        parentComp = parent;
        this.graphData = graphData;
        eastWestCursor = parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZEWE);
        this.dataUpdate = dataUpdate;
        init();
    }

    private void init() {
        yellow = new Color(this.getDisplay(), RGBColors.getRGBColor("Yellow"));
        orange = new Color(this.getDisplay(), RGBColors.getRGBColor("Orange"));
        red = new Color(this.getDisplay(), RGBColors.getRGBColor("Red"));
        purple = new Color(this.getDisplay(), RGBColors.getRGBColor("Purple"));

        begin = parentComp.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        rise = parentComp.getDisplay().getSystemColor(SWT.COLOR_DARK_YELLOW);
        crest = parentComp.getDisplay().getSystemColor(SWT.COLOR_RED);
        fall = parentComp.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
        end = parentComp.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);

        xMin = SimulatedTime.getSystemTime().getMillis()
                - TimeUtil.MILLIS_PER_DAY;
        xMax = xMin + TimeUtil.MILLIS_PER_DAY * 7;
        yMin = graphData.getyMin();
        yMax = graphData.getyMax();

        scaleMgr = new ScaleManager(yMin, yMax);
        yMin = scaleMgr.getMinScaleValue();
        yMax = scaleMgr.getMaxScaleValue();

        GridLayout gl = new GridLayout(1, false);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = canvasWidth;
        gd.heightHint = canvasHeight;
        this.setLayout(gl);
        this.setLayoutData(gd);
        this.setSize(canvasWidth, canvasHeight);

        createEventLines();

        boldFont = new Font(parentComp.getDisplay(), "Monospace", 10,
                SWT.NORMAL | SWT.BOLD);

        normalFont = new Font(parentComp.getDisplay(), "Monospace", 9,
                SWT.NORMAL);

        addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                drawCanvas(e.gc);
            }
        });

        addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if ((boldFont != null) && (!boldFont.isDisposed())) {
                    boldFont.dispose();
                }

                if (normalFont != null && !normalFont.isDisposed()) {
                    normalFont.dispose();
                }
            }
        });

        addMouseListener(new MouseListener() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Intentionally blank
            }

            @Override
            public void mouseDown(MouseEvent e) {
                handleMouseDownEvent(e);
            }

            @Override
            public void mouseUp(MouseEvent e) {
                handleMouseUpEvent(e);
            }
        });

        addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                handleMouseMoveEvent(e);
            }
        });

        parentComp.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                resizeGraph(parentComp.getClientArea());
                redraw();
            }
        });

        this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected void handleMouseUpEvent(MouseEvent e) {

        int buttonReleased = e.button;

        if (buttonReleased == 1) {
            mouseDown = false;
            dragEventRegion = null;
            setCursor(null);
        }
    }

    protected void handleMouseDownEvent(MouseEvent e) {
        if (e.button == 1) {
            mouseDown = true;
            dragEventRegion = null;
        } else if (e.button == 3) {
            createGraphPopupMenu(this);
        }
    }

    protected void handleMouseMoveEvent(MouseEvent e) {

        int curX = e.x;
        int curY = e.y;
        displayGraphPointData(curX, curY);

        if (mouseDown == true) {
            if (dragEventRegion == null) {
                lastX = Integer.MIN_VALUE;
                lastY = Integer.MIN_VALUE;
                dragEventRegion = grabEventRegion(curX, curY);
                if (dragEventRegion != null) {
                    setCursor(eastWestCursor);
                    lastX = curX;
                    lastY = curY;
                } else {
                    setCursor(null);
                }
            }

            if (this.dragEventRegion != null) {
                // Did we move in the X axis
                if (lastX != curX) {
                    Region r = new Region();
                    r.add(curX - (regionWidth / 2), GRAPHBORDER_TOP,
                            regionWidth, graphAreaHeight);
                    dragEventRegion.setRegion(r);

                    lastX = curX;
                    lastY = curY;
                    Date date = pixel2x(curX - GRAPHBORDER_LEFT);
                    dragEventRegion.setDate(date);
                    dataUpdate.setDate(dragEventRegion.getEventType(), date);
                    redraw();
                }
            }
        } else {
            // Mouse is up
            EventRegion er = grabEventRegion(curX, curY);
            if (er != null) {
                setCursor(eastWestCursor);
            } else {
                setCursor(null);
            }
        }
    }

    /**
     * Grab the Event Region in the Graph Data for the given coordinate.
     * 
     * @param x
     *            Horizontal axis screen coordinate
     * @param y
     *            Vertical axis screen coordinate
     * @return Event Region that X and Y are inside of or Null
     */
    private EventRegion grabEventRegion(int x, int y) {
        EventRegion grabbedEr = null;

        for (Entry<EventType, EventRegion> entry : graphData.getEventRegions()
                .entrySet()) {
            EventRegion er = entry.getValue();
            if (er.getRegion() != null && er.getRegion().contains(x, y)) {
                grabbedEr = er;
                break;
            }
        }

        return (grabbedEr);
    }

    /**
     * Create the table popup menu.
     * 
     * @param parent
     *            Parent control.
     */
    private void createGraphPopupMenu(Control parent) {
        if (graphPopupMenu != null) {
            graphPopupMenu.dispose();
        }

        graphPopupMenu = new Menu(parent);
        MenuItem displayGraphDataMenuItemCB = new MenuItem(graphPopupMenu,
                SWT.CHECK);
        displayGraphDataMenuItemCB.setText("Display Graph Data");
        displayGraphDataMenuItemCB.setSelection(this.showGraphPointData);
        displayGraphDataMenuItemCB.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleDisplayGraphDataCheckbox();
            }
        });
        parent.setMenu(graphPopupMenu);
        graphPopupMenu.setVisible(true);
    }

    private void handleDisplayGraphDataCheckbox() {
        this.showGraphPointData = !this.showGraphPointData;
        this.redraw();
    }

    /**
     * Resize the graph when the window size changes
     * 
     * @param rect
     *            The new graph size
     */
    protected void resizeGraph(Rectangle rect) {
        canvasHeight = rect.height;
        canvasWidth = rect.width;
        graphAreaWidth = canvasWidth - GRAPHBORDER_LEFT - GRAPHBORDER_RIGHT;
        graphAreaHeight = canvasHeight - GRAPHBORDER_BOTTOM - GRAPHBORDER_TOP;
        this.redraw();
    }

    protected void drawCanvas(GC gc) {
        gc.setFont(boldFont);

        if (fontHeight == -999) {
            fontHeight = (gc.getFontMetrics().getHeight());
        }
        int fontAveWidth = gc.getFontMetrics().getAverageCharWidth();

        // Draw graph border
        gc.setForeground(parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_WHITE));
        gc.drawRectangle(GRAPHBORDER_LEFT, GRAPHBORDER_TOP, canvasWidth
                - GRAPHBORDER_LEFT - GRAPHBORDER_RIGHT, canvasHeight
                - GRAPHBORDER_TOP - GRAPHBORDER_BOTTOM);

        if (graphData != null) {
            // Draw the title text
            String s = graphData.getLabel();
            int x = (canvasWidth / 2) - HORIZONTAL_SPACING
                    - ((s.length() * fontAveWidth) / 2);
            int y = LINE_SPACING;
            gc.drawString(s, x, y);

            // PE line
            StringBuilder sb = new StringBuilder("PE/DUR/TS: ");
            sb.append(graphData.getPe()).append(graphData.getDur())
                    .append(graphData.getObservedTs()).append("   ");
            sb.append(graphData.getPe()).append(graphData.getDur())
                    .append(graphData.getForecastTs()).append("   ");

            x = (canvasWidth / 2) - HORIZONTAL_SPACING
                    - ((sb.length() * fontAveWidth) / 2);
            y = fontHeight + LINE_SPACING;
            gc.drawString(sb.toString(), x, y);
        }

        // Draw axis lines
        drawYAxis(gc);
        drawXAxis(gc);

        // Draw flood cat lines
        drawFloodCatLines(gc);

        /* Draw reference vertical line at present time */
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int curTimeLoc = GRAPHBORDER_LEFT + x2pixel(cal.getTimeInMillis());
        int[] curTimeLine = { curTimeLoc, GRAPHBORDER_TOP, curTimeLoc,
                GRAPHBORDER_TOP + graphAreaHeight };
        gc.setForeground(parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_WHITE));
        gc.setLineWidth(3);
        gc.drawPolyline(curTimeLine);
        gc.setLineStyle(SWT.LINE_SOLID);

        createEventLines();
        drawEventLines(gc);

        gc.setLineWidth(2);
        gc.setForeground(parentComp.getDisplay().getSystemColor(
                SWT.COLOR_YELLOW));
        gc.setBackground(parentComp.getDisplay().getSystemColor(
                SWT.COLOR_YELLOW));

        // Draw data
        List<GraphPoint> observedPoints = graphData.getObservedPointList();
        int[] observedPointArray = new int[observedPoints.size() * 2];
        int pointArrayIdx = -1;
        for (int i = 0; i < observedPoints.size(); i++) {
            GraphPoint point = observedPoints.get(i);
            int xPix = x2pixel(point.getX().getTime());
            xPix += GRAPHBORDER_LEFT;
            int yPix = y2pixel(point.getY());
            yPix += GRAPHBORDER_TOP;
            observedPointArray[++pointArrayIdx] = xPix;
            observedPointArray[++pointArrayIdx] = yPix;
            gc.fillOval(xPix - 2, yPix - 2, 4, 4);
        }

        gc.drawPolyline(observedPointArray);

        gc.setForeground(parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_GREEN));
        gc.setBackground(parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_GREEN));

        List<GraphPoint> forecastPoints = graphData.getForecastPointList();
        int[] forecastPointArray = new int[forecastPoints.size() * 2];
        pointArrayIdx = -1;
        for (int i = 0; i < forecastPoints.size(); i++) {
            GraphPoint point = forecastPoints.get(i);
            int xPix = x2pixel(point.getX().getTime());
            xPix += GRAPHBORDER_LEFT;
            int yPix = y2pixel(point.getY());
            yPix += GRAPHBORDER_TOP;
            forecastPointArray[++pointArrayIdx] = xPix;
            forecastPointArray[++pointArrayIdx] = yPix;
            gc.fillOval(xPix - 2, yPix - 2, 4, 4);
        }

        gc.drawPolyline(forecastPointArray);

        gc.setBackground(parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_BLACK));

        if (this.showGraphPointData == true) {
            if ((this.graphPointDataX > 0) && (this.graphPointDataY > 0)) {
                Font f = new Font(this.getDisplay(), "Monospace", 14, SWT.BOLD);
                gc.setForeground(this.getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                gc.drawText(this.graphPointDataText, this.graphPointDataX,
                        this.graphPointDataY, false);
                f.dispose();
            }
        }
    }

    private void drawYAxis(GC gc) {
        gc.setFont(normalFont);
        int dx = 8;
        double yDiff = yMax - yMin;
        int xoffset = 40;
        if (yDiff < 1.0) {
            this.formatter = new DecimalFormat("0.00");
        } else {
            this.formatter = new DecimalFormat("0.0");
        }

        int numberTicks = scaleMgr.getMajorTickCount();
        double minScaleVal = scaleMgr.getMinScaleValue();

        double inc = scaleMgr.getMajorTickIncrement();

        int y = 0;
        double tickVal = minScaleVal;

        ratingCurveExist = false;

        /* Maximum discharge value */
        double maxDischarge = -999.9;

        String pe = graphData.getPe().toUpperCase();
        boolean isStageValue = (pe.startsWith("Q") == false);

        /* Does a rating table exist for this site? */
        String lid = graphData.getLid();
        if (!ratingCurveExist && (pe.startsWith("H") || pe.startsWith("Q"))) {
            ratingCurveExist = StageDischargeUtils.checkRatingTable(lid);
        }

        int labelX = GRAPHBORDER_LEFT - xoffset;
        int labelX2 = GRAPHBORDER_LEFT + graphAreaWidth + 15;

        for (int i = 0; i < numberTicks; i++) {
            y = y2pixel(tickVal);
            if (i > 0 && i < numberTicks - 1) {
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.setForeground(parentComp.getDisplay().getSystemColor(
                        SWT.COLOR_DARK_GRAY));
                gc.setLineStyle(SWT.LINE_DOT);
                gc.drawPolyline(gridLine);
                gc.setLineStyle(SWT.LINE_SOLID);
                gc.setForeground(parentComp.getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
            }

            double dischargeValue = 0.0;
            double stageValue = 0.0;

            // Determine the stage and discharge values
            if (isStageValue) {
                stageValue = tickVal;
                if (ratingCurveExist) {
                    maxDischarge = StageDischargeUtils.stage2discharge(lid,
                            scaleMgr.getMaxScaleValue());
                    dischargeValue = StageDischargeUtils.stage2discharge(lid,
                            tickVal);
                    if ((dischargeValue < 0.0)) {
                        dischargeValue = 0.0;
                    } else if (maxDischarge >= 10000.0) {
                        dischargeValue /= 1000.0;
                    }
                }
            } else if (pe.startsWith("Q")) {
                dischargeValue = tickVal;
                maxDischarge = scaleMgr.getMaxScaleValue();
                if (ratingCurveExist) {
                    double value = StageDischargeUtils.discharge2stage(
                            graphData.getLid(), tickVal);
                    if (value != RiverHydroConstants.MISSING_VALUE) {
                        stageValue = value;
                    } else {
                        break;
                    }
                }
            }

            /* Draw the tick marks and left axis values */
            int labelY = GRAPHBORDER_TOP + y - 8;
            int[] tick = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                    GRAPHBORDER_LEFT - dx, GRAPHBORDER_TOP + y };
            gc.drawPolyline(tick);
            if (isStageValue == false && ratingCurveExist == false) {
                // Only have the discharge value, so it goes on left Y axis
                gc.drawText("" + formatter.format(dischargeValue), labelX,
                        labelY);
            } else {
                gc.drawText("" + formatter.format(stageValue), labelX, labelY);
            }

            /* Draw the tick marks and right axis values if rating curve exists */
            if (ratingCurveExist) {
                int[] tick2 = { GRAPHBORDER_LEFT + graphAreaWidth,
                        GRAPHBORDER_TOP + y,
                        GRAPHBORDER_LEFT + graphAreaWidth + dx,
                        GRAPHBORDER_TOP + y };
                gc.drawPolyline(tick2);
                gc.drawText("" + formatter.format(dischargeValue), labelX2,
                        labelY);
            }
            tickVal += inc;
        }

        // Label the left axis
        labelLeftYAxis(gc, maxDischarge);
        if (ratingCurveExist) {
            // Label the right axis
            labelRightYAxis(gc, maxDischarge);
        }
    }

    /**
     * Label the left y axis.
     */
    private void labelLeftYAxis(GC gc, double maxDischarge) {
        String label = "";
        if (graphData.getPe().toUpperCase().startsWith("Q")
                && ratingCurveExist == false) {
            if (maxDischarge >= 10000.0) {
                label = "KCFS";
            } else {
                label = "CFS";
            }
        } else {
            label = "Feet";
        }
        // Draw the label on the graph
        gc.drawText(label, GRAPHBORDER_LEFT - 40, GRAPHBORDER_TOP - 25, true);
    }

    /**
     * Label the right y axis.
     */
    private void labelRightYAxis(GC gc, double maxDischarge) {
        String label = "";
        if (maxDischarge >= 10000.0) {
            label = "KCFS";
        } else {
            label = "CFS";
        }
        // Draw the label on the graph
        gc.drawText(label, GRAPHBORDER_LEFT + graphAreaWidth + 15,
                GRAPHBORDER_TOP - 25, true);
    }

    private void drawXAxis(GC gc) {
        int x = -999;
        int dy = 5;
        int dx = 4;

        gc.setFont(normalFont);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        for (long i = xMin; i <= xMax; i += TimeUtil.MILLIS_PER_HOUR) {
            dy = 5; // reset here for next iteration
            x = x2pixel(i + 59000); // 59 seconds for better sampling
            c.setTimeInMillis(i);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour == 0 || hour == 6 || hour == 12 || hour == 18) {
                dy = 12;
                if (hour == 0) {
                    // only draw date text on 00Z
                    gc.drawText(
                            c.get(Calendar.MONTH) + 1 + "/"
                                    + c.get(Calendar.DAY_OF_MONTH), x
                                    + GRAPHBORDER_LEFT - 8, graphAreaHeight
                                    + GRAPHBORDER_TOP + 30);
                }
                // Draw larger tick for 0Z
                int[] tickArray = { x + GRAPHBORDER_LEFT,
                        GRAPHBORDER_TOP + graphAreaHeight,
                        x + GRAPHBORDER_LEFT,
                        graphAreaHeight + GRAPHBORDER_TOP + dy };
                gc.drawPolyline(tickArray);
            } else {
                // Draw ticks
                int[] tickArray = { x + GRAPHBORDER_LEFT,
                        GRAPHBORDER_TOP + graphAreaHeight,
                        x + GRAPHBORDER_LEFT,
                        graphAreaHeight + GRAPHBORDER_TOP + dy };
                gc.drawPolyline(tickArray);
            }

            if (hour % 6 == 0) {
                // Draw grid lines
                gc.setForeground(parentComp.getDisplay().getSystemColor(
                        SWT.COLOR_DARK_GRAY));
                gc.setLineStyle(SWT.LINE_DOT);
                gc.drawLine(x + GRAPHBORDER_LEFT, GRAPHBORDER_TOP, x
                        + GRAPHBORDER_LEFT, GRAPHBORDER_TOP + graphAreaHeight);
                gc.setLineStyle(SWT.LINE_SOLID);
                String hr = String.valueOf(hour);
                if (hour < 9) {
                    hr = "0" + hour;
                }
                gc.setForeground(parentComp.getDisplay().getSystemColor(
                        SWT.COLOR_WHITE));
                gc.drawText(hr + "Z", x + GRAPHBORDER_LEFT - dx,
                        graphAreaHeight + GRAPHBORDER_TOP + 15);
            }
        }
    }

    private void drawFloodCatLines(GC gc) {
        int y = 0;
        gc.setLineWidth(2);
        gc.setLineStyle(SWT.LINE_SOLID);

        /* Action stage/flow */
        if (graphData.getActionStage() != RiverHydroConstants.MISSING_VALUE) {
            y = y2pixel(graphData.getActionStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(yellow);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Flood stage/flow */
        if (graphData.getFloodStage() != RiverHydroConstants.MISSING_VALUE) {
            y = y2pixel(graphData.getFloodStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(orange);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Moderate stage/flow */
        if (graphData.getModerateStage() != RiverHydroConstants.MISSING_VALUE) {
            y = y2pixel(graphData.getModerateStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(red);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Major stage/flow */
        if (graphData.getMajorStage() != RiverHydroConstants.MISSING_VALUE) {
            y = y2pixel(graphData.getMajorStage());
            gc.setForeground(purple);
            int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                    GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
            gc.drawPolyline(gridLine);
        }
    }

    private void createEventLines() {
        int top = GRAPHBORDER_TOP;
        Map<EventType, EventRegion> regionMap = graphData.getEventRegions();

        Date beginDate = graphData.getBeginDate();
        Date endDate = graphData.getEndDate();

        EventRegion er = regionMap.get(EventType.BEGIN);
        Region r = er.getRegion();
        if (r == null) {
            r = new Region();
        }
        int x = x2pixel(beginDate.getTime());
        x += GRAPHBORDER_LEFT;
        r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                graphAreaHeight);
        er.setRegion(r);
        er.setEventColor(begin);
        er.setDate(beginDate);

        er = regionMap.get(EventType.RISE);
        er.setEventColor(rise);
        r = er.getRegion();
        if (r == null) {
            r = new Region();
        }
        Date riseDate = graphData.getRiseDate();
        if (riseDate != null && riseDate.after(beginDate)
                && riseDate.after(endDate)) {
            x = x2pixel(riseDate.getTime());
            x += GRAPHBORDER_LEFT;
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(riseDate);
        }

        er = regionMap.get(EventType.CREST);
        er.setEventColor(crest);
        r = er.getRegion();
        if (r == null) {
            r = new Region();
        }
        Date crestDate = graphData.getCrestDate();
        if (crestDate != null) {
            x = x2pixel(crestDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(crestDate);
        }

        er = regionMap.get(EventType.FALL);
        er.setEventColor(fall);
        r = er.getRegion();
        if (r == null) {
            r = new Region();
        }
        Date fallDate = graphData.getFallDate();
        if (fallDate != null) {
            x = x2pixel(fallDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(fallDate);
        }

        er = regionMap.get(EventType.END);
        er.setEventColor(end);
        r = er.getRegion();
        if (r == null) {
            r = new Region();
        }
        if (endDate != null
                && endDate.getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            x = x2pixel(endDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(endDate);
        }
    }

    private void drawEventLines(GC gc) {
        int top = GRAPHBORDER_TOP;
        int bottom = GRAPHBORDER_TOP + graphAreaHeight;
        int offset = 20;
        gc.setLineWidth(3);
        gc.setFont(this.boldFont);
        for (Entry<EventType, EventRegion> entry : graphData.getEventRegions()
                .entrySet()) {
            EventRegion er = entry.getValue();
            if (er.getDate() != null) {
                long regionTime = er.getDate().getTime();
                if (er.isVisible() && regionTime > xMin && regionTime < xMax) {
                    int x = x2pixel(regionTime);
                    x += GRAPHBORDER_LEFT;
                    gc.setForeground(er.getEventColor());
                    gc.drawLine(x, top, x, bottom);
                    gc.drawText(er.getAbbreviation(), x, top - offset);
                    Region r = new Region();
                    r.add(x - (regionWidth / 2), top, regionWidth,
                            graphAreaHeight);
                    er.setRegion(r);
                }
            }
        }
        gc.setLineWidth(1);
        gc.setFont(this.normalFont);
    }

    /**
     * Does not account for the left border shift
     * 
     * @param x
     *            millisecond value
     * @return
     */
    private int x2pixel(long x) {
        long xDiff = xMax - xMin;
        long millisPerPixel = xDiff / graphAreaWidth;
        float xValue = (x - xMin) / millisPerPixel;

        return Math.round(xValue);
    }

    private int y2pixel(double val) {
        if (val == RiverHydroConstants.MISSING_VALUE) {
            return graphAreaHeight + GRAPHBORDER_TOP;
        }

        double yValue = (graphAreaHeight / (yMax - yMin) * (val - yMin));

        return (int) (graphAreaHeight - Math.round(yValue));
    }

    /**
     * Convert Canvas Graph Point X value to Graph Stage value.
     * 
     * @param ypix
     *            Current Graph Canvas Mouse Pointer X coordinate
     * @return Flood Stage value at Pixel X
     */
    protected Date pixel2x(int xpix) {
        long xDiff = xMax - xMin;
        double millisPerPixel = xDiff / graphAreaWidth;
        long millisTime = (long) (xpix * millisPerPixel) + xMin;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millisTime);

        return cal.getTime();
    }

    /**
     * Convert Canvas Graph Point Y value to Graph Stage value.
     * 
     * @param ypix
     *            Current Graph Canvas Mouse Pointer Y coordinate
     * @return Flood Stage value at Pixel Y
     */
    protected double pixel2y(int ypix) {
        double yDiff = yMax - yMin;
        double pixelPerUnit = graphAreaHeight / yDiff;
        double stageValue = (yMax - ((ypix - GRAPHBORDER_TOP) / pixelPerUnit));

        return (stageValue);
    }

    /**
     * Set Current Graph Point Data and call redraw.
     * 
     * @param xpix
     *            Current Graph Canvas Mouse Pointer X coordinate
     * @param ypix
     *            Current Graph Canvas Mouse Pointer Y coordinate
     */
    private void displayGraphPointData(int xpix, int ypix) {

        if (this.showGraphPointData == false) {
            return;
        }

        int graphXLeft = 0 + GRAPHBORDER_LEFT;
        int graphXRight = canvasWidth - GRAPHBORDER_RIGHT;
        int graphYTop = 0 + GRAPHBORDER_TOP;
        int graphYBottom = canvasHeight - GRAPHBORDER_BOTTOM;

        String xVal = "";
        String yVal = "";
        if ((xpix >= graphXLeft) && (xpix <= graphXRight) && (ypix > graphYTop)
                && (ypix < graphYBottom)) {
            Date xValDate = this.pixel2x(xpix - GRAPHBORDER_LEFT);
            xVal = this.dateFormat.format(xValDate);
            double yValDbl = this.pixel2y(ypix);
            yVal = formatter.format(yValDbl);
            String labelText = "Time: " + xVal;

            if (graphData.getPe().toUpperCase().startsWith("Q")) {
                if (ratingCurveExist == false) {
                    // Only discharge data available
                    labelText += "\nDischarge: " + yVal;
                } else {
                    double stageVal = StageDischargeUtils.discharge2stage(
                            graphData.getLid(), yValDbl);
                    labelText += "\nStage: " + formatter.format(stageVal);
                    labelText += "\nDischarge: " + yVal;
                }
            } else {
                if (ratingCurveExist == false) {
                    // Only Stage data available
                    labelText += "\nStage: " + yVal;
                } else {
                    double dischargeVal = StageDischargeUtils.stage2discharge(
                            graphData.getLid(), yValDbl);
                    if ((dischargeVal < 0.0)) {
                        dischargeVal = 0.0;
                    } else if (dischargeVal >= 10000.0) {
                        dischargeVal /= 1000.0;
                    }
                    labelText += "\nStage: " + yVal;
                    labelText += "\nDischarge: "
                            + formatter.format(dischargeVal);
                }
            }

            this.graphPointDataText = labelText;
            this.graphPointDataX = xpix + 12;
            this.graphPointDataY = ypix + 12;
            this.redraw();
        } else {
            if ((this.graphPointDataX != -1) && (this.graphPointDataY != -1)) {
                this.graphPointDataText = "";
                this.graphPointDataX = -1;
                this.graphPointDataY = -1;
                this.redraw();
            }
        }
    }

    public void setGraphData(GraphData graphData) {
        this.graphData = graphData;
    }

    @Override
    public void dispose() {
        if (this.yellow != null && !yellow.isDisposed()) {
            this.yellow.dispose();
        }
        if (this.orange != null && !yellow.isDisposed()) {
            this.orange.dispose();
        }
        if (this.red != null && !yellow.isDisposed()) {
            this.red.dispose();
        }
        if (this.purple != null && !yellow.isDisposed()) {
            this.purple.dispose();
        }
        if (boldFont != null && !boldFont.isDisposed()) {
            boldFont.dispose();
        }
        if (normalFont != null && !normalFont.isDisposed()) {
            normalFont.dispose();
        }

        if (graphData != null) {
            graphData.dispose();
        }
    }

    public void setVisible(EventType type, boolean visible) {
        EventRegion er = graphData.getEventRegions().get(type);
        er.setVisible(visible);
        redraw();
    }

    public void setDate(EventType eventType, Date date) {
        graphData.getEventRegions().get(eventType).setDate(date);
        redraw();
    }
}