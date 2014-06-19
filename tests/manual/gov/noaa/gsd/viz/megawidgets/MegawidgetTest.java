/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.megawidgets.sideeffects.PythonSideEffectsApplier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Description: Megawidget tester.
 * <p>
 * Either one or two arguments may be supplied at the command line. The first
 * argument is the relative path to a JSON file holding a list of megawidget
 * definitions, each definition as a dictionary. The second argument, which is
 * optional, is the relative path to a Python script that is used to create
 * interdependencies between the megawidgets specified in the JSON file, if any
 * interdependencies are desired.
 * </p>
 * <p>
 * Running this tester either causes an error, which will be displayed to
 * stderr, or else brings up a dialog showing the megawidget(s) specified.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 06, 2014    2155    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MegawidgetTest extends Dialog {

    private final String specifiersFilePath;

    private final String scriptFilePath;

    private static final String PATH_PREFIX = "test_resources/gov/noaa/gsd/viz/megawidgets/";

    // Public Static Methods

    /**
     * Entry point to the application.
     */
    public static void main(String[] args) {
        Display display = new Display();

        if (args.length == 0) {
            System.err
                    .println("Error: No JSON file provided from which to take megawidget specifiers.");
            System.exit(1);
        }
        if (args.length > 1) {
            PythonSideEffectsApplier.initialize();
        }
        MegawidgetTest dialog = new MegawidgetTest(args[0],
                (args.length > 1 ? args[1] : null));
        dialog.open();

        while ((dialog.getShell() != null)
                && (dialog.getShell().isDisposed() == false)) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
        if (args.length > 1) {
            PythonSideEffectsApplier.prepareForShutDown();
        }
    }

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public MegawidgetTest(String specifiersFilePath, String scriptFilePath) {
        super((Shell) null);
        this.specifiersFilePath = specifiersFilePath;
        this.scriptFilePath = scriptFilePath;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    // Protected Methods

    @Override
    protected Control createDialogArea(Composite parent) {

        Composite top = (Composite) super.createDialogArea(parent);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 10;
        mainLayout.marginTop = 10;
        top.setLayout(mainLayout);

        List<Map<String, Object>> specifiers = null;
        try {
            DictList specifiersDictList = DictList
                    .getInstance(readFile(specifiersFilePath));
            specifiers = new ArrayList<>(specifiersDictList.size());
            for (Object specifier : specifiersDictList) {
                specifiers.add((Dict) specifier);
            }
        } catch (Exception e) {
            System.err.println("Error: JSON Syntax exception within \""
                    + specifiersFilePath + "\":");
            e.printStackTrace(System.err);
            PythonSideEffectsApplier.prepareForShutDown();
            System.exit(1);
        }

        String script = null;
        if (scriptFilePath != null) {
            script = readFile(scriptFilePath);
        }

        File file = new File(specifiersFilePath);
        String name = file.getName();
        parent.getShell().setText(
                "Megawidget Demo: " + name.split("\\.(?=[^\\.]+$)")[0]);

        try {
            new MegawidgetManager(top, specifiers,
                    new HashMap<String, Object>(), System.currentTimeMillis()
                            - TimeUnit.DAYS.toMillis(4L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(4L),
                    new ICurrentTimeProvider() {

                        @Override
                        public long getCurrentTime() {
                            return System.currentTimeMillis();
                        }
                    }, (script == null ? null : new PythonSideEffectsApplier(
                            script))) {

                @Override
                protected void commandInvoked(String identifier,
                        String extraCallback) {
                }

                @Override
                protected void stateElementChanged(String identifier,
                        Object state) {
                    System.out.println("STATE CHANGE: " + identifier + " = "
                            + state);
                }

                @Override
                protected void sideEffectMutablePropertyChangeErrorOccurred(
                        MegawidgetPropertyException exception) {
                    System.err.println("Error: ");
                    exception.printStackTrace(System.err);
                }

            };
        } catch (Exception e) {
            System.err.println("Error: Megawidget improperly specified:");
            e.printStackTrace(System.err);
            PythonSideEffectsApplier.prepareForShutDown();
            System.exit(1);
        }

        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }

    // Private Methods

    /**
     * Read the specified file. The latter must be a relative path from
     * <project-location>/test_resources/gov/noaa/gsd/viz/megawidgets.
     * 
     * @param path
     *            Path to the file.
     * @return Contents of the file.
     */
    private String readFile(String path) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(PATH_PREFIX + path));
            return new String(encoded);
        } catch (Exception e) {
            System.err.println("Error: Could not read in file \"" + path
                    + "\": " + e);
            PythonSideEffectsApplier.prepareForShutDown();
            System.exit(1);
            return null;
        }
    }
}