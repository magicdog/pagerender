package com.hipu.render.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

import java.awt.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weijian
 * Date : 2013-03-18
 */

public class Config extends MapConfiguration{
	
    private static  final Logger LOG = Logger.getLogger(Config.class);
    
    private ConfigurationListener listener;

    public static final ArgProp RENDER_BROWSER_COUNT = new ArgProp("render.browser.count", true, Integer.class, 3);

    public static final ArgProp RENDER_BROWSER_TIMEOUT = new ArgProp("render.browser.timeout", true, Integer.class, 10);
    
    public static final ArgProp RENDER_BROWSER_CACHE_DIR = new ArgProp("render.browser.cache.dir", false, String.class, "/var/cache");
    
    public static final ArgProp RENDER_BROWSER_ENABLE_PROXY = new ArgProp("render.browser.eable.proxy", false, Boolean.class, false);
    
    public static final ArgProp RENDER_BROWSER_PROXY_IP = new ArgProp("render.browser.proxy.ip", false, String.class, "localhost");
    
    public static final ArgProp RENDER_BROWSER_PROXY_PORT = new ArgProp("render.browser.proxy.port", false, Integer.class, 3128);
    
    public final ImmutableMap<String, ArgProp> argsMap;

    private static Config instance;

    
    
    private Config(){
        super(new ConcurrentHashMap<String, Object>());

        ImmutableMap.Builder builder = new ImmutableMap.Builder<String, ArgProp>();
        for ( Field field : getClass().getFields() ){
            if ( field.getType().getSimpleName().equals(ArgProp.class.getSimpleName()) ){
                try {
                    ArgProp argProp = (ArgProp) field.get(null);
                    builder.put(argProp.name, argProp);

                    if ( argProp.getDefaultValue() != null ){
                        super.setProperty(argProp.name, argProp.convertProperty(argProp.defaultValue));
                    }
                } catch (IllegalAccessException e) {
                    System.out.println(e);
                    LOG.error("Init Config argument error: " + field.getName(), e);
                }
            }
        }
        argsMap = builder.build();
    }

    public static Config getInstance(){
        synchronized (Config.class){
            if ( instance == null ){
                instance = new Config();
            }
        }
        return instance;
    }

    public void initConfig(Configuration config){
    	
        for ( Map.Entry<String, ArgProp> entry : argsMap.entrySet()){
            Object val = config.getProperty(entry.getKey());
            if ( val != null ){
                super.setProperty(entry.getKey(), entry.getValue().convertProperty(val));
            }
        }
        
        listener = new BrowserListener();
    	this.addConfigurationListener(listener);
    }


    @Override
    public void setProperty(String key, Object value) {
        ArgProp argProp = argsMap.get(key);
        if ( argProp == null ){
            throw new RuntimeException("Not such property in Config: " + key);
        }

        if ( !argProp.isChangeable() ){
            throw new RuntimeException("Property[" + key + "] is unchangeable!") ;
        }

        super.setProperty(key, argProp.convertProperty(value));
    }

    public void setProperty(ArgProp argProp, Object value) {
        if ( !argProp.isChangeable() ){
            throw new RuntimeException("Property[" + argProp.name() + "] is unchangeable!") ;
        }

        super.setProperty(argProp.name(), argProp.convertProperty(value));
    }


    public List<Map<String, Object>> getArgsInfo(){
        List<Map<String, Object>> list = Lists.newLinkedList();
        for ( ArgProp argProp : argsMap.values() ){
            Map<String, Object> map  = argProp.toMap();
            Object val =  getProperty(argProp.name);
            map.put("value",  val == null ? "" : val);
            list.add(map);
        }
        return list;
    }

    public Object getProperty(ArgProp prop) {
        return super.getProperty(prop.name());
    }

    public BigDecimal getBigDecimal(ArgProp prop) {
        return super.getBigDecimal(prop.name());     
    }

    
    public BigDecimal getBigDecimal(ArgProp prop, BigDecimal defaultValue) {
        return super.getBigDecimal(prop.name(), defaultValue);
    }

    
    public BigInteger getBigInteger(ArgProp prop) {
        return super.getBigInteger(prop.name());
    }

    
    public BigInteger getBigInteger(ArgProp prop, BigInteger defaultValue) {
        return super.getBigInteger(prop.name(), defaultValue);
    }

    
    public boolean getBoolean(ArgProp prop) {
        return super.getBoolean(prop.name());
    }

    
    public boolean getBoolean(ArgProp prop, boolean defaultValue) {
        return super.getBoolean(prop.name(), defaultValue);
    }

    
    public Boolean getBoolean(ArgProp prop, Boolean defaultValue) {
        return super.getBoolean(prop.name(), defaultValue);
    }

    
    public byte getByte(ArgProp prop) {
        return super.getByte(prop.name());
    }

    public byte getByte(ArgProp prop, byte defaultValue) {
        return super.getByte(prop.name(), defaultValue);
    }

    public Byte getByte(ArgProp prop, Byte defaultValue) {
        return super.getByte(prop.name(), defaultValue);
    }

    public double getDouble(ArgProp prop) {
        return super.getDouble(prop.name());
    }
    
    public double getDouble(ArgProp prop, double defaultValue) {
        return super.getDouble(prop.name(), defaultValue);
    }
    
    public Double getDouble(ArgProp prop, Double defaultValue) {
        return super.getDouble(prop.name(), defaultValue);
    }
    
