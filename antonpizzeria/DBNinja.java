package antonpizzeria;

import java.io.*; 
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import jdk.nashorn.internal.ir.BreakableNode;
import sun.util.calendar.LocalGregorianCalendar.Date;

/*
HELP https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html

This file is where most of your code changes will occur
You will write the code to retrieve information from the database, 
or save information to the database

The class has several hard coded static variables used for the connection,
 you will need to change those to your connection information

This class also has static string variables for pickup,
 delivery and dine-in. If your database stores the strings differently 
 (i.e "pick-up" vs "pickup") changing these static variables will ensure that
  the comparison is checking for the right string in other places in the program. 
  You will also need to use these strings if you store this as boolean fields 
  or an integer.


*/



public final class DBNinja{
	//enter your user name here
	private static String user = "mfergu3";
	//enter your password here
	private static String password = "RedHotDatabase$$";
	//enter your database name here
	private static String database_name = "mySQL01";
	//Do not change the port. 3306 is the default MySQL port
	private static String port = "3306";
	private static Connection conn;
	
	//Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
	public static String pickup = "pickup";
	public static String delivery = "delivery";
	public static String dine_in = "dine-in";
	
	public static String size_s = "Small";
	public static String size_m = "Medium";
	public static String size_l = "Large";
	public static String size_xl = "X-Large";
	
	public static String crust_thin = "Thin";
	public static String crust_orig = "Original";
	public static String crust_pan = "Pan";
	public static String crust_gf = "Gluten-Free";
	
	//This function will handle the connection to the database
	public static boolean connect_to_db() throws SQLException, IOException
	{
		try 
		{
      		Class.forName("com.mysql.jdbc.Driver");
    	} catch (ClassNotFoundException e) {
        	System.out.println ("Could not load the driver");
			
          		System.out.println("Message     : " + e.getMessage());
          		
        
        	return false;
      	}
		
		conn = DriverManager.getConnection("jdbc:mysql://mysql1.cs.clemson.edu:"+port+"/"+database_name, user, password);
		return true;
	}
	
	public static void addOrder(Order o) throws SQLException, IOException
	{
		connect_to_db();
		/* add code to add the order to the DB. Remember to add the pizzas and
		 discounts as well, which will involve multiple tables. Customer should 
		 already exist. Toppings will need to be added to the pizzas.
		
		It may be beneficial to define more functions to add an individual pizza
		 to a database, add a topping to a pizza, etc.
		
		Note: the order ID will be -1 and will need to be replaced to be a fitting
		 primary key.

		You will also need to add timestamps to your pizzas/orders in your database.
		 Those timestamps are not stored in this program, but you can get the
		  current time before inserting into the database
		
		Remember, when a new order comes in the ingredient levels for 
		the topping need to be adjusted accordingly. Remember to check for
		 "extra" of a topping here as well.
		
		You do not need to check to see if you have the topping in stock before
		 adding to a pizza. You can just let it go negative.
		*/
		addorder(o);
		adddiscounts(o);

		
		
		
		conn.close();
		
	}

	private static void adddiscounts(Order o){

		//add code to add order discounts to DISCOUNT_ORDERS table
	}
	private static void addpizzas(Order o, int order_id){
		for(Pizza p : o.getPizzas()){
			addpizza(p, order_id);
		}
	}

	private static void addpizza(Pizza p, int order_id){

		try{
			String sz = p.getSize();
			if(sz.equals("Small")){
				sz = "small";
			}else if(sz.equals("Medium")){
				sz = "medium";
			}

			PreparedStatement get_base = conn.prepareStatement("SELECT Base_id FROM BASE_PRICES WHERE Size_=? AND Crust=?;");
			get_base.setString(1, sz);
			get_base.setString(2, p.getCrust());
			ResultSet rset = get_base.executeQuery();
			rset.first();
			PreparedStatement add_pizza = conn.prepareStatement("INSERT INTO PIZZAS(Time_ord, Order_id, Base_type) VALUES( ?, ?, ?);");
			java.util.Date now = new java.util.Date();
			add_pizza.setString(1, now.toString() );
			add_pizza.setInt(2, order_id);
			add_pizza.setInt(3, rset.getInt(1));
			add_pizza.executeUpdate();
			Statement stmt = conn.createStatement();
			String get_new_pizzaid = " SELECT Pizza_id FROM PIZZAS WHERE Pizza_id >= ALL( SELECT Pizza_id FROM PIZZAS);";
			rset = stmt.executeQuery(get_new_pizzaid);
			rset.first();
			addtoppings(rset.getInt(1), p);
		}  catch (SQLException e) {
        	System.out.println("Error adding pizza");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
		}
	}

