

import java.io.*;
import java.util.*;
import java.sql.*;
import java.util.*;
import antonpizzeria.*;

/* This is your interface file. Note: I have not fully tested this yet, as time has been short. Hopefully there are no major errors in it, and you should not need to make any major changes to this file.

Also note, there is currently little to no data validation happening in this code, so when you run it, choose your inputs carefully. Do not just test it on meaningless input, as it will affect your database.

The rest of the code is in the antonpizzeria package. This is compiled separately to keep the interface separate from the rest of the code

YOU DO NEED TO EDIT THE PROMPT FOR THE CUSTOMERS PHONE NUMBER, TO LET ME KNOW WHAT FORMAT IT SHOULD BE (with dashes, or without, etc.)
 */

public class Menu{
	
	
	public static void main(String[] args) throws SQLException, IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Welcome to Antons Pizzeria!\n");
		
		int menu_option = 0;
		
		//present a menu of options and take their selection
		PrintMenu();
		String option = reader.readLine();
		menu_option = Integer.parseInt(option);
			
			
		while(menu_option != 7)
		{
			if (menu_option == 1)
			{
				EnterOrder();
				
			}
			else if(menu_option == 2)
			{
				EnterCustomer();
			}
			else if(menu_option == 3)
			{
				ViewOrders();
			}
			else if(menu_option == 4)
			{
				MarkOrderAsComplete();
			}
			else if(menu_option == 5)
			{
				ViewInventoryLevels();
			}
			else if(menu_option == 6)
			{
				AddInventory();
			}
			PrintMenu();
			option = reader.readLine();
			menu_option = Integer.parseInt(option);
		}
		
	}
	
	public static void PrintMenu()
	{
		System.out.println("\n\n\nPlease enter a menu option:");
		System.out.println("1. Enter a new order");
		System.out.println("2. Enter a new Customer ");
		System.out.println("3. View current orders");
		System.out.println("4. Mark an order as completed");
		System.out.println("5. View Inventory Levels");
		System.out.println("6. Add Inventory");
		System.out.println("7. Exit\n\n");
		System.out.println("Enter your option: ");
		
		
	}
	
	//allow for a new order to be placed
	public static void EnterOrder() throws SQLException, IOException
	{
		System.out.println("Is this order for: \n1.) Dine-in\n2.) Pick-up\n3.) Delivery\nEnter the number of your choice:");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int answer = Integer.parseInt(reader.readLine());
		
		String order_type = "";
		int table_num = -1;
		Customer newCust;
		
		if(answer == 1)
		{
			//Dine-in, get table number
			order_type = DBNinja.dine_in;
			System.out.println("Enter a table number: ");
			table_num = Integer.parseInt(reader.readLine());
			//need a value for newCust even though we will ignore it remember that this happens when you save the order, and do not allow our empty customer to be saved in the database
			newCust = new Customer("", "", "", -1);
			
		}
		else
		{
			//need customer info
			System.out.println("Is this order for an existing customer? Answer Y/N: ");
		
			String yn = reader.readLine();
			if(!(yn.equals("Y") || yn.equals("y")))
			{
				System.out.println("Create a new customer:");
				EnterCustomer();
			}
		
			//Now that customer exists, display and have them choose a customer
			
			ArrayList<Customer> custs = DBNinja.getCustomerList();
			boolean needsCust = true;
			int cust_no = 1; 
			while(needsCust)
			{
				int c_count = 1;
				for (Customer c : custs)
				{
					System.out.println(Integer.toString(c_count) + ".) " + c.toPrint());
					c_count++;
				}
		
				System.out.println("Please select a customer by their number: ");
				cust_no = Integer.parseInt(reader.readLine());
				if(cust_no <= custs.size())
				{
					//make a copy to avoid aliasing issues
					
					needsCust = false;
			
				}
				else
				{
					System.out.println("Not a valid entry: Please select again");
				
				}
			}
			newCust = new Customer(custs.get(cust_no - 1).getName(), custs.get(cust_no - 1).getAddress(), custs.get(cust_no - 1).getPhone(), custs.get(cust_no - 1).getID());
			
			//still need to set order_type
			if(answer == 2)
			{
				order_type = DBNinja.pickup;
			}
			else
			{
				order_type = DBNinja.delivery;
			}
		}
		Order newOrder = new Order(-1, newCust, table_num, order_type);
		
		//Add pizzas to orders
		String more = "Y";
		while(more.equals("Y") || more.equals("y"))
		{
			newOrder.addPizza(buildPizza());
			System.out.println("Would you like to add another Pizza? Enter Y/N: ");
			more = reader.readLine();
		}
		
		//add discounts
		System.out.println("Would you like to add a discount to this order? Enter Y/N: ");
		more = reader.readLine();
		while(more.equals("Y") || more.equals("y"))
		{
		
		
			// add discounts
			int chosen_d = 0;
			ArrayList<Discount> discs = DBNinja.getDiscountList();
			int d_count = 1;
			for(Discount d: discs)
			{
				System.out.println(Integer.toString(d_count) + ".) " + d.toPrint());
				d_count++;
			}
				
			System.out.println("Which discount do you want to add? Enter the number. ");
			
			chosen_d = Integer.parseInt(reader.readLine());
			if(chosen_d <= discs.size())
			{
				//make copy to avoid aliasing issues
				Discount newD = new Discount(discs.get(chosen_d-1).getName(), discs.get(chosen_d-1).getPercentDisc(), discs.get(chosen_d-1).getCashDisc(), discs.get(chosen_d-1).getID());
				newOrder.addDiscount(newD);
			}
			else
			{
				System.out.println("Incorrect entry, not an option");
			}
			
			System.out.println("Would you like to add a discount to this order? Enter Y/N: ");
			more = reader.readLine();	
		}
		
		//Order is now complete with pizzas and discounts. Save it to the database
		DBNinja.addOrder(newOrder);
		
		
	}
	
	
	//Enter a new customer in the database
	public static void EnterCustomer() throws SQLException, IOException
	{
		System.out.println("Please Enter the Customer name: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String name = reader.readLine();
		System.out.println("Do you have an Address for this customer? Enter Y/N");
		String option = reader.readLine();
		String address = "";
		if(option.equals("y") || option.equals("Y"))
		{
			System.out.println("Please enter the address: ");
			address = reader.readLine();
		}
		/**********************************************************************************
		
		
		TELL ME WHAT FORMAT TO USE FOR THE PHONE NUMBER IN THE PROMPT
		THIS DEPENDS ON THE FORMAT YOU HAVE IN YOUR DATABASE
		
		
		***********************************************************************************/
		System.out.println("Please Enter the Customer phone number: ");
		String phone = reader.readLine();
		
		Customer new_cust = new Customer(name, address, phone, -1);
		DBNinja.addCustomer(new_cust);
		
	}
	
	//View any orders that are not marked as completed
	public static void ViewOrders() throws SQLException, IOException
	{
		ArrayList<Order> currOrders = DBNinja.getCurrentOrders();
		
		//Print off high level information about the order
		int o_count = 1;
		for (Order o : currOrders)
		{
			System.out.println(Integer.toString(o_count) + ": " + o.toSimplePrint());
			o_count++;
		}
		
		// User can now select an order and get the full detail
		System.out.println("Which order would you like to see in detail? Enter the number: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int chosen_order = Integer.parseInt(reader.readLine());
		if(chosen_order <= currOrders.size())
		{
			System.out.println(currOrders.get(chosen_order-1).toFullPrint());
		}
		else
		{
			System.out.println("Incorrect entry, not an option");
		}
		
	}
	
	//When an order is completed, we need to make sure it is marked as complete
	public static void MarkOrderAsComplete() throws SQLException, IOException
	{
		ArrayList<Order> currOrders = DBNinja.getCurrentOrders();
		int o_count = 1;
		//see all open orders
		for (Order o : currOrders)
		{
			System.out.println(Integer.toString(o_count) + ": " + o.toSimplePrint());
			o_count++;
		}
		
		//pick the order to mark as completed
		System.out.println("Which order would you like mark as complete? Enter the number: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int chosen_order = Integer.parseInt(reader.readLine());
		if(chosen_order <= currOrders.size())
		{
			DBNinja.CompleteOrder(currOrders.get(chosen_order - 1));
		}
		else
		{
			System.out.println("order:" + Integer.toString(currOrders.size()));	
			System.out.println("Incorrect entry, not an option");
		}
	
	}
	
	//See the list of inventory and it's current level
	public static void ViewInventoryLevels() throws SQLException, IOException
	{
		ArrayList<Topping> curInventory = DBNinja.getInventory();
		int t_count = 1;
		for(Topping t : curInventory)
		{
			System.out.println(Integer.toString(t_count) + ": " + t.getName() + " Level: " + Double.toString(t.getInv()));
			t_count++;
		}
		
	}
	
	//Select an inventory item and add more to the inventory level to re-stock the inventory
	public static void AddInventory() throws SQLException, IOException
	{
		ArrayList<Topping> curInventory = DBNinja.getInventory();
		int t_count = 1;
		//see all toppings
		for (Topping t : curInventory)
		{
			System.out.println(Integer.toString(t_count) + ": " + t.getName() + " Level: " + Double.toString(t.getInv()));
			t_count ++;
		}
		
		//select a topping to add inventory to
		System.out.println("Which topping do you want to add inventory to? Enter the number: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int chosen_t = Integer.parseInt(reader.readLine());
		if(chosen_t <= curInventory.size())
		{
			System.out.println("How many units would you like to add? ");
			double add = Double.parseDouble(reader.readLine());
			DBNinja.connect_to_db();
			DBNinja.AddToInventory(curInventory.get(chosen_t - 1), add);
		}
		else
		{
			System.out.println("Incorrect entry, not an option");
		}
	}
	
	//A function that builds a pizza. Used in our add new order function
	public static Pizza buildPizza() throws SQLException, IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		//select size
		System.out.println("What size is the pizza? \n1.) Small \n2.) Medium\n3.) Large\n4.) X-Large \n Enter the corresponding number: ");
		int size_option = Integer.parseInt(reader.readLine());
		String size = "";
		if(size_option == 1)
		{
			size = DBNinja.size_s;
		}
		else if(size_option == 2)
		{
			size = DBNinja.size_m;
		}
		else if(size_option == 3)
		{
			size = DBNinja.size_l;
		}
		else
		{
			size = DBNinja.size_xl;
		}
		
		//select crust
		System.out.println("What crust for this pizza? \n1.) Thin \n2.) Original\n3.) Pan\n4.) Gluten-Free \n Enter the corresponding number: ");
		int c_option = Integer.parseInt(reader.readLine());
		String crust = "";
		if(c_option == 1)
		{
			crust = DBNinja.crust_thin;
		}
		else if(c_option == 2)
		{
			crust = DBNinja.crust_orig;
		}
		else if(c_option == 3)
		{
			crust = DBNinja.crust_pan;
		}
		else
		{
			crust = DBNinja.crust_gf;
		}
		
		//get the base price
		double base_price = DBNinja.getBasePrice(size, crust);
		
		Pizza newPizza = new Pizza(-1, size, crust, base_price);
		
		//add toppings to the pizza
		int chosen_t = 0;
		ArrayList<Topping> curInventory = DBNinja.getInventory();
		while(chosen_t != -1)
		{
			
			int t_count = 1;
			for(Topping t : curInventory)
			{
				System.out.println(Integer.toString(t_count) + ": " + t.getName() + " Level: " + Double.toString(t.getInv()));
				t_count++;
			}	
		
			System.out.println("Which topping do you want to add? Enter the number. Enter -1 to stop adding toppings: ");
			
			chosen_t = Integer.parseInt(reader.readLine());
			if (chosen_t != -1)
			{
				if(chosen_t <= curInventory.size())
				{
					//make copy to avoid aliasing issues
					Topping newT = new Topping(curInventory.get(chosen_t - 1).getName(), curInventory.get(chosen_t - 1).getPrice(), curInventory.get(chosen_t - 1).getInv(), curInventory.get(chosen_t - 1).getID());
					System.out.println("Would you like to add extra of this topping? Enter Y for yes: ");
					String yn = reader.readLine();
					if(yn.equals("Y") || yn.equals("y"))
					{
						newT.makeExtra();
					}
					newPizza.addTopping(newT);
				}
				else
				{
					System.out.println("Incorrect entry, not an option");
				}
			}
		}
		
		//add discounts that apply to the pizza
		System.out.println("Should any discounts be added for this pizza? Enter Y or N: ");
		String yn = reader.readLine();
		if(yn.equals("Y") || yn.equals("y"))
		{
			// add discounts
			int chosen_d = 0;
			ArrayList<Discount> discs = DBNinja.getDiscountList();
			while(chosen_d != -1)
			{
				int d_count = 1;
				for (Discount d: discs)
				{
					System.out.println(Integer.toString(d_count) + ".) " + d.toPrint());
					d_count++;
				}
				
				System.out.println("Which discount do you want to add? Enter the number. Enter -1 to stop adding discounts: ");
			
				chosen_d = Integer.parseInt(reader.readLine());
				if (chosen_d != -1)
				{
					if(chosen_d <= discs.size())
					{
						//make copy to avoid aliasing issues
						Discount newD = new Discount(discs.get(chosen_d-1).getName(), discs.get(chosen_d-1).getPercentDisc(), discs.get(chosen_d-1).getCashDisc(), discs.get(chosen_d-1).getID());
						newPizza.addDiscount(newD);
					}
					else
					{
						System.out.println("Incorrect entry, not an option");
					}
				}
			}
			
		}
		
		return newPizza;
		
	}
	
	
}