import exceptions.FileTooLargetException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class BMPSteganographEncoder {

    private BMPEditor editor;
    private final BufferedImage originalImage;
    private byte[] encodingBytes;

    public BMPEditor getEditor() {
        return editor;
    }

    public BMPSteganographEncoder(BufferedImage inputBMP, byte[] encodingBytes, String extension) throws IOException {

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

        this.encodingBytes = encodeAux;

        System.out.println(maxHiddenFileSize(editor));
    }

//    public void LSB1() throws FileTooLargetException {
//        if( (encodingBytes.length * 8) > editor.getBitArraySize())
//            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));
//        ByteIterator iterator = editor.byteIterator();
//        byte currentByte;
//
//        for(byte b : encodingBytes) {
//            currentByte = b;
//            for (int i = 0; i <8; i++) {
//                iterator.NextSetLeastSignificantBits(currentByte, 1);
//                currentByte >>= 1;
//            }
//        }
//    }

    public void LSB1() throws FileTooLargetException {
        if( (encodingBytes.length * 8) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));

        byte[] image = this.editor.getCoverImageBytes();
        byte [] encodeData = this.encodingBytes;
        int indexImage = 54;

        for(int i=0;i<encodeData.length;i++){
           int encodeByte = encodeData[i];
           for (int j=0;j<8;j++,indexImage++){
               int bit = encodeByte & 0x80;
               encodeByte <<= 1;
               bit >>=7;
               if (bit == 0){
                   if (image[indexImage] % 2 != 0){
                       image[indexImage] -= 1;
                   }
               }else{
                   if (image[indexImage] % 2 == 0){
                       image[indexImage] += 1;
                   }
               }
           }
        }

        this.editor.setBytes(image);
    }


    public static byte[] revertLSB1(byte[] aux) throws IOException {
        byte[] image = aux;

        // Obtenemos los bytes del largo de la data que se escondio en la imagen portadora
        byte[] len = new byte[4] ;
        StringBuilder byteBuilderForLength = new StringBuilder();
        int index = 0;
        for (int i=54 , j=0; i<54+32; i++){ // La condicion de corte es desde los headers hasta obtener los 4 bytes del largo del archivo

            // Leemos un byte de la imagen y nos quedamos con su ultimo bit
            byte b = image[i];
            int lastBit = b & 1;
            byteBuilderForLength.append(lastBit);
            if(j==7){
                /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
                len[index++] = (byte)Integer.parseInt(byteBuilderForLength.toString(), 2);
                byteBuilderForLength = new StringBuilder();
                j=0;
            }else {
                j++;
            }
        }

        // Transformamos esos bytes a un int para facilitar su lecutra
        int encodedDataLength= new BigInteger(len).intValue() + 4; // LE agremos 4 bytes mas para leer la extension al final del archivo
        System.out.println(encodedDataLength-4);
        // Calculamos la cantidad de bytes de la imagen portadora a leer para obtener la data. Como es LSB1, deben leerse 8 bytes de la portadora para obtener 1 byte de la data escondida
        int cantBitsToRead = encodedDataLength * 8;

        StringBuilder byteBuilder = new StringBuilder();
        // Offset para excluir headers y tamano de la data escondida
        int offset = 54+32;
        // Instanciamos el vector que vamos a guardar lo que extraemos
        byte[] desencondedData = new byte[encodedDataLength];

        int index2 =0;
        int j = 0;
        for (int i = 0 ; i<cantBitsToRead; i++){
            // Leemos un byte de la imagen y nos quedamos con su ultimo bit
             byte b = image[offset + i];
             int lastBit = b & 1;
             byteBuilder.append(lastBit);
             if(j==7){
                 /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
//                 desencondedData[index2++] = Byte.parseByte(byteBuilder.reverse().toString(),2);
                 desencondedData[index2++] = (byte)Integer.parseInt(byteBuilder.toString(), 2);
                 byteBuilder = new StringBuilder();
                 j=0;
             }else {
                 j++;
             }
        }

        return desencondedData;

    }

    public void LSB4() throws FileTooLargetException {
        if( (encodingBytes.length * 2) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 2));

        byte[] image = this.editor.getCoverImageBytes();
        byte [] encodeData = this.encodingBytes;
        int indexImage = 54;

        for(int i=0;i<encodeData.length;i++){
            int encodeByte = encodeData[i];
            for (int j=0;j<2;j++,indexImage++){
                for(int k=0;k<4;k++) {
                    byte val = getBit((byte) encodeByte, 7 - k);
                    image[indexImage] = setBit(image[indexImage], 3 - k, val);
                }
                encodeByte <<= 4;
            }
        }

        this.editor.setBytes(image);
    }

    private byte getBit(byte num,int pos){
        return (byte) ((num >> pos) & 1); // 0 menos significativo, 7 mas significativo
    }

    private byte setBit(byte num,int pos, byte value){
         return (byte) ((num & ~(1 << pos)) | (value << pos));

    }

    public static byte[] revertLSB4(byte[] aux1) throws IOException {
        byte[] image = aux1;
        // Obtenemos los bytes del largo de la data que se escondio en la imagen portadora
        byte[] len = new byte[4] ;
        int indexLen = 0;
        StringBuilder lenBuilder = new StringBuilder();
        for (int i = 54,j=0 ; i<54+8; i++){ // + 8 es por que tenemos que obtener 4 bytes, y como de cada byte de la imagen portadora leemos 4 bits --> hay que leer 8 bytes de la portadora
            // Leemos un byte de la imagen
            byte b = image[i];

            StringBuilder aux = new StringBuilder();
            for(int dec = 0; dec < 4; dec++){ // Itero decalando los bits del byte de la imagen portadora obteniendo los 4 bits menos relevantes
                int lastBit = b & 1;
                aux.append(lastBit);
                b = (byte) (b>>1);
            }
            lenBuilder.append(aux.reverse());
            if(j==1){
                /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
                len[indexLen++] = (byte)Integer.parseInt(lenBuilder.toString(), 2);
                lenBuilder = new StringBuilder();
                j=0;
            }else {
                j++;
            }
        }

        // Transformamos esos bytes a un int para facilitar su lecutra
        int encodedDataLength= new BigInteger(len).intValue()+4; //TODO resolver este magic number, es para obtener 4 bytes mas de donde sacar la extension
        System.out.println(encodedDataLength);
        // Calculamos la cantidad de bytes de la imagen portadora a leer para obtener la data. Como es LSB4, deben leerse 2 bytes de la portadora para obtener 1 byte de la data escondida
        int cantBitsToRead = encodedDataLength * 2;

        StringBuilder byteBuilder = new StringBuilder();
        // Offset para excluir headers y tamano de la data escondida
        int offset = 54+8;
        // Instanciamos el vector que vamos a guardar lo que extraemos
        byte[] desencondedData = new byte[encodedDataLength];

        int index =0;
        int j = 0;
        for (int i = 0 ; i<cantBitsToRead; i++){
            // Leemos un byte de la imagen y nos quedamos con su ultimo bit
            byte b = image[offset + i];

            StringBuilder aux = new StringBuilder();
            for(int dec = 0; dec < 4; dec++){ // Itero decalando los bits del byte de la imagen portadora obteniendo los 4 bits relevantes
                int lastBit = b & 1;
                aux.append(lastBit);
                b = (byte) (b>>1);
            }
            byteBuilder.append(aux.reverse());
            if(j==1){
                /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
                desencondedData[index++] = (byte)Integer.parseInt(byteBuilder.toString(), 2);
                byteBuilder = new StringBuilder();
                j=0;
            }else {
                j++;
            }
        }

        return desencondedData;

    }

    public void LSBImproved() throws FileTooLargetException, IOException {

        if( (encodingBytes.length * 8) > editor.getBitArraySize())
            throw new FileTooLargetException(Integer.toString(editor.getBitArraySize() / 8));

        // Copia auxiliar de la imagen portadora
        byte[] auxCoverImageBytes = editor.getCoverImageBytes();

        // Paso de hacerle LSB1 a la imagen portadora con los bytes a ocultar
        BMPEditor oldEditor = this.editor;
        this.editor = new BMPEditor(this.originalImage);
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
