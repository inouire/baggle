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


package inouire.baggle.client;

import inouire.baggle.client.gui.AvatarFactory;
import inouire.baggle.client.gui.MainFrame;
import inouire.baggle.client.threads.AutoRefresh;
import inouire.baggle.client.threads.BotServerConnection;
import inouire.baggle.client.threads.ServerConnection;
import inouire.basics.Args;
import inouire.basics.SimpleLog;
import inouire.utils.Utils;
import java.io.File;
import java.util.Timer;
import org.apache.log4j.Level;

/**
 *
 * @author Edouard de Labareyre
 */
public class Main {

    //global variables
    public static String LOCALE=System.getProperty("user.language");
    
    public final static String VERSION = "3.1";
    public final static int BUILD=3100;
    
    public static final String OFFICIAL_WEBSITE="http://baggle.org";
    public static String MASTER_SERVER_HOST="masterserver.baggle.org";
    public static int MASTER_SERVER_PORT=80;
    
    public static String STATIC_SERVER_IP="localhost";
    public static int STATIC_SERVER_PORT=42705;

    public static boolean STORE_CONFIG_LOCALLY=false;
    public static String CONNECT_ONLY_TO=null;
    public static File CONFIG_FOLDER;

    public static MainFrame mainFrame;

    public static ServerConnection connection;
    public static BotServerConnection bot;
    
    public static CachedServersList cachedServers;
    public static CachedServersList cachedLANServers;
    
    public static AvatarFactory avatarFactory;
    public static UserConfiguration configuration;
        
    public static void printUsage(){
        System.out.println("B@ggle client version "+Main.VERSION);
        System.out.println("Usage:      \n\t-M [master server host]"+
                                       "\n\t-P [master server port]"+
                                       "\n\t-l (store config file in current directory)");
        System.out.println("All parameters are optionnal.");
    }
    
    /**
     * Main function for baggle-client.
     * Schedules all the others
     * @param Args the command line Arguments
     */
    public static void main(String[] args) throws InterruptedException {

        Utils.setBestLookAndFeelAvailable();

        printUsage();
        
        SimpleLog.initConsoleConfig();
        SimpleLog.logger.setLevel(Level.INFO);
        
        MASTER_SERVER_HOST=Args.getStringOption("-M",args,MASTER_SERVER_HOST);
        MASTER_SERVER_PORT=Args.getIntegerOption("-P", args, MASTER_SERVER_PORT);
        STORE_CONFIG_LOCALLY=Args.getOption("-l",args);
        CONNECT_ONLY_TO=Args.getStringOption("--only",args,CONNECT_ONLY_TO);
        
        //set path of config folder
        if(Main.STORE_CONFIG_LOCALLY){
            CONFIG_FOLDER=new File(".baggle"+File.separator);
        }else{
            CONFIG_FOLDER=new File(System.getProperty("user.home")+File.separator+".baggle"+File.separator);
        }
        
        //init graphical elements
        avatarFactory=new AvatarFactory();
        
        //init main frame with connect panel only (for faster startup)
        mainFrame = new MainFrame();
        mainFrame.initConnectionPanel();
        mainFrame.setVisible(true);
        
        //load configuration
        configuration = new UserConfiguration();
        configuration.loadFromFile();
        
        //load cached servers list
        cachedServers = new CachedServersList(Main.MASTER_SERVER_HOST);
        cachedServers.loadList(7);
        
        //set auto refresh for server detection
        Timer T = new Timer();
        T.schedule(new AutoRefresh(), 0, 120000);//every 2 minutes
        
        //now init the game part of the main frame
        mainFrame.initGamePanel();
             
        //check for new version in the background
        mainFrame.connectionPane.sideConnectionPane.newVersionPane.checkForNewVersion();
    }
}
