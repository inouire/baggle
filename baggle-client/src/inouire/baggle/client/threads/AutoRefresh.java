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

import java.util.TimerTask;
import inouire.baggle.client.Main;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class AutoRefresh extends TimerTask{

    @Override
    public void run() {
        
        if(!Main.mainFrame.inRoom()){
            SimpleLog.logger.debug("Auto refresh servers list & scores");
        
            //get information from master server
            new MasterServerHTTPConnection(Main.mainFrame.connectionPane.officialServersPane).start();
            
            //then get scores
            Main.mainFrame.connectionPane.sideConnectionPane.scoresPane.updateScores();
        }
        
    }
    
}
