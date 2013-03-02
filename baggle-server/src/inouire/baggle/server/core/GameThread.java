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

import java.util.ArrayList;
import inouire.baggle.server.Main;
import inouire.baggle.server.bean.ServerConfigXML;
import inouire.baggle.solver.GameBoard;
import inouire.baggle.solver.Solver;
import inouire.basics.SimpleLog;

/**
 *
 * @author Edouard de Labareyre
 */
public class GameThread extends Thread{

    private boolean is_playing=false;//true if playing, false if idle

    //game ressources
    public GameBoard game_board;
    public Solver grid_solver;

    public String grid="BAGGLEBAGGLEBAGG";
    public int grid_total=0;
    public ArrayList<String> solutions=new ArrayList<String>();
    
    public PlayerList players;
    
    private int total_number_of_words_found=0;
    public int elapsed_time=0;
        
    public GameThread(){
        
        this.setName("gameThread");
        
        ServerConfigXML config = Main.server.configuration;
        
        //game board init
        game_board= new GameBoard(config.isBigBoard(),config.getLanguage());
        if(config.isBigBoard()){
            grid="BAGGLEBAGGLEBAGGLEBAGGLEB";
        }
        
        //solver initialisation
        try {
            grid_solver=new Solver(config.getLanguage(),config.isParentalFilter(),config.isBigBoard());
        } catch (Exception ex) {
            SimpleLog.logException(ex);
        }
    }

    @Override
    public void run(){
        SimpleLog.logger.info("Starting game thread");
        SimpleLog.logger.info("Waiting for players to connect");

        boolean was_reset=false;//true if players performed a reset

        //main loop
        while(true){
            try {

                //wait for players to be ready
                if(!was_reset){
                    
                    //special inactivity timeout when waiting for ready players
                    Main.server.configuration.setShortInactivityTimeout();
                
                    SimpleLog.logger.info("Waiting for players to be ready");
                    while(!players.hasEnoughReadyPlayers()){
                        //sleep while waiting for players
                        Thread.sleep(800);
                    }
                }
                was_reset=false;
                SimpleLog.logger.info("Starting a new game ("+players.getNumberOfPlayers()+" players)");

                //mix grid
                SimpleLog.logger.debug("Virtually rolling dices to create a new grid");
                grid=game_board.mixBoard();

                //solve grid
                SimpleLog.logger.debug("Solving grid");
                ArrayList<String> S;
                while((S = grid_solver.solveGrid(game_board))==null){
                    SimpleLog.logger.warn("Error while solving grid, retrying");
                }
                solutions=S;
                grid_total=Solver.getNbPoints(solutions);

                //keeping it cool
                try{sleep(250);}catch(Exception e){}
                
                Main.server.configuration.setLongInactivityTimeout();
                players.touchAll();
                
                //init a new game
                players.resetWordsFound();
                players.resetStatus();
                players.broadcastGrid(grid,grid_total);
                players.broadcastTime(0);
                total_number_of_words_found = 0;
                is_playing=true;
                elapsed_time=0;
                long start_date=System.currentTimeMillis();
                
                SimpleLog.logger.debug("Start game");
                SimpleLog.logger.info("Grid: "+grid);

                //game loop, break when no more time or when reset expected
                while(elapsed_time<Main.server.configuration.getGameTime()){
                    
                    //sleep a bit
                    try{sleep(1000);}catch(Exception e){}

                    //how many time 
                    elapsed_time=(int)((System.currentTimeMillis()-start_date)/1000);

                    if(elapsed_time%5==0){
                        players.broadcastTime(elapsed_time);
                        SimpleLog.logger.info(elapsed_time+" seconds elapsed");
                    }

                    //listen to the people
                    if(players.everybodyWantsToReset()){
                        was_reset=true;
                        break;
                    }
                }
                
                is_playing=false;
                players.resetStatus();
                
                SimpleLog.logger.info("End of the game");
                
                if(!was_reset){
                    SimpleLog.logger.info("Sending results");
                    players.computeAndBroadcastResults();
                }else{
                    players.broadcastResetSignal();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SimpleLog.logger.error("Anormal exception caught in game thread: please report this");
            }
        }
    }
    
    public int getRemainingTime(){
        return Main.server.configuration.getGameTime() - elapsed_time;
    }
    
    public boolean isInGame(){
        return is_playing;
    }
    
    public int getTotalNbOfWordsFound(){
        return total_number_of_words_found;
    }
    
    public void addNewWordFound(){
        total_number_of_words_found++;
    }
    
    public void removeNbWordsFound(int nbWords){
        total_number_of_words_found-=nbWords;
    }
}
