package ru.typicalcoder.tools;

import ru.typicalcoder.tools.pdf.Slide;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Helper {
    public static void drawStringMultiLine(Graphics2D g, String text, int lineWidth, int x, int y) {
        FontMetrics m = g.getFontMetrics();
        if(m.stringWidth(text) < lineWidth) {
            g.drawString(text, x, y);
        } else {
            String[] words = text.split(" ");
            StringBuilder currentLine = new StringBuilder(words[0]);
            for(int i = 1; i < words.length; i++) {
                if(m.stringWidth(currentLine+words[i]) < lineWidth) {
                    currentLine.append(" ").append(words[i]);
                } else {
                    g.drawString(currentLine.toString(), x, y);
                    y += m.getHeight();
                    currentLine = new StringBuilder(words[i]);
                }
            }
            if(currentLine.toString().trim().length() > 0) {
                g.drawString(currentLine.toString(), x, y);
            }
        }
    }

    static void addImagesToSlides(Main main, java.util.List<Slide> result, List<BufferedImage> bufferedImageList) {
        int places = bufferedImageList.size();
        Iterator<BufferedImage> bufListIter = bufferedImageList.iterator();
        Slide slide = new Slide();

        for (; bufListIter.hasNext(); ) {
            BufferedImage image = bufListIter.next();
            if(places > 0) {
                slide.photos.add(image);
                slide.count++;
                places--;
            } else break;
        }

        switch (slide.count) {
            case 2:
                slide.positions = main.half_tpl;
                break;
            case 3:
                slide.positions = main.random.nextBoolean() ? main.triple_left_tpl : main.triple_right_tpl;
                break;
        }

        result.add(slide);
    }

    static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public static BufferedImage createCoveredCopy(BufferedImage image, int needleWidth, int needleHeight) {

        double mainImageWidth = image.getWidth();
        double mainImageHeight = image.getHeight();
        if(mainImageWidth < needleWidth) {
            double computedHeight = (float)(needleWidth / mainImageWidth) * mainImageHeight;
            image = createResizedCopy(image, needleWidth, (int)Math.ceil(computedHeight), true);
        }
        if(mainImageHeight < needleHeight) {
            double computedWidth = (float)(needleHeight / mainImageHeight) * mainImageWidth;
            image = createResizedCopy(image, (int)Math.ceil(computedWidth), needleHeight, true);
        }
        int x_offset = (image.getWidth() - needleWidth) / 2;
        int y_offset = (image.getHeight() - needleHeight) / 2;

        image = image.getSubimage(x_offset, y_offset, needleWidth, needleHeight);
        return image;
    }

    static List<BufferedImage> filesToImages(List<File> files) {
        List<BufferedImage> images = new ArrayList<>();
        for(File file : files) {
            try {
                BufferedImage image = ImageIO.read(file);
                images.add(image);
            } catch (IOException ex){
                ex.printStackTrace();
            }
        }
        return images;
    }
}
