package com.harvey.review_system;

import com.harvey.review_system.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplicationTests {
    @Test
    void test(){
    }

    public static void main(String[] args) {
        String s = null;
        System.out.println(s);
        Object o = s;
        System.out.println(o);
        String s2 = (String) s;
        System.out.println(s2);
    }
}
