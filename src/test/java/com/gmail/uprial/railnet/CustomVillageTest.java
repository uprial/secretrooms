package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.junit.Test;

public class CustomVillageTest extends TestConfigBase {
    @Test
    public void testLoadException() throws Exception {
        RailNet.loadConfig(getPreparedConfig(""), getCustomLogger());
    }
}