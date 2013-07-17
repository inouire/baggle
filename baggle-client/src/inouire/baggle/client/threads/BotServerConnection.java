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

package inouire.baggle.client.threads;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import inouire.baggle.client.Language;
import inouire.baggle.client.Main;
import inouire.baggle.datagrams.*;
import inouire.baggle.solver.Solver;
import inouire.baggle.types.Key;
import inouire.baggle.types.Status;
import inouire.basics.SimpleLog;

/**
 * Thread that handles the bot connection
 * @author Edouard de Labareyre
 */
public class BotServerConnection extends Thread{

    private Solver solver;

    private String room_address;
    private int room_port;
    private String room_password;

    private int bot_id;
    private int bot_level;
    
    private Status bot_state=Status.IDLE;
    
    private Socket socket;
    private PrintWriter out = null;
    private BufferedReader in = null;

    ArrayList<String> solutions;
    
    public boolean is_playing=false;

    static Random r = new Random();

    String grid_tmp;
    
    public BotServerConnection(int level, String server, int port,String password){
        this.bot_level=level;
        this.room_address = server;
        this.room_port = port;
        this.room_password=password;
    }

    /**
     * Send a message to the server
     * @param message the message to send
     */
    public void send(String message){
        if(out != null){
            out.println(message);
        }
    }

    /**
     * Properly close all the connection with the server
     */
    public void closeAllConnections(){
        String bye_message = Datagram.replaceAccents(Language.getString(72));
        send(new CHATDatagram(bye_message).toString());
        send(new DISCONNECTDatagram().toString());
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException ex) {}
    }

    @Override
    public void run(){
        socket = null;
        try {
            socket = new Socket(room_address, room_port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception e) {
            SimpleLog.logger.error("Error while connecting to the server");
            return;
        }

        if(!performNegociationPhase()){
            //do something ?
            return;
        }

         //the bot is now connected, tell the server that we are a bot
        send(new CLIENTDatagram(Main.VERSION,"bot "+Main.mainFrame.roomPane.getBotLevel()).toString());
        String welcome_message=Datagram.replaceAccents(Language.getString(71));
        send(new CHATDatagram(welcome_message).toString());

        //game phase

        String packet;
        String[] datagram;
        Key key=null;

        try{
            while ((packet = in.readLine()) != null){

                datagram = packet.split("\\|");

                try{
                    key=Key.valueOf(datagram[0]);
                    switch (key){
                        case STATUS:
                            STATUSDatagram statusD = new STATUSDatagram(datagram);
                            if(statusD.id != bot_id){
                                switch(statusD.state){
                                    case IDLE:
                                        if(bot_state == Status.RESET){
                                            sendWithDelay(new STATUSDatagram(Status.IDLE).toString(),1000);
                                        }
                                        break;
                                    case READY:
                                        if(bot_state != Status.READY){
                                            sendWithDelay(new STATUSDatagram(Status.READY).toString(),1000);
                                        }
                                        break;
                                    case RESET:
                                        if(bot_state != Status.RESET){
                                            sendWithDelay(new STATUSDatagram(Status.RESET).toString(),1000);
                                        }
                                        break;
                                }
                            }else{
                                bot_state=statusD.state;
                            }
                            break;
                        case START:
                            STARTDatagram startD = new STARTDatagram(datagram);
                            solver=new Solver(Main.connection.LANG,Main.connection.PARENTAL_FILTER,Main.connection.BIG_BOARD);
                            solver.setMinLength(Main.connection.MIN_LENGTH);
                            solutions = solver.solveGrid(startD.grid);
                            is_playing=true;
                            gameStart_hook();
                            break;
                        case STOP:
                            is_playing=false;
                            break;
                        default:
                            //Log.printLogIfVerbose("Unexpected message: "+packet);
                            break;
                    }
                }catch(Exception e){
                    SimpleLog.logger.debug("Illegal datagram received from server: "+packet);
                }
            }
        }catch(IOException e){
        }

    }

    boolean performNegociationPhase(){
        //negociation phase
        //connect to the server
        int level = this.bot_level+1;
        String bot_name=Datagram.replaceAccents(Main.connection.my_nick)+" robot "+level;
        send(new CONNECTDatagram(bot_name,"robot").toString());
        //TODO: cas où il y a un password

        String packet;
        String[] datagram;
        Key key=null;

        try{
            while ((packet = in.readLine()) != null){

                datagram = packet.split("\\|");

                try{
                    key=Key.valueOf(datagram[0]);
                    switch (key){
                        case ACCEPT:
                            ACCEPTDatagram acceptD = new ACCEPTDatagram(datagram);
                            bot_id=acceptD.id;
                            return true;
                        case DENY:
                            DENYDatagram statusD = new DENYDatagram(datagram);
                            return false;
                        case PASSWORD:
                            //TODO
                            break;
                        default:
                            SimpleLog.logger.debug("Unexpected message: "+packet);
                            break;
                    }
                }catch(Exception e){
                    SimpleLog.logger.debug("Illegal datagram received from server: "+packet);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    private void gameStart_hook(){

        Thread t=new Thread(){
        @Override
          public void run(){
                int moy=7000;
                int div=4;
                int max_nb_letters=7;
                if(solutions==null){
                    return;
                }
                int tot=solutions.size();

                switch(bot_level){
                    case 0:
                        div=5;
                        moy=9000;
                        max_nb_letters=4;
                        break;
                    case 1:
                        div=4;
                        moy=7000;
                        max_nb_letters=5;
                        break;
                    case 2:
                        div=3;
                        moy=5000;
                        max_nb_letters=7;
                        break;
                    case 3:
                        div=2;
                        moy=3000;
                        max_nb_letters=8;
                        break;
                    case 4:
                        div=1;
                        moy=2000;
                        max_nb_letters=9;
                        break;
                }
                int max=tot/div;
                SimpleLog.logger.info("Robot level "+bot_level+": "+tot+" -> "+max+ " (/"+div+")");

                String w;
                for(int i=0;i<max;i++){
                    randomWait(moy);
                    if(!is_playing){
                        return;
                    }
                    try{
                        w=solutions.get(BotServerConnection.r.nextInt(tot));
                        if(w.length()<=max_nb_letters){
                            send(new WORDDatagram(solutions.get(BotServerConnection.r.nextInt(tot))).toString());
                        }
                    }catch(Exception e){}
                 }
                }
        };
        t.start();
    }

    /**
     * Wait for a random time around the specified value
     * @param moy the average waiting time
     */
    public void randomWait(int moy){
        int delay = moy/2 + BotServerConnection.r.nextInt(moy);
        try {
            sleep(delay);
        } catch (InterruptedException ex) {
        }
        return;
    }

    /**
     * Send a message with a specified delay
     * @param message the message to send
     * @param delay the time to wait before sending
     */
    public void sendWithDelay(final String message,final int delay){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(delay);
                } catch (InterruptedException ex) {

                }
                send(message);
                return;
            }
        };
        t.start();
    }
}
