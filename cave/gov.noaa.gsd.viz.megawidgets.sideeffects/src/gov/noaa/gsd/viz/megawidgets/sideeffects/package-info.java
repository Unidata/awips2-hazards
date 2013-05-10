/**
 * Contains {@link gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier} implementations. Each
 * such implementation provides ways to allow {@link gov.noaa.gsd.viz.megawidgets.INotifier}
 * invocations and {@link gov.noaa.gsd.viz.megawidgets.IStateful} state changes to trigger
 * the application of side effects, including the altering of the mutable properties of
 * other {@link gov.noaa.gsd.viz.megawidgets.IMegawidgets} instances managed by the same
 * {@link gov.noaa.gsd.viz.megawidgets.MegawidgetManager}.
 */
package gov.noaa.gsd.viz.megawidgets.sideeffects;