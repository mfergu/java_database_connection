

default: Menu.java 
	javac -cp ./lib/mysql-connector-java-5.1.42/mysql-connector-java-5.1.42-bin.jar:. Menu.java 
	
run: Menu.class
	java -cp ./lib/mysql-connector-java-5.1.42/mysql-connector-java-5.1.42-bin.jar:. Menu