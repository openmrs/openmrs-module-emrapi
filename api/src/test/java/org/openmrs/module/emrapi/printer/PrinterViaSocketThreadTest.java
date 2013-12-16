package org.openmrs.module.emrapi.printer;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IOUtils.class)
public class PrinterViaSocketThreadTest {

    private Socket mockedSocket;

    @Before
    public void setup() {
        mockStatic(IOUtils.class);
        mockedSocket = mock(Socket.class);
    }

    @Test
    public void shouldPrintToSocket() throws Exception {

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("9100");

        String testData = "test data";

        PrintViaSocketThread printViaSocketThread = new PrintViaSocketThread(testData, printer, "UTF-8");
        printViaSocketThread.setSocket(mockedSocket);
        Thread thread = new Thread(printViaSocketThread);
        thread.start();
        thread.join();

        verify(mockedSocket).connect(argThat(new IsExpectedSocketAddress("127.0.0.1", "9100")), eq(1000));
        verify(mockedSocket).getOutputStream();

        verifyStatic();
        IOUtils.write(eq(testData), any(OutputStream.class), eq("UTF-8"));
    }

    @Test
    public void shouldPrintToSocketUsingWindowsEncoding() throws Exception {

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("9100");

        String testData = "test data";

        PrintViaSocketThread printViaSocketThread = new PrintViaSocketThread(testData, printer, "Windows-1252");
        printViaSocketThread.setSocket(mockedSocket);
        Thread thread = new Thread(printViaSocketThread);
        thread.start();
        thread.join();

        verify(mockedSocket).connect(argThat(new IsExpectedSocketAddress("127.0.0.1", "9100")), eq(1000));
        verify(mockedSocket).getOutputStream();

        verifyStatic();
        IOUtils.write(eq(testData.getBytes("Windows-1252")), any(OutputStream.class));
    }

    @Test
    public void shouldPrintToMultipleSocketsAndBlock() throws Exception {

        Printer printer = new Printer();
        printer.setIpAddress("127.0.0.1") ;
        printer.setPort("9100");

        String testData1 = "first test data";
        String testData2 = "second test data";

        PrintViaSocketThread printViaSocketThread1 = new PrintViaSocketThread(testData1, printer, "UTF-8", 10000);
        printViaSocketThread1.setSocket(mockedSocket);
        Thread thread1 = new Thread(printViaSocketThread1);
        thread1.start();

        PrintViaSocketThread printViaSocketThread2 = new PrintViaSocketThread(testData2, printer, "UTF-8", 1000);
        printViaSocketThread2.setSocket(mockedSocket);
        Thread thread2 = new Thread(printViaSocketThread2);
        thread2.start();

        // wait for the second thread to finish
        thread2.join();

        // if the synchronization is not working right, thread2 will terminate before thread1 since thread1 has a ten-second delay and thread 2 only has a 1 second delay
        assertThat(thread1.getState(), is(Thread.State.TERMINATED));

        verify(mockedSocket, times(2)).connect(argThat(new IsExpectedSocketAddress("127.0.0.1", "9100")), eq(1000));
        verify(mockedSocket, times(2)).getOutputStream();

        verifyStatic();
        IOUtils.write(eq(testData1), any(OutputStream.class), eq("UTF-8"));
        IOUtils.write(eq(testData2), any(OutputStream.class), eq("UTF-8"));
    }

    private class IsExpectedSocketAddress extends ArgumentMatcher<SocketAddress> {

        private InetSocketAddress expectedSocketAddress;

        public IsExpectedSocketAddress(String ipAddress, String port) throws UnknownHostException {
            InetAddress addr = InetAddress.getByName(ipAddress);
            expectedSocketAddress = new InetSocketAddress(addr, Integer.valueOf(port));
        }

        @Override
        public boolean matches(Object o) {
            InetSocketAddress actualSocketAddress = (InetSocketAddress) o;

            assertThat(actualSocketAddress.getAddress(), is(expectedSocketAddress.getAddress()));
            assertThat(actualSocketAddress.getPort(), is(expectedSocketAddress.getPort()));

            return true;
        }
    }

}
