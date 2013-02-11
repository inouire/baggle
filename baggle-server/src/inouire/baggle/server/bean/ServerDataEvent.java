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

package inouire.baggle.server.bean;

/**
 *
 * @author Edouard de Labareyre
 */
import java.nio.channels.SocketChannel;
import inouire.baggle.server.core.NioServer;

public class ServerDataEvent {
    
    public NioServer nioServer;
    public SocketChannel socket;
    public byte[] data;
    public String message;
    public String[] datagram;
    
    public Player author;

    public ServerDataEvent(NioServer server, SocketChannel socket, byte[] data) {
        this.nioServer = server;
        this.socket = socket;
        this.data = data;
        this.message = bytes2String(data).trim();
        this.datagram = this.message.split("\\|");
    }
    
    public void setAuthor(Player author){
        this.author = author;
    }
        
            
    /**
     * Convert a byte array into a String
     * @param bytes the byte array to convert
     * @return the converted String
     */
    public static String bytes2String( byte[] bytes ){
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++){
                stringBuffer.append( (char) bytes[i] );
        }
        return stringBuffer.toString();
    }
}
