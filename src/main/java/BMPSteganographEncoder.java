import exceptions.FileTooLargetException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BMPSteganographEncoder {

    private BMPEditor editor;
    private final byte[] originalImage;
    private final byte[] encodingBytes;

    public BMPEditor getEditor() {
        return editor;
    }

    public BMPSteganographEncoder(byte[] inputBMP, byte[] encodingBytes, String extension,ArgumentsParser argsParser) throws IOException {

        editor = new BMPEditor(inputBMP);
        originalImage = inputBMP;

        StringBuilder byteBuilderForInvertingBytes = new StringBuilder();

        // Agreamos al principio los bytes de tamaño y al final la extension TODO REVISAR
        byte[] len = ByteBuffer.allocate(4).putInt(encodingBytes.length).array();
        byte [] encodeAux = new byte[4+ encodingBytes.length + extension.getBytes(StandardCharsets.UTF_8).length];

        byte[] aux = new byte[encodingBytes.length];
        System.out.println(encodingBytes.length+" LEN");

        System.arraycopy(len, 0, encodeAux, 0, 4);
        System.arraycopy(encodingBytes, 0, encodeAux, 4, encodingBytes.length);

        for(int i= encodingBytes.length+4,j=0; j<extension.getBytes(StandardCharsets.UTF_8).length;j++,i++){
            encodeAux[i] = extension.getBytes(StandardCharsets.UTF_8)[j];
        }

        if (argsParser.encodeMode && !argsParser.revertMode) {
            FileOutputStream outputStream = new FileOutputStream("auxiliarToEncript");
            outputStream.write(encodeAux);
            outputStream.close();
            argsParser.encoder.encrypt("auxiliarToEncript", "encryptedFile");

            FileInputStream f2 = new FileInputStream("encryptedFile");
            byte[] f2ByteArray = f2.readAllBytes();
            byte[] encodedSecretayx = new byte[4+f2ByteArray.length];
            System.arraycopy(ByteBuffer.allocate(4).putInt(f2ByteArray.length).array(),0,encodedSecretayx,0, 4);
            System.arraycopy(f2ByteArray, 0, encodedSecretayx, 4, f2ByteArray.length);
            this.encodingBytes = encodedSecretayx;
            System.out.println("LEN"+ encodingBytes.length);
            System.out.println("LEN ENCR" + f2ByteArray.length);
        }else{
            this.encodingBytes = encodeAux;
        }
        System.out.println(maxHiddenFileSize(editor));
    }

    /// TAM ENCI || ENCR(

    // TODO: hacer funcion que retorne la extension del archivo escondido

    private static final int END_OF_HEADER_INDEX = 54;
    private static final int FILE_EXTENSION_LENGTH = 4;
    private static final int BYTES_OF_LENGTH_LSB4 = 8;
    private static final int BYTES_OF_LENGTH_LSB1 = 32;

    private static byte getBit(byte num,int pos){
        return (byte) ((num >> pos) & 1); // 0 menos significativo, 7 mas significativo
    }

    private static byte setBit(byte num,int pos, byte value){
        return (byte) ((num & ~(1 << pos)) | (value << pos));

    }
    
    public void LSB1() throws FileTooLargetException {

        if( (encodingBytes.length * 8) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));

        byte[] image = this.editor.getCoverImageBytes();
        byte [] encodeData = this.encodingBytes;

        int indexImage = END_OF_HEADER_INDEX;

        for(int currentIndex=0; currentIndex<encodeData.length; currentIndex++){

           int currentEncodedByte = encodeData[currentIndex];

           for (int bytePart=0; bytePart<8; bytePart++,indexImage++){

               byte valueOfCurrentBit = getBit((byte) currentEncodedByte, 7-bytePart);
               image[indexImage] = setBit(image[indexImage], 0, valueOfCurrentBit);

           }
        }

        this.editor.setBytes(image);
    }

    public void LSB4() throws FileTooLargetException {

        if( (encodingBytes.length * 2) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 2));

        byte[] image = this.editor.getCoverImageBytes();
        byte [] encodeData = this.encodingBytes;

        int indexImage = END_OF_HEADER_INDEX;

        for(int currentIndex=0;currentIndex<encodeData.length;currentIndex++){

            int currentEncodedByte = encodeData[currentIndex];

            for (int bytePart=0; bytePart<2; bytePart++, indexImage++){

                for(int k=0;k<4;k++) {

                    byte valueOfCurrentBit = getBit((byte) currentEncodedByte, 7 - k);
                    image[indexImage] = setBit(image[indexImage], 3 - k, valueOfCurrentBit);

                }

                currentEncodedByte <<= 4;

            }
        }

        this.editor.setBytes(image);
    }

    // TODO: revisar su funcionamiento
    public void LSBImproved() throws FileTooLargetException, IOException {

        if( (encodingBytes.length * 8) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));

        // Copia auxiliar de la imagen portadora
        byte[] auxCoverImageBytes = editor.getCoverImageBytes();

        // Paso de hacerle LSB1 a la imagen portadora con los bytes a ocultar
        BMPEditor oldEditor = this.editor;

        this.editor = new BMPEditor(originalImage);
        this.LSB1();

        // Copia auxiliar de la imagen portadora + bytes a ocultar
        byte[] auxStegoImage = this.editor.getCoverImageBytes();

        this.editor = oldEditor;

        int index = 0;

        // Variables para llevar la cuenta de en cuantos bytes cambia el LSB en base al patron
        int countEqual00 = 0;
        int countDistinct00 = 0;
        int countEqual01 = 0;
        int countDistinct01 = 0;
        int countEqual10 = 0;
        int countDistinct10 = 0;
        int countEqual11 = 0;
        int countDistinct11 = 0;

        for (byte b : auxCoverImageBytes){

            // Salteamos el header del .bmp
            if (index < this.editor.getBitArrayOffset()){
                index++;
                continue;
            }

            if ((b^auxStegoImage[index]) == 0){ // Si el LSB NO cambio

                // 6 ==> 00000110, la hacer un AND con ese byte, obtenemos el patron
                switch (b & 6){

                    // 0 ==> 00000000
                    case 0: countEqual00++; break;

                    // 2 ==> 00000010
                    case 2: countEqual01++; break;

                    // 4 ==> 00000100
                    case 4: countEqual10++; break;

                    // 6 ==> 00000110
                    case 6: countEqual11++; break;
                }
            }else{ // Si el LSB cambio

                switch (b & 6){
                    case 0: countDistinct00++; break;
                    case 2: countDistinct01++; break;
                    case 4: countDistinct10++; break;
                    case 6: countDistinct11++; break;
                }
            }
            index++;
        }

        index = 0;

        for (byte b : auxCoverImageBytes){

            if (index < this.editor.getBitArrayOffset()){
                index++;
                continue;
            }

            switch (b & 6){
                case 0:
                    // Si la cantidad que cambiaron en el LSB1 es mayor a los que no, el ultimo bit debe invertirse
                    if(countEqual00 < countDistinct00){
                        if (b % 2 == 0){ // Es par ==> el byte termina en 0
                            auxStegoImage[index] += 1; // Le sumamos 1 para que termine en 1
                        }else{ // Es impar ==> el bute termina en 1
                            auxStegoImage[index] -= 1; // Le restamos 1 para que termine en 0
                        }
                    }
                    break;
                case 2:
                    if(countEqual01 < countDistinct01){
                        if (b % 2 == 0){
                            auxStegoImage[index] += 1;
                        }else{
                            auxStegoImage[index] -= 1;
                        }
                    }
                    break;
                case 4:
                    if(countEqual10 < countDistinct10){
                        if (b % 2 == 0){
                            auxStegoImage[index] += 1;
                        }else{
                            auxStegoImage[index] -= 1;
                        }
                    }
                    break;
                case 6:
                    if(countEqual11 < countDistinct11){
                        if (b % 2 == 0){
                            auxStegoImage[index] += 1;
                        }else{
                            auxStegoImage[index] -= 1;
                        }
                    }
                    break;
            }
            index++;
        }

        // Actualizamos la imagen a LSBImproved( portadora + bytes a ocultar )
        this.editor.setBytes(auxStegoImage);

        // Debemos guardar el patron para luego poder hacer la des-estenografia
        int pattern = 0; // 00000000

        if (countEqual00 < countDistinct00){
            pattern += 8; // 00001000
        }

        if (countEqual01 < countDistinct01){
            pattern += 4; // 00000100
        }

        if (countEqual10 < countDistinct10){
            pattern += 2; // 00000010
        }

        if (countEqual11 < countDistinct11){
            pattern += 1; // 00000001
        }

        System.out.println(pattern);
        // TODO: hay que guardar este patron en los primero 4bytes de la imagen (leer mail Ana 6/06 )

    }



    private int maxHiddenFileSize(BMPEditor editor){
        return (editor.getImageHeight() * editor.getImageWidth() * 3) / 8;
    }


}
