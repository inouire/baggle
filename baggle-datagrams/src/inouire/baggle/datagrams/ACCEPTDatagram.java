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

public class ACCEPTDatagram {

    public String auth=null;
    public Integer id=null;
    public String lang=null;
    public Boolean chat=null;
    public Integer min=null;
    public Boolean pf=null;
    public String mode=null;
    public Integer time=null;

    //ACCEPT|id=654|auth=45RT76TR|lang=fr|chat=yes|min=3|pf=no|mode=trad|time=120
    public ACCEPTDatagram(String auth,int id,String lang,boolean chat,int min,boolean pf, String mode,Integer time){
        this.auth=auth;
        this.id=id;
        this.lang=lang;
        this.chat=chat;
        this.min=min;
        this.pf=pf;
        this.mode=mode;
        this.time=time;
    }

    public ACCEPTDatagram(String[] args) throws IllegalDatagramException{
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
        if( auth==null || lang==null || id==null || min==null || pf==null || chat==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){
            
            String key=keyValue[0];
            String value=keyValue[1];
            
            if(key.equals("auth")){
                this.auth=value;
            }else if(key.equals("lang")){
                this.lang=value.toLowerCase();
            }else if(key.equals("mode")){
                this.mode=value.toLowerCase();
            }else if(key.equals("id")){
                this.id=Integer.parseInt(value);
            }else if(key.equals("min")){
                this.min=Integer.parseInt(value);
            }else if(key.equals("chat")){
                if(value.equals("yes")){
                    this.chat=true;
                }else if(value.equals("no")){
                    this.chat=false;
                }
            }else if(key.equals("pf")){
                if(value.equals("yes")){
                    this.pf=true;
                }else if(value.equals("no")){
                    this.pf=false;
                }
            }else if(key.equals("time")){
                this.time=Integer.parseInt(value);
            }
        }
    }
  
    @Override
    public String toString(){
        String p,c;
        if(pf){
            p="yes";
        }else{
            p="no";
        }
        if(chat){
            c="yes";
        }else{
            c="no";
        }
        return "ACCEPT|id="+id+"|auth="+auth+"|lang="+lang+"|chat="+c+"|min="+min+"|pf="+p+"|mode="+mode+"|time="+time;
    }

}
