package antonpizzeria;

import java.util.*;

/*
This class contains the code for the Order object.

NOTE: If the order is dine-in, we still pass a customer object to it, but it is not actually saved. The customer will have blank strings for name, address and phone number, and -1 for an id. If it's not dine in, table number should be set to -1. You can add a second constructor for this if you want, which would be better code, but as of right now I just need to get this assignment to you.
*/

public class Order{
	private int ID;
	private Customer cust;
	private int table_num;
	private String order_type; //pick-up, delivery, dine-in
	private ArrayList<Pizza> pizzas;
	private ArrayList<Discount> discounts;
	
	//You pass in the ID, customer, table number and order type.
	//Once the Order is created you can add pizzas and discounts.
	public Order(int i, Customer c, int tn, String type)
	{
		ID = i;
		
		//always compare to the DBNinja variables for order type
		if(type.equals(DBNinja.dine_in))
		{
			table_num = tn;
		}
		else
		{
			table_num = -1;
			cust = c;
		}
		order_type = type;
		
		pizzas = new ArrayList<Pizza>();
		discounts = new ArrayList<Discount>();
	}
	
	public int getID()
	{
		return ID;
	}
	
	public Customer getCustomer()
	{
		return cust;
		
	}
	
	public int getTable()
	{
		return table_num;
	}
	
	public String getType()
	{
		return order_type;
	}
	
	
	public double calcPrice()
	{
		double price = 0.0;
		for(Pizza p : pizzas)
		{
			price += p.calcPrice();
		}
		
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
	
	
	public void addPizza(Pizza p)
	{
		pizzas.add(p);
	}
	
	public void addDiscount( Discount d)
	{
		discounts.add(d);
	}
	
	/*
	Its bad practice to expose the arrays like this, but it will make it easier to convert the order to the format needed for your database and I didn't have the time to go do this properly
	
	Only use these functions to read the lists, never to modify them
	
	*/
	public ArrayList<Pizza> getPizzas()
	{
		return pizzas;
	}
	
	public ArrayList<Discount> getDiscounts()
	{
		return discounts;
	}
	
	//Print the high level info
	public String toSimplePrint()
	{
		String s = "Order Number: " + Integer.toString(ID) + " Type: " + order_type;
		if(order_type.equals(DBNinja.dine_in))
		{
			s += " Table: " + Integer.toString(table_num);
		}
		else 
		{
			s += " Customer: " + cust.getName();
		}
		s+= " Number of Pizzas: " + Integer.toString(pizzas.size()) + " Price: $" + Double.toString(this.calcPrice()) + "\n"; 
		return s;
	}
	
	public String toFullPrint()
	{
		String s = "Order Number: " + Integer.toString(ID) + " Type: " + order_type;
		if(order_type.equals(DBNinja.dine_in))
		{
			s += " Table: " + Integer.toString(table_num);
		}
		else 
		{
			s += "\n" + cust.toPrint();
		}
		s += "\n\nPizzas:\n";
		for (Pizza p: pizzas)
		{
			s += p.toPrint() + "\n";
		}
		
		s += "\nOrder Discounts: \n";
		for (Discount d : discounts)
		{
			s += d.toPrint() + "\n";
		}
		
		s += "\nTotal Order Price: $" + Double.toString(this.calcPrice()) + "\n" ;
		return s;
	}
}