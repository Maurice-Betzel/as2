//$Header: /chessopening/de/mendelson/util/ImageUtil.java 5     4/28/17 3:46p Heller $
package de.mendelson.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;


/*
 * Copyright (C) mendelson-e-commerce GmbH Berlin Germany
 *
 * This software is subject to the license agreement set forth in the license.
 * Please read and agree to all terms before using this software.
 * Other product and brand names are trademarks of their respective owners.
 */
/**
 * Class that contains routines for the image processing
 *
 * @author S.Heller
 * @version $Revision: 5 $
 */
public class ImageUtil {

    /**
     * Composite used to paint the "hidden" element transparent
     */
    private Composite compositeTransparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    /**
     * Replaces a single color in the passed image and returns the new one
     *
     * @param background original image to set new rgb values in
     * @param hexColorOld RGB hex str for the old color to replace
     * @param hexColorNew RGB hex str for the new, replacing color
     */
    public ImageIcon replaceColor(ImageIcon background, String hexColorOld, String hexColorNew) {
        if (hexColorOld.startsWith("#")) {
            hexColorOld = hexColorOld.substring(1);
        }
        if (hexColorNew.startsWith("#")) {
            hexColorNew = hexColorNew.substring(1);
        }
        if (!hexColorOld.startsWith("0x")) {
            hexColorOld = "0x" + hexColorOld;
        }
        if (!hexColorNew.startsWith("0x")) {
            hexColorNew = "0x" + hexColorNew;
        }
        Color oldColor = Color.decode(hexColorOld);
        Color newColor = Color.decode(hexColorNew);
        int oldColorRGB = oldColor.getRGB();
        int newColorRGB = newColor.getRGB();
        BufferedImage image = new BufferedImage(
                background.getIconWidth(),
                background.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.drawImage(background.getImage(), 0, 0, null);
        for (int x = 0; x < background.getIconWidth(); x++) {
            for (int y = 0; y < background.getIconHeight(); y++) {
                if (image.getRGB(x, y) == oldColorRGB) {
                    image.setRGB(x, y, newColorRGB);
                }
            }
        }
        return (new ImageIcon(image));
    }

    /**
     * Mixes two images, the foreground image is painted onto the background
     * image
     *
     * @param background Background image, is painted first
     * @param foreground Foreground image, is painted second
     */
    public ImageIcon mixImages(ImageIcon background, ImageIcon foreground) {
        BufferedImage image = new BufferedImage(
                background.getIconWidth(),
                background.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.drawImage(background.getImage(), 0, 0, null);
        int foregroundOffsetX = background.getIconWidth() - foreground.getIconWidth();
        if (foregroundOffsetX < 0) {
            foregroundOffsetX = 0;
        }
        int foregroundOffsetY = background.getIconHeight() - foreground.getIconHeight();
        if (foregroundOffsetY < 0) {
            foregroundOffsetY = 0;
        }
        g.drawImage(foreground.getImage(), foregroundOffsetX, foregroundOffsetY, null);
        return (new ImageIcon(image));
    }

    /**
     * Turns the passed icon into a transparent image, this is used to mark a
     * hidden element
     */
    public ImageIcon transparentImage(ImageIcon icon) {
        BufferedImage image = new BufferedImage(
                icon.getIconWidth(),
                icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setComposite(this.compositeTransparent);
        g.drawImage(icon.getImage(), 0, 0, null);
        return (new ImageIcon(image));
    }

    /**
     * Turns the passed icon into a grayed out and returns it
     *
     * @param brightness brightness from 0-100
     */
    public ImageIcon grayImage(ImageIcon icon, int brightness) {
        Image image = icon.getImage();
        ImageFilter filter = new GrayFilter(true, brightness);
        FilteredImageSource filteredSrc = new FilteredImageSource(image.getSource(), filter);
        image = Toolkit.getDefaultToolkit().createImage(filteredSrc);
        return (new ImageIcon(image));
    }

    /**
     * Scales a passed image icon to a new size and returns this
     */
    public ImageIcon scaleWidthKeepingProportions(ImageIcon icon, int newWidth) {
        BufferedImage sourceImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = sourceImage.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        BufferedImage targetImage = this.scaleWidthKeepingProportions(sourceImage, newWidth);
        return (new ImageIcon(targetImage));
    }

    /**
     * Scales a passed image icon to a new size and returns this
     */
    public ImageIcon scaleHeightKeepingProportions(ImageIcon icon, int newHeight) {
        BufferedImage sourceImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = sourceImage.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();
        BufferedImage targetImage = this.scaleHeightKeepingProportions(sourceImage, newHeight);
        return (new ImageIcon(targetImage));
    }
    
    /**
     * Scales a passed buffered image to a new size and returns this - keeping the proportions
     *
     * @return
     */
    public BufferedImage scale(BufferedImage sourceImage, int newWidth, int newHeight) {
        BufferedImage targetImage = new BufferedImage(newWidth, newHeight, sourceImage.getType());
        Graphics2D g = targetImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(sourceImage, 0, 0, newWidth, newHeight, 0, 0, sourceImage.getWidth(),
                sourceImage.getHeight(), null);
        g.dispose();
        return( targetImage);
    }
    
     /**
     * Scales a passed buffered image to a new width and returns this
     *
     * @return
     */
    public BufferedImage scaleWidthKeepingProportions(BufferedImage sourceImage, int newWidth) {
        double scaleFactor = (double)newWidth/(double)sourceImage.getWidth();
        return( this.scale( sourceImage, (int)(sourceImage.getWidth()*scaleFactor), (int)(sourceImage.getHeight()*scaleFactor)));
    }
    
    /**
     * Scales a passed buffered image to a new width and returns this
     *
     * @return
     */
    public BufferedImage scaleHeightKeepingProportions(BufferedImage sourceImage, int newHeight) {
        double scaleFactor = (double)newHeight/(double)sourceImage.getHeight();
        return( this.scale( sourceImage, (int)(sourceImage.getWidth()*scaleFactor), (int)(sourceImage.getHeight()*scaleFactor)));
    }
   
    
}
