package top.outlands.foundation.boot;

import top.outlands.foundation.trie.PrefixTrie;

/**
 * @author ZZZank
 */
public interface ClassLoadingRules {

    ClassLoader target();

    /**
     * if class names matches any of the exclusions defined here, and matches none of the inclusions defined in
     * {@link #getInclusions()}, it will not be loaded by {@link #target()}
     *
     * @see #exclude(String)
     */
    PrefixTrie<Boolean> getExclusions();

    /**
     * if class names matches any of the exclusions defined in {@link #getExclusions()}, and matches none of the
     * inclusions defined here, it will not be loaded by {@link #target()}
     *
     * @see #include(String)
     */
    PrefixTrie<Boolean> getInclusions();

    /**
     * @see #getExclusions()
     */
    default boolean exclude(String prefix) {
        return getExclusions().put(prefix, Boolean.TRUE);
    }

    /**
     * @see #getInclusions()
     */
    default boolean include(String prefix) {
        return getInclusions().put(prefix, Boolean.TRUE);
    }
}
