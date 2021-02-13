package ru.typicalcoder.tools.pdf;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Slide {
    public int count = 0;
    public List<BufferedImage> photos = new ArrayList<>();
    public List<PdfGenerate.PhotoPositions> positions = new ArrayList<>();
}
