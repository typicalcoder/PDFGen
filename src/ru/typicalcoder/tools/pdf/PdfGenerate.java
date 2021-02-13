package ru.typicalcoder.tools.pdf;


import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;
import ru.typicalcoder.tools.Helper;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class PdfGenerate {
    static int OFFSET = 10;
    static int PAGE_WIDTH = 1500;
    static int PAGE_HEIGHT = 810;
    static int tries = 0;

    public enum PhotoPositions {
        MAIN(10, 10, 1480, 790),

        HALF_1(10, 10, 735, 790),
        HALF_2(755, 10, 735, 790),

        TRIPLE_LEFT_MAIN(10, 10, 735, 790),
        TRIPLE_LEFT_1(755, 10, 735, 390),
        TRIPLE_LEFT_2(755, 410, 735, 390),

        TRIPLE_RIGHT_1(10, 10, 735, 390),
        TRIPLE_RIGHT_2(10, 410, 735, 390),
        TRIPLE_RIGHT_MAIN(755, 10, 735, 790),

        TRIPLE_1(10, 10, 480, 790),
        TRIPLE_2(505, 10, 480, 790),
        TRIPLE_3(995, 10, 480, 790);

        public final int x;
        public final int y;
        public final int width;
        public final int height;

        PhotoPositions(int x_ , int y_, int width_, int height_){
            x = x_;
            y = y_;
            width = width_;
            height = height_;
        }
    }

    public static void createDocument(BufferedImage mainImage, java.util.List<Slide> otherImages, String title, java.util.List<String> description, String savePath) {
        try {
            PdfWriter writer = new PdfWriter(savePath);
            writer.setSmartMode(true);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(new PageSize(PAGE_WIDTH, PAGE_HEIGHT));
            Document document = new Document(pdf);
            document.setMargins(OFFSET, OFFSET, OFFSET, OFFSET);
            PdfFont russian = PdfFontFactory.createFont("ClearSans-Medium.ttf", "CP1251", true);

            // Background
            Graphics2D titlePaneGr = mainImage.createGraphics();
            titlePaneGr.setPaint( new Color(0xABB0B0B0, true) );
            titlePaneGr.fillRect ( 1000, 100, 400, 150 );
            titlePaneGr.fillRect ( 30, 450, 620, 320 );

            ImageData data = ImageDataFactory.create(mainImage, Color.WHITE);
            Image image = new Image(data);
            image.setFixedPosition(PhotoPositions.MAIN.x, PhotoPositions.MAIN.y);
            document.add(image);

            Paragraph titleP = new Paragraph();
            titleP.setFontSize(42);
            titleP.add(new Text(title.toUpperCase()));
            titleP.setFont(russian);
            titleP.setTextAlignment(TextAlignment.CENTER);
            titleP.setFontColor(ColorConstants.BLACK);
            titleP.setSpacingRatio(1);
            titleP.setFixedPosition(1040, PAGE_HEIGHT - 220 - (russian.getWidth(title, 42) > 300 ? 42 : 0), 340);
            document.add(titleP);

            Div descrDiv = new Div();
            descrDiv.setFontSize(38);
            descrDiv.add(new Paragraph("ЭТАЖ "+description.get(0).toUpperCase()));
            descrDiv.add(new Paragraph("ПЛОЩАДЬ "+description.get(1).toUpperCase()+" М.КВ."));
            descrDiv.add(new Paragraph(description.get(2).toUpperCase()+" "));
            descrDiv.add(new Paragraph("СТОИМОСТЬ: "+description.get(3).toUpperCase()));
            descrDiv.setFont(russian);
            descrDiv.setTextAlignment(TextAlignment.LEFT);
            descrDiv.setFontColor(ColorConstants.BLACK);
            descrDiv.setSpacingRatio(1);
            descrDiv.setFixedPosition(60, 40, 580);
            document.add(descrDiv);

            for(Slide slide : otherImages) {
                generatePage(document, slide);
            }
            document.close();
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(new File(savePath));
            }
            JOptionPane.showMessageDialog(null, "PDF сохранен по пути: "+savePath, "УСПЕХ", JOptionPane.INFORMATION_MESSAGE);
            tries = 0;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Закройте существующий PDF", "Ошибка доступа к файлу", JOptionPane.ERROR_MESSAGE);
            if(tries < 4) createDocument(mainImage, otherImages, title, description, savePath);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            tries = 0;
        }
    }

    private static void generatePage(Document document, Slide slide) throws IOException {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        int index = 0;
        for(BufferedImage bImage : slide.photos) {
            bImage = Helper.createCoveredCopy(bImage, slide.positions.get(index).width, slide.positions.get(index).height);
            ImageData data = ImageDataFactory.create(bImage, Color.WHITE);
            Image image = new Image(data);
            image.setFixedPosition(slide.positions.get(index).x, slide.positions.get(index).y);
            document.add(image);
            index++;
        }
    }
}
