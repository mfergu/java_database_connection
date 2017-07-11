package antonpizzeria;

import java.util.*;

/* Your class to hold the pizza*/

public class Pizza
{
	private int ID;
	private String size;
	private String crust;
	private ArrayList<Topping> toppings;
	private ArrayList<Discount> discounts;
	private double base_price;
	
	//pass in id, size, crust, and base_price
	public Pizza(int i, String s, String c, double bp)
	{
		ID = i;
		size = s;
		crust = c;
		toppings = new ArrayList<Topping>();
		discounts = new ArrayList<Discount>();
		base_price = bp;
	}
	
	public int getID()
	{
		return ID;
	}
	
	public String getSize()
	{
		return size;
	}
	
	public String getCrust()
	{
		return crust;
	}
	
	//Use these to add toppings and discounts
	public void addTopping(Topping t)
	{
		toppings.add(t);
	}
	
	public void addDiscount(Discount d)
	{
		discounts.add(d);
	}
	
	public double calcPrice()
	{
		double price = base_price;
		for (Topping t : toppings)
		{
			price += t.getPrice();
			if(t.getExtra())
			{
				price += t.getPrice();
			}
		}
		
		//discounts
		for (Discount d : discounts)
		{
			if(d.percentDiscount())
			{
				price = price * (1 - d.getPercentDisc());
			}
			else
			{
				price = price - d.getCashDisc();
			}
		}
		
		return price;
	}
	
	public String toPrint()
	{
		String s = "Size: " + size + " Crust: " + crust + "\nToppings: \n";
		for (Topping t : toppings)
		{
			if(t.getExtra())
			{
				s = s + " Extra " + t.getName() + ", $" + Double.toString(t.getPrice() * 2 )+ "\n";
			}
			else
			{
				s = s + t.getName() + ", $" + Double.toString(t.getPrice() )+ "\n";
			}
		}
		s += "\n Discounts:";
		
		for (Discount d : discounts)
		{
		
			s += d.toPrint() + "\n";
		}
		s += "\nPrice: $" + Double.toString(this.calcPrice()) + "\n" ;
		
		return s;
	}
	
	/*
	Its bad practice to expose the arrays like this, but it will make it easier to convert the order to the format needed for your database and I didn't have the time to go do this properly
	
	Only use these functions to read the lists, never to modify them
	
	*/
	public ArrayList<Topping> getToppings()
	{
		return toppings;
	}
	
	public ArrayList<Discount> getDiscounts()
	{
		return discounts;
	}
	
	
}