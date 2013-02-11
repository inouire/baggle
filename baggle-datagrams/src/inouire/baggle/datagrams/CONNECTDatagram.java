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

package inouire.baggle.datagrams;
import inouire.baggle.types.IllegalDatagramException;

public class CONNECTDatagram {

    public String nick=null;
    public String logo=null;
    public String pwd=null;

    //CONNECT|nick=Edouard|logo=tux
    public CONNECTDatagram(String nick,String logo){
        this.nick=nick;
        this.logo=logo;
    }
    
    //CONNECT|nick=Edouard|logo=tux|pwd=hehehe
    public CONNECTDatagram(String nick,String logo,String pwd){
        this.nick=nick;
        this.logo=logo;
        this.pwd=pwd;
    }
    
    public CONNECTDatagram(String[] args) throws IllegalDatagramException{
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
        if( nick==null || logo==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("nick")){
                nick=Datagram.addAccents(value);
            }else if(key.equals("logo")){
                logo=value.toLowerCase().trim();
            }else if(key.equals("pwd")){
                pwd=value.trim();
            }
        }
    }
    
    @Override
    public String toString(){
        String s = "CONNECT|nick="+nick+"|logo="+logo;
        if(pwd!=null){
            s+="|pwd="+pwd;
        }
        return s;
    }

}
