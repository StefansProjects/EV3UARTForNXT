package stefan;

import lejos.nxt.Button;
import lejos.nxt.LCD;

public class NXTUARTSensor {
	private static UARTSensorPort sens;
	private static EV3ColorSensor col;
	
	public static void main(String[] args) throws Exception {
		col = new EV3ColorSensor();
		sens = new UARTSensorPort(col);
		sens.start();
		
		
		//col.setMode((byte) 2); //Mode COL_COLOR;
		while(Button.ENTER.isDown() == false)
		{
			
		}

	}
}