package com.bilibili.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SensitiveFilter {
    // 替换符
    private static final String REPLACEMENT = "***";
    //根节点
    private TreeNode rootNode= new TreeNode();

    //初始化
    @PostConstruct
    public void init(){
        try (
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("Sensitive-word.txt");
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
        ){
            String[] keyword = bufferedReader.readLine().split("，");
            for (String s : keyword) {
                this.addTreeNode(s);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filterText(String text){
        if (StringUtils.isEmpty(text))
            return null;
        // 指针1
        TreeNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();
        while (position<text.length()){
            char c=text.charAt(position);
            //跳过符号
            if (isSymbol(c)){
                if (tempNode==rootNode){
                    sb.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            tempNode = tempNode.getNode(c);
            if (tempNode==null){
                sb.append(text.charAt(begin));
                tempNode=rootNode;
                position=++begin;
            }else if (tempNode.getKeywordEnd()){
                sb.append(REPLACEMENT);
                begin=++position;
                tempNode=rootNode;
            }else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    //检测是否是符号
    private boolean isSymbol(Character c){
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //将一个敏感词添加到前缀树中
    private void addTreeNode(String keyword){
       TreeNode tempNode=rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            Character c=keyword.charAt(i);
            TreeNode node = tempNode.getNode(c);
            if (node==null){
                node = new TreeNode();
                tempNode.addNode(c,node);
            }
            tempNode=node;
            if (i==keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    //前缀树
    private class TreeNode{
        //关键词结束标识
        private boolean isKeywordEnd=false;
        //存当前字符和下一个节点
        Map<Character,TreeNode> subNodes=new HashMap<>();
        private boolean getKeywordEnd(){
            return this.isKeywordEnd;
        }
        private void setKeywordEnd(boolean isKeywordEnd){
            this.isKeywordEnd=isKeywordEnd;
        }
        //添加子节点
        private void addNode(Character c,TreeNode node){
            subNodes.put(c,node);
        }
        //获取子节点
        private TreeNode getNode(Character c){
            return subNodes.get(c);
        }
    }
}
