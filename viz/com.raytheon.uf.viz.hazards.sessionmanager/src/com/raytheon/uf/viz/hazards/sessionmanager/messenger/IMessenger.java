/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.messenger;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Provides access to tools for alerting the user and retrieving
 * yes/no responses from the user. This allows the Session Manager and other
 * Hazard Services managers access to a common way of creating message dialogs
 * which can be easily stubbed for testing purposes. It also prevents a
 * dependency on the gov.noaa.gsd.viz.hazards plugin.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 29, 2013            Bryon.Lawrence      Initial creation
 * Feb 28, 2015   3847     mpduff      Add Rise Crest Fall editor
 * May 14, 2015   7560     mpduff      Added apply callback.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public interface IMessenger {

    /**
     * Interface defining a warner
     */
    public interface IWarner {
        public void warnUser(String title, String warning);
    }

    /**
     * Interface defining a questioner
     */
    public interface IQuestionAnswerer {
        public boolean getUserAnswerToQuestion(String question);

        public boolean getUserAnswerToQuestion(String question,
                String[] buttonLabels);
    }

    /**
     * Interface allowing the user to continue or cancel an operation based on a
     * question.
     */
    public interface IContinueCanceller {
        public boolean getUserAnswerToQuestion(String title, String question);
    }

    /**
     * Interface allowing the user to edit rise-crest-fall information for the
     * specified hazard event graphically.
     */
    public interface IRiseCrestFallEditor {
        public IHazardEvent getRiseCrestFallEditor(IHazardEvent event,
                IEventApplier applier);
    }

    /**
     * Interface for applying the Graphical Editor changes without closing.
     */
    public interface IEventApplier {
        public void apply(IHazardEvent event);
    }

    /**
     * Returns a question/answer.
     * 
     * @param
     * @return `A question/answer. The implementation will allow a question to
     *         be asked of the user and a boolean answer to be returned. This
     *         can be easily stubbed for testing.
     */
    public IQuestionAnswerer getQuestionAnswerer();

    /**
     * Returns a warner.
     * 
     * @param
     * @return A warner. The implementation will allow a warning to be displayed
     *         to the user. This can be easily stubbed for testing.
     */
    public IWarner getWarner();

    /**
     * Returns a continue/canceller.
     * 
     * @param
     * @return A continue/canceller. The implementation will allow a question to
     *         be displayed and give the forecaster the ability to continue or
     *         cancel the current operation.
     */
    public IContinueCanceller getContinueCanceller();

    /**
     * Returns a rise-crest-fall editor.
     * 
     * @return A rise-crest-fall editor. The implementation will allow a
     *         graphical editor to be displayed for a specified event, and give
     *         the forecaster the ability to manipulate the rise-crest-fall
     *         values for that event.
     */
    public IRiseCrestFallEditor getRiseCrestFallEditor(IHazardEvent event);
}
