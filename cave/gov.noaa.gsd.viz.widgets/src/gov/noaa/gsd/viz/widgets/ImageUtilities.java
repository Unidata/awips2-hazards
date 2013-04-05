/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Image utilities.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ImageUtilities {

    // Public Static Methods

    /**
     * Convert the specified AWT image to an SWT image. The AWT image must use
     * the direct color model (i.e. not an indexed color palette). AWT images
     * are useful because they may be drawn on using colors that have alpha
     * transparency values.
     * 
     * @param awtImage
     *            AWT image to be converted.
     * @return SWT image.
     */
    public static Image convertAwtImageToSwt(BufferedImage awtImage) {
        DirectColorModel model = (DirectColorModel) awtImage.getColorModel();
        PaletteData palette = new PaletteData(model.getRedMask(),
                model.getGreenMask(), model.getBlueMask());
        ImageData imageData = new ImageData(awtImage.getWidth(),
                awtImage.getHeight(), model.getPixelSize(), palette);
        WritableRaster raster = awtImage.getRaster();
        int[] pixelArray = new int[4];
        for (int y = 0; y < imageData.height; y++) {
            for (int x = 0; x < imageData.width; x++) {
                raster.getPixel(x, y, pixelArray);
                int pixel = palette.getPixel(new RGB(pixelArray[0],
                        pixelArray[1], pixelArray[2]));
                imageData.setPixel(x, y, pixel);
                imageData.setAlpha(x, y, pixelArray[3]);
            }
        }
        return new Image(Display.getCurrent(), imageData);
    }
}
