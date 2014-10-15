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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
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
 * Jun 23, 2014    4010    Chris.Golden      Changed to work with latest
 *                                           megawidget changes, and to
 *                                           embed all megawidgets within
 *                                           expand bars.
 * Jun 30, 2014    3512    Chris.Golden      Changed to work with new
 *                                           parameters editor changes.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@SuppressWarnings("unused")
public class MegawidgetGeneratorDemo extends Dialog {

    // Private Static Classes

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

    // Private Static Constants

    private static final long serialVersionUID = 1L;

    // Public Static Methods

    /**
     * Entry point.
     * 
     * @param args
     *            Ignored.
     */
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

    // Public Constructors

    /**
     * Construct a standard instance.
     */
    public MegawidgetGeneratorDemo() {
        super((Shell) null);
    }

    // Protected Methods

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Parameters Editor Demo");
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        /*
         * Let the superclass create the area, and then set up its layout. It
         * includes a scrolled composite with a child composite so that if the
         * megawidgets take up too much room, their composite becomes
         * scrollable.
         */
        Composite top = (Composite) super.createDialogArea(parent);
        top.setLayout(new FillLayout());
        final ScrolledComposite scrolledComposite = new ScrolledComposite(top,
                SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        final Composite inner = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(inner);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 10;
        mainLayout.marginTop = 10;
        inner.setLayout(mainLayout);

        /*
         * Set up the minimum and maximum times allowed for date-time
         * megawidgets.
         */
        long minTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2);
        long maxTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2);

        /*
         * Create the parameters list.
         */
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

        /*
         * Create a new parameters editor factory, and tell it to embed
         * parameter-editing megawidgets within expand bars, then create the
         * editor.
         * 
         * TODO: If https://bugs.eclipse.org/bugs/show_bug.cgi?id=223486 is ever
         * fixed (i.e. if drag-and-drop ever works within an SWT
         * ExpandBar/ExpandItem), then we could embed the UnboundedListBuilder
         * within an expand bar as well, but until then, we leave that one out.
         */
        try {
            ParametersEditorFactory factory = new ParametersEditorFactory();

            factory.registerParameterType(String.class, TextSpecifier.class,
                    new HashMap<String, Object>(), null, true);
            factory.registerParameterType(Integer.class,
                    IntegerSpinnerSpecifier.class,
                    new HashMap<String, Object>(), null, true);
            factory.registerParameterType(Long.class, TimeSpecifier.class,
                    new HashMap<String, Object>(), null, true);
            factory.registerParameterType(Float.class,
                    FractionSpinnerSpecifier.class,
                    new HashMap<String, Object>(), new IConverter() {
                        @Override
                        public Object toFirst(Object value) {
                            return ((Double) value).floatValue();
                        }

                        @Override
                        public Object toSecond(Object value) {
                            return ((Float) value).doubleValue();
                        }
                    }, true);
            factory.registerParameterType(Double.class,
                    FractionSpinnerSpecifier.class,
                    new HashMap<String, Object>(), null, true);
            factory.registerParameterType(Date.class, TimeSpecifier.class,
                    new HashMap<String, Object>(), new IConverter() {
                        @Override
                        public Object toFirst(Object value) {
                            return new Date((Long) value);
                        }

                        @Override
                        public Object toSecond(Object value) {
                            return ((Date) value).getTime();
                        }
                    }, true);

            factory.buildParametersEditor(inner, parameters,
                    valuesForParameters, System.currentTimeMillis()
                            - TimeUnit.DAYS.toMillis(1L),
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

                        @Override
                        public void parameterValuesChanged(
                                Map<SimpleParameterInfo, Object> valuesForParameters) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Map.Entry<SimpleParameterInfo, Object> entry : valuesForParameters
                                    .entrySet()) {
                                if (stringBuilder.length() > 0) {
                                    stringBuilder.append(", ");
                                }
                                stringBuilder.append("\"");
                                stringBuilder.append(entry.getKey());
                                stringBuilder.append("\" changed to ");
                                stringBuilder.append(entry.getValue()
                                        .getClass().getSimpleName());
                                stringBuilder.append(": ");
                                stringBuilder.append(entry.getValue());
                            }
                            System.err
                                    .println("One or more parameters changed ("
                                            + stringBuilder + ")");
                        }

                        @Override
                        public void sizeChanged(SimpleParameterInfo parameter) {
                            inner.setSize(inner.computeSize(SWT.DEFAULT,
                                    SWT.DEFAULT));
                            scrolledComposite.setMinSize(inner.computeSize(
                                    SWT.DEFAULT, SWT.DEFAULT));
                        }
                    });
        } catch (MegawidgetException e) {
            System.err.println("Error creating megawidgets: " + e);
            e.printStackTrace(System.err);
        }

        /*
         * Return the created composite.
         */
        return top;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.CLOSE_LABEL, false);
    }
}
