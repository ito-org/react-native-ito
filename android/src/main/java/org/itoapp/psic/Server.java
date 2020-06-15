package org.itoapp.psic;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {
    private final ECCommutativeCipher commutativeCipher;

    public Server() {
        this(SecureRandom.getSeed(32));
    }

    public Server(byte[] privateKey) {
        this.commutativeCipher = new ECCommutativeCipher(privateKey);
    }

    /**
     * Creates the setup bloom filter for all the servers elements.
     */
    public String createSetupMessage(double falsePositiveRate,
                                     long nClientInputs,
                                     Set<byte[]> elements) {

        double correctedFalsePositiveRate = falsePositiveRate / nClientInputs;

        BloomFilter bloomFilter = new BloomFilter(correctedFalsePositiveRate, elements.size());

        elements.parallelStream()
                .map(commutativeCipher::hashEncrypt)
                .forEach(bloomFilter::add);

        return GsonSingleton.GSON.toJson(bloomFilter);
    }

    /**
     * Processes the client request.
     * <p>
     * This method decodes all the client's hashed and encrypted elements
     * and re-encrypts them. They are then serialized back to json.
     */
    public String processRequest(String request) {
        RequestResponse requestObject = GsonSingleton.GSON.fromJson(request, RequestResponse.class);

        RequestResponse responseObject = new RequestResponse();

        responseObject.encrypted_elements = requestObject.encrypted_elements.parallelStream()
                .map(commutativeCipher::encryptEncodedPoint)
                .unordered()
                .collect(Collectors.toSet());

        return GsonSingleton.GSON.toJson(responseObject);
    }
}
