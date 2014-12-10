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
public class SUBMITDatagram {
    //TODO
    //SUBMIT|nick=Edouard|score=67
    public SUBMITDatagram() {
        
    }


//                    if(key.equals("ip")){
//                        ip=value.trim();
//                    }else if(key.equals("port")){
//                        port=Integer.parseInt(value.trim());
//                    }
               

    @Override
    public String toString(){
        return "SUBMIT|";
    }
}

