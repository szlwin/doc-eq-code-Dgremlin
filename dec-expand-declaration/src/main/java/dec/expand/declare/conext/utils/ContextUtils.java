package dec.expand.declare.conext.utils;

import dec.expand.declare.conext.DescContext;
import dec.expand.declare.conext.desc.business.BusinessDesc;
import dec.expand.declare.conext.desc.system.SystemDesc;
import dec.expand.declare.conext.parser.xml.exception.XMLParseException;
import dec.expand.declare.conext.parser.xml.parser.ContextDescParser;
import dec.expand.declare.system.System;
import dec.expand.declare.system.SystemContext;

public class ContextUtils {


    public static void load(BusinessDesc businessDesc) {
        DescContext.get().addBusiness(businessDesc);
    }

    public static void load(System system) {
        SystemContext.get().add(system);
    }

    /**
     * 根据文件路径加载系统配置文件
     *
     * @param filePathArray
     */
    public static void loadConfig(String filePathArray[]) throws XMLParseException {
        ContextDescParser contextDescParser = new ContextDescParser();
        for (String filePath : filePathArray) {
            contextDescParser.parser(filePath);
        }
    }

    /**
     * 根据文件路径加载业务配置文件
     *
     * @param filePath
     */
    public static void loadBusiness(String filePath) {

    }
}
