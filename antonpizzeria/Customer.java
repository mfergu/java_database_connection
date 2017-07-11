package antonpizzeria;

// Your customer class. SHould not need modifications unless you do not have an ID field
public class Customer{
	private String name;
	private String address;
	private int ID;
	private String phone;
	
	public Customer(String n, String a, String p, int i)
	{
		name = n;
		address = a;
		phone = p;
		ID = i;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public String getPhone()
	{
		return phone;
	}
	
	public int getID()
	{
		return ID;
	}
	
	public String toPrint()
	{
		String s = "";
		if(address.equals(""))
		{
			s += "Customer: " + name + "Phone: " + phone;
		}
		else
		{
			s += "Customer: " + name + "Phone: " + phone + "Address: " + address;
		}
		return s;
	}
}