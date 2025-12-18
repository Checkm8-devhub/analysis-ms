package com.checkm8.analysis.ms.dtos;

import java.util.List;

public class AnalysisBeanResponse {

    public Integer depth;
    public List<AnalysisResponsePv> pvs;

    public static class AnalysisResponsePv {
        public Integer rank;
        public List<String> pv;
        public String scoreType; // cp | mate
        public Integer score;
    }
}