	private static void addtoppings(int pizza_id, Pizza p){

		try{
			for(Topping t : p.getToppings()){
				PreparedStatement add_topping = conn.prepareStatement("INSERT INTO PIZZA_TOPPINGS VALUES (?,?,?);");
				add_topping.setInt(1, pizza_id );
				add_topping.setInt(2, t.getID());
				char extra = t.getExtra()? 'Y':'N';
				add_topping.setString(3, String.valueOf(extra));
				add_topping.executeUpdate();
				PreparedStatement get_amt_red = conn.prepareStatement("SELECT Sml_amt, Med_amt, Lrg_amt, Xlrg_amt FROM TOPPINGS WHERE Topping_id = ?;");
				get_amt_red.setInt(1,t.getID());
				ResultSet rset = get_amt_red.executeQuery();
				rset.first();
				double remove_me = 0;
				if(p.getSize() == DBNinja.size_xl){
					remove_me = rset.getDouble(4);
				}else if( p.getSize() == DBNinja.size_l){
					remove_me = rset.getDouble(3);
				}else if(p.getSize() == DBNinja.size_m){
					remove_me = rset.getDouble(2);
				}else{
					remove_me = rset.getDouble(1);
				}
				AddToInventory(t, remove_me);
				//add code to update inv amount
			}
		}  catch (SQLException e) {
        	System.out.println("Error adding toppings");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
		}   catch (IOException e) {
        	System.out.println("Error adding toppings");
			System.out.println("Message     : " + e.getMessage());
		}


	}
	private static void addorder(Order o){
		int table_num = o.getTable();
		String deliv = o.getType();
		char c = 'T';
		if(deliv.equals(DBNinja.pickup)){
			c='P';
			table_num = 0;
		} else if (deliv.equals(DBNinja.delivery)){
			c = 'D';
			table_num = 0;
		} else if ( deliv.equals(DBNinja.dine_in)){
			c = 'T';
		}
		Customer cust = o.getCustomer();
		int cust_id = cust.getID();
		try{
			PreparedStatement add_ord = conn.prepareStatement("INSERT INTO ORDERS(Table_num, Cust_id, Delivery_method) VALUES(?,?,?);");
			add_ord.setInt(1, table_num);
			add_ord.setInt(2, cust_id);
			add_ord.setString(3, String.valueOf(c));
			add_ord.executeUpdate();

			String get_new_ord_id = " SELECT Order_id FROM ORDERS WHERE Order_id >= ALL( SELECT Order_id FROM ORDERS);";
			Statement get_real_id = conn.createStatement();
			ResultSet rset = get_real_id.executeQuery(get_new_ord_id);
			rset.first();

			addpizzas(o, rset.getInt(1) );

		} catch (SQLException e) {
        	System.out.println("Error adding order");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
		}

	}
	
	public static void addCustomer(Customer c) throws SQLException, IOException
	{
		connect_to_db();
		/*add code to add the customer to the DB.
		Note: the ID will be -1 and will need to be replaced to be a fitting
		 primary key*/

		/* name = n;
		address = a;
		phone = p;
		ID = i; */
		String nm = c.getName();
		String a = c.getAddress();
		String p = c.getPhone();
		PreparedStatement pstmt = conn.prepareStatement(
			"INSERT INTO CUSTOMER (name, Address, phone) VALUES ( ?, ?, ?);");

		pstmt.setString(1, nm);
		pstmt.setString(2, a);
		pstmt.setString(3, p);
		/*
		String updateString = "INSERT INTO " + database_name +
			".CUSTOMER (name, Address, phone) VALUES ("" + nm +
			"", "" + a + "", "" + p + "");";
		*/
		Statement stmt = null;
		try{
			
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
        	System.out.println("Error adding Customer");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
			conn.close();
		}
		conn.close();
	}
	
	public static void CompleteOrder(Order o) throws SQLException, IOException
	{
		connect_to_db();
		/*add code to mark an order as complete in the DB. You may have a
		 boolean field for this, or maybe a completed time timestamp. However 
		 you have it, */
		int id = o.getID();
		String time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		PreparedStatement pstmt = conn.prepareStatement(
			"UPDATE mySQL01.PIZZAS SET Time_comp = ? WHERE Order_id = ?;");

		pstmt.setString(1, time);
		pstmt.setInt(2, id);
		try{
			pstmt.executeUpdate();
		}
		catch (SQLException e) {
        	System.out.println("Error completing Order");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
			conn.close();
		}

		System.out.println("Here is your order: ");
		System.out.println(o.toFullPrint());
		conn.close();
	}
	
