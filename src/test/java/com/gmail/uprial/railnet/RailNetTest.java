package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RailNetTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testLoadException() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("[ERROR] Empty 'underground-railways' flag");
        RailNet.loadConfig(getPreparedConfig(""), getCustomLogger());
    }
}