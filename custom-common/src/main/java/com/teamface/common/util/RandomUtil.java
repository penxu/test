package com.teamface.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class RandomUtil
{
    
    public static String genRandomNum()
    {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'};
            
        StringBuffer pwd = new StringBuffer("");
        
        Random r = new Random();
        while (count < 6)
        {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length)
            {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }
    
    
    
    public static String queryRandomNum(int maxNumber)
    {
        int maxNum = 36;
        int i;
        int count = 0;
        char[] str = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9'};
            
        StringBuffer pwd = new StringBuffer("");
        
        Random r = new Random();
        while (count < maxNumber)
        {
            i = Math.abs(r.nextInt(maxNum));
            if (i >= 0 && i < str.length)
            {
                pwd.append(str[i]);
                count++;
            }
        }
        return pwd.toString();
    }
    /**
     * 生成六位随机,速度最优算法
     * 
     * @author Liu
     * @date 2017-02-07
     * @return int
     */
    public static int create6Random()
    {
        int[] array = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Random rand = new Random();
        for (int i = 10; i > 1; i--)
        {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        for (int i = 0; i < 6; i++)
        {
            if (i == 0 && array[i] == 0)
            {
                array[i] = 1;
            }
            result = result * 10 + array[i];
        }
        return result;
    }
    
    public static long findTheMinEggs()
    {
        long eggs = 0;
        for (long n = 1; n < Long.MAX_VALUE; n++)
        {
            boolean flag =
                ((n - 1) % 2 == 0) && (n % 3 == 0) && ((n - 1) % 4 == 0) && ((n + 1) % 5 == 0) && ((n + 3) % 6 == 0) && (n % 7 == 0) && ((n - 1) % 8 == 0) && (n % 9 == 0);
            if (flag)
            {
                eggs = n;
                break;
            }
        }
        return eggs;
    }
    
    public static boolean isAnagram(String s, String t)
    {
        if (s == null)
            return Boolean.FALSE;
        if (t == null)
            return Boolean.FALSE;
        if ("".equals(s))
            return Boolean.FALSE;
        if ("".equals(t))
            return Boolean.FALSE;
        // 把字字符串变为单个的字符串列表
        char[] sc = s.toCharArray();
        char[] tc = t.toCharArray();
        List<Character> scharList = new ArrayList<Character>(sc.length);
        List<Character> tcharList = new ArrayList<Character>(tc.length);
        for (char i : sc)
        {
            scharList.add(i);
        }
        for (char j : tc)
        {
            tcharList.add(j);
        }
        // 先判断元素的个数是否相等，不相等的直接返回否
        if (scharList.size() != tcharList.size())
            return Boolean.FALSE;
        // 去重，去重后的元素个数是否相等，不相等的直接返回否
        HashSet<Character> sSet = new HashSet<Character>(scharList);
        HashSet<Character> tSet = new HashSet<Character>(tcharList);
        if (sSet.size() != tSet.size())
            return Boolean.FALSE;
        // 合并集合，若合并集合有添加新元素，直接返回否
        sSet.addAll(tSet);
        if (sSet.size() != tSet.size())
            return Boolean.FALSE;
        return Boolean.TRUE;
    }
    
    public static void main(String[] args)
    {
        
        // 测试1：如果是删除subList中的元素，对原列表的影响
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println("this list=" + list);
        List<Integer> sub = list.subList(0, 2);
        System.out.println("this sub=" + sub);
        
        // sub.remove(1);// 此处不会出错，修改子列表就是直接修改原列表，仍可以继续操作子列表和原列表
        list.remove(1);// 此处一定会出错，修改列表并不会修改子列表，子列表的是原列表的视图引用，原列表修改了视图引用会被破坏
        
        System.out.println("tis list after remove:" + list);
        System.out.println("this sub after remove:" + sub);
        
    }
}
