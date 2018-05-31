package com.teamface.common.util;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
public class MD5Util {
    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};       
        try {
            byte[] btInput = s.getBytes();
            // ���MD5ժҪ�㷨�� MessageDigest ����
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // ʹ��ָ�����ֽڸ���ժҪ
            mdInst.update(btInput);
            // �������
            byte[] md = mdInst.digest();
            // ������ת����ʮ�����Ƶ��ַ�����ʽ
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
//    public static void main(String[] args) {
//        List<String> list=new ArrayList<String>();
//        list.add("1234567890");
//        list.add("qazwsxedc555");
//        list.add("123456");
//        list.add("zhoulang");
//        list.add("111111");
//        list.add("19793703");
//        list.add("123+qwe");
//        list.add("nautro123");
//        list.add("681119");
//        list.add("9198791987");
//        list.add("lj89968189");
//        list.add("223456");
//        list.add("n3294262002");
//        list.add("peng123568");
//        list.add("183520");
//        list.add("838223");
//        list.add("13265413009");
//        list.add("knt89968689");
//        list.add("5202133044");
//        list.add("w13141592678w");
//        list.add("nevercry1993");
//        list.add("123123");
//        
//        for(String password:list){
//        	String md5Password=MD5Util.MD5(password);
//        	System.out.println("update tbl_user set password ='"+md5Password+"' where password='"+password+"';");
//        }
//    }
    public static void main(String[] args) {
    	String str = "123123";
    	String md5Str = MD5(MD5(str+"hjhq2017Teamface"));
    	System.out.println("this md5Str :"+md5Str);
    }
}
