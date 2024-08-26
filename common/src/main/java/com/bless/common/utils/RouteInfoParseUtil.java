package com.bless.common.utils;

import com.bless.common.BaseErrorCode;
import com.bless.common.exception.ApplicationException;
import com.bless.common.route.RouteInfo;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-26 12:59
 */

public class RouteInfoParseUtil {
    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }
}
