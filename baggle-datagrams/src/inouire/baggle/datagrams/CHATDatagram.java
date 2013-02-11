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

package inouire.baggle.datagrams;import inouire.baggle.types.IllegalDatagramException;
import inouire.baggle.types.Key;

public class CHATDatagram{
        
    public String msg=null;
    public Integer id=null;

    //CHAT|msg=Bonjour tout le monde! (client -> server)
    public CHATDatagram(String msg){
        this.msg=msg;
        this.id=null;
    }

    //CHAT|id=657|msg=Bonjour Edouard, comment vas tu ?  (server -> client)
    public CHATDatagram(int id,String msg){
        this.msg=msg;
        this.id=id;
    }

    public CHATDatagram(String[] args) throws IllegalDatagramException{
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
        if(msg==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("msg")){
                msg=Datagram.addAccents(value);
            }else if(key.equals("id")){
                id=Integer.parseInt(value);
            }
        }
    }
    
    @Override
    public String toString(){
        if(id==null){
            return Key.CHAT+"|msg="+msg;
        }else{
            return Key.CHAT+"|id="+id+"|msg="+Datagram.replaceAccents(msg);
        }
    }

}