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
package inouire.baggle.server.bean;

import inouire.basics.SimpleLog;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author edouard
 */
@XmlRootElement(name="serverConfig")
@XmlType(propOrder={"roomName","welcomeMessage","listenningPort","autoPortIncrement",
                    "registerToMasterServer","masterServerHost","masterServerPort",
                    "listenOnLan","lanListenningPort",
                    "maxPlayers","gameTime","nbLettersMin","allWordsCount","language",
                    "blockChat","isPrivate","parentalFilter","password","logLevel"})
public class ServerConfigXML {
    
    
    private String roomName;
    private String welcomeMessage;
    private int listenningPort;
    private boolean autoPortIncrement;

    private boolean registerToMasterServer;
    private String masterServerHost;
    private int masterServerPort;
    
    private boolean listenOnLan;
    private int lanListenningPort;
    
    @XmlTransient
    private boolean bigBoard;
    
    private int maxPlayers;
    private int gameTime;
    private int nbLettersMin;
    private boolean allWordsCount;
    private String language;
    
    private boolean blockChat;
    private boolean parentalFilter;
    private boolean isPrivate;
    private String password;
    
    private String logLevel;
    
    //non exposed parameters
    @XmlTransient
    public int inactivityTimeout;//msec
    @XmlTransient
    public int zombieTimeout=60000;//msec, = 1min
    @XmlTransient
    public int kickTimeout=600000;//msec, = 60min

    public ServerConfigXML(){
        this.roomName="le salon de "+System.getProperty("user.name");
        this.welcomeMessage="Bienvenue dans ce salon !";
        this.listenningPort=42705;
        this.autoPortIncrement=true;
        
        this.registerToMasterServer=true;
        this.masterServerHost="masterserver.baggle.org";
        this.masterServerPort=80;
        
        this.listenOnLan=true;
        this.lanListenningPort=42710;
        
        this.bigBoard=false;
        
        this.maxPlayers=6;
        this.gameTime=180;
        this.inactivityTimeout=500*gameTime;
        this.nbLettersMin=3;
        this.allWordsCount=true;
        this.language="fr";
        
        this.blockChat=false;
        this.password="";
        this.isPrivate=false;
        
        this.logLevel="info";
    }
    
    public static ServerConfigXML loadFromFile(String uri){
        ServerConfigXML configXML=null;
        try {
            JAXBContext context = JAXBContext.newInstance(ServerConfigXML.class);
            Unmarshaller um = context.createUnmarshaller();
            configXML  = (ServerConfigXML) um.unmarshal(new FileReader(uri));
            SimpleLog.logger.info("Configuration loaded from file "+uri);
        } catch (Exception ex) {
            SimpleLog.logger.warn("Impossible to load configuration from file "+uri);
        }
        return configXML;
    }
    
    public static void writeToFile(ServerConfigXML configXML,String uri){
        
        File toWriteTo = new File(uri);
        
        if(!toWriteTo.getParentFile().exists()){
            try{
                toWriteTo.getParentFile().mkdirs();
            }catch(Exception e){
                //do nothing
            }
        }
        
        FileWriter fw = null;
        try{
            JAXBContext context = JAXBContext.newInstance(ServerConfigXML.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            //m.marshal(configXML, System.out);
            fw=new FileWriter(uri);
            m.marshal(configXML, fw);
            SimpleLog.logger.info("Configuration saved to file "+uri);
        }catch(JAXBException jbe){
            SimpleLog.logger.warn("Impossible to save configuration to file "+uri);
            jbe.printStackTrace();
        }catch(IOException ioe){
            SimpleLog.logger.warn("Impossible to save configuration to file "+uri);
        }finally {
            try {
                fw.close();
            } catch (Exception e) {
            }
        }
    }
    
    public void printRecap(){
        SimpleLog.logger.info("----------------------------------------");
        SimpleLog.logger.info("B@ggle room: "+roomName);
        
        //rules
        String board="Using classical 4x4 board";
        if(bigBoard){
             board="Using big 5x5 board";
        }
        SimpleLog.logger.info(board);
        
        //rules
        String rule="game time "+gameTime+"s, ";
        rule+="lang "+language+", ";
        if(allWordsCount){
            rule+="count all words, ";
        }else{
            rule+="traditionnal rule, ";
        }
        rule+=nbLettersMin+" letters min";
        SimpleLog.logger.info(rule);
        
        //network
        String network="port "+listenningPort+", ";
        if(registerToMasterServer){
            network+="register to masterserver at "+masterServerHost+":"+masterServerPort;
        }else{
            network+="not registering to masterserver";
        }
        SimpleLog.logger.info(network);
        
        //access
        SimpleLog.logger.info(maxPlayers+" players max");
        if(isPrivate){
            SimpleLog.logger.info("protected by password '"+password+"'");
        }
        
        //properties
        String properties="";
        if(blockChat){
            properties+="chat blocked, ";
        }
        if(parentalFilter){
            properties+="parental filter, ";
        }
        if(properties.length()>0){
            SimpleLog.logger.info(properties);
        }
        SimpleLog.logger.info("----------------------------------------");
    }

    public void setShortInactivityTimeout(){
        inactivityTimeout = 25000;//25 sec
    }
    public void setLongInactivityTimeout(){
        inactivityTimeout = gameTime * 1000;
    }   
    
    public boolean isParentalFilter() {
        return parentalFilter;
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

    @XmlTransient
    public void setBigBoard(boolean bigBoard){
        this.bigBoard=bigBoard;
    }
    public boolean isBigBoard(){
        return this.bigBoard;
    }
    
}
