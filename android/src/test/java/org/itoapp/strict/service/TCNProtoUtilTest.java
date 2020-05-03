package org.itoapp.strict.service;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.itoapp.strict.service.TCNProtoGenTest.REPORTTCNS;
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
        assertArrayEquals(REPORTTCNS,tcns.toArray());

    }
}
