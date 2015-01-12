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
package gov.noaa.gsd.viz.hazards.setting;

import gov.noaa.gsd.viz.hazards.UIOriginator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.recommenders.EventRecommender;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsToolsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * A composite to hold the inventory of recommenders.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 28, 2014 3768       Robert.Blum  Initial creation.
 * Sep 08, 2014 4243       Chris.Golden Changed to work with latest version
 *                                      of abstract recommender engine.
 * Dec 05, 2014 4124       Chris.Golden Changed to work with ObservedSettings.
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

public class RecommenderInventoryComposite extends Composite {

    SettingsPresenter presenter;

    private TreeViewer recommenderTree;

    private StyledText stText;

    private ISettings currentSetting;

    /**
     * @param parentShell
     */
    public RecommenderInventoryComposite(Composite parent,
            SettingsPresenter presenter) {
        super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
        this.presenter = presenter;
        initializeComponents(parent);
    }

    /**
     * Initializes the layout and components of the composite.
     * 
     * @param parent
     */
    protected void initializeComponents(Composite parent) {
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;

        setLayout(gl);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Add controls to the composite
        createTreeControl(this);
        createTextControl(parent);
    }

    /**
     * Creates the tree that contains all the recommenders
     * 
     * @param sashForm
     */
    private void createTreeControl(Composite treeComp) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 300;
        recommenderTree = new TreeViewer(treeComp, SWT.BORDER | SWT.CHECK);
        recommenderTree.getTree().setLayoutData(gd);
        recommenderTree.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                TreeItem item = (TreeItem) event.item;
                EventRecommender rec = presenter.getSessionManager()
                        .getRecommenderEngine().getInventory(item.getText());
                if (event.detail == SWT.CHECK) {
                    boolean checked = item.getChecked();
                    checkItems(item, checked);
                    checkPath(item.getParentItem(), checked, false);
                    Tool tool = null;
                    ISessionConfigurationManager<ObservedSettings> manager = presenter
                            .getSessionManager().getConfigurationManager();
                    if (item.getText().equals(rec.getName())) {

                        if (checked) {
                            tool = new Tool();
                            tool.setDisplayName(rec.getName());
                            tool.setToolName(rec.getName());
                            manager.getSettings().getToolbarTools().add(tool);
                        } else {
                            for (Tool t : manager.getSettings()
                                    .getToolbarTools()) {
                                if (t.getToolName().equals(rec.getName())) {
                                    tool = t;
                                    break;
                                }
                            }
                            manager.getSettings().getToolbarTools()
                                    .remove(tool);
                        }
                        presenter.publish(new SettingsToolsModified(manager,
                                UIOriginator.SETTINGS_DIALOG));
                    }
                } else {
                    if (item.getText().equals(rec.getName())) {
                        StringBuilder builder = new StringBuilder();
                        List<StyleRange> ranges = new ArrayList<StyleRange>();
                        buildString(ranges, builder, EventRecommender.AUTHOR
                                + " : ", rec.getAuthor());
                        buildString(ranges, builder, EventRecommender.VERSION
                                + " : ", rec.getVersion());
                        buildString(ranges, builder,
                                EventRecommender.DESCRIPTION + " : ",
                                rec.getDescription());
                        if (builder.length() != 0) {
                            stText.setText(builder.toString());
                            stText.setStyleRanges(ranges
                                    .toArray(new StyleRange[0]));
                        } else {
                            stText.setText("No information available");
                            StyleRange style = new StyleRange();
                            style.start = 0;
                            style.length = stText.getText().length();
                            stText.setStyleRange(style);
                        }
                    }

                }
            }
        });

        populateTreeControl();
    }

    private void buildString(List<StyleRange> ranges, StringBuilder builder,
            String constant, String value) {
        if (value != null && value.isEmpty() == false) {
            StyleRange style = new StyleRange();
            style.start = builder.length();
            style.length = constant.length();
            style.fontStyle = SWT.BOLD;
            builder.append(WordUtils.capitalize(constant));
            builder.append(value);
            builder.append("\n");
            ranges.add(style);
        }
    }

    private void createTextControl(Composite parent) {
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        Composite stTextComp = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        stTextComp.setLayout(gl);
        stTextComp.setLayoutData(gd);

        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 500;
        gd.heightHint = 300;
        stText = new StyledText(stTextComp, SWT.BORDER | SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        stText.setWordWrap(true);
        stText.setLayoutData(gd);
    }

    private void checkPath(TreeItem item, boolean checked, boolean grayed) {
        if (item == null) {
            return;
        }
        if (grayed) {
            checked = true;
        } else {
            int index = 0;
            TreeItem[] items = item.getItems();
            while (index < items.length) {
                TreeItem child = items[index];
                if (child.getGrayed() || checked != child.getChecked()) {
                    checked = grayed = true;
                    break;
                }
                index++;
            }
        }
        item.setChecked(checked);
        item.setGrayed(grayed);
        checkPath(item.getParentItem(), checked, grayed);
    }

    private void checkItems(TreeItem item, boolean checked) {
        item.setGrayed(false);
        item.setChecked(checked);
        TreeItem[] items = item.getItems();
        for (int i = 0; i < items.length; i++) {
            checkItems(items[i], checked);
        }
    }

    /**
     * Retrieves a list of recommenders based on the filesystem
     */
    private void populateTreeControl() {
        Job job = new Job("Retrieving recommenders...") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                final List<String> filenames = getRecommenderFilenames();
                VizApp.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        for (String recom : filenames) {
                            TreeItem item = new TreeItem(recommenderTree
                                    .getTree(), SWT.NONE);
                            item.setText(recom);
                        }
                        updateChecks(presenter.getSessionManager()
                                .getConfigurationManager().getSettings());
                    }
                });
                for (String recom : filenames) {
                    // instantiates each of the recommenders
                    presenter.getSessionManager().getRecommenderEngine()
                            .getInventory(recom);
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    /**
     * Based on the recommenders currently available in the setting, this
     * updates the checks in the Tree
     */
    private void updateChecks(ISettings setting) {
        currentSetting = setting;
        for (TreeItem item : recommenderTree.getTree().getItems()) {
            boolean inSetting = false;
            for (Tool tool : currentSetting.getToolbarTools()) {
                if (tool.getToolName().equals(item.getText())) {
                    inSetting = true;
                    break;
                }
            }
            item.setChecked(inSetting);
        }
    }

    /**
     * Gets the list of recommender filenames, much quicker than a full
     * inventory, so this is an initial get and we fill in as it goes with the
     * actual inventory
     * 
     * @return
     */
    private List<String> getRecommenderFilenames() {
        IPathManager manager = PathManagerFactory.getPathManager();
        String recDir = FileUtil.join("python", "events", "recommenders");
        LocalizationFile[] files = manager.listStaticFiles(recDir,
                new String[] { ".py" }, true, true);
        final List<String> filenames = new ArrayList<String>();
        for (LocalizationFile file : files) {
            String fname = file.getName();
            // check not in the config directory
            if (fname.replaceFirst(recDir, "").startsWith(
                    File.separator + "config") == false) {
                String name = FilenameUtils.getBaseName(file.getName());
                filenames.add(name);
            }
        }
        return filenames;
    }
}
