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

package inouire.baggle.datagrams;
import inouire.baggle.types.IllegalDatagramException;

public class SERVERDatagram {

    public String ip=null;
    public Integer port=null;

    //SERVER|ip=92.168.0.4|port=12345 (master -> client)
    public SERVERDatagram(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    //SERVER|port=12345 (server -> master)
    public SERVERDatagram(Integer port) {
        this.ip = null;
        this.port = port;
    }

    public SERVERDatagram(String[] args) throws IllegalDatagramException{
        try{
            for(int k=1;k<args.length;k++){
                parseArg(args[k]);
            }
        }catch(Exception e){
            throw new IllegalDatagramException();
        }
        checkDatagram();
    }

    private void checkDatagram() throws IllegalDatagramException {
        if(port==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("ip")){
                ip=value;
            }else if(key.equals("port")){
                port=Integer.parseInt(value);
            }
        }
    }

    @Override
    public String toString(){
        if(ip==null){
            return "SERVER|port="+port;
        }else{
            return "SERVER|ip="+ip+"|port="+port;
        }
    }
}

