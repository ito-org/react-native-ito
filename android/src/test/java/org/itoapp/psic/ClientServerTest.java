package org.itoapp.psic;

import org.junit.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;

public class ClientServerTest {

    private static Set<byte[]> generateRandomSet(int setSize, int elementSize, Random random) {
        Set<byte[]> set = new HashSet<>(setSize);

        for (int i = 0; i < setSize; i++) {
            byte[] elementBytes = new byte[elementSize];
            random.nextBytes(elementBytes);
            set.add(elementBytes);
        }
        return set;
    }

    @Test
    public void testClientServerCommunication() {
        // fixed seed because we don't want "a small percentage" of tests to randomly fail
        Random random = new Random(12345);

        byte[] clientPrivateKey = new byte[32];
        byte[] serverPrivateKey = new byte[32];

        random.nextBytes(clientPrivateKey);
        random.nextBytes(serverPrivateKey);

        Client client = new Client(clientPrivateKey);
        Server server = new Server(serverPrivateKey);

        Set<byte[]> clientSet = generateRandomSet(100, 16, random);
        Set<byte[]> serverSet = generateRandomSet(10000, 16, random);

        serverSet.stream().limit(10).forEach(clientSet::add);

        String setup = server.createSetupMessage(0.001, clientSet.size(), serverSet);
        String request = client.createRequest(clientSet);
        String response = server.processRequest(request);

        assertEquals(10, client.calculateIntersectionCardinality(setup, response));
    }
}
