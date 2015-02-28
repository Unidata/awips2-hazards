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

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.hazards.hydro.RiverForecastPoint;
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
 * 
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

    private long xMin;

    private long xMax;

    private double yMin;

    private double yMax;

    private ScaleManager scaleMgr;

    private final IDataUpdate dataUpdate;

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
    }

    protected void handleMouseUpEvent(MouseEvent e) {
        this.mouseDown = false;
    }

    protected void handleMouseDownEvent(MouseEvent e) {
        this.mouseDown = true;
    }

    protected void handleMouseMoveEvent(MouseEvent e) {
        for (Entry<EventType, EventRegion> entry : graphData.getEventRegions()
                .entrySet()) {
            EventRegion er = entry.getValue();
            if (er.getRegion() != null && er.getRegion().contains(e.x, e.y)) {
                setCursor(eastWestCursor);
                if (mouseDown) {
                    // Move the line
                    Date date = pixel2x(e.x - GRAPHBORDER_LEFT);
                    er.setDate(date);
                    Region r = new Region();
                    r.add(e.x - (regionWidth / 2), GRAPHBORDER_TOP,
                            regionWidth, graphAreaHeight);
                    er.setRegion(r);
                    redraw();
                    this.dataUpdate.setDate(er.getEventType(), date);
                }
                break;
            } else {
                setCursor(null);
            }
        }
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
        gc.setLineWidth(3);
        gc.drawPolyline(curTimeLine);
        gc.setLineStyle(SWT.LINE_SOLID);

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
    }

    private void drawYAxis(GC gc) {
        gc.setFont(normalFont);
        int dx = 8;
        double yDiff = yMax - yMin;
        int xoffset = 40;
        NumberFormat formatter = new DecimalFormat("0");
        if (yDiff < 1.0) {
            formatter = new DecimalFormat("0.00");
        } else {
            formatter = new DecimalFormat("0.0");
        }

        int numberTicks = scaleMgr.getMajorTickCount();
        double minScaleVal = scaleMgr.getMinScaleValue();

        double inc = scaleMgr.getMajorTickIncrement();

        int y = 0;
        double tickVal = minScaleVal;

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

            /* Draw the tick marks and values */
            int labelX = GRAPHBORDER_LEFT - xoffset;
            int labelY = GRAPHBORDER_TOP + y - 8;
            int[] tick = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                    GRAPHBORDER_LEFT - dx, GRAPHBORDER_TOP + y };
            gc.drawPolyline(tick);
            gc.drawText("" + formatter.format(tickVal), labelX, labelY);
            tickVal += inc;
        }
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
        if (graphData.getActionStage() != GraphData.MISSING) {
            y = y2pixel(graphData.getActionStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(yellow);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Flood stage/flow */
        if (graphData.getFloodStage() != GraphData.MISSING) {
            y = y2pixel(graphData.getFloodStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(orange);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Moderate stage/flow */
        if (graphData.getModerateStage() != GraphData.MISSING) {
            y = y2pixel(graphData.getModerateStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(red);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }

        /* Major stage/flow */
        if (graphData.getMajorStage() != GraphData.MISSING) {
            y = y2pixel(graphData.getMajorStage());
            if (y > GRAPHBORDER_TOP) {
                gc.setForeground(purple);
                int[] gridLine = { GRAPHBORDER_LEFT, GRAPHBORDER_TOP + y,
                        GRAPHBORDER_RIGHT + graphAreaWidth, GRAPHBORDER_TOP + y };
                gc.drawPolyline(gridLine);
            }
        }
    }

    private void createEventLines() {
        int top = GRAPHBORDER_TOP;

        Color begin = parentComp.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        Color rise = parentComp.getDisplay().getSystemColor(
                SWT.COLOR_DARK_YELLOW);
        Color crest = parentComp.getDisplay().getSystemColor(SWT.COLOR_RED);
        Color fall = parentComp.getDisplay().getSystemColor(
                SWT.COLOR_DARK_MAGENTA);
        Color end = parentComp.getDisplay()
                .getSystemColor(SWT.COLOR_DARK_GREEN);

        Map<EventType, EventRegion> regionMap = graphData.getEventRegions();

        Date beginDate = graphData.getBeginDate();
        Date endDate = graphData.getEndDate();

        EventRegion er = regionMap.get(EventType.BEGIN);
        Region r = new Region();
        int x = x2pixel(beginDate.getTime());
        x += GRAPHBORDER_LEFT;
        r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                graphAreaHeight);
        er.setRegion(r);
        er.setEventColor(begin);
        er.setDate(beginDate);

        er = regionMap.get(EventType.RISE);
        er.setEventColor(rise);
        r = new Region();
        Date riseDate = graphData.getRiseDate();
        if (riseDate != null && riseDate.after(beginDate)
                && riseDate.after(endDate)) {
            x = x2pixel(riseDate.getTime());
            x += GRAPHBORDER_LEFT;
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(riseDate);
            er.setVisible(true);
        }

        er = regionMap.get(EventType.CREST);
        er.setEventColor(crest);
        r = new Region();
        Date crestDate = graphData.getCrestDate();
        if (crestDate != null) {
            x = x2pixel(crestDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(crestDate);
            er.setVisible(true);
        }

        er = regionMap.get(EventType.FALL);
        er.setEventColor(fall);
        r = new Region();
        Date fallDate = graphData.getFallDate();
        if (fallDate != null) {
            x = x2pixel(fallDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(fallDate);
            er.setVisible(true);
        }

        er = regionMap.get(EventType.END);
        er.setEventColor(end);
        r = new Region();
        if (endDate != null
                && endDate.getTime() != HazardConstants.UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS) {
            x = x2pixel(endDate.getTime());
            r.add(x + GRAPHBORDER_LEFT - (regionWidth / 2), top, regionWidth,
                    graphAreaHeight);
            er.setRegion(r);
            er.setDate(endDate);
            er.setVisible(true);
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
            long regionTime = er.getDate().getTime();
            if (er.isVisible() && regionTime > xMin && regionTime < xMax) {
                int x = x2pixel(regionTime);
                x += GRAPHBORDER_LEFT;
                gc.setForeground(er.getEventColor());
                gc.drawLine(x, top, x, bottom);
                gc.drawText(er.getAbbreviation(), x, top - offset);
                Region r = new Region();
                r.add(x - (regionWidth / 2), top, regionWidth, graphAreaHeight);
                er.setRegion(r);
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
        if (val == RiverForecastPoint.MISSINGVAL) {
            return graphAreaHeight + GRAPHBORDER_TOP;
        }

        double yValue = (graphAreaHeight / (yMax - yMin) * (val - yMin));

        return (int) (graphAreaHeight - Math.round(yValue));
    }

    protected Date pixel2x(int xpix) {
        long xDiff = xMax - xMin;
        double millisPerPixel = xDiff / graphAreaWidth;
        long millisTime = (long) (xpix * millisPerPixel) + xMin;

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(millisTime);

        return cal.getTime();
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