package com.teamface.common.util.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: ZZH
 * @date: 2017年12月21日 下午4:32:08
 * @version: 1.0
 */

public class NodeUtil
{
    
    public static class Node
    {
        // 节点id
        public String id = null;
        
        // 父节点id
        public String pid = null;
        
        public Node(String id, String pid)
        {
            this.id = id;
            this.pid = pid;
        }
        
        // 定义上一节点变量
        public Node upNode = null;
        
        // 定义向下节点集合
        public List<Node> subList = new ArrayList<>();
    }
    
    private NodeUtil()
    {
        
    }
    
    public static Map<String, Node> setNodes(List<Node> nodes)
    {
        // 使用map存放节点
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : nodes)
        {
            // 将ID对应的node放到HashMap中
            nodeMap.put(node.id, node);
        }
        
        // 创建动态数组，用来处理顶级节点“1”
        List<Node> toplist = new ArrayList<>();
        
        for (Node node : nodes)
        {
            if (node.pid != null && !"".equals(node.pid))
            {
                Node upNode = nodeMap.get(node.pid);
                if (upNode != null)
                {
                    // 添加下级
                    upNode.subList.add(node);
                    // 设置上级
                    node.upNode = upNode;
                }
                else
                {
                    toplist.add(node);
                }
            }
            else
            {
                toplist.add(node);
            }
        }
        return nodeMap;
    }
    
    /**
     * 
     * @param id 当前节点
     * @param nodeMap
     * @param nodeSB
     * @Description:获取给定节点的上级节点（包括本级）
     */
    public static void getNodeUpNode(String id, Map<String, Node> nodeMap, StringBuilder nodeSB)
    {
        Node node = nodeMap.get(id);
        if (node == null)
        {
            return;
        }
        if (node.upNode != null)
        {
            // 递归调用
            getNodeUpNode(node.upNode.id, nodeMap, nodeSB);
        }
        if (nodeSB.length() > 0)
        {
            nodeSB.append(",");
        }
        nodeSB.append(node.id);
    }
    
    /**
     * 
     * @param id 当前节点
     * @param nodeMap
     * @param nodeSB
     * @Description:获取给定节点的下级节点
     */
    public static void getNodeSubNode(String id, Map<String, Node> nodeMap, StringBuilder nodeSB)
    {
        Node node = nodeMap.get(id);
        if (node == null)
        {
            return;
        }
        for (Node subNode : node.subList)
        {
            // 递归调用
            if (nodeSB.length() > 0)
            {
                nodeSB.append(",");
            }
            nodeSB.append(subNode.id);
            getNodeSubNode(subNode.id, nodeMap, nodeSB);
        }
    }
    /**
     * 
     * @param id 当前节点
     * @param nodeMap
     * @param nodeSB
     * @Description:获取给定节点的下级节点(包括本级)
     */
    public static void getNodeSubAndLocalNode(String id, Map<String, Node> nodeMap, StringBuilder nodeSB)
    {
        Node node = nodeMap.get(id);
        if (node == null)
        {
            return;
        }
        for (Node subNode : node.subList)
        {
            // 递归调用
            if (nodeSB.length() > 0)
            {
                nodeSB.append(",");
            }
            nodeSB.append(subNode.id);
            getNodeSubNode(subNode.id, nodeMap, nodeSB);
        }
        if (nodeSB.length() > 0)
        {
            nodeSB.append(",");
        }
        nodeSB.append(id);
    }
    public static void main(String[] args)
    {
        // 创建集合,建立题目中的树形结构
        List<Node> list = new ArrayList<>();
        list.add(new Node("1", ""));
        list.add(new Node("11", "1"));
        list.add(new Node("12", "1"));
        list.add(new Node("111", "11"));
        list.add(new Node("1111", "111"));
        list.add(new Node("121", "12"));
        list.add(new Node("122", "12"));
        list.add(new Node("1211", "121"));
        list.add(new Node("1221", "122"));
        
        Map<String, Node> nodeMap = setNodes(list);
        StringBuilder sb = new StringBuilder();
        getNodeUpNode("1111",nodeMap,sb);
        System.out.println(sb.toString());
        System.out.println("--------------------------------------");
        sb.setLength(0);
        getNodeSubNode("11",nodeMap,sb);
        System.out.println(sb.toString());
        
    }
}
