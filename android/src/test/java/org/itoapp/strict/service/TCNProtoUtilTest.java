package org.itoapp.strict.service;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static org.itoapp.strict.service.TCNProtoGenTest.REPORTTCNS;
import static org.itoapp.strict.service.TCNProtoGenTest.byte2Hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TCNProtoUtilTest {


    @Test
    public void testVerifySig() throws Exception {
        byte[] report = TCNProtoGenTest.hex2Byte(TCNProtoGenTest.VALIDREPORT);
        assertEquals(true, TCNProtoUtil.verifySignatureOfReportCorrect(report));

    }

    @Test
    public void generateFromTo() throws Exception {
        byte[] report = TCNProtoGenTest.hex2Byte(TCNProtoGenTest.VALIDREPORT);
        List<String> tcns = new LinkedList<>();
        TCNProtoUtil.generateAllTCNsFromReport(report, x ->
                tcns.add(TCNProtoGenTest.byte2Hex(x))
        );
        assertArrayEquals(REPORTTCNS, tcns.toArray());
    }

    @Test // not too much of a unit test
    public void testBounds() throws Exception {
        TCNProtoGen tcnGen = new TCNProtoGen();
        int ulimit = Short.MAX_VALUE * 2;
        List<String> tcnsSource = new LinkedList<>();
        IntStream.range(0, ulimit).forEach(i
                -> tcnsSource.add(byte2Hex(tcnGen.getNewTCN())));

        List<String> tcnRegenerated = new LinkedList<>();
        TCNProtoUtil.generateAllTCNsFromReport(tcnGen.generateReport(tcnGen.getRatchetTickCount()), x ->
                tcnRegenerated.add(TCNProtoGenTest.byte2Hex(x))
        );
        assertEquals(ulimit, tcnRegenerated.size());
        assertArrayEquals(tcnsSource.toArray(),tcnRegenerated.toArray());
    }
}
