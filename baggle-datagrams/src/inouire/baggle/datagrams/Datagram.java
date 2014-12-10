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

public abstract class Datagram {
   
    public static String[] decompArg(String arg){
        String[] decomp = arg.split("=");
        if(decomp.length!=2){
            return null;
        }else{
            decomp[0]=decomp[0].trim().toLowerCase();
            decomp[1]=decomp[1].trim();
        }
        return decomp;
    }
    

    public static String replaceAccents(String s){
        String t=s;
        t=t.replaceAll("é", "&eacute;");
        t=t.replaceAll("è", "&eagrave;");
        t=t.replaceAll("ê", "&ecirc;");
        t=t.replaceAll("ë", "&euml;");

        t=t.replaceAll("î", "&icirc;");
        t=t.replaceAll("ï", "&iuml;");

        t=t.replaceAll("à", "&agrave;");
        t=t.replaceAll("â", "&acirc;");

        t=t.replaceAll("ô","&ocirc");

        t=t.replaceAll("û","&ucirc;");
        t=t.replaceAll("ù","&ugrave;");
        t=t.replaceAll("ü","&uuml;");

        t=t.replaceAll("ç","&ccedil;");
        return t;
    }

     public static String addAccents(String s){
        String t=s;
        t=t.replaceAll("&eacute;","é");
        t=t.replaceAll("&eagrave;","è");
        t=t.replaceAll("&ecirc;","ê");
        t=t.replaceAll("&euml;","ë");

        t=t.replaceAll("&icirc;","î");
        t=t.replaceAll("&iuml;","ï");

        t=t.replaceAll("&agrave;","à");
        t=t.replaceAll("&acirc;","â");

        t=t.replaceAll("&ocirc","ô");

        t=t.replaceAll("&ucirc;","û");
        t=t.replaceAll("&ugrave;","ù");
        t=t.replaceAll("&uuml;","ü");

        t=t.replaceAll("&ccedil;","ç");
        return t;
    }

    public static String removeAccents(String s){
        s=s.replaceAll("é", "e");
        s=s.replaceAll("è", "e");
        s=s.replaceAll("ê", "e");
        s=s.replaceAll("ë", "e");
        s=s.replaceAll("à", "a");
        s=s.replaceAll("â", "a");
        s=s.replaceAll("ä", "a");
        s=s.replaceAll("î", "i");
        s=s.replaceAll("ï", "i");
        s=s.replaceAll("ô", "o");
        s=s.replaceAll("ö", "o");
        s=s.replaceAll("û", "u");
        s=s.replaceAll("ù", "u");
        s=s.replaceAll("ü","u");
        s=s.replaceAll("ç", "c");
        return s;
    }
    
    public static boolean isChatMessage(String s) {
        s=s.trim();
        if(s.length()<3){
            if(s.equalsIgnoreCase("ok")){
                return true;
            }else{
                return false;
            }
        }else if(s.length() >= 16){
            return true;
        }
        for(char c : s.toUpperCase().toCharArray()){
            if(c<'A'||c>'Z'){
                return true;
            }
        }
        return false;
    }

}
