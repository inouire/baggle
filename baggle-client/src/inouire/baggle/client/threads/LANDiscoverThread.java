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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import inouire.baggle.client.gui.modules.LANDiscoverPanel;
import inouire.baggle.datagrams.PINGDatagram;
import inouire.baggle.types.IllegalDatagramException;
import inouire.basics.SimpleLog;

/**
 *
 * @author inouire
 */
public class LANDiscoverThread extends Thread{
    
    protected DatagramSocket socket =null;
    
    public Integer lanListeningPort=42710;
    public Integer returnPort = 42711;
        
    private LANDiscoverPanel listener;
    
    public LANDiscoverThread(LANDiscoverPanel listener) {
        this.setName("lanDiscoverThread");
        this.listener=listener;
        do{
            try {
                socket= new DatagramSocket(returnPort);
            } catch (SocketException se) {
                SimpleLog.logger.debug("Impossible to create socket on port "+returnPort);
                //TODO améliorer ça, trop bourrin
                returnPort++;
            }
        }while(socket==null);
        
    }

    @Override
    public void run(){
        
        SimpleLog.logger.info("Starting LAN discovey");
        
        int refreshId = listener.newRefreshId();
        
        try {
            // build broadcast message
            String dString="PING|returnPort="+returnPort;
            byte[] bufS = dString.getBytes();

            //broadcast this message
            InetAddress group = InetAddress.getByName("230.0.0.1");
            DatagramPacket packetS = new DatagramPacket(bufS, bufS.length, group, lanListeningPort);
            socket.send(packetS);

            //receive answers
            byte[] bufR = new byte[512];
            DatagramPacket packetR;
            String received;
            while(true){
                packetR = new DatagramPacket(bufR, bufR.length);
                socket.receive(packetR);
                InetAddress host = packetR.getAddress();
                received = new String(packetR.getData(), 0, packetR.getLength());
                SimpleLog.logger.info("[<LANRCV] "+received);
                try {
                    PINGDatagram pingD = new PINGDatagram(received.split("\\|"));
                    if(pingD.port==null){
                        throw new IllegalDatagramException();
                    }
                    listener.addValidServer(refreshId, host.getHostName(),pingD.port,pingD);
                } catch (IllegalDatagramException ex) {
                    SimpleLog.logger.warn("error while parsing "+received);
                }
                        
            }
        } catch (IOException e) {
            SimpleLog.logger.error("error during lan discover",e);
        }
	socket.close();
        
    }
        
}
