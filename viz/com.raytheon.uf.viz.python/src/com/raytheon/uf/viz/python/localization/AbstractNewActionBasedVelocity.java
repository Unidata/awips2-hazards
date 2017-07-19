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
package com.raytheon.uf.viz.python.localization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.raytheon.uf.common.localization.FileUpdatedMessage;
import com.raytheon.uf.common.localization.FileUpdatedMessage.FileChangeType;
import com.raytheon.uf.common.localization.ILocalizationFileObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.localization.perspective.service.ILocalizationService;
import com.raytheon.uf.viz.localization.perspective.service.LocalizationPerspectiveUtils;

/**
 * Uses velocity to generate a python file based on a template supplied by a
 * child class.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 22, 2013            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public abstract class AbstractNewActionBasedVelocity extends Action
        implements INewBasedVelocityAction {
    private static final String MENU_TEXT = "New ...";

    private IUFStatusHandler statusHandler;

    private String velocityTemplateLocation;

    private String velocityTemplate;

    private String dialogTitle;

    private String dialogMessage;

    private String initialValue;

    /**
     * 
     */
    public AbstractNewActionBasedVelocity(IUFStatusHandler statusHandler,
            String velocityTemplateLocation, String velocityTemplate,
            String dialogTitle, String dialogMessage, String initialValue) {
        super(MENU_TEXT);
        this.statusHandler = statusHandler;
        this.velocityTemplateLocation = velocityTemplateLocation;
        this.velocityTemplate = velocityTemplate;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        this.initialValue = initialValue;
    }

    protected static String mergeDirectoryPaths(String... directoryElements) {
        StringBuilder stringBuilder = new StringBuilder();

        int count = 0;
        for (String directoryElement : directoryElements) {
            if (count > 0) {
                stringBuilder.append(File.separator);
            }
            stringBuilder.append(directoryElement);
            ++count;
        }

        return stringBuilder.toString();
    }

    @Override
    public void run() {
        IInputValidator validator = this.getInputValidator();

        InputDialog dialog = new InputDialog(
                Display.getCurrent().getActiveShell(), this.dialogTitle,
                this.dialogMessage, this.initialValue, validator);
        int returnCode = dialog.open();
        if (returnCode != Window.OK) {
            return;
        }

        String scriptName = dialog.getValue();
        final LocalizationFile file = this.getUserFile(scriptName);

        File f = file.getFile();
        FileWriter writer = null;
        try {
            writer = new FileWriter(f, false);
        } catch (IOException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to write to " + f.getName(), e);
        }
        initVelocity();
        Map<String, Object> velocityContextValues = new HashMap<String, Object>();
        /*
         * these are parameters that are common to all hazards templates.
         */
        velocityContextValues.put("author",
                LocalizationManager.getInstance().getCurrentUser());
        velocityContextValues.put("scriptName", scriptName);
        VelocityContext velocityContext = this
                .buildVelocityContext(velocityContextValues);

        Template template = this.getVelocityEngine()
                .getTemplate(this.velocityTemplate);
        template.merge(velocityContext, writer);
        try {
            writer.close();
        } catch (IOException e) {
            this.statusHandler.handle(Priority.ERROR,
                    "Unable to close the file writer", e);
        }

        final ILocalizationService service = LocalizationPerspectiveUtils
                .changeToLocalizationPerspective();
        try {
            final Runnable select = new Runnable() {
                @Override
                public void run() {
                    service.selectFile(file);
                    service.openFile(file);
                }
            };
            final ILocalizationFileObserver[] observers = new ILocalizationFileObserver[1];
            ILocalizationFileObserver observer = new ILocalizationFileObserver() {
                @Override
                public void fileUpdated(FileUpdatedMessage message) {
                    if (message.getChangeType() != FileChangeType.DELETED) {
                        service.fileUpdated(message);
                        VizApp.runAsync(select);
                    }
                    file.removeFileUpdatedObserver(observers[0]);
                }
            };
            observers[0] = observer;
            file.addFileUpdatedObserver(observer);
            file.save();
        } catch (LocalizationException e) {
            statusHandler.handle(Priority.ERROR,
                    "Unable to save file to localization", e);
        }
    }

    protected VelocityEngine initVelocity() {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext baseContext = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
        File templateFile = pm.getFile(baseContext,
                this.velocityTemplateLocation);
        Properties properties = new Properties();
        properties.setProperty("file.resource.loader.path",
                templateFile.getPath());
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init(properties);

        return velocityEngine;
    }

    protected abstract VelocityEngine getVelocityEngine();

    protected abstract IInputValidator getInputValidator();

    private LocalizationFile getUserFile(String filename) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext userContext = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);
        return this.getUserFile(filename, pm, userContext);
    }

    protected abstract LocalizationFile getUserFile(String filename,
            IPathManager pm, LocalizationContext userContext);

    protected VelocityContext buildVelocityContext(
            Map<String, Object> contextValues) {
        VelocityContext context = new VelocityContext();

        Iterator<String> contextIterator = contextValues.keySet().iterator();
        while (contextIterator.hasNext()) {
            String key = contextIterator.next();

            context.put(key, contextValues.get(key));
        }

        return context;
    }
}