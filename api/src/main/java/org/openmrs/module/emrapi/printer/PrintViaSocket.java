package org.openmrs.module.emrapi.printer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Implementing socket printing in a separate thread so that it can be performed asynchronously
 */
public class PrintViaSocket implements Runnable {

    private final Log log = LogFactory.getLog(getClass());

    private Printer printer;

    private String data;

    private Integer wait;

    private String encoding;

    private Socket socket;

    public PrintViaSocket(String data, Printer printer, String encoding) {
        this.printer = printer;
        this.data = data;
        this.encoding = encoding;
    }


    public PrintViaSocket(String data, Printer printer, String encoding, Integer wait) {
        this.printer = printer;
        this.data = data;
        this.encoding = encoding;
        this.wait = wait;
    }

    @Override
    public void run() {

        try {
            printViaSocket();
        }
        catch (UnableToPrintViaSocketException e) {
            throw new RuntimeException("Thread unable to print", e);
        }

    }

    public void printViaSocket() throws UnableToPrintViaSocketException {

        // only allow one call to a printer at time
        synchronized(printer) {

            // Create a socket with a timeout
            try {
                InetAddress addr = InetAddress.getByName(printer.getIpAddress());
                SocketAddress sockaddr = new InetSocketAddress(addr, Integer.valueOf(printer.getPort()));

                // Create an unbound socket
                if (socket == null) {
                    socket = new Socket();
                }

                // This method will block no more than timeoutMs.
                // If the timeout occurs, SocketTimeoutException is thrown.
                int timeoutMs = 1000;   // 1s
                socket.connect(sockaddr, timeoutMs);

                if (encoding.equals("Windows-1252")) {
                    IOUtils.write(data.toString().getBytes("Windows-1252"), socket.getOutputStream());
                } else {
                    IOUtils.write(data.toString(), socket.getOutputStream(), encoding);
                }

                // wait before returning if a wait to specified (we hold lock on printer during that time)
                if (wait != null) {
                    Thread.sleep(wait);
                }

            } catch (Exception e) {
                throw new UnableToPrintViaSocketException("Unable to print to printer " + printer.getName(), e);
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    log.error("failed to close the socket to printer " + printer.getName(), e);
                }
            }
        }

    }

    public void setPrinter(Printer printer) {
        this.printer = printer;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
