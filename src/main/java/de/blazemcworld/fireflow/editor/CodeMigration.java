package de.blazemcworld.fireflow.editor;

import net.minestom.server.network.NetworkBuffer;

import java.util.function.Consumer;

public enum CodeMigration {
    STRUCTS(2, buffer -> buffer.write(NetworkBuffer.INT, 0)),
    FUNCTIONS(1, buffer -> buffer.write(NetworkBuffer.INT, 0)),
    ;

    private final int ver;
    private final Consumer<NetworkBuffer> migrator;
    CodeMigration(int ver, Consumer<NetworkBuffer> migrator) {
        this.ver = ver;
        this.migrator = migrator;
    }

    // called in order!
    public NetworkBuffer apply(int ver, int length, NetworkBuffer buffer) {
        if (ver >= this.ver) return buffer;
        NetworkBuffer newBuffer = new NetworkBuffer();
        int readIndex = buffer.readIndex();
        newBuffer.readIndex(readIndex);
        newBuffer.write(NetworkBuffer.RAW_BYTES, buffer.readBytes(readIndex));
        migrator.accept(newBuffer);
        newBuffer.write(NetworkBuffer.RAW_BYTES, buffer.readBytes(length - readIndex));
        return newBuffer;
    }
}
