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
        
        //prepare HMI
        listener.setStartOfPingProcess();
        
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
                SimpleLog.logger.debug("[<LANRCV] "+received);
                try {
                    PINGDatagram pingD = new PINGDatagram(received.split("\\|"));
                    if(pingD.port==null){
                        throw new IllegalDatagramException();
                    }
                    listener.addValidServer(refreshId, host.getHostAddress(),pingD.port,pingD);
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
