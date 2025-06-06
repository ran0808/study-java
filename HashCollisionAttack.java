package com.nieran;

import java.util.HashMap;
import java.util.Map;

public class HashCollisionAttack {

    static class HashCollisionKey {
        private final int id;

        public HashCollisionKey(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return 1; // 仍然强制哈希冲突
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashCollisionKey that = (HashCollisionKey) o;
            return id == that.id;
        }

    }

    public static void main(String[] args) {
        testWithCapacity(10000, 65536);
    }
    static void testWithCapacity(int entryCounter, int capacity) {
        System.out.println("\n===== 测试 HashMap (" + entryCounter + "条,容量=" + capacity + ") =====");

        Map<HashCollisionKey, Integer> map = new HashMap<>(capacity);
        long startTime = System.nanoTime();

        for (int i = 0; i < entryCounter; i++) {
            map.put(new HashCollisionKey(i), i);
        }
        long totalTime = System.nanoTime() - startTime;
        System.out.println("总插入耗时:"+ totalTime/1000000+"ms");
        System.out.println("最终Map大小: " + map.size());
    }
}