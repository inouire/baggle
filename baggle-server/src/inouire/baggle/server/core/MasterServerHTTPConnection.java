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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import inouire.baggle.server.Main;
import inouire.baggle.server.bean.OneScore;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class MasterServerHTTPConnection {
    
    private static String token;

    private static final LinkedList<OneScore> scoresSubmitQueue=new LinkedList<OneScore>();
    
    /**
     * Register or heartbeat to the master server
     */
    public static void register(){
        
        //register to master
        String result=callWs("registerserver.php?port="+Main.server.configuration.getListenningPort());
        
        if(result==null){
            SimpleLog.logger.error("Impossible to contact masterserver on "+Main.server.configuration.getMasterServerHost()+":"+
                        Main.server.configuration.getMasterServerPort());
            SimpleLog.logger.info("You should consider not registering to masterserver if you know that "+
                    Main.server.configuration.getMasterServerHost()+
                    " is not reachable from this network");
            return;
        }
        
        try{//manual parsing to get the token
            result=result.trim();
            String[] decomp=result.split("\"");
            if(decomp[5].equals("token")){
                token=decomp[7];
                SimpleLog.logger.debug("Registration token updated");
            }else{
                SimpleLog.logger.warn("Error during registration to master server: "+decomp[7]);
            }
        }catch(Exception e){
            SimpleLog.logger.warn("Error while parsing json for registration token");
            e.printStackTrace();
        }
        
    }
    
    public static int testPing(){

        
        //call web service
        String result=callWs("pingme.php?port="+Main.server.configuration.getListenningPort());
        
        if(result==null){
            SimpleLog.logger.error("Impossible to contact masterserver on "+Main.server.configuration.getMasterServerHost()+":"+
                        Main.server.configuration.getMasterServerPort());
            return 1;
        }
        
        //TODO make a real parsing
        try{
            //DECOMP[2]=code de retour
            //{"status":"4","reason":"server not pingable on port 42345"}
            result=result.trim();
            String[] decomp=result.split("\"");
            if(decomp[2].equals("0")){
                SimpleLog.logger.info("Server pingable on port "+Main.server.configuration.getListenningPort());
                return 0;
            }else{
                SimpleLog.logger.warn("Server not pingable on port "+Main.server.configuration.getListenningPort());
                return 2;
            }
        }catch(Exception e){
            SimpleLog.logger.warn("Error while parsing json for result code");
            return 2;
        }
    }
    
    /**
     * Submit all the scores of the queue
     */
    public static void submitScores(){
        synchronized(scoresSubmitQueue){
            while(!scoresSubmitQueue.isEmpty()){
                submitScore(scoresSubmitQueue.removeFirst());
            }
        }
    }        

    /**
     * Send the score to the master server
     * @param score the score to submit
     */
    private static void submitScore(OneScore score){
        
        //build submit string
        String serviceURL = "submitscore.php?token="+token+score.toURL();
        
        //call web service with this url
        String result=callWs(serviceURL);
        
        if(result==null){
            SimpleLog.logger.warn("Impossible to submit score at "+serviceURL);
            return;
        }else if(result.trim().equals("{\"status\":\"0\"}")){
            SimpleLog.logger.debug("Score successfully submitted to "+serviceURL);
        }else{
            SimpleLog.logger.warn("Error while submitting score at "+serviceURL+", "+result);
        }
    }
    
    /**
     * Add a score to the queue
     * @param score the score to add
     */
    public static void addScore(OneScore score){
        
        synchronized(scoresSubmitQueue){
            scoresSubmitQueue.add(score);
        }
        SimpleLog.logger.debug("Adding score: "+score.toURL());
        
    }
    /**
     * Call to a ws and get its answer
     * @param ws_name the name of the ws to call
     * @return the answer of the ws
     */
    private static String callWs(String ws_name){
        String line=null;
        try{
            URL serverListURL = new URL("http", Main.server.configuration.getMasterServerHost(),Main.server.configuration.getMasterServerPort(),"/ws/"+ws_name);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverListURL.openStream()));
            line="";
            String a="";
            while(a!=null){
                line+=a.trim();
                a=in.readLine();
            }
            in.close();
        }catch(Exception e){
            //e.printStackTrace();
            SimpleLog.logger.debug("Error while getting ws answer from masterserver");
        }
        return line;
    }
}