	public static void AddToInventory(Topping t, double toAdd) throws SQLException, IOException
	{
		/*add code to add toAdd to the inventory level of T. This is not
		 adding a new topping, it is adding a certain amount of stock for a topping.
		  This would be used to show that an order was made to replenish the
		   restaurants supply of pepperoni, etc*/
		   int top_id = t.getID();
		   PreparedStatement to_update_inv = conn.prepareStatement("UPDATE mySQL01.TOPPINGS SET Current_inv = Current_inv + ?;");
		   to_update_inv.setDouble(1, toAdd);
		   to_update_inv.executeUpdate();
	}
	

	/*
		A function to get the list of toppings and their inventory levels. 
		I have left this code "complete" as an example of how to use JDBC to
		 get data from the database. This query will not work on your database if
		  you have different field or table names, so it will need to be changed
		
		Also note, this is just getting the topping ids and then calling getTopping()
		 to get the actual topping. You will need to complete this on your own
		
		You don't actually have to use and write the getTopping() function,
		 but it can save some repeated code if the program were to expand,
		  and it keeps the functions simpler, more elegant and easy to read.
		   Breaking up the queries this way also keeps them simpler. I think it's a
		    better way to do it, and many people in the industry would agree,
			 but its a suggestion, not a requirement.
	*/
	public static ArrayList<Topping> getInventory() throws SQLException, IOException
	{
		//start by connecting
		connect_to_db();
		ArrayList<Topping> ts = new ArrayList<Topping>();
		//create a string with out query, this one is an easy one
		String query = "SELECT Topping_id From mySQL01.TOPPINGS;";
		
		Statement stmt = conn.createStatement(); 
		try {
      			ResultSet rset = stmt.executeQuery(query);
				//even if you only have one result, you still need to call ResultSet.next() to load the first tuple
				while(rset.next())
				{
					/*Use getInt, getDouble, getString to get the actual value.
					 You can use the column number starting with 1, or use the 
					 column name as a string
					
					NOTE: You want to use rset.getInt() instead of
					 Integer.parseInt(rset.getString()), not just because it's shorter,
					  but because of the possible NULL values. A NUll would cause
					   parseInt to fail
					
					If there is a possibility that it could return a NULL value you need to
					 check to see if it was NULL. In this query we won't get nulls,
					  so I didn't. If I was going to I would do:
					
					if(rset.wasNull())
					{
						//set ID to what it should be for NULL, and whatever you need to do.
					}
					
					NOTE: you can't check for NULL until after you have read the 
					value using one of the getters.
					
					*/
					//Now I'm just passing my primary key to this function to get the topping itself individually
					int ID = rset.getInt(1);
					ts.add(getTopping(ID));
				}
		}
		catch (SQLException e) {
        	System.out.println("Error loading inventory");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
			//don't leave your connection open!
			conn.close();
			return ts;
      	}
		
		
		//end by closing the connection
		conn.close();
		return ts;
	}
	
	public static ArrayList<Order> getCurrentOrders() throws SQLException, IOException
	{
		connect_to_db();
		
		ArrayList<Order> os = new ArrayList<Order>();
		
		Statement stmt = conn.createStatement(); 
		String query = 
			"SELECT * FROM mySQL01.ORDERS JOIN PIZZAS ON ORDERS.Order_id = PIZZAS.Order_id WHERE Time_comp IS NULL GROUP BY ORDERS.Order_id; ";
	
		try {
      			ResultSet rset = stmt.executeQuery(query);
				while(rset.next())
				{
					if(rset.wasNull()){
						//set ID to what it should be for NULL, and whatever you need to do.
					}
					int order_id = rset.getInt(1);
					Customer curr_cust = getCustomer(order_id);
					//System.out.println("custm:" + curr_cust.getName());
					int table_num = rset.getInt(2);
					String order_type = rset.getString(4);
					ArrayList<Pizza> pizzas_ordered = getPizzas(order_id);
					ArrayList<Discount> order_discounts = getDiscounts(order_id, "order");

					Order temp = new Order(order_id, curr_cust,table_num,order_type);
					for(Pizza t : pizzas_ordered){
						temp.addPizza(t);
					}
					for(Discount d : order_discounts){
						temp.addDiscount(d);
					}
					os.add(temp);
					int ID = rset.getInt(1);
					//Now I'm just passing my primary key to this function to get the topping itself individually
					//os.add(getTopping(ID));
				}
				return os;
		}
		catch (SQLException e) {
        	System.out.println("Error loading inventory");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
			//don't leave your connection open!
			conn.close();
			return os;
      	}
		
	}
	
