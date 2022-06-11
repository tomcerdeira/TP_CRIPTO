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

    public BMPSteganographEncoder(BufferedImage inputBMP, byte[] encodingBytes,String extension) throws IOException {
        editor = new BMPEditor(inputBMP);
        originalImage = inputBMP;
        this.encodingBytes = encodingBytes;
        // Agreamos al principio los bytes de tamaño y al final la extension TODO REVISAR
        byte[] len = ByteBuffer.allocate(4).putInt(encodingBytes.length).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(len);
        outputStream.write(editor.getCoverImageBytes());
//        outputStream.write(extension.getBytes(StandardCharsets.UTF_8)); // TODO agregar extension

        editor.setBytes(outputStream.toByteArray());
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


    public byte[] revertLSB1() throws IOException {
        byte[] image = this.editor.getCoverImageBytes();
        // Obtenemos los bytes del largo de la data que se escondio en la imagen portadora
        byte[] len = new byte[4] ;
        for (int i=0; i<4; i++){
            len[i]=image[i];
        }
        // Transformamos esos bytes a un int para facilitar su lecutra
        int encodedDataLength= new BigInteger(len).intValue();
        // Calculamos la cantidad de bytes de la imagen portadora a leer para obtener la data. Como es LSB1, deben leerse 8 bytes de la portadora para obtener 1 byte de la data escondida
        int cantBitsToRead = encodedDataLength * 8;

        StringBuilder byteBuilder = new StringBuilder();
        // Offset para excluir headers y tamano de la data escondida
        int offset = this.editor.getBitArrayOffset();
        // Instanciamos el vector que vamos a guardar lo que extraemos
        byte[] desencondedData = new byte[encodedDataLength];

        int index =0;
        int j = 0;
        for (int i = 0 ; i<cantBitsToRead; i++){
            // Leemos un byte de la imagen y nos quedamos con su ultimo bit
             byte b = image[offset + i];
             int lastBit = b & 1;
             byteBuilder.append(lastBit);
             if(j==7){
                 /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
                 desencondedData[index++] = (byte)Integer.parseInt(byteBuilder.reverse().toString(), 2);
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

        ByteIterator iterator = editor.byteIterator();

        for(byte b : encodingBytes) {
            iterator.NextSetLeastSignificantBits(b, 4);
            iterator.NextSetLeastSignificantBits((byte)(b>>4), 4);
        }
    }

    public byte[] revertLSB4() throws IOException {
        byte[] image = this.editor.getCoverImageBytes();
        // Obtenemos los bytes del largo de la data que se escondio en la imagen portadora
        byte[] len = new byte[4] ;
        for (int i=0; i<4; i++){
            len[i]=image[i];
        }
        // Transformamos esos bytes a un int para facilitar su lecutra
        int encodedDataLength= new BigInteger(len).intValue();
        // Calculamos la cantidad de bytes de la imagen portadora a leer para obtener la data. Como es LSB4, deben leerse 2 bytes de la portadora para obtener 1 byte de la data escondida
        int cantBitsToRead = encodedDataLength * 2;

        StringBuilder byteBuilder = new StringBuilder();
        // Offset para excluir headers y tamano de la data escondida
        int offset = this.editor.getBitArrayOffset();
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
            byteBuilder.append(aux);
            if(j==1){
                /// Despues de armar un byte en string, lo transformamos a un tipo byte. OBS hay que usarlo reverse ya que estamos trabajando en LITTLE_ENDIAN
                desencondedData[index++] = (byte)Integer.parseInt(byteBuilder.reverse().toString(), 2);
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
