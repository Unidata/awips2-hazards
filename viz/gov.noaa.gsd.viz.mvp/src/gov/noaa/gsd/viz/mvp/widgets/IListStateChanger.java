/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp.widgets;

import java.util.List;

/**
 * Interface describing the methods required in any sort of HMI component that
 * is a list widget, meaning that it holds a list of elements as its state that,
 * when said list is changed, notifies its {@link IListStateChangeHandler} of
 * the change. The generic parameter <code>I</code> provides the type of widget
 * identifier to be used, while <code>E</code> provides the type of element
 * within the list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Mar 14, 2016   15676    Chris.Golden Initial creation.
 * Jul 25, 2016   19537    Chris.Golden Changed method names, and altered
 *                                      the replace() method to allow a
 *                                      different number of items to be
 *                                      removed from the number being
 *                                      inserted.
 * Aug 22, 2016   19537    Chris.Golden Made passed-in list parameters more
 *                                      flexible with regard to their element
 *                                      types.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IListStateChanger<I, E> extends IWidget<I> {

    // Public Methods

    /**
     * Set the editability of the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its editability set.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param editable
     *            Flag indicating whether or not the list should be editable.
     */
    public void setEditable(I identifier, boolean editable);

    /**
     * Get the values of the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents retrieved.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @return Contents of the specified list.
     */
    public List<E> get(I identifier);

    /**
     * Clear the specified list of any contents.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents retrieved.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     */
    public void clear(I identifier);

    /**
     * Set the contents of the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents set. This
     *            may be <code>null</code> if this object only handles one
     *            particular list.
     * @param elements
     *            New contents for the specified list. Note that any associated
     *            {@link IListStateChangeHandler} should not be notified of
     *            values set in this fashion.
     */
    public void set(I identifier, List<? extends E> elements);

    /**
     * Add the specified element to the end of the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param element
     *            Element to be appended to the list.
     */
    public void addElement(I identifier, E element);

    /**
     * Add the specified elements to the end of the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param elements
     *            List of elements to be appended to the list.
     */
    public void addElements(I identifier, List<? extends E> elements);

    /**
     * Insert the specified element at the specified index of the specified
     * list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to insert the element.
     * @param element
     *            Element to be inserted into the list.
     */
    public void insertElement(I identifier, int index, E element);

    /**
     * Insert the specified elements at the specified index of the specified
     * list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to insert the elements.
     * @param elements
     *            List of elements to be inserted into the list.
     */
    public void insertElements(I identifier, int index,
            List<? extends E> elements);

    /**
     * Replace the element at the specified index in the specified list with the
     * specified new element.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to replace the existing element.
     * @param element
     *            Element to replace the element at the index in the list.
     */
    public void replaceElement(I identifier, int index, E element);

    /**
     * Replace the specified number of elements at the specified index in the
     * specified list with the specified new elements.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to start replacing the existing elements.
     * @param count
     *            Number of elements to be removed during replacement.
     * @param elements
     *            List of elements to replace the elements starting at the index
     *            in the list.
     */
    public void replaceElements(I identifier, int index, int count,
            List<? extends E> elements);

    /**
     * Remove the element at the specified index in the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to remove an existing element.
     */
    public void removeElement(I identifier, int index);

    /**
     * Remove the specified number of elements starting at the specified index
     * in the specified list.
     * 
     * @param identifier
     *            Identifier of the list widget to have its contents modified.
     *            This may be <code>null</code> if this object only handles one
     *            particular list.
     * @param index
     *            Index at which to start removing existing elements.
     * @param count
     *            Number of elements to be removed.
     */
    public void removeElements(I identifier, int index, int count);

    /**
     * Set the list state change handler to that specified. The handler will be
     * notified when the list state changes.
     * 
     * @param handler
     *            Handler to be used.
     */
    public void setListStateChangeHandler(IListStateChangeHandler<I, E> handler);
}
