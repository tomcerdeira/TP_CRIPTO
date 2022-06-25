import encoders.AESEncoder;
import encoders.DESEncoder;
import encoders.Encoder;

import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class ArgumentsParser {
    List<String> args = new ArrayList<>();
    HashMap<String, List<String>> map = new HashMap<>();
    Set<String> flags = new HashSet<>();
    Encoder encoder;
    File fileToEncrypt;
    File fileCarrier;
    File outputFile;
    String stegMode;
    boolean encodeMode = false;
    boolean revertMode = false;


    ArgumentsParser(String[] arguments) throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
        this.args = Arrays.asList(arguments);
        map();
        if (!checkIfCorrectArgs()){
            throw  new IllegalArgumentException();
        }
    }

    // Check if flag is given
    public boolean containsFlag(String flagName)
    {
        return flags.contains(flagName);
    }

    public boolean containsArg(String name)
    {
        return map.containsKey(name);
    }

    // Return argument value for particular argument name
    public String[] getArgumentValue(String argumentName)
    {
        if(map.containsKey(argumentName))
            return map.get(argumentName).toArray(new String[0]);
        else
            return null;
    }

    // Map the flags and argument names with the values
    public void map()
    {
        for(String arg: args)
        {
            if(arg.startsWith("-"))
            {
                if (args.indexOf(arg) == (args.size() - 1))
                {
                    flags.add(arg.replace("-", ""));
                }
                else if (args.get(args.indexOf(arg)+1).startsWith("-"))
                {
                    flags.add(arg.replace("-", ""));
                }
                else
                {
                    //List of values (can be multiple)
                    List<String> argumentValues = new ArrayList<>();
                    int i = 1;
                    while(args.indexOf(arg)+i != args.size() && !args.get(args.indexOf(arg)+i).startsWith("-"))
                    {
                        argumentValues.add(args.get(args.indexOf(arg)+i));
                        i++;
                    }
                    map.put(arg.replace("-", ""), argumentValues);
                }
            }
        }
    }

    // TODO: HAcer que en vez de retornar un boolean devuelva una instancia de una clase que encapsule el coportamiento dado por los parametros
    public boolean checkIfCorrectArgs() throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
        if(this.containsFlag("embed")) {
            this.revertMode = false;
            if (this.containsArg("in") && this.containsArg("p") && this.containsArg("out") && this.containsArg("steg") && this.getArgumentValue("in").length != 0 && this.getArgumentValue("p").length != 0 && this.getArgumentValue("out").length != 0 && this.getArgumentValue("steg").length != 0) {
                this.fileToEncrypt = new File(Arrays.stream(this.getArgumentValue("in")).findAny().get());
                this.fileCarrier = new File(Arrays.stream(this.getArgumentValue("p")).findAny().get());
                this.outputFile = new File(Arrays.stream(this.getArgumentValue("out")).findAny().get());
                String steg = Arrays.stream(this.getArgumentValue("steg")).findAny().get();
                switch (steg) {
                    case "LSB1":
                    case "LSBI":
                    case "LSB4":
                        break;
                    default:
                        throw new IllegalArgumentException("Steg possible values are: LSB1, LSB4 and LSBI");
                }
                this.setStegMode();
                this.setEncoderFromArgs();
                return true;
                }
                throw new IllegalArgumentException("When using embed mode, parameters \"-in <filepath> \", \"-p <bmpPath>\" , \"-out <filePath>\" and \"-steg <LSB1,LSB4,LSBI>\" are mandatory");
            }
            else if(this.containsFlag("extract")){
                this.revertMode = true;

                if (this.containsArg("p") && this.containsArg("out") && this.containsArg("steg") && this.getArgumentValue("p").length != 0 && this.getArgumentValue("out").length != 0 && this.getArgumentValue("steg").length != 0) {
                   this.fileCarrier = new File(Arrays.stream(this.getArgumentValue("p")).findAny().get());
                   this.outputFile = new File(Arrays.stream(this.getArgumentValue("out")).findAny().get());

                    this.setStegMode();
                    this.setEncoderFromArgs();

                }
                return  true;
            }




            throw new IllegalArgumentException();

    }

    private void setEncoderFromArgs() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        if(this.containsArg("pass")) {
            this.encodeMode = true;
            String encodeMode = "AES"; // aes128 en modo CBC por default.
            String blocksMode = "CBC";
            Integer keyLen = 128;
            if (this.containsArg("a")) {
                encodeMode = Arrays.stream(this.getArgumentValue("a")).findAny().get();
                switch (encodeMode) {
                    case "aes128":
                        encodeMode = "AES";
                        keyLen = 128;
                        break;
                    case "aes192":
                        encodeMode = "AES";
                        keyLen = 192;
                        break;

                    case "aes256":
                        encodeMode = "AES";
                        keyLen = 256;
                        break;
                    case "des":
                        encodeMode = "DES";
                        break;
                    default:
                        throw new IllegalArgumentException("Encode \"-a\" possible values are: aes128, aes192, aes256 and des");
                }
            }

            if (this.containsArg("m")) {
                blocksMode = Arrays.stream(this.getArgumentValue("m")).findAny().get();
                blocksMode = switch (blocksMode) {
                    case "ecb", "ofb", "cbc" -> blocksMode.toUpperCase();
                    case "cfb" -> blocksMode.toUpperCase() + "8";
                    default -> throw new IllegalArgumentException("Encode \"-m\" possible values are: ecb, cfb, ofb and cbc");
                };
            }

            String password = Arrays.stream(this.getArgumentValue("pass")).findAny().get();
            switch (encodeMode){
                case "AES": {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(keyLen);
                    byte[][] keyAndIV = EVPBytesToKeyAndIv(1, password.getBytes(StandardCharsets.UTF_8), keyLen/8, 16);
                    byte[] keyArr = keyAndIV[0];
                    SecretKey key = new SecretKeySpec(keyArr, "AES");
                    this.encoder = new AESEncoder("AES/"+blocksMode+"/NoPadding",key,new IvParameterSpec(keyAndIV[1]));
                    break;
                }
                case "DES": {
                    if(fileToEncrypt == null){
                        fileToEncrypt = fileCarrier;
                    }
                    byte[][] keyAndIV = EVPBytesToKeyAndIv(1, password.getBytes(StandardCharsets.UTF_8), keyLen/16, 8);
                    this.encoder = new DESEncoder("DES/" + blocksMode+"/PKCS5Padding" , new SecretKeySpec( keyAndIV[0], "DES"),new IvParameterSpec(keyAndIV[1]));
                    break;
                }
            }
        }
    }

    private void setStegMode(){
        String steg = Arrays.stream(this.getArgumentValue("steg")).findAny().get();
        switch (steg) {
            case "LSB1":
            case "LSBI":
            case "LSB4":
                break;
            default:
                throw new IllegalArgumentException("Steg possible values are: LSB1, LSB4 and LSBI");
        }
        this.stegMode = steg;
    }

    private static byte[] hash(byte[] bytes, String method) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(method);
        return md.digest(bytes);
    }

    public static byte[] sha256(byte[] bytes) throws NoSuchAlgorithmException {
        return hash(bytes, "SHA-256");
    }

    private byte[][] EVPBytesToKeyAndIv(int iterations, byte[] data, int keySize, int ivSize) throws NoSuchAlgorithmException {
        byte[] mdBuff = sha256(data);
        byte[] key = new byte[keySize];
        int keyIndex = 0;
        byte[] iv = new byte[ivSize];
        int ivIndex = 0;
        while (true) {
            for (int i = 1; i < iterations; i++) {
                mdBuff = sha256(mdBuff);
            }
            int mdBuffIndex = 0;
            if (keyIndex < key.length) {
                for (; (mdBuffIndex < mdBuff.length) && (keyIndex < key.length); mdBuffIndex++, keyIndex++) {
                    key[keyIndex] = mdBuff[mdBuffIndex];
                }
            }
            if (ivIndex < iv.length && mdBuffIndex < mdBuff.length) {
                for (; (mdBuffIndex < mdBuff.length) && (ivIndex < iv.length); mdBuffIndex++, ivIndex++) {
                    iv[ivIndex] = mdBuff[mdBuffIndex];
                }
            }
            if (ivIndex == iv.length && keyIndex == key.length) {
                break;
            } else {
                byte[] aux = new byte[mdBuff.length + data.length];
                System.arraycopy(mdBuff, 0, aux, 0, mdBuff.length);
                System.arraycopy(data, 0, aux, mdBuff.length, data.length);
                mdBuff = sha256(aux);
            }
        }
        byte[][] keyAndIv = new byte[2][];
        keyAndIv[0] = key;
        keyAndIv[1] = iv;
        return keyAndIv;
    }
}
