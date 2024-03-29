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

public class PINGDatagram {

    public String lang=null;
    public Boolean chat=null;
    public Integer min=null;
    public Boolean pf=null;
    public String mode=null;
    public Integer nb=null;
    public Integer max=null;
    public String name=null;
    public Boolean priv=null;
    public String grid=null;
    public Integer time=null;
    public Integer port=null;
    public String players=null;
    public Boolean big=false;
    public Boolean rewardbigwords=false;
    
    public Integer returnPort=null;
    
    public PINGDatagram(int returnPort){
        this.returnPort=returnPort;
    }
    
    //PING|lang=fr|chat=yes|min=3|pf=no|mode=trad|nb=3|max=5|name=salut a toi|priv=no|grid=BAGRIURGUIGIUEG|time=120|players=bibi,beber,mika|big=no|rewardbigwords=no
    public PINGDatagram(int port,String name,String lang,boolean chat,int min,boolean pf, String mode,int nb,int max,boolean priv,String grid,int time,String players, boolean big, boolean rewardbigwords){
        this.port=port;
        this.lang=lang;
        this.chat=chat;
        this.min=min;
        this.pf=pf;
        this.mode=mode;
        this.nb=nb;
        this.max=max;
        this.name=name;
        this.priv=priv;
        this.grid=grid;
        this.time=time;
        this.players=players;
        this.big=big;
        this.rewardbigwords=rewardbigwords;
    }

    public PINGDatagram(String[] args) throws IllegalDatagramException{
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
        if(returnPort==null){
            if(name==null || nb==null || lang==null || max==null || min==null || pf==null || chat==null || priv==null || grid==null || time==null){
                throw new IllegalDatagramException();
            }
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("lang")){
                lang=value.toLowerCase();
            }else if(key.equals("mode")){
                mode=value.toLowerCase();
            }else if(key.equals("nb")){
                nb=Integer.parseInt(value);
            }else if(key.equals("max")){
                max=Integer.parseInt(value);
            }else if(key.equals("min")){
                min=Integer.parseInt(value);
            }else if(key.equals("chat")){
                if(value.equals("yes")){
                    chat=true;
                }else if(value.equals("no")){
                    chat=false;
                }
            }else if(key.equals("pf")){
                if(value.equals("yes")){
                    pf=true;
                }else if(value.equals("no")){
                    pf=false;
                }
            }else if(key.equals("name")){
                name=Datagram.addAccents(value);
            }else if(key.equals("priv")){
                if(value.equals("yes")){
                    priv=true;
                }else if(value.equals("no")){
                    priv=false;
                }
            }else if(key.equals("grid")){
                grid=value.toUpperCase();
            }else if(key.equals("time")){
                time=Integer.parseInt(value);
            }else if(key.equals("returnport")){
                returnPort=Integer.parseInt(value);
            }else if(key.equals("port")){
                port=Integer.parseInt(value);
            }else if(key.equals("players")){
                players=value;
            }else if(key.equals("big")){
                if(value.equals("yes")){
                    this.big=true;
                }else if(value.equals("no")){
                    this.big=false;
                }
            }else if(key.equals("rewardbigwords")){
                if(value.equals("yes")){
                    this.rewardbigwords=true;
                }else if(value.equals("no")){
                    this.rewardbigwords=false;
                }
            }
        }
    }

    @Override
    public String toString(){
        if(returnPort!=null){
            return "PING|returnPort="+returnPort;
        }else{
            String p,c,pr,pl,b,rbw;
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
            if(priv){
                pr="yes";
            }else{
                pr="no";
            }
            if(players!=null && players.length()>0){
                pl = "|players="+players;
            }else{
                pl="";
            }
            if(big){
                b="yes";
            }else{
                b="no";
            }
            if(rewardbigwords){
                rbw="yes";
            }else{
                rbw="no";
            }
            return "PING|port="+port+"|lang="+lang+"|chat="+c+"|min="+min+"|pf="+p+"|mode="+mode+"|nb="+nb+
                    "|max="+max+"|name="+Datagram.replaceAccents(name)+"|priv="+pr+"|grid="+grid+"|time="+time+pl+
                    "|big="+"|rewardbigwords="+rbw;
        }
    }

}
