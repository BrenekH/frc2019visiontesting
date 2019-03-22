package visiontesting.rioclient;

import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import com.google.common.primitives.Bytes;

class CustomReader {

    InputStreamReader reader;

    public CustomReader(InputStreamReader readeri) {
        this.reader = readeri;
    }
    Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public String read() throws IOException {
        LinkedList<Byte> theArray = new LinkedList<Byte>();

        // Waiting for the client to send data
        while (!this.reader.ready()) {}

        // Add the client's data to a MutableList to provide for differences in buffer size
        while (this.reader.ready()) {
            theArray.add((byte) reader.read());
        }

        // Convert the MutableList to a decodable ByteArray
        byte[] theByteArray = Bytes.toArray(theArray);
        // for (int i; i < theArray.size(); i++) {
        //     theByteArray[i] = theArray[i];
        // }
        return decodeUTF8(theByteArray);
    }

    public void close() throws IOException {
        this.reader.close();
    }

    public String decodeUTF8(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }
}