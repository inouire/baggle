 /* Copyright 2009-2014 Edouard Garnier de Labareyre
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
import java.util.Arrays;
import java.util.LinkedList;
import inouire.baggle.types.IllegalDatagramException;

public class RESULTDatagram {

    public LinkedList<String> words;

    public Integer score=null;
    public Integer id=null;
    public Integer rank=null;
    
    //RESULT|id=657|score=4|words=une,tel,ces,ses
    public RESULTDatagram(int id,int score,LinkedList<String> words){
        this.id=id;
        this.score=score;
        this.words=words;
    }
    
    //RESULT|id=657|rank=2|score=4|words=une,tel,ces,ses
    public RESULTDatagram(int id,int score,LinkedList<String> words,int rank){
        this.id=id;
        this.score=score;
        this.words=words;
        this.rank=rank;
    }
    
    public RESULTDatagram(String[] args) throws IllegalDatagramException{
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
        if( words==null || score==null || id==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("words")){
                words=parseWords(value);
            }else if(key.equals("score")){
                score=Integer.parseInt(value);
            }else if(key.equals("id")){
                id=Integer.parseInt(value);
            }else if(key.equals("rank")){
                rank=Integer.parseInt(value);
            }
        }
    }

    private static LinkedList<String> parseWords(String a){
        LinkedList<String> W = new LinkedList<String>();
        if(a.equalsIgnoreCase("N/A")){
            return W;
        }else{
            String[] list=a.split(",");
            W.addAll(Arrays.asList(list));
            return W;
        }
    }

    @Override
    public String toString(){
        String W="";
        if(words!=null){
            if(words.size()==0){
                W="N/A";
            }else{
                for(String a : words){
                    W+=a+",";
                }
                if(W.length()>0){
                    W=W.substring(0,W.length()-1);
                }
            }
        }
        if(rank==null){
            return "RESULT|id="+id+"|score="+score+"|words="+W;
        }else{
            return "RESULT|id="+id+"|rank="+rank+"|score="+score+"|words="+W;
        }
    }

}
