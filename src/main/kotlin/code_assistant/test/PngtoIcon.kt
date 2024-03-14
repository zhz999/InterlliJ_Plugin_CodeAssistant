package code_assistant.test
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun convertPngToIcon(pngFilePath: String, iconFilePath: String) {
    val pngImage = ImageIO.read(File(pngFilePath))
    val iconImage = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
    val g2d = iconImage.createGraphics()
    g2d.drawImage(pngImage.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH), 0, 0, null)
    g2d.dispose()
    ImageIO.write(iconImage, "ICO", File(iconFilePath))
}

fun main() {
    val pngFilePath = "path/to/input.png"
    val iconFilePath = "path/to/output.ico"
    convertPngToIcon(pngFilePath, iconFilePath)
}
