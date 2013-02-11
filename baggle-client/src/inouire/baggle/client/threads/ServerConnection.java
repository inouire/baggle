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

package inouire.baggle.client.threads;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import javax.swing.JOptionPane;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.client.gui.RoomPanel;
import inouire.baggle.datagrams.*;
import inouire.baggle.types.Key;
import inouire.baggle.types.Words;

/**
 *
 * @author Edouard de Labareyre
 */
public class ServerConnection extends Thread{

    public String SERVER;
    public int PORT;

    //player properties
    public String my_nick;
    public String my_logo;
    public String my_auth_token;
    public int my_id;

    //server properties
    public String LANG;
    public boolean CHAT;
    public int MIN_LENGTH;
    public boolean PARENTAL_FILTER;
    public String GAME_MODE;

    //communication channels
    public PrintWriter out = null;
    public BufferedReader in = null;
    public Socket socket = null;
    
    //game properties
    public String grid="????????????????";

    public HashMap<Integer,String> players_id_name = new HashMap<Integer,String>();
    public HashMap<Integer,String> players_id_avatar = new HashMap<Integer,String>();
    
    public boolean in_game = false;
    public boolean is_connected=false;    
    
    public ServerConnection(String server, int port){
        this.SERVER = server;
        this.PORT = port;
    }

    /**
     * Send a message to the server through the opened socket
     * @param message
     */
    public void send(String message){
        if(out != null){
            out.println(message);
            Main.logger.trace("[SND>>] "+message);
        }
    }
    
    public void disconnect(){
        send(new DISCONNECTDatagram().toString());
        
        is_connected=false;
        try{
            out.close();
            in.close();
            socket.close();
        }catch(Exception e){
            
        }
    }
    