	public static double getBasePrice(String size, String crust) throws SQLException, IOException
	{
		connect_to_db();
		double bp;
		String sz;
		if(size.equals("Small")){
				sz = "small";
		}else if(size.equals("Medium")){
				sz = "medium";
		}else {
			sz = size;
		}
		try{
			PreparedStatement get_base_price = conn.prepareStatement("SELECT * FROM mySQL01.BASE_PRICES WHERE Size_=? AND Crust = ?;");
			get_base_price.setString(1, sz);
			get_base_price.setString(2, crust);
			ResultSet rset = get_base_price.executeQuery();
			rset.first();
			bp = rset.getDouble("Price");
			return bp;
		} catch (SQLException e) {
        	System.out.println("Error getting BasePrice");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
      	}
		conn.close();
		return bp=0.0;
	}
	
	public static ArrayList<Discount> getDiscountList() throws SQLException, IOException
	{
		ArrayList<Discount> discs = new ArrayList<Discount>();
		connect_to_db();
		//add code to get a list of all discounts
		try{
			PreparedStatement all_disc = conn.prepareStatement("SELECT Discount_id FROM mySQL01.DISCOUNTS;");
			ResultSet rset = all_disc.executeQuery();
			while(rset.next()){
				Discount D = getDiscount(rset.getInt(1));
				discs.add(D);
			}
			return discs;
		} catch (SQLException e) {
        	System.out.println("Error getting Pizzas");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
      	}
		conn.close();
		return discs;
	}
	
	public static ArrayList<Customer> getCustomerList() throws SQLException, IOException
	{
		ArrayList<Customer> custs = new ArrayList<Customer>();
		connect_to_db();
		//add code to get a list of all customers
		try{
			PreparedStatement add_custs = conn.prepareStatement("SELECT * FROM mySQL01.CUSTOMER;");
			ResultSet rset = add_custs.executeQuery();
			while(rset.next()){
				String name = rset.getString("name");
				if(rset.wasNull()){
					name = "";
				}
				String add = rset.getString("Address");
				if(rset.wasNull()){
					add = "";
				}
				String phone = rset.getString("phone");
				if(rset.wasNull()){
					phone = "";
				}
				int cust_id = rset.getInt("Cust_id");
				Customer c = new Customer(name, add, phone, cust_id);
				custs.add(c);
			}
		} catch (SQLException e) {
        	System.out.println("Error getting Pizzas");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
      	}
		conn.close();
		return custs;
	}
	
	private static ArrayList<Pizza> getPizzas(int order_id) throws SQLException, IOException 
	{

		ArrayList<Pizza> unfinished_pizzas = new ArrayList<Pizza>();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM mySQL01.PIZZAS WHERE Order_id=?;");
		pstmt.setInt(1,order_id);
		try{
			ResultSet rset = pstmt.executeQuery();
			while(rset.next()) {
				Pizza P = getPizza(rset.getInt("Pizza_id"));
				unfinished_pizzas.add(P);
			}
		}catch (SQLException e) {
        	System.out.println("Error getting Pizzas");
        	while (e != null) {
          		System.out.println("Message     : " + e.getMessage());
          		e = e.getNextException();
        	}
        	
      	}
		return unfinished_pizzas;
	}

