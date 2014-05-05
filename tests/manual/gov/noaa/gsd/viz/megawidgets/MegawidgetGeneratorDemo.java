/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import gov.noaa.gsd.common.utilities.collect.IParameterInfo;

import java.util.Date;
import java.util.HashMap;
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

    /**
     * Implementation of parameter info in which the key and the label are one
     * and the same.
     */
    private static class SimpleParameterInfo implements IParameterInfo {

        private final String identifier;

        public SimpleParameterInfo(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getKey() {
            return identifier;
        }

        @Override
        public String getLabel() {
            return identifier;
        }
    }

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

        List<SimpleParameterInfo> parameters = Lists.newArrayList(
                new SimpleParameterInfo("Sample String"),
                new SimpleParameterInfo("Sample Integer"),
                new SimpleParameterInfo("Sample Long"),
                new SimpleParameterInfo("Sample Float"),
                new SimpleParameterInfo("Sample Double"),
                new SimpleParameterInfo("Sample Date"),
                new SimpleParameterInfo("Sample List"));
        Map<SimpleParameterInfo, Object> valuesForParameters = new HashMap<>();
        valuesForParameters.put(parameters.get(0), "Hello there!");
        valuesForParameters.put(parameters.get(1), 100);
        valuesForParameters.put(parameters.get(2), System.currentTimeMillis());
        valuesForParameters.put(parameters.get(3), 0.5f);
        valuesForParameters.put(parameters.get(4), 0.25);
        valuesForParameters.put(parameters.get(5),
                new Date(System.currentTimeMillis()));
        valuesForParameters.put(parameters.get(6),
                Lists.newArrayList("One", "Two", "Three"));

        try {
            ParametersEditorFactory factory = new ParametersEditorFactory();
            factory.buildParametersEditor(top, parameters, valuesForParameters,
                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1L),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L),
                    null, new IParametersEditorListener<SimpleParameterInfo>() {
                        @Override
                        public void parameterValueChanged(
                                SimpleParameterInfo parameter, Object value) {
                            System.err.println("Parameter \""
                                    + parameter.getKey() + "\" changed to "
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
