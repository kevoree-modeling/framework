package org.kevoree.modeling.generator.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by gregory.nain on 07/11/14.
 */
public class HtmlTemplateGenerator {


    public static void generateHtml(Path targetDir, String jsFileName, String metaModelFQN) throws IOException {
        String metaModelName;
        if (metaModelFQN.contains(".")) {
            metaModelName = metaModelFQN.substring(metaModelFQN.lastIndexOf(".") + 1);
        } else {
            metaModelName = metaModelFQN;
            metaModelFQN = metaModelFQN.toLowerCase() + "." + metaModelFQN;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head lang=\"en\">\n    <meta charset=\"UTF-8\">\n    <title></title>\n</head>\n");
        sb.append("<body>\n");
        sb.append("<script src=\"./" + jsFileName + "\"></script>\n");
        sb.append("<script>\n");
        sb.append("    var " + metaModelName.toLowerCase() + "Universe = new " + metaModelFQN + "Universe();\n");
        sb.append("    " + metaModelName.toLowerCase() + "Universe.connect(function(error){\n");
        sb.append("        var " + metaModelName.toLowerCase() + "View = "+metaModelName.toLowerCase()+"Universe.newDimension().time(0);\n");
        sb.append("\n");
        sb.append("        //create your root element from '" + metaModelName.toLowerCase() + "View' ::  var element = " + metaModelName.toLowerCase() + "View.create[...]();\n");
        sb.append("        //set your root element (if necessary) '" + metaModelName.toLowerCase() + "View' ::  " + metaModelName.toLowerCase() + "View.setRoot(<element>);\n");
        sb.append("\n");
        sb.append("        //Load from JSON\n");
        sb.append("        //" + metaModelName.toLowerCase() + "View.json().load(\"Model_As_String\", function(error){console.error(error);});\n");
        sb.append("\n");
        sb.append("        /*  Serialize in JSON:\n");
        sb.append("\n");
        sb.append("         " + metaModelName.toLowerCase() + "View.json().save(<model>, function(serializedModel, error){\n");
        sb.append("         if(!error) {\n");
        sb.append("             //Do something with 'serializedModel'\n");
        sb.append("         } else {\n");
        sb.append("             console.error(error);\n");
        sb.append("         }\n");
        sb.append("         */\n");
        sb.append("    });\n");
        sb.append("\n");
        sb.append("</script>\n");
        sb.append("</body>\n");
        sb.append("</html>");

        if (!Paths.get(targetDir.toAbsolutePath().toString(), "index.html").toFile().exists()) {
            Files.write(Paths.get(targetDir.toAbsolutePath().toString(), "index.html"), sb.toString().getBytes());
        }

        if (System.getProperty("additional") != null) {
            String additional = System.getProperty("additional");
            String[] additionals = additional.split(File.pathSeparator);
            for (String potential : additionals) {
                File file = new File(potential);
                if (file.exists() && file.getName().endsWith(".jar")) {
                    JarFile jar = new JarFile(file);
                    Enumeration<JarEntry> iterator = jar.entries();
                    while (iterator.hasMoreElements()) {
                        JarEntry entry = iterator.nextElement();
                        if (entry.getName().endsWith(".js") || entry.getName().endsWith(".ts")) {
                            Files.copy(jar.getInputStream(entry), Paths.get(targetDir.toAbsolutePath().toString(), entry.getName()), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    jar.close();
                }
            }
        }


    }

}
