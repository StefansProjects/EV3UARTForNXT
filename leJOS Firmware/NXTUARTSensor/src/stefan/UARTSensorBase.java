package stefan;

public abstract class UARTSensorBase {
	protected UARTSensorPort p;
	protected int CurrentMode = 0;
	
	public int HeaderLen = 0;
	public int TypeID	= 0;
	public int DataBaudrate = 2400;
	
	
	public abstract int parseHeader(byte[] data, int offset, int maxlen);
	public abstract int parseData(byte[] data, int offset, int maxlen);
	
	public void attachPort(UARTSensorPort port)
	{
		p = port;
	}
	
	public byte calculateChecksum(byte[] data, int offset, int len)
	{
		byte result = (byte) 0xff;
		
		for(int i = offset;i<offset+len;i++)
		{
			result ^= data[i];
		}
		
		return result;
	}
	public abstract void setMode(byte b) throws InterruptedException ;
	
}
