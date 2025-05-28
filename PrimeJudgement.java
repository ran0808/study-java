package com.example.test;

import java.util.Scanner;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

public class PrimeJudgement {
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        System.out.println("请输入一个整数：");
        if (s.hasNext()) {
            int number=s.nextInt();
            if (number<=1){
                System.out.println("请输入大于1的整数");
            }
            else{
                int remainder =0;
                for (int i=2;i<sqrt(number)+1;i++)
                {
                    remainder=number%i;
                    if(remainder==0){
                        break;
                    }
                }
                if(remainder!=0){
                    System.out.println(number+"是素数");
                }
                else {
                    System.out.println(number+"不是素数");
                }
            }
            }
        else {
            System.out.println("你的输入不是整数");

        }    }


}
