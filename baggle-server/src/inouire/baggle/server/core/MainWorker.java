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

package inouire.baggle.server.core;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import inouire.baggle.datagrams.*;
import inouire.baggle.server.Main;
import inouire.baggle.server.bean.Player;
import inouire.baggle.server.bean.ServerConfigXML;
import inouire.baggle.server.bean.ServerDataEvent;
import inouire.baggle.types.IllegalDatagramException;
import inouire.baggle.types.Key;
import inouire.baggle.types.Status;
import inouire.baggle.types.Words;
import inouire.basics.SimpleLog;

/**
 *
 * @author Edouard de Labareyre
 */
public class MainWorker implements Runnable {
    
    private List queue = new LinkedList();

    public PlayerList players;
    private ServerConfigXML configuration;
    
    public MainWorker(ServerConfigXML configuration,PlayerList players){
        this.configuration=configuration;
        this.players = players;
    }
    
    public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        
        synchronized(queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
        
    }

    @Override
    public void run() {
        
        ServerDataEvent dataEvent;
        Player author;
        
        while(true) {
            
            // Wait for some data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
            }

            SimpleLog.logger.trace("[RCV<<] >"+dataEvent.message+"<");
            
            //try to get the author of the data
            author = players.getPlayer(dataEvent.socket);
            
            //if the author is not in the list of players yet: ping or nego
            if(author == null){
                if(dataEvent.message.startsWith("PING|")){
                    handlePing(dataEvent);                    
                }else{
                    handleNegociation(dataEvent);
                }
            }else{
                //assign the author and handle the response
                dataEvent.setAuthor(author);
                handleGame(dataEvent);
            }
        }
    }
    
    public void handlePing(ServerDataEvent dataEvent){
        dataEvent.nioServer.send(dataEvent.socket, getAnswerToPING());
    }
    
    public void handleNegociation(ServerDataEvent dataEvent){

        Key key = Key.valueOf(dataEvent.datagram[0]);
        try{
            switch(key){
                case CONNECT:
                    //Test if the server if full
                    if(Main.server.gameThread.players.isFull()){
                        dataEvent.nioServer.send(dataEvent.socket,new DENYDatagram("server_full").toString());
                        SimpleLog.logger.warn("Negociation failed: server is full");
                    }else{
                        CONNECTDatagram connectD = new CONNECTDatagram(dataEvent.datagram);
                        if(connectD!=null){
                            connectAction(connectD,dataEvent.socket);
                            //TODO gérer le cas ou ça ne marche pas
                            SimpleLog.logger.debug("Negociation successful!");
                        }
                    }
                    
                    break;
                case RECONNECT:
                    RECONNECTDatagram reconnectD = new RECONNECTDatagram(dataEvent.datagram);
                    if(reconnectD!=null){
                        reconnectAction(reconnectD,dataEvent.socket);
                    }
                    break;
                default:
                    SimpleLog.logger.debug("Unexpected datagram during negociation: "+dataEvent.message);
                    break;
            }
        }catch(IllegalDatagramException ide){
            SimpleLog.logger.debug("Illegal datagram during negociation: "+dataEvent.message);
        }
//        send(new DENYDatagram("bad_syntax").toString());
//        SimpleLog.logger.error("Negociation failed: bad syntax of first datagram.");
    }
    
    public void handleGame(ServerDataEvent dataEvent){
        try{
            Key key=Key.valueOf(dataEvent.datagram[0]);
            switch (key){
                case CHAT:
                    chatAction(dataEvent);
                    break;
                case STATUS:
                    statusAction(dataEvent);
                    break;
                case WORD:
                    wordAction(dataEvent);
                    break;
                case CLIENT:
                    clientAction(dataEvent);
                    break;
                case DISCONNECT:
                    disconnectAction(dataEvent);
                    break;
                default:
                    SimpleLog.logger.warn("Unexpected message "+dataEvent.message);
                    break;
            }
        }catch(Exception e){
            SimpleLog.logger.warn("Illegal message "+dataEvent.message);
        }
    }
    
    public String getAnswerToPING(){
        String mode;
        if(configuration.isAllWordsCount()){
            mode="all";
        }else{
            mode="trad";
        }
        
        int nb_players=players.getNumberOfPlayers();
        PINGDatagram P = new PINGDatagram(configuration.getListenningPort(),
                            configuration.getRoomName(),configuration.getLanguage(),
                            !configuration.isBlockChat(),configuration.getNbLettersMin(),
                            configuration.isParentalFilter(), mode,nb_players,
                            configuration.getMaxPlayers(),configuration.isIsPrivate(),
                            Main.server.gameThread.grid,configuration.getGameTime(),
                            players.toString());
        return P.toString();
    }

    public void reply(String message,SocketChannel socket){
        Main.server.nioServer.send(socket, message);
    }
    
    private void connectAction(CONNECTDatagram connect,SocketChannel socket) {
        if(connect == null) return;
        
        //generate unique player id
        int new_player_id=PlayerList.getUniqueId();
        
        //generate auth code
        String auth="bidon";//TODO do it for real
        
        //Player new_player = new Player(this,new_player_id,connect.nick,connect.logo,auth);
        Player new_player = new Player(socket,new_player_id,connect.nick,connect.logo,auth);

        String mode;
        if(Main.server.configuration.isAllWordsCount()){
            mode="all";
        }else{
            mode="trad";
        }
        reply(new ACCEPTDatagram(auth,new_player_id,configuration.getLanguage(),
                                !configuration.isBlockChat(),configuration.getNbLettersMin(),
                                configuration.isParentalFilter(),mode,configuration.getGameTime()).toString(),
                socket);

        //send information to this new player
        Main.server.gameThread.players.sendInitInformation(new_player);
        
        //add this new player to the player list
        players.addPlayer(new_player);
        
        //touch timestamp
        new_player.touch();
        
        //inform all the players about this new player
        players.broadcast(new JOINDatagram(connect.nick,connect.logo,new_player.id).toString());

        //send the welcome message
        reply(new CHATDatagram(0,Main.server.configuration.getWelcomeMessage()).toString(),socket);

    }
    
    private void reconnectAction(RECONNECTDatagram reconnect,SocketChannel socket) {

        SimpleLog.logger.debug("Trying reconnection");

        players.displayZombiePlayers();

        Player back = players.tryToComeBack(reconnect.id,reconnect.auth,socket);
        if(back==null){
            SimpleLog.logger.info("Nobody has been resurrected.");
            return;
        }else{
            SimpleLog.logger.info(back.name+" has been resurrected!");
            
            String mode;
            if(Main.server.configuration.isAllWordsCount()){
                mode="all";
            }else{
                mode="trad";
            }
            reply(new ACCEPTDatagram(back.auth_token,back.id,configuration.getLanguage(),
                                !configuration.isBlockChat(),configuration.getNbLettersMin(),
                                configuration.isParentalFilter(),mode,configuration.getGameTime()).toString(),socket);

            //send information to this player and add it to the players list
            players.sendInitInformation(back);

        }
    }

    private void statusAction(ServerDataEvent dataEvent) throws IllegalDatagramException {
        
        STATUSDatagram statusD = new STATUSDatagram(dataEvent.datagram);

        //some status change are forbidden depending on game or not
        if(Main.server.gameThread.isInGame()){
            if(statusD.state != Status.READY){
                dataEvent.author.goToStatus(statusD.state);
            }
        }else{
            if(statusD.state != Status.RESET){
                dataEvent.author.goToStatus(statusD.state);
            }
        }
    }
    
    private void chatAction(ServerDataEvent dataEvent) throws IllegalDatagramException {
        
        CHATDatagram chatD = new CHATDatagram(dataEvent.datagram);

        //send back this message to all the players
        players.broadcast(new CHATDatagram(dataEvent.author.id,chatD.msg).toString());
    }
    
    private void wordAction(ServerDataEvent dataEvent) throws IllegalDatagramException {
        
        WORDDatagram wordD = new WORDDatagram(dataEvent.datagram);

        GameThread game = Main.server.gameThread;

        //if not playing -> chat, if playing-> testing the dic
        if(game.isInGame()){
            Words status=null;
            dataEvent.author.touch();
            
            if(wordD.word.length() < Main.server.configuration.getNbLettersMin()){ //word too short
                status=Words.SHORT;
            }else if(game.solutions.contains(wordD.word)){
                if(dataEvent.author.foundAWord(wordD.word)==true){//word already found
                    status=Words.ALREADY_FOUND;
                }else{
                    status=Words.GOOD;
                }
            }else{
                if(game.grid_solver.getDictionnary().contains(wordD.word)){
                    status=Words.NOT_IN_GRID;
                }else{
                    status=Words.NOT_IN_DIC;
                }
            }
            //immediatly send the status
            reply(new WORDDatagram(wordD.word,status).toString(),dataEvent.socket);
            
            //diffuser les jauges si le mots était correct
            if(status==Words.GOOD){
                Main.server.gameThread.players.broadcastGauge();
            }
            //remettre le joueur en idle même si le mot n'est pas bon
            if(dataEvent.author.status==Status.PAUSE){
                dataEvent.author.goToStatus(Status.IDLE);
            }
        }else{
            //a word out of a game is considered as chat
            players.broadcast(new CHATDatagram(dataEvent.author.id,wordD.word).toString());
        }
        
    }
    
    private void disconnectAction(ServerDataEvent dataEvent) throws IllegalDatagramException {
        players.removePlayer(dataEvent.author.id);
    }
    
    private void clientAction(ServerDataEvent dataEvent) throws IllegalDatagramException {
        CLIENTDatagram clientD = new CLIENTDatagram(dataEvent.datagram);
        dataEvent.author.os=clientD.os;
        dataEvent.author.version=clientD.version;
        if(clientD.os.toLowerCase().startsWith("bot")){
            dataEvent.author.is_bot=true;
        }
        
    }
} 
