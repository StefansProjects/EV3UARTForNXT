package stefan;

import lejos.nxt.LCD;

public class EV3ColorSensor extends UARTSensorBase {
	public enum SensorModes {
		COL_REFLECT, COL_AMBIENT, COL_COLOR, REF_RAW, RGB_RAW, COL_CAL
	}

	EV3ColorSensor() {
		HeaderLen = 310;
		TypeID = 0x1d;
		DataBaudrate = 57600;
	}

	@Override
	public int parseHeader(byte[] data, int offset, int maxlen) {
		// TODO parse and check all informations and corresponding checksums
		// send by the sensor. Return amount of data actually parsed or -1
		if (data[1] == TypeID)
			return HeaderLen;
		return -1;

	}

	private int parseColor(byte[] data, int offset, int maxlen)
	{
		//TODO check command byte and checksum and not only print result to LCD
		switch(data[1])
		{
		case 0:
			LCD.drawString("none   ",0,7);
			break;
		case 1:
			LCD.drawString("black  ",0,7);
			break;
		case 2:
			LCD.drawString("blue   ",0,7);
			break;
		case 3:
			LCD.drawString("green  ",0,7);
			break;
		case 4:
			LCD.drawString("yellow ",0,7);
			break;
		case 5:
			LCD.drawString("red    ",0,7);
			break;
		case 6:
			LCD.drawString("white  ",0,7);
			break;
		case 7:
			LCD.drawString("brown  ",0,7);
			break;
		default:
			LCD.drawString("unkown ",0,7);
			break;
		}
		return 3;
	}
	
	@Override
	public int parseData(byte[] data, int offset, int maxlen) {
		//TODO check if first byte is really a data message byte
		
		switch(CurrentMode)
		{
		case 0:  //MODE COL_REFLECT
			//TODO implement;
			break;
		case 1: //MODE COL_AMBIENT
			//TODO implement;
			break;
		case 2: //Mode COL-COLOR
			return parseColor(data, offset, maxlen);
		}
		
		return 3;
	}

	public void setMode(byte i) throws InterruptedException {
		CurrentMode = i;
		byte[] msg = new byte[4];
		msg[0] = 0x43; // SELECT command
		msg[1] = i;
		msg[2] = calculateChecksum(msg, 0, 2);
		p.sendData(msg, 0, 3);

	}

}
