package pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PageMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author Muhammed Demirbaş
 * @since 2016-05-15, 16:58
 */
public class CombineIntoPDF {
    public static void combineImagesIntoPDF(String pdfPath, HashMap<String, String> menuMap, String... inputDirsAndFiles) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            //新建目录
            PDDocumentOutline outline =  new PDDocumentOutline();
            //目录挂载上
            doc.getDocumentCatalog().setDocumentOutline( outline );
            doc.getDocumentCatalog().setPageMode(PageMode.USE_OUTLINES);


            for (String input : inputDirsAndFiles) {
                Files.find(Paths.get(input),
                                Integer.MAX_VALUE,
                                (path, basicFileAttributes) -> Files.isRegularFile(path))
                        .forEachOrdered(path -> addImageAsNewPage(doc, menuMap, path.toString()));
            }
            doc.save(pdfPath);
        }
    }

    private static void addImageAsNewPage(PDDocument doc, HashMap<String, String> menuMap, String imagePath) {
        try {
            PDImageXObject image = PDImageXObject.createFromFile(imagePath, doc);
            PDRectangle pageSize = PDRectangle.A4;

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();
            float pageWidth = pageSize.getWidth();
            float pageHeight = pageSize.getHeight();
            float ratio = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            float scaledWidth = originalWidth * ratio;
            float scaledHeight = originalHeight * ratio;
            float x = (pageWidth - scaledWidth) / 2;
            float y = (pageHeight - scaledHeight) / 2;

            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
            System.out.println("Added: " + imagePath);
            String title = menuMap.get(imagePath);
            if (title != null){
                PDDocumentOutline outline = doc.getDocumentCatalog().getDocumentOutline();

                PDOutlineItem pagesOutline = new PDOutlineItem();
                PDPageDestination dest = new PDPageFitWidthDestination();
                dest.setPage( page );
                pagesOutline.setDestination( dest );
                pagesOutline.setTitle( title );
                outline.addLast( pagesOutline );
            }
        } catch (IOException e) {
            System.err.println("Failed to process: " + imagePath);
            e.printStackTrace(System.err);
        }
    }
}