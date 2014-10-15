/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.utils;

/**
 *
 * @author arikp
 */
public class StringUtils {

    public static boolean isBlank(String value) {
        if ((value == null) || (value.isEmpty()) || (value.trim().isEmpty())) {
            return true;
        }
        
        return false;
    }
}
