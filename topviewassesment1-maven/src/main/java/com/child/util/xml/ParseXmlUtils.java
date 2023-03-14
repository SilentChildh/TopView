package com.child.util.xml;

import com.child.util.ChildLogger;
import com.child.util.orm.bean.MapperStatement;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 解析XML文件的工具类。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/14
 */
public class ParseXmlUtils {
    private static final Logger logger = ChildLogger.getLogger();
    /**
     * 通过指定包名获取包下所有“.xml”为后缀的文件<br/>
     * <p/>
     * 该方法始终不会返回一个null，当不存在文件时，返回的是一个空的{@link ArrayList}<br/>
     *
     * @param packageName 指定包名，包下含有xml文件
     * @return {@link List}<{@link File}> 返回指定包下的所有xml文件集合
     */
    public static List<File> getXmlFileFromPackage(String packageName) {
        final String file = "file";
        try {
            // 获取指定包下的所有文件的URL
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    // 注意！需要将包名各式转换为目录路径格式
                    .getResources(packageName.replace('.', '/'));

            // 遍历每一个URL
            while (resources.hasMoreElements()) {
                // 获取URL
                URL url = resources.nextElement();
                // 获取协议
                String protocol = url.getProtocol();
                // 如果满足协议，则进行解析，并将解析后的元素合并到集合中
                if (file.equals(protocol)) {
                    return getXmlFileFromDirectory(url.getPath());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("通过包名获取XML文件失败\n" + e.getMessage());
        }
        // 不存在文件则返回一个空list
        return new ArrayList<>();
    }

    /**
     * 通过指定目录路径获取包下所有“.xml”为后缀的文件<br/>
     * <p/>
     * 根据目录名可以获取该目录下的所有xml文件，通过包名可以获取进一步解析子目录。<br/>
     * 该方法始终不会返回一个null，当不存在文件时，返回的是一个空的{@link ArrayList}<br/>
     *
     * @param directoryPath 指定包名的目录路径
     * @return {@link List}<{@link File}> 返回指定目录路径下的所有xml文件集合
     */
    public static List<File> getXmlFileFromDirectory(String directoryPath) {
        // 接收文件的容器
        Stream<File> stream = new ArrayList<File>().stream();
        List<File> list = new ArrayList<>();

        // 根据目录创建文件
        File currentFile = new File(directoryPath);

        // 如果不存在该目录则直接返回一个空list
        if (!currentFile.isDirectory() || !currentFile.exists()) {
            return new ArrayList<>();
        }

        // 获取目录下的所有".xml"结尾的文件和子目录
        File[] files =
                currentFile.listFiles(file -> file.getName().endsWith(".xml") || file.isDirectory());

        // 如果不存在文件，返回一个空list
        if (files == null) {
            return new ArrayList<>();
        }

        for (File file : files) {
            // 如果是的xml文件的话则添加到集合中
            boolean isXml = file.getName().endsWith(".xml");
            if (isXml){
                list.add(file);
            }

            // 如果是子目录则继续搜索
            if (file.isDirectory()) {
                // 并将返回结果与原来的stream进行合并
                stream = Stream.concat(stream, getXmlFileFromDirectory(file.getAbsolutePath()).stream());
            }

        }
        // 最后再合并一次stream吗，并返回结果
        return Stream.concat(stream, list.stream()).collect(Collectors.toList());
    }

    /**
     * 解析Mapper.xml文件，用于获取SQL映射对象。<br/>
     * <p/>
     * 根据目录名可以获取该目录下的所有xml文件，通过包名可以获取进一步解析子目录。<br/>
     * 其中核心部分为：
     * 解析处理器{@link ParseMapperHandler}帮助完成了将xml中的sql映射放置到Map集合中。<br/>
     *
     * @param files 文件
     * @return {@link Map}<{@link String}, {@link MapperStatement}>
     */
    public static Map<String, MapperStatement> parseMapper(List<File> files) {
        try {
            // 获取解析器工厂
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            // 获取解析器
            SAXParser saxParser = saxParserFactory.newSAXParser();
            // 获取解析器处理器
            ParseMapperHandler parseMapperHandler = new ParseMapperHandler();

            Map<String, MapperStatement> collect = files.stream()
                    // 将流中的.xml文件进行解析，返回SQL映射集合的K-V条目
                    .map(x -> {
                        try {
                            saxParser.parse(x, parseMapperHandler);
                            // 返回K-V条目回到流中
                            return parseMapperHandler.getStatementMapper().entrySet();
                        } catch (SAXException | IOException e) {
                            logger.info("解析Mapper.xml文件失败\n" + e.getMessage());
                            throw new RuntimeException("解析Mapper.xml文件失败\n" + e.getMessage());
                        }
                    })
                    // 将Set<Entry<String, String>>一对多映射为Entry<String, String>，即从Set集合中取出元素
                    .flatMap(Set::stream)
                    // 最后通过线程安全的终结管道操作，将流中元素包装进Map集合中进行返回
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (entity1, entity2) -> entity1));

            logger.info("解析Mapper.xml文件成功");
            return collect;

        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("解析Mapper.xml文件失败\n"  +e.getMessage());
        }
    }
}
