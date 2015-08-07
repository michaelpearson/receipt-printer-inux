import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by michael on 3/08/15.
 */
public class Printer {
    private OutputStream output;
    public Printer(String printer) throws FileNotFoundException {
        output = new FileOutputStream(printer);
    }
    public void writeString(String data) {
        byte[] buff = new byte[data.length()];
        int a = 0;
        for(char c : data.toCharArray()) {
            buff[a++] = (byte)c;
        }
        try {
            output.write(buff);
        } catch(IOException e) {
            handelException(e);
        }
    }

    public void flush() {
        try {
            output.flush();
        } catch(IOException e) {
            handelException(e);
        }
    }
    public void writeByte(int b) {
        try {
            output.write((byte)(b&0xFF));
            output.flush();
        } catch (IOException e) {
            handelException(e);
        }
    }

    public void printImage(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        byte greyscale[][] = new byte[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                byte val = getGreyscale(image.getRGB(x, y));
                greyscale[x][y] = val;
            }
        }
        printRawImage(ditherImage(greyscale));
    }

    private static byte getGreyscale(int color) {
        Color c = new Color(color);
        int v = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
        return((byte)v);
    }

    private static boolean[][] ditherImage(byte[][] image) {
        for(int y = 0; y < image[0].length; y++) {
            for(int x = 0; x < image.length; x++) {
                int cc = image[x][y] & 0xFF;
                int rc = (cc < 128) ? 0 : 255;
                int err = cc - rc;
                image[x][y] = (byte)rc;
                try {
                    image[x + 1][y    ] += (err*5)>>4;
                    image[x - 1][y + 1] += (err*3)>>4;
                    image[x    ][y + 1] += (err*5)>>4;
                    image[x + 1][y + 1] += (err*1)>>4;
                } catch(ArrayIndexOutOfBoundsException e) {}
            }
        }
        boolean newImage[][] = new boolean[image.length][image[0].length];
        for(int x = 0;x < image.length;x++) {
            for (int y = 0; y < image[x].length; y++) {
                newImage[x][y] = image[x][y] == 0;
            }
        }
        return(newImage);
    }

    public void printRawImage(boolean[][] image) {
        writeByte(0x1B); //ESC
        writeByte(0x50); //Reset
        flush();

        writeByte(0x1B); //ESC
        writeByte(0x33); //Set Line Height
        writeByte(24);   //24 dots?... yeah, 24 dots.
        flush();

        int width = Math.min(image.length, 380);
        int height = image[0].length;

        for(int y = 0; y < height; y = y + 24) {
            writeByte(0x1B);
            writeByte(0x2A);
            writeByte(33);
            writeByte(width & 255);
            writeByte((width >> 8) & 0b111);
            for(int x = 0; x < width; x++) {
                int b1 = 0, b2 = 0, b3 = 0;
                try {
                    b1 |= image[x][y + 0] ? 128 : 0;
                    b1 |= image[x][y + 1] ? 64 : 0;
                    b1 |= image[x][y + 2] ? 32 : 0;
                    b1 |= image[x][y + 3] ? 16 : 0;
                    b1 |= image[x][y + 4] ? 8 : 0;
                    b1 |= image[x][y + 5] ? 4 : 0;
                    b1 |= image[x][y + 6] ? 2 : 0;
                    b1 |= image[x][y + 7] ? 1 : 0;
                    b2 |= image[x][y + 8] ? 128 : 0;
                    b2 |= image[x][y + 9] ? 64 : 0;
                    b2 |= image[x][y + 10] ? 32 : 0;
                    b2 |= image[x][y + 11] ? 16 : 0;
                    b2 |= image[x][y + 12] ? 8 : 0;
                    b2 |= image[x][y + 13] ? 4 : 0;
                    b2 |= image[x][y + 14] ? 2 : 0;
                    b2 |= image[x][y + 15] ? 1 : 0;
                    b3 |= image[x][y + 16] ? 128 : 0;
                    b3 |= image[x][y + 17] ? 64 : 0;
                    b3 |= image[x][y + 18] ? 32 : 0;
                    b3 |= image[x][y + 19] ? 16 : 0;
                    b3 |= image[x][y + 20] ? 8 : 0;
                    b3 |= image[x][y + 21] ? 4 : 0;
                    b3 |= image[x][y + 22] ? 2 : 0;
                    b3 |= image[x][y + 23] ? 1 : 0;
                } catch(ArrayIndexOutOfBoundsException e) {}
                writeByte(b1);
                writeByte(b2);
                writeByte(b3);
            }
            writeByte(0x0A); //LF
            flush();
        }
    }


    private void handelException(IOException e) {
        System.err.println("An exception occured communicating with the printer.");
        System.exit(1);
    }
}


