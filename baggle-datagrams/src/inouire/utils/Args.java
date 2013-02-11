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

package inouire.utils;

import javax.swing.UIManager;

/**
 * TODO use new Args class for this
 * @author edouard
 */
public class Args {

    public static boolean getOption(String flag, String[] args) {
        for(int k = 0 ; k < args.length ; k++){
            if(args[k].equals("-"+flag)){
                return true;
            }
        }
        return false;
    }

    public static int getIntOption(String flag , String[] args, int default_option) throws NumberFormatException{
        for(int k = 0 ; k < args.length-1 ; k++){
            if(args[k].equals("-"+flag)){
                try{
                    return Integer.parseInt(args[k+1]);
                }catch(NumberFormatException nfe){
                    return default_option;
                }
            }
        }
        return default_option;
    }

    public static String getStringOption(String flag , String[] args, String default_option) throws NumberFormatException{
        for(int k = 0 ; k < args.length-1 ; k++){
            if(args[k].equals("-"+flag)){
                return args[k+1];
            }
        }
        return default_option;
    }

}
