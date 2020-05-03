package org.itoapp.strict.service;

import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TCNProtoGenTest {


    public static final String VALIDREPORT = "fd8deb9d91a13e144ca5b0ce14e289532e040fe0bf922c6e3dadb1e4e2333c78df535b90ac99bec8be3a8add45ce77897b1e7cb1906b5cff1097d3cb142fd9d002000a00000c73796d70746f6d206461746131078ec5367b67a8c793b740626d81ba904789363137b5a313419c0f50b180d8226ecc984bf073ff89cbd9c88fea06bda1f0f368b0e7e88bbe68f15574482904";


    public static final String[] REPORTTCNS = new String[]{
            "f4350a4a33e30f2f568898fbe4c4cf34",
            "135eeaa6482b8852fea3544edf6eabf0",
            "d713ce68cf4127bcebde6874c4991e4b",
            "5174e6514d2086565e4ea09a45995191",
            "ccae4f2c3144ad1ed0c2a39613ef0342",
            "3b9e600991369bba3944b6e9d8fda370",
            "dc06a8625c08e946317ad4c89e6ee8a1",
            "9d671457835f2c254722bfd0de76dffc",
            "8b454d28430d3153a500359d9a49ec88"};

    /**
     * Test Generate Report ~henry's vector from rust
     */
    @Test
    public void testReportVector() throws Exception {
        String rak = "577cfdae21fee71579211ab02c418ee0948bacab613cf69d0a4a5ae5a1557dbb";
        TCNProtoGen demo = new TCNProtoGen(TCNProtoGen.TCN_ID_COEPI);
        demo.rak = hex2Byte(rak);
        demo.genRVKandTck0();
        List<String> tcns = new LinkedList<>();
        IntStream.range(0, 9).forEach(i
                -> tcns.add(byte2Hex(demo.getNewTCN()))
        );
        assertArrayEquals(REPORTTCNS, tcns.toArray());
        assertEquals(VALIDREPORT, byte2Hex(demo.generateReport(8)));
    }


    @Test
    public void testReport() {
        TCNProtoGen tcnGen = new TCNProtoGen();
        tcnGen.getNewTCN();
        tcnGen.generateReport(0);
        tcnGen.generateReport(1);
        tcnGen.generateReport(2);
    }

    @Test(expected = RuntimeException.class)
    public void testReportNotNegative() {
        TCNProtoGen tcnGen = new TCNProtoGen();
        tcnGen.getNewTCN();

        tcnGen.generateReport(-1);

    }

    @Test
    public void testReportSizing() {
        // "public report" tck start at 0x2 (first tck to follow tck0) in count = 2

        String rak = "577cfdae21fee71579211ab02c418ee0948bacab613cf69d0a4a5ae5a1557dbb";
        TCNProtoGen tcnGen = new TCNProtoGen(TCNProtoGen.TCN_ID_COEPI);
        tcnGen.rak = hex2Byte(rak);
        tcnGen.genRVKandTck0();


        String r0 = "fd8deb9d91a13e144ca5b0ce14e289532e040fe0bf922c6e3dadb1e4e2333c78df535b90ac99bec8be3a8add45ce77897b1e7cb1906b5cff1097d3cb142fd9d002000200000c73796d70746f6d2064617461400c2b0049c2345c2f91385ab053db5605cf9e8910348efbcfcc67dc505454d93579792742aab9a4343243bb6595c1d2ae6b824daa18eb4e7b64ce45e3b1260b";
        String r1 = "fd8deb9d91a13e144ca5b0ce14e289532e040fe0bf922c6e3dadb1e4e2333c78df535b90ac99bec8be3a8add45ce77897b1e7cb1906b5cff1097d3cb142fd9d002000300000c73796d70746f6d206461746110c6d4c0ed3033c41ba85315758aa9aed18db9d142fc40d408f3df3095357f3daacb36cc76da8da841ef22cd785b4ab9f4e2277013e88738d253ef7ea9965509";
        String r2 = "fd8deb9d91a13e144ca5b0ce14e289532e040fe0bf922c6e3dadb1e4e2333c78df535b90ac99bec8be3a8add45ce77897b1e7cb1906b5cff1097d3cb142fd9d002000400000c73796d70746f6d2064617461046abf5b87d61b29c498b0cf7976a9132ed14046656f36c14c7336c3f9130fc4267015560c3a6564d24c56cfa5c1350690026818e36d6fba20771f9e41954c03";
        String r3 = "fd8deb9d91a13e144ca5b0ce14e289532e040fe0bf922c6e3dadb1e4e2333c78df535b90ac99bec8be3a8add45ce77897b1e7cb1906b5cff1097d3cb142fd9d002000500000c73796d70746f6d20646174614f82be44f2b6dcf98732ff05a302c0da13e35a14ad610c4fd8fbbfa33d7f969ac744c85a9adc223749bafb5ab2db4030042ef4c9b599a358050d5c2cce49e905";


        tcnGen.getNewTCN();
        assertEquals(tcnGen.getRatchetTickCount(), 1);
        assertEquals(r0, byte2Hex(tcnGen.generateReport(tcnGen.getRatchetTickCount())));
        tcnGen.getNewTCN();
        assertEquals(tcnGen.getRatchetTickCount(), 2);
        assertEquals(r1, byte2Hex(tcnGen.generateReport(tcnGen.getRatchetTickCount())));
        tcnGen.getNewTCN();
        assertEquals(tcnGen.getRatchetTickCount(), 3);
        assertEquals(r2, byte2Hex(tcnGen.generateReport(tcnGen.getRatchetTickCount())));
        tcnGen.getNewTCN();
        //getRatchetTickCount()
        // fixme
        assertEquals(r3, byte2Hex(tcnGen.generateReport(tcnGen.getRatchetTickCount())));

    }


    public static byte[] hex2Byte(String in) {
        byte[] ba = new BigInteger(in, 16).toByteArray();
        return Arrays.copyOfRange(ba,1,ba.length);
    }

    public static String byte2Hex(byte[] in) {
        String s = "";
        for (byte b : in) {
            String st = String.format("%02X", b).toLowerCase();
            s += st;
        }
        return s;
    }

}
