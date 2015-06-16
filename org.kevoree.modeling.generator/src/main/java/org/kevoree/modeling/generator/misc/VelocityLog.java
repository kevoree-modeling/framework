package org.kevoree.modeling.generator.misc;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Created by duke on 7/17/14.
 */
public class VelocityLog implements LogChute {

    public static VelocityLog INSTANCE = new VelocityLog();

    @Override
    public void init(RuntimeServices runtimeServices) throws Exception {

    }

    @Override
    public void log(int i, String s) {
    }

    @Override
    public void log(int i, String s, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public boolean isLevelEnabled(int i) {
        return false;
    }

}
