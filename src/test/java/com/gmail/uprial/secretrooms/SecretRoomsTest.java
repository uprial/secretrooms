package com.gmail.uprial.secretrooms;

import com.gmail.uprial.secretrooms.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SecretRoomsTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testLoadException() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("[ERROR] Empty 'distance-density-multiplier' value");
        SecretRooms.loadConfig(getPreparedConfig(""), getCustomLogger());
    }
}