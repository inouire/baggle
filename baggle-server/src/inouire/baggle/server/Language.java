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

package inouire.baggle.server;

/**
 *
 * @author Edouard de Labareyre
 */
public class Language {

    static String[] fr = new String[] {
        " et "," remportent "," remporte ","la manche avec "," points.",//0
        " a quitt&eacute; le salon."," a rejoint le salon.",//5
        "Le chat est désactiv&eacute; pour ce salon.",//7
        "Bravo, tout les mots de plus de 7 lettres ou plus ont &eacute;t&eacute; trouv&eacute;s !",//8
        "Il y avait aussi",//9
    };

    static String[] en = new String[] {
        " and "," win "," wins ","the game with "," points.",//0
        " has left the room."," has join the room.",//5
        "Chat has been disabled for this room.",//7
        "Congrats, all the 7+ letters words have been found !",//8
        "Other words",//9
    };

    public static String getString(int id){
        String a="###";
        try{
            if(Main.server.configuration.get("room.rules.language").equals("fr")){
                a=fr[id];
            }else if(Main.server.configuration.equals("en")){
                a=en[id];
            }else{
                a=fr[id];
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }finally{
            return a;
        }
        
    }
}
