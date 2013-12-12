/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Description: Functional testbed for the megawidget package's parameters
 * editor factory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer          Description
 * ------------ ---------- -------------------------------------
 * Nov 20, 2013    2336    Chris.Golden      Initial creation
 * Dec 14, 2013    2545    Chris.Golden      Changed to only have a "Close"
 *                                           button that is not activated by
 *                                           the Enter key.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("unused")
public class MegawidgetGeneratorDemo extends Dialog {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {
        Display display = new Display();

        MegawidgetGeneratorDemo dialog = new MegawidgetGeneratorDemo();
        dialog.open();

        while ((dialog.getShell() != null)
                && (dialog.getShell().isDisposed() == false)) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    public MegawidgetGeneratorDemo() {
        super((Shell) null);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Parameters Editor Demo");
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Let the superclass create the area, and then set up its
        // layout.
        Composite top = (Composite) super.createDialogArea(parent);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 10;
        mainLayout.marginTop = 10;
        top.setLayout(mainLayout);

        long minTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
        long maxTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2);

        List<String> labels = Lists.newArrayList("Sample String",
                "Sample Integer", "Sample Long", "Sample Float",
                "Sample Double", "Sample Date", "Sample List");
        Map<String, Object> parametersForLabels = Maps.newHashMap();
        parametersForLabels.put("Sample String", "Hello there!");
        parametersForLabels.put("Sample Integer", 100);
        parametersForLabels.put("Sample Long", System.currentTimeMillis());
        parametersForLabels.put("Sample Float", 0.5f);
        parametersForLabels.put("Sample Double", 0.25);
        parametersForLabels.put("Sample Date",
                new Date(System.currentTimeMillis()));
        parametersForLabels.put("Sample List",
                Lists.newArrayList("One", "Two", "Three"));

        try {
            ParametersEditorFactory factory = new ParametersEditorFactory();
            factory.buildParametersEditor(top, labels, parametersForLabels,
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    null, new IParametersEditorListener() {
                        @Override
                        public void parameterValueChanged(String label,
                                Object value) {
                            System.err.println("Parameter \"" + label
                                    + "\" changed to "
                                    + value.getClass().getSimpleName() + ": "
                                    + value);
                        }
                    });
        } catch (MegawidgetException e) {
            System.err.println("Error creating megawidgets.");
            e.printStackTrace(System.err);
        }

        // Return the created area.
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }
}
