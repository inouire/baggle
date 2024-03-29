package inouire.baggle.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import inouire.baggle.types.IllegalDatagramException;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class CachedServersList {
    
    private LinkedList<ServerEntry> cached_servers;
    private String list_name;
    
    private File LIST_FILE; 
            
    public CachedServersList(String name){
        this.LIST_FILE=new File(Main.CONFIG_FOLDER,name+"-cache.conf");
        this.list_name=name;
        this.cached_servers=new LinkedList<ServerEntry>();
    }
    
    /**
     * Load the server list from file
     * @param nb_days the max age in days for validity of an entry. invalid entry is not loaded
     */
    public void loadList(int nb_days){
        
        long timestamp_min = System.currentTimeMillis()-(86400000*nb_days);
        
        FileInputStream in;
        try {
            in = new FileInputStream(LIST_FILE);
        } catch (FileNotFoundException ex) {
            SimpleLog.logger.warn("Impossible to open file at "+LIST_FILE.getAbsolutePath());
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try{
            String line="";
            String[] content;
            ConfigKey key;
            String value;

            while((line = reader.readLine()) != null ){
                content = line.split("\\|");
                try{
                    ServerEntry entry = new ServerEntry(content);
                    if(entry.timestamp > timestamp_min){
                        cached_servers.add(entry);
                    }else{
                        SimpleLog.logger.debug("Not adding server entry, too old: "+line);
                    }
                }catch(IllegalDatagramException ide){
                    SimpleLog.logger.warn("Server entry not recognized: "+line);
                }
            }
            SimpleLog.logger.info("Successfully loaded cached servers list: "+LIST_FILE.getAbsolutePath());
        }catch(Exception e){
            SimpleLog.logger.warn("Error wile loading cached server list: "+LIST_FILE.getAbsolutePath());
        }finally{
            try{
                reader.close();
                in.close();
            }catch(IOException e){}
        }
    }
    
    /**
     * Save the server list to file
     */
    public void saveList(){
        try{
            FileWriter fw=new FileWriter(LIST_FILE);
            for(ServerEntry server : cached_servers){
                fw.write(server.toString()+"\n");
            }
            fw.close();
            SimpleLog.logger.info("Cached servers list '"+list_name+"' saved to file "+LIST_FILE.getAbsolutePath());
        }catch(IOException e){
            SimpleLog.logger.warn("Impossible to save cached server list to "+LIST_FILE.getAbsolutePath());
        }
        
    }
    
    public void touchCached(String host){
        for(ServerEntry server : cached_servers){
            if(server.port==null){
                if(server.host.equals(host)){
                    server.timestamp=System.currentTimeMillis();
                    SimpleLog.logger.debug(server.host+" has been touched");
                    return;
                }
            }
        }
        SimpleLog.logger.debug(host+" has been added");
        cached_servers.add(new ServerEntry(host));
    }
    
    public void touchCached(String host, int port){
         for(ServerEntry server : cached_servers){
            if(server.port!=null){
                if(server.host.equals(host) && server.port==port){
                    server.timestamp=System.currentTimeMillis();
                    SimpleLog.logger.debug(server.host+":"+server.port+" has been touched");
                    return;
                }
            }
        }
        SimpleLog.logger.debug(host+":"+port+" has been added");
        cached_servers.add(new ServerEntry(host,port));
    }
    
    /**
     * Get the list of implicit cached servers (no port specified)
     * @return the list of cached hosts (no port specified)
     */
    public LinkedList<ServerEntry> getImplicitHostList(){
        LinkedList<ServerEntry> host_list= new LinkedList<ServerEntry>();
        
        for(ServerEntry server : cached_servers){
            if(server.port==null){
                host_list.add(new ServerEntry(server.host));
            }
        }
        
        return host_list;
    }
    
    /**
     * Get the list of explicit cached servers (host and port specified)
     * @return the list of custom servers as SocketAddress
     */
    public LinkedList<ServerEntry> getExplicitHostList(){
        LinkedList<ServerEntry> host_list= new LinkedList<ServerEntry>();
        
        for(ServerEntry server : cached_servers){
            if(server.port!=null){
                host_list.add(new ServerEntry(server.host,server.port));
            }
        }
        
        return host_list;
    }
    
}

