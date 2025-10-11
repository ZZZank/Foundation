package net.minecraft.launchwrapper;

import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.ClassLoadingRules;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;

public class LaunchClassLoader extends ActualClassLoader {
    private Set<String> classLoaderExceptions = new HashSet<String>();
    private Set<String> transformerExceptions = new HashSet<String>();
    /**
     * FoamFix (and many other mods) are still using these even some of them have long gone from upstream
     */
    private Map<Package, Manifest> packageManifests = null;
    private static Manifest EMPTY = new Manifest();
    private final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    private final Set<String> invalidClasses = new HashSet<>(0);

    private final Map<String,byte[]> resourceCache = new ConcurrentHashMap<>(0);
    private final Set<String> negativeResourceCache = ConcurrentHashMap.newKeySet();
    public LaunchClassLoader(URL[] sources) {
        super(sources, LaunchClassLoader.class.getClassLoader());
        Launch.classLoader = this;
    }
    
    public LaunchClassLoader(ClassLoader loader) {
        super(getUCP(), loader);
        Launch.classLoader = this;
    }
    
    private static URL[] getUCP(){
        String[] classpaths = System.getProperty("java.class.path").split(File.pathSeparator);
        List<URL> urls = new ArrayList<>();
        try {
            for (String classpath : classpaths) {
                urls.add(new File(classpath).toURI().toURL());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls.toArray(new URL[0]);
    }

    protected void addParentLoadingRules(ClassLoadingRules rules) {
        rules.exclude("org.objectweb.asm.");
        rules.exclude("org.spongepowered.asm.");
        rules.exclude("com.llamalad7.mixinextras.");
        rules.exclude("net.minecraft.");
        rules.exclude("top.outlands.foundation.");
        rules.exclude("org.lwjgl.");
        rules.exclude("com.cleanroommc.");
        rules.exclude("ibxm.");
        rules.exclude("paulscode.sound.codecs.");
        rules.exclude("zone.rong.mixinbooter.");
        rules.exclude("paulscode.sound.");

        rules.include("net.minecraft.launchwrapper.LaunchClassLoader");
        rules.include("net.minecraft.launchwrapper.Launch");
        rules.include("top.outlands.foundation.boot.");
        rules.include("top.outlands.foundation.function.");
        rules.include("top.outlands.foundation.trie.");
        rules.include("net.minecraftforge.server.terminalconsole.");

        addTransformerExclusion("org.spongepowered.asm.bridge.");
        addTransformerExclusion("org.spongepowered.asm.lib.");
        addTransformerExclusion("org.spongepowered.asm.launch.");
        addTransformerExclusion("org.spongepowered.asm.logging.");
        addTransformerExclusion("org.spongepowered.asm.mixin.");
        addTransformerExclusion("org.spongepowered.asm.obfuscation.");
        addTransformerExclusion("org.spongepowered.asm.service.");
        addTransformerExclusion("org.spongepowered.asm.transformers.");
        addTransformerExclusion("org.spongepowered.asm.util.");
        addTransformerExclusion("org.spongepowered.include.com.google.");
        addTransformerExclusion("org.spongepowered.tools.");
        addTransformerExclusion("com.llamalad7.mixinextras.");
    }

    /**
     * CCL is calling this
     */
    public byte[] runTransformers(final String name, final String transformedName, byte[] basicClass) {
        return super.runTransformers(name, transformedName, basicClass);
    }
    
}
