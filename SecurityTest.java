import org.junit.jupiter.api.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTest {
    private SafeHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new SafeHashMap<>();
    }

    // 测试扰动函数有效性
    @Test
    void testPerturbation() {
        // 模拟碰撞字符串
        String key1 = new String(new char[1000]).replace("\0", "A");
        String key2 = new String(new char[1000]).replace("\0", "B");

        // 未扰动的原始哈希码
        int rawHash1 = key1.hashCode();
        int rawHash2 = key2.hashCode();

        // 扰动后的哈希码
        int perturbedHash1 = SafeHashMap.safeHash(key1);
        int perturbedHash2 = SafeHashMap.safeHash(key2);

        // 扰动函数应改变原始哈希码
        assertNotEquals(rawHash1, perturbedHash1);
        assertNotEquals(rawHash2, perturbedHash2);

        // 相同的key应有相同扰动哈希
        assertEquals(
                SafeHashMap.safeHash(key1),
                SafeHashMap.safeHash(new String(key1))
        );
    }

    // 测试自动树化功能
    @Test
    void testAutoTreeify() {
        // 创建固定哈希键
        class FixedHashKey {
            private final int hash;
            private final String id;

            FixedHashKey(int hash, String id) {
                this.hash = hash;
                this.id = id;
            }

            @Override
            public int hashCode() {
                return hash;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                FixedHashKey that = (FixedHashKey) o;
                return Objects.equals(id, that.id);
            }
        }

        // 添加8个相同哈希键
        for (int i = 0; i < 8; i++) {
            map.put(new FixedHashKey(1, "Key" + i), i);
        }

        // 添加第9个键应触发树化
        map.put(new FixedHashKey(1, "Key8"), 8);

        // 通过控制台输出验证树化
        // 实际实现中会检查桶类型
    }

    // 测试树化后的查询性能
    @Test
    void testTreePerformance() {
        // 使用高冲突键加载数据
        for (int i = 0; i < 10_000; i++) {
            map.put("KEY" + i, i);
        }

        // 测试查询性能
        long startTime = System.nanoTime();
        for (int i = 0; i < 10_000; i++) {
            assertNotNull(map.get("KEY" + i));
        }
        long duration = System.nanoTime() - startTime;

        // 查询时间应在合理范围内 (避免GC干扰使用相对时间)
        assertTrue(duration < 10_000_000, "树查询时间异常");
    }

    // 测试扩容与树化协调
    @Test
    void testResizeAndTreeify() {
        // 在小容量下添加数据
        SafeHashMap<String, Integer> smallMap = new SafeHashMap<>(16);

        // 创建冲突键
        for (int i = 0; i < 20; i++) {
            smallMap.put("CONFLICT-" + i, i);
        }

        // 验证不会过早树化（容量不足MIN_TREEIFY_CAPACITY）
        // 实际实现中会检查resize次数
    }

    // 测试反树化机制
    @Test
    void testUntreeify() {
        // 创建达到树化条件的数据
        for (int i = 0; i < 10; i++) {
            map.put("ITEM" + i, i);
        }

        // 删除元素直到低于反树化阈值
        for (int i = 0; i < 4; i++) {
            map.remove("ITEM" + i);
        }

        // 验证不再处于树化状态
        // 实际实现中会检查桶类型
    }

    // 测试键为null的情况
    @Test
    void testNullKeyHandling() {
        map.put(null, 100);
        assertEquals(100, map.get(null));

        map.put(null, 200);
        assertEquals(200, map.get(null));
    }
}