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
package com.raytheon.uf.viz.productgen.localization;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jface.dialogs.IInputValidator;

import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Action for the localization perspective to create a new MetaData with
 * the metadata.vm template.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 20, 2014            bkowal     Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

public class NewMetadataAction extends AbstractNewActionBasedVelocity {

    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(NewMetadataAction.class);

    private static final String[] directoryPath = { "hazardServices",
            "hazardMetaData" };

    private static final String LOCALIZATION_DIR = mergeDirectoryPaths(directoryPath);

    private static final String METADATA_TEMPLATE_NAME = "metadata.vm";

    private static final String DIALOG_TITLE = "New MetaData";

    private static final String DIALOG_MESSAGE = "Input name for new metadata:";

    private static final String DIALOG_INITIAL_VALUE = "NewMetaData.py";

    private static VelocityEngine ENGINE;

    public NewMetadataAction() {
        super(statusHandler, LOCALIZATION_DIR, METADATA_TEMPLATE_NAME,
                DIALOG_TITLE, DIALOG_MESSAGE, DIALOG_INITIAL_VALUE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.localization.AbstractNewActionBasedVelocity
     * #getVelocityEngine()
     */
    @Override
    protected VelocityEngine getVelocityEngine() {
        synchronized (this) {
            if (ENGINE == null) {
                ENGINE = super.initVelocity();
            }
        }

        return ENGINE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.localization.AbstractNewActionBasedVelocity
     * #getInputValidator()
     */
    @Override
    protected IInputValidator getInputValidator() {
        IInputValidator validator = new IInputValidator() {

            @Override
            public String isValid(String newText) {
                if (newText.endsWith(".py")) {
                    return null;
                }
                return "MetaData name must end with .py";
            }
        };

        return validator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.viz.productgen.localization.AbstractNewActionBasedVelocity
     * #getUserFile(java.lang.String,
     * com.raytheon.uf.common.localization.IPathManager,
     * com.raytheon.uf.common.localization.LocalizationContext)
     */
    @Override
    protected LocalizationFile getUserFile(String filename, IPathManager pm,
            LocalizationContext userContext) {
        return pm.getLocalizationFile(userContext, LOCALIZATION_DIR
                + IOUtils.DIR_SEPARATOR + filename);
    }
}