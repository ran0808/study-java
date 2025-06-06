package com.nieran;

import java.util.Map;
import java.util.Objects;
import java.util.*;


    /**
     * 安全 HashMap 实现，包含扰动函数和自动树化机制
     */
    public class SafeHashMap<K, V> {
        // 默认初始容量
        static final int DEFAULT_INITIAL_CAPACITY = 16;
        // 最大容量
        static final int MAXIMUM_CAPACITY = 1 << 30;
        // 默认负载因子
        static final float DEFAULT_LOAD_FACTOR = 0.75f;
        // 树化阈值
        static final int TREEIFY_THRESHOLD = 8;
        // 反树化阈值
        static final int UNTREEIFY_THRESHOLD = 6;
        // 最小树化容量
        static final int MIN_TREEIFY_CAPACITY = 64;

        // 底层存储数组
        Node<K, V>[] table;
        // 键值对数量
        int size;
        // 修改计数器
        int modCount;
        // 扩容阈值 (容量 * 负载因子)
        int threshold;
        // 负载因子
        final float loadFactor;

        /**
         * 哈希桶节点基类
         */
        static class Node<K, V> {
            final int hash;
            final K key;
            V value;
            Node<K, V> next;

            Node(int hash, K key, V value, Node<K, V> next) {
                this.hash = hash;
                this.key = key;
                this.value = value;
                this.next = next;
            }

            public final K getKey() { return key; }
            public final V getValue() { return value; }

            public final String toString() {
                return key + "=" + value;
            }

            public final int hashCode() {
                return Objects.hashCode(key) ^ Objects.hashCode(value);
            }

            public final V setValue(V newValue) {
                V oldValue = value;
                value = newValue;
                return oldValue;
            }

            public final boolean equals(Object o) {
                if (o == this) return true;
                if (o instanceof Map.Entry) {
                    Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                    return Objects.equals(key, e.getKey()) &&
                            Objects.equals(value, e.getValue());
                }
                return false;
            }
        }

        /**
         * 红黑树节点类
         */
        static final class TreeNode<K, V> extends Node<K, V> {
            TreeNode<K, V> parent;
            TreeNode<K, V> left;
            TreeNode<K, V> right;
            TreeNode<K, V> prev; // 用于反树化
            boolean red;

            TreeNode(int hash, K key, V val, Node<K, V> next) {
                super(hash, key, val, next);
            }

            // 树化方法
            final void treeify(Node<K, V>[] tab) {
                // 实际树化实现会放在这里
                System.out.println("树化桶: " + (hash & (tab.length - 1)));
            }

            // 其他红黑树操作...
        }

        /**
         * 默认构造函数
         */
        public SafeHashMap() {
            this.loadFactor = DEFAULT_LOAD_FACTOR;
        }

        /**
         * 带初始容量的构造函数
         */
        public SafeHashMap(int initialCapacity) {
            this(initialCapacity, DEFAULT_LOAD_FACTOR);
        }

        /**
         * 完整构造函数
         */
        public SafeHashMap(int initialCapacity, float loadFactor) {
            if (initialCapacity < 0)
                throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
            if (initialCapacity > MAXIMUM_CAPACITY)
                initialCapacity = MAXIMUM_CAPACITY;
            if (loadFactor <= 0 || Float.isNaN(loadFactor))
                throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

            this.loadFactor = loadFactor;
            this.threshold = tableSizeFor(initialCapacity);
        }

        /**
         * 扰动函数实现
         */
        static final int perturb(int keyHash) {
            // 双重扰动：高16位与低16位混合
            int h = keyHash;
            h ^= (h >>> 16) ^ (h >>> 8);
            h ^= (h << 16) ^ (h << 8);
            return h ^ (h >>> 4);
        }

        /**
         * 计算数组容量（向上取整到2的幂）
         */
        static final int tableSizeFor(int cap) {
            int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
            return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        }

        /**
         * 计算哈希值（使用扰动函数）
         */
        static final int safeHash(Object key) {
            int h = key == null ? 0 : perturb(key.hashCode());
            return h;
        }

        /**
         * 获取元素
         */
        public V get(Object key) {
            Node<K, V> e;
            return (e = getNode(safeHash(key), key)) == null ? null : e.value;
        }

        /**
         * 内部获取实现
         */
        final Node<K, V> getNode(int hash, Object key) {
            Node<K, V>[] tab = table;
            Node<K, V> first, e;
            int n;
            K k;

            if (tab != null && (n = tab.length) > 0 &&
                    (first = tab[(n - 1) & hash]) != null) {

                // 总是先检查桶的首节点
                if (first.hash == hash &&
                        ((k = first.key) == key || (key != null && key.equals(k))))
                    return first;

                if ((e = first.next) != null) {
                    // 如果是树节点，使用树查找
                    if (first instanceof TreeNode)
                        return ((TreeNode<K, V>)first).getTreeNode(hash, key);

                    // 否则遍历链表
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key || (key != null && key.equals(k))))
                            return e;
                    } while ((e = e.next) != null);
                }
            }
            return null;
        }

        /**
         * 放入元素
         */
        public V put(K key, V value) {
            return putVal(safeHash(key), key, value, false, true);
        }

        /**
         * 核心的放入实现
         */
        final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
            Node<K, V>[] tab = table;
            Node<K, V> p;
            int n, i;

            // 初始化表
            if (tab == null || (n = tab.length) == 0)
                n = (tab = resize()).length;

            // 计算桶位置
            i = (n - 1) & hash;
            p = tab[i];

            // 桶为空直接放入
            if (p == null) {
                tab[i] = newNode(hash, key, value, null);
            } else {
                // 桶不为空
                Node<K, V> e = null; // 用于存放找到的节点
                K k = p.key;

                // 检查首节点
                if (p.hash == hash && (Objects.equals(k, key))) {
                    e = p;
                }
                // 如果是树节点
                else if (p instanceof TreeNode) {
                    e = ((TreeNode<K, V>)p).putTreeVal(this, tab, hash, key, value);
                }
                // 否则遍历链表
                else {
                    int binCount = 1;
                    while (true) {
                        if ((e = p.next) == null) {
                            p.next = newNode(hash, key, value, null);
                            // 检查是否达到树化阈值
                            if (binCount >= TREEIFY_THRESHOLD - 1) {
                                treeifyBin(tab, hash);
                            }
                            break;
                        }
                        // 找到相同key的节点
                        if (e.hash == hash && (Objects.equals(k = e.key, key))) {
                            break;
                        }
                        p = e;
                        binCount++;
                    }
                }

                // 更新已有节点的值
                if (e != null) {
                    V oldValue = e.value;
                    if (!onlyIfAbsent || oldValue == null) {
                        e.value = value;
                    }
                    return oldValue;
                }
            }

            // 更新大小并检查是否需要扩容
            if (++size > threshold) {
                resize();
            }
            return null;
        }

        /**
         * 树化处理
         */
        final void treeifyBin(Node<K, V>[] tab, int hash) {
            int n, index;
            Node<K, V> e;

            // 当容量不足时先扩容
            if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
                resize();
            }
            // 当容量足够时进行树化
            else if ((index = (n - 1) & hash) >= 0 &&
                    (e = tab[index]) != null) {

                // 构建红黑树
                TreeNode<K, V> hd = null, tl = null;
                do {
                    TreeNode<K, V> p = new TreeNode<>(e.hash, e.key, e.value, null);
                    if (tl == null) {
                        hd = p;
                    } else {
                        p.prev = tl;
                        tl.next = p;
                    }
                    tl = p;
                } while ((e = e.next) != null);

                // 执行实际树化
                if ((tab[index] = hd) != null) {
                    hd.treeify(tab);
                }
            }
        }

        /**
         * 扩容方法
         */
        final Node<K, V>[] resize() {
            Node<K, V>[] oldTab = table;
            int oldCap = (oldTab == null) ? 0 : oldTab.length;
            int oldThr = threshold;
            int newCap, newThr = 0;

            // 计算新容量
            if (oldCap > 0) {
                if (oldCap >= MAXIMUM_CAPACITY) {
                    threshold = Integer.MAX_VALUE;
                    return oldTab;
                } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                        oldCap >= DEFAULT_INITIAL_CAPACITY) {
                    newThr = oldThr << 1; // 双倍阈值
                }
            }
            // 初始构造
            else if (oldThr > 0) {
                newCap = oldThr;
            } else {
                newCap = DEFAULT_INITIAL_CAPACITY;
                newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
            }

            // 计算新阈值
            if (newThr == 0) {
                float ft = (float)newCap * loadFactor;
                newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                        (int)ft : Integer.MAX_VALUE);
            }
            threshold = newThr;

            // 创建新桶数组
            @SuppressWarnings({"rawtypes", "unchecked"})
            Node<K, V>[] newTab = (Node<K, V>[])new Node[newCap];
            table = newTab;

            // 重新哈希现有元素
            if (oldTab != null) {
                for (int j = 0; j < oldCap; j++) {
                    Node<K, V> e;
                    if ((e = oldTab[j]) != null) {
                        oldTab[j] = null;

                        // 桶中只有一个节点
                        if (e.next == null) {
                            newTab[e.hash & (newCap - 1)] = e;
                        }
                        // 树节点处理
                        else if (e instanceof TreeNode) {
                            // 实际实现会拆分树
                            System.out.println("树节点迁移: " + j);
                        }
                        // 链表节点处理
                        else {
                            // 保留原顺序
                            Node<K, V> loHead = null, loTail = null;
                            Node<K, V> hiHead = null, hiTail = null;
                            Node<K, V> next;

                            do {
                                next = e.next;
                                if ((e.hash & oldCap) == 0) {
                                    if (loTail == null) {
                                        loHead = e;
                                    } else {
                                        loTail.next = e;
                                    }
                                    loTail = e;
                                } else {
                                    if (hiTail == null) {
                                        hiHead = e;
                                    } else {
                                        hiTail.next = e;
                                    }
                                    hiTail = e;
                                }
                            } while ((e = next) != null);

                            if (loTail != null) {
                                loTail.next = null;
                                newTab[j] = loHead;
                            }
                            if (hiTail != null) {
                                hiTail.next = null;
                                newTab[j + oldCap] = hiHead;
                            }
                        }
                    }
                }
            }
            return newTab;
        }

        // 创建新节点
        Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
            return new Node<>(hash, key, value, next);
        }

        // 其他HashMap方法省略...
    }