	private static ArrayList<Discount> getDiscounts(int pizza_or_order_id, String discount_type){
		ArrayList<Discount> discounts = new ArrayList<Discount>();
		if(discount_type.equals("pizza")){
			try{
				PreparedStatement get_pizza_discounts = conn.prepareStatement("SELECT * FROM mySQL01.DISCOUNT_PIZZAS WHERE Pizza_id = ?;");
				get_pizza_discounts.setInt(1, pizza_or_order_id);
				ResultSet rset = get_pizza_discounts.executeQuery();
				while(rset.next()){
					Discount D = getDiscount(rset.getInt(1));
					discounts.add(D);
				}
			}catch (SQLException e) {
				System.out.println("Error getting Discounts");
				while (e != null) {
					System.out.println("Message     : " + e.getMessage());
					e = e.getNextException();
				}
				return discounts;
	      	}catch (IOException e) {
				System.out.println("Error getting Discounts");
				System.out.println("Message     : " + e.getMessage());
			}
			return discounts;
		} else {
			try{
				PreparedStatement get_order_discounts = conn.prepareStatement("SELECT * FROM mySQL01.DISCOUNT_ORDERS WHERE Order_id = ?;");
				get_order_discounts.setInt(1, pizza_or_order_id);
				ResultSet rset = get_order_discounts.executeQuery();
				while(rset.next()){
					/* str name, dub percent off, dub cash off, int id */
					Discount D = getDiscount(rset.getInt(2));
					discounts.add(D);
				}
			}catch (SQLException e) {
				System.out.println("Error getting Discounts");
				while (e != null) {
					System.out.println("Message     : " + e.getMessage());
					e = e.getNextException();
				}
	      	}catch (IOException e) {
				System.out.println("Error getting Discounts");
				System.out.println("Message     : " + e.getMessage());
			}
		}
		return discounts;
	}

	private static ArrayList<Topping> getToppings(int pizza_id) throws SQLException, IOException
	{
		
		//add code to get a topping
		ArrayList<Topping> toppins = new ArrayList<Topping>();
		PreparedStatement get_pizza_toppings = conn.prepareStatement("SELECT * FROM mySQL01.PIZZA_TOPPINGS WHERE Pizza_id=?;");
		get_pizza_toppings.setInt(1, pizza_id);
		try{
			ResultSet rset = get_pizza_toppings.executeQuery();
			while(rset.next()){
				int top_id = rset.getInt(2);
				Topping T = getTopping(top_id);
				toppins.add(T);
			}
		}catch (SQLException e) {
			System.out.println("Error getting Toppings");
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
		}

	return toppins;
	}
	
	/*
	Note: The following incomplete functions are not strictly required,
	 but could make your DBNinja class much simpler. For instance, instead of writing
	  one query to get all of the information about an order, you can find the
	   primary key of the order, and use that to find the primary keys of the
	    pizzas on that order, then use the pizza primary keys individually to
		 build your pizzas. We are no longer trying to get everything in one query,
		  so feel free to break them up as much as possible
	
	You could also add functions that take in a Pizza object and add that to the
	 database, or take in a pizza id and a topping id and add that topping to the
	  pizza in the database, etc. I would recommend this to keep your
	   addOrder function much simpler
	
	These simpler functions should still not be called from our menu class.
	 That is why they are private

	We don't need to open and close the connection in these, since they are
	 only called by a function that has opened the connection and will close it after
	
	*/
	
	private static Topping getTopping(int top_id) throws SQLException, IOException
	{
		
		//add code to get a toppin
		Topping t;
		PreparedStatement get_toppings = conn.prepareStatement("SELECT * FROM mySQL01.TOPPINGS WHERE Topping_id = ?;");
		get_toppings.setInt(1, top_id);
		try{
			ResultSet rset = get_toppings.executeQuery();
			rset.next(); 
			
			String name = new String(rset.getString("name"));
			double price = rset.getDouble("Price");
			double inv = rset.getDouble("Current_inv");
			int id = top_id;
			t = new Topping(name, price, inv, id);
			return t;
		}catch (SQLException e) {
			System.out.println("Error getting Topping");
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
		}
		t = new Topping("a",0.0,0.0,0);
		return t;
		
	}
	
	private static Discount getDiscount(int discount_id)  throws SQLException, IOException
	{
		Discount D;
		try{
			PreparedStatement get_pizza_discounts = conn.prepareStatement("SELECT * FROM mySQL01.DISCOUNTS WHERE Discount_id = ?;");
			get_pizza_discounts.setInt(1, discount_id);
			ResultSet rset = get_pizza_discounts.executeQuery();
			rset.first();
			
			String name = new String(rset.getString(1));
			double perc_off = rset.getDouble(2);
			if(rset.wasNull()){
				perc_off = 0.0;
			}
			double cash_off = rset.getDouble(3);
			if(rset.wasNull()){
				cash_off = 0.0;
				perc_off = perc_off / 100;
			}
			D = new Discount(name, perc_off, cash_off, discount_id);
			return D;
		}catch (SQLException e) {
			System.out.println("Error getting Discount");
			System.out.println("discount id:"+ discount_id);
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
		}
		D = new Discount("a",0.0,0.0,0);
		return D;
	}
	
