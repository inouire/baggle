package inouire.baggle.server;

import inouire.basics.SimpleLog;
import inouire.basics.Value;
import inouire.basics.myml.MyMl;
import inouire.basics.myml.MyMlException;
import inouire.basics.myml.MyMlValidator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author edouard
 */
public class ServerConfiguration {
    
    MyMl config;

    public String roomName;
    public String welcomeMessage;
    public int maxPlayers;
    
    public int listeningPort;
    public boolean autoPortIncrement;
    public boolean registerToMasterServer;
    public String masterServerHost;
    public int masterServerPort;
    public boolean listenOnLan;
    public int lanListenningPort;
    
    public int gameTime;
    public int nbLettersMin;
    public boolean allWordsCount;
    public String language;
    public boolean bigBoard;
    
    public boolean blockChat;
    public boolean parentalFilter;
    
    public String logLevel;
    
    //non exposed parameters
    public int inactivityTimeout;//msec
    public int zombieTimeout=60000;//msec, = 1min
    public int kickTimeout=600000;//msec, = 60min
    
    public ServerConfiguration(MyMl myml_structure){
        this.config = myml_structure;
    }
    
    public static ServerConfiguration createDefault(String config_file){
        MyMl default_config=null;
        try {
            default_config = MyMl.loadContent(new ArrayList(Arrays.asList(default_config_lines)));
            default_config.writeToFile(config_file);
        } catch (MyMlException ex) {
            SimpleLog.logger.error("Impossible to create default server configuration");
            SimpleLog.logger.warn(ex.getMessage());
        } catch (IOException ex) {
            SimpleLog.logger.warn("Impossible to write default server configuration to file "+config_file);
        }
        
        //no need to validate, this one is safe        
        ServerConfiguration loaded_config = new ServerConfiguration(default_config);
        loaded_config.attribute();
        return loaded_config;
    }
    
    public static ServerConfiguration loadFromFile(String config_file){
        //load myml file
        MyMl config=null;
        try{
            config=MyMl.loadFile(config_file);
        }catch(FileNotFoundException fnfe){
            SimpleLog.logger.warn("Impossible to find configuration file "+config_file);
        } catch (MyMlException ex) {
            SimpleLog.logger.warn("Invalid MyMl syntax for file "+config_file);
            SimpleLog.logger.warn(ex.getMessage());
        }
        
        //load config validator
        MyMlValidator validator=new MyMlValidator();
        try {
            validator.useFullValidation()
                     .setValidationPattern(config_validator);
        } catch (MyMlException ex) {
            SimpleLog.logger.error(ex.getMessage());
            return null;
        }
        
        //validate config against constraints
        boolean invalid=false;
        try {
            validator.validate(config);
        } catch (MyMlException ex) {
            SimpleLog.logger.error(ex.getMessage());
            invalid=true;
        }
        if(invalid){
            return null;
        }
        
        //return final config
        ServerConfiguration loaded_config = new ServerConfiguration(config);
        loaded_config.attribute();
        return loaded_config;
    }
    
    public String get(String absolute_key){
        try {
            return config.getValue(absolute_key);
        } catch (MyMlException ex) {
            SimpleLog.logger.warn(ex.getMessage());
            return "N/A";
        }
    }
        
    public void printRecap(){
        SimpleLog.logger.info("----------------------------------------");
        SimpleLog.logger.info("The following config will be used:\n"+config.toString());
    }
    
    private void attribute(){
        try{
            this.roomName = config.getValue("room.name");
            this.welcomeMessage = config.getValue("room.welcomeMessage");
            this.maxPlayers = Value.bound(config.getInt("room.maxPlayers",6),1,20);
            
            this.listeningPort = config.getInt("network.public.listeningPort",42705);
            this.autoPortIncrement = config.getBool("network.public.autoPortIncrement",true);
            this.registerToMasterServer = config.getBool("network.masterServer.register",true);
            this.masterServerHost = config.getValue("network.masterServer.host");
            this.masterServerPort  = config.getInt("network.masterServer.port",80);
            this.listenOnLan = config.getBool("network.lan.listenOnLan", true);
            this.lanListenningPort = config.getInt("network.lan.lanListeningPort",42710);
            
            this.gameTime = config.getInt("room.rules.gameTime",180);
            this.nbLettersMin = Value.bound(config.getInt("room.rules.nbLettersMin", 3),3,6);
            this.allWordsCount = config.getBool("room.rules.allWordsCount", true);
            this.language = config.getValue("room.rules.language");
            this.bigBoard = config.getBool("room.rules.bigBoard", false);
            
            this.blockChat = config.getBool("room.options.blockChat", false);
            this.parentalFilter = config.getBool("room.options.parentalFilter", false);
            
            this.logLevel = config.getValue("log.level");
        }catch(MyMlException ex){
            ex.printStackTrace();
        }
        
    }
    
    static String[] default_config_lines=new String[]{
        "room:",
        "    name: le salon de "+System.getProperty("user.name"),
        "    welcomeMessage: Bienvenue dans ce salon",
        "    maxPlayers: 6",
        "    rules:",
        "        gameTime: 180",
        "        nbLettersMin: 3",
        "        allWordsCount: yes",
        "        language: fr",
        "        bigBoard: no",
        "    options:",
        "        blockChat: no",
        "        parentalFilter: no",
        "network:",
        "    public:",
        "        listeningPort: 42705",
        "        autoPortIncrement: yes",
        "    masterServer:",
        "        register: yes",
        "        host: masterserver.baggle.org",
        "        port: 80",
        "    lan:",
        "        listenOnLan: yes",
        "        listeningPort: 42710",
        "log:",
        "    level: INFO"
    };
    
    static String[] config_validator=new String[]{
        "room:",
        "    name: string",
        "    welcomeMessage: string",
        "    maxPlayers: integer",
        "    rules:",
        "        gameTime: integer",
        "        bigBoard: boolean",
        "        nbLettersMin: integer",
        "        allWordsCount: boolean",
        "        language: string",
        "    options:",
        "        blockChat: boolean",
        "        parentalFilter: boolean",
        "network:",
        "    public:",
        "        listeningPort: integer",
        "        autoPortIncrement: boolean",
        "    masterServer:",
        "        register: boolean",
        "        host: string",
        "        port: integer",
        "    lan:",
        "        listenOnLan: boolean",
        "        listeningPort: integer",
        "log:",
        "    level: string"
    };    
    
}

