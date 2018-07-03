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
import inouire.baggle.types.Status;

public class STATUSDatagram {
    
    public Status state=null;
    public Integer id=null;

    //STATUS|state=ready (client -> server)
    public STATUSDatagram(Status state){
        this.state=state;
        this.id=null;
    }

    //STATE|id=player|state=ready (server -> client)
    public STATUSDatagram(int id,Status state){
        this.id=id;
        this.state=state;
    }

    public STATUSDatagram(String[] args) throws IllegalDatagramException{
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
        if( state==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("state")){
                state=Status.valueOf(value.toUpperCase());
            }else if(key.equals("id")){
                id=Integer.parseInt(value);
            }
        }
    }
    
    @Override
    public String toString(){
        if(id==null){
            return "STATUS|state="+state;
        }else{
            return "STATUS|id="+id+"|state="+state;
        }
    }
}
