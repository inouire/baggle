package inouire.baggle.client.threads;

import inouire.baggle.client.Main;
import inouire.baggle.client.ServerEntry;
import inouire.baggle.client.gui.modules.OfficialServersPanel;
import inouire.baggle.datagrams.Datagram;
import inouire.baggle.datagrams.PINGDatagram;
import inouire.basics.SimpleLog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author edouard
 */
public class MasterServerHTTPConnection extends Thread{
    
    private OfficialServersPanel listener;
    
    public MasterServerHTTPConnection(OfficialServersPanel listener){
        this.listener=listener;
    }
    
    @Override
    public void run(){
        
        //prepare HMI
        listener.setStartOfPingProcess();
        
        //get local list if any
        SimpleLog.logger.info("Loading cached servers list");
        LinkedList<ServerEntry> memorized_servers = Main.cachedServers.getExplicitHostList();
        
        //get a new id for refresh process
        int refresh_id = listener.newRefreshId(memorized_servers.size());
        
        //ping the servers and add them to the panel if they are ok
        for(ServerEntry server : memorized_servers){
            new ServerPingThread(server,refresh_id,listener).start();          
        }
        
        //get list from master
        SimpleLog.logger.info("Completing with online server list");
        LinkedList<ServerEntry> connected_servers = getServersList();
        LinkedList<ServerEntry> new_servers = new LinkedList<ServerEntry>();
        
        //ping the servers and add them to the panel if they are ok
        if (connected_servers != null) {
            for(ServerEntry server : connected_servers){
                if (isNewServer(server, memorized_servers)){
                    new ServerPingThread(server,refresh_id,listener).start();
                }
            }
        }       
    }
    
