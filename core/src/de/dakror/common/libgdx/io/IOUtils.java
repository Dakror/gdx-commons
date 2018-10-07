/*******************************************************************************
 * Copyright 2017 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.dakror.common.libgdx.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author Maximilian Stark | Dakror
 */
public class IOUtils {
    public static byte[] gunzip(InputStream is) throws IOException {
        GZIPInputStream gz = new GZIPInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[32000];
        int len = 0;
        while ((len = gz.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        gz.close();
        return baos.toByteArray();
    }

    public static byte[] twiddleBoolArray(boolean[] array) {
        int len = (int) Math.ceil(array.length / 8f);
        byte[] data = new byte[len];
        for (int i = 0; i < array.length; i++)
            if (array[i]) data[i / 8] |= 1 << i % 8;
        return data;
    }

    public static boolean[] getBoolArray(byte[] data) {
        boolean[] bools = new boolean[data.length * 8];
        for (int i = 0; i < bools.length; i++)
            bools[i] = (data[i / 8] & (1 << (i % 8))) != 0;
        return bools;
    }
}
