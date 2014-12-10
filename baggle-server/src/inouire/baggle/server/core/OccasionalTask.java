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
