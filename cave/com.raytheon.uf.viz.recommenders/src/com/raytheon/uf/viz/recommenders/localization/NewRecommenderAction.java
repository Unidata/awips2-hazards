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
package com.raytheon.uf.viz.recommenders.localization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import com.raytheon.uf.common.localization.exception.LocalizationOpFailedException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.uf.viz.core.localization.LocalizationManager;
import com.raytheon.uf.viz.localization.LocalizationPerspectiveUtils;
import com.raytheon.uf.viz.localization.service.ILocalizationService;

/**
 * Action for the localization perspective to create a new recommender with the
 * recommender.vm template
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 19, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class NewRecommenderAction extends Action {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewRecommenderAction.class);

    private static final String RECOMMENDER_TEMPLATE_DIR = "python"
            + File.separator + "events" + File.separator + "recommenders"
            + File.separator + "config";

    private static final String RECOMMENDER_TEMPLATE_NAME = "recommender.vm";

    private static VelocityEngine ENGINE;

    public NewRecommenderAction() {
        super("New...");
    }

    @Override
    public void run() {
        // create new LocalizationFile
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (newText.endsWith(".py")) {
                    return null;
                }
                return "Recommender name must end with .py";
            }
        };
        InputDialog dialog = new InputDialog(Display.getCurrent()
                .getActiveShell(), "New Recommender",
                "Input name for new recommender:", "NewRecommender.py",
                validator);
        int returnCode = dialog.open();
        if (returnCode == Window.OK) {
            String scriptName = dialog.getValue();
            final LocalizationFile file = getUserFile(scriptName);

            File f = file.getFile();
            if (f.exists()) {
                f.delete();
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(f);
            } catch (IOException e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to write to " + f.getName(), e);
            }
            initVelocity();
            VelocityContext context = new VelocityContext();
            String author = LocalizationManager.getInstance().getCurrentUser();
            context.put("author", author);
            context.put("scriptName", scriptName);

            Template template = ENGINE.getTemplate(RECOMMENDER_TEMPLATE_NAME);
            template.merge(context, writer);
            try {
                writer.close();
            } catch (IOException e) {
                statusHandler.handle(Priority.ERROR,
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
            } catch (LocalizationOpFailedException e) {
                statusHandler.handle(Priority.ERROR,
                        "Unable to save file to localization", e);
            }
        }
    }

    private LocalizationFile getUserFile(String filename) {
        IPathManager pm = PathManagerFactory.getPathManager();
        LocalizationContext userContext = pm.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.USER);
        return pm.getLocalizationFile(userContext, "python" + File.separator
                + "events" + File.separator + "recommenders" + File.separator
                + filename);
    }

    private void initVelocity() {
        synchronized (NewRecommenderAction.class) {
            if (ENGINE == null) {
                IPathManager pm = PathManagerFactory.getPathManager();
                LocalizationContext baseContext = pm.getContext(
                        LocalizationType.COMMON_STATIC, LocalizationLevel.BASE);
                File templateFile = pm.getFile(baseContext,
                        RECOMMENDER_TEMPLATE_DIR);
                Properties properties = new Properties();
                properties.setProperty("file.resource.loader.path",
                        templateFile.getPath());
                ENGINE = new VelocityEngine();
                ENGINE.init(properties);
            }
        }
    }
}
