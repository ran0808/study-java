package com.example.test;
import java.util.Scanner;
public class Scaner {
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        System.out.println("请输入你的年龄：");
        int age =s.nextInt();
        System.out.println("请输入你的姓名：");
        String name=s.next();
        System.out.println("你好，"+name+",你今年"+age+"岁了!");

    }
}
