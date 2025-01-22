package org.example.service;

import java.io.IOException;
import java.util.Map;

public interface IconGeneratorService {
    Map<Integer, byte[]> generateIcons(byte[] sourceImageData) throws IOException;
}