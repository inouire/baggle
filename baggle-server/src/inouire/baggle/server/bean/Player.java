 /* Copyright 2009-2014 Edouard Garnier de Labareyre
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

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import inouire.baggle.datagrams.STATUSDatagram;
import inouire.baggle.server.Main;
import inouire.baggle.solver.Solver;
import inouire.baggle.types.Status;
import inouire.basics.SimpleLog;

/**
 *
 * @author Edouard de Labareyre
 */
public class Player {

    public SocketChannel socket;
    
    public int id=-1;
    public String name="Unkwown player";
    public String avatar="unknown";
    public int total_score=0;
    public int nb_beaten=0;
    public Status status=Status.IDLE;
    public boolean is_zombie=false;
    public boolean is_bot=false;

    public String version="?";
    public String os="?";

    public String auth_token="";

    public double zombie_time=0;
    private double time_of_last_action=0;

    private final LinkedList<String> words_found=new LinkedList<String>();
    private final Lock wfm=new ReentrantLock();//semaphore for words_found
    
    public int score_result=0;
    public LinkedList<String> words_found_result;

    public Player(SocketChannel socket,int id,String name,String avatar,String auth_token){
        this.id=id;
        this.name=name;
        this.avatar=avatar;
        this.auth_token=auth_token;
        this.socket=socket;
    }

    public void touch(){
        this.time_of_last_action=System.currentTimeMillis();
    }
    
    public double getTimeOfLastAction(){
        return time_of_last_action;
    }
    
    
    public void send(String message){
        Main.server.nioServer.send(socket,message);
    }
    
    public void setSocket(SocketChannel socket){
        this.socket=socket;
    }
    
    public void goToStatus(Status state){
        //modify player current state
        this.status=state;

        //notify all the players of the changing state
        Main.server.gameThread.players.broadcast(new STATUSDatagram(id,status).toString());
    }

    /**
     * Add a word in the list of words found by the player
     * @param word the word to add
     * @return true if the word already existed, false if not
     *
     */
    public boolean foundAWord(String word){
        boolean already=false;
        wfm.lock();
        try{
            if(words_found.contains(word.toUpperCase())){
                already=true;
            }else{
                words_found.add(word.toUpperCase());
                Main.server.gameThread.addNewWordFound();
                already=false;
            }   
        }catch(Exception e){
        }finally{
            wfm.unlock();
            return already;
        }
    }

    /**
     * Reset the list of words found
     */
    public void purgeWordsFound(){
        wfm.lock();
        try{
            words_found.clear();
        }catch(Exception e){
        }finally{
            wfm.unlock();
        }
    }

    /**
     * Returns the number of words the player found in the list
     * @return
     */
    public int getNumberOfWordsFound(){
        return words_found.size();
    }

    @Override
    public String toString(){
        return name+"/"+id+"/"+auth_token;
    }

    /**
     * Add the results of the player in the list passed as argument
     * @param all
     */
    public void addResultsTo(LinkedList<String> all) {
        wfm.lock();
        try{
            for(String a:words_found){
                if(!all.contains(a)){
                    all.add(a);
                }
            }
        }catch(Exception ex){
            SimpleLog.logException(ex);
        }finally{
            wfm.unlock();
        }
    }

    public void sortResultsAndComputeScore(){
        int points=0;
        words_found_result=new LinkedList<String>();
        wfm.lock();
        try{
            //sort the list by word length
            Collections.sort(words_found,new StringLengthComparator());
            
            //get the corresponding score
            for(String word: words_found){
                points+=Solver.getPoints(word, Main.server.configuration.rewardBigWords);
                words_found_result.add(word);
            }
        }catch(Exception e){
        }finally{
            wfm.unlock();
        }
        this.score_result=points;
    }
    
    public void agregateResultsAndComputeScore(LinkedList<String> all){
        int points=0;
        words_found_result=new LinkedList<String>();
        wfm.lock();
        try{
            //eleminate word found by other players
            for(String word: words_found){
                if(!all.contains(word)){
                    words_found_result.add(word);
                }
            }
            
            //sort result
            Collections.sort(words_found_result,new StringLengthComparator());
            
            //compute score
            for(String word: words_found_result){
                points+=Solver.getPoints(word, Main.server.configuration.rewardBigWords);
            }
        }catch(Exception e){
        }finally{
            wfm.unlock();
        }
        this.score_result=points;
    }

}

class StringLengthComparator implements Comparator<String> {
    
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() > o2.length()){
            return -1;
        }else if (o1.length() < o2.length()){
            return 1;
        }else{
            return 0;
        }
    }
    
}

