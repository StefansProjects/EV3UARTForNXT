package stefan;

import lejos.nxt.I2CPort;
import lejos.nxt.LCD;
import lejos.nxt.SensorConstants;
import lejos.nxt.SensorPort;
import lejos.nxt.comm.*;

public class UARTSensorPort extends Thread {
	private I2CPort p;
	UARTSensorBase s;
	private int currBaudrate;
	private static int BufferSize = 800;
	boolean sensorInitalized = false; // Marks whether we are in sensor
										// information or data mode

	private static byte NACK = 0x02;
	private static byte ACK = 0x04;
	private static byte TYPE = 0x40;

	public UARTSensorPort(UARTSensorBase sensor) {
		p = SensorPort.S4;
		s = sensor;
		s.attachPort(this);
	}

	// true = send, false = receive
	private void switchDir(boolean state) {
		if (state == false) {
			p.setType(SensorConstants.TYPE_LOWSPEED);
		} else {
			p.setType(SensorConstants.TYPE_LOWSPEED_9V);
		}

	}

	private void setBaudrate(int baudRate) {
		RS485.hsDisable();
		RS485.hsEnable(baudRate, BufferSize);
		currBaudrate = baudRate;

	}

	private void flushBuffer() {
		byte[] buf = new byte[100];
		int res = 0;
		do {
			res = RS485.hsRead(buf, 0, 100);
		} while (res > 0);

	}

	public void sendData(byte[] buf, int offset, int len)
			throws InterruptedException {
		switchDir(true);

		// Some time necessary to really get the start byte
		// TODO optimize waiting time
		Thread.sleep(5);

		RS485.hsWrite(buf, offset, len);
		// TODO optimize formula for waiting time
		int timeToWait = (1000 * len * 10) / currBaudrate;
		Thread.sleep(timeToWait); // Wait for the bytes to be transmitted
		switchDir(false);
	}

	private int receiveData(byte[] buf, int offset, int len) {
		// No data direction switch necessary because receiving is default
		return RS485.hsRead(buf, offset, len);
	}

	public boolean isInitalized() {
		return sensorInitalized;
	}

	public void run() {

		boolean uartFastSpeed = true; // marks whether the UART is set to low
										// speed (2400 baud) or fast speed (set
										// by sensor) / uninitalized
		byte[] in = new byte[BufferSize];
		byte[] out = new byte[3];
		while (true) {
			if (sensorInitalized == false) {
				if (uartFastSpeed == true) {
					setBaudrate(2400);
					uartFastSpeed = false;
				}
				int header_len = 1;
				int read_res = 0;

				flushBuffer(); // empty the RS485 buffer

				// Receive data and wait until we find 0x40 --> TYPE Command,
				// start byte of the information header transmission
				LCD.drawString("Wait for TYPE", 0, 0);

				do {
					receiveData(in, 0, 1);
				} while (in[0] != TYPE);
				LCD.drawString("TYPE found", 0, 1);

				try {
					Thread.sleep(1200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Data packet took around 1300 ms anyway, Thread can sleep
					// and main programm may continue

				do {
					read_res = receiveData(in, header_len, 1);
					if (read_res > 0) {
						header_len += read_res;
					}
				} while (!((header_len > s.HeaderLen) && (in[header_len - 1] == ACK)));
				// wait for the sensor specific size of the header and the final
				// byte ACK
				LCD.drawString("Read header", 0, 2);
				LCD.drawString("Count: " + Integer.toString(header_len), 0, 3);

				if ((in[header_len - 1] == ACK)
						&& (header_len == s.HeaderLen + 1)) {
					LCD.drawString("Header valid", 0, 4);
					// ACK has to be answered with ACK to switch sensor into
					// Data mode
					out[0] = ACK;
					try {
						sendData(out, 0, 1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Set this state machine to Data mode
					sensorInitalized = true;
					LCD.drawString("Data mode", 0, 5);

					setBaudrate(s.DataBaudrate);
					uartFastSpeed = true;
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						s.setMode((byte) 2);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					
				}
			} else // Sensor is in data mode
			{
				int read_res = 0;
				
				if (uartFastSpeed == false) {
					setBaudrate(s.DataBaudrate);
					uartFastSpeed = true;
				}

				
				//NACK has to be send every 100ms
				try {
					Thread.sleep(80);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				out[0] = NACK;
				try {
					sendData(out, 0, 1);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Receive data and hand it over to the sensor for parsing
				read_res = receiveData(in, 0, 34);
				s.parseData(in, 0, 34);
			}

		}

	}
}
