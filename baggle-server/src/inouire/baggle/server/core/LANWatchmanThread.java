 /* Copyright 2009-2018 Edouard Garnier de Labareyre
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

import java.io.IOException;
import java.net.*;
import inouire.baggle.datagrams.PINGDatagram;
import inouire.baggle.server.Main;
import inouire.baggle.types.IllegalDatagramException;
import inouire.basics.SimpleLog;

/**
 *
 * @author edouard
 */
public class LANWatchmanThread extends Thread{
    
    private MulticastSocket socket;
    private InetAddress address;
    
    public LANWatchmanThread(){
        this.setName("lanWatchmanThread");
        int port=Main.server.configuration.lanListenningPort;
        try{
            socket = new MulticastSocket(port);
            address = InetAddress.getByName("230.0.0.1");
            socket.joinGroup(address);
        }catch(UnknownHostException uhe){
            SimpleLog.logger.error("Impossible to listen for lan watching on port "+port,uhe);
        }catch(IOException ioe){
            SimpleLog.logger.error("Impossible to listen for lan watching on port "+port,ioe);
        }
    }
    
    @Override
    public void run(){
        
        SimpleLog.logger.info("Listening for local broadcast on port "+Main.server.configuration.lanListenningPort);
        
        DatagramPacket packet;
        PINGDatagram pingD;
        
        try{
            while(true){
                byte[] buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                SimpleLog.logger.info("[<LANRCV] "+received);
                try{
                    
                    //parse the content of the message
                    pingD =new PINGDatagram(received.split("\\|"));
                    if(pingD.returnPort==null){
                        throw new IllegalDatagramException();
                    }
                    
                    //send PING response  at specified port to the same address
                    InetAddress answerHost = packet.getAddress();
                    answerLANAsker(answerHost,pingD.returnPort);
                    
                }catch(IllegalDatagramException ide){
                    SimpleLog.logger.warn("Invalid PING broadcast message: "+received,ide);
                }
            }
        }catch(Exception e){
            SimpleLog.logger.error("Error while watching lan, abort LAN watching",e);
        }finally{
            try{
                socket.leaveGroup(address);
            }catch(IOException ioe){}
            socket.close();
        }

    }
    
    public void answerLANAsker(InetAddress host,int port){ 
        try {
            //prepare answer
            String answerAsString = Main.server.nioWorker.getAnswerToPING();
            byte[] answer =  answerAsString.getBytes();

            // send the answer to the client at "address" and "port"
            DatagramPacket packet = new DatagramPacket(answer, answer.length, host, port);
            socket.send(packet);
            SimpleLog.logger.trace("[LANSND>] "+answerAsString);
            SimpleLog.logger.info("Answering to LAN asker with a PING");
        } catch (UnknownHostException e) {
            SimpleLog.logger.warn("Impossible to anwser to asker, unknow host "+host);
        } catch(ConnectException ce){
            SimpleLog.logger.warn("Asker refused connection for answer");
        } catch (IOException e) {
            SimpleLog.logger.warn("IO error when trying to answer LAN asker",e);
        } catch (IllegalArgumentException e) {
            SimpleLog.logger.warn("Error when trying to answer LAN asker",e);
        }
    }
}
