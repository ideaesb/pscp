package pscp.restlet.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author iws
 */
public class ImageScaler {

    private File input;
    private File output;
    private int width;
    private int height;
    private float quality = .5f;
    private boolean hq;

    public void setInput(File file) {
        this.input = file;
    }

    public void setOutput(File file) {
        this.output = file;
    }

    public void setBounds(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setHQMethod(boolean hq) {
        this.hq = hq;
    }

    public void scale() throws Exception {
        BufferedImage src = ImageIO.read(input);
        BufferedImage dest;
        double scale = 1;
        if (src.getWidth() > src.getHeight()) {
            scale = width / (double) src.getWidth();
        } else if (src.getHeight() > src.getWidth()) {
            scale = height / (double) src.getHeight();
        }
        if (width >= src.getWidth() && height >= src.getHeight()) {
            scale = 1;
        }
        if (!hq) {
            dest = new BufferedImage((int) (src.getWidth() * scale), (int) (src.getHeight() * scale),
                    BufferedImage.TYPE_4BYTE_ABGR_PRE);
            Graphics2D g2 = dest.createGraphics();
            AffineTransform trans = AffineTransform.getScaleInstance(scale, scale);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawRenderedImage(src, trans);
        } else {
            dest = getScaledInstance(src, (int) (src.getWidth() * scale), (int) (src.getHeight() * scale),
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
        }
        String ext = output.getName().substring(output.getName().lastIndexOf('.') + 1);
        ImageWriter writer = ImageIO.getImageWritersBySuffix(ext).next();
        writer.setOutput(new FileImageOutputStream(output));
        ImageWriteParam params = writer.getDefaultWriteParam();
        if (params.canWriteCompressed()) {
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionType(params.getCompressionTypes()[0]);
            params.setCompressionQuality(quality);
        }
        writer.write(null, new IIOImage(dest, null, null), params);
    }

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    public BufferedImage getScaledInstance(BufferedImage img,
            int targetWidth,
            int targetHeight,
            Object hint,
            boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage) img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

    public static void main(String[] args) throws Exception {
        ImageScaler s = new ImageScaler();
        s.setBounds(256, 256);
        s.setHQMethod(true);
        for (String arg : args) {
            int qual = 5;
            while (qual <= 10) {
                File in = new File(arg);
                String out = arg;
                String suffix = String.format("-scaled-%d.gif", qual);
                out = out.replace(".jpg", suffix);
                out = out.replace(".gif", suffix);
                s.setInput(in);
                s.setOutput(new File(out));
                s.setQuality(qual / 10f);
                s.scale();
                qual += 1;
            }
        }
    }
}