	private static Pizza getPizza(int pizza_id)  throws SQLException, IOException
	{
		Pizza P;
		try{
			PreparedStatement get_pizza = conn.prepareStatement("SELECT * FROM PIZZAS WHERE Pizza_id = ?;");
			get_pizza.setInt(1, pizza_id);
			ResultSet rset = get_pizza.executeQuery();
			rset.first();
			int new_pizza_id = rset.getInt("Pizza_id");
			String pizza_size = getSize(new_pizza_id);
			String pizza_crust = getCrust(new_pizza_id);
			double base_price = getBasePrice(pizza_size, pizza_crust);
			ArrayList<Topping> toppins = getToppings(new_pizza_id);
			ArrayList<Discount> pizza_discounts = getDiscounts(new_pizza_id, "pizza");

			P = new Pizza(new_pizza_id, pizza_size, pizza_crust, base_price);
			for(Topping t : toppins){
				P.addTopping(t);
			}
			for(Discount d : pizza_discounts){
				P.addDiscount(d);
			}
			return P;
		}catch (SQLException e) {
			System.out.println("Error getting Pizza");
			System.out.println("pizza id:"+ pizza_id);
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
		}
		
		P = new Pizza(0,"a","a",0.0);
		return P;
				
	}
	
	private static Customer getCustomer(int order_id)  throws SQLException, IOException
	{
		Customer C;	
		PreparedStatement query = conn.prepareStatement(" SELECT Cust_id FROM ORDERS WHERE Order_id = ?;");
		query.setInt(1, order_id);
		try{
			ResultSet rset = query.executeQuery();
			rset.next();
			int cust_id = rset.getInt("Cust_id");

			PreparedStatement get_cust = conn.prepareStatement("SELECT * FROM CUSTOMER WHERE Cust_id = ?;");
			get_cust.setInt(1, cust_id);
			rset = get_cust.executeQuery();
			rset.next();
			String name = rset.getString("name");
			String add = rset.getString("Address");
			if(rset.wasNull()) {
					add = "n/a";
			}
			String ph = rset.getString("phone");
			if(rset.wasNull()) {
					ph = "     n/a    ";
			}
			int cust_num_id = rset.getInt(4);	//add code to get customer
			C = new Customer(name, add, ph, cust_num_id);
			return C;

		}catch (SQLException e) {
			System.out.println("Error getting Customer");
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
		}
		C = new Customer("a","b","c",1);
		return C;
	}
	
	/*
	private static Order getOrder()  throws SQLException, IOException
	{
		try{
			Order O;
			PreparedStatement get_order = conn.prepareStatement();
		}		
		return O;
		
	}
	*/
	
	private static String getSize(int pizza_id) {

		try{
			PreparedStatement get_base_type = conn.prepareStatement(" SELECT Base_type FROM PIZZAS WHERE Pizza_id =?;");
			get_base_type.setInt(1, pizza_id);
			ResultSet rset = get_base_type.executeQuery();
			rset.next();
			int base_type = rset.getInt("Base_type");
			PreparedStatement get_size = conn.prepareStatement(" SELECT Size_ FROM BASE_PRICES WHERE Base_id =?;");
			get_size.setInt(1, base_type);
			rset = get_size.executeQuery();
			rset.next();
			String temp = rset.getString(1);	
			if(temp.equals("small")){
				return size_s;
			} else if( temp.equals("medium")){
				return size_m;
			} else if (temp.equals("Large")){
				return size_l;
			}
			return size_xl;
		}catch (SQLException e) {
			System.out.println("Error getting Size");
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
			return size_xl;
		}

	}
	private static String getCrust(int pizza_id) {

		try{
			PreparedStatement get_crust = conn.prepareStatement(" SELECT * FROM PIZZAS JOIN BASE_PRICES ON PIZZAS.Base_type = BASE_PRICES.Base_id WHERE Pizza_id = ?;");
			get_crust.setInt(1, pizza_id);
			ResultSet rset = get_crust.executeQuery();
			rset.first();
			String temp = rset.getString("Crust");
			if(temp.equals("Thin")){
				return crust_thin;
			}else if( temp.equals("Original")){
				return crust_orig;
			}else if( temp.equals("Pan")){
				return crust_pan;
			} else {
				return crust_gf;
			}
		}catch (SQLException e) {
			System.out.println("Error getting Crust");
			while (e != null) {
				System.out.println("Message     : " + e.getMessage());
				e = e.getNextException();
			}
			return crust_gf;
		}

	}
	
}
