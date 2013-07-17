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

import java.util.TimerTask;
import inouire.baggle.server.Main;
import inouire.basics.SimpleLog;

/**
 *
 * @author Edouard de Labareyre
 */
public class OccasionalTask extends TimerTask{

    @Override
    public void run() {
        
        //~5 minutes
        
        if(Main.server.configuration.registerToMasterServer){
            //submit scores
            try{
                MasterServerHTTPConnection.submitScores();
            }catch(Exception e){
                SimpleLog.logger.warn("Error while submitting scores");
            }
            //register
            try{
                MasterServerHTTPConnection.register();
            }catch(Exception e){
                SimpleLog.logger.warn("Error while registering to master server");
            }
        }
        
        //remove player who are in pause for too long
        Main.server.gameThread.players.cleanupOldPausedPlayers();
    }

}
