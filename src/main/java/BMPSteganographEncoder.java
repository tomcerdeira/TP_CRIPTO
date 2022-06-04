import exceptions.FileTooLargetException;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class BMPSteganographEncoder {

    private final BMPEditor editor;
    private byte[] encodingBytes;

    public BMPSteganographEncoder(BufferedImage inputBMP, byte[] encodingBytes) throws IOException {
        editor = new BMPEditor(inputBMP);
        this.encodingBytes = encodingBytes; // TODO Agregar al principio los bytes de tamaño y al final la extension

        System.out.println(maxHiddenFileSize(editor));
    }

    public void LSB1() throws FileTooLargetException {
        if( (encodingBytes.length * 8) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));

        ByteIterator iterator = editor.byteIterator();
        byte currentByte;

        for(byte b : encodingBytes) {
            currentByte = b;
            for (int i = 0; i < 8; i++) {
                iterator.NextSetLeastSignificantBits(currentByte, 1);
                currentByte >>= 1;
            }
        }
    }

    public void LSB4() throws FileTooLargetException {
        if( (encodingBytes.length * 2) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 2));

        ByteIterator iterator = editor.byteIterator();

        for(byte b : encodingBytes) {
            iterator.NextSetLeastSignificantBits(b, 4);
            iterator.NextSetLeastSignificantBits((byte)(b>>4), 4);
        }
    }

    public void LSBImproved(){
        //TODO
    }

    private int maxHiddenFileSize(BMPEditor editor){
        return (editor.getImageHeight() * editor.getImageWidth() * 3) / 8;
    }


}
