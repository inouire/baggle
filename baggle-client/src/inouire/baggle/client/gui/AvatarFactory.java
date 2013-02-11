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

package inouire.baggle.client.gui;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author Edouard de Labareyre
 */
public class AvatarFactory {
    
    private HashMap<String,Icon> avatar_map;
    
    private String[] player_list;
    private String[] system_list;
        
    public AvatarFactory(){
        avatar_map = new HashMap<String,Icon>();
             
        system_list=new String[]{
            "server",
            "mobile",
            "unknown",
            "robot"
        };
        player_list =new String[]{
            "colors",
            "donkey",
            "ladybug",
            "hat",
            "tiger",
            "wine",
            "coffee",
            "tux",
        };
                
        for(String avatar : system_list){
            addRessource(avatar);
        }
        
        for(String avatar : player_list){
            addRessource(avatar);
        }

    }
    
    private void addRessource(String name){
        avatar_map.put(name,new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/avatars/"+name+".png")));
        avatar_map.put(name+"_small",new ImageIcon(getClass().getResource("/inouire/baggle/client/icons/avatars/"+name+"_small.png")));
    }
    
    /**
     * Get a list of icons for the avatars of the players
     * A bit heavy but is called only once on startup
     * @return the list of available icons for players
     */
    public Icon[] getPlayerAvatarList(){
        int nb_avatars = player_list.length;
        Icon[] avatar_list = new Icon[player_list.length];
        for(int k=0 ; k<nb_avatars ; k++){
            avatar_list[k]=avatar_map.get(player_list[k]);
        }
        return avatar_list;
    }
   
    public String getAvatarByIdInAvatarList(int id){
        return player_list[id];
    }
    
    public int getAvatarIdByName(String name){
        for(int k=0;k<player_list.length;k++){
            if(player_list[k].equals(name)){
                return k;
            }
        }
        return 0;
    }
    


    public Icon getAvatar(String name){
        Icon avatar = avatar_map.get(name.toLowerCase());
        if(avatar==null){
            avatar=avatar_map.get("unknown");
        }
        return avatar;
    }
    
    public Icon getSmallAvatar(String name){
        Icon avatar = avatar_map.get(name.toLowerCase()+"_small");
        if(avatar==null){
            avatar=avatar_map.get("unknown_small");
        }
        return avatar;
    } 

}
