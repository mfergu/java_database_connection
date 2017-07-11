package antonpizzeria;

/*
Your topping class. It should not need modification unless you don't have an ID for your toppings in your database
*/

public class Topping{
	private String name;
	private double price;
	private double inventory;
	private int ID;
	private boolean extra;
	
	
	public Topping(String n, double p, double inv, int i)
	{
		name = n;
		price = p;
		inventory = inv;
		ID = i;
		extra = false;
	}
	
	public String getName()
	{
		return name;
	}
	
	public double getPrice()
	{
		return price;
	}
	
	public double getInv()
	{
		return inventory;
	}
	
	public int getID()
	{
		return ID;
	}
	
	//extra is just a boolean value
	public boolean getExtra()
	{
		return extra;
	}
	
	public void makeExtra()
	{
		extra = true;
	}
	


}