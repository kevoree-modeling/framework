/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors:
 * Fouquet Francois
 * Nain Gregory
 */
package org.kevoree.modeling.generator;

import org.kevoree.modeling.ast.*;
import org.kevoree.modeling.idea.psi.MetaModelTypeDeclaration;
import org.kevoree.modeling.util.PrimitiveTypes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Gregory NAIN
 * Date: 22/09/11
 * Time: 09:49
 */

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

    public boolean isPrimitive(MetaModelTypeDeclaration tDecl) {
        return isPrimitive(tDecl.getName());
    }

    public boolean isPrimitive(String tDecl) {
        return PrimitiveTypes.isPrimitive(tDecl);
    }

    public boolean isEnum(GenerationContext context, MetaModelTypeDeclaration tDecl) {
        MModelClassifier resolved = context.getModel().get(tDecl.getName());
        return resolved != null && resolved instanceof MModelEnum;
    }


    public String convertToJavaType(String t) {
        return PrimitiveTypes.toEcoreType(t);
    }

    public void consolidate(MModel model) {
        for (MModelClass decl : model.getClasses()) {
            internal_consolidate(decl);
        }
    }

    private void internal_consolidate(MModelClass classRelDecls) {
        int globalIndex = 0;
        for (MModelAttribute att : classRelDecls.getAttributes()) {
            att.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelReference ref : classRelDecls.getReferences()) {
            ref.setIndex(globalIndex);
            globalIndex++;
        }
        for (MModelOperation op : classRelDecls.getOperations()) {
            op.setIndex(globalIndex);
            globalIndex++;
        }
    }

    public String toCamelCase(String ref) {
        return ref.substring(0, 1).toUpperCase() + ref.substring(1);
    }

}