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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import inouire.baggle.datagrams.*;
import inouire.baggle.server.Language;
import inouire.baggle.server.Main;
import inouire.baggle.server.bean.OneScore;
import inouire.baggle.server.bean.Player;
import inouire.baggle.types.Status;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class PlayerList {
    
    private Player[] playerList;
    private int maxPlayers;

    public static int ID_COUNTER=100;

        
    public PlayerList(int maxNbPlayers){
        
        this.maxPlayers=maxNbPlayers;
        this.playerList=new Player[maxNbPlayers];
        
        //initialize the list
        for(int k=0;k<maxNbPlayers;k++){
            playerList[k]=null;
        }
    }
    
    /**
     * Get a player of the list from its id
     * @return the player that has been found, null if no id matched
     */
    public Player getPlayer(int id){
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k] != null){
                if(playerList[k].id == id){
                    return playerList[k];
                }
            }
        }
        return null;
    }
    
    /**
     * Get a player of the list from its socket
     * @return the player that has been found, null if no socket matched
     */
    public Player getPlayer(SocketChannel socket){
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k] != null){
                if(playerList[k].socket == socket){
                    return playerList[k];
                }
            }
        }
        return null;
    }
    
    /**
     * Properly add a player in the list
     * @param newPlayer the player to add to the list
     */
    public synchronized void addPlayer(Player newPlayer){
        SimpleLog.logger.info("New player "+newPlayer.name);
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k] == null){
                playerList[k] = newPlayer;
                return;
            }
        }
    }

    /**
     * Properly remove a player from the list
     * @param id the id of the player to remove
     */
    public synchronized void removePlayer(int id){
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k]!=null){
                if(playerList[k].id==id){
                    String name = playerList[k].name;
                    Main.server.gameThread.removeNbWordsFound(playerList[k].getNumberOfWordsFound());
                    playerList[k]=null;
                    broadcast(new LEAVEDatagram(id).toString());
                    SimpleLog.logger.info("Player "+name+" left the room");
                    return;
                }
            }
        }
    }
    
    /**
     * Properly remove a player from the list
     * @param id the id of the player to remove
     */
    public synchronized void removePlayer(SocketChannel socket){
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k]!=null){
                if(playerList[k].socket==socket){
                    int id = playerList[k].id;
                    String name = playerList[k].name;
                    Main.server.gameThread.removeNbWordsFound(playerList[k].getNumberOfWordsFound());
                    playerList[k]=null;
                    broadcast(new LEAVEDatagram(id).toString());
                    SimpleLog.logger.info("Player "+name+" left the room");
                    return;
                }
            }
        }
    }
    
    /**
     * Turn a player into a zombie, and set it to pause
     * @param socket the socket corresponding to the player to tuen into a zombie
     */
    public synchronized void turnPlayerToZombie(SocketChannel socket){
        for(Player p : playerList){
            if(p!=null){
                if(p.socket==socket){
                    p.is_zombie=true;
                    SimpleLog.logger.info("Player "+p.name+" is now a zombie");
                    p.socket=null;
                    p.goToStatus(Status.PAUSE);
                    return;
                }
            }
        }
    }

    /**
     * Touch all the players that are not in pause
     */
    public void touchAll(){
        SimpleLog.logger.debug("Touching all non-paused players");
        for(Player p : playerList){
            if(p != null && !p.is_zombie && p.status!=Status.PAUSE){
                p.touch();
            }
        }
    }
    
    /**
     * Display the list of the zombies players
     */
    public void displayZombiePlayers(){
        for(Player p : playerList){
            if(p != null && p.is_zombie){
                SimpleLog.logger.debug("zombie: "+p.toString());
                return;
            }
        }
    }

    /**
      * Test if a zombie player can come back
      * @param id the id of the player
      * @param auth its auth code
      * @return the player if it worked, null if not
      */
    public Player tryToComeBack(int id, String auth,SocketChannel socket){
        for(Player p : playerList){
            if(p != null && p.is_zombie){
                if(p.id==id && p.auth_token.equals(auth)){
                    p.setSocket(socket);
                    p.is_zombie=false;
                    return p;
                }
            }
        }
        return null;
    }
    
    /**
     * Test if the if the player is inactive for too long
     * If it's the case, set the player to pause.
     */
    public void pauseInactivePlayers() {
        SimpleLog.logger.debug("Setting inactive players to pause");
        double now_time=System.currentTimeMillis();
        for(Player p : playerList){
            if(p!=null){
                if(p.status==Status.IDLE && now_time - p.getTimeOfLastAction()
                        > Main.server.configuration.inactivityTimeout ){
                    p.goToStatus(Status.PAUSE);
                    SimpleLog.logger.info(p.name+" is having an automatic break.");
                }
            }
        }
    }
    
    /**
     * Remove the zombies who (which?) are too old.
     */
    public void cleanupOldZombies() {
        SimpleLog.logger.debug("Cleaning up zombies");
        double now_time=System.currentTimeMillis();
        for(Player p : playerList){
            if(p != null && p.is_zombie){
                if(now_time - p.zombie_time > Main.server.configuration.zombieTimeout){
                    SimpleLog.logger.info("Killing a zombie");
                    removePlayer(p.id);
                }
            }
        }
    }

    /**
     * Remove the players in pause who are really too old (>60min)
     */
    public void cleanupOldPausedPlayers() {
        SimpleLog.logger.debug("Cleaning up players paused for too long");
        double now_time=System.currentTimeMillis();
        for(Player p : playerList){
            if(p != null && p.status==Status.PAUSE){
                if(now_time - p.getTimeOfLastAction() > Main.server.configuration.kickTimeout){
                    SimpleLog.logger.info("Killing a player paused for too old");
                    removePlayer(p.id);
                }
            }
        }
    }
    
    /**
     * Send the version of each player connected to the room
     * @param T
     */
    public void sendClientsVersion(Player q){
        for(Player p : playerList){
            if(p!=null && !p.equals(q)){
                q.send(new CLIENTDatagram(p.id,p.version,p.os).toString());
            }
        }
    }
    
    /**
     * Reset the state of each of the players to idle if it was active
     */
    public void resetStatus(){
        for(Player p : playerList){
            if(p!=null){
                if(p.status!=Status.PAUSE){
                    p.goToStatus(Status.IDLE);
                }
            }
        }
    }

    /**
     * Tests if the server has enough ready players to start a new game
     * @return true if there are enough players, false if not
     */
    public boolean hasEnoughReadyPlayers(){
        //ready players needed: half + 1
      
        int ready=0;
        int active=0;
        boolean result=false;

        if(getNumberOfPlayers()==0){//no players on the server
            result=false;
        }else{
            for(Player p : playerList){
                if(p!=null){
                    if(p.status==Status.READY){
                        ready++;
                    }
                    if(p.status!=Status.PAUSE){
                        active++;
                    }
                }
            }
            if(ready > active/2){
                result=true;
            }
        }
        return result;
    }

    /**
     * Tests if all the players wants to reset the grid
     * @return true if all the players want to reset, false if not
     */
    public boolean everybodyWantsToReset() {
        int reset=0;
        int active=0;
        for(Player p : playerList){
            if(p!=null){
                if(p.status==Status.RESET){
                    reset++;
                }
                if(p.status!=Status.PAUSE){
                    active++;
                }
            }
        }
        if(active > 0 && reset == active){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Tests if the server is full
     * @return true if it is full, false if not
     */
    public boolean isFull(){
        if(getNumberOfPlayers()>=maxPlayers){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Get the number of players  connected to the server
     * @return the number of players
     */
    public int getNumberOfPlayers(){
        int result=0;
        for(Player p : playerList){
            if(p!=null){
                result++;
            }
        }
        return result;
    }

    /**
     * Erase the list of words found of all the players
     */
    public void resetWordsFound() {
        for(Player p : playerList){
            if(p!=null){
                p.purgeWordsFound();
            }
        }
    }

    /**
     * Send all the necessary information to a newly connected player
     * @param T the client which to send the information
     */
    public void sendInitInformation(Player new_player) {

        for(Player p : playerList){
            if(p!=null){
                new_player.send(new JOINDatagram(p.name,p.avatar,p.id).toString());
                new_player.send(new STATUSDatagram(p.id,p.status).toString());
                new_player.send(new SCOREDatagram(p.id,p.total_score).toString());
            }
        }

        //special case if currently playing
        if(Main.server.gameThread.isInGame()){
            new_player.send(new STARTDatagram(Main.server.gameThread.grid,Main.server.gameThread.grid_total).toString());
            broadcastGauge();
            new_player.send(new TIMEDatagram(Main.server.gameThread.getRemainingTime(),Main.server.configuration.gameTime).toString());
        }else{
            new_player.send(new STOPDatagram("nogame").toString());
        }
        
    }
    
    /**
     * Broadcast the message to all the players connected, except zombies
     * @param message the message to broadcast
     */
    public void broadcast(String message){
        for(Player p : playerList){
            if(p!=null && !p.is_zombie && p.socket!=null ){
                Main.server.nioServer.send(p.socket,message);
            }
        }
    }
    
    /**
     * Broadcast the gauge information
     */
    public void broadcastGauge(){
        int total=Main.server.gameThread.getTotalNbOfWordsFound();
        if(total!=0){
            for(Player p : playerList){
                if(p!=null){
                    try{
                        int percent = (100*p.getNumberOfWordsFound())/total;
                        broadcast(new PROGRESSDatagram(p.id,percent).toString());
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Broadcast the signal of "end of game"
     */
    public void broadcastEndSignal(){
        broadcast(new STOPDatagram("eot").toString());
    }

    /**
     * Broadcast the signal of "reset of game"
     */
    public void broadcastResetSignal(){
        broadcast(new STOPDatagram("reset").toString());
    }
    
    /**
     * Computes and broadcast all the results to the players
     */
    public void computeAndBroadcastResults(){
        
        //agregate results for each player and broadcast them

        //create the list of players taken into account for the results
        ArrayList<Player> participants=new ArrayList<Player>();
        for(Player p : playerList){
            if(p!=null &&  (p.getNumberOfWordsFound()>0 || p.status != Status.PAUSE)){
                participants.add(p);
            }
        }

        broadcastEndSignal();

        //do nothing if participants list is empty
        if(participants.isEmpty()){
            return;
        }
        
        //all words count rule
        if(Main.server.configuration.allWordsCount){
            for(Player p : participants){
                //get sorted list of words found + score
                p.sortResultsAndComputeScore();
                SimpleLog.logger.debug(p.name+": "+p.score_result+" points (total "+p.total_score+")");
            }
        }else{
            for(Player p : participants){
                //create a list of the words found by the other players
                LinkedList<String> all = new LinkedList<String>();
                for(Player q : participants){
                    if(!q.equals(p)){
                        q.addResultsTo(all);
                    }
                }
                //get agregated and sorted list of words found + score
                p.agregateResultsAndComputeScore(all);
                SimpleLog.logger.debug(p.name+": "+p.score_result+" points (total "+p.total_score+")");
            }
        }
        
        //sort the participants to get the top three of the players
        Collections.sort(participants, new PlayerScoreComparator());
        
        //add points to total score broadcast eveything (results and rank)
        for(Player p : participants){
            p.total_score+=p.score_result;
            int rank = participants.indexOf(p)+1;
            if(rank<=3){
                broadcast(new RESULTDatagram(p.id,p.score_result,p.words_found_result,rank).toString());
            }else{
                broadcast(new RESULTDatagram(p.id,p.score_result,p.words_found_result).toString());
            }

        }
        
        //broadcast the total score of each player
        broadcastScores();
        
        
        //tell who won the game
        int best_score=0;
        LinkedList<Player> winners=new LinkedList<Player>();
        for(Player p : participants){
            if(p!=null){
                if(p.score_result==best_score){
                    winners.add(p);
                }
                if(p.score_result>best_score){
                    winners.clear();
                    winners.add(p);
                    best_score=p.score_result;
                }
            }
        }
        String message="";
        if(winners.size()>1){
            for(int k =0;k<winners.size()-1;k++){
                message+=winners.get(k).name;
                if(k<winners.size()-2) message+=", ";
            }
            message+=Language.getString(0)+winners.get(winners.size()-1).name;
            message+=Language.getString(1);
        }else if(winners.size()==1){
            message+=winners.get(0).name+Language.getString(2);
        }
        message+=Language.getString(3)+best_score+Language.getString(4);
        broadcast(new CHATDatagram(0,message).toString());
        
        
        //add the scores to the queue to send to master
        for(Player p : participants){
            if(!p.is_bot){//only for non-bot participants
                OneScore score = new OneScore(p.name,p.score_result);
                if(winners.contains(p)){
                    score.setWon(1);
                    score.setBeat(participants.size()-1);
                }else{
                    score.setWon(0);
                    score.setBeat(0);
                }
                int nbLongWords=0;
                for(String word:p.words_found_result){
                    if(word.length()>=6){
                        nbLongWords++;
                    }
                }
                score.setSixp(nbLongWords);
                MasterServerHTTPConnection.addScore(score);
            }
        }
          
        
        //display some big words that haven't been found (7 words max)
        
        //create a list of the words found by all the players
        LinkedList<String> all = new LinkedList<String>();
        for(Player p : participants){
            p.addResultsTo(all);
        }
                
        ArrayList<String> not_found=new ArrayList<String>();
        int A=0;
        for(String w : Main.server.gameThread.solutions){
            if(w.length()>=7){
                if(!all.contains(w)){
                    not_found.add(w);
                }
                A++;
            }
        }
        if(not_found.size()>0){
            String a="";
            int k=0;
            for(String w:not_found){
                a+=" "+w+",";
                k++;
                if(k>=6) break;
            }
            broadcast(new CHATDatagram(0, Language.getString(9)+":"+a.substring(0, a.length()-1)).toString());
        }else{
            if(A>0){
                broadcast(new CHATDatagram(0, Language.getString(8)).toString());
            }
        }
    }

    /**
     * Broadcast the grid to the players + the total nb of points of this grid
     */
    public void broadcastGrid(String grid, int nb_points){
        broadcast(new STARTDatagram(grid,nb_points).toString());
    }

    /**
     * Broadcast the time information to players
     * @param time
     */
    public void broadcastTime(int time){
        broadcast(new TIMEDatagram(Main.server.configuration.gameTime-time,Main.server.configuration.gameTime).toString());
    }

    /**
     * Broadcast the total score of each player
     */
    public void broadcastScores(){
        for(Player p : playerList){
            if(p!=null){
                broadcast(new SCOREDatagram(p.id,p.total_score).toString());
            }
        }
    }

    
    
    public static synchronized int getUniqueId(){
        if(ID_COUNTER>99999){
            ID_COUNTER=100;
        }
        ID_COUNTER++;
        return ID_COUNTER;
    }
    
    @Override
    public String toString(){
        String list="";
        for(int k=0;k<maxPlayers;k++){
            if(playerList[k] != null){
                list+=playerList[k].name+",";
            }
        }
        if(list.length()>0){
            list=list.substring(0, list.length()-1);
        }
        return list;
    }
}


class PlayerScoreComparator implements Comparator<Player> {

    public int compare(Player t1, Player t2) {
        if (t1.score_result > t2.score_result){
            return -1;
        }else if (t1.score_result < t2.score_result){
            return 1;
        }else{
            return 0;
        }
    }
    
}