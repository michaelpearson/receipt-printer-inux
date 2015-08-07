import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main {
    private static final String PRINTER = "/dev/usb/lp0";
    private static Printer printer;

    public static void main(String argv[]) {
        try {
            printer = new Printer(PRINTER);
        } catch (IOException e) {
            System.err.println("Cannot open printer, " + e.getMessage());
            System.exit(1);
        }
        try {
            BufferedImage image = ImageIO.read(new File(argv[0]));
            printer.printImage(image);
            printer.writeString("\n\n\n\n\n");
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Could not open image, please specify an image in the first argument");
        }
    }
}