    public float getFloat(ArgProp prop) {
        return super.getFloat(prop.name());
    }

    public float getFloat(ArgProp prop, float defaultValue) {
        return super.getFloat(prop.name(), defaultValue);
    }
    
    public Float getFloat(ArgProp prop, Float defaultValue) {
        return super.getFloat(prop.name(), defaultValue);
    }
    
    public int getInt(ArgProp prop) {
        return super.getInt(prop.name());
    }

    public int getInt(ArgProp prop, int defaultValue) {
        return super.getInt(prop.name(), defaultValue);
    }

    public Integer getInteger(ArgProp prop, Integer defaultValue) {
        return super.getInteger(prop.name(), defaultValue);
    }

    public List<Object> getList(ArgProp prop) {
        return super.getList(prop.name());
    }

    public List<Object> getList(ArgProp prop, List<Object> defaultValue) {
        return super.getList(prop.name(), defaultValue);
    }

    public long getLong(ArgProp prop) {
        return super.getLong(prop.name());
    }

    public long getLong(ArgProp prop, long defaultValue) {
        return super.getLong(prop.name(), defaultValue);
    }

    public Long getLong(ArgProp prop, Long defaultValue) {
        return super.getLong(prop.name(), defaultValue);
    }

    public Properties getProperties(ArgProp prop) {
        return super.getProperties(prop.name());
    }

    public Properties getProperties(ArgProp prop, Properties defaults) {
        return super.getProperties(prop.name(), defaults);
    }

    public short getShort(ArgProp prop) {
        return super.getShort(prop.name());
    }

    public short getShort(ArgProp prop, short defaultValue) {
        return super.getShort(prop.name(), defaultValue);
    }

    public Short getShort(ArgProp prop, Short defaultValue) {
        return super.getShort(prop.name(), defaultValue);
    }

    public String getString(ArgProp prop) {
        return super.getString(prop.name());
    }

    public String getString(ArgProp prop, String defaultValue) {
        return super.getString(prop.name(), defaultValue);
    }

    public String[] getStringArray(ArgProp prop) {
        return super.getStringArray(prop.name());
    }

    private static Object convertProperty(Class<?> cls, Object value) throws ConversionException
    {
        if (cls.isInstance(value))
        {
            return value; // no conversion needed
        }

        if ( List.class.equals(cls)){
            return value;
        }

        if (Boolean.class.equals(cls) || Boolean.TYPE.equals(cls))
        {
            return PropertyConverter.toBoolean(value);
        }
        else if (Number.class.isAssignableFrom(cls) || cls.isPrimitive())
        {
            if (Integer.class.equals(cls) || Integer.TYPE.equals(cls))
            {
                return PropertyConverter.toInteger(value);
            }
            else if (Long.class.equals(cls) || Long.TYPE.equals(cls))
            {
                return PropertyConverter.toLong(value);
            }
            else if (Byte.class.equals(cls) || Byte.TYPE.equals(cls))
            {
                return PropertyConverter.toByte(value);
            }
            else if (Short.class.equals(cls) || Short.TYPE.equals(cls))
            {
                return PropertyConverter.toShort(value);
            }
            else if (Float.class.equals(cls) || Float.TYPE.equals(cls))
            {
                return PropertyConverter.toFloat(value);
            }
            else if (Double.class.equals(cls) || Double.TYPE.equals(cls))
            {
                return PropertyConverter.toDouble(value);
            }
            else if (BigInteger.class.equals(cls))
            {
                return PropertyConverter.toBigInteger(value);
            }
            else if (BigDecimal.class.equals(cls))
            {
                return PropertyConverter.toBigDecimal(value);
            }
        }
        else if (URL.class.equals(cls))
        {
            return PropertyConverter.toURL(value);
        }
        else if (Locale.class.equals(cls))
        {
            return PropertyConverter.toLocale(value);
        }
        else if (Color.class.equals(cls))
        {
            return PropertyConverter.toColor(value);
        }

        throw new ConversionException("The value '" + value + "' (" + value.getClass() + ")"
                + " can't be converted PropertyConverter.to a " + cls.getName() + " object");
    }

    public boolean containProperty(String property){
        return argsMap.containsKey(property);
    }

    public static class ArgProp {
        private final String name;
        private Object defaultValue;
        private final boolean changeable;
        private final Class<?> classType;

        public ArgProp(String name, boolean changeable, Class<?> classType, Object defaultValue) {
            this.name = name;
            this.changeable = changeable;
            this.classType = classType;
            this.defaultValue = defaultValue;
        }

        public boolean isChangeable() {
            return changeable;
        }

        public Class<?> getClassType() {
            return classType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String name(){
            return this.name;
        }

        public Object convertProperty(Object value) throws ConversionException{
            return Config.convertProperty(classType, value);
        }

        public Map<String, Object> toMap(){
            Map<String, Object> map = Maps.newHashMap();
            map.put("name", name);
            map.put("changeable", changeable);
            map.put("class", classType.getSimpleName());

            map.put("default", defaultValue == null ? "" : defaultValue);
            return map;
        }

         
        public String toString() {
            return "ArgProp{" +
                    "name='" + name + '\'' +
                    ", changeable=" + changeable +
                    ", classType=" + classType +
                    ", defaultValue=" + defaultValue +
                    '}';
        }
    }

//    public static void main(String[] args) {
//        System.out.println(Config.getInstance().getMap());
//        System.out.println(JSON.toJSON(Config.CRAWL_MONGO_DB));
//    }
}
