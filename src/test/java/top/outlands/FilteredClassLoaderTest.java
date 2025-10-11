package top.outlands;

import net.minecraft.launchwrapper.Launch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.outlands.foundation.boot.FilteredClassLoader;

/**
 * @author ZZZank
 */
public class FilteredClassLoaderTest {

    @Test
    public void testExclusions() {
        var loader = createLoader();
        try {
            loader.loadClass(Launch.class.getName());
        } catch (ClassNotFoundException e) {
            Assertions.assertSame(FilteredClassLoader.FALL_THROUGH, e);
        }
    }

    @Test
    public void testInclusions() throws ClassNotFoundException {
        var loader = createLoader();
        loader.include(Launch.class.getName());

        loader.loadClass(Launch.class.getName());
    }

    private static FilteredClassLoader createLoader() {
        var loader = new FilteredClassLoader(Thread.currentThread().getContextClassLoader(), null);
        loader.exclude("net.minecraft.");
        return loader;
    }
}
