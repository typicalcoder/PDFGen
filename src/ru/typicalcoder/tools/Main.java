package ru.typicalcoder.tools;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import ru.typicalcoder.tools.pdf.PdfGenerate;
import ru.typicalcoder.tools.pdf.Slide;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

public class Main extends Application {

    Random random = new Random();
    List<PdfGenerate.PhotoPositions> half_tpl = Arrays.asList(PdfGenerate.PhotoPositions.HALF_1,PdfGenerate.PhotoPositions.HALF_2);
    List<PdfGenerate.PhotoPositions> triple_left_tpl = Arrays.asList(PdfGenerate.PhotoPositions.TRIPLE_LEFT_MAIN,PdfGenerate.PhotoPositions.TRIPLE_LEFT_1,PdfGenerate.PhotoPositions.TRIPLE_LEFT_2);
    List<PdfGenerate.PhotoPositions> triple_right_tpl = Arrays.asList(PdfGenerate.PhotoPositions.TRIPLE_RIGHT_MAIN,PdfGenerate.PhotoPositions.TRIPLE_RIGHT_1,PdfGenerate.PhotoPositions.TRIPLE_RIGHT_2);
    List<PdfGenerate.PhotoPositions> triple_tpl = Arrays.asList(PdfGenerate.PhotoPositions.TRIPLE_1,PdfGenerate.PhotoPositions.TRIPLE_2,PdfGenerate.PhotoPositions.TRIPLE_3);

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("picker.fxml"));
        primaryStage.setTitle("Апартоген");
        primaryStage.setScene(new Scene(root, 481, 321));
        primaryStage.show();

        TextArea name = (TextArea) root.lookup("#nameText");
        TextField descr1 = (TextField) root.lookup("#descr1");
        TextField descr2 = (TextField) root.lookup("#descr2");
        TextField descr3 = (TextField) root.lookup("#descr3");
        TextField descr4 = (TextField) root.lookup("#descr4");
        Button selectMain = (Button) root.lookup("#selectMain");
        String downloads_dir = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "{374DE290-123F-4565-9164-39C4925E467B}");
        FileChooser.ExtensionFilter filters = new FileChooser.ExtensionFilter("Изображения", "*.png","*.jpg","*.jpeg");

        selectMain.setOnAction((event -> {
            FileChooser mainChooser = new FileChooser();
            mainChooser.setTitle("Выберите главное изображение");
            mainChooser.getExtensionFilters().add(filters);
            mainChooser.setInitialDirectory(new File(downloads_dir));
            File choose = mainChooser.showOpenDialog(primaryStage);
            if (choose == null) return;

            List<Slide> slides = new ArrayList<>();

            try {
                BufferedImage mainImage = ImageIO.read(choose);
                mainImage = Helper.createCoveredCopy(mainImage, PdfGenerate.PhotoPositions.MAIN.width, PdfGenerate.PhotoPositions.MAIN.height);
                while(true){
                    FileChooser otherChooser = new FileChooser();
                    otherChooser.setTitle("Выберите изображения на слайд (2-3 фото)");
                    otherChooser.getExtensionFilters().add(filters);
                    otherChooser.setInitialDirectory(new File(choose.getParentFile().getAbsolutePath()));
                    List<File> chooseOther = otherChooser.showOpenMultipleDialog(primaryStage);
                    if (chooseOther == null) break;
                    if(chooseOther.size() > 3) {
                        JOptionPane.showMessageDialog(null, "Вы выбрали слишком много фото на слайд. Максимум: 3", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        continue;
                    } else if(chooseOther.size() < 2) {
                        JOptionPane.showMessageDialog(null, "Вы выбрали слишком мало фото на слайд. Минимум: 2", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    List<BufferedImage> otherImages = Helper.filesToImages(chooseOther);
                    Helper.addImagesToSlides(this, slides, otherImages);
                }
                FileChooser savePDFChoser = new FileChooser();
                savePDFChoser.setTitle("Выберите место для PDF");
                savePDFChoser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
                savePDFChoser.setInitialDirectory(new File(choose.getParentFile().getAbsolutePath()));
                File savePDF = savePDFChoser.showSaveDialog(primaryStage);
                if (savePDF == null) return;

                PdfGenerate.createDocument(mainImage, slides, name.getText(), Arrays.asList(descr1.getText(), descr2.getText(), descr3.getText(), descr4.getText()), savePDF.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
