package org.kevoree.modeling.microframework.typescript;

import de.flapdoodle.embed.nodejs.NodejsConfig;
import de.flapdoodle.embed.nodejs.NodejsExecutable;
import de.flapdoodle.embed.nodejs.NodejsProcess;
import de.flapdoodle.embed.nodejs.NodejsRuntimeConfigBuilder;
import de.flapdoodle.embed.nodejs.NodejsStarter;
import de.flapdoodle.embed.nodejs.NodejsVersion;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class NoJs_MainTest {

    private static int launchRunner(String file) {

        IRuntimeConfig runtimeConfig = (new NodejsRuntimeConfigBuilder()).defaults().build();
        NodejsProcess node = null;
        try {
            String basePath = NoJs_MainTest.class.getClassLoader().getResource(file).getFile().replaceAll("%20", " ");
            NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.Main.V0_10, basePath, new ArrayList<String>(), basePath.toString().substring(0, basePath.toString().lastIndexOf("/")));
            NodejsStarter runtime = new NodejsStarter(runtimeConfig);
            NodejsExecutable e = runtime.prepare(nodejsConfig);
            node = e.start();
            return node.waitFor();
        } catch (InterruptedException var11) {
            var11.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (node != null) {
                node.stop();
            }
        }
        return -1;
    }


    @Test
    public void baseTest() {
        Assert.assertEquals(0, launchRunner("MyTestRunner.js"));
    }


}
