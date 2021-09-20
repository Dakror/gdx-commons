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

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.scannotation.AnnotationDB;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Patcher {
    public static @interface ModEnum {
        Class<?> value();
    }

    public static void patchEnums(URL[] urls) throws IOException, NotFoundException {
        AnnotationDB db = new AnnotationDB();
        db.scanArchives(urls);

        Set<String> annotations = db.getAnnotationIndex().get(ModEnum.class.getName());

        ClassPool pool = new ClassPool();

        if (annotations == null)
            return;

        for (String s : annotations) {
            CtClass cls = pool.get(s);

        }
    }
}