    public static Boolean isNewServer(ServerEntry server, LinkedList<ServerEntry> memorized_servers) {
        for(ServerEntry memorized_server : memorized_servers) {
            if (memorized_server.equals(server)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get all the scores for a given name
     * @param name the name for which to search the score
     * @return the scores as an int array
     */
    public static int[] getScores(String name){
        //remove accents
        String name_no_accents=Datagram.removeAccents(name);
        
        //get scores
        String scores=getWsAnswer("getscore.php?name="+name_no_accents);
        if(scores==null){
            return null;
        }
        
        try{
            //{"name":"Edouard","general_rank":53,"hunter_score":261,"brute_score":37,"winner_score":0,"star_score":4,"nb_players":82}
            Object obj=JSONValue.parse(scores);
            JSONObject jso=(JSONObject)obj;
            int general_rank=Integer.parseInt(jso.get("general_rank").toString());
            int hunter_score=Integer.parseInt(jso.get("hunter_score").toString());
            int brute_score=Integer.parseInt(jso.get("brute_score").toString());
            int winner_score=Integer.parseInt(jso.get("winner_score").toString());
            int star_score=Integer.parseInt(jso.get("star_score").toString());
            int nb_players=Integer.parseInt(jso.get("nb_players").toString());
            
            return new int[]{general_rank,nb_players,hunter_score,brute_score,winner_score,star_score};
            
        }catch(Exception e){
            //test bourrin pour voir si le nom n'est juste pas en base
            if(scores.contains("\"status\":\"1\"")){
                //TODO rendre plus propre le renvoi d'erreurs
                return new int[]{0};
            }
            SimpleLog.logger.warn("Error while parsing json for score");
            return null;
        }

    }
    
    /**
     * Get all the ranks for a given name
     * @param name the name for which to search the ranks
     * @return the ranks as an in array
     */
    public static int[] getRanks(String name){
        //remove accents
        String name_no_accents=Datagram.removeAccents(name);
        
        //get scores
        String ranks=getWsAnswer("getrank.php?name="+name_no_accents);
        if(ranks==null){
            return null;
        }
        
        //{"name":"Ersatz","general_rank":3,"hunter_rank":3,"brute_rank":2,"winner_rank":3,"star_rank":5,"nb_players":82}
        
        try{
            Object obj=JSONValue.parse(ranks);
            JSONObject jso=(JSONObject)obj;
            int general_rank=Integer.parseInt(jso.get("general_rank").toString());
            int hunter_rank=Integer.parseInt(jso.get("hunter_rank").toString());
            int brute_rank=Integer.parseInt(jso.get("brute_rank").toString());
            int winner_rank=Integer.parseInt(jso.get("winner_rank").toString());
            int star_rank=Integer.parseInt(jso.get("star_rank").toString());
            int nb_players=Integer.parseInt(jso.get("nb_players").toString());
            
            return new int[]{general_rank,nb_players,hunter_rank,brute_rank,winner_rank,star_rank};
            
        }catch(Exception e){
            SimpleLog.logger.warn("Error while parsing json for rank");
            return null;
        }

    }
    
    static LinkedList<ServerEntry> getServersList(){
        String list=getWsAnswer("getlist.php");
        if(list==null || list.isEmpty()){
            return null;
        }
        
        Object obj=JSONValue.parse(list);
        
        LinkedList<ServerEntry> serversList = new LinkedList<ServerEntry>();
        
        //TODO add a try catch ?
        JSONArray array=(JSONArray)obj;
        String host;
        int port;
        for(Object o : array){
            try{
                JSONObject jso=(JSONObject)o;
                host=(String) jso.get("ip");
                port=Integer.parseInt(jso.get("port").toString());
                serversList.add(new ServerEntry(host,port));
            }catch(Exception e){
                SimpleLog.logger.warn("Error while parsing json for server list");
            }
        }
        
        return serversList;
    }
    
    /**
     * Check if a more recent build is available
     * @return null if not, and if yes an array containing the number of version + version message
     */
    public static String[] checkNewVersion(){
        String version_info=getWsAnswer("getversion.php");
        
        if(version_info==null){
            return null;
        }
        
        try{
            Object obj=JSONValue.parse(version_info);
            JSONObject jso=(JSONObject)obj;
            String version=(String) jso.get("version");
            int build=Integer.parseInt(jso.get("build").toString());
            String message=(String) jso.get("message");
            
            if(build>Main.BUILD){
                return new String[]{version,message};
            }else{
                return null;
            }
         }catch(Exception e){
            SimpleLog.logger.warn("Error while parsing json for checkversion");
            return null;
        }     
    }
    
    /**
     * Get the output of a call to a ws
     * @param ws_name the name of the ws to call
     * @return the answer of the ws
     */
    static String getWsAnswer(String ws_name){
        String line;
        try{
            URL serverListURL = new URL("http", Main.MASTER_SERVER_HOST,Main.MASTER_SERVER_PORT,"/ws/"+ws_name);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverListURL.openStream()));
            line="";
            String a="";
            while(a!=null){
                line+=a.trim();
                a=in.readLine();
            }
            in.close();
        }catch(Exception e){
            SimpleLog.logger.error("Error while getting ws answer from masterserver at "+ws_name);
            line = null;
        }
        return line;
    }
    
}

class ServerPingThread extends Thread{
    
    private ServerEntry server;
    private int id;
    private OfficialServersPanel listener;
    
    public ServerPingThread(ServerEntry se,int id,OfficialServersPanel listener){
        this.server=se;
        this.id=id;
        this.listener=listener;
    }
    
    @Override
    public void run(){
        PINGDatagram pingD = ServerConnection.pingWebServer(server.host,server.port);
        if(pingD!=null){
            SimpleLog.logger.debug("PING ok on "+server.host+":"+server.port);
            listener.addValidServer(id,server.host, server.port, pingD);
            Main.cachedServers.touchCached(server.host,server.port);
        }else{
            listener.addFailedServer(id);
            SimpleLog.logger.debug("PING failed on "+server.host+":"+server.port);
        }
    }
}
