package top.outlands.foundation.boot;

import org.apache.logging.log4j.Logger;
import top.outlands.foundation.trie.PrefixTrie;
import zone.rong.imaginebreaker.ImagineBreaker;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * @author ZZZank
 */
public class FilteredClassLoader extends ClassLoader implements ClassLoadingRules {
    public static final ClassNotFoundException FALL_THROUGH = new ClassNotFoundException("Class denied by FilteredClassLoader filters, falling through");
    private static final MethodHandle H_LOAD_CLASS;

    static {
        try {
            H_LOAD_CLASS = MethodHandles.privateLookupIn(ClassLoader.class, ImagineBreaker.lookup())
                .findVirtual(ClassLoader.class, "loadClass", MethodType.methodType(Class.class, String.class, boolean.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final PrefixTrie<Boolean> exclusions = new PrefixTrie<>();
    private final PrefixTrie<Boolean> inclusions = new PrefixTrie<>();
    private final Logger logger;

    public FilteredClassLoader(String name, ClassLoader parent, Logger logger) {
        super(name, parent);
        this.logger = logger;
    }

    public FilteredClassLoader(ClassLoader parent, Logger logger) {
        super(parent);
        this.logger = logger;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (exclusions.getFirstKeyValueNode(name) != null && inclusions.getFirstKeyValueNode(name) == null) {
            // excluded and not explicitly included again
            throw FALL_THROUGH;
        }
        try {
            return (Class<?>) H_LOAD_CLASS.invoke(this.getParent(), name, resolve);
        } catch (ClassNotFoundException notFound) {
            throw notFound;
        } catch (Throwable e) {
            if (e instanceof RuntimeException runtimeEx) {
                throw runtimeEx;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (exclusions.getFirstKeyValueNode(name) != null && inclusions.getFirstKeyValueNode(name) == null) {
            // excluded and not explicitly included again
            throw FALL_THROUGH;
        }
        return this.getParent().loadClass(name);
    }

    @Override
    public PrefixTrie<Boolean> getExclusions() {
        return exclusions;
    }

    @Override
    public PrefixTrie<Boolean> getInclusions() {
        return inclusions;
    }

    @Override
    public boolean exclude(String prefix) {
        if (logger != null) {
            logger.debug("Excluding classes from being loaded by parent classloader: {}", prefix);
        }
        return ClassLoadingRules.super.exclude(prefix);
    }

    @Override
    public boolean include(String prefix) {
        if (logger != null) {
            logger.debug("Including classes to be loaded by parent classloader: {}", prefix);
        }
        return ClassLoadingRules.super.include(prefix);
    }
}
