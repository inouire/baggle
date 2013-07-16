 /* Copyright 2009-2013 Edouard Garnier de Labareyre
  *
  * This file is part of B@ggle.
  *
  * B@ggle is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * B@ggle is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with B@ggle.  If not, see <http://www.gnu.org/licenses/>.
  */
package inouire.baggle.server;

import inouire.basics.SimpleLog;
import inouire.basics.myml.MyMl;
import inouire.basics.myml.MyMlException;
import inouire.basics.myml.MyMlTypeValidator;
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
    
    MyMl myml_structure;
    
    static String[] default_config_lines=new String[]{
        "room:",
        "    name: le salon de "+System.getProperty("user.name"),
        "    welcomeMessage: Bienvenue dans ce salon",
        "    rules:",
        "        maxPlayers: 6",
        "        gameTime: 180",
        "        boardType: classic",
        "        nbLettersMin: 3",
        "        allWordsCount: true",
        "        language: fr",
        "    options:",
        "        blockChat: false",
        "        parentalFilter: false",
        "        private: false",
        "network:",
        "    public:",
        "        listeningPort: 42705",
        "        portAutoIncrement: true",
        "    masterServer:",
        "        register: true",
        "        host: masterserver.baggle.org",
        "        port: 80",
        "    lan:",
        "        listenOnLan: true",
        "        listeningPort: 42710",
        "log:",
        "    level: INFO"
    };
    
    static String[] config_validator=new String[]{
        "room:",
        "    name: string",
        "    welcomeMessage: string",
        "    rules:",
        "        maxPlayers: integer",
        "        gameTime: integer",
        "        boardType: string",
        "        nbLettersMin: integer",
        "        allWordsCount: boolean",
        "        language: string",
        "    options:",
        "        blockChat: boolean",
        "        parentalFilter: boolean",
        "        private: boolean",
        "network:",
        "    public:",
        "        listeningPort: integer",
        "        portAutoIncrement: boolean",
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
       
    public ServerConfiguration(MyMl myml_structure){
        this.myml_structure = myml_structure;
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
        return new ServerConfiguration(default_config);
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
        MyMlTypeValidator validator=null;
        try {
            validator = new MyMlTypeValidator(new ArrayList(Arrays.asList(config_validator)));
        } catch (MyMlException ex) {
            SimpleLog.logger.error(ex.getMessage());
            return null;
        }
        
        //validate config against cosntraints
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
        return new ServerConfiguration(config);
    }
    
    public String getValue(String absolute_key){
        try {
            return myml_structure.getValue(absolute_key);
        } catch (MyMlException ex) {
            SimpleLog.logger.warn(ex.getMessage());
            return "N/A";
        }
    }
    /*
    public void setShortInactivityTimeout(){
        inactivityTimeout = 25000;//25 sec
    }
    public void setLongInactivityTimeout(){
        inactivityTimeout = gameTime * 1000;
    }   
    
    public boolean isParentalFilter() {
        return myml_structure.getValue("room.options.parentalFilter");
    }
    public void setParentalFilter(boolean parentalFilter) {
        this.parentalFilter = parentalFilter;
    }

    public boolean isAutoPortIncrement() {
        return autoPortIncrement;
    }
    public void setAutoPortIncrement(boolean autoPortIncrement) {
        this.autoPortIncrement = autoPortIncrement;
    }
    
    public boolean isAllWordsCount() {
        return allWordsCount;
    }
    public void setAllWordsCount(boolean allWordsCount) {
        this.allWordsCount = allWordsCount;
    }

    public boolean isBlockChat() {
        return blockChat;
    }
    public void setBlockChat(boolean blockChat) {
        this.blockChat = blockChat;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }
    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public int getGameTime() {
        return gameTime;
    }
    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
        this.inactivityTimeout=gameTime*500;//in msec
    }

    public boolean isIsPrivate() {
        return isPrivate;
    }
    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language.toLowerCase();
    }

    public int getListenningPort() {
        return listenningPort;
    }
    public void setListenningPort(int listenningPort) {
        this.listenningPort = listenningPort;
    }

    public String getMasterServerHost() {
        return masterServerHost;
    }
    public void setMasterServerHost(String masterServerHost) {
        this.masterServerHost = masterServerHost;
    }

    public int getMasterServerPort() {
        return masterServerPort;
    }
    public void setMasterServerPort(int masterServerPort) {
        this.masterServerPort = masterServerPort;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
    public void setMaxPlayers(int maxPlayers) {
        if(maxPlayers<1){
            this.maxPlayers = 1;
        }else if(maxPlayers>20){
            this.maxPlayers = 20;
        }else{
            this.maxPlayers = maxPlayers;
        }
    }

    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getNbLettersMin() {
        return nbLettersMin;
    }
    public void setNbLettersMin(int nbLettersMin) {
        if(nbLettersMin>6 ){
            this.nbLettersMin=6;
        }else if(nbLettersMin<3){
            this.nbLettersMin=3;
        }else{
            this.nbLettersMin = nbLettersMin;
        }
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRegisterToMasterServer() {
        return registerToMasterServer;
    }
    public void setRegisterToMasterServer(boolean registerToMasterServer) {
        this.registerToMasterServer = registerToMasterServer;
    }

    public int getLanListenningPort() {
        return lanListenningPort;
    }
    public void setLanListenningPort(int lanListenningPort) {
        this.lanListenningPort = lanListenningPort;
    }

    public boolean isListenOnLan() {
        return listenOnLan;
    }
    public void setListenOnLan(boolean listenOnLan) {
        this.listenOnLan = listenOnLan;
    }

    public String getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setBigBoard(boolean bigBoard){
        this.bigBoard=bigBoard;
    }
    public boolean isBigBoard(){
        return this.bigBoard;
    }*/
    
    
}

