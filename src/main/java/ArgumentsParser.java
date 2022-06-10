import java.util.*;

public class ArgumentsParser {
    List<String> args = new ArrayList<>();
    HashMap<String, List<String>> map = new HashMap<>();
    Set<String> flags = new HashSet<>();

    ArgumentsParser(String arguments[])
    {
        this.args = Arrays.asList(arguments);
        map();
        checkIfCorrectArgs();
    }

    // Return argument names
    public Set<String> getArgumentNames()
    {
        Set<String> argumentNames = new HashSet<>();
        argumentNames.addAll(flags);
        argumentNames.addAll(map.keySet());
        return argumentNames;
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
    public boolean checkIfCorrectArgs(){
        if(this.containsFlag("embed")) {
            if (this.containsArg("in") && this.containsArg("p") && this.containsArg("out") && this.containsArg("steg") && this.getArgumentValue("in").length != 0 && this.getArgumentValue("p").length != 0 && this.getArgumentValue("out").length != 0 && this.getArgumentValue("steg").length != 0) {
                String steg = Arrays.stream(this.getArgumentValue("steg")).findAny().get();
                switch (steg) {
                    case "LSB1":
                    case "LSBI":
                    case "LSB4":
                        break;
                    default:
                        throw new IllegalArgumentException("Steg possible values are: LSB1, LSB4 and LSBI");
                }

                if (this.containsArg("a")) {
                    String encodeMode = Arrays.stream(this.getArgumentValue("a")).findAny().get();
                    switch (encodeMode) {
                        case "aes128":
                        case "aes192":
                        case "aes256":
                        case "des":
                            break;
                        default:
                            throw new IllegalArgumentException("Encode \"-a\" possible values are: aes128, aes192, aes256 and des");
                    }
                }

                    if (this.containsArg("m")) {
                        String blocksMode = Arrays.stream(this.getArgumentValue("m")).findAny().get();
                        switch (blocksMode) {
                            case "ecb":
                            case "cfb":
                            case "ofb":
                            case "cbc":
                                break;
                            default:
                                throw new IllegalArgumentException("Encode \"-m\" possible values are: ecb, cfb, ofb and cbc");
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
