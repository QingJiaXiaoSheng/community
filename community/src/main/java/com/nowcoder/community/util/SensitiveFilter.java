package com.nowcoder.community.util;

/**
 * 敏感词可以存放到文件中，一个占一行，也可以存放到数据库中
 * <p>
 * 数据结构——前缀树(Trie、字典树、查找树)
 * <p>
 * 做好标记，维护好上下级的关系
 * <p>
 * 敏感词过滤器流程：
 * 1、定义前缀树
 * 2、根据敏感词，初始化前缀树
 * 3、编写过滤敏感词的方法
 */


import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    // 该注解是初始化注解，表明该方法是初始化方法，表示说当容器实例化这个bean时，在调用构造器之后，该方法自动调用
    // 服务启动之后，就被调用
    public void init() {
        try (
                // ClassLoader类加载器，从类路径(target/classes)中加载资源
                // 获得的是字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");

                // 从字节流中读取文字不太方便，需要转换为字符流，直接使用InputStreamReader也不太方便，最后转换为缓冲流，效率高。
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树（逻辑复杂，最后封装到一个独立的方法去调用）
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }


    }

    // 将一个敏感词添加到前缀树当中去
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点,进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1,指向树
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 忽略跳过特殊符号,封装成一个方法
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检查当前字符
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // [begin,position]子字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 指针1重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,[begin,position]字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 指针1重新指向根节点
                tempNode = rootNode;
            }else {
                // 继续检查下一个字符
                position++;
            }
        }
        // 将最后一批字符记入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    // 前缀树、定义一个内部类（因为这个类是在这个类工具中使用，其他地方基本不会用到）
    private class TrieNode { // 描述前缀树的节点

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点（key是下级字符，value是下级节点）,因为节点是多个,且子节点有对应字符，所以用Map
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
