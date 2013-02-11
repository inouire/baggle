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

import java.util.Random;
import javax.swing.UIManager;

/**
 *
 * @author edouard
 */
public class Utils {
    
    
    public static String createRandomPassword(){
        Random r=new Random();
        int nb_letters=r.nextInt(5)+5;
        String password="";
        for(int k=0;k<nb_letters;k++){
            password+=r.nextInt(10)+"";
        }
        return password;
    }
    
    public static int getIntValue(String string_value, int default_value){
        int int_value;
        try{
            int_value=Integer.parseInt(string_value);
        }catch(NumberFormatException nfe){
            int_value=default_value;
            System.err.println("Impossible to cast "+string_value+" to integer");
        }
        return int_value;
    }
    
    public static boolean getBoolValue(String string_value, boolean default_value){
        boolean bool_value;
        try{
            bool_value=Boolean.parseBoolean(string_value);
        }catch(NumberFormatException nfe){
            bool_value=default_value;
            System.err.println("Impossible to cast "+string_value+" to boolean");
        }
        return bool_value;
    }
    
    public static void setBestLookAndFeelAvailable(){
        String system_lf = UIManager.getSystemLookAndFeelClassName().toLowerCase();
        if(system_lf.contains("metal")){
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }catch (Exception e) {}
        }else{
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }catch (Exception e) {}
        }
    }
}
