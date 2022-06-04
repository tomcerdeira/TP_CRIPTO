import exceptions.BitsPerPixelException;
import exceptions.CompressionMethodException;
import exceptions.NotVersion3BMPException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BMPEditor {

    /*
    * You can find all the header properties and offsets in the following wikipedia page
    *  https://en.wikipedia.org/wiki/BMP_file_format#Bitmap_file_header
    */

    private static final int HEADER_FIELD = 0x00; // 2 Bytes
    private static final short HEADER_FIELD_VERSION3 = 0x4D42; // BMP Version 3 Identifier

    private static final int BIT_ARRAY_OFFSET = 0x0A; // 4 Bytes

    private static final int COMPRESSION_METHOD = 0x1E; // 4 Bytes
    private static final int COMPRESSION_METHOD_REQUIRED = 0; // BMP requires NONE Compression method

    private static final int BITMAP_WIDTH = 0x12; // 4 Bytes
    private static final int BITMAP_HEIGHT = 0x16; // 4 Bytes

    private static final int BITS_PER_PIXEL = 0x1C; // 2 Bytes
    private static final short BITS_PER_PIXEL_REQUIRED = 24; // BMP requires 24 bits per pixel

    private byte[] bytes;

    private static int bitArrayOffset;

    private final int imageWidth;
    private final int imageHeight;

    private final short bitsPerPixel;
    private final short bytesPerPixel;

    private final int rowBytesSize;

    private final int bitArraySize;

    public BMPEditor(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", baos);

        bytes = baos.toByteArray();

        if(!verifyVersion3())
            throw new NotVersion3BMPException();
        if(!verifyCompressionMethod())
            throw new CompressionMethodException();
        if(!verifyBitsPerPixel())
            throw new BitsPerPixelException();

        bitArrayOffset = getIntValue(BIT_ARRAY_OFFSET);
        bitArraySize = bytes.length - bitArrayOffset;

        // Since no compression method is used Bitmap_Width == Image_Width
        imageWidth = getIntValue(BITMAP_WIDTH);
        imageHeight = getIntValue(BITMAP_HEIGHT);

        bitsPerPixel = getShortValue(BITS_PER_PIXEL);
        bytesPerPixel = (short) (bitsPerPixel / 8);

        rowBytesSize = (int)( ((float)bitsPerPixel * imageWidth + 31) / 32) * 4;
    }

    private boolean verifyVersion3(){
        return getShortValue(HEADER_FIELD) == HEADER_FIELD_VERSION3;
    }

    private boolean verifyCompressionMethod(){
        return getIntValue(COMPRESSION_METHOD) == COMPRESSION_METHOD_REQUIRED;
    }

    private boolean verifyBitsPerPixel(){
        return getShortValue(BITS_PER_PIXEL) == BITS_PER_PIXEL_REQUIRED;
    }

    public void setByte(int index, byte b){
        bytes[index] = b;
    }

    public byte getBitArrayByte(int index) {
        return bytes[bitArrayOffset + index];
    }

    public void setBitArrayByte(int index, byte b) {
        bytes[bitArrayOffset + index] = b;
    }

    public byte[] getPixel(int index){
        int x = index % imageWidth;
        int y = index / imageWidth;
        int paddedIndex = y * rowBytesSize + x * bytesPerPixel;
        return Arrays.copyOfRange(bytes, bitArrayOffset+paddedIndex, bitArrayOffset+paddedIndex+bytesPerPixel);
    }

    public void setPixel(int index, byte[] bgr) {
        int x = index % imageWidth;
        int y = index / imageWidth;
        int paddedIndex = y * rowBytesSize + x * bytesPerPixel;

        for(int i = 0; i < bytesPerPixel; i++)
            bytes[bitArrayOffset + paddedIndex + i] = bgr[i];
    }

    public void outputToFile(String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream("./"+fileName+".bmp")) {
            fos.write(bytes);
        }
    }

    private short getShortValue(int offset) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes, offset, 2);
        wrapped.order(ByteOrder.LITTLE_ENDIAN); // All integer values in BMP are stored in LITTLE ENDIAN
        return wrapped.getShort();
    }

    private int getIntValue(int offset) {
        ByteBuffer wrapped = ByteBuffer.wrap(bytes, offset, 4);
        wrapped.order(ByteOrder.LITTLE_ENDIAN); // All integer values in BMP are stored in LITTLE ENDIAN
        return wrapped.getInt();
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getBitsPerPixel() {
        return bitsPerPixel;
    }

    public int getBitArraySize() {
        return bitArraySize;
    }

    public ByteIterator byteIterator(){
        return new ByteIterator(this);
    }
}
