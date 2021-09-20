/**
 * Copyright 2021 Dakror Games
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dakror.common.libgdx.modding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModLoader extends URLClassLoader {
    private ClassLoader parent;
    private Map<String, byte[]> classes = new HashMap<>();
    private Map<String, Class<?>> definedClasses = new HashMap<>();
    private Map<String, byte[]> resources = new HashMap<>();
    String mainClass;

    public ModLoader(String mainClass, String mainJarPath, URL[] urls, ClassLoader parent) throws IOException {
        super(urls, null);

        this.mainClass = mainClass;
        this.parent = parent;
        JarInputStream is = new JarInputStream(new File(mainJarPath).toURI().toURL().openStream());
        JarEntry entry = is.getNextJarEntry();
        while (entry != null) {
            if (entry.getName().contains(".class")) {
                String className = entry.getName().replace(".class", "").replace('/', '.');
                byte[] classBytes = bufferStream(is);
                classes.put(className, classBytes);
            } else if (!entry.isDirectory()) {
                byte[] bytes = bufferStream(is);
                resources.put(entry.getName(), bytes);
            }
            entry = is.getNextJarEntry();
        }
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if (resources.containsKey(name)) {
            return new ByteArrayInputStream(resources.get(name));
        }
        return super.getResourceAsStream(name);
    }

    private byte[] bufferStream(InputStream is) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int nextValue = is.read();
        while (nextValue != -1) {
            byteStream.write(nextValue);
            nextValue = is.read();
        }
        return byteStream.toByteArray();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(mainClass)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = findClass(name);
                if (c == null) {
                    c = super.loadClass(name);
                }
            }
            return c;
        } else {
            try {
                return parent.loadClass(name);
            } catch (ClassNotFoundException e) {
                return super.loadClass(name);
            }
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> ret;
        try {
            ret = super.findClass(name);
        } catch (ClassNotFoundException e) {
            ret = definedClasses.get(name);
            if (ret == null) {
                byte[] classBytes = classes.remove(name);
                if (classBytes == null)
                    throw new ClassNotFoundException(name);
                ret = defineClass(name, classBytes, 0, classBytes.length, (ProtectionDomain) null);
                definedClasses.put(name, ret);
            }
        }
        return ret;
    }

    public void start(String[] args) throws Exception {
        Patcher.patchEnums(getURLs());

        loadClass(mainClass).getDeclaredMethod("main", String[].class).invoke(null, (Object) args);
    }
}