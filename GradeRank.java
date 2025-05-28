package com.example.test;
import java.util.Scanner;
public class GradeRank {
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        System.out.println("请输入你的成绩：");
        if (s.hasNextInt() ) {
            int grade=s.nextInt();
            if(grade>=90&&grade<=100){
                System.out.println("你的等级是A!!!");
            }
            else if(90>grade&&grade>=80){
                System.out.println("你的等级是B!!");

            }
            else if (grade<80&&grade>=70) {
                System.out.println("你的等级是：C!");

            }
            else if(grade<70&&grade>=0) {
                System.out.println("你的成绩等级是D!?");

            }
            else {
                System.out.println("成绩超出范围");
            }
        }
        else{
            System.out.println("你的成绩是无效的");
        }

    }


}
