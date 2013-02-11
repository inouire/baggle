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

/**
 *
 * @author Edouard de Labareyre
 */
public class FrequentTask extends TimerTask{

    @Override
    public void run() {
        
        //period should be ~10 sec
        Main.server.gameThread.players.pauseInactivePlayers();
        Main.server.gameThread.players.cleanupOldZombies();
    }

}
