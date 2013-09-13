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

/**
 * A set of color used in all the project...
 */
public class ColorFactory {
    final static public Color BROWN_BOARD= new Color(74,50,19);
    final static public Color BLUE_BOARD = new Color(28,47,85);
    final static public Color GREEN_BOARD = new Color(23,77,19);
    final static public Color RED_BOARD = new Color(113,38,35);
    final static public Color PURPLE_BOARD = new Color(58,42,69);
    
    final static public Color LIGHT_BLUE = new Color(164,178,207);
    final static public Color LIGHT_RED = new Color(255,113,113);
    final static public Color LIGHT_GREEN = new Color(157,195,104);
    
    final static public Color VERY_LIGHT_GRAY = new Color(220,220,220);
    final static public Color LIGHT_GRAY = new Color(190,190,190);
    
    final static public Color YELLOW_NOTIF = new Color(255,250,151);

    final static public Color SERVER_BG=Color.LIGHT_GRAY;
    final static public Color MOBILE_BG=Color.LIGHT_GRAY;
    final static public Color UNKNOWN_BG=Color.LIGHT_GRAY;
    final static public Color ROBOT_BG=new Color(255,250,151);
    
    final static public Color DONKEY_BG=new Color(159,196,123);
    final static public Color LADYBUG_BG=new Color(255,215,247);
    final static public Color HAT_BG=new Color(127,141,157);
    final static public Color TIGER_BG=new Color(211,214,255);
    final static public Color TUX_BG=new Color(255,219,151);
    final static public Color WINE_BG=new Color(255,155,155);
    final static public Color COFFEE_BG =new Color(199,192,175);
    final static public Color COLORS_BG=new Color(210,214,234);
    
    final static public Color GOOD_WORD = new Color(70,122,15);
    final static public Color BAD_WORD = new Color(187,41,41);
    
//        new Color(246,205,147),
//        new Color(191,191,191),
//        new Color(219,180,121),
//        new Color(175,180,132),
//        new Color(220,220,220),

        
    public static Color getAvatarColor(String name){
        Color c=Color.LIGHT_GRAY;
        if(name.equals("robot")){
            c=ROBOT_BG;
        }else if(name.equals("donkey")){
            c=DONKEY_BG;
        }else if(name.equals("ladybug")){
            c=LADYBUG_BG;
        }else if(name.equals("hat")){
            c=HAT_BG;
        }else if(name.equals("tiger")){
            c=TIGER_BG;
        }else if(name.equals("tux")){
            c=TUX_BG;
        }else if(name.equals("wine")){
            c=WINE_BG;
        }else if(name.equals("coffee")){
            c=COFFEE_BG;
        }else if(name.equals("colors")){
            c=COLORS_BG;
        }
        return c;
    }
    
    public static Color getBoardColor(boolean big_board,String game_mode){
        Color c;
        if(big_board){
            if(game_mode.equalsIgnoreCase("trad")){
                c=ColorFactory.PURPLE_BOARD;
            }else{
                c=ColorFactory.BROWN_BOARD;
            }
        }else{
            if(game_mode.equalsIgnoreCase("all")){
                c=ColorFactory.BLUE_BOARD;
            }else{
                c=ColorFactory.GREEN_BOARD;
            }
        }
        return c;
    }

}
