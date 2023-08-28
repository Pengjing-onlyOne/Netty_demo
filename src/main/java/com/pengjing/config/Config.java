package com.pengjing.config;

import com.pengjing.protocol.Serial;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Description:
 * @Version: V1.0
 */
public abstract class Config {
    //读取配置文件
    static Properties properties;
    static {
        try(InputStream in = Config.class.getResourceAsStream("/application.properties")){
            properties = new Properties();
            properties.load(in);
        }catch (IOException e){
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Serial.decodec getSerialDecodec(){
        String value = properties.getProperty("Serial.decodec ");
        if(value != null){
            return Serial.decodec.valueOf(value);
        }else {
            return Serial.decodec.Java;
        }
    }

}
