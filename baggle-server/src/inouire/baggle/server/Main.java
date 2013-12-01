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

package inouire.baggle.server;

import java.io.File;
import inouire.baggle.server.core.BaggleServer;
import inouire.baggle.server.gui.MainFrame;
import inouire.basics.Args;
import inouire.basics.SimpleLog;

/**
 *
 * @author Edouard de Labareyre
 */
public class Main {

    public static String VERSION = "3.3";
    public static boolean WITH_GUI = false;
    public static String CONFIG_FILE="";
    public static String DEFAULT_CONFIG_FILE="conf/baggle-server_config.myml";
    public static boolean DEV_MODE;
        
    //core process
    public static BaggleServer server;
    
    //main frame (gui only)
    public static MainFrame mainFrame = null;
    
    
    private static final String[] helpFlags = new String[] {"help","-h","--help"};
    private static final String[] guiFlags = new String[] {"-g","--gui"};
    private static final String[] configFlags = new String[] {"-c","--config"};
    
    /**
     * Main function
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if(Args.getOption(helpFlags, args)){
            printHelp();
            return;
        }else{
            printVersion();
            System.out.println("Use --help option to know more about the provided options");
        }

        WITH_GUI = Args.getOption(guiFlags, args);
        DEV_MODE = Args.getOption("--dev", args);
        
        SimpleLog.initConsoleConfig();
        
        if(WITH_GUI){
            SimpleLog.logger.info("Starting graphical user interface");
            DEFAULT_CONFIG_FILE = System.getProperty("user.home")+File.separator+".baggle"+File.separator+"baggle-server_config.myml";
        }
        
        SimpleLog.logger.info("Loading server configuration");
        ServerConfiguration server_config=null;
        
        //use config file from args
        if(Args.getOption(configFlags, args)){
            CONFIG_FILE=Args.getStringOption(configFlags, args, CONFIG_FILE);
            if(CONFIG_FILE.length()>0){
                server_config = ServerConfiguration.loadFromFile(CONFIG_FILE);
            }else{
                SimpleLog.logger.error("You must specify a config file after -c option");
            }
        }else{ //or use default config file
            CONFIG_FILE=DEFAULT_CONFIG_FILE;
            if(!new File(CONFIG_FILE).exists()){
                server_config = ServerConfiguration.createDefault(CONFIG_FILE);
                SimpleLog.logger.info("Default config file has been created in "+CONFIG_FILE+". You can edit it and restart b@ggle server.");
                SimpleLog.logger.info("Un fichier de configuration par défaut a été créé dans "+CONFIG_FILE+". Editez le (ou pas) et "+
                            "redémarrez b@ggle server pour prendre en compte les modifications.");              
            }else{
                server_config = ServerConfiguration.loadFromFile(CONFIG_FILE);
            }
        }
        
        if(server_config==null){
            SimpleLog.logger.error("Impossible to load server configuration from "+CONFIG_FILE);
            return;
        }
        
        //log in a room-named file
        String log_file = "log/"+server_config.get("room.name").replaceAll(" ","-")+".log";
        SimpleLog.logger.info("From now on everything will be logged into "+log_file+", see ya !");
        if(DEV_MODE){
            SimpleLog.initDevConfig();
        }else{
            SimpleLog.initProdConfig(log_file);
        }
        
        
        //start server, with or without gui assistant
        if(WITH_GUI){
            mainFrame = new MainFrame(server_config);
            mainFrame.setVisible(true);
        }else{
            server = new BaggleServer(server_config);
            server.startServer();
        }
        
    }

    /**
     * Displays basic options for the program
     */
    public static void printHelp(){
        printVersion();
        System.out.println("Usage:"
                        + "\n\t--gui            Use graphical user interface to start the server"
                        + "\n\t-c [config file] Use specified config file instead"
                        + "\n\t                 (by default "+DEFAULT_CONFIG_FILE+" is used)");
    }
    
    /**
     * Displays name and version for the program
     */
    public static void printVersion(){
        System.out.println("B@ggle server version "+Main.VERSION);
    }

}
