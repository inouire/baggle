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

public class CLIENTDatagram {

    public String version=null;
    public String os=null;
    public Integer id=null;

    //CLIENT|version=2.0 web|os=GNU/Linux (client -> server)
    public CLIENTDatagram(String version,String os){
        this.version=version;
        this.os=os;
        this.id=null;
    }

    //CLIENT|id=564|version=2.0 web|os=GNU/Linux  (server -> client)
    public CLIENTDatagram(int id,String version,String os){
        this.version=version;
        this.os=os;
        this.id=id;
    }

    public CLIENTDatagram(String[] args) throws IllegalDatagramException{
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
        if( version==null || os ==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("version")){
                version=value;
            }else if(key.equals("os")){
                os=value;
            }else if(key.equals("id")){
                id=Integer.parseInt(value);
            }
        }
    }

    @Override
    public String toString(){
        if(id==null){
            return "CLIENT|version="+version+"|os="+os;
        }else{
            return "CLIENT|id="+id+"|version="+version+"|os="+os;
        }
    }
}
