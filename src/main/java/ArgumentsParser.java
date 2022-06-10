import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    File fileSteganographed;
    String stegMode;
    boolean encodeMode = false;


    ArgumentsParser(String[] arguments) throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException {
        this.args = Arrays.asList(arguments);
        map();
        checkIfCorrectArgs();
    }

    // Check if flag is given
    public boolean containsFlag(String flagName)
    {
        if(flags.contains(flagName))
            return true;
        return false;
    }

    public boolean containsArg(String name)
    {
        if(map.containsKey(name)){
        return true;
    }
        return false;
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
    public boolean checkIfCorrectArgs() throws NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException {
        if(this.containsFlag("embed")) {
            if (this.containsArg("in") && this.containsArg("p") && this.containsArg("out") && this.containsArg("steg") && this.getArgumentValue("in").length != 0 && this.getArgumentValue("p").length != 0 && this.getArgumentValue("out").length != 0 && this.getArgumentValue("steg").length != 0) {
                this.fileToEncrypt = new File(Arrays.stream(this.getArgumentValue("in")).findAny().get());
                this.fileCarrier = new File(Arrays.stream(this.getArgumentValue("p")).findAny().get());
                this.fileSteganographed = new File(Arrays.stream(this.getArgumentValue("out")).findAny().get());


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
                            case "aes256":
                                encodeMode = "AES";
                                keyLen = 265;
                            case "des":
                                encodeMode = "DES";
                                break;
                            default:
                                throw new IllegalArgumentException("Encode \"-a\" possible values are: aes128, aes192, aes256 and des");
                        }
                    }

                    if (this.containsArg("m")) {
                        blocksMode = Arrays.stream(this.getArgumentValue("m")).findAny().get();
                        switch (blocksMode) {
                            case "ecb":
                            case "cfb":
                            case "ofb":
                            case "cbc":
                                blocksMode = blocksMode.toUpperCase();
                                break;
                            default:
                                throw new IllegalArgumentException("Encode \"-m\" possible values are: ecb, cfb, ofb and cbc");
                        }
                    }

                    String password = Arrays.stream(this.getArgumentValue("pass")).findAny().get();
                    switch (encodeMode){
                        case "AES":{
                            SecretKey keyForAes = GeneratedSecretKey.getKeyFromPassword("PBKDF2WithHmacSHA256","AES",password, "salt",keyLen);
                            this.encoder = new AESEncoder("AES/"+blocksMode+"/PKCS5Padding",fileToEncrypt.getPath(),"aux",keyForAes,AESEncoder.generateIv());
                        }
                        case "DES":{

                        }
                    }
                }
                    return true;
                }
                throw new IllegalArgumentException("When using embed mode, parameters \"-in <filepath> \", \"-p <bmpPath>\" , \"-out <filePath>\" and \"-steg <LSB1,LSB4,LSBI>\" are mandatory");
            }
            else if(this.containsFlag("extract")){
                return  true;
            }

            throw new IllegalArgumentException();

    }
}
