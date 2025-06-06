package com.nieran;
//改良版，将键值改为int型，比较就会快点
import java.util.HashMap;
import java.util.Map;

    public class HashCollisionAttack1 {

        static class HashCollisionKey implements Comparable<HashCollisionKey> {
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

            //提高效能的关键
            @Override
            public int compareTo(HashCollisionKey o) {
                return Integer.compare(this.id, o.id);
            }
        }

        public static void main(String[] args) {
            testWithCapacity(50_000, 65536);
        }

        static void testWithCapacity(int entryCounter, int capacity) {
            System.out.println("\n===== 测试 HashMap (" + entryCounter + " 条, 容量=" + capacity + ") =====");

            Map<HashCollisionKey, Integer> map = new HashMap<>(capacity);
            long startTime = System.nanoTime();

            for (int i = 0; i < entryCounter; i++) {
                map.put(new HashCollisionKey(i), i);
            }

            long totalTime = System.nanoTime() - startTime;
            System.out.printf("总插入耗时: %.6f 秒%n", totalTime / 1_000_000_000.0);
            //System.out.println("平均插入时间: %.3f 微秒/条%n", (totalTime * 1000.0) / entryCounter);
            System.out.println("最终Map大小: " + map.size());
        }
    }
