package inouire.baggle.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import inouire.baggle.datagrams.Datagram;
import inouire.baggle.types.IllegalDatagramException;

/**
 *
 * @author edouard
 */
public class ServerEntry {
    
    public String host=null;
    public Integer port=null;
    public Long timestamp=null;

    //host=192.168.0.1|timestamp=7657654
    public ServerEntry(String host){
        this.host=host;
        this.timestamp=System.currentTimeMillis();
    }

    //host=192.168.0.1|port=12345|timestamp=7657654
    public ServerEntry(String host,int port){
        this.host=host;
        this.port=port;
        this.timestamp=System.currentTimeMillis();
    }

    public ServerEntry(String[] content) throws IllegalDatagramException{
        try{
            for(int k=0;k<content.length;k++){
                parseArg(content[k]);
            }
        }catch(Exception e){
            throw new IllegalDatagramException();
        }
        checkDatagram();
    }

    public String getHost(){
        return host;
    }
    public int getPort(){
        return port;
    }
    public SocketAddress getSocketAddress(){
        if(port==null){
            return null;
        }else{
            return new InetSocketAddress(host,port);
        } 
    }
    
    private void checkDatagram() throws IllegalDatagramException {
        if(host==null || timestamp==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("host")){
                host=value;
            }else if(key.equals("port")){
                port=Integer.parseInt(value);
            }else if(key.equals("timestamp")){
                timestamp=Long.parseLong(value);
            }
        }
    }
    
    @Override
    public String toString(){
        if(port==null){
            return "host="+host+"|timestamp="+timestamp;
        }else{
            return "host="+host+"|port="+port+"|timestamp="+timestamp;
        }
    }

}
