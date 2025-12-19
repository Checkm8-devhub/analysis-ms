package com.checkm8.analysis.ms.beans;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.checkm8.analysis.ms.dtos.AnalysisResponsePv;
import com.checkm8.analysis.ms.utils.StockfishEngine;

@ApplicationScoped
public class AnalysisBean {

    private Logger log = Logger.getLogger(AnalysisBean.class.getName());
    private StockfishEngine engine;

    @ConfigProperty(name = "engine.stockfish.path", defaultValue = "../engine/stockfish")
    private String enginePath;
    @ConfigProperty(name = "engine.stockfish.threads", defaultValue = "1")
    private Integer engineThreads;
    @ConfigProperty(name = "engine.stockfish.hashMb", defaultValue = "128")
    private Integer engineHashMb;
    @ConfigProperty(name = "engine.stockfish.multipv", defaultValue = "3")
    private Integer engineMutlipv;

    @PostConstruct
    private void init() {
        log.info("Bean initialized " + AnalysisBean.class.getSimpleName());
    }
    @PreDestroy
    private void destroy() {
        log.info("Bean destroyed " + AnalysisBean.class.getSimpleName());
        try {
            this.engine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Object engineLock = new Object();
    public List<List<AnalysisResponsePv>> analyzeUcis(List<String> uciList, int moveTime) throws IOException {
        synchronized (engineLock) {

            if (this.engine == null) 
                this.engine = new StockfishEngine(this.enginePath, this.engineThreads, this.engineHashMb, this.engineMutlipv);
            List<List<AnalysisResponsePv>> response = new ArrayList<>();

            for (int i = 0; i < uciList.size(); i++) {
                String currentUcis = String.join(" ", uciList.subList(0, i + 1));
                response.add(this.engine.analyzePosition(currentUcis, moveTime));
            }

            return response;
        }
    }
}
