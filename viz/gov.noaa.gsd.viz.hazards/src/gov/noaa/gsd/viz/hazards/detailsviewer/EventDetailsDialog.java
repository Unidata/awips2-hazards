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
package gov.noaa.gsd.viz.hazards.detailsviewer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.SessionHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * Dialog to view the details of hazard events.
 * 
 * NOTE: This is a fairly quick and dirty implementation, designed for debugging
 * purposes, and does not follow the Model View Presenter architecture. If it
 * ever needs to be made a permanent fixture, it should be refactored to do so.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date            Ticket#    Engineer              Description
 * ------------    ---------- ------------          --------------------------
 * Mar 23, 2017     30537     mpduff                Initial creation
 * Apr 27, 2017     33189     Robert.Blum           Added ability to compare events.
 * Apr 28, 2017     33430     Robert.Blum           Removed HazardMode.
 * May 04, 2017     33778     Robert.Blum           Improvements to event comparing.
 * May 08, 2018     15561     Chris.Golden          Changed BaseHazardEvent to
 *                                                  SessionHazardEvent.
 * </pre>
 * 
 */
public class EventDetailsDialog extends CaveSWTDialog {

    private final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EventDetailsDialog.class);

    /**
     * List of fields that will not be included in the verbose data when
     * displaying or comparing events.
     */
    private final List<String> HAZARD_FIELD_BLACK_LIST = Arrays
            .asList(new String[] { "statusHandler", "undoStack", "redoStack",
                    "geometryFactory", "hazardAreaUpdateTracker" });

    private List<? extends List<? extends IReadableHazardEvent>> events;

    private Map<String, String> data = new HashMap<>();

    private Map<String, String> verboseData = new HashMap<>();

    private Text text = null;

    private Button verboseChk;

    private Combo combo;

    private Button highlightButton;

    /**
     * @param parentShell
     * @param events
     */
    public EventDetailsDialog(Shell parentShell,
            List<? extends List<? extends IReadableHazardEvent>> events) {
        super(parentShell, SWT.DIALOG_TRIM | SWT.RESIZE);
        this.events = events;
        setText("Event Details Viewer");
        loadData();
    }

    @Override
    protected Layout constructShellLayout() {
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 3;
        return mainLayout;
    }

    @Override
    protected Object constructShellLayoutData() {
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, true);
        return gd;
    }

    @Override
    protected void initializeComponents(Shell shell) {
        Composite mainComp = new Composite(shell, SWT.NONE);
        mainComp.setLayout(new GridLayout(1, false));
        mainComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite textComp = new Composite(mainComp, SWT.NONE);
        textComp.setLayout(new GridLayout(1, false));
        textComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        text = new Text(textComp, SWT.MULTI | SWT.BORDER | SWT.WRAP
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.RESIZE | SWT.READ_ONLY);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 400;
        gridData.heightHint = 500;
        text.setLayoutData(gridData);

        createEventChooserArea(mainComp);
        createEventCompareArea(mainComp);
        Composite buttonComp = new Composite(mainComp, SWT.NONE);
        buttonComp.setLayout(new GridLayout(1, false));
        buttonComp.setLayoutData(
                new GridData(SWT.CENTER, SWT.DEFAULT, true, true));

        Button closeBtn = new Button(buttonComp, SWT.PUSH);
        closeBtn.setText("  Close  ");
        closeBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });
    }

    private String formatedTime(Date t) {
        if (t != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH':'mm':'ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String str = sdf.format(t);
            str = str.replace('+', '.');
            str += "Z";
            return str;
        } else {
            return "";
        }
    }

    private void createEventChooserArea(Composite comp) {
        Composite selectionComp = new Composite(comp, SWT.NONE);
        selectionComp.setLayout(new GridLayout(3, true));
        selectionComp
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        combo = new Combo(selectionComp, SWT.SINGLE | SWT.READ_ONLY);
        combo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof Combo) {
                    loadTextField();
                }
            }
        });

        verboseChk = new Button(selectionComp, SWT.CHECK);
        verboseChk.setText("View/Compare Verbose");
        verboseChk.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadTextField();
            }
        });

        Button copyBtn = new Button(selectionComp, SWT.PUSH);
        copyBtn.setText("Copy to Clipboard");
        copyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                copyContents();
            }
        });
        for (String option : getComboOptions()) {
            combo.add(option);
        }
        combo.select(0);
        loadTextField();
    }

    private void createEventCompareArea(Composite mainComp) {
        Composite diffComp = new Composite(mainComp, SWT.NONE);
        diffComp.setLayout(new GridLayout(1, true));
        diffComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

        Group group = new Group(diffComp, SWT.NONE);
        group.setLayout(new GridLayout(2, true));
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        group.setText("Compare Event Versions");

        final Combo event1 = new Combo(group, SWT.SINGLE | SWT.READ_ONLY);
        event1.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        List<String> comboOptions = getComboOptions();
        for (String option : comboOptions) {
            event1.add(option);
        }
        event1.select(0);

        final Combo event2 = new Combo(group, SWT.SINGLE | SWT.READ_ONLY);
        event2.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        for (String option : comboOptions) {
            event2.add(option);
        }
        if (comboOptions.size() > 1) {
            event2.select(1);
        } else {
            event2.select(0);
        }

        highlightButton = new Button(group, SWT.CHECK);
        highlightButton.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));
        highlightButton.setText("Individual Changes");
        highlightButton.setToolTipText(
                "Highlights individual changes within a single line. This can cause performance issues with complex geometries.");

        Button diffBtn = new Button(group, SWT.PUSH);
        diffBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        diffBtn.setText("Compare Events");
        diffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                compareEvents(event1.getText(), event2.getText());
            }
        });
    }

    protected void compareEvents(String combo1, String combo2) {
        String event1Data = getText(combo1);
        String event2Data = getText(combo2);

        CompareEvent event1 = new CompareEvent(combo1, event1Data);
        CompareEvent event2 = new CompareEvent(combo2, event2Data);
        IPreferenceStore prefStore = new CompareConfiguration()
                .getPreferenceStore();
        prefStore.setValue(ComparePreferencePage.HIGHLIGHT_TOKEN_CHANGES,
                highlightButton.getSelection());
        prefStore.setValue(ComparePreferencePage.CAPPING_DISABLED, true);
        CompareConfiguration newConfig = new CompareConfiguration(prefStore);
        CompareEditorInput input = new EventCompareInput(event1, event2,
                newConfig);
        IRunnableContext context = PlatformUI.getWorkbench()
                .getProgressService();
        try {
            context.run(true, true, input);
            EventComparerDialog dialog = new EventComparerDialog(getShell(),
                    input);
            dialog.open();
        } catch (InvocationTargetException | InterruptedException e) {
            statusHandler.handle(Priority.PROBLEM, e.getLocalizedMessage(), e);
        }
    }

    private String getText(String id) {
        String textData = null;
        if (verboseChk.getSelection()) {
            textData = verboseData.get(id);
        } else {
            textData = data.get(id);
        }
        return textData;
    }

    private void loadTextField() {
        text.setText(getText(combo.getText()));
    }

    private void copyContents() {
        text.selectAll();
        text.copy();
    }

    private void loadData() {
        for (List<? extends IReadableHazardEvent> history : events) {
            int historyIndex = 0;
            for (IReadableHazardEvent iEvent : history) {
                historyIndex++;
                SessionHazardEvent event = new SessionHazardEvent(iEvent);
                StringBuilder sb = new StringBuilder();
                TreeMap<String, Serializable> orderedAttrs = new TreeMap<>(
                        event.getHazardAttributes());

                sb.append("CreationTime: ")
                        .append(formatedTime(event.getCreationTime()))
                        .append("\n");
                sb.append("StartTime: ")
                        .append(formatedTime(event.getStartTime()))
                        .append("\n");
                sb.append("EndTime: ").append(formatedTime(event.getEndTime()))
                        .append("\n");
                sb.append("ExpirationTime: ")
                        .append(formatedTime(event.getExpirationTime()))
                        .append("\n");
                sb.append("InsertTime: ")
                        .append(formatedTime(event.getInsertTime()))
                        .append("\n");
                sb.append("Status: ").append(event.getStatus()).append("\n");
                sb.append("HazardType: ").append(event.getHazardType())
                        .append("\n");
                sb.append("Phenomenon: ").append(event.getPhenomenon())
                        .append("\n");
                sb.append("Phensig: ").append(event.getPhensig()).append("\n");
                sb.append("SubType: ").append(event.getSubType()).append("\n");

                StringBuilder attBuilder = new StringBuilder("\nATTRIBUTES:\n");
                for (Entry<String, Serializable> entry : orderedAttrs
                        .entrySet()) {
                    Serializable value = entry.getValue();
                    if (value instanceof Collection) {
                        attBuilder.append(value.getClass().getSimpleName())
                                .append(": ").append(entry.getKey())
                                .append(": ").append(value).append("\n");
                    } else if (value instanceof Map) {
                        attBuilder.append(entry.getKey()).append(": ");
                        addMapValues((Map<Serializable, Serializable>) value,
                                attBuilder);
                    } else {
                        attBuilder.append(entry.getKey()).append(": ")
                                .append(value).append("\n");
                    }
                }

                sb.append(attBuilder.toString());
                sb.append("\n\n");
                sb.append("Geometry: ").append(event.getGeometry())
                        .append("\n");

                data.put(event.getDisplayEventID() + "-" + historyIndex,
                        sb.toString());
                StringBuilder verboseBuilder = new StringBuilder();
                for (Field field : event.getClass().getDeclaredFields()) {
                    if (HAZARD_FIELD_BLACK_LIST.contains(field.getName())) {
                        continue;
                    }
                    field.setAccessible(true);
                    Object value = null;
                    try {
                        if (field.getName().equals("attributes")) {
                            continue;
                        }
                        value = field.get(event);
                        if (value instanceof Collection) {
                            verboseBuilder
                                    .append(value.getClass().getSimpleName())
                                    .append(": ").append(field.getName())
                                    .append(": ").append(value).append("\n");
                        } else if (value instanceof Map) {
                            verboseBuilder.append(field.getName()).append(": ");
                            addMapValues(
                                    (Map<Serializable, Serializable>) value,
                                    verboseBuilder);
                        } else {
                            verboseBuilder.append(field.getName()).append(": ")
                                    .append(value).append("\n");
                        }
                    } catch (IllegalArgumentException
                            | IllegalAccessException e1) {
                        statusHandler.error("Error getting event details", e1);
                    }
                }
                verboseBuilder.append(attBuilder.toString());
                verboseData.put(event.getDisplayEventID() + "-" + historyIndex,
                        verboseBuilder.toString());
            }
        }
    }

    private void addMapValues(Map<Serializable, Serializable> map,
            StringBuilder sb) {
        sb.append("{\n");
        // Create TreeMap to do the sorting
        TreeMap<Serializable, Serializable> tMap = new TreeMap<>();
        tMap.putAll(map);
        for (Entry<Serializable, Serializable> entry : tMap.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue())
                    .append("\n");
        }
        sb.append("}\n");
    }

    private List<String> getComboOptions() {
        List<String> comboOptions = new ArrayList<>();
        for (List<? extends IReadableHazardEvent> history : events) {
            int historyIndex = 0;
            for (IReadableHazardEvent iEvent : history) {
                historyIndex++;
                SessionHazardEvent event = new SessionHazardEvent(iEvent);
                comboOptions
                        .add(event.getDisplayEventID() + "-" + historyIndex);
            }
        }

        java.util.Collections.sort(comboOptions);
        java.util.Collections.reverse(comboOptions);
        return comboOptions;
    }
}
