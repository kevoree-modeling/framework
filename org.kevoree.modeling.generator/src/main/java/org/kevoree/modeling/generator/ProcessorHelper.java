/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * Authors:
 * Fouquet Francois
 * Nain Gregory
 */
package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProcessorHelper {

    public boolean isNull(Object o) {
        return o == null;
    }

    private static ProcessorHelper INSTANCE = null;

    public static ProcessorHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProcessorHelper();
        }
        return INSTANCE;
    }

    public void checkOrCreateFolder(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String toEcoreType(String originalName) {
        if (originalName.equals("String")) {
            return "java.lang.String";
        }
        if (originalName.equals("Bool")) {
            return "java.lang.Boolean";
        }
        if (originalName.equals("Int")) {
            return "java.lang.Integer";
        }
        if (originalName.equals("Long")) {
            return "java.lang.Long";
        }
        if (originalName.equals("Double")) {
            return "java.lang.Double";
        }
        if (originalName.equals("Continuous")) {
            return "java.lang.Double";
        }
        return originalName;
    }


    public boolean isPrimitive(String tDecl) {
        if (toEcoreType(tDecl).startsWith("java.lang.")) {
            return true;
        } else {
            return false;
        }
    }

    public String convertToJavaType(String t) {
        if (isPrimitive(t)) {
            return toEcoreType(t);
        } else {
            return "org.kevoree.modeling.meta.KLiteral";
        }
    }



    public String toCamelCase(String ref) {
        return ref.substring(0, 1).toUpperCase() + ref.substring(1);
    }

}