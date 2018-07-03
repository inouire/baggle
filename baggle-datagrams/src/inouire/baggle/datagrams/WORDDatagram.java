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
import inouire.baggle.types.Words;

/**
 *
 * @author Edouard de Labareyre
 */
public class WORDDatagram {

    public String word=null;
    public Words status=null;

    //WORD|word=pointu
    public WORDDatagram(String word){
        this.word=word;
        this.status=null;
    }

    //WORD|word=pointu|status=GOOD
    public WORDDatagram(String word,Words status){
        this.word=word;
        this.status=status;
    }
    
    public WORDDatagram(String[] args) throws IllegalDatagramException{
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
        if(word==null){
            throw new IllegalDatagramException();
        }
    }

    private void parseArg(String arg){
        String [] keyValue = Datagram.decompArg(arg);
        if(keyValue!=null){

            String key=keyValue[0];
            String value=keyValue[1];

            if(key.equals("word")){
                word=value.toUpperCase();
            }else if(key.equals("status")){
                status=Words.valueOf(value.toUpperCase());
            }
        }
    }
    
    @Override
    public String toString(){
        if(status==null){
            return "WORD|word="+word;
        }else{
            return "WORD|word="+word+"|status="+status;
        }
    }
}
