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
