package cn.marwin.util;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MyIntegerUtils {

   public static Integer toInteger(String str){
     if(StringUtils.hasLength(str)){
       return Integer.valueOf(str);
    }
    return 0;
   }
}
