package com.checkm8.analysis.ms.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import com.checkm8.analysis.ms.dtos.AnalysisResponsePv;

public class StockfishEngine implements AutoCloseable {

    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    private Integer engineThreads;
    private Integer engineHashMb;
    private Integer engineMultipv;

    public StockfishEngine(String enginePath, Integer engineThreads, Integer engineHashMb, Integer engineMultipv) throws IOException {
        this.process = new ProcessBuilder(enginePath)
            .redirectErrorStream(true)
            .start();

        this.writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));

        this.engineThreads = engineThreads;
        this.engineHashMb = engineHashMb;
        this.engineMultipv = engineMultipv;
        this.init(engineThreads, engineHashMb, engineMultipv);
    }

    public List<AnalysisResponsePv> analyzePosition(String moves, Integer moveTime) throws IOException {

        List<AnalysisResponsePv> response = new ArrayList<>();
        Stack<AnalysisResponsePv> pvsStack = new Stack<>();

        this.send(String.format("position startpos moves %s", moves));
        this.send(String.format("go movetime %d", moveTime));

        while (true) {
            String line = this.reader.readLine();
            if (line == null) throw new IOException("Stockfish process terminated");

            if (line.startsWith("info")) {
                pvsStack.push(this.parseRawInfoLine(line));
            }
            else if (line.startsWith("bestmove")) {
                break;
            }
        }

        if (pvsStack.size() < this.engineMultipv) throw new IOException("Stockfish analysis did not return appropriate ammount of pvs...");
        for (int i = 0; i < this.engineMultipv; i++) response.add(pvsStack.pop());
        Collections.reverse(response);

        return response;
    }

    // ********************************************************************************************

    private AnalysisResponsePv parseRawInfoLine(String line) {

        if (!line.startsWith("info")) throw new RuntimeException("Got invalid info line");
        AnalysisResponsePv response = new AnalysisResponsePv();
        response.rank = 1;

        String[] tokens = line.split(" ");
        for (int i = 0; i < tokens.length; i++) {

            switch (tokens[i]) {
                case "multipv":
                    if (i + 1 < tokens.length) response.rank = Integer.parseInt(tokens[++i]);
                    break;

                case "score":
                    if (i + 2 < tokens.length) {
                        response.scoreType = tokens[++i];
                        response.score = Integer.parseInt(tokens[++i]);
                    }
                    break;

                case "pv":
                    response.pv = List.of(Arrays.copyOfRange(tokens, i + 1, tokens.length));
            }
        }

        return response;
    }

    private void init(Integer engineThreads, Integer engineHashMb, Integer engineMultipv) throws IOException {
        this.send("uci");
        this.waitForExactLine("uciok");

        this.send(String.format("setoption name Threads value %d", engineThreads));
        this.send(String.format("setoption name Hash value %d", engineHashMb));
        this.send(String.format("setoption name MultiPV value %d", engineMultipv));

        this.send("isready");
        this.waitForExactLine("readyok");
    }

    private void send(String command) throws IOException {
        this.writer.write(command);
        this.writer.write("\n");
        this.writer.flush();
    }

    private void waitForExactLine(String expected) throws IOException {
        while (true) {
            String line = this.reader.readLine();
            if (line == null) throw new IOException("Stockfish process terminated");
            if (expected.equals(line)) return;
        }
    }
    private String waitForPrefix(String expectedPrefix) throws IOException {
        while (true) {
            String line = this.reader.readLine();
            if (line == null) throw new IOException("Stockfish process terminated");
            if (line.startsWith(expectedPrefix)) return line;
        }
    }

    @Override
    public void close() throws Exception {
        this.process.destroy();
        this.writer.close();
        this.reader.close();
    }
}
