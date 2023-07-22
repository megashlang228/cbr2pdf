import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix
import java.awt.Dimension
import java.awt.geom.AffineTransform
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


fun main(args: Array<String>) {
    println("Запуск...")
    val targetPath = "D:\\comix\\blame\\pdf"
    val zipFile = "D:\\comix\\blame\\cbr"
    cbr2pdf(targetPath, zipFile)
}

fun cbr2pdf(targetPath: String, zipFilesDirectory: String){
    val files = File(zipFilesDirectory).listFiles() ?: return
    for((index, file) in files.withIndex()){
        println("Начать извлечение: ${file.name} (${index + 1}/${files.size})")
        val fileName = file.name
        val photosPath = "${File("").absolutePath}\\photos\\"
        unzipFile(photosPath, file.path)
        createPdfFromImages(targetPath, photosPath, fileName)
        File(photosPath).listFiles()?.forEach { photoFile ->
            photoFile.delete()
        }
    }
}

fun createPdfFromImages(targetPath: String, photosPath: String, pdfFileName: String){
    val document = PDDocument()
    val file = File(photosPath)
    val fileList = file.listFiles() ?: throw RuntimeException()

    for (filePath in fileList) {
        val image = PDImageXObject.createFromFile(filePath.path, document)
        val page = if (image.width > image.height) {
            PDPage(PDRectangle(PDRectangle.A4.height, PDRectangle.A4.width))
        }else PDPage()
        document.addPage(page)
        val contentStream = PDPageContentStream(document, page)

        contentStream.drawImage(image, 0f, 0f, page.cropBox.width, page.cropBox.height)
        contentStream.close()
    }
    val pdfFile = File("$targetPath\\$pdfFileName.pdf")
    document.save(pdfFile)
    document.close()
    
    println("создан файл $pdfFileName")

}

fun unzipFile(targetPath: String, zipFilePath: String) {

    try {
        val zipFile = File(zipFilePath);
        val instr = FileInputStream(zipFile);
        val zis = ZipInputStream(instr);
        var entry: ZipEntry?
        entry = zis.nextEntry
        while (entry != null) {
            val zipPath = entry.name;
            try {

                if (entry.isDirectory) {
                    val zipFolder = File(
                        targetPath + File.separator
                                + zipPath
                    );
                    if (!zipFolder.exists()) {
                        zipFolder.mkdirs();
                    }
                } else {
                    val file = File(
                        targetPath + File.separator
                                + zipPath
                    );
                    if (!file.exists()) {
                        val pathDir = file.parentFile;
                        pathDir.mkdirs();
                        file.createNewFile();
                    }

                    val fos = FileOutputStream(file);
                    var bread: Int;
                    bread = zis.read()
                    while (bread != -1) {
                        fos.write(bread);
                        bread = zis.read()
                    }
                    fos.close();

                }
                System.out.println("Успешно распаковано:" + zipPath);

            } catch (e: Exception) {
                println("Разархивировать" + zipPath + "Ошибка")
                continue;
            }
            entry = zis.nextEntry
            if (entry == null) break
        }
        zis.close();
        instr.close();
        println("Конец распаковки");
    } catch (e: Exception) {
        e.printStackTrace();
    }

}