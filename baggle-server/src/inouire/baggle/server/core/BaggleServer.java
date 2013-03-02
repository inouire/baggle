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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Timer;
import inouire.baggle.server.bean.ServerConfigXML;
import inouire.basics.SimpleLog;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author Edouard de Labareyre
 */
public class BaggleServer {

    public ServerConfigXML configuration;
    
    public NioServer nioServer;
    public MainWorker nioWorker;
    
    public LANWatchmanThread lanWatchmanThread;
    public GameThread gameThread;
        
    
    public BaggleServer(ServerConfigXML configuration){
        
        //assign configuration for this server
        this.configuration = configuration;
               
        //ajust log level
        SimpleLog.logger.setLevel(Level.toLevel(configuration.getLogLevel(),Level.INFO));
    }
    
    public void addLog4jAppender(OutputStream out){
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(out));
        ca.setLayout(new PatternLayout("%d - %-5p - %m%n"));
        SimpleLog.logger.setLevel(Level.toLevel(configuration.getLogLevel(),Level.INFO));
        SimpleLog.logger.addAppender(ca);
    }
    
    public void setConfiguration(ServerConfigXML configuration){
        this.configuration = configuration;
    }
    
    public boolean startServer(){
        
        //recap the option that will be used for this server instance
        configuration.printRecap();    
        
        //game thread creation
        gameThread = new GameThread();

        //client list initialisation
        gameThread.players = new PlayerList(configuration.getMaxPlayers());

        //start the main worker part
        nioWorker = new MainWorker(configuration,gameThread.players);
        new Thread(nioWorker).start();
        
        //start the listenning part of the server
        nioServer = new NioServer(null, configuration,nioWorker); 
        new Thread(nioServer).start();

        //start the periodic tasks thread
        Timer T = new Timer();
        T.schedule(new FrequentTask(), 0, 10000);//10 sec
        T.schedule(new OccasionalTask(), 0, 300000);//5 min 

        //start the game thread
        gameThread.start();
        
        //listen on lan if needed
        if(configuration.isListenOnLan()){
            lanWatchmanThread = new LANWatchmanThread();
            lanWatchmanThread.start();
        }
        
        return true;
    }
    
    public boolean stopServer(){
        //TODO
        SimpleLog.logger.info("Stopping server (not implemented yet)");
        return true;
    }
    
}