    /**
     * Connect to the server and performs the negociation phase
     * @param nick the nickname of the player in the room
     * @param logo the id of the logo in the room (between 1 and 10)
     * @return 0 if the connection is successful
     * other codes:
     * 1: misc problem
     * 2: unkown host
     * 3: too many players
     * 4: give password
     * 5: bad password
     */
    public int connect(String nick,String logo){
        this.my_nick=nick;
        this.my_logo=logo;

        
        this.socket=null;
        try {
            socket = new Socket(SERVER, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            Main.logger.warn(Language.getString(45)+" - "+Language.getString(6));
            return 2;
        } catch (IOException e) {
            Main.logger.warn(Language.getString(46)+PORT+Language.getString(47)+" - "+Language.getString(6));
            return 1;
        } catch (IllegalArgumentException e) {
            Main.logger.warn(Language.getString(48)+" - "+Language.getString(6));
            return 1;
        }

        //negociation phase
        send(new CONNECTDatagram(Datagram.replaceAccents(nick),logo).toString());

        String packet;
        String[] datagram;
        Key key=null;

        try{
            while ((packet = in.readLine()) != null){

                datagram = packet.split("\\|");
                Main.logger.warn("[<<RECV] "+packet);
                try{
                    key=Key.valueOf(datagram[0]);
                    switch (key){
                        case ACCEPT:
                            ACCEPTDatagram acceptD = new ACCEPTDatagram(datagram);
                            acceptAction(acceptD);
                            return 0;
                        case DENY:
                            DENYDatagram statusD = new DENYDatagram(datagram);
                            denyAction(statusD);
                            if(statusD.reason.contains("server_full")){
                                return 3;
                            }else{
                                return 1;
                            }
                        case PASSWORD:
                            passwordAction();
                            break;
                        default:
                            Main.logger.warn("Unexpected message: "+packet);
                            break;
                    }
                }catch(Exception e){
                    Main.logger.warn("Illegal datagram received from server: "+packet);
                }
            }
        }catch(IOException e){
            Main.logger.warn("IOException during negociation phase");
        }
        return 1;
    }

    /**
     * Hook when receiving an ACCEPT datagram during negociation phase
     * @param acceptD the parsed datagram
     */
    public void acceptAction(ACCEPTDatagram acceptD){
        //get useful server info
        my_auth_token = acceptD.auth;
        LANG=acceptD.lang;
        CHAT=acceptD.chat;
        MIN_LENGTH=acceptD.min;
        PARENTAL_FILTER=acceptD.pf;
        GAME_MODE=acceptD.mode;
        my_id=acceptD.id;

        //update UI
        Main.mainFrame.roomPane.setGameMode(GAME_MODE);
        Main.mainFrame.roomPane.wordsFoundPane.setLanguage(LANG);
        Main.mainFrame.roomPane.wordsFoundPane.setMinWordLength(MIN_LENGTH);
        Main.mainFrame.roomPane.wordsFoundPane.setGameMode(GAME_MODE);
        Main.mainFrame.roomPane.rulesPane.updateRules(acceptD);
        
        //by default, show nothing
        Main.mainFrame.roomPane.showNothing();
        
        //TODO set game mode

        //launch the game thread
        this.start();
    }

    /**
     * Hook when receiving a DENY datagram during negociation phase
     * @param statusD the parsed datagram
     */
    private void denyAction(DENYDatagram statusD){
        Main.logger.info("Access to server denied: "+statusD.reason);
    }

    /**
     * Hook when receiving a PASSWORD datagram during negociation phase
     */
    private void passwordAction(){
//        send(new PASSWORDDatagram(password));
        Main.logger.info("Sending password");
    }

    /**
     * Game thread
     */
    @Override
    public void run(){

        is_connected=true;
                
        String packet;
        String[] datagram;
        Key key;
        
        try{
            while ((packet = in.readLine()) != null){
                datagram = packet.split("\\|");
                Main.logger.trace("[<<RCV] "+packet);
                try{
                    key=Key.valueOf(datagram[0]);
                    switch (key){
                        //related to game
                        case START:
                            STARTDatagram startD = new STARTDatagram(datagram);
                            STARTAction(startD);
                            break;
                        case TIME:
                            TIMEDatagram timeD = new TIMEDatagram(datagram);
                            TIMEAction(timeD);
                            break;
                        case WORD:
                            WORDDatagram wordD = new WORDDatagram(datagram);
                            WORDAction(wordD);
                            break;
                        case STOP:
                            STOPDatagram stopD = new STOPDatagram(datagram);
                            STOPAction(stopD);
                            break;
                        case RESULT:
                            RESULTDatagram resultD = new RESULTDatagram(datagram);
                            RESULTAction(resultD);
                            break;

                        //related to players
                        case JOIN:
                            JOINDatagram joinD = new JOINDatagram(datagram);
                            JOINAction(joinD);
                            break;
                        case LEAVE:
                            LEAVEDatagram leaveD = new LEAVEDatagram(datagram);
                            LEAVEAction(leaveD);
                            break;
                        case PROGRESS:
                            PROGRESSDatagram progressD = new PROGRESSDatagram(datagram);
                            PROGRESSAction(progressD);
                            break;
                        case SCORE:
                            SCOREDatagram scoreD = new SCOREDatagram(datagram);
                            SCOREAction(scoreD);
                            break;
                        case CLIENT:
                            CLIENTDatagram clientD = new CLIENTDatagram(datagram);
                            CLIENTAction(clientD);
                            break;

                        //generic
                        case CHAT:
                            CHATDatagram chatD = new CHATDatagram(datagram);
                            CHATAction(chatD);
                            break;
                        case STATUS:
                            STATUSDatagram statusD = new STATUSDatagram(datagram);
                            STATUSAction(statusD);
                            break;
                        default:
                            Main.logger.warn("Unexpected message: "+packet);
                            break;
                    }
                }catch(Exception e){
                    Main.logger.warn("Illegal datagram received from server: "+packet);
                }
            }
        }catch(IOException e){
            Main.logger.debug("IOException during game phase");
        }
        Main.logger.info("Connection lost unexpectly");
        if(is_connected){//ie on a perdu la connexion sans passer par la case "disconnect volontaire"
            JOptionPane.showMessageDialog(Main.mainFrame,
                    Language.getString(52),
                    Language.getString(6),
                    JOptionPane.ERROR_MESSAGE);
            Main.mainFrame.leaveRoom();
        }
        
    }

    /**
     * Action when receiving a JOIN datagram
     * @param st
     */
    private void JOINAction(JOINDatagram joinD){
        String nick=Datagram.addAccents(joinD.nick);
        players_id_name.put(joinD.id,nick);
        players_id_avatar.put(joinD.id,joinD.logo);
        Main.mainFrame.roomPane.playersPane.addPlayer(joinD.id,nick, joinD.logo);
    }

    /**
     * Action when receiving a LEAVE datagram
     * @param st
     */
    private void LEAVEAction(LEAVEDatagram leaveD){
        players_id_name.remove(leaveD.id);
        players_id_avatar.remove(leaveD.id);
        Main.mainFrame.roomPane.playersPane.removePlayer(leaveD.id);
    }

    /**
     * Action when receiving a STATUS datagram
     * @param st
     */
    private void STATUSAction(STATUSDatagram statusD){
        Main.mainFrame.roomPane.playersPane.updateStatus(statusD.id, statusD.state);
    }

    /**
     * Action when receiving a CHAT datagram
     * @param st
     */
    private void CHATAction(CHATDatagram chatD){
        if(chatD.id==0){
            Main.mainFrame.roomPane.chatPane.addServerInfo(chatD.msg);
        }else{
            Main.mainFrame.roomPane.chatPane.addMessage(chatD.id,chatD.msg);
        }
    }

    /**
     * Action when receiving a TIME datagram
     * @param st
     */
    private void TIMEAction(TIMEDatagram timeD){
        Main.mainFrame.roomPane.timePane.setServerTime(timeD.rem,timeD.tot);
    }

    /**
     * Action when receiving a PROGRESS datagram
     * @param st
     */
    private void PROGRESSAction(PROGRESSDatagram progressD){
        Main.mainFrame.roomPane.playersPane.setGauge(progressD.id,progressD.prog);
    }

    /**
     * Action when receiving a SCORE datagram
     * @param st
     */
    private void SCOREAction(SCOREDatagram scoreD){
        Main.mainFrame.roomPane.playersPane.setTotalScore(scoreD.id,scoreD.score);
    }

    /**
     * Action when receiving a START datagram
     * @param st
     */
    private void STARTAction(STARTDatagram startD) {
        in_game=true;
        grid=startD.grid;
        
        Main.mainFrame.roomPane.boardPane.setBigBoard(grid.length()>16);
        Main.mainFrame.roomPane.boardPane.enableAll();
        Main.mainFrame.roomPane.boardPane.enableAll();
        Main.mainFrame.roomPane.resetActionPane.setToMode(true);
        Main.mainFrame.roomPane.readyActionPane.setToMode(true);
        Main.mainFrame.roomPane.wordsFoundPane.setMaxPoints(startD.max);
        Main.mainFrame.roomPane.wordsFoundPane.resetWordsFound();
        
        Main.mainFrame.roomPane.showGame();

        Main.mainFrame.roomPane.wordEntryPane.giveFocus();
        Main.mainFrame.roomPane.refreshCenter();
        
        Main.mainFrame.roomPane.boardPane.animateShuffle();
    }

    /**
     * Action when receiving a WORD datagram
     * @param st
     */
    private void WORDAction(WORDDatagram wordD) {
        if(wordD.status==Words.GOOD){
            Main.mainFrame.roomPane.wordsFoundPane.addWord(wordD.word.toUpperCase());
        }
        Main.mainFrame.roomPane.wordStatusPane.setWord(wordD.word.toUpperCase(), wordD.status);
    }

    /**
     * Action when receiving a STOP datagram
     * @param st
     */
    private void STOPAction(STOPDatagram stopD) {
        
        //update 'in game' variable
        in_game=false;
        
        RoomPanel gamePane=Main.mainFrame.roomPane;
        
        gamePane.resetActionPane.setToMode(true);
        gamePane.readyActionPane.setToMode(true);
        gamePane.playersPane.resetAllGauges();
        gamePane.boardPane.disableAll();
        gamePane.wordStatusPane.reset();
        gamePane.timePane.resetTimer();
        gamePane.resultsPane.clearResults();
        gamePane.resultsPane.setGrid(grid);
        
        if(stopD.reason.equals("eot")){
            //display results
            Main.mainFrame.roomPane.showResults();
        }else if(stopD.reason.equals("nogame")){
            //display the 'nogame' screen
            Main.mainFrame.roomPane.showNoGame();
        }
         
        
    }

    /**
     * Action when receiving a RESULT datagram
     * @param st
     */
    private void RESULTAction(RESULTDatagram resultD) {
        //add result to result pane
        Main.mainFrame.roomPane.resultsPane.addResult(resultD);
    }

    /**
     * Action when receiving a CLIENT datagram
     * @param st
     */
    private void CLIENTAction(CLIENTDatagram clientD) {
        //TODO
        Main.logger.info("FYI, "+ players_id_name.get(clientD.id)+" runs version "+clientD.version+" for "+clientD.os);
    }
    
    public static PINGDatagram pingLocalServer(String host,int port){
        return pingServer(host,port,200);
    }
    
    public static PINGDatagram pingWebServer(String host,int port){
        return pingServer(host,port,4000);
    }
    
    private static PINGDatagram pingServer(String host,int port,int timeout){
        
        PINGDatagram pingD=null;

        //connection avec le serveur
        Socket socket = null;
        PrintWriter out=null;
        BufferedReader in = null;
        SocketAddress sockaddr = new InetSocketAddress(host, port);
        try {
            socket = new Socket();
            socket.connect(sockaddr,timeout);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }catch (Exception e) {
            Main.logger.info("Connection with "+host+":"+port+" failed");
            return null;
        }
        
        //récupération des données
        String packet=null;
        String[] datagram;
        Key key=null;

        out.println("PING|");
        
        try{
            if ((packet = in.readLine()) != null){

                datagram = packet.split("\\|");

                try{
                    key=Key.valueOf(datagram[0]);
                    if(key==Key.PING){
                        pingD = new PINGDatagram(datagram);
                    }
                }catch(Exception e){
                    Main.logger.warn("Illegal datagram received from server: "+packet);
                }
            }
        }catch(IOException e){
            Main.logger.warn("Error while sending data to server "+host+":"+port);
        }finally{
            try{
                out.close();
                socket.close();
                in.close();
            }catch(Exception ex){}
        }
        return pingD;
    }
}

