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
import inouire.baggle.server.bean.ServerConfigXML;
import inouire.baggle.server.core.BaggleServer;
import inouire.baggle.server.gui.MainFrame;
import inouire.utils.Args;
import org.apache.log4j.Logger;
/**
 *
 * @author Edouard de Labareyre
 */
public class Main {

    public static String VERSION = "2.5";
    public static boolean WITH_GUI = false;
    public static String CONFIG_FILE="";
    
    //core process
    public static BaggleServer server;
    
    //main frame (gui only)
    public static MainFrame mainFrame = null;
    
    public static String defaultConfig="conf/baggle-server_config.xml";
    
    private static Logger logger = Logger.getLogger(Main.class);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //check if the user just want to display the help
        if(args.length>0 && (args[0].startsWith("-h")||args[0].startsWith("--h"))){
            printHelp();
            return;
        }else{
            printVersion();
            System.out.println("Use -help to know more about the provided options");
        }

        WITH_GUI = Args.getOption("gui", args);
        
        if(WITH_GUI){
            logger.info("Starting application with graphical user interface");
            defaultConfig = System.getProperty("user.home")+File.separator+".baggle"+File.separator+"baggle-server_config.xml";
        }else{
            logger.info("Starting application");
            defaultConfig = "conf/baggle-server_config.xml";
        }
        
        //load configuration
        
        ServerConfigXML configuration;
        
        //use config file from args ?
        if(Args.getOption("c", args)){
            CONFIG_FILE=Args.getStringOption("c", args, CONFIG_FILE);
            if(CONFIG_FILE.length()>0){//load from file
                configuration = ServerConfigXML.loadFromFile(CONFIG_FILE);
                if(configuration==null){
                    logger.fatal("Impossible to load config from "+CONFIG_FILE);
                    return;
                }
            }else{
                logger.fatal("You must specify a config after -c option");
                return;
            }
        }else{ //or use default config file
            if(!new File(defaultConfig).exists()){
                configuration=new ServerConfigXML();
                ServerConfigXML.writeToFile(configuration,defaultConfig);
                logger.info("Default config file has been created in "+defaultConfig+". You can edit it and restart b@ggle server.");
                logger.info("Un fichier de configuration par défaut a été créé dans "+defaultConfig+". Editez le (ou pas) et "+
                            "redémarrez b@ggle server pour prendre en compte les modifications.");              
            }else{
                configuration = ServerConfigXML.loadFromFile(defaultConfig);
            }
        }
        
        if(configuration==null){
            logger.fatal("Error while loading configuration at "+defaultConfig);
            return;
        }
        
        //start gui or directly server
        if(WITH_GUI){
            mainFrame = new MainFrame(configuration);
            mainFrame.setVisible(true);
        }else{
            server = new BaggleServer(configuration);
            server.startServer();
        }

    }

    
    public static void printHelp(){
        printVersion();
        System.out.println("Usage:"
                        + "\n\t-gui             Use graphical user interface to start the server"
                        + "\n\t-c [config file] Use specified config file instead"
                        + "\n\t                 (by default baggle-server_default_config.xml is used)");
    }
    
    public static void printVersion(){
        System.out.println("B@ggle server version "+Main.VERSION);
    }

}
