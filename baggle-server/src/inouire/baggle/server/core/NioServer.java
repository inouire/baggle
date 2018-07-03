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

import inouire.baggle.server.ServerConfiguration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import inouire.baggle.server.bean.ChangeRequest;
import inouire.basics.SimpleLog;


/**
 *
 * @author Edouard de Labareyre
 */
public class NioServer implements Runnable {

    // The host:port combination to listen on
    private final InetAddress hostAddress;

    private final ServerConfiguration configuration;
    
    private final MainWorker worker;
    
    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;

    // The selector we'll be monitoring
    private final Selector selector;

    // The buffer into which we'll read data when it's available
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);
    
    // A list of PendingChange instances
    private final List pendingChanges = new LinkedList();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final Map pendingData = new HashMap();
    
    public NioServer(InetAddress hostAddress, ServerConfiguration configuration,MainWorker worker){
        this.hostAddress = hostAddress;
        this.configuration = configuration;
        this.selector = this.initSelector();
        this.worker = worker;
    }

    private Selector initSelector(){
        
        try{
            // Create a new selector
            Selector socketSelector = SelectorProvider.provider().openSelector();

            // Create a new non-blocking server socket channel
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.configureBlocking(false);
            
            //auto find available port
            int port=configuration.listeningPort;
            boolean success=false;
            do{
                try{
                    // Bind the server socket to the specified address and port
                    InetSocketAddress isa = new InetSocketAddress(this.hostAddress, port);
                    serverChannel.socket().bind(isa);
                    success=true;
                }catch (IOException e) {
                    if(configuration.autoPortIncrement){
                        SimpleLog.logger.warn("Couldn't create listenning socket on port "+port);
                        port++;
                        SimpleLog.logger.info("Trying port "+port+" instead");
                    }else{
                        SimpleLog.logger.error("Couldn't create listenning socket on port "+port);
                        SimpleLog.logger.info("Set 'autoPortIncrement' to 'true' if you expect the server to try another port automatically");
                        System.exit(1);
                    }
                }
            }while(!success);
            configuration.listeningPort = port;
            
            // Register the server socket channel, indicating an interest in 
            // accepting new connections
            serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

            return socketSelector;
        
        }catch(Exception e){
            SimpleLog.logger.fatal("Impossible to init NioServer selector, aborting",e);
            System.exit(1);
        }
        return null;
        
    } 
      
    public void send(SocketChannel socket, byte[] data) {
        synchronized (this.pendingChanges) {
            // Indicate we want the interest ops set changed
            this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }
        
    @Override
    public void run() {
        SimpleLog.logger.info("Listenning for incoming connections on port "+configuration.listeningPort);
        
        while (true) {
            
            try {
                
                // Process any pending changes
                synchronized(this.pendingChanges) {
                    Iterator changes = this.pendingChanges.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        if(change.socket.isConnected()){
                            switch(change.type) {
                                case ChangeRequest.CHANGEOPS:
                                    SelectionKey key = change.socket.keyFor(this.selector);
                                    if(key!=null && key.isValid()){
                                        key.interestOps(change.ops);
                                    }else{
                                        SimpleLog.logger.warn("Invalid key "+key.toString());
                                    }
                                    break;
                            }
                        }else{
                            SimpleLog.logger.debug("Socket not connected, closing it");
                            worker.players.turnPlayerToZombie(change.socket);
                            change.socket.close();
                        }
                        
                    }
                    this.pendingChanges.clear();
                }
        
                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        SimpleLog.logger.warn("Invalid key on the iterator "+key.toString());
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        this.accept(key);
                    }else if (key.isReadable()) {
                        this.read(key);
                    }  else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (Exception e) {
                SimpleLog.logger.warn("Exception in main NioServer loop");
                SimpleLog.logger.warn(e.getStackTrace().toString());
            }
        }
    }
    
    private void accept(SelectionKey key) throws IOException {
        SimpleLog.logger.debug("Accept connection");
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    } 
    
    private void read(SelectionKey key) throws IOException {
        
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            SimpleLog.logger.info("Abrut close of connection");

            //it's brutal: we must not delete this player immmediatly
            //just turn it into a zombie
            worker.players.turnPlayerToZombie(socketChannel);
            //worker.players.removePlayer(socketChannel);
            key.channel().close();
            key.cancel();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            SimpleLog.logger.debug("Soft close of connection");
            worker.players.removePlayer(socketChannel);
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
        this.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }
    
    private void write(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            
            List queue = (List) this.pendingData.get(socketChannel);

            try{
                // Write until there's not more data ...
                while (!queue.isEmpty()) {
                    ByteBuffer buf = (ByteBuffer) queue.get(0);
                    socketChannel.write(buf);
                    if (buf.remaining() > 0) {
                        // ... or the socket's buffer fills up
                        break;
                    }
                    queue.remove(0);
                }

                if (queue.isEmpty()) {
                    // We wrote away all data, so we're no longer interested
                    // in writing on this socket. Switch back to waiting for
                    // data.
                    key.interestOps(SelectionKey.OP_READ);
                }
            }catch(IOException ioe){
                SimpleLog.logger.warn("Error while writing to socket",ioe);
                worker.players.turnPlayerToZombie(socketChannel);
                try{socketChannel.close();}catch(IOException e){}
                key.cancel();
            }
        }
    } 
      
    public void finishConnection(SelectionKey key) throws IOException {
        
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Finish the connection. If the connection operation failed
        // this will raise an IOException.
        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            // Cancel the channel's registration with our selector
            System.out.println(e);
            key.cancel();
            return;
        }

        // Register an interest in writing on this channel
        key.interestOps(SelectionKey.OP_WRITE);
    }
    
    public void send(SocketChannel socket, String message){
        if(message != null && !message.isEmpty()){
            SimpleLog.logger.trace("[SND>>] >"+message+"<");
            message+="\n";
            send(socket,message.getBytes());
        }else{
            SimpleLog.logger.warn("Empty message, impossible to send");
        }
    }
}

