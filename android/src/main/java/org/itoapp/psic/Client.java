package org.itoapp.psic;

import java.security.SecureRandom;
import java.util.Set;
import java.util.stream.Collectors;


public class Client {
    private final ECCommutativeCipher commutativeCipher;

    /**
     * Create a client with a random private key
     */
    public Client() {
        this(SecureRandom.getSeed(32));
    }

    /**
     * Create a client with the given private key
     */
    public Client(byte[] privateKey) {
        this.commutativeCipher = new ECCommutativeCipher(privateKey);
    }

    /**
     * Create a request with the given elements.
     * <p>
     * The elements are individually hashed and encrypted with the instance's
     * private key. The result is then put into a json object.
     */
    public String createRequest(Set<byte[]> elements) {
        RequestResponse requestObject = new RequestResponse();

        requestObject.encrypted_elements = elements.parallelStream()
                .map(commutativeCipher::hashEncrypt)
                .unordered()
                .collect(Collectors.toSet());

        return GsonSingleton.GSON.toJson(requestObject);
    }

    /**
     * Calculates the cardinality (number of elements) of the set intersection.
     * <p>
     * The setup message is a bloom filter containing the elements hashed and
     * encrypted by the server. The response message is a set containing the
     * elements from the client request re-encrypted by the server. The client
     * now decrypts all elements from the response and tests whether they are
     * contained in the bloom filter.
     * <p>
     * This works because the cipher used by the client and the server is
     * commutative, meaning that a(B(A(h(x))) = B(h(x))
     */
    public int calculateIntersectionCardinality(String setup, String response) {
        BloomFilter bloomFilter = GsonSingleton.GSON.fromJson(setup, BloomFilter.class);
        RequestResponse responseObject = GsonSingleton.GSON.fromJson(response, RequestResponse.class);

        return (int) responseObject.encrypted_elements.parallelStream()
                .map(commutativeCipher::decrypt)
                .filter(bloomFilter::contains)
                .count();
    }
}
