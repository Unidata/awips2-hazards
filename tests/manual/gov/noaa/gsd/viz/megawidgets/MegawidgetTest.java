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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Range;

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
 * Jun 23, 2014    4010    Chris.Golden Changed to work with latest megawidget
 *                                      manager changes.
 * Jun 30, 2014    3512    Chris.Golden Changed to work with more megawidget
 *                                      manager changes.
 * Aug 19, 2014    4098    Chris.Golden Added use of SWT wrapper megawidget if
 *                                      one is specified.
 * Aug 21, 2014    4243    Chris.Golden Changed to work with new side effects
 *                                      applier.
 * Jan 26, 2014    2331    Chris.Golden Changed to ensure that time scale and
 *                                      time range megawidgets always have
 *                                      their visible range area include their
 *                                      starting values (so that users can see
 *                                      the thumbs when they bring up the
 *                                      test dialog).
 * Apr 02, 2015    4162    Chris.Golden Added Python initialize required to
 *                                      employ Jep-using objects.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MegawidgetTest extends Dialog {

    // Private Static Constants

    private static final String PATH_PREFIX = "test_resources/gov/noaa/gsd/viz/megawidgets/";

    // Private Variables

    private final String specifiersFilePath;

    private final String scriptFilePath;

    private final String swtWrapperIdentifier;

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
                (args.length > 1 ? args[1] : null), (args.length > 2 ? args[2]
                        : null));
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
    public MegawidgetTest(String specifiersFilePath, String scriptFilePath,
            String swtWrapperIdentifier) {
        super((Shell) null);
        this.specifiersFilePath = specifiersFilePath;
        this.scriptFilePath = scriptFilePath;
        this.swtWrapperIdentifier = swtWrapperIdentifier;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    // Protected Methods

    @Override
    protected Control createDialogArea(Composite parent) {

        /*
         * Lay out the overall area.
         */
        Composite top = (Composite) super.createDialogArea(parent);
        GridLayout mainLayout = new GridLayout(1, false);
        mainLayout.marginHeight = 3;
        mainLayout.marginWidth = 10;
        mainLayout.marginTop = 10;
        top.setLayout(mainLayout);

        /*
         * Get the megawidget specifiers.
         */
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

        /*
         * Set the window title.
         */
        File file = new File(specifiersFilePath);
        String name = file.getName();
        parent.getShell().setText(
                "Megawidget Demo: " + name.split("\\.(?=[^\\.]+$)")[0]);

        MegawidgetManager manager = null;
        try {

            /*
             * Create the megawidget specifier manager first, and find the
             * minimum and maximum times used as starting values by any time
             * megawidgets.
             */
            MegawidgetSpecifierManager specifierManager = new MegawidgetSpecifierManager(
                    specifiers, IControlSpecifier.class,
                    new ICurrentTimeProvider() {

                        @Override
                        public long getCurrentTime() {
                            return System.currentTimeMillis();
                        }
                    }, (scriptFilePath == null ? null
                            : new PythonSideEffectsApplier(new File(PATH_PREFIX
                                    + scriptFilePath))));
            Range<Long> bounds = getTimeBoundaries(null,
                    specifierManager.getSpecifiers());
            long minTime = (bounds == null ? System.currentTimeMillis()
                    : bounds.lowerEndpoint());
            long maxTime = (bounds == null ? System.currentTimeMillis()
                    : bounds.upperEndpoint());

            /*
             * Determine the delta between the minimum and maximum times, and
             * from that, determine the lower and upper boundaries of the
             * visible time for any time megawidgets, giving some room to either
             * side of the found minimum and maximum values.
             */
            long delta = maxTime - minTime;
            if (delta < TimeUnit.HOURS.toMillis(1L)) {
                minTime -= TimeUnit.MINUTES.toMillis(30L);
                maxTime += TimeUnit.MINUTES.toMillis(30L);
            } else if (delta < TimeUnit.DAYS.toMillis(1L)) {
                minTime -= TimeUnit.HOURS.toMillis(12L);
                maxTime += TimeUnit.HOURS.toMillis(12L);
            } else {
                minTime -= TimeUnit.DAYS.toMillis(2L);
                maxTime += TimeUnit.DAYS.toMillis(2L);
            }

            /*
             * Create the megawidget manager.
             */
            manager = new MegawidgetManager(top, specifierManager,
                    new HashMap<String, Object>(),
                    new IMegawidgetManagerListener() {

                        @Override
                        public void commandInvoked(MegawidgetManager manager,
                                String identifier) {
                            System.out
                                    .println("COMMAND INVOKED: " + identifier);
                        }

                        @Override
                        public void stateElementChanged(
                                MegawidgetManager manager, String identifier,
                                Object state) {
                            System.out.println("STATE CHANGE: " + identifier
                                    + " = " + state);
                        }

                        @Override
                        public void stateElementsChanged(
                                MegawidgetManager manager,
                                Map<String, ?> statesForIdentifiers) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Map.Entry<String, ?> entry : statesForIdentifiers
                                    .entrySet()) {
                                if (stringBuilder.length() > 0) {
                                    stringBuilder.append(", ");
                                }
                                stringBuilder.append(entry.getKey());
                                stringBuilder.append(" = ");
                                stringBuilder.append(entry.getValue());
                            }
                            System.out
                                    .println("POTENTIALLY MULTIPLE STATE CHANGES: "
                                            + stringBuilder);
                        }

                        @Override
                        public void sizeChanged(MegawidgetManager manager,
                                String identifier) {
                            System.out.println("SIZE CHANGED.");
                        }

                        @Override
                        public void sideEffectMutablePropertyChangeErrorOccurred(
                                MegawidgetManager manager,
                                MegawidgetPropertyException exception) {
                            System.err.println("Error: " + exception);
                            exception.printStackTrace(System.err);
                        }

                        @Override
                        public void visibleTimeRangeChanged(
                                MegawidgetManager manager, String identifier,
                                long lower, long upper) {
                            System.out.println("TIME RANGE CHANGED.");
                        }

                    }, minTime, maxTime);
        } catch (Exception e) {
            System.err.println("Error: Megawidget improperly specified: " + e);
            e.printStackTrace(System.err);
            PythonSideEffectsApplier.prepareForShutDown();
            System.exit(1);
        }

        /*
         * If an SWT wrapper megawidget was found, add an SWT composite and
         * button to it as a test.
         */
        if (swtWrapperIdentifier != null) {
            SwtWrapperMegawidget megawidget = manager
                    .getSwtWrapper(swtWrapperIdentifier);
            if (megawidget == null) {
                System.err
                        .println("Error: No SWT wrapper megawidget with identifier \""
                                + swtWrapperIdentifier + "\" found.");
            } else {
                Composite wrapper = megawidget.getWrapperComposite();
                wrapper.setLayout(new FillLayout());
                Button button = new Button(wrapper, SWT.PUSH);
                button.setText("Sample SWT Button");
                megawidget.sizeChanged();
            }
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

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private Range<Long> getTimeBoundaries(Range<Long> startingBounds,
            List<ISpecifier> specifiers) {
        long minTime = (startingBounds == null ? Long.MAX_VALUE
                : startingBounds.lowerEndpoint());
        long maxTime = (startingBounds == null ? Long.MIN_VALUE
                : startingBounds.upperEndpoint());
        for (ISpecifier specifier : specifiers) {
            if (specifier instanceof TimeMegawidgetSpecifier) {
                IStatefulSpecifier statefulSpecifier = (IStatefulSpecifier) specifier;
                for (String identifier : statefulSpecifier
                        .getStateIdentifiers()) {
                    Object startingValue = statefulSpecifier
                            .getStartingState(identifier);
                    if (startingValue != null) {
                        long time = (Long) startingValue;
                        if (minTime > time) {
                            minTime = time;
                        }
                        if (maxTime < time) {
                            maxTime = time;
                        }
                    }
                }
            }
            if (specifier instanceof IParentSpecifier) {
                Range<Long> bounds = getTimeBoundaries(
                        (minTime == Long.MAX_VALUE ? null : Range.closed(
                                minTime, maxTime)),
                        ((IParentSpecifier<ISpecifier>) specifier)
                                .getChildMegawidgetSpecifiers());
                if (bounds != null) {
                    minTime = bounds.lowerEndpoint();
                    maxTime = bounds.upperEndpoint();
                }
            }
        }
        return (minTime == Long.MAX_VALUE ? null : Range.closed(minTime,
                maxTime));
    }
}
